import Link from "next/link";
import type { Metadata } from "next";
import { ErrorBox } from "@/components/ErrorBox";
import { Markdown } from "@/components/Markdown";
import { getDocTree } from "@/lib/api/public";

export const metadata: Metadata = {
  title: "Pusat Dokumentasi OrgSpace",
  description:
    "Panduan lengkap spesifikasi template, perkakas yang didukung, katalog modul sistem & kustom, serta cara meng-online-kan website bagi pembuat website.",
};

const DETAILED_DOCS_CONTENT = `
# Panduan Lengkap Dokumentasi OrgSpace

Selamat datang di pusat dokumentasi resmi OrgSpace. Dokumen ini menjabarkan seluruh spesifikasi teknis, requirement proyek, perkakas (*tools*) yang didukung, katalog modul dinamis, dan panduan lengkap membuat serta meng-online-kan website bagi organisasi.

---

## 1. Requirement Project yang Harus Dipenuhi

Setiap template website yang diunggah ke platform OrgSpace wajib mematuhi standar pemeriksaan otomatis (*Template Verification Engine*) berikut:

### A. Format & Batasan Berkas
| Parameter | Nilai Batas | Penjelasan |
|---|---|---|
| **Format Berkas** | \`.zip\` (MIME: \`application/zip\`) | Template wajib dikemas ke dalam satu berkas arsip ZIP standar. Format lain (RAR, 7z, TAR) akan ditolak. |
| **Ukuran Maksimal ZIP** | **20 MB** | Batas maksimal ukuran arsip ZIP yang diunggah. |
| **Batas Ekstraksi Aman** | **50 MB** | Batas total ukuran seluruh berkas setelah diekstrak (pencegahan celah *zip bomb*). |
| **Batas Jumlah Berkas** | **500 berkas** | Jumlah total file di dalam arsip tidak boleh melebihi 500 item. |
| **Encoding Karakter** | **UTF-8** | Semua berkas HTML, CSS, dan JS wajib ber-encoding UTF-8 tanpa BOM (*Byte Order Mark*). |

### B. Struktur Direktori Wajib
Template wajib memiliki susunan berkas yang rapi dan standar:
\`\`\`text
template-organisasi.zip/
├── index.html            <- WAJIB: Halaman utama berada di root direktori ZIP
├── css/
│   └── style.css         <- Berkas styling utama
├── js/
│   └── main.js           <- Berkas script interaktif
└── img/                  <- Folder aset visual (atau assets/)
    ├── logo.png
    └── banner.jpg
\`\`\`

> **PENTING**: Berkas \`index.html\` **harus berada di akar (root)** direktori ZIP. Jika dibungkus dalam folder induk (misal \`folder/index.html\`), sistem akan mengembalikan error \`MISSING_INDEX_HTML\`.

### C. Pemanggilan Aset Internal (Relative Path)
- Semua pemanggilan CSS, JS, font, dan gambar internal **wajib menggunakan path relatif**.
- **BENAR**: \`<link rel="stylesheet" href="css/style.css">\`, \`<img src="img/logo.png">\`
- **SALAH**: \`<link rel="stylesheet" href="/css/style.css">\` (path absolut root) atau path lokal komputer seperti \`C:\\Users\\...\`.

### D. Standar Teknologi (Vanilla Web Murni)
- Template harus berupa **Vanilla HTML5, CSS3, dan Vanilla JavaScript**.
- **DILARANG KERAS** mengunggah berkas hasil kompilasi/bundle framework SPA modern yang mengandalkan client-side hydration kosong, seperti:
  - Next.js / Nuxt.js / Remix
  - React (CRA / Vite bundle)
  - Vue.js / Svelte / Angular
- Dokumen HTML harus berisi struktur semantik lengkap sebelum script dieksekusi agar ramah mesin pencari (SEO) dan dapat diproses oleh parser isian OrgSpace.

### E. Aturan Keamanan & Pencegahan XSS
- **Dilarang Inline Event Handlers**: Jangan gunakan \`onclick="..."\`, \`onload="..."\`, \`onerror="..."\`, atau fungsi \`eval()\` di HTML. Gunakan \`addEventListener()\` di berkas JavaScript eksternal.
- **Dilarang Skema URI Berbahaya**: Hindari \`href="javascript:..."\` atau \`href="data:..."\`.
- **Dilarang Script Pelacak Pihak Ketiga**: Dilarang memasang Google Tag Manager, Meta/Facebook Pixel, Hotjar, atau pelacak analitik tersembunyi.
- **Iframe Dibatasi**: Iframe hanya diizinkan untuk pemutar video YouTube (\`youtube.com\`, \`youtube-nocookie.com\`) dan peta Google Maps (\`google.com/maps\`).

### F. Penandaan Isian (Marking Requirement)
- Template wajib memiliki minimal **3 elemen bertanda** untuk dapat diedit oleh pembuat website (contoh: Identitas Organisasi, Judul Hero, Deskripsi Singkat).
- Elemen bertanda tidak boleh bertingkat (*nested marking*), artinya elemen bertanda tidak boleh memiliki anak elemen yang juga ditandai untuk isian teks.

---

## 2. Tools & Teknologi yang Didukung

Penyedia template bebas menggunakan berbagai tools pengembangan frontend modern:

### A. Code Editor
- **Visual Studio Code (VS Code)** (Sangat direkomendasikan).
- **Sublime Text**, **WebStorm**, **Cursor**, **Vim / Neovim**, atau editor teks lainnya.

### B. Standar Bahasa Frontend
- **HTML5**: Tag semantik murni (\`<header>\`, \`<nav>\`, \`<main>\`, \`<section>\`, \`<article>\`, \`<footer>\`).
- **CSS3**: Flexbox, CSS Grid, Media Queries responsif, CSS Variables / Custom Properties (\`:root { --primary: #0068D6; }\`).
- **Vanilla JavaScript (ES6+)**: \`const/let\`, Arrow Functions, \`fetch()\`, DOM Manipulation, Promises, Async/Await.

### C. Format Aset Visual
- **Gambar**: WebP (Sangat direkomendasikan karena ringan), PNG (untuk transparansi), JPG/JPEG (untuk foto), SVG (untuk logo & ikon), ICO (favicon).
- **Ukuran Rekomendasi**: Di bawah 500 KB per gambar agar loading website cepat di smartphone.

### D. Tipografi & Font
- **Google Fonts** (\`fonts.googleapis.com\` / \`fonts.gstatic.com\`).
- **Font Terbuka (OFL)**: Geist Sans, Geist Mono, Plus Jakarta Sans, Inter, Roboto, Poppins.
- Berkas font lokal (.woff2, .woff, .ttf) dapat disertakan di folder \`fonts/\` atau \`font/\`.

### E. Framework CSS & Library CDN yang Diizinkan (Whitelist)
Domain CDN resmi yang diizinkan oleh sistem:
- \`cdn.jsdelivr.net\`
- \`cdnjs.cloudflare.com\`
- \`unpkg.com\`
- \`cdn.tailwindcss.com\`
- \`code.jquery.com\`

Library yang didukung:
- **Framework CSS**: Tailwind CSS (standalone CDN \`https://cdn.tailwindcss.com\`), Bootstrap 5, Bulma.
- **Interaktivitas Ringan**: Alpine.js, Swiper, Splide, AOS (Animate on Scroll), GSAP (GreenSock), jQuery.
- **Paket Ikon**: Lucide Icons, Font Awesome 6 (via CDN), Bootstrap Icons, Remix Icon.

---

## 3. Modul-Modul Sistem & Modul Kustom

OrgSpace menyediakan sistem modul cerdas tanpa perlu koding backend atau konfigurasi database. Anda cukup menyematkan atribut \`data-ws-*\` di markup HTML.

### A. Modul Autentikasi Pengurus (\`data-ws-auth\`)
Modul ini menghubungkan website dengan sesi kepengurusan dan Portal Pengurus Mandiri.

| Atribut HTML | Elemen | Fungsi |
|---|---|---|
| \`data-ws-auth="status"\` | \`<span>\`, \`<strong>\` | Menampilkan status login: teks "Tamu" jika belum login, atau nama pengurus jika sudah login. |
| \`data-ws-auth="login"\` | \`<a>\` | Tautan langsung menuju halaman login Portal Pengurus (\`/admin\`). |
| \`data-ws-auth="logout"\` | \`<button>\`, \`<a>\` | Tombol aksi keluar dari sesi pengurus. |

**Contoh Penerapan:**
\`\`\`html
<header class="navbar">
  <div class="brand">
    <img src="img/logo.png" alt="Logo">
    <span>Nama Organisasi</span>
  </div>
  <div class="auth-bar">
    <span>Hai, <strong data-ws-auth="status">Tamu</strong></span>
    <a href="admin" data-ws-auth="login" class="btn-portal">Portal Pengurus</a>
    <button type="button" data-ws-auth="logout" class="btn-logout">Keluar</button>
  </div>
</header>
\`\`\`

### B. Modul Daftar Dinamis Berita Kegiatan (\`data-ws-list="berita"\`)
Modul ini secara otomatis mengambil data berita kegiatan dari CMS OrgSpace dan merendernya dalam bentuk kartu berulang.

| Atribut HTML | Peran | Keterangan |
|---|---|---|
| \`data-ws-list="berita"\` | Pembungkus List | Kontainer utama daftar berita. |
| \`data-ws-item\` | Kartu Berulang | Template item yang akan digandakan untuk setiap data berita. |
| \`data-ws-field="judul"\` | Judul Berita | Mengisi teks judul berita. |
| \`data-ws-field="ringkasan"\` | Kutipan | Mengisi ringkasan isi berita. |
| \`data-ws-field="tanggal"\` | Tanggal | Mengisi tanggal rilis artikel berita. |
| \`data-ws-field="gambar"\` | Thumbnail | Mengisi atribut \`src\` foto berita. |
| \`data-ws-field="link"\` | Link Selengkapnya | Mengisi atribut \`href\` artikel lengkap. |
| \`data-ws-empty\` | Fallback Kosong | Elemen yang tampil jika belum ada data berita. |

**Contoh Penerapan:**
\`\`\`html
<section id="berita">
  <h2>Kabar & Berita Kegiatan</h2>
  <div data-ws-list="berita" class="news-grid">
    <article data-ws-item class="news-card">
      <img data-ws-field="gambar" src="img/placeholder.jpg" alt="Foto Berita">
      <span data-ws-field="tanggal">15 Oktober 2026</span>
      <h3 data-ws-field="judul">Judul Kegiatan</h3>
      <p data-ws-field="ringkasan">Ringkasan isi berita...</p>
      <a data-ws-field="link" href="#">Baca Selengkapnya -></a>
    </article>
    <div data-ws-empty class="empty-state">
      <p>Belum ada kabar berita yang dipublikasikan.</p>
    </div>
  </div>
</section>
\`\`\`

### C. Modul Formulir Interaktif (\`data-ws-form="pendaftaran"\`)
Modul formulir interaktif untuk pendaftaran open recruitment anggota, kontak, atau aspirasi.

| Atribut HTML | Peran |
|---|---|
| \`data-ws-form="pendaftaran"\` | Atribut pada tag \`<form>\`. Sistem otomatis menangani pengiriman request via AJAX ke backend. |
| Atribut \`name="..."\` | Nama field input (contoh: \`name="nama"\`, \`name="email"\`, \`name="whatsapp"\`, \`name="divisi"\`, \`name="alasan"\`). |
| \`<button type="submit">\` | Tombol kirim formulir. |

**Contoh Penerapan:**
\`\`\`html
<form data-ws-form="pendaftaran" class="form-pendaftaran">
  <div class="form-group">
    <label>Nama Lengkap</label>
    <input type="text" name="nama" placeholder="Masukkan nama" required>
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
      <option value="Humas">Humas</option>
      <option value="Teknologi">Teknologi & Informasi</option>
      <option value="Kaderisasi">Kaderisasi</option>
    </select>
  </div>
  <div class="form-group">
    <label>Motivasi Bergabung</label>
    <textarea name="alasan" rows="3"></textarea>
  </div>
  <button type="submit" class="btn-submit">Kirim Pendaftaran</button>
</form>
\`\`\`
*Data formulir yang dikirim oleh pengunjung otomatis tersimpan di database dan dapat dilihat/diunduh oleh pengurus melalui Portal Pengurus.*

### D. Modul Kustom Bebas (\`data-ws-collection="[nama]"\`)
Modul kustom memberikan kebebasan bagi kreator template untuk membuat koleksi data dinamis apa pun (contoh: \`prestasi\`, \`galeri\`, \`alumni\`, \`program_kerja\`, \`divisi\`).

**Aturan Penulisan:**
1. Gunakan tag pembungkus \`<div data-ws-collection="[nama_koleksi]">\`.
2. Gunakan tag perulangan item \`<div data-ws-item>\`.
3. Gunakan tag data field \`<... data-ws-field="[nama_field]">\`.
4. Sertakan penanda \`<div data-ws-empty>\` sebagai tampilan fallback jika data kosong.

**Contoh Penerapan Modul Prestasi Kustom:**
\`\`\`html
<section id="prestasi">
  <h2>Prestasi & Penghargaan</h2>
  <div data-ws-collection="prestasi" class="grid-prestasi">
    <div data-ws-item class="card-prestasi">
      <span data-ws-field="tahun">2026</span>
      <h3 data-ws-field="judul">Juara 1 Kompetisi Inovasi Nasional</h3>
      <p>Peraih: <strong data-ws-field="peraih">Tim Robotika</strong></p>
    </div>
    <div data-ws-empty class="empty-state">
      <p>Belum ada data prestasi yang ditambahkan.</p>
    </div>
  </div>
</section>
\`\`\`
*Keunggulan: Di Portal Pengurus, sistem otomatis menyediakan halaman CRUD (Tambah, Edit, Hapus) untuk koleksi \`prestasi\` ini tanpa perlu coding backend!*

---

## 4. Panduan Lengkap Meng-online-kan Website bagi Pembuat Website

Berikut adalah panduan menyeluruh bagi pengurus organisasi atau pengguna umum untuk membuat dan mempublikasikan website resmi organisasi menggunakan aplikasi OrgSpace Android:

### Langkah 1: Membuka Aplikasi & Memilih Template
1. Buka aplikasi **OrgSpace** di HP Android.
2. Pastikan Anda berada di **Mode Pembuat Website** (terlihat nama organisasi di kiri atas).
3. Buka tab navigasi **Template** di bagian bawah.
4. Gunakan filter kategori organisasi:
   - *OSIS / MPK*, *BEM / DEMA / DPM*, *Unit Kegiatan Mahasiswa (UKM)*, *Ekstrakurikuler*, *Komunitas*, dll.
5. Perhatikan badge modul dinamis (Berita, Oprec, Prestasi, Kontak).
6. Ketuk kartu template pilihan Anda untuk memulai.

### Langkah 2: Menggunakan Project Editor & Live Preview
1. Aplikasi otomatis menginisiasi proyek baru di database lokal dan membuka **Project Editor**.
2. **Live Preview WebView**:
   - Preview website langsung aktif di bagian tengah layar.
   - Uji responsivitas menggunakan toggle **HP** (mobile) dan **Desktop** (layar lebar).
3. **Mengedit Teks & Paragraf**:
   - Pada lembar isian di bawah (Tab **Isi**), pilih section (misal: *Hero*).
   - Masukkan nama organisasi Anda (contoh: "Himpunan Mahasiswa Inovasi Digital").
   - Teks pada WebView **langsung terupdate secara real-time** seketika saat Anda mengetik.
4. **Mengubah Gambar / Banner**:
   - Ketuk tombol **Ubah Gambar** pada field foto/banner.
   - Pilih gambar dari galeri HP Anda. Sistem akan mengompresi gambar secara otomatis agar ringan.
5. **Mode Pratinjau Penuh**:
   - Ketuk ikon layar penuh di toolbar atas untuk melihat website satu layar penuh tanpa gangguan form.

### Langkah 3: Melakukan Cek Kelengkapan
1. Buka menu titik tiga (⋮) di toolbar kanan atas -> pilih **Cek kelengkapan**.
2. Periksa status proyek:
   - Pastikan seluruh isian wajib terisi (status berubah menjadi **"Siap export"**).
   - Periksa saran opsional jika ada isian yang masih memakai teks contoh.

### Langkah 4: Publikasi Langsung ke Server (Publishing)
1. Ketuk tombol **Export / Selesai & Publikasi**.
2. Tersedia dua pilihan:
   - **Publish Langsung Sekarang**: Menerbitkan website langsung ke server lokal / cloud hosting OrgSpace.
   - **Download as ZIP File**: Mengunduh seluruh berkas website ke memori internal HP.
3. Pilih **"Publish Langsung Sekarang"**.
4. Server OrgSpace akan memproses kompilasi template dan mengaktifkan website ke status publik.
5. Dialog sukses muncul:
   - **Status**: \`Online 🟢\`
   - **Ruang Kerja**: Nama Organisasi Anda
   - **Port Server**: \`8080\` (atau port hosting publik)
   - **Alamat Website**: \`http://localhost:8080/sites/[nama-website]/index.html\` (atau domain publik \`orgspace.app/[slug]\`).

### Langkah 5: Membuka & Mengelola Website yang Sudah Online
1. Ketuk tombol **"Buka Website"** pada dialog sukses.
2. Browser Chrome di HP akan langsung terbuka dan merender website resmi Anda dengan status \`HTTP 200 OK\`.
3. **Pengelolaan Lanjutan di Tab Website**:
   - Website yang sudah online tercatat rapi di tab **Website** pada Ruang Pribadi Anda.
   - Kapan pun ingin mengubah teks, memperbarui berita, atau mengganti banner, cukup ketuk website tersebut lalu pilih **"Edit tampilan"**.
   - Perubahan yang Anda simpan akan langsung disinkronkan ke website yang sedang tayang secara real-time.
`;

