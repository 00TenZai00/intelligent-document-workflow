package com.innovinlabs.documentworkflow.service;


import com.innovinlabs.documentworkflow.entity.AuditLog;
import com.innovinlabs.documentworkflow.entity.Document;
import com.innovinlabs.documentworkflow.entity.User;
import com.innovinlabs.documentworkflow.repository.AuditLogRepository;
import com.innovinlabs.documentworkflow.repository.DocumentRepository;
import com.innovinlabs.documentworkflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor

public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    /**
     * Record an audit log entry for a security-sensitive action.
     */
    @Transactional
    public void logAction(Long userId, String action, Long documentId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .document(document)
                .timestamp(Instant.now())
                .build();

        auditLogRepository.save(auditLog);
    }
}

