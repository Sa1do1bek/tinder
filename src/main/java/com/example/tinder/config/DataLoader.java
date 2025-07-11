package com.example.tinder.config;

import com.example.tinder.entity.UserRole;
import com.example.tinder.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        saveRoleIfNotExists("USER");
        saveRoleIfNotExists("ADMIN");
    }

    private void saveRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            roleRepository.save(UserRole.builder().name(roleName).build());
        }
    }
}
