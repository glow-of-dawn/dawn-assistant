package com.dawn.plugin.subscriber.subscription.receiver;

import com.dawn.plugin.enmu.LogEnmu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

/**
 * [消息接收（订阅）服务]
 * 创建时间：2021/5/30 20:23
 *
 * @author hforest-480s
 */
@Slf4j
@Service(value = "masterReceiverRedisImpl")
@ConditionalOnProperty(name = {
    "plugin-status.subscriber-redis-status",
    "plugin-subscriber.queue.master-receiver-redis"
}, havingValue = "enable", matchIfMissing = true)
public class ReceiverMasterRedisServiceImpl implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;

    public ReceiverMasterRedisServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * [向通道发送消息的方法]
     *
     * @param message [message]
     * @param pattern [pattern = channel]
     **/
    @Override
    public void onMessage(Message message, byte[] pattern) {
        RedisSerializer<String> valueSerializer = stringRedisTemplate.getStringSerializer();
        String deserialize = valueSerializer.deserialize(message.getBody());
        String channel = valueSerializer.deserialize(pattern);
        log.info(LogEnmu.LOG3.value(), "订阅-redis", channel, deserialize);
    }

}
