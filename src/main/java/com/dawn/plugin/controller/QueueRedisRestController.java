package com.dawn.plugin.controller;

import com.dawn.plugin.controller.service.QueueRedisRestService;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * [消息队列]
 * 创建时间：2021/5/30 20:10
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/queue/redis")
@ConditionalOnProperty(name = {"plugin-status.subscriber-redis-status", "plugin-rest-controller.redis-status"}, havingValue = "enable")
public class QueueRedisRestController {

    private final QueueRedisRestService queueRedisRestService;

    public QueueRedisRestController(QueueRedisRestService queueRedisRestService) {
        this.queueRedisRestService = queueRedisRestService;
    }

    @PostMapping("/publisher/send/message")
    public Response<Object> publisherRedisService(@RequestBody Map<String, Object> bodyMap) {
        return queueRedisRestService.publisherRedisService(bodyMap);
    }

    @PostMapping("/producer/send/message/{queueName}")
    public Response<Object> producerRedisService(@PathVariable String queueName, @RequestBody String body) {
        return queueRedisRestService.producerRedisService(queueName, body);
    }

    @GetMapping("/producer/send/message/{streamKeyHeader}/{queueName}")
    public Response<Object> producerRedisMessage(@PathVariable String streamKeyHeader,
                                                 @PathVariable String queueName,
                                                 @RequestHeader(value = "range-cnt", defaultValue = "1") int rangeCnt,
                                                 @RequestHeader(value = "item-cnt", defaultValue = "10") int itemCnt) {
        return queueRedisRestService.producerRedisMessage(streamKeyHeader, queueName, rangeCnt, itemCnt);
    }

    @GetMapping("/producer/test")
    public Response<Object> producerTest() {
        return queueRedisRestService.producerTest();
    }

}
