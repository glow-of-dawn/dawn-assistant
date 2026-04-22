//package com.dawn.plugin.controller.authtoken;
//
//import com.vivi.plugin.authtoken.Authtoken;
//import com.vivi.plugin.enmu.AlgEnmu;
//import com.vivi.plugin.enmu.LogEnmu;
//import com.vivi.plugin.enmu.VarEnmu;
//import com.vivi.plugin.redis.primary.RedisKeyService;
//import com.vivi.plugin.util.Response;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 创建时间：2021/2/4 15:41
// *
// * @author hforest-480s
// */
//@Slf4j
//@RestController
//@RequestMapping(value = "/rest/authtoken/service/")
//@ConditionalOnProperty(name = {"plugin-status.auth-status",
//    "plugin-rest-controller.auth-status"}, havingValue = "enable", matchIfMissing = true)
//public class AuthTokenServiceRestController {
//
//    private ApplicationContext applicationContext;
//    private RedisKeyService redisKeyService;
//
//    public AuthTokenServiceRestController(RedisKeyService redisKeyService,
//                                          ApplicationContext applicationContext) {
//        this.redisKeyService = redisKeyService;
//        this.applicationContext = applicationContext;
//    }
//
//    @Authtoken(openAuthtoken = true)
//    @GetMapping("/shutdown")
//    public Response<Object> get() {
//        ConfigurableApplicationContext cyx = (ConfigurableApplicationContext) this.applicationContext;
//        cyx.close();
//        return new Response<>().message("shutdown").success();
//    }
//
//    @Authtoken(openAuthtoken = true)
//    @GetMapping("/algorithm-key")
//    public Response<Object> getAlgorithmKey(@RequestHeader("auth-token") String authToken) {
//        /* 获取动态令牌 */
//        Map<String, Object> map = HashMap.newHashMap(VarEnmu.FOUR.ivalue());
//        map.put(AlgEnmu.ALGORITHM_KEY.algorithm(), redisKeyService.getAlgorithmKey(authToken));
//        return new Response<>().success().data(map);
//    }
//
//    @Authtoken(openAuthtoken = true, openEncryp = true)
//    @GetMapping("/authtoken")
//    public Object authtoken() {
//        return new Response<>().success().message(VarEnmu.SESSION_ID.value());
//    }
//
//    @Authtoken(openAuthtoken = true, openEncryp = true)
//    @PostMapping("/append-encryp")
//    public Object appendEncryp(@RequestBody String body) {
//        /* 必须包含加解密处理机制 */
//        log.debug(LogEnmu.LOG2.value(), "appendEncryp.body", body);
//        Response<Object> response = new Response<>().success()
//            .data("加解密信息:".concat(DigestUtils.sha256Hex(body)))
//            .message(body);
//        log.debug(LogEnmu.LOG2.value(), "appendEncryp.response", response);
//        return response;
//    }
//
//    @Authtoken(openAuthtoken = true, openSignature = true, openEncryp = true)
//    @PostMapping("/signature")
//    public Object signature(@RequestBody String body) {
//        Map<String, Object> map = HashMap.newHashMap(VarEnmu.FOUR.ivalue());
//        map.put(VarEnmu.AUTHTOKEN.value(), "123");
//        map.put("encryp", "加解密信息");
//        map.put("signature", "321");
//        map.put("sha256Hex", DigestUtils.sha256Hex(body));
//        return new Response<>().data(map).success().message(body);
//    }
//
//    @Authtoken(openAuthtoken = true, openSignature = true)
//    @PostMapping("/signature2")
//    public Object signature2(@RequestBody String body) {
//        Map<String, Object> map = HashMap.newHashMap(VarEnmu.FOUR.ivalue());
//        map.put(VarEnmu.AUTHTOKEN.value(), "123");
//        map.put("encryp", "不含加解密信息");
//        map.put("signature", "321");
//        map.put("body", body);
//        return new Response<>().data(map).success().message(body);
//    }
//
//}
