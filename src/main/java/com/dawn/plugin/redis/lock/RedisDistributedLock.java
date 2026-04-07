package com.dawn.plugin.redis.lock;

import com.dawn.plugin.redis.lock.impl.DataBaseDistributedLockImpl;
import com.dawn.plugin.redis.lock.impl.RedisSingleDistributedLockImpl;
import com.dawn.plugin.redis.primary.RedisKeyService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 创建时间：2025/7/3 10:41
 *
 * @author hforest-480s
 */
@Data
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class RedisDistributedLock implements DistributedLock {

    private RedisSingleDistributedLockImpl redisSingleDistributedLockImpl;
    private DataBaseDistributedLockImpl dataBaseDistributedLockImpl;
    private DistributedLock distributedLock;
    private RedisKeyService redisKeyService;

    public RedisDistributedLock(RedisSingleDistributedLockImpl redisSingleDistributedLockImpl,
                                DataBaseDistributedLockImpl dataBaseDistributedLockImpl,
                                RedisKeyService redisKeyService) {
        this.redisSingleDistributedLockImpl = redisSingleDistributedLockImpl;
        this.dataBaseDistributedLockImpl = dataBaseDistributedLockImpl;
        this.redisKeyService = redisKeyService;
        /* 默认redis模式 */
        this.distributedLock = redisSingleDistributedLockImpl;
    }

    /**
     * 锁机制调整
     *
     */
    public void flushDistributedLock() {
        this.distributedLock = redisKeyService.isRedisHealth()
            ? redisSingleDistributedLockImpl : dataBaseDistributedLockImpl;
    }

    @Override
    public String acquire(final String lockKey) {
        return distributedLock.acquire(lockKey);
    }

    @Override
    public String acquire(final String lockKey, final Integer lockExpireTime) {
        return distributedLock.acquire(lockKey, lockExpireTime);
    }

    @Override
    public String acquire(final String lockKey, final Integer lockAcquireTimeout, final Integer lockExpireTime) {
        return distributedLock.acquire(lockKey, lockAcquireTimeout, lockExpireTime);
    }

    @Override
    public void expire(final String lockKey, final Integer lockExpireTime) {
        distributedLock.expire(lockKey, lockExpireTime);
    }

    @Override
    public boolean release(final String lockKey, final String indentifier) {
        return distributedLock.release(lockKey, indentifier);
    }

    @Override
    public boolean release(final String lockKey, final String indentifier, final Integer lockExtendTime) {
        return distributedLock.release(lockKey, indentifier, lockExtendTime);
    }

    @Override
    public boolean hasKey(final String lockKey) {
        return distributedLock.hasKey(lockKey);
    }
}
