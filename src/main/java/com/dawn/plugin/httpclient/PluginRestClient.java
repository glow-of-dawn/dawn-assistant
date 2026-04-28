package com.dawn.plugin.httpclient;

import com.dawn.plugin.enmu.LogEnmu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * [http 交互]
 * 创建时间：2025/9/14 10:36
 *
 * @author forest
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.rest-client-status"}, havingValue = "enable", matchIfMissing = true)
public class PluginRestClient {

    private final RestClient restClient;

    public PluginRestClient() {
        this.restClient = RestClient.builder().build();
    }

    /**
     * [http 交互]
     *
     * @param uri [uri]
     * @return String
     **/
    public String clientGetJson(String uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return clientGet(uri, String.class, headers);
    }

    /**
     * [http 交互]
     *
     * @param uri          [uri]
     * @param responseType [返回类型]
     * @param httpHeaders  [httpHeaders]
     * @return <T> T
     **/
    public <T> T clientGet(String uri, Class<T> responseType, HttpHeaders httpHeaders) {
        try {
            return restClient.get()
                    .uri(uri)
                    .headers(headers -> headers.addAll(httpHeaders))
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException e) {
            log.warn(LogEnmu.LOG4.value(), "rest.client.get", uri, e.getMessage());
            return null;
        }
    }

    /**
     * [http 交互]
     *
     * @param uri         [uri]
     * @param requestBody [requestBody]
     * @return String
     **/
    public String clientPostJson(String uri, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return clientPost(uri, String.class, headers, requestBody);
    }

    /**
     * [http 交互]
     *
     * @param uri          [uri]
     * @param responseType [返回类型]
     * @param httpHeaders  [httpHeaders]
     * @param requestBody  [requestBody]
     * @return <T> T
     **/
    public <T> T clientPost(String uri, Class<T> responseType, HttpHeaders httpHeaders, Object requestBody) {
        try {
            return restClient.post()
                    .uri(uri)
                    .headers(headers -> headers.addAll(httpHeaders))
                    .body(requestBody)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException e) {
            log.warn(LogEnmu.LOG4.value(), "rest.client.post", uri, e.getMessage());
            return null;
        }
    }

}
