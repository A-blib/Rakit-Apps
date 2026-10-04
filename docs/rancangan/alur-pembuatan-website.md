# Alur Fitur Pembuatan Website

> **Status:** Beranda, Project Anda, Profil (versi awal), dua halaman editor "Segera hadir", dan **galeri Template versi awal (data dummy, bagian 6.2)** sudah cukup jelas untuk dijadikan instruksi agent berikutnya. **Editor (template mode & custom mode), kerangka section, dan galeri Template lanjutan belum dibahas**. Ketiganya akan didesain mendalam bersamaan dengan fitur Upload di sisi provider. Dokumen ini belum untuk dieksekusi agent sampai Aris memintanya.
>
> Terakhir diperbarui: 4 Oktober 2026 (galeri Template versi awal dengan data dummy, bagian 6.2)

---

## 1. Keputusan yang sudah final

| Keputusan | Isi |
|---|---|
| Dua cara membuat website | **Template mode** (mulai dari template jadi) dan **Custom mode** (menyusun sendiri dari kerangka yang disediakan). |
| Satu mesin, dua tampilan | Tools editor template mode dan custom mode **berbeda desainnya**, tetapi keduanya **berjalan di mesin yang sama**: format project JSON sama, renderer (preview) sama, dan exporter (ZIP) sama. |
| Kerangka (skeleton) | Kerangka disediakan **per jenis section**, misalnya kerangka header, kerangka hero, dan kerangka footer. User custom mode menyusun website dari kerangka-kerangka ini. Detail dibahas bersama editor. |
| Sumber kerangka | **Versi awal: kerangka disediakan langsung oleh app** (dibuat Aris, ikut terpasang di dalam app sehingga bisa dipakai offline). **Ke depan:** provider juga bisa membuat kerangka untuk dipajang siap pakai di custom mode. Fitur ini **tidak dibangun sekarang** (lihat bagian 9). |
| Format kerangka | Kerangka bawaan app dan kerangka buatan provider nanti memakai **format yang sama**, supaya saat fitur provider ditambahkan, mesin custom mode tidak perlu diubah. Format detailnya dibahas bersama editor. |
| Tombol buat project | **Bukan halaman**, melainkan tombol **+** di tengah bottom nav yang **lebih menonjol** dari menu lain. |
| Buat via template | User cukup **klik template** (dari tab Template atau "Template untuk anda" di Beranda), lalu app **otomatis membuka editor khusus template mode**. Tidak ada langkah tambahan. Detail di bagian 2.1. |
| Buka project yang sudah ada | Dari mana pun project dibuka (Lanjutkan project di Beranda atau tab Project), user cukup **klik kartu project**, lalu app **langsung** membuka **editor sesuai mode project itu** tanpa langkah tambahan. Aturan ini berlaku sama untuk kedua mode: project template → editor template mode, project custom → editor custom mode. |
| Editor versi awal | Dua halaman editor (template mode dan custom mode) **dibuat sebagai layar terpisah** yang menampilkan pesan "Segera hadir" karena masih tahap perencanaan dan desain. Klik project atau template sudah benar-benar membuka halaman editor yang sesuai (bagian 6.1). |
| Status project | Tiga tahap: **Draft** → **Siap export** → **Sudah diexport**, dihitung otomatis oleh app setiap kali project disimpan (bagian 4.2). |
| Status setelah diedit ulang | Project yang sudah diexport lalu diedit tetap `exported` + teks kecil "Ada perubahan sejak export terakhir" (bagian 4.2). |
| Daftar project | **List satu kolom** (bagian 4.1 dan 4.5). |
| Jumlah project | **Tidak dibatasi**, selama user masih mau membuat. |
| Penyimpanan | **Autosave** setiap kali user melakukan perubahan (bagian 4.7). |
| Nama awal project | Template: **mengikuti nama template**. Custom: **"Project tanpa nama"** (bagian 4.7). |
| Data contoh untuk testing | Tombol debug **"Isi project contoh"** dan **"Hapus project contoh"**, hanya di build debug (bagian 6). |
| Panduan di Beranda | Section **Panduan** ada di Beranda, berisi artikel singkat statis yang tersimpan di app dan bisa dibaca offline. Minimal ada panduan mengonlinekan file ZIP (bagian 3.4). |
| Galeri Template versi awal | Tab **Template** dan section **"Template untuk anda"** di Beranda **dibangun sekarang** dengan **data dummy** untuk kebutuhan testing (bagian 6.2). Klik template tetap membuka Editor Template Mode "Segera hadir". **Editor belum dibangun.** |
| Fokus diskusi sekarang | Beranda, Project Anda, Profil, dan galeri Template versi awal. Editor dan galeri Template lanjutan ditunda (bagian 9). |

