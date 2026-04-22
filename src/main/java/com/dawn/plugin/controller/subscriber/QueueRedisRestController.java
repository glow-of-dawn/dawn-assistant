//package com.dawn.plugin.controller.subscriber;
//
//import com.dawn.plugin.enmu.LogEnmu;
//import com.dawn.plugin.enmu.VarEnmu;
//import com.dawn.plugin.entity.ctemp.Temp;
//import com.dawn.plugin.mapper.ctemp.TempMapper;
//import com.dawn.plugin.redis.primary.RedisKeyService;
//import com.dawn.plugin.subscriber.SubscriberRedisService;
//import com.dawn.plugin.util.RandomUtil;
//import com.dawn.plugin.util.Response;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.IntStream;
//
///**
// * [消息队列]
// * 创建时间：2021/5/30 20:10
// *
// * @author hforest-480s
// */
//@Slf4j
//@RestController
//@RequestMapping(value = "/rest/queue/redis")
//@ConditionalOnProperty(name = {"plugin-status.subscriber-redis-status", "plugin-rest-controller.redis-status"}, havingValue = "enable")
//public class QueueRedisRestController {
//
//    private final TempMapper tempMapper;
//    private final RedisKeyService redisKeyService;
//    private final SubscriberRedisService<Object> publisherRedisService;
//    private final SubscriberRedisService<Object> producerRedisService;
//
//    public QueueRedisRestController(TempMapper tempMapper,
//                                    RedisKeyService redisKeyService,
//                                    @Qualifier("publisherRedisServiceImpl") SubscriberRedisService<Object> publisherRedisService,
//                                    @Qualifier("producerRedisServiceImpl") SubscriberRedisService<Object> producerRedisService) {
//        this.tempMapper = tempMapper;
//        this.redisKeyService = redisKeyService;
//        this.publisherRedisService = publisherRedisService;
//        this.producerRedisService = producerRedisService;
//    }
//
//    @PostMapping("/publisher/send/message")
//    public Response<Object> publisherRedisService(@RequestBody Map<String, Object> bodyMap) {
//        log.info(LogEnmu.LOG1.value(), "publisher-redis-service-start");
//        int maxCnt = VarEnmu.TEN.ivalue();
//        for (int i = VarEnmu.ZERO.ivalue(); i < maxCnt; i++) {
//            bodyMap.put("count", i);
//            publisherRedisService.sendMessage("masterReceiverRedisImpl", bodyMap);
//            publisherRedisService.sendMessage("slaveReceiverRedisImpl", bodyMap);
//        }
//        log.info(LogEnmu.LOG1.value(), "publisher-redis-service-over");
//        return new Response<>().success().message("/publisher/send/message");
//    }
//
//    @PostMapping("/producer/send/message/{queueName}")
//    public Response<Object> producerRedisService(@PathVariable String queueName, @RequestBody String body) {
//        return producerRedisService.sendMessage(queueName, body);
//    }
//
//    @GetMapping("/producer/send/message/{streamKeyHeader}/{queueName}")
//    public Response<Object> producerRedisMessage(@PathVariable String streamKeyHeader,
//                                                 @PathVariable String queueName,
//                                                 @RequestHeader(value = "range-cnt", defaultValue = "1") int rangeCnt,
//                                                 @RequestHeader(value = "item-cnt", defaultValue = "10") int itemCnt) {
//        IntStream.range(VarEnmu.ONE.ivalue(), VarEnmu.ONE.ivalue() + rangeCnt)
//            .forEach(index -> {
//                List<Map<String, Object>> itemList = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());
//                IntStream.range(VarEnmu.ONE.ivalue(), VarEnmu.ONE.ivalue() + itemCnt)
//                    .forEach(idx -> {
//                        Map<String, Object> itemMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
//                        itemMap.put(VarEnmu.ID.value(), idx);
//                        itemMap.put(VarEnmu.TOKEN.value(), VarEnmu.TOKEN.value().concat(String.valueOf(idx)));
//                        itemList.add(itemMap);
//                    });
//                producerRedisService.sendMessage(streamKeyHeader, queueName, itemList);
//            });
//        return new Response<>().success();
//    }
//
//    @GetMapping("/producer/test")
//    public Response<Object> producerTest() {
//        log.info(LogEnmu.LOG1.value(), "producer-redis-service-start");
//        int maxCnt = VarEnmu.TEN.ivalue();
//        for (int i = VarEnmu.ZERO.ivalue(); i < maxCnt; i++) {
//            producerHandler(i);
//        }
//        log.info(LogEnmu.LOG1.value(), "producer-redis-service-over");
//        return new Response<>().success();
//    }
//
//    private void producerHandler(int i) {
//        Temp temp = new Temp();
//        temp.setId(redisKeyService.getPrimary());
//        temp.setC1("master");
//        temp.setC2(RandomUtil.getRandomChar(10));
//        temp.setC3(LocalDateTime.now());
//        temp.setC5(LocalDate.now());
//        temp.setC4(BigDecimal.valueOf(i));
//        tempMapper.create(temp);
//
//        /* master */
//        Response<Object> response = producerRedisService.sendMessage(temp.getC1(), temp);
//        log.info(LogEnmu.LOG4.value(), "producer-test-master", i, response.success(), response.getData());
//
//        /* slave */
//        temp.setId(redisKeyService.getPrimary());
//        temp.setC1("slave");
//        tempMapper.create(temp);
//        response = producerRedisService.sendMessage(temp.getC1(), temp);
//        log.info(LogEnmu.LOG4.value(), "producer-test-slave", i, response.success(), response.getData());
//    }
//
//}
