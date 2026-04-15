package com.fusemi.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleDTO {

    private Long roleId;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private String description;

    /** 0=正常, 1=禁用 */
    private Boolean enabled;

    /** 菜单ID列表，逗号分隔 */
    private String menuIds;
}