---

## 2. Segmen Dashboard Pembuat Website

Bottom navigation dengan 4 menu + 1 tombol aksi di tengah:

```
Beranda · Project · [ + ] · Template · Profil
```

| Segmen | Fungsi singkat | Status diskusi |
|---|---|---|
| **Beranda** | Titik awal: mulai bikin, lanjutkan project, rekomendasi template, panduan | Siap untuk versi awal (bagian 3) |
| **Project** | Daftar semua project milik user + filter + search | Siap untuk versi awal (bagian 4) |
| **[ + ] Buat project** | Tombol menonjol, membuka bottom sheet pilihan **Pakai template** / **Custom** | Tombol dan alurnya final. Halaman editornya masih "Segera hadir" (bagian 2.1 dan 6.1) |
| **Template** | Galeri template lengkap dengan search | Versi awal dengan data dummy (bagian 6.2). Versi lanjutan ditunda (bagian 9) |
| **Profil** | Data diri, info onboarding, pengaturan, beralih mode | Siap untuk versi awal (bagian 5) |

**Gaya tombol +:** kotak berisi `color_foreground` (hitam di mode terang, putih di mode gelap), radius 6–8dp, ikon `add` Outlined, sedikit lebih besar dan sedikit naik dari baris bottom nav. Tidak bulat warna-warni dan tanpa bayangan tebal, supaya tetap sesuai gaya monokrom (AGENTS.md bagian 9).

### 2.1 Alur masuk ke editor

```
Tombol + / kartu Mulai
├─ Pakai template ──→ tab Template ──→ klik template ──→ EDITOR TEMPLATE MODE (project baru)
└─ Custom ─────────────────────────────────────────→ EDITOR CUSTOM MODE (project baru)

Klik template di tab Template / "Template untuk anda" ──→ EDITOR TEMPLATE MODE (project baru)

Buka project (Lanjutkan project di Beranda / tab Project)
└─ cek kolom `mode` project
   ├─ template ──→ EDITOR TEMPLATE MODE
   └─ custom   ──→ EDITOR CUSTOM MODE
```

- Klik kartu project (di Beranda maupun tab Project) **langsung** membuka editor, baik untuk project template maupun project custom. Menu ⋮ di kartu hanya untuk aksi lain (ganti nama, duplikat, export, hapus).
- Kolom `mode` di tabel `projects` (bagian 4.6) menjadi penentu editor mana yang dibuka. Karena kedua editor memakai mesin yang sama, yang berbeda hanya layar tools-nya.
- Saat template diklik, isi template **disalin** menjadi project baru. Template aslinya tidak pernah berubah, sehingga satu template bisa dipakai berkali-kali untuk project berbeda.
- Kapan project baru tersimpan dan bagaimana nama awalnya: lihat bagian 4.7.

Prinsip: Beranda hanya berisi **titik masuk** dan **ringkasan**. Pengelolaan project (ganti nama, hapus, filter) ada di tab Project, supaya keduanya tidak tumpang tindih. Prinsip ini sama dengan Dashboard Provider.

---

## 3. Beranda

### 3.1 Isi Beranda (urut dari atas ke bawah)

| No | Bagian | Isi | Status |
|---|---|---|---|
| 1 | **Mulai** | Dua kartu pilihan: **Pakai template** dan **Custom**. Tampil besar jika user belum punya project, lalu mengecil menjadi satu baris jika sudah punya. | Dari alur Aris |
| 2 | **Lanjutkan project** | 1 kartu besar (project terakhir diedit) + maksimal 3 kartu kecil yang bisa digeser ke samping, serta link "Lihat semua →" ke tab Project. Disembunyikan jika belum ada project. | Dari alur Aris |
| 3 | **Template untuk anda** | Rekomendasi template yang bisa digeser ke samping, berdasarkan **tujuan website** dari onboarding (`website_purpose`). Tamu atau user yang melewati onboarding mendapat template populer. "Lihat semua →" mengarah ke tab Template. | Versi awal dengan data dummy (bagian 6.2) |
| 4 | **Panduan** | Artikel singkat yang tersimpan di dalam app (bisa dibaca offline). | Final (bagian 3.4) |

