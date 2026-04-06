package com.dawn.plugin.entity.ccore;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * ViewOrguser
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Data
@Accessors(chain = true)
public class ViewOrguser implements Serializable {

    private static final long serialVersionUID = 3622111339094628595L;

    private String porgid;
    private String porgname;
    private String orgid;
    private String orgname;
    private String orgtypeid;
    private String userid;
    private String username;
    private String nickname;
    private String userState;
    private String layer;

}
