package com.fusemi.dashboard.config;

import com.fusemi.dashboard.entity.Role;
import com.fusemi.dashboard.entity.User;
import com.fusemi.dashboard.repository.RoleRepository;
import com.fusemi.dashboard.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 创建默认角色
        if (roleRepository.count() == 0) {
            Role superAdmin = new Role();
            superAdmin.setName("超级管理员");
            superAdmin.setCode("R_SUPER");
            superAdmin.setDescription("系统超级管理员，拥有所有权限");
            superAdmin.setStatus(0);
            roleRepository.save(superAdmin);

            Role admin = new Role();
            admin.setName("管理员");
            admin.setCode("R_ADMIN");
            admin.setDescription("系统管理员");
            admin.setStatus(0);
            roleRepository.save(admin);

            Role user = new Role();
            user.setName("普通用户");
            user.setCode("R_USER");
            user.setDescription("普通用户");
            user.setStatus(0);
            roleRepository.save(user);
        }

        // 创建默认管理员账号
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setEmail("admin@fusemi.com");
            admin.setStatus(0);

            roleRepository.findByCode("R_SUPER").ifPresent(role ->
                admin.setRoles(Set.of(role))
            );
            userRepository.save(admin);
        }
    }
}
