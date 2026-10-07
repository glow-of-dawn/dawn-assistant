package com.dawn.plugin.controller;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.controller.service.AssistantRestService;
import com.dawn.plugin.controller.service.AuthTokenAccountsRestService;
import com.dawn.plugin.controller.service.CryptRestService;
import com.dawn.plugin.controller.service.SvrRestService;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.httpclient.PluginRestClient;
import com.dawn.plugin.util.CryptUtil;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 联合测试
 * 创建时间 2026/10/6 09:23
 *
 * @author bhyt2
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/mul")
@ConditionalOnProperty(name = {"plugin-rest-controller.mul-status"}, havingValue = "enable", matchIfMissing = true)
public class MulRestController {

    private final String reg = """
        {
            "name": "aaa",
            "id": "6",
            "": "%s"
        }
        """.formatted(VarEnmu.DEF_USERID.value());
    private final URI uri;
    private String authToken = VarEnmu.NONE.value();
    private String algorithm = AlgEnmu.AES.algorithm();
    private String algorithmKey = VarEnmu.NONE.value();
    private AssistantRestService assistantRestService;
    private AuthTokenAccountsRestService authTokenAccountsRestService;
    private SvrRestService svrService;
    private CryptRestService cryptRestService;
    private final PluginConfig config;
    private final PluginRestClient pluginRestClient;
    private final RedisTemplate<String, String> redisTemplate;

    public MulRestController(@Value("${plugin-params.rest-client-url:}") String restClientUrl,
                             PluginConfig config,
                             PluginRestClient pluginRestClient,
                             RedisTemplate<String, String> redisTemplate) throws URISyntaxException {
        this.config = config;
        this.pluginRestClient = pluginRestClient;
        this.redisTemplate = redisTemplate;
        this.uri = new URI(restClientUrl);
    }

    @Autowired
    public void init(AssistantRestService assistantRestService,
                     AuthTokenAccountsRestService authTokenAccountsRestService,
                     SvrRestService svrService,
                     CryptRestService cryptRestService) {
        this.assistantRestService = assistantRestService;
        this.authTokenAccountsRestService = authTokenAccountsRestService;
        this.svrService = svrService;
        this.cryptRestService = cryptRestService;
    }

    @GetMapping("/base")
    public Response<Object> base() {
        var code = "mul-base";
        var responseServiceInfo = assistantRestService.getServiceInfo();
        log.info(LogEnmu.LOG4.value(), code, "getServiceInfo", responseServiceInfo.getCode(), responseServiceInfo.getMessage());

        responseServiceInfo = assistantRestService.postServiceInfo(RandomUtil.getRandomChar(VarEnmu.TEN.ivalue()));
        log.info(LogEnmu.LOG4.value(), code, "postServiceInfo", responseServiceInfo.getCode(), responseServiceInfo.getMessage());

        var msg = assistantRestService.healthLive();
        log.info(LogEnmu.LOG3.value(), code, "healthLive", msg);

        responseServiceInfo = assistantRestService.healthRead(VarEnmu.NONE.value());
        log.info(LogEnmu.LOG4.value(), code, "healthRead", responseServiceInfo.getCode(), responseServiceInfo.getMessage());

        return new Response<>().success().message("base is success");
    }

