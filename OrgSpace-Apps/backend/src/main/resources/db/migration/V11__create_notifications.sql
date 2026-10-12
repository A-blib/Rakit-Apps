-- Notifikasi di dalam app untuk provider (bagian 4.6). Push notification menyusul bersama fitur Upload.
CREATE TABLE notifications (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type        VARCHAR(40)  NOT NULL,
    template_id UUID         REFERENCES templates (id) ON DELETE CASCADE,
    title       VARCHAR(150) NOT NULL,
    body        VARCHAR(500) NOT NULL,
    read_at     TIMESTAMPTZ,
    -- Diisi saat masalahnya beres (mis. versi perbaikan lolos), agar notifikasi tidak lagi dianggap "perlu tindakan".
    resolved_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_notifications_type CHECK (type IN
        ('TEMPLATE_CHECK_FAILED', 'TEMPLATE_CHECK_WARNING', 'TEMPLATE_PUBLISHED', 'TEMPLATE_DISABLED'))
);

CREATE INDEX ix_notifications_user_created ON notifications (user_id, created_at DESC);
