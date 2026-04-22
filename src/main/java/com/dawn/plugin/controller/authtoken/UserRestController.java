//package com.dawn.plugin.controller.authtoken;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.dawn.plugin.authtoken.Authtoken;
//import com.dawn.plugin.config.PluginConfig;
//import com.dawn.plugin.enmu.VarEnmu;
//import com.dawn.plugin.entity.ccore.TabUser;
//import com.dawn.plugin.entity.ccore.ViewOrguser;
//import com.dawn.plugin.util.Response;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.util.Assert;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
///**
// * 用户信息服务
// * 创建时间：2021/2/3 19:49
// *
// * @author forest
// */
//@Slf4j
//@RestController
//@RequestMapping(value = "/rest/authtoken/user")
//@ConditionalOnProperty(name = {"plugin-rest-controller.user-status"}, havingValue = "enable", matchIfMissing = true)
//public class UserRestController {
//
//    @Value("#{'${plugin-params.session.user-keys:userid,groupid,orgtypeid}'}")
//    private String sessionUserKeys;
//    @Value("#{'${spring.application.name}:authtoken:'}")
//    private String redisAuthtokenKey;
//    private final PluginConfig config;
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    public UserRestController(PluginConfig config,
//                              final RedisTemplate<String, Object> redisTemplate) {
//        this.config = config;
//        this.redisTemplate = redisTemplate;
//    }
//
//    @Authtoken(openAuthtoken = true, openEncryp = true, openSignature = true)
//    @PostMapping("/session/async")
//    public Object sessionAsync(@RequestBody String body,
//                               @RequestHeader(value = "auth-token", defaultValue = "") String authToken) throws JsonProcessingException {
//        var map = config.getMapperLowerCamel().readValue(body, Map.class);
//        var authTokenHash = redisAuthtokenKey.concat(authToken);
//        Arrays.stream(sessionUserKeys.split(VarEnmu.COMMA.value()))
//                .filter(key -> Objects.nonNull(map.get(key)) && map.get(key) instanceof String)
//                .forEach(key -> redisTemplate.opsForHash().put(authTokenHash, key, map.get(key)));
//        return new Response<>().success().message("完成信息同步");
//    }
//
//    @Authtoken(openAuthtoken = true)
//    @GetMapping("/self")
//    public Object getSelf(String userid, TabUser tabUser, ViewOrguser viewOrguser) {
//        Map<String, Object> map = HashMap.newHashMap(VarEnmu.FOUR.ivalue());
//        map.put("userid", userid);
//        map.put("tabUser", tabUser);
//        map.put("viewOrguser", viewOrguser);
//        return new Response<>().data(map).success();
//    }
//
//    @Authtoken(openAuthtoken = true)
//    @PostMapping("/assert/exception")
//    public Object assertException(@RequestBody String body) {
//        Assert.notNull(body, "499");
//        return new Response<>().success().data(body);
//    }
//
//}
