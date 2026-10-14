-- Pembaruan dan penambahan artikel dokumentasi untuk penyedia template (provider)
-- Mengenai aturan wajib & rekomendasi, modul bawaan (login, berita, form), serta modul kustom.

-- 1. Perbarui artikel panduan & aturan upload template
UPDATE doc_articles
SET title = 'Aturan Wajib & Rekomendasi Template',
    summary = 'Panduan lengkap syarat struktur file ZIP, larangan teknis, keamanan, serta rekomendasi desain untuk template OrgSpace.',
    body = '# Aturan Wajib & Rekomendasi Template OrgSpace

Sebagai penyedia template (*provider*), Anda merancang dan mengunggah template website yang akan digunakan oleh berbagai organisasi (sekolah, komunitas, UKM, BEM, OSIS, dan organisasi publik lainnya). Agar template dapat diverifikasi oleh sistem dan berjalan optimal, patuhi seluruh aturan teknis berikut.

---

## 1. Aturan Wajib (Harus Dipenuhi)

Template yang diunggah harus memenuhi kriteria di bawah ini agar lolos tahap pemeriksaan otomatis (*Template Verification Engine*):

### A. Format dan Batasan Berkas
- **Format Arsip**: Berkas harus berformat **ZIP** (`.zip`).
- **Ukuran Maksimal**: Ukuran berkas ZIP maksimal **20 MB**.
- **Ekspansi Aman**: Ukuran seluruh berkas setelah diekstrak maksimal **50 MB** (pencegahan *zip-bomb*).
- **Jumlah Berkas**: Maksimal **500 berkas** di dalam arsip.

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
- **DILARANG**: Memuat script dari CDN tidak dikenal atau menyertakan SDK backend pihak ketiga seperti Firebase, Supabase, AWS Amplify, dll.

### E. Keamanan Kode & Larangan
- **Dilarang Inline Event Handlers**: Jangan gunakan `onload="..."`, `onclick="..."`, `onerror="..."`, atau fungsi evaluasi dinamis `eval()` di HTML.
- **Dilarang Skema Tautan Berbahaya**: Hindari atribut `href="javascript:..."`.
- **Dilarang Script Pelacak (Analytics/Tracker)**: Dilarang menyisipkan Google Tag Manager, Facebook Pixel, Hotjar, atau pelacak analytics lainnya.
- **Iframe Terbatas**: Hanya iframe dari `youtube.com`, `youtube-nocookie.com`, dan `google.com/maps` yang diizinkan.

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
',
    sort_order = 10,
    status = 'published',
    published_at = now(),
    updated_at = now()
WHERE slug = 'menjadi-penyedia-template';

