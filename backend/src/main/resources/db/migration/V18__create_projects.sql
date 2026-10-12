-- Website (project) disimpan di cloud dan dimiliki ruang kerja (alur-buat-website-via-template.md 12.1, K6, K17).
-- Room di HP hanya cache. Tabel aset, kunci edit, dan cadangan dibuat sekalian karena satu kesatuan.

CREATE TABLE projects (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id    UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name               VARCHAR(100) NOT NULL,
    mode               VARCHAR(20)  NOT NULL,
    -- Template asal boleh dihapus kreatornya; website yang sudah dibuat tetap ada.
    source_template_id UUID         REFERENCES templates (id) ON DELETE SET NULL,
    template_version   INT,
    -- Format values.json: { fields, styles, theme }.
    content            JSONB        NOT NULL DEFAULT '{}'::jsonb,
    -- Naik setiap isi berubah; dipakai optimistic locking (baseVersion) saat sinkron dari HP.
    content_version    INT          NOT NULL DEFAULT 0,
    content_updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    missing_count      INT          NOT NULL DEFAULT 0,
    thumbnail_asset_id UUID,
    created_by         UUID         REFERENCES users (id) ON DELETE SET NULL,
    updated_by         UUID         REFERENCES users (id) ON DELETE SET NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_projects_mode CHECK (mode IN ('template', 'assembled')),
    CONSTRAINT chk_projects_missing CHECK (missing_count >= 0)
);

-- Tab Website: daftar per ruang kerja, urut terakhir diubah.
CREATE INDEX idx_projects_org_updated ON projects (organization_id, updated_at DESC);

-- Gambar website. Tidak pernah diubah setelah dibuat; isi project merujuknya sebagai "asset:{id}".
CREATE TABLE project_assets (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    project_id      UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    sha256          VARCHAR(64)  NOT NULL,
    mime_type       VARCHAR(50)  NOT NULL,
    width           INT          NOT NULL,
    height          INT          NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    storage_path    TEXT         NOT NULL,
    created_by      UUID         REFERENCES users (id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- Unggahan ganda (mis. sinkron diulang) tidak menambah file.
    CONSTRAINT uq_project_assets_hash UNIQUE (project_id, sha256)
);

-- Batas penyimpanan dihitung per ruang kerja.
CREATE INDEX idx_project_assets_org ON project_assets (organization_id);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_thumbnail FOREIGN KEY (thumbnail_asset_id)
        REFERENCES project_assets (id) ON DELETE SET NULL;

-- Kunci edit: satu website hanya diedit satu orang dalam satu waktu (K17).
CREATE TABLE project_locks (
    project_id  UUID         PRIMARY KEY REFERENCES projects (id) ON DELETE CASCADE,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    device_id   VARCHAR(64)  NOT NULL,
    acquired_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL
);

-- Isi yang tergeser (kunci hilang, diambil alih, konflik, pulihkan) disimpan agar tidak ada kerja yang hilang.
CREATE TABLE project_content_backups (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    content    JSONB        NOT NULL,
    reason     VARCHAR(20)  NOT NULL,
    created_by UUID         REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_project_content_backups_reason CHECK (reason IN ('lock_lost', 'takeover', 'restore', 'conflict'))
);

CREATE INDEX idx_project_content_backups_project ON project_content_backups (project_id, created_at DESC);
