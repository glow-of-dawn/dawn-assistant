package com.dawn.plugin.system;

import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * [关机服务]
 *
 * @author hforest-480s
 */
@Data
@Slf4j
@Component(value = "gracefulShutdownTomcatService")
@ConditionalOnProperty(name = {"plugin-status.system-status"}, havingValue = "enable", matchIfMissing = true)
public class GracefulShutdownTomcatService implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

    private Connector connector = new Connector();
    private List<ShutdownService> shutdownServices = new ArrayList<>();

    @Override
    public void customize(@NonNull Connector connector) {
        this.connector = connector;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent contextClosedEvent) {
        log.info(LogEnmu.LOG1.value(), " shutdown services");
        shutdownServices.forEach(ShutdownService::shutdown);
        log.info(LogEnmu.LOG1.value(), " shutdown services over");
        this.connector.pause();
        Executor executor = this.connector.getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor threadPoolExecutor) {
            try {
                threadPoolExecutor.shutdown();
                log.info(LogEnmu.LOG1.value(), " 尝试 shutdown tomcat thread pool");
                if (!threadPoolExecutor.awaitTermination(CodeEnmu.SYS_WAIT_TIME_30S.icode(), TimeUnit.SECONDS)) {
                    log.warn(LogEnmu.LOG2.value(), CodeEnmu.SYS_WAIT_TIME_30S.icode(),
                            "s shutdown tomcat thread pool 如果失败，请尝试 forceful shutdown");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
