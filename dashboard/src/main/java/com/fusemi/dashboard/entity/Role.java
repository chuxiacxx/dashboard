package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_role")
public class Role extends BaseEntity {

    private String name;

    private String code;

    private String description;

    /** 0=正常, 1=禁用 */
    private Integer status;

    /** 菜单ID列表，逗号分隔，如: 1,2,3 */
    @Column(name = "menu_ids")
    private String menuIds;
}
