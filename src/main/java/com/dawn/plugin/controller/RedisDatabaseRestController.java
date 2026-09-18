package com.dawn.plugin.controller;

import com.dawn.plugin.controller.service.RedisDatabaseRestService;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private final RedisDatabaseRestService redisDatabaseRestService;

    public RedisDatabaseRestController(RedisDatabaseRestService redisDatabaseRestService) {
        this.redisDatabaseRestService = redisDatabaseRestService;
    }

    @GetMapping("/redis/live")
    public Response<Object> redisLive() {
        return redisDatabaseRestService.redisLive();
    }

    @GetMapping("/redis/primary-key/{count}/{threadCnt}")
    public Response<Object> getPrimaryKeyFromRedis(@PathVariable Integer count,
                                                   @PathVariable Integer threadCnt) {
        return redisDatabaseRestService.getPrimaryKeyFromRedis(count, threadCnt);
    }

    @GetMapping("/redis-vs-database")
    public Response<Object> redisVsDatabase() {
        return redisDatabaseRestService.redisVsDatabase();
    }

}
