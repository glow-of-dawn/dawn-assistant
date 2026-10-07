package com.dawn.plugin.controller;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.controller.service.AssistantRestService;
import com.dawn.plugin.controller.service.AuthTokenAccountsRestService;
import com.dawn.plugin.controller.service.SvrRestService;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.httpclient.PluginRestClient;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Objects;

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
    @Value("${plugin-params.rest-client-url}")
    private String restClientUrl;
    private String authToken;
    private final AssistantRestService assistantRestService;
    private final AuthTokenAccountsRestService authTokenAccountsRestService;
    private final SvrRestService svrService;
    private final PluginConfig config;
    private final PluginRestClient pluginRestClient;
    private final RedisTemplate<String, String> redisTemplate;

    public MulRestController(AssistantRestService assistantRestService,
                             AuthTokenAccountsRestService authTokenAccountsRestService,
                             SvrRestService svrService,
                             PluginConfig config,
                             PluginRestClient pluginRestClient,
                             RedisTemplate<String, String> redisTemplate) {
        this.assistantRestService = assistantRestService;
        this.authTokenAccountsRestService = authTokenAccountsRestService;
        this.svrService = svrService;
        this.config = config;
        this.pluginRestClient = pluginRestClient;
        this.redisTemplate = redisTemplate;
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

        /* SvrRestController */
        URI uri = new URI(restClientUrl);
        pluginRestClient.exchangeGet(uri.resolve("svr/rest-client"), MediaType.APPLICATION_JSON);
        pluginRestClient.exchangeGet(uri.resolve("svr/logs/assistant"), MediaType.APPLICATION_JSON);
        pluginRestClient.exchangeGet(uri.resolve("svr/log-sensitive/disable"), MediaType.APPLICATION_JSON);
        log.info(LogEnmu.LOG2.value(), code, "svr-logs-assistant-disable", MediaType.APPLICATION_JSON);
        pluginRestClient.exchangeGet(uri.resolve("svr/logs/assistant"), MediaType.APPLICATION_JSON);
        log.info(LogEnmu.LOG2.value(), code, "svr-logs-assistant-enable");

        var body = """
            {
                "path": [
                    "/dawn-assistant/rest/authtoken/user/assert/exception",
                    "/dawn-assistant/rest/authtoken/user/self",
                    "/dawn-assistant/rest/svr/shutdown"
                ]
            }
            """;
        pluginRestClient.exchangeJson(uri.resolve("svr/http/clinet/ssrf/white/list"), regUser(), body);
        var responseWhiteList = pluginRestClient.exchangeJson(uri.resolve("svr/http/clinet/ssrf/white/list"));
        log.info(LogEnmu.LOG4.value(), code, "ssrf/white/list", responseWhiteList.getCode(), responseWhiteList.getData());

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
                "orgtypeid": "7766",
            }
            """;
        /* fixme 此处测试待完善 */
        responseUser = pluginRestClient.exchangeJson(uri.resolve("authtoken/user/session/async"), regUser(), body);
        log.info(LogEnmu.LOG4.value(), code, "authtoken/user/session/async", responseUser.getCode(), responseUser.getData());

        /* shutdown */
        pluginRestClient.exchangeGet(uri.resolve("svr/shutdown2"), regUser(), MediaType.APPLICATION_JSON);

        return new Response<>().success().message("svr is success");
    }

    @GetMapping("/authtoken")
    public Response<Object> authtoken() throws URISyntaxException {
        var code = "mul-authtoken";

        return new Response<>().success().message("authtoken is success");
    }

    private Map<String, String> regUser() {
        if (Objects.isNull(authToken) || Objects.isNull(redisTemplate.hasKey(config.getSpringApplicationName().concat(authToken)))) {
            var responseAuthToken = authTokenAccountsRestService.regUser(VarEnmu.DEF_USERID.value(), reg);
            log.info(LogEnmu.LOG3.value(), "regUser", responseAuthToken.getCode(), responseAuthToken.getMessage());
            Map<String, String> resMap = responseAuthToken.getData();
            authToken = resMap.get(VarEnmu.AUTH_TOKEN.value());
        }

        Map<String, String> headers = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        headers.put(VarEnmu.AUTH_TOKEN.value(), authToken);
        headers.put(AlgEnmu.ONCE.algorithm(), RandomUtil.getRandomChar(VarEnmu.SIX.ivalue()));
        headers.put(VarEnmu.TIMESTAMP.value(), String.valueOf(Instant.now().toEpochMilli()));
        return headers;
    }

}
