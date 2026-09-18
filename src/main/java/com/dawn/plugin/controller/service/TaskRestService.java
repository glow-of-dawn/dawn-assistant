package com.dawn.plugin.controller.service;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.AlgEnmu;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabRunLog;
import com.dawn.plugin.entity.ccore.TabTask;
import com.dawn.plugin.mapper.ccore.TabRunLogMapper;
import com.dawn.plugin.mapper.ccore.TabTaskMapper;
import com.dawn.plugin.task.service.HandleService;
import com.dawn.plugin.util.HashUtil;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 *
 * 创建时间 2026/8/26 20:39
 *
 * @author bhyt2
 */
@Slf4j
@ConditionalOnProperty(name = {"plugin-rest-controller.task-status"}, havingValue = "enable", matchIfMissing = true)
public class TaskRestService {

    private final PluginConfig config;
    private final TabTaskMapper tabTaskMapper;
    private final TabRunLogMapper tabRunLogMapper;

    public TaskRestService(PluginConfig config,
                              TabTaskMapper tabTaskMapper,
                              TabRunLogMapper tabRunLogMapper) {
        this.config = config;
        this.tabTaskMapper = tabTaskMapper;
        this.tabRunLogMapper = tabRunLogMapper;
    }

    public Response<Object> editTabTask(String body) {
        Map<String, Object> taskMap = config.getMapperLowerCamel().readValue(body, Map.class);
        TabTask tabTask = config.getMapperLowerCamel().convertValue(taskMap, TabTask.class);
        if (Objects.isNull(tabTask.getId())) {
            return new Response<>().failure("editTabTask.id参数无效");
        } else if (taskMap.size() == VarEnmu.ONE.ivalue()) {
            log.debug(LogEnmu.LOG1.value(), "查询");
        } else if (VarEnmu.DELETE.value().equals(tabTask.getTaskServer())) {
            tabTaskMapper.remove(tabTask.getId());
        } else if (Objects.isNull(tabTaskMapper.find(tabTask.getId()))) {
            tabTaskMapper.create(tabTask);
        } else {
            tabTaskMapper.edit(tabTask);
        }
        tabTask = tabTaskMapper.find(tabTask.getId());
        return new Response<>().data(tabTask).success();
    }

    public Response<Object> runid(String id) {
        var tabTask = tabTaskMapper.find(id);
        if (Objects.isNull(tabTask)) {
            return new Response<>().failure("run.id参数无效");
        }
        var handlerService = (HandleService) config.getComponentServiceBean(tabTask.getTaskServiceName());
        handlerService.setTabTask(tabTask);
        return handlerService.handle();
    }

    public Response<Object> runlog(String id) {
        TabRunLog tabRunLog = new TabRunLog();
        tabRunLog
            .setId(id)
            .setTaskProject(config.getSpringApplicationName())
            .setTaskType("task-type")
            .setTaskClass(this.getClass().getSimpleName())
            .setTaskBatchSerial(HashUtil.hashString(id, AlgEnmu.SHA256.algorithm()))
            .setTaskStartTime(LocalDateTime.now(PluginConfig.ZONE))
            .setTaskOverTime(LocalDateTime.now(PluginConfig.ZONE))
            .setTaskResult(CodeEnmu.STS_S.code())
            .setTaskException(CodeEnmu.STS_S.description());
        tabRunLogMapper.create(tabRunLog);
        tabRunLogMapper.create(tabRunLog);
        tabRunLog.setTaskType("task-type-task")
            .setTaskOverTime(LocalDateTime.now(PluginConfig.ZONE));
        tabRunLogMapper.create(tabRunLog);

        tabRunLog = tabRunLogMapper.find(id);
        return new Response<>().data(tabRunLog).success();
    }

}
