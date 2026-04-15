package com.fusemi.dashboard.vo;

import lombok.Data;
import java.util.List;

@Data
public class MenuTreeVO {
    private Long id;
    private String path;
    private String name;
    private String component;
    private Integer type;
    private String icon;
    private Integer sort;
    private Long parentId;
    private String permission;
    private Integer visible;
    private Integer status;
    private List<MenuTreeVO> children;
}
