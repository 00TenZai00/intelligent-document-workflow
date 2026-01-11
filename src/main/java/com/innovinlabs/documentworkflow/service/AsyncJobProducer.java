package com.innovinlabs.documentworkflow.service;


import com.innovinlabs.documentworkflow.entity.AsyncJob;
import com.innovinlabs.documentworkflow.entity.JobStatus;
import com.innovinlabs.documentworkflow.entity.JobType;
import com.innovinlabs.documentworkflow.repository.AsyncJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AsyncJobProducer {

    private final AsyncJobRepository asyncJobRepository;

    /**
     * Enqueue signature request notification job.
     * Triggered when a document enters AWAITING_SIGNATURE state.
     */
    @Transactional
    public void enqueueSignatureRequest(Long documentId) {
        enqueueJob(
                JobType.SIGN_REQUEST,
                documentId,
                null,
                Instant.now()
        );
    }

    /**
     * Enqueue reminder job with delay (e.g., 24 hours).
     */
    @Transactional
    public void enqueueReminder(Long documentId, Instant triggerTime) {
        enqueueJob(
                JobType.REMINDER,
                documentId,
                null,
                triggerTime
        );
    }

    /**
     * Enqueue notification after successful signing.
     */
    @Transactional
    public void enqueueSignConfirmation(Long documentId) {
        enqueueJob(
                JobType.NOTIFICATION,
                documentId,
                null,
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
}
