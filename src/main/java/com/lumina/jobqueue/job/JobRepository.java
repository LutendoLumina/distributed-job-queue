package com.lumina.jobqueue.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findByStatus(JobStatus status);

    @Query(value = """
            SELECT * FROM jobs
            WHERE run_at <= now()
              AND (
                    status IN ('PENDING', 'FAILED')
                    OR (status = 'RUNNING' AND locked_until < now())
                  )
            ORDER BY priority DESC, run_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Job> findReadyForUpdate(@Param("limit") int limit);
}