    @GetMapping("/svr")
    public Response<Object> svr() throws URISyntaxException {
        var code = "mul-svr";

        var body = """
            {
                "path": [
                    "/dawn-assistant/rest/svr/rest-client",
                    "/dawn-assistant/rest/authtoken/user/assert/exception",
                    "/dawn-assistant/rest/svr/shutdown",
                    "/dawn-assistant/rest/svr/logs/assistant",
                    "/dawn-assistant/rest/svr/log-sensitive/enable",
                    "/dawn-assistant/rest/svr/log-sensitive/disable",
                    "/dawn-assistant/rest/database/service/edit/params",
                    "/dawn-assistant/rest/svr/rest-client",
                    "/dawn-assistant/rest/assistant/service/health-read",
                    "/dawn-assistant/rest/authtoken/account/aes/user/none",
                    "/dawn-assistant/rest/authtoken/user/session/async",
                    "/dawn-assistant/rest/authtoken/user/self"
                ]
            }
            """;
        String ssrf = "svr/http/clinet/ssrf/white/list";
        pluginRestClient.exchangeJson(uri.resolve(ssrf), regUser(), body);
        var responseWhiteList = pluginRestClient.exchangeJson(uri.resolve(ssrf));
        log.info(LogEnmu.LOG4.value(), code, "ssrf/white/list", responseWhiteList.getCode(), responseWhiteList.getData());

        /* SvrRestController */
        pluginRestClient.exchangeGet(uri.resolve("svr/rest-client"), MediaType.APPLICATION_JSON);
        pluginRestClient.exchangeGet(uri.resolve("svr/logs/assistant"), MediaType.APPLICATION_JSON);
        pluginRestClient.exchangeGet(uri.resolve("svr/log-sensitive/disable"), MediaType.APPLICATION_JSON);
        log.info(LogEnmu.LOG2.value(), code, "svr-logs-assistant-disable", MediaType.APPLICATION_JSON);
        pluginRestClient.exchangeGet(uri.resolve("svr/logs/assistant"), MediaType.APPLICATION_JSON);
        log.info(LogEnmu.LOG2.value(), code, "svr-logs-assistant-enable");

        svrService.testTask(true, VarEnmu.THREE.ivalue());
        svrService.testTask(false, VarEnmu.THREE.ivalue());

        /* AuthTokenUserRestController */
        pluginRestClient.exchangeJson(uri.resolve("authtoken/user/assert/exception"), regUser(), body);
        var responseUser = pluginRestClient.exchangeGet(uri.resolve("authtoken/user/self"), regUser(), MediaType.APPLICATION_JSON);
        log.info(LogEnmu.LOG4.value(), code, "authtoken/user/self", responseUser.getCode(), responseUser.getData());
        body = """
            {
                "userid"   : "9988",
                "groupid"  : "8877",
                "orgtypeid": "7766"
            }
            """;
        Map<String, String> headers = regUser();
        var result = CryptUtil.crypt(algorithm.toLowerCase().concat(VarEnmu.SLIGHTLY.value()).concat(AlgEnmu.ENCRYPT.algorithm()),
            body, algorithmKey, algorithmKey, VarEnmu.NONE.value(), VarEnmu.NONE.value());
        var enVal = result[VarEnmu.ZERO.ivalue()];
        String content = algorithmKey.concat(enVal).concat(headers.get(VarEnmu.TIMESTAMP.value()));
        var sign = DigestUtils.sha256Hex(content);
        headers.put(AlgEnmu.SIGNATURE.algorithm(), sign);
        responseUser = pluginRestClient.exchangeJson(uri.resolve("authtoken/user/session/async"), headers, enVal);
        result = CryptUtil.crypt(algorithm.toLowerCase().concat(VarEnmu.SLIGHTLY.value()).concat(AlgEnmu.DECRYPT.algorithm()),
            responseUser.getData(), algorithmKey, algorithmKey, VarEnmu.NONE.value(), VarEnmu.NONE.value());
        log.info(LogEnmu.LOG4.value(), code, "session-async", result[VarEnmu.ZERO.ivalue()], result[VarEnmu.ONE.ivalue()]);

        /* shutdown */
        pluginRestClient.exchangeGet(uri.resolve("svr/shutdown2"), regUser(), MediaType.APPLICATION_JSON);

        return new Response<>().success().message("svr is success");
    }

