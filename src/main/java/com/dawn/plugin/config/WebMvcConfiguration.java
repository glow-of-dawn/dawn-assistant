//package com.dawn.plugin.config;
//
//import com.dawn.plugin.enmu.VarEnmu;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.util.Arrays;
//
///**
// * 拦截器
// * 创建时间 2021/3/4 11:53
// *
// * @author hforest-480s
// */
//@Configuration
//@ConditionalOnProperty(name = {"plugin-status.config-status"}, havingValue = "enable", matchIfMissing = true)
//public class WebMvcConfiguration implements WebMvcConfigurer {
//
//    @Value("#{'${plugin-params.request-path:/rest/**,/static/**,/druid/**,/index.html,/error,/actuator}'}")
//    private String interceptorPath;
//    private final PluginAuthtokenInterceptor pluginAuthtokenInterceptor;
//
//    public WebMvcConfiguration(PluginAuthtokenInterceptor pluginAuthtokenInterceptor) {
//        this.pluginAuthtokenInterceptor = pluginAuthtokenInterceptor;
//    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/static/**")
//                .addResourceLocations("classpath:/static/");
//    }
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        /* 允许访问路径拦截器 */
//        var interceptorPaths = Arrays.asList(interceptorPath.split(VarEnmu.COMMA.value()));
//        registry
//                .addInterceptor(new PluginHandlerInterceptor())
//                .addPathPatterns("/**")
//                .excludePathPatterns(interceptorPaths);
//        /* 会话安全拦截器 */
//        registry
//                .addInterceptor(pluginAuthtokenInterceptor)
//                .addPathPatterns("/**");
//    }
//
//}