export default async function DocsHome() {
  let tree;
  try {
    tree = await getDocTree();
  } catch (error) {
    return <ErrorBox error={error} />;
  }

  return (
    <article className="space-y-12">
      <div>
        <h1 className="text-3xl font-bold">Dokumentasi OrgSpace</h1>
        <p className="mt-2 text-muted">
          Panduan teknis spesifikasi template, perkakas yang didukung, modul dinamis, serta cara membuat & meng-online-kan website.
        </p>

        {/* Indeks Cepat Artikel */}
        <div className="mt-8 grid gap-4 md:grid-cols-2">
          {tree.categories.flatMap((category) =>
            category.articles.map((article) => (
              <Link
                key={article.id}
                href={`/docs/${article.slug}`}
                className="card hover:border-border-strong transition-colors"
              >
                <p className="text-xs font-semibold text-muted">{category.label}</p>
                <p className="mt-1 font-semibold text-foreground">{article.title}</p>
                {article.summary && <p className="mt-1 text-sm text-muted">{article.summary}</p>}
              </Link>
            )),
          )}
        </div>
      </div>

      <hr className="border-border" />

      {/* Konten Dokumentasi Lengkap & Rinci */}
      <div className="pt-2">
        <Markdown>{DETAILED_DOCS_CONTENT}</Markdown>
      </div>
    </article>
  );
}
