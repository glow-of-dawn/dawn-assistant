package com.dawn.plugin.subscriber.config;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * [订阅者配置及监听]
 * [@AutoConfigureBefore({PluginConfig.class})]
 * [@AutoConfigureAfter({ReceiverMasterServiceImpl.class, ReceiverSlaveServiceImpl.class})]
 * 创建时间：2021/5/30 20:53
 *
 * @author hforest-480s
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = {"plugin-status.subscriber-redis-status"}, havingValue = "enable", matchIfMissing = true)
public class ReceiverRedisConfig {

    /**
     * [创建消息监听容器 - master]
     *
     * @param redisConnectionFactory [redisConnectionFactory]
     * @return RedisMessageListenerContainer
     */
    @Bean
    @DependsOn(value = "getComponentServiceBeans")
    public RedisMessageListenerContainer getRedisMasterMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, PluginConfig config) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        /* 订阅者 */
        config.getComponentServiceBeans("ReceiverRedisImpl")
            .forEach(bean -> {
                log.info(LogEnmu.LOG3.value(), "addMessageListener", "增加订阅通道", bean);
                Object delegate = config.getComponentServiceBean(bean);
                MessageListenerAdapter adapter = new MessageListenerAdapter(delegate);
                redisMessageListenerContainer.addMessageListener(adapter, new PatternTopic(bean));
            });
        return redisMessageListenerContainer;
    }

}
