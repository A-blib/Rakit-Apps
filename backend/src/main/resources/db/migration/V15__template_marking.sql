-- Data tandaan provider (alur-fitur-upload.md bagian 7 & 11): halaman, section, isian, tema global.
-- Disimpan utuh sebagai JSON karena bentuk finalnya baru ditetapkan bersama format project JSON (bagian 11).
ALTER TABLE templates
    ADD COLUMN marking             JSONB,
    -- Jumlah isian, disimpan terpisah agar kartu draft & syarat "minimal 3 isian" tidak perlu membaca JSON.
    ADD COLUMN marking_field_count INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_templates_marking_field_count CHECK (marking_field_count >= 0);
