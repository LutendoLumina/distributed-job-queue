package com.lumina.jobqueue.worker;

import com.lumina.jobqueue.job.Job;
import com.lumina.jobqueue.job.JobLifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JobWorker {

    private static final Logger log = LoggerFactory.getLogger(JobWorker.class);

    private final JobLifecycleService lifecycle;
    private final Map<String, JobHandler> handlers;
    private final String workerId = "worker-" + UUID.randomUUID().toString().substring(0, 8);
    private final int batchSize;
    private final Duration lease;

    public JobWorker(JobLifecycleService lifecycle,
                     List<JobHandler> handlerList,
                     @Value("${jobqueue.worker.batch-size:5}") int batchSize,
                     @Value("${jobqueue.worker.lease-seconds:60}") int leaseSeconds) {
        this.lifecycle = lifecycle;
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(JobHandler::type, Function.identity()));
        this.batchSize = batchSize;
        this.lease = Duration.ofSeconds(leaseSeconds);
        log.info("{} started with handlers {}", workerId, handlers.keySet());
    }

    @Scheduled(fixedDelayString = "${jobqueue.worker.poll-interval-ms:1000}")
    public void poll() {
        List<Job> jobs = lifecycle.claim(workerId, batchSize, lease);
        for (Job job : jobs) {
            execute(job);
        }
    }

    private void execute(Job job) {
        JobHandler handler = handlers.get(job.getType());
        if (handler == null) {
            lifecycle.markFailed(job.getId(), workerId, "No handler for type " + job.getType());
            return;
        }
        try {
            log.info("{} running job {} ({}), attempt {}", workerId, job.getId(), job.getType(), job.getAttempts());
            handler.handle(job.getPayload());
            lifecycle.markSucceeded(job.getId(), workerId);
        } catch (Exception e) {
            log.warn("{} job {} failed: {}", workerId, job.getId(), e.getMessage());
            lifecycle.markFailed(job.getId(), workerId, e.getMessage());
        }
    }
}