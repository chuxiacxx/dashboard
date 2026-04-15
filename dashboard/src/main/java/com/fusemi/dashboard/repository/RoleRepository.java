package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findByStatus(Integer status);

    boolean existsByCode(String code);

    Optional<Role> findByCode(String code);
}
