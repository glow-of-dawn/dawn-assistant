package com.dawn.plugin.httpclient;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.Response;
import com.dawn.plugin.util.SensitiveUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * [http 交互]
 * 创建时间：2025/9/14 10:36
 *
 * @author hforest-480s
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.rest-client-status"}, havingValue = "enable", matchIfMissing = true)
public class PluginRestClient implements PluginHttpClient {

    @Value("${plugin-params.rest-template.http-header-charset:}")
    private String httpHeaderCharset;
    private final PluginConfig config;
    private final RestClient restClient;
    private final Map<String, String> httpHeadersMap = HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
    private final Range<Integer> statusCodeRange = Range.of(VarEnmu.NUMBER_200.ivalue(), VarEnmu.NUMBER_300.ivalue());
    private final AtomicBoolean printLogFlag = new AtomicBoolean(false);

    public PluginRestClient(PluginConfig config) {
        this.config = config;
        httpHeadersMap.put("X-Access-Token", "--");
        this.restClient = RestClient.builder().build();
    }

    /**
     * [xml报文交互]
     *
     * @param uri [uri]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> exchangeXml(URI uri) {
        var httpHeaders = generateHttpHeaders(MediaType.APPLICATION_XML, Map.of());
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        return exchange(uri, HttpMethod.GET, requestEntity);
    }

    /**
     * [xml报文交互]
     *
     * @param uri  [uri]
     * @param body [body is xml]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> exchangeXml(URI uri, String body) {
        var httpHeaders = generateHttpHeaders(MediaType.APPLICATION_XML, Map.of());
        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
        return exchange(uri, HttpMethod.POST, requestEntity);
    }

    /**
     * [xml报文交互]
     *
     * @param uri     [uri]
     * @param headers [headers]
     * @param body    [body is xml]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> exchangeXml(URI uri, Map<String, String> headers, String body) {
        var httpHeaders = generateHttpHeaders(MediaType.APPLICATION_XML, headers);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
        return exchange(uri, HttpMethod.POST, requestEntity);
    }

    /**
     * [json报文交互]
     *
     * @param uri [uri]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> exchangeJson(URI uri) {
        var httpHeaders = generateHttpHeaders(MediaType.APPLICATION_JSON, headers);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
        return exchange(uri, HttpMethod.POST, requestEntity);
    }

    /**
     * [json报文交互]
     *
     * @param uri  [uri]
     * @param body [body is json]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> exchangeJson(URI uri, String body) {
        var httpHeaders = generateHttpHeaders(MediaType.APPLICATION_JSON, headers);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
        return exchange(uri, HttpMethod.POST, requestEntity);
    }

    /**
     * [json报文交互]
     *
     * @param uri     [uri]
     * @param headers [headers]
     * @param body    [body is json]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> exchangeJson(URI uri, Map<String, String> headers, String body) {
        var httpHeaders = generateHttpHeaders(MediaType.TEXT_PLAIN, Map.of());
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        return exchange(uri, HttpMethod.GET, requestEntity);
    }

    /**
     * [本地服务调用]
     *
     * @param uri [uri]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> exchangeText(URI uri) {
        return null;
    }

    /**
     * [本地服务调用]
     *
     * @param uri  [uri]
     * @param body [body is text]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> exchangeText(URI uri, String body) {
        var httpHeaders = generateHttpHeaders(MediaType.TEXT_PLAIN, Map.of());
        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
        return exchange(uri, HttpMethod.POST, requestEntity);
    }

    /**
     * [本地服务调用]
     *
     * @param uri     [uri]
     * @param headers [headers]
     * @param body    [body is text]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> exchangeText(URI uri, Map<String, String> headers, String body) {
        var httpHeaders = generateHttpHeaders(MediaType.TEXT_PLAIN, headers);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
        return exchange(uri, HttpMethod.POST, requestEntity);
    }

    /**
     * [是否打印 请求/响应完整 日志；默认：false]
     * 设置为true，当前请求会打印请求/响应日志，随后则关闭日志打印
     *
     * @param isPrint [true 打印, false 不打印]
     **/
    @Override
    public void printLogs(boolean isPrint) {
        printLogFlag.set(isPrint);
    }

