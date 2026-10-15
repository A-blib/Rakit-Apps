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
          title: "Katalog Modul Sistem & Modul Kustom",
          summary: "Panduan teknis modul bawaan (data-ws-auth, data-ws-list, data-ws-form) dan modul kustom bebas (data-ws-collection).",
          sortOrder: 20,
          status: "published",
          publishedAt: "2026-10-15T00:00:00Z",
          updatedAt: "2026-10-15T00:00:00Z",
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
    title: "Katalog Modul Sistem & Modul Kustom",
    summary: "Panduan teknis modul bawaan (data-ws-auth, data-ws-list, data-ws-form) dan modul kustom bebas (data-ws-collection).",
    sortOrder: 20,
    status: "published",
    publishedAt: "2026-10-15T00:00:00Z",
    updatedAt: "2026-10-15T00:00:00Z",
    body: `# Katalog Modul Sistem & Modul Kustom OrgSpace

Platform OrgSpace memiliki mesin dinamis cerdas. Anda **tidak perlu membuat backend, routing server, atau database sendiri**. Cukup sematkan atribut khusus HTML \`data-ws-*\` pada elemen template Anda.

---

## 1. Modul Bawaan Sistem (Built-in Modules)

### A. Modul Autentikasi Pengurus (\`data-ws-auth\`)
Modul ini menghubungkan antarmuka website publik dengan sesi kepengurusan organisasi dan Portal Pengurus Mandiri.

| Atribut HTML | Elemen | Fungsi & Perilaku Sistem |
|---|---|---|
| \`data-ws-auth="status"\` | \`<span>\`, \`<strong>\` | Menampilkan nama pengguna yang sedang login (misal: "Ahmad Rizky") atau teks fallback "Tamu" jika belum login. |
| \`data-ws-auth="login"\` | \`<a>\` | Tombol/link yang otomatis mengarahkan pengurus ke halaman login Portal Pengurus (\`/admin\`). |
| \`data-ws-auth="logout"\` | \`<button>\`, \`<a>\` | Tombol untuk mengakhiri sesi login pengurus dan mereset status kembali menjadi Tamu. |

**Contoh Kode HTML:**
\`\`\`html
<div class="user-auth-bar">
  <span>Halo, <strong data-ws-auth="status">Tamu</strong></span>
  <a href="admin" data-ws-auth="login" class="btn-login">Portal Pengurus</a>
  <button type="button" data-ws-auth="logout" class="btn-logout">Keluar</button>
</div>
\`\`\`

---

### B. Modul Daftar Dinamis Berita Kegiatan (\`data-ws-list="berita"\`)
Modul ini secara otomatis mengambil data artikel kabar atau berita kegiatan dari sistem CMS OrgSpace dan merendernya secara berulang.

| Atribut HTML | Peran | Keterangan |
|---|---|---|
| \`data-ws-list="berita"\` | Pembungkus Grid/Daftar | Wadah penampung seluruh item berita. |
| \`data-ws-item\` | Item Berulang | Template kartu berita yang akan digandakan untuk setiap data berita yang ada di server. |
| \`data-ws-field="judul"\` | Judul Artikel | Mengisi teks judul berita. |
| \`data-ws-field="ringkasan"\` | Deskripsi Singkat | Mengisi kutipan atau ringkasan isi berita. |
| \`data-ws-field="tanggal"\` | Tanggal Publikasi | Mengisi format tanggal rilis berita. |
| \`data-ws-field="gambar"\` | Foto Berita | Otomatis mengisi atribut \`src\` gambar berita. |
| \`data-ws-field="link"\` | Tautan Selengkapnya | Otomatis mengisi \`href\` ke halaman baca penuh. |
| \`data-ws-empty\` | Tampilan Kosong | Elemen fallback yang hanya tampil jika belum ada data berita yang dipublikasikan. |

**Contoh Kode HTML:**
\`\`\`html
<section id="berita">
  <h2>Kabar & Berita Terkini</h2>
  
  <div data-ws-list="berita" class="news-grid">
    <!-- Template satu kartu berita -->
    <article data-ws-item class="news-card">
      <img data-ws-field="gambar" src="img/berita-placeholder.jpg" alt="Foto Kegiatan">
      <span data-ws-field="tanggal" class="date">15 Oktober 2026</span>
      <h3 data-ws-field="judul">Rapat Kerja Pengurus Baru</h3>
      <p data-ws-field="ringkasan">Pengurus resmi dilantik dengan mengusung tema inovasi...</p>
      <a data-ws-field="link" href="#">Baca Selengkapnya →</a>
    </article>

    <!-- Tampilan jika belum ada berita -->
    <div data-ws-empty class="empty-state">
      <p>Belum ada berita kegiatan yang dipublikasikan saat ini.</p>
    </div>
  </div>
</section>
\`\`\`

---

### C. Modul Formulir Interaktif Pendaftaran (\`data-ws-form="pendaftaran"\`)
Modul ini memungkinkan pengunjung mengisi formulir pendaftaran anggota atau open recruitment yang datanya langsung masuk ke basis data organisasi.

| Atribut HTML | Peran |
|---|---|
| \`data-ws-form="pendaftaran"\` | Atribut pada tag \`<form>\`. Sistem otomatis menangani submit request via AJAX. |
| \`name="..."\` | Nama atribut input (misal: \`nama\`, \`email\`, \`whatsapp\`, \`divisi\`, \`alasan\`). |
| \`<button type="submit">\` | Tombol eksekusi pengiriman formulir. |

**Contoh Kode HTML:**
\`\`\`html
<form data-ws-form="pendaftaran" class="registration-form">
  <div class="form-group">
    <label>Nama Lengkap</label>
    <input type="text" name="nama" placeholder="Masukkan nama Anda" required>
  </div>
  <div class="form-group">
    <label>Alamat Email</label>
    <input type="email" name="email" placeholder="nama@email.com" required>
  </div>
  <div class="form-group">
    <label>Nomor WhatsApp</label>
    <input type="tel" name="whatsapp" placeholder="08xxxxxxxxxx" required>
  </div>
  <div class="form-group">
    <label>Pilihan Divisi</label>
    <select name="divisi" required>
      <option value="">Pilih divisi...</option>
      <option value="Humas">Hubungan Masyarakat</option>
      <option value="Iptek">Ilmu Pengetahuan & Teknologi</option>
      <option value="Kaderisasi">Kaderisasi</option>
    </select>
  </div>
  <div class="form-group">
    <label>Motivasi Bergabung</label>
    <textarea name="alasan" rows="3" placeholder="Tuliskan motivasi Anda..."></textarea>
  </div>

  <button type="submit" class="btn-submit">Kirim Formulir Pendaftaran</button>
</form>
\`\`\`

---

## 2. Modul Kustom Bebas (\`data-ws-collection="[nama]"\`)

Selain modul bawaan, OrgSpace mengizinkan provider membuat modul data kustom sesuai kebutuhan organisasi (misal: daftar prestasi mahasiswa, galeri foto, portofolio karya, daftar alumni, dll.).

### Aturan Penulisan Modul Kustom:
1. Bungkus blok daftar dengan \`data-ws-collection="[nama_koleksi_bebas]"\` (contoh: \`data-ws-collection="prestasi"\`).
2. Tandai elemen kartu perulangan dengan \`data-ws-item\`.
3. Tandai elemen data di dalam kartu dengan \`data-ws-field="[nama_field]"\` (contoh: \`judul\`, \`peraih\`, \`tahun\`, \`foto\`).
4. Sertakan penanda \`data-ws-empty\` untuk fallback tampilan ketika data masih kosong.

**Contoh Kode HTML Modul Prestasi Kustom:**
\`\`\`html
<section id="prestasi">
  <h2>Prestasi Mahasiswa</h2>
  
  <div data-ws-collection="prestasi" class="achievement-grid">
    <!-- Template item prestasi -->
    <div data-ws-item class="achievement-card">
      <span data-ws-field="tahun" class="badge">2026</span>
      <h3 data-ws-field="judul">Juara 1 Lomba Inovasi Nasional</h3>
      <p>Diraih oleh: <strong data-ws-field="peraih">Tim Robotika</strong></p>
    </div>

    <!-- Fallback data kosong -->
    <div data-ws-empty class="empty-state">
      <p>Belum ada data prestasi yang ditambahkan.</p>
    </div>
  </div>
</section>
\`\`\`

> **Keunggulan Sistem**: Saat website di-online-kan, backend OrgSpace secara otomatis membuat antarmuka tabel pengisian di Portal Pengurus untuk modul kustom ini tanpa perlu coding tambahan!

---

## 3. Penandaan Isian (*Marking System*)

Agar teks dan banner di template bisa diedit oleh pembuat website melalui aplikasi mobile, gunakan atribut penanda bagian:
- **\`data-section="[Nama Bagian]"\`**: Mengelompokkan elemen ke section tertentu (misalnya: \`data-section="Hero"\`, \`data-section="Tentang Kami"\`).
- Pada wizard upload Langkah 4 di aplikasi, provider tinggal menandai elemen judul, deskripsi paragraf, atau gambar banner dengan mudah.
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
