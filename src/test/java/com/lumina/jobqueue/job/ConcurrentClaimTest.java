package com.lumina.jobqueue.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcurrentClaimTest {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobLifecycleService lifecycle;

    @BeforeEach
    void cleanDatabase() {
        jobRepository.deleteAll();
    }

    @Test
    void eachJobIsClaimedExactlyOnce() throws Exception {
        int jobCount = 200;
        int workerCount = 5;

        // 1. Create 200 jobs
        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < jobCount; i++) {
            jobs.add(new Job("SEND_EMAIL", "{}", 0, 5, Instant.now().minusSeconds(1)));
        }
        jobRepository.saveAll(jobs);

        // 2. Start 5 workers at the same moment
        List<UUID> claimed = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<?>> running = new ArrayList<>();

        for (int w = 0; w < workerCount; w++) {
            String workerId = "test-worker-" + w;
            running.add(pool.submit(() -> {
                startSignal.await();
                while (true) {
                    List<Job> batch = lifecycle.claim(workerId, 10, Duration.ofMinutes(5));
                    if (batch.isEmpty()) break;
                    batch.forEach(job -> claimed.add(job.getId()));
                }
                return null;
            }));
        }

        startSignal.countDown(); // GO!
        for (Future<?> f : running) f.get(30, TimeUnit.SECONDS);
        pool.shutdown();

        // 3. Check: 200 claims, and 200 different jobs (no duplicates)
        assertThat(claimed).hasSize(jobCount);
        assertThat(new HashSet<>(claimed)).hasSize(jobCount);
    }
}