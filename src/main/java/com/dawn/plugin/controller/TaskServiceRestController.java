//package com.dawn.plugin.controller;
//
//import com.dawn.plugin.authtoken.Authtoken;
//import com.dawn.plugin.config.PluginConfig;
//import com.dawn.plugin.enmu.LogEnmu;
//import com.dawn.plugin.enmu.VarEnmu;
//import com.dawn.plugin.entity.ccore.TabParams;
//import com.dawn.plugin.entity.ccore.TabTask;
//import com.dawn.plugin.mapper.ccore.TabParamsMapper;
//import com.dawn.plugin.mapper.ccore.TabTaskMapper;
//import com.dawn.plugin.task.service.HandleService;
//import com.dawn.plugin.util.Response;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//import java.util.Objects;
//
///**
// * 创建时间：2024/3/21 8:18
// *
// * @author hforest-480s
// */
//@Slf4j
//@RestController
//@RequestMapping(value = "/rest/task")
//@ConditionalOnProperty(name = {"plugin-rest-controller.task-status"}, havingValue = "enable", matchIfMissing = true)
//public class TaskServiceRestController {
//
//    private final TabParamsMapper tabParamsMapper;
//    private final TabTaskMapper tabTaskMapper;
//    private final PluginConfig config;
//
//    public TaskServiceRestController(PluginConfig config,
//                                     TabTaskMapper tabTaskMapper,
//                                     TabParamsMapper tabParamsMapper) {
//        this.config = config;
//        this.tabTaskMapper = tabTaskMapper;
//        this.tabParamsMapper = tabParamsMapper;
//    }
//
//    @SneakyThrows
//    @Authtoken(openAuthtoken = true)
//    @PostMapping("/edit")
//    public Response<Object> editTabTask(@RequestBody String body) {
//        Map<String, Object> taskMap = config.getMapperLowerCamel().readValue(body, Map.class);
//        TabTask tabTask = config.getMapperLowerCamel().convertValue(taskMap, TabTask.class);
//        if (Objects.isNull(tabTask.getId())) {
//            return new Response<>().failure("editTabTask.id参数无效");
//        } else if (taskMap.size() == VarEnmu.ONE.ivalue()) {
//            log.debug(LogEnmu.LOG1.value(), "查询");
//        } else if (VarEnmu.DELETE.value().equals(tabTask.getTaskServer())) {
//            tabTaskMapper.remove(tabTask.getId());
//        } else if (Objects.isNull(tabTaskMapper.find(tabTask.getId()))) {
//            tabTaskMapper.create(tabTask);
//        } else {
//            tabTaskMapper.edit(tabTask);
//        }
//        tabTask = tabTaskMapper.find(tabTask.getId());
//        return new Response<>().data(tabTask).success();
//    }
//
//    @Authtoken(openAuthtoken = true)
//    @GetMapping("/run/{id}")
//    public Response<Object> handleService(@PathVariable("id") String id) {
//        var tabTask = tabTaskMapper.find(id);
//        if (Objects.isNull(tabTask)) {
//            return new Response<>().failure("run.id参数无效");
//        }
//        var handleService = (HandleService) config.getComponentServiceBean(tabTask.getTaskServiceName());
//        handleService.setTabTask(tabTask);
//        handleService.run();
//        return new Response<>().data(tabTask);
//    }
//
//    /**
//     * [tabParams变更]
//     *
//     * @param body [body]
//     * @return
//     */
//    @Authtoken(openAuthtoken = true)
//    @SneakyThrows
//    @PostMapping("/edit/params")
//    public Response<Object> editTabParams(@RequestBody String body) {
//        Map<String, Object> tabParamsMap = config.getMapperLowerCamel().readValue(body, Map.class);
//        TabParams tabParams = config.getMapperLowerCamel().convertValue(tabParamsMap, TabParams.class);
//        if (Objects.isNull(tabParams.getId())) {
//            return new Response<>().failure("editTabParams.id参数无效");
//        } else if (tabParamsMap.size() == VarEnmu.ONE.ivalue()) {
//            log.debug(LogEnmu.LOG1.value(), "查询");
//        } else if (VarEnmu.DELETE.value().equals(tabParams.getId())) {
//            tabParamsMapper.remove(tabParams.getId());
//        } else if (Objects.isNull(tabParamsMapper.find(tabParams.getId()))) {
//            tabParamsMapper.create(tabParams);
//        } else {
//            tabParamsMapper.edit(tabParams);
//        }
//        tabParams = tabParamsMapper.find(tabParams.getId());
//        return new Response<>().data(tabParams).success();
//    }
//
//}
