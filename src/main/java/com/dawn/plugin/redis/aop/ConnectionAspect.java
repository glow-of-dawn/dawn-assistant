//package com.dawn.plugin.redis.aop;
//
//import com.dawn.plugin.config.PluginConfigurableEnvironment;
//import com.dawn.plugin.enmu.LogEnmu;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.annotation.Aspect;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.data.redis.support.collections.RedisProperties;
//import org.springframework.stereotype.Component;
//
///**
// * [redis 连接切面处理]
// * 创建时间 2025/9/18 14:02
// *
// * @author bhyt2
// */
//@Slf4j
//@Aspect
//@Component
//@Order(20)
//@Configuration
//@ConditionalOnProperty(name = {"plugin-status.redis-status"}, havingValue = "enable", matchIfMissing = true)
//public class ConnectionAspect {
//
//    private final RedisProperties redisProperties;
//    @Value("#{'${spring.data.redis.password:}'}")
//    private String password;
//
//    public ConnectionAspect(PluginConfigurableEnvironment pluginConfigurableEnvironment,
//                            RedisProperties redisProperties) {
//        this.redisProperties = redisProperties;
//        log.debug(LogEnmu.LOG2.value(), "redis数据库密钥解析需要优先于设置数据库密码", pluginConfigurableEnvironment.toString());
//    }
//
////    @Pointcut("execution(* org.springframework.boot.autoconfigure.data.redis.RedisProperties.getPassword())")
////    public void getRedisConnectionPwd() {
////        throw new UnsupportedOperationException();
////    }
////
////    @Before("getRedisConnectionPwd()")
////    public void setRedisProperties() {
////        redisProperties.setPassword(password);
////        log.debug(LogEnmu.LOG2.value(), "RedisProperties.getPassword", password);
////    }
//
//}
