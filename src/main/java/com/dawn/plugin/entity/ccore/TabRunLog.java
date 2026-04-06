package com.dawn.plugin.entity.ccore;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * TabRunLog
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Data
@Accessors(chain = true)
public class TabRunLog implements Serializable {

    private static final long serialVersionUID = -1806422942835198692L;

    private String id;
    private String taskProject;
    private String taskType;
    private String taskClass;
    private String taskBatchSerial;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime taskStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime taskOverTime;
    private String taskResult;
    private String taskException;

}
