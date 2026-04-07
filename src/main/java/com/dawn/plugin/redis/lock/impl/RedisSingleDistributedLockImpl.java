package com.dawn.plugin.redis.lock.impl;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.redis.lock.AbstractRedisDistributedLock;
import com.dawn.plugin.redis.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁 - 串行唯一 - redis
 * 创建时间：2020/12/1 11:04
 *
 * @author hforest-480s
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class RedisSingleDistributedLockImpl extends AbstractRedisDistributedLock implements DistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisSingleDistributedLockImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取锁
     *
     * @param lockKey                  [锁标识key]
     * @param lockAcquireTimeoutMillis [获取锁的超时时间，超过这个时间则放弃获取锁]
     * @param lockExpireTimeSeconds    [锁设定生命周期]
     * @return 锁标识
     */
    @Override
    public String acquire(String lockKey, Integer lockAcquireTimeoutMillis, Integer lockExpireTimeSeconds) {
        try {
            String keyToken = redisLockHeader.concat(lockKey);
            /* 生成随机 token，用于持有者标识和释放校验 */
            String token = UUID.randomUUID().toString();

            long timeoutMillis = Math.max(VarEnmu.ZERO.ivalue(), lockAcquireTimeoutMillis);
            long deadline = System.currentTimeMillis() + timeoutMillis;

            while (System.currentTimeMillis() < deadline) {
                /* 原子地设置 token（SET NX EX） */
                Boolean success = redisTemplate.opsForValue().setIfAbsent(keyToken, token, lockExpireTimeSeconds, TimeUnit.SECONDS);
                if (Boolean.TRUE.equals(success)) {
                    return token;
                }
                Thread.sleep(VarEnmu.ONE_HUNDRED.ivalue());
            }

            return VarEnmu.FALSE.value();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn(LogEnmu.LOG2.value(), "Thread.sleep", ex.toString());
            return VarEnmu.FALSE.value();
        } catch (Exception ex) {
            log.error(LogEnmu.LOG3.value(), "acquire", "acquire lock due to error", ex.toString());
            /* 忽略错误，原因为redis获取的二进制数值转long异常 */
            return VarEnmu.FALSE.value();
        }
    }

    /**
     * [延长锁的生命周期]
     *
     * @param lockExpireTimeSeconds [def: 100 * 1000]
     * @param lockKey               [锁标识key]
     * @return 锁标识
     */
    @Override
    public void expire(String lockKey, Integer lockExpireTimeSeconds) {
        redisTemplate.expire(redisLockHeader.concat(lockKey), lockExpireTimeSeconds, TimeUnit.SECONDS);
    }

    /**
     * 释放锁
     *
     * @param lockKey              [锁标识key]
     * @param indentifier          [销毁认证标识]
     * @param lockExtendTimeMillis [延时时常]
     * @return 成功否
     */
    @Override
    public boolean release(String lockKey, String indentifier, Integer lockExtendTimeMillis) {
        try {
            String key = redisLockHeader.concat(lockKey);
            String script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    redis.call('pexpire', KEYS[1], ARGV[2]);
                    return 1
                else
                    return 0
                end
                """;
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            Long result = redisTemplate.execute(redisScript, List.of(key), indentifier, lockExtendTimeMillis);
            return VarEnmu.ONE.value().equals(String.valueOf(result));
        } catch (IllegalArgumentException ex) {
            log.info(LogEnmu.LOG2.value(), "hasKey(keyToken).release",
                ex.toString().replace(VarEnmu.ILLEGAL_ARGUMENT_EXCEPTION_LOG.value(), VarEnmu.NONE.value()));
            return false;
        } catch (Exception ex) {
            log.warn(LogEnmu.LOG2.value(), "release lock due to error", ex.toString());
            return false;
        }
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
        try {
            /* 检查key是否存在 */
            return redisTemplate.hasKey(redisLockHeader.concat(lockKey));
        } catch (Exception ex) {
            log.warn(LogEnmu.LOG2.value(), "find lock key to error", ex.toString());
            return false;
        }
    }

}
