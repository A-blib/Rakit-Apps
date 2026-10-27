-- Template unggulan yang dipajang di landing page portal web, dipilih tim internal dari galeri. Daftar disimpan
-- utuh lewat PUT /api/admin/landing/templates (maks 12, dicek di LandingTemplateService); urutan = sort_order.
CREATE TABLE landing_templates (
    template_id uuid        PRIMARY KEY REFERENCES templates (id) ON DELETE CASCADE,
    sort_order  int         NOT NULL,
    added_by    uuid        REFERENCES users (id) ON DELETE SET NULL,
    added_at    timestamptz NOT NULL DEFAULT now()
);
