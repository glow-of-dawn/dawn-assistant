package com.dawn.plugin.controller;

import com.dawn.plugin.controller.service.AssistantServiceRestService;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private final AssistantServiceRestService assistantServiceRestService;

    public AssistantServiceRestController(AssistantServiceRestService assistantServiceRestService) {
        this.assistantServiceRestService = assistantServiceRestService;
    }

    @GetMapping("/info")
    public Response<Object> getServiceInfo() {
        return assistantServiceRestService.getServiceInfo();
    }

    @PostMapping("/info")
    public Response<Object> postServiceInfo(@RequestBody String body) {
        return assistantServiceRestService.postServiceInfo(body);
    }

    @GetMapping("/health-live")
    public String healthLive() {
        return assistantServiceRestService.healthLive();
    }

    @GetMapping(value = "/health-read", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<Object> healthRead(@RequestHeader(value = "health", defaultValue = "") String health) {
        return assistantServiceRestService.healthRead(health);
    }

}
