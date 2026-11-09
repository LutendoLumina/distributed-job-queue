package com.lumina.jobqueue.job;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class BackoffTest {

    @Test
    void waitDoublesEachAttempt() {
        assertThat(JobLifecycleService.backoff(1).toMillis()).isBetween(2000L, 3000L);
        assertThat(JobLifecycleService.backoff(2).toMillis()).isBetween(4000L, 5000L);
        assertThat(JobLifecycleService.backoff(3).toMillis()).isBetween(8000L, 9000L);
    }

    @Test
    void waitNeverGoesAbove5Minutes() {
        assertThat(JobLifecycleService.backoff(50))
                .isLessThanOrEqualTo(Duration.ofMinutes(5).plusSeconds(1));
    }
}