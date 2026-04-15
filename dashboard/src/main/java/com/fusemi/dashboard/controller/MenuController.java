package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.dto.MenuDTO;
import com.fusemi.dashboard.service.MenuService;
import com.fusemi.dashboard.vo.MenuTreeVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** 前端 /api/v3/system/menus/simple */
    @GetMapping("/v3/system/menus/simple")
    public Result<List<MenuTreeVO>> getMenuSimple() {
        return Result.ok(menuService.getMenuTree());
    }

    @GetMapping("/menu/tree")
    public Result<List<MenuTreeVO>> getMenuTree() {
        return Result.ok(menuService.getAllMenuTree());
    }

    @PostMapping("/menu")
    public Result<?> addMenu(@Valid @RequestBody MenuDTO dto) {
        return menuService.addMenu(dto);
    }

    @PutMapping("/menu/{id}")
    public Result<?> updateMenu(@PathVariable Long id, @RequestBody MenuDTO dto) {
        dto.setId(id);
        return menuService.updateMenu(dto);
    }

    @DeleteMapping("/menu/{id}")
    public Result<?> deleteMenu(@PathVariable Long id) {
        return menuService.deleteMenu(id);
    }
}
