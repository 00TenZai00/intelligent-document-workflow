package com.innovinlabs.documentworkflow.service;


import com.innovinlabs.documentworkflow.repository.DocumentRepository;
import com.innovinlabs.documentworkflow.repository.UserRepository;
import com.innovinlabs.documentworkflow.entity.Document;
import com.innovinlabs.documentworkflow.entity.DocumentStatus;
import com.innovinlabs.documentworkflow.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AsyncJobProducer asyncJobProducer;
    private final AuditService auditService;

    /**
     * Create a new document and assign a signer.
     */
    @Transactional
    public Document createDocument(
            String title,
            String content,
            Long ownerId,
            Long signerId
    ) {
        if (ownerId.equals(signerId)) {
            throw new IllegalArgumentException("Owner and signer must be different users");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        User signer = userRepository.findById(signerId)
                .orElseThrow(() -> new IllegalArgumentException("Signer not found"));

        Document document = Document.builder()
                .title(title)
                .content(content)
                .status(DocumentStatus.CREATED)
                .owner(owner)
                .signer(signer)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        document = documentRepository.save(document);

        // Transition to AWAITING_SIGNATURE
        document.setStatus(DocumentStatus.AWAITING_SIGNATURE);

        // Async signature request notification
        asyncJobProducer.enqueueSignatureRequest(document.getId());

        // Audit
        auditService.logAction(ownerId, "CREATE_DOCUMENT", document.getId());

        return document;
    }

    /**
     * Fetch documents created by a user.
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsOwnedBy(Long ownerId) {
        return documentRepository.findByOwnerId(ownerId);
    }

    /**
     * Fetch documents assigned to a signer.
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsToSign(Long signerId) {
        return documentRepository.findPendingDocumentsToSign(
                signerId,
                DocumentStatus.AWAITING_SIGNATURE
        );
    }

    /**
     * Fetch a document with ownership validation.
     */
    @Transactional(readOnly = true)
    public Document getDocumentForOwner(Long documentId, Long ownerId) {
        return documentRepository.findByIdAndOwnerId(documentId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found or access denied"));
    }

    /**
     * Fetch a document with signer validation.
     */
    @Transactional(readOnly = true)
    public Document getDocumentForSigner(Long documentId, Long signerId) {
        return documentRepository.findByIdAndSignerId(documentId, signerId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found or access denied"));
    }

    /**
     * Internal helper for signature workflow.
     * Used by SignatureService.
     */
    @Transactional
    public Document markDocumentAsSigned(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalStateException("Document not found"));

        if (document.getStatus() != DocumentStatus.AWAITING_SIGNATURE) {
            throw new IllegalStateException("Document is not in a signable state");
        }

        document.setStatus(DocumentStatus.SIGNED);
        document.setSignedAt(Instant.now());
        document.setUpdatedAt(Instant.now());

        return document;
    }
}

