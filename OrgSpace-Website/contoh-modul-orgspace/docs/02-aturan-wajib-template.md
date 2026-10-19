# Aturan Wajib & Rekomendasi Template OrgSpace

Sebagai penyedia template (*provider*), Anda merancang dan mengunggah template website yang akan digunakan oleh berbagai organisasi (sekolah, komunitas, UKM, BEM, OSIS, dan organisasi publik lainnya). Agar template dapat diverifikasi oleh sistem dan berjalan optimal, patuhi seluruh aturan teknis berikut.

---

## 1. Aturan Wajib (Harus Dipenuhi)

Template yang diunggah harus memenuhi kriteria di bawah ini agar lolos tahap pemeriksaan otomatis (*Template Verification Engine*):

### A. Format dan Batasan Berkas
- **Format Arsip**: Berkas harus berformat **ZIP** (`.zip`).
- **Ukuran Maksimal**: Ukuran berkas ZIP maksimal **20 MB**.
- **Ekspansi Aman**: Ukuran seluruh berkas setelah diekstrak maksimal **50 MB** (pencegahan *zip-bomb*).
- **Jumlah Berkas**: Maksimal **500 berkas** di dalam arsip, masing-masing maksimal **10 MB**.
- **Jumlah Halaman**: Maksimal **10 halaman HTML**, saling terhubung lewat link relatif.

### B. Struktur Berkas Standar
- **`index.html` Wajib di Root**: Berkas `index.html` **wajib** berada di akar direktori ZIP (tidak boleh terbungkus di dalam folder induk seperti `template/index.html`).
- **Pemisahan Aset yang Rapi**:
  - Berkas gaya ditempatkan di folder `css/` (misalnya: `css/style.css`).
  - Berkas JavaScript ditempatkan di folder `js/` (misalnya: `js/main.js`).
  - Berkas media gambar ditempatkan di folder `img/` atau `assets/`.
- **Relatif Path**: Semua pemanggilan berkas internal harus menggunakan path relatif (misal: `css/style.css`, bukan `/css/style.css` atau path absolut).

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
- **CDN Host yang Diizinkan**: `cdn.jsdelivr.net`, `cdnjs.cloudflare.com`, `unpkg.com`, `cdn.tailwindcss.com`, `code.jquery.com`.
- **Versi Lengkap**: URL library wajib memakai versi pasti (mis. `swiper@11.1.4`), bukan tanpa versi atau `@latest`.
- **DILARANG**: Memuat script dari CDN tidak dikenal atau menyertakan SDK backend pihak ketiga seperti Firebase, Supabase, AWS Amplify, dll.

### E. Keamanan Kode & Larangan
- **Dilarang Inline Event Handlers**: Jangan gunakan `onload="..."`, `onclick="..."`, `onerror="..."`, atau fungsi evaluasi dinamis `eval()` di HTML.
- **Dilarang Skema Tautan Berbahaya**: Hindari atribut `href="javascript:..."`.
- **Dilarang Script Pelacak (Analytics/Tracker)**: Dilarang menyisipkan Google Tag Manager, Facebook Pixel, Hotjar, atau pelacak analytics lainnya.
- **Iframe Terbatas**: Hanya iframe dari `youtube.com`, `youtube-nocookie.com`, dan `google.com/maps` yang diizinkan.
- **Tanpa Permintaan ke Situs Lain**: JavaScript tidak boleh `fetch`/XHR/WebSocket ke situs luar, dan form tidak boleh `action` ke situs lain. Data dinamis memakai modul `data-ws-*`.

### F. Aturan Penandaan Elemen (*Marking*)
- **Minimal 3 Elemen Bertanda**: Template harus memiliki minimal 3 elemen yang ditandai untuk dapat diedit oleh pembuat website (misalnya: tag identitas organisasi, judul hero, dan deskripsi singkat).
- **Aturan Pohon & Nesting**: Elemen yang ditandai tidak boleh saling membungkus secara bertingkat (elemen bertanda tidak boleh memiliki anak elemen bertanda teks lain).

---

## 2. Hal Opsional yang Sangat Disarankan

Penerapan poin-poin berikut akan meningkatkan kualitas, kenyamanan, dan nilai adopsi template Anda oleh organisasi:

### A. Desain Responsif & Mobile-First
- Pastikan template memiliki tag `<meta name="viewport" content="width=device-width, initial-scale=1.0">`.
- Gunakan CSS Media Queries (`@media (max-width: 768px)`) agar tata letak tombol, navigasi, dan kartu berita menyesuaikan layar ponsel secara mulus.

### B. Variabel CSS (*CSS Custom Properties*)
- Definisikan warna utama, warna sekunder, dan font di `:root` CSS agar memudahkan penyesuaian tema.
- Contoh:
  ```css
  :root {
    --primary: #2563eb;
    --primary-hover: #1d4ed8;
    --bg-surface: #ffffff;
    --text-main: #0f172a;
    --text-muted: #64748b;
  }
  ```

### C. Struktur Semantik HTML5
- Gunakan tag `<header>`, `<nav>`, `<main>`, `<section>`, `<article>`, dan `<footer>`.
- Struktur yang rapi meningkatkan aksesibilitas dan keramahan mesin pencari (SEO).

### D. Optimasi Gambar
- Gunakan format modern seperti WebP atau JPG terkompresi dengan resolusi wajar (di bawah 500 KB per gambar).
- Sertakan teks alternatif (`alt="..."`) pada setiap elemen gambar.
