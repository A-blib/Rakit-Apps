# Alur Fitur Upload (Provider)

> **Status:** Hasil diskusi 6 Oktober 2026. Alur besar, susunan langkah, pengecekan di awal, cara menandai, aturan pengecekan file, library luar, dan multi-halaman sudah disepakati. **Dokumen ini belum untuk dieksekusi agent sampai Aris memintanya.** Selama itu, tab Upload tetap "Segera hadir" sesuai `alurUntukProvider.md` bagian 5.0.
>
> Dokumen ini melengkapi `alurUntukProvider.md` bagian 5 dan **menggantikan bagian 5.3** (cara kerja mode tandai) di sana.
>
> Terakhir diperbarui: 7 Oktober 2026 (cek konten JS statis di server; laporkan pengecekan keliru; ZIP uji; pesan error → Panduan)

---

## 1. Keputusan yang sudah final

| Keputusan | Isi |
|---|---|
| Fungsi Upload | Tempat provider **mengunggah project website (ZIP)** lalu **menandai bagian mana saja yang boleh diedit** oleh pembuat website, sebelum dipajang sebagai template. |
| Fitur dipecah | Upload **tidak dibuat sebagai satu layar besar**, melainkan **langkah-langkah kecil** (wizard), satu layar satu tugas (bagian 3). |
| Info template | Nama, kategori, **deskripsi (wajib)**, **kata kunci (wajib, minimal 1, maks 5)**, thumbnail (otomatis / dari section / upload), dan info teknis otomatis (bagian 6). |
| Urutan langkah | Pilih file → Pengecekan file → **Info template** → Tandai bagian → Coba sebagai pengguna → Kirim. Info template diisi **sebelum** pekerjaan inti (bagian 3 dan 6). |
| Pengecekan di awal | Sistem **mengecek file langsung setelah ZIP diupload**, sebelum provider menandai. Lolos → lanjut. Gagal → layar "Belum memenuhi standar" + kartu di "Perlu tindakan" Beranda (bagian 5). Tujuannya agar provider tidak capek menandai lalu ditolak di akhir. |
| Aturan pengecekan file | Daftar aturan bagian 5.6, dibagi **Error** (tidak bisa lanjut) dan **Peringatan** (boleh lanjut, terus diingatkan). |
| Batas ukuran | ZIP maks **20 MB**; isi setelah diekstrak maks **50 MB** dan **500 file**; satu file maks **10 MB**; gambar > **1 MB** = peringatan; total berat halaman > **5 MB** = peringatan. |
| Struktur project | **Bebas memakai banyak file CSS dan JS** dalam folder apa pun, demi kode yang rapi bagi pembuatnya (bagian 10.1). |
| Multi-halaman | Template **boleh berisi banyak halaman HTML** (mis. `index.html`, `tentang.html`, `kontak.html`), maksimal **10 halaman**. `index.html` wajib sebagai halaman utama (bagian 10.2). |
| HTML potongan (partials) & alat build | **Boleh**, dengan syarat provider memakai **alat build** (Vite, Eleventy, Astro, dll.) **di laptopnya sendiri** lalu mengupload **hasil build** (mis. folder `dist/`). Server **tidak** menjalankan build. Partials yang dimuat lewat `fetch()` tanpa build tetap tunduk pada aturan "konten harus tertulis di HTML" (bagian 10.3). |
| Framework komponen | **React, Vue, Svelte (termasuk Next.js, Gatsby, Nuxt, Astro island) belum didukung** di versi awal. Build tidak wajib: HTML/CSS/JS yang ditulis langsung boleh diupload apa adanya (bagian 10.6). |
| Library luar | **Boleh** memakai library seperti Tailwind, Bootstrap, Alpine.js, dan library pendukung lain, baik disertakan di ZIP maupun dari CDN. Library dari CDN wajib ada di **daftar library yang diizinkan**, dari CDN terpercaya, dan versinya jelas (bagian 10.4). |
| Penyalinan library | Saat template dikirim, **server mengunduh library dari CDN dan menyimpannya di dalam paket template**, lalu link CDN di HTML diganti ke file lokal. Preview tetap rapi saat offline dan ZIP hasil export utuh (bagian 10.5). |
| Cara menandai | Provider **mengetuk elemen** (pengganti hover/kursor di HP) untuk menyorotnya, lalu menetapkannya sebagai bagian yang boleh diedit lewat bottom sheet (bagian 7.3). |
| Tampilan slide | Website ditampilkan **per section seperti slide PPT**, satu section satu slide (bagian 7.2). |
| Ukuran slide | Slide **tidak berukuran tetap**. Tinggi slide **mengikuti tinggi asli section**, lebar pas layar HP (bagian 7.2). |
| Deteksi section | Berlapis: tanda `data-section` → tag semantik → **membaca tampilan** (untuk HTML yang isinya `div` semua) → seluruh halaman jadi 1 section. Provider bisa mengoreksi hasilnya (bagian 7.1). |
| Cek konten buatan JS | Error "konten utama dibuat JS" ditentukan **server secara statis**; WebView HP hanya menambah Peringatan. Versi berikutnya: render di server dengan Chromium headless (bagian 5.1). |
| Laporkan pengecekan keliru | Setiap Error punya tautan **"Ini keliru? Laporkan"**. Laporan disimpan untuk memperbaiki aturan; template tidak otomatis lolos (bagian 5.9). |
| ZIP uji per aturan | Setiap aturan pengecekan punya minimal `gagal.zip` dan `lolos.zip` sebagai test otomatis (bagian 5.10). |
| Pesan error → Panduan | Setiap aturan punya kode yang tertaut ke artikel Panduan (kenapa, contoh salah/benar, cara memperbaiki, tips); artikel disimpan di server (bagian 5.11). |
| Pilihan tampilan | **HP dan Desktop saja** (tanpa Tablet). Desktop = simulasi lebar 1280px di dalam HP (bagian 7.4). |
| Gaya yang bisa diubah | Selain isi (teks, gambar, link), pembuat website bisa mengubah **gaya**: warna teks, warna latar, sudut (radius), ukuran huruf dalam rentang, tema global (variabel CSS); warna hover dan jenis huruf menyusul (V1+). **Tata letak tetap terkunci.** Diterapkan lewat lapisan `custom.css`, file provider tidak diubah (bagian 7.11). |
| Tingkat penandaan | **Tingkat elemen** (teks, gambar, link, tombol; plus gaya per elemen). Section hanya alat navigasi provider. Data section tetap disimpan agar nanti bisa dikembangkan ke penandaan tingkat section (bagian 7.1). |
| Jumlah isian minimal | **3 isian** yang ditandai agar template boleh dikirim. Syarat ini berlaku untuk tombol **Lanjut: Kirim**, bukan untuk masuk ke Coba (bagian 7.12). |
| Tandai ⇄ Coba bolak-balik | Provider bisa bekerja **bertahap**: tandai beberapa elemen → **Simpan** → **Coba** → kembali ke Tandai → dan seterusnya. Tandaan hanya bisa dicoba setelah tombol **Simpan** ditekan (bagian 7.12). |
| Halaman awal tab Upload | Tab Upload **tidak langsung membuka pemilih file**, melainkan halaman awal yang isinya menyesuaikan kondisi provider. Urutan: **Perlu diperbaiki → Lanjutkan draft → Upload template baru** (bagian 3.1). |
| Wizard layar penuh | Selama wizard, **bottom nav disembunyikan**. Tombol ✕ di kiri atas; setelah langkah 2 lolos, keluar = tersimpan sebagai draft (bagian 3.2). |
| Tata letak editor | Semua layar wizard memakai **tiga zona**: atas = navigasi, tengah = preview website, bawah = alat (bagian 3.3, 7.2, 7.3, 8). |
| Sub-fitur editor | Daftar sub-fitur dan prioritasnya (V1 / V1+ / Nanti) ada di bagian 7.10. |
| Library `androidx.webkit` | **Disetujui** untuk `WebViewAssetLoader` (bagian 7.9). |
| Aturan draft | Memakai `alurUntukProvider.md` bagian 5.4: maks 5 draft per provider, draft yang tidak disentuh 30 hari dihapus otomatis dengan pemberitahuan sebelumnya. |

---

## 2. Alur besar (dari Aris)

```
Provider klik Upload → pilih file ZIP project
        │
        ▼
Sistem verifikasi dengan algoritma pengecekan
        │
        ├─ lolos → masuk ke pengaturan project: bagian mana saja yang bisa diedit user, dll.
        │
        └─ gagal → tampilkan UI "file project belum memenuhi standar requirement"
                   + kirim notifikasi ke halaman Beranda
```

---

## 3. Susunan langkah

```
1. Pilih file → 2. Pengecekan file → 3. Info template → 4. Tandai bagian → 5. Coba sebagai pengguna → 6. Kirim
                       │
                       └─ gagal → layar "Belum memenuhi standar" + "Perlu tindakan" di Beranda
```

| Langkah | Tugas | Bagian |
|---|---|---|
| **1. Pilih file** | Pilih ZIP dari HP / Drive / WhatsApp / USB. App melakukan cek kilat di HP | 4 |
| **2. Pengecekan file** | Server mengecek requirement file. Error menghentikan proses, peringatan boleh lanjut | 5 |
| **3. Info template** | Nama, kategori, deskripsi, thumbnail. Diisi **sebelum** pekerjaan inti | 6 |
| **4. Tandai bagian** | Menandai elemen yang boleh diedit, per halaman dan per section seperti slide | 7 |
| **5. Coba sebagai pengguna** | Melihat dan mencoba form edit yang akan dilihat pembuat website | 8 |
| **6. Kirim** | Pengecekan akhir yang ringan, penyalinan library, lalu template tayang | 9 |

Aturan untuk semua langkah:

- Indikator **"Langkah X dari 6"** di bagian atas.
- Setelah lolos langkah 2, pekerjaan tersimpan sebagai **draft** di server. Langkah 3 tersimpan otomatis; tandaan di langkah 4 tersimpan lewat tombol **Simpan**, dengan cadangan otomatis di HP (bagian 7.12). Provider boleh berhenti dan melanjutkan kapan saja.
- Langkah 4 (Tandai) dan 5 (Coba) bisa **bolak-balik** (bagian 7.12).
- Provider bisa kembali ke langkah sebelumnya tanpa kehilangan pekerjaan.

### 3.1 Halaman awal tab Upload

Saat provider menekan tab **Upload** di bottom nav, yang tampil adalah halaman awal Upload, bukan langsung pemilih file. Isinya menyesuaikan kondisi provider.

**Kondisi A: belum ada draft dan tidak ada upload yang gagal**

