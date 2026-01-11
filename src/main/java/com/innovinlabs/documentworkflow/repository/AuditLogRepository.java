package com.innovinlabs.documentworkflow.repository;


import com.innovinlabs.documentworkflow.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByDocumentIdOrderByTimestampDesc(Long documentId);

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
}

