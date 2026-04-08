package com.dawn.plugin.redis;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.mapper.ccore.TabRedisMapper;
import com.dawn.plugin.redis.lock.RedisDistributedLock;
import com.dawn.plugin.redis.primary.RedisKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * [静态redis调度服务]
 * 创建时间：2025/7/3 14:31
 *
 * @author hforest-480s
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
public class StaticRedisSchedulingService {

    private final RedisKeyService redisKeyService;
    private final TabRedisMapper tabRedisMapper;
    private final PluginConfig config;
    private final RedisDistributedLock redisDistributedLock;

    public StaticRedisSchedulingService(final PluginConfig config,
                                        final RedisKeyService redisKeyService,
                                        final TabRedisMapper tabRedisMapper,
                                        final @Qualifier("redisDistributedLock") RedisDistributedLock redisDistributedLock) {
        this.config = config;
        this.redisKeyService = redisKeyService;
        this.tabRedisMapper = tabRedisMapper;
        this.redisDistributedLock = redisDistributedLock;
    }

    /**
     * [动态任务加载处理]
     * 秒 分 时 日 月 星期 年份
     *
     **/
    @Scheduled(cron = "#{'${schedule.refresh-dynamic-scheduled-tasks-cron:30 * * * * ?}'}")
    public void refreshDynamicScheduledTasks() {
        log.debug(LogEnmu.LOG1.value(), "redis任务调度");
        /* redis服务状态检查 */
        redisKeyService.redisHealth();
        /* tabRedis失效业务清理 */
        tabRedisMapper.removeByInvalid(config.getSpringApplicationName());
        /* 键值机制检查 */
        redisKeyService.flushRedisKeyService();
        /* 锁机制检查 */
        redisDistributedLock.flushDistributedLock();
    }

}
