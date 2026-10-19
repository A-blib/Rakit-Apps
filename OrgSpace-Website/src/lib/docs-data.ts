import type { DocDetail, DocTree } from "./api/types";

/**
 * Data dokumentasi komprehensif OrgSpace (fallback & konten mandiri web portal).
 * Mencakup requirement proyek, tools yang didukung, modul sistem & kustom, serta panduan meng-online-kan website.
 */

export const FALLBACK_DOC_TREE: DocTree = {
  categories: [
    {
      value: "mulai",
      label: "Memulai",
      articles: [
        {
          id: "doc-mulai-1",
          slug: "apa-itu-orgspace",
          category: "mulai",
          audience: "all",
          title: "Apa itu OrgSpace?",
          summary: "Platform pembuat website organisasi modern tanpa coding langsung dari HP Android.",
          sortOrder: 10,
          status: "published",
          publishedAt: "2026-10-15T00:00:00Z",
          updatedAt: "2026-10-15T00:00:00Z",
        },
        {
          id: "doc-mulai-2",
          slug: "memulai-dari-hp",
          category: "mulai",
          audience: "all",
          title: "Memulai dari HP",
          summary: "Langkah awal memasang aplikasi, login akun, dan membuat ruang kerja organisasi.",
          sortOrder: 20,
          status: "published",
          publishedAt: "2026-10-15T00:00:00Z",
          updatedAt: "2026-10-15T00:00:00Z",
        },
      ],
    },
    {
      value: "website",
      label: "Membuat & Meng-online-kan Website",
      articles: [
        {
          id: "doc-web-1",
          slug: "cara-mengonlinekan-website",
          category: "website",
          audience: "creator",
          title: "Panduan Lengkap Meng-online-kan Website",
          summary: "Alur memilih template, editing live preview, cek kelengkapan, publish langsung ke server, dan akses di browser.",
          sortOrder: 10,
          status: "published",
          publishedAt: "2026-10-15T00:00:00Z",
          updatedAt: "2026-10-15T00:00:00Z",
        },
        {
          id: "doc-web-2",
          slug: "membuat-website-dari-template",
          category: "website",
          audience: "creator",
          title: "Penyuntingan Konten & Fitur Editor",
          summary: "Cara mengubah teks, gambar, navigasi section, mode pratinjau penuh, dan sinkronisasi real-time.",
          sortOrder: 20,
          status: "published",
          publishedAt: "2026-10-15T00:00:00Z",
          updatedAt: "2026-10-15T00:00:00Z",
        },
      ],
    },
    {
      value: "provider",
      label: "Penyedia Template (Provider)",
      articles: [
        {
          id: "doc-prov-1",
          slug: "requirement-project-dan-tools",
          category: "provider",
          audience: "provider",
          title: "Requirement Project & Tools yang Didukung",
          summary: "Spesifikasi file ZIP, struktur direktori wajib, batas ukuran, editor yang didukung, library CDN, dan aturan keamanan.",
          sortOrder: 10,
          status: "published",
          publishedAt: "2026-10-15T00:00:00Z",
          updatedAt: "2026-10-15T00:00:00Z",
        },
        {
          id: "doc-prov-2",
          slug: "modul-sistem-dan-kustom",
          category: "provider",
          audience: "provider",
          title: "Panduan Modul Dinamis",
          summary: "Pengumuman, formulir, modul kustom, Portal Pengurus, dan JavaScript template. Lengkap dengan paket contoh siap unduh untuk kamu atau AI agent.",
          sortOrder: 20,
          status: "published",
          publishedAt: "2026-10-19T00:00:00Z",
          updatedAt: "2026-10-19T00:00:00Z",
        },
        {
          id: "doc-prov-3",
          slug: "menjadi-penyedia-template",
          category: "provider",
          audience: "provider",
          title: "Aturan Wajib & Rekomendasi Template",
          summary: "Standar pemeriksaan otomatis (verification engine) agar template lolos kurasi dan siap tayang di galeri.",
          sortOrder: 30,
          status: "published",
          publishedAt: "2026-10-15T00:00:00Z",
          updatedAt: "2026-10-15T00:00:00Z",
        },
      ],
    },
    {
      value: "faq",
      label: "Tanya Jawab & Bantuan",
      articles: [
        {
          id: "doc-faq-1",
          slug: "kirim-kritik-dan-saran",
          category: "faq",
          audience: "all",
          title: "Mengirim Kritik & Saran",
          summary: "Saluran pelaporan bug, usulan fitur, dan feedback pengguna.",
          sortOrder: 10,
          status: "published",
          publishedAt: "2026-10-15T00:00:00Z",
          updatedAt: "2026-10-15T00:00:00Z",
        },
      ],
    },
  ],
};

