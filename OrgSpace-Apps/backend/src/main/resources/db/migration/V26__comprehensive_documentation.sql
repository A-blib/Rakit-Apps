-- Pembaruan dokumentasi komprehensif: alur membuat & meng-online-kan website,
-- requirement proyek, tools yang didukung, serta katalog modul.

-- 1. Perbarui artikel membuat-website-dari-template menjadi panduan lengkap meng-online-kan website
UPDATE doc_articles
SET title = 'Panduan Lengkap Meng-online-kan Website',
    summary = 'Alur memilih template, editing live preview, cek kelengkapan, publish langsung ke server, dan akses di browser.',
    body = '# Panduan Lengkap Meng-online-kan Website bagi Pembuat Website

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

Setelah memilih template, Anda akan langsung diarahkan ke layar **Project Editor** (`TemplateEditorFragment`):

### A. Live Preview Interaktif (WebView)
- Halaman website langsung dirender secara langsung (*real-time*) di area preview tengah.
- Anda dapat menguji responsivitas dengan tombol di atas preview:
  - **Tombol "HP"**: Menampilkan simulasi tampilan layar ponsel mobile.
  - **Tombol "Desktop"**: Menampilkan simulasi tampilan layar lebar komputer/laptop.

### B. Mengubah Teks & Paragraf
- Pada lembar isian di bawah (Tab **Isi**), pilih section yang ingin diedit (misal: *Hero*, *Tentang*, *Kontak*).
- Masukkan teks yang sesuai dengan identitas organisasi Anda:
  - Contoh: Ubah `Nama Organisasi` menjadi "Himpunan Mahasiswa Inovasi Digital".
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
   - **Status**: `Online 🟢`
   - **Port Server**: `8080`
   - **Alamat Website**: `http://localhost:8080/sites/[nama-website]/index.html` (atau domain `orgspace.app/[slug]`)

---

## Tahap 5: Membuka & Mengelola Website Online

1. Ketuk tombol **"Buka Website"** pada dialog sukses publikasi.
2. Peramban web HP (Google Chrome) akan langsung terbuka dan menampilkan website organisasi Anda yang sudah resmi tayang online dengan kode status `HTTP 200 OK`.
3. **Pengelolaan Lanjutan di Tab Website**:
   - Website yang sudah online akan tercatat di tab **Website** pada Ruang Pribadi Anda.
   - Kapan saja Anda ingin memperbarui teks, berita, atau gambar, cukup buka website tersebut dan ketuk tombol **"Edit tampilan"**.
   - Perubahan yang Anda simpan akan langsung disinkronkan ke website yang sedang tayang online.
',
    updated_at = now()
WHERE slug = 'membuat-website-dari-template';

