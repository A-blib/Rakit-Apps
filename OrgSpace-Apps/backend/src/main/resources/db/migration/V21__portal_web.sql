-- Fondasi data untuk portal web OrgSpace (docs/rancangan/portal-web.md): kritik & saran (feedback), pusat
-- dokumentasi, dan monitoring (pengguna aktif harian + lalu lintas API). Web hanya klien REST API ini.

-- ---------- Kritik & saran ----------
-- Satu feedback = satu percakapan antara pengirim (pembuat website / provider) dan tim internal.
CREATE TABLE feedback (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    -- Mode pengirim saat mengirim (bukan peran sekarang), agar laporan per sisi tetap benar walau user berganti mode.
    sender_role     VARCHAR(20)   NOT NULL,
    organization_id UUID          REFERENCES organizations (id) ON DELETE SET NULL,
    category        VARCHAR(20)   NOT NULL,
    subject         VARCHAR(150)  NOT NULL,
    message         VARCHAR(4000) NOT NULL,
    rating          SMALLINT,
    status          VARCHAR(20)   NOT NULL DEFAULT 'new',
    source          VARCHAR(20)   NOT NULL DEFAULT 'android',
    app_version     VARCHAR(30),
    device_info     VARCHAR(150),
    -- Ada balasan tim yang belum dibaca pengirim / ada pesan pengirim yang belum dibaca tim.
    user_unread     BOOLEAN       NOT NULL DEFAULT false,
    staff_unread    BOOLEAN       NOT NULL DEFAULT true,
    last_message_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_feedback_sender_role CHECK (sender_role IN ('creator', 'provider')),
    CONSTRAINT chk_feedback_category CHECK (category IN ('bug', 'saran', 'pertanyaan', 'keluhan', 'lainnya')),
    CONSTRAINT chk_feedback_status CHECK (status IN ('new', 'in_progress', 'resolved', 'closed')),
    CONSTRAINT chk_feedback_source CHECK (source IN ('android', 'web')),
    CONSTRAINT chk_feedback_rating CHECK (rating IS NULL OR rating BETWEEN 1 AND 5)
);

CREATE INDEX ix_feedback_status_last ON feedback (status, last_message_at DESC);
CREATE INDEX ix_feedback_user_last ON feedback (user_id, last_message_at DESC);

