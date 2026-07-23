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

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 参数加载
 * 创建时间 2025/9/11 16:36
 *
 * @author hforest-480s
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
        String lastKey = redisHeader.concat(name).concat(VarEnmu.QUOTE.value()).concat(key);
        AtomicReference<String> atomVal = new AtomicReference<>(VarEnmu.NONE.value());
        Optional.ofNullable(redisTemplate.opsForValue().get(lastKey))
            .ifPresentOrElse(value -> atomVal.set(value.toString()),
                () -> {
                    /* 参数提取 */
                    Optional.ofNullable(tabParamsMapper.findByClassAndNameAndKey(springApplicationName, name, key))
                        .ifPresentOrElse(tabParams -> atomVal.set(propDecry(tabParams.getParamsValue())),
                            () -> atomVal.set(devVal));
                    redisTemplate.opsForValue().set(lastKey, atomVal.get(), Duration.ofSeconds(redisKeyService.getRedisShot5mExpires()));
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

    /**
     * [配置信息-解密处理]
     *
     * @param propValue [propValue]
     * @return String
     */
    @SneakyThrows
    public String propDecry(String propValue) {
        String headName = "BEE_ENC_COMMON_";
        log.debug(LogEnmu.LOG3.value(), "propertySource", headName, propValue);
        if (StringUtils.isBlank(propValue)
            || propValue.length() < VarEnmu.TWELVE.ivalue()
            || propValue.indexOf(headName) == VarEnmu.IIT_MINUS_ONE.ivalue()) {
            return propValue;
        } else {
            var encryVal = propValue.replace(headName, VarEnmu.NONE.value());
            return new BeeSm4EcbEncryptorCustomer().decrypt(encryVal);
        }
    }

}
