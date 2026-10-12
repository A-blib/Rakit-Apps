-- Data mode penyedia template. Hanya profil & status verifikasi; belum ada tabel template.
CREATE TABLE provider_profiles (
    user_id          UUID         PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    creator_name     VARCHAR(100) NOT NULL,
    bio              VARCHAR(300),
    portfolio_url    VARCHAR(500),
    specialties      TEXT[]       NOT NULL DEFAULT '{}',
    status           VARCHAR(20)  NOT NULL DEFAULT 'pending',
    rejection_reason VARCHAR(300),
    agreed_terms_at  TIMESTAMPTZ  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_provider_profiles_status
        CHECK (status IN ('pending', 'approved', 'rejected', 'suspended'))
);
