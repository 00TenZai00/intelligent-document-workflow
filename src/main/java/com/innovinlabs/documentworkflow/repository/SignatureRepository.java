package com.innovinlabs.documentworkflow.repository;


import com.innovinlabs.documentworkflow.entity.Signature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignatureRepository extends JpaRepository<Signature, Long> {

    Optional<Signature> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);
}

