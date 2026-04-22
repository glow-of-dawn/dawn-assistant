//package com.dawn.plugin.controller.authtoken;
//
//import com.vivi.plugin.authtoken.Authtoken;
//import com.vivi.plugin.entity.ccore.TabOrggroup;
//import com.vivi.plugin.mapper.ccore.TabOrggroupMapper;
//import com.vivi.plugin.util.Response;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
///**
// * [机构列表]
// * 创建时间：2021/2/3 21:59
// *
// * @author forest
// */
//@Slf4j
//@RestController
//@RequestMapping(value = "/rest/authtoken/org/")
//@ConditionalOnProperty(name = {"plugin-rest-controller.org-status"}, havingValue = "enable", matchIfMissing = true)
//public class OrgRestController {
//
//    private final TabOrggroupMapper tabOrggroupMapper;
//
//    public OrgRestController(TabOrggroupMapper tabOrggroupMapper) {
//        this.tabOrggroupMapper = tabOrggroupMapper;
//    }
//
//    @Authtoken(openAuthtoken = true)
//    @GetMapping("/orggroup/orgtypeid/{orgtypeid}")
//    public Response<Object> get(@PathVariable("orgtypeid") String orgtypeid) {
//        List<TabOrggroup> tabOrggroups = tabOrggroupMapper.findByOrgtypeid(orgtypeid);
//        return new Response<>().data(tabOrggroups).success();
//    }
//
//}
