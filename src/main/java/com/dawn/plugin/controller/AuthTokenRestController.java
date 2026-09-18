package com.dawn.plugin.controller;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.controller.service.AuthTokenRestService;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabUser;
import com.dawn.plugin.entity.ccore.ViewOrguser;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创建时间：2021/2/4 15:41
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/authtoken/service/")
@ConditionalOnProperty(name = {"plugin-status.auth-status", "plugin-rest-controller.auth-status"}, havingValue = "enable", matchIfMissing = true)
public class AuthTokenRestController {

    private final AuthTokenRestService authTokenRestService;

    public AuthTokenRestController(AuthTokenRestService authTokenRestService) {
        this.authTokenRestService = authTokenRestService;
    }

    @Authtoken(openAuthtoken = true)
    @GetMapping("/shutdown")
    public Response<Object> shutdown() {
        return authTokenRestService.shutdown();
    }

    @Authtoken(openAuthtoken = true)
    @GetMapping("/algorithm-key")
    public Response<Object> getAlgorithmKey(@RequestHeader("auth-token") String authToken) {
        return authTokenRestService.getAlgorithmKey(authToken);
    }

    @Authtoken(openAuthtoken = true, openEncrypt = true)
    @GetMapping("/authtoken")
    public Object authtoken() {
        return new Response<>().success().message(VarEnmu.SESSION_ID.value());
    }

    @Authtoken(openAuthtoken = true, openEncrypt = true)
    @PostMapping("/append-encrypt")
    public Object appendEncrypt(@RequestBody String body) {
        return authTokenRestService.appendEncrypt(body);
    }

    @Authtoken(openAuthtoken = true, openSignature = true, openEncrypt = true)
    @PostMapping("/signature")
    public Object signature(@RequestBody String body) {
        return authTokenRestService.signature(body);
    }

    @Authtoken(openAuthtoken = true, openSignature = true)
    @PostMapping("/signature2")
    public Object signature2(@RequestBody String body) {
        return authTokenRestService.signature2(body);
    }

    @Authtoken(openAuthtoken = true)
    @GetMapping("/param")
    public Object param(String userid, TabUser tabUser, ViewOrguser viewOrguser) {
        return authTokenRestService.param(userid, tabUser, viewOrguser);
    }

}
