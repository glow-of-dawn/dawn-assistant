package com.dawn.plugin.subscriber.subscription.publisher;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.subscriber.SubscriberRedisService;
import com.dawn.plugin.util.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * [消息发布者]
 * hforest-480s
 *
 * @author hforest-480s
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.subscriber-redis-status"}, havingValue = "enable", matchIfMissing = true)
public class PublisherRedisServiceImpl<T> implements SubscriberRedisService<T> {

    private final StringRedisTemplate stringRedisTemplate;
    private final PluginConfig config;

    public PublisherRedisServiceImpl(final PluginConfig config,
                                     final StringRedisTemplate stringRedisTemplate) {
        this.config = config;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * [向通道发送消息的方法]
     *
     * @param channel [channel 例如：ReceiverMasterRedisServiceImpl的服务名：MasterReceiverRedisImpl]
     * @param message [message]
     * @return Response<Object>
     **/
    @SneakyThrows
    @Override
    public Response<Object> sendMessage(String channel, T message) {
        log.info(LogEnmu.LOG3.value(), "发布-redis", channel, message);
        String messageStr = message instanceof String str ? str : config.getMapperLowerCamel().writeValueAsString(message);
        stringRedisTemplate.convertAndSend(channel, messageStr);
        return new Response<>().success().message(channel).data(messageStr);
    }

    /**
     * [向通道发送消息的方法]
     *
     * @param streamKeyHeader [streamKeyHeader 参数无用]
     * @param channel         [channel]
     * @param message         [message]
     **/
    @Override
    public Response<Object> sendMessage(String streamKeyHeader, String channel, T message) {
        return sendMessage(channel, message);
    }

}
