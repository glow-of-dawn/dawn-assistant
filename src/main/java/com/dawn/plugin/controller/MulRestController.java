package com.dawn.plugin.controller;

import com.dawn.plugin.controller.service.AssistantRestService;
import com.dawn.plugin.controller.service.AuthTokenAccountsRestService;
import com.dawn.plugin.controller.service.SvrRestService;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.httpclient.PluginRestClient;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

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
    private final AssistantRestService assistantRestService;
    private final AuthTokenAccountsRestService authTokenAccountsRestService;
    private final AuthTokenUserRestController authTokenUserRestController;
    private final SvrRestService svrService;
    private final PluginRestClient pluginRestClient;

    public MulRestController(AssistantRestService assistantRestService,
                             AuthTokenAccountsRestService authTokenAccountsRestService,
                             AuthTokenUserRestController authTokenUserRestController,
                             SvrRestService svrService,
                             PluginRestClient pluginRestClient) {
        this.assistantRestService = assistantRestService;
        this.authTokenAccountsRestService = authTokenAccountsRestService;
        this.authTokenUserRestController = authTokenUserRestController;
        this.svrService = svrService;
        this.pluginRestClient = pluginRestClient;
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

    @GetMapping("/authtoken")
    public Response<Object> authtoken() throws URISyntaxException {
        var code = "mul-authtoken";

        var responseAuthToken = authTokenAccountsRestService.regUser(VarEnmu.DEF_USERID.value(), reg);
        log.info(LogEnmu.LOG4.value(), code, "regUser", responseAuthToken.getCode(), responseAuthToken.getMessage());

        URI uri = new URI(restClientUrl);
        pluginRestClient.exchangeGet(uri.resolve("svr/rest-client"));
        var responseWhiteList = pluginRestClient.exchangeJson(uri.resolve("svr/http/clinet/ssrf/white/list"));
        log.info(LogEnmu.LOG4.value(), code, "ssrf/white/list", responseWhiteList.getCode(), responseWhiteList.getMessage());

        pluginRestClient.exchangeGet(uri.resolve("svr/logs/assistant"));
        pluginRestClient.exchangeGet(uri.resolve("svr/log-sensitive/disable"));
        log.info(LogEnmu.LOG2.value(), code, "svr-logs-assistant-disable");
        pluginRestClient.exchangeGet(uri.resolve("svr/logs/assistant"));
        log.info(LogEnmu.LOG2.value(), code, "svr-logs-assistant-enable");

        return new Response<>().success().message("authtoken is success");
    }

}
