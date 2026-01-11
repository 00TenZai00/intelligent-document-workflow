package com.innovinlabs.documentworkflow.repository;


import com.innovinlabs.documentworkflow.entity.AsyncJob;
import com.innovinlabs.documentworkflow.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;

public interface AsyncJobRepository extends JpaRepository<AsyncJob, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT j FROM AsyncJob j
        WHERE j.status = :status
          AND j.nextRetryAt <= :now
    """)
    List<AsyncJob> findJobsForProcessing(
            JobStatus status,
            Instant now
    );
}

