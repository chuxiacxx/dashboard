package com.fusemi.dashboard.service;

import com.fusemi.dashboard.dto.LoginDTO;
import com.fusemi.dashboard.entity.User;
import com.fusemi.dashboard.vo.UserVO;
import com.fusemi.dashboard.repository.UserRepository;
import com.fusemi.dashboard.util.JwtUtil;
import com.fusemi.dashboard.common.Result;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public Result<?> login(LoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        if (user.getStatus() == 1) {
            return Result.fail("账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return Result.ok("登录成功", token);
    }

    public UserVO getUserInfo(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return null;

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
