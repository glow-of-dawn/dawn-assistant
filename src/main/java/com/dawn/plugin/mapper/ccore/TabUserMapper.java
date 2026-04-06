package com.dawn.plugin.mapper.ccore;

import com.vivi.plugin.entity.ccore.TabUser;
import org.apache.ibatis.annotations.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TabUser
 * 创建时间2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Mapper
@Component
@ConditionalOnProperty(name = {"plugin-status.auth-status"}, havingValue = "enable", matchIfMissing = true)
public interface TabUserMapper {

    /**
     * [Insert]
     *
     * @param tabUser []
     * @return int
     */
    @Insert("""
            INSERT INTO TAB_USER(ID, NAME, NICKNAME, USER_STATE)
            VALUES(#{id}, #{name}, #{nickname}, #{userState})
            """)
    int create(TabUser tabUser);

    /**
     * [Update]
     *
     * @param tabUser [TabUser]
     * @return int
     */
    @Update("""
            UPDATE TAB_USER SET NAME=#{name}, NICKNAME=#{nickname}, USER_STATE=#{userState}
            WHERE ID=#{id}
            """)
    int edit(TabUser tabUser);

    /**
     * [Delete]
     *
     * @param id [主键]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_USER
            WHERE ID=#{id}
            """)
    int remove(@Param("id") Object id);

    /* -+-- select --+- */

    /**
     * [find by 主键]
     *
     * @param id [id]
     * @return TabUser
     */
    @Select("""
            SELECT ID, NAME, NICKNAME, USER_STATE
            FROM TAB_USER
            WHERE ID=#{id}
            """)
    TabUser find(@Param("id") Object id);

    /**
     * [Select]
     *
     * @return List<TabUser>
     */
    @Select("""
            SELECT ID, NAME, NICKNAME, USER_STATE
            FROM TAB_USER
            """)
    List<TabUser> findAll();

    /* -+-- others --+- */

}