### 3.2 Beranda menyesuaikan kondisi user

| Kondisi | Yang tampil |
|---|---|
| Belum punya project (tamu atau user login) | **Mulai** tampil besar sebagai kartu utama → Template untuk anda → Panduan |
| Sudah punya project | **Lanjutkan project** paling atas → Mulai (satu baris) → Template untuk anda → Panduan |
| Tamu | App bar menampilkan tombol **Masuk** |
| User login | App bar menampilkan avatar (mengarah ke tab Profil) |

### 3.3 Gambaran layar

```
┌──────────────────────────────────┐
│ Nama app             [Masuk] / ○ │
├──────────────────────────────────┤
│ LANJUTKAN PROJECT                │
│ ┌──────────────────────────────┐ │
│ │ [thumbnail]                  │ │
│ │ Toko Kue Bu Ani              │ │
│ │ DRAFT · diedit 2 jam lalu    │ │
│ │ 3 isian belum diisi          │ │
│ └──────────────────────────────┘ │
│ [kecil] [kecil] [kecil]  →       │
│                     Lihat semua →│
│                                  │
│ MULAI                            │
│ [Pakai template]  [Custom]       │
│                                  │
│ TEMPLATE UNTUK ANDA              │
│ [ ] [ ] [ ]  →     Lihat semua → │
│                                  │
│ PANDUAN                          │
│ Cara mengonlinekan file ZIP    › │
│ Beda template mode & custom    › │
├──────────────────────────────────┤
│ Beranda  Project [+] Template  Profil │
└──────────────────────────────────┘
```

### 3.4 Panduan

Alasan: hasil akhir app berupa **file ZIP**. User non-teknis belum tentu tahu apa yang harus dilakukan dengan file itu, jadi panduan mengonlinekan website sangat membantu.

| Judul | Isi singkat |
|---|---|
| Cara mengonlinekan file ZIP | Langkah gratis lewat layanan hosting statis (misalnya Netlify Drop atau GitHub Pages), dengan bahasa sederhana |
| Beda template mode & custom mode | Kapan memilih yang mana |
| Cara bikin website pertamamu | Ditulis setelah editor selesai didesain |

Disimpan sebagai konten statis di app (tanpa backend), supaya bisa dibaca offline.

---

## 4. Project Anda

### 4.1 Isi layar (urut dari atas)

1. Kolom **search** (cari berdasarkan nama project)
2. **Chip filter status** beserta jumlahnya: `Semua (8) · Draft (5) · Siap export (2) · Sudah diexport (1)`
3. **Urutan**: Terakhir diedit (default) · Terbaru dibuat · Nama A–Z
4. **Daftar project** dalam bentuk **list satu kolom**: satu kartu per baris, thumbnail di kiri dan info di kanan

### 4.2 Status project

| Status | Arti | Badge |
|---|---|---|
| `draft` | Masih ada isi contoh (teks/foto bawaan template) yang belum diganti, atau isian wajib masih kosong (misalnya nomor WA di section kontak) | `DRAFT` abu-abu |
| `ready` | Semua isian lolos cek otomatis | `SIAP EXPORT` hijau (`color_success`) |
| `exported` | Sudah pernah diexport; tampilkan tanggal export terakhir | `DIEXPORT` outlined |

**Project yang sudah diexport lalu diedit lagi** tetap berstatus `exported`, ditambah teks kecil **"Ada perubahan sejak export terakhir"** (warna `color_warning`). Tujuannya agar user tahu websitenya pernah diexport, sekaligus sadar file ZIP yang sudah dia online-kan sudah ketinggalan dan perlu diexport ulang. Kondisi ini tidak butuh kolom baru: cukup cek `updated_at` lebih baru dari `last_exported_at`.

Status dihitung otomatis oleh app setiap kali project disimpan, mirip pengecekan otomatis template di sisi provider. **Aturan cek detailnya ditentukan saat membahas editor**, karena bergantung pada isi kerangka dan section.

### 4.3 Kartu project

