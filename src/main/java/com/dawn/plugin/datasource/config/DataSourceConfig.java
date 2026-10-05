package com.dawn.plugin.datasource.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.dawn.plugin.datasource.datasource.DataType;
import com.dawn.plugin.datasource.datasource.DatabaseContextHolder;
import com.dawn.plugin.datasource.datasource.DynamicDataSource;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [Description 数据源配置]
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 **/
@Slf4j
@Order(10)
@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(name = {"plugin-status.datasource-status"}, havingValue = "enable", matchIfMissing = true)
public class DataSourceConfig {

    private final ConfigurableEnvironment env;
    private static final String ENV_HEADER = "spring.datasource.";
    @Value("#{'${spring.datasource.dynamic-enable:false}'}")
    private boolean dynamicEnable;
    @Value("#{'${spring.datasource.druid-prop-head:spring.datasource.druid.}'}")
    private String druidPropHead;
    private final List<DruidDataSource> dataSources = new ArrayList<>(VarEnmu.SIXTEEN.ivalue());

    public DataSourceConfig(ConfigurableEnvironment environment) {
        this.env = environment;
    }

    /**
     * 创建数据源
     *
     * @param envName [envName]
     * @return javax.sql.DataSource
     **/
    public DruidDataSource createDataSource(String envName) {
        log.info(LogEnmu.LOG2.value(), "数据源装载.create", envName);
        DruidDataSource dataSource = new DruidDataSource();
        try {
            /* 获取参数 */
            String url = env.getProperty(ENV_HEADER.concat(envName).concat(".url"));
            String driverClassName = env.getProperty(ENV_HEADER.concat(envName).concat(".driver-class-name"));
            String username = env.getProperty(ENV_HEADER.concat(envName).concat(".username"));
            String password = env.getProperty(ENV_HEADER.concat(envName).concat(".password"));
            /* */
            dataSource.setUrl(url);
            dataSource.setDriverClassName(driverClassName);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            /* 配置信息获取 */
            Map<String, Object> propMap = this.getPropMap(druidPropHead, env);
            /* 寻找 */
            final BeanWrapper src = new BeanWrapperImpl(dataSource);
            java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
            AtomicInteger atomCnt = new AtomicInteger(VarEnmu.ZERO.ivalue());
            Arrays.stream(pds)
                .filter(pd -> propMap.containsKey(pd.getName().toLowerCase()))
                .forEach(pd -> {
                    log.debug(LogEnmu.LOG4.value(), "发现配置", atomCnt.getAndIncrement(),
                        pd.getName().toLowerCase(), propMap.get(pd.getName().toLowerCase()));
                    src.setPropertyValue(pd.getName(), propMap.get(pd.getName().toLowerCase()));
                });
            log.info(LogEnmu.LOG2.value(), "druid自定义配置", atomCnt.get());
            dataSources.add(dataSource);
            return dataSource;
        } catch (RuntimeException ex) {
            dataSource.close();
            throw ex;
        }
    }


    @Bean
    @Primary
    public DynamicDataSource dataSource() {
        DynamicDataSource dataSource = new DynamicDataSource();
        if (!this.dynamicEnable) {
            return this.singeDataSource(dataSource);
        }
        log.info(LogEnmu.LOG1.value(), "数据源装载.start");
        Map<Object, Object> targetDataSources = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        /* 获取数据列表 */
        List<String> dynamicDatasources = Binder.get(env)
            .bind("spring.datasource.dynamic-datasources", Bindable.listOf(String.class))
            .orElseGet(List::of);
        for (String dsName : dynamicDatasources) {
            DataType dataType = new DataType(dsName);
            try {
                /* 创建数据源 */
                DruidDataSource ds = createDataSource(dataType.getName());
                /* 默认的datasource设置 */
                if ("master".equals(dataType.getName())) {
                    log.info(LogEnmu.LOG2.value(), "主数据源", dataType.getName());
                    dataSource.setDefaultTargetDataSource(ds);
                }
                /* 增加数据源 */
                targetDataSources.put(dataType.getName(), ds);
                log.debug(LogEnmu.LOG3.value(), "targetDataSources.put", dataType.getName(), ds);
                /* 增加数据源分配规则 */
                String path = ENV_HEADER.concat(dataType.getName()).concat(".package-");
                List<String> packageNames = Binder.get(env)
                    .bind(path.concat("names"), Bindable.listOf(String.class))
                    .orElseGet(List::of);
                packageNames.forEach(packageName -> {
                    List<String> defs = Binder.get(env)
                        .bind(path.concat(packageName).concat("-def"), Bindable.listOf(String.class))
                        .orElseGet(ArrayList::new);
                    if (defs.isEmpty()) {
                        defs.add("*");
                    }
                    dataSource.setMethodType(dataType, packageName, defs);
                });
            } catch (Exception ex) {
                log.error(LogEnmu.LOG3.value(), "dataSource.setup is error", dataType.getName(), ex.toString());
            }
        }
        /* 该方法是AbstractRoutingDataSource的方法 */
        dataSource.setTargetDataSources(targetDataSources);
        log.info(LogEnmu.LOG1.value(), "数据源装载.over");
        return dataSource;
    }

