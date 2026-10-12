-- Kejadian "dilihat" (detail template dibuka) dan "didownload" (website dari template diexport), bagian 3.5–3.6.
CREATE TABLE template_events (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID        NOT NULL REFERENCES templates (id) ON DELETE CASCADE,
    type        VARCHAR(10) NOT NULL,
    -- Hanya untuk download: satu project dihitung sekali (export ulang tidak menambah angka).
    project_id  VARCHAR(64),
    -- ID acak per instalasi app, agar tamu tetap terhitung tanpa data pribadi.
    install_id  VARCHAR(64) NOT NULL,
    user_id     UUID        REFERENCES users (id) ON DELETE SET NULL,
    -- Waktu kejadian di HP (export bisa terjadi offline lalu dikirim belakangan).
    occurred_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_template_events_type CHECK (type IN ('view', 'download')),
    CONSTRAINT chk_template_events_project CHECK (type <> 'download' OR project_id IS NOT NULL)
);

-- Event ganda (project + jenis sama) ditolak database, jadi angka tidak bisa terhitung dua kali.
CREATE UNIQUE INDEX ux_template_events_project_type ON template_events (project_id, type) WHERE project_id IS NOT NULL;
-- Untuk ringkasan & grafik per template per periode.
CREATE INDEX ix_template_events_template_type_time ON template_events (template_id, type, occurred_at);
