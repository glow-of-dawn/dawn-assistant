package com.dawn.plugin.mapper.ccore;

import com.dawn.plugin.entity.ccore.TabParams;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TabParams
 * 创建时间：2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Mapper
@Component
@ConditionalOnProperty(name = {"plugin-status.mapper-status"}, havingValue = "enable", matchIfMissing = true)
public interface TabParamsMapper {

    /**
     * [Insert]
     *
     * @param tabParams []
     * @return int
     */
    @Insert("""
            INSERT INTO TAB_PARAMS(ID, PARAMS_NAME, PARAMS_VALUE, PARAMS_CLASS, PARAMS_ABS, PARAMS_KEY)
            VALUES(#{id}, #{paramsName}, #{paramsValue}, #{paramsClass}, #{paramsAbs}, #{paramsKey})
            """)
    int create(TabParams tabParams);

    /**
     * [Update]
     *
     * @param tabParams [TabParams]
     * @return int
     */
    @Update("""
            UPDATE TAB_PARAMS SET PARAMS_NAME=#{paramsName}, PARAMS_VALUE=#{paramsValue}, PARAMS_CLASS=#{paramsClass}, PARAMS_ABS=#{paramsAbs}, PARAMS_KEY=#{paramsKey}
            WHERE ID=#{id}
            """)
    int edit(TabParams tabParams);

    /**
     * [Delete]
     *
     * @param id [主键]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_PARAMS
            WHERE ID=#{id}
            """)
    int remove(@Param("id") Object id);

    /* --->>> select <<<--- */

    /**
     * [find by 主键]
     *
     * @param id [id]
     * @return TabParams
     */
    @Select("""
            SELECT ID, PARAMS_NAME, PARAMS_VALUE, PARAMS_CLASS, PARAMS_ABS, PARAMS_KEY
            FROM TAB_PARAMS
            WHERE ID=#{id}
            """)
    TabParams find(@Param("id") Object id);

    /**
     * [Select]
     *
     * @return List<TabParams>
     */
    @Select("""
            SELECT ID, PARAMS_NAME, PARAMS_VALUE, PARAMS_CLASS, PARAMS_ABS, PARAMS_KEY
            FROM TAB_PARAMS
            """)
    List<TabParams> findAll();

    /* -+-- others --+- */

    /**
     * [findByAny]
     *
     * @param paramsClass [paramsClass]
     * @param paramsName  [paramsName]
     * @param paramsKey   [paramsKey]
     * @return TabParams
     */
    @Select("""
            SELECT ID, PARAMS_NAME, PARAMS_VALUE, PARAMS_CLASS, PARAMS_ABS, PARAMS_KEY
            FROM TAB_PARAMS
            WHERE PARAMS_NAME=#{paramsName} AND PARAMS_CLASS=#{paramsClass} AND PARAMS_KEY=#{paramsKey}
            """)
    TabParams findByAny(@Param("paramsClass") String paramsClass, @Param("paramsName") String paramsName, @Param("paramsKey") String paramsKey);

    /**
     * [findByClassAndName]
     *
     * @param paramsClass [paramsClass]
     * @param paramsName  [paramsName]
     * @return List<TabParams>
     */
    @Select("""
            SELECT ID, PARAMS_NAME, PARAMS_VALUE, PARAMS_CLASS, PARAMS_ABS, PARAMS_KEY
            FROM TAB_PARAMS
            WHERE PARAMS_NAME=#{paramsName} AND PARAMS_CLASS=#{paramsClass}
            """)
    List<TabParams> findByClassAndName(@Param("paramsClass") String paramsClass,
                                       @Param("paramsName") String paramsName);

    /**
     * [findByClassAndName]
     *
     * @param paramsClass [paramsClass]
     * @param paramsName  [paramsName]
     * @param paramsKey   [paramsKey]
     * @return List<TabParams>
     */
    @Select("""
            SELECT ID, PARAMS_NAME, PARAMS_VALUE, PARAMS_CLASS, PARAMS_ABS, PARAMS_KEY
            FROM TAB_PARAMS
            WHERE PARAMS_NAME=#{paramsName} AND PARAMS_CLASS=#{paramsClass} AND PARAMS_KEY=#{paramsKey}
            """)
    TabParams findByClassAndNameAndKey(@Param("paramsClass") String paramsClass,
                                       @Param("paramsName") String paramsName,
                                       @Param("paramsKey") String paramsKey);

    /**
     * [findByClass]
     *
     * @param paramsClass [paramsClass]
     * @return List<TabParams>
     */
    @Select("""
            SELECT ID, PARAMS_NAME, PARAMS_VALUE, PARAMS_CLASS, PARAMS_ABS, PARAMS_KEY
            FROM TAB_PARAMS
            WHERE PARAMS_CLASS=#{paramsClass}
            """)
    List<TabParams> findByClass(@Param("paramsClass") String paramsClass);
}
