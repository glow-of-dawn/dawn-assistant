package com.dawn.plugin.task.service;

import com.dawn.plugin.config.LoadParams;
import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabServer;
import com.dawn.plugin.entity.ccore.TabTask;
import com.dawn.plugin.mapper.ccore.TabRunLogMapper;
import com.dawn.plugin.mapper.ccore.TabServerMapper;
import com.dawn.plugin.mapper.ccore.TabTaskMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 静态定时任务，基于注入
 *
 * @author forest
 * @date 2020/11/27 17:20
 */
@Slf4j
@Service
@ConditionalOnProperty(name = {"plugin-status.task-status"}, havingValue = "enable", matchIfMissing = true)
public class StaticSchedulingService {

    private final TabTaskMapper tabTaskMapper;
    private final LoadParams loadParams;
    private final TabRunLogMapper tabRunLogMapper;
    private final TabServerMapper tabServerMapper;
    private final DynamicSchedulingService dynamicSchedulingService;
    @Value("${spring.application.name}")
    private String springApplicationName;
    @Value("${plugin-params.addr-host:localhost}")
    private String addrHost;
    @Value("#{'${plugin-params.addr-site:}'}")
    private String addrSite;
    /* 定时任务执行日志存活天数：默认半年 */
    @Value("${plugin-params.run-log.expire:180}")
    private int expire;
    private final PluginConfig config;

    public StaticSchedulingService(PluginConfig config,
                                   LoadParams loadParams,
                                   TabTaskMapper tabTaskMapper,
                                   TabRunLogMapper tabRunLogMapper,
                                   TabServerMapper tabServerMapper,
                                   DynamicSchedulingService dynamicSchedulingService) {
        this.config = config;
        this.loadParams = loadParams;
        this.tabTaskMapper = tabTaskMapper;
        this.tabRunLogMapper = tabRunLogMapper;
        this.tabServerMapper = tabServerMapper;
        this.dynamicSchedulingService = dynamicSchedulingService;
    }

    /**
     * [动态任务加载处理]
     * 秒 分 时 日 月 星期 年份
     *
     **/
    @Scheduled(cron = "#{'${plugin-schedule.refresh-dynamic-scheduled-tasks-cron:0 * * * * ?}'}")
    public void refreshDynamicScheduledTasks() {
        log.info(LogEnmu.LOG2.value(), "定时任务", "动态任务加载");
        List<TabTask> tabTasks = tabTaskMapper.findByProjectAndSts(springApplicationName, "R", LocalDateTime.now());
        var response = dynamicSchedulingService.refreshTasks(tabTasks);
        if (!response.isSuccess()) {
            log.warn(LogEnmu.LOG2.value(), "定时任务动态加载异常，请检查配置", response.getMessage());
        }
        registerServer(config.getApplicationId());
    }

    /**
     * [定时清理tabRunLog历史数据]
     * 秒 分 时 日 月 星期 年份
     *
     **/
    @Scheduled(cron = "#{'${plugin-schedule.clear-tab-run-log-scheduled-tasks-cron:25 11 * * * ?}'}")
    public void cleartabRunLogScheduledTasks() {
        var status = loadParams.loadKey("clear-tab-run-log", "status");
        if (VarEnmu.DISABLE.value().equals(status)) {
            return;
        }
        /* tabRunLogMapper长期日志清理 */
        var cnt = tabRunLogMapper.removeByInvalid(springApplicationName, expire);
        log.info(LogEnmu.LOG2.value(), "清理过期日志", cnt);
    }

    /**
     * [服务注册 / 服务更新]
     *
     * @param applicationId [applicationId]
     **/
    @SneakyThrows
    private void registerServer(String applicationId) {
        Optional<TabServer> opt = Optional.ofNullable(tabServerMapper.find(applicationId));
        var addrLocal = InetAddress.getLocalHost().getHostAddress();
        TabServer tabServer = opt.orElseGet(() -> {
            var tserver = new TabServer()
                .setId(applicationId)
                .setApplicationName(springApplicationName)
                .setAddrLocal(addrLocal)
                .setAddrHost(addrHost)
                .setApplicationSts(CodeEnmu.STS_A.code())
                .setAddrSite(addrSite)
                .setCreateTime(LocalDateTime.now())
                .setLastTime(LocalDateTime.now())
                .setReadCnt(VarEnmu.ZERO.ivalue());
            tabServerMapper.create(tserver);
            return tserver;
        });
        tabServer.setApplicationSts(CodeEnmu.STS_A.code());
        tabServer.setLastTime(LocalDateTime.now());
        tabServerMapper.edit(tabServer);

        var tabServers = tabServerMapper.findByApplicationStsAndActionTime(CodeEnmu.STS_A.code(),
            LocalDateTime.now().minusSeconds(VarEnmu.NUMBER_600.ivalue()));
        tabServers.forEach(tserver -> {
            tserver.setApplicationSts(CodeEnmu.STS_D.code());
            tabServerMapper.edit(tserver);
        });
    }

}
