package com.dawn.plugin.entity.ccore;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * TabTask
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Data
@Accessors(chain = true)
public class TabTask implements Serializable {

    private static final long serialVersionUID = 1529368811217687574L;

    private String id;
    private String taskProject;
    private String taskId;
    private String taskServiceName;
    private String taskCron;
    private String taskInfo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime taskStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime taskOver;
    private String taskSts;
    private String taskServer;
    private Integer taskCount;

}
