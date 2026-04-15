package com.fusemi.dashboard.vo;

import lombok.Data;

@Data
public class RoleItemVO {
    private Long roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private Boolean enabled;
    private String createTime;
}
