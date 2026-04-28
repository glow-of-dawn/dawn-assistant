package com.dawn.plugin.task.service;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabRunLog;
import com.dawn.plugin.entity.ccore.TabTask;
import com.dawn.plugin.mapper.ccore.TabRunLogMapper;
import com.dawn.plugin.mapper.ccore.TabTaskMapper;
import com.dawn.plugin.redis.lock.RedisDistributedLock;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.util.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author hforest-480s
 * @date 2021/2/5 16:46
 */
@Data
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.task-status"}, havingValue = "enable", matchIfMissing = true)
public abstract class AbstractHandleService<T> {

    protected String statMsg = "gogo";
    /* 服务名称 */
    protected String transCode;
    /* redis-锁key */
    protected String lockKey = "schedule-cron";
    protected String title = "定时任务";
    /* redis-资源令牌 */
    protected String requireToken = VarEnmu.FALSE.value();
    /* 此处为运行日志，视情况而定，此处有锁可以如此定义，如果没有锁情况，并发时必然有问题 */
    /* 多任务联合锁请留意 */
    protected TabRunLog tabRunLog;
    protected TabTask tabTask;
    protected T data;
    protected PluginConfig config;
    protected RedisKeyService redisKeyService;
    protected RedisDistributedLock distributedLock;
    protected TabTaskMapper tabTaskMapper;
    protected TabRunLogMapper tabRunLogMapper;
    protected AtomicLong roundNo;

    @Autowired
    public void init(PluginConfig config,
                     RedisKeyService redisKeyService,
                     RedisDistributedLock distributedLock,
                     TabRunLogMapper tabRunLogMapper,
                     TabTaskMapper tabTaskMapper) {
        this.config = config;
        this.tabTaskMapper = tabTaskMapper;
        this.redisKeyService = redisKeyService;
        this.distributedLock = distributedLock;
        this.tabRunLogMapper = tabRunLogMapper;
    }

    /**
     * [建议填装到 abstract class]
     * [获取任务锁]
     *
     * @return boolean
     **/
    public boolean getTaskLock() {
        /* TabTask.taskServer == '*' */
        tabRunLog = new TabRunLog();
        if (VarEnmu.STAR.value().equals(tabTask.getTaskServer())) {
            statMsg = "集群任务启动";
            requireToken = distributedLock.acquire(lockKey);
            if (VarEnmu.FALSE.value().equals(requireToken)) {
                statMsg = title.concat(" - 其他资源执行");
                log.info(LogEnmu.LOG4.value(), transCode, roundNo.incrementAndGet(), statMsg, requireToken);
                return false;
            }
            tabRunLog.setTaskBatchSerial(requireToken);
        } else if (config.getApplicationId().equals(tabTask.getTaskServer())) {
            statMsg = "单点任务启动";
            log.info(LogEnmu.LOG6.value(), transCode, roundNo.incrementAndGet(), statMsg, tabTask.getTaskServer(), "当前服务ID：",
                config.getApplicationId());
            tabRunLog.setTaskBatchSerial(config.getApplicationId());
        } else {
            statMsg = "当前服务不执行，当前服务ID：";
            log.info(LogEnmu.LOG4.value(), transCode, roundNo.incrementAndGet(), statMsg, config.getApplicationId());
            return false;
        }

        if (tabTask.getTaskCount() == VarEnmu.IIT_MINUS_ONE.ivalue()) {
            log.debug(LogEnmu.LOG3.value(), transCode, roundNo.incrementAndGet(), "不限次数");
        } else if (LocalDateTime.now().isAfter(tabTask.getTaskOver()) || tabTask.getTaskCount() == VarEnmu.ZERO.ivalue()) {
            tabTask.setTaskSts(CodeEnmu.STS_C.code());
            tabTaskMapper.edit(tabTask);
        } else if (tabTask.getTaskCount() > VarEnmu.ZERO.ivalue()) {
            tabTask.setTaskCount(tabTask.getTaskCount() - VarEnmu.ONE.ivalue());
            tabTaskMapper.edit(tabTask);
        }
        tabRunLog.setId(redisKeyService.getPrimary());
        tabRunLog.setTaskProject(config.getSpringApplicationName());
        tabRunLog.setTaskClass(transCode);
        tabRunLog.setTaskType(lockKey);
        tabRunLog.setTaskStartTime(LocalDateTime.now());
        tabRunLog.setTaskResult(CodeEnmu.STS_A.code());
        tabRunLog.setTaskException(statMsg);
        tabRunLogMapper.create(tabRunLog);
        return true;
    }

    /**
     * [建议填装到 abstract class]
     * [任务记录 - 结束]
     *
     * @param response [结果 Response<Object>]
     */
    public void overlog(Response<Object> response) {
        tabRunLog.setTaskResult(String.valueOf(response.getCode()));
        tabRunLog.setTaskException(response.getMessage());
        tabRunLog.setTaskOverTime(LocalDateTime.now());
        tabRunLogMapper.edit(tabRunLog);
        /* 释放资源 */
        statMsg = title.concat(" - 释放资源");
        log.info(LogEnmu.LOG5.value(), transCode, roundNo.incrementAndGet(), statMsg,
            "任务执行完成，延后[".concat(String.valueOf(redisKeyService.getRedisShot5sExpires())).concat("s]资源释放"), requireToken);
        distributedLock.release(lockKey, requireToken, redisKeyService.getRedisShot5sExpires());
        requireToken = VarEnmu.FALSE.value();
    }

    public <V> V getData() {
        return (V) this.data;
    }

    /**
     * [程序处理]
     *
     * @return Response<Object>
     **/
    public Response<Object> handle() {
        return new Response<>().failure("请编写[handle]处理函数");
    }

    /**
     * 一批相互约束的业务设置一个key
     * ** 切记 ***
     * 否则发生多笔相互非关联业务相互阻碍业务发生
     * [设置锁机制key] *****
     * ------------------------------------------
     * 调用方式
     * [.setLockKey(lockKey);]
     * ------------------------------------------
     * setTabTask(tabTask) 当多个同 task_service_name 执行时，仅显示最后一次 set的task，请特别留意。
     * 可采用 @Scope("prototype") 多例模式来避免问题发生，但是需要考虑确实需要性，以及内存溢出风险
     **/
    public void run() {
        roundNo.set(VarEnmu.ZERO.ivalue());
        lockKey = transCode.concat(VarEnmu.SLIGHTLY.value()).concat(tabTask.getTaskId());
        if (getTaskLock()) {
            overlog(handle());
        }
    }

}
