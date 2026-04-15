package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_menu")
public class Menu extends BaseEntity {

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
