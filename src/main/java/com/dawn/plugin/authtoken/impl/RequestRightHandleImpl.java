package com.dawn.plugin.authtoken.impl;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 权限处理
 * 创建时间 2025/9/18 09:42
 *
 * @author bhyt2
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.auth-status"}, havingValue = "enable", matchIfMissing = true)
public class RequestRightHandleImpl {

    /**
     * [权限校验]
     *
     * @param atoken [Authtoken]
     * @return Response<Object>
     */
    public Response<Object> handle(Authtoken atoken) {
        if (!atoken.openRight()) {
            /* 无需权限校验 */
            return new Response<>().success();
        } else {
            return new Response<>().failure("权限管理未能实现，请勿打开");
        }
    }

}
