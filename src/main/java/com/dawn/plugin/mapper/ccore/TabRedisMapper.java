package com.dawn.plugin.mapper.ccore;

import com.dawn.plugin.entity.ccore.TabRedis;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TabRedis
 * 创建时间：2025-06-26 14:46:06
 *
 * @author hforest-480s
 */
@Mapper
@Component
public interface TabRedisMapper {

    /**
     * [Insert]
     *
     * @param tabRedis []
     * @return int
     */
    @Insert("""
            INSERT INTO TAB_REDIS(ID, REDIS_PROJECT, REDIS_KEY, REDIS_KEY_TOKEN, REDIS_EXPIRE, REDIS_TIME, REDIS_VALUE)
            VALUES(#{id}, #{redisProject}, #{redisKey}, #{redisKeyToken}, #{redisExpire}, #{redisTime}, #{redisValue})
            """)
    int create(TabRedis tabRedis);

    /**
     * [Update]
     *
     * @param tabRedis [TabRedis]
     * @return int
     */
    @Update("""
            UPDATE TAB_REDIS SET REDIS_PROJECT=#{redisProject}, REDIS_KEY=#{redisKey}, REDIS_KEY_TOKEN=#{redisKeyToken}, REDIS_EXPIRE=#{redisExpire}, REDIS_TIME=#{redisTime}, REDIS_VALUE=#{redisValue}
            WHERE ID=#{id}
            """)
    int edit(TabRedis tabRedis);

    /**
     * [Delete]
     *
     * @param id [主键]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_REDIS
            WHERE ID=#{id}
            """)
    int remove(@Param("id") Object id);

    /* -+-- select --+- */

    /**
     * [find by 主键]
     *
     * @param id [id]
     * @return TabRedis
     */
    @Select("""
            SELECT ID, REDIS_PROJECT, REDIS_KEY, REDIS_KEY_TOKEN, REDIS_EXPIRE, REDIS_TIME, REDIS_VALUE
            FROM TAB_REDIS
            WHERE ID=#{id}
            """)
    TabRedis find(@Param("id") Object id);

    /**
     * [Select]
     *
     * @return List<TabRedis>
     */
    @Select("""
            SELECT ID, REDIS_PROJECT, REDIS_KEY, REDIS_KEY_TOKEN, REDIS_EXPIRE, REDIS_TIME, REDIS_VALUE
            FROM TAB_REDIS
            """)
    List<TabRedis> findAll();

    /* -+-- others --+- */

    /**
     * [findByProjectAndKey]
     *
     * @param redisProject [redisProject]
     * @param redisKey     [redisKey]
     * @return TabRedis
     */
    @Select("""
            SELECT ID, REDIS_PROJECT, REDIS_KEY, REDIS_KEY_TOKEN, REDIS_EXPIRE, REDIS_TIME, REDIS_VALUE
            FROM TAB_REDIS
            WHERE REDIS_PROJECT=#{redisProject}  AND REDIS_KEY=#{redisKey}
            """)
    TabRedis findByProjectAndKey(@Param("redisProject") Object redisProject, @Param("redisKey") Object redisKey);

    /**
     * [removeByInvalid]
     *
     * @param redisProject  [redisProject]
     * @param redisKey      [redisKey]
     * @param redisKeyToken [redisKeyToken]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_REDIS
            WHERE REDIS_PROJECT=#{redisProject}  AND REDIS_KEY=#{redisKey} AND REDIS_KEY_TOKEN=#{redisKeyToken}
            """)
    int removeByProjectAndIndentifierAndKey(@Param("redisProject") Object redisProject,
                                            @Param("redisKey") Object redisKey,
                                            @Param("redisKeyToken") Object redisKeyToken);

    /**
     * [removeByInvalid]
     *
     * @param redisProject [redisProject]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_REDIS tr
            WHERE REDIS_PROJECT=#{redisProject} and CURRENT_TIMESTAMP() - tr.redis_time > tr.redis_expire
            LIMIT 100
            """)
    int removeByInvalid(@Param("redisProject") Object redisProject);

}
