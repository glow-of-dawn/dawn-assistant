package com.dawn.plugin.redis.primary.impl;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabParams;
import com.dawn.plugin.entity.ccore.TabRedis;
import com.dawn.plugin.mapper.ccore.TabParamsMapper;
import com.dawn.plugin.mapper.ccore.TabRedisMapper;
import com.dawn.plugin.redis.primary.AbstractRedisKeyService;
import com.dawn.plugin.redis.primary.KeyService;
import com.dawn.plugin.util.RandomUtil;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * redis降级处理，依赖于数据库物化处理
 * 创建时间：2025/7/3 15:22
 *
 * @author hforest-480s
 */
@EqualsAndHashCode(callSuper = false)
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class DatabaseKeyServiceImpl extends AbstractRedisKeyService implements KeyService {

    private final TabRedisMapper tabRedisMapper;

    public DatabaseKeyServiceImpl(final PluginConfig config,
                                  final TabParamsMapper tabParamsMapper,
                                  final RedisTemplate<String, Object> redisTemplate,
                                  TabRedisMapper tabRedisMapper) {
        this.config = config;
        this.redisTemplate = redisTemplate;
        this.tabParamsMapper = tabParamsMapper;
        this.tabRedisMapper = tabRedisMapper;
    }

    @Override
    public String roundNo(final String lastKey, final int digLen) {
        String keyIncrement = redisRoundHeader.concat(lastKey);
        AtomicLong atomRoundNo = new AtomicLong(VarEnmu.ONE.ivalue());
        var tRedis = tabRedisMapper.findByProjectAndKey(config.getSpringApplicationName(), keyIncrement);
        Optional.ofNullable(tRedis)
                .ifPresentOrElse(tabRedis -> {
                            atomRoundNo.set(Long.parseLong(tabRedis.getRedisValue()));
                            tabRedis.setRedisValue(String.valueOf(atomRoundNo.incrementAndGet()));
                            tabRedisMapper.edit(tabRedis);
                        },
                        () -> {
                            String timestamp =
                                    String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(VarEnmu.EIGHT.ivalue())).toEpochMilli());
                            TabRedis tabRedis = new TabRedis();
                            tabRedis.setId(timestamp)
                                    .setRedisProject(config.getSpringApplicationName())
                                    .setRedisKey(keyIncrement)
                                    .setRedisKeyToken(VarEnmu.NONE.value())
                                    .setRedisTime(LocalDateTime.now())
                                    .setRedisExpire(redisShot30sExpires)
                                    .setRedisValue(VarEnmu.ONE.value());
                            tabRedisMapper.create(tabRedis);
                        });
        Long roundNo = atomRoundNo.get();
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
        TabParams tabParams = tabParamsMapper.findByAny(config.getSpringApplicationName(), "aes", lastKey);
        if (Objects.isNull(tabParams)) {
            log.warn(LogEnmu.LOG3.value(), "getKeyLen16", config.getSpringApplicationName(), "AES:".concat(lastKey));
            return RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue());
        } else {
            return tabParams.getParamsValue();
        }
    }

    @Override
    public String getAlgorithmKey(final String authToken) {
        String redisAlgorithmKey = redisAuthtokenKey.concat(authToken).concat(":algorithm-key");
        var tabRedis = tabRedisMapper.findByProjectAndKey(config.getSpringApplicationName(), redisAlgorithmKey);
        var opt = Optional.ofNullable(tabRedis);
        return opt.orElseGet(() -> {
            var algorithmKey = RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue());
            String timestamp =
                    String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(VarEnmu.EIGHT.ivalue())).toEpochMilli());
            var tRedis = new TabRedis()
                    .setId(timestamp)
                    .setRedisProject(config.getSpringApplicationName())
                    .setRedisKey(redisAlgorithmKey)
                    .setRedisKeyToken(VarEnmu.NONE.value())
                    .setRedisTime(LocalDateTime.now())
                    .setRedisExpire(redisShot10mExpires)
                    .setRedisValue(algorithmKey);
            tabRedisMapper.create(tRedis);
            return tRedis;
        }).getRedisValue();
    }

}