| Isi | Keterangan |
|---|---|
| Thumbnail | Di sisi kiri kartu. Gambar tampilan atas website, dibuat app saat project disimpan |
| Nama project | Maksimal 2 baris |
| Badge status | Lihat 4.2 |
| Label mode | `TEMPLATE` atau `CUSTOM` dengan font Geist Mono |
| Waktu | "Diedit 2 jam lalu" |
| Info tambahan | Jika draft: "3 isian belum diisi". Jika diexport lalu diedit: "Ada perubahan sejak export terakhir" |

**Klik kartu** langsung membuka editor sesuai mode (bagian 2.1).

**Menu ⋮ di kartu:** Ganti nama · Duplikat · Export (hanya jika status `ready` atau `exported`) · Hapus (dengan dialog konfirmasi).

### 4.4 Kondisi khusus

| Kondisi | Tampilan |
|---|---|
| Belum ada project | Lottie + "Belum ada project" + tombol utama "Buat website" (membuka bottom sheet yang sama dengan tombol +) |
| Hasil search/filter kosong | "Tidak ada project yang cocok." + tombol "Hapus filter" |
| Offline | Tidak ada perbedaan, karena semua project tersimpan di HP |

### 4.5 Gambaran layar

```
┌──────────────────────────────────┐
│ Project                          │
├──────────────────────────────────┤
│ [ Cari project...            ]   │
│ (Semua 8)(Draft 5)(Siap 2)(Exp 1)│
│ Urutkan: Terakhir diedit ▾       │
│                                  │
│ ┌──────────────────────────────┐ │
│ │ [thumb] Toko Kue Bu Ani    ⋮ │ │
│ │         DRAFT · TEMPLATE     │ │
│ │         Diedit 2 jam lalu    │ │
│ │         3 isian belum diisi  │ │
│ └──────────────────────────────┘ │
│ ┌──────────────────────────────┐ │
│ │ [thumb] SMK Negeri 1       ⋮ │ │
│ │         DIEXPORT · CUSTOM    │ │
│ │         Diedit kemarin       │ │
│ │         Ada perubahan sejak  │ │
│ │         export terakhir      │ │
│ └──────────────────────────────┘ │
│ ┌──────────────────────────────┐ │
│ │ ...                          │ │
└──────────────────────────────────┘
```

### 4.6 Gambaran data (di HP, Room)

Project disimpan di HP (offline dulu). Tabel `projects` versi awal hanya berisi **metadata**. Isi website (format JSON section) ditentukan saat membahas editor.

| Kolom | Keterangan |
|---|---|
| `id` | UUID (string) |
| `name` | Nama project |
| `mode` | `template` / `custom` |
| `source_template_id` | ID template asal (null untuk custom). Dipakai untuk penghitungan "Didownload" di sisi provider |
| `status` | `draft` / `ready` / `exported` |
| `missing_count` | Jumlah isian yang belum lengkap (untuk teks "3 isian belum diisi") |
| `thumbnail_path` | Lokasi file thumbnail di folder project |
| `content_path` | Lokasi file JSON isi project. Formatnya menunggu diskusi editor |
| `created_at`, `updated_at`, `last_exported_at` | Waktu |

Search, filter, dan urutan dijalankan lewat query Room, sehingga semuanya tetap jalan tanpa internet.

**Tidak ada batas jumlah project.** User boleh membuat project sebanyak yang dia mau, karena semuanya tersimpan di HP-nya sendiri.

### 4.7 Penyimpanan dan nama awal project

| Aturan | Isi |
|---|---|
| Kapan tersimpan | **Otomatis tersimpan saat user melakukan perubahan** (autosave), tanpa tombol Simpan. Project baru dari template atau custom baru masuk ke tab Project setelah perubahan pertama. Jika user hanya membuka editor lalu keluar tanpa mengubah apa pun, tidak ada project yang tersimpan. |
| Nama awal project template | **Mengikuti nama template** yang dipakai |
| Nama awal project custom | **"Project tanpa nama"** |
| Ganti nama | Kapan saja lewat menu ⋮ → Ganti nama |

---

## 5. Profil (versi awal)

### 5.1 User yang sudah login

| Bagian | Isi |
|---|---|
| Kepala | Avatar, nama, email, badge mode `PEMBUAT WEBSITE` |
| Info pembuat website | Tujuan website dan nama organisasi (dari onboarding), bisa diedit |
| Ringkasan | Jumlah project dan jumlah yang sudah diexport (dihitung dari Room di HP) |
| Menu | Beralih ke mode provider / Jadi penyedia template · Pengaturan (termasuk metode login terhubung) · Keluar |

