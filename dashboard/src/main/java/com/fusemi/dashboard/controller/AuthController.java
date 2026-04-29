package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.dto.LoginDTO;
import com.fusemi.dashboard.service.AuthService;
import com.fusemi.dashboard.vo.LoginVO;
import com.fusemi.dashboard.vo.UserInfoVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    @GetMapping("/auth/userinfo")
    public Result<UserInfoVO> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        UserInfoVO userInfo = authService.getUserInfo(username);
        if (userInfo == null) {
            return Result.unauthorized("用户不存在");
        }
        return Result.ok(userInfo);
    }

    /** 前端调用 /api/user/info，需兼容 /api/auth/userinfo */
    @GetMapping("/user/info")
    public Result<UserInfoVO> getUserInfoAlt(Authentication authentication) {
        return getUserInfo(authentication);
    }

    @PutMapping("/auth/password")
    public Result<?> changePassword(Authentication authentication,
                                     @RequestParam String oldPassword,
                                     @RequestParam String newPassword) {
        String username = authentication.getName();
        return authService.changePassword(username, oldPassword, newPassword);
    }

    @PutMapping("/auth/userinfo")
    public Result<?> updateUserInfo(Authentication authentication,
                                     @RequestBody Map<String, String> params) {
        String username = authentication.getName();
        return authService.updateUserInfo(username, params);
    }
}
