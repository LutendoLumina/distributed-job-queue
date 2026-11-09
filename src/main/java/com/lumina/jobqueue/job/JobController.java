package com.lumina.jobqueue.job;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> create(@Valid @RequestBody CreateJobRequest request) {
        Job job = jobService.create(request);
        return ResponseEntity
                .created(URI.create("/jobs/" + job.getId()))
                .body(JobResponse.from(job));
    }

    @GetMapping("/{id}")
    public JobResponse get(@PathVariable UUID id) {
        return JobResponse.from(jobService.get(id));
    }

    @GetMapping
    public List<JobResponse> list(@RequestParam(required = false) JobStatus status) {
        return jobService.list(status).stream().map(JobResponse::from).toList();
    }
}