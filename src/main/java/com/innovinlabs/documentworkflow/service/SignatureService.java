package com.innovinlabs.documentworkflow.service;


import com.innovinlabs.documentworkflow.entity.Document;
import com.innovinlabs.documentworkflow.entity.DocumentStatus;
import com.innovinlabs.documentworkflow.entity.Signature;
import com.innovinlabs.documentworkflow.entity.User;
import com.innovinlabs.documentworkflow.repository.DocumentRepository;
import com.innovinlabs.documentworkflow.repository.SignatureRepository;
import com.innovinlabs.documentworkflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final DocumentRepository documentRepository;
    private final SignatureRepository signatureRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final DocumentService documentService;
    private final AuditService auditService;
    private final AsyncJobProducer asyncJobProducer;

    /**
     * Request OTP for signing a document.
     */
    @Transactional
    public void requestOtp(Long documentId, Long signerId) {

        validateSignerAndState(documentId, signerId);

        otpService.generateAndSendOtp(documentId, signerId);

        auditService.logAction(signerId, "REQUEST_SIGN_OTP", documentId);
    }

    /**
     * Verify OTP and sign the document.
     * Step 1: Verify OTP
     * Step 2: Compute document hash
     * Step 3: Persist signature
     */
    @Transactional
    public void signDocument(Long documentId, Long signerId, String otp) {

        Document document = validateSignerAndState(documentId, signerId);
        otpService.verifyOtp(documentId, signerId, otp);
        String documentHash = computeHash(document.getContent());

        User signer = userRepository.findById(signerId)
                .orElseThrow(() -> new IllegalArgumentException("Signer not found"));

        Signature signature = Signature.builder()
                .document(document)
                .signer(signer)
                .documentHash(documentHash)
                .signedAt(Instant.now())
                .build();

        signatureRepository.save(signature);
        document.setDocumentHash(documentHash);
        documentService.markDocumentAsSigned(documentId);

        auditService.logAction(signerId, "SIGN_DOCUMENT", documentId);
        asyncJobProducer.enqueueSignConfirmation(documentId);
    }

    // =========================
    // Helper Methods
    // =========================

    private Document validateSignerAndState(Long documentId, Long signerId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (!document.getSigner().getId().equals(signerId)) {
            throw new IllegalStateException("User is not authorized to sign this document");
        }

        if (document.getStatus() != DocumentStatus.AWAITING_SIGNATURE) {
            throw new IllegalStateException("Document is not in a signable state");
        }

        if (signatureRepository.existsByDocumentId(documentId)) {
            throw new IllegalStateException("Document already signed");
        }

        return document;
    }

    private String computeHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute document hash", e);
        }
    }
}

