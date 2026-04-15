package com.fusemi.dashboard.service;

import com.fusemi.dashboard.dto.UserDTO;
import com.fusemi.dashboard.entity.Role;
import com.fusemi.dashboard.entity.User;
import com.fusemi.dashboard.repository.RoleRepository;
import com.fusemi.dashboard.repository.UserRepository;
import com.fusemi.dashboard.vo.UserItemVO;
import com.fusemi.dashboard.vo.UserPageVO;
import com.fusemi.dashboard.common.PageResult;
import com.fusemi.dashboard.common.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<UserItemVO> getUserPage(Integer current, Integer size,
                                              String userName, String status) {
        PageRequest pageRequest = PageRequest.of(
                current - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<User> page = userRepository.findAll(pageRequest);

        List<UserItemVO> records = page.getContent().stream()
                .filter(u -> {
                    if (StringUtils.hasText(userName) && !u.getUsername().contains(userName)) return false;
                    if (StringUtils.hasText(status) && !String.valueOf(u.getStatus()).equals(status)) return false;
                    return true;
                })
                .map(this::toItemVO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), records, (long) current, (long) size);
    }

    @Transactional
    public Result<?> addUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUserName())) {
            return Result.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUserName());
        user.setNickname(dto.getNickName());
        user.setEmail(dto.getUserEmail());
        user.setPhone(dto.getUserPhone());
        user.setAvatar(dto.getAvatar());
        user.setStatus("1".equals(dto.getStatus()) ? 1 : 0);

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            user.setRoles(roles);
        }

        userRepository.save(user);
        return Result.ok();
    }

    @Transactional
    public Result<?> updateUser(UserDTO dto) {
        User user = userRepository.findById(dto.getId()).orElse(null);
        if (user == null) return Result.fail("用户不存在");

        user.setNickname(dto.getNickName());
        user.setEmail(dto.getUserEmail());
        user.setPhone(dto.getUserPhone());
        user.setAvatar(dto.getAvatar());
        user.setStatus("1".equals(dto.getStatus()) ? 1 : 0);

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            user.setRoles(roles);
        }

        userRepository.save(user);
        return Result.ok();
    }

    @Transactional
    public Result<?> deleteUser(Long id) {
        if (!userRepository.existsById(id)) return Result.fail("用户不存在");
        userRepository.deleteById(id);
        return Result.ok();
    }

    private UserItemVO toItemVO(User user) {
        UserItemVO vo = new UserItemVO();
        vo.setId(user.getId());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(String.valueOf(user.getStatus()));
        vo.setUserName(user.getUsername());
        vo.setNickName(user.getNickname());
        vo.setUserPhone(user.getPhone());
        vo.setUserEmail(user.getEmail());
        vo.setUserGender("");
        vo.setCreateBy("system");
        vo.setUpdateBy("system");
        if (user.getCreateTime() != null) {
            vo.setCreateTime(user.getCreateTime().toString());
            vo.setUpdateTime(user.getUpdateTime() != null ? user.getUpdateTime().toString() : null);
        }
        if (user.getRoles() != null) {
            vo.setUserRoles(user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()));
        }
        return vo;
    }
}