```
┌──────────────────────────────┐
│ Upload                       │
├──────────────────────────────┤
│ Upload template baru         │
│ Ubah project websitemu jadi  │
│ template yang bisa dipakai   │
│ orang lain.                  │
│                              │
│ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐ │
│        [ikon file zip]       │
│ │    Pilih file ZIP        │ │
│     ZIP · maks 20 MB         │
│ └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘ │
│                              │
│ SEBELUM UPLOAD               │
│ ✓ Ada index.html             │
│ ✓ Konten ditulis di HTML     │
│ ✓ Library dari daftar resmi  │
│ ✓ Pakai Vite? Upload dist/   │
│ Lihat panduan lengkap →      │
│                              │
│ ALUR UPLOAD                  │
│ 1 Pilih → 2 Cek → 3 Info →   │
│ 4 Tandai → 5 Coba → 6 Kirim  │
├──────────────────────────────┤
│ Beranda  Upload  Template  Profil│
└──────────────────────────────┘
```

- **Kotak bergaris putus-putus** = tombol utama; satu ketukan membuka pemilih file Android.
- **Sebelum upload** = 4 syarat terpenting dari aturan pengecekan (bagian 5.6), dengan tautan ke Panduan.
- **Alur upload** = garis kecil 6 langkah agar provider tahu apa yang akan dikerjakan.

**Kondisi B: ada draft dan/atau upload yang gagal**

```
┌──────────────────────────────┐
│ Upload                       │
├──────────────────────────────┤
│ PERLU DIPERBAIKI (1)         │
│ ┌──────────────────────────┐ │
│ │ ✕ toko-kue.zip           │ │
│ │ 2 error · 1 peringatan   │ │
│ │ [Upload file perbaikan]  │ │
│ └──────────────────────────┘ │
│                              │
│ LANJUTKAN DRAFT      2 dari 5│
│ ┌──────────────────────────┐ │
│ │[thumb] Sekolah Hijau     │ │
│ │ Langkah 4/6 · Tandai     │ │
│ │ ▓▓▓▓▓▓▓░░░ 5 isian       │ │
│ │ Diedit 2 jam lalu        │ │
│ └──────────────────────────┘ │
│ ┌──────────────────────────┐ │
│ │[thumb] Portofolio Minimal│ │
│ │ Langkah 3/6 · Info       │ │
│ │ ⚠ Dihapus dalam 5 hari   │ │
│ └──────────────────────────┘ │
│                              │
│ [ + Upload template baru ]   │
└──────────────────────────────┘
```

| Bagian | Aturan |
|---|---|
| Urutan | **Perlu diperbaiki → Lanjutkan draft → Upload template baru**, karena pekerjaan yang belum selesai sebaiknya dituntaskan dulu |
| Perlu diperbaiki | Upload berstatus `check_failed`; isinya sama dengan kartu di "Perlu tindakan" Beranda. Tombol **Upload file perbaikan** |
| Lanjutkan draft | Kartu berisi thumbnail, nama, langkah terakhir, progres isian, waktu edit terakhir, dan peringatan jika draft akan dihapus (30 hari tidak disentuh). Ketuk → melanjutkan **tepat di langkah terakhir**. Menu ⋮ → Hapus draft |
| Kuota draft | Penghitung "2 dari 5". Jika sudah 5, tombol upload baru nonaktif dengan teks "Selesaikan atau hapus draft dulu (maks 5)" |
| Upload template baru | Tombol biasa di bawah. Checklist "Sebelum upload" ditampilkan setelah tombol ditekan, sebelum pemilih file terbuka |

### 3.2 Aturan tampilan wizard

- Wizard terbuka **layar penuh**; **bottom nav disembunyikan** agar provider fokus.
- Tombol **✕** di kiri atas. Setelah langkah 2 lolos, menekan ✕ menampilkan: *"Pekerjaanmu tersimpan sebagai draft. Lanjutkan kapan saja dari tab Upload."*
- Memilih file **RAR** langsung memunculkan pesan "Format RAR belum didukung" + tombol "Lihat caranya".
- Gaya visual mengikuti token desain AGENTS.md bagian 9: monokrom, garis tipis, label kecil Geist Mono (`PERLU DIPERBAIKI`, `LANJUTKAN DRAFT`). Warna semantik hanya untuk error (merah), peringatan (kuning), dan tanda selesai (hijau).

### 3.3 Tata letak umum editor (langkah 4–5)

Semua layar editor memakai **tiga zona** yang sama, supaya provider tidak perlu belajar ulang setiap pindah langkah:

| Zona | Isi |
|---|---|
| **Atas** (navigasi) | Tombol ✕/kembali, judul langkah, `X/6`, pemilih halaman, toggle HP/Desktop, penghitung isian |
| **Tengah** (preview) | Website yang sedang dikerjakan; mendapat ruang paling besar |
| **Bawah** (alat) | Saran otomatis, toolbar ikon, strip slide atau form, tombol lanjut |

Bottom sheet (tandai elemen, daftar elemen, panel isian) muncul dari zona bawah dan bisa ditarik lebih tinggi.

---

## 4. Langkah 1: Pilih file

- Satu tombol **"Pilih file ZIP"** yang membuka pemilih file bawaan Android (sudah diputuskan di `alurUntukProvider.md` bagian 5.2: ZIP saja, bantuan untuk pengguna RAR).
- **Cek kilat di HP** sebelum upload, supaya file yang jelas rusak tidak perlu dikirim dan kuota provider tidak terbuang:
  - ZIP bisa dibuka
  - Ada `index.html` (di folder paling luar, atau di dalam satu folder pembungkus)
  - Ukuran ZIP tidak lebih dari 20 MB
- Lolos cek kilat → ZIP diupload ke server → langkah 2.

---

## 5. Langkah 2: Pengecekan file

### 5.1 Siapa yang mengecek

| Pengecek | Isi | Alasan |
|---|---|---|
| **Server** (penentu akhir) | Semua aturan file (bagian 5.6) | Pengecekan di HP bisa diakali, jadi hasil yang sah selalu dari server |
| **WebView di HP** (pembantu) | Menemukan **sebagian** konten yang dibuat JavaScript, error JS saat dijalankan, tampilan melebar. Hasilnya **hanya Peringatan** | Cek ini butuh halaman yang dirender. Error "konten utama dibuat JS" tidak bergantung pada HP, karena hasil HP bisa berbeda per perangkat dan bisa dipalsukan |

### 5.2 Jenis pengecekan

| Jenis | Isi | Kapan dicek |
|---|---|---|
| **Pengecekan file** | Semua aturan di bagian 5.6 | **Di langkah 2** (awal) |
| **Pengecekan tandaan** | Minimal 3 isian, setiap isian berlabel, `data-key` tidak ganda, elemen versi HP dan desktop sudah dihubungkan | **Langsung saat menandai** (bagian 7.7) dan sekali lagi saat Kirim (bagian 9) |

### 5.3 Tingkat masalah

| Tingkat | Akibat |
|---|---|
| **Error (E)** | Provider **tidak bisa lanjut** ke langkah berikutnya. Muncul layar "Belum memenuhi standar". |
| **Peringatan (P)** | Provider **tetap boleh lanjut**. Peringatan dicatat, template boleh tayang, dan provider terus diingatkan sampai diperbaiki (`alurUntukProvider.md` bagian 4.8). |

### 5.4 Layar "Belum memenuhi standar"

```
┌──────────────────────────────┐
│ ✕ Belum memenuhi standar     │
│ toko-kue.zip                 │
├──────────────────────────────┤
│ ERROR (2)                    │
│ ● File index.html tidak ada  │
│   Pastikan index.html ada di │
│   folder paling luar ZIP.    │
│ ● Library belum diizinkan    │
│   main.html memuat           │
│   cdn.jsdelivr.net/npm/xyz   │
│   Sertakan file-nya di ZIP.  │
│   Pelajari caranya ›         │
│   Ini keliru? Laporkan ›     │
│                              │
│ PERINGATAN (1)               │
│ ● hero.jpg 4 MB (maks 1 MB)  │
├──────────────────────────────┤
│ [ Upload file perbaikan ]    │
│ [ Nanti saja ]               │
└──────────────────────────────┘
```

- Setiap masalah berisi **apa yang salah**, **letaknya** (file, dan baris jika ada), dan **cara memperbaikinya**.
- **Upload file perbaikan**: pilih ZIP baru → langsung dicek lagi. File baru **menggantikan** ZIP di catatan template yang sama, bukan membuat template baru.
- **Nanti saja**: kembali ke Beranda. Masalahnya tetap ada di **"Perlu tindakan"** beserta notifikasinya.
- **Ini keliru? Laporkan**: ada di setiap Error (bagian 5.9).

### 5.5 Status template selama Upload

Memakai status yang sudah ada di `alurUntukProvider.md` bagian 4.3:

```
Upload ZIP → checking ─┬─ error ───────────→ check_failed  (Perlu tindakan + notifikasi)
                       └─ lolos / peringatan → draft        (lanjut, tersimpan otomatis)

draft → Kirim → checking ─┬─ lolos ──→ published
                          └─ error ──→ kembali ke draft dengan daftar masalah (jarang terjadi, lihat bagian 9)
```

### 5.6 Daftar aturan pengecekan file

**E** = Error, **P** = Peringatan.

Setiap aturan punya salah satu dari empat tujuan: **bisa tampil** (website terbuka benar), **bisa ditandai** (sistem penandaan bekerja), **aman** (melindungi pembuat website dan pengunjungnya), **berkualitas** (cepat, rapi, responsif). Hampir semua Error ada di kelompok "bisa ditandai" dan "aman"; kualitas cukup Peringatan. Ringkasan visual: `validasi-upload-template.drawio`.

Pengecekan dijalankan dalam tiga tahap:

| Tahap | Di mana | Isi |
|---|---|---|
| **A. Struktur ZIP** | Server | Bagian A, B, G (ukuran). Jika ada **error fatal** (ZIP rusak, tanpa `index.html`, terlalu besar), pengecekan berhenti di sini |
| **B. Baca kode** | Server | Bagian C, C2, D, E, F |
| **C. Jalankan halaman** | WebView di HP (pembantu) | Error JS saat dijalankan, konten buatan JS, tampilan melebar |

Selain error fatal, **semua aturan tetap dijalankan** dan semua masalah dilaporkan **sekaligus**, agar provider tidak terjebak siklus "perbaiki satu, upload, ketemu error baru".

**A. ZIP dan struktur**

