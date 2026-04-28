package com.dawn.plugin.authtoken.impl;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.CryptUtil;
import com.dawn.plugin.util.Response;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 签名校验
 * 创建时间 2025/9/18 09:53
 *
 * @author bhyt2
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.auth-status"}, havingValue = "enable", matchIfMissing = true)
public class RequestSignatureHandleImpl {

    /**
     * [签名校验]
     *
     * @param atoken     [Authtoken]
     * @param request    [request]
     * @param sessionMap [sessionMap]
     * @param body [body]
     * @return Response<Object>
     */
    public Response<Object> handle(Authtoken atoken,
                                   HttpServletRequest request,
                                   Map<String, String> sessionMap,
                                   String body) {
        if (!atoken.openSignature()) {
            /* 无需签名校验 */
            return new Response<>().success();
        }

        /* 签名准备 */
        var algorithmKey = sessionMap.get(AlgEnmu.ALGORITHM_KEY.algorithm());
        String timestamp = Optional.ofNullable(request.getHeader(VarEnmu.TIMESTAMP.value())).orElse(VarEnmu.NONE.value());
        var signature = Optional.ofNullable(request.getHeader(AlgEnmu.SIGNATURE.algorithm())).orElse(VarEnmu.NONE.value());

        /* 签名处理 */
        String content = algorithmKey.concat(body).concat(timestamp);
        String sign = switch (sessionMap.get(AlgEnmu.HASH_TYPE.algorithm())) {
            case "SHA512" -> DigestUtils.sha512Hex(content);
            case "SM3" -> CryptUtil.sm3Hex(content);
            /* default sha256 */
            default -> DigestUtils.sha256Hex(content);
        };

        Assert.isTrue(Objects.equals(signature, sign),
                "请求报文签名验证失败:signature[".concat(signature)
                        .concat("] sign:").concat(sign)
                        .concat("] timestamp:").concat(timestamp)
                        .concat("]"));

        log.debug(LogEnmu.LOG5.value(), "验签验证", "signature", signature, "timestamp", timestamp);
        return new Response<>().success().data(sign);
    }

    /**
     * [签名生成]
     *
     * @param atoken     [Authtoken]
     * @param response   [response]
     * @param sessionMap [sessionMap]
     * @param body [body]
     * @return Response<Object>
     */
    public Response<Object> handle(Authtoken atoken,
                                   String timestamp,
                                   ServerHttpResponse response,
                                   Map<String, String> sessionMap,
                                   String body) {
        if (!atoken.openSignature()) {
            /* 无需签名校验 */
            return new Response<>().success();
        }

        /* 签名准备 */
        var algorithmKey = sessionMap.get(AlgEnmu.ALGORITHM_KEY.algorithm());
        String hashType = sessionMap.get(AlgEnmu.HASH_TYPE.algorithm());

        /* 签名处理 */
        String content = algorithmKey.concat(body).concat(timestamp);
        String sign = switch (hashType) {
            case "SHA512" -> DigestUtils.sha512Hex(content);
            case "SM3" -> CryptUtil.sm3Hex(content);
            /* default sha256 */
            default -> DigestUtils.sha256Hex(content);
        };

        response.getHeaders().add(AlgEnmu.SIGNATURE.algorithm(), sign);
        response.getHeaders().add(VarEnmu.TIMESTAMP.value(), timestamp);
        log.debug(LogEnmu.LOG5.value(), "验签处理", "sign", sign, "timestamp", timestamp);
        return new Response<>().success().data(sign);
    }

}
