-- Isi hero landing page portal web (orgspace.com) yang diubah tim internal dari dashboard. Selalu tepat satu baris
-- (id = 1), jadi tidak ada endpoint buat/hapus; nilai awal = teks yang dulu ditulis langsung di halaman.
CREATE TABLE landing_hero (
    id                  smallint     PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    headline            varchar(120) NOT NULL,
    headline_highlight  varchar(80)  NOT NULL,
    subheadline         varchar(300) NOT NULL,
    primary_cta_label   varchar(60)  NOT NULL,
    secondary_cta_label varchar(60)  NOT NULL,
    secondary_cta_url   varchar(300) NOT NULL,
    -- Poin singkat di bawah tombol, dipisah baris baru (maks 3, dicek di LandingHeroRequest).
    highlights          text         NOT NULL,
    image_alt           varchar(200) NOT NULL,
    -- null = web memakai gambar bawaannya sendiri; terisi = gambar unggahan, sekaligus penanda versi untuk cache.
    image_updated_at    timestamptz,
    updated_by          uuid         REFERENCES users (id) ON DELETE SET NULL,
    updated_at          timestamptz  NOT NULL DEFAULT now()
);

INSERT INTO landing_hero (headline, headline_highlight, subheadline, primary_cta_label, secondary_cta_label,
                          secondary_cta_url, highlights, image_alt)
VALUES ('Bikin Website Impian,', 'Tanpa Coding!',
        'Dari UMKM, organisasi, hingga portofolio pribadi—rakit website-mu dalam hitungan menit dengan mudah, cepat, dan profesional.',
        'Mulai Rakit Sekarang – Gratis!', 'Lihat Demo', '/docs',
        E'Tanpa Kartu Kredit\nSiap Online 5 Menit\nHosting & SSL Otomatis',
        'Mockup Workspace Rakit Website di Meja Kerja dengan Laptop dan Sticky Notes');