| Aturan | Tingkat |
|---|---|
| ZIP rusak atau dikunci password | E |
| Tidak ada `index.html` | E |
| `index.html` berada di dalam satu folder pembungkus (mis. `toko-kue/index.html`) | **Diterima otomatis**; folder itu dianggap folder utama |
| ZIP > 20 MB | E |
| Isi setelah diekstrak > 50 MB atau lebih dari 500 file (mencegah *zip bomb*) | E |
| Nama file berbahaya (`../`, path absolut, symlink) | E |
| File sampah (`__MACOSX`, `.DS_Store`, `Thumbs.db`) | Dibuang otomatis, tidak dianggap masalah |
| Lebih dari 10 halaman HTML | E |
| ZIP berisi **kode sumber** (`package.json`, `vite.config.*`, `src/`, `node_modules/`) tanpa HTML hasil build | E: "Sepertinya ini kode sumber, bukan hasil build. Jalankan `npm run build` di laptopmu, lalu upload isi folder `dist/` sebagai ZIP." |
| `package.json` / file konfigurasi ikut terbawa di samping hasil build yang valid | Diabaikan, tidak dianggap masalah |
| File `.scss` / `.less` mentah, atau tag `<include>` di HTML ("resep" yang belum di-build) | E. Saran: build dulu lalu upload isi `dist/` |
| **Huruf besar/kecil nama file tidak cocok** (HTML memanggil `img/Hero.jpg`, filenya `img/hero.jpg`) | E. Di Windows/macOS terlihat normal, tetapi di hosting Linux dan di Android (app ini) file tidak ditemukan |
| Nama file dengan spasi atau karakter aneh (`foto produk (1).jpg`) | P. Saran: huruf kecil, tanpa spasi, pakai tanda hubung |

**B. Jenis file yang diizinkan**

- Boleh: `.html` `.htm` `.css` `.js` `.mjs` `.png` `.jpg` `.jpeg` `.webp` `.gif` `.svg` `.ico` `.woff` `.woff2` `.ttf` `.otf` `.txt` `.md`
- Selain itu: **E** (mis. `.php`, `.py`, `.exe`, `.apk`, `.sh`, `.bat`, video).
- Khusus `.php`: pesan "Template ini butuh server (PHP). App hanya mendukung website statis HTML/CSS/JS."

**C. HTML**

| Aturan | Tingkat |
|---|---|
| File HTML kosong atau tidak bisa dibaca | E |
| Konten utama dibuat oleh JavaScript. **Ditentukan server secara statis**: `<body>` HTML asli hampir kosong (mis. teks < 50 karakter **dan** gambar < 1) dan ada `<script>`. Ambang disimpan di pengaturan server | E |
| Sebagian kecil konten dibuat JavaScript/Alpine (mis. isi slider, `x-text`) | P: "Bagian ini tidak bisa ditandai" |
| `<form>` mengirim data ke domain luar | E (melindungi data pengunjung website pemakai template). Saran: pakai link WhatsApp atau `mailto:` |
| Redirect otomatis ke situs lain (`meta refresh`, `window.location`) | E |
| Tidak ada `<meta name="viewport">` | P |
| Tidak ada `<title>`, atau gambar tanpa `alt` | P |
| **Framework komponen terdeteksi** (jejak React/Vue/Svelte/Next.js/Gatsby/Nuxt, mis. `__NEXT_DATA__`, `___gatsby`, `__NUXT__`, `data-reactroot`, `<astro-island>`) | E: "Template React/Vue/Svelte belum didukung. Gunakan HTML, CSS, dan JS biasa." (alasan: bagian 10.6) |
| Halaman hampir tanpa teks/gambar yang bisa ditandai (perkiraan kasar, hanya kasus ekstrem) | E. Jumlah minimal 3 isian yang sebenarnya dihitung di langkah 4 |
| Atribut khusus app sudah dipakai provider (`data-edit`, `data-key`, `data-label`, `data-tpl-id`) | E (bentrok dengan sistem penandaan) |
| `<base href>` | E: membuat semua path relatif diambil dari situs lain, bukan dari ZIP. Saran: hapus baris ini |
| `<object>`, `<embed>`, `<applet>` | E (teknologi plugin lama) |
| Encoding bukan UTF-8 / tidak ada `<meta charset>` | P (huruf bisa rusak) |
| Tidak ada `<!DOCTYPE html>` atau `<html lang>` | P |
| `id` ganda dalam satu halaman | P (anchor dan JS bisa salah sasaran) |
| Gambar base64 besar ditanam langsung di HTML | P (HTML jadi berat; sebaiknya file terpisah) |

**D. File yang dirujuk dan antar-halaman**

| Aturan | Tingkat |
|---|---|
| CSS/JS lokal yang dirujuk tidak ada | E |
| Gambar lokal yang dirujuk tidak ada | P |
| Link ke halaman HTML lokal yang tidak ada (`<a href="kontak.html">`) | E |
| Link `#anchor` ke `id` yang tidak ada (`href="#kontak"`) | P (menu hanya tidak menggulir; website tidak rusak) |
| Website **tanpa navigasi sama sekali** (satu halaman, cukup di-scroll) | **Boleh.** Pengecekan link hanya berjalan jika link memang ada |
| File HTML yang tidak ditautkan dari halaman mana pun | P: "Halaman ini tidak bisa dibuka dari menu" |
| Gambar diambil langsung dari situs lain (hotlink) | P: bisa hilang sewaktu-waktu |
| Path diawali `/` (mis. `/assets/index.js`, bawaan Vite) | P: bisa rusak jika website dipasang di subfolder (mis. GitHub Pages). Saran: tambahkan `base: './'` di `vite.config.js`. Selama di app, path seperti ini dibaca dari folder utama ZIP |

**C2. CSS**

| Aturan | Tingkat |
|---|---|
| Gambar/font di `url(...)` (termasuk `@font-face`) tidak ada di ZIP | P |
| Kesalahan penulisan (kurung kurawal tidak tertutup, dll.) | P |
| `expression()`, `-moz-binding`, `behavior:` | E (trik lama menjalankan script lewat CSS) |
| Tidak ada `@media` sama sekali dan tidak memakai framework responsif | P: "Tampilan mungkin tidak menyesuaikan layar HP" |
| File CSS > 1 MB | P (biasanya Tailwind/Bootstrap belum di-*purge*) |

Bonus (bukan validasi): server mendeteksi variabel CSS di `:root` (mis. `--primary`) untuk fitur **tema global** di editor tandai (bagian 7.11).

**E. Library dan sumber dari luar** (detail di bagian 10.4)

| Aturan | Tingkat |
|---|---|
| Library dari CDN terpercaya, ada di daftar yang diizinkan, versi jelas | **Boleh** (disalin server saat Kirim) |
| Library dari CDN tetapi **tidak ada di daftar** | E. Saran: sertakan file library di ZIP, atau ajukan agar ditambahkan |
| Versi library tidak jelas (`@latest` atau tanpa versi) | E |
| Script/CSS dari domain yang bukan CDN terpercaya | E |
| Tailwind Play CDN (`cdn.tailwindcss.com`) | P: "Tidak disarankan untuk website produksi karena lebih berat. Sebaiknya pakai file CSS hasil build Tailwind" |
| Google Fonts | Boleh, P kecil: "Font butuh internet; tanpa internet font bawaan HP yang tampil" |
| `import` library dari CDN **di dalam file JS** | E (belum didukung versi awal). Saran: pakai `<script src>` di HTML atau sertakan file di ZIP |
| Analytics / pelacak (Google Analytics, Facebook Pixel, Hotjar, dll.) | E (melacak pengunjung website orang lain) |
| Library yang mengambil data dari server (mis. Firebase SDK) | E (template harus statis) |
| `<iframe>` dari YouTube atau Google Maps | Boleh |
| `<iframe>` dari domain lain | E |

**F. JavaScript (pemindaian sederhana)**

Berlaku untuk file JS milik provider. File library terkenal yang dikenali dari hash-nya tidak dipindai ulang.

| Aturan | Tingkat |
|---|---|
| Mengirim data ke domain luar (`fetch`, `XMLHttpRequest`, `WebSocket`, `sendBeacon`) | E |
| `eval()` / `new Function()` | P |
| Link ke situs luar yang disembunyikan (`display:none`, ukuran 0) | E (mencegah backlink titipan di website orang lain) |
| Mendaftarkan **Service Worker** | E (bisa terus mengendalikan website walaupun file sudah diganti) |
| Kode diacak/disamarkan (*obfuscated*, mis. `eval(function(p,a,c,k,e,d)…`) | E (tidak bisa diperiksa). Kode yang hanya di-*minify* tetap boleh |
| Pola penambang kripto / WebAssembly dari luar | E |
| `setTimeout("teks")` / `setInterval("teks")` | P |
| `document.write()` | P (bisa merusak halaman dan penandaan) |
| Popup saat halaman dibuka (`alert`, `confirm`, `window.open`) | P |
| Syntax error | P (script tidak jalan, interaksi mati) |
| Error JavaScript saat halaman dijalankan (ditangkap dari WebView) | P, mis. "main.js baris 42: `slider` tidak ditemukan" |

**G. Ukuran dan performa**

| Aturan | Tingkat |
|---|---|
| Satu file > 10 MB | E |
| Satu gambar > 1 MB | P. Saran: kompres atau ubah ke WebP |
| Total berat satu halaman > 5 MB | P: lambat dibuka |
| Hasil render di tampilan HP **melebar ke samping** (muncul scroll horizontal) | P: "Ada elemen yang lebih lebar dari layar HP" |

### 5.7 Catatan teknis pengecekan

- Setiap aturan punya **kode dan versi** (mis. `EXT_SCRIPT` v1). Jika aturan diperketat, template lama bisa dicek ulang (`alurUntukProvider.md` bagian 4.1).
- Target lama pengecekan **di bawah 30 detik**. Jika provider keluar dari layar, hasil dikirim lewat notifikasi.
- Daftar CDN terpercaya, daftar library yang diizinkan, dan daftar hash library terkenal disimpan di **pengaturan server**, sehingga bisa ditambah tanpa update app.

### 5.7a Alasan batas ukuran

Template statis yang sehat biasanya hanya **2–6 MB** (HTML 20–200 KB, CSS 50–300 KB, JS + library 100–500 KB, font 100–400 KB, gambar terkompres 1–5 MB).

| Batas | Nilai | Alasan |
|---|---|---|
| ZIP | 20 MB (E) | ± 3–4× ukuran template wajar. Di atasnya hampir pasti ada masalah: foto belum dikompres, video, atau `node_modules` ikut |
| Isi setelah ekstrak | 50 MB (E) | Gambar hampir tidak mengecil saat di-ZIP; jika jauh melebihi ini, hampir pasti *zip bomb* |
| Jumlah file | 500 (E) | Template wajar berisi puluhan sampai ± 150 file |
| Satu file | 10 MB (E) | Tidak ada file website normal sebesar ini |
| Satu gambar | 1 MB (P) | Gambar web idealnya 100–500 KB |
| Berat satu halaman | 5 MB (P) | Rata-rata halaman web di HP ± 2 MB; di atas 5 MB terasa lambat |

