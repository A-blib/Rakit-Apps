-- Folder di "Template Anda" (ala Google Drive): provider mengelompokkan template miliknya sendiri.
-- Satu template berada di paling banyak satu folder; template tanpa folder tampil di halaman utama.
CREATE TABLE template_folders (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name        VARCHAR(60) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Nama folder unik per provider tanpa membedakan huruf besar/kecil ("Sekolah" = "sekolah").
CREATE UNIQUE INDEX uq_template_folders_provider_name ON template_folders (provider_id, lower(name));

-- Folder dihapus → template di dalamnya kembali ke halaman utama, tidak ikut terhapus.
ALTER TABLE templates ADD COLUMN folder_id UUID REFERENCES template_folders (id) ON DELETE SET NULL;
CREATE INDEX ix_templates_folder_id ON templates (folder_id);
