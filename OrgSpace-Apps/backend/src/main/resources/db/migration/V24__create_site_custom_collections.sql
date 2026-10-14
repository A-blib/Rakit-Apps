-- Penyimpanan data modul koleksi kustom dinamis (mis. prestasi, agenda, proker, galeri, dsb.)
CREATE TABLE site_custom_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id         UUID NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    collection_name VARCHAR(60) NOT NULL,
    data            JSONB NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_site_custom_items_collection ON site_custom_items (site_id, collection_name, sort_order ASC, created_at DESC);
