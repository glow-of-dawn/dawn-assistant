package com.dawn.plugin.entity.ccore;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * TabUser
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Data
@Accessors(chain = true)
public class TabUser implements Serializable {

    private static final long serialVersionUID = 3034834052869321693L;

    private String id;
    private String name;
    private String nickname;
    private String userState;

}
