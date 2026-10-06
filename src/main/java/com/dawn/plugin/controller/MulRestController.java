package com.dawn.plugin.controller;

import com.dawn.plugin.controller.service.AssistantRestService;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private final AssistantRestService assistantRestService;

    public MulRestController(AssistantRestService assistantRestService) {
        this.assistantRestService = assistantRestService;
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

}
