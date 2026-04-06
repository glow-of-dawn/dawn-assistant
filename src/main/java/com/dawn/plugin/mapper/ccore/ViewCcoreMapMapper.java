package com.dawn.plugin.mapper.ccore;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * 创建时间：2022/5/26 16:30
 *
 * @author hforest-480s
 */
@Mapper
@Component
@ConditionalOnProperty(name = {"plugin-status.mapper-status"}, havingValue = "enable", matchIfMissing = true)
public interface ViewCcoreMapMapper {

    /**
     * [findByViews]
     *
     * @param view [view]
     * @param args [args]
     * @return List<T>
     */
    @SelectProvider(type = ViewSql.class, method = "viewAction")
    List<HashMap<String, Object>> findByViews(String view, Object... args);

    /**
     * [findByViews]
     *
     * @param view [view]
     * @param args [args]
     * @return T
     */
    @SelectProvider(type = ViewSql.class, method = "viewAction")
    HashMap<String, Object> findByView(String view, Object... args);

}
