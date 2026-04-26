package com.dawn.plugin.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.Padding;
import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.mapper.ccore.TabServerMapper;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.util.CryptoUtil;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import com.dawn.plugin.util.SensitiveUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
//    private final PluginRestClient pluginRestClient;
    @Value("${plugin-params.rest-client-url}")
    private String restClientUrl;
    private final TabServerMapper tabServerMapper;
//    private final TestSimpleTask testSimpleTask;
    private final RedisKeyService redisKeyService;

    public AssistantServiceRestController(PluginConfig config,
//                                          PluginRestClient pluginRestClient,
                                          TabServerMapper tabServerMapper,
//                                          TestSimpleTask testSimpleTask,
                                          RedisKeyService redisKeyService) {
        this.config = config;
        this.tabServerMapper = tabServerMapper;
//        this.testSimpleTask = testSimpleTask;
//        this.pluginRestClient = pluginRestClient;
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
//
//    @SneakyThrows
//    @GetMapping("/rest-client")
//    public Response<Object> restClient() {
//        var resMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
//        var res = pluginRestClient.clientGetJson(restClientUrl);
//        resMap.put("clientGetJson", res);
//        res = pluginRestClient.clientPostJson(restClientUrl, "{\"name\": \"rest-client\"}");
//        resMap.put("clientPostJson", res);
//        return new Response<>().data(resMap).success();
//    }

    @SneakyThrows
    @PostMapping("/crypto")
    public Response<Object> crypto(@RequestBody String body) {
        Map<String, String> cryptoMap = config.getMapperLowerCamel().readValue(body, Map.class);
        var algorithmType = cryptoMap.getOrDefault(VarEnmu.TYPE.value(), VarEnmu.NONE.value());
        var data = cryptoMap.getOrDefault(VarEnmu.DATA.value(), VarEnmu.NONE.value());
        var algorithmKey = cryptoMap.get(AlgEnmu.ALGORITHM_KEY.algorithm());
        var algorithmIv = cryptoMap.getOrDefault(AlgEnmu.ALGORITHM_IV.algorithm(), algorithmKey);
        var privateKey = cryptoMap.get(VarEnmu.PRIVATE_KEY.value());
        var publicKey = cryptoMap.get(VarEnmu.PUBLIC_KEY.value());
        String value0;
        String value1 = switch (algorithmType) {
            case "sm4-encrypto" -> {
                value0 = CryptoUtil.encryptoBase64BySm4Cbc(algorithmKey, algorithmIv, data, Padding.PKCS5Padding, VarEnmu.UTF8.value());
                yield CryptoUtil.decodeBase64BySm4Cbc(algorithmKey, algorithmIv, value0, Padding.PKCS5Padding, VarEnmu.UTF8.value());
            }
            case "sm4-decrypto" -> {
                value0 = CryptoUtil.decodeBase64BySm4Cbc(algorithmKey, algorithmIv, data, Padding.PKCS5Padding, VarEnmu.UTF8.value());
                yield CryptoUtil.encryptoBase64BySm4Cbc(algorithmKey, algorithmIv, value0, Padding.PKCS5Padding, VarEnmu.UTF8.value());
            }
            case "aes-encrypto" -> {
                value0 = CryptoUtil.encryptoBase64ByWorld(algorithmKey, algorithmIv, data, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
                yield CryptoUtil.decodeBase64ByWorld(algorithmKey, algorithmIv, value0, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
            }
            case "aes-decrypto" -> {
                value0 = CryptoUtil.decodeBase64ByWorld(algorithmKey, algorithmIv, data, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
                yield CryptoUtil.encryptoBase64ByWorld(algorithmKey, algorithmIv, value0, AlgEnmu.AES.transformation(), AlgEnmu.AES.algorithm(), VarEnmu.UTF8.value());
            }
            case "sm2-encrypto" -> {
                value0 = CryptoUtil.encryptoBase64BySm2(data, publicKey);
                yield CryptoUtil.decodeBase64BySm2(value0, privateKey);
            }
            case "sm2-decryp" -> {
                value0 = CryptoUtil.decodeBase64BySm2(data, privateKey);
                yield CryptoUtil.encryptoBase64BySm2(value0, publicKey);
            }
            case "rsa-encryp" -> {
                value0 = CryptoUtil.encryptoBase64BySm2(data, publicKey);
                yield CryptoUtil.decodeBase64BySm2(value0, privateKey);
            }
            case "rsa-decryp" -> {
                value0 = CryptoUtil.decodeBase64BySm2(data, privateKey);
                yield CryptoUtil.encryptoBase64BySm2(value0, publicKey);
            }
            default -> {
                value0 = data;
                yield data;
            }
        };
        cryptoMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ZERO.value()), value0);
        cryptoMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ONE.value()), value1);
        if (algorithmType.contains("SM2")) {
            cryptoMap.put(VarEnmu.MESSAGE.value(), value1.equals(VarEnmu.NONE.value()) ? "结果不可用" : "结果可用");
        } else {
            cryptoMap.put(VarEnmu.MESSAGE.value(), value1.equals(data) ? "结果可用" : "结果不可用");
        }
        cryptoMap.put(AlgEnmu.ALGORITHM_KEY.algorithm(), RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue()));
        cryptoMap.put(VarEnmu.TYPE.value().concat(VarEnmu.ONE.value()), "sm4-encryp");
        cryptoMap.put(VarEnmu.TYPE.value().concat(VarEnmu.TWO.value()), "sm4-decryp");
        cryptoMap.put(VarEnmu.TYPE.value().concat(VarEnmu.THREE.value()), "aes-encryp");
        cryptoMap.put(VarEnmu.TYPE.value().concat(VarEnmu.FOUR.value()), "aes-decryp");
        cryptoMap.put(VarEnmu.TYPE.value().concat(VarEnmu.FIVE.value()), "sm2-encryp");
        cryptoMap.put(VarEnmu.TYPE.value().concat(VarEnmu.SIX.value()), "sm2-decryp");
        return value1.equals(VarEnmu.NONE.value())
            ? new Response<>().data(cryptoMap).success().message("结果无输出")
            : new Response<>().data(cryptoMap).success().message("结果已输出");
    }

    @SneakyThrows
    @PostMapping("/cryp2")
    public Response<Object> cryp2(@RequestBody String body) {
        Map<String, String> cryptoMap = config.getMapperLowerCamel().readValue(body, Map.class);
        var algorithmType = cryptoMap.getOrDefault(VarEnmu.TYPE.value(), VarEnmu.NONE.value());
        var data = cryptoMap.getOrDefault(VarEnmu.DATA.value(), VarEnmu.NONE.value());
        String value0;
        String value1;
        switch (algorithmType) {
            case "base64-encode":
                value0 = Base64.encode(data);
                value1 = Base64.decodeStr(value0);
                break;
            case "base64-decode":
                value0 = Base64.decodeStr(data);
                value1 = Base64.encode(value0);
                break;
            default:
                value0 = data;
                value1 = data;
        }
        cryptoMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ZERO.value()), value0);
        cryptoMap.put(VarEnmu.VALUE.value().concat(VarEnmu.ONE.value()), value1);
        cryptoMap.put(VarEnmu.MESSAGE.value(), value1.equals(data) ? "结果可用" : "结果不可用2");

        cryptoMap.put(AlgEnmu.ALGORITHM_KEY.algorithm(), RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue()));
        cryptoMap.put(VarEnmu.TYPE.value().concat(VarEnmu.SEVEN.value()), "base64-encode");
        cryptoMap.put(VarEnmu.TYPE.value().concat(VarEnmu.EIGHT.value()), "base64-decode");
        return value1.equals(data)
            ? new Response<>().data(cryptoMap).success().message("结果可用")
            : new Response<>().data(cryptoMap).success().message("结果不可用2");
    }
//
//    @GetMapping("/thread-pool/{closeErrTest}")
//    public Response<Object> testTask(@PathVariable("closeErrTest") boolean closeErrTest) {
//        List<Integer> numbers = IntStream
//            .range(VarEnmu.ONE.ivalue(), VarEnmu.NUMBER_1000.ivalue() * VarEnmu.TEN.ivalue())
//            .boxed()
//            .toList();
//        /* 激进测试 */
//        numbers
//            .parallelStream()
//            .forEach(i -> {
//                try {
//                    testSimpleTask.task1(closeErrTest);
//                    var task = testSimpleTask.task2();
//                    log.info(LogEnmu.LOG2.value(), "线程池", "task2", task.get());
//                } catch (InterruptedException | ExecutionException e) {
//                    Thread.currentThread().interrupt();
//                    log.warn(LogEnmu.LOG2.value(), "线程中断", e.toString());
//                }
//            });
//
//        return new Response<>().success().data(config.getApplicationId()).message(springApplicationName);
//    }

}
