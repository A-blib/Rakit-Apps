-- Template milik penyedia template (docs/rancangan/alur-provider.md bagian 4.3 & 6.6).
-- Versi ini belum ada Upload, jadi baris hanya berasal dari test & seeder demo.
CREATE TABLE templates (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id   UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name          VARCHAR(100) NOT NULL,
    -- Kategori sama dengan pilihan tujuan website di onboarding pembuat website.
    category      VARCHAR(20)  NOT NULL,
    thumbnail_url VARCHAR(500),
    status        VARCHAR(20)  NOT NULL DEFAULT 'draft',
    -- Jumlah peringatan dari pengecekan terakhir; disimpan agar daftar & filter "Perlu perbaikan" cepat.
    warning_count INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at  TIMESTAMPTZ,
    CONSTRAINT chk_templates_category
        CHECK (category IN ('sekolah', 'organisasi', 'umkm', 'instansi', 'pribadi', 'lainnya')),
    CONSTRAINT chk_templates_status
        CHECK (status IN ('draft', 'checking', 'published', 'check_failed', 'disabled')),
    CONSTRAINT chk_templates_warning_count CHECK (warning_count >= 0)
);

CREATE INDEX ix_templates_provider_id ON templates (provider_id);
