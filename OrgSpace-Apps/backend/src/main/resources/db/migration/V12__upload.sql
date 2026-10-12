-- Fitur Upload provider (docs/rancangan/alur-fitur-upload.md).

-- 1. Template hasil upload.
-- Kategori boleh kosong: upload yang baru lolos/gagal pengecekan belum mengisi Info template (langkah 3).
-- Template yang tayang selalu punya kategori karena Info template wajib sebelum Kirim.
ALTER TABLE templates ALTER COLUMN category DROP NOT NULL;

ALTER TABLE templates
    ADD COLUMN description         VARCHAR(300),
    -- Kata kunci untuk pencarian galeri (bagian 6.1): 1–5 kata, huruf kecil.
    ADD COLUMN keywords            TEXT[]       NOT NULL DEFAULT '{}',
    -- Info teknis otomatis dari pengecekan (jumlah halaman, library, ukuran, responsif, variabel CSS).
    ADD COLUMN tech_info           JSONB,
    -- Nama & ukuran ZIP terakhir; dipakai kartu "Perlu diperbaiki" sebelum template punya nama sendiri.
    ADD COLUMN source_file_name    VARCHAR(255),
    ADD COLUMN source_size         BIGINT,
    -- Langkah wizard terakhir (1–6), agar "Lanjutkan draft" membuka tepat di langkah itu.
    ADD COLUMN wizard_step         INTEGER      NOT NULL DEFAULT 1,
    -- Kapan pemberitahuan "draft akan dihapus" dikirim; dikosongkan lagi saat draft disentuh.
    ADD COLUMN expiry_notified_at  TIMESTAMPTZ,
    ADD CONSTRAINT chk_templates_wizard_step CHECK (wizard_step BETWEEN 1 AND 6);

-- 2. Tahap pengecekan yang sedang berjalan, untuk layar "daftar tahap yang dicentang" (bagian 5.8).
ALTER TABLE template_checks
    ADD COLUMN stage VARCHAR(20),
    ADD CONSTRAINT chk_template_checks_stage
        CHECK (stage IN ('uploaded', 'opening_zip', 'structure', 'html_library', 'size', 'done'));

-- 3. Setiap masalah mencatat versi aturan (bagian 5.7) dan potongan kode yang tertangkap (untuk laporan keliru).
ALTER TABLE template_check_issues
    ADD COLUMN rule_version INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN snippet      VARCHAR(300);

-- 4. Sesi upload per potongan (bagian 5.7b): jika sinyal putus, app menanyakan received_size lalu melanjutkan.
CREATE TABLE upload_sessions (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id   UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    -- Diisi jika ini "Upload file perbaikan" untuk template yang gagal pengecekan (ZIP lama diganti).
    template_id   UUID         REFERENCES templates (id) ON DELETE CASCADE,
    file_name     VARCHAR(255) NOT NULL,
    total_size    BIGINT       NOT NULL,
    received_size BIGINT       NOT NULL DEFAULT 0,
    status        VARCHAR(20)  NOT NULL DEFAULT 'uploading',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_upload_sessions_status CHECK (status IN ('uploading', 'completed')),
    CONSTRAINT chk_upload_sessions_size CHECK (total_size > 0 AND received_size BETWEEN 0 AND total_size)
);

CREATE INDEX ix_upload_sessions_provider ON upload_sessions (provider_id);

-- 5. Laporan "Ini keliru? Laporkan" (bagian 5.9). Dibaca langsung lewat database (belum ada panel admin).
CREATE TABLE check_reports (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id  UUID         NOT NULL REFERENCES templates (id) ON DELETE CASCADE,
    check_id     UUID         NOT NULL REFERENCES template_checks (id) ON DELETE CASCADE,
    -- Satu laporan per Error per upload, agar tidak bisa dibanjiri.
    issue_id     UUID         NOT NULL UNIQUE REFERENCES template_check_issues (id) ON DELETE CASCADE,
    rule_code    VARCHAR(50)  NOT NULL,
    rule_version INTEGER      NOT NULL,
    location     VARCHAR(300),
    snippet      VARCHAR(300),
    reason       VARCHAR(500),
    provider_id  UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'baru',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_check_reports_status
        CHECK (status IN ('baru', 'dibaca', 'aturan_diperbaiki', 'tidak_berubah'))
);

CREATE INDEX ix_check_reports_rule ON check_reports (rule_code, status);

-- 6. Jenis notifikasi baru: hasil pengecekan lolos dan pengingat/penghapusan draft (alur-provider.md 5.4).
ALTER TABLE notifications DROP CONSTRAINT chk_notifications_type;
ALTER TABLE notifications ADD CONSTRAINT chk_notifications_type CHECK (type IN
    ('TEMPLATE_CHECK_FAILED', 'TEMPLATE_CHECK_WARNING', 'TEMPLATE_PUBLISHED', 'TEMPLATE_DISABLED',
     'UPLOAD_CHECK_PASSED', 'DRAFT_EXPIRING', 'DRAFT_DELETED'));
