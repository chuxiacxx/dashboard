package com.fusemi.dashboard.service;

import com.fusemi.dashboard.dto.LoginDTO;
import com.fusemi.dashboard.dto.RegisterDTO;
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
import java.util.Collections;
import java.util.Set;
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

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setRefreshToken(token);
        return Result.ok("登录成功", vo);
    }

    @Transactional
    public Result<?> register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            return Result.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getUsername());
        user.setStatus(0);

        // 默认给 R_USER 角色
        roleRepository.findByCode("R_USER").ifPresent(role ->
            user.setRoles(Set.of(role))
        );

        userRepository.save(user);
        return Result.ok("注册成功", true);
    }

    public UserInfoVO getUserInfo(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return null;

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUserName(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setButtons(Collections.emptyList());

        Set<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
        vo.setRoles(roles.stream().toList());

        return vo;
    }
}
