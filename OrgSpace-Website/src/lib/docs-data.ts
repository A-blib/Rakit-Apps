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
          id: "doc-prov-4",
          slug: "membuat-fitur-kustom",
          category: "provider",
          audience: "provider",
          title: "Membuat Fitur Kustom",
          summary: "Rancang fitur sendiri (agenda, galeri, filter, pencarian) dari modul + CSS + JavaScript, lengkap dengan resep dan batasnya.",
          sortOrder: 25,
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
| **Ukuran per Berkas** | **10 MB** | Satu file di dalam ZIP tidak boleh lebih dari 10 MB. Gambar di atas 1 MB mendapat peringatan. |
| **Jumlah Halaman** | **10 halaman HTML** | Setiap halaman harus bisa dibuka lewat link dari halaman lain. |
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

> **PENTING**: Berkas \`index.html\` **wajib berada langsung di akar ZIP**, bukan di dalam subfolder (misal \`template-saya/index.html\` akan ditolak dengan error \`NO_INDEX\`).

### C. Pemanggilan Aset Internal (Relative Paths)
- Seluruh tautan ke berkas CSS, JavaScript, gambar, dan font lokal **wajib menggunakan path relatif**.
- **BENAR**: \`<link rel="stylesheet" href="css/style.css">\` atau \`<img src="img/logo.png">\`.
- **SALAH**: \`<link rel="stylesheet" href="/css/style.css">\` (path absolut root server) atau \`C:\\Users\\...\\style.css\`.

### D. Larangan Framework SPA Monolitik
OrgSpace dirancang untuk memuat website statis semantik murni.
- **DILARANG** mengunggah hasil build framework SPA client-side heavy seperti Next.js, Nuxt, Angular, React SPA (CRA/Vite) atau Svelte kit yang mengeksekusi hydration kosong di HTML.
- Markup HTML harus memiliki elemen semantik lengkap di dokumen sebelum JavaScript dieksekusi agar ramah SEO dan mudah diproses oleh engine penandaan isian.

### E. Aturan Keamanan & Pencegahan XSS
- **Dilarang Inline Event Handlers**: Jangan gunakan \`onclick="..."\`, \`onload="..."\`, \`onerror="..."\` di HTML. Gunakan \`addEventListener()\` di berkas JavaScript eksternal.
- **Dilarang Skema Tautan Berbahaya**: Hindari atribut tautan berbahaya seperti \`href="javascript:..."\` atau \`href="data:..."\`.
- **Dilarang Script Tracker Pihak Ketiga**: Tidak boleh menyisipkan Google Tag Manager, Meta/Facebook Pixel, Hotjar, atau pelacak analitik tanpa izin.
- **Penggunaan Iframe Dibatasi**: Iframe hanya diizinkan untuk penyematan video YouTube (\`youtube.com\`, \`youtube-nocookie.com\`) dan peta Google Maps (\`google.com/maps\`).
- **Dilarang Mengambil/Mengirim Data ke Situs Lain dari JavaScript**: \`fetch("https://...")\`, XHR, WebSocket, dan \`sendBeacon\` ke situs luar ditolak (\`EXT_FETCH\`). Data dinamis diambil dari modul \`data-ws-*\`; \`fetch\` ke file lokal di ZIP (mis. \`data/menu.json\`) boleh.
- **Dilarang Kode JavaScript Diacak (obfuscated), Service Worker, dan \`import\` library dari CDN di dalam file JS.** Muat library dengan \`<script src>\` di HTML.

---

## 2. Tools & Teknologi yang Didukung

Penyedia template bebas menggunakan alur kerja modern selama menghasilkan output berkas yang sesuai dengan standar OrgSpace:

### A. Code Editor yang Direkomendasikan
- **Visual Studio Code (VS Code)** (Rekomendasi Utama)
- **Sublime Text**, **WebStorm**, **Cursor**, atau text editor apa pun.

### B. Standar Bahasa Frontend
- **HTML5 Murni**: Mengutamakan tag semantik seperti \`<header>\`, \`<nav>\`, \`<main>\`, \`<section>\`, \`<article>\`, \`<footer>\`.
- **CSS3 Modern**: Mendukung Flexbox, CSS Grid, Media Queries (@media), dan CSS Custom Properties (\`:root { --primary-color: #0068D6; }\`).
- **Vanilla JavaScript (ES6+)**: Menggunakan sintaks modern seperti \`const/let\`, Arrow Functions, Template Literals, dan Async/Await. \`fetch()\` hanya untuk file lokal di dalam ZIP.

### C. Framework CSS & Utilitas (Whitelist CDN)
- **Tailwind CSS**: Standalone script CDN (\`https://cdn.tailwindcss.com\`) diterima dengan peringatan karena lebih berat; file CSS hasil build Tailwind lebih disarankan.
- **Bootstrap 5**: CSS & JS bundle via jsDelivr atau cdnjs.
- **Bulma CSS**
- **Domain CDN yang Diizinkan**: \`cdn.jsdelivr.net\`, \`cdnjs.cloudflare.com\`, \`unpkg.com\`, \`cdn.tailwindcss.com\`, \`code.jquery.com\`.
- **Versi wajib lengkap**: tulis versi pasti di URL, mis. \`https://cdn.jsdelivr.net/npm/swiper@11.1.4/swiper-bundle.min.js\`. URL tanpa versi atau \`@latest\` ditolak (\`CDN_VERSION_UNCLEAR\`). Library di luar daftar ini ditolak (\`CDN_NOT_ALLOWED\`).

### D. Library Interaktif Ringan yang Didukung
- **Slider & Carousel**: Swiper.js, Splide.js.
- **Animasi Ringan**: AOS (*Animate on Scroll*), GSAP (*GreenSock*). Animasi CSS buatan sendiri (\`@keyframes\`, \`transition\`) selalu boleh.
- **DOM & Reaktivitas Sederhana**: Alpine.js, jQuery.

### E. Ikonografi & Tipografi
- **Web Fonts**: Google Fonts (\`fonts.googleapis.com\` / \`fonts.gstatic.com\`, diterima dengan peringatan karena butuh internet), atau font lokal berlisensi SIL OFL di dalam ZIP.
- **Ikon Paket**: Lucide Icons, Font Awesome 6 (via CDN), Bootstrap Icons, Remix Icon.

### F. Format Aset Media Gambar
- **Format Didukung**: WebP (Sangat Direkomendasikan), PNG, JPG/JPEG, SVG, ICO, GIF.
- **Ukuran Ideal**: Di bawah 500 KB per gambar untuk memastikan kecepatan render optimal.
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

Contoh sederhana ada di folder \`contoh-template/\`, contoh lengkap & interaktif di \`template-sinergi/\`. Cara merancang fitur sendiri (agenda, galeri, filter, pencarian) beserta resep JavaScript-nya ada di \`docs/04-membuat-fitur-kustom.md\`.

## Unduh paket contoh

[⬇ Unduh contoh-modul-orgspace.zip (223 KB)](/downloads/contoh-modul-orgspace.zip)

Satu paket berisi template contoh, dokumentasi, dan instruksi untuk AI agent. Berikan paket ini ke AI agent (Claude Code, Codex, Cursor, dll.) sebagai referensi agar ia bisa membuat template yang langsung lolos pengecekan upload.

| Isi paket | Untuk apa |
|---|---|
| \`AGENTS.md\` / \`CLAUDE.md\` | Instruksi untuk AI agent: cara kerja produk, apa yang dibaca, aturan, checklist, cara membuat ZIP, dan laporan saran isian Tandai |
| \`docs/\` | Panduan modul dinamis (halaman ini), aturan wajib, requirement & tools, dan **Membuat Fitur Kustom** (resep JavaScript) |
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

  "membuat-fitur-kustom": {
    id: "doc-prov-4",
    slug: "membuat-fitur-kustom",
    category: "provider",
    categoryLabel: "Penyedia Template (Provider)",
    audience: "provider",
    title: "Membuat Fitur Kustom",
    summary: "Rancang fitur sendiri (agenda, galeri, filter, pencarian) dari modul + CSS + JavaScript, lengkap dengan resep dan batasnya.",
    sortOrder: 25,
    status: "published",
    publishedAt: "2026-10-19T00:00:00Z",
    updatedAt: "2026-10-19T00:00:00Z",
    body: `# Membuat Fitur Kustom

