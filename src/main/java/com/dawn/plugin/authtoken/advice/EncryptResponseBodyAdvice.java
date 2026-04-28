package com.dawn.plugin.authtoken.advice;

import cn.hutool.crypto.Padding;
import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.authtoken.impl.RequestSignatureHandleImpl;
import com.dawn.plugin.config.LoadParams;
import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.except.GlobalControllerAdvice;
import com.dawn.plugin.util.CryptUtil;
import com.dawn.plugin.util.PluginAssert;
import com.dawn.plugin.util.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 加密处理
 * 创建时间 2025/9/15 13:58
 *
 * @author bhyt2
 */
@Slf4j
@ControllerAdvice
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Value("#{'${plugin-params.authtoken-path:/rest/authtoken/service/authtoken}'}")
    private String authtokenPath;
    private final PluginConfig config;
    private final LoadParams loadParams;
    private final MethodAnnotationHandler methodAnnotationHandler;
    private final RequestSignatureHandleImpl requestSignatureHandle;

    public EncryptResponseBodyAdvice(final PluginConfig config,
                                     final LoadParams loadParams,
                                     final MethodAnnotationHandler methodAnnotationHandler,
                                     final RequestSignatureHandleImpl requestSignatureHandle) {
        this.config = config;
        this.loadParams = loadParams;
        this.methodAnnotationHandler = methodAnnotationHandler;
        this.requestSignatureHandle = requestSignatureHandle;
    }

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        Authtoken atoken = methodAnnotationHandler.getAuthtokenByMethodParameter(methodParameter);
        Method method = methodParameter.getMethod();
        if (Objects.isNull(method)
                || GlobalControllerAdvice.class.equals(method.getDeclaringClass())
                || "illegalArgument".equals(method.getName())) {
            return false;
        }
        return Objects.nonNull(atoken) && atoken.openAuthtoken() && (atoken.openEncrypt() || atoken.openSignature());
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        Authtoken atoken = methodAnnotationHandler.getAuthtokenByMethodParameter(returnType);
        Map<String, String> sessionMap = LinkedHashMap.newLinkedHashMap(VarEnmu.SIXTEEN.ivalue());
        /* sessionMap */
        if (request.getAttributes().get(VarEnmu.SESSION_ID.value()) instanceof Map<?, ?> smap) {
            smap.forEach((k, v) -> sessionMap.put(String.valueOf(k), String.valueOf(v)));
        }

        String resBody = body instanceof String str ? str : config.getMapperLowerCamel().writeValueAsString(body);

        /* 加密参数 */
        HttpHeaders headers = request.getHeaders();
        String algorithm = atoken.openEncrypt() ? sessionMap.get(AlgEnmu.ALGORITHM.algorithm()) : VarEnmu.NONE.value();
        var algorithmKey = sessionMap.get(AlgEnmu.ALGORITHM_KEY.algorithm());
        var algorithmIv = Optional.ofNullable(headers.getFirst(AlgEnmu.ALGORITHM_IV.algorithm())).orElse(algorithmKey);
        response.getHeaders().add(AlgEnmu.ALGORITHM_IV.algorithm(), algorithmIv);
        response.getHeaders().add(AlgEnmu.ALGORITHM.algorithm(), algorithm);

        /* 返回认证信息 */
        if (request.getURI().getPath().contains(authtokenPath)) {
            /* 返回认证信息 */
            var clientMap = LinkedHashMap.newLinkedHashMap(VarEnmu.SIXTEEN.ivalue());
            sessionMap.entrySet().stream()
                    .filter(en -> StringUtils.isNotBlank(en.getValue()))
                    .forEach(en -> clientMap.put(en.getKey(), en.getValue()));
            if (request.getAttributes().get(VarEnmu.CLIENT.value()) instanceof Map<?, ?> smap) {
                smap.forEach((k, v) -> clientMap.put(String.valueOf(k), String.valueOf(v)));
            }
            var res = new Response<>().success().data(clientMap);
            resBody = config.getMapperLowerCamel().writeValueAsString(res);
            algorithm = Optional.ofNullable(headers.getFirst(AlgEnmu.ALGORITHM_IFACE.algorithm())).orElse(algorithm);
            algorithmKey = loadParams.loadKey(AlgEnmu.ALGORITHM.algorithm(), VarEnmu.KEY.value());
            Assert.hasText(algorithmKey, "请配置默认密钥");
            algorithmIv = Optional.ofNullable(headers.getFirst(AlgEnmu.ALGORITHM_IV.algorithm())).orElse(algorithmKey);
            response.getHeaders().remove(AlgEnmu.ALGORITHM.algorithm());
            /* 不应当响应 提供加密方式 headers . add algorithm */
            response.getHeaders().add(AlgEnmu.ALGORITHM.algorithm(), VarEnmu.SESSION_ID.value());
            log.info(LogEnmu.LOG3.value(), "auth-token", sessionMap.get(VarEnmu.SESSION_ID.value()), sessionMap.get(VarEnmu.AUTH_TOKEN.value()));
        }

        /* 加密处理 */
        String encryptedBody = switch (algorithm) {
            case "AES" -> CryptUtil.encryptBase64ByWorld(algorithmKey,
                    algorithmIv,
                    resBody,
                    AlgEnmu.AES.transformation(),
                    AlgEnmu.AES.algorithm(),
                    VarEnmu.UTF8.value());
            case "SM2" -> CryptUtil.encryptBase64BySm2(resBody, sessionMap.get(VarEnmu.PUBLIC_KEY.value()));
            case "SM4" -> CryptUtil.encryptBase64BySm4Cbc(algorithmKey,
                    algorithmIv,
                    resBody,
                    Padding.PKCS5Padding,
                    VarEnmu.UTF8.value());
            /* default 不做加解密处理 */
            default -> resBody;
        };

        if (VarEnmu.NONE.value().equals(encryptedBody)) {
            throw new IllegalArgumentException("请求报文加密异常");
        }

        /* 签名校验 */
        Response<Object> res = requestSignatureHandle.handle(atoken,
                headers.getFirst(VarEnmu.TIMESTAMP.value()),
                response,
                sessionMap,
                encryptedBody);
        PluginAssert.notHttp200(res);

        return Optional.ofNullable(encryptedBody).orElse(VarEnmu.NONE.value());
    }
}
