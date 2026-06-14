package com.ongmanager.config;

import com.ongmanager.entity.Role;
import com.ongmanager.entity.User;
import com.ongmanager.repository.RoleRepository;
import com.ongmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.name:Administrador}")
    private String adminName;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseThrow(() -> new IllegalStateException("Role ADMIN nao encontrada"));

        User user = userRepository.findByEmail(adminEmail).orElse(null);
        if (user == null) {
            user = User.builder()
                .name(adminName)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .enabled(true)
                .active(true)
                .roles(new HashSet<>())
                .build();
            user.getRoles().add(adminRole);
            userRepository.save(user);
            return;
        }

        boolean changed = false;
        if (!user.getRoles().contains(adminRole)) {
            user.getRoles().add(adminRole);
            changed = true;
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            user.setEnabled(true);
            changed = true;
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            user.setActive(true);
            changed = true;
        }
        if (changed) {
            userRepository.save(user);
        }
    }
}

