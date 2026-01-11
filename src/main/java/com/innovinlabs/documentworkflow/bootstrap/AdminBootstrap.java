package com.innovinlabs.documentworkflow.bootstrap;

import com.innovinlabs.documentworkflow.entity.User;
import com.innovinlabs.documentworkflow.entity.UserRole;
import com.innovinlabs.documentworkflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    public void run(ApplicationArguments args) {

        boolean enabled = Boolean.parseBoolean(env.getProperty("admin.bootstrap.enabled", "false"));
        if (!enabled) return;

        String email = env.getProperty("admin.bootstrap.email");
        String password = env.getProperty("admin.bootstrap.password");

        if (email == null || password == null) {
            throw new IllegalStateException("Admin bootstrap credentials not configured");
        }

        if (userRepository.existsByEmail(email)) return;

        User admin = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(UserRole.ROOT_ADMIN)
                .build();

        userRepository.save(admin);

        System.out.println("Admin account provisioned: " + email);
    }
}