    /**
     * [生成 HttpHeaders]
     *
     * @param mediaType [mediaType]
     * @param headers   [headers]
     * @return HttpHeaders
     **/
    private HttpHeaders generateHttpHeaders(MediaType mediaType, final Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeadersMap.forEach(httpHeaders::set);
        headers.entrySet()
            .stream()
            .filter(entry -> Objects.nonNull(entry.getKey()))
            .filter(entry -> Objects.nonNull(entry.getValue()))
            .forEach(entry -> httpHeaders.set(entry.getKey(), entry.getValue()));
        Charset charset = StringUtils.isNotBlank(httpHeaderCharset) ? Charset.forName(httpHeaderCharset) : StandardCharsets.UTF_8;

        List<MediaType> accepts = new ArrayList<>();
        switch (mediaType.toString()) {
            case "application/json":
                httpHeaders.setContentType(new MediaType(MediaType.APPLICATION_JSON, charset));
                accepts.add(MediaType.APPLICATION_JSON);
                break;
            case "application/xml":
                httpHeaders.setContentType(new MediaType(MediaType.APPLICATION_XML, charset));
                accepts.add(MediaType.APPLICATION_XML);
                break;
            case "text/plain":
                httpHeaders.setContentType(new MediaType(MediaType.TEXT_PLAIN, charset));
                accepts.add(MediaType.TEXT_PLAIN);
                break;
            default:
                httpHeaders.setContentType(MediaType.TEXT_HTML);
                accepts.add(MediaType.TEXT_HTML);
        }
        httpHeaders.setAccept(accepts);
        log.debug(LogEnmu.LOG4.value(), "http-headers", mediaType, headers, httpHeaders);
        return httpHeaders;
    }

    /**
     * [执行请求]
     *
     * @param uri           [uri]
     * @param method        [method]
     * @param requestEntity [requestEntity]
     * @return Response<Object>
     **/
    private Response<Object> exchange(URI uri, HttpMethod method, HttpEntity<String> requestEntity) {
        if (!config.getSsrfHostWhiteList().isEmpty() && !config.getSsrfHostWhiteList().contains(uri.getHost())) {
            throw new IllegalArgumentException("访问地址不在白名单内<Host>:".concat(uri.getHost()));
        } else if (!config.getSsrfPathWhiteList().isEmpty() && !config.getSsrfPathWhiteList().contains(uri.getPath())) {
            throw new IllegalArgumentException("访问地址不在白名单内<Path>:".concat(uri.getPath()));
        }
        Optional.ofNullable(uri.getScheme())
            .filter(scheme -> !"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
            .ifPresent(scheme -> {
                throw new IllegalArgumentException("访问协议仅支持 http / https 协议!");
            });

        if (printLogFlag.get()) {
            log.info(LogEnmu.LOG3.value(), "请求报文", uri, SensitiveUtil.desensitization(requestEntity.getBody()));
        } else {
            log.info(LogEnmu.LOG3.value(), "请求报文", uri, String.valueOf(requestEntity.getBody()).length());
        }

        Response<Object> response = new Response<>();
        try {
            var requestSpec = restClient.method(method)
                .uri(uri)
                .headers(headers -> headers.addAll(requestEntity.getHeaders()));

            if (requestEntity.hasBody() && Objects.nonNull(requestEntity.getBody())) {
                RestClient.RequestBodySpec bodySpec = requestSpec;
                requestSpec = bodySpec.body(requestEntity.getBody());
            }

            ResponseEntity<String> responseEntity = requestSpec
                .retrieve()
                .toEntity(String.class);
            response = new Response<>().data(responseEntity.getBody());

            log.info(LogEnmu.LOG3.value(), "响应报文", responseEntity.getStatusCode(),
                printLogFlag.get() ? SensitiveUtil.desensitization(response.getData()) : StringUtils.length(response.getData()));

            if (statusCodeRange.contains(responseEntity.getStatusCode().value())) {
                response.success().code(responseEntity.getStatusCode().value()).data(responseEntity.getBody());
            } else {
                response.failure(String.valueOf(responseEntity.getBody())).code(getStatusCode().value());
            }
        } catch (Exception ex) {
            log.warn(LogEnmu.LOG2.value(), "PluginRestTemplateImpl.exchange", ex.toString());
            response.failure("请求失败").data(ex.toString()).code(VarEnmu.NUMBER_450.ivalue());
        }
        printLogFlag.set(false);
        return response;
    }

}
