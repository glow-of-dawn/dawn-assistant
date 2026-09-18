package com.dawn.plugin.controller;

import com.dawn.plugin.authtoken.Authtoken;
import com.dawn.plugin.controller.service.DatabaseRestService;
import com.dawn.plugin.util.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;

/**
 * [database服务]
 * 创建时间：2021/5/30 20:10
 *
 * @author hforest-480s
 */
@Slf4j
@RestController
@RequestMapping(value = "/rest/database/service")
@ConditionalOnProperty(name = {"plugin-rest-controller.assistant-status"}, havingValue = "enable", matchIfMissing = true)
public class DatabaseRestController {

    private final DatabaseRestService databaseRestService;

    public DatabaseRestController(DatabaseRestService databaseRestService) {
        this.databaseRestService = databaseRestService;
    }

    /**
     * [数据调整]
     *
     * @param body 请求体内容
     * @return Object
     **/
    @PostMapping("/edit/tab/temp")
    public Object editHandler(@RequestBody String body) {
        return databaseRestService.editHandler(body);
    }

    @GetMapping("/persistence/test")
    public Response<Object> persistence() {
        return databaseRestService.persistence();
    }


    /**
     * [tabParams变更]
     *
     * @param body [body]
     * @return Response<Object>
     */
    @Authtoken(openAuthtoken = true)
    @SneakyThrows
    @PostMapping("/edit/params")
    public Response<Object> editTabParams(@RequestBody String body) {
        return databaseRestService.editTabParams(body);
    }

    /**
     * [mapper变更]
     *
     * @param body [body]
     * @return Response<Object>
     */
    @Authtoken(openAuthtoken = true)
    @PostMapping("/mapper")
    public Response<Object> mapper(@RequestBody String body) throws InvocationTargetException, IllegalAccessException {
        return databaseRestService.mapper(body);
    }

}
