package com.dawn.plugin.listener;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.dataformat.xml.XmlMapper;

import javax.xml.stream.XMLOutputFactory;

/**
 * [项目初始化信息]
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.listener-status"}, havingValue = "enable", matchIfMissing = true)
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    private final PluginConfig config;
    @Value("${plugin-params.log-sensitive:enable}")
    private String logSensitive;

    public ApplicationReadyEventListener(PluginConfig config) {
        this.config = config;
    }

    /**
     * [这个和 ApplicationStartedEvent 很类似，也是在应用程序上下文刷新之后之后调用，]
     * [区别在于此时ApplicationRunner 和 CommandLineRunner已经完成调用了，也意味着 SpringBoot 加载已经完成。]
     *
     * @param event []
     **/
    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        log.trace(LogEnmu.LOG3.value(), "ApplicationListener", "SpringBoot 加载完成", "ApplicationReadyEvent");
        log.trace(LogEnmu.LOG2.value(), "mapper", "初始化");

        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
        config.setXmlHeadMapper(XmlMapper.builder()
            .defaultUseWrapper(false)
            .build());
        config.setXmlMapper(XmlMapper.builder()
            .defaultUseWrapper(false)
            .build());

        /* 辅助 string @DateTimeFormat(pattern = "yyyy-MM-dd") to java.time.LocalDate */
        config.getMapperUpperCamel().registerModule(new JavaTimeModule());
        config.getMapperSnake().registerModule(new JavaTimeModule());
        config.getMapperLowerCamel().registerModule(new JavaTimeModule());
//        config.getXmlMapper().registerModule(new JavaTimeModule());
//        config.getXmlHeadMapper().registerModule(new JavaTimeModule());

        /* 对象为空,不抛异常 */
        config.getMapperUpperCamel().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        config.getMapperSnake().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        config.getMapperLowerCamel().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        config.getXmlMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        config.getXmlHeadMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        /* 反序列化多出属性，不抛异常 */
        config.getMapperUpperCamel().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        config.getMapperSnake().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        config.getMapperLowerCamel().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        config.getXmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        config.getXmlHeadMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /* userName -> UserName */
        config.getMapperUpperCamel().setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
        /* userName -> user_name */
        config.getMapperSnake().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        /* xml [<?xml version="1.0" encoding="UTF-8"?>] */
//        config.getXmlHeadMapper().configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
//        /* xml 禁用命名空间 */
//        config.getXmlHeadMapper().configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
//        config.getXmlMapper().configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
//        /* xml 禁用命名空间 */
//        config.getXmlHeadMapper().getFactory()
//            .getXMLOutputFactory()
//            .setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
//        config.getXmlMapper().getFactory()
//            .getXMLOutputFactory()
//            .setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);

        log.trace(LogEnmu.LOG2.value(), "mapper", "over");
        log.info(LogEnmu.LOG1.value(), "项目初始化完成");
        /* 此项请勿调整，该写法标识设置日志脱敏规则是否启用 */
        log.info(LogEnmu.LOG_SENSITIVE_STATUS.value(), logSensitive);
    }

}