-- 2. Tambahkan artikel Modul Bawaan OrgSpace
INSERT INTO doc_articles (slug, category, audience, title, summary, body, sort_order, status, published_at)
VALUES (
    'modul-bawaan-orgspace',
    'provider',
    'provider',
    'Modul Bawaan: Login, Berita & Formulir',
    'Cara memasang modul autentikasi pengurus, portal web pengurus, daftar berita otomatis, dan formulir pendaftaran interaktif.',
    '# Modul Bawaan OrgSpace: Login, Berita & Formulir

OrgSpace menyediakan modul-modul dinamis siap pakai yang dapat langsung diintegrasikan ke dalam template HTML hanya dengan menyematkan atribut `data-ws-*`. 

Provider **tidak perlu menulis kode backend, API, atau query database**. Semua pemrosesan data ditangani otomatis oleh platform OrgSpace.

---

## 1. Arsitektur Web Dinamis & Portal Pengurus

Setiap website yang diterbitkan di OrgSpace dilengkapi dengan **Portal Pengurus Mandiri** di URL:
```
https://[domain-hosting]/[slug-organisasi]/admin
```

Contoh di lingkungan lokal/pengembangan:
```
http://localhost:3001/web-dinamis-organisasi/admin
```

### Keunggulan Portal Pengurus:
- **Akses Fleksibel**: Pengurus organisasi dapat mengelola website langsung dari browser HP, tablet, maupun laptop tanpa perlu menginstal aplikasi tambahan.
- **Autentikasi Terintegrasi**: Pengurus masuk menggunakan akun OrgSpace mereka (Pemilik, Admin, atau Editor).
- **Pengelolaan Kotak Masuk & Konten**: Memantau formulir pendaftaran masuk, menerbitkan/menghapus berita, dan mengelola koleksi data kustom.

---

## 2. Modul Autentikasi Pengurus (Login & Logout)

Modul ini memudahkan pengurus organisasi untuk mengakses portal administrasi langsung dari halaman publik website.

### A. Tombol Masuk / Portal Pengurus
Sematkan atribut `data-ws-auth="login"` pada tombol atau tautan navigasi:
```html
<a href="admin" data-ws-auth="login" class="btn btn-login">
  Portal Pengurus
</a>
```
- Sistem secara otomatis mengarahkan pengurus ke halaman login portal pengurus.

### B. Tombol Keluar (Logout)
Sematkan atribut `data-ws-auth="logout"` pada tombol keluar:
```html
<button data-ws-auth="logout" class="btn btn-logout">
  Keluar
</button>
```
- Menghapus sesi JWT pengurus dan mengembalikan tampilan ke mode tamu.

### C. Status Sesi Pengurus
Sematkan atribut `data-ws-auth="status"` untuk menampilkan identitas pengurus yang sedang aktif:
```html
<div class="user-greeting">
  Selamat datang, <strong data-ws-auth="status">Tamu</strong>
</div>
```

### D. Pembatasan Elemen Berdasarkan Peran
Gunakan atribut `data-ws-role` untuk menampilkan elemen khusus peran tertentu:
```html
<!-- Hanya tampil bagi pengurus dengan peran ''admin'' atau ''owner'' -->
<div data-ws-role="admin">
  <a href="admin">Pengaturan Website Organisasi</a>
</div>
```

---

## 3. Modul Berita & Agenda Organisasi

Modul ini menampilkan daftar kabar terbaru, pengumuman, atau agenda kegiatan yang dipublikasikan oleh pengurus.

### Atribut yang Digunakan:
- `data-ws-list="berita"` atau `data-ws-collection="berita"`: Kontainer daftar berita.
- `data-ws-item`: Elemen kartu/baris template perulangan.
- `data-ws-field="[field]"`: Field dinamis di dalam item:
  - `judul`: Judul artikel/kegiatan.
  - `ringkasan`: Kutipan ringkas isi berita.
  - `tanggal`: Tanggal publikasi (terformat otomatis).
  - `foto`: Gambar sampul berita (mengisi atribut `src`).
- `data-ws-empty`: Kontainer fallback yang tampil jika belum ada berita.

### Contoh Penerapan HTML:
```html
<section class="section-news">
  <div class="container">
    <h2>Kabar & Pengumuman</h2>
    
    <!-- Kontainer Daftar Berita -->
    <div class="news-grid" data-ws-list="berita">
      
      <!-- Template Item Berita -->
      <article class="news-card" data-ws-item>
        <img src="img/news-default.jpg" alt="Foto Berita" data-ws-field="foto">
        <div class="news-content">
          <span class="news-date" data-ws-field="tanggal">14 Oktober 2026</span>
          <h3 data-ws-field="judul">Judul Kegiatan Organisasi</h3>
          <p data-ws-field="ringkasan">Deskripsi singkat mengenai agenda atau pencapaian organisasi...</p>
        </div>
      </article>

      <!-- Tampilan Jika Belum Ada Berita -->
      <div class="empty-state" data-ws-empty>
        <p>Belum ada kabar terbaru saat ini.</p>
      </div>

    </div>
  </div>
</section>
```

---

## 4. Modul Formulir Interaktif (Pendaftaran & Kontak)

Modul ini memungkinkan pengunjung mengisi formulir (seperti pendaftaran anggota baru, formulir aspirasi, atau kontak).

### Atribut yang Digunakan:
- `data-ws-form="[nama-formulir]"`: Sematkan pada tag `<form>`. Contoh: `data-ws-form="pendaftaran"` atau `data-ws-form="kontak"`.
- Setiap `<input>`, `<textarea>`, atau `<select>` harus memiliki atribut `name` yang unik.

### Contoh Penerapan HTML:
```html
<section class="section-form">
  <div class="container">
    <h2>Formulir Pendaftaran Anggota</h2>
    
    <form class="custom-form" data-ws-form="pendaftaran">
      <div class="form-group">
        <label for="nama">Nama Lengkap</label>
        <input type="text" id="nama" name="nama" required placeholder="Masukkan nama lengkap">
      </div>
      
      <div class="form-group">
        <label for="email">Alamat Email</label>
        <input type="email" id="email" name="email" required placeholder="email@contoh.com">
      </div>
      
      <div class="form-group">
        <label for="whatsapp">Nomor WhatsApp</label>
        <input type="tel" id="whatsapp" name="whatsapp" placeholder="08xxxxxxxxxx">
      </div>

      <div class="form-group">
        <label for="alasan">Alasan Bergabung</label>
        <textarea id="alasan" name="alasan" rows="3" placeholder="Tuliskan motivasi Anda..."></textarea>
      </div>

      <button type="submit" class="btn btn-primary">Kirim Pendaftaran</button>
    </form>

  </div>
</section>
```

### Alur Data:
1. Ketika formulir disubmit oleh pengunjung, skrip platform OrgSpace menangani validasi dan pengiriman data ke server secara aman via background AJAX.
2. Pengurus organisasi dapat langsung melihat isian pendaftar secara waktu nyata (*real-time*) di **Portal Pengurus (`/[slug]/admin`) → Kotak Masuk**.
',
    20,
    'published',
    now()
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    summary = EXCLUDED.summary,
    body = EXCLUDED.body,
    sort_order = EXCLUDED.sort_order,
    status = EXCLUDED.status,
    published_at = EXCLUDED.published_at,
    updated_at = now();

-- 3. Tambahkan artikel Panduan Modul Kustom Sendiri
INSERT INTO doc_articles (slug, category, audience, title, summary, body, sort_order, status, published_at)
VALUES (
    'panduan-modul-kustom',
    'provider',
    'provider',
    'Panduan Membuat Modul Kustom Sendiri',
    'Cara mendefinisikan koleksi data dinamis bebas (prestasi, galeri, pengurus, dll.) dan pengelolaannya di Portal Pengurus.',
    '# Panduan Membuat Modul Kustom Sendiri

Selain modul bawaan (seperti Berita dan Formulir), OrgSpace mendukung **Modul Kustom Bebas (*Custom Collections*)**. 

Sebagai provider, Anda dapat merancang fitur dinamis apa saja sesuai kreativitas Anda—misalnya: Daftar Prestasi, Struktur Pengurus, Galeri Kegiatan, Program Kerja, Testimoni, Fasilitas, dan lain-lain—**tanpa batasan nama modul yang kaku**.

---

## 1. Aturan Sintaks Modul Kustom

Modul kustom dibangun dengan menyematkan atribut HTML khusus pada elemen kontainer dan elemen anak:

### A. Kontainer Koleksi: `data-ws-collection` atau `data-ws-list`
Sematkan atribut ini pada kontainer pembungkus (misal `<div>`, `<section>`, atau `<ul>`).
- **Aturan Penamaan Slug**:
  - Karakter yang diizinkan: huruf kecil (`a-z`), angka (`0-9`), tanda strip (`-`), dan garis bawah (`_`).
  - Panjang nama: **2 hingga 40 karakter**.
  - Contoh nama yang valid: `prestasi`, `galeri`, `program-kerja`, `alumni_sukses`, `fasilitas`.
  - Format sintaks: `data-ws-collection="prestasi"` atau `data-ws-list="prestasi"`.

### B. Baris Template: `data-ws-item`
Sematkan atribut `data-ws-item` pada elemen kartu/baris template di dalam kontainer koleksi.
- Elemen ini berfungsi sebagai *blueprint* (cetak biru) tampilan yang akan diulang sebanyak data yang dimiliki organisasi.

### C. Field Dinamis: `data-ws-field="[nama_field]"`
Di dalam elemen bertanda `data-ws-item`, sematkan atribut `data-ws-field` pada elemen yang akan menampilkan nilai data dinamis.
- **Nama field bebas**: Anda dapat menamai field sesuai konteks data (misalnya: `judul`, `deskripsi`, `tahun`, `tingkat`, `foto`, `kategori`, `jabatan`, dll.).
- **Perilaku Rendering**:
  - Pada elemen biasa (`<h3>`, `<p>`, `<span>`): sistem mengisi teks (*textContent*).
  - Pada elemen gambar (`<img>`): sistem mengisi alamat gambar (*src*).

### D. Tampilan Data Kosong: `data-ws-empty`
Sematkan atribut `data-ws-empty` pada elemen pemberitahuan alternatif di dalam kontainer koleksi.
- Elemen ini otomatis ditampilkan saat organisasi belum memiliki data untuk koleksi tersebut.

---

## 2. Contoh Lengkap Implementasi

Berikut adalah contoh modul kustom untuk menampilkan **Daftar Prestasi Siswa / Anggota**:

### File `index.html`:
```html
<section class="section-prestasi">
  <div class="container">
    <div class="section-header">
      <h2>Prestasi & Penghargaan</h2>
      <p>Pencapaian membanggakan yang diraih anggota organisasi kami.</p>
    </div>

    <!-- Kontainer Koleksi Kustom: prestasi -->
    <div class="prestasi-grid" data-ws-collection="prestasi">
      
      <!-- Template Item Baris (Diulang Otomatis) -->
      <div class="prestasi-card" data-ws-item>
        <div class="badge-wrapper">
          <span class="badge badge-year" data-ws-field="tahun">2026</span>
          <span class="badge badge-level" data-ws-field="tingkat">Nasional</span>
        </div>
        <h3 class="prestasi-title" data-ws-field="judul">Juara 1 Lomba Cipta Inovasi</h3>
        <p class="prestasi-desc" data-ws-field="deskripsi">
          Meraih medali emas tingkat nasional dalam ajang kompetisi teknologi digital kreatif.
        </p>
      </div>

      <!-- Tampilan Jika Belum Ada Data -->
      <div class="empty-state" data-ws-empty>
        <p>Belum ada data prestasi yang ditambahkan.</p>
      </div>

    </div>
  </div>
</section>
```

### File `css/style.css`:
```css
.prestasi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
}

.prestasi-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.badge {
  display: inline-block;
  padding: 0.25rem 0.6rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-right: 0.5rem;
}

.badge-year { background: #f1f5f9; color: #475569; }
.badge-level { background: #dbeafe; color: #1e40af; }

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 3rem 1rem;
  color: #94a3b8;
}
```

---

## 3. Cara Pengurus Mengelola Data Modul Kustom

Bagaimana pengurus organisasi mengisi data untuk modul kustom yang Anda buat?

1. **Otomatis Dikenali**: Saat website diterbitkan, sistem OrgSpace secara otomatis memindai seluruh atribut `data-ws-collection` dan `data-ws-list` yang ada di template Anda.
2. **Tab Modul Kustom di Portal Pengurus**:
   - Pengurus membuka Portal Pengurus di `/[slug-organisasi]/admin`.
   - Masuk ke tab **Modul Kustom**.
   - Nama koleksi Anda (misalnya `prestasi`) langsung muncul dalam daftar modul aktif.
3. **Penambahan Data Fleksibel**:
   - Pengurus dapat menambahkan item baru dan mengisi field-field (`judul`, `deskripsi`, `tahun`, `tingkat`).
   - Sistem menyediakan antarmuka form dinamis yang ramah pengguna.
4. **Sinkronisasi Langsung ke Website Publik**:
   - Begitu pengurus menyimpan data, data tersebut langsung muncul secara otomatis di halaman website publik sesuai tata letak kartu yang Anda rancang.
   - Pengurus juga dapat menghapus item sewaktu-waktu.

Dengan sistem ini, provider memiliki kebebasan tak terbatas untuk menciptakan aneka ragam template website organisasi yang interaktif, profesional, dan dinamis!
',
    30,
    'published',
    now()
)
ON CONFLICT (slug) DO UPDATE
SET title = EXCLUDED.title,
    summary = EXCLUDED.summary,
    body = EXCLUDED.body,
    sort_order = EXCLUDED.sort_order,
    status = EXCLUDED.status,
    published_at = EXCLUDED.published_at,
    updated_at = now();
