package com.fusemi.dashboard.service;

import com.fusemi.dashboard.dto.LoginDTO;
import com.fusemi.dashboard.entity.User;
import com.fusemi.dashboard.vo.LoginVO;
import com.fusemi.dashboard.vo.UserInfoVO;
import com.fusemi.dashboard.vo.UserVO;
import com.fusemi.dashboard.repository.UserRepository;
import com.fusemi.dashboard.util.JwtUtil;
import com.fusemi.dashboard.common.Result;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
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
        vo.setRefreshToken(token); // 简化：refreshToken 同 token
        return Result.ok("登录成功", vo);
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
                .map(r -> r.getCode())
                .collect(Collectors.toSet());
        vo.setRoles(roles.stream().toList());

        return vo;
    }
}
