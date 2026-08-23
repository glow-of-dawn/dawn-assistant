package com.dawn.plugin.controller.service;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.httpclient.PluginRestClient;
import com.dawn.plugin.thread.TestSimpleTask;
import com.dawn.plugin.util.Response;
import com.dawn.plugin.util.SensitiveUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

/**
 *
 * 创建时间 2026/8/20 23:02
 *
 * @author bhyt2
 */
@Slf4j
@ConditionalOnProperty(name = {"plugin-rest-controller.svr-status"}, havingValue = "enable", matchIfMissing = true)
public class SvrService {

    @Value("${spring.application.name}")
    private String springApplicationName;
    private final PluginConfig config;
    private final PluginRestClient pluginRestClient;
    @Value("${plugin-params.rest-client-url}")
    private String restClientUrl;
    private final TestSimpleTask testSimpleTask;

    public SvrService(PluginConfig config,
                      PluginRestClient pluginRestClient,
                      TestSimpleTask testSimpleTask) {
        this.config = config;
        this.testSimpleTask = testSimpleTask;
        this.pluginRestClient = pluginRestClient;
    }

    public Response<Object> logSensitive(String logSensitive) {
        log.info(LogEnmu.LOG_SENSITIVE_STATUS.value(), logSensitive);
        return new Response<>().success().message("日志脱敏:".concat(logSensitive));
    }

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

    public Response<Object> getSsrfWhiteList() {
        return new Response<>().data(Map.of(
            "host", config.getSsrfHostWhiteList(),
            "path", config.getSsrfPathWhiteList()
        )).success();
    }

    public Response<Object> setSsrfWhiteList(String body) {
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

    public Response<Object> testTask(boolean closeErrTest,
                                     int multipleSize) {
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