### 5.2 Tamu

| Bagian | Isi |
|---|---|
| Kartu ajakan | "Kamu belum masuk" + tombol **Masuk** |
| Info | Teks kecil: "Project tersimpan di HP ini." (usulan) |
| Menu | Pengaturan |

### 5.3 Catatan terhadap AGENTS.md

- Profil sekarang menjadi **tab sendiri**, sama seperti di Dashboard Provider. Bottom sheet menu profil (`ProfileSheet`) di AGENTS.md nantinya digantikan tab ini. Avatar di app bar mengarah ke tab Profil.
- Pemindahan project tamu ke akun tetap ditunda (AGENTS.md bagian 2.2).

### 5.4 Kebutuhan backend

| Endpoint | Fungsi |
|---|---|
| `PATCH /api/users/me/creator-profile` | Mengubah `displayName`, `websitePurpose`, `organizationName`. Membalas `UserResponse` |

Data lain sudah tersedia dari `GET /api/users/me`.

---

## 6. Versi awal: bagian yang "Segera hadir"

Karena editor masih dalam tahap perencanaan dan desain, user belum bisa membuat project. Perilaku versi awal:

| Bagian | Perilaku versi awal |
|---|---|
| Tombol **+** | Membuka bottom sheet Pakai template / Custom. **Pakai template** membuka tab Template (galeri versi awal, bagian 6.2). **Custom** membuka **halaman Editor Custom Mode "Segera hadir"** (bagian 6.1) |
| Kartu **Mulai** di Beranda | Sama dengan tombol + |
| Tab **Template** | **Galeri versi awal dengan data dummy** (bagian 6.2) |
| Section **Template untuk anda** | **Dibangun dengan data dummy** (bagian 6.2) |
| Klik kartu project (Beranda / tab Project) | Membuka **halaman editor sesuai mode project** (bagian 2.1): project template → Editor Template Mode "Segera hadir", project custom → Editor Custom Mode "Segera hadir" (bagian 6.1) |
| Klik template (tab Template / Template untuk anda) | Membuka Editor Template Mode "Segera hadir" |
| Menu ⋮ → **Export** | Dialog "Segera hadir" |
| Tab **Project** | Dibangun lengkap (list, filter, search, urutan, ganti nama, duplikat, hapus) beserta state kosong |

### 6.1 Halaman editor "Segera hadir"

Dua halaman editor **sudah dibuat sebagai layar terpisah**, masing-masing untuk satu mode. Navigasi dan logika pemilihan editor berdasarkan `mode` sudah berjalan sungguhan; isinya saja yang masih berupa pesan "Segera hadir". Saat editor didesain nanti, cukup isi layar ini tanpa mengubah alur navigasi.

| Bagian | Editor Template Mode | Editor Custom Mode |
|---|---|---|
| App bar | Tombol kembali + nama project (atau nama template) | Tombol kembali + nama project (atau "Project tanpa nama") |
| Label mode | `EDITOR TEMPLATE` (Geist Mono) | `EDITOR CUSTOM` (Geist Mono) |
| Badge | `SEGERA HADIR` | `SEGERA HADIR` |
| Judul | "Editor template sedang dirancang" | "Editor custom sedang dirancang" |
| Teks | "Fitur ini masih dalam tahap perencanaan dan desain. Nantinya kamu bisa mengganti teks, foto, dan warna template di sini." | "Fitur ini masih dalam tahap perencanaan dan desain. Nantinya kamu bisa menyusun website dari kerangka header, hero, footer, dan lainnya di sini." |
| Animasi | Lottie garis monokrom | Lottie garis monokrom |
| Tombol | "Kembali" (outlined) | "Kembali" (outlined) |

Aturan versi awal:

- Membuka halaman ini **tidak membuat project baru** dan tidak mengubah data project yang ada (termasuk `updated_at`).
- Tombol kembali mengembalikan user ke layar asal (Beranda, tab Project, atau bottom sheet +).
- Halaman Editor Template Mode bisa dicoba dengan mengklik template di galeri (data dummy) atau project contoh bermode `template` dari tombol debug.

**Data contoh (sama polanya dengan template provider):**

