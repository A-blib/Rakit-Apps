-- Antrean bangun website dan penyimpanan data modul dinamis (alur-publish-website.md & penyempurnaanSystem_instruksi.md)

CREATE TABLE site_builds (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id             UUID REFERENCES sites (id) ON DELETE CASCADE,
    project_id          UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    kind                VARCHAR(20) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    requested_by        UUID REFERENCES users (id) ON DELETE SET NULL,
    not_before          TIMESTAMPTZ,
    first_requested_at  TIMESTAMPTZ,
    release_id          UUID REFERENCES site_releases (id) ON DELETE SET NULL,
    export_path         VARCHAR(255),
    export_token_hash   VARCHAR(64),
    export_expires_at   TIMESTAMPTZ,
    error_code          VARCHAR(50),
    error_detail        JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at          TIMESTAMPTZ,
    finished_at         TIMESTAMPTZ,
    CONSTRAINT chk_site_builds_kind CHECK (kind IN ('publish', 'refresh', 'export')),
    CONSTRAINT chk_site_builds_status CHECK (status IN ('queued', 'running', 'succeeded', 'failed'))
);

CREATE INDEX idx_site_builds_project ON site_builds (project_id);
CREATE INDEX idx_site_builds_queue ON site_builds (status, not_before, created_at) WHERE status = 'queued';

-- Penyimpanan kiriman formulir pengunjung (modul dinamis formulir: pendaftaran, aspirasi, dsb.)
CREATE TABLE form_submissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id     UUID NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    form_name   VARCHAR(60) NOT NULL,
    data        JSONB NOT NULL,
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_form_submissions_site ON form_submissions (site_id, form_name, created_at DESC);

-- Konten dinamis bawaan OrgSpace untuk modul berita / kegiatan organisasi
CREATE TABLE site_news (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    summary         VARCHAR(500),
    content         TEXT,
    image_url       VARCHAR(500),
    published_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_site_news_org ON site_news (organization_id, published_at DESC);
