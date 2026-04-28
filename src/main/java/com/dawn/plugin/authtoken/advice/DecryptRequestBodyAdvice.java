package com.dawn.plugin.authtoken.advice;

import cn.hutool.crypto.Padding;
import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.authtoken.impl.RequestSignatureHandleImpl;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.CryptUtil;
import com.dawn.plugin.util.PluginAssert;
import com.dawn.plugin.util.Response;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 解密处理
 * 创建时间 2025/9/15 11:59
 *
 * @author bhyt2
 */
@Slf4j
@ControllerAdvice
@ConditionalOnProperty(name = {"plugin-status.auth-status"}, havingValue = "enable", matchIfMissing = true)
public class DecryptRequestBodyAdvice implements RequestBodyAdvice {

    private final MethodAnnotationHandler methodAnnotationHandler;
    private final RequestSignatureHandleImpl requestSignatureHandle;

    public DecryptRequestBodyAdvice(MethodAnnotationHandler methodAnnotationHandler,
                                    RequestSignatureHandleImpl requestSignatureHandle) {
        this.methodAnnotationHandler = methodAnnotationHandler;
        this.requestSignatureHandle = requestSignatureHandle;
    }

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter,
                            @NonNull Type targetType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        Authtoken atoken = methodAnnotationHandler.getAuthtokenByMethodParameter(methodParameter);
        return Objects.nonNull(atoken) && atoken.openAuthtoken() && (atoken.openEncrypt() || atoken.openSignature());
    }

    @Override
    public HttpInputMessage beforeBodyRead(@NonNull HttpInputMessage inputMessage,
                                           @NonNull MethodParameter parameter,
                                           @NonNull Type targetType,
                                           @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @SneakyThrows
    @Override
    public Object afterBodyRead(@NonNull Object body,
                                @NonNull HttpInputMessage inputMessage,
                                @NonNull MethodParameter parameter,
                                @NonNull Type targetType,
                                @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        /* sessionMap */
        HttpServletRequest request;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            request = attributes.getRequest();
        } else {
            throw new IllegalArgumentException("DecryptRequestBodyAdvice.beforeBodyRead 获取 request异常");
        }

        Map<String, String> sessionMap = LinkedHashMap.newLinkedHashMap(VarEnmu.SIXTEEN.ivalue());
        if (request.getAttribute(VarEnmu.SESSION_ID.value()) instanceof Map<?, ?> smap) {
            Map<String, String> finalSessionMap = sessionMap;
            smap.forEach((k, v) -> finalSessionMap.put(String.valueOf(k), String.valueOf(v)));
        } else {
            sessionMap = LinkedHashMap.newLinkedHashMap(VarEnmu.SIXTEEN.ivalue());
        }

        Authtoken atoken = methodAnnotationHandler.getAuthtokenByMethodParameter(parameter);
        String encryptedBody = body instanceof String str ? str : String.valueOf(body);
        var algorithm = atoken.openEncrypt() ? sessionMap.get(AlgEnmu.ALGORITHM.algorithm()) : VarEnmu.NONE.value();
        var algorithmKey = sessionMap.get(AlgEnmu.ALGORITHM_KEY.algorithm());
        var algorithmIv = Optional.ofNullable(request.getHeader(AlgEnmu.ALGORITHM_IV.algorithm())).orElse(algorithmKey);

        /* 签名校验 */
        Response<Object> response = requestSignatureHandle.handle(atoken, request, sessionMap, encryptedBody);
        PluginAssert.notHttp200(response);

        /* 解密处理 */
        String decryptedBody = switch (algorithm) {
            case "AES" -> CryptUtil.decodeBase64ByWorld(algorithmKey,
                    algorithmIv,
                    encryptedBody,
                    AlgEnmu.AES.transformation(),
                    AlgEnmu.AES.algorithm(),
                    VarEnmu.UTF8.value());
            case "SM2" -> CryptUtil.decodeBase64BySm2(encryptedBody, sessionMap.get(VarEnmu.PRIVATE_KEY.value()));
            case "SM4" -> CryptUtil.decodeBase64BySm4Cbc(algorithmKey,
                    algorithmIv,
                    encryptedBody,
                    Padding.PKCS5Padding,
                    VarEnmu.UTF8.value());
            /* default 不做加解密处理 */
            default -> encryptedBody;
        };
        if (VarEnmu.NONE.value().equals(decryptedBody)) {
            throw new IllegalArgumentException("请求报文解密异常");
        }

        return Optional.ofNullable(decryptedBody).orElse(VarEnmu.NONE.value());
    }

    @Override
    public Object handleEmptyBody(Object body,
                                  @NonNull HttpInputMessage inputMessage,
                                  @NonNull MethodParameter parameter,
                                  @NonNull Type targetType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

}
