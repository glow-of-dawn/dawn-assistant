package com.dawn.plugin.task.service;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabTask;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * [动态任务]
 *
 * @author hforest-480s
 * @date 2020/12/3 10:24
 */
@Slf4j
@Service
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = {"plugin-status.task-status"}, havingValue = "enable", matchIfMissing = true)
public class DynamicSchedulingService implements SchedulingConfigurer, DisposableBean {

    private ScheduledTaskRegistrar registrar;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TabTask> taskMap = new ConcurrentHashMap<>();
    /* [实现并发的定时任务, 指定线程池] */
    private final TaskScheduler dynamicThreadPoolTaskScheduler;
    private final PluginConfig config;

    public DynamicSchedulingService(PluginConfig config,
                                    TaskScheduler dynamicThreadPoolTaskScheduler) {
        this.config = config;
        this.dynamicThreadPoolTaskScheduler = dynamicThreadPoolTaskScheduler;
    }

    /**
     * [任务注册管理器]
     *
     * @param registrar [scheduledTaskRegistrar]
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        log.info(LogEnmu.LOG2.value(), "定时任务", "初始化");
        this.registrar = registrar;
        /*
        简单粗暴的方式直接指定
        Executors.newScheduledThreadPool(5)
        Executors 不建议使用存在耗尽资源嫌疑，改用如下
        new ThreadPoolExecutor(2, 10, 20, TimeUnit.SECONDS, new ArrayBlockingQueue(200))
        也可以自定义的线程池，方便线程的使用与维护，这里不多说了
        */
        registrar.setTaskScheduler(dynamicThreadPoolTaskScheduler);
    }

    /**
     * [刷新任务]
     *
     * @param tabTasks 任务组
     * @return Response<Object>
     */
    public Response<Object> refreshTasks(List<TabTask> tabTasks) {
        log.debug(LogEnmu.LOG2.value(), "定时任务", "刷新");
        int taskHandlerCnt = VarEnmu.ZERO.ivalue();
        taskMap.clear();
        Set<String> sids = scheduledFutures.keySet();
        /* 取消已经删除/变更的策略任务 */
        tabTasks.forEach(t -> {
            String ttid = "-";
            try {
                ttid = config.getMapperLowerCamel().writeValueAsString(t);
            } catch (JacksonException ex) {
                log.warn(LogEnmu.LOG3.value(), "定时任务", "refreshTasks.writeValueAsString", ex.toString());
            }
            taskMap.put(DigestUtils.sha256Hex(ttid), t);
        });
        /* 不存在 [tabTasks] 任务停止 */
        for (String sid : sids) {
            if (taskMap.get(sid) == null) {
                log.info(LogEnmu.LOG3.value(), "定时任务", "删除任务", sid);
                scheduledFutures.get(sid).cancel(false);
                sids.remove(sid);
            }
        }
        /* 不存在 [scheduledFutures] 任务收集 */
        List<String> ttns = new ArrayList<>();
        taskMap.entrySet().stream()
            .filter(en -> (!sids.contains(en.getKey())))
            .forEach(en -> ttns.add(en.getKey()));
        /* 不存在 [scheduledFutures] 任务启动 */
        for (String tt : ttns) {
            TabTask tabTask = taskMap.get(tt);
            Object service = config.getComponentServiceBean(tabTask.getTaskServiceName());
            TaskScheduler ts = registrar.getScheduler();
            if (Objects.isNull(service) || Objects.isNull(ts)) {
                continue;
            }
            HandleService handleService = (HandleService) service;
            handleService.setTabTask(tabTask);
            CronTask task = new CronTask(handleService, tabTask.getTaskCron());
            log.info(LogEnmu.LOG7_3KV.value(), "定时任务加载", "任务调用", tabTask.getTaskServiceName(), "任务cron", tabTask.getTaskCron(),
                "任务说明", tabTask.getTaskInfo());
            ScheduledFuture<?> future = ts.schedule(task.getRunnable(), task.getTrigger());
            scheduledFutures.put(tt, future);
            taskHandlerCnt++;
        }
        if (ttns.size() == taskHandlerCnt) {
            return new Response<>().success().message("动态任务加载完成");
        } else {
            return new Response<>().failure("计划加载任务数:"
                .concat(String.valueOf(ttns.size()))
                .concat("成功加载任务数:")
                .concat(String.valueOf(taskHandlerCnt)));
        }

    }

    /**
     * [销毁任务]
     *
     **/
    @Override
    public void destroy() {
        Optional.ofNullable(this.registrar)
            .ifPresent(ScheduledTaskRegistrar::destroy);
    }

}
