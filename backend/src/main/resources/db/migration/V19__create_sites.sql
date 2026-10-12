-- Website yang punya alamat (alur-publish-website.md 7.2): situs, rilis, dan permintaan terbit.
-- Dibuat lebih awal (fase W2) karena status di tab Website (Online / ada perubahan / Dimatikan / Ditangguhkan)
-- dihitung dari tabel ini. Antrean bangun, domain, dan riwayat nama ditambahkan di fase publish.

CREATE TABLE sites (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id           UUID         NOT NULL UNIQUE REFERENCES projects (id) ON DELETE CASCADE,
    -- Disalin dari project agar cek batas website per ruang kerja cepat.
    organization_id      UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    slug                 VARCHAR(40)  NOT NULL UNIQUE,
    status               VARCHAR(20)  NOT NULL,
    current_release_id   UUID,
    suspension_reason    TEXT,
    seo_title            VARCHAR(70),
    seo_description      VARCHAR(160),
    favicon_asset_id     UUID         REFERENCES project_assets (id) ON DELETE SET NULL,
    share_image_asset_id UUID         REFERENCES project_assets (id) ON DELETE SET NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at         TIMESTAMPTZ,
    CONSTRAINT chk_sites_status CHECK (status IN ('online', 'offline', 'suspended')),
    CONSTRAINT chk_sites_slug CHECK (slug = lower(slug))
);

CREATE INDEX idx_sites_org ON sites (organization_id);

CREATE TABLE site_releases (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id          UUID         NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    version          INT          NOT NULL,
    -- Salinan data project saat dibangun; kembali ke versi lama juga mengembalikan isi editor (K21).
    project_snapshot JSONB        NOT NULL,
    kind             VARCHAR(20)  NOT NULL,
    published_by     UUID         REFERENCES users (id) ON DELETE SET NULL,
    size_bytes       BIGINT       NOT NULL DEFAULT 0,
    file_count       INT          NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_site_releases_version UNIQUE (site_id, version),
    CONSTRAINT chk_site_releases_kind CHECK (kind IN ('publish', 'refresh', 'rollback'))
);

ALTER TABLE sites
    ADD CONSTRAINT fk_sites_current_release FOREIGN KEY (current_release_id)
        REFERENCES site_releases (id) ON DELETE SET NULL;

-- "Minta diterbitkan" dari Editor ke Admin/Pemilik.
CREATE TABLE publish_requests (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    organization_id UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    requested_by    UUID         REFERENCES users (id) ON DELETE SET NULL,
    note            VARCHAR(200),
    status          VARCHAR(20)  NOT NULL DEFAULT 'open',
    handled_by      UUID         REFERENCES users (id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    handled_at      TIMESTAMPTZ,
    CONSTRAINT chk_publish_requests_status CHECK (status IN ('open', 'done', 'dismissed'))
);

-- Satu website hanya punya satu permintaan terbuka; permintaan baru memperbarui yang lama.
CREATE UNIQUE INDEX uq_publish_requests_open ON publish_requests (project_id) WHERE status = 'open';
CREATE INDEX idx_publish_requests_org_open ON publish_requests (organization_id) WHERE status = 'open';
