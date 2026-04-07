package com.dawn.plugin.redis.primary.impl;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabParams;
import com.dawn.plugin.mapper.ccore.TabParamsMapper;
import com.dawn.plugin.redis.primary.AbstractRedisKeyService;
import com.dawn.plugin.redis.primary.KeyService;
import com.dawn.plugin.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 依赖于redis的键值获取
 * 创建时间：2025/7/3 15:21
 *
 * @author hforest-480s
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class RedisKeyServiceImpl extends AbstractRedisKeyService implements KeyService {

    public RedisKeyServiceImpl(final PluginConfig config,
                               final TabParamsMapper tabParamsMapper,
                               final RedisTemplate<String, Object> redisTemplate) {
        this.config = config;
        this.redisTemplate = redisTemplate;
        this.tabParamsMapper = tabParamsMapper;
    }

    @Override
    public String roundNo(final String lastKey, final int digLen) {
        String keyIncrement = redisRoundHeader.concat(lastKey);
        Long roundNo = redisTemplate.opsForValue().increment(keyIncrement, 1L);
        /* redis 集群模式禁用 delete 方法，高并发会导致 increment 获取重复值 范围在 [1~3之间] */
        redisTemplate.expire(keyIncrement, redisShot30sExpires, TimeUnit.SECONDS);
        log.debug(LogEnmu.LOG1.pair("获取序列", 1), keyIncrement, roundNo);
        return String.format("%0".concat(String.valueOf(digLen)).concat("d"), roundNo);
    }

    /**
     * 获取一个16位随机串 可用做AES、SM4口令 / 建议预设在数据库中
     *
     * @param lastKey [key 末尾串 标识: redis.key: x-x-lastKey]
     * @return String 返回串，定长补 0
     */
    @Override
    public String getKeyLen16(final String lastKey) {
        log.debug(LogEnmu.LOG2.value(), "设置aeskey", lastKey);
        String key = redisAesHeader.concat(lastKey);
        String aes = (String) redisTemplate.opsForValue().get(key);
        if (aes == null) {
            TabParams tabParams = tabParamsMapper.findByAny(config.getSpringApplicationName(), "aes", lastKey);
            if (Objects.isNull(tabParams)) {
                log.warn(LogEnmu.LOG3.value(), "getKeyLen16", config.getSpringApplicationName(), "AES:".concat(lastKey));
                aes = RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue());
            } else {
                aes = tabParams.getParamsValue();
                redisTemplate.opsForValue().set(key, aes, redisShot5mExpires, TimeUnit.SECONDS);
            }
        }
        return aes;
    }

    @Override
    public String getAlgorithmKey(final String authToken) {
        String redisAlgorithmKey = redisAuthtokenKey.concat(authToken).concat(":algorithm-key");
        var opt = Optional.ofNullable(redisTemplate.opsForValue().get(redisAlgorithmKey));
        return opt.orElseGet(() -> {
            var algorithmKey = RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue());
            redisTemplate.opsForValue().set(redisAlgorithmKey, algorithmKey, this.redisShot10mExpires, TimeUnit.SECONDS);
            return algorithmKey;
        }).toString();
    }

}
