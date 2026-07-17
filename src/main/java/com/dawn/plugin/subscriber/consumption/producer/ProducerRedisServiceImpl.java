package com.dawn.plugin.subscriber.consumption.producer;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.subscriber.SubscriberRedisService;
import com.dawn.plugin.util.ConvertUtil;
import com.dawn.plugin.util.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [消息生产者]
 * 创建时间：2021/9/9 22:28
 *
 * @author hforest-480s
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.subscriber-redis-status"}, havingValue = "enable", matchIfMissing = true)
public class ProducerRedisServiceImpl<T> implements SubscriberRedisService<T> {

    /* Stream队列 = [spring.application.name + ':' + consumer + ':' + master(-consumer-redis-impl) ] */
    @Value("#{'${spring.application.name}:consumer:'}")
    private String streamKeyHeader;
    @Value("#{'${plugin-subscriber.queue.expire-time:300}'}")
    private Long streamKeyExpireTime;
    @Value("${plugin-subscriber.queue.stream-compress:base64}")
    private String streamCompress;
    private final AtomicInteger atomicInteger = new AtomicInteger(VarEnmu.ZERO.ivalue());
    private final PluginConfig config;
    private final RedisTemplate<String, String> redisTemplate;

    public ProducerRedisServiceImpl(PluginConfig config,
                                    RedisTemplate<String, String> redisTemplate) {
        this.config = config;
        this.redisTemplate = redisTemplate;
    }

    /**
     * [向队列发送消息的方法]
     * sendMessage("master", tabUser);
     *
     * @param queueName [queueName = "master"]
     * @param message   [message = tabUser]
     * @return Response<Object>
     **/
    @Override
    public Response<Object> sendMessage(String queueName, T message) {
        return sendMessage(streamKeyHeader, queueName, message);
    }

    /**
     * [向队列发送消息的方法 - 避免给 messageSha256 相同的信息]
     * sendMessage("assistant-consumed-master", "master", tabUser);
     *
     * @param queueName [queueName = "master"]
     * @param message   [message = tabUser]
     * @return Response<Object>
     **/
    @Override
    @SneakyThrows
    public Response<Object> sendMessage(String streamKeyHeader, String queueName, T message) {
        String streamKey = streamKeyHeader.concat(queueName);
        log.debug(LogEnmu.LOG4.value(), "produced-redis", "sendMessage", streamKey, message);
        String messageJson = message instanceof String str ? str : config.getMapperLowerCamel().writeValueAsString(message);
        if (messageJson.contains(VarEnmu.GROUP_ID.value())) {
            return new Response<>().failure("请勿包含:".concat(VarEnmu.GROUP_ID.value()));
        }
        /* String -> base64 */
        String messageCompress = convertMessage(messageJson);

        /* 消息推送 */
        var recordMsg = StreamRecords.newRecord()
            .ofObject(messageCompress)
            .withStreamKey(streamKey);
        RecordId recordId = redisTemplate.opsForStream().add(recordMsg);
        Assert.notNull(recordId, "sendMessage:RecordId is null!");
        /* 设置生命周期 */
        redisTemplate.expire(streamKey, Duration.ofSeconds(streamKeyExpireTime));
        if (atomicInteger.incrementAndGet() % VarEnmu.ONE_HUNDRED.ivalue() == VarEnmu.ZERO.ivalue()) {
            log.info(LogEnmu.LOG4.value(), "生产-redis", streamKey, "total.producered", atomicInteger.get());
            atomicInteger.set(VarEnmu.ZERO.ivalue());
        }
        return new Response<>().success().message(queueName).data(recordId);
    }

    /**
     * [消息转换]
     */
    private String convertMessage(String messageJson) {
        String messageCompress;
        if (messageJson.isEmpty()) {
            messageCompress = VarEnmu.NONE.value();
        } else if ("base64".equals(streamCompress)) {
            messageCompress = Base64.encodeBase64String(messageJson.getBytes(StandardCharsets.UTF_8));
        } else if ("gzip".equals(streamCompress)) {
            messageCompress = ConvertUtil.gzipCompress(messageJson);
        } else {
            messageCompress = messageJson;
        }
        return messageCompress;
    }

    /**
     * [获取队列长度]
     *
     * @param queueName [queueName = "master"]
     **/
    public long getStreamSize(String queueName) {
        String streamKey = streamKeyHeader.concat(queueName);
        try {
            var size = redisTemplate.opsForStream().size(streamKey);
            return Objects.isNull(size) ? VarEnmu.ONE_HUNDRED.ivalue() : size;
        } catch (Exception e) {
            log.debug(LogEnmu.LOG2.value(), "getStreamSize", e.toString());
            return VarEnmu.ONE_HUNDRED.ivalue();
        }

    }

}
