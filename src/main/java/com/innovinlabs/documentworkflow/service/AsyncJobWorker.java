package com.innovinlabs.documentworkflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovinlabs.documentworkflow.entity.AsyncJob;
import com.innovinlabs.documentworkflow.entity.JobStatus;
import com.innovinlabs.documentworkflow.repository.AsyncJobRepository;
import com.innovinlabs.documentworkflow.service.dto.SignatureJobPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncJobWorker {

    private final AsyncJobRepository jobRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void pollAndProcess() {

        List<AsyncJob> jobs =
                jobRepository.findJobsForProcessing(JobStatus.PENDING, Instant.now());
        if (jobs.isEmpty()) {
            return;
        }

        for (AsyncJob job : jobs) {
            try {
                job.setStatus(JobStatus.PROCESSING);
                jobRepository.save(job);

                process(job);

                job.setStatus(JobStatus.COMPLETED);
            } catch (Exception ex) {
                handleFailure(job, ex);
            }
        }
    }

    private void process(AsyncJob job) {
        try {
            SignatureJobPayload payload =
                    objectMapper.readValue(job.getPayload(), SignatureJobPayload.class);

            switch (job.getJobType()) {
                case SIGN_REQUEST -> notificationService.sendSignatureRequest(payload);
                case REMINDER -> notificationService.sendReminder(payload);
                case NOTIFICATION -> notificationService.sendConfirmation(payload);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process async job " + job.getId(), e);
        }
    }


    private void handleFailure(AsyncJob job, Exception ex) {
        job.setRetryCount(job.getRetryCount() + 1);

        if (job.getRetryCount() > 5) {
            job.setStatus(JobStatus.FAILED);
        } else {
            job.setNextRetryAt(Instant.now().plusSeconds(60));
            job.setStatus(JobStatus.PENDING);
        }

        log.error("Job {} failed. Retry {}", job.getId(), job.getRetryCount(), ex);
    }
}