Urutan alasan: (1) **kualitas** website hasil akhir, (2) **kelancaran** upload, unduh, dan editor di HP biasa, (3) **biaya dan keamanan** server, (4) **hemat kuota** provider dan pembuat website (bonus).

Batas lebih ketat (mis. 5 MB) berisiko menolak template bagus yang banyak fotonya; lebih longgar (50–100 MB) membuat upload lambat, rawan gagal, boros kuota, dan mahal. Semua angka disimpan di **pengaturan server** dan bisa diubah berdasarkan data nyata tanpa update app.

### 5.7b Kenyamanan proses upload

| Fitur | Isi |
|---|---|
| Upload bisa dilanjutkan (*resumable*) | Jika sinyal putus di 70%, upload lanjut dari 70%, bukan dari awal |
| Progress bar dengan ukuran | Mis. "12,4 dari 18 MB" |
| Peringatan data seluler | Untuk file > 10 MB saat memakai data seluler: "File ini 18 MB. Kamu sedang memakai data seluler. Lanjutkan?" |
| Pesan error menunjuk file terbesar | Jika ZIP melewati batas: "5 file terbesar: hero.jpg 6,2 MB, …" + saran kompres / ubah ke WebP |

### 5.8 Layar saat pengecekan berjalan

```
┌──────────────────────────────┐
│ ✕  Upload       Langkah 2/6  │
├──────────────────────────────┤
│ toko-kue.zip · 3,2 MB        │
│                              │
│ ✓ Mengunggah file            │
│ ✓ Membuka ZIP                │
│ ✓ Memeriksa struktur & file  │
│ ◌ Memeriksa HTML & library   │
│ ○ Memeriksa ukuran           │
│                              │
│ Biasanya kurang dari 30 detik│
│ Boleh tinggalkan layar ini,  │
│ hasilnya dikirim lewat       │
│ notifikasi.                  │
└──────────────────────────────┘
```

- Pengecekan ditampilkan sebagai **daftar tahap yang dicentang satu per satu**, bukan sekadar loading berputar.
- **Lolos** → langsung ke langkah 3. Jika ada peringatan, ditampilkan dulu dengan tombol "Lanjut".
- **Gagal** → layar "Belum memenuhi standar" (bagian 5.4).

### 5.9 Laporkan pengecekan keliru

Pengecekan otomatis berbasis pola pasti kadang **salah tuduh** (kode sah yang mirip pola berbahaya, library yang belum dikenal hash-nya, jejak framework yang keliru). Karena tidak ada review manual, provider butuh jalan untuk melapor.

| Aturan | Isi |
|---|---|
| Letak | Tautan kecil **"Ini keliru? Laporkan"** di setiap Error, di layar "Belum memenuhi standar" dan di detail hasil pengecekan |
| Isi laporan | Alasan singkat dari provider (opsional), ditambah otomatis: kode & versi aturan, file dan baris, potongan kode yang tertangkap, ID upload/ZIP |
| Setelah dikirim | Pesan: "Terima kasih, laporanmu kami tinjau." Tautan berubah menjadi "Sudah dilaporkan" |
| Yang **tidak** terjadi | Template **tidak otomatis lolos**; provider tetap perlu memperbaiki atau menunggu aturan diperbaiki |
| Batas | Satu laporan per Error per upload, agar tidak bisa dibanjiri |
| Penanganan versi awal | Tanpa panel admin: laporan disimpan di tabel `check_reports` dan dibaca langsung lewat database (atau ringkasan email berkala, jika dibuat nanti). Aturan yang sering dilaporkan diperbaiki atau dilonggarkan, lalu versinya dinaikkan (bagian 5.7) |

Gambaran data:

```
check_reports
  id
  template_id
  check_id        → hasil pengecekan yang dilaporkan
  rule_code       → mis. EXT_FETCH
  rule_version
  location        → file + baris
  snippet         → potongan kode yang tertangkap
  reason          → alasan dari provider (boleh kosong)
  provider_id
  created_at
  status          → baru / dibaca / aturan_diperbaiki / tidak_berubah
```

### 5.10 Kumpulan ZIP uji per aturan

Setiap aturan pengecekan wajib punya minimal **dua ZIP kecil** sebagai data test otomatis:

```
backend/src/test/resources/test-fixtures/
├── CASE_MISMATCH/
│   ├── gagal.zip    ← HTML memanggil img/Hero.jpg, file aslinya hero.jpg
│   └── lolos.zip    ← nama cocok
├── BASE_HREF/
│   ├── gagal.zip    ← ada <base href="https://...">
│   └── lolos.zip
├── EXT_FETCH/
│   ├── gagal.zip    ← fetch('https://evil.com/...')
│   └── lolos.zip    ← fetch('data/menu.json') (lokal, boleh)
└── ...
```

| Aturan | Isi |
|---|---|
| Isi minimal | `gagal.zip` (harus tertangkap dengan kode aturan yang benar) dan `lolos.zip` (tidak boleh tertangkap) |
| Dijalankan | `./mvnw test`; test memastikan `gagal.zip` gagal dan `lolos.zip` lolos |
| Aturan baru | Tidak boleh ditambahkan tanpa pasangan ZIP ujinya |
| Dari laporan keliru | ZIP dari laporan yang terbukti salah tuduh (bagian 5.9) disimpan sebagai kasus `lolos` baru, agar kesalahan yang sama tidak terulang |
| Manfaat | Perubahan aturan tidak diam-diam merusak aturan lain; sejalan dengan aturan proyek "data dummy untuk test wajib ada" |
| Kerahasiaan | ZIP uji dibuat sendiri atau disederhanakan; ZIP asli provider tidak disimpan di repo tanpa izin |

### 5.11 Pesan error menautkan ke Panduan

Setiap aturan punya **kode** (mis. `CASE_MISMATCH`) yang terhubung ke **satu artikel pendek** di Panduan provider.

```
✕ Huruf besar/kecil nama file tidak cocok
  index.html baris 15 memanggil img/Hero.jpg,
  tapi nama filenya img/hero.jpg.
  Pelajari cara memperbaikinya ›        ← membuka artikel Panduan
  Ini keliru? Laporkan ›
```

| Bagian artikel | Contoh untuk `CASE_MISMATCH` |
|---|---|
| Kenapa ini masalah | Windows tidak membedakan huruf besar/kecil, tetapi hosting Linux dan Android membedakannya |
| Contoh salah ✕ dan benar ✓ | `<img src="img/Hero.jpg">` ✕ → `<img src="img/hero.jpg">` ✓ |
| Cara memperbaiki | Ganti nama file atau path di HTML menjadi huruf kecil semua, lalu ZIP ulang |
| Tips mencegah | Nama file huruf kecil, tanpa spasi, pakai tanda hubung |

| Aturan | Isi |
|---|---|
| Letak tautan | **"Pelajari cara memperbaikinya"** di setiap Error dan Peringatan (layar hasil pengecekan, "Perlu tindakan", layar Kirim) |
| Penyimpanan artikel | Di **server**, sehingga bisa diperbarui tanpa update app |
| Pesan di layar | Tetap pendek (apa yang salah · letaknya · saran singkat); penjelasan panjang ada di artikel |
| Penulisan | Satu artikel per aturan, dicicil mulai dari aturan yang paling sering memicu Error. Aturan yang belum punya artikel menampilkan tautan ke halaman Panduan umum |

---

## 6. Langkah 3: Info template

Tujuan layar ini: **membuat template mudah ditemukan dan menarik di galeri**. Semua isian muncul di kartu dan pencarian galeri pembuat website. Diisi **sebelum pekerjaan inti** (menandai), setelah file lolos pengecekan, sehingga draft sudah punya nama yang jelas sejak awal.

### 6.1 Isian

| Isian | Wajib? | Aturan | Kegunaan |
|---|---|---|---|
| **Nama template** | Wajib | 3–60 karakter. Terisi otomatis dari nama ZIP (`toko-kue.zip` → "Toko Kue"), bisa diganti. Peringatan jika sama dengan nama template lain milik provider sendiri | Judul kartu galeri dan pencarian |
| **Kategori** | Wajib | Pilih **satu**: Sekolah, Organisasi, Usaha / UMKM, Instansi, Pribadi / Portofolio, Lainnya | Filter galeri dan rekomendasi "Template untuk anda" (cocok dengan tujuan website di onboarding) |
| **Deskripsi singkat** | **Wajib** | 20–300 karakter, dengan penghitung. Contoh: "Landing page satu halaman untuk toko kue rumahan, ada katalog produk dan tombol pesan via WhatsApp" | Menjelaskan cocok untuk apa; ikut dicari di galeri |
| **Kata kunci** | **Wajib, minimal 1** | 1–5 kata kunci, masing-masing maks 20 karakter, huruf kecil, tanpa duplikat. Mis. `kue`, `kuliner`, `whatsapp` | Membantu pencarian di galeri |
| **Thumbnail** | Otomatis | Lihat bagian 6.2 | Gambar pertama yang dilihat orang di galeri |
| **Info teknis** | Otomatis, tidak bisa diedit | Diambil dari hasil validasi: jumlah halaman, library yang dipakai (mis. Bootstrap 5, Alpine.js), ukuran, responsif atau tidak | Pembuat website tahu isi template sebelum memilih |

### 6.2 Thumbnail

| Pilihan | Keterangan |
|---|---|
| **Otomatis** (default) | Tangkapan tampilan atas `index.html` (file sudah bisa dirender karena lolos langkah 2) |
| **Pilih dari section lain** | Mis. Hero atau Galeri; memakai hasil deteksi section (bagian 7.1) |
| **Upload gambar sendiri** | Maks 1 MB, dipotong sesuai rasio kartu galeri |

Toggle **HP / Desktop** menentukan thumbnail diambil dari tampilan mana.

### 6.3 Gambaran layar

