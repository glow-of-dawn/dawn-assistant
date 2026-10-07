package com.dawn.plugin.redis.config;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
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
@Configuration
@EnableCaching
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class RedisCacheAutoConfiguration {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory,
                                     RedisSerializer<String> redisStringSerializer,
                                     RedisSerializer<Object> redisJsonSerializer) {
        var redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisStringSerializer))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisJsonSerializer));

        return RedisCacheManager.builder(factory)
            .cacheDefaults(redisCacheConfiguration)
            .build();
    }

    /**
     * [RedisTemplate配置]
     *
     * @param lettuceConnectionFactory [LettuceConnectionFactory]
     * @return {@code RedisTemplate<String, Object>}
     */
    @Bean
    public RedisTemplate<String, Object> generateRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory,
                                                               RedisSerializer<String> redisStringSerializer,
                                                               RedisSerializer<Object> redisJsonSerializer) {
        log.trace(LogEnmu.LOG2.value(), "RedisTemplate<String, Object>", "初始化:RedisTemplate<String, Object>");
        /* 配置redisTemplate */
        var redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);

        /* key采用String的序列化方式 */
        redisTemplate.setKeySerializer(redisStringSerializer);
        /* hash的key也采用String的序列化方式 */
        redisTemplate.setHashKeySerializer(redisStringSerializer);
        /* value序列化方式采用jackson */
        redisTemplate.setValueSerializer(redisJsonSerializer);
        /* hash的value序列化方式采用jackson */
        redisTemplate.setHashValueSerializer(redisJsonSerializer);

        redisTemplate.setDefaultSerializer(redisJsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Serializable> cacheRedisTemplate(LettuceConnectionFactory redisConnectionFactory,
                                                                  RedisSerializer<String> redisStringSerializer,
                                                                  RedisSerializer<Object> redisJsonSerializer) {
        var template = new RedisTemplate<String, Serializable>();
        template.setKeySerializer(redisStringSerializer);
        template.setHashKeySerializer(redisStringSerializer);
        template.setValueSerializer(redisJsonSerializer);
        template.setHashValueSerializer(redisJsonSerializer);
        template.setDefaultSerializer(redisJsonSerializer);
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
    public ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory,
                                                                       RedisSerializer<String> redisStringSerializer,
                                                                       RedisSerializer<Object> redisJsonSerializer) {
        log.trace(LogEnmu.LOG1.value(), "初始化:ReactiveRedisTemplate<Object, Object>");
        var serializationContext = RedisSerializationContext
            .newSerializationContext(redisJsonSerializer)
            /* [builder.value(jackson2JsonRedisSerializer);] */
            .value(redisJsonSerializer)
            .hashKey(redisStringSerializer)
            /* [builder.hashValue(jackson2JsonRedisSerializer);] */
            .hashValue(redisJsonSerializer)
            .string(redisStringSerializer)
            .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    /**
     * 创建一个 ReactiveRedisTemplate 实例，用于与 Redis 进行异步的、响应式的交互。
     *
     * @param connectionFactory     Redis 连接工厂
     * @param redisStringSerializer Redis 字符串序列化器
     * @return ReactiveRedisTemplate 实例
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(ReactiveRedisConnectionFactory connectionFactory,
                                                                             RedisSerializer<String> redisStringSerializer) {
        log.trace(LogEnmu.LOG1.value(), "初始化:ReactiveRedisTemplate<String, String>");
        var serializationContext = RedisSerializationContext
            .<String, String>newSerializationContext(redisStringSerializer)
            .value(redisStringSerializer)
            .hashKey(redisStringSerializer)
            .hashValue(redisStringSerializer)
            .string(redisStringSerializer)
            .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    @Bean
    public RedisSerializer<Object> redisJsonSerializer(PluginConfig config) {
        return new JacksonJsonRedisSerializer<>(config.getMapperLowerCamel(), Object.class);
    }

    @Bean
    public RedisSerializer<String> redisStringSerializer() {
        return new StringRedisSerializer();
    }

    /**
     * 提供 Caffeine 构造器（可通过 spring.cache.caffeine.spec 配置覆盖），
     * 并作为在没有 Redis 的场景下的 CacheManager 回退来源。
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
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
