package com.lumina.jobqueue.job;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    private static final int DEFAULT_MAX_ATTEMPTS = 5;

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public Job create(CreateJobRequest request) {
        Job job = new Job(
                request.type(),
                request.payload().toString(),
                request.priority() != null ? request.priority() : 0,
                request.maxAttempts() != null ? request.maxAttempts() : DEFAULT_MAX_ATTEMPTS,
                request.runAt() != null ? request.runAt() : Instant.now()
        );
        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Job get(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Job> list(JobStatus status) {
        return status == null ? jobRepository.findAll() : jobRepository.findByStatus(status);
    }
}