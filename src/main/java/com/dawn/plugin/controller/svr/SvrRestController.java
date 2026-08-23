package com.dawn.plugin.controller.svr;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.controller.service.SvrService;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 创建时间 2026/8/20 21:32
 *
 * @author bhyt2
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/svr")
@ConditionalOnProperty(name = {"plugin-rest-controller.svr-status"}, havingValue = "enable", matchIfMissing = true)
public class SvrRestController {

    private final SvrService svrService;

    public SvrRestController(SvrService svrService) {
        this.svrService = svrService;
    }

    @GetMapping("/log-sensitive/{logSensitive}")
    public Response<Object> logSensitive(@PathVariable("logSensitive") String logSensitive) {
        return svrService.logSensitive(logSensitive);
    }

    @GetMapping("/logs/assistant")
    public Response<Object> logs() {
        return svrService.logs();
    }

    @GetMapping("/http/clinet/ssrf/white/list")
    public Response<Object> getSsrfWhiteList() {
        return svrService.getSsrfWhiteList();
    }

    @Authtoken(openAuthtoken = true)
    @PostMapping("/http/clinet/ssrf/white/list")
    public Response<Object> setSsrfWhiteList(@RequestBody String body) {
        return svrService.setSsrfWhiteList(body);
    }

    @GetMapping("/rest-client")
    public Response<Object> restClient() {
        return svrService.restClient();
    }

    @GetMapping("/thread-pool/{closeErrTest}/{multipleSize}")
    public Response<Object> testTask(@PathVariable("closeErrTest") boolean closeErrTest,
                                     @PathVariable("multipleSize") int multipleSize) {
        return svrService.testTask(closeErrTest, multipleSize);
    }

}
