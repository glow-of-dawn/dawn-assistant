package com.dawn.plugin.entity.ccore;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;

/**
 * TabRedis
 * 创建时间：2025-06-26 14:46:06
 *
 * @author hforest-480s
 */
@Data
@Accessors(chain = true)
public class TabRedis implements Serializable {

    @Serial
    private static final long serialVersionUID = -6866701307650014552L;

    private String id;
    private String redisProject;
    private String redisKey;
    private String redisKeyToken;
    private Integer redisExpire;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime redisTime;
    private String redisValue;

}
