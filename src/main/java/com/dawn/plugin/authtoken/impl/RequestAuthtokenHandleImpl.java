package com.dawn.plugin.authtoken.impl;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.util.CryptUtil;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *
 * 创建时间 2025/9/17 15:53
 *
 * @author bhyt2
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.auth-status"}, havingValue = "enable", matchIfMissing = true)
public class RequestAuthtokenHandleImpl {

    /* session参数 */
    @Value("#{'${spring.application.name}:authtoken:'}")
    private String redisAuthtokenKey;
    @Value("#{'${plugin-params.authtoken-path:/rest/authtoken/service/authtoken}'}")
    private String authtokenPath;
    @Value("#{'${plugin-params.session.keys:session-id,algorithm,algorithm-key,public-key,private-key,userid,groupid,orgtypeid}'}")
    private String sessionKeys;
    private final RedisKeyService redisKeyService;
    private final RedisTemplate<String, Object> redisTemplate;

    public RequestAuthtokenHandleImpl(
            final RedisKeyService redisKeyService,
            final RedisTemplate<String, Object> redisTemplate) {
        this.redisKeyService = redisKeyService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * [令牌认证]
     *
     * @param atoken  [Authtoken]
     * @param request [request]
     * @return Response<Object>
     */
    public Response<Object> handle(Authtoken atoken, HttpServletRequest request) {
        if (!atoken.openAuthtoken()) {
            /* 无需令牌认证 */
            return new Response<>().success();
        }

        String once = request.getHeader(AlgEnmu.ONCE.algorithm());
        String authtoken = request.getHeader(VarEnmu.AUTH_TOKEN.value());
        String timestamp = request.getHeader(VarEnmu.TIMESTAMP.value());

        if (Objects.isNull(once) || Objects.isNull(timestamp) || Objects.isNull(authtoken)) {
            /* 无认证 */
            return new Response<>().codeMessage(CodeEnmu.HTTP_460.icode());
        }

        /* once 校验 */
        var authTokenHash = redisAuthtokenKey.concat(authtoken);
        String redisOnceKey = authTokenHash.concat("-once-").concat(once);
        if (!Objects.isNull(redisTemplate.opsForValue().get(redisOnceKey))) {
            return new Response<>().codeMessage(CodeEnmu.HTTP_459.icode());
        } else {
            redisTemplate.opsForValue()
                    .set(redisOnceKey, timestamp, redisKeyService.getRedisShot5mExpires(), TimeUnit.SECONDS);
        }

        /* timestamp 校验 */
        String tstamp = String.format("%-13s", timestamp).replace(" ", "0");
        LocalDateTime reqLocalDateTime = Instant.ofEpochMilli(Long.parseLong(tstamp))
                .atZone(ZoneOffset.ofHours(VarEnmu.EIGHT.ivalue()))
                .toLocalDateTime();
        Duration duration = Duration.between(LocalDateTime.now(), reqLocalDateTime);
        if (Math.abs(duration.getSeconds()) > redisKeyService.getRedisShot5mExpires()) {
            return new Response<>().codeMessage(CodeEnmu.HTTP_461.icode());
        }

        /* session-id 校验 */
        Map<String, String> sessionMap = LinkedHashMap.newLinkedHashMap(VarEnmu.SEVENTEEN.ivalue());
        var response = new Response<>().success();
        if (request.getServletPath().contains(authtokenPath) && VarEnmu.SESSION_ID.value().equals(authtoken)) {
            /* 创建认证信息 */
            response.setMessage(VarEnmu.SESSION_ID.value());
            authTokenHandler(request, sessionMap);
        } else {
            authTokenHandler(request.getHeader(VarEnmu.AUTH_TOKEN.value()), sessionMap);
        }
        response.data(sessionMap);
        var sessionId = sessionMap.get(VarEnmu.SESSION_ID.value());

        /* 是否有认证 */
        Assert.isTrue(StringUtils.isNotBlank(sessionId), CodeEnmu.HTTP_457.codeIex());

        request.setAttribute(VarEnmu.SESSION_ID.value(), sessionMap);

        return response;
    }

    /**
     * [初始化 auth-token]
     * <p>
     * session-id
     * auth-token
     * algorithm: aec / sm4
     * algorithm-key: vAr8K5fZxrT4iTo5
     * public-key: 公钥 / sm2
     * private-key: 私钥 / sm2
     * userid:
     * groupid:
     * orgtypeid:
     *
     * @param request    [request]
     * @param sessionMap [sessionMap]
     * @return Map<String, String>
     */
    private void authTokenHandler(HttpServletRequest request, Map<String, String> sessionMap) {
        Arrays.stream(sessionKeys.split(VarEnmu.COMMA.value()))
                .forEach(key -> sessionMap.put(key, VarEnmu.NONE.value()));
        var sessionId = redisKeyService.getPrimary();
        sessionMap.put(VarEnmu.SESSION_ID.value(), sessionId);
        var authToken = DigestUtils.sha256Hex(sessionId);
        sessionMap.put(VarEnmu.AUTH_TOKEN.value(), authToken);
        String authTokenHash = redisAuthtokenKey.concat(authToken);
        var algorithm = Optional.ofNullable(request.getHeader(AlgEnmu.ALGORITHM.algorithm())).orElse(AlgEnmu.AES.algorithm());
        sessionMap.put(AlgEnmu.ALGORITHM.algorithm(), algorithm);
        /* 数据库配置密钥 */
        var algorithmKey = RandomUtil.getRandomChar(VarEnmu.SIXTEEN.ivalue());
        sessionMap.put(AlgEnmu.ALGORITHM_KEY.algorithm(), algorithmKey);
        /* 签名类型 */
        var hashType = Optional.ofNullable(request.getHeader(AlgEnmu.HASH_TYPE.algorithm())).orElse(AlgEnmu.SHA256.algorithm());
        sessionMap.put(AlgEnmu.HASH_TYPE.algorithm(), hashType);

        if (AlgEnmu.SM2.algorithm().equals(request.getHeader(AlgEnmu.ALGORITHM.algorithm()))) {
            var serverMap = CryptUtil.generateSm2Key();
            var clientMap = CryptUtil.generateSm2Key();
            /* 服务端保留 */
            sessionMap.put(VarEnmu.PRIVATE_KEY.value(), serverMap.get(VarEnmu.PRIVATE_KEY.value()));
            sessionMap.put(VarEnmu.PUBLIC_KEY.value(), clientMap.get(VarEnmu.PUBLIC_KEY.value()));
            /* 客户端保留 */
            clientMap.put(VarEnmu.PUBLIC_KEY.value(), serverMap.get(VarEnmu.PUBLIC_KEY.value()));
            request.setAttribute(VarEnmu.CLIENT.value(), clientMap);
        }
        /* 建立auth-token */
        redisTemplate.opsForHash().putAll(authTokenHash, sessionMap);
        redisTemplate.expire(authTokenHash, redisKeyService.getRedisShot10mExpires(), TimeUnit.SECONDS);
    }

    /**
     * [获取 sessionMap]
     *
     * @param authToken  [authToken]
     * @param sessionMap [sessionMap]
     */
    private void authTokenHandler(String authToken, Map<String, String> sessionMap) {
        var sMap = redisTemplate.opsForHash().entries(redisAuthtokenKey.concat(authToken));
        sMap.forEach((key, value) -> sessionMap.put(String.valueOf(key), String.valueOf(value)));
    }

}