-- 2. Tambahkan artikel requirement project & tools yang didukung
INSERT INTO doc_articles (slug, category, audience, title, summary, body, sort_order, status, published_at)
VALUES (
    'requirement-project-dan-tools',
    'provider',
    'provider',
    'Requirement Project & Tools yang Didukung',
    'Spesifikasi file ZIP, struktur direktori wajib, batas ukuran, editor yang didukung, library CDN, dan aturan keamanan.',
    '# Requirement Project & Tools yang Didukung

Halaman ini merangkum seluruh persyaratan teknis berkas template website, perkakas pengembangan (*developer tools*) yang didukung, serta aturan validasi yang diterapkan oleh **OrgSpace Template Verification Engine**.

---

## 1. Requirement Berkas & Proyek (Wajib Dipenuhi)

Semua template yang diunggah oleh penyedia template (*provider*) ke sistem OrgSpace harus memenuhi kriteria berikut agar dapat lolos tahap verifikasi otomatis:

### A. Format Berkas & Batasan Ukuran
| Parameter | Nilai Batas | Penjelasan |
|---|---|---|
| **Format Berkas** | `.zip` (MIME: `application/zip`) | Template wajib dikemas ke dalam satu berkas arsip ZIP standar. Format lain (RAR, 7z, TAR) akan ditolak. |
| **Ukuran Maksimal ZIP** | **20 MB** | Batas maksimal ukuran arsip ZIP yang diunggah. |
| **Batas Ekstraksi Aman** | **50 MB** | Batas total ukuran seluruh berkas setelah diekstrak (perlindungan terhadap celah *zip bomb*). |
| **Batas Jumlah Berkas** | **500 berkas** | Jumlah total file di dalam arsip tidak boleh melebihi 500 item. |
| **Encoding Karakter** | **UTF-8** | Semua berkas HTML, CSS, dan JS wajib ber-encoding UTF-8 tanpa BOM (*Byte Order Mark*). |

### B. Struktur Direktori Wajib
Template wajib memiliki struktur berkas yang teratur dan bersih:
```text
template-anda.zip/
├── index.html            ← WAJIB: Halaman beranda di akar direktori (root)
├── css/
│   └── style.css         ← Berkas gaya tampilan utama
├── js/
│   └── main.js           ← Berkas script logika interaktif (opsional jika statis)
└── img/                  ← Folder aset media (bisa juga bernama assets/ atau images/)
    ├── logo.png
    └── hero.jpg
```

> **PENTING**: Berkas `index.html` **wajib berada langsung di akar ZIP**, bukan di dalam subfolder (misal `template-saya/index.html` akan ditolak dengan error `MISSING_INDEX_HTML`).

### C. Pemanggilan Aset Internal (Relative Paths)
- Seluruh tautan ke berkas CSS, JavaScript, gambar, dan font lokal **wajib menggunakan path relatif**.
- **BENAR**: `<link rel="stylesheet" href="css/style.css">` atau `<img src="img/logo.png">`.
- **SALAH**: `<link rel="stylesheet" href="/css/style.css">` (path absolut root server) atau `C:\Users\...\style.css`.

### D. Larangan Framework SPA Monolitik
OrgSpace dirancang untuk memuat website statis semantik murni.
- **DILARANG** mengunggah hasil build framework SPA client-side heavy seperti Next.js, Nuxt, Angular, React SPA (CRA/Vite) atau Svelte kit yang mengeksekusi hydration kosong di HTML.
- Markup HTML harus memiliki elemen semantik lengkap di dokumen sebelum JavaScript dieksekusi agar ramah SEO dan mudah diproses oleh engine penandaan isian.

### E. Aturan Keamanan & Pencegahan XSS
- **Dilarang Inline Event Handlers**: Jangan gunakan `onclick="..."`, `onload="..."`, `onerror="..."` di HTML. Gunakan `addEventListener()` di berkas JavaScript eksternal.
- **Dilarang Skema Tautan Berbahaya**: Hindari atribut tautan berbahaya seperti `href="javascript:..."` atau `href="data:..."`.
- **Dilarang Script Tracker Pihak Ketiga**: Tidak boleh menyisipkan Google Tag Manager, Meta/Facebook Pixel, Hotjar, atau pelacak analitik tanpa izin.
- **Penggunaan Iframe Dibatasi**: Iframe hanya diizinkan untuk penyematan video YouTube (`youtube.com`, `youtube-nocookie.com`) dan peta Google Maps (`google.com/maps`).

---

## 2. Tools & Teknologi yang Didukung

Penyedia template bebas menggunakan alur kerja modern selama menghasilkan output berkas yang sesuai dengan standar OrgSpace:

### A. Code Editor yang Direkomendasikan
- **Visual Studio Code (VS Code)** (Rekomendasi Utama)
- **Sublime Text**, **WebStorm**, **Cursor**, atau text editor apa pun.

### B. Standar Bahasa Frontend
- **HTML5 Murni**: Mengutamakan tag semantik seperti `<header>`, `<nav>`, `<main>`, `<section>`, `<article>`, `<footer>`.
- **CSS3 Modern**: Mendukung Flexbox, CSS Grid, Media Queries (@media), dan CSS Custom Properties (`:root { --primary-color: #0068D6; }`).
- **Vanilla JavaScript (ES6+)**: Menggunakan sintaks modern seperti `const/let`, Arrow Functions, Template Literals, `fetch()`, dan Async/Await.

### C. Framework CSS & Utilitas (Whitelist CDN)
- **Tailwind CSS**: Mendukung standalone script CDN (`https://cdn.tailwindcss.com`).
- **Bootstrap 5**: CSS & JS bundle via jsDelivr atau cdnjs.
- **Bulma CSS**
- **Domain CDN yang Diizinkan**: `cdn.jsdelivr.net`, `cdnjs.cloudflare.com`, `unpkg.com`, `cdn.tailwindcss.com`, `code.jquery.com`.

### D. Library Interaktif Ringan yang Didukung
- **Slider & Carousel**: Swiper.js, Splide.js.
- **Animasi Ringan**: AOS (*Animate on Scroll*), GSAP (*GreenSock*), Animate.css.
- **DOM & Reaktivitas Sederhana**: Alpine.js, jQuery.

### E. Ikonografi & Tipografi
- **Web Fonts**: Google Fonts (`fonts.googleapis.com` / `fonts.gstatic.com`), Geist Sans, Plus Jakarta Sans, atau font lokal berlisensi SIL OFL.
- **Ikon Paket**: Lucide Icons, Font Awesome 6 (via CDN), Bootstrap Icons, Remix Icon.

### F. Format Aset Media Gambar
- **Format Didukung**: WebP (Sangat Direkomendasikan), PNG, JPG/JPEG, SVG, ICO, GIF.
- **Ukuran Ideal**: Di bawah 500 KB per gambar untuk memastikan kecepatan render optimal.
',
    15,
    'published',
    now()
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    summary = EXCLUDED.summary,
    body = EXCLUDED.body,
    updated_at = now();
