-- Pilihan thumbnail di langkah Info template (alur-fitur-upload.md bagian 6.2), disimpan agar draft yang
-- dilanjutkan menampilkan pilihan yang sama.
ALTER TABLE templates
    ADD COLUMN thumbnail_source VARCHAR(10),
    ADD COLUMN thumbnail_view   VARCHAR(10),
    ADD CONSTRAINT chk_templates_thumbnail_source CHECK (thumbnail_source IN ('auto', 'section', 'custom')),
    ADD CONSTRAINT chk_templates_thumbnail_view CHECK (thumbnail_view IN ('mobile', 'desktop'));
