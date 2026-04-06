package com.dawn.plugin.mapper.ccore;

import com.dawn.plugin.enmu.LogEnmu;
import lombok.extern.slf4j.Slf4j;

/**
 * 创建时间2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Slf4j
public class ViewSql {

    public String viewAction(String view, Object... args) {
        String sql = String.format(view, args);
        log.debug(LogEnmu.LOG2.value(), "viewSql", sql);
        return sql;
    }

}
