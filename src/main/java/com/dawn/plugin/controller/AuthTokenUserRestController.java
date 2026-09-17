package com.dawn.plugin.controller;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.controller.service.AuthTokenUserRestService;
import com.dawn.plugin.entity.ccore.TabUser;
import com.dawn.plugin.entity.ccore.ViewOrguser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;

/**
 * 用户信息服务
 * 创建时间：2021/2/3 19:49
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/authtoken/user")
@ConditionalOnProperty(name = {"plugin-rest-controller.user-status"}, havingValue = "enable", matchIfMissing = true)
public class AuthTokenUserRestController {

    private final AuthTokenUserRestService authTokenUserRestService;

    public AuthTokenUserRestController(AuthTokenUserRestService authTokenUserRestService) {
        this.authTokenUserRestService = authTokenUserRestService;
    }

    @Authtoken(openAuthtoken = true, openEncrypt = true, openSignature = true)
    @PostMapping("/session/async")
    public Object sessionAsync(@RequestBody String body,
                               @RequestHeader(value = "auth-token", defaultValue = "") String authToken) throws JacksonException {
        return authTokenUserRestService.sessionAsync(body, authToken);
    }

    @Authtoken(openAuthtoken = true)
    @GetMapping("/self")
    public Object getSelf(String userid, TabUser tabUser, ViewOrguser viewOrguser) {
        return authTokenUserRestService.getSelf(userid, tabUser, viewOrguser);
    }

    @Authtoken(openAuthtoken = true)
    @PostMapping("/assert/exception")
    public Object assertException(@RequestBody String body) {
        return authTokenUserRestService.assertException(body);
    }

}
