//package com.dawn.plugin.controller.subscriber;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.dawn.plugin.config.PluginConfig;
//import com.dawn.plugin.enmu.CodeEnmu;
//import com.dawn.plugin.enmu.LogEnmu;
//import com.dawn.plugin.enmu.VarEnmu;
//import com.dawn.plugin.mapper.ccore.TabServerMapper;
//import com.dawn.plugin.mapper.ctemp.TempMapper;
//import com.dawn.plugin.redis.lock.RedisDistributedLock;
//import com.dawn.plugin.redis.primary.RedisKeyService;
//import com.dawn.plugin.util.ConvertUtil;
//import com.dawn.plugin.util.RandomUtil;
//import com.dawn.plugin.util.Response;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.util.Assert;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * [redis服务]
// * 创建时间：2021/5/30 20:10
// *
// * @author hforest-480s
// */
//@Slf4j
//@RestController
//@RequestMapping(value = "/rest/redis/service")
//@ConditionalOnProperty(name = {"plugin-rest-controller.assistant-status"}, havingValue = "enable", matchIfMissing = true)
//public class RedisDatabaseRestController {
//
//    private final PluginConfig config;
//    private final ConvertUtil convertUtil;
//    private final TempMapper tempMapper;
//    private final TabServerMapper tabServerMapper;
//    private final RedisDistributedLock distributedLock;
//    private final RedisKeyService redisKeyService;
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    public RedisDatabaseRestController(PluginConfig config,
//                                       ConvertUtil convertUtil,
//                                       TabServerMapper tabServerMapper,
//                                       TempMapper tempMapper,
//                                       RedisKeyService redisKeyService,
//                                       RedisDistributedLock distributedLock,
//                                       RedisTemplate<String, Object> redisTemplate) {
//        this.config = config;
//        this.tabServerMapper = tabServerMapper;
//        this.tempMapper = tempMapper;
//        this.convertUtil = convertUtil;
//        this.redisKeyService = redisKeyService;
//        this.distributedLock = distributedLock;
//        this.redisTemplate = redisTemplate;
//    }
//
//    /**
//     * [数据调整]
//     *
//     * @param body [body]
//     * @return Object
//     **/
//    @PostMapping("/edit/tab/temp")
//    public Object editHandler(@RequestBody String body) throws JsonProcessingException {
//        Map<String, Object> entityMap = config.getMapperLowerCamel().readValue(body, Map.class);
//        var id = entityMap.getOrDefault(VarEnmu.ID.value(), VarEnmu.NONE.value());
//        var temp = tempMapper.find(id);
//        Assert.notNull(temp, "temp is null!");
//        if (!convertUtil.editEntity(entityMap, temp, "temp")) {
//            tempMapper.edit(temp);
//        }
//        return new Response<>().success().data(temp);
//    }
//
//    @GetMapping("/redis/live")
//    public Response<Object> redisLive() {
//        /* 常规操作 */
//        log.info(LogEnmu.LOG1.value(), "redis-live-start");
//        log.info(LogEnmu.LOG2.value(), "getPrimary.DEF", redisKeyService.getPrimary());
//        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE1", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
//        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE2", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
//        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE3", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
//        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE4", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
//        log.info(LogEnmu.LOG2.value(), "getPrimary.THREE5", redisKeyService.getPrimary(VarEnmu.THREE.ivalue()));
//        /* set检测 */
//        var redisHeader = config.getSpringApplicationName().concat(VarEnmu.QUOTE.value());
//        List<Integer> list = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());
//        int maxCnt = VarEnmu.TEN.ivalue();
//        for (int i = VarEnmu.ZERO.ivalue(); i < maxCnt; i++) {
//            list.add(i);
//        }
//        /* set检测 */
//        list.parallelStream()
//            .forEach(n -> {
//                String key = redisHeader.concat("set-".concat(String.valueOf(n)));
//                String val = RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue());
//                redisTemplate.opsForValue().set(key, val, redisKeyService.getRedisShot1mExpires(), TimeUnit.SECONDS);
//                long t = redisTemplate.getExpire(key, TimeUnit.SECONDS);
//                log.info(LogEnmu.LOG4.value(), "set检测", t, key, val);
//            });
//        /* del检测 */
//        list.parallelStream()
//            .forEach(n -> {
//                String key = redisHeader.concat("del-".concat(String.valueOf(n)));
//                String val = RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue());
//                redisTemplate.opsForValue().set(key, val, redisKeyService.getRedisShot1mExpires(), TimeUnit.SECONDS);
//                long t = redisTemplate.getExpire(key, TimeUnit.SECONDS);
//                log.info(LogEnmu.LOG4.value(), "del检测", t, key, val);
//                redisTemplate.expire(key, VarEnmu.ONE.ivalue(), TimeUnit.SECONDS);
//            });
//        /* haskey */
//        list.parallelStream()
//            .forEach(n -> {
//                String setkey = redisHeader.concat("set-".concat(String.valueOf(n)));
//                String delkey = redisHeader.concat("del-".concat(String.valueOf(n)));
//                log.info(LogEnmu.LOG5_2KV.value(), "haskey",
//                    setkey, redisTemplate.hasKey(setkey),
//                    delkey, redisTemplate.hasKey(delkey));
//            });
//        /* 锁机制 */
//        list.parallelStream()
//            .forEach(n -> {
//                String lockkey = redisHeader.concat("lock-".concat(String.valueOf(n)));
//                var requireToken = distributedLock.acquire(lockkey, redisKeyService.getRedisShot5sExpires());
//                log.info(LogEnmu.LOG3.value(), "锁机制", lockkey, requireToken);
//                distributedLock.release(lockkey, requireToken, redisKeyService.getRedisShot1sExpires());
//            });
//        log.info(LogEnmu.LOG1.value(), "redis-live-over");
//        return new Response<>().success().message("/redis/live");
//    }
//
//    @GetMapping("/persistence/test")
//    public Response<Object> persistence() {
//        var temps = tempMapper.findAll();
//        var tabServers = tabServerMapper.findByApplicationSts(CodeEnmu.STS_A.code());
//        return new Response<>()
//            .success()
//            .data(Map.of(
//                "temps", temps,
//                "tabServers", tabServers));
//    }
//
//    @GetMapping("/redis-vs-database")
//    public Response<Object> redisVsDatabase() {
//        redisKeyService.setRedisHealth(false);
//        redisKeyService.flushRedisKeyService();
//        distributedLock.flushDistributedLock();
//
//        int maxCnt = VarEnmu.TWENTY.ivalue();
//        while (maxCnt-- > 0) {
//            primaryHandler();
//        }
//
//        maxCnt = VarEnmu.TWENTY.ivalue();
//        while (maxCnt-- > 0) {
//            roundNoHandler();
//        }
//
//        maxCnt = VarEnmu.TWENTY.ivalue();
//        while (maxCnt-- > 0) {
//            lockHandler(maxCnt);
//        }
//
//        return new Response<>().success();
//    }
//
//    private void primaryHandler() {
//        var primary = redisKeyService.getPrimary();
//        log.info(LogEnmu.LOG2.value(), "primary", primary);
//    }
//
//    private void roundNoHandler() {
//        var roundNo = redisKeyService.roundNo("short", 2);
//        log.info(LogEnmu.LOG2.value(), "roundNo", roundNo);
//    }
//
//    private void lockHandler(int maxCnt) {
//        String lockkey = "lock-".concat(String.valueOf(maxCnt));
//        log.info(LogEnmu.LOG2.value(), "redisHeader", lockkey);
//        /* 正常获取锁 */
//        var requireToken = distributedLock.acquire(lockkey, VarEnmu.ONE_HUNDRED.ivalue());
//        log.info(LogEnmu.LOG2.value(), "正常获取锁", requireToken);
//
//        /* 二次获取锁 */
//        var rt = distributedLock.acquire(lockkey, VarEnmu.ONE_HUNDRED.ivalue());
//        log.info(LogEnmu.LOG2.value(), "二次获取锁", rt);
//
//        /* 非法释放锁 */
//        var lock = distributedLock.release(lockkey, rt);
//        log.info(LogEnmu.LOG2.value(), "非法释放锁", lock);
//
//        /* 正常释放锁 */
//        lock = distributedLock.release(lockkey, requireToken);
//        log.info(LogEnmu.LOG2.value(), "正常释放锁", lock);
//    }
//
//}
