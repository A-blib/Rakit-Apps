-- Data mode pembuat website. user_id sekaligus primary key karena satu user maksimal satu profil.
CREATE TABLE creator_profiles (
    user_id           UUID         PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    website_purpose   VARCHAR(20),
    organization_name VARCHAR(150),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_creator_profiles_website_purpose
        CHECK (website_purpose IN ('sekolah', 'organisasi', 'umkm', 'instansi', 'pribadi', 'lainnya'))
);
