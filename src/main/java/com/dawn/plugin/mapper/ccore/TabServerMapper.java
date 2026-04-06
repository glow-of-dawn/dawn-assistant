package com.dawn.plugin.mapper.ccore;

import com.vivi.plugin.entity.ccore.TabServer;
import org.apache.ibatis.annotations.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TabServer
 * 创建时间2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Mapper
@Component
@ConditionalOnProperty(name = {"plugin-status.mapper-status"}, havingValue = "enable", matchIfMissing = true)
public interface TabServerMapper {

    /**
     * [Insert]
     *
     * @param tabServer []
     * @return int
     */
    @Insert("""
            INSERT INTO TAB_SERVER(ID, APPLICATION_NAME, ADDR_LOCAL, ADDR_HOST, APPLICATION_STS, ADDR_SITE, CREATE_TIME, LAST_TIME, READ_CNT)
            VALUES(#{id}, #{applicationName}, #{addrLocal}, #{addrHost}, #{applicationSts}, #{addrSite}, #{createTime}, #{lastTime}, #{readCnt})
            """)
    int create(TabServer tabServer);

    /**
     * [Update]
     *
     * @param tabServer [TabServer]
     * @return int
     */
    @Update("""
            UPDATE TAB_SERVER SET APPLICATION_NAME=#{applicationName}, ADDR_LOCAL=#{addrLocal}, ADDR_HOST=#{addrHost}, APPLICATION_STS=#{applicationSts}, ADDR_SITE=#{addrSite}, CREATE_TIME=#{createTime}, LAST_TIME=#{lastTime}, READ_CNT=#{readCnt}
            WHERE ID=#{id}
            """)
    int edit(TabServer tabServer);

    /**
     * [Delete]
     *
     * @param id [主键]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_SERVER
            WHERE ID=#{id}
            """)
    int remove(@Param("id") Object id);

    /* -+-- select --+- */

    /**
     * [find by 主键]
     *
     * @param id [id]
     * @return TabServer
     */
    @Select("""
            SELECT ID, APPLICATION_NAME, ADDR_LOCAL, ADDR_HOST, APPLICATION_STS, ADDR_SITE, CREATE_TIME, LAST_TIME, READ_CNT
            FROM TAB_SERVER
            WHERE ID=#{id}
            """)
    TabServer find(@Param("id") Object id);

    /**
     * [Select]
     *
     * @return List<TabServer>
     */
    @Select("""
            SELECT ID, APPLICATION_NAME, ADDR_LOCAL, ADDR_HOST, APPLICATION_STS, ADDR_SITE, CREATE_TIME, LAST_TIME, READ_CNT
            FROM TAB_SERVER
            """)
    List<TabServer> findAll();

    /* -+-- others --+- */

    /**
     * [findByApplicationStsAndActionTime]
     *
     * @param applicationSts [applicationSts]
     * @param actionTime     [actionTime]
     * @return List<TabServer>
     */
    @Select("""
            SELECT ID, APPLICATION_NAME, ADDR_LOCAL, ADDR_HOST, APPLICATION_STS, ADDR_SITE, CREATE_TIME, LAST_TIME, READ_CNT
            FROM TAB_SERVER
            WHERE APPLICATION_STS=#{applicationSts} AND LAST_TIME < #{actionTime}
            """)
    List<TabServer> findByApplicationStsAndActionTime(@Param("applicationSts") Object applicationSts, @Param("actionTime") Object actionTime);

    /**
     * [findByApplicationSts]
     *
     * @param applicationSts [applicationSts]
     * @return List<TabServer>
     */
    @Select("""
            SELECT ID, APPLICATION_NAME, ADDR_LOCAL, ADDR_HOST, APPLICATION_STS, ADDR_SITE, CREATE_TIME, LAST_TIME, READ_CNT
            FROM TAB_SERVER
            WHERE APPLICATION_STS=#{applicationSts}
            """)
    List<TabServer> findByApplicationSts(@Param("applicationSts") Object applicationSts);

    /**
     * [findByApplicationNameAndApplicationSts]
     *
     * @param applicationName [applicationName]
     * @param applicationSts  [applicationSts]
     * @return List<TabServer>
     */
    @Select("""
            SELECT ID, APPLICATION_NAME, ADDR_LOCAL, ADDR_HOST, APPLICATION_STS, ADDR_SITE, CREATE_TIME, LAST_TIME, READ_CNT
            FROM TAB_SERVER
            WHERE APPLICATION_NAME=#{applicationName} AND APPLICATION_STS=#{applicationSts}
            """)
    List<TabServer> findByApplicationNameAndApplicationSts(@Param("applicationName") Object applicationName,
                                                           @Param("applicationSts") Object applicationSts);

}
