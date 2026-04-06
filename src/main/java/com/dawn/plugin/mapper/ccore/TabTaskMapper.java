package com.dawn.plugin.mapper.ccore;

import com.dawn.plugin.entity.ccore.TabTask;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TabTask
 * 创建时间2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Mapper
@Component
@ConditionalOnProperty(name = {"plugin-status.mapper-status"}, havingValue = "enable", matchIfMissing = true)
public interface TabTaskMapper {

    /**
     * [Insert]
     *
     * @param tabTask []
     * @return int
     */
    @Insert("""
            INSERT INTO TAB_TASK(ID, TASK_PROJECT, TASK_ID, TASK_SERVICE_NAME, TASK_CRON, TASK_INFO, TASK_START, TASK_OVER, TASK_STS, TASK_SERVER, TASK_COUNT)
            VALUES(#{id}, #{taskProject}, #{taskId}, #{taskServiceName}, #{taskCron}, #{taskInfo}, #{taskStart}, #{taskOver}, #{taskSts}, #{taskServer}, #{taskCount})
            """)
    int create(TabTask tabTask);

    /**
     * [Update]
     *
     * @param tabTask [TabTask]
     * @return int
     */
    @Update("""
            UPDATE TAB_TASK SET TASK_PROJECT=#{taskProject}, TASK_ID=#{taskId}, TASK_SERVICE_NAME=#{taskServiceName}, TASK_CRON=#{taskCron}, TASK_INFO=#{taskInfo}, TASK_START=#{taskStart}, TASK_OVER=#{taskOver}, TASK_STS=#{taskSts}, TASK_SERVER=#{taskServer}, TASK_COUNT=#{taskCount}
            WHERE ID=#{id}
            """)
    int edit(TabTask tabTask);

    /**
     * [Delete]
     *
     * @param id [主键]
     * @return int
     */
    @Delete("""
            DELETE FROM TAB_TASK
            WHERE ID=#{id}
            """)
    int remove(@Param("id") Object id);

    /* -+-- select --+- */

    /**
     * [find by 主键]
     *
     * @param id [id]
     * @return TabTask
     */
    @Select("""
            SELECT ID, TASK_PROJECT, TASK_ID, TASK_SERVICE_NAME, TASK_CRON, TASK_INFO, TASK_START, TASK_OVER, TASK_STS, TASK_SERVER, TASK_COUNT
            FROM TAB_TASK
            WHERE ID=#{id}
            """)
    TabTask find(@Param("id") Object id);

    /**
     * [Select]
     *
     * @return List<TabTask>
     */
    @Select("""
            SELECT ID, TASK_PROJECT, TASK_ID, TASK_SERVICE_NAME, TASK_CRON, TASK_INFO, TASK_START, TASK_OVER, TASK_STS, TASK_SERVER, TASK_COUNT
            FROM TAB_TASK
            """)
    List<TabTask> findAll();

    /* -+-- others --+- */

    /**
     * [find by 主键]
     *
     * @param taskProject [taskProject]
     * @param taskSts     [taskSts]
     * @param taskTime    [taskTime]
     * @return TabTask
     */
    @Select("""
            SELECT ID, TASK_PROJECT, TASK_ID, TASK_SERVICE_NAME, TASK_CRON, TASK_INFO, TASK_START, TASK_OVER, TASK_STS, TASK_SERVER, TASK_COUNT
            FROM TAB_TASK
            WHERE TASK_PROJECT=#{taskProject} AND TASK_STS=#{taskSts} AND #{taskTime} BETWEEN TASK_START AND TASK_OVER
            """)
    List<TabTask> findByProjectAndSts(@Param("taskProject") String taskProject,
                                      @Param("taskSts") String taskSts,
                                      @Param("taskTime") LocalDateTime taskTime);

}
