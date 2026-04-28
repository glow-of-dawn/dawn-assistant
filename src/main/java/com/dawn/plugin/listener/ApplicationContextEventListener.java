package com.dawn.plugin.listener;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.redis.primary.RedisKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.stereotype.Component;

/**
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.listener-status"}, havingValue = "enable", matchIfMissing = true)
public class ApplicationContextEventListener implements ApplicationListener<ApplicationContextEvent> {

    private final RedisKeyService redisKeyService;
    private final PluginConfig config;

    public ApplicationContextEventListener(PluginConfig config,
                                           RedisKeyService redisKeyService) {
        this.config = config;
        this.redisKeyService = redisKeyService;
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        log.trace(LogEnmu.LOG3.value(), "ApplicationListener", "SpringBoot 上下文初始化", "ApplicationContextEvent");
        config.setApplicationId(redisKeyService.getPrimary());
    }

}
