-- Tiket sekali pakai berumur pendek: state OAuth GitHub, hasil login GitHub, dan penyambungan akun.
CREATE TABLE auth_tickets (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    type       VARCHAR(20) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    user_id    UUID        REFERENCES users (id) ON DELETE CASCADE,
    payload    JSONB,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_auth_tickets_type CHECK (type IN ('GITHUB_STATE', 'LOGIN_RESULT', 'LINK')),
    CONSTRAINT uq_auth_tickets_token_hash UNIQUE (token_hash)
);
