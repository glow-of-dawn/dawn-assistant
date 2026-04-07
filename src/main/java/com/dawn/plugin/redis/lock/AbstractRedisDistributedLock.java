package com.dawn.plugin.redis.lock;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 创建时间：2025/7/3 10:43
 *
 * @author hforest-480s
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public abstract class AbstractRedisDistributedLock {

    /* redis前缀 */
    @Value("#{'${spring.application.name}:lock:'}")
    protected String redisLockHeader;
    /* redis锁的生命周期 / 建议 300s / 5分钟，意味着一个锁的业务必须5分钟内处理完成，如需调整请分配 */
    @Value("${spring.data.redis.lock-expire-time:300}")
    protected int lockExpireTime = 300;
    /* 锁等待，防止线程饥饿 */
    @Value("${spring.data.redis.lock-acquire-timeout:100000}")
    protected int lockAcquireTimeout = VarEnmu.ONE_HUNDRED.ivalue() * VarEnmu.NUMBER_1000.ivalue();
    /* 延时 ?s 后,redis锁解除 */
    @Value("${spring.data.redis.lock-extend-time:2}")
    protected int lockExtendTime = VarEnmu.TWO.ivalue();

    /**
     * [获取锁]
     * lockAcquireTimeout def: 300
     * lockExpireTime def: 100 * 1000
     *
     * @param lockKey [锁标识key]
     * @return 锁标识
     */
    public String acquire(String lockKey) {
        return acquire(lockKey, this.lockAcquireTimeout, this.lockExpireTime);
    }

    /**
     * 获取锁
     *
     * @param lockKey        [锁标识key]
     * @param lockExpireTime [锁设定生命周期]
     * @return 锁标识
     */
    public String acquire(String lockKey, Integer lockExpireTime) {
        return acquire(lockKey, this.lockAcquireTimeout, lockExpireTime);
    }

    /**
     * 获取锁
     *
     * @param lockKey            [锁标识key]
     * @param lockAcquireTimeout [获取锁的超时时间，超过这个时间则放弃获取锁]
     * @param lockExpireTime     [锁设定生命周期]
     * @return 锁标识
     */
    public String acquire(String lockKey, Integer lockAcquireTimeout, Integer lockExpireTime) {
        log.debug(LogEnmu.LOG4.value(), "[acquire]函数待实现");
        return "[acquire]函数待实现";
    }

    /**
     * [延长锁的生命周期]
     *
     * @param lockExpireTime def: 100 * 1000
     * @param lockKey        [锁标识key]
     */
    public void expire(String lockKey, Integer lockExpireTime) {
        log.debug(LogEnmu.LOG4.value(), "[expire]函数待实现");
    }

    /**
     * 释放锁
     *
     * @param lockKey     [锁标识key]
     * @param indentifier [销毁认证标识]
     * @return 成功否
     */
    public boolean release(String lockKey, String indentifier) {
        return release(lockKey, indentifier, this.lockExtendTime);
    }

    /**
     * 释放锁
     *
     * @param lockKey        [锁标识key]
     * @param indentifier    [销毁认证标识]
     * @param lockExtendTime [延时时常]
     * @return 成功否
     */
    public boolean release(String lockKey, String indentifier, Integer lockExtendTime) {
        log.debug(LogEnmu.LOG4.value(), "[release]函数待实现");
        return false;
    }

    /**
     * 判断锁是否存在，仅限锁状态判断
     * 如果判断key是否存在可参考：Boolean.TRUE.equals(redisTemplate.hasKey(key))
     *
     * @param lockKey [锁标识key]
     * @return 存在否
     */
    public boolean hasKey(String lockKey) {
        log.debug(LogEnmu.LOG4.value(), "[hasKey]函数待实现");
        return false;
    }

}
