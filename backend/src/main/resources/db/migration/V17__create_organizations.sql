-- Ruang kerja OrgSpace (revisi-fondasi-orgspace.md bagian 3–4): organisasi dan ruang pribadi,
-- anggota + peran, kode undangan, dan riwayat aktivitas. Website nanti dimiliki ruang kerja, bukan user.

CREATE TABLE organizations (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    type              VARCHAR(20)  NOT NULL,
    name              VARCHAR(100) NOT NULL,
    short_name        VARCHAR(40),
    category          VARCHAR(30),
    level             VARCHAR(20),
    institution_name  VARCHAR(150),
    city              VARCHAR(100),
    period_start_year SMALLINT,
    period_end_year   SMALLINT,
    logo_url          TEXT,
    address           VARCHAR(255),
    email             VARCHAR(255),
    whatsapp          VARCHAR(20),
    instagram         VARCHAR(100),
    tiktok            VARCHAR(100),
    youtube           VARCHAR(100),
    created_by        UUID         NOT NULL REFERENCES users (id),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_organizations_type CHECK (type IN ('organization', 'personal')),
    CONSTRAINT chk_organizations_category CHECK (category IN (
        'osis_mpk', 'bem_dema_dpm', 'himpunan', 'ukm', 'pramuka', 'jurnalistik_media',
        'pmr_ksr', 'paskibra', 'kerohanian', 'seni_olahraga', 'lainnya')),
    CONSTRAINT chk_organizations_level CHECK (level IN ('smp', 'sma', 'smk', 'perguruan_tinggi')),
    CONSTRAINT chk_organizations_period CHECK (
        period_start_year IS NULL OR period_end_year IS NULL OR period_end_year >= period_start_year),
    -- Ruang pribadi tidak punya kategori, jenjang, dan instansi.
    CONSTRAINT chk_organizations_personal_fields CHECK (
        type = 'organization' OR (category IS NULL AND level IS NULL AND institution_name IS NULL))
);

-- Satu user paling banyak punya satu ruang pribadi.
CREATE UNIQUE INDEX uq_organizations_personal_owner ON organizations (created_by) WHERE type = 'personal';

CREATE TABLE organization_members (
    organization_id UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    user_id         UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role            VARCHAR(10) NOT NULL,
    position_title  VARCHAR(60),
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (organization_id, user_id),
    CONSTRAINT chk_organization_members_role CHECK (role IN ('owner', 'admin', 'editor'))
);

-- Daftar ruang kerja milik satu user (GET /api/organizations) dicari lewat user_id.
CREATE INDEX idx_organization_members_user ON organization_members (user_id);

CREATE TABLE organization_invitations (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    -- Kode asli tidak disimpan; hanya hash SHA-256-nya (pola sama dengan auth_tickets).
    code_hash       VARCHAR(64) NOT NULL UNIQUE,
    role            VARCHAR(10) NOT NULL,
    created_by      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_organization_invitations_role CHECK (role IN ('admin', 'editor'))
);

CREATE INDEX idx_organization_invitations_org ON organization_invitations (organization_id);

CREATE TABLE activity_logs (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    -- Null untuk aksi sistem, atau jika akun pelakunya sudah dihapus.
    actor_user_id   UUID        REFERENCES users (id) ON DELETE SET NULL,
    action          VARCHAR(50) NOT NULL,
    target_type     VARCHAR(30),
    target_id       UUID,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_logs_org_created ON activity_logs (organization_id, created_at DESC);

-- Ruang kerja terakhir yang dibuka, agar app membukanya lagi di HP mana pun.
ALTER TABLE users
    ADD COLUMN last_organization_id UUID REFERENCES organizations (id) ON DELETE SET NULL;

-- Tujuan website & nama organisasi pindah ke tabel organizations. Tabel creator_profiles tetap ada
-- sebagai penanda peran pembuat website. (Penghapusan kolom disetujui Aris, 12 Okt 2026.)
ALTER TABLE creator_profiles
    DROP CONSTRAINT chk_creator_profiles_website_purpose,
    DROP COLUMN website_purpose,
    DROP COLUMN organization_name;
