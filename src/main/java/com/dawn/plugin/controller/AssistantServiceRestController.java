package com.dawn.plugin.controller;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.mapper.ccore.TabServerMapper;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * [服务器信息]
 * 创建时间：2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/assistant/service")
@ConditionalOnProperty(name = {"plugin-rest-controller.assistant-status"}, havingValue = "enable", matchIfMissing = true)
public class AssistantServiceRestController {

    @Value("${spring.application.name}")
    private String springApplicationName;
    @Value("${server.port}")
    private String port;
    @Value("#{'${spring.yml-redis:config/redis.yml from def}'}")
    private String ymlRedis;
    @Value("#{'${spring.yml-plugin:config/plugin.yml from def}'}")
    private String ymlPlugin;
    @Value("#{'${spring.yml-datasources:config/datasources.yml from def}'}")
    private String ymlDatasources;
    private final PluginConfig config;
    private final TabServerMapper tabServerMapper;
    private final RedisKeyService redisKeyService;

    public AssistantServiceRestController(PluginConfig config,
                                          TabServerMapper tabServerMapper,
                                          RedisKeyService redisKeyService) {
        this.config = config;
        this.tabServerMapper = tabServerMapper;
        this.redisKeyService = redisKeyService;
    }

    @GetMapping("/info")
    public Response<Object> getServiceInfo() {
        return new Response<>().success().data(config.getApplicationId()).message(springApplicationName);
    }

    @PostMapping("/info")
    public Response<Object> postServiceInfo(@RequestBody String body) {
        var map = getServiceInfo(body);
        return new Response<>().data(map).success();
    }

    private Map<String, Object> getYmlLoader() {
        Map<String, Object> map = LinkedHashMap.newLinkedHashMap(VarEnmu.SIXTEEN.ivalue());
        map.put("yml-redis", ymlRedis);
        map.put("yml-plugin", ymlPlugin);
        map.put("yml-datasources", ymlDatasources);
        return map;
    }

    @GetMapping("/health-live")
    public String healthLive() {
        return springApplicationName.concat(":").concat(config.getApplicationId());
    }

    @GetMapping(value = "/health-read", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<Object> healthRead(@RequestHeader(value = "health", defaultValue = "") String health) {
        if (VarEnmu.NONE.value().equals(health)) {
            return new Response<>().success().data(config.getApplicationId()).message(springApplicationName);
        } else {
            return new Response<>().success().data(getServiceInfo(health));
        }
    }

    private Map<String, Object> getServiceInfo(String body) {
        Map<String, Object> map = LinkedHashMap.newLinkedHashMap(VarEnmu.SIXTEEN.ivalue());
        map.put("serverName", springApplicationName);
        map.put("port", port);
        map.put("body", body);
        map.put("redisKeyService", redisKeyService.isRedisHealth());
        map.put("applicationId", config.getApplicationId());
        map.put("yml-loader", getYmlLoader());
        Optional.ofNullable(tabServerMapper.find(config.getApplicationId()))
            .ifPresent(tabServer -> {
                tabServer.setApplicationSts(CodeEnmu.STS_A.code());
                tabServer.setReadCnt(tabServer.getReadCnt() + 1);
                tabServerMapper.edit(tabServer);
            });
        return map;
    }

}
