package com.dawn.plugin.config;

import cn.hutool.crypto.Padding;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.CryptUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
 * 配置参数处理
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Data
@Slf4j
@Order(0)
@Configuration
@ConditionalOnProperty(name = {"plugin-status.config-environment-status"}, havingValue = "enable", matchIfMissing = true)
public class PluginConfigurableEnvironment implements EnvironmentPostProcessor {

    private String aes;
    private String headName;
    private String headNames;
    private List<String> headNameList;
    private ConfigurableEnvironment environment;

    public PluginConfigurableEnvironment(@Value("#{'${plugin-params.config.aes:vAr8K5fZxrT4iTo5}'}") String aes,
                                         @Value("#{'${plugin-params.config.head-name:}'}") String headName,
                                         @Value("#{'${plugin-params.config.head-names:}'}") String headNames,
                                         ConfigurableEnvironment environment) {
        this.aes = aes;
        this.headName = headName;
        this.headNames = headNames;
        this.environment = environment;
        this.propertyDecry();
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.info(LogEnmu.LOG3.value(), "environment", "environment");
    }

    /**
     * [筛选配置]
     *
     */
    public void propertyDecry() {
        this.headNameList = Arrays
            .stream(headNames.split(VarEnmu.COMMA.value()))
            .map(String::trim)
            .toList();
        MutablePropertySources propertySources = environment.getPropertySources();
        environment.getPropertySources()
            .stream()
            .filter(EnumerablePropertySource.class::isInstance)
            .filter(propertySource -> propertySource.getName().contains(".yml"))
            .filter(propertySource -> propertySource.getName().contains("application"))
            .forEach(propertySource -> {
                log.debug(LogEnmu.LOG2.value(), "propertySource.name", propertySource.getName());
                /* 解密处理 */
                Map<String, Object> propMap = new HashMap<>();
                Arrays.stream(((EnumerablePropertySource<?>) propertySource).getPropertyNames())
                    .forEach(propName -> propMap.put(propName, propDecry(propName, propertySource.getProperty(propName))));
                PropertySource<?> propSource = new MapPropertySource(propertySource.getName(), propMap);
                propertySources.replace(propertySource.getName(), propSource);
            });
    }

    /**
     * [解密处理]
     *
     * @param propName  [propName]
     * @param propValue [propValue]
     * @return Object
     */
    private Object propDecry(String propName, Object propValue) {
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
            case "SRC" -> encryVal;
            case "SM4" ->
                CryptUtil.decodeBase64BySm4Cbc(aes, aes, encryVal, Padding.PKCS5Padding, VarEnmu.UTF8.value());
            case "AES" -> CryptUtil.decodeAesBase64(aes, encryVal);
            default -> encryVal;
        };
    }

    /**
     * [批量获取参数]
     *
     * @param propHead [propHead]
     * @return Map<String, Object>
     */
    public Map<String, Object> getPropMap(String propHead) {
        /* 配置信息获取 */
        Map<String, Object> propMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        environment.getPropertySources()
            .stream()
            .filter(EnumerablePropertySource.class::isInstance)
            .filter(propertySource -> propertySource.getName().contains(".yml"))
            .filter(propertySource -> propertySource.getName().contains("application"))
            .forEach(propertySource -> {
                log.debug(LogEnmu.LOG2.value(), "propertySource", propertySource.getName());
                Arrays.stream(((EnumerablePropertySource) propertySource)
                        .getPropertyNames())
                    .filter(propName -> propName.indexOf(propHead) > VarEnmu.IIT_MINUS_ONE.ivalue())
                    .filter(propName -> !propName.replace(propHead, VarEnmu.NONE.value()).contains(VarEnmu.POINT.value()))
                    .forEach(propName -> {
                        log.debug(LogEnmu.LOG2.value(), "propertySource.".concat(propHead), propName);
                        var prop = propName.replace(propHead, VarEnmu.NONE.value());
                        prop = prop.replaceAll(VarEnmu.SLIGHTLY.value(), VarEnmu.NONE.value()).toLowerCase();
                        propMap.put(prop, environment.getProperty(propName));
                    });
            });
        return propMap;
    }

}
