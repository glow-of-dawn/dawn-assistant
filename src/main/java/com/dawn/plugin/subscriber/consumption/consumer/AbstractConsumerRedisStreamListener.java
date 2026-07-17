package com.dawn.plugin.subscriber.consumption.consumer;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.util.ConvertUtil;
import com.dawn.plugin.util.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [消息消费方]
 * 创建时间：2021/9/18 15:41
 *
 * @author hforest-480s
 */
@Data
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.subscriber-redis-status"}, havingValue = "enable", matchIfMissing = true)
public abstract class AbstractConsumerRedisStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    protected PluginConfig config;
    protected RedisTemplate<String, String> redisTemplate;
    @Value("${plugin-subscriber.master-consumer-redis-stream-listener.acknowledge-after:delete}")
    protected String acknowledgeAfter;
    @Value("${plugin-subscriber.queue.block-time:100}")
    private Long blockTime = 100L;
    @Value("${plugin-subscriber.queue.expire-time:300}")
    private Long streamKeyExpireTime;
    /* 默认不留存直接消费掉，避免对redis信息积压 */
    @Value("${plugin-subscriber.queue.stream-acknowledge:true}")
    private boolean streamAcknowledge;
    @Value("${plugin-subscriber.queue.stream-compress:base64}")
    private String streamCompress;
    private final AtomicInteger atomicInteger = new AtomicInteger(VarEnmu.ZERO.ivalue());
    private String serviceName = "consumed*";
    /* Stream队列 = [spring.application.name + ':' + consumer + ':' + master(ConsumerRedisImpl) ] */
    private String streamKey;
    /* 消费组 = group + Master[ConsumerRedisImpl] */
    private String streamGroup = "group*";
    /* 消费者 = consumer + Master[ConsumerRedisImpl] + ApplicationId */
    private String streamConsumer = "consumer*";

    @Autowired
    public void init(PluginConfig config,
                     RedisTemplate<String, String> redisTemplate) {
        this.config = config;
        this.redisTemplate = redisTemplate;
    }

    /**
     * [消息消费]
     *
     * @param message [message]
     */
    @Override
    public void onMessage(@NonNull MapRecord<String, String, String> message) {
        log.debug(LogEnmu.LOG3.value(), this.getServiceName(), "onMessage", message);
        /* 处理消息 */
        RecordId recordId = message.getId();
        Map<String, String> map = message.getValue();
        String messageCompress = map.getOrDefault(VarEnmu.PAYLOAD.value(), VarEnmu.NONE.value());
        if (atomicInteger.incrementAndGet() % VarEnmu.ONE_HUNDRED.ivalue() == VarEnmu.ZERO.ivalue()) {
            log.info(LogEnmu.LOG4.value(), "onMessage", streamKey, "total.consumer", atomicInteger.get());
            atomicInteger.set(VarEnmu.ZERO.ivalue());
        }

        try {
            /* base64 -> String */
            String messageJson = convertMessage(messageCompress);
            /* 处理直接消费 */
            if (streamAcknowledge) {
                redisTemplate.opsForStream().acknowledge(this.getStreamGroup(), message);
            }
            if (VarEnmu.NONE.value().equals(messageJson)) {
                log.debug(LogEnmu.LOG4.value(), this.getStreamKey(), recordId, "无效消息", map);
            } else if (messageJson.contains(VarEnmu.GROUP_ID.value())) {
                log.debug(LogEnmu.LOG4.value(), this.getStreamKey(), recordId, "忽略消息", VarEnmu.GROUP_ID.value());
            } else {
                Response<Object> response = this.handle(messageJson);
                if (!response.isSuccess()) {
                    log.warn(LogEnmu.LOG4.value(), this.getStreamKey(), "handle失败", response);
                    return;
                }
            }
            String successKey = this.getStreamKey().concat("-success-count");
            Long successCount = redisTemplate.opsForValue().increment(successKey);

            /* 生命周期 */
            redisTemplate.expire(successKey, Duration.ofSeconds(streamKeyExpireTime));
            log.debug(LogEnmu.LOG8.value(), this.getServiceName(), this.getStreamGroup(),
                this.getStreamConsumer(), this.getStreamKey(), recordId, "total.consumed", successCount);
        } catch (Exception ex) {
            /* 异常消费掉 */
            log.warn(LogEnmu.LOG3.value(), "onMessage.异常消费.此线程将中断", Thread.currentThread().getName(), ex.toString());
            /* 异常线程可能装死 */
            Thread.currentThread().interrupt();
        } finally {
            if (!streamAcknowledge) {
                redisTemplate.opsForStream().acknowledge(this.getStreamGroup(), message);
            }
            if (VarEnmu.DELETE.value().equals(acknowledgeAfter)) {
                redisTemplate.opsForStream().delete(this.getStreamKey(), recordId);
            }
        }
    }

    /**
     * [执行]
     *
     * @param messageJson [messageJson]
     * @return Response<Object>
     */
    protected abstract Response<Object> handle(String messageJson);

    /**
     * [销毁队列]
     */
    public void destroy() {
        redisTemplate.expire(streamKey, Duration.ofSeconds(VarEnmu.ONE.ivalue()));
    }

    /**
     * [清理队列 / 无效队列]
     */
    public void clean() {
        redisTemplate.opsForStream().destroyGroup(streamKey, streamGroup);
    }

    /**
     * [消息转换]
     */
    private String convertMessage(String messageCompress) {
        String messageJson;
        if (Objects.isNull(messageCompress) || messageCompress.isEmpty()) {
            messageJson = VarEnmu.NONE.value();
        } else if ("base64".equals(streamCompress)) {
            byte[] base64Data = Base64.decodeBase64(messageCompress);
            messageJson = new String(base64Data, StandardCharsets.UTF_8);
        } else if ("gzip".equals(streamCompress)) {
            messageJson = ConvertUtil.gzipDecompress(messageCompress);
        } else {
            messageJson = messageCompress;
        }
        return messageJson;
    }

}
