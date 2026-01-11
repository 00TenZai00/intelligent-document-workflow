package com.innovinlabs.documentworkflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequest {
    @NotBlank
    private String otp;
}