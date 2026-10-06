package com.dawn.plugin.authtoken.interceptor;

import cn.hutool.core.util.ReUtil;
import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.authtoken.impl.RequestAuthtokenHandleImpl;
import com.dawn.plugin.authtoken.impl.RequestRightHandleImpl;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.RegexEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.util.PluginAssert;
import com.dawn.plugin.util.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 创建时间 2025/9/17 14:39
 *
 * @author hforest-480s
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
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse httpServletResponse,
                             @NonNull Object handler) {
        var checkCount = new AtomicInteger(VarEnmu.ZERO.ivalue());
        Optional.ofNullable(request.getHeader(AlgEnmu.ONCE.algorithm()))
            .filter(once -> !once.equals(ReUtil.getGroup0(RegexEnmu.NUMBER_AND_LETTER.regex(), once)))
            .ifPresent(once -> checkCount.getAndIncrement());
        Optional.ofNullable(request.getHeader(VarEnmu.AUTH_TOKEN.value()))
            .filter(auth -> !auth.equals(ReUtil.getGroup0(RegexEnmu.NUMBER_AND_LETTER.regex(), auth)))
            .ifPresent(auth -> checkCount.getAndIncrement());
        Optional.ofNullable(request.getHeader(VarEnmu.TIMESTAMP.value()))
            .filter(time -> !time.equals(ReUtil.getGroup0(RegexEnmu.NUMBER.regex(), time)))
            .ifPresent(time -> checkCount.getAndIncrement());
        Assert.isTrue(checkCount.get() == VarEnmu.ZERO.ivalue(), "请求报文参数异常");

        Optional.ofNullable(handler instanceof HandlerMethod hMethod
                ? hMethod.getMethodAnnotation(Authtoken.class)
                : null)
            .ifPresent(atoken -> {
                /* 认证校验 */
                Response<Object> response = requestAuthtokenHandle.handle(atoken, request);
                PluginAssert.notHttp200(response);

                /* sessionMap */
                Map<String, String> sessionMap = response.getData() instanceof Map map
                    ? map : LinkedHashMap.newLinkedHashMap(VarEnmu.SIXTEEN.ivalue());

                /* 权限校验 */
                response = requestRightHandle.handle(atoken);
                PluginAssert.notHttp200(response);

                /* 更新 auth-token-expires 10分钟内有效 */
                String authToken = sessionMap.get(VarEnmu.AUTH_TOKEN.value());
                redisTemplate.expire(redisAuthtokenKey.concat(authToken), Duration.ofSeconds(redisKeyService.getRedisShot10mExpires()));
            });
        return true;
    }

}
