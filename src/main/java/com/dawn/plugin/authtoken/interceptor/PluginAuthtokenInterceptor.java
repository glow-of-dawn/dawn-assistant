package com.dawn.plugin.authtoken.interceptor;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.authtoken.impl.RequestAuthtokenHandleImpl;
import com.dawn.plugin.authtoken.impl.RequestRightHandleImpl;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.util.PluginAssert;
import com.dawn.plugin.util.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 *
 * 创建时间 2025/9/17 14:39
 *
 * @author bhyt2
 */
@Slf4j
@Component
public class PluginAuthtokenInterceptor implements HandlerInterceptor {

    /* session参数 */
    @Value("#{'${spring.application.name}:authtoken:'}")
    private String redisAuthtokenKey;
    private final RedisKeyService redisKeyService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RequestAuthtokenHandleImpl requestAuthtokenHandle;
    private final RequestRightHandleImpl requestRightHandle;

    public PluginAuthtokenInterceptor(final RedisKeyService redisKeyService,
                                      final RedisTemplate<String, Object> redisTemplate,
                                      RequestAuthtokenHandleImpl requestAuthtokenHandle,
                                      RequestRightHandleImpl requestRightHandle) {
        this.redisKeyService = redisKeyService;
        this.redisTemplate = redisTemplate;
        this.requestAuthtokenHandle = requestAuthtokenHandle;
        this.requestRightHandle = requestRightHandle;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object handler) {
        Authtoken atoken;
        if (handler instanceof HandlerMethod handlerMethod
                && Objects.nonNull(handlerMethod.getMethodAnnotation(Authtoken.class))) {
            atoken = handlerMethod.getMethodAnnotation(Authtoken.class);

            /* 认证校验 */
            Response<Object> response = requestAuthtokenHandle.handle(atoken, request);
            PluginAssert.notHttp200(response);

            /* sessionMap */
            Map<String, String> sessionMap = response.getData() instanceof Map
                    ? response.getData() : LinkedHashMap.newLinkedHashMap(VarEnmu.SIXTEEN.ivalue());

            /* 权限校验 */
            response = requestRightHandle.handle(atoken);
            PluginAssert.notHttp200(response);

            /* 更新 auth-token-expires 10分钟内有效 */
            String authToken = sessionMap.get(VarEnmu.AUTH_TOKEN.value());
            redisTemplate.expire(redisAuthtokenKey.concat(authToken), redisKeyService.getRedisShot10mExpires(), TimeUnit.SECONDS);
        }
        return true;
    }

}
