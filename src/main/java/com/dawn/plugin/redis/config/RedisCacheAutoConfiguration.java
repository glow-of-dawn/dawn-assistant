package com.dawn.plugin.redis.config;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 创建时间：2024/5/24 14:45
 *
 * @author hforest-480s
 * 开启缓存支持
 * @EnableCaching
 */
@Slf4j
@DependsOn("pluginConfigurableEnvironment")
@Configuration
@EnableCaching
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class RedisCacheAutoConfiguration {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(this.getObjectMapper(), Object.class);
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
        RedisCacheManager cacheManager = RedisCacheManager.builder(factory).cacheDefaults(redisCacheConfiguration).build();
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
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(this.getObjectMapper(), Object.class);
        /* 配置redisTemplate */
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        RedisSerializer<?> stringSerializer = new StringRedisSerializer();
        /* key采用String的序列化方式 */
        redisTemplate.setKeySerializer(stringSerializer);
        /* hash的key也采用String的序列化方式 */
        redisTemplate.setHashKeySerializer(stringSerializer);
        /* value序列化方式采用jackson */
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        /* hash的value序列化方式采用jackson */
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        log.info(LogEnmu.LOG3.value(), "redis.redisTemplate", "setKeySerializer/setHashKeySerializer:stringSerializer",
                "setValueSerializer/setHashValueSerializer:jackson2JsonRedisSerializer");
        /* 默认序列号 */
        redisTemplate.setDefaultSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Serializable> redisCacheTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    public ObjectMapper getObjectMapper() {
        JsonMapper jsonMapper = JsonMapper
                .builder()
                .disable(MapperFeature.USE_ANNOTATIONS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
        jsonMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jsonMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY
        );
        jsonMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.ALWAYS));
        return jsonMapper;
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
        RedisSerializationContext.SerializationPair<String> stringSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer.UTF_8);
        /* 设置序列化 */
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(this.getObjectMapper(), Object.class);
        Jackson2JsonRedisSerializer<String> jackson2JsonRedisSerializerStr = new Jackson2JsonRedisSerializer<>(this.getObjectMapper(), String.class);
        RedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(this.getObjectMapper(), Object.class);
        var builder = RedisSerializationContext.newSerializationContext();
        builder.key(serializer);
        /* [builder.value(jackson2JsonRedisSerializer);] */
        builder.value(jackson2JsonRedisSerializer);
        builder.hashKey(stringSerializationPair);
        /* [builder.hashValue(jackson2JsonRedisSerializer);] */
        builder.hashValue(jackson2JsonRedisSerializer);
        /* [builder.string(jackson2JsonRedisSerializer);] */
        builder.string(jackson2JsonRedisSerializerStr);

        return new ReactiveRedisTemplate<>(connectionFactory, builder.build());
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
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