```
┌──────────────────────────────┐
│ ✕   Info template        3/6 │
├──────────────────────────────┤
│ Thumbnail                    │
│ ┌──────────────────────────┐ │
│ │   [tangkapan tampilan]   │ │
│ └──────────────────────────┘ │
│ [Ganti dari section] [Upload]│
│                              │
│ Nama template *              │
│ [ Toko Kue               ]   │
│                              │
│ Kategori *                   │
│ (Sekolah)(Organisasi)(UMKM)  │
│ (Instansi)(Pribadi)(Lainnya) │
│                              │
│ Deskripsi singkat *          │
│ [                        ]   │
│ [                        ]   │
│                       0/300  │
│                              │
│ Kata kunci * (1–5)           │
│ [kue ×][kuliner ×][+ tambah] │
│                              │
│ INFO TEKNIS (otomatis)       │
│ 3 halaman · Bootstrap 5 ·    │
│ Alpine.js · 4,2 MB · ✓ HP    │
├──────────────────────────────┤
│ [ Kembali ]     [ Lanjut ]   │
└──────────────────────────────┘
```

### 6.4 Aturan layar

- Tombol **Lanjut** aktif setelah **nama, kategori, deskripsi, dan minimal 1 kata kunci** terisi sesuai aturan.
- Semua isian **tersimpan otomatis** ke draft.
- Semua isian bisa diubah lagi: sebelum Kirim, dan setelah tayang lewat Template Anda → Edit info.
- Konfirmasi hak pakai aset ("Saya berhak memakai gambar dan font di template ini") ditempatkan di langkah **Kirim** (bagian 9), bukan di sini.
- Upload yang gagal di langkah 2 belum punya info template, sehingga kartunya di "Perlu tindakan" memakai nama file ZIP.
- **Pencarian galeri** mencari di nama, deskripsi, dan kata kunci (`alurFiturPembuatanWibesite.md` bagian 6.2).

---

## 7. Langkah 4: Tandai bagian

### 7.1 Deteksi section

Script dijalankan di WebView setelah halaman tampil dan **semua gambar selesai dimuat**. Deteksi dilakukan per halaman.

| Prioritas | Cara | Contoh |
|---|---|---|
| 1 | Tanda dari provider di HTML (opsional) | `<div data-section="Hero">` |
| 2 | Tag semantik | `<header>`, `<nav>`, `<section>`, `<footer>` |
| 3 | **Membaca tampilan** (untuk HTML berisi `div` semua) | lihat di bawah |
| 4 | Semua gagal → seluruh halaman jadi 1 section "Halaman" | provider tetap bisa menandai elemen |

**Cara ke-3, membaca tampilan:**

1. Turun melewati elemen pembungkus (`div` yang hanya punya satu anak atau membungkus semuanya, mis. `wrapper`, `container`, juga `<main>`).
2. Berhenti di tingkat yang berisi beberapa blok besar yang tersusun ke bawah.
3. Blok dianggap section jika lebarnya hampir selebar halaman, tingginya cukup besar, dan tidak saling menumpuk.
4. Nama ditebak dari `class`/`id` (`hero`, `banner`, `about`, `contact`, `nav`, `footer`, dll.). Jika tidak ada petunjuk, pakai "Section 1", "Section 2", dst.

Contoh struktur umum:

```
<header> logo + nav </header>   → Slide 1: Header (nav ikut di dalamnya)
<main>                           ← pembungkus, bukan slide
  <div class="hero">            → Slide 2: Hero
  <div class="about">           → Slide 3: About
  <div class="contact">         → Slide 4: Contact
</main>
<footer>                         → Slide 5: Footer
```

**Alat koreksi** (di daftar section): ganti nama, gabungkan dua section, pecah section (ketuk elemen → "Jadikan section"), pilih induk/anak, hapus section yang salah deteksi.

Section hanya alat navigasi, jadi deteksi yang meleset **bukan error**. Panduan provider menyarankan pemakaian `<section>` atau `data-section` agar deteksi lebih rapi.

### 7.2 Tampilan slide

```
┌──────────────────────────────┐
│ ✕   Tandai bagian        4/6 │  ← zona atas
│ [Beranda ▾]       [HP][Desk] │
│ Slide 2/5 · hero  ✓ 4 isian  │
│                     (min 3)  │
├──────────────────────────────┤
│ ┌──────────────────────────┐ │  ← zona tengah: slide
│ │ [Toko Kue Bu Ani]  ✓     │ │     (elemen terpilih
│ │ Kue rumahan, ...         │ │      bergaris tebal)
│ │ ┌ ─ foto hero ─ ┐        │ │
│ │ [Pesan sekarang] ✓       │ │
│ └──────────────────────────┘ │
│ main › div.hero › h1         │  ← breadcrumb posisi
│ ✦ Saran: 3 elemen lagi [Tandai semua]
├──────────────────────────────┤
│  ↶     ☰      ▤      ?       │  ← toolbar: undo, daftar elemen,
├──────────────────────────────┤     panel isian, bantuan
│ ▭1✓ ▯2• ▯3  ▭4  ▭5✓          │  ← strip slide (tinggi thumbnail
├──────────────────────────────┤     mengikuti tinggi section)
│ ● 2 perubahan belum disimpan │
│ [ Simpan ]     [ Coba ▶ ]    │
└──────────────────────────────┘
```

| Bagian | Aturan |
|---|---|
| Pemilih halaman | Dropdown di atas (Beranda / Tentang / Kontak, …). Hanya muncul jika template punya lebih dari satu halaman. Setiap halaman punya slide sendiri |
| Area tengah | Menampilkan **satu section saja**; bagian lain disembunyikan/diredupkan |
| Ukuran slide | Lebar = lebar area di HP. **Tinggi = tinggi asli section × skala** (skala = lebar area ÷ lebar halaman). Header pendek → slide pendek, section panjang → slide panjang |
| Slide kecil | Cubit untuk zoom, ketuk dua kali untuk memperbesar |
| Slide lebih tinggi dari layar | Bisa digulir di dalam bingkainya |
| Breadcrumb | Menampilkan posisi elemen terpilih, mis. `main › div.hero › h1` |
| Saran otomatis | Satu baris kecil di bawah slide (bagian 7.10 A4), tidak menutupi slide |
| Toolbar ikon | Undo, daftar elemen, panel semua isian, bantuan. Daftar elemen dan panel isian terbuka sebagai bottom sheet |
| Strip bawah | Thumbnail tiap section dengan status: `✓` ada tandaan, `•` sedang dibuka, kosong = belum. Ketuk atau geser kiri/kanan untuk pindah slide |
| Ukur ulang | Saat ganti tampilan HP ↔ Desktop, ganti halaman, dan saat orientasi layar berubah |
| Header yang menempel | Header `position: fixed/sticky` dibuat tidak menempel **hanya selama mode tandai**, agar tidak menutupi slide lain. File asli tidak diubah |
| Link di dalam website | Tidak berpindah halaman saat diketuk (ketukan dipakai untuk menandai). Pindah halaman lewat pemilih halaman |

Teknis: cukup **satu WebView** per halaman. Tinggi WebView diatur sesuai slide lalu digulir ke posisi section, seperti jendela yang mengintip satu bagian halaman.

### 7.3 Menandai elemen

1. **Ketuk elemen** → elemen disorot (pengganti hover).
2. **Bottom sheet** muncul. Elemen yang sedang diatur tetap terlihat di bagian atas layar:

```
┌──────────────────────────────┐
│ main › div.hero › h1         │
│ [Toko Kue Bu Ani]            │  ← elemen tetap terlihat
├──────────────────────────────┤
│           ───                │
│ Tandai elemen       teks · h1│
│ [Induk ↑]        [Anak ↓]    │
│ Jenis isian                  │
│ (Teks pendek) Paragraf Gambar│
│  Link  Tombol                │
│ Label untuk pengguna         │
│ [ Nama toko               ]  │
│ Petunjuk (opsional)          │
│ [ Tulis nama usahamu      ]  │
│ Maks karakter    Wajib       │
│ [ 30 ]           [ Ya ]      │
│ JUGA BOLEH DIUBAH (gaya)     │
│ ☑ Warna teks  ☐ Warna latar  │
│ ☑ Ukuran huruf  [32]–[48] px │
│ ☐ Sudut                      │
│ 🔗 Hubungkan ke isian lain › │
│ [ Batal ]        [ Simpan ]  │
└──────────────────────────────┘
```

| Isian | Keterangan |
|---|---|
| Jenis | Ditebak otomatis, bisa diubah: **Teks pendek**, **Paragraf** (teks panjang), **Gambar**, **Link**, **Tombol** (teks + link sekaligus). Jenis "Warna" dihapus; warna kini menjadi **gaya** (baris di bawah) |
| Label untuk pengguna | Wajib. Mis. "Judul utama", "Foto produk" |
| Petunjuk isian | Opsional. Teks bantuan untuk pembuat website, mis. "Tulis nama usahamu, maks 3 kata" |
| Batas isian | **Maks karakter** (disarankan otomatis dari panjang teks asli agar layout tidak rusak), **wajib/opsional**, **rasio gambar** (otomatis dari gambar asli, mis. 16:9) |
| Juga boleh diubah (gaya) | Centang gaya yang boleh diubah pembuat website untuk elemen ini: warna teks, warna latar, ukuran huruf (dengan rentang), sudut. V1+: warna hover, jenis huruf. Detail di bagian 7.11 |
| Pilih induk / pilih anak | Memperluas atau mempersempit elemen yang dipilih |
| Hubungkan ke isian yang sudah ada | Untuk konten yang ditulis lebih dari sekali (versi HP & desktop, atau header/footer di beberapa halaman): semuanya jadi satu isian |

3. **Simpan** → elemen diberi tanda ✓ dan labelnya.
4. Ringkasan per section: daftar elemen yang sudah ditandai, bisa diubah atau dihapus. Tombol **"Section selesai"**.

Cara kedua untuk memilih: **daftar elemen** (seperti panel layer di Figma), untuk elemen yang tersembunyi (menu dropdown, popup, isi tab, elemen `x-show` Alpine) atau terlalu kecil untuk diketuk.

Gambar latar dari CSS (`background-image`) dideteksi khusus agar bisa ditandai sebagai jenis Gambar.

Elemen yang dibuat oleh JavaScript/library (tidak ada di HTML asli) **tidak bisa ditandai**. Saat diketuk, muncul pesan: "Elemen ini dibuat oleh JavaScript dan tidak bisa ditandai. Tulis isinya langsung di HTML jika ingin bisa diedit." (cara mengenalinya: bagian 7.8).

### 7.4 Tampilan HP dan Desktop

| Tampilan | Cara kerja |
|---|---|
| **HP** | Lebar asli layar HP; website memakai CSS versi mobile |
| **Desktop** | WebView memakai **lebar virtual 1280px** sehingga `@media` desktop aktif, lalu hasilnya diperkecil agar muat. Selama mode desktop, app boleh mengirim user-agent browser desktop untuk website yang mengecek jenis perangkat lewat JavaScript |

