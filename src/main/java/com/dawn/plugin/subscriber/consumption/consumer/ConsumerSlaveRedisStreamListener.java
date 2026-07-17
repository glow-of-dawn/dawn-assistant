package com.dawn.plugin.subscriber.consumption.consumer;

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
 * 创建时间：2021/9/17 21:35
 *
 * @author hforest-480s
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
@Component(value = "slaveConsumerRedisStreamListener")
@ConditionalOnProperty(name = {
    "plugin-status.subscriber-redis-status",
    "plugin-subscriber.queue.consumer-slave-redis-stream-listener"
}, havingValue = "enable", matchIfMissing = true)
public class ConsumerSlaveRedisStreamListener extends AbstractConsumerRedisStreamListener {

    private TempMapper tempMapper;

    public ConsumerSlaveRedisStreamListener(TempMapper tempMapper) {
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
            log.warn(LogEnmu.LOG3.value(), "slave消费空值", map);
        } else {
            temp.setC4(temp.getC4().add(BigDecimal.ONE));
            temp.setC5(LocalDate.now());
            temp.setC3(LocalDateTime.now());
            temp.setC1("slave");
            tempMapper.edit(temp);
        }
    }

}
