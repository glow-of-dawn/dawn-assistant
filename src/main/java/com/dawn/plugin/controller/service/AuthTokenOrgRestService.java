package com.dawn.plugin.controller.service;

import com.dawn.plugin.entity.ccore.TabOrggroup;
import com.dawn.plugin.mapper.ccore.TabOrggroupMapper;
import com.dawn.plugin.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

/**
 *
 * 创建时间 2026/8/26 21:48
 *
 * @author bhyt2
 */
@Slf4j
@ConditionalOnProperty(name = {"plugin-rest-controller.org-status"}, havingValue = "enable", matchIfMissing = true)
public class AuthTokenOrgRestService {

    private final TabOrggroupMapper tabOrggroupMapper;

    public AuthTokenOrgRestService(TabOrggroupMapper tabOrggroupMapper) {
        this.tabOrggroupMapper = tabOrggroupMapper;
    }

    public Response<Object> orggroupAndOrgtypeid(String orgtypeid) {
        List<TabOrggroup> tabOrggroups = tabOrggroupMapper.findByOrgtypeid(orgtypeid);
        return new Response<>().data(tabOrggroups).success();
    }

}
