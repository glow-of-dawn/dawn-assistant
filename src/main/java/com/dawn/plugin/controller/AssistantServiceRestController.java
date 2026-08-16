package com.dawn.plugin.controller;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.httpclient.PluginRestClient;
import com.dawn.plugin.mapper.ccore.TabServerMapper;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.thread.TestSimpleTask;
import com.dawn.plugin.util.Response;
import com.dawn.plugin.util.SensitiveUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

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
    private final PluginRestClient pluginRestClient;
    @Value("${plugin-params.rest-client-url}")
    private String restClientUrl;
    private final TabServerMapper tabServerMapper;
    private final TestSimpleTask testSimpleTask;
    private final RedisKeyService redisKeyService;

    public AssistantServiceRestController(PluginConfig config,
                                          PluginRestClient pluginRestClient,
                                          TabServerMapper tabServerMapper,
                                          TestSimpleTask testSimpleTask,
                                          RedisKeyService redisKeyService) {
        this.config = config;
        this.tabServerMapper = tabServerMapper;
        this.testSimpleTask = testSimpleTask;
        this.pluginRestClient = pluginRestClient;
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

    @GetMapping("/log-sensitive/{logSensitive}")
    public Response<Object> logSensitive(@PathVariable("logSensitive") String logSensitive) {
        log.info(LogEnmu.LOG_SENSITIVE_STATUS.value(), logSensitive);
        return new Response<>().success().message("日志脱敏:".concat(logSensitive));
    }

    @GetMapping("/logs/assistant")
    public Response<Object> logs() {
        log.info(LogEnmu.LOG2.value(), "日志脱敏", "测试");
        log.info(LogEnmu.LOG2.value(), "111122224444477777", "测试测试测试测试测试测试测试测试测试测试");
        log.info(LogEnmu.LOG3.value(), "13668200646", "15222222222", "15648523699");
        log.info(LogEnmu.LOG1.value(), SensitiveUtil.desensitization("张三"));
        log.info(LogEnmu.LOG3.value(), "150303195208077885", "15030319520807158X", "15030319520807908X");
        log.info(LogEnmu.LOG1.value(), "621483958546999");
        log.info(LogEnmu.LOG1.value(), "6214 8395 8546 999");
        Map<String, String> params = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        params.put("Phones", SensitiveUtil.desensitization("13668200646,15222222222,15648523699"));
        params.put("timestamp", "1231");
        params.put("NAME", SensitiveUtil.desensitization("张三"));
        params.put("身份证", SensitiveUtil.desensitization("150303195208077885,15030319520807158X,15030319520807908X"));
        log.info(LogEnmu.LOG2.value(), "map", params);
        log.info(LogEnmu.LOG1.value(), "over");
        return new Response<>().data(params).success();
    }

    @GetMapping("/http/clinet/ssrf/white/list")
    public Response<Object> getSsrfWhiteList() {
        return new Response<>().data(Map.of(
            "host", config.getSsrfHostWhiteList(),
            "path", config.getSsrfPathWhiteList()
        )).success();
    }

    @Authtoken(openAuthtoken = true)
    @PostMapping("/http/clinet/ssrf/white/list")
    public Response<Object> setSsrfWhiteList(@RequestBody String body) {
        Map<String, String> ssrfMap = config.getMapperLowerCamel().readValue(body, Map.class);
        ssrfMap.forEach((k, v) -> {
            Optional.ofNullable(k)
                .filter(StringUtils::isNotBlank)
                .ifPresent(config.getSsrfHostWhiteList()::add);
            Optional.ofNullable(v)
                .filter(StringUtils::isNotBlank)
                .ifPresent(config.getSsrfPathWhiteList()::add);
        });
        return new Response<>().data(Map.of(
            "host", config.getSsrfHostWhiteList(),
            "path", config.getSsrfPathWhiteList()
        )).success();
    }

    @SneakyThrows
    @GetMapping("/rest-client")
    public Response<Object> restClient() {
        var resMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        var res = pluginRestClient.clientGetJson(restClientUrl);
        resMap.put("clientGetJson", res);
        res = pluginRestClient.clientPostJson(restClientUrl, "{\"name\": \"rest-client\"}");
        resMap.put("clientPostJson", res);
        return new Response<>().data(resMap).success();

        // var url = "http://localhost:8080/yc-mvp-assistant/rest/";
        // URI uri = new URI(url);
        // var response = httpClient.exchangeJson(uri.resolve("assistant/service/health-read"));
        // log.info(LogEnmu.LOG4.value(), "http-clinet-1", response.getCode(), response.getMessage(), response.getData());

        // var body = "{\"name\": \"中文\",\"id\": \"6\",\"algorithm\": \"AES\",\"\": \"9000\"}";
        // response = httpClient.exchangeJson(uri.resolve("authtoken/account/aes/user/none"), body);
        // log.info(LogEnmu.LOG4.value(), "http-clinet-2", response.getCode(), response.getMessage(), response.getData());

        // String rebody = response.getData();
        // var resMap = config.getMapperLowerCamel().readValue(rebody, Map.class);
        // Map<String, String> datMap = (Map)resMap.get(VarEnmu.DATA.value());
        // Map<String, String> map = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        // map.put(VarEnmu.TIMESTAMP.value(), String.valueOf(resMap.get(VarEnmu.TIMESTAMP.value())));
        // map.put(AlgEnmu.ONCE.algorithm(), RandomUtil.getRandomChar(VarEnmu.SIX.ivalue()));
        // map.put(VarEnmu.AUTH_TOKEN.value(), String.valueOf(datMap.get("atoken")));
        // body = "{\"id\": \"1\"}";
        // response = httpClient.exchangeJson(uri.resolve("database/service/edit/params"), map, body);
        // log.info(LogEnmu.LOG5.value(), "http-clinet-3", response.getCode(), response.getMessage(), response.getData());

        // return response;
    }

    @GetMapping("/thread-pool/{closeErrTest}/{multipleSize}")
    public Response<Object> testTask(@PathVariable("closeErrTest") boolean closeErrTest,
                                     @PathVariable("multipleSize") int multipleSize) {
        List<Integer> numbers = IntStream
            .range(VarEnmu.ONE.ivalue(), VarEnmu.NUMBER_1000.ivalue() * multipleSize)
            .boxed()
            .toList();
        /* 激进测试 */
        numbers
            .parallelStream()
            .forEach(_ -> {
                try {
                    testSimpleTask.task1(closeErrTest);
                    var task = testSimpleTask.task2();
                    log.info(LogEnmu.LOG2.value(), "线程池", "task2", task.get());
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    log.warn(LogEnmu.LOG2.value(), "线程中断", e.toString());
                }
            });

        return new Response<>().success().data(config.getApplicationId()).message(springApplicationName);
    }

}
