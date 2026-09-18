package com.dawn.plugin.controller.service;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ctemp.Temp;
import com.dawn.plugin.mapper.ctemp.TempMapper;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.subscriber.SubscriberRedisService;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * [消息队列]
 * 创建时间 2026/8/26 20:38
 *
 * @author bhyt2
 */
@Slf4j
@ConditionalOnProperty(name = {"plugin-rest-controller.svr-status"}, havingValue = "enable", matchIfMissing = true)
public class QueueRedisRestService {

    private final TempMapper tempMapper;
    private final RedisKeyService redisKeyService;
    private final SubscriberRedisService<Object> publisherRedisService;
    private final SubscriberRedisService<Object> producerRedisService;

    public QueueRedisRestService(TempMapper tempMapper,
                                 RedisKeyService redisKeyService,
                                 @Qualifier("publisherRedisServiceImpl") SubscriberRedisService<Object> publisherRedisService,
                                 @Qualifier("producerRedisServiceImpl") SubscriberRedisService<Object> producerRedisService) {
        this.tempMapper = tempMapper;
        this.redisKeyService = redisKeyService;
        this.publisherRedisService = publisherRedisService;
        this.producerRedisService = producerRedisService;
    }

    public Response<Object> publisherRedisService(Map<String, Object> bodyMap) {
        log.info(LogEnmu.LOG1.value(), "publisher-redis-service-start");
        int maxCnt = VarEnmu.TEN.ivalue();
        for (int i = VarEnmu.ZERO.ivalue(); i < maxCnt; i++) {
            bodyMap.put("count", i);
            publisherRedisService.sendMessage("masterReceiverRedisImpl", bodyMap);
            publisherRedisService.sendMessage("slaveReceiverRedisImpl", bodyMap);
        }
        log.info(LogEnmu.LOG1.value(), "publisher-redis-service-over");
        return new Response<>().success().message("/publisher/send/message");
    }

    public Response<Object> producerRedisService(String queueName, String body) {
        return producerRedisService.sendMessage(queueName, body);
    }

    public Response<Object> producerRedisMessage(String streamKeyHeader,
                                                 String queueName,
                                                 int rangeCnt,
                                                 int itemCnt) {
        IntStream.range(VarEnmu.ONE.ivalue(), VarEnmu.ONE.ivalue() + rangeCnt)
            .forEach(index -> {
                List<Map<String, Object>> itemList = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());
                IntStream.range(VarEnmu.ONE.ivalue(), VarEnmu.ONE.ivalue() + itemCnt)
                    .forEach(idx -> {
                        Map<String, Object> itemMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
                        itemMap.put(VarEnmu.ID.value(), idx);
                        itemMap.put(VarEnmu.TOKEN.value(), VarEnmu.TOKEN.value().concat(String.valueOf(idx)));
                        itemList.add(itemMap);
                    });
                producerRedisService.sendMessage(streamKeyHeader, queueName, itemList);
            });
        return new Response<>().success();
    }

    public Response<Object> producerTest() {
        log.info(LogEnmu.LOG1.value(), "producer-redis-service-start");
        int maxCnt = VarEnmu.TEN.ivalue();
        for (int i = VarEnmu.ZERO.ivalue(); i < maxCnt; i++) {
            producerHandler(i);
        }
        log.info(LogEnmu.LOG1.value(), "producer-redis-service-over");
        return new Response<>().success();
    }

    private void producerHandler(int i) {
        Temp temp = new Temp();
        temp.setId(redisKeyService.getPrimary());
        temp.setC1("master");
        temp.setC2(RandomUtil.getRandomChar(10));
        temp.setC3(LocalDateTime.now(PluginConfig.ZONE));
        temp.setC5(LocalDate.now(PluginConfig.ZONE));
        temp.setC4(BigDecimal.valueOf(i));
        tempMapper.create(temp);

        /* master */
        Response<Object> response = producerRedisService.sendMessage(temp.getC1(), temp);
        log.info(LogEnmu.LOG4.value(), "producer-test-master", i, response.success(), response.getData());

        /* slave */
        temp.setId(redisKeyService.getPrimary());
        temp.setC1("slave");
        tempMapper.create(temp);
        response = producerRedisService.sendMessage(temp.getC1(), temp);
        log.info(LogEnmu.LOG4.value(), "producer-test-slave", i, response.success(), response.getData());
    }

}