-- Pesan lanjutan dalam satu feedback (balasan tim atau tanggapan pengirim). Pesan pertama ada di feedback.message.
CREATE TABLE feedback_messages (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    feedback_id UUID          NOT NULL REFERENCES feedback (id) ON DELETE CASCADE,
    -- SET NULL: balasan tetap ada walau akun staf dihapus.
    author_id   UUID          REFERENCES users (id) ON DELETE SET NULL,
    from_staff  BOOLEAN       NOT NULL,
    body        VARCHAR(4000) NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX ix_feedback_messages_feedback ON feedback_messages (feedback_id, created_at);

-- ---------- Pusat dokumentasi ----------
-- Artikel panduan untuk halaman Docs di web (dan nanti di app). Isi berformat Markdown.
CREATE TABLE doc_articles (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    slug         VARCHAR(120) NOT NULL UNIQUE,
    category     VARCHAR(30)  NOT NULL,
    audience     VARCHAR(20)  NOT NULL DEFAULT 'all',
    title        VARCHAR(150) NOT NULL,
    summary      VARCHAR(300),
    body         TEXT         NOT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    status       VARCHAR(20)  NOT NULL DEFAULT 'draft',
    published_at TIMESTAMPTZ,
    created_by   UUID         REFERENCES users (id) ON DELETE SET NULL,
    updated_by   UUID         REFERENCES users (id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_doc_articles_slug CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT chk_doc_articles_category CHECK (category IN
        ('mulai', 'ruang-kerja', 'website', 'template', 'terbit', 'provider', 'akun', 'faq')),
    CONSTRAINT chk_doc_articles_audience CHECK (audience IN ('all', 'creator', 'provider')),
    CONSTRAINT chk_doc_articles_status CHECK (status IN ('draft', 'published')),
    CONSTRAINT chk_doc_articles_published CHECK (status = 'draft' OR published_at IS NOT NULL)
);

CREATE INDEX ix_doc_articles_published ON doc_articles (category, sort_order) WHERE status = 'published';

-- ---------- Monitoring ----------
-- Pengguna aktif: satu baris per user per hari per platform. Ditulis berkelompok dari memori (UsageRecorder),
-- bukan satu INSERT per request. "day" memakai zona waktu app (APP_TIMEZONE).
CREATE TABLE user_daily_activity (
    user_id       UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    day           DATE        NOT NULL,
    platform      VARCHAR(20) NOT NULL,
    request_count INT         NOT NULL DEFAULT 0,
    first_seen_at TIMESTAMPTZ NOT NULL,
    last_seen_at  TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, day, platform),
    CONSTRAINT chk_user_daily_activity_platform CHECK (platform IN ('android', 'web', 'other'))
);

CREATE INDEX ix_user_daily_activity_day ON user_daily_activity (day);

-- Lalu lintas API per hari per endpoint. route = pola Spring (mis. /api/projects/{id}), jadi ID tidak membuat
-- baris baru dan tidak ada data pribadi di sini.
CREATE TABLE api_traffic_daily (
    day           DATE         NOT NULL,
    method        VARCHAR(10)  NOT NULL,
    route         VARCHAR(200) NOT NULL,
    request_count BIGINT       NOT NULL DEFAULT 0,
    -- Respons 5xx (kesalahan server). 4xx dihitung terpisah karena biasanya kesalahan input pengguna.
    error_count   BIGINT       NOT NULL DEFAULT 0,
    client_error_count BIGINT  NOT NULL DEFAULT 0,
    total_ms      BIGINT       NOT NULL DEFAULT 0,
    max_ms        INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (day, method, route)
);

-- Statistik pendaftar per hari dihitung dari users.created_at.
CREATE INDEX IF NOT EXISTS ix_users_created_at ON users (created_at);

-- ---------- Artikel awal pusat dokumentasi ----------
INSERT INTO doc_articles (slug, category, audience, title, summary, body, sort_order, status, published_at) VALUES
('apa-itu-orgspace', 'mulai', 'all', 'Apa itu OrgSpace?',
 'OrgSpace membantu organisasi membuat website sendiri dari HP, tanpa coding.',
 '## OrgSpace dalam satu menit

OrgSpace adalah app Android untuk membuat website organisasi (OSIS, BEM, UKM, komunitas, UMKM) **tanpa menulis kode**.

- Pilih template buatan kreator, lalu ganti teks, foto, dan warnanya.
- Kerjakan bersama pengurus lain di satu **ruang kerja**.
- Terbitkan website dengan alamat sendiri, atau unduh ZIP-nya.

## Siapa yang memakai OrgSpace?

| Peran | Yang dilakukan |
|---|---|
| Pembuat website | Membuat dan mengelola website organisasi |
| Penyedia template | Mengunggah template untuk dipakai organisasi lain |
', 10, 'published', now()),

('memulai-dari-hp', 'mulai', 'all', 'Memulai dari HP',
 'Pasang app, masuk, lalu buat atau gabung ruang kerja organisasi.',
 '## Langkah awal

1. Pasang app OrgSpace di HP Android (Android 8.0 ke atas).
2. Kamu bisa melihat-lihat galeri template sebagai **tamu**.
3. Untuk membuat website, ketuk **Masuk** dan pilih Google, GitHub, atau email.
4. Buat ruang kerja organisasi baru, atau masukkan **kode undangan** dari pengurus.

> Tips: satu akun bisa menjadi pembuat website sekaligus penyedia template. Pindah mode dari menu Profil.
', 20, 'published', now()),

('ruang-kerja-dan-peran', 'ruang-kerja', 'creator', 'Ruang kerja & peran anggota',
 'Pemilik, Admin, dan Editor punya hak yang berbeda.',
 '## Ruang kerja

Ruang kerja adalah tempat semua website milik satu organisasi. Ada juga **ruang pribadi** untuk website pribadi.

## Peran

| Peran | Hak |
|---|---|
| Pemilik | Semua hak, termasuk menghapus ruang kerja dan menyerahkan kepemilikan |
| Admin | Mengundang anggota, menerbitkan website, mengambil alih editor |
| Editor | Mengedit isi website dan meminta website diterbitkan |

Undang anggota dari **Profil → Anggota → Undang**. Kode undangan berlaku 7 hari.
', 10, 'published', now()),

('membuat-website-dari-template', 'website', 'creator', 'Membuat website dari template',
 'Pilih template, isi teks & foto, lalu cek kelengkapan.',
 '## Langkah

1. Buka tab **Template**, pilih template yang cocok untuk organisasimu.
2. Ketuk **Pakai template**. Nama organisasi, logo, dan media sosial langsung terisi dari Profil organisasi.
3. Di editor, ketuk bagian pratinjau untuk mengubah teks atau foto.
4. Perubahan tersimpan otomatis di HP lalu dikirim ke cloud.
5. Buka menu ⋮ → **Cek kelengkapan** sebelum menerbitkan.

## Mengedit bersama

Hanya satu orang yang bisa mengedit satu website dalam satu waktu. Anggota lain melihat banner
"Sedang diedit oleh ..." dan isinya ikut diperbarui otomatis.
', 10, 'published', now()),

('menjadi-penyedia-template', 'provider', 'provider', 'Menjadi penyedia template',
 'Unggah template HTML/CSS/JS statis dan tandai bagian yang boleh diedit.',
 '## Syarat template

- HTML, CSS, dan JavaScript biasa (bukan hasil build React/Vue/Svelte).
- Ada `index.html` di akar ZIP, ukuran ZIP maksimal 20 MB.
- Library dari CDN hanya yang ada di daftar resmi.

## Alur upload

1. Aktifkan mode provider dari menu Profil.
2. Buka tab **Upload**, pilih ZIP template.
3. Server memeriksa template; perbaiki semua Error yang muncul.
4. **Tandai** teks, gambar, dan link yang boleh diedit pembuat website.
5. **Coba** template, lalu **Kirim**.
', 10, 'published', now()),

('kirim-kritik-dan-saran', 'faq', 'all', 'Mengirim kritik & saran',
 'Laporkan masalah atau beri ide langsung dari app.',
 '## Dari app

Buka **Profil → Kritik & saran** (menu ini hadir di versi app berikutnya), pilih jenisnya (bug, saran, pertanyaan, keluhan), lalu tulis pesanmu.
Tim OrgSpace akan membalas di percakapan yang sama, dan kamu mendapat tanda pesan baru.

## Agar cepat ditangani

- Tulis langkah yang kamu lakukan sebelum masalah muncul.
- Sebutkan nama website atau template yang bermasalah.
', 10, 'published', now());
