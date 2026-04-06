package com.dawn.plugin.entity.ctemp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;

/**
 * Temp
 * 创建时间 2025-12-08 11:50:35
 *
 * @author bhyt2
 */
@Data
@Accessors(chain = true)
public class Temp implements Serializable {

    @Serial
    private static final long serialVersionUID = 3971374309019302640L;

    private String id;
    private String c1;
    private String c2;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime c3;
    private java.math.BigDecimal c4;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate c5;
    private Integer c6;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime c7;

}
