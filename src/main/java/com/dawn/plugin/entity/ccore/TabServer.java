package com.dawn.plugin.entity.ccore;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * TabServer
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Data
@Accessors(chain = true)
public class TabServer implements Serializable {

    private static final long serialVersionUID = 7715606132365535310L;

    private String id;
    private String applicationName;
    private String addrLocal;
    private String addrHost;
    private String applicationSts;
    private String addrSite;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime lastTime;
    private Integer readCnt;

}
