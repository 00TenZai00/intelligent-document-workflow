package com.innovinlabs.documentworkflow.service;

import com.innovinlabs.documentworkflow.api.dto.RegisterUserRequest;
import com.innovinlabs.documentworkflow.entity.User;
import com.innovinlabs.documentworkflow.entity.UserRole;
import com.innovinlabs.documentworkflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createAdmin(String rootAdminEmail, RegisterUserRequest request) {

        User rootAdmin = userRepository.findByEmail(rootAdminEmail)
                .orElseThrow(() -> new AccessDeniedException("Root admin not found"));

        if (rootAdmin.getRole() != UserRole.ROOT_ADMIN) {
            throw new AccessDeniedException("Only Root Admin can create admins");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User admin = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ADMIN)
                .build();

        userRepository.save(admin);
    }
}
