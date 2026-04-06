package com.dawn.plugin.mapper.ccore;

import com.vivi.plugin.entity.ccore.TabRunLog;
import org.apache.ibatis.annotations.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TabRunLog
 * 创建时间2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Mapper
@Component
@ConditionalOnProperty(name = {"plugin-status.mapper-status"}, havingValue = "enable", matchIfMissing = true)
public interface TabRunLogMapper {

    /**
     * [Insert]
     *
     * @param tabRunLog []
     * @return int
     */
    @Insert("""
            INSERT INTO TAB_RUN_LOG(ID, TASK_PROJECT, TASK_TYPE, TASK_CLASS, TASK_BATCH_SERIAL, TASK_START_TIME, TASK_OVER_TIME, TASK_RESULT, TASK_EXCEPTION)
            VALUES(#{id}, #{taskProject}, #{taskType}, #{taskClass}, #{taskBatchSerial}, #{taskStartTime}, #{taskOverTime}, #{taskResult}, #{taskException})
            """)
    int create(TabRunLog tabRunLog);

    /**
     * [Update]
     *
     * @param tabRunLog [TabRunLog]
     * @return int
     */
    @Update("""
            UPDATE TAB_RUN_LOG SET TASK_PROJECT=#{taskProject}, TASK_TYPE=#{taskType}, TASK_CLASS=#{taskClass}, TASK_BATCH_SERIAL=#{taskBatchSerial}, TASK_START_TIME=#{taskStartTime}, TASK_OVER_TIME=#{taskOverTime}, TASK_RESULT=#{taskResult}, TASK_EXCEPTION=#{taskException}
            WHERE ID=#{id}
            """)
    int edit(TabRunLog tabRunLog);

    /**
     * [Delete]
     *
     * @param id [主键]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_RUN_LOG
            WHERE ID=#{id}
            """)
    int remove(@Param("id") Object id);

    /* -+-- select --+- */

    /**
     * [find by 主键]
     *
     * @param id [id]
     * @return TabRunLog
     */
    @Select("""
            SELECT ID, TASK_PROJECT, TASK_TYPE, TASK_CLASS, TASK_BATCH_SERIAL, TASK_START_TIME, TASK_OVER_TIME, TASK_RESULT, TASK_EXCEPTION
            FROM TAB_RUN_LOG
            WHERE ID=#{id}
            """)
    TabRunLog find(@Param("id") Object id);

    /**
     * [Select]
     *
     * @return List<TabRunLog>
     */
    @Select("""
            SELECT ID, TASK_PROJECT, TASK_TYPE, TASK_CLASS, TASK_BATCH_SERIAL, TASK_START_TIME, TASK_OVER_TIME, TASK_RESULT, TASK_EXCEPTION
            FROM TAB_RUN_LOG
            """)
    List<TabRunLog> findAll();

    /* -+-- others --+- */

    /**
     * [findByTaskProject]
     *
     * @param taskProject [taskProject]
     * @return TabRunLog
     */
    @Select("""
            SELECT ID, TASK_PROJECT, TASK_TYPE, TASK_CLASS, TASK_BATCH_SERIAL, TASK_START_TIME, TASK_OVER_TIME, TASK_RESULT, TASK_EXCEPTION
            FROM TAB_RUN_LOG
            WHERE TASK_PROJECT=#{taskProject}
            ORDER BY TASK_START_TIME DESC
            """)
    List<TabRunLog> findByTaskProject(@Param("taskProject") String taskProject);

    /**
     * [findByTaskProject]
     *
     * @param taskProject [taskProject]
     * @param taskClass   [taskClass]
     * @return TabRunLog
     */
    @Select("""
            SELECT ID, TASK_PROJECT, TASK_TYPE, TASK_CLASS, TASK_BATCH_SERIAL, TASK_START_TIME, TASK_OVER_TIME, TASK_RESULT, TASK_EXCEPTION
            FROM TAB_RUN_LOG
            WHERE TASK_PROJECT=#{taskProject} AND TASK_CLASS=#{taskClass}
            ORDER BY TASK_START_TIME DESC
            """)
    List<TabRunLog> findByTaskProjectAndTaskClass(@Param("taskProject") String taskProject, @Param("taskClass") String taskClass);


    /**
     * [removeByInvalid]
     *
     * @param taskProject [taskProject]
     * @param expire      [expire]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_RUN_LOG trl
            WHERE trl.TASK_PROJECT=#{taskProject} AND trl.TASK_START_TIME < DATE_SUB(NOW(), INTERVAL #{expire} DAY)
            LIMIT 100
            """)
    int removeByInvalid(@Param("taskProject") String taskProject, @Param("expire") int expire);

}
