package com.dawn.plugin.controller.service;

import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.CodeEnmu;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import com.dawn.plugin.entity.ccore.TabParams;
import com.dawn.plugin.mapper.ReflectionMapper;
import com.dawn.plugin.mapper.ccore.TabParamsMapper;
import com.dawn.plugin.mapper.ccore.TabServerMapper;
import com.dawn.plugin.mapper.ctemp.TempMapper;
import com.dawn.plugin.util.ConvertUtil;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;

/**
 * database服务
 * 创建时间 2026/8/26 20:38
 *
 * @author bhyt2
 */
@Slf4j
@ConditionalOnProperty(name = {"plugin-rest-controller.assistant-status"}, havingValue = "enable", matchIfMissing = true)
public class DatabaseRestService {

    private final PluginConfig config;
    private final ConvertUtil convertUtil;
    private final TempMapper tempMapper;
    private final TabServerMapper tabServerMapper;
    private final TabParamsMapper tabParamsMapper;
    private final ReflectionMapper reflectionMapper;

    public DatabaseRestService(PluginConfig config,
                                  ConvertUtil convertUtil,
                                  TempMapper tempMapper,
                                  TabServerMapper tabServerMapper,
                                  TabParamsMapper tabParamsMapper,
                                  ReflectionMapper reflectionMapper) {
        this.config = config;
        this.convertUtil = convertUtil;
        this.tempMapper = tempMapper;
        this.tabServerMapper = tabServerMapper;
        this.tabParamsMapper = tabParamsMapper;
        this.reflectionMapper = reflectionMapper;
    }

    /**
     * [数据调整]
     *
     * @param body 请求体内容
     * @return Object
     **/
    public Object editHandler(String body) {
        Map<String, Object> entityMap = config.getMapperLowerCamel().readValue(body, Map.class);
        var id = entityMap.getOrDefault(VarEnmu.ID.value(), VarEnmu.NONE.value());
        var temp = tempMapper.find(String.valueOf(id));
        Assert.notNull(temp, "temp is null!");
        if (!convertUtil.editEntity(entityMap, temp, "temp")) {
            tempMapper.edit(temp);
        }
        return new Response<>().success().data(temp);
    }

    public Response<Object> persistence() {
        var temps = tempMapper.findAll();
        var tabServers = tabServerMapper.findByApplicationSts(CodeEnmu.STS_A.code());
        return new Response<>()
            .success()
            .data(Map.of(
                "temps", temps,
                "tabServers", tabServers));
    }

    /**
     * [tabParams变更]
     *
     * @param body [body]
     * @return Response<Object>
     */
    public Response<Object> editTabParams(String body) {
        Map<String, Object> tabParamsMap = config.getMapperLowerCamel().readValue(body, Map.class);
        TabParams tabParams = config.getMapperLowerCamel().convertValue(tabParamsMap, TabParams.class);
        if (Objects.isNull(tabParamsMapper.find(tabParams.getId()))) {
            tabParamsMapper.create(tabParams);
        } else if (tabParamsMap.containsKey(VarEnmu.DELETE.value())) {
            tabParamsMapper.remove(tabParams.getId());
        } else if (tabParamsMap.size() == VarEnmu.ONE.ivalue()) {
            log.debug(LogEnmu.LOG1.value(), "查询");
        } else {
            tabParamsMapper.edit(tabParams);
        }
        tabParams = tabParamsMapper.find(tabParams.getId());
        return new Response<>().data(tabParams).success();
    }

    /**
     * [mapper变更]
     *
     * @param body [body]
     * @return Response<Object>
     */
    public Response<Object> mapper(String body) throws InvocationTargetException, IllegalAccessException {
        return new Response<>().data(reflectionMapper.invokeMethod(body)).success();
    }

}
