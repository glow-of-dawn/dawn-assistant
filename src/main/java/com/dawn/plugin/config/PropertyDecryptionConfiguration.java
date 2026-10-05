package com.dawn.plugin.config;

import cn.hutool.crypto.Padding;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.CryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * 创建时间 2026/10/5 20:39
 *
 * @author bhyt2
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "plugin-status.config-environment-status", havingValue = "enable", matchIfMissing = true)
public class PropertyDecryptionConfiguration {

    @Bean
    public static BeanFactoryPostProcessor propertyDecryptionPostProcessor(ConfigurableEnvironment environment) {
        return new PropertyDecryptionPostProcessor(environment);
    }

    private static final class PropertyDecryptionPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered {

        private final ConfigurableEnvironment environment;

        private PropertyDecryptionPostProcessor(ConfigurableEnvironment environment) {
            this.environment = environment;
        }

        @Override
        public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
            log.info(LogEnmu.LOG2.value(), "environment", beanFactory.getClass());
            String aes = environment.getProperty("plugin-params.config.aes", "vAr8K5fZxrT4iTo5");
            String headName = environment.getProperty("plugin-params.config.head-name", VarEnmu.NONE.value());
            String headNames = environment.getProperty("plugin-params.config.head-names", VarEnmu.NONE.value());
            var headNameList = Arrays
                .stream(headNames.split(VarEnmu.COMMA.value()))
                .map(String::trim)
                .toList();

            MutablePropertySources sources = environment.getPropertySources();
            environment.getPropertySources()
                .stream()
                .filter(EnumerablePropertySource.class::isInstance)
                .filter(source -> source.getName().contains(".yml"))
                .filter(source -> source.getName().contains("application"))
                .forEach(source -> {
                    log.debug(LogEnmu.LOG2.value(), "propertySource.name", source.getName());
                    /* 解密处理 */
                    Map<String, Object> propMap = new HashMap<>();
                    Arrays.stream(((EnumerablePropertySource<?>) source).getPropertyNames())
                        .forEach(propName -> propMap.put(propName, propDecry(aes, headName, headNameList, propName, source.getProperty(propName))));
                    PropertySource<?> propSource = new MapPropertySource(source.getName(), propMap);
                    sources.replace(source.getName(), propSource);
                });
        }

        /**
         * [解密处理]
         *
         * @param aes          [aes]
         * @param headName     [headName]
         * @param headNameList [headNameList]
         * @param propName     [propName]
         * @param propValue    [propValue]
         * @return Object
         */
        private Object propDecry(String aes,
                                 String headName,
                                 List<String> headNameList,
                                 String propName,
                                 Object propValue) {
            log.debug(LogEnmu.LOG3.value(), "propertySource.entry", propName, propValue);
            if (Objects.isNull(propValue)
                || !(propValue instanceof String)
                || propValue.toString().length() < VarEnmu.TWELVE.ivalue()
                || propValue.toString().indexOf(headName) == VarEnmu.IIT_MINUS_ONE.ivalue()
                || propValue.toString().equals(headName)) {
                return propValue;
            }
            var propVal = propValue.toString();
            String algorithmType = headNameList.stream()
                .filter(propVal::contains)
                .findFirst()
                .orElse(VarEnmu.NONE.value());
            var encryVal = propVal.replace(headName.concat(algorithmType), VarEnmu.NONE.value());
            algorithmType = algorithmType.replace(VarEnmu.UNDERLINE.value(), VarEnmu.NONE.value());
            return switch (algorithmType) {
                case "SM4" -> CryptUtil.decodeBase64BySm4Cbc(aes, aes, encryVal, Padding.PKCS5Padding, VarEnmu.UTF8.value());
                case "AES" -> CryptUtil.decodeAesBase64(aes, encryVal);
                default -> encryVal;
            };
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }

    }

}
