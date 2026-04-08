package com.dawn.plugin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Slf4j
@Component
@Configuration
@ConditionalOnProperty(name = {"plugin-status.config-status"}, havingValue = "enable", matchIfMissing = true)
public class RestTemplateConfig {

    @Value("${plugin-params.rest-template.read-timeout:180000}")
    private int readTimeout;
    @Value("${plugin-params.rest-template.connect-timeout:240000}")
    private int connectTimeout;

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        /* [SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();] */
        /* [HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();] */
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectTimeout);
        /* 代理 */
        /* [factory.setProxy();] */
        return factory;
    }

}
