package com.dawn.plugin.controller.service;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabUser;
import com.dawn.plugin.entity.ccore.ViewOrguser;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.core.JacksonException;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * 创建时间 2026/8/26 21:48
 *
 * @author bhyt2
 */
@Slf4j
@ConditionalOnProperty(name = {"plugin-rest-controller.user-status"}, havingValue = "enable", matchIfMissing = true)
public class AuthTokenUserRestService {

    @Value("#{'${plugin-params.session.user-keys:userid,groupid,orgtypeid}'}")
    private String sessionUserKeys;
    @Value("#{'${spring.application.name}:authtoken:'}")
    private String redisAuthtokenKey;
    private final PluginConfig config;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuthTokenUserRestService(PluginConfig config,
                                    final RedisTemplate<String, Object> redisTemplate) {
        this.config = config;
        this.redisTemplate = redisTemplate;
    }

    public Object sessionAsync(String body, String authToken) throws JacksonException {
        var map = config.getMapperLowerCamel().readValue(body, Map.class);
        var authTokenHash = redisAuthtokenKey.concat(authToken);
        Arrays.stream(sessionUserKeys.split(VarEnmu.COMMA.value()))
            .filter(key -> Objects.nonNull(map.get(key)) && map.get(key) instanceof String)
            .forEach(key -> {
                redisTemplate.opsForHash().put(authTokenHash, key, map.get(key));
                redisTemplate.expire(authTokenHash, Duration.ofSeconds(VarEnmu.NUMBER_300.ivalue()));
            });
        return new Response<>().success().message("完成信息同步");
    }

    public Object getSelf(String userid, TabUser tabUser, ViewOrguser viewOrguser) {
        Map<String, Object> map = HashMap.newHashMap(VarEnmu.FOUR.ivalue());
        map.put("userid", userid);
        map.put("tabUser", tabUser);
        map.put("viewOrguser", viewOrguser);
        return new Response<>().data(map).success();
    }

    public Object assertException(@RequestBody String body) {
        Assert.notNull(body, "499");
        return new Response<>().success().data(body);
    }

}
