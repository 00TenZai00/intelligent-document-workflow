package com.innovinlabs.documentworkflow.api.controller;

import com.innovinlabs.documentworkflow.api.dto.RegisterUserRequest;
import com.innovinlabs.documentworkflow.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("root/admin-register")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ResponseEntity<Void> createAdmin(
            @AuthenticationPrincipal UserDetails rootAdmin,
            @Valid @RequestBody RegisterUserRequest request
    ) {
        adminService.createAdmin(rootAdmin.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