export const FALLBACK_DOC_ARTICLES: Record<string, DocDetail> = {
  "requirement-project-dan-tools": {
    id: "doc-prov-1",
    slug: "requirement-project-dan-tools",
    category: "provider",
    categoryLabel: "Penyedia Template (Provider)",
    audience: "provider",
    title: "Requirement Project & Tools yang Didukung",
    summary: "Spesifikasi file ZIP, struktur direktori wajib, batas ukuran, editor yang didukung, library CDN, dan aturan keamanan.",
    sortOrder: 10,
    status: "published",
    publishedAt: "2026-10-15T00:00:00Z",
    updatedAt: "2026-10-15T00:00:00Z",
    body: `# Requirement Project & Tools yang Didukung

Halaman ini merangkum seluruh persyaratan teknis berkas template website, perkakas pengembangan (*developer tools*) yang didukung, serta aturan validasi yang diterapkan oleh **OrgSpace Template Verification Engine**.

---

## 1. Requirement Berkas & Proyek (Wajib Dipenuhi)

Semua template yang diunggah oleh penyedia template (*provider*) ke sistem OrgSpace harus memenuhi kriteria berikut agar dapat lolos tahap verifikasi otomatis:

### A. Format Berkas & Batasan Ukuran
| Parameter | Nilai Batas | Penjelasan |
|---|---|---|
| **Format Berkas** | \`.zip\` (MIME: \`application/zip\`) | Template wajib dikemas ke dalam satu berkas arsip ZIP standar. Format lain (RAR, 7z, TAR) akan ditolak. |
| **Ukuran Maksimal ZIP** | **20 MB** | Batas maksimal ukuran arsip ZIP yang diunggah. |
| **Batas Ekstraksi Aman** | **50 MB** | Batas total ukuran seluruh berkas setelah diekstrak (perlindungan terhadap celah *zip bomb*). |
| **Batas Jumlah Berkas** | **500 berkas** | Jumlah total file di dalam arsip tidak boleh melebihi 500 item. |
| **Encoding Karakter** | **UTF-8** | Semua berkas HTML, CSS, dan JS wajib ber-encoding UTF-8 tanpa BOM (*Byte Order Mark*). |

### B. Struktur Direktori Wajib
Template wajib memiliki struktur berkas yang teratur dan bersih:
\`\`\`text
template-anda.zip/
├── index.html            ← WAJIB: Halaman beranda di akar direktori (root)
├── css/
│   └── style.css         ← Berkas gaya tampilan utama
├── js/
│   └── main.js           ← Berkas script logika interaktif (opsional jika statis)
└── img/                  ← Folder aset media (bisa juga bernama assets/ atau images/)
    ├── logo.png
    └── hero.jpg
\`\`\`

> **PENTING**: Berkas \`index.html\` **wajib berada langsung di akar ZIP**, bukan di dalam subfolder (misal \`template-saya/index.html\` akan ditolak dengan error \`MISSING_INDEX_HTML\`).

### C. Pemanggilan Aset Internal (Relative Paths)
- Seluruh tautan ke berkas CSS, JavaScript, gambar, dan font lokal **wajib menggunakan path relatif**.
- **BENAR**: \`<link rel="stylesheet" href="css/style.css">\` atau \`<img src="img/logo.png">\`.
- **SALAH**: \`<link rel="stylesheet" href="/css/style.css">\` (path absolut root server) atau \`C:\\Users\\...\\style.css\`.

### D. Larangan Framework SPA Monolitik
OrgSpace dirancang untuk memuat website statis semantik murni.
- **DILARANG** mengunggah hasil build framework SPA client-side heavy seperti Next.js, Nuxt, Angular, React SPA (CRA/Vite) atau Svelte kit yang mengeksekusi hydration kosong di HTML.
- Markup HTML harus memiliki elemen semantik lengkap di dokumen sebelum JavaScript dieksekusi agar ramah SEO dan mudah diproses oleh engine penandaan isian.

### E. Aturan Keamanan & Pencegahan XSS
- **Dilarang Inline Event Handlers**: Jangan menyematkan \`onclick="..."\`, \`onload="..."\`, \`onerror="..."\` di HTML. Gunakan \`addEventListener()\` di berkas JavaScript eksternal.
- **Dilarang Skema Tautan Berbahaya**: Hindari atribut tautan berbahaya seperti \`href="javascript:..."\` atau \`href="data:..."\`.
- **Dilarang Script Tracker Pihak Ketiga**: Tidak boleh menyisipkan Google Tag Manager, Meta/Facebook Pixel, Hotjar, atau pelacak analitik tanpa izin.
- **Penggunaan Iframe Dibatasi**: Iframe hanya diizinkan untuk penyematan video YouTube (\`youtube.com\`, \`youtube-nocookie.com\`) dan peta Google Maps (\`google.com/maps\`).

---

## 2. Tools & Teknologi yang Didukung

Penyedia template bebas menggunakan alur kerja modern selama menghasilkan output berkas yang sesuai dengan standar OrgSpace:

### A. Code Editor yang Direkomendasikan
- **Visual Studio Code (VS Code)** (Rekomendasi Utama)
- **Sublime Text**, **WebStorm**, **Cursor**, atau text editor apa pun.
- Ekstensi yang membantu: *Live Server*, *Prettier*, *HTML CSS Support*.

### B. Standar Bahasa Frontend
- **HTML5 Murni**: Mengutamakan tag semantik seperti \`<header>\`, \`<nav>\`, \`<main>\`, \`<section>\`, \`<article>\`, \`<footer>\`.
- **CSS3 Modern**: Mendukung Flexbox, CSS Grid, Media Queries (@media), dan CSS Custom Properties (\`:root { --primary-color: #0068D6; }\`).
- **Vanilla JavaScript (ES6+)**: Menggunakan sintaks modern seperti \`const/let\`, Arrow Functions, Template Literals, \`fetch()\`, dan Async/Await.

### C. Framework CSS & Utilitas (Whitelist CDN)
Jika Anda menggunakan framework atau library dari CDN, pastikan berasal dari domain terpercaya yang diizinkan (*whitelisted*):
- **Tailwind CSS**: Mendukung standalone script CDN (\`https://cdn.tailwindcss.com\`).
- **Bootstrap 5**: CSS & JS bundle via jsDelivr atau cdnjs.
- **Bulma CSS**
- **Domain CDN yang Diizinkan**:
  - \`cdn.jsdelivr.net\`
  - \`cdnjs.cloudflare.com\`
  - \`unpkg.com\`
  - \`cdn.tailwindcss.com\`
  - \`code.jquery.com\`

### D. Library Interaktif Ringan yang Didukung
- **Slider & Carousel**: Swiper.js, Splide.js.
- **Animasi Ringan**: AOS (*Animate on Scroll*), GSAP (*GreenSock*), Animate.css.
- **DOM & Reaktivitas Sederhana**: Alpine.js, jQuery.

### E. Ikonografi & Tipografi
- **Web Fonts**: Google Fonts (\`fonts.googleapis.com\` / \`fonts.gstatic.com\`), Geist Sans, Plus Jakarta Sans, atau font lokal di folder \`fonts/\` berlisensi terbuka (SIL Open Font License).
- **Ikon Paket**: Lucide Icons, Font Awesome 6 (via CDN), Bootstrap Icons, Remix Icon.

### F. Format Aset Media Gambar
- **Format Didukung**: WebP (Sangat Direkomendasikan), PNG, JPG/JPEG, SVG, ICO, GIF.
- **Ukuran Ideal**: Di bawah 500 KB per gambar untuk memastikan kecepatan render optimal saat dibuka di HP pembuat website.
`,
  },

  "modul-sistem-dan-kustom": {
    id: "doc-prov-2",
    slug: "modul-sistem-dan-kustom",
    category: "provider",
    categoryLabel: "Penyedia Template (Provider)",
    audience: "provider",
    title: "Panduan Modul Dinamis",
    summary: "Pengumuman, formulir, modul kustom, Portal Pengurus, dan JavaScript template. Lengkap dengan paket contoh siap unduh untuk kamu atau AI agent.",
    sortOrder: 20,
    status: "published",
    publishedAt: "2026-10-19T00:00:00Z",
    updatedAt: "2026-10-19T00:00:00Z",
    body: `Modul dinamis membuat template HTML biasa bisa diisi data oleh pengurus organisasi: pengumuman, prestasi, agenda, formulir pendaftaran, dan tombol ke Portal Pengurus. Provider **tidak perlu menulis backend, API, atau database**. Cukup tambahkan atribut \`data-ws-*\` ke elemen HTML. JavaScript template sendiri (menu, slider, tab, dark mode) tetap boleh dan ikut jalan.

## Unduh paket contoh

[⬇ Unduh contoh-modul-orgspace.zip (23 KB)](/downloads/contoh-modul-orgspace.zip)

Satu paket berisi template contoh, dokumentasi, dan instruksi untuk AI agent:

| Isi paket | Untuk apa |
|---|---|
| \`contoh-template/\` | Template yang sudah lolos pengecekan upload dan memakai **semua** modul di halaman ini |
| \`docs/\` | Dokumentasi ini, aturan wajib template, serta requirement & tools (format Markdown) |
| \`AGENTS.md\` / \`CLAUDE.md\` | Instruksi untuk AI agent: apa yang dibaca, aturan, checklist, dan cara membuat ZIP |
| \`BACA-SAYA.md\` | Panduan singkat untuk kamu |

Bagian template contoh dan modul yang dipakainya:

| Bagian di contoh | Modul |
|---|---|
| Menu atas + tombol Menu di HP (JavaScript \`js/main.js\`) | Tombol Portal Pengurus |
| Pengumuman Terbaru | Pengumuman (bawaan) |
| Prestasi Anggota + filter per tingkat (JavaScript) | Modul kustom \`prestasi\` + event \`orgspace:siap\` |
| Agenda Kegiatan | Modul kustom \`agenda\` |
| Pendaftaran Anggota & Kotak Aspirasi | Formulir |

### Dibuatkan AI agent

1. Ekstrak ZIP, lalu buka foldernya di editor yang punya AI agent (Claude Code, Cursor, Codex, dll.).
2. Beri perintah, misalnya:

   > Baca AGENTS.md lalu buatkan template website untuk OSIS SMA bergaya modern dan cerah. Pakai modul pengumuman, prestasi, galeri kegiatan, dan formulir pendaftaran anggota.

3. Agent membaca dokumentasi dan contoh, membuat folder \`template/\`, lalu membungkusnya menjadi \`template-saya.zip\`.
4. Upload \`template-saya.zip\` dari aplikasi: beralih ke mode Penyedia Template, buka tab Upload.

### Membuat sendiri

1. Salin folder \`contoh-template/\`, lalu ubah desainnya.
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

Alamat: \`orgspace.app/nama-website/admin\`. Pengurus masuk dengan email dan password akun OrgSpace, dan harus menjadi anggota organisasi pemilik website.

| Tab | Isi |
|---|---|
| Ringkasan | Jumlah isian form, berita, item modul kustom, dan pengurus |
| Kotak Masuk Form | Semua isian dari setiap \`data-ws-form\` |
| Berita & Agenda | Tambah dan hapus pengumuman |
| Modul Kustom | Daftar modul dari template beserta field-nya; tambah, edit, hapus, urutkan data, dan unggah foto |
| Pengurus & Hak Akses | Tambah pengurus (hanya akun OrgSpace yang sudah terdaftar) dan atur perannya |

Sesi Portal Pengurus berlaku 8 jam dan **hanya untuk satu website itu**.

## JavaScript template

JavaScript template **dijalankan**: hamburger menu, dark mode, slider, tab, accordion, Tailwind CDN, Alpine, Swiper, AOS, dan library lain dari CDN yang diizinkan.

Semua website tayang di satu domain (\`orgspace.app/...\`), jadi setiap halaman dijalankan di **sandbox** browser: script template tidak bisa membaca data \`orgspace.app\`, Portal Pengurus, atau website lain. Akibatnya:

| Hal | Perilaku |
|---|---|
| \`localStorage\`, \`sessionStorage\`, cookie | Bisa dipakai, tapi **hanya sementara**: hilang saat halaman ditutup atau pindah halaman. Mis. pilihan dark mode tidak diingat. |
| Status login pengurus di halaman publik | Tidak tersedia (lihat bagian 1). |
| \`fetch()\` ke API luar | Jalan jika API itu mengizinkan semua origin (CORS \`*\`). |
| Atribut \`onclick\`, \`onload\`, \`onerror\`, dst. dan link \`javascript:\` | **Dihapus** saat website diterbitkan. Pasang event di file JS dengan \`addEventListener\`. |

## JavaScript + data modul

Script template berjalan **sebelum** OrgSpace mengisi modul. Jika script-mu perlu kartu atau data modul (filter, slider, grafik, peta), tunggu event \`orgspace:siap\`:

\`\`\`js
function mulai() {
  // Data modul (salinan): window.orgspace.data
  //   .slug          nama website
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
- Event ini hanya ada di website yang tayang di OrgSpace. Di preview app atau file ZIP export, \`window.orgspace\` tidak ada, jadi tulis script yang tetap aman tanpa data (mis. filter tidak dibuat).

## Banyak halaman

Template boleh punya beberapa halaman (\`index.html\`, \`tentang.html\`, \`kontak.html\`). Hubungkan dengan link relatif biasa, mis. \`<a href="tentang.html">\`. Hasilnya \`orgspace.app/nama-website/tentang.html\`. Modul dan formulir bekerja di semua halaman.

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

  "cara-mengonlinekan-website": {
    id: "doc-web-1",
    slug: "cara-mengonlinekan-website",
    category: "website",
    categoryLabel: "Membuat & Meng-online-kan Website",
    audience: "creator",
    title: "Panduan Lengkap Meng-online-kan Website",
    summary: "Alur memilih template, editing live preview, cek kelengkapan, publish langsung ke server, dan akses di browser.",
    sortOrder: 10,
    status: "published",
    publishedAt: "2026-10-15T00:00:00Z",
    updatedAt: "2026-10-15T00:00:00Z",
    body: `# Panduan Lengkap Meng-online-kan Website bagi Pembuat Website

Panduan langkah demi langkah bagi pengurus organisasi atau pengguna umum untuk membuat dan mempublikasikan website resmi organisasi secara online melalui aplikasi OrgSpace Android.

---

## Tahap 1: Memilih Template di Galeri

1. Buka aplikasi **OrgSpace** di perangkat HP Android Anda.
2. Pastikan Anda berada di **Mode Pembuat Website** (ditandai dengan nama ruang kerja organisasi di pojok kiri atas).
3. Buka tab navigasi **Template** di bagian bawah.
4. Anda dapat mencari template yang sesuai dengan kebutuhan organisasi Anda:
   - **Filter Kategori**: Pilih kategori organisasi Anda, seperti *OSIS / MPK*, *BEM / DEMA / DPM*, *Unit Kegiatan Mahasiswa (UKM)*, *Ekstrakurikuler*, *Komunitas*, atau *Pendidikan*.
   - **Badge Modul**: Perhatikan badge pada kartu template (misalnya: *Berita*, *Pendaftaran / Oprec*, *Prestasi*, *Kontak*) untuk mengetahui fitur interaktif yang disediakan template tersebut.
5. Ketuk kartu template yang Anda minati untuk melihat detail atau langsung menginstansiasi proyek baru.

---

## Tahap 2: Penyuntingan Konten di Project Editor

Setelah memilih template, Anda akan langsung diarahkan ke layar **Project Editor** (\`TemplateEditorFragment\`):

### A. Live Preview Interaktif (WebView)
- Halaman website langsung dirender secara langsung (*real-time*) di area preview tengah.
- Anda dapat menguji responsivitas dengan tombol di atas preview:
  - **Tombol "HP"**: Menampilkan simulasi tampilan layar ponsel mobile.
  - **Tombol "Desktop"**: Menampilkan simulasi tampilan layar lebar komputer/laptop.

### B. Mengubah Teks & Paragraf
- Pada lembar isian di bawah (Tab **Isi**), pilih section yang ingin diedit (misal: *Hero*, *Tentang*, *Kontak*).
- Masukkan teks yang sesuai dengan identitas organisasi Anda:
  - Contoh: Ubah \`Nama Organisasi\` menjadi "Himpunan Mahasiswa Inovasi Digital".
- Saat Anda mengetik, preview WebView di atas **langsung terupdate secara seketika tanpa perlu me-reload**.

### C. Mengganti Gambar & Banner
- Ketuk tombol **Ubah Gambar** pada field foto/banner yang ingin diganti.
- Pilih foto dari galeri HP Anda. Foto akan otomatis dikompresi dengan rasio yang tepat agar website tetap ringan diakses.

### D. Mode Pratinjau Penuh (*Full Screen Preview*)
- Ketuk ikon layar penuh di pojok kanan atas toolbar untuk menyembunyikan panel form dan melihat website layaknya pengunjung asli.
- Tekan tombol *Back* HP untuk kembali ke mode edit.

---

## Tahap 3: Cek Kelengkapan Proyek

Sebelum menerbitkan website, pastikan semua data penting sudah terisi:
1. Buka menu titik tiga (⋮) di toolbar kanan atas -> pilih **Cek kelengkapan**.
2. Sistem akan memvalidasi isian Anda:
   - Memeriksa field wajib yang belum diisi.
   - Memberikan status kelayakan: **"Siap export"** atau **"Draft"**.
   - Menampilkan saran opsional jika masih menggunakan teks contoh default.

---

## Tahap 4: Menerbitkan Website ke Publik (*Publishing*)

1. Pada lembar kelengkapan atau menu aplikasi, ketuk tombol utama **Export / Selesai & Publikasi**.
2. Anda akan disajikan dua opsi:
   - **Publish Langsung Sekarang**: Menerbitkan website langsung ke server hosting OrgSpace dan menghubungkannya dengan domain organisasi.
   - **Download as ZIP File**: Mengunduh seluruh berkas web (HTML, CSS, JS, gambar) ke memori HP.
3. Ketuk **"Publish Langsung Sekarang"**.
4. Sistem backend OrgSpace akan memproses kompilasi template, menyuntikkan data isian organisasi, dan mengaktifkan endpoint situs publik.
5. Dialog sukses akan muncul:
   - **Status**: \`Online 🟢\`
   - **Port Server**: \`8080\`
   - **Alamat Website**: \`http://localhost:8080/sites/[nama-website]/index.html\` (atau domain \`orgspace.app/[slug]\`)

---

## Tahap 5: Membuka & Mengelola Website Online

1. Ketuk tombol **"Buka Website"** pada dialog sukses publikasi.
2. Peramban web HP (Google Chrome) akan langsung terbuka dan menampilkan website organisasi Anda yang sudah resmi tayang online dengan kode status \`HTTP 200 OK\`.
3. **Pengelolaan Lanjutan di Tab Website**:
   - Website yang sudah online akan tercatat di tab **Website** pada Ruang Pribadi Anda.
   - Kapan saja Anda ingin memperbarui teks, berita, atau gambar, cukup buka website tersebut dan ketuk tombol **"Edit tampilan"**.
   - Perubahan yang Anda simpan akan langsung disinkronkan ke website yang sedang tayang online.
`,
  },

  "membuat-website-dari-template": {
    id: "doc-web-2",
    slug: "membuat-website-dari-template",
    category: "website",
    categoryLabel: "Membuat & Meng-online-kan Website",
    audience: "creator",
    title: "Penyuntingan Konten & Fitur Editor",
    summary: "Cara mengubah teks, gambar, navigasi section, mode pratinjau penuh, dan sinkronisasi real-time.",
    sortOrder: 20,
    status: "published",
    publishedAt: "2026-10-15T00:00:00Z",
    updatedAt: "2026-10-15T00:00:00Z",
    body: `# Penyuntingan Konten & Fitur Editor Template

Halaman ini membahas fitur-fitur yang tersedia di dalam editor template OrgSpace untuk membantu Anda menyusun tampilan website organisasi secara cepat dan presisi.

---

## 1. Penyuntingan Bersama & Kunci Edit (Locking)
- OrgSpace mengimplementasikan sistem kolaborasi aman: hanya satu pengurus yang dapat membuka editor pada satu website di satu waktu.
- Anggota lain yang membuka website yang sama akan melihat banner status *"Sedang diedit oleh..."* dan pratinjaunya akan diperbarui otomatis begitu sesi selesai.

## 2. Navigasi Antar Section
- Chip navigasi di bawah preview memudahkan Anda berpindah bagian secara instan:
  - Bagian **Semua**: Menampilkan seluruh field isian template.
  - Bagian per Section (*Hero*, *Tentang*, *Visi Misi*, *Kontak*): Memfilter isian agar Anda fokus mengedit satu area tertentu.
  - Tanda centang hijau (\`✓\`) menandakan bahwa seluruh field wajib pada section tersebut telah terisi dengan baik.

## 3. Tab Konten & Tab Gaya
- **Tab Isi**: Mengubah konten esensial seperti nama organisasi, deskripsi visi misi, tautan media sosial, dan banner foto.
- **Tab Gaya**: Menyesuaikan aksen warna tema, kontras warna tombol, dan tipografi agar serasi dengan identitas visual organisasi Anda.

## 4. Undo dan Redo
- Salah mengetik atau tidak sengaja mengubah data? Gunakan tombol **Undo (Urungkan)** dan **Redo (Ulangi)** di toolbar atas untuk mengembalikan kondisi sebelumnya tanpa khawatir kehilangan data.
`,
  },

  "menjadi-penyedia-template": {
    id: "doc-prov-3",
    slug: "menjadi-penyedia-template",
    category: "provider",
    categoryLabel: "Penyedia Template (Provider)",
    audience: "provider",
    title: "Aturan Wajib & Rekomendasi Template",
    summary: "Standar pemeriksaan otomatis (verification engine) agar template lolos kurasi dan siap tayang di galeri.",
    sortOrder: 30,
    status: "published",
    publishedAt: "2026-10-15T00:00:00Z",
    updatedAt: "2026-10-15T00:00:00Z",
    body: `# Aturan Wajib & Rekomendasi Template OrgSpace

Sebagai penyedia template (*provider*), Anda merancang dan mengunggah template website yang akan digunakan oleh berbagai organisasi (sekolah, komunitas, UKM, BEM, OSIS, dan organisasi publik lainnya). Agar template dapat diverifikasi oleh sistem dan berjalan optimal, patuhi seluruh aturan teknis berikut.

---

## 1. Aturan Wajib (Harus Dipenuhi)

Template yang diunggah harus memenuhi kriteria di bawah ini agar lolos tahap pemeriksaan otomatis (*Template Verification Engine*):

### A. Format dan Batasan Berkas
- **Format Arsip**: Berkas harus berformat **ZIP** (\`.zip\`).
- **Ukuran Maksimal**: Ukuran berkas ZIP maksimal **20 MB**.
- **Ekspansi Aman**: Ukuran seluruh berkas setelah diekstrak maksimal **50 MB** (pencegahan *zip-bomb*).
- **Jumlah Berkas**: Maksimal **500 berkas** di dalam arsip.

### B. Struktur Berkas Standar
- **\`index.html\` Wajib di Root**: Berkas \`index.html\` **wajib** berada di akar direktori ZIP (tidak boleh terbungkus di dalam folder induk seperti \`template/index.html\`).
- **Pemisahan Aset yang Rapi**:
  - Berkas gaya ditempatkan di folder \`css/\` (misalnya: \`css/style.css\`).
  - Berkas JavaScript ditempatkan di folder \`js/\` (misalnya: \`js/main.js\`).
  - Berkas media gambar ditempatkan di folder \`img/\` atau \`assets/\`.
- **Relatif Path**: Semua pemanggilan berkas internal harus menggunakan path relatif (misal: \`css/style.css\`, bukan \`/css/style.css\` atau path absolut).

### C. Teknologi Frontend Murni (Bukan Framework SPA)
- Template harus berupa **Vanilla HTML, CSS, dan JavaScript**.
- **DILARANG** mengunggah proyek hasil kompilasi/bundle framework SPA modern seperti:
  - Next.js, Nuxt, Remix
  - React, Vue, Svelte, Angular
  - Vite / Webpack bundle yang mengeksekusi rendering melalui hydration client-side penuh
- Website harus memiliki markup HTML semantik yang utuh di sisi dokumen sebelum JavaScript dieksekusi.

### D. CDN dan Library yang Diizinkan
Aset eksternal hanya boleh dimuat dari CDN resmi terpercaya yang masuk ke daftar putih (*whitelist*):
- **Framework CSS & Utilitas**: Tailwind CSS (standalone CDN), Bootstrap 5, Bulma.
- **Interaktivitas Ringan**: Alpine.js, jQuery, AOS (Animate on Scroll), Swiper, Splide, GSAP.
- **Ikonografi & Tipografi**: Google Fonts, Font Awesome, Bootstrap Icons, Lucide Icons, Remix Icon.
- **CDN Host yang Diizinkan**: \`cdn.jsdelivr.net\`, \`cdnjs.cloudflare.com\`, \`unpkg.com\`, \`cdn.tailwindcss.com\`, \`code.jquery.com\`.
- **DILARANG**: Memuat script dari CDN tidak dikenal atau menyertakan SDK backend pihak ketiga seperti Firebase, Supabase, AWS Amplify, dll.

### E. Keamanan Kode & Larangan
- **Dilarang Inline Event Handlers**: Jangan gunakan \`onload="..."\`, \`onclick="..."\`, \`onerror="..."\`, atau fungsi evaluasi dinamis \`eval()\` di HTML.
- **Dilarang Skema Tautan Berbahaya**: Hindari atribut \`href="javascript:..."\`.
- **Dilarang Script Pelacak (Analytics/Tracker)**: Dilarang menyisipkan Google Tag Manager, Facebook Pixel, Hotjar, atau pelacak analytics lainnya.
- **Iframe Terbatas**: Hanya iframe dari \`youtube.com\`, \`youtube-nocookie.com\`, dan \`google.com/maps\` yang diizinkan.

### F. Aturan Penandaan Elemen (*Marking*)
- **Minimal 3 Elemen Bertanda**: Template harus memiliki minimal 3 elemen yang ditandai untuk dapat diedit oleh pembuat website (misalnya: tag identitas organisasi, judul hero, dan deskripsi singkat).
- **Aturan Pohon & Nesting**: Elemen yang ditandai tidak boleh saling membungkus secara bertingkat (elemen bertanda tidak boleh memiliki anak elemen bertanda teks lain).

---

    // Rekomendasi tambahan
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

  for (const fallbackCat of FALLBACK_DOC_TREE.categories) {
    let targetCat = mergedCategories.find((c) => c.value === fallbackCat.value);
    if (!targetCat) {
      targetCat = { ...fallbackCat, articles: [] };
      mergedCategories.push(targetCat);
    }

    for (const art of fallbackCat.articles) {
      const exists = targetCat.articles.some((a) => a.slug === art.slug);
      if (!exists) {
        targetCat.articles.push(art);
      }
    }
  }

  return { categories: mergedCategories };
}
