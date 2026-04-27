package com.dawn.plugin.thread;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 创建时间：2021/3/5 9:06
 *
 * @author hforest-480s
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.thread-status"}, havingValue = "enable", matchIfMissing = true)
public class TestSimpleTask {

    @Async("asyncServiceExecutor")
    public void task1(boolean closeErrTest) throws InterruptedException {
        var sleep = RandomUtil.getRandomInt(VarEnmu.THREE.ivalue());
        Thread.sleep(sleep);
        log.info(LogEnmu.LOG3.value(), "无返回值的任务", VarEnmu.TWO.ivalue(), sleep);
        Assert.isTrue(sleep % VarEnmu.ELEVEN.ivalue() == VarEnmu.ZERO.ivalue() || closeErrTest, "测试异常");
    }

    @Async("asyncServiceExecutor")
    public Future<String> task2() throws InterruptedException {
        var sleep = RandomUtil.getRandomInt(VarEnmu.FOUR.ivalue());
        Thread.sleep(sleep);
        log.info(LogEnmu.LOG3.value(), "有返回值的任务", VarEnmu.TWO.ivalue(), sleep);
        return CompletableFuture.completedFuture(Thread.currentThread().getName());
    }

}
