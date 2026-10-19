-- Tabel custom domain untuk website organisasi (alur-publish-website.md 7.2 & 7.10)
CREATE TABLE site_domains (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id             UUID NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    hostname            VARCHAR(253) NOT NULL UNIQUE,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending',
    verification_token  VARCHAR(64) NOT NULL,
    last_checked_at     TIMESTAMPTZ,
    activated_at        TIMESTAMPTZ,
    failing_since       TIMESTAMPTZ,
    last_error          TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_site_domains_status CHECK (status IN ('pending', 'active', 'failed', 'removed')),
    CONSTRAINT chk_site_domains_hostname CHECK (hostname = lower(hostname))
);

CREATE INDEX idx_site_domains_site ON site_domains (site_id);
CREATE INDEX idx_site_domains_hostname_active ON site_domains (hostname) WHERE status = 'active';
