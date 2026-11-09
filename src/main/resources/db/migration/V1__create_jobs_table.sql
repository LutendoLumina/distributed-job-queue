CREATE TABLE jobs (
                      id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      type          VARCHAR(100) NOT NULL,
                      payload       JSONB        NOT NULL,
                      status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                      priority      INT          NOT NULL DEFAULT 0,
                      attempts      INT          NOT NULL DEFAULT 0,
                      max_attempts  INT          NOT NULL DEFAULT 5,
                      run_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
                      locked_by     VARCHAR(100),
                      locked_until  TIMESTAMPTZ,
                      last_error    TEXT,
                      created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                      updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

                      CONSTRAINT chk_status CHECK (status IN ('PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED', 'DEAD'))
);

CREATE INDEX idx_jobs_ready ON jobs (status, run_at);