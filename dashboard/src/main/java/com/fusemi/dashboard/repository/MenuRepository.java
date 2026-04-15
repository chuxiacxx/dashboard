package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByParentIdOrderBySort(Long parentId);

    List<Menu> findByStatus(Integer status);
}
