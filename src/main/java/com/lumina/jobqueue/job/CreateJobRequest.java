package com.lumina.jobqueue.job;

import jakarta.validation.constraints.*;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record CreateJobRequest(
        @NotBlank @Size(max = 100) String type,
        @NotNull JsonNode payload,
        Integer priority,
        @Min(1) @Max(20) Integer maxAttempts,
        Instant runAt
) {}