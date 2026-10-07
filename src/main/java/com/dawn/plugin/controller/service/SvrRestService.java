package com.dawn.plugin.controller.service;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.httpclient.PluginRestClient;
import com.dawn.plugin.thread.TestSimpleTask;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import com.dawn.plugin.util.SensitiveUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

/**
 *
 * 创建时间 2026/8/20 23:02
 *
 * @author bhyt2
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-rest-controller.svr-status"}, havingValue = "enable", matchIfMissing = true)
public class SvrRestService {

    @Value("${spring.application.name}")
    private String springApplicationName;
    @Value("${plugin-params.rest-client-url}")
    private String restClientUrl;
    private final ApplicationContext applicationContext;
    private final PluginConfig config;
    private final PluginRestClient pluginRestClient;
    private final TestSimpleTask testSimpleTask;

    public SvrRestService(PluginConfig config,
                          PluginRestClient pluginRestClient,
                          ApplicationContext applicationContext,
                          TestSimpleTask testSimpleTask) {
        this.config = config;
        this.pluginRestClient = pluginRestClient;
        this.applicationContext = applicationContext;
        this.testSimpleTask = testSimpleTask;
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
            VarEnmu.HOST.value(), config.getSsrfHostWhiteList(),
            VarEnmu.PATH.value(), config.getSsrfPathWhiteList()
        )).success();
    }

    public Response<Object> setSsrfWhiteList(String body) {
        Map<String, Object> ssrfMap = config.getMapperLowerCamel().readValue(body, Map.class);
        if(ssrfMap.get(VarEnmu.HOST.value()) instanceof List<?> list) {
            list.stream()
                .filter(host -> host instanceof String str && StringUtils.isNotBlank(str))
                .forEach(host -> config.getSsrfHostWhiteList().add(String.valueOf(host)));
        }
        if(ssrfMap.get(VarEnmu.PATH.value()) instanceof List<?> list) {
            list.stream()
                .filter(path -> path instanceof String str && StringUtils.isNotBlank(str))
                .forEach(path -> config.getSsrfPathWhiteList().add(String.valueOf(path)));
        }
        return new Response<>().data(Map.of(
            VarEnmu.HOST.value(), config.getSsrfHostWhiteList(),
            VarEnmu.PATH.value(), config.getSsrfPathWhiteList()
        )).success();
    }

    public Response<Object> restClient() throws URISyntaxException {
        URI uri = new URI(restClientUrl);
        var response = pluginRestClient.exchangeGet(uri.resolve("assistant/service/health-read"), MediaType.APPLICATION_JSON);
        log.info(LogEnmu.LOG4.value(), "http-clinet-1", response.getCode(), response.getMessage(), response.getData());

        var body = "{\"name\": \"中文\",\"id\": \"6\",\"algorithm\": \"AES\",\"\": \"9000\"}";
        response = pluginRestClient.exchangeJson(uri.resolve("authtoken/account/aes/user/none"), body);
        log.info(LogEnmu.LOG4.value(), "http-clinet-2", response.getCode(), response.getMessage(), response.getData());

        String rebody = response.getData();
        var resMap = config.getMapperLowerCamel().readValue(rebody, Map.class);
        Map<String, String> datMap = (Map) resMap.get(VarEnmu.DATA.value());
        Map<String, String> map = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        map.put(VarEnmu.TIMESTAMP.value(), String.valueOf(resMap.get(VarEnmu.TIMESTAMP.value())));
        map.put(AlgEnmu.ONCE.algorithm(), RandomUtil.getRandomChar(VarEnmu.SIX.ivalue()));
        map.put(VarEnmu.AUTH_TOKEN.value(), String.valueOf(datMap.get(VarEnmu.AUTH_TOKEN.value())));
        body = """
            {
                "id": "1",
                "paramsName": "algorithm",
                "paramsValue": "%s",
                "paramsClass": "assistant",
                "paramsAbs": "assistant test",
                "paramsKey": "%s"
            }
            """.formatted(map.get(VarEnmu.TIMESTAMP.value()), map.get(AlgEnmu.ONCE.algorithm()));
        response = pluginRestClient.exchangeJson(uri.resolve("database/service/edit/params"), map, body);
        log.info(LogEnmu.LOG5.value(), "http-clinet-3", response.getCode(), response.getMessage(), response.getData());

        return response;
    }

    public Response<Object> testTask(boolean closeErrTest,
                                     int multipleSize) {
        List<Integer> numbers = IntStream
            .range(VarEnmu.ONE.ivalue(), multipleSize)
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

    public Response<Object> shutdown() {
        ConfigurableApplicationContext cyx = (ConfigurableApplicationContext) this.applicationContext;
        cyx.close();
        return new Response<>().message("shutdown").success();
    }

}
