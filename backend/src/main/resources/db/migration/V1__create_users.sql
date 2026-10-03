-- Satu baris = satu akun, apa pun metode login yang dipakai (lihat user_identities).
CREATE TABLE users (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name         VARCHAR(100) NOT NULL,
    email                VARCHAR(255),
    avatar_url           VARCHAR(500),
    is_admin             BOOLEAN      NOT NULL DEFAULT FALSE,
    active_mode          VARCHAR(20)  NOT NULL DEFAULT 'creator',
    onboarding_completed BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_active_mode CHECK (active_mode IN ('creator', 'provider'))
);

-- Unik tanpa membedakan huruf besar/kecil: "Aris@Mail.com" dan "aris@mail.com" dianggap sama.
-- Email boleh kosong karena akun GitHub bisa tidak punya email publik.
CREATE UNIQUE INDEX ux_users_email_lower ON users (lower(email));
