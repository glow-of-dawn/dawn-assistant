package com.dawn.plugin.controller.redis;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.redis.lock.RedisDistributedLock;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.thread.TestSimpleTask;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * [redis服务]
 * 创建时间：2021/5/30 20:10
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/redis/service")
@ConditionalOnProperty(name = {"plugin-rest-controller.assistant-status"}, havingValue = "enable", matchIfMissing = true)
public class RedisDatabaseRestController {

    private final PluginConfig config;
    private final TestSimpleTask testSimpleTask;
    private final RedisDistributedLock distributedLock;
    private final RedisKeyService redisKeyService;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisDatabaseRestController(PluginConfig config,
                                       RedisKeyService redisKeyService,
                                       RedisDistributedLock distributedLock,
                                       TestSimpleTask testSimpleTask,
                                       RedisTemplate<String, Object> redisTemplate) {
        this.config = config;
        this.testSimpleTask = testSimpleTask;
        this.redisKeyService = redisKeyService;
        this.distributedLock = distributedLock;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis/live")
    public Response<Object> redisLive() {
        /* 常规操作 */
        log.info(LogEnmu.LOG1.value(), "redis-live-start");
        log.info(LogEnmu.LOG2.value(), "getPrimary.DEF", redisKeyService.getPrimary());
        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE1", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE2", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE3", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE4", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE5", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
        /* set检测 */
        var redisHeader = config.getSpringApplicationName().concat(VarEnmu.QUOTE.value());
        List<Integer> list = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());
        int maxCnt = VarEnmu.TEN.ivalue();
        for (int i = VarEnmu.ZERO.ivalue(); i < maxCnt; i++) {
            list.add(i);
        }
        /* set检测 */
        list.parallelStream()
            .forEach(n -> {
                String key = redisHeader.concat("set-".concat(String.valueOf(n)));
                String val = RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue());
                redisTemplate.opsForValue().set(key, val, Duration.ofSeconds(redisKeyService.getRedisShot1mExpires()));
                long t = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                log.info(LogEnmu.LOG4.value(), "set检测", t, key, val);
            });
        /* del检测 */
        list.parallelStream()
            .forEach(n -> {
                String key = redisHeader.concat("del-".concat(String.valueOf(n)));
                String val = RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue());
                redisTemplate.opsForValue().set(key, val, Duration.ofSeconds(redisKeyService.getRedisShot1mExpires()));
                long t = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                log.info(LogEnmu.LOG4.value(), "del检测", t, key, val);
                redisTemplate.expire(key, Duration.ofSeconds(VarEnmu.ONE.ivalue()));
            });
        /* haskey */
        list.parallelStream()
            .forEach(n -> {
                String setkey = redisHeader.concat("set-".concat(String.valueOf(n)));
                String delkey = redisHeader.concat("del-".concat(String.valueOf(n)));
                log.info(LogEnmu.LOG5_2KV.value(), "haskey",
                    setkey, redisTemplate.hasKey(setkey),
                    delkey, redisTemplate.hasKey(delkey));
            });
        /* 锁机制 */
        list.parallelStream()
            .forEach(n -> {
                String lockkey = redisHeader.concat("lock-".concat(String.valueOf(n)));
                var requireToken = distributedLock.acquire(lockkey, redisKeyService.getRedisShot5sExpires());
                log.info(LogEnmu.LOG3.value(), "锁机制", lockkey, requireToken);
                distributedLock.release(lockkey, requireToken, redisKeyService.getRedisShot1sExpires());
            });
        log.info(LogEnmu.LOG1.value(), "redis-live-over");
        return new Response<>().success().message("/redis/live");
    }

    @GetMapping("/redis/primary-key/{count}/{threadCnt}")
    public Response<Object> getPrimaryKeyFromRedis(@PathVariable Integer count,
                                                   @PathVariable Integer threadCnt) {
        log.info(LogEnmu.LOG1.value(), "主键压力测试开始");
        AtomicReference<List<String>> atomList = new AtomicReference<>(new ArrayList<>(VarEnmu.NUMBER_4096.ivalue()));
        var sleep = VarEnmu.FIVE.ivalue();
        testSimpleTask.primaryKeyFromRedisObserver(sleep, atomList);
        int i = count;
        while (i > VarEnmu.ZERO.ivalue()) {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(sleep));
            int ii = threadCnt;
            while (ii > VarEnmu.ZERO.ivalue()) {
                testSimpleTask.primaryKeyFromRedisByThread(String.format("thread-%s-%s", i, ii), VarEnmu.NUMBER_64.ivalue(), atomList);
                testSimpleTask.primaryKeyFromRedisByTask(String.format("task-%s-%s", i, ii), VarEnmu.FIVE.ivalue(), atomList);
                ii--;
            }
            i--;
        }
        return new Response<>().success();
    }

    @GetMapping("/redis-vs-database")
    public Response<Object> redisVsDatabase() {
        redisKeyService.setRedisHealth(false);
        redisKeyService.flushRedisKeyService();
        distributedLock.flushDistributedLock();

        int maxCnt = VarEnmu.TWENTY.ivalue();
        while (maxCnt-- > VarEnmu.ZERO.ivalue()) {
            primaryHandler();
        }

        maxCnt = VarEnmu.TWENTY.ivalue();
        while (maxCnt-- > VarEnmu.ZERO.ivalue()) {
            roundNoHandler();
        }

        maxCnt = VarEnmu.TWENTY.ivalue();
        while (maxCnt-- > VarEnmu.ZERO.ivalue()) {
            lockHandler(maxCnt);
        }

        return new Response<>().success();
    }

    private void primaryHandler() {
        var primary = redisKeyService.getPrimary();
        log.info(LogEnmu.LOG2.value(), "primary", primary);
    }

    private void roundNoHandler() {
        var roundNo = redisKeyService.roundNo("short", 2);
        log.info(LogEnmu.LOG2.value(), "roundNo", roundNo);
    }

    private void lockHandler(int maxCnt) {
        String lockkey = "lock-".concat(String.valueOf(maxCnt));
        log.info(LogEnmu.LOG2.value(), "redisHeader", lockkey);
        /* 正常获取锁 */
        var requireToken = distributedLock.acquire(lockkey, VarEnmu.ONE_HUNDRED.ivalue());
        log.info(LogEnmu.LOG2.value(), "正常获取锁", requireToken);
        if (VarEnmu.FALSE.value().equals(requireToken)) {
            log.warn(LogEnmu.LOG2.value(), "获取锁失败!", requireToken);
            return;
        }

        /* 二次获取锁 */
        var rt = distributedLock.acquire(lockkey, VarEnmu.ONE_HUNDRED.ivalue());
        log.info(LogEnmu.LOG2.value(), "二次获取锁", rt);

        /* 非法释放锁 */
        var lock = distributedLock.release(lockkey, rt);
        log.info(LogEnmu.LOG2.value(), "非法释放锁", lock);

        /* 正常释放锁 */
        lock = distributedLock.release(lockkey, requireToken);
        log.info(LogEnmu.LOG2.value(), "正常释放锁", lock);
    }

}
