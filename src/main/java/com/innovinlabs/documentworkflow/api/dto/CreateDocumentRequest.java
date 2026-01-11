package com.innovinlabs.documentworkflow.api.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDocumentRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private Long signerId;
}

