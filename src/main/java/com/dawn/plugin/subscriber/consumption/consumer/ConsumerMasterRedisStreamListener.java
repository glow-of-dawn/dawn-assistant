package com.dawn.plugin.subscriber.consumption.consumer;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ctemp.Temp;
import com.dawn.plugin.mapper.ctemp.TempMapper;
import com.dawn.plugin.util.RandomUtil;
import com.dawn.plugin.util.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * [消息接收（消费）服务]
 * 创建时间：2021/9/10 6:30
 *
 * @author hforest-480s
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
@Component(value = "masterConsumerRedisStreamListener")
@ConditionalOnProperty(name = {
    "plugin-status.subscriber-redis-status",
    "plugin-subscriber.queue.consumer-master-redis-stream-listener"
}, havingValue = "enable", matchIfMissing = true)
public class ConsumerMasterRedisStreamListener extends AbstractConsumerRedisStreamListener {

    private TempMapper tempMapper;

    public ConsumerMasterRedisStreamListener(TempMapper tempMapper) {
        this.tempMapper = tempMapper;
    }

    /**
     * [执行]
     *
     * @param messageJson [messageJson]
     * @return Response<Object>
     */
    @Override
    @SneakyThrows
    protected Response<Object> handle(String messageJson) {
        log.info(LogEnmu.LOG2.value(), "消费-redis", messageJson);
        long r = RandomUtil.getRandomLong(VarEnmu.FOUR.ivalue());
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(r));
        data(messageJson);
        return new Response<>().success();
    }

    @SneakyThrows
    private void data(String messageJson) {
        Map<String, Object> map = config.getMapperLowerCamel().readValue(messageJson, Map.class);
        Temp temp = config.getMapperLowerCamel().convertValue(map, Temp.class);
        temp = tempMapper.find(temp.getId());
        if (Objects.isNull(temp)) {
            log.warn(LogEnmu.LOG3.value(), "master消费空值", map);
        } else {
            temp.setC4(temp.getC4().add(BigDecimal.ONE));
            temp.setC5(LocalDate.now(PluginConfig.ZONE));
            temp.setC3(LocalDateTime.now(PluginConfig.ZONE));
            temp.setC1("master");
            tempMapper.edit(temp);
        }
    }

}