    @GetMapping("/authtoken")
    public Response<Object> authtoken() throws URISyntaxException {
        var code = "mul-authtoken";
        config.getSsrfPathWhiteList().add("/dawn-assistant/rest/authtoken/service/algorithm-key");
        var responseAuthtoken = pluginRestClient.exchangeGet(uri.resolve("authtoken/service/algorithm-key"), regUser(), MediaType.APPLICATION_JSON);
        log.info(LogEnmu.LOG4.value(), code, "algorithm-key", responseAuthtoken.getCode(), responseAuthtoken.getData());

        config.getSsrfPathWhiteList().add("/dawn-assistant/rest/authtoken/service/authtoken");
        responseAuthtoken = pluginRestClient.exchangeGet(uri.resolve("authtoken/service/authtoken"), regUser(), MediaType.APPLICATION_JSON);
        var result = CryptUtil.crypt(algorithm.toLowerCase().concat(VarEnmu.SLIGHTLY.value()).concat(AlgEnmu.DECRYPT.algorithm()),
            responseAuthtoken.getData(), algorithmKey, algorithmKey, VarEnmu.NONE.value(), VarEnmu.NONE.value());
        log.info(LogEnmu.LOG4.value(), code, "authtoken", result[VarEnmu.ZERO.ivalue()], result[VarEnmu.ONE.ivalue()]);

        var body = """
            {
                "a": "%s",
                "b": "%s",
                "c": "%s",
            }
            """.formatted(RandomUtil.getRandomChar(VarEnmu.TEN.ivalue()),
            RandomUtil.getRandomChar(VarEnmu.TEN.ivalue()),
            RandomUtil.getRandomChar(VarEnmu.TEN.ivalue()));
        result = CryptUtil.crypt(algorithm.toLowerCase().concat(VarEnmu.SLIGHTLY.value()).concat(AlgEnmu.ENCRYPT.algorithm()),
            body, algorithmKey, algorithmKey, VarEnmu.NONE.value(), VarEnmu.NONE.value());
        var enVal = result[VarEnmu.ZERO.ivalue()];
        config.getSsrfPathWhiteList().add("/dawn-assistant/rest/authtoken/service/append-encrypt");
        responseAuthtoken = pluginRestClient.exchangeJson(uri.resolve("authtoken/service/append-encrypt"), regUser(), enVal);
        result = CryptUtil.crypt(algorithm.toLowerCase().concat(VarEnmu.SLIGHTLY.value()).concat(AlgEnmu.DECRYPT.algorithm()),
            responseAuthtoken.getData(), algorithmKey, algorithmKey, VarEnmu.NONE.value(), VarEnmu.NONE.value());
        log.info(LogEnmu.LOG4.value(), code, "append-encrypt", result[VarEnmu.ZERO.ivalue()], result[VarEnmu.ONE.ivalue()]);

        config.getSsrfPathWhiteList().add("/dawn-assistant/rest/authtoken/service/signature");
        Map<String, String> headers = regUser();
        String content = algorithmKey.concat(enVal).concat(headers.get(VarEnmu.TIMESTAMP.value()));
        var sign = DigestUtils.sha256Hex(content);
        headers.put(AlgEnmu.SIGNATURE.algorithm(), sign);
        responseAuthtoken = pluginRestClient.exchangeJson(uri.resolve("authtoken/service/signature"), headers, enVal);
        result = CryptUtil.crypt(algorithm.toLowerCase().concat(VarEnmu.SLIGHTLY.value()).concat(AlgEnmu.DECRYPT.algorithm()),
            responseAuthtoken.getData(), algorithmKey, algorithmKey, VarEnmu.NONE.value(), VarEnmu.NONE.value());
        log.info(LogEnmu.LOG4.value(), code, "signature", result[VarEnmu.ZERO.ivalue()], result[VarEnmu.ONE.ivalue()]);

        config.getSsrfPathWhiteList().add("/dawn-assistant/rest/authtoken/service/signature2");
        headers = regUser();
        content = algorithmKey.concat(body).concat(headers.get(VarEnmu.TIMESTAMP.value()));
        sign = DigestUtils.sha256Hex(content);
        headers.put(AlgEnmu.SIGNATURE.algorithm(), sign);
        responseAuthtoken = pluginRestClient.exchangeJson(uri.resolve("authtoken/service/signature2"), headers, body);
        log.info(LogEnmu.LOG4.value(), code, "signature", responseAuthtoken.getCode(), responseAuthtoken.getData());

        return new Response<>().success().message("authtoken is success");
    }

    private Map<String, String> regUser() {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(config.getSpringApplicationName().concat(authToken)))) {
            var responseAuthToken = authTokenAccountsRestService.regUser(VarEnmu.DEF_USERID.value(), reg);
            log.info(LogEnmu.LOG3.value(), "regUser", responseAuthToken.getCode(), responseAuthToken.getMessage());
            Map<String, String> resMap = responseAuthToken.getData();
            authToken = resMap.get(VarEnmu.AUTH_TOKEN.value());
            algorithm = resMap.get(AlgEnmu.ALGORITHM.algorithm());
            algorithmKey = resMap.get(AlgEnmu.ALGORITHM_KEY.algorithm());
        }

        Map<String, String> headers = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        headers.put(VarEnmu.AUTH_TOKEN.value(), authToken);
        headers.put(AlgEnmu.ONCE.algorithm(), RandomUtil.getRandomChar(VarEnmu.SIX.ivalue()));
        headers.put(VarEnmu.TIMESTAMP.value(), String.valueOf(Instant.now().toEpochMilli()));
        return headers;
    }

    @GetMapping("/task")
    public Response<Object> task() throws URISyntaxException {
        var code = "mul-task";
        return new Response<>().success().message("task is success");
    }

    @GetMapping("/redis")
    public Response<Object> redis() throws URISyntaxException {
        var code = "mul-redis";
        return new Response<>().success().message("redis is success");
    }

    @GetMapping("/crypt")
    public Response<Object> crypt() throws URISyntaxException {
        var code = "mul-crypt";
        return new Response<>().success().message("crypt is success");
    }

}
