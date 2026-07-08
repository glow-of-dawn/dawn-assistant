package com.dawn.plugin.system;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * [配置服务]
 * 创建时间：2021/4/9 11:17
 *
 * @author hforest-480s
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = {"plugin-status.system-status"}, havingValue = "enable", matchIfMissing = true)
public class SystemConfig {

    private final GracefulShutdownTomcatService gracefulShutdownTomcat;
    private final ApplicationContext applicationContext;

    public SystemConfig(ApplicationContext applicationContext, GracefulShutdownTomcatService gracefulShutdownTomcat) {
        this.applicationContext = applicationContext;
        this.gracefulShutdownTomcat = gracefulShutdownTomcat;
    }

    public Map<String, ShutdownService> getBeans() {
        Map<String, ShutdownService> beansMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        List<String> beans = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());
        beans.addAll(Arrays.stream(applicationContext.getBeanDefinitionNames()).toList());
        log.debug(LogEnmu.LOG2.value(), "寻找", "*-shutdown-service-impl");
        beans.stream()
            .filter(name -> name.contains("-shutdown-service-impl"))
            .forEach(name -> Optional.ofNullable(applicationContext.getType(name))
                .ifPresent(beanType -> {
                    if (applicationContext.getBean(beanType) instanceof ShutdownService shutdownService) {
                        log.info(LogEnmu.LOG2.value(), "装载关机服务", name);
                        beansMap.put(name, shutdownService);
                    }
                })
            );
        return beansMap;
    }

    @Bean
    public Response<Object> simpleShutdown(PluginConfig config) {
        /*
         * [gracefulShutdownTomcat.getShutdownServices().add(testSimpleShutdownServiceImpl);]
         * [gracefulShutdownTomcat.getShutdownServices().add(new TestSimpleShutdownServiceImpl());]
         */
        /* 检索关机服务并加载 */
        var beans = config.getComponentServiceBeans("-shutdown-service-impl");
        beans.stream()
            .filter(bean -> config.getComponentServiceBeans(bean) instanceof ShutdownService)
            .forEach(bean -> gracefulShutdownTomcat.getShutdownServices().add((ShutdownService) config.getComponentServiceBean(bean)));
        return new Response<>().success();
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(gracefulShutdownTomcat);
        return tomcat;
    }


}
