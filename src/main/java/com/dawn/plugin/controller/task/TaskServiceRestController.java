package com.dawn.plugin.controller.task;

import com.dawn.plugin.authtoken.Authtoken;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 创建时间：2024/3/21 8:18
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/task")
@ConditionalOnProperty(name = {"plugin-rest-controller.task-status"}, havingValue = "enable", matchIfMissing = true)
public class TaskServiceRestController {

    private final PluginConfig config;
    private final TabTaskMapper tabTaskMapper;
    private final TabRunLogMapper tabRunLogMapper;

    public TaskServiceRestController(PluginConfig config,
                                     TabTaskMapper tabTaskMapper,
                                     TabRunLogMapper tabRunLogMapper) {
        this.config = config;
        this.tabTaskMapper = tabTaskMapper;
        this.tabRunLogMapper = tabRunLogMapper;
    }

    @SneakyThrows
    @Authtoken(openAuthtoken = true)
    @PostMapping("/edit")
    public Response<Object> editTabTask(@RequestBody String body) {
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

    @Authtoken(openAuthtoken = true)
    @GetMapping("/run/{id}")
    public Response<Object> handlerService(@PathVariable("id") String id) {
        var tabTask = tabTaskMapper.find(id);
        if (Objects.isNull(tabTask)) {
            return new Response<>().failure("run.id参数无效");
        }
        var handlerService = (HandleService) config.getComponentServiceBean(tabTask.getTaskServiceName());
        handlerService.setTabTask(tabTask);
        return handlerService.handle();
    }

    @GetMapping("/runlog/{id}")
    public Response<Object> runlog(@PathVariable("id") String id) {
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
