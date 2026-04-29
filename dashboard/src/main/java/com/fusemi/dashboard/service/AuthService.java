package com.fusemi.dashboard.service;

import com.fusemi.dashboard.dto.LoginDTO;
import com.fusemi.dashboard.entity.Role;
import com.fusemi.dashboard.entity.User;
import com.fusemi.dashboard.repository.RoleRepository;
import com.fusemi.dashboard.repository.UserRepository;
import com.fusemi.dashboard.vo.LoginVO;
import com.fusemi.dashboard.vo.UserInfoVO;
import com.fusemi.dashboard.util.JwtUtil;
import com.fusemi.dashboard.common.Result;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Result<LoginVO> login(LoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUserName())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        if (user.getStatus() == 1) {
            return Result.fail("账号已被禁用");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 生成包含角色的JWT token
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setRefreshToken(token);
        return Result.ok("登录成功", vo);
    }

    public UserInfoVO getUserInfo(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return null;

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUserName(user.getUsername());
        vo.setNickName(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setButtons(Collections.emptyList());

        Set<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
        vo.setRoles(roles.stream().toList());

        return vo;
    }

    @Transactional
    public Result<?> changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.fail("原密码错误");
        }

        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail("新密码长度不能少于6位");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return Result.ok("密码修改成功");
    }

    @Transactional
    public Result<?> updateUserInfo(String username, Map<String, String> params) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (params.containsKey("nickName")) {
            user.setNickname(params.get("nickName"));
        }
        if (params.containsKey("email")) {
            user.setEmail(params.get("email"));
        }
        if (params.containsKey("phone")) {
            user.setPhone(params.get("phone"));
        }
        if (params.containsKey("gender")) {
            user.setGender(params.get("gender"));
        }

        userRepository.save(user);
        return Result.ok("用户信息更新成功");
    }
}