OrgSpace tidak punya fitur "agenda", "prestasi", atau "galeri" bawaan. Semua fitur seperti itu **dibuat provider sendiri** di
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
- **Jumlah Berkas**: Maksimal **500 berkas** di dalam arsip, masing-masing maksimal **10 MB**.
- **Jumlah Halaman**: Maksimal **10 halaman HTML**, saling terhubung lewat link relatif.

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
- **Versi Lengkap**: URL library wajib memakai versi pasti (mis. \`swiper@11.1.4\`), bukan tanpa versi atau \`@latest\`.
- **DILARANG**: Memuat script dari CDN tidak dikenal atau menyertakan SDK backend pihak ketiga seperti Firebase, Supabase, AWS Amplify, dll.

### E. Keamanan Kode & Larangan
- **Dilarang Inline Event Handlers**: Jangan gunakan \`onload="..."\`, \`onclick="..."\`, \`onerror="..."\`, atau fungsi evaluasi dinamis \`eval()\` di HTML.
- **Dilarang Skema Tautan Berbahaya**: Hindari atribut \`href="javascript:..."\`.
- **Dilarang Script Pelacak (Analytics/Tracker)**: Dilarang menyisipkan Google Tag Manager, Facebook Pixel, Hotjar, atau pelacak analytics lainnya.
- **Iframe Terbatas**: Hanya iframe dari \`youtube.com\`, \`youtube-nocookie.com\`, dan \`google.com/maps\` yang diizinkan.
- **Tanpa Permintaan ke Situs Lain**: JavaScript tidak boleh \`fetch\`/XHR/WebSocket ke situs luar, dan form tidak boleh \`action\` ke situs lain. Data dinamis memakai modul \`data-ws-*\`.

### F. Aturan Penandaan Elemen (*Marking*)
- **Minimal 3 Elemen Bertanda**: Template harus memiliki minimal 3 elemen yang ditandai untuk dapat diedit oleh pembuat website (misalnya: tag identitas organisasi, judul hero, dan deskripsi singkat).
- **Aturan Pohon & Nesting**: Elemen yang ditandai tidak boleh saling membungkus secara bertingkat (elemen bertanda tidak boleh memiliki anak elemen bertanda teks lain).

---

## 2. Hal Opsional yang Sangat Disarankan

Penerapan poin-poin berikut akan meningkatkan kualitas, kenyamanan, dan nilai adopsi template Anda oleh organisasi:

### A. Desain Responsif & Mobile-First
- Pastikan template memiliki tag \`<meta name="viewport" content="width=device-width, initial-scale=1.0">\`.
- Gunakan CSS Media Queries (\`@media (max-width: 768px)\`) agar tata letak tombol, navigasi, dan kartu berita menyesuaikan layar ponsel secara mulus.

### B. Variabel CSS (*CSS Custom Properties*)
- Definisikan warna utama, warna sekunder, dan font di \`:root\` CSS agar memudahkan penyesuaian tema.
- Contoh:
  \`\`\`css
  :root {
    --primary: #2563eb;
    --primary-hover: #1d4ed8;
    --bg-surface: #ffffff;
    --text-main: #0f172a;
    --text-muted: #64748b;
  }
  \`\`\`

### C. Struktur Semantik HTML5
- Gunakan tag \`<header>\`, \`<nav>\`, \`<main>\`, \`<section>\`, \`<article>\`, dan \`<footer>\`.
- Struktur yang rapi meningkatkan aksesibilitas dan keramahan mesin pencari (SEO).

### D. Optimasi Gambar
- Gunakan format modern seperti WebP atau JPG terkompresi dengan resolusi wajar (di bawah 500 KB per gambar).
- Sertakan teks alternatif (\`alt="..."\`) pada setiap elemen gambar.
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
