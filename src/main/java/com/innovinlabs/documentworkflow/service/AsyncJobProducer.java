package com.innovinlabs.documentworkflow.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovinlabs.documentworkflow.entity.*;
import com.innovinlabs.documentworkflow.repository.AsyncJobRepository;
import com.innovinlabs.documentworkflow.repository.DocumentRepository;
import com.innovinlabs.documentworkflow.repository.UserRepository;
import com.innovinlabs.documentworkflow.service.dto.SignatureJobPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AsyncJobProducer {

    private final AsyncJobRepository asyncJobRepository;
    private final ObjectMapper objectMapper;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    /**
     * Enqueue signature request notification job.
     * Triggered when a document enters AWAITING_SIGNATURE state.
     */
    @Transactional
    public void enqueueSignatureRequest(Long documentId) {

        Document doc = documentRepository.findById(documentId)
                .orElseThrow();

        User signer = userRepository.findById(doc.getSigner().getId()).orElseThrow();
        User owner = userRepository.findById(doc.getOwner().getId()).orElseThrow();

        SignatureJobPayload payload = new SignatureJobPayload(
                doc.getId(),
                doc.getTitle(),
                signer.getEmail(),
                owner.getEmail()
        );

        enqueueJob(
                JobType.SIGN_REQUEST,
                documentId,
                toJson(payload),
                Instant.now()
        );
    }

    /**
     * Enqueue reminder job with delay
     */
    @Transactional
    public void enqueueReminder(Long documentId, Instant triggerTime) {

        Document doc = documentRepository.findById(documentId).orElseThrow();
        User signer = userRepository.findById(doc.getSigner().getId()).orElseThrow();
        User owner = userRepository.findById(doc.getOwner().getId()).orElseThrow();

        SignatureJobPayload payload = new SignatureJobPayload(
                doc.getId(),
                doc.getTitle(),
                signer.getEmail(),
                owner.getEmail()
        );

        enqueueJob(
                JobType.REMINDER,
                documentId,
                toJson(payload),
                triggerTime
        );
    }

    /**
     * Enqueue notification after successful signing.
     */
    @Transactional
    public void enqueueSignConfirmation(Long documentId) {

        Document doc = documentRepository.findById(documentId).orElseThrow();
        User signer = userRepository.findById(doc.getSigner().getId()).orElseThrow();
        User owner = userRepository.findById(doc.getOwner().getId()).orElseThrow();

        SignatureJobPayload payload = new SignatureJobPayload(
                doc.getId(),
                doc.getTitle(),
                signer.getEmail(),
                owner.getEmail()
        );

        enqueueJob(
                JobType.NOTIFICATION,
                documentId,
                toJson(payload),
                Instant.now()
        );
    }

    /**
     * Core job creation logic.
     * Ensures idempotency via DB unique constraints.
     */
    private void enqueueJob(
            JobType jobType,
            Long referenceId,
            String payload,
            Instant nextRetryAt
    ) {
        AsyncJob job = AsyncJob.builder()
                .jobType(jobType)
                .referenceId(referenceId)
                .payload(payload)
                .status(JobStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .nextRetryAt(nextRetryAt)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        asyncJobRepository.save(job);
    }

    /** Serialize payload to JSON string */
    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize payload", e);
        }
    }

}
