package com.dawn.plugin.thread;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * [含任务信息 - 线程池服务 = ThreadPoolTaskExecutor]
 * 创建时间：2021/3/5 9:33
 *
 * @author hforest-480s
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.thread-status"}, havingValue = "enable", matchIfMissing = true)
public class TrackThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    private final Map<TimedFutureTask<?>, TimedTaskInfo> taskMap = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(0);

    /**
     * [适用于不需要返回结果的任务]
     *
     * @param task [task]
     */
    @Override
    public void execute(@NotNull @NonNull Runnable task) {
        TimedFutureTask<?> ft = createFutureTask(task, null);
        super.execute(ft);
    }

    /**
     * [适用于需要跟踪任务执行状态的任务]
     * [return super.submit(ft);]
     *
     * @param task [task]
     */
    @NotNull
    @Override
    public Future<?> submit(@NotNull @NonNull Runnable task) {
        TimedFutureTask<?> ft = createFutureTask(task, null);
        super.execute(ft);
        return ft;
    }

    /**
     * [适用于需要获取任务执行结果的任务]
     * [return super.submit(task);]
     *
     * @param task [task]
     */
    @NotNull
    @Override
    public <T> Future<T> submit(@NotNull Callable<T> task) {
        TimedFutureTask<T> ft = createFutureTask(task);
        super.execute(ft);
        return ft;
    }

    /**
     * [队列任务数]
     *
     * @return int [队列任务数]
     */
    public int queuedCount() {
        ThreadPoolExecutor exec = getThreadPoolExecutor();
        return exec.getQueue().size();
    }

    /**
     * [当前执行数]
     *
     * @return int [当前执行数]
     */
    public int activeCount() {
        ThreadPoolExecutor exec = getThreadPoolExecutor();
        return exec.getActiveCount();
    }

    /**
     * 队列清理
     **/
    public void cleanQueue() {
        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();
        Assert.notNull(threadPoolExecutor, "线程池showThreadPoolInfo(): method is null");
        threadPoolExecutor.getQueue().clear();
    }

    /**
     * [取消在队列中等待超过 thresholdMs 的任务]
     *
     * @param thresholdMs [任务等待时间 / 毫秒]
     */
    public void cancelQueuedTasksLongerThan(long thresholdMs) {
        ThreadPoolExecutor exec = getThreadPoolExecutor();
        exec.getQueue()
            .stream()
            .filter(TimedFutureTask.class::isInstance)
            .forEach(r -> {
                TimedFutureTask<?> tf = (TimedFutureTask<?>) r;
                TimedTaskInfo info = tf.info;
                long wait = System.currentTimeMillis() - info.submitTimeMs;
                if (wait > thresholdMs) {
                    boolean removed = exec.getQueue().remove(r);
                    tf.cancel(true);
                    taskMap.remove(tf);
                    log.warn(LogEnmu.LOG7_3KV.value(), "线程池.取消队列中等待过久的任务", "id", info.id, "waitMs", wait, "removed", removed);
                }
            });
    }

    /**
     * [取消运行时间超过 thresholdMs 的任务（尝试中断）]
     *
     * @param thresholdMs [任务运行时间 / 毫秒]
     */
    public void cancelRunningTasksLongerThan(long thresholdMs) {
        taskMap
            .forEach((tf, info) -> {
                long run = info.runMillis();
                if (run > thresholdMs) {
                    boolean cancelled = tf.cancel(true);
                    taskMap.remove(tf);
                    log.warn(LogEnmu.LOG7_3KV.value(), "线程池.取消运行过久的任务", "id", info.id, "runMs", run, "cancelled", cancelled);
                }
            });
    }

    /**
     * [如果队列过长且存在长时间等待，尝试重建线程池]
     *
     * @param queueThreshold   [队列长度阈值]
     * @param idleActiveMillis [队列任务最长等待超过此值判断为假死 / 毫秒]
     */
    public void tryRebuildIfStuck(int queueThreshold, long idleActiveMillis) {
        ThreadPoolExecutor exec = getThreadPoolExecutor();
        int qSize = exec.getQueue().size();
        int active = exec.getActiveCount();
        /* 简单策略：队列很长且活动线程较少且队列任务等待时间也很久 -> 重建 */
        if (qSize >= queueThreshold) {
            /* 进一步判断队列中最长等待时间 */
            AtomicLong atomMaxWait = new AtomicLong(VarEnmu.ZERO.ivalue());
            exec.getQueue()
                .stream()
                .filter(TimedFutureTask.class::isInstance)
                .forEach(r -> {
                    TimedTaskInfo info = ((TimedFutureTask<?>) r).info;
                    long wait = System.currentTimeMillis() - info.submitTimeMs;
                    if (wait > atomMaxWait.get()) {
                        atomMaxWait.set(wait);
                    }
                });
            if (atomMaxWait.get() > idleActiveMillis) {
                log.warn(LogEnmu.LOG7_3KV.value(), "线程池.检测到线程池疑似假死，准备重建",
                    "queueSize", qSize,
                    "active", active,
                    "maxWaitMs", atomMaxWait.get());
                rebuildThreadPool();
            }
        }
    }

    /**
     * [重建线程池（关闭当前执行器并重新 initialize）]
     */
    public synchronized void rebuildThreadPool() {
        ThreadPoolExecutor exec = getThreadPoolExecutor();
        if (!exec.isShutdown()) {
            exec.shutdownNow();
        }
        /* 清理记录的任务映射（因为已取消/移除） */
        taskMap.clear();
        /* 重新初始化底层执行器（ThreadPoolTaskExecutor 提供 initialize 方法） */
        try {
            this.initialize();
            log.info(LogEnmu.LOG2.value(), "线程池已重建", "namePrefix", this.getThreadNamePrefix());
        } catch (Exception e) {
            log.warn(LogEnmu.LOG2.value(), "线程池重建失败", e.toString());
        }
    }

    private <V> TimedFutureTask<V> createFutureTask(Runnable runnable, V result) {
        TimedTaskInfo info = new TimedTaskInfo("t-" + idGen.incrementAndGet());
        TimedFutureTask<V> ft = new TimedFutureTask<V>(runnable, result, info) {
        };
        taskMap.put(ft, info);
        return ft;
    }

    private <V> TimedFutureTask<V> createFutureTask(Callable<V> callable) {
        TimedTaskInfo info = new TimedTaskInfo("t-" + idGen.incrementAndGet());
        TimedFutureTask<V> ft = new TimedFutureTask<V>(callable, info) {
        };
        taskMap.put(ft, info);
        return ft;
    }

    /**
     * [线程池服务 = TimedTaskInfo]
     *
     * @author hforest-480s
     */
    private static class TimedTaskInfo {
        final long submitTimeMs;
        volatile long startTimeMs;
        final String id;

        TimedTaskInfo(String id) {
            this.submitTimeMs = System.currentTimeMillis();
            this.startTimeMs = VarEnmu.ZERO.ivalue();
            this.id = id;
        }

        void markStarted() {
            this.startTimeMs = System.currentTimeMillis();
        }

        long waitMillis() {
            long s = (startTimeMs == VarEnmu.ZERO.ivalue() ? System.currentTimeMillis() : startTimeMs);
            return s - submitTimeMs;
        }

        long runMillis() {
            if (startTimeMs == VarEnmu.ZERO.ivalue()) {
                return VarEnmu.ZERO.ivalue();
            }
            return System.currentTimeMillis() - startTimeMs;
        }
    }

    /**
     * [线程池服务 = TimedFutureTask]
     *
     * @author hforest-480s
     */
    @EqualsAndHashCode(callSuper = false)
    private class TimedFutureTask<V> extends FutureTask<V> {
        final TimedTaskInfo info;

        TimedFutureTask(Callable<V> c, TimedTaskInfo info) {
            super(c);
            this.info = info;
        }

        TimedFutureTask(Runnable r, V result, TimedTaskInfo info) {
            super(r, result);
            this.info = info;
        }

        @Override
        public void run() {
            /* mark started before actual runnable/callable runs */
            info.markStarted();
            super.run();
        }

        @Override
        protected void done() {
            /* 移除完成的任务 */
            taskMap.remove(this);
        }
    }

}
