package com.dawn.plugin.redis.config;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 创建时间：2024/5/24 14:45
 *
 * @author hforest-480s
 */
@Slf4j
@DependsOn("pluginConfigurableEnvironment")
@Configuration
@EnableCaching
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class RedisCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        var keySerializer = new StringRedisSerializer();
        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();

        var redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        var cacheManager = RedisCacheManager.builder(factory)
            .cacheDefaults(redisCacheConfiguration)
            .build();

        cacheManager.setTransactionAware(false);
        return cacheManager;
    }

    /**
     * [RedisTemplate配置]
     *
     * @param lettuceConnectionFactory [LettuceConnectionFactory]
     * @return RedisTemplate<String, Object>
     */
    @Bean
    public RedisTemplate<String, Object> getLettuceRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        log.info(LogEnmu.LOG2.value(), "redis", "初始化:LettuceRedisTemplate");
        /* 设置序列化 */
        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();
        /* 配置redisTemplate */
        var redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);

        var stringSerializer = new StringRedisSerializer();
        /* key采用String的序列化方式 */
        redisTemplate.setKeySerializer(stringSerializer);
        /* hash的key也采用String的序列化方式 */
        redisTemplate.setHashKeySerializer(stringSerializer);
        /* value序列化方式采用jackson */
        redisTemplate.setValueSerializer(jsonSerializer);
        /* hash的value序列化方式采用jackson */
        redisTemplate.setHashValueSerializer(jsonSerializer);

        log.info(LogEnmu.LOG3.value(), "redis.redisTemplate",
            "setKeySerializer/setHashKeySerializer:stringSerializer",
            "setValueSerializer/setHashValueSerializer:genericJacksonSerializer");

        redisTemplate.setDefaultSerializer(jsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Serializable> redisCacheTemplate(LettuceConnectionFactory redisConnectionFactory) {
        var template = new RedisTemplate<String, Serializable>();
        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * 应用启动后，Spring会自动生成ReactiveRedisTemplate（它的底层框架是Lettuce）
     * ReactiveRedisTemplate
     * ReactiveRedisTemplate与RedisTemplate使用类似，但它提供的是异步的，响应式Redis交互方式。
     * 这里再强调一下，响应式编程是异步的，ReactiveRedisTemplate发送Redis请求后不会阻塞线程，当前线程可以去执行其他任务。
     * 等到Redis响应数据返回后，ReactiveRedisTemplate再调度线程处理响应数据。
     * 响应式编程可以通过优雅的方式实现异步调用以及处理异步结果，正是它的最大的意义。
     **/
    @Bean
    public ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        log.info(LogEnmu.LOG2.value(), "redis", "初始化:reactiveRedisTemplate");

        var stringSerializer = new StringRedisSerializer();
        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();

        var builder = RedisSerializationContext.newSerializationContext(stringSerializer);
        /* [builder.value(jackson2JsonRedisSerializer);] */
        builder.value(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));
        builder.hashKey(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer));
        /* [builder.hashValue(jackson2JsonRedisSerializer);] */
        builder.hashValue(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));
        /* [builder.string(jackson2JsonRedisSerializer);] */
        builder.string(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer));

        return new ReactiveRedisTemplate<>(connectionFactory, builder.build());
    }

    /**
     * 提供 Caffeine 构造器（可通过 spring.cache.caffeine.spec 配置覆盖），
     * 并作为在没有 Redis 的场景下的 CacheManager 回退来源。
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig(CacheProperties cacheProperties) {
        /* 如果 application.properties/yml 中配置了 spring.cache.caffeine.spec，则优先使用它 */
        String spec = null;
        if (cacheProperties != null && cacheProperties.getCaffeine() != null) {
            spec = cacheProperties.getCaffeine().getSpec();
        }
        if (spec != null && !spec.isEmpty()) {
            return Caffeine.from(CaffeineSpec.parse(spec));
        }

        /* 否则使用默认程序化配置 */
        return Caffeine.newBuilder()
            /* 最大缓存条目数 */
            .maximumSize(VarEnmu.NUMBER_1000.ivalue())
            /* 写入后10分钟过期 */
            .expireAfterWrite(VarEnmu.TEN.ivalue(), TimeUnit.MINUTES)
            /* 最后一次访问后5分钟过期 */
            .expireAfterAccess(VarEnmu.FIVE.ivalue(), TimeUnit.MINUTES)
            /* 启用统计信息 */
            .recordStats();
    }

}
