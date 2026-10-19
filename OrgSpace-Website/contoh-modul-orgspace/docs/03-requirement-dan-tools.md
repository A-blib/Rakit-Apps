# Requirement Project & Tools yang Didukung

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
