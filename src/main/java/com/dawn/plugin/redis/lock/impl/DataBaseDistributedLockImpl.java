package com.dawn.plugin.redis.lock.impl;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabRedis;
import com.dawn.plugin.mapper.ccore.TabRedisMapper;
import com.dawn.plugin.redis.lock.AbstractRedisDistributedLock;
import com.dawn.plugin.redis.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

/**
 * redis降级处理，依赖于数据库物化处理
 * 创建时间：2025/6/26 11:53
 *
 * @author hforest-480s
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class DataBaseDistributedLockImpl extends AbstractRedisDistributedLock implements DistributedLock {

    private final TabRedisMapper tabRedisMapper;
    private final PluginConfig config;

    public DataBaseDistributedLockImpl(PluginConfig config,
                                       TabRedisMapper tabRedisMapper) {
        this.config = config;
        this.tabRedisMapper = tabRedisMapper;
    }

    /**
     * 获取锁
     *
     * @param lockKey            [锁标识key]
     * @param lockAcquireTimeout [获取锁的超时时间，超过这个时间则放弃获取锁]
     * @param lockExpireTime     [锁设定生命周期]
     * @return 锁标识
     */
    @Override
    public String acquire(String lockKey, Integer lockAcquireTimeout, Integer lockExpireTime) {
        var tabRedis = tabRedisMapper.findByProjectAndKey(config.getSpringApplicationName(), lockKey);
        if (Objects.nonNull(tabRedis)) {
            /* 令牌已存在,获取锁失败 */
            return VarEnmu.FALSE.value();
        }
        /* 随机生成一个value */
        var requireToken = UUID.randomUUID().toString();
        String timestamp =
                String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.ofHours(VarEnmu.EIGHT.ivalue())).toEpochMilli());
        TabRedis tRedis = new TabRedis();
        tRedis.setId(timestamp)
                .setRedisProject(config.getSpringApplicationName())
                .setRedisKey(lockKey)
                .setRedisKeyToken(requireToken)
                .setRedisTime(LocalDateTime.now())
                .setRedisExpire(lockExpireTime)
                .setRedisValue(VarEnmu.ZERO.value());
        tabRedisMapper.create(tRedis);
        return requireToken;
    }

    /**
     * [延长锁的生命周期]
     *
     * @param lockExpireTime def: 100 * 1000
     * @param lockKey        [锁标识key]
     * @return 锁标识
     */
    @Override
    public void expire(String lockKey, Integer lockExpireTime) {
        var tabRedis = tabRedisMapper.findByProjectAndKey(config.getSpringApplicationName(), lockKey);
        tabRedis.setRedisExpire(lockExpireTime);
        tabRedisMapper.edit(tabRedis);
    }

    /**
     * 释放锁
     *
     * @param lockKey        [锁标识key]
     * @param indentifier    [销毁认证标识]
     * @param lockExtendTime [延时时常]
     * @return 成功否
     */
    @Override
    public boolean release(String lockKey, String indentifier, Integer lockExtendTime) {
        int cnt = tabRedisMapper.removeByProjectAndIndentifierAndKey(config.getSpringApplicationName(), lockKey, indentifier);
        return cnt > VarEnmu.ZERO.ivalue();
    }

    /**
     * 判断锁是否存在，仅限锁状态判断
     * 如果判断key是否存在可参考：Boolean.TRUE.equals(redisTemplate.hasKey(key))
     *
     * @param lockKey [锁标识key]
     * @return 存在否
     */
    @Override
    public boolean hasKey(String lockKey) {
        var tabRedis = tabRedisMapper.findByProjectAndKey(config.getSpringApplicationName(), lockKey);
        return Objects.nonNull(tabRedis);
    }

}
