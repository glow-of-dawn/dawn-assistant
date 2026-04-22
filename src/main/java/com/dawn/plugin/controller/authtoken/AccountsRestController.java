//package com.dawn.plugin.controller.authtoken;
//
//import com.dawn.plugin.config.PluginConfig;
//import com.dawn.plugin.enmu.AlgEnmu;
//import com.dawn.plugin.enmu.LogEnmu;
//import com.dawn.plugin.enmu.VarEnmu;
//import com.dawn.plugin.entity.ccore.TabUser;
//import com.dawn.plugin.mapper.ccore.TabUserMapper;
//import com.dawn.plugin.mapper.ccore.ViewCcoreMapMapper;
//import com.dawn.plugin.redis.primary.RedisKeyService;
//import com.dawn.plugin.util.CrypUtil;
//import com.dawn.plugin.util.Response;
//import lombok.Data;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * [用户信息注册]
// * 创建时间：2021/2/3 22:38
// *
// * @author forest
// */
//@Slf4j
//@Data
//@RestController
//@RequestMapping(value = "/rest/authtoken/account/")
//@ConditionalOnProperty(name = {"plugin-status.auth-status",
//        "plugin-rest-controller.auth-status"}, havingValue = "enable", matchIfMissing = true)
//public class AccountsRestController {
//
//    @Value("${view-sql.view_orguser}")
//    private String view;
//    @Value("#{'${spring.application.name}:authtoken:'}")
//    private String redisAuthtokenKey;
//    private PluginConfig config;
//    private TabUserMapper tabUserMapper;
//    private RedisTemplate<String, Object> redisTemplate;
//    private RedisKeyService redisKeyService;
//    private ViewCcoreMapMapper viewCcoreMapMapper;
//
//    public AccountsRestController(PluginConfig config,
//                                  TabUserMapper tabUserMapper,
//                                  RedisTemplate<String, Object> redisTemplate,
//                                  RedisKeyService redisKeyService,
//                                  ViewCcoreMapMapper viewCcoreMapMapper) {
//        this.config = config;
//        this.tabUserMapper = tabUserMapper;
//        this.redisTemplate = redisTemplate;
//        this.redisKeyService = redisKeyService;
//        this.viewCcoreMapMapper = viewCcoreMapMapper;
//    }
//
//    /**
//     * -----------------------------------------------------------------------------------------------------------------
//     * [模拟注册用户-aes]
//     * -----------------------------------------------------------------------------------------------------------------
//     *
//     * @param userid [String]
//     * @return Response<Object>
//     */
//    @SneakyThrows
//    @PostMapping("/aes/user/{userid}")
//    public Response<Object> regUser(@PathVariable("userid") String userid, @RequestBody String body) {
//        Map<String, String> infoMap = config.getMapperLowerCamel().readValue(body, Map.class);
//        if (!VarEnmu.DEF_USERID.value().equals(infoMap.get(VarEnmu.NONE.value()))) {
//            return new Response<>().data("you are good!").success();
//        }
//        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern(VarEnmu.DATE_TIME_FORMATTER.value()));
//        String atoken = DigestUtils.sha1Hex(userid.concat(time));
//        String key = redisAuthtokenKey.concat(atoken);
//        log.info(LogEnmu.LOG2.value(), "reg log and in", key);
//        TabUser tabUser = tabUserMapper.find(userid);
//        var viewOrguser = viewCcoreMapMapper.findByView(view, "1", userid);
//        Map<String, Object> map = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
//        map.put(VarEnmu.USERID.value(), userid);
//        map.put("tabUser", tabUser);
//        map.put("atoken", atoken);
//        map.put("viewOrguser", viewOrguser);
//        String txt;
//        /* 默认 AES 算法 */
//        String algorithm = infoMap.getOrDefault(AlgEnmu.ALGORITHM.algorithm(), AlgEnmu.AES.algorithm());
//        var keyLen16 = redisKeyService.getKeyLen16(VarEnmu.KEY.value());
//        switch (algorithm) {
//            case "AES":
//                map.put(AlgEnmu.ALGORITHM_KEY.algorithm(), redisKeyService.getAlgorithmKey(atoken));
//                txt = CrypUtil.encrypAesBase64(keyLen16, body);
//                break;
//            case "SM4":
//                map.put(AlgEnmu.ALGORITHM_KEY.algorithm(), redisKeyService.getAlgorithmKey(atoken));
//                txt = CrypUtil.encrypSm4Base64(keyLen16, body);
//                break;
//            case "SM2":
//                /* 服务端密钥对 */
//                var serverSm2Map = CrypUtil.generateSm2Key();
//                /* 客户端密钥对 */
//                var clientSm2Map = CrypUtil.generateSm2Key();
//                Map<String, String> clientKeyMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
//                var priksm4 = CrypUtil.encrypSm4Base64(keyLen16, clientSm2Map.get(VarEnmu.PRIVATE_KEY.value()));
//                clientKeyMap.put(VarEnmu.PRIVATE_KEY.value(), priksm4);
//                var pubksm4 = CrypUtil.encrypSm4Base64(keyLen16, serverSm2Map.get(VarEnmu.PUBLIC_KEY.value()));
//                clientKeyMap.put(VarEnmu.PUBLIC_KEY.value(), pubksm4);
//                map.put(AlgEnmu.ALGORITHM_MAP.algorithm(), clientKeyMap);
//                redisTemplate.opsForValue().set(key.concat(VarEnmu.QUOTE.value()).concat(VarEnmu.PRIVATE_KEY.value()),
//                        serverSm2Map.get(VarEnmu.PRIVATE_KEY.value()), redisKeyService.getRedisExpires(), TimeUnit.SECONDS);
//                redisTemplate.opsForValue().set(key.concat(VarEnmu.QUOTE.value()).concat(VarEnmu.PUBLIC_KEY.value()),
//                        clientSm2Map.get(VarEnmu.PUBLIC_KEY.value()), redisKeyService.getRedisExpires(), TimeUnit.SECONDS);
//                txt = CrypUtil.encrypBase64BySm2(body, clientSm2Map.get(VarEnmu.PUBLIC_KEY.value()));
//                break;
//            default:
//                txt = "无法识别加密算法";
//        }
//
//        map.put(VarEnmu.BODY.value(), txt);
//        map.put(AlgEnmu.ALGORITHM_KEY.algorithm(), keyLen16);
//        map.put(AlgEnmu.ALGORITHM.algorithm(), algorithm);
//        redisTemplate.opsForValue().set(key, txt, redisKeyService.getRedisExpires(), TimeUnit.SECONDS);
//        redisTemplate.opsForValue().set(key.concat(":firsttime"), time, redisKeyService.getRedisExpires(), TimeUnit.SECONDS);
//        return new Response<>().data(map).success();
//    }
//
//}
