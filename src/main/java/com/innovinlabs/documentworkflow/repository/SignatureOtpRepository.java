package com.innovinlabs.documentworkflow.repository;


import com.innovinlabs.documentworkflow.entity.SignatureOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface SignatureOtpRepository extends JpaRepository<SignatureOtp, Long> {

    Optional<SignatureOtp> findByDocumentIdAndSignerIdAndUsedFalse(
            Long documentId,
            Long signerId
    );

    Optional<SignatureOtp> findByDocumentIdAndSignerIdAndUsedFalseAndExpiresAtAfter(
            Long documentId,
            Long signerId,
            Instant now
    );


    void deleteByExpiresAtBefore(Instant cutoffTime);
}

