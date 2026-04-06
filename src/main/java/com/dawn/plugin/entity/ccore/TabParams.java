package com.dawn.plugin.entity.ccore;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * TabParams
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Data
@Accessors(chain = true)
public class TabParams implements Serializable {

    private static final long serialVersionUID = 8680934317627975130L;

    private String id;
    private String paramsName;
    private String paramsValue;
    private String paramsClass;
    private String paramsAbs;
    private String paramsKey;

}
