-- Hasil pengecekan otomatis template (bagian 4.2 & 4.6) beserta daftar masalahnya.
CREATE TABLE template_checks (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID        NOT NULL REFERENCES templates (id) ON DELETE CASCADE,
    version     INTEGER     NOT NULL,
    status      VARCHAR(20) NOT NULL,
    finished_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_template_checks_status CHECK (status IN ('running', 'passed', 'failed')),
    CONSTRAINT uq_template_checks_version UNIQUE (template_id, version)
);

CREATE TABLE template_check_issues (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    check_id   UUID         NOT NULL REFERENCES template_checks (id) ON DELETE CASCADE,
    -- error = template tidak tayang; warning = tetap tayang tetapi disarankan diperbaiki.
    severity   VARCHAR(10)  NOT NULL,
    code       VARCHAR(50)  NOT NULL,
    message    VARCHAR(300) NOT NULL,
    file       VARCHAR(255),
    line       INTEGER,
    suggestion VARCHAR(300),
    CONSTRAINT chk_template_check_issues_severity CHECK (severity IN ('error', 'warning'))
);

CREATE INDEX ix_template_check_issues_check_id ON template_check_issues (check_id);
