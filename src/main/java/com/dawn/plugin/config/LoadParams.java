package com.dawn.plugin.config;


import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabParams;
import com.dawn.plugin.mapper.ccore.TabParamsMapper;
import com.dawn.plugin.redis.primary.RedisKeyService;
import jakarta.annotation.Nonnull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 参数加载
 * 创建时间 2025/9/11 16:36
 *
 * @author bhyt2
 */
@Configuration
@ConditionalOnProperty(name = {"plugin-status.load-params-status"}, havingValue = "enable", matchIfMissing = true)
public class LoadParams {

    @Value("${spring.application.name}")
    private String springApplicationName;
    /* redis前缀 */
    @Value("#{'${spring.application.name}:'}")
    private String redisHeader;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TabParamsMapper tabParamsMapper;
    private final RedisKeyService redisKeyService;

    public LoadParams(TabParamsMapper tabParamsMapper,
                      RedisKeyService redisKeyService,
                      final RedisTemplate<String, Object> redisTemplate) {
        this.tabParamsMapper = tabParamsMapper;
        this.redisKeyService = redisKeyService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * [从数据库获取参数信息]
     *
     * @param name [name]
     * @param key  [key]
     * @return String
     **/
    public String loadKey(@Nonnull String name, @Nonnull String key) {
        return this.loadKey(name, key, VarEnmu.NONE.value());
    }

    /**
     * [从数据库获取参数信息]
     *
     * @param name [name]
     * @param key  [key]
     * @return String
     **/
    @SneakyThrows
    public String loadKey(@Nonnull String name, @Nonnull String key, String devVal) {
        String lastKey = redisHeader.concat(name).concat(key);
        AtomicReference<String> atomVal = new AtomicReference<>(VarEnmu.NONE.value());
        Optional.ofNullable(redisTemplate.opsForValue().get(lastKey))
                .ifPresentOrElse(value -> atomVal.set(value.toString()),
                        () -> {
                            /* 参数提取 */
                            Optional.ofNullable(tabParamsMapper.findByClassAndNameAndKey(springApplicationName, name, key))
                                    .ifPresentOrElse(tabParams -> atomVal.set(tabParams.getParamsValue()),
                                            () -> atomVal.set(devVal));
                            redisTemplate.opsForValue().set(lastKey, atomVal.get(), redisKeyService.getRedisShot5mExpires(), TimeUnit.SECONDS);
                        });
        return atomVal.get();
    }

    /**
     * [从数据库获取参数信息]
     *
     * @param name [name]
     * @return Map<String, String>
     **/
    public Map<String, String> loadKeys(@Nonnull String name) {
        var tabParams = tabParamsMapper.findByClassAndName(springApplicationName, name);
        return tabParams.stream()
                .collect(Collectors.toMap(TabParams::getParamsKey, TabParams::getParamsValue));
    }

}
