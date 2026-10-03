-- Yang disimpan hanya hash SHA-256 (64 karakter hex), jadi token asli tidak bisa dibaca dari database.
CREATE TABLE refresh_tokens (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL,
    device_name VARCHAR(100),
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked_at  TIMESTAMPTZ,
    -- Diisi saat token dirotasi; dipakai untuk mendeteksi token lama yang dipakai ulang.
    replaced_by UUID,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash)
);

-- Untuk mencabut semua token milik satu user sekaligus.
CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id);
