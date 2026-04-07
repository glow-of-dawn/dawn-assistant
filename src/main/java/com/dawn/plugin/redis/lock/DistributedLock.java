package com.dawn.plugin.redis.lock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 分布式锁
 * 创建时间：2020/12/1 11:04
 *
 * @author hforest-480s
 */
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public interface DistributedLock {

    /**
     * 获取锁
     *
     * @param lockKey [锁标识key]
     * @return 锁标识
     */
    String acquire(String lockKey);

    /**
     * [获取锁]
     *
     * @param lockExpireTime def: 100 * 1000
     * @param lockKey        [锁标识key]
     * @return 锁标识
     */
    String acquire(String lockKey, Integer lockExpireTime);

    /**
     * [获取锁]
     *
     * @param lockAcquireTimeout def: 300
     * @param lockExpireTime     def: 100 * 1000
     * @param lockKey            [锁标识key]
     * @return 锁标识
     */
    String acquire(String lockKey, Integer lockAcquireTimeout, Integer lockExpireTime);

    /**
     * [延长锁的生命周期]
     *
     * @param lockExpireTime def: 100 * 1000
     * @param lockKey        [锁标识key]
     * @return 锁标识
     */
    void expire(String lockKey, Integer lockExpireTime);

    /**
     * 释放锁
     *
     * @param lockKey     [锁标识key]
     * @param indentifier [销毁认证标识]
     * @return 成功否
     */
    boolean release(String lockKey, String indentifier);

    /**
     * 释放锁
     *
     * @param lockKey        [锁标识key]
     * @param indentifier    [销毁认证标识]
     * @param lockExtendTime [延时时常]
     * @return 成功否
     */
    boolean release(String lockKey, String indentifier, Integer lockExtendTime);

    /**
     * 判断锁是否存在
     *
     * @param lockKey [锁标识key]
     * @return 存在否
     */
    boolean hasKey(String lockKey);

}
