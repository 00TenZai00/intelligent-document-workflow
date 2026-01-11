package com.innovinlabs.documentworkflow.api.controller;

import com.innovinlabs.documentworkflow.api.dto.RegisterUserRequest;
import com.innovinlabs.documentworkflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PublicAuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {
        userService.registerUser(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