- Halaman tandai **boleh diputar ke landscape**, agar tampilan desktop lebih besar dan mudah diketuk.
- Efek hover tidak bisa dicoba (tidak ada kursor).
- Elemen yang hanya muncul di satu tampilan diberi label `HANYA DESKTOP` / `HANYA HP`.

### 7.5 Konten berbeda antara HP dan desktop

Masalah: banyak website menulis konten dua kali (mis. menu desktop dan menu ☰ HP, atau judul versi HP dan desktop). Jika hanya satu yang ditandai, perubahan pembuat website hanya muncul di satu tampilan.

Solusi:

1. Fitur **"Hubungkan ke isian yang sudah ada"** (bagian 7.3): satu isian mengubah beberapa elemen sekaligus.
2. Label `HANYA DESKTOP` / `HANYA HP` di elemen terkait.
3. **Peringatan** (bukan error) jika ada teks yang mirip dengan isian yang sudah ditandai tetapi belum dihubungkan.

### 7.6 Bagian yang sama di beberapa halaman

HTML statis tidak punya fitur *include*, jadi header dan footer biasanya ditulis ulang di setiap file halaman.

- App **mendeteksi section yang sama persis** di beberapa halaman (struktur dan isi sama).
- Saat provider menandai elemen di section seperti itu, app menawarkan: *"Header ini sama di 4 halaman. Tandai sekali untuk semua halaman?"*
- Jika disetujui, satu isian berisi elemen dari semua halaman tersebut (memakai mekanisme "hubungkan ke isian yang sudah ada"). Pembuat website cukup mengganti sekali.

### 7.7 Pengecekan langsung saat menandai

Supaya tidak ada kejutan di akhir:

- Penghitung di atas: "4 isian ditandai (minimal 3) ✓" (dihitung dari semua halaman)
- Label kosong → isian ditandai merah, tidak bisa disimpan
- Peringatan HP/desktop belum terhubung → muncul di slide terkait
- Tombol **Coba ▶** aktif begitu minimal **1 isian tersimpan**. Syarat **3 isian** berlaku untuk tombol **Lanjut: Kirim** di langkah 5 (bagian 7.12)

### 7.8 Mengenali elemen asli vs buatan JavaScript

Library seperti Alpine.js dan Bootstrap JS mengubah isi halaman setelah tampil. Supaya tandaan selalu menunjuk elemen yang benar di HTML asli:

1. Sebelum halaman ditampilkan di mode tandai, app membuat **salinan** HTML dan memberi **nomor unik** pada setiap elemen di HTML asli, mis. `data-tpl-id="17"`. File asli provider tidak diubah.
2. Saat elemen diketuk, nomornya langsung menunjukkan posisi elemen itu di HTML asli.
3. Elemen **tanpa nomor** pasti dibuat oleh JavaScript, sehingga otomatis ditolak untuk ditandai (bagian 7.3).

Contoh batasan Alpine.js (dijelaskan juga di Panduan provider):

```html
<!-- ✓ BISA ditandai: teksnya tertulis di HTML, Alpine hanya untuk buka-tutup -->
<div x-data="{ open: false }">
  <button @click="open = !open">Menu</button>
  <h1>Toko Kue Bu Ani</h1>
</div>

<!-- ✕ TIDAK BISA ditandai: teksnya ada di dalam JavaScript -->
<div x-data="{ judul: 'Toko Kue Bu Ani' }">
  <h1 x-text="judul"></h1>
</div>
```

### 7.9 Keamanan

- **JavaScript milik provider tetap berjalan** di mode tandai, karena library seperti Tailwind Play CDN, Alpine, dan Bootstrap JS membutuhkannya agar tampilan benar. Keamanan dijaga dengan cara lain:
  - Jembatan JS ↔ Java dibuat **sangat terbatas**: hanya bisa melaporkan "elemen nomor sekian dipilih". Isi pesan divalidasi di Java.
  - WebView **memblokir koneksi** ke domain di luar daftar CDN terpercaya, dan **memblokir perpindahan** ke situs lain.
- ZIP diekstrak ke folder pribadi app dengan pengecekan nama file berbahaya (*zip slip*, mis. `../../`).
- File lokal ditampilkan lewat `WebViewAssetLoader` dari library **`androidx.webkit`** (disetujui Aris, 6 Oktober 2026), bukan lewat akses `file://` langsung.

---

### 7.10 Daftar sub-fitur editor dan prioritas

**V1** = wajib di versi awal · **V1+** = disarankan, dibuat jika sempat · **Nanti** = versi berikutnya.

**A1. Navigasi**

| Sub-fitur | Fungsi | Prioritas |
|---|---|---|
| Pemilih halaman | Pindah antar halaman | V1 |
| Strip slide | Pindah antar section + status ✓ | V1 |
| Daftar section + koreksi | Ganti nama, gabung, pecah, hapus section | V1 |
| Toggle HP / Desktop + landscape | Melihat dua tampilan | V1 |
| Zoom (cubit, ketuk dua kali) | Elemen kecil mudah diketuk | V1 |

**A2. Memilih elemen**

| Sub-fitur | Fungsi | Prioritas |
|---|---|---|
| Ketuk untuk menyorot | Pengganti hover | V1 |
| Pilih induk / anak | Memperluas atau mempersempit pilihan | V1 |
| Breadcrumb posisi | Lokasi elemen, mis. `header › nav › li › a` | V1 |
| Daftar elemen (seperti layer Figma) | Untuk elemen tersembunyi atau terlalu kecil | V1 |
| Cari elemen berdasarkan teks | Ketik "Hubungi kami" → lompat ke elemennya | V1+ |

**A3. Menandai (isi bottom sheet)**

| Sub-fitur | Fungsi | Prioritas |
|---|---|---|
| Jenis isian | Teks pendek, paragraf, gambar, link, tombol | V1 |
| Gaya per elemen | Warna teks, warna latar, sudut, ukuran huruf dalam rentang (bagian 7.11) | V1 |
| Gaya per elemen lanjutan | Warna hover, jenis huruf (bagian 7.11) | V1+ |
| Label untuk pengguna | Nama isian di form pembuat website | V1 |
| Petunjuk isian | Teks bantuan untuk pembuat website | V1 |
| Batas isian | Maks karakter, wajib/opsional, rasio gambar | V1 |
| Hubungkan ke isian yang sudah ada | HP/desktop dan lintas halaman | V1 |

**A4. Penghemat waktu**

| Sub-fitur | Fungsi | Prioritas |
|---|---|---|
| Saran tandai otomatis | Per slide, app menyarankan elemen yang biasanya diedit (`h1`–`h3`, paragraf, gambar, tombol) dengan label tebakan ("Judul", "Deskripsi", "Gambar 1"). **[Tandai semua]** atau pilih satu per satu | V1 |
| Deteksi bagian yang sama di beberapa halaman | "Header ini sama di 4 halaman, tandai sekali?" (bagian 7.6) | V1 |
| Tema global | Jika CSS punya variabel seperti `:root { --primary: #2563eb; --radius: 8px }`, app menawarkan "Jadikan *Warna utama* bisa diganti?" Satu pengaturan mengubah seluruh website (bagian 7.11) | V1 |

**A5. Mengelola tandaan**

| Sub-fitur | Fungsi | Prioritas |
|---|---|---|
| Panel semua isian | Daftar isian dari semua halaman, filter per halaman/section; ketuk → lompat ke elemennya | V1 |
| Undo / redo | Membatalkan tandaan yang salah | V1 |
| Penghitung + pengecekan langsung | Bagian 7.7 | V1 |
| Tombol Simpan + cadangan otomatis di HP | Simpan ke draft di server; perubahan yang belum disimpan dicadangkan di HP (bagian 7.12) | V1 |
| Bolak-balik Tandai ⇄ Coba | Kerja bertahap (bagian 7.12) | V1 |
| Urutkan isian | Menentukan urutan tampil di form pembuat website | V1+ |

**A6. Bantuan**

| Sub-fitur | Fungsi | Prioritas |
|---|---|---|
| Tur singkat pertama kali | 3–4 sorotan saat pertama kali membuka editor: "Ketuk elemen untuk menandai", "Geser untuk pindah section", dst. | V1 |
| Tautan Panduan | Dari setiap langkah | V1 |

Tiga sub-fitur yang paling berpengaruh ke kenyamanan provider: **saran tandai otomatis**, **batas isian**, dan **uji isi panjang** (bagian 8).

---

### 7.11 Gaya yang bisa diubah (tanpa mengubah tata letak)

**Prinsip:** pembuat website boleh mengubah **gaya** elemen yang diizinkan provider, tetapi **tata letak tetap terkunci**.

**Cara kerja:** file CSS provider **tidak diubah**. App membuat satu lapisan CSS tambahan (`custom.css`) dari pilihan pembuat website, dimuat **paling akhir** agar mengalahkan CSS provider:

```css
/* custom.css: dibuat otomatis */
[data-key="hero_img"]        { border-radius: 24px !important; }
[data-key="judul_hero"]      { color: #1e3a8a !important; font-size: 40px !important; }
[data-key="header"]          { background-color: #fef3c7 !important; }
[data-key="menu_link"]:hover { color: #dc2626 !important; }
:root                        { --primary: #16a34a; }   /* tema global */
```

- `[data-key]` menunjuk elemen yang ditandai; `!important` memastikan pilihan pembuat website menang walaupun CSS provider sangat spesifik.
- Hover bisa diatur karena ini CSS sungguhan (`:hover`).
- **Reset** cukup dengan menghapus aturan di `custom.css`.

| Boleh diubah (gaya) | Tetap terkunci (tata letak) |
|---|---|
| Warna teks | Posisi dan urutan elemen |
| Warna latar (header, section, tombol) | Lebar, tinggi, margin, padding |
| Warna saat hover (V1+) | Jumlah kolom, grid/flex |
| Sudut (`border-radius`) | `display`, `position` |
| Ukuran huruf (dalam rentang) | Animasi dan efek |
| Jenis huruf dari daftar (V1+) | Struktur HTML |

**Dua cara menyediakan gaya** (bisa dipakai bersamaan):

| Cara | Isi | Kapan dipakai |
|---|---|---|
| **A. Gaya per elemen** | Di bottom sheet Tandai, provider mencentang gaya yang boleh diubah untuk elemen itu (bagian 7.3) | Untuk elemen tertentu, mis. warna latar header saja |
| **B. Tema global** | Jika CSS provider memakai variabel di `:root` (`--primary`, `--radius`, `--font-heading`, dll.), app menawarkan menjadikannya **pengaturan tema**. Provider memberi label, mis. "Warna utama" | **Diutamakan** jika tersedia: satu pengaturan mengubah seluruh website secara konsisten, paling mudah untuk pembuat website non-teknis |

