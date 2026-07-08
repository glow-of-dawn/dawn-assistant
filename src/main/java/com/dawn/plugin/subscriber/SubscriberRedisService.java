package com.dawn.plugin.subscriber;

import com.dawn.plugin.util.Response;

/**
 * [消息生产]
 * 创建时间：2021/5/30 17:49
 *
 * @author hforest-480s
 */
public interface SubscriberRedisService<T> {

    /**
     * [向通道发送消息的方法，streamKeyHeader：spring.application.name 服务间生产/消费]
     *
     * @param streamKeyHeader     [推送redisHeader]
     * @param channelAndQueueName [channel: 通道 、 队列名称，例如在[assistant:consumer:master]中 channelAndQueueName = master]
     * @param message             [message]
     **/
    Response<Object> sendMessage(String streamKeyHeader, String channelAndQueueName, T message);

    /**
     * [向通道发送消息的方法 当前服务自身生产/消费]
     *
     * @param channelAndQueueName [channel: 通道 、 队列名称，例如在[assistant:consumer:master]中 channelAndQueueName = master]
     * @param message             [message]
     **/
    Response<Object> sendMessage(String channelAndQueueName, T message);

}
