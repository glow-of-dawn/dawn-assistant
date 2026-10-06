package com.dawn.plugin.listener;

import com.dawn.plugin.enmu.LogEnmu;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * [项目初始化信息]
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.listener-status"}, havingValue = "enable", matchIfMissing = true)
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${plugin-params.log-sensitive:enable}")
    private String logSensitive;

    /**
     * [这个和 ApplicationStartedEvent 很类似，也是在应用程序上下文刷新之后之后调用，]
     * [区别在于此时ApplicationRunner 和 CommandLineRunner已经完成调用了，也意味着 SpringBoot 加载已经完成。]
     *
     * @param event []
     **/
    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        log.trace(LogEnmu.LOG3.value(), "ApplicationListener", "SpringBoot 加载完成", "ApplicationReadyEvent");
        log.info(LogEnmu.LOG1.value(), "项目初始化完成");
        /* 此项请勿调整，该写法标识设置日志脱敏规则是否启用 */
        log.info(LogEnmu.LOG_SENSITIVE_STATUS.value(), logSensitive);
    }

}
