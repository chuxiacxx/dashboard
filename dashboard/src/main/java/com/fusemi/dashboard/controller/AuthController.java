package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.dto.LoginDTO;
import com.fusemi.dashboard.service.AuthService;
import com.fusemi.dashboard.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    @GetMapping("/userinfo")
    public Result<UserVO> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        UserVO userInfo = authService.getUserInfo(username);
        if (userInfo == null) {
            return Result.unauthorized("用户不存在");
        }
        return Result.ok(userInfo);
    }
}
