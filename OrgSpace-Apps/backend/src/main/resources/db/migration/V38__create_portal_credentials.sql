-- Kredensial Portal Pengurus per organisasi: satu username + password untuk tiap peran (owner wajib, admin & editor
-- opsional), berlaku di Portal semua website organisasi itu. Menggantikan login Portal dengan akun OrgSpace.
CREATE TABLE portal_credentials (
    organization_id UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    role            VARCHAR(10)  NOT NULL CHECK (role IN ('owner', 'admin', 'editor')),
    -- Disimpan huruf kecil; login mencari peran dari username ini.
    username        VARCHAR(40)  NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    -- Naik setiap kali disimpan ulang, sehingga token Portal lama peran ini langsung tidak berlaku.
    version         INT          NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (organization_id, role)
);

-- Username harus berbeda antarperan dalam satu organisasi, agar satu username hanya menunjuk satu peran.
CREATE UNIQUE INDEX uq_portal_credentials_username ON portal_credentials (organization_id, username);