- **Test otomatis (wajib):** data project dummy di unit test untuk filter, search, urutan, dan perhitungan status.
- **Tombol debug "Isi project contoh" (disetujui, untuk kebutuhan testing):**
  - Letak: grup `DEBUG` di bagian paling bawah layar Pengaturan.
  - "Isi project contoh" mengisi Room dengan 6–8 project dummy dengan status (`draft`, `ready`, `exported`, termasuk yang diedit setelah export), mode (`template`, `custom`), dan waktu yang beragam.
  - "Hapus project contoh" mengosongkannya lagi, supaya state kosong juga bisa dicek.
  - Tujuannya agar Beranda, tab Project, dan halaman Editor Template Mode "Segera hadir" bisa dicoba dan didemokan di HP asli.
  - Kodenya ditaruh di source set `src/debug/`, sehingga **tidak pernah ikut ke build release**.

### 6.2 Galeri Template versi awal (data dummy)

Tujuan versi awal: **menguji tampilan dan alur** tab Template dan section "Template untuk anda" di Beranda. Datanya dummy. Detail galeri lanjutan (preview sebelum dipakai, template bawaan app, dll.) tetap dibahas nanti (bagian 9).

**Sumber data: backend, tabel `templates` yang sama dengan sisi provider** (`alurUntukProvider.md` bagian 6.6). Galeri hanya menampilkan template berstatus `published`.

| Aturan | Isi |
|---|---|
| Data dummy | Memakai **seeder demo yang sama** dengan sisi provider: profile `dev`, mati secara bawaan, dinyalakan dengan `app.seed.demo-templates=true`. Template `published` milik akun provider demo otomatis muncul di galeri. Tanpa seeder, galeri tampil kosong. |
| Test otomatis | Wajib: filter kategori, search, urutan, paginasi, dan rekomendasi berdasarkan `website_purpose` (backend: Testcontainers; Android: unit test ViewModel/mapper). |
| Login | **Tidak perlu login.** Tamu boleh melihat galeri (endpoint publik). |
| Internet | Galeri butuh internet. Saat offline tampil "Tidak ada koneksi. Periksa internet lalu coba lagi." + tombol coba lagi. |
| Klik template | Membuka **Editor Template Mode "Segera hadir"** (bagian 6.1). Tidak membuat project. |
| Kategori | Sama dengan pilihan tujuan website di onboarding: Sekolah, Organisasi, Usaha / UMKM, Instansi, Pribadi / Portofolio, Lainnya (nilai `sekolah`/`organisasi`/`umkm`/`instansi`/`pribadi`/`lainnya`). |
| Thumbnail | Jika `thumbnail_url` kosong (data dummy), app menampilkan placeholder netral bergaya garis monokrom. |

**Tab Template, isi layar (urut dari atas):**

1. Kolom **search** (nama template)
2. **Chip kategori**: Semua · Sekolah · Organisasi · Usaha / UMKM · Instansi · Pribadi / Portofolio · Lainnya
3. **Urutan**: Populer (Didownload terbanyak, default) · Terbaru
4. **Daftar template**, paginasi 20 per halaman, dimuat lanjut saat scroll

**Kartu template:**

| Isi | Keterangan |
|---|---|
| Thumbnail | Tampilan website |
| Nama template | Maksimal 2 baris |
| Nama kreator | `creator_name` provider |
| Label kategori | Geist Mono, mis. `UMKM` |
| Jumlah download | Mis. "120 download" |

**Kondisi khusus:**

| Kondisi | Tampilan |
|---|---|
| Belum ada template (seeder mati) | Lottie + "Belum ada template" + teks "Template akan muncul di sini." |
| Hasil search/filter kosong | "Tidak ada template yang cocok." + tombol "Hapus filter" |
| Offline / error server | `StateView` error + tombol coba lagi |

**"Template untuk anda" di Beranda:**

| Kondisi | Isi |
|---|---|
| User login dengan `website_purpose` | 6 template terpopuler di kategori itu. Jika kurang dari 6, app memanggil endpoint sekali lagi tanpa kategori untuk melengkapinya (tanpa duplikat) |
| Tamu / melewati onboarding | 6 template terpopuler dari semua kategori |
| "Lihat semua →" | Membuka tab Template dengan chip kategori yang sesuai sudah terpilih |
| Offline atau galeri kosong | Section disembunyikan, agar Beranda tidak menampilkan error |

**Kebutuhan backend:**

