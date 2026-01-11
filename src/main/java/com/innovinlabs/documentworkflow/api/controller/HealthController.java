package com.innovinlabs.documentworkflow.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    // Simple health check endpoint
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}
