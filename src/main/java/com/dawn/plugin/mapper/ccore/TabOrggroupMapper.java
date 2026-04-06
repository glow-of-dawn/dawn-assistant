package com.dawn.plugin.mapper.ccore;

import com.vivi.plugin.entity.ccore.TabOrggroup;
import org.apache.ibatis.annotations.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TabOrggroup
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Mapper
@Component
@ConditionalOnProperty(name = {"plugin-status.auth-status"}, havingValue = "enable", matchIfMissing = true)
public interface TabOrggroupMapper {

    /**
     * [Insert]
     *
     * @param tabOrggroup []
     * @return int
     */
    @Insert("""
            INSERT INTO TAB_ORGGROUP(ID, PARENTID, ORGTYPEID, NAME, LAYER, LAYER_NODE, ORG_ENABLE, UNIT_TYPE, LAYERID)
            VALUES(#{id}, #{parentid}, #{orgtypeid}, #{name}, #{layer}, #{layerNode}, #{orgEnable}, #{unitType}, #{layerid})
            """)
    int create(TabOrggroup tabOrggroup);

    /**
     * [Update]
     *
     * @param tabOrggroup [TabOrggroup]
     * @return int
     */
    @Update("""
            UPDATE TAB_ORGGROUP SET PARENTID=#{parentid}, ORGTYPEID=#{orgtypeid}, NAME=#{name}, LAYER=#{layer}, LAYER_NODE=#{layerNode}, ORG_ENABLE=#{orgEnable}, UNIT_TYPE=#{unitType}, LAYERID=#{layerid}
            WHERE ID=#{id}
            """)
    int edit(TabOrggroup tabOrggroup);

    /**
     * [Delete]
     *
     * @param id [主键]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_ORGGROUP
            WHERE ID=#{id}
            """)
    int remove(@Param("id") Object id);

    /* -+-- select --+- */

    /**
     * [find by 主键]
     *
     * @param id [id]
     * @return TabOrggroup
     */
    @Select("""
            SELECT ID, PARENTID, ORGTYPEID, NAME, LAYER, LAYER_NODE, ORG_ENABLE, UNIT_TYPE, LAYERID
            FROM TAB_ORGGROUP
            WHERE ID=#{id}
            """)
    TabOrggroup find(@Param("id") Object id);

    /**
     * [Select]
     *
     * @return List<TabOrggroup>
     */
    @Select("""
            SELECT ID, PARENTID, ORGTYPEID, NAME, LAYER, LAYER_NODE, ORG_ENABLE, UNIT_TYPE, LAYERID
            FROM TAB_ORGGROUP
            """)
    List<TabOrggroup> findAll();

    /* -+-- others --+- */

    /**
     * [Select]
     *
     * @param orgtypeid [orgtypeid]
     * @return List<TabOrggroup>
     */
    @Select("""
            SELECT ID, PARENTID, ORGTYPEID, NAME, LAYER, LAYER_NODE, ORG_ENABLE, UNIT_TYPE, LAYERID
            FROM TAB_ORGGROUP
            WHERE ORGTYPEID=#{orgtypeid}
            """)
    List<TabOrggroup> findByOrgtypeid(@Param("orgtypeid") String orgtypeid);

}
