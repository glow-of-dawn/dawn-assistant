package com.dawn.plugin.entity.ccore;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * TabOrggroup
 * 创建时间 2021/3/4 11:53
 *
 * @author hforest-480s
 */
@Data
@Accessors(chain = true)
public class TabOrggroup implements Serializable {

    private static final long serialVersionUID = -5557305175149431545L;

    private String id;
    private String parentid;
    private String orgtypeid;
    private String name;
    private String layer;
    private String layerNode;
    private String orgEnable;
    private String unitType;
    private String layerid;

}
