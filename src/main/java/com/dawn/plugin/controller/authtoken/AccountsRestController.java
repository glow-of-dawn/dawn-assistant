package com.dawn.plugin.controller.authtoken;

import com.dawn.plugin.controller.service.AuthtokenAccountsRestService;
import com.dawn.plugin.util.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [用户信息注册]
 * 创建时间：2021/2/3 22:38
 *
 * @author hforest-480s
 */
@Slf4j
@Data
@RestController
@RequestMapping(value = "/rest/authtoken/account/")
@ConditionalOnProperty(name = {"plugin-status.auth-status", "plugin-rest-controller.auth-status"}, havingValue = "enable", matchIfMissing = true)
public class AccountsRestController {

    private final AuthtokenAccountsRestService authtokenAccountsRestService;

    public AccountsRestController(AuthtokenAccountsRestService authtokenAccountsRestService) {
        this.authtokenAccountsRestService = authtokenAccountsRestService;
    }

    /**
     * -----------------------------------------------------------------------------------------------------------------
     * [模拟注册用户-aes]
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param userid [String]
     * @param body [String]
     * @return Response<Object>
     */
    @PostMapping("/aes/user/{userid}")
    public Response<Object> regUser(@PathVariable("userid") String userid, @RequestBody String body) {
        return authtokenAccountsRestService.regUser(userid, body);
    }

}
