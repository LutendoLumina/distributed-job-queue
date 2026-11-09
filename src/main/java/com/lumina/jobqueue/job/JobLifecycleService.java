package com.lumina.jobqueue.job;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class JobLifecycleService {

    private static final Duration BASE_DELAY = Duration.ofSeconds(2);
    private static final Duration MAX_DELAY = Duration.ofMinutes(5);

    private final JobRepository jobRepository;

    public JobLifecycleService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public List<Job> claim(String workerId, int batchSize, Duration lease) {
        List<Job> jobs = jobRepository.findReadyForUpdate(batchSize);
        Instant now = Instant.now();
        for (Job job : jobs) {
            job.setStatus(JobStatus.RUNNING);
            job.setLockedBy(workerId);
            job.setLockedUntil(now.plus(lease));
            job.setAttempts(job.getAttempts() + 1);
        }
        return jobs;
    }

    @Transactional
    public void markSucceeded(UUID jobId, String workerId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
        if (!workerId.equals(job.getLockedBy())) return; // another worker took over, don't overwrite

        job.setStatus(JobStatus.SUCCEEDED);
        job.setLockedBy(null);
        job.setLockedUntil(null);
        job.setLastError(null);
    }

    @Transactional
    public void markFailed(UUID jobId, String workerId, String error) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
        if (!workerId.equals(job.getLockedBy())) return;

        job.setLastError(error);
        job.setLockedBy(null);
        job.setLockedUntil(null);

        if (job.getAttempts() >= job.getMaxAttempts()) {
            job.setStatus(JobStatus.DEAD);
        } else {
            job.setStatus(JobStatus.FAILED);
            job.setRunAt(Instant.now().plus(backoff(job.getAttempts())));
        }
    }

    // 2s, 4s, 8s, 16s... capped at 5 min, plus up to 1s of randomness
    static Duration backoff(int attempts) {
        long delayMs = BASE_DELAY.toMillis() * (1L << Math.min(attempts - 1, 20));
        delayMs = Math.min(delayMs, MAX_DELAY.toMillis());
        long jitterMs = ThreadLocalRandom.current().nextLong(0, 1000);
        return Duration.ofMillis(delayMs + jitterMs);
    }
}