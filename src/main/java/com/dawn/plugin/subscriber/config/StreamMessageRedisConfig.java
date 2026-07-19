package com.dawn.plugin.subscriber.config;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.subscriber.consumption.consumer.AbstractConsumerRedisStreamListener;
import com.dawn.plugin.thread.ThreadPoolTaskExecutorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author hforest-480s
 */
@Slf4j
@Component(value = "streamMessageContainerShutdownServiceImpl")
@ConditionalOnProperty(name = {"plugin-status.subscriber-redis-status"}, havingValue = "enable", matchIfMissing = true)
public class StreamMessageRedisConfig implements ApplicationRunner, DisposableBean {

    private final Map<AbstractConsumerRedisStreamListener,
        StreamMessageListenerContainer<String, MapRecord<String, String, String>>> containers =
        HashMap.newHashMap(VarEnmu.SIXTEEN.ivalue());
    @Value("${plugin-subscriber.queue.expire-time:300}")
    private Long streamKeyExpireTime;
    @Value("${plugin-subscriber.queue.record-action-time-seconds:300}")
    private Long recordActionTimeSeconds;
    private final PluginConfig config;
    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisTemplate<String, String> redisTemplate;
    private final ThreadPoolTaskExecutorConfig threadPoolTaskExecutorConfig;

    public StreamMessageRedisConfig(PluginConfig config,
                                    RedisConnectionFactory redisConnectionFactory,
                                    RedisTemplate<String, String> redisTemplate,
                                    ThreadPoolTaskExecutorConfig threadPoolTaskExecutorConfig) {
        this.config = config;
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisTemplate = redisTemplate;
        this.threadPoolTaskExecutorConfig = threadPoolTaskExecutorConfig;
    }

    /**
     * [启动队列]
     */
    public void strartContainer() {
        /* 消费者 */
        config.getComponentServiceBeans(VarEnmu.CONSUMER_REDIS_STREAM_LISTENER.value())
            .forEach(bean -> {
                Object crsListener = config.getComponentServiceBean(bean);
                createReceiveConsumer(bean, (AbstractConsumerRedisStreamListener) crsListener);
            });
    }

    /**
     * [建立消费通道]
     *
     * @param serviceName [serviceName]
     * @param crsListener [crsListener]
     */
    private void createReceiveConsumer(String serviceName, AbstractConsumerRedisStreamListener crsListener) {
        /* 预设加载参数 */
        String key = serviceName.replace(VarEnmu.CONSUMER_REDIS_STREAM_LISTENER.value(), VarEnmu.NONE.value());
        crsListener.setServiceName("consumed-".concat(key));
        /* Stream队列 = [spring.application.name + ':' + consumer + ':' + master(-consumer-redis-impl) ] */
        crsListener.setStreamKey(config.getSpringApplicationName().concat(":consumer:").concat(key));
        /* 消费组 = group + '-' + master(-consumer-redis-impl) */
        crsListener.setStreamGroup("group-".concat(key));
        /* 消费者 = consumer + '-' + master(-consumer-redis-impl) + '-' + applicationId */
        crsListener.setStreamConsumer("consumer-".concat(key).concat(VarEnmu.SLIGHTLY.value()).concat(config.getApplicationId()));

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = getStreamMessageListenerContainer();
        log.debug(LogEnmu.LOG3.value(), "ConsumerRedisStreamListener:增加消费通道", crsListener.getStreamKey(), crsListener.getStreamGroup());
        var createSts = createConsumerGroup(crsListener.getStreamKey(), crsListener.getStreamGroup());
        log.debug(LogEnmu.LOG3.value(), "ConsumerRedisStreamListener:增加消费通道", createSts);
        container.receive(Consumer.from(crsListener.getStreamGroup(), crsListener.getStreamConsumer()),
            StreamOffset.create(crsListener.getStreamKey(), ReadOffset.lastConsumed()),
            crsListener);
        container.start();
        containers.put(crsListener, container);
    }

    /**
     * [建立消费组]
     *
     * @param streamKey   [streamKey]
     * @param streamGroup [streamGroup]
     * @return boolean
     */
    private boolean createConsumerGroup(String streamKey, String streamGroup) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(streamKey))) {
            log.debug(LogEnmu.LOG3_1KV.value(), "消费组已存在", streamKey, streamGroup);
            return true;
        }
        try {
            Optional.ofNullable(redisTemplate
                    .opsForStream()
                    .add(streamKey, Collections.singletonMap(VarEnmu.GROUP_ID.value(), config.getApplicationId())))
                .ifPresent(recordId -> leftPushLostList(streamKey, recordId));
            redisTemplate.expire(streamKey, Duration.ofSeconds(streamKeyExpireTime));
            redisTemplate.expire(streamKey.concat("-success-count"), Duration.ofSeconds(streamKeyExpireTime));
            /*
             * 销毁会注销所有节点，create group 是在redis 服务进行组建
             * [redisTemplate.opsForStream().destroyGroup(streamKey, streamGroup).block(Duration.ofSeconds(blockTime));]
             */
            redisTemplate.opsForStream().createGroup(streamKey, streamGroup);
            return true;
        } catch (RedisSystemException | QueryTimeoutException ex) {
            log.debug(LogEnmu.LOG4.value(), "createConsumerGroup", streamKey, streamGroup, ex.toString());
            return true;
        } catch (Exception ex) {
            log.warn(LogEnmu.LOG4.value(), "createConsumerGroup", streamKey, streamGroup, ex.toString());
            return false;
        }
    }

    /**
     * [定时确保 group-streamKey 存活，如果失效进行恢复]
     * fixedRate 上一次开始执行时间点向后延迟多少时间执行，非上一次执行完[fixedDelay]
     */
    @Scheduled(initialDelay = 1000 * 10, fixedRateString = "${plugin-subscriber.queue.check-time:295000}")
    public void checkConsumerRedisStreamListener() {
        log.debug(LogEnmu.LOG2.value(), "checkConsumerRedisStreamListener.containers", containers);
        containers.forEach((crsListener, container) -> {
            log.debug(LogEnmu.LOG3.value(), "checkConsumerRedisStreamListener.containers", crsListener, container);
            try {
                redisTemplate.expire(crsListener.getStreamKey(), Duration.ofSeconds(streamKeyExpireTime));
                redisTemplate.expire(crsListener.getStreamKey().concat("-success-count"),
                    Duration.ofSeconds(streamKeyExpireTime));
                Optional.ofNullable(redisTemplate.opsForStream().size(crsListener.getStreamKey()))
                    .filter(size -> size > VarEnmu.ZERO.ivalue())
                    .ifPresent(size -> log.info(LogEnmu.LOG2.value(), "待消费数量", size));

                /* 清理lost队列 */
                leftPopLostList(crsListener.getStreamKey());
                /* 死信排查及处理 */
                recordHandler(crsListener.getStreamKey(), crsListener);
            } catch (RedisSystemException ex) {
                log.debug(LogEnmu.LOG3.value(), "消费端可能异常，正在尝试恢复", "checkConsumerRedisStreamListener", ex);
                this.strartContainer();
            }
        });
    }

    /**
     * [获取Container]
     */
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> getStreamMessageListenerContainer() {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> containerOptions =
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                /* 一批次拉取的最大count数，因为是任务，需要逐一拉取 */
                .batchSize(1)
                /* 超时时间，设置为0，表示不超时（超时后会抛出异常） [.pollTimeout(Duration.ofSeconds(blockTime))] */
                /* 序列化器[.serializer(new StringRedisSerializer())] */
                /* 执行消息轮询的执行器 [.executor(this.threadPoolTaskExecutor)] */
                .executor(threadPoolTaskExecutorConfig.asyncServiceExecutor())
                .errorHandler(t -> {
                    log.debug(LogEnmu.LOG3.value(), "消费端可能异常，正在尝试恢复", "StreamMessageListenerContainer", t);
                    strartContainer();
                })
                .build();

        return StreamMessageListenerContainer.create(redisConnectionFactory, containerOptions);
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        this.strartContainer();
    }

    @Override
    public void destroy() {
        containers.forEach((k, v) -> {
            v.stop();
            k.destroy();
        });
    }

    /**
     * [不遍历队列 / 仅清理检索出来的死信]
     *
     * @param streamKey   [streamKey]
     * @param crsListener [crsListener]
     */
    private void recordHandler(String streamKey,
                               AbstractConsumerRedisStreamListener crsListener) {
        /* 清理 5 个 */
        log.debug(LogEnmu.LOG5.value(), "队列检查", crsListener.getStreamKey(), crsListener.getServiceName(), "limit", VarEnmu.FIVE.ivalue());
        var streamOps = crsListener.getRedisTemplate().boundStreamOps(streamKey);
        var recordList = streamOps.range(Range.unbounded(), Limit.limit().count(VarEnmu.FIVE.ivalue()));

        Optional.ofNullable(recordList)
            .filter(records -> !records.isEmpty())
            .ifPresent(records -> records.stream()
                .filter(mapRecord -> mapRecord.getId().getValue().length() >= VarEnmu.THIRTEEN.ivalue())
                .forEach(mapRecord -> {
                    /* 1767860117111-0 */
                    var timestamp = StringUtils.left(mapRecord.getId().getValue(), VarEnmu.THIRTEEN.ivalue());
                    String tstamp = String.format("%-13s", timestamp).replace(" ", "0");
                    LocalDateTime tstampLocalDateTime = Instant.ofEpochMilli(Long.parseLong(tstamp))
                        .atZone(ZoneOffset.ofHours(VarEnmu.EIGHT.ivalue()))
                        .toLocalDateTime();
                    long seconds = Duration.between(tstampLocalDateTime, LocalDateTime.now(PluginConfig.ZONE)).getSeconds();
                    if (seconds > recordActionTimeSeconds) {
                        /* 最后清理 */
                        leftPushLostList(streamKey, mapRecord.getId());
                        log.info(LogEnmu.LOG4.value(), "清理死信消息", streamKey, mapRecord.getId(), seconds);
                    }
                }));
    }

    /**
     * [丢入lost队列]
     *
     * @param streamKey [streamKey]
     * @param recordId  [recordId]
     **/
    private void leftPushLostList(String streamKey, RecordId recordId) {
        String lostKey = streamKey.concat("-lost");
        var val = String.format("%s-%s",
            recordId.getTimestamp(),
            recordId.getSequence());
        Long result = redisTemplate
            .opsForList()
            .leftPush(lostKey, val);
        redisTemplate.expire(lostKey, Duration.ofSeconds(streamKeyExpireTime * VarEnmu.TWO.ivalue()));
        log.debug(LogEnmu.LOG4.value(), "leftPushLostList", streamKey, recordId, result);
    }

    /**
     * [清理lost队列]
     *
     * @param streamKey [streamKey]
     **/
    private void leftPopLostList(String streamKey) {
        String lostKey = streamKey.concat("-lost");
        String logTit = "leftPopLostList";
        if (Boolean.FALSE.equals(redisTemplate.hasKey(lostKey))) {
            log.debug(LogEnmu.LOG3.value(), logTit, streamKey, "没有需要清理的队列");
            return;
        }
        int max = VarEnmu.ONE_HUNDRED.ivalue();
        while (max > VarEnmu.ZERO.ivalue()) {
            String recordIdStr = redisTemplate.opsForList().leftPop(lostKey);
            if (Objects.isNull(recordIdStr)) {
                log.debug(LogEnmu.LOG3.value(), logTit, streamKey, "没有需要清理的数据");
                break;
            }
            String[] os1 = recordIdStr.split(VarEnmu.SLIGHTLY.value());
            RecordId recordId = RecordId.of(Long.parseLong(os1[VarEnmu.ZERO.ivalue()]), Long.parseLong(os1[VarEnmu.ONE.ivalue()]));
            Long delResult = redisTemplate.opsForStream().delete(streamKey, recordId);
            log.debug(LogEnmu.LOG4.value(), logTit, streamKey, "清理结果", delResult);
            max--;
        }
        redisTemplate.expire(lostKey, Duration.ofSeconds(streamKeyExpireTime * VarEnmu.TWO.ivalue()));
    }

}
