package com.dawn.plugin.datasource.config;

import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * [druid 配置]
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 **/
@Slf4j
@Configuration
@ConditionalOnProperty(name = {"plugin-status.datasource-status"}, havingValue = "enable", matchIfMissing = true)
public class DruidConfiguration {

    /* 控制台管理用户 */
    @Value("${spring.datasource.druid.stat-view-servlet.login-username:admin-do}")
    private String loginUsername;
    @Value("${spring.datasource.druid.stat-view-servlet.login-password:admin-do}")
    private String loginPassword;
    /* IP白名单 */
    @Value("#{'${spring.datasource.druid.stat-view-servlet.allow:}'}")
    private String allow;
    /* IP黑名单(共同存在时，deny优先于allow) */
    @Value("#{'${spring.datasource.druid.stat-view-servlet.deny:}'}")
    private String deny;
    /* 是否能够重置数据 禁用HTML页面上的“Reset All”功能 */
    @Value("${spring.datasource.druid.stat-view-servlet.reset-enable:false}")
    private String resetEnable;

    @Bean
    public ServletRegistrationBean<Servlet> druidServlet() {
        log.debug(LogEnmu.LOG1.value(), "init Druid Servlet Configuration");
        ServletRegistrationBean<Servlet> servletRegistrationBean = new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        Map<String, String> initParams = HashMap.newHashMap(VarEnmu.FIVE.ivalue());
        initParams.put("loginUsername", loginUsername);
        initParams.put("loginPassword", loginPassword);
        initParams.put("allow", allow);
        initParams.put("deny", deny);
        initParams.put("resetEnable", resetEnable);
        servletRegistrationBean.setInitParameters(initParams);
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<Filter> filterRegistrationBean() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new WebStatFilter());
        /* [bean.setUrlPatterns(Arrays.asList("/*"));] */
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        return filterRegistrationBean;
    }

    /**
     * Spring Boot集成Druid异常discard long time none received connection.
     * 解决druid 日志报错：discard long time none received connection:xxx
     **/
    @PostConstruct
    public void setProperties() {
        log.info(LogEnmu.LOG2.value(), "setProperties", "discard long time none received connection");
        System.setProperty("druid.mysql.usePingMethod", "false");
    }

}