| Endpoint | Fungsi |
|---|---|
| `GET /api/templates?category=&q=&sort=popular\|newest&page=&size=` | Publik. Daftar template `published` + info kreator dan jumlah download |

"Template untuk anda" memakai endpoint yang sama (`category={website_purpose}&sort=popular&size=6`), sehingga tidak perlu endpoint khusus.

**Gambaran layar:**

```
┌──────────────────────────────────┐
│ Template                         │
├──────────────────────────────────┤
│ [ Cari template...           ]   │
│ (Semua)(Sekolah)(Organisasi)(UMKM)→
│ Urutkan: Populer ▾               │
│                                  │
│ ┌──────────────────────────────┐ │
│ │ [thumb] Toko Kue Modern      │ │
│ │         oleh Rina Studio     │ │
│ │         UMKM · 120 download  │ │
│ └──────────────────────────────┘ │
│ ┌──────────────────────────────┐ │
│ │ [thumb] Sekolah Hijau        │ │
│ │         oleh Budi Design     │ │
│ │         SEKOLAH · 87 download│ │
│ └──────────────────────────────┘ │
└──────────────────────────────────┘
```

---

## 7. Keputusan yang masih terbuka

| No | Pertanyaan | Usulan |
|---|---|---|
| 1 | ~~Daftar di tab Template: list satu kolom atau grid 2 kolom?~~ | **Diputuskan Aris (4 Okt 2026): list satu kolom.** |
| 2 | ~~Angka **"Dilihat"** milik provider didefinisikan sebagai "halaman detail template dibuka" (`alurUntukProvider.md` 3.4), padahal di sisi pembuat website **klik template langsung membuka editor** tanpa halaman detail. Kapan "Dilihat" dihitung? | Dihitung saat **template diklik** (editor template mode terbuka). Berlaku juga di versi awal saat editor masih "Segera hadir", supaya mekanisme event bisa dites. Jika disetujui, definisi di `alurUntukProvider.md` 3.4 ikut diperbarui~~ **Diputuskan Aris (4 Okt 2026): dihitung saat template diklik**; definisi provider 3.4 sudah diperbarui. |
| 3 | Menu profil (bottom sheet) di Dashboard Provider setelah Profil jadi tab | **Diputuskan Aris (4 Okt 2026): avatar di kedua dashboard membuka tab Profil; bottom sheet dihapus.** |

---

## 8. Hubungan dengan fitur lain

| Fitur | Hubungan |
|---|---|
| Onboarding (AGENTS.md 6.6) | `website_purpose` dipakai untuk "Template untuk anda" |
| Provider: angka "Didownload" | Export pertama dari project yang `source_template_id`-nya template provider dicatat sebagai event (lihat `alurUntukProvider.md` bagian 3.4–3.6) |
| Provider: tabel `templates` & seeder demo | Galeri memakai tabel `templates` dan seeder demo yang sama dengan sisi provider (`alurUntukProvider.md` bagian 5.0 dan 6.6) |
| Mode tamu & offline | Semua yang ada di dokumen ini bisa jalan tanpa login. Yang butuh internet: edit profil dan galeri Template (termasuk "Template untuk anda") |

---

## 9. Yang perlu dibahas berikutnya

> **Belum didiskusikan secara matang.** Jangan membangun apa pun dari daftar di bawah ini. Bagian ini dibahas bersamaan dengan fitur Upload di sisi provider.

- **Editor template mode**: desain tools, cara ganti teks/foto/warna, preview
- **Editor custom mode**: desain tools, cara memilih dan menyusun kerangka (header, hero, footer, dll.)
- **Kerangka section**: daftar jenis kerangka bawaan app, variasi layout, cara kerangka ditampilkan di custom mode
- **Kerangka buatan provider** (rencana ke depan): cara provider membuat dan mengunggah kerangka, pengecekan otomatisnya, cara kerangka provider dipajang di custom mode, dan apakah pemakaiannya ikut dihitung di statistik provider
- **Format project JSON** (dipakai bersama oleh kedua mode)
- **Website satu halaman atau multi-halaman**
- **Galeri Template lanjutan**: preview sebelum dipakai, template bawaan app (offline) vs template provider (online), cache galeri untuk offline, halaman profil kreator
- **Preview & Export**: toggle tampilan HP/desktop, isi ZIP, cara membagikan file
- **Aturan cek status project** (kapan dianggap `ready`)
