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
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlWriteFeature;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import javax.xml.stream.XMLOutputFactory;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    @Value("${plugin-params.zone.id:Asia/Shanghai}")
    private String zoneId;
    @Value("${plugin-params.encoding:UTF-8}")
    private String encoding;
    @Value("${spring.application.name}")
    private String springApplicationName;
    private String applicationId;
    private JsonMapper mapperUpperCamel;
    private JsonMapper mapperLowerCamel;
    private JsonMapper mapperSnake;
    private XmlMapper xmlHeadMapper;
    private XmlMapper xmlMapper;
    private Map<String, Map<String, Object>> componentServicesMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
    private ApplicationContext applicationContext;
    private List<String> beans = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());
    /* SSRF 防护白名单 */
    private Set<String> ssrfHostWhiteList = HashSet.newHashSet(VarEnmu.SIXTEEN.ivalue());
    private Set<String> ssrfPathWhiteList = HashSet.newHashSet(VarEnmu.SIXTEEN.ivalue());

    public PluginConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.mapperUpperCamel = JsonMapper.builder()
            /* new JavaTimeModule() 辅助 string @DateTimeFormat(pattern = "yyyy-MM-dd") to java.time.LocalDate */
            .addModule(new JavaTimeModule())
            /* 对象为空,不抛异常 */
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            /* 反序列化多出属性，不抛异常 */
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            /* userName -> UserName */
            .propertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
            .build();
        this.mapperLowerCamel = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            /* userName -> userName */
            .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
            .build();
        this.mapperSnake = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            /* userName -> user_name */
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();
        this.xmlHeadMapper = XmlMapper.builder()
            .defaultUseWrapper(false)
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            /* xml [<?xml version="1.0" encoding="UTF-8"?>] */
            .enable(XmlWriteFeature.WRITE_XML_DECLARATION)
            .build();
        this.xmlHeadMapper
            .tokenStreamFactory()
            .getXMLOutputFactory()
            .setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
        this.xmlMapper = XmlMapper.builder()
            .defaultUseWrapper(false)
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            /* xml 禁用命名空间 */
            .disable(XmlWriteFeature.WRITE_XML_DECLARATION)
            .build();
        this.xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory()
            .setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
    }

    /**
     * [获取beans列表]
     *
     * @param partServiceName [partServiceName]
     * @return {@code Map<String, Object>}
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
