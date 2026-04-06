package com.dawn.plugin.config;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 主参数
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 **/
@Data
@Slf4j
@Order(2)
@Configuration
@ConditionalOnProperty(name = {"plugin-status.config-status"}, havingValue = "enable", matchIfMissing = true)
public class PluginConfig {

    @Value("${plugin-params.encoding:UTF-8}")
    private String encoding;
    @Value("${spring.application.name}")
    private String springApplicationName;
    private String applicationId;
    private JsonMapper mapperUpperCamel = JsonMapper.builder().build();
    private JsonMapper mapperLowerCamel = JsonMapper.builder().build();
    private JsonMapper mapperSnake = JsonMapper.builder().build();
    private XmlMapper xmlHeadMapper = XmlMapper.builder().build();
    private XmlMapper xmlMapper = XmlMapper.builder().build();
    private Map<String, Map<String, Object>> componentServicesMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
    private ApplicationContext applicationContext;
    private List<String> beans = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());

    public PluginConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * [获取beans列表]
     *
     * @param partServiceName [partServiceName]
     * @return Map<String, Object>
     */
    public List<String> getComponentServiceBeans(String partServiceName) {
        log.debug(LogEnmu.LOG2.value(), "寻找*", partServiceName);
        List<String> beanNames = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());
        beans.stream()
                .filter(name -> (name.contains(partServiceName) || VarEnmu.STAR.value().equals(partServiceName)))
                .forEach(beanNames::add);
        return beanNames;
    }

    /**
     * [获取bean]
     *
     * @param serviceName [serviceName]
     * @return Object
     */
    public Object getComponentServiceBean(String serviceName) {
        Class<?> beanType = applicationContext.getType(serviceName);
        log.debug(LogEnmu.LOG3.value(), "getComponentServiceBean", serviceName, beanType);
        return Objects.isNull(beanType) ? null : applicationContext.getBean(beanType);
    }

    @Bean(name = "getComponentServiceBeans")
    public List<String> getComponentServiceBeans() {
        beans.addAll(Arrays.asList(applicationContext.getBeanDefinitionNames()));
        return beans;
    }

}
