package com.fusemi.dashboard.service;

import com.fusemi.dashboard.dto.RoleDTO;
import com.fusemi.dashboard.entity.Role;
import com.fusemi.dashboard.repository.RoleRepository;
import com.fusemi.dashboard.vo.RoleItemVO;
import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public PageResult<RoleItemVO> getRolePage(Integer current, Integer size,
                                              String roleName, String enabled) {
        PageRequest pageRequest = PageRequest.of(
                current - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<Role> page = roleRepository.findAll(pageRequest);

        List<RoleItemVO> records = page.getContent().stream()
                .filter(r -> {
                    if (StringUtils.hasText(roleName) && !r.getName().contains(roleName)) return false;
                    if (StringUtils.hasText(enabled)) {
                        boolean isEnabled = "true".equalsIgnoreCase(enabled) || "1".equals(enabled);
                        if (r.getStatus() == 1 != isEnabled) return false;
                    }
                    return true;
                })
                .map(this::toItemVO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), records, (long) current, (long) size);
    }

    @Transactional
    public Result<?> addRole(RoleDTO dto) {
        if (roleRepository.existsByCode(dto.getRoleCode())) {
            return Result.fail("角色编码已存在");
        }

        Role role = new Role();
        role.setName(dto.getRoleName());
        role.setCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setStatus(Boolean.TRUE.equals(dto.getEnabled()) ? 0 : 1);
        role.setMenuIds(dto.getMenuIds());
        roleRepository.save(role);
        return Result.ok();
    }

    @Transactional
    public Result<?> updateRole(RoleDTO dto) {
        Role role = roleRepository.findById(dto.getRoleId()).orElse(null);
        if (role == null) return Result.fail("角色不存在");

        role.setName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setStatus(Boolean.TRUE.equals(dto.getEnabled()) ? 0 : 1);
        role.setMenuIds(dto.getMenuIds());
        roleRepository.save(role);
        return Result.ok();
    }

    @Transactional
    public Result<?> deleteRole(Long roleId) {
        if (!roleRepository.existsById(roleId)) return Result.fail("角色不存在");
        roleRepository.deleteById(roleId);
        return Result.ok();
    }

    private RoleItemVO toItemVO(Role role) {
        RoleItemVO vo = new RoleItemVO();
        vo.setRoleId(role.getId());
        vo.setRoleName(role.getName());
        vo.setRoleCode(role.getCode());
        vo.setDescription(role.getDescription());
        vo.setEnabled(role.getStatus() != 1);
        vo.setCreateTime(role.getCreateTime() != null ? role.getCreateTime().toString() : null);
        return vo;
    }
}