**Pengaman agar website tidak rusak:**

| Gaya | Pengaman |
|---|---|
| Ukuran huruf | Hanya dalam rentang yang ditentukan provider (bawaan ±20% dari ukuran asli) |
| Sudut | Slider 0–32 px |
| Jenis huruf (V1+) | Hanya dari **daftar font pilihan** berlisensi bebas yang disimpan di app (bisa offline dan ikut di ZIP export) |
| Warna | Bebas, dengan **peringatan kontras** jika teks dan latar terlalu mirip |
| Hover (V1+) | Preview punya tombol **"Tampilkan keadaan hover"** karena HP tidak punya kursor |

**Prioritas:**

| Gaya | Prioritas | Alasan |
|---|---|---|
| Warna teks, warna latar | V1 | Paling sering diminta, mudah |
| Sudut (radius) | V1 | Mudah, aman |
| Ukuran huruf dengan rentang | V1 | Aman karena dibatasi |
| Tema global (variabel CSS) | V1 | Paling bermanfaat untuk pembuat website non-teknis |
| Warna hover | V1+ | Butuh mode preview hover |
| Jenis huruf | V1+ | Butuh daftar font dan penyimpanan file font |

---

### 7.12 Tandai ⇄ Coba bolak-balik (kerja bertahap)

Langkah 4 (Tandai) dan langkah 5 (Coba) **bukan jalan satu arah**. Provider bisa bekerja bertahap:

```
Tandai beberapa elemen → SIMPAN → Coba (cek hasil) → Kembali ke Tandai → tandai lagi → SIMPAN → Coba lagi → ... → Kirim
```

**Di layar Tandai:**

```
├──────────────────────────────┤
│ ● 2 perubahan belum disimpan │
│ [ Simpan ]     [ Coba ▶ ]    │
└──────────────────────────────┘
```

| Aturan | Isi |
|---|---|
| Penanda perubahan | Setiap tambah/ubah/hapus tandaan memunculkan **"● X perubahan belum disimpan"** |
| Tombol **Simpan** | Mengecek tandaan (label tidak kosong, `data-key` tidak ganda, dll.) lalu menyimpannya ke draft di server. Tandaan yang tersimpan menjadi **bisa dicoba** |
| Tombol **Coba ▶** | Aktif jika minimal **1 isian sudah tersimpan** |
| Coba dengan perubahan belum disimpan | Dialog: *"Ada 2 perubahan belum disimpan. Simpan dulu agar bisa dicoba."* → **[Simpan & coba]** **[Batal]** |
| Keluar wizard (✕) dengan perubahan belum disimpan | Dialog: **[Simpan & keluar]** **[Keluar tanpa simpan]** **[Batal]** |
| Cadangan otomatis di HP | Perubahan yang belum disimpan dicadangkan diam-diam di HP. Jika app tertutup atau HP mati, saat dibuka lagi: *"Ada perubahan yang belum disimpan. Pulihkan?"* |

**Di layar Coba:**

```
┌──────────────────────────────┐
│ ◀ Tandai   Coba sebagai  5/6 │
│            pengguna          │
│ (preview + form)             │
├──────────────────────────────┤
│ [ ◀ Lanjut menandai ]        │
│ [ Lanjut: Kirim ▶ ]          │
└──────────────────────────────┘
```

| Aturan | Isi |
|---|---|
| Isi form | Hanya isian yang **sudah disimpan** |
| **◀ Lanjut menandai** | Kembali ke Tandai, tepat di halaman dan slide terakhir yang dikerjakan |
| **Ubah tandaan ini** | Di setiap isian; langsung melompat ke elemennya di Tandai (naik dari V1+ menjadi V1) |
| Isian percobaan | Teks/foto/gaya yang dicoba **tetap ada** selama bolak-balik; tombol **Isi asli** untuk reset |
| **Lanjut: Kirim ▶** | Aktif jika minimal **3 isian** tersimpan dan tidak ada perubahan yang belum disimpan |

---

## 8. Langkah 5: Coba sebagai pengguna

```
┌──────────────────────────────┐
│ ←  Coba sebagai pengguna 5/6 │  ← zona atas
│ [Beranda ▾]       [HP][Desk] │
├──────────────────────────────┤
│ ┌──────────────────────────┐ │  ← zona tengah: preview
│ │ Dapur Mama Rina          │ │     berubah langsung
│ │ Kue rumahan, ...         │ │     saat form diisi
│ │ ┌ ─ ─ ─ ─ ─ ─ ┐          │ │
│ └──────────────────────────┘ │
│ [Uji isi panjang] [Isi asli] │
├──────────────────────────────┤
│           ───                │  ← zona bawah: form
│ (Header)(Hero)(About)(Kontak)│     (bottom sheet)
│ Nama toko                    │
│ [ Dapur Mama Rina      ]15/30│
│ Deskripsi                    │
│ [ Kue rumahan, ...     ]     │
│ Foto hero · 16:9             │
│ [ ⤒ Ganti foto ]             │
│ [◀ Lanjut menandai]          │
│ [ Lanjut: Kirim ▶ ]          │
└──────────────────────────────┘
```

- Menampilkan **form edit yang akan dilihat pembuat website**, disusun per halaman dan per section dengan isian dari tandaan provider. Chip section (Header, Hero, About, …) menggantikan strip slide.
- **Preview langsung**: mengetik di form → preview langsung berubah. Form berupa bottom sheet yang bisa ditarik naik/turun.
- Isian menampilkan label, petunjuk, penghitung karakter (mis. `15/30`), dan rasio gambar sesuai pengaturan di langkah 4.
- Kontrol **gaya** ikut tampil: pemilih warna, slider ukuran huruf dan sudut, serta bagian **Tema** di paling atas form jika tema global tersedia (bagian 7.11).
- Toggle **HP / Desktop** dan pemilih halaman tersedia; link antar-halaman di preview berfungsi normal.
- Perubahan di langkah ini **hanya percobaan** dan tidak mengubah isi contoh template.
- Bisa bolak-balik dengan langkah 4; form hanya berisi isian yang sudah disimpan (bagian 7.12).

| Sub-fitur | Fungsi | Prioritas |
|---|---|---|
| Form per section | Sama persis dengan editor template mode di sisi pembuat website | V1 |
| Preview langsung | Preview di atas, form di bawah | V1 |
| Toggle HP / Desktop + navigasi antar halaman | Memastikan semua tampilan dan halaman ikut berubah | V1 |
| Kembalikan ke isi asli | Reset semua percobaan | V1 |
| Uji isi panjang | Satu tombol mengisi semua isian dengan teks maksimal dan gambar rasio berbeda, untuk melihat apakah layout rusak | V1+ |
| Lompat balik ke Tandai ("Ubah tandaan ini") | Dari isian yang kurang pas, satu ketukan kembali ke elemennya di langkah 4 | V1 |

Bentuk form dan preview ini sama dengan **editor template mode** di sisi pembuat website (`alurFiturPembuatanWibesite.md`), sehingga cukup dibangun satu kali.

---

## 9. Langkah 6: Kirim

1. Ringkasan: nama, kategori, jumlah halaman, jumlah section, jumlah isian, jumlah peringatan.
   - **Konfirmasi hak pakai aset** (wajib dicentang): "Saya berhak memakai semua gambar, font, dan isi dalam template ini."
   - **Preview kartu galeri**: contoh tampilan template di galeri (thumbnail, nama, kategori).
   - **Daftar peringatan** yang tersisa, masing-masing dengan tautan untuk memperbaikinya.
2. Tombol **Kirim** → status `checking`.
3. Server menjalankan **pengecekan akhir**: aturan tandaan + aturan file sekali lagi. Karena file sudah lolos di langkah 2 dan tandaan sudah dicek langsung di langkah 4, pengecekan ini **hampir pasti lolos**.
4. Server **menyalin library dari CDN** ke dalam paket template (bagian 10.5).
5. Server menyisipkan atribut ke HTML asli: `data-edit`, `data-key`, `data-label`, dan `data-section`.
6. Lolos → `published`, tayang di galeri. Ada peringatan → tetap tayang, peringatan masuk "Perlu tindakan".

---

## 10. Struktur project dan library yang didukung

### 10.1 Banyak file CSS dan JS

Struktur seperti ini langsung didukung:

```
toko-kue.zip
├── index.html
├── tentang.html
├── kontak.html
├── css/
│   ├── reset.css
│   ├── layout.css
│   └── components.css
├── js/
│   ├── navbar.js
│   ├── slider.js
│   └── main.js
├── vendor/
│   └── alpine.min.js        ← library boleh disertakan di ZIP
└── assets/
    ├── img/
    └── fonts/
```

- Folder dan subfolder bebas, selama masih dalam batas ukuran (bagian 5.6 A).
- `@import` antar file CSS lokal boleh.
- JS modul (`<script type="module">`) dengan `import` antar file lokal boleh.

### 10.2 Multi-halaman

- `index.html` wajib sebagai halaman utama; maksimal **10 halaman**.
- Halaman saling terhubung lewat link biasa (`<a href="tentang.html">`).
- Di langkah 4 ada **pemilih halaman**; setiap halaman punya slide sendiri (bagian 7.2).
- Bagian yang sama di beberapa halaman (header/footer) bisa ditandai sekali untuk semua halaman (bagian 7.6).
- Pengecekan tambahan: link ke halaman yang tidak ada (E), halaman yang tidak ditautkan (P), lebih dari 10 halaman (E).

### 10.3 HTML potongan (partials) dan alat build

Provider boleh menulis kode dengan potongan HTML (partials), komponen, Sass, dll., **asal memakai alat build di laptopnya sendiri** dan mengupload **hasil build**-nya.

```
Laptop provider                                   App
───────────────                                   ───
src/
├── index.html      (memakai include header)
├── partials/
│   ├── header.html          npm run build
│   └── footer.html        ──────────────→  dist/ → di-ZIP → Upload
└── vite.config.js                          ├── index.html  ← header & footer sudah
                                            └── assets/       tergabung utuh
```

| Aturan | Keterangan |
|---|---|
| Siapa yang build | **Provider sendiri** di laptopnya. Server **tidak** menjalankan `npm install` / build, karena itu berarti menjalankan kode sembarangan dari provider di server (risiko keamanan) dan berat |
| Yang diupload | Isi folder hasil build (mis. `dist/`) |
| Salah upload kode sumber | Terdeteksi dan ditolak dengan pesan cara memperbaikinya (bagian 5.6 A) |
| Nama file ber-hash (`assets/index-a1b2c3.js`) | Boleh, normal untuk hasil build |
| Path diawali `/` | Peringatan + saran `base: './'` (bagian 5.6 D) |
| Hasil build SPA biasa (React/Vue/Svelte) | Error, karena isi halaman dibuat JavaScript (bagian 5.6 C) |
| Hasil build yang menghasilkan HTML statis (Vite + plugin include HTML, Eleventy, Astro) | Boleh |
| Partials dimuat lewat `fetch()` **tanpa** build | Isinya tidak tertulis di HTML asli → tidak bisa ditandai (P), atau error jika konten utama seluruhnya dimuat dengan cara ini (E) |

