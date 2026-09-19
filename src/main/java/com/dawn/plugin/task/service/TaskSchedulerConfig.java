package com.dawn.plugin.task.service;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 说明
 * 创建时间：2025/9/14 12:39
 *
 * @author hforest-480s
 */
@Slf4j
@Configuration
public class TaskSchedulerConfig {

    /* 调度器shutdown被调用时等待当前被调度的任务完成 */
    private final int awaitTerminationSeconds = VarEnmu.NUMBER_60.ivalue();

    @Bean(name = "dynamicThreadPoolTaskScheduler")
    public TaskScheduler getMyThreadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        /* 线程数上限 */
        threadPoolTaskScheduler.setPoolSize(VarEnmu.TEN.ivalue());
        /* 线程名前缀 */
        String threadName = "def-scheduled-";
        threadPoolTaskScheduler.setThreadNamePrefix(threadName);
        threadPoolTaskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        /* 调度器shutdown被调用时等待当前被调度的任务完成 */
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        /* 等待时长 */
        threadPoolTaskScheduler.setAwaitTerminationSeconds(this.awaitTerminationSeconds);
        log.info(LogEnmu.LOG7_3KV.value(), "定时任务.多线程初始化",
            "poolSize:", VarEnmu.TEN.ivalue(),
            "ThreadNamePrefix:", threadName,
            "awaitTerminationSeconds:", awaitTerminationSeconds
        );
        return threadPoolTaskScheduler;
    }

}
