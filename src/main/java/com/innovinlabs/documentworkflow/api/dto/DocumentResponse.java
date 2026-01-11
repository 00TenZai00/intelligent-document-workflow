package com.innovinlabs.documentworkflow.api.dto;


import com.innovinlabs.documentworkflow.entity.DocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DocumentResponse {

    private Long id;
    private String title;
    private DocumentStatus status;
    private Long ownerId;
    private Long signerId;
    private Instant createdAt;
    private Instant signedAt;
}

