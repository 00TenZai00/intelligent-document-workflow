package com.innovinlabs.documentworkflow.repository;

import com.innovinlabs.documentworkflow.entity.Document;
import com.innovinlabs.documentworkflow.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwnerId(Long ownerId);

    List<Document> findBySignerId(Long signerId);

    List<Document> findByStatus(DocumentStatus status);
    @Query("""
    SELECT d FROM Document d
    WHERE d.signer.id = :signerId
      AND d.status = :status
      AND NOT EXISTS (
          SELECT 1 FROM Signature s WHERE s.document = d
      )
""")
List<Document> findPendingDocumentsToSign(
        @Param("signerId") Long signerId,
        @Param("status") DocumentStatus status
);


    Optional<Document> findByIdAndSignerId(Long documentId, Long signerId);

    Optional<Document> findByIdAndOwnerId(Long documentId, Long ownerId);


}

