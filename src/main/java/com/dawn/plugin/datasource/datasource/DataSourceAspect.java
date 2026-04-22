package com.dawn.plugin.datasource.datasource;

import com.dawn.plugin.config.PluginConfigurableEnvironment;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 动态处理数据源，根据命名区分
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Aspect
@Component
@Order(11)
@AutoConfigureAfter(PluginConfigurableEnvironment.class)
@Slf4j
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ConditionalOnProperty(name = {"plugin-status.datasource-status"}, havingValue = "enable", matchIfMissing = true)
public class DataSourceAspect {

    @Pointcut("execution(* com.dawn..*.trans..*.*(..))")
    public void transAspect() {
        /* none */
    }

    /* @Pointcut("execution(* com.dawn.mapper.read8write.*.*(..))") */
    @Pointcut("execution(* com.dawn..*.mapper..*.*(..))")
    public void aspect() {
        /* none */
    }

    @Before("transAspect()")
    public void transBefore(JoinPoint point) {
        log.debug(LogEnmu.LOG3.value(), "dataSource", "transAspect", point.getSignature().getDeclaringTypeName());
        this.before(point);
    }

    @Before("aspect()")
    public void before(JoinPoint point) {
        String method = point.getSignature().getName();
        String declaringTypeName = point.getSignature().getDeclaringTypeName();
        try {
            DynamicDataSource.PACKAGE_8_METHODS_MAP.forEach((k, defs) -> {
                if (declaringTypeName.contains(VarEnmu.POINT.value().concat(k).concat(VarEnmu.POINT.value()))) {
                    for (String key : defs) {
                        if (method.startsWith(key)) {
                            List<DataType> datasources = DynamicDataSource.PACKAGE_METHOD_8_DATASOURCE_MAP.get(k.concat("-").concat(key));
                            directionDynamicDataSource(datasources, declaringTypeName);
                            return;
                        }
                    }
                    /* defs.contains("*") 的情况 */
                    List<DataType> datasources = DynamicDataSource.PACKAGE_METHOD_8_DATASOURCE_MAP.get(k.concat("-*"));
                    directionDynamicDataSource(datasources, declaringTypeName);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 指向数据源
     * String packageName, String methodStart,
     */
    private void directionDynamicDataSource(List<DataType> datasources, String declaringTypeName) {
        DataType type = datasources.get(0);
        log.debug(LogEnmu.LOG3.value(), "方法使用的数据源", declaringTypeName, type);
        DatabaseContextHolder.setDatabaseType(type);
        /* 单数据源不处理 */
        if (datasources.size() == 1) {
            return;
        }
        /* 多数据源进行轮训 */
        type.pushTimestamp();
        /* 进行排序 */
        datasources.sort((DataType dt1, DataType dt2) -> dt1.getTimestamp().compareTo(dt2.getTimestamp()));
        /* 数据源均衡打印 */
        datasources.forEach(ds ->
                log.debug(LogEnmu.LOG3.value(), "name", ds.getName(), "seq", ds.getTimestamp())
        );
        log.debug(LogEnmu.LOG1.value(), "over");
    }

    @Before("@annotation(ds)")
    public void changeDataSource(JoinPoint point, TargetDataSource ds) {
        DataType type = DynamicDataSource.DATA_TYPE_MAP.get(ds.name());
        log.debug(LogEnmu.LOG2.value(), "dataSource", ds.name());
        DatabaseContextHolder.setDatabaseType(type);
    }
}
