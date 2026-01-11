package com.innovinlabs.documentworkflow.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignatureJobPayload {
    private Long documentId;
    private String documentTitle;
    private String signerEmail;
    private String ownerEmail;
}
