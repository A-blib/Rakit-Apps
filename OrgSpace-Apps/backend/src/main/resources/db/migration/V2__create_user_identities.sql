-- Cara user masuk. Satu user bisa punya beberapa identitas (local, google, github),
-- tetapi maksimal satu per provider.
CREATE TABLE user_identities (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider         VARCHAR(20)  NOT NULL,
    -- ID user di sisi provider. Untuk 'local' diisi email huruf kecil.
    provider_user_id VARCHAR(255) NOT NULL,
    email            VARCHAR(255),
    email_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    password_hash    VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_user_identities_provider CHECK (provider IN ('local', 'google', 'github')),
    -- Hanya identitas 'local' yang punya password, dan identitas 'local' wajib punya password.
    CONSTRAINT chk_user_identities_password CHECK ((provider = 'local') = (password_hash IS NOT NULL)),
    CONSTRAINT uq_user_identities_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT uq_user_identities_user_provider UNIQUE (user_id, provider)
);
