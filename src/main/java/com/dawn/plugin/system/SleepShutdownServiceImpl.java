package com.dawn.plugin.system;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.mapper.ccore.TabServerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * [关机服务样例]
 * 创建时间：2021/4/9 11:17
 *
 * @author hforest-480s
 */
@Slf4j
@Component(value = "sleepShutdownServiceImpl")
@ConditionalOnProperty(name = {"plugin-status.system-status"}, havingValue = "enable", matchIfMissing = true)
public class SleepShutdownServiceImpl implements ShutdownService {

    private final PluginConfig config;
    private final TabServerMapper tabServerMapper;

    public SleepShutdownServiceImpl(PluginConfig config, TabServerMapper tabServerMapper) {
        this.config = config;
        this.tabServerMapper = tabServerMapper;
    }

    /**
     * [程序处理]
     **/
    @Override
    public void shutdown() {
        Optional.ofNullable(tabServerMapper.find(config.getApplicationId()))
            .ifPresent(tabServer -> {
                tabServer.setApplicationSts(CodeEnmu.STS_C.code());
                tabServerMapper.edit(tabServer);
            });
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(VarEnmu.THREE.ivalue()));
        log.info(LogEnmu.LOG2.value(), "shutdown sleep", "3s");
    }

}
