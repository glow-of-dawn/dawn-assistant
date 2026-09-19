//package com.ycmvp.plugin.config;
//
//import com.cmb.bee.commons.crypto.algorithm.BeeSm4EcbEncryptorCustomer;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.dataformat.xml.XmlMapper;
//import com.ycmvp.plugin.enmu.LogEnmu;
//import com.ycmvp.plugin.enmu.VarEnmu;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.NoSuchBeanDefinitionException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//
///**
// * @author 01109209
// * @date 2025/07/07 14:00:00
// **/
//@Data
//@Slf4j
//@Order(2)
//@Configuration
//@ConditionalOnProperty(name = {"plugin-status.config-status"}, havingValue = "enable", matchIfMissing = true)
//public class PluginConfig {
//
//    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
//    @Value("${plugin-params.zone.id:Asia/Shanghai}")
//    private String zoneId;
//    @Value("${plugin-params.encoding:UTF-8}")
//    private String encoding;
//    @Value("${spring.application.name}")
//    private String springApplicationName;
//    private PluginConfigurableEnvironment pluginConfigurableEnvironment;
//    private String applicationId;
//    private ObjectMapper mapperUpperCamel = new ObjectMapper();
//    private ObjectMapper mapperLowerCamel = new ObjectMapper();
//    private ObjectMapper mapperSnake = new ObjectMapper();
//    private XmlMapper xmlHeadMapper;
//    private XmlMapper xmlMapper;
//    private Map<String, Map<String, Object>> componentServicesMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
//    private ApplicationContext applicationContext;
//    private List<String> beans = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());
//    /* SSRF 防护白名单 */
//    private Set<String> ssrfHostWhiteList = HashSet.newHashSet(VarEnmu.SIXTEEN.ivalue());
//    private Set<String> ssrfPathWhiteList = HashSet.newHashSet(VarEnmu.SIXTEEN.ivalue());
//    private BeeSm4EcbEncryptorCustomer beeSm4EcbEncryptorCustomer = new BeeSm4EcbEncryptorCustomer();
//
//    public PluginConfig(ApplicationContext applicationContext,
//                        PluginConfigurableEnvironment pluginConfigurableEnvironment) {
//        this.applicationContext = applicationContext;
//        this.pluginConfigurableEnvironment = pluginConfigurableEnvironment;
//        pluginConfigurableEnvironment.propertyDecry();
//    }
//
//    /**
//     * [获取beans列表]
//     *
//     * @param partServiceName [partServiceName]
//     * @return Map<String, Object>
//     */
//    public List<String> getComponentServiceBeans(String partServiceName) {
//        log.debug(LogEnmu.LOG2.value(), "寻找*", partServiceName);
//        List<String> beanNames = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());
//        beans.stream()
//            .filter(name -> (name.contains(partServiceName) || VarEnmu.STAR.value().equals(partServiceName)))
//            .forEach(beanNames::add);
//        return beanNames;
//    }
//
//    /**
//     * [获取bean]
//     *
//     * @param serviceName [serviceName]
//     * @return Object
//     */
//    public Object getComponentServiceBean(String serviceName) {
//        Class<?> beanType = applicationContext.getType(serviceName);
//        log.debug(LogEnmu.LOG3.value(), "getComponentServiceBean", serviceName, beanType);
//        return Objects.isNull(beanType) ? null : applicationContext.getBean(beanType);
//    }
//
//    @Bean(name = "getComponentServiceBeans")
//    public List<String> getComponentServiceBeans() {
//        beans.addAll(Arrays.asList(applicationContext.getBeanDefinitionNames()));
//        return beans;
//    }
//
//    public Map<String, Object> getPropMap(String propHead) {
//        return pluginConfigurableEnvironment.getPropMap(propHead);
//    }
//
//}
