package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.common.PageResult;
import com.fusemi.dashboard.dto.RoleDTO;
import com.fusemi.dashboard.service.RoleService;
import com.fusemi.dashboard.vo.RoleItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list")
    public Result<PageResult<RoleItemVO>> getRoleList(
            @RequestParam Integer current,
            @RequestParam Integer size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String enabled) {
        return Result.ok(roleService.getRolePage(current, size, roleName, enabled));
    }

    @PostMapping
    public Result<?> addRole(@Valid @RequestBody RoleDTO dto) {
        return roleService.addRole(dto);
    }

    @PutMapping("/{id}")
    public Result<?> updateRole(@PathVariable Long id, @RequestBody RoleDTO dto) {
        dto.setRoleId(id);
        return roleService.updateRole(dto);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteRole(@PathVariable Long id) {
        return roleService.deleteRole(id);
    }
}
