package com.dawn.plugin.httpclient;

import com.dawn.plugin.util.Response;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.net.URI;
import java.util.Map;

/**
 * [http 交互]
 * 创建时间：2025/9/14 10:36
 *
 * @author hforest-480s
 */
@Component
public interface PluginHttpClient {

    /**
     * [xml报文交互]
     *
     * @param uri [uri]
     * @return Response<Object>
     **/
    Response<Object> exchangeXml(URI uri);

    /**
     * [xml报文交互]
     *
     * @param uri  [uri]
     * @param body [body is xml]
     * @return Response<Object>
     **/
    Response<Object> exchangeXml(URI uri, @RequestBody String body);

    /**
     * [xml报文交互]
     *
     * @param uri     [uri]
     * @param headers [headers]
     * @param body    [body is xml]
     * @return Response<Object>
     **/
    Response<Object> exchangeXml(URI uri, @RequestHeader Map<String, String> headers, @RequestBody String body);

    /**
     * [json报文交互]
     *
     * @param uri [uri]
     * @return Response<Object>
     **/
    Response<Object> exchangeJson(URI uri);

    /**
     * [json报文交互]
     *
     * @param uri  [uri]
     * @param body [body is json]
     * @return Response<Object>
     **/
    Response<Object> exchangeJson(URI uri, @RequestBody String body);

    /**
     * [json报文交互]
     *
     * @param uri     [uri]
     * @param headers [headers]
     * @param body    [body is json]
     * @return Response<Object>
     **/
    Response<Object> exchangeJson(URI uri, @RequestHeader Map<String, String> headers, @RequestBody String body);

    /**
     * [本地服务调用]
     *
     * @param uri [uri]
     * @return Response<Object>
     **/
    Response<Object> exchangeText(URI uri);

    /**
     * [本地服务调用]
     *
     * @param uri  [uri]
     * @param body [body is text]
     * @return Response<Object>
     **/
    Response<Object> exchangeText(URI uri, @RequestBody String body);

    /**
     * [本地服务调用]
     *
     * @param uri     [uri]
     * @param headers [headers]
     * @param body    [body is text]
     * @return Response<Object>
     **/
    Response<Object> exchangeText(URI uri, @RequestHeader Map<String, String> headers, @RequestBody String body);

    /**
     * [是否打印 请求/响应完整 日志；默认：false]
     * 设置为true，当前请求会打印请求/响应日志，随后则关闭日志打印
     *
     * @param isPrint     [true 打印, false 不打印]
     **/
    void printLogs(boolean isPrint);

}
