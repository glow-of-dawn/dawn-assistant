package com.dawn.plugin.controller.service;

import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabUser;
import com.dawn.plugin.entity.ccore.ViewOrguser;
import com.dawn.plugin.redis.primary.RedisKeyService;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息服务
 * 创建时间 2026/8/26 21:48
 *
 * @author bhyt2
 */
@Slf4j
@ConditionalOnProperty(name = {"plugin-status.auth-status", "plugin-rest-controller.auth-status"}, havingValue = "enable", matchIfMissing = true)
public class AuthTokenRestService {

    private final ApplicationContext applicationContext;
    private final RedisKeyService redisKeyService;

    public AuthTokenRestService(RedisKeyService redisKeyService,
                                ApplicationContext applicationContext) {
        this.redisKeyService = redisKeyService;
        this.applicationContext = applicationContext;
    }

    public Response<Object> shutdown() {
        ConfigurableApplicationContext cyx = (ConfigurableApplicationContext) this.applicationContext;
        cyx.close();
        return new Response<>().message("shutdown").success();
    }

    public Response<Object> getAlgorithmKey(String authToken) {
        /* 获取动态令牌 */
        Map<String, Object> map = HashMap.newHashMap(VarEnmu.FOUR.ivalue());
        map.put(AlgEnmu.ALGORITHM_KEY.algorithm(), redisKeyService.getAlgorithmKey(authToken));
        return new Response<>().success().data(map);
    }

    public Object appendEncrypt(String body) {
        /* 必须包含加解密处理机制 */
        log.debug(LogEnmu.LOG2.value(), "appendEncrypt.body", body);
        Response<Object> response = new Response<>().success()
            .data("加解密信息:".concat(DigestUtils.sha256Hex(body)))
            .message(body);
        log.debug(LogEnmu.LOG2.value(), "appendEncrypt.response", response);
        return response;
    }

    public Object signature(String body) {
        Map<String, Object> map = HashMap.newHashMap(VarEnmu.FOUR.ivalue());
        map.put(VarEnmu.AUTHTOKEN.value(), "123");
        map.put("encrypt", "加解密信息");
        map.put("signature", "321");
        map.put("sha256Hex", DigestUtils.sha256Hex(body));
        return new Response<>().data(map).success().message(body);
    }

    public Object signature2(String body) {
        Map<String, Object> map = HashMap.newHashMap(VarEnmu.FOUR.ivalue());
        map.put(VarEnmu.AUTHTOKEN.value(), "123");
        map.put("encrypt", "不含加解密信息");
        map.put("signature", "321");
        map.put("body", body);
        return new Response<>().data(map).success().message(body);
    }

    public Object param(String userid, TabUser tabUser, ViewOrguser viewOrguser) {
        Map<String, Object> map = HashMap.newHashMap(VarEnmu.FOUR.ivalue());
        map.put("userid", userid);
        map.put("tabUser", tabUser);
        map.put("viewOrguser", viewOrguser);
        log.info(LogEnmu.LOG2.value(), "authtoken.response", map);
        return new Response<>().data(map).success();
    }

}