**Panduan provider** menjelaskan: cara memakai partials dengan Vite (plugin include HTML) atau Eleventy, contoh `vite.config.js` dengan `base: './'`, dan cara men-ZIP folder `dist/`.

### 10.4 Library luar

| Cara | Aturan |
|---|---|
| **File disertakan di ZIP** | Boleh untuk library apa pun. Library terkenal dikenali dari hash-nya dan tidak dipindai ulang; file lain dipindai dengan aturan bagian 5.6 F |
| **Dari CDN** | Boleh jika: (1) dari **CDN terpercaya**, (2) library ada di **daftar yang diizinkan**, (3) **versi ditulis jelas** (mis. `@3.14.1`, bukan `@latest`) |

**CDN terpercaya (awal):** jsDelivr, cdnjs, unpkg, `cdn.tailwindcss.com`, `code.jquery.com`, Google Fonts.

**Daftar library yang diizinkan (awal):**

| Jenis | Library |
|---|---|
| CSS | Tailwind, Bootstrap, Bulma |
| JS | Alpine.js, jQuery, Bootstrap JS, AOS, Swiper, Splide, GSAP |
| Ikon & font | Font Awesome, Bootstrap Icons, Lucide, Remix Icon, Google Fonts |

Alasan memakai daftar library (bukan semua isi CDN): siapa pun bisa menerbitkan paket di npm, termasuk paket jahat, dan jsDelivr/unpkg akan menyajikannya. Yang dipercaya adalah **library-nya**, bukan sekadar CDN-nya. Daftar ini disimpan di pengaturan server dan bisa ditambah tanpa update app.

### 10.5 Penyalinan library dari CDN ke paket template

Saat template dikirim (langkah 6), server:

1. Membaca HTML dan menemukan link library dari CDN.
2. **Mengunduh** file library itu satu kali.
3. **Menyimpannya** di dalam paket template, mis. `vendor/bootstrap@5.3.3/bootstrap.min.css`.
4. **Mengganti** link CDN di HTML paket template menjadi link lokal.

```html
<!-- di file provider -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">

<!-- di paket template yang dipajang -->
<link rel="stylesheet" href="vendor/bootstrap@5.3.3/bootstrap.min.css">
```

| Untuk siapa | Manfaat |
|---|---|
| Pembuat website | Preview di editor tetap rapi saat **offline**; ZIP hasil export **utuh** dan tetap jalan walaupun CDN mati |
| Keamanan | File yang dicek saat upload sama persis dengan yang dipakai; tidak bisa berubah diam-diam |
| Provider | Tidak perlu melakukan apa pun; boleh tetap menulis link CDN seperti biasa. File asli provider tidak diubah |

- Konsekuensi: ukuran paket template sedikit bertambah (mis. Bootstrap CSS + JS ± 300 KB, Alpine ± 45 KB).
- **Pengecualian versi awal:** Google Fonts tetap dimuat dari internet (satu link memuat banyak file font). Saat offline, tampilan jatuh ke font bawaan HP tanpa merusak layout.

---

### 10.6 Framework komponen (React, Vue, Svelte) — belum didukung

| Cara build | Hasil |
|---|---|
| React/Vue/Svelte biasa (SPA, mis. `vite build` React) | `index.html` hanya berisi `<div id="root"></div>`; semua isi dibuat JS → ditolak oleh aturan "konten utama dibuat JavaScript" |
| React/Vue/Svelte dengan *prerender* + hidrasi (Next.js `output: 'export'`, Gatsby, Nuxt `generate`) | HTML berisi konten, tetapi saat dibuka framework melakukan **hidrasi** dan bisa **menimpa kembali teks yang sudah diedit** pembuat website. Berbahaya karena baru ketahuan setelah website online |
| Astro dengan island React | Bagian statis aman, tetapi island tetap dikendalikan React |

Keputusan versi awal: **semua framework komponen belum didukung** agar alur tetap sederhana. Server mendeteksi jejaknya (bagian 5.6 C) dan menolak dengan pesan yang jelas. Validasi "uji tahan edit" (mengisi teks uji lalu mengecek apakah tertimpa) **tidak dipakai** di versi awal.

Cara menulis yang didukung:

| Cara menulis | Perlu build? | Didukung |
|---|---|---|
| HTML, CSS, JS ditulis langsung (paling umum) | Tidak; folder langsung di-ZIP | ✓ |
| HTML/CSS/JS + alat build untuk kerapian (partials, Sass, minify) | Ya; upload isi `dist/` | ✓ (hasil build harus HTML statis) |
| Framework komponen | – | ✗ belum |

## 11. Gambaran data tandaan (sketsa)

Data yang dikirim app ke server bersama draft:

```json
{
  "pages": [
    { "file": "index.html",   "name": "Beranda" },
    { "file": "tentang.html", "name": "Tentang" }
  ],
  "sections": [
    { "id": "s1", "page": "index.html", "name": "Header", "tplId": 3 },
    { "id": "s2", "page": "index.html", "name": "Hero",   "tplId": 21 }
  ],
  "fields": [
    {
      "key": "hero_title",
      "label": "Judul utama",
      "type": "text",
      "hint": "Tulis nama usahamu, maks 3 kata",
      "maxLength": 30,
      "required": true,
      "order": 1,
      "styles": [
        { "prop": "color" },
        { "prop": "font-size", "min": 32, "max": 48, "unit": "px" }
      ],
      "sectionId": "s2",
      "elements": [
        { "page": "index.html", "tplId": 24, "visibleIn": ["desktop"] },
        { "page": "index.html", "tplId": 31, "visibleIn": ["mobile"] }
      ]
    },
    {
      "key": "nama_toko",
      "label": "Nama toko",
      "type": "text",
      "sectionId": "s1",
      "elements": [
        { "page": "index.html",   "tplId": 5, "visibleIn": ["mobile", "desktop"] },
        { "page": "tentang.html", "tplId": 5, "visibleIn": ["mobile", "desktop"] }
      ]
    }
  ]
}
```

- `styles` berisi gaya yang boleh diubah untuk isian itu (bagian 7.11). Tema global disimpan terpisah, mis. `"theme": [{ "var": "--primary", "label": "Warna utama", "type": "color" }]`.
- `hint`, `maxLength`, `required`, dan `order` berasal dari bottom sheet tandai (bagian 7.3) dan fitur urutkan isian (bagian 7.10). Untuk jenis gambar ada `aspectRatio` (mis. `"16:9"`).
- `tplId` adalah nomor unik elemen di **HTML asli** (bagian 7.8), sehingga server bisa menemukan elemen yang sama walaupun library mengubah halaman saat tampil.
- Satu `field` bisa berisi beberapa `elements`, termasuk lintas halaman → wujud fitur "hubungkan ke isian yang sudah ada".
- Format final ditetapkan bersama format project JSON di sisi pembuat website.

---

## 12. Hubungan dengan fitur lain

| Fitur | Hubungan |
|---|---|
| `alurUntukProvider.md` bagian 4 | Status template, error vs peringatan, notifikasi, pengingat peringatan |
| `alurUntukProvider.md` bagian 5.2 & 5.4 | Cara memasukkan file (ZIP saja) dan aturan draft |
| `alurUntukProvider.md` bagian 5.3 | **Digantikan** oleh dokumen ini (ZIP diupload dan dicek di awal; tampilan slide; JS provider tetap berjalan dengan jembatan terbatas) |
| Editor template mode (`alurFiturPembuatanWibesite.md`) | Memakai struktur halaman, slide, dan isian yang sama dengan langkah 5. Karena template boleh multi-halaman, editor template mode harus bisa berpindah halaman, dan export ZIP berisi semua halaman |
| Kerangka buatan provider | Rencana ke depan; kemungkinan memakai alur upload serupa |

**Dampak ke dokumen lain (belum disinkronkan; disesuaikan saat dokumen itu dikerjakan):**

| Dokumen | Yang perlu disesuaikan |
|---|---|
| `alurFiturPembuatanWibesite.md` bagian 6.2 | Pencarian galeri mencari di **nama, deskripsi, dan kata kunci** (bukan nama saja); kartu/detail template bisa menampilkan info teknis |
| `alurFiturPembuatanWibesite.md` (editor template mode) | Editor punya kontrol **gaya** (pemilih warna, slider ukuran huruf/sudut, Tema) dan menghasilkan `custom.css`; export ZIP menyertakan `custom.css` |
| `alurUntukProvider.md` bagian 6.6 | Tabel `templates` ditambah kolom `deskripsi`, `kata_kunci` (text[]), `info_teknis` (jsonb) |

---

## 13. Keputusan yang masih terbuka

Usulan perbaikan proses validasi yang belum diputuskan Aris:

| No | Usulan | Keterangan |
|---|---|---|
| 1 | **Perbaikan otomatis** (versi berikutnya) | Mis. menyamakan huruf besar/kecil nama file atau menghapus `<base href>`, atas persetujuan provider |

Sudah diterapkan di dokumen ini: laporkan semua masalah sekaligus (bagian 5.6), cek "konten utama dibuat JS" secara statis di server (bagian 5.1 dan 5.6 C), tombol laporkan pengecekan keliru (bagian 5.9), ZIP uji per aturan (bagian 5.10), pesan error menautkan ke Panduan (bagian 5.11), dan aturan "halaman hampir tanpa elemen" hanya untuk kasus ekstrem (bagian 5.6 C).

---

## 14. Yang perlu dibahas berikutnya

> **Belum didiskusikan secara matang.** Jangan membangun apa pun dari daftar di bawah ini.

- **Versi template**: cara provider memperbarui template yang sudah tayang, dan nasib project pembuat website yang memakai versi lama
- Penandaan **tingkat section** (pembuat website bisa menyembunyikan/mengurutkan section)
- Detail visual lanjutan (ukuran, animasi, ikon final) disesuaikan saat implementasi berdasarkan token desain AGENTS.md bagian 9
- Render halaman di server (Chromium headless, mis. lewat Playwright) agar hasil pengecekan render konsisten
- `import` library dari CDN di dalam file JS modul
- Proses pengajuan library baru ke daftar yang diizinkan
