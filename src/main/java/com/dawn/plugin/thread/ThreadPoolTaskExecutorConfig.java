package com.dawn.plugin.thread;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * [ThreadPoolTaskExecutor 线程池默认配置]
 * 创建时间：2021/3/5 8:38
 *
 * @author hforest-480s
 */
@Slf4j
@EnableAsync
@Configuration
@ConditionalOnProperty(name = {"plugin-status.thread-status"}, havingValue = "enable", matchIfMissing = true)
public class ThreadPoolTaskExecutorConfig {

    @Value("${thread-pool.core-pool-size:4}")
    private int corePoolSize;
    @Value("${thread-pool.max-pool-size:6}")
    private int maxPoolSize;
    @Value("${thread-pool.queue-capacity:100}")
    private int queueCapacity;
    @Value("${thread-pool.rejection-policy:abort-policy}")
    private String rejectionPolicy;
    @Value("${thread-pool.thread-name-prefix:default-simple-thread-}")
    private String threadNamePrefix;
    /* 默认队列任务存活5分钟 */
    @Value("${thread-pool.cancel-queued-threshold-time-millis:300000}")
    private long cancelQueuedThresholdMs;
    /* 默认任务执行最长运行5分钟 */
    @Value("${thread-pool.cancel-running-taskshold-time-millis:300000}")
    private long cancelRunningTasksholdMs;
    /* 默认队列长度达到50个，且长时间存在60秒，重新初始化线程池 */
    @Value("${thread-pool.rebuild-thread-pool-queue-size:50}")
    private int rebuildThreadPoolQueueSize;
    /* 默认队列长度达到50个，且长时间存在60秒，重新初始化线程池 */
    @Value("${thread-pool.rebuild-thread-pool-idle-active-millis:60000}")
    private long rebuildThreadPoolIdleActiveMillis;
    private final TrackThreadPoolTaskExecutor executor;

    public ThreadPoolTaskExecutorConfig(TrackThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    @Bean
    public Executor asyncServiceExecutor() {
        log.info(LogEnmu.LOG2.value(), "asyncServiceExecutor", "start");
        /* 配置核心线程数 - 维持运行的线程数 */
        executor.setCorePoolSize(corePoolSize);
        /* 配置最大线程数 - 可扩增到最大线程数 */
        executor.setMaxPoolSize(maxPoolSize);
        /*
         * 配置队列大小, 合理名称:配置排队队列大小, 超出增加线程数
         * task <= queue-capacity :pool-size = core-pool-size
         * task > queue-capacity :pool-size <= max-pool-size
         * 此列疑问点在于 超出queue-capacity的任务等待在那里, 还是queue-capacity不是队列上线, 貌似是后者
         * 此列测试后应该叫 [排队上线, 超出新开线程]
         * 此值的设置耐人寻味，过大将导致 max-pool-size 无法生效，闲置资源
         * 例如: queue-capacity = 100 当task瞬间 = 99 时，pool-size = core-pool-size[5]，此时，99-5=95在等待，而不会增加新线程
         * 建议配置小一些 queue-capacity = core-pool-size ?
         */
        executor.setQueueCapacity(queueCapacity);
        /*
         * 用于多个线程池中区分线程池
         * 本例将导致项目启动只有一个线程池，如需多个线程池存在于项目中请增加功能
         * 配置线程池中的线程的名称前缀
         */
        executor.setThreadNamePrefix(threadNamePrefix);
        /*
         * rejection-policy：当pool已经达到max size的时候，如何处理新任务
        * CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
         * AbortPolicy
         * DiscardPolicy
         * DiscardOldestPolicy
         */
        switch (rejectionPolicy) {
            case "discard-policy":
                /* 当线程池无法接受新任务时，直接丢弃这些任务，不做任何处理。 */
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
                break;
            case "discard-oldest-policy":
                /* 当线程池无法接受新任务时，丢弃任务队列中最旧的任务，腾出空间来执行新的任务。 */
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
                break;
            default:
                /* 默认策略: AbortPolicy */
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        }

        /* 执行初始化 */
        executor.initialize();

        log.info(LogEnmu.LOG2.value(), "asyncServiceExecutor", "over");
        return executor;
    }

    /**
     * [队列信息打印 / 每 5分钟 执行一次]
     */
    @Scheduled(fixedDelayString = "#{'${thread-pool.print-thread-pool-info-interval-ms:300000}'}")
    public void printThreadPoolInfo() {
        ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
        /*
         * 这里可以用使用存储到在线库、打日志、存缓存等方式把监控数据记录下来
         * 用到线程池里面具体哪个方法执行的线程任务
         * 配置在config里面线程前缀的名字（用于多个线程池中区分线程池）
         */
        var poolParams = new StringBuilder()
            .append("pool-size").append(VarEnmu.SLASH.value())
            .append("min").append(VarEnmu.SLASH.value())
            .append("max").append(VarEnmu.SLASH.value())
            .append("keep-seconds")
            .append("[")
            .append(threadPoolExecutor.getPoolSize()).append(VarEnmu.SLASH.value())
            .append(threadPoolExecutor.getCorePoolSize()).append(VarEnmu.SLASH.value())
            .append(threadPoolExecutor.getMaximumPoolSize()).append(VarEnmu.SLASH.value())
            .append(threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS))
            .append("]");
        var taskInfo = new StringBuilder()
            .append("[待执行数").append(VarEnmu.QUOTE.value())
            .append(threadPoolExecutor.getQueue().size())
            .append("][执行中").append(VarEnmu.QUOTE.value())
            .append(threadPoolExecutor.getActiveCount())
            .append("][完成数").append(VarEnmu.QUOTE.value())
            .append(threadPoolExecutor.getCompletedTaskCount())
            .append("][提交数").append(VarEnmu.QUOTE.value())
            .append(threadPoolExecutor.getTaskCount())
            .append("][queue预设长度").append(VarEnmu.QUOTE.value())
            .append(queueCapacity).append("]");
        log.info(LogEnmu.LOG3.value(), "线程池状态", taskInfo, poolParams);
    }

    /**
     * [队列状态检查]
     * 每 5 秒检查一次
     */
    @Scheduled(fixedDelayString = "#{'${thread-pool.monitor-interval-ms:5000}'}")
    public void monitorScheduled() {
        try {
            /* 取消队列中等待超过 30s 的任务 */
            executor.cancelQueuedTasksLongerThan(cancelQueuedThresholdMs);
            /* 取消运行超过 5min 的任务（尝试中断） */
            executor.cancelRunningTasksLongerThan(cancelRunningTasksholdMs);
            /* 如果队列过长且存在长时间等待，尝试重建线程池 / queueThreshold / idleActiveMillis(队列任务最长等待超过此值判断为“假死”) */
            executor.tryRebuildIfStuck(rebuildThreadPoolQueueSize, rebuildThreadPoolIdleActiveMillis);
        } catch (Exception e) {
            log.warn(LogEnmu.LOG3.value(), "线程池监控异常", e.toString());
        }
    }

}
