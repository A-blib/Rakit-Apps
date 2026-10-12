-- Paket template yang diunduh HP pembuat website (alur-buat-website-via-template.md bagian 5 & 12).
-- Versi paket selalu 1 sampai fitur "versi template" dibahas (bagian 4.2); kolom disiapkan agar HP bisa
-- membedakan paket lama dan baru nanti tanpa mengubah format.
ALTER TABLE templates
    ADD COLUMN package_version INTEGER NOT NULL DEFAULT 1,
    -- Ukuran package.zip, untuk progres unduh dan peringatan data seluler. Null = template belum punya paket.
    ADD COLUMN package_size    BIGINT,
    ADD CONSTRAINT chk_templates_package_version CHECK (package_version >= 1),
    ADD CONSTRAINT chk_templates_package_size CHECK (package_size IS NULL OR package_size > 0);
