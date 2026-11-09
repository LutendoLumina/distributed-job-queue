package com.lumina.jobqueue.job;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.time.Instant;
import java.util.UUID;

public record JobResponse(
        UUID id,
        String type,
        @JsonRawValue String payload,
        JobStatus status,
        int priority,
        int attempts,
        int maxAttempts,
        Instant runAt,
        String lastError,
        Instant createdAt,
        Instant updatedAt
) {
    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(), job.getType(), job.getPayload(), job.getStatus(),
                job.getPriority(), job.getAttempts(), job.getMaxAttempts(),
                job.getRunAt(), job.getLastError(), job.getCreatedAt(), job.getUpdatedAt()
        );
    }
}