-- Info terbaru OrgSpace (fitur baru, optimasi, desain ulang) untuk portal web. Ditulis tim internal di dashboard;
-- landing page menampilkan ringkasannya (judul, ringkasan, gambar), halaman /info/{id} isi lengkapnya (Markdown).
CREATE TABLE portal_updates (
    id               uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    category         varchar(20)  NOT NULL CHECK (category IN ('fitur', 'optimasi', 'desain', 'lainnya')),
    title            varchar(120) NOT NULL,
    summary          varchar(300) NOT NULL,
    content          text         NOT NULL,
    status           varchar(20)  NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'published')),
    image_alt        varchar(200),
    -- null = tanpa gambar; terisi = gambar unggahan, sekaligus penanda versi untuk cache (?v=).
    image_updated_at timestamptz,
    -- Diisi saat pertama kali tayang dan tidak berubah lagi; landing mengurutkan dari yang terbaru.
    published_at     timestamptz,
    created_by       uuid         REFERENCES users (id) ON DELETE SET NULL,
    updated_by       uuid         REFERENCES users (id) ON DELETE SET NULL,
    created_at       timestamptz  NOT NULL DEFAULT now(),
    updated_at       timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX portal_updates_published_idx ON portal_updates (published_at DESC) WHERE status = 'published';
