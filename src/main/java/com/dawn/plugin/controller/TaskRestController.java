package com.dawn.plugin.controller;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.controller.service.TaskRestService;
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

/**
 * 创建时间：2024/3/21 8:18
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/task")
@ConditionalOnProperty(name = {"plugin-rest-controller.task-status"}, havingValue = "enable", matchIfMissing = true)
public class TaskRestController {

    private final TaskRestService taskRestService;

    public TaskRestController(TaskRestService taskRestService) {
        this.taskRestService = taskRestService;
    }

    @SneakyThrows
    @Authtoken(openAuthtoken = true)
    @PostMapping("/edit")
    public Response<Object> editTabTask(@RequestBody String body) {
        return taskRestService.editTabTask(body);
    }

    @Authtoken(openAuthtoken = true)
    @GetMapping("/run/{id}")
    public Response<Object> runid(@PathVariable("id") String id) {
        return taskRestService.runid(id);
    }

    @GetMapping("/runlog/{id}")
    public Response<Object> runlog(@PathVariable("id") String id) {
        return taskRestService.runlog(id);
    }

}