    private DynamicDataSource singeDataSource(DynamicDataSource dataSource) {
        log.info(LogEnmu.LOG1.value(), "数据源装载.singe-start");
        /* 不采用多数据源方式，独立建立数据源并默认指向master */
        DataType dataType = new DataType("master");
        /* 配置项中抹去数据源配置可获取原生数据配置信息并进行创建加载 */
        DataSource ds = createDataSource(VarEnmu.NONE.name());
        dataSource.setDefaultTargetDataSource(ds);
        Map<Object, Object> targetDataSources = HashMap.newHashMap(VarEnmu.ONE.ivalue());
        targetDataSources.put(dataType.getName(), ds);
        DatabaseContextHolder.setDatabaseType(dataType);
        /* 该方法是AbstractRoutingDataSource的方法 */
        dataSource.setTargetDataSources(targetDataSources);
        DynamicDataSource.setDataTypeMap(dataType);
        log.info(LogEnmu.LOG1.value(), "数据源装载.over");
        return dataSource;
    }

    @Bean
    @ConfigurationProperties(prefix = "mybatis.configuration")
    public org.apache.ibatis.session.Configuration globalConfiguration() {
        return new org.apache.ibatis.session.Configuration();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(org.apache.ibatis.session.Configuration config) throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(this.dataSource());
        fb.setTypeAliasesPackage(env.getProperty("mybatis.type-aliases-package"));
        log.debug(LogEnmu.LOG2.value(), "SqlSessionFactoryBean.setTypeAliasesPackage", env.getProperty("mybatis.type-aliases-package"));
        String locations = env.getProperty("mybatis.mapper-locations");
        if (Objects.nonNull(locations)) {
            fb.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(locations));
        }
        log.debug(LogEnmu.LOG2.value(), "SqlSessionFactoryBean.setMapperLocations", env.getProperty("mybatis.mapper-locations"));
        fb.setConfiguration(config);
        return fb.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DynamicDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @PreDestroy
    public void closeDataSources() {
        dataSources.forEach(DruidDataSource::close);
        dataSources.clear();
    }

    /**
     * [批量获取参数]
     *
     * @param propHead [propHead]
     * @return {@code Map<String, Object>}
     */
    public Map<String, Object> getPropMap(String propHead,
                                          ConfigurableEnvironment environment) {
        /* 配置信息获取 */
        Map<String, Object> propMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
        environment.getPropertySources()
            .stream()
            .filter(EnumerablePropertySource.class::isInstance)
            .filter(source -> source.getName().contains(".yml"))
            .filter(source -> source.getName().contains("application"))
            .forEach(source -> {
                log.debug(LogEnmu.LOG2.value(), "source", source.getName());
                Arrays.stream(((EnumerablePropertySource<?>) source)
                        .getPropertyNames())
                    .filter(propName -> propName.indexOf(propHead) > VarEnmu.IIT_MINUS_ONE.ivalue())
                    .filter(propName -> !propName.replace(propHead, VarEnmu.NONE.value()).contains(VarEnmu.POINT.value()))
                    .forEach(propName -> {
                        log.debug(LogEnmu.LOG2.value(), "source.".concat(propHead), propName);
                        var prop = propName.replace(propHead, VarEnmu.NONE.value());
                        prop = prop.replaceAll(VarEnmu.SLIGHTLY.value(), VarEnmu.NONE.value()).toLowerCase();
                        propMap.put(prop, environment.getProperty(propName));
                    });
            });
        return propMap;
    }

}
