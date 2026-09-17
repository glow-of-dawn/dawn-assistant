package com.dawn.plugin.controller;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.controller.service.AuthTokenOrgRestService;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [机构列表]
 * 创建时间：2021/2/3 21:59
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/authtoken/org/")
@ConditionalOnProperty(name = {"plugin-rest-controller.org-status"}, havingValue = "enable", matchIfMissing = true)
public class AuthTokenOrgRestController {

    private final AuthTokenOrgRestService authTokenOrgRestService;

    public AuthTokenOrgRestController(AuthTokenOrgRestService authTokenOrgRestService) {
        this.authTokenOrgRestService = authTokenOrgRestService;
    }

    @Authtoken(openAuthtoken = true)
    @GetMapping("/orggroup/orgtypeid/{orgtypeid}")
    public Response<Object> orggroupAndOrgtypeid(@PathVariable("orgtypeid") String orgtypeid) {
        return authTokenOrgRestService.orggroupAndOrgtypeid(orgtypeid);
    }

}
