import type { DocDetail, DocTree } from "./api/types";

/**
 * Cadangan pusat dokumentasi OrgSpace, dipakai saat backend tidak bisa dihubungi atau artikelnya belum ada di server.
 * Isinya sama dengan migrasi backend V39__dokumentasi_lengkap_oktober.sql; versi yang tampil normalnya dari server
 * (bisa diedit admin di dashboard).
 */

export const FALLBACK_DOC_TREE: DocTree = {
  categories: [
    {
      value: "mulai",
      label: "Memulai",
      articles: [
        {"id": "doc-apa-itu-orgspace", "slug": "apa-itu-orgspace", "category": "mulai", "audience": "all", "title": "Apa itu OrgSpace?", "summary": "Platform pembuat website organisasi tanpa coding dari HP Android, lengkap dengan Portal Pengurus untuk mengisi data.", "sortOrder": 10, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-memulai-dari-hp", "slug": "memulai-dari-hp", "category": "mulai", "audience": "all", "title": "Memulai dari HP", "summary": "Pasang app, masuk, membuat atau bergabung ke ruang kerja, dan mengenal menu utama.", "sortOrder": 20, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
      ],
    },
    {
      value: "ruang-kerja",
      label: "Ruang kerja & anggota",
      articles: [
        {"id": "doc-ruang-kerja-dan-peran", "slug": "ruang-kerja-dan-peran", "category": "ruang-kerja", "audience": "creator", "title": "Ruang Kerja & Peran", "summary": "Jenis ruang kerja, hak Pemilik/Admin/Editor, mengundang anggota dengan kode, dan bergabung.", "sortOrder": 10, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-akses-portal-pengurus", "slug": "akses-portal-pengurus", "category": "ruang-kerja", "audience": "all", "title": "Akses Portal Pengurus", "summary": "Username & password Portal per peran (Pemilik, Admin, Editor): cara mengatur, mengganti, dan mencabut.", "sortOrder": 20, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-pergantian-pengurus", "slug": "pergantian-pengurus", "category": "ruang-kerja", "audience": "creator", "title": "Pergantian Pengurus & Serah Terima", "summary": "Urutan aman saat kepengurusan berganti: undang pengurus baru, serah terima, dan ganti akses Portal.", "sortOrder": 30, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
      ],
    },
    {
      value: "website",
      label: "Membuat website",
      articles: [
        {"id": "doc-membuat-website-dari-template", "slug": "membuat-website-dari-template", "category": "website", "audience": "creator", "title": "Mengedit Website dari Template", "summary": "Memilih template, bagian editor, jenis isian, tema, cek kelengkapan, dan menu editor.", "sortOrder": 10, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-kelola-website", "slug": "kelola-website", "category": "website", "audience": "creator", "title": "Kelola Website", "summary": "Daftar website, status, minta diterbitkan, ganti nama, duplikat, hapus, dan bagian halaman Kelola website.", "sortOrder": 20, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
      ],
    },
    {
      value: "terbit",
      label: "Menerbitkan website",
      articles: [
        {"id": "doc-cara-mengonlinekan-website", "slug": "cara-mengonlinekan-website", "category": "terbit", "audience": "creator", "title": "Meng-online-kan Website", "summary": "Publish Langsung ke orgspace.app/nama-website atau unduh ZIP, beserta hal yang perlu diketahui setelah terbit.", "sortOrder": 10, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-portal-pengurus", "slug": "portal-pengurus", "category": "terbit", "audience": "all", "title": "Menggunakan Portal Pengurus", "summary": "Mengisi berita, data modul, dan membaca isian formulir website tanpa terbit ulang.", "sortOrder": 20, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-domain-kustom", "slug": "domain-kustom", "category": "terbit", "audience": "creator", "title": "Memasang Domain Sendiri", "summary": "Menghubungkan domain seperti osis-sman1.sch.id: record DNS, verifikasi, www, dan masalah umum.", "sortOrder": 30, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
      ],
    },
    {
      value: "provider",
      label: "Penyedia template",
      articles: [
        {"id": "doc-menjadi-penyedia-template", "slug": "menjadi-penyedia-template", "category": "provider", "audience": "provider", "title": "Menjadi Penyedia Template", "summary": "Mengaktifkan mode kreator, menu kreator, enam langkah Upload, dan status template.", "sortOrder": 10, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-contoh-template-dan-paket", "slug": "contoh-template-dan-paket", "category": "provider", "audience": "provider", "title": "Contoh Template & Paket Unduhan", "summary": "Unduh paket contoh (template sederhana & lengkap, panduan, instruksi AI agent) dan coba dari upload sampai Portal.", "sortOrder": 15, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-requirement-project-dan-tools", "slug": "requirement-project-dan-tools", "category": "provider", "audience": "provider", "title": "Requirement Project & Aturan Pengecekan", "summary": "Bentuk project, batas ukuran, jenis file, library CDN, keamanan, dan daftar lengkap kode pengecekan upload.", "sortOrder": 20, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-modul-sistem-dan-kustom", "slug": "modul-sistem-dan-kustom", "category": "provider", "audience": "provider", "title": "Panduan Modul Dinamis", "summary": "Pengumuman, formulir, modul kustom, statistik, profil organisasi, Portal Pengurus, dan JavaScript template. Lengkap dengan paket contoh siap unduh.", "sortOrder": 30, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-membuat-fitur-kustom", "slug": "membuat-fitur-kustom", "category": "provider", "audience": "provider", "title": "Membuat Fitur Kustom", "summary": "Rancang fitur sendiri (agenda, galeri, filter, pencarian) dari modul + CSS + JavaScript, lengkap dengan resep dan batasnya.", "sortOrder": 40, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-menandai-bagian-template", "slug": "menandai-bagian-template", "category": "provider", "audience": "provider", "title": "Menandai Bagian Template", "summary": "Langkah Tandai: jenis isian, gaya, tema warna, hubungan ke profil organisasi, dan aturan elemen modul.", "sortOrder": 50, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-tampilan-portal-pengurus", "slug": "tampilan-portal-pengurus", "category": "provider", "audience": "provider", "title": "Mengatur Tampilan Portal Pengurus", "summary": "File portal.json opsional: warna, susunan menu, header banner, kartu/tabel, label field, dan menu HP untuk Portal Pengurus.", "sortOrder": 60, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
      ],
    },
    {
      value: "faq",
      label: "Tanya jawab",
      articles: [
        {"id": "doc-pertanyaan-umum", "slug": "pertanyaan-umum", "category": "faq", "audience": "all", "title": "Tanya Jawab Umum", "summary": "Solusi masalah yang sering terjadi: undangan, Portal Pengurus, terbit, domain, dan pengecekan template.", "sortOrder": 10, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
        {"id": "doc-kirim-kritik-dan-saran", "slug": "kirim-kritik-dan-saran", "category": "faq", "audience": "all", "title": "Mengirim Kritik & Saran", "summary": "Cara melaporkan masalah dan memberi masukan agar cepat ditangani.", "sortOrder": 20, "status": "published", "publishedAt": "2026-10-28T00:00:00Z", "updatedAt": "2026-10-28T00:00:00Z"},
      ],
    },
  ],
};

export const FALLBACK_DOC_ARTICLES: Record<string, DocDetail> = {
  "apa-itu-orgspace": {
    id: "doc-apa-itu-orgspace",
    slug: "apa-itu-orgspace",
    category: "mulai",
    audience: "all",
    title: "Apa itu OrgSpace?",
    summary: "Platform pembuat website organisasi tanpa coding dari HP Android, lengkap dengan Portal Pengurus untuk mengisi data.",
    sortOrder: 10,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Memulai",
    body: `## OrgSpace dalam satu menit

OrgSpace adalah app Android untuk membuat website organisasi (OSIS, BEM, himpunan, UKM, Pramuka, komunitas) **tanpa menulis kode**.

- Pilih template buatan kreator, lalu ganti teks, foto, dan warnanya.
- Kerjakan bersama pengurus lain di satu **ruang kerja** organisasi.
- Terbitkan website ke \`orgspace.app/nama-website\` secara gratis, atau pasang domain sendiri.
- Pengurus mengisi berita, agenda, prestasi, dan membaca isian formulir lewat **Portal Pengurus** (\`orgspace.app/nama-website/admin\`) tanpa menerbitkan ulang.

## Siapa yang memakai OrgSpace?

| Peran | Yang dilakukan | Di mana |
|---|---|---|
| Pembuat website | Memilih template, mengedit tampilan, menerbitkan | App (mode pembuat website) |
| Pengurus organisasi | Mengisi data website: berita, modul, formulir masuk | Portal Pengurus di browser |
| Penyedia template (kreator) | Membuat template HTML/CSS/JS, mengunggah, menandai bagian yang bisa diedit | Komputer + app (mode kreator) |

Satu akun bisa menjadi pembuat website sekaligus kreator template. Pindah mode lewat tab **Profil**.

## Peta dokumentasi

| Kamu ingin... | Baca |
|---|---|
| Mulai memakai app | [Memulai dari HP](/docs/memulai-dari-hp) |
| Mengajak pengurus lain | [Ruang kerja & peran](/docs/ruang-kerja-dan-peran) |
| Membuat dan mengedit website | [Mengedit website dari template](/docs/membuat-website-dari-template) |
| Menerbitkan website | [Meng-online-kan website](/docs/cara-mengonlinekan-website) |
| Mengisi data website | [Portal Pengurus](/docs/portal-pengurus) |
| Membuat template sendiri | [Menjadi penyedia template](/docs/menjadi-penyedia-template) |
`,
  },
  "memulai-dari-hp": {
    id: "doc-memulai-dari-hp",
    slug: "memulai-dari-hp",
    category: "mulai",
    audience: "all",
    title: "Memulai dari HP",
    summary: "Pasang app, masuk, membuat atau bergabung ke ruang kerja, dan mengenal menu utama.",
    sortOrder: 20,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Memulai",
    body: `## Langkah awal

1. Pasang app OrgSpace di HP Android (Android 8.0 ke atas).
2. Buka app. Kamu bisa melihat-lihat galeri template sebagai **tamu** tanpa akun.
3. Ketuk **Masuk**, lalu pilih **Google**, **GitHub**, atau **email + password**. Akun email baru dibuat lewat menu **Daftar**.
4. Pilih cara memulai ruang kerja:
   - **Buat organisasi**: isi nama, kategori (mis. OSIS / MPK, BEM, Pramuka), dan jenjang. Kamu menjadi **Pemilik**.
   - **Gabung organisasi**: masukkan kode undangan dari pengurus.
   - **Ruang pribadi**: untuk website pribadi atau portofolio.

## Menu bawah (mode pembuat website)

| Menu | Isi |
|---|---|
| Beranda | Ringkasan ruang kerja dan website terakhir |
| Website | Semua website di ruang kerja aktif: cari, filter status, kelola |
| **+** | Mulai website baru dari template |
| Template | Galeri template per kategori organisasi |
| Profil | Akun, ruang kerja, pindah mode, pengaturan |

Ruang kerja aktif tampil di kiri atas. Ketuk namanya untuk berganti ruang kerja, membuka **Kelola organisasi**, membuat organisasi baru, atau **Gabung organisasi**.

## Akun

- Akun Google, GitHub, dan email dengan email terverifikasi yang sama bisa disambungkan menjadi satu akun.
- Metode login yang tersambung dilihat di **Profil → Pengaturan → Metode login terhubung**.

> Tips: Portal Pengurus website **tidak** memakai akun ini. Pengurus masuk Portal dengan username + password per peran yang diatur Pemilik organisasi. Lihat [Akses Portal Pengurus](/docs/akses-portal-pengurus).
`,
  },
  "ruang-kerja-dan-peran": {
    id: "doc-ruang-kerja-dan-peran",
    slug: "ruang-kerja-dan-peran",
    category: "ruang-kerja",
    audience: "creator",
    title: "Ruang Kerja & Peran",
    summary: "Jenis ruang kerja, hak Pemilik/Admin/Editor, mengundang anggota dengan kode, dan bergabung.",
    sortOrder: 10,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Ruang kerja & anggota",
    body: `## Ruang kerja

Ruang kerja adalah tempat semua website milik satu organisasi beserta pengurusnya. Ada dua jenis:

| Jenis | Untuk | Anggota |
|---|---|---|
| **Organisasi** | OSIS, BEM, himpunan, UKM, komunitas | Sampai 30 anggota, lewat kode undangan |
| **Ruang pribadi** | Website pribadi atau portofolio | Hanya kamu (tidak bisa mengundang atau diserahterimakan) |

Kelola ruang kerja lewat nama ruang kerja di kiri atas → **Kelola organisasi**: Profil organisasi, Anggota, Riwayat aktivitas, Akses Portal website (Pemilik), dan Serah terima (Pemilik).

## Peran dan hak

| Aksi | Editor | Admin | Pemilik |
|---|:-:|:-:|:-:|
| Melihat website dan mengedit draft | ✓ | ✓ | ✓ |
| Meminta Admin menerbitkan website | ✓ | | |
| Menerbitkan website, mengatur domain | | ✓ | ✓ |
| Melihat isian formulir pengunjung | | ✓ | ✓ |
| Mengubah profil organisasi dan logo | | ✓ | ✓ |
| Mengundang anggota, mengubah peran, mengeluarkan anggota | | ✓ | ✓ |
| Mengatur akses Portal Pengurus | | | ✓ |
| Menghapus website, serah terima, menghapus organisasi | | | ✓ |

Admin tidak bisa mengubah atau mengeluarkan Pemilik.

## Mengundang anggota

1. **Kelola organisasi → Anggota → Undang anggota**.
2. Pilih peran: **Editor** atau **Admin**.
3. Ketuk **Buat kode undangan**, lalu bagikan kodenya (mis. \`OSIS-7K2P\`).

Aturan kode undangan:

- Satu kode hanya untuk **satu orang** dan berlaku **1 jam**. Buat kode baru untuk setiap orang.
- Undangan Admin atau Editor baru bisa dibuat setelah **username & password Portal peran itu diatur** (lihat [Akses Portal Pengurus](/docs/akses-portal-pengurus)).
- Kode yang belum dipakai bisa dicabut dari daftar undangan aktif.

## Bergabung dengan kode

1. Buat akun OrgSpace sendiri (Google, GitHub, atau email).
2. Ketuk nama ruang kerja di kiri atas → **Gabung organisasi**.
3. Masukkan kode → **Cek kode**. Pastikan nama organisasi dan peranmu benar.
4. Isi jabatan (opsional), lalu **Gabung sekarang**.

## Keluar dan menghapus

- **Keluar dari organisasi**: dari Kelola organisasi. Pemilik terakhir harus menyerahkan kepemilikan dulu.
- **Hapus organisasi** (Pemilik): ketik ulang nama organisasi. Anggota, undangan, dan riwayatnya ikut terhapus permanen.
- Semua perubahan penting tercatat di **Riwayat aktivitas**.
`,
  },
  "akses-portal-pengurus": {
    id: "doc-akses-portal-pengurus",
    slug: "akses-portal-pengurus",
    category: "ruang-kerja",
    audience: "all",
    title: "Akses Portal Pengurus",
    summary: "Username & password Portal per peran (Pemilik, Admin, Editor): cara mengatur, mengganti, dan mencabut.",
    sortOrder: 20,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Ruang kerja & anggota",
    body: `Portal Pengurus (\`orgspace.app/nama-website/admin\`) adalah dashboard di browser untuk mengisi data website. Login Portal **tidak memakai akun OrgSpace pribadi**, tetapi username + password **per peran** milik organisasi.

## Tiga akses Portal

| Akses | Wajib? | Bisa membuka |
|---|---|---|
| **Pemilik** | Wajib | Semua tab, termasuk Kotak Masuk Form |
| **Admin** | Opsional | Semua tab, termasuk Kotak Masuk Form |
| **Editor** | Opsional | Ringkasan, Berita & Agenda, Modul Kustom (tanpa Kotak Masuk Form) |

- Satu akses berlaku untuk **semua website** di organisasi itu, berapa pun jumlah admin dan editornya.
- Username adalah "kartu nama" peran: Portal mengenali peran dari username, lalu mencocokkan password **hanya** dengan peran itu. Username Editor + password Admin ditolak walau password Admin benar.
- Akses organisasi lain dan akun OrgSpace pribadi (email/Google/GitHub) **selalu ditolak**.

## Mengatur akses (Pemilik)

1. Di app: nama ruang kerja → **Kelola organisasi → Akses Portal website** (menu ini hanya tampil untuk Pemilik).
2. Ketuk **Pemilik (wajib)**, isi username dan password baru, lalu **Simpan**.
3. Ulangi untuk **Admin** dan **Editor** jika organisasi butuh pengurus lain.
4. Bagikan username + password suatu peran hanya ke pengurus peran itu.

Aturan isian:

- Username 3–40 karakter: huruf, angka, titik, garis bawah, atau minus. Tidak membedakan huruf besar/kecil. Username ketiga peran harus berbeda.
- Password 8–72 karakter.

## Lupa atau ingin mencabut akses

- Password **tidak bisa dilihat lagi** setelah disimpan, juga oleh Pemilik. Jika lupa, Pemilik membuat password baru.
- Saat password suatu peran diganti, **semua yang sedang masuk dengan peran itu langsung keluar**. Cara ini juga dipakai untuk mencabut akses pengurus yang sudah tidak menjabat.
- Akses Admin/Editor yang belum diatur membuat undangan Admin/Editor di app ditolak.

## Pesan saat login Portal

| Pesan | Artinya |
|---|---|
| "Password Portal belum diatur..." | Organisasi belum punya akses Portal sama sekali. Pemilik perlu mengaturnya di app |
| "Username atau password salah." | Username tidak dikenal di organisasi ini, password salah, atau password milik peran lain |
| "Terlalu banyak percobaan..." | Tunggu sebentar, lalu coba lagi |

Lihat juga: [Menggunakan Portal Pengurus](/docs/portal-pengurus) dan [Pergantian pengurus](/docs/pergantian-pengurus).
`,
  },
  "pergantian-pengurus": {
    id: "doc-pergantian-pengurus",
    slug: "pergantian-pengurus",
    category: "ruang-kerja",
    audience: "creator",
    title: "Pergantian Pengurus & Serah Terima",
    summary: "Urutan aman saat kepengurusan berganti: undang pengurus baru, serah terima, dan ganti akses Portal.",
    sortOrder: 30,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Ruang kerja & anggota",
    body: `Saat kepengurusan berganti (mis. ketua OSIS baru), website dan datanya tetap di organisasi yang sama. Yang dipindahkan hanya kepemilikan dan akses.

## Urutan yang disarankan

1. **Ketua lama** memastikan akses Portal **Admin** (dan Editor bila perlu) sudah diatur. Tanpa itu, undangan ditolak.
2. **Ketua lama mengundang pengurus baru** dari Kelola organisasi → Anggota → Undang anggota. Setiap orang memakai akun OrgSpace-nya sendiri dan kodenya sendiri (sekali pakai, berlaku 1 jam). Ketua baru diundang sebagai **Admin**.
3. **Serah terima**: Kelola organisasi → **Serah terima** → pilih ketua baru → pilih peranmu setelahnya (**tetap Admin**, **tetap Editor**, atau **keluar**) → konfirmasi.
4. **Ketua baru mengeluarkan anggota lama** dari Kelola organisasi → Anggota.
5. **Ketua baru membuat akses Portal Pemilik** dan **mengganti password Admin & Editor** di Akses Portal website.

## Yang terjadi otomatis saat serah terima

- Penerima menjadi **Pemilik**. Penerima harus sudah menjadi anggota organisasi.
- **Akses Portal Pemilik dihapus**, jadi ketua lama langsung keluar dari Portal dan ketua baru wajib membuat username & password Pemilik sendiri.
- Akses Admin dan Editor **tetap berlaku**, tetapi di Akses Portal website tampil peringatan *"ganti password, masih yang lama dari sebelum serah terima"* sampai passwordnya diganti.
- Serah terima tercatat di Riwayat aktivitas. Hanya Pemilik baru yang bisa mengembalikan kepemilikan.

## Hal yang perlu diingat

- Password Portal tidak diwariskan dan tidak bisa dilihat siapa pun. Ketua baru membuat yang baru.
- Pengurus lama yang masih tahu password Admin/Editor tetap bisa masuk Portal sampai passwordnya diganti (langkah 5).
- Ruang pribadi tidak bisa diserahterimakan.
`,
  },
  "membuat-website-dari-template": {
    id: "doc-membuat-website-dari-template",
    slug: "membuat-website-dari-template",
    category: "website",
    audience: "creator",
    title: "Mengedit Website dari Template",
    summary: "Memilih template, bagian editor, jenis isian, tema, cek kelengkapan, dan menu editor.",
    sortOrder: 10,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Membuat website",
    body: `Website OrgSpace dibuat dari **template** buatan kreator. Tata letak template sudah dikunci; kamu mengganti teks, foto, link, dan warna pada bagian yang ditandai kreator.

## 1. Memilih template

1. Buka tab **Template** atau ketuk tombol **+** di menu bawah.
2. Saring per kategori organisasi (OSIS / MPK, BEM, Himpunan, UKM, Pramuka, dan lainnya). Kartu template menampilkan isi yang dimilikinya (mis. struktur kepengurusan, agenda, galeri, formulir).
3. Ketuk template. File template diunduh ke HP, lalu editor terbuka dengan isi contoh.

## 2. Bagian-bagian editor

| Bagian | Fungsi |
|---|---|
| Preview | Tampilan website, langsung berubah saat kamu mengetik |
| Chip section | **Semua** atau per section (Hero, Tentang, Kontak, ...). \`Hero ✓\` = isian wajibnya lengkap; \`Hero · 2\` = masih 2 yang perlu dilengkapi |
| Tab **Isi** | Teks, paragraf, foto, link, dan tombol |
| Tab **Gaya** | Warna, ukuran huruf, dan sudut yang diizinkan kreator, serta **Tema website** (warna utama) |
| Urungkan / Ulangi | Membatalkan atau mengulang perubahan terakhir |
| Preview layar penuh | Melihat website seperti pengunjung |
| Status simpan | \`menyimpan…\` → \`tersimpan\`. Perubahan disimpan otomatis |

## 3. Jenis isian

| Isian | Cara mengisi |
|---|---|
| Teks / paragraf | Ketik langsung. Ada batas karakter agar tata letak tidak rusak. Tanda \`*\` = wajib |
| Foto | **Ganti foto** dari galeri HP. Foto disesuaikan dengan rasio yang tertulis (mis. 16:9). **Kembalikan foto contoh** untuk membatalkan |
| Link / tombol | Alamat harus diawali \`https://\`, \`http://\`, \`mailto:\`, \`tel:\`, atau \`https://wa.me/\`. **Pakai nomor WhatsApp** membuat link WA dari nomor HP |
| Warna | Pilih warna atau ketik kode seperti \`#1E3A8A\`. Muncul peringatan jika kontras teks terlalu rendah |

Isian yang terhubung ke **profil organisasi** (nama, logo, kontak, media sosial) terisi otomatis dari Profil organisasi.

## 4. Cek kelengkapan

Menu **⋮ → Cek kelengkapan** menampilkan:

- **Perlu dilengkapi**: isian wajib yang kosong atau link yang tidak valid. Ketuk **Lengkapi sekarang** untuk langsung ke isiannya.
- **Saran (opsional)**: isian yang masih teks, foto, atau link contoh.

## 5. Menu lain

| Menu | Fungsi |
|---|---|
| Export | Membuka **Selesai & Publikasi**: terbitkan atau unduh ZIP (lihat [Meng-online-kan website](/docs/cara-mengonlinekan-website)) |
| Kembalikan ke isi template | Semua teks, foto, dan gaya kembali seperti template (masih bisa diurungkan) |
| Ganti nama project | Nama di daftar website |
| Hapus project | Menghapus project dan foto pilihannya dari HP |

## Tips

- Kerjakan section demi section sampai chip-nya bertanda ✓.
- Data yang sering berubah (berita, agenda, prestasi, galeri) **tidak** diedit di sini. Data itu diisi pengurus lewat [Portal Pengurus](/docs/portal-pengurus) setelah website terbit.
- Jika tampil *"Sedang diedit oleh ..."*, pengurus lain sedang membuka editor website itu. Tunggu sampai ia selesai.
`,
  },
  "kelola-website": {
    id: "doc-kelola-website",
    slug: "kelola-website",
    category: "website",
    audience: "creator",
    title: "Kelola Website",
    summary: "Daftar website, status, minta diterbitkan, ganti nama, duplikat, hapus, dan bagian halaman Kelola website.",
    sortOrder: 20,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Membuat website",
    body: `Tab **Website** di app berisi semua website ruang kerja aktif. Ketuk sebuah website untuk membuka halaman **Kelola website**.

## Daftar website

- Cari nama website dan saring per status.
- Status: **Draft** (belum terbit), **Online**, **Dimatikan**, **Ditangguhkan** (oleh tim OrgSpace, disertai alasan).
- Label *"Ada perubahan belum diterbitkan"* muncul jika website sudah diedit setelah terbit terakhir.
- *"Sedang diedit oleh ..."* berarti pengurus lain sedang membuka editornya.

## Tombol utama

| Tombol | Siapa | Fungsi |
|---|---|---|
| **Edit tampilan** | Semua peran | Membuka editor (lihat [Mengedit website](/docs/membuat-website-dari-template)) |
| **Minta diterbitkan** | Editor | Mengirim permintaan (dengan catatan opsional) ke Admin. Status permintaan tampil di banner |

Untuk menerbitkan, Pemilik atau Admin memakai **Export → Publish Langsung** dari editor (lihat [Meng-online-kan website](/docs/cara-mengonlinekan-website)).

## Menu ⋮

| Menu | Keterangan |
|---|---|
| Ganti nama | Nama website di app. Alamat yang sudah terbit tidak berubah |
| Duplikat | Membuat salinan untuk dicoba tanpa mengubah aslinya |
| Hapus | Website yang sudah pernah online ikut menghapus alamatnya; ketik nama website untuk konfirmasi |

## Bagian halaman Kelola website

| Bagian | Status |
|---|---|
| **Domain** | Tersedia setelah terbit: alamat bawaan + [domain sendiri](/docs/domain-kustom) |
| Ringkasan (link, QR code), Riwayat versi, Pengaturan situs | Segera hadir |
| Konten, Statistik pengunjung, Data masuk | Segera hadir. Sementara itu, berita, modul, dan isian formulir dikelola di [Portal Pengurus](/docs/portal-pengurus) |
`,
  },
  "cara-mengonlinekan-website": {
    id: "doc-cara-mengonlinekan-website",
    slug: "cara-mengonlinekan-website",
    category: "terbit",
    audience: "creator",
    title: "Meng-online-kan Website",
    summary: "Publish Langsung ke orgspace.app/nama-website atau unduh ZIP, beserta hal yang perlu diketahui setelah terbit.",
    sortOrder: 10,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Menerbitkan website",
    body: `Website bisa **diterbitkan** ke \`orgspace.app/nama-website\` (gratis, langsung online) atau **diunduh sebagai ZIP** untuk di-hosting sendiri.

## 1. Membuka Selesai & Publikasi

1. Di editor, buka **⋮ → Cek kelengkapan** dan lengkapi isian wajib. Website boleh tetap diekspor sebagai draft, tapi isian kosong akan tampil apa adanya.
2. Ketuk **Export**. Layar **Selesai & Publikasi** menampilkan dua pilihan: **Publish Langsung** dan **Download as ZIP File**.

## 2. Publish Langsung

1. Isi **Nama website**. Nama ini menjadi alamat: \`orgspace.app/nama-website\`. Gunakan huruf kecil, angka, dan tanda minus; app memberi tahu jika nama sudah dipakai.
2. Ketuk **Publish Langsung Sekarang**.
3. Setelah berhasil, muncul dialog **Website Berhasil Diterbitkan!** dengan alamat website, tombol **Buka Website**, **Salin Tautan**, dan **Atur Domain Sendiri**.

Yang perlu diketahui:

- Hanya **Pemilik** dan **Admin** yang bisa menerbitkan. **Editor** memakai **Minta diterbitkan** di halaman Kelola website, lalu Admin yang menerbitkan.
- Website tersimpan di **ruang kerja aktif** dan tampil di tab **Website** untuk semua anggota.
- Setelah terbit pertama kali, **nama website dikunci** agar link yang sudah dibagikan tetap jalan.
- Untuk memperbarui tampilan: edit lagi, lalu Publish Langsung lagi. Alamatnya tetap sama.
- Foto yang filenya sudah tidak ada di HP diganti foto contoh. App memberi tahu foto mana; pilih ulang foto itu lalu terbitkan lagi.
- Data modul (berita, agenda, prestasi) dan formulir dikelola di [Portal Pengurus](/docs/portal-pengurus) dan langsung tampil **tanpa terbit ulang**.

> Sebelum membagikan link, minta Pemilik mengatur [akses Portal Pengurus](/docs/akses-portal-pengurus) agar pengurus bisa mengisi data website.

## 3. Download as ZIP File

1. Di bagian **Download as ZIP File**, atur **Nama file** dan **Kompres foto agar ringan** (disarankan).
2. Ketuk **Download File ZIP** untuk menyimpan ke HP, atau **Bagikan** untuk mengirimnya lewat aplikasi lain.

ZIP berisi HTML, CSS, JS, dan foto yang siap diunggah ke hosting statis mana pun. Bagian modul dinamis (berita, agenda, formulir) di ZIP **tetap menampilkan isi contoh** karena modul hanya bekerja di website yang tayang di OrgSpace.

## 4. Setelah terbit

- [Kelola website](/docs/kelola-website): ganti nama, duplikat, domain, hapus.
- [Domain sendiri](/docs/domain-kustom): mis. \`osis-sman1.sch.id\`.
- [Portal Pengurus](/docs/portal-pengurus): mengisi berita, modul, dan membaca formulir.
`,
  },
  "portal-pengurus": {
    id: "doc-portal-pengurus",
    slug: "portal-pengurus",
    category: "terbit",
    audience: "all",
    title: "Menggunakan Portal Pengurus",
    summary: "Mengisi berita, data modul, dan membaca isian formulir website tanpa terbit ulang.",
    sortOrder: 20,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Menerbitkan website",
    body: `Portal Pengurus adalah dashboard di browser untuk mengisi **data** website yang sudah terbit: berita, data modul (agenda, prestasi, galeri, dll.), dan isian formulir pengunjung. Perubahan **langsung tampil** di website tanpa terbit ulang.

## Membuka Portal

- Alamat: \`orgspace.app/nama-website/admin\` (atau \`domain-kamu/admin\`). Banyak template juga punya tombol **Masuk Pengurus**.
- Masuk dengan **username + password akses Portal** dari Pemilik organisasi. Cara mengaturnya: [Akses Portal Pengurus](/docs/akses-portal-pengurus).
- Sesi berlaku **8 jam** dan hanya untuk website itu. Tombol **Keluar** ada di menu samping.

## Tab

| Tab | Isi | Peran |
|---|---|---|
| **Ringkasan** | Jumlah isian formulir, berita, item modul, dan akses yang sedang dipakai | Semua |
| **Kotak Masuk Form** | Semua kiriman dari formulir website (pendaftaran, aspirasi, dll.) | Pemilik, Admin |
| **Berita & Agenda** | Tambah dan hapus pengumuman: judul (maks 200), ringkasan (maks 500), dan isi | Semua |
| **Modul Kustom** | Data modul yang disediakan template | Semua |

## Mengisi Modul Kustom

1. Pilih modul (mis. \`prestasi\`, \`agenda\`, \`galeri\`). Daftar modul dan isiannya dibaca otomatis dari template.
2. Isi form. Jenis isian mengikuti template: teks singkat, teks panjang, angka, tanggal, link, atau **foto** (diunggah dan diperkecil otomatis).
3. **Simpan**. Data langsung tampil di website.
4. Untuk data yang sudah ada: **edit**, **hapus**, atau ubah urutan dengan tombol **naik/turun**. Urutan di Portal sama dengan urutan di website.

Isian yang dikosongkan tetap menampilkan isi contoh dari template. Isi semua isian penting agar teks contoh tidak tampil.

## Yang tidak ada di Portal

- **Mengubah tampilan** (teks hero, foto utama, warna): dilakukan di editor app lalu diterbitkan ulang.
- **Mengelola anggota**: dari app, Kelola organisasi → Anggota.
- **Mengatur akses Portal**: dari app, oleh Pemilik.

## Mencoba tanpa akun

Template di galeri Website OrgSpace punya tombol **Preview**. Dari preview, buka \`/admin\` untuk mencoba Portal Pengurus demo (login demo tertera di layar). Data demo hanya ada di tab browser itu dan hilang saat ditutup.
`,
  },
  "domain-kustom": {
    id: "doc-domain-kustom",
    slug: "domain-kustom",
    category: "terbit",
    audience: "creator",
    title: "Memasang Domain Sendiri",
    summary: "Menghubungkan domain seperti osis-sman1.sch.id: record DNS, verifikasi, www, dan masalah umum.",
    sortOrder: 30,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Menerbitkan website",
    body: `Setiap website yang terbit mendapat alamat gratis \`orgspace.app/nama-website\`. Kamu juga bisa memakai domain sendiri, mis. \`osis-sman1.sch.id\` atau \`himatif.my.id\`.

## Syarat

- Website sudah **terbit** (bukan draft).
- Kamu **Pemilik** atau **Admin** organisasi.
- Kamu punya akses ke panel DNS tempat domain dibeli (mis. Cloudflare, Niagahoster, Domainesia, Rumahweb).

## Langkah

1. Kelola website → **Domain** → isi domain (tanpa \`https://\`), lalu **Sambungkan Domain**.
2. App menampilkan record DNS yang harus ditambahkan. Ketuk nilainya untuk menyalin.

| Jenis | Host | Kapan |
|---|---|---|
| \`A\` | \`@\` | Untuk domain utama (mis. \`osis.id\`) |
| \`CNAME\` | nama subdomain (mis. \`web\`) | Untuk subdomain (mis. \`web.osis.id\`) |
| \`TXT\` | \`_orgspace-verify\` (atau \`_orgspace-verify.web\` untuk subdomain) | **Wajib**: bukti domain ini milikmu |
| \`CNAME www\` / \`A @\` | \`www\` / \`@\` | **Opsional**: agar versi dengan dan tanpa \`www\` sama-sama membuka website |

3. Tambahkan record itu di panel DNS domainmu, simpan, lalu tunggu beberapa menit.
4. Kembali ke app → **Periksa & Aktifkan DNS**. Jika berhasil, status berubah menjadi **Domain Aktif**.

> Domain berakhiran \`.sch.id\`, \`.my.id\`, \`.ac.id\`, \`.or.id\` dikenali dengan benar: \`osis.sch.id\` dianggap domain utama (record \`A @\`), bukan subdomain.

## Setelah aktif

- \`osis.id\` dan \`www.osis.id\` membuka website yang sama (jika record opsional ditambahkan).
- OrgSpace memeriksa domain secara berkala. Jika \`A\`/\`CNAME\` belum mengarah ke OrgSpace, domain tetap aktif dengan peringatan di app.
- Jika record \`TXT\` hilang (mis. domain kedaluwarsa), app menampilkan peringatan. Setelah **7 hari** tetap hilang, domain dilepas otomatis dari website.
- **Lepas Domain** kapan saja dari layar yang sama. Website tetap bisa dibuka lewat alamat bawaan.

## Masalah umum

| Gejala | Solusi |
|---|---|
| "Record TXT verifikasi belum terdeteksi" | DNS butuh beberapa menit sampai beberapa jam. Pastikan host TXT persis seperti di app, lalu periksa lagi |
| Domain aktif tapi website belum terbuka | Record \`A\`/\`CNAME\` belum benar atau masih propagasi. Lihat peringatan di app |
| Hanya versi \`www\` (atau tanpa \`www\`) yang terbuka | Tambahkan record opsional pasangannya |
`,
  },
  "menjadi-penyedia-template": {
    id: "doc-menjadi-penyedia-template",
    slug: "menjadi-penyedia-template",
    category: "provider",
    audience: "provider",
    title: "Menjadi Penyedia Template",
    summary: "Mengaktifkan mode kreator, menu kreator, enam langkah Upload, dan status template.",
    sortOrder: 10,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Penyedia template",
    body: `Penyedia template (kreator) membuat template website HTML/CSS/JS yang dipakai organisasi lain di OrgSpace. Template-mu tayang di galeri, dipakai pembuat website, lalu datanya diisi pengurus lewat Portal Pengurus.

## Jalur belajar untuk kreator baru

1. **Unduh paket contoh**: [Contoh template & paket unduhan](/docs/contoh-template-dan-paket). Coba unggah contohnya apa adanya.
2. **Pahami syaratnya**: [Requirement project & aturan pengecekan](/docs/requirement-project-dan-tools).
3. **Buat bagian dinamis**: [Panduan Modul Dinamis](/docs/modul-sistem-dan-kustom) (\`data-ws-*\`) dan [Membuat fitur kustom](/docs/membuat-fitur-kustom).
4. **Unggah dan tandai**: langkah di bawah, lalu [Menandai bagian template](/docs/menandai-bagian-template).

## 1. Mengaktifkan mode kreator

1. Masuk ke app dengan akunmu.
2. Buka **Profil → Jadi kreator template**.
3. Isi **nama kreator** (wajib), bio (maks 300 karakter), link portofolio/GitHub, dan keahlian, lalu centang persetujuan aturan kreator.
4. Kirim. Mode kreator **langsung aktif** tanpa menunggu verifikasi.

Pindah mode kapan saja: **Profil → Beralih ke mode kreator / mode pembuat website**. App membuka mode terakhir yang kamu pakai.

## 2. Menu bawah mode kreator

| Menu | Isi |
|---|---|
| Beranda | Ringkasan **Aktif**, **Dilihat**, **Didownload**, tren download, dan **Perlu tindakan** (upload gagal, draft hampir dihapus) |
| Upload | Mengunggah template baru dan melanjutkan draft |
| Template Anda | Semua templatemu, bisa dikelompokkan dalam folder, dicari, dan disaring per status |
| Profil | Profil kreator dan pengaturan |

Arti angka: **Dilihat** = berapa kali halaman detail templatemu dibuka pembuat website (7 atau 30 hari). **Didownload** = berapa project dari templatemu yang selesai dan diexport (sekali per project; export oleh pemilik template sendiri tidak dihitung).

## 3. Enam langkah Upload

| Langkah | Yang dilakukan |
|---|---|
| **1. Pilih file** | Pilih ZIP (maks 20 MB) dari HP, Drive, atau WhatsApp. App memeriksa sekilas di HP. ZIP dikirim per potongan 1 MB dan bisa dilanjutkan jika koneksi putus |
| **2. Pengecekan file** | Server memeriksa ZIP dengan [aturan pengecekan](/docs/requirement-project-dan-tools). **Error** menghentikan proses; **peringatan** boleh dilanjutkan. Setiap temuan menampilkan file, baris, dan cara memperbaikinya |
| **3. Info template** | Nama (3–60 karakter), **kategori** organisasi, **isi template** (profil, struktur, agenda, galeri, ...), deskripsi (20–300 karakter), 1–5 kata kunci, dan thumbnail (otomatis, dari section lain, atau unggah sendiri) |
| **4. Tandai bagian** | Pilih teks, gambar, link, dan tombol yang boleh diganti pembuat website, per halaman dan per section. Detail: [Menandai bagian template](/docs/menandai-bagian-template) |
| **5. Coba sebagai pengguna** | Mencoba form edit yang akan dilihat pembuat website, dengan preview langsung. Hanya percobaan, isi contoh template tidak berubah |
| **6. Kirim** | Ringkasan, centang **hak pakai aset** ("Saya berhak memakai semua gambar, font, dan isi dalam template ini"), lalu Kirim |

Setelah lolos langkah 2, pekerjaan tersimpan sebagai **draft**. Kamu boleh berhenti dan melanjutkan dari tab Upload. Langkah 4 dan 5 bisa bolak-balik.

## 4. Setelah dikirim

1. Status menjadi **Sedang dicek**. Server memeriksa ulang file dan tandaan, menyalin library CDN ke dalam paket template, lalu membuat paket yang dipakai app.
2. Lolos → **Tayang** di galeri. Peringatan yang tersisa masuk **Perlu tindakan** di Beranda.
3. Gagal → masuk **Perlu diperbaiki** di tab Upload. Perbaiki file, lalu **Upload file perbaikan**.

## Status template

| Status | Arti |
|---|---|
| Draft | Belum dikirim. Maksimal **5 draft**; draft yang 30 hari tidak disentuh dihapus (diingatkan 5 hari sebelumnya) |
| Sedang dicek | Pengecekan akhir sedang berjalan |
| Tayang | Tampil di galeri dan bisa dipakai |
| Tidak lolos | Perlu diperbaiki lalu diunggah ulang |
| Dinonaktifkan | Tidak tampil di galeri |

> Pengecekan keliru? Laporkan dari layar hasil pengecekan agar tim OrgSpace memeriksanya.
`,
  },
  "contoh-template-dan-paket": {
    id: "doc-contoh-template-dan-paket",
    slug: "contoh-template-dan-paket",
    category: "provider",
    audience: "provider",
    title: "Contoh Template & Paket Unduhan",
    summary: "Unduh paket contoh (template sederhana & lengkap, panduan, instruksi AI agent) dan coba dari upload sampai Portal.",
    sortOrder: 15,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Penyedia template",
    body: `Cara tercepat membuat template OrgSpace adalah **mulai dari contoh yang sudah lolos pengecekan**.

## [⬇ Unduh paket contoh (ZIP)](/downloads/contoh-modul-orgspace.zip)

\`\`\`
contoh-modul-orgspace/
├── BACA-SAYA.md       ← mulai dari sini
├── AGENTS.md          ← instruksi untuk AI agent (Codex, Cursor, dll.)
├── CLAUDE.md          ← sama, untuk Claude Code
├── docs/
│   ├── 01-panduan-modul-dinamis.md   ← semua atribut data-ws-*
│   ├── 02-requirement-dan-aturan.md  ← batas ZIP, CDN, daftar kode pengecekan
│   ├── 03-menandai-bagian.md         ← langkah Tandai di app
│   └── 04-membuat-fitur-kustom.md    ← merancang fitur + resep JavaScript
├── contoh-template/   ← contoh sederhana 1 halaman, semua modul dasar
└── template-sinergi/  ← contoh lengkap & interaktif, 2 halaman
\`\`\`

## Dua contoh template

| | \`contoh-template/\` | \`template-sinergi/\` |
|---|---|---|
| Halaman | 1 (\`index.html\`) | 2 (\`index.html\`, \`tentang.html\`) |
| Modul | Pengumuman, \`prestasi\`, \`agenda\` | Pengumuman + 7 modul kustom (program, prestasi, agenda, galeri, testimoni, FAQ, struktur) |
| Formulir | \`pendaftaran\`, \`aspirasi\` | Pendaftaran 2 langkah, aspirasi |
| Data otomatis | – | \`data-ws-stat\` (statistik), \`data-ws-org\` (profil organisasi) |
| JavaScript | Menu HP, filter prestasi per tingkat | Statistik naik saat di-scroll, filter dari data, agenda + hitung mundur, lightbox galeri, pencarian pengurus, tema gelap |
| Tema | – | Warna utama, aksen, dan sudut di variabel \`:root\` |

## Mencoba contoh dari nol sampai Portal

1. Unduh dan ekstrak paket.
2. ZIP **isi** folder \`contoh-template/\` sehingga \`index.html\` ada di akar ZIP:
   - Linux/macOS: \`cd contoh-template && zip -r ../contoh.zip . && cd ..\`
   - Windows (PowerShell): \`Compress-Archive -Path contoh-template\\* -DestinationPath contoh.zip -Force\`
3. Di app: mode kreator → **Upload** → pilih \`contoh.zip\` → Info → Tandai minimal 3 elemen (mis. judul hero, deskripsi, logo) → Coba → Kirim.
4. Pindah ke mode pembuat website, pakai template itu dari galeri, lalu **Publish Langsung**.
5. Pemilik organisasi mengatur [akses Portal](/docs/akses-portal-pengurus), lalu buka \`orgspace.app/nama-website/admin\` dan isi pengumuman, prestasi, dan agenda. Data langsung tampil di website.
6. Kirim formulir dari website, lalu lihat isinya di tab **Kotak Masuk Form** Portal.

## Dibantu AI agent

Ekstrak paket, buka foldernya di editor dengan AI agent, lalu beri perintah, misalnya:

> Baca AGENTS.md lalu buatkan template website untuk OSIS SMA bergaya modern, 3 halaman: Beranda, Agenda, dan Tentang. Pengurus harus bisa mengisi agenda, prestasi (bisa difilter per tingkat), galeri foto, dan struktur pengurus. Tambahkan formulir pendaftaran anggota dan angka statistik.

Agent membuat folder \`template/\`, file ZIP, dan daftar elemen yang sebaiknya ditandai di app.

## Coba Portal demo tanpa mengunggah

Setiap template di galeri Website OrgSpace punya tombol **Preview**. Buka \`/admin\` dari preview untuk mencoba Portal Pengurus demo dengan data contoh.
`,
  },
  "requirement-project-dan-tools": {
    id: "doc-requirement-project-dan-tools",
    slug: "requirement-project-dan-tools",
    category: "provider",
    audience: "provider",
    title: "Requirement Project & Aturan Pengecekan",
    summary: "Bentuk project, batas ukuran, jenis file, library CDN, keamanan, dan daftar lengkap kode pengecekan upload.",
    sortOrder: 20,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Penyedia template",
    body: `Semua aturan di halaman ini diperiksa otomatis saat Upload langkah 2. **Error** menghentikan upload; **peringatan** boleh dilanjutkan tapi sebaiknya diperbaiki. Setiap temuan menyebut kodenya (mis. \`NO_INDEX\`), file, baris, dan cara memperbaikinya.

## 1. Bentuk project

- **HTML + CSS + JavaScript biasa** (vanilla). Teks dan gambar utama ditulis langsung di HTML, bukan dibuat JavaScript.
- Hasil build alat seperti Vite, Astro, Eleventy, atau Hugo boleh, asalkan yang diunggah adalah **folder hasil build** (mis. \`dist/\`) yang berisi HTML jadi.
- **Belum didukung**: hasil build React/Vue/Svelte/Angular/Next.js/Nuxt yang isi halamannya dibuat JavaScript, dan template yang butuh server (PHP).

\`\`\`text
template-anda.zip
├── index.html        ← WAJIB di akar ZIP (bukan template-anda/index.html)
├── tentang.html      ← halaman lain (opsional)
├── css/style.css
├── js/main.js        ← opsional
├── img/...           ← gambar
└── fonts/...         ← font lokal (opsional)
\`\`\`

Nama folder \`css/\`, \`js/\`, \`img/\` hanya saran; yang wajib adalah \`index.html\` di akar dan semua path **relatif** (\`css/style.css\`, bukan \`/css/style.css\`).

## 2. Batas ukuran

| Batas | Nilai | Tingkat |
|---|---|---|
| Ukuran ZIP | 20 MB | Error |
| Total setelah diekstrak | 50 MB | Error |
| Jumlah file | 500 | Error |
| Ukuran satu file | 10 MB | Error |
| Halaman HTML | 10 | Error |
| Satu gambar | > 1 MB | Peringatan |
| Berat satu halaman | > 5 MB | Peringatan |
| Satu file CSS | > 1 MB | Peringatan |
| Gambar base64 di HTML | > 20 KB | Peringatan |

## 3. Jenis file

- **Boleh**: \`html\`, \`htm\`, \`css\`, \`js\`, \`mjs\`, \`png\`, \`jpg\`, \`jpeg\`, \`webp\`, \`gif\`, \`svg\`, \`ico\`, \`woff\`, \`woff2\`, \`ttf\`, \`otf\`, \`txt\`, \`md\`, \`json\` (data lokal, mis. \`fetch("data/menu.json")\`).
- **Diabaikan** (boleh ikut): \`package.json\`, lockfile, \`*.config.js\`, \`tsconfig.json\`, README, LICENSE.
- **Ditolak**: PHP; \`scss\`/\`sass\`/\`less\` yang dipakai tanpa di-build; kode sumber \`ts\`/\`tsx\`/\`jsx\`/\`vue\`/\`svelte\` sebagai pengganti hasil build.

## 4. Library dari CDN

- Host yang diizinkan: \`cdn.jsdelivr.net\`, \`cdnjs.cloudflare.com\`, \`unpkg.com\`, \`cdn.tailwindcss.com\`, \`code.jquery.com\`. Google Fonts boleh (peringatan karena butuh internet).
- Library yang diizinkan: **Tailwind**, **Bootstrap**, **Bulma**, **Alpine.js** (+ collapse, intersect, focus), **jQuery**, **AOS**, **Swiper**, **Splide**, **GSAP**, **Font Awesome**, **Bootstrap Icons**, **Lucide**, **Remix Icon**.
- Tulis **versi lengkap** di URL: \`https://cdn.jsdelivr.net/npm/swiper@11.1.4/swiper-bundle.min.js\`. Tanpa versi atau \`@latest\` ditolak.
- Muat library dengan \`<script src>\` / \`<link>\` di HTML, bukan \`import\` dari CDN di dalam file JS.
- Saat template dikirim, file library **disalin ke dalam paket template**, jadi website tetap jalan walau CDN bermasalah.
- File library yang disertakan langsung di ZIP juga boleh. Versi terkenal (mis. jQuery 3.7.1, Swiper 11.1.4) dikenali otomatis.

## 5. Keamanan

- Tanpa analytics/pelacak (Google Analytics/Tag Manager, Meta Pixel, Hotjar, Clarity) dan tanpa SDK yang mengambil data dari server (mis. Firebase).
- JavaScript tidak boleh mengambil atau mengirim data ke situs lain (\`fetch("https://...")\`, XHR, WebSocket, \`sendBeacon\`). Data dinamis memakai [modul \`data-ws-*\`](/docs/modul-sistem-dan-kustom).
- Form tidak boleh \`action\` ke situs lain, dan halaman tidak boleh berpindah otomatis ke situs lain.
- \`iframe\` hanya dari YouTube (\`youtube.com\`, \`youtube-nocookie.com\`) dan Google Maps.
- Dilarang kode diacak (obfuscated), Service Worker, pola penambang kripto, \`<base href>\`, dan atribut khusus app (\`data-edit\`, \`data-key\`, \`data-label\`, \`data-tpl-id\`).
- **Saat website terbit**, atribut \`onclick\`/\`onload\`/\`onerror\` dsb. dan link \`javascript:\` **dihapus otomatis**. Pasang event dengan \`addEventListener\` di file JS.

## 6. Daftar lengkap pemeriksaan

### Struktur ZIP

| Kode | Tingkat | Arti |
|---|---|---|
| \`ZIP_INVALID\` | Error | ZIP rusak atau tidak bisa dibuka |
| \`ZIP_ENCRYPTED\` | Error | ZIP dikunci password |
| \`NO_INDEX\` | Error | \`index.html\` tidak ada di akar ZIP |
| \`ZIP_TOO_LARGE\`, \`EXTRACTED_TOO_LARGE\`, \`TOO_MANY_FILES\`, \`FILE_TOO_LARGE\`, \`TOO_MANY_PAGES\` | Error | Melewati batas di bagian 2 |
| \`UNSAFE_PATH\` | Error | Nama file berbahaya di dalam ZIP (mis. \`../\`) |
| \`SOURCE_NOT_BUILT\`, \`UNBUILT_RESOURCE\` | Error | Kode sumber atau SCSS/LESS yang belum di-build |
| \`SOURCE_FILES_INCLUDED\` | Peringatan | File sumber ikut terunggah (tidak dipakai halaman) |
| \`CASE_MISMATCH\` | Error | Huruf besar/kecil nama file berbeda dengan yang dipanggil |
| \`FILE_NAME_STYLE\` | Peringatan | Nama file memakai spasi atau karakter khusus |
| \`FILE_TYPE_NOT_ALLOWED\` | Error | Jenis file tidak diizinkan |
| \`SERVER_SCRIPT\` | Error | Template butuh server (PHP) |

### HTML

| Kode | Tingkat | Arti |
|---|---|---|
| \`HTML_UNREADABLE\` | Error | File HTML kosong atau tidak bisa dibaca |
| \`JS_RENDERED_CONTENT\` | Error | Konten utama dibuat oleh JavaScript |
| \`JS_PARTIAL_CONTENT\` | Peringatan | Sebagian konten dibuat oleh JavaScript (tidak bisa ditandai) |
| \`COMPONENT_FRAMEWORK\` | Error | Template React/Vue/Svelte belum didukung |
| \`TOO_FEW_EDITABLE\` | Error | Kurang dari 3 teks/gambar yang bisa ditandai |
| \`FORM_EXTERNAL_ACTION\` | Error | Form mengirim data ke situs lain |
| \`AUTO_REDIRECT\` | Error | Halaman berpindah otomatis ke situs lain |
| \`RESERVED_ATTRIBUTE\` | Error | Memakai atribut khusus app |
| \`BASE_HREF\` | Error | Ada tag \`<base href>\` |
| \`PLUGIN_TAG\` | Error | Memakai teknologi plugin lama (\`<object>\`, \`<embed>\`, \`<applet>\`) |
| \`DYNAMIC_MODULE_INVALID\` | Error | Penanda \`data-ws-*\` tidak valid (lihat [Panduan Modul Dinamis](/docs/modul-sistem-dan-kustom)) |
| \`NO_VIEWPORT\`, \`NO_TITLE\`, \`IMG_NO_ALT\`, \`CHARSET\`, \`DOCTYPE_LANG\`, \`DUPLICATE_ID\` | Peringatan | Tanpa meta viewport, \`<title>\`, \`alt\` gambar, UTF-8, \`<!DOCTYPE html>\`/\`lang\`, atau ada \`id\` ganda |

### Link dan aset

| Kode | Tingkat | Arti |
|---|---|---|
| \`MISSING_ASSET\` | Error | File CSS/JS yang dipanggil tidak ada |
| \`BROKEN_PAGE_LINK\` | Error | Link ke halaman yang tidak ada |
| \`MISSING_IMAGE\`, \`BROKEN_ANCHOR\`, \`ORPHAN_PAGE\`, \`HOTLINK_IMAGE\`, \`ROOT_ABSOLUTE_PATH\` | Peringatan | Gambar hilang, \`#anchor\` tanpa \`id\`, halaman tidak bisa dibuka dari menu, gambar diambil dari situs lain, path diawali \`/\` |

### CSS

| Kode | Tingkat | Arti |
|---|---|---|
| \`CSS_SCRIPT_TRICK\` | Error | CSS menjalankan script |
| \`CSS_MISSING_URL\`, \`CSS_SYNTAX\`, \`NOT_RESPONSIVE\`, \`CSS_TOO_LARGE\` | Peringatan | Gambar/font di CSS tidak ada, kesalahan tulis, tidak menyesuaikan layar HP, file terlalu besar |

### Library luar

| Kode | Tingkat | Arti |
|---|---|---|
| \`CDN_NOT_ALLOWED\`, \`CDN_VERSION_UNCLEAR\`, \`UNTRUSTED_SOURCE\` | Error | Library di luar daftar, versi tidak jelas, atau script dari situs yang bukan CDN terpercaya |
| \`JS_CDN_IMPORT\` | Error | \`import\` library dari CDN di dalam file JS |
| \`TRACKER\`, \`REMOTE_DATA_SDK\`, \`IFRAME_NOT_ALLOWED\` | Error | Pelacak, SDK data server, atau iframe yang tidak diizinkan |
| \`TAILWIND_PLAY_CDN\`, \`GOOGLE_FONTS\` | Peringatan | Tailwind Play CDN lebih berat; Google Fonts butuh internet |

### JavaScript

| Kode | Tingkat | Arti |
|---|---|---|
| \`EXT_FETCH\` | Error | Mengirim/mengambil data ke situs lain |
| \`HIDDEN_EXTERNAL_LINK\` | Error | Link ke situs luar disembunyikan |
| \`SERVICE_WORKER\`, \`OBFUSCATED_JS\`, \`CRYPTO_MINER\` | Error | Service Worker, kode diacak, pola penambang kripto |
| \`JS_EVAL\`, \`STRING_TIMER\`, \`DOCUMENT_WRITE\`, \`POPUP_ON_LOAD\`, \`JS_SYNTAX\`, \`JS_RUNTIME_ERROR\`, \`HORIZONTAL_OVERFLOW\` | Peringatan | \`eval()\`, \`setTimeout\` dengan teks, \`document.write()\`, popup, kesalahan tulis, error saat dijalankan, elemen melebar melewati layar HP |

### Ukuran

| Kode | Tingkat | Arti |
|---|---|---|
| \`IMAGE_TOO_LARGE\`, \`PAGE_TOO_HEAVY\` | Peringatan | Gambar > 1 MB, halaman > 5 MB |

## 7. Saran agar template disukai

- \`<meta name="viewport" content="width=device-width, initial-scale=1">\` dan desain rapi di lebar 360 px.
- \`<!DOCTYPE html>\`, \`<html lang="id">\`, \`<meta charset="utf-8">\`, dan \`<title>\` di setiap halaman.
- Warna utama, aksen, dan sudut sebagai variabel \`:root\` agar bisa didaftarkan sebagai **tema** (lihat [Menandai bagian template](/docs/menandai-bagian-template)).
- Gambar WebP/JPG di bawah 500 KB dengan \`alt\` yang jelas; gambar contoh milik sendiri atau berlisensi bebas.
- Banyak teks dan gambar biasa di luar modul (judul, paragraf, logo, foto) dengan \`id\` jelas (\`hero-judul\`), agar mudah ditandai.

## Editor yang disarankan

VS Code (atau editor apa pun) + ekstensi Live Server untuk melihat hasil di browser. Coba juga di layar HP dengan mode responsif browser.
`,
  },
  "modul-sistem-dan-kustom": {
    id: "doc-modul-sistem-dan-kustom",
    slug: "modul-sistem-dan-kustom",
    category: "provider",
    audience: "provider",
    title: "Panduan Modul Dinamis",
    summary: "Pengumuman, formulir, modul kustom, statistik, profil organisasi, Portal Pengurus, dan JavaScript template. Lengkap dengan paket contoh siap unduh.",
    sortOrder: 30,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Penyedia template",
    body: `Modul dinamis membuat template HTML biasa bisa diisi data oleh pengurus organisasi: pengumuman, prestasi, agenda, formulir pendaftaran, dan tombol ke Portal Pengurus. Provider **tidak perlu menulis backend, API, atau database**. Cukup tambahkan atribut \`data-ws-*\` ke elemen HTML. JavaScript template sendiri (menu, slider, tab, dark mode) tetap boleh dan ikut jalan.

Contoh sederhana ada di folder \`contoh-template/\`, contoh lengkap & interaktif di \`template-sinergi/\`. Cara merancang fitur sendiri (agenda, galeri, filter, pencarian) beserta resep JavaScript-nya ada di [Membuat fitur kustom](/docs/membuat-fitur-kustom).

## Unduh paket contoh

[⬇ Unduh contoh-modul-orgspace.zip (228 KB)](/downloads/contoh-modul-orgspace.zip)

Satu paket berisi template contoh, dokumentasi, dan instruksi untuk AI agent. Berikan paket ini ke AI agent (Claude Code, Codex, Cursor, dll.) sebagai referensi agar ia bisa membuat template yang langsung lolos pengecekan upload.

| Isi paket | Untuk apa |
|---|---|
| \`AGENTS.md\` / \`CLAUDE.md\` | Instruksi untuk AI agent: cara kerja produk, apa yang dibaca, aturan, checklist, cara membuat ZIP, dan laporan saran isian Tandai |
| \`docs/\` | Panduan modul dinamis (halaman ini), aturan pengecekan, requirement & tools, **Membuat Fitur Kustom** (resep JavaScript), dan **Menandai bagian** |
| \`template-sinergi/\` | Template **lengkap & interaktif**: 2 halaman, 7 modul kustom, pengumuman, 2 formulir, statistik, profil organisasi, tema gelap |
| \`contoh-template/\` | Template **sederhana** satu halaman yang memakai semua jenis modul |
| \`BACA-SAYA.md\` | Panduan singkat untuk kamu |

### Dibuatkan AI agent

1. Ekstrak ZIP, lalu buka foldernya di editor yang punya AI agent.
2. Beri perintah, misalnya:

   > Baca AGENTS.md lalu buatkan template website untuk OSIS SMA bergaya modern dan cerah, 3 halaman: Beranda, Agenda, dan Tentang. Pengurus harus bisa mengisi agenda (dengan hitung mundur), prestasi (bisa difilter per tingkat), galeri foto, dan struktur pengurus. Tambahkan formulir pendaftaran anggota dan angka statistik.

3. Agent menunjukkan rencana halaman & modul, membuat folder \`template/\` dan \`template-saya.zip\`, lalu memberi daftar elemen yang sebaiknya ditandai.
4. Upload \`template-saya.zip\` dari aplikasi: beralih ke mode Penyedia Template, buka tab Upload.

### Membuat sendiri

1. Salin folder \`contoh-template/\` (sederhana) atau \`template-sinergi/\` (lengkap), lalu ubah desainnya.
2. Jangan hapus atribut \`data-ws-*\`. Boleh dipindah ke elemen lain asal aturannya tetap terpenuhi.
3. ZIP **isi** foldernya (\`index.html\` harus ada di paling atas ZIP, bukan di dalam folder), lalu upload dari aplikasi.

## Cara kerjanya

1. Provider menandai bagian template dengan \`data-ws-*\`, lalu mengupload template.
2. Pengecekan upload memeriksa semua atribut. Kesalahan ditampilkan lengkap dengan baris dan cara memperbaikinya.
3. Pembuat website memakai template itu lalu menerbitkannya ke \`orgspace.app/nama-website\`.
4. Pengurus mengisi data lewat **Portal Pengurus** di \`orgspace.app/nama-website/admin\`.
5. Setiap kali halaman dibuka, data terbaru langsung tampil. **Tidak perlu menerbitkan ulang.**

> Modul hanya bekerja di website yang tayang di OrgSpace. Di file ZIP hasil export, bagian modul tetap menampilkan isi contoh dari template.

## Daftar atribut

| Atribut | Dipasang di | Fungsi |
|---|---|---|
| \`data-ws-auth="login"\` | \`<a>\` | Tombol menuju Portal Pengurus website itu. |
| \`data-ws-auth="logout"\` | \`<button>\` / \`<a>\` | Disembunyikan di halaman publik (lihat bagian 1). |
| \`data-ws-auth="status"\` | \`<span>\` / \`<strong>\` | Isi aslinya (mis. "Tamu") yang tampil (lihat bagian 1). |
| \`data-ws-role="admin,editor"\` | elemen apa saja | Disembunyikan di halaman publik (lihat bagian 1). |
| \`data-ws-list="nama"\` | pembungkus daftar | Daftar data. Sama persis dengan \`data-ws-collection\`. |
| \`data-ws-collection="nama"\` | pembungkus daftar | Daftar data. Sama persis dengan \`data-ws-list\`. |
| \`data-ws-item\` | 1 kartu contoh | Kartu yang diulang untuk setiap data. |
| \`data-ws-field="nama_field"\` | elemen di dalam kartu | Diisi teks data. Pada \`<img>\` yang diisi adalah \`src\`. |
| \`data-ws-link="nama_field"\` | \`<a>\` di dalam kartu | Diisi \`href\` dari data. |
| \`data-ws-type="tanggal"\` | elemen \`data-ws-field\` | Jenis isian di Portal Pengurus: \`teks\` (bawaan), \`paragraf\`, \`angka\`, atau \`tanggal\` (lihat bagian 3). |
| \`data-ws-stat="prestasi"\` | elemen apa saja | Diisi **angka** dari database: jumlah item modul, pengurus, berita, atau isian form (lihat bagian 3a). |
| \`data-ws-org="nama"\` | elemen apa saja | Diisi profil organisasi: nama, logo, kontak, alamat (lihat bagian 3a). |
| \`data-ws-empty\` | di dalam pembungkus | Tampil hanya jika belum ada data. |
| \`data-ws-limit="6"\` | pembungkus daftar | Batas jumlah kartu yang tampil (1–100). |
| \`data-ws-form="nama"\` | \`<form>\` | Isian dikirim ke Kotak Masuk Portal Pengurus. |
| \`data-ws-form-alert\` | elemen di dalam form | Tempat pesan berhasil/gagal setelah form dikirim. |

## 1. Tombol Portal Pengurus

\`\`\`html
<a href="admin" data-ws-auth="login">Masuk Pengurus</a>
\`\`\`

- \`href="admin"\` dan \`data-ws-auth="login"\` sama-sama membuka Portal Pengurus website itu (\`/nama-website/admin\`).
- Pengurus mengelola semua data di Portal Pengurus. **Halaman publik website selalu tampil sebagai tamu**: demi keamanan, halaman template tidak bisa membaca sesi login pengurus.
- Karena itu \`data-ws-auth="logout"\` dan \`data-ws-role\` disembunyikan di halaman publik, dan \`data-ws-auth="status"\` tetap menampilkan isi aslinya. Atribut ini masih diterima agar template lama tetap lolos pengecekan, tapi tidak perlu dipakai di template baru.

## 2. Pengumuman (modul bawaan)

Nama \`pengumuman\`, \`berita\`, dan \`kegiatan\` memakai data yang sama: yang diisi pengurus di tab **Berita** Portal Pengurus.

\`\`\`html
<div data-ws-list="pengumuman" data-ws-limit="6">
  <article data-ws-item>
    <img src="img/pengumuman.jpg" alt="Foto pengumuman" data-ws-field="foto">
    <span data-ws-field="tanggal">1 Januari 2026</span>
    <h3 data-ws-field="judul">Judul pengumuman</h3>
    <p data-ws-field="ringkasan">Ringkasan singkat isi pengumuman.</p>
  </article>
  <div data-ws-empty>Belum ada pengumuman.</div>
</div>
\`\`\`

| Field | Isi |
|---|---|
| \`judul\` | Judul pengumuman |
| \`ringkasan\` | Ringkasan singkat |
| \`konten\` | Isi lengkap |
| \`tanggal\` | Tanggal terbit, mis. "19 Oktober 2026" |
| \`foto\` | Foto pengumuman (jika pengurus mengisinya) |
| \`detail\` | Link ke pengumuman; pakai dengan \`data-ws-link="detail"\` |

## 3. Modul kustom (buat sendiri)

Nama selain \`pengumuman\`, \`berita\`, dan \`kegiatan\` menjadi **modul kustom**. Nama modul dan nama field bebas: huruf kecil, angka, \`-\` atau \`_\`, 2–40 karakter. Portal Pengurus membaca template dan langsung menyiapkan form isian dengan field yang sama.

\`\`\`html
<div data-ws-collection="prestasi" data-ws-limit="9">
  <div data-ws-item>
    <img src="img/prestasi.jpg" alt="Foto prestasi" data-ws-field="foto">
    <span data-ws-field="tingkat">Nasional</span>
    <h3 data-ws-field="judul">Juara 1 Lomba</h3>
    <p><span data-ws-field="nama">Nama anggota</span> · <span data-ws-field="tahun">2026</span></p>
  </div>
  <div data-ws-empty>Belum ada prestasi yang dicatat.</div>
</div>

<div data-ws-collection="agenda">
  <div data-ws-item>
    <h3 data-ws-field="nama_kegiatan">Nama kegiatan</h3>
    <span data-ws-field="tanggal" data-ws-type="tanggal">Tanggal</span> · <span data-ws-field="lokasi">Lokasi</span>
    <a href="#daftar" data-ws-link="link_daftar">Ikuti</a>
  </div>
  <div data-ws-empty>Belum ada agenda terdekat.</div>
</div>
\`\`\`

Contoh di atas membuat dua modul di Portal Pengurus: \`prestasi\` (foto, tingkat, judul, nama, tahun) dan \`agenda\` (nama_kegiatan, tanggal, lokasi, link_daftar).

Jenis isian di Portal Pengurus mengikuti elemennya:

| Di template | Isian di Portal | Tampil di website |
|---|---|---|
| \`<img data-ws-field>\` | Unggah foto (diperkecil otomatis) | \`src\` gambar |
| \`data-ws-link\` | Link (\`https://...\`) | \`href\` |
| \`data-ws-type="paragraf"\` | Teks panjang | Teks, baris baru dipertahankan |
| \`data-ws-type="angka"\` | Angka | Teks |
| \`data-ws-type="tanggal"\` | Pilih tanggal | "19 Oktober 2026" |
| tanpa \`data-ws-type\` | Teks singkat | Teks |

- Pengurus bisa menambah, mengedit, menghapus, dan **mengatur urutan** data. Urutan di Portal sama dengan urutan kartu di website.
- Field yang tidak diisi pengurus tetap menampilkan isi contoh dari template. Jadi isi contoh sebaiknya netral, mis. foto default.
- \`<img data-ws-field>\` hanya menerima alamat gambar \`http://\`, \`https://\`, atau \`/...\`.
- \`data-ws-link\` hanya menerima \`http://\`, \`https://\`, \`mailto:\`, \`tel:\`, \`/...\`, atau \`#...\`. Alamat lain diabaikan dan \`href\` asli dari template yang dipakai.

## 3a. Statistik & profil organisasi (langsung dari database)

Kartu angka seperti "12 pengurus aktif" atau "40 pendaftar" tidak perlu diketik pengurus: OrgSpace menghitungnya dari
database setiap kali halaman dibuka.

\`\`\`html
<div class="stat"><strong data-ws-stat="pengurus">12</strong> pengurus aktif</div>
<div class="stat"><strong data-ws-stat="prestasi">18</strong> prestasi</div>
<div class="stat"><strong data-ws-stat="form.pendaftaran">40</strong> pendaftar</div>
\`\`\`

| \`data-ws-stat\` | Angka |
|---|---|
| \`pengurus\` | Jumlah anggota ruang kerja OrgSpace (pengurus yang punya akun) |
| \`berita\` | Jumlah pengumuman/berita |
| \`form.{nama-form}\` | Jumlah isian form itu, mis. \`form.pendaftaran\` (hanya angka, isi form tetap rahasia) |
| nama modul kustom | Jumlah item modul itu, mis. \`prestasi\`, \`agenda\`, \`anggota\` |

Nama bawaan (\`pengurus\`, \`berita\`, \`form.*\`) dipakai lebih dulu jika modul kustom memakai nama yang sama. Nama yang
belum punya data tampil \`0\`.

Profil organisasi diisi dari halaman Profil organisasi di app:

\`\`\`html
<img src="img/logo.svg" alt="Logo" data-ws-org="logo">
<span data-ws-org="nama">Nama Organisasi</span>
<a href="#" data-ws-org="whatsapp">WhatsApp</a>
\`\`\`

Pilihan: \`nama\`, \`singkatan\`, \`kategori\`, \`institusi\`, \`kota\`, \`alamat\`, \`email\`, \`whatsapp\`, \`instagram\`, \`tiktok\`,
\`youtube\`, \`logo\`.

- Pada \`<img>\` yang diisi \`src\`. Pada \`<a>\`, \`email\`/\`whatsapp\`/\`instagram\` otomatis menjadi link (\`mailto:\`, \`wa.me\`, Instagram).
- Profil yang belum diisi organisasi **disembunyikan** agar teks contoh tidak tampil di website asli. Logo yang belum
  ada tetap memakai logo template.
- Di ruang kerja pribadi hanya \`nama\` yang tersedia.

## 4. Formulir

\`\`\`html
<form data-ws-form="pendaftaran">
  <div data-ws-form-alert></div>
  <label for="nama">Nama lengkap</label>
  <input id="nama" name="nama" required>
  <label for="email">Email</label>
  <input id="email" name="email" type="email" required>
  <label for="alasan">Alasan bergabung</label>
  <textarea id="alasan" name="alasan"></textarea>
  <button type="submit">Kirim pendaftaran</button>
</form>
\`\`\`

- Setiap \`<input>\`, \`<textarea>\`, dan \`<select>\` wajib punya \`name\`. Nama itulah yang tampil di Kotak Masuk.
- Satu halaman boleh punya beberapa form dengan nama berbeda, mis. \`pendaftaran\` dan \`aspirasi\`.
- Hanya form ber-\`data-ws-form\` yang dikirim ke OrgSpace. Form lain (mis. pencarian) tidak disentuh.
- Atribut seperti \`required\` dan \`type="email"\` tetap diperiksa browser sebelum form dikirim.
- Batas: maksimal 30 isian per form, masing-masing maksimal 5.000 karakter.
- Tanpa \`data-ws-form-alert\`, pesan berhasil tampil sebagai notifikasi kecil di pojok layar.
- Elemen \`data-ws-form\` yang **tidak berisi** \`<form>\` akan diisi form pendaftaran bawaan (nama, kelas, WhatsApp, pesan).

## Portal Pengurus

Alamat: \`orgspace.app/nama-website/admin\`. Pengurus masuk dengan **username dan password akses Portal** organisasi pemilik website. Ada tiga akses: Pemilik (wajib), Admin, dan Editor. Pemilik organisasi mengaturnya di app OrgSpace (Kelola organisasi → Akses Portal website), dan satu akses berlaku untuk semua website organisasi itu. Akun OrgSpace pribadi (email/Google/GitHub) dan akses organisasi lain tidak bisa dipakai di sini.

| Tab | Isi |
|---|---|
| Ringkasan | Jumlah isian form, berita, item modul kustom, dan akses yang sedang dipakai |
| Kotak Masuk Form | Semua isian dari setiap \`data-ws-form\` (hanya Pemilik & Admin) |
| Berita & Agenda | Tambah dan hapus pengumuman |
| Modul Kustom | Daftar modul dari template beserta field-nya; tambah, edit, hapus, urutkan data, dan unggah foto |

Sesi Portal Pengurus berlaku 8 jam dan **hanya untuk satu website itu**. Saat Pemilik mengganti password suatu akses, semua yang sedang masuk dengan akses itu langsung keluar.

## JavaScript template

JavaScript template **dijalankan**: hamburger menu, dark mode, slider, tab, accordion, Tailwind CDN, Alpine, Swiper, AOS, dan library lain dari CDN yang diizinkan.

Semua website tayang di satu domain (\`orgspace.app/...\`), jadi setiap halaman dijalankan di **sandbox** browser: script template tidak bisa membaca data \`orgspace.app\`, Portal Pengurus, atau website lain. Akibatnya:

| Hal | Perilaku |
|---|---|
| \`localStorage\`, \`sessionStorage\`, cookie | Bisa dipakai, tapi **hanya sementara**: hilang saat halaman ditutup atau pindah halaman. Mis. pilihan dark mode tidak diingat. |
| Status login pengurus di halaman publik | Tidak tersedia (lihat bagian 1). |
| \`fetch()\` / XHR ke situs luar | **Ditolak** saat pengecekan upload (\`EXT_FETCH\`). Pakai modul \`data-ws-*\`, atau \`fetch\` ke file lokal di ZIP (\`data/menu.json\`). |
| Atribut \`onclick\`, \`onload\`, \`onerror\`, dst. dan link \`javascript:\` | **Dihapus** saat website diterbitkan. Pasang event di file JS dengan \`addEventListener\`. |

## JavaScript + data modul

Script template berjalan **sebelum** OrgSpace mengisi modul. Jika script-mu perlu kartu atau data modul (filter, slider, grafik, peta), tunggu event \`orgspace:siap\`:

\`\`\`js
function mulai() {
  // Data modul (salinan): window.orgspace.data
  //   .slug          nama website
  //   .preview       true jika halaman ini preview template di galeri (data contoh, form tidak dikirim)
  //   .news          pengumuman/berita: [{ judul, ringkasan, konten, tanggal, foto, detail }]
  //   .collections   modul kustom: { prestasi: [{ judul, tingkat, ... }], agenda: [...] }
  //   .stats         angka: { pengurus: 12, berita: 5, prestasi: 18, "form.pendaftaran": 40 }
  //   .org           profil organisasi: { nama, kategori, kota, whatsapp, logo, ... }
  var prestasi = window.orgspace.data.collections.prestasi || [];
  console.log(prestasi.length + " prestasi");
}
if (window.orgspace && window.orgspace.siap) mulai();
else document.addEventListener("orgspace:siap", mulai);
\`\`\`

- Setelah event ini, kartu \`data-ws-item\` sudah terisi dan bisa dipilih dengan \`querySelectorAll\`.
- Contoh lengkap: **filter prestasi per tingkat** di \`contoh-template/js/main.js\` pada paket unduhan.
- Contoh template lengkap & interaktif: \`template-sinergi/\` (statistik naik saat di-scroll, chip filter dari data, agenda
  diurutkan per tanggal + hitung mundur, lightbox galeri, form 2 langkah, pencarian pengurus, tema gelap).
- Tanggal modul (\`data-ws-type="tanggal"\`) di data mentah berbentuk \`2026-10-19\`; teks di kartu sudah diubah menjadi
  "19 Oktober 2026". Pakai data mentah untuk menghitung atau mengurutkan.
- Event ini ada di website yang tayang di OrgSpace dan di preview galeri (\`orgspace.app/demo/{id}/\`, berisi data contoh). Di preview app atau file ZIP export, \`window.orgspace\` tidak ada, jadi tulis script yang tetap aman tanpa data (mis. filter tidak dibuat).

## Banyak halaman

Template boleh punya beberapa halaman (\`index.html\`, \`tentang.html\`, \`kontak.html\`). Hubungkan dengan link relatif biasa, mis. \`<a href="tentang.html">\`. Hasilnya \`orgspace.app/nama-website/tentang.html\`. Modul dan formulir bekerja di semua halaman.

- Maksimal 10 halaman HTML, dan setiap halaman harus bisa dibuka dari menu.
- Modul yang sama boleh dipakai di beberapa halaman dan datanya tetap satu. Mis. beranda menampilkan 3 agenda (\`data-ws-limit="3"\`) dengan link ke \`agenda.html\` yang menampilkan semuanya. Portal Pengurus menampilkan gabungan field dari semua halaman.
- Halaman detail per item (mis. \`agenda/123\`) tidak tersedia. Pakai kartu yang bisa dibuka (\`<details>\` atau dialog) atau \`data-ws-link\` ke link luar.

## Modul dan langkah Tandai

Bagian yang diisi modul **tidak bisa ditandai** di app, karena isinya diganti data saat website tayang: elemen ber-\`data-ws-field\`, \`data-ws-stat\`, \`data-ws-org\`, link/tombol ber-\`data-ws-link\` atau \`data-ws-auth\`, dan teks yang membungkus modul. Teks statis di sekitarnya (judul section, label formulir, teks \`data-ws-empty\`) tetap boleh ditandai. Detail: [Menandai bagian template](/docs/menandai-bagian-template).

## Cek sebelum upload

- [ ] Setiap \`data-ws-list\` / \`data-ws-collection\` punya **tepat satu** \`data-ws-item\`.
- [ ] Setiap \`data-ws-item\` punya minimal satu \`data-ws-field\`.
- [ ] \`data-ws-field\`, \`data-ws-link\`, dan \`data-ws-empty\` berada di dalam daftarnya, bukan di luar.
- [ ] \`data-ws-limit\` berisi angka 1–100.
- [ ] \`data-ws-stat\` memakai \`pengurus\`, \`berita\`, \`form.{nama}\`, atau nama modul kustom; \`data-ws-org\` memakai nama dari daftar bagian 3a.
- [ ] Setiap isian form punya atribut \`name\`.
- [ ] Tombol Portal Pengurus memakai \`<a href="admin" data-ws-auth="login">\`.
- [ ] Tidak ada \`onclick=...\` atau \`href="javascript:..."\`; event dipasang dengan \`addEventListener\` di file JS.
- [ ] Script tidak bergantung pada data yang harus tersimpan permanen di \`localStorage\` atau cookie.

## Masalah yang sering terjadi

| Gejala | Penyebab & solusi |
|---|---|
| Upload gagal: "tidak memiliki elemen data-ws-item" | Tambahkan \`data-ws-item\` pada satu kartu contoh di dalam daftar. |
| Upload gagal: "hanya 1 kartu contoh yang diizinkan" | Hapus kartu contoh lain; OrgSpace yang mengulang kartunya. |
| Daftar menampilkan isi contoh, bukan data | Pengurus belum mengisi data, atau nama field di Portal berbeda dengan \`data-ws-field\`. |
| Foto contoh tetap tampil | Data tidak punya foto, atau alamat fotonya bukan \`http(s)://\`. |
| Isian form tidak masuk Kotak Masuk | Form belum punya \`data-ws-form\`, atau isiannya tidak punya \`name\`. |
| Tombol Keluar atau bagian khusus pengurus tidak terlihat | Memang disembunyikan di halaman publik; pengurus bekerja di Portal Pengurus. |
| Pilihan dark mode hilang setelah halaman ditutup | Penyimpanan browser di website hanya sementara (sandbox). |
| Tombol dengan \`onclick\` tidak bereaksi | Atribut \`on...\` dihapus saat terbit; pasang event dengan \`addEventListener\` di file JS. |
| Script hanya melihat kartu contoh, bukan data asli | Jalankan script setelah event \`orgspace:siap\` (bagian JavaScript + data modul). |
`,
  },
  "membuat-fitur-kustom": {
    id: "doc-membuat-fitur-kustom",
    slug: "membuat-fitur-kustom",
    category: "provider",
    audience: "provider",
    title: "Membuat Fitur Kustom",
    summary: "Rancang fitur sendiri (agenda, galeri, filter, pencarian) dari modul + CSS + JavaScript, lengkap dengan resep dan batasnya.",
    sortOrder: 40,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Penyedia template",
    body: `OrgSpace tidak punya fitur "agenda", "prestasi", atau "galeri" bawaan. Semua fitur seperti itu **dibuat provider sendiri** di
dalam template, dari tiga bahan:

| Bahan | Isinya | Ditulis dengan |
|---|---|---|
| **Data** | Daftar yang diisi pengurus lewat Portal Pengurus | Atribut \`data-ws-collection\` + \`data-ws-field\` (lihat \`01-panduan-modul-dinamis.md\`) |
| **Tampilan** | Bentuk kartu, halaman, menu | HTML + CSS |
| **Perilaku** | Urutkan, filter, cari, hitung mundur, lightbox, slider | JavaScript yang membaca \`window.orgspace.data\` |

OrgSpace hanya menyediakan mesinnya: tempat menyimpan data, form isian di Portal Pengurus, dan pengisian kartu otomatis.
Contoh nyata semua resep di halaman ini ada di \`template-sinergi/\` (lihat \`js/main.js\`).

## 1. Siapa mengerjakan apa

| Pihak | Yang dikerjakan | Di mana |
|---|---|---|
| **Provider** (kamu / AI agent) | Membuat halaman, section, modul, desain, dan JavaScript. Menandai teks/gambar yang boleh diganti | File template + mode Penyedia Template di app |
| **Pembuat website** | Memilih template, mengganti teks/foto/warna yang ditandai, lalu menerbitkan | Editor di app |
| **Pengurus organisasi** | Mengisi, mengedit, menghapus, dan mengurutkan data modul; membaca isian form | Portal Pengurus \`nama-website/admin\` |
| **Pengunjung** | Melihat website dan mengirim form | \`orgspace.app/nama-website\` |

Pembuat website dan pengurus **tidak bisa mengubah layout atau menambah section/modul baru**. Apa pun yang ingin bisa diisi
mereka harus sudah disiapkan provider di template.

## 2. Pilih jenis isi yang tepat

| Jika isinya... | Pakai | Contoh |
|---|---|---|
| Satu teks atau gambar yang jarang berubah | **Isian Tandai** (elemen biasa yang nanti ditandai di app) | Judul hero, visi, foto utama, teks footer |
| Daftar yang bisa bertambah/berkurang | **Modul kustom** \`data-ws-collection\` | Pengurus, program, prestasi, agenda, galeri, FAQ, alumni |
| Berita/pengumuman | **Modul bawaan** \`data-ws-list="pengumuman"\` | Kabar terbaru |
| Kiriman dari pengunjung | **Formulir** \`data-ws-form\` | Pendaftaran, aspirasi, kontak |
| Angka jumlah data | **Statistik** \`data-ws-stat\` | "12 pengurus", "40 pendaftar" |
| Nama, logo, kontak organisasi | **Profil organisasi** \`data-ws-org\` | Header, footer, tombol WhatsApp |
| Warna dan bentuk | **Variabel CSS** di \`:root\` (nanti didaftarkan sebagai tema di app) | \`--primary\`, \`--accent\`, \`--radius\` |

Jangan mengetik daftar pengurus atau angka statistik langsung di HTML. Isi seperti itu harus berupa modul atau statistik
agar pengurus bisa memperbaruinya tanpa terbit ulang.

## 3. Bisa vs tidak bisa

**Bisa**: fitur apa pun yang bentuknya "daftar item ber-field", ditampilkan semenarik mungkin, plus interaksi di browser.
Mis. katalog produk koperasi, jadwal piket, kalender lomba, daftar alumni, sponsor, menu kantin, timeline sejarah.

**Tidak bisa** (butuh logika server; hanya OrgSpace yang bisa menambahkannya sebagai modul bawaan):

| Tidak didukung | Alternatif di template |
|---|---|
| Halaman detail per item (\`agenda/123\`) | Kartu yang bisa dibuka (accordion/dialog) berisi field \`deskripsi\`, atau \`data-ws-link\` ke link luar |
| Relasi antar-modul (agenda milik pengurus X) | Simpan sebagai teks biasa, mis. field \`penanggung_jawab\` |
| Login/akun pengunjung, member area | – |
| Pembayaran, iuran, tiket | Field link ke formulir/pembayaran luar (\`data-ws-link\`) |
| Voting, like, komentar | Formulir \`data-ws-form\` (hasilnya dibaca pengurus di Kotak Masuk) |
| Unggah file oleh pengunjung | Formulir hanya mengirim teks; minta link Google Drive di isian teks |
| Ambil data dari API luar (\`fetch("https://...")\`) | Ditolak pengecekan upload (\`EXT_FETCH\`). Pakai modul, atau file lokal di ZIP (\`fetch("data/menu.json")\`) |

## 4. Rancang dulu sebelum menulis kode

Buat tabel seperti ini, lalu baru tulis HTML-nya:

| Halaman | Section | Jenis | Modul / field |
|---|---|---|---|
| \`index.html\` | Hero | Isian Tandai + statistik | \`data-ws-stat="pengurus"\`, \`data-ws-stat="agenda"\` |
| \`index.html\` | Agenda terdekat | Modul (3 item) | \`agenda\`: nama_kegiatan, tanggal (\`tanggal\`), lokasi, link_daftar (link) |
| \`agenda.html\` | Semua agenda | Modul (semua) | \`agenda\` (sama) |
| \`index.html\` | Kabar terbaru | Modul bawaan | \`pengumuman\` |
| \`tentang.html\` | Struktur pengurus | Modul | \`struktur\`: foto (gambar), nama, jabatan, divisi |
| \`index.html\` | Daftar anggota | Formulir | \`pendaftaran\`: nama, kelas, whatsapp, alasan |

Nama modul dan field: huruf kecil, angka, \`-\` atau \`_\`, 2–40 karakter. Nama yang sama = data yang sama di semua halaman.

## 5. Resep

### Resep 1: Fitur di halaman sendiri + ringkasan di beranda

Satu modul boleh dipakai di beberapa halaman. Datanya tetap satu dan diisi sekali di Portal.

\`\`\`html
<!-- index.html: 3 agenda saja + link ke halaman lengkap -->
<div data-ws-collection="agenda" data-ws-limit="3">
  <article class="card" data-ws-item>
    <h3 data-ws-field="nama_kegiatan">Nama kegiatan</h3>
    <p data-ws-field="tanggal" data-ws-type="tanggal">1 Januari 2026</p>
  </article>
  <p data-ws-empty>Belum ada agenda.</p>
</div>
<a href="agenda.html">Lihat semua agenda</a>

<!-- agenda.html: semua agenda, kartu lebih lengkap -->
<div data-ws-collection="agenda">
  <article class="card" data-ws-item>
    <h3 data-ws-field="nama_kegiatan">Nama kegiatan</h3>
    <p><span data-ws-field="tanggal" data-ws-type="tanggal">1 Januari 2026</span> · <span data-ws-field="lokasi">Lokasi</span></p>
    <p data-ws-field="deskripsi" data-ws-type="paragraf">Deskripsi kegiatan.</p>
    <a href="#" data-ws-link="link_daftar">Daftar kegiatan</a>
  </article>
  <p data-ws-empty>Belum ada agenda.</p>
</div>
\`\`\`

- Maksimal **10 halaman HTML** per template. Setiap halaman harus bisa dibuka dari menu (link relatif \`agenda.html\`).
- Gabungan field dari semua halaman yang muncul di Portal. Di contoh ini Portal menampilkan nama_kegiatan, tanggal,
  lokasi, deskripsi, dan link_daftar.

### Pola dasar script yang butuh data

Data modul diisi OrgSpace **setelah** script template jalan. Pakai pola ini di setiap template:

\`\`\`js
(function () {
  var started = false;
  function start() {
    if (started) return;
    started = true;
    var data = window.orgspace && window.orgspace.data ? window.orgspace.data : null; // null di preview/export
    // ...panggil fitur di sini
  }
  document.addEventListener("orgspace:siap", start); // website tayang: setelah modul terisi
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", function () { setTimeout(start, 0); });
  else setTimeout(start, 0); // preview/laptop: jalan dengan isi contoh
})();
\`\`\`

Kartu yang tidak dipakai disembunyikan OrgSpace dengan \`style.display = "none"\`. Saat memilih kartu, lewati yang
tersembunyi:

\`\`\`js
function visibleItems(list) {
  return Array.prototype.filter.call(list.querySelectorAll("[data-ws-item]"), function (item) {
    return item.style.display !== "none";
  });
}
\`\`\`

### Resep 2: Agenda terurut + "hari lagi"

Tanggal di kartu sudah berupa teks "19 Oktober 2026". Untuk menghitung, pakai data mentah \`2026-10-19\`. Urutan data mentah
sama dengan urutan kartu.

\`\`\`js
function markAgenda(data) {
  var list = document.querySelector('[data-ws-collection="agenda"]');
  var rows = data && data.collections.agenda ? data.collections.agenda : [];
  if (!list || !rows.length) return;
  var items = visibleItems(list), today = new Date();
  today.setHours(0, 0, 0, 0);
  var dated = [];
  rows.forEach(function (row, i) {
    if (!items[i] || !/^\\d{4}-\\d{2}-\\d{2}$/.test(row.tanggal || "")) return;
    var date = new Date(row.tanggal + "T00:00:00");
    items[i].classList.toggle("lewat", date < today);
    var tag = document.createElement("span");
    var days = Math.round((date - today) / 86400000);
    tag.textContent = days < 0 ? "Selesai" : days === 0 ? "Hari ini" : days + " hari lagi";
    items[i].appendChild(tag);
    dated.push({ date: date, item: items[i] });
  });
  var empty = list.querySelector("[data-ws-empty]");
  dated.sort(function (a, b) { return a.date - b.date; })
       .forEach(function (d) { list.insertBefore(d.item, empty); });
}
\`\`\`

### Resep 3: Chip filter yang dibuat dari isi data

\`\`\`html
<div class="chips" data-filter="prestasi" data-filter-field="tingkat"></div>
<div data-ws-collection="prestasi">
  <article data-ws-item>
    <span data-ws-field="tingkat">Nasional</span>
    <h3 data-ws-field="judul">Juara 1 Lomba</h3>
  </article>
</div>
\`\`\`

\`\`\`js
function buildFilters() {
  document.querySelectorAll(".chips[data-filter]").forEach(function (chips) {
    var list = document.querySelector('[data-ws-collection="' + chips.dataset.filter + '"]');
    if (!list) return;
    var items = visibleItems(list), field = chips.dataset.filterField;
    var valueOf = function (item) {
      var el = item.querySelector('[data-ws-field="' + field + '"]');
      return el ? el.textContent.trim() : "";
    };
    var values = [];
    items.forEach(function (it) { var v = valueOf(it); if (v && values.indexOf(v) < 0) values.push(v); });
    if (values.length < 2) return; // tidak perlu filter
    ["Semua"].concat(values).forEach(function (v, i) {
      var chip = document.createElement("button");
      chip.type = "button";
      chip.textContent = v;
      chip.setAttribute("aria-pressed", String(i === 0));
      chip.addEventListener("click", function () {
        chips.querySelectorAll("button").forEach(function (c) { c.setAttribute("aria-pressed", String(c === chip)); });
        items.forEach(function (it) { it.hidden = i !== 0 && valueOf(it) !== v; });
      });
      chips.appendChild(chip);
    });
  });
}
\`\`\`

### Resep 4: Pencarian

\`\`\`html
<input type="search" id="cari" placeholder="Cari nama atau jabatan" aria-label="Cari pengurus">
<p id="tidak-ketemu" hidden>Tidak ada yang cocok.</p>
\`\`\`

\`\`\`js
function setupSearch() {
  var input = document.getElementById("cari");
  var list = document.querySelector('[data-ws-collection="struktur"]');
  if (!input || !list) return;
  var items = visibleItems(list);
  input.addEventListener("input", function () {
    var q = input.value.trim().toLowerCase(), shown = 0;
    items.forEach(function (it) {
      it.hidden = it.textContent.toLowerCase().indexOf(q) < 0;
      if (!it.hidden) shown++;
    });
    document.getElementById("tidak-ketemu").hidden = shown > 0;
  });
}
\`\`\`

### Resep 5: Galeri + lightbox tanpa library

\`\`\`html
<div data-ws-collection="galeri">
  <button class="shot" type="button" data-ws-item>
    <img src="img/galeri-1.jpg" alt="Foto kegiatan" data-ws-field="foto">
    <span data-ws-field="keterangan">Keterangan foto</span>
  </button>
</div>
<dialog id="lightbox">
  <img id="lightbox-img" alt="">
  <p id="lightbox-caption"></p>
  <button type="button" data-close>Tutup</button>
</dialog>
\`\`\`

\`\`\`js
var box = document.getElementById("lightbox");
document.addEventListener("click", function (e) {
  var shot = e.target.closest(".shot");
  if (shot && box.showModal) {
    var img = shot.querySelector("img");
    document.getElementById("lightbox-img").src = img.src;
    document.getElementById("lightbox-img").alt = img.alt;
    document.getElementById("lightbox-caption").textContent = shot.textContent.trim();
    box.showModal();
  }
  if (e.target === box || e.target.closest("[data-close]")) box.close();
});
\`\`\`

\`document.addEventListener\` + \`closest()\` tetap berfungsi untuk kartu yang baru ditambahkan OrgSpace, jadi resep ini
tidak perlu menunggu \`orgspace:siap\`.

### Resep 6: FAQ / detail yang bisa dibuka tanpa JavaScript

Pengganti "halaman detail per item":

\`\`\`html
<div data-ws-collection="faq">
  <details data-ws-item>
    <summary data-ws-field="pertanyaan">Pertanyaan</summary>
    <p data-ws-field="jawaban" data-ws-type="paragraf">Jawaban.</p>
  </details>
</div>
\`\`\`

### Resep 7: Statistik dan profil organisasi

\`\`\`html
<p><strong data-ws-stat="pengurus">12</strong> pengurus ·
   <strong data-ws-stat="prestasi">18</strong> prestasi ·
   <strong data-ws-stat="form.pendaftaran">40</strong> pendaftar</p>

<footer>
  <img src="img/logo.svg" alt="Logo organisasi" data-ws-org="logo">
  <span data-ws-org="nama">Nama Organisasi</span>
  <div data-org-group>
    <a href="#" data-ws-org="whatsapp">WhatsApp</a>
    <a href="#" data-ws-org="instagram">Instagram</a>
  </div>
</footer>
\`\`\`

Profil yang belum diisi organisasi disembunyikan. Jika semua kontak kosong, sembunyikan juga pembungkusnya:

\`\`\`js
document.querySelectorAll("[data-org-group]").forEach(function (g) {
  var items = g.querySelectorAll("[data-ws-org]");
  g.hidden = items.length > 0 && Array.prototype.every.call(items, function (el) { return el.style.display === "none"; });
});
\`\`\`

### Resep 8: Animasi angka statistik

\`\`\`js
function countUp() {
  if (!("IntersectionObserver" in window)) return;
  var io = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (!entry.isIntersecting) return;
      io.unobserve(entry.target);
      var el = entry.target, target = parseInt(el.textContent, 10) || 0, t0 = null;
      requestAnimationFrame(function frame(t) {
        if (t0 === null) t0 = t;
        var p = Math.min((t - t0) / 1200, 1);
        el.textContent = String(Math.round(target * p));
        if (p < 1) requestAnimationFrame(frame);
      });
    });
  }, { threshold: 0.6 });
  document.querySelectorAll("[data-ws-stat]").forEach(function (el) { io.observe(el); });
}
\`\`\`

Panggil di \`start()\` agar angka yang dihitung adalah angka dari database, bukan angka contoh.

## 6. Siapkan untuk pembuat website

- **Teks dan gambar di luar modul** (judul hero, deskripsi, visi, misi, foto utama, teks tombol, footer) ditulis langsung
  di HTML. Setelah upload, provider menandainya di mode **Tandai** di app agar pembuat website bisa menggantinya.
  Beri \`id\` yang jelas (\`hero-judul\`, \`visi-teks\`) agar mudah dikenali saat menandai.
- **Warna dan bentuk** ditulis sebagai variabel di \`:root\` dan dipakai di seluruh CSS:
  \`\`\`css
  :root { --primary: #4f46e5; --accent: #f59e0b; --radius: 16px; }
  .btn { background: var(--primary); border-radius: var(--radius); }
  \`\`\`
  Saat Tandai, variabel ini didaftarkan sebagai tema sehingga pembuat website bisa memilih warnanya sendiri.
- Teks contoh netral ("Nama Organisasi", "Judul pengumuman"), karena semua akan diganti.

## 7. Setelah ZIP jadi

Di app OrgSpace: mode **Penyedia Template** → tab **Upload**:

1. **Pilih ZIP**. Pengecekan otomatis menampilkan error (wajib diperbaiki) dan peringatan (sebaiknya diperbaiki)
   lengkap dengan file, baris, dan cara memperbaikinya.
2. **Info**: nama, kategori organisasi, deskripsi, maksimal 5 kata kunci.
3. **Thumbnail**: gambar sampul di galeri.
4. **Tandai**: pilih teks/gambar yang boleh diganti pembuat website (minimal 3), kelompokkan per section, dan daftarkan
   variabel tema.
5. **Coba** lalu **Kirim**. Template tayang di galeri setelah lolos pengecekan akhir.
`,
  },
  "tampilan-portal-pengurus": {
    id: "doc-tampilan-portal-pengurus",
    slug: "tampilan-portal-pengurus",
    category: "provider",
    audience: "provider",
    title: "Mengatur Tampilan Portal Pengurus",
    summary: "File portal.json opsional: warna, susunan menu, header banner, kartu/tabel, label field, dan menu HP untuk Portal Pengurus.",
    sortOrder: 60,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Penyedia template",
    body: `Portal Pengurus (\`orgspace.app/nama-website/admin\`) adalah dashboard tempat pengurus mengisi data modul website. Semua website memakai Portal yang sama. Lewat file **\`portal.json\`** kamu bisa mengatur **tampilan** Portal agar serasi dengan template: warna, menu, header, dan cara data ditampilkan. **Fitur Portal tidak berubah**: kamu hanya memilih dari pilihan yang disediakan OrgSpace, tidak menulis kode.

\`portal.json\` **opsional**. Tanpa file ini Portal tetap rapi: setiap modul \`data-ws-collection\` otomatis menjadi menu sendiri di sidebar.

## Letak file

Taruh \`portal.json\` di folder paling luar ZIP, sejajar \`index.html\`:

\`\`\`text
template-saya.zip
├── index.html
├── agenda.html
├── css/  js/  img/
└── portal.json
\`\`\`

File ini dicek saat upload. Jika ada yang salah, pengecekan gagal dengan kode **\`PORTAL_CONFIG_INVALID\`** dan pesan yang menyebut bagian mana yang keliru. Perbaiki lalu upload ulang, atau hapus file ini untuk memakai tampilan standar.

## Contoh lengkap

Contoh dari template **Pramuka Tunas Rimba**:

\`\`\`json
{
  "version": 1,
  "theme": { "primary": "#2F6B3F", "accent": "#6B4F2A", "radius": 10 },
  "layout": {
    "sidebar": { "position": "left", "width": 268, "resizable": true },
    "header": {
      "variant": "banner",
      "image": "img/hero.jpg",
      "title": "Pangkalan Tunas Rimba",
      "subtitle": "Kelola agenda, jadwal latihan, pembina, prestasi, dan galeri."
    }
  },
  "nav": [
    { "builtin": "overview", "label": "Ringkasan", "icon": "compass" },
    { "group": "Kegiatan", "items": [
      { "builtin": "news", "label": "Kabar Kegiatan", "icon": "megaphone" },
      { "module": "agenda", "label": "Agenda", "icon": "calendar" },
      { "module": "jadwal", "label": "Jadwal Latihan", "icon": "clipboard-list" }
    ]},
    { "builtin": "submissions", "label": "Pendaftar Baru", "icon": "inbox" }
  ],
  "modules": {
    "agenda": {
      "description": "Kegiatan mendatang yang tampil di beranda.",
      "view": "kartu",
      "title": "judul",
      "subtitle": "tanggal",
      "fields": {
        "judul": { "label": "Nama kegiatan" },
        "waktu": { "label": "Jam", "help": "Contoh: 07.00–12.00 WIB" }
      },
      "groups": [
        { "title": "Waktu & tempat", "fields": ["judul", "tanggal", "waktu", "lokasi"] },
        { "title": "Detail", "fields": ["golongan", "deskripsi"] }
      ]
    },
    "jadwal": { "view": "tabel" }
  },
  "mobile": { "nav": "bottom", "bottom": ["overview", "agenda", "news", "submissions"] }
}
\`\`\`

Semua bagian boleh dihilangkan. Bagian yang tidak ditulis memakai tampilan standar.

## Pilihan yang tersedia

### \`theme\`: warna dan sudut

| Kunci | Nilai | Keterangan |
|---|---|---|
| \`primary\` | Hex 6 digit, mis. \`#2F6B3F\` | Warna tombol, menu aktif, dan aksen. Harus cukup gelap: teks putih di atasnya wajib terbaca (kontras minimal 3:1) |
| \`accent\` | Hex 6 digit | Warna aksen kedua. Aturan kontras sama |
| \`radius\` | Angka 0–24 | Sudut kartu, tombol, dan isian (px) |

Mode gelap memakai versi lebih terang dari warnamu secara otomatis. Warna merah untuk hapus/error dan hijau untuk sukses tidak bisa diganti.

### \`layout.sidebar\`: menu samping (desktop)

| Kunci | Nilai |
|---|---|
| \`position\` | \`left\` atau \`right\` |
| \`width\` | Angka 200–360 (px), lebar awal |
| \`resizable\` | \`true\` agar pengurus bisa menggeser lebar menu. Lebar hasil geser disimpan di browser masing-masing pengurus |

### \`layout.header\`: header halaman

| Kunci | Nilai |
|---|---|
| \`variant\` | \`minimal\` (tanpa header besar) atau \`banner\` |
| \`image\` | Path gambar di ZIP (png/jpg/webp), mis. \`img/hero.jpg\`. Dipakai sebagai latar banner, otomatis digelapkan agar teks terbaca |
| \`title\` | Teks 1–60 karakter. Bawaan: nama organisasi |
| \`subtitle\` | Teks 1–140 karakter |

### \`nav\`: susunan menu

Daftar menu dari atas ke bawah. Setiap isi berupa salah satu:

| Bentuk | Contoh |
|---|---|
| Menu bawaan | \`{ "builtin": "overview" }\`. Pilihan: \`overview\` (Ringkasan), \`news\` (Berita), \`submissions\` (Kotak Masuk Form) |
| Menu modul | \`{ "module": "agenda" }\`. Nama harus ada sebagai \`data-ws-collection\` di HTML |
| Grup | \`{ "group": "Profil", "items": [ ...menu... ] }\`. Satu tingkat saja |

Setiap menu boleh diberi \`label\` (maks. 40 karakter) dan \`icon\`. Setiap menu hanya boleh muncul sekali. Menu yang tidak kamu sebut tetap muncul di grup **Lainnya**, agar data pengurus tidak pernah terkunci. Kotak Masuk Form tetap tersembunyi untuk peran Editor walau kamu menaruhnya di \`nav\`.

**Ikon yang tersedia:** \`layout-dashboard\`, \`inbox\`, \`newspaper\`, \`calendar\`, \`users\`, \`user\`, \`trophy\`, \`award\`, \`star\`, \`image\`, \`camera\`, \`target\`, \`flag\`, \`book-open\`, \`graduation-cap\`, \`briefcase\`, \`handshake\`, \`heart\`, \`map-pin\`, \`megaphone\`, \`clipboard-list\`, \`file-text\`, \`folder\`, \`list\`, \`boxes\`, \`tent\`, \`compass\`, \`music\`, \`sparkles\`, \`mail\`, \`link\`.

### \`modules\`: halaman tiap modul

Kunci = nama modul. Isi:

| Kunci | Nilai |
|---|---|
| \`label\` | Judul halaman modul (maks. 40 karakter) |
| \`icon\` | Ikon modul (dipakai jika \`nav\` tidak memberi ikon) |
| \`description\` | Penjelasan singkat di bawah judul (maks. 140 karakter) |
| \`view\` | \`kartu\` (bawaan) atau \`tabel\`. Di HP selalu kartu |
| \`title\`, \`subtitle\` | Nama field untuk judul dan subjudul kartu |
| \`image\` | Nama field gambar (\`<img data-ws-field>\`) untuk foto kartu |
| \`fields\` | Per field: \`label\` (maks. 40) dan \`help\` (teks bantuan, maks. 140) |
| \`groups\` | Kelompok field di formulir: \`[{ "title": "...", "fields": ["..."] }]\`. Field yang tidak masuk grup ditaruh di akhir |

Tanpa \`label\`, nama field ditampilkan rapi otomatis: \`nama_kegiatan\` menjadi "Nama Kegiatan".

### \`mobile\`: tampilan HP

| Kunci | Nilai |
|---|---|
| \`nav\` | \`drawer\` (bawaan, menu geser dari samping) atau \`bottom\` (menu bawah seperti app) |
| \`bottom\` | Maks. 4 menu untuk menu bawah: \`overview\`, \`news\`, \`submissions\`, atau nama modul. Menu lain ada di tombol **Lainnya** |

## Yang tidak bisa diatur

Login Portal, tombol keluar, nama dan peran pengurus, tombol "Lihat Website", dan mode gelap selalu tampil sama di semua website. Ini menjaga pengurus selalu tahu sedang berada di Portal OrgSpace yang asli. \`portal.json\` juga tidak bisa menambah fitur baru, menjalankan JavaScript, atau memuat CSS sendiri.

## Mencoba hasilnya

Setelah template tayang, buka Portal demo di \`/demo/{id}/admin\` (atau tombol login pengurus \`data-ws-auth="login"\` di preview template). Portal demo memakai \`portal.json\` yang sama dengan website asli, dengan data contoh.
`,
  },
  "menandai-bagian-template": {
    id: "doc-menandai-bagian-template",
    slug: "menandai-bagian-template",
    category: "provider",
    audience: "provider",
    title: "Menandai Bagian Template",
    summary: "Langkah Tandai: jenis isian, gaya, tema warna, hubungan ke profil organisasi, dan aturan elemen modul.",
    sortOrder: 50,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Penyedia template",
    body: `Di langkah **Tandai** (Upload langkah 4), kamu memilih elemen yang boleh diganti pembuat website. Elemen yang tidak ditandai tidak bisa diubah, dan **tata letak selalu terkunci**.

## Dua jenis isi di template OrgSpace

| Jenis | Siapa yang mengisi | Contoh | Cara membuat |
|---|---|---|---|
| **Isian Tandai** | Pembuat website, di editor app, lalu diterbitkan | Judul hero, visi-misi, foto utama, teks footer | Elemen HTML biasa, ditandai di app |
| **Data modul** | Pengurus, di Portal Pengurus, tanpa terbit ulang | Berita, agenda, prestasi, galeri, isian formulir | Atribut [\`data-ws-*\`](/docs/modul-sistem-dan-kustom) di HTML |

## Cara menandai

1. Tandai per **halaman** dan per **section** (ditampilkan seperti slide). Ganti tampilan **HP / Desktop** untuk elemen yang hanya tampil di salah satunya.
2. Ketuk elemen. Pakai **Induk ↑** / **Anak ↓** untuk memperluas atau mempersempit pilihan, atau pilih dari daftar elemen (untuk menu tersembunyi, isi tab, dan elemen kecil).
3. Atur isian di lembar bawah, lalu **Simpan**. Tandaan disimpan ke server lewat tombol Simpan, dengan cadangan otomatis di HP.

| Pengaturan | Keterangan |
|---|---|
| Jenis | **Teks pendek**, **Paragraf**, **Gambar**, **Link**, atau **Tombol** (teks + link). Ditebak otomatis |
| Label | Wajib. Nama isian di editor, mis. "Judul utama" |
| Petunjuk | Opsional. Bantuan untuk pembuat website, mis. "Maks 3 kata" |
| Maks karakter / wajib / rasio gambar | Disarankan otomatis dari isi asli agar tata letak tidak rusak |
| Juga boleh diubah (gaya) | Warna teks, warna latar, ukuran huruf (dengan rentang), sudut |
| Hubungkan ke isian lain | Teks yang sama di beberapa tempat (versi HP & desktop, header di semua halaman) menjadi satu isian |
| Hubungkan ke profil organisasi | Nama, singkatan, logo, institusi, alamat, email, WhatsApp, Instagram, TikTok, YouTube terisi otomatis. Centang **Sembunyikan jika kosong** untuk kontak dan media sosial |

## Tema

Variabel CSS di \`:root\` (mis. \`--primary\`, \`--accent\`, \`--radius\`) bisa didaftarkan sebagai **tema** dengan jenis **warna** atau **ukuran**. Pembuat website mengubahnya di tab Gaya → **Tema website**, dan seluruh template ikut berubah.

\`\`\`css
:root {
  --primary: #2563eb;
  --accent: #f59e0b;
  --radius: 12px;
}
.tombol { background: var(--primary); border-radius: var(--radius); }
\`\`\`

## Aturan

- Minimal **3 isian** sebelum template bisa dikirim, dan minimal satu pilihan **isi template** di langkah Info.
- **Teks pendek, Paragraf, dan Tombol** mengganti seluruh isi elemen, jadi tidak boleh membungkus elemen lain yang ditandai. **Gambar** dan **Link** hanya mengubah \`src\`/\`href\`, jadi boleh membungkus.
- Elemen yang dibuat JavaScript (tidak ada di HTML asli) **tidak bisa ditandai**. Tulis isinya langsung di HTML.
- Elemen modul dinamis **tidak bisa ditandai**, karena isinya diisi otomatis saat website tayang:
  - elemen ber-\`data-ws-field\`, \`data-ws-stat\`, atau \`data-ws-org\` (beserta isinya);
  - **Link/Tombol** pada elemen ber-\`data-ws-link\` atau \`data-ws-auth\` (jenis Teks pendek masih boleh);
  - Teks/Paragraf/Tombol yang **membungkus** modul dinamis.
- Label formulir, teks \`data-ws-empty\`, dan teks statis lain di dalam kartu tetap boleh ditandai.

## Mencoba hasilnya

Di langkah **Coba sebagai pengguna**, kamu melihat form edit persis seperti pembuat website. Isian yang kurang pas bisa langsung dibuka kembali di langkah Tandai.
`,
  },
  "pertanyaan-umum": {
    id: "doc-pertanyaan-umum",
    slug: "pertanyaan-umum",
    category: "faq",
    audience: "all",
    title: "Tanya Jawab Umum",
    summary: "Solusi masalah yang sering terjadi: undangan, Portal Pengurus, terbit, domain, dan pengecekan template.",
    sortOrder: 10,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Tanya jawab",
    body: `## Akun & ruang kerja

| Pertanyaan | Jawaban |
|---|---|
| Kode undangan ditolak | Kode hanya untuk satu orang dan berlaku 1 jam. Minta kode baru ke Admin/Pemilik |
| Tidak bisa membuat undangan Admin/Editor | Pemilik belum mengatur akses Portal peran itu. Lihat [Akses Portal Pengurus](/docs/akses-portal-pengurus) |
| Tidak bisa keluar dari organisasi | Kamu Pemilik terakhir. Lakukan [serah terima](/docs/pergantian-pengurus) dulu |
| Ruang pribadi tidak bisa mengundang | Memang begitu. Buat organisasi jika ingin bekerja bersama |

## Portal Pengurus

| Pertanyaan | Jawaban |
|---|---|
| "Password Portal belum diatur" | Pemilik organisasi mengatur username & password di app: Kelola organisasi → Akses Portal website |
| "Username atau password salah" padahal benar | Pastikan memakai akses Portal organisasi pemilik website itu, bukan akun OrgSpace (email/Google). Username Editor tidak bisa dipakai dengan password Admin |
| Tiba-tiba keluar dari Portal | Sesi berlaku 8 jam, atau Pemilik baru mengganti password peranmu |
| Tab Kotak Masuk Form tidak ada | Tab ini hanya untuk Pemilik dan Admin |
| Data di Portal tidak tampil di website | Pastikan isian sesuai field di template. Isian kosong menampilkan isi contoh |

## Website

| Pertanyaan | Jawaban |
|---|---|
| Tidak bisa menerbitkan | Hanya Pemilik/Admin. Editor memakai **Minta diterbitkan** |
| Nama website tidak bisa diganti | Alamat dikunci setelah terbit pertama agar link lama tetap jalan |
| Foto berubah jadi foto contoh setelah terbit | File foto sudah tidak ada di HP. Pilih ulang foto di editor, lalu terbitkan lagi |
| Berita/formulir di file ZIP menampilkan isi contoh | Modul dinamis hanya bekerja di website yang tayang di OrgSpace |
| Domain belum aktif | DNS butuh waktu. Lihat [Domain sendiri](/docs/domain-kustom) |

## Template (kreator)

| Pertanyaan | Jawaban |
|---|---|
| \`NO_INDEX\` | \`index.html\` harus di akar ZIP. ZIP **isi** folder, bukan foldernya |
| \`JS_RENDERED_CONTENT\` / \`COMPONENT_FRAMEWORK\` | Isi halaman dibuat JavaScript (React/Vue/SPA). Tulis konten di HTML |
| \`CDN_VERSION_UNCLEAR\` / \`CDN_NOT_ALLOWED\` | Tulis versi lengkap di URL dan pakai library dari daftar resmi |
| \`EXT_FETCH\` | JavaScript mengambil data dari situs lain. Pakai modul \`data-ws-*\` atau file JSON lokal |
| \`DYNAMIC_MODULE_INVALID\` | Penanda \`data-ws-*\` salah. Lihat "Cek sebelum upload" di [Panduan Modul Dinamis](/docs/modul-sistem-dan-kustom) |
| Elemen tidak bisa ditandai | Elemen dibuat JavaScript atau berada di modul dinamis. Lihat [Menandai bagian template](/docs/menandai-bagian-template) |
| Tombol upload baru nonaktif | Sudah ada 5 draft. Selesaikan atau hapus draft dulu |
| Template tidak muncul di galeri | Status masih Draft, Sedang dicek, atau Tidak lolos. Cek tab Upload dan Template Anda |

Daftar lengkap kode pengecekan: [Requirement project & aturan pengecekan](/docs/requirement-project-dan-tools).
`,
  },
  "kirim-kritik-dan-saran": {
    id: "doc-kirim-kritik-dan-saran",
    slug: "kirim-kritik-dan-saran",
    category: "faq",
    audience: "all",
    title: "Mengirim Kritik & Saran",
    summary: "Cara melaporkan masalah dan memberi masukan agar cepat ditangani.",
    sortOrder: 20,
    status: "published",
    publishedAt: "2026-10-28T00:00:00Z",
    updatedAt: "2026-10-28T00:00:00Z",
    categoryLabel: "Tanya jawab",
    body: `## Mengirim kritik & saran

Menu **Kritik & saran** di app sedang disiapkan. Nantinya kamu bisa memilih jenisnya (bug, saran, pertanyaan, keluhan), menulis pesan, dan menerima balasan tim OrgSpace di percakapan yang sama.

Untuk masalah di **pengecekan upload template** yang menurutmu keliru, gunakan tombol laporkan di layar hasil pengecekan.

## Agar laporan cepat ditangani

- Tulis langkah yang kamu lakukan sebelum masalah muncul.
- Sebutkan nama website, alamatnya (\`orgspace.app/...\`), atau nama template yang bermasalah.
- Sertakan pesan error persis seperti yang tampil, dan kode pengecekan jika ada (mis. \`EXT_FETCH\`).
- Untuk masalah tampilan, sebutkan HP atau browser yang dipakai.

Sebelum melapor, cek dulu [Tanya jawab umum](/docs/pertanyaan-umum).
`,
  },
};

/**
 * Menggabungkan pohon dokumen dari server dengan daftar panduan komprehensif,
 * sehingga pengguna selalu melihat daftar lengkap requirement, modul, dan cara publikasi.
 */
export function mergeDocTree(serverTree: DocTree): DocTree {
  const mergedCategories = serverTree.categories.map((cat) => ({
    ...cat,
    articles: [...cat.articles],
  }));

  // Slug dicek di semua kategori: artikel yang dipindah admin ke kategori lain tidak boleh muncul dua kali.
  const serverSlugs = new Set(serverTree.categories.flatMap((cat) => cat.articles.map((a) => a.slug)));

  for (const fallbackCat of FALLBACK_DOC_TREE.categories) {
    const missing = fallbackCat.articles.filter((art) => !serverSlugs.has(art.slug));
    if (missing.length === 0) continue;
    let targetCat = mergedCategories.find((c) => c.value === fallbackCat.value);
    if (!targetCat) {
      targetCat = { ...fallbackCat, articles: [] };
      mergedCategories.push(targetCat);
    }
    targetCat.articles.push(...missing);
  }

  return { categories: mergedCategories };
}
