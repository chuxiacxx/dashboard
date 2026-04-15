package com.fusemi.dashboard.service;

import com.fusemi.dashboard.dto.MenuDTO;
import com.fusemi.dashboard.entity.Menu;
import com.fusemi.dashboard.repository.MenuRepository;
import com.fusemi.dashboard.vo.MenuTreeVO;
import com.fusemi.dashboard.common.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    /** 获取菜单树（扁平结构，用于前端渲染） */
    public List<MenuTreeVO> getMenuTree() {
        List<Menu> all = menuRepository.findAll();
        return buildTree(all, 0L);
    }

    private List<MenuTreeVO> buildTree(List<Menu> all, Long parentId) {
        return all.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .map(m -> {
                    MenuTreeVO vo = toVO(m);
                    vo.setChildren(buildTree(all, m.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /** 获取所有菜单（树形） */
    public List<MenuTreeVO> getAllMenuTree() {
        return getMenuTree();
    }

    @Transactional
    public Result<?> addMenu(MenuDTO dto) {
        Menu menu = new Menu();
        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setType(dto.getType() != null ? dto.getType() : 1);
        menu.setIcon(dto.getIcon());
        menu.setSort(dto.getSort() != null ? dto.getSort() : 0);
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        menu.setPermission(dto.getPermission());
        menu.setVisible(dto.getVisible() != null ? dto.getVisible() : 0);
        menu.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        menuRepository.save(menu);
        return Result.ok();
    }

    @Transactional
    public Result<?> updateMenu(MenuDTO dto) {
        Menu menu = menuRepository.findById(dto.getId()).orElse(null);
        if (menu == null) return Result.fail("菜单不存在");

        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setType(dto.getType());
        menu.setIcon(dto.getIcon());
        menu.setSort(dto.getSort());
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        menu.setPermission(dto.getPermission());
        menu.setVisible(dto.getVisible());
        menu.setStatus(dto.getStatus());
        menuRepository.save(menu);
        return Result.ok();
    }

    @Transactional
    public Result<?> deleteMenu(Long id) {
        if (!menuRepository.existsById(id)) return Result.fail("菜单不存在");
        // 删除子菜单
        List<Menu> children = menuRepository.findByParentIdOrderBySort(id);
        for (Menu child : children) {
            menuRepository.deleteById(child.getId());
        }
        menuRepository.deleteById(id);
        return Result.ok();
    }

    private MenuTreeVO toVO(Menu menu) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(menu.getId());
        vo.setPath(menu.getPath());
        vo.setName(menu.getName());
        vo.setComponent(menu.getComponent());
        vo.setType(menu.getType());
        vo.setIcon(menu.getIcon());
        vo.setSort(menu.getSort());
        vo.setParentId(menu.getParentId());
        vo.setPermission(menu.getPermission());
        vo.setVisible(menu.getVisible());
        vo.setStatus(menu.getStatus());
        return vo;
    }
}
