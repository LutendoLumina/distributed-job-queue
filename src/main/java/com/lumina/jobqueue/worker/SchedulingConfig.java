package com.lumina.jobqueue.worker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "jobqueue.worker.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
