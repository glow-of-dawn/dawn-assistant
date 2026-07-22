package com.dawn.plugin.thread;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 创建时间：2021/3/5 9:06
 *
 * @author hforest-480s
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.thread-status"}, havingValue = "enable", matchIfMissing = true)
public class TestSimpleTask {

    private final RedisKeyService redisKeyService;

    public TestSimpleTask(RedisKeyService redisKeyService) {
        this.redisKeyService = redisKeyService;
    }

    @Async("asyncServiceExecutor")
    public void task1(boolean closeErrTest) throws InterruptedException {
        var sleep = RandomUtil.getRandomInt(VarEnmu.THREE.ivalue());
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(sleep));
        log.info(LogEnmu.LOG3.value(), "无返回值的任务", VarEnmu.TWO.ivalue(), sleep);
        Assert.isTrue(sleep % VarEnmu.ELEVEN.ivalue() == VarEnmu.ZERO.ivalue() || closeErrTest, "测试异常");
    }

    @Async("asyncServiceExecutor")
    public Future<String> task2() throws InterruptedException {
        var sleep = RandomUtil.getRandomInt(VarEnmu.FOUR.ivalue());
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(sleep));
        log.info(LogEnmu.LOG3.value(), "有返回值的任务", VarEnmu.TWO.ivalue(), sleep);
        return CompletableFuture.completedFuture(Thread.currentThread().getName());
    }

    @Async("asyncServiceExecutor")
    public void primaryKeyFromRedisByTask(String groupId, int count, AtomicReference<List<String>> atomList) {
        var i = VarEnmu.ZERO.ivalue();
        log.info(LogEnmu.LOG2.value(), "组", groupId);
        while (i < count) {
            var primary = redisKeyService.getPrimary();
            atomList.get().add(primary);
            i++;
        }
        log.info(LogEnmu.LOG2.value(), "新增主键数量", count);
    }

    @Async("asyncServiceExecutor")
    public void primaryKeyFromRedisObserver(int sleep, AtomicReference<List<String>> atomList) {
        var i = VarEnmu.ZERO.ivalue();
        while (i < VarEnmu.NUMBER_1000.ivalue()) {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(sleep));
            /* 检查list 数量，计算重复之 */
            Set<String> set = new HashSet<>(atomList.get());
            set.addAll(atomList.get());
            log.warn(LogEnmu.LOG7.value(), "观察轮次", i,
                "主键数", atomList.get().size(),
                "排重数", set.size(),
                atomList.get().size() - set.size());
            i++;
        }
        log.info(LogEnmu.LOG1.value(), "主键压力测试结束");
    }

    @Async("asyncServiceExecutor")
    public void primaryKeyFromRedisByThread(String groupId, int count, AtomicReference<List<String>> atomList) {
        /**
         * 压力测试
         * 创建时间：2026/6/2 14:06
         *
         * @author hforest-480s
         * @date 2026/6/2 14:06:00
         */
        class PrimaryThread extends Thread {
            private final String groupId;
            private final RedisKeyService redisKeyService;
            private final org.slf4j.Logger log;
            private final int count;
            private final AtomicReference<List<String>> atomList;

            public PrimaryThread(String groupId,
                                 RedisKeyService redisKeyService,
                                 org.slf4j.Logger log,
                                 int count,
                                 AtomicReference<List<String>> atomList) {
                this.groupId = groupId;
                this.redisKeyService = redisKeyService;
                this.log = log;
                this.count = count;
                this.atomList = atomList;
            }

            @Override
            public void run() {
                var i = VarEnmu.ZERO.ivalue();
                log.info(LogEnmu.LOG2.value(), "组", groupId);
                while (i < count) {
                    var primary = redisKeyService.getPrimary();
                    atomList.get().add(primary);
                    i++;
                }
                log.info(LogEnmu.LOG2.value(), "新增主键数量", count);
            }
        }

        Thread thread1 = new PrimaryThread(groupId, redisKeyService, log, count, atomList);
        thread1.start();
    }

}
