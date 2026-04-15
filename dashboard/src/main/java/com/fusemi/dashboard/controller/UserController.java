package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.PageResult;
import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.dto.UserDTO;
import com.fusemi.dashboard.service.UserService;
import com.fusemi.dashboard.vo.UserItemVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN')")
    public Result<PageResult<UserItemVO>> getUserList(
            @RequestParam Integer current,
            @RequestParam Integer size,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String status) {
        return Result.ok(userService.getUserPage(current, size, userName, status));
    }

    @PostMapping
    @PreAuthorize("hasRole('R_SUPER')")
    public Result<?> addUser(@Valid @RequestBody UserDTO dto) {
        return userService.addUser(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN')")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        dto.setId(id);
        return userService.updateUser(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('R_SUPER')")
    public Result<?> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}
