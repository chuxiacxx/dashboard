package com.fusemi.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuDTO {

    private Long id;

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    private String path;

    private String component;

    /** 0=目录, 1=菜单, 2=按钮 */
    private Integer type;

    private String icon;

    private Integer sort;

    /** 父菜单ID，0=顶级 */
    private Long parentId;

    private String permission;

    /** 0=正常, 1=隐藏 */
    private Integer visible;

    /** 0=正常, 1=禁用 */
    private Integer status;
}
