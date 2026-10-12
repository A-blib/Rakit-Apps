# Fitur Buat Website via Template (Mode "Pakai template")

> **Untuk siapa dokumen ini:** programmer junior atau model AI yang akan membangun fitur ini.
> **Status:** rancangan siap dieksekusi **setelah Aris meminta**. **Revisi besar 12 Oktober 2026** mengikuti `penyempurnaanSystem.md` (K6, K10, K15, K17, K21).
> **Prasyarat:** `revisiFondasiOrgSpace.md` fase R1–R4 dan `alurFiturPembuatanWibesite.md` fase W1–W2.
>
> **Perubahan terbesar dari versi 8 Oktober:** project **disimpan dan disinkron ke cloud** (milik ruang kerja), dengan **kunci edit** (satu orang mengedit dalam satu waktu). Room di HP menjadi **cache** agar tetap bisa dibuka dan diedit saat offline. Export ZIP dan penyusunan HTML akhir **dikerjakan server** (`alurFiturPublishWebsite.md`), sehingga jsoup di Android tidak dipakai lagi.

---

## 0. Baca dulu sebelum mulai

1. Baca **`AGENTS.md`** dan **`revisiFondasiOrgSpace.md`**. Semua aturan kerja tetap berlaku: kerja per fase, laporan format 13.4, catatan belajar, komentar Bahasa Indonesia, jangan commit rahasia, jangan menebak versi library. Desain dan teks mengikuti `revisiFondasiOrgSpace.md` bagian 7–8.
2. Dokumen ini **melengkapi** dokumen lain. Jika ada yang bertentangan, ikuti dokumen ini untuk editor mode Pakai template, lalu laporkan perbedaannya ke Aris:
   - `alurFiturPembuatanWibesite.md`: dashboard, tab Website, halaman kelola website.
   - `alurFiturPublishWebsite.md`: Terbitkan, export ZIP, Riwayat versi.
   - `alurFiturUpload.md`: cara template dibuat, ditandai, dan dipaket oleh kreator.
3. **Jangan menambah library** di luar daftar bagian 11.8 tanpa bertanya ke Aris.
4. **Tanyakan ke Aris** jika ada instruksi yang ambigu atau bertentangan.
5. Kerjakan per fase (bagian 13). Setelah satu fase selesai: tes, laporkan, lalu **berhenti dan tunggu konfirmasi**.

---

## 1. Tujuan fitur

Pengurus organisasi (non-teknis) bisa **membuat website dari template kreator**:

1. memilih template,
2. mengganti isi (teks, gambar, link, tombol) dan gaya (warna, sudut, ukuran huruf, tema) **hanya pada bagian yang diizinkan kreator**, dengan data dasar organisasi **terisi otomatis** dari Profil organisasi,
3. menyimpan pekerjaan di cloud sehingga **anggota lain bisa melanjutkan dari HP masing-masing**,
4. lalu **menerbitkannya** (dibangun server) atau mengunduhnya sebagai ZIP.

Mengedit website yang sudah ada tetap bisa dilakukan **offline**; perubahan dikirim ke cloud saat online. Membuat website baru butuh internet (bagian 3.1, E3).

---

## 2. Alur besar

```
Galeri Template → ketuk template
        │
        ▼
1. Layar detail template + unduh paket ke HP (sekali per versi, butuh internet)
        │  [Pakai template] (butuh masuk + ruang kerja)
        ▼
2. Server membuat website baru di ruang kerja aktif
   (isian terhubung diisi dari Profil organisasi) + memberi kunci edit
        │
        ▼
3. EDITOR
   ├─ isi teks, ganti foto, atur gaya
   ├─ pratinjau HP / Desktop, pindah halaman
   └─ autosave di HP → sinkron ke cloud
        │
        ▼
4. Cek kelengkapan (info, tidak memblokir)
        │
        ▼
5. TERBITKAN (Admin/Pemilik) / MINTA DITERBITKAN (Editor) / UNDUH ZIP
   → lihat alurFiturPublishWebsite.md
```

---

## 3. Keputusan yang sudah final

| Keputusan | Isi |
|---|---|
| Masuk ke editor | Ketuk template → **layar detail** (bagian 5) → **Pakai template** → editor website baru. Website lama dibuka dari kartu "Lanjutkan" di Beranda atau tombol **Edit tampilan** di halaman kelola website |
| Pemilik website | **Ruang kerja aktif** (K6). Semua anggota ruang kerja bisa melihat dan mengedit (peran mengatur siapa yang boleh menerbitkan) |
| Website baru | Isi template **disalin** menjadi website baru. Paket template di HP tidak pernah diubah |
| Kapan website dibuat | **Saat menekan Pakai template** (bukan menunggu perubahan pertama), karena server perlu mengisi data Profil organisasi dan memberi kunci edit. Jika user keluar tanpa mengubah apa pun, website baru yang masih sama persis dengan template **dihapus otomatis** (bagian 6.7) |
| Penyimpanan | Autosave di HP (debounce 500 ms) → sinkron ke cloud (bagian 11.4) |
| Kunci edit (K17) | Satu website hanya diedit satu orang dalam satu waktu. Anggota lain membuka dalam **mode baca** (bagian 11.5) |
| Isi otomatis (K10, v1) | Isian yang oleh kreator dihubungkan ke Profil organisasi langsung terisi saat website dibuat. Di v1 isian ini tetap bisa diubah seperti isian biasa |
| Nama awal | Nama template; jika sudah ada di ruang kerja, tambahkan nomor: "Aksara (2)". Bisa diganti kapan saja |
| Status website | Dihitung server: Draft / Online / Online · ada perubahan / Dimatikan / Ditangguhkan (`alurFiturPembuatanWibesite.md` 4.2) |
| Yang bisa diubah | **Hanya elemen yang ditandai kreator**: isi dan gaya yang diizinkan. Tata letak selalu terkunci |
| Gaya | Ditulis sebagai `custom.css` yang dimuat paling akhir (pratinjau di HP, hasil akhir dibuat server dengan aturan yang sama). File CSS template tidak diubah |
| Statistik kreator | "Dilihat": dikirim app saat layar detail dibuka. "Didownload": dicatat **server** saat terbit pertama **atau** export pertama (`alurFiturPublishWebsite.md` D5) |

### 3.1 Keputusan default (boleh diubah Aris; laporkan jika berubah)

| No | Pertanyaan | Default yang dibangun |
|---|---|---|
| D1 | Boleh terbit/export saat masih ada isian belum lengkap? | **Boleh**, dengan konfirmasi "Masih ada {n} isian yang belum lengkap. Tetap lanjut?" |
| D2 | Isi dan Gaya digabung atau dipisah? | **Dipisah** menjadi dua tab: **Isi** dan **Gaya** |
| D3 | Perlu layar detail template terpisah dari layar unduh? | **Tidak.** Layar detail sekaligus tempat unduh paket |
| E1 | Umur kunci edit | **2 menit**, diperpanjang otomatis tiap **45 detik** selama editor terbuka dan online |
| E2 | Siapa boleh mengambil alih kunci | **Admin & Pemilik** |
| E3 | Membuat website baru saat offline | **Tidak bisa** (butuh internet). Mengedit website yang sudah ada tetap bisa offline |
| E4 | Batas ukuran satu gambar setelah diproses | **1 MB** (WebP, sisi terpanjang maks 1920 px) |
| E5 | Batas total penyimpanan gambar per ruang kerja | **300 MB** (konfigurasi `PROJECT_STORAGE_MAX_MB_PER_ORG`; angka final menunggu T6) |

---

## 4. Cakupan

### 4.1 Bangun

- Backend: data project di cloud, aset gambar, kunci edit, cadangan isi, endpoint (bagian 12)
- Layar detail template + unduh paket (bagian 5)
- Editor: tab Isi, tab Gaya, pemilih halaman, toggle HP/Desktop, pratinjau layar penuh, undo/redo, autosave, sinkron, kunci edit, mode baca (bagian 6, 11)
- Isi otomatis dari Profil organisasi saat website dibuat (bagian 11.6)
- Ganti gambar: Photo Picker, potong tengah otomatis, kompres, unggah sebagai aset (bagian 6.6)
- Cek kelengkapan (bagian 7)
- Tombol **Terbitkan** / **Minta diterbitkan** dan **Unduh ZIP** di editor (memanggil alur `alurFiturPublishWebsite.md`)
- Event "Dilihat" dengan antrean offline (bagian 10)

### 4.2 Jangan bangun sekarang

- Editor **Rakit sendiri** (rancangan di `penyempurnaanSystem.md` 5.5; dokumen instruksi terpisah)
- **Versi template** (kreator memperbarui template; website lama ikut update atau tidak)
- Edit bersamaan oleh beberapa orang (real-time). Versi ini memakai kunci edit
- Potong gambar manual (geser/zoom area potong)
- Gaya **warna hover** dan **jenis huruf** (V1+ di `alurFiturUpload.md` 7.11)
- Pemberitahuan "Profil berubah, perlu diterbitkan ulang" dan tombol "Ubah khusus di website ini" (K10 v2)
- Modul Konten (`data-ws-list`) di editor (v2)

Jika sebuah tugas terasa membutuhkan salah satu hal di atas, **berhenti dan tanyakan ke Aris**.

---

## 5. Layar 1: Detail template + unduh paket

```
┌──────────────────────────────────┐
│ ←  Template                      │
├──────────────────────────────────┤
│ ┌──────────────────────────────┐ │
│ │         [thumbnail]          │ │
│ └──────────────────────────────┘ │
│ Aksara                           │
│ oleh Rina Studio                 │
│ [OSIS / MPK] [3 halaman] [4,2 MB]│
│ Punya: Struktur · Proker · Kontak│
│ Deskripsi template ...           │
│                                  │
│ ┌──────────────────────────────┐ │
│ │ Menyiapkan template...       │ │
│ │ Diunduh sekali, setelah itu  │ │
│ │ bisa dibuka tanpa internet.  │ │
│ │ ▓▓▓▓▓▓▓▓░░░░░  2,6 / 4,2 MB  │ │
│ └──────────────────────────────┘ │
├──────────────────────────────────┤
│ [ Pakai template ]               │
└──────────────────────────────────┘
```

| Aturan | Isi |
|---|---|
| Data detail | `GET /api/templates/{id}`: nama, kreator, kategori, label isi template, deskripsi, kata kunci, info teknis, thumbnail, ukuran paket, versi |
| Event "Dilihat" | Kirim `view` saat layar ini dibuka (bagian 10) |
| Unduh paket | Mulai otomatis saat layar dibuka jika paket versi ini belum ada di HP. Tampilkan progres (MB). Tombol **Pakai template** aktif setelah paket siap |
| Paket sudah ada di HP | Progres tidak ditampilkan; tombol langsung aktif |
| Tamu | Tombol **Pakai template** → layar Masuk → onboarding (jika perlu) → kembali ke layar ini |
| Pakai template | Panggil `POST /api/organizations/{orgId}/projects` (bagian 12) → buka editor website baru (kunci sudah dipegang). Tampilkan loading "Menyiapkan website..." |
| Batas penyimpanan ruang kerja penuh | Pesan: "Penyimpanan {org} sudah penuh. Hapus website yang tidak dipakai dulu, ya." |
| Data seluler + paket > 10 MB | Dialog sebelum mengunduh: "Template ini 18 MB dan kamu sedang pakai data seluler. Lanjut unduh?" |
| Offline | `StateView` error: "Butuh internet untuk membuka template ini." + **Coba lagi** |
| Gagal di tengah | Pesan error + **Coba lagi** (unduhan dimulai ulang dari awal) |
| Keamanan ekstrak | Tolak entri ZIP dengan path berbahaya (`../`, path absolut) — cegah *zip slip* |

---

## 6. Layar 2: Editor

### 6.1 Tata letak (tiga zona)

```
┌──────────────────────────────────┐
│ ← Website OSIS 2026 ✎   ↶ ↷ 👁 ⋮ │  ← app bar
│   Draft · tersimpan di cloud     │
│ [Beranda ▾]  [Terbitkan] [HP|Desk]│ ← pemilih halaman, aksi, tampilan
├──────────────────────────────────┤
│ ┌──────────────────────────────┐ │  ← pratinjau (WebView)
│ │ [OSIS Smansa]                │ │     elemen aktif disorot
│ │ Bersama melangkah, ...       │ │
│ └──────────────────────────────┘ │
├──────────────────────────────────┤
│           ───                    │  ← bottom sheet (bisa ditarik)
│ (Header ✓)(Hero ·2)(Tentang)     │  ← chip section + progres
│ [    Isi    |    Gaya    ]       │  ← tab
│ Nama organisasi *                │
│ [ OSIS Smansa          ] 11/30   │
│ ⓘ Diambil dari profil organisasi │
│ Slogan *                         │
│ [ Bersama melangkah, ... ]       │
│ ⚠ Masih teks contoh              │
│ Foto utama · 16:9                │
│ [ Ganti foto ]                   │
└──────────────────────────────────┘
```

(Ikon di gambaran ini hanya penanda posisi; di app memakai Material Symbols Rounded, bukan emoji.)

### 6.2 App bar dan baris aksi

| Elemen | Fungsi |
|---|---|
| ← | Kembali ke layar asal. Autosave sudah berjalan; kunci edit dilepas (bagian 11.5) |
| Nama website ✎ | Ketuk → dialog ganti nama |
| Status + simpan | Badge status + teks kecil: "menyimpan..." / "tersimpan di HP" (offline) / "tersimpan di cloud" / "gagal sinkron, akan dicoba lagi" |
| ↶ / ↷ | Undo / redo. Riwayat di memori selama editor terbuka, maks 50 langkah |
| 👁 | Pratinjau layar penuh (tanpa form). Toggle HP/Desktop tetap tersedia; link antar-halaman berfungsi |
| **Terbitkan** / **Minta diterbitkan** | Sesuai peran. Sebelum memanggil alur terbit, pastikan semua perubahan sudah tersinkron (`alurFiturPublishWebsite.md` 8.3) |
| ⋮ | Cek kelengkapan · Unduh ZIP · Kembalikan ke isi template (konfirmasi) · Lepas kunci & tutup |

### 6.3 Bagian atas pratinjau

- **Pemilih halaman**: hanya muncul jika template punya lebih dari 1 halaman.
- **Toggle HP / Desktop**: Desktop = WebView dengan lebar virtual 1280px lalu diperkecil (sama dengan sisi kreator, `alurFiturUpload.md` 7.4).

### 6.4 Tab Isi

| Aturan | Isi |
|---|---|
| Chip section | Urutan sesuai paket. `✓` jika semua isian wajib di section itu lengkap, `·N` jika masih ada N isian belum lengkap. Ketuk → form pindah ke section itu dan pratinjau menggulir ke sana |
| Urutan isian | Sesuai `order` di manifest; jika tidak ada, sesuai urutan di halaman |
| Komponen per jenis | Teks pendek → `TextInputLayout` 1 baris + penghitung; Paragraf → multi-baris + penghitung; Gambar → tombol "Ganti foto" + thumbnail; Link → input URL; Tombol → teks tombol + link |
| Label & petunjuk | Dari manifest (`label`, `hint`). Isian wajib bertanda `*` |
| Batas | `maxLength` ditegakkan saat mengetik. Rasio gambar ditampilkan, mis. "16:9" |
| Teks/foto contoh | Nilai masih sama dengan contoh kreator → tanda "Masih teks contoh" / "Masih foto contoh" (`color_warning`) |
| Isian dari profil (K10) | Isian dengan `profileKey` yang terisi dari Profil organisasi diberi info kecil **"Diambil dari profil organisasi"**. Hilang setelah user mengubah nilainya |
| Profil kosong | Jika kolom profil kosong dan manifest menandai `hideWhenEmpty`, elemen disembunyikan di pratinjau dan hasil terbit. Isian tetap tampil di form dengan info "Kosong, jadi tidak ditampilkan di website" |
| Sinkron dua arah | Ketuk isian → elemennya disorot di pratinjau. Ketuk elemen yang bisa diedit di pratinjau → form membuka isian tersebut |
| Isian terhubung | Satu isian dengan beberapa elemen (HP/desktop, lintas halaman) → satu input, semua elemen berubah |
| Validasi link | Hanya `https://`, `http://`, `mailto:`, `tel:`, dan `https://wa.me/...`. Tolak `javascript:` dan skema lain |
| Bantuan WhatsApp | Untuk link/tombol, pilihan "Nomor WhatsApp" yang otomatis menjadi `https://wa.me/62…` |
| Mode baca | Semua input nonaktif (bagian 11.5) |

### 6.5 Tab Gaya

```
TEMA WEBSITE
Warna utama            [■]
Sudut                  14 px  ───●────
SECTION HERO
Warna judul            [■]
Ukuran judul           40 px  ──●──   (32–48 px)
⚠ Teksnya susah dibaca   [Perbaiki otomatis]
```

| Aturan | Isi |
|---|---|
| Tema website | Paling atas, hanya jika paket punya `theme` (variabel CSS `:root`) |
| Gaya per section | Dikelompokkan per section, hanya gaya yang diizinkan (`styles` di manifest) |
| Komponen | Warna → kotak warna + pemilih warna (palet + input hex); ukuran huruf & sudut → `Slider` Material dengan rentang dari manifest |
| Cek keterbacaan | Kontras teks vs latar < 4,5 → peringatan + tombol **Perbaiki otomatis** (ganti warna teks ke gelap/terang yang lolos). Tidak memblokir |
| Reset per gaya | Ikon ↺ → kembali ke nilai asli template |
| Hasil | Setiap perubahan memperbarui `custom.css` di pratinjau secara langsung (bagian 11.3) |

### 6.6 Ganti gambar

1. Ketuk "Ganti foto" → **Photo Picker** Android (`ActivityResultContracts.PickVisualMedia`, hanya gambar).
2. Proses di background (`AppExecutors`):
   - Baca gambar, perbaiki orientasi EXIF.
   - Jika isian punya `aspectRatio` → **potong tengah otomatis**.
   - Perkecil hingga sisi terpanjang maks **1920 px**; simpan **WebP kualitas 80** (≤ E4) ke cache gambar website (bagian 11.2) dengan id lokal.
3. Pasang di form dan pratinjau segera (memakai file lokal).
4. `SyncProjectWorker` mengunggah gambar ke `POST /api/projects/{id}/assets`, lalu mengganti rujukan `local:{id}` menjadi `asset:{assetId}` di isi website sebelum isi dikirim (bagian 11.4).
5. Pilihan **"Kembalikan foto contoh"** tersedia di menu isian.

### 6.7 Autosave, sinkron, dan website kosong

| Aturan | Isi |
|---|---|
| Website baru | Dibuat server saat **Pakai template** (bagian 5). Kunci edit langsung dipegang pembuatnya |
| Setiap perubahan | Simpan isi ke Room/file lokal (debounce 500 ms), tandai `sync_state = pending`, jadwalkan `SyncProjectWorker` |
| Kirim ke cloud | `PUT /api/projects/{id}/content` dengan `baseVersion` (bagian 11.4) |
| Thumbnail | Saat editor ditutup: tangkap bagian atas pratinjau halaman utama (WebP kecil) → `POST /api/projects/{id}/thumbnail` |
| Gagal sinkron | Teks "gagal sinkron, akan dicoba lagi"; WorkManager mencoba ulang dengan jeda bertahap |
| Keluar tanpa perubahan | Jika website baru dan isinya masih sama persis dengan template → `DELETE /api/projects/{id}` otomatis (tanpa konfirmasi), agar tab Website tidak penuh website kosong |

---

## 7. Layar 3: Cek kelengkapan

### 7.1 Aturan kelengkapan

| Kondisi isian | Kelompok |
|---|---|
| Isian **wajib** kosong | **Perlu dilengkapi** |
| Isian **wajib** masih berisi teks/foto contoh kreator | **Perlu dilengkapi** |
| Isian **opsional** masih berisi contoh | **Saran** |
| Link/tombol dengan URL tidak valid | **Perlu dilengkapi** |
| Isian dari profil yang kosong dan `hideWhenEmpty` | Tidak dihitung |

- `missing_count` = jumlah "Perlu dilengkapi". Dihitung di HP oleh `CompletenessChecker` setiap autosave dan dikirim bersama isi (`missingCount`). Server menyimpannya untuk ditampilkan di kartu website ("3 isian belum diisi").
- Kelengkapan **tidak** menentukan status Draft/Online lagi; hanya info dan peringatan sebelum terbit (D1).

### 7.2 Layar

```
┌──────────────────────────────────┐
│ ←  Kelengkapan                   │
├──────────────────────────────────┤
│ 9 dari 12 isian lengkap          │
│ ▓▓▓▓▓▓▓▓▓░░░                     │
│ PERLU DILENGKAPI                 │
│ ◌ Slogan · masih contoh        › │
│ ◌ Nomor WhatsApp · kosong      › │
│ ◌ Foto utama · masih contoh    › │
│ SARAN (opsional)                 │
│ ⓘ Foto galeri 3 masih contoh     │
├──────────────────────────────────┤
│ [ Lengkapi sekarang ]            │
│ [ Tetap terbitkan ]              │  ← teks menyesuaikan: "Tetap unduh ZIP" / tidak tampil untuk Editor
└──────────────────────────────────┘
```

| Aturan | Isi |
|---|---|
| Cara membuka | Menu ⋮ → Cek kelengkapan, atau otomatis saat menekan Terbitkan/Unduh ZIP ketika `missing_count > 0` |
| Ketuk baris | Kembali ke editor, buka halaman + section + isian tersebut |
| Lengkapi sekarang | Lompat ke isian pertama yang belum lengkap |
| Semua lengkap | "Semua isian wajib sudah lengkap. Mantap!" + tombol sesuai aksi |

---

## 8. Unduh ZIP

- Menu ⋮ → **Unduh ZIP** (semua peran). Jika ada perubahan belum tersinkron, sinkronkan dulu.
- App memanggil `POST /api/projects/{id}/exports`, menampilkan status antrean seperti terbit, lalu mengunduh dari `downloadUrl` (`alurFiturPublishWebsite.md` 7.12).
- Setelah terunduh, bottom sheet: **Simpan ke HP** (`ACTION_CREATE_DOCUMENT`) · **Bagikan** (`FileProvider` + `ACTION_SEND`).
- Nama file: dari nama website, huruf kecil, spasi → tanda hubung, hanya `a–z 0–9 -`, + `.zip` (mis. "Website OSIS 2026" → `website-osis-2026.zip`).
- Setelah berhasil: snackbar "ZIP tersimpan." + tautan Panduan **"Mengonlinekan sendiri file ZIP"** (`alurFiturPembuatanWibesite.md` 3.4).
- Offline: "Butuh internet untuk menyiapkan ZIP." + **Coba lagi**.

---

## 9. Setelah terbit

Layar hasil, QR code, dan Riwayat versi diatur `alurFiturPublishWebsite.md` bagian 8. Dari layar hasil, **Kembali** membawa user ke editor (jika dibuka dari editor) atau halaman kelola website.

---

## 10. Event "Dilihat" untuk kreator

| Event | Kapan dikirim | Body `POST /api/templates/{id}/events` |
|---|---|---|
| `view` | Layar detail template dibuka | `{ type: "view", installId, occurredAt }` |

- `installId` = UUID acak per instalasi app, disimpan di `SessionStore`, tidak terhubung ke identitas siapa pun.
- Offline → simpan di tabel `pending_events`, kirim dengan **WorkManager** saat ada koneksi.
- Dikirim juga untuk tamu. Jika login, sertakan JWT.
- Server mengabaikan event ganda dan event dari pemilik template (`alurUntukProvider.md` 3.5–3.6).
- Event `download` **tidak lagi dikirim app**; server mencatatnya saat terbit/export pertama.

---

## 11. Data dan teknis

### 11.1 Room (cache di HP)

**`projects`** (cache; sumber kebenaran di server):

| Kolom | Keterangan |
|---|---|
| `id` | UUID dari server |
| `organization_id` | Ruang kerja pemilik |
| `name`, `mode` (`template` / `assembled`) | |
| `source_template_id`, `template_version` | Template asal dan versi paket |
| `content_path` | Lokasi file isi lokal (`values.json`) |
| `server_version` | `content_version` terakhir yang diketahui dari server |
| `sync_state` | `synced` / `pending` / `conflict` |
| `lock_holder_user_id`, `lock_holder_name`, `lock_expires_at`, `i_hold_lock` | Info kunci edit terakhir |
| `missing_count` | Untuk kartu website |
| `thumbnail_path` | Thumbnail lokal |
| `site_*`, `has_unpublished_changes`, `latest_build_status` | Dari `alurFiturPublishWebsite.md` 8.1 |
| `updated_at`, `updated_by_name` | Untuk "Diubah 2 jam lalu oleh Budi" |

**`template_packages`**: `template_id` + `version` (kunci), `name`, `creator_name`, `category`, `path`, `size_bytes`, `downloaded_at`, `last_used_at`. Paket yang tidak dipakai website mana pun di cache dan tidak dibuka 30 hari boleh dihapus otomatis.

**`pending_images`**: `local_id`, `project_id`, `file_path`, `field_key`, `asset_id` (null sampai terunggah), `attempts`.

**`pending_events`**: `id`, `template_id`, `type`, `occurred_at`, `attempts`.

### 11.2 Struktur folder di HP (penyimpanan internal app)

```
files/
├── templates/{templateId}/{version}/     ← paket hasil unduh (TIDAK PERNAH diubah)
│   ├── manifest.json
│   ├── index.html, tentang.html, ...
│   ├── css/ js/ img/ vendor/
└── projects/{projectId}/
    ├── values.json                       ← isi lokal (salinan isi di cloud + perubahan belum terkirim)
    ├── thumbnail.webp
    └── images/                           ← gambar lokal (belum/sudah terunggah) + cache aset dari server
```

### 11.3 Format data

**`manifest.json`** (dibuat server saat template dipublikasikan; `alurFiturUpload.md` bagian 11):

```json
{
  "templateId": "uuid",
  "version": 1,
  "category": "osis_mpk",
  "pages": [ { "file": "index.html", "name": "Beranda" } ],
  "sections": [ { "id": "s2", "page": "index.html", "name": "Hero" } ],
  "theme": [ { "var": "--primary", "label": "Warna utama", "type": "color", "default": "#3B5BFD" } ],
  "fields": [
    {
      "key": "nama_organisasi",
      "label": "Nama organisasi",
      "type": "text",
      "maxLength": 30,
      "required": true,
      "order": 1,
      "sectionId": "s1",
      "sample": "OSIS Harapan Bangsa",
      "profileKey": "short_name",
      "styles": [ { "prop": "color" }, { "prop": "font-size", "min": 32, "max": 48, "unit": "px", "default": 40 } ]
    },
    {
      "key": "link_instagram",
      "label": "Instagram",
      "type": "link",
      "required": false,
      "sectionId": "s9",
      "sample": "https://instagram.com/contoh",
      "profileKey": "instagram",
      "hideWhenEmpty": true
    },
    {
      "key": "foto_utama",
      "label": "Foto utama",
      "type": "image",
      "aspectRatio": "16:9",
      "required": true,
      "sectionId": "s2",
      "sample": "img/hero.jpg"
    }
  ]
}
```

- `profileKey` (opsional): salah satu kolom Profil organisasi — `name`, `short_name`, `logo`, `institution_name`, `address`, `email`, `whatsapp`, `instagram`, `tiktok`, `youtube`.
- `hideWhenEmpty` (opsional, hanya untuk isian dengan `profileKey`): sembunyikan elemen jika nilainya kosong.
- `type`: `text`, `paragraph`, `image`, `link`, `button`.

**Isi website (`values.json`, sama di HP dan cloud):**

```json
{
  "fields": {
    "nama_organisasi": { "text": "OSIS Smansa", "fromProfile": true },
    "foto_utama": { "image": "asset:5b1e...", "fromProfile": false },
    "tombol_daftar": { "text": "Gabung bareng kami", "href": "https://wa.me/62812..." }
  },
  "styles": { "nama_organisasi": { "color": "#1A2150", "font-size": 40 } },
  "theme": { "--primary": "#16A34A" }
}
```

- Isian yang tidak ada di `fields` = masih memakai isi template.
- Rujukan gambar: `asset:{assetId}` (sudah di server) atau `local:{localId}` (hanya di HP, **tidak pernah** dikirim ke server).
- `fromProfile: true` hanya penanda tampilan ("Diambil dari profil organisasi"); berubah `false` saat user mengubah nilainya.

**`custom.css`** (aturan sama untuk pratinjau di HP dan tahap Susun di server):

```css
:root { --primary: #16A34A; }
[data-key="nama_organisasi"] { color: #1A2150 !important; font-size: 40px !important; }
```

- Di pratinjau: disuntikkan sebagai `<style id="app-custom-css">`.
- Di server: karena atribut `data-key` dihapus, setiap elemen bergaya diberi class unik dulu (mis. `ws-nama_organisasi`), lalu selector memakai class itu.
- Warna wajib valid (`#rrggbb`), angka wajib di dalam rentang manifest. Input mentah user **tidak pernah** ditulis ke CSS.

### 11.4 Sinkron ke cloud

```
Perubahan di editor
→ simpan lokal (sync_state = pending)
→ SyncProjectWorker (WorkManager, butuh koneksi):
   1. unggah gambar di pending_images → dapat assetId → ganti local:{id} menjadi asset:{id}
   2. PUT /api/projects/{id}/content { baseVersion, content, missingCount }
      ├─ 200 → server_version = versi baru, sync_state = synced
      ├─ 409 VERSION_CONFLICT → (seharusnya jarang, karena ada kunci) simpan isi lokal sebagai cadangan
      │                          (POST /api/projects/{id}/backups), muat isi terbaru server, sync_state = synced,
      │                          tampilkan dialog (bagian 11.5)
      └─ 409 LOCK_LOST → sama seperti VERSION_CONFLICT
```

- Server menaikkan `content_version` setiap isi berubah dan menolak `baseVersion` yang tidak sama dengan versi saat ini (**optimistic locking**), sebagai pengaman kedua di samping kunci edit.
- Saat membuka website dari daftar: jika online, ambil isi terbaru (`GET /api/projects/{id}`) bila `content_version` server lebih baru dari cache; jika offline, pakai cache.

### 11.5 Kunci edit

| Situasi | Yang terjadi |
|---|---|
| Membuka editor (online) | `POST /api/projects/{id}/lock` → dapat kunci (`expiresAt` = 2 menit). Diperpanjang tiap 45 detik selama editor terbuka |
| Kunci dipegang orang lain yang masih aktif | Editor terbuka dalam **mode baca**: banner "Sedang diedit oleh {nama} · terakhir aktif {waktu}". Isi diperbarui tiap 15 detik. Admin/Pemilik melihat tombol **Ambil alih** |
| Kunci orang lain sudah kedaluwarsa | Kunci boleh diambil langsung (tanpa ambil alih) |
| Menutup editor | `DELETE /api/projects/{id}/lock` (setelah sinkron terakhir dicoba) |
| Membuka editor saat offline | Boleh mengedit **jika** cache mencatat HP ini pemegang kunci terakhir (`i_hold_lock`). Jika tidak, mode baca dari cache dengan banner "Kamu sedang offline" |
| Kembali online setelah offline | Coba perpanjang kunci. Server **mengizinkan** perpanjangan kunci yang sudah kedaluwarsa **selama belum diambil orang lain**, lalu perubahan offline disinkron seperti biasa |
| Kunci ternyata sudah diambil orang lain | `409 LOCK_LOST`: isi lokal disimpan sebagai **cadangan**, editor berpindah ke mode baca, dialog: "Website ini sekarang diedit oleh {nama}. Perubahanmu saat offline sudah disimpan sebagai cadangan." + **Lihat cadangan** |
| Ambil alih (Admin/Pemilik) | Dialog konfirmasi: "{nama} sedang mengedit. Kalau diambil alih, perubahan {nama} yang belum tersimpan akan jadi cadangan." → `POST /api/projects/{id}/lock/takeover` |
| Cadangan | Tersimpan di server (`project_content_backups`), terlihat di menu ⋮ → **Cadangan** (maks 5 terbaru per website). Admin/Pemilik bisa **Pulihkan** (mengganti isi saat ini, butuh kunci). Cadangan berumur > 30 hari dihapus otomatis |

### 11.6 Isi otomatis dari Profil organisasi (K10 v1)

1. `POST /api/organizations/{orgId}/projects` dengan `sourceTemplateId` + `templateVersion`.
2. Server membaca `manifest.json` paket dan Profil organisasi; untuk setiap isian dengan `profileKey` yang nilainya ada, tulis nilai ke `fields` dengan `fromProfile: true`. Untuk `logo`, server menyalin file logo organisasi menjadi aset project.
3. Server mengembalikan isi awal + kunci edit; app menyimpan ke cache dan membuka editor.
4. Perubahan Profil organisasi **setelahnya** tidak mengubah website yang sudah ada di v1 (pemberitahuan terbit ulang adalah v2).

### 11.7 Pratinjau (WebView)

- Tampilkan file paket lewat **`WebViewAssetLoader`** (`androidx.webkit`), bukan `file://`.
- Gambar website (lokal dan cache aset) dilayani lewat path handler `WebViewAssetLoader`. Aset yang belum ada di cache diunduh dari `GET /api/assets/{id}` (dengan JWT) lalu disimpan.
- Setelah halaman dimuat, suntikkan script milik app yang menerapkan nilai ke `[data-key]` (teks lewat `textContent`, bukan `innerHTML`), memasang `<style id="app-custom-css">`, menyembunyikan elemen `hideWhenEmpty` yang kosong, menyorot elemen aktif, dan melaporkan ketukan.
- Setiap perubahan form → `evaluateJavascript` untuk memperbarui elemen terkait saja.
- Jembatan JS ↔ Java **sangat terbatas**: hanya menerima `{ type: "tap", key: "..." }`. Validasi di Java.
- Blokir navigasi ke situs luar dan koneksi ke domain di luar daftar CDN terpercaya.

### 11.8 Arsitektur Android dan library

| Kelas | Tugas |
|---|---|
| `TemplateDetailFragment` + `TemplateDetailViewModel` | Layar detail + unduh (bagian 5) |
| `TemplatePackageRepository` | Unduh, ekstrak, cache paket |
| `TemplateEditorFragment` + `TemplateEditorViewModel` | Editor; state form, undo stack, mode baca |
| `ProjectRepository` | Cache Room, file isi lokal, panggilan API project |
| `EditLockManager` | Ambil, perpanjang (timer 45 detik), lepas, ambil alih kunci |
| `SyncProjectWorker` | Unggah gambar lalu isi (bagian 11.4) |
| `TemplateRenderer` | Script suntik pratinjau; membuat `custom.css` |
| `ImageProcessor` | Potong tengah, perkecil, WebP |
| `CompletenessChecker` | Aturan bagian 7.1 (murni Java, mudah di-unit-test) |
| `TemplateEventRepository` + `SendEventsWorker` | Event bagian 10 |

Pola tetap: ViewModel + LiveData, `Resource`, `StateView`, `AppExecutors`, Hilt, ViewBinding, Navigation Component.

| Library | Status | Untuk |
|---|---|---|
| Room | Boleh (`revisiFondasiOrgSpace.md` 9) | Cache project, paket, gambar & event tertunda |
| WorkManager | Boleh | Sinkron isi, unggah gambar, event |
| `androidx.webkit` | **Disetujui Aris** (6 Okt 2026) | `WebViewAssetLoader` |
| Photo Picker (`androidx.activity`) | Sudah ada | Memilih gambar |
| `androidx.exifinterface` | **Tanyakan ke Aris dulu** | Orientasi foto |
| Glide | Boleh | Thumbnail di form dan daftar |
| `java.util.zip` | Bawaan Java | Ekstrak paket |
| jsoup (Android) | **Tidak dipakai lagi** | Penyusunan HTML akhir pindah ke server |

---

## 12. Backend

### 12.1 Migrasi (nomor versi melanjutkan migrasi terakhir)

**`projects`**

| Kolom | Keterangan |
|---|---|
| `id` | uuid |
| `organization_id` | FK `organizations`, on delete cascade |
| `name` | varchar(100) |
| `mode` | `template` / `assembled` (CHECK) |
| `source_template_id` | FK `templates`, boleh null |
| `template_version` | int, boleh null |
| `content` | jsonb (format `values.json`) |
| `content_version` | int, naik setiap isi berubah |
| `content_updated_at` | timestamptz (untuk `hasUnpublishedChanges`) |
| `missing_count` | int |
| `thumbnail_asset_id` | FK `project_assets`, boleh null |
| `created_by`, `updated_by` | FK `users` |
| `created_at`, `updated_at` | timestamptz |

**`project_assets`** (gambar; tidak pernah diubah setelah dibuat)

| Kolom | Keterangan |
|---|---|
| `id` | uuid |
| `organization_id` | FK, untuk menghitung batas penyimpanan |
| `project_id` | FK `projects`, on delete cascade |
| `sha256` | hash isi; unik per (`project_id`, `sha256`) agar unggahan ganda tidak menambah file |
| `mime_type`, `width`, `height`, `size_bytes` | |
| `storage_path` | Lokasi file di server (`PROJECT_ASSETS_DIR`) |
| `created_by`, `created_at` | |

Aset boleh dihapus job pembersihan hanya jika **tidak** dirujuk isi project saat ini, cadangan, maupun snapshot rilis yang masih tersimpan.

**`project_locks`**: `project_id` (PK, FK cascade), `user_id`, `device_id`, `acquired_at`, `expires_at`.

**`project_content_backups`**: `id`, `project_id` (FK cascade), `content` (jsonb), `reason` (`lock_lost` / `takeover` / `restore` / `conflict`), `created_by`, `created_at`.

### 12.2 Endpoint

| Method & path | Body | Hasil | Peran |
|---|---|---|---|
| `GET /api/templates/{id}` | – | Detail template `published` (+ label isi template) | publik |
| `GET /api/templates/{id}/package` | – | ZIP paket versi terbaru (`Content-Length` untuk progres) | publik |
| `POST /api/templates/{id}/events` | `{ type: "view", installId, occurredAt }` | 204 | publik (JWT opsional) |
| `POST /api/organizations/{orgId}/projects` | `{ mode: "template", sourceTemplateId, templateVersion, name? }` | `ProjectResponse` (201) + kunci edit | editor |
| `GET /api/projects/{id}` | – | `ProjectResponse` (isi, versi, info kunci) | anggota |
| `PUT /api/projects/{id}/content` | `{ baseVersion, content, missingCount }` | `{ contentVersion }`; `409 VERSION_CONFLICT` / `LOCK_LOST` | pemegang kunci |
| `PATCH /api/projects/{id}` | `{ name }` | `ProjectResponse` | editor |
| `POST /api/projects/{id}/duplicate` | `{ name? }` | `ProjectResponse` (website baru, draft) | editor |
| `DELETE /api/projects/{id}` | `{ confirmName? }` | 204 (lihat `alurFiturPublishWebsite.md` untuk website yang pernah terbit; website draft baru yang masih kosong boleh dihapus pembuatnya) | owner / pembuat draft kosong |
| `POST /api/projects/{id}/lock` | `{ deviceId }` | `{ expiresAt }`; `409 PROJECT_LOCKED` + `{ holderName, lastActiveAt }` | editor |
| `DELETE /api/projects/{id}/lock` | – | 204 | pemegang kunci |
| `POST /api/projects/{id}/lock/takeover` | `{ deviceId }` | `{ expiresAt }` | admin |
| `POST /api/projects/{id}/assets` | multipart `file` (WebP/JPEG/PNG ≤ E4) | `{ assetId, width, height }`; `STORAGE_LIMIT_REACHED` | pemegang kunci |
| `GET /api/assets/{id}` | – | File gambar | anggota ruang kerja pemilik |
| `POST /api/projects/{id}/thumbnail` | multipart `file` | 204 | editor |
| `GET /api/projects/{id}/backups` | – | Daftar cadangan | anggota |
| `POST /api/projects/{id}/backups` | `{ content, reason }` | 201 | anggota |
| `POST /api/projects/{id}/backups/{backupId}/restore` | – | `{ contentVersion }` | admin + pemegang kunci |

**Kode error baru:** `PROJECT_NOT_FOUND`, `PROJECT_LOCKED`, `LOCK_LOST`, `VERSION_CONFLICT`, `STORAGE_LIMIT_REACHED`, `ASSET_INVALID`, `TEMPLATE_NOT_FOUND`.

**Aturan:**
- Semua endpoint project memakai `OrganizationAccess` (peran) **dan** memastikan project milik ruang kerja itu.
- Validasi `content` di server: hanya `key` yang ada di manifest; panjang teks sesuai `maxLength`; link sesuai aturan 6.4; warna/angka sesuai manifest; rujukan `asset:` harus milik project yang sama. Rujukan `local:` ditolak.
- Gambar: periksa tipe dari isi file, ubah ulang ke WebP di server (jangan simpan file asli), tolak > E4.
- Seeder `dev`: minimal **2 paket template contoh** berkategori organisasi lengkap dengan `manifest.json` (1 template satu halaman dengan isian `profileKey`; 1 template multi-halaman dengan isian terhubung, tema, dan `hideWhenEmpty`). Sumber paket di `backend/src/test/resources/template-packages/`.

---

## 13. Fase pengerjaan

| Fase | Kerjakan | Anggap selesai jika |
|---|---|---|
| **T1** Backend paket & event | Endpoint template (12.2 baris 1–3), seeder 2 paket contoh, test | Detail + unduh paket + event berhasil lewat Swagger |
| **T2** Backend project cloud | Migrasi 12.1, endpoint project/kunci/aset/cadangan, isi otomatis dari profil (11.6), validasi isi, test | Integration test: buat → ubah isi dengan `baseVersion` → konflik versi → kunci, ambil alih, kunci kedaluwarsa diperpanjang → cadangan & pulihkan → batas penyimpanan |
| **T3** Detail & unduh paket (Android) | Layar detail, `TemplatePackageRepository`, ekstrak aman, Pakai template | Template terunduh; Pakai template membuat website di ruang kerja aktif dengan nama organisasi sudah terisi |
| **T4** Editor: pratinjau, tab Isi, sinkron, kunci | WebView, script suntik, form per section, sinkron dua arah, pemilih halaman, HP/Desktop, autosave, `SyncProjectWorker`, `EditLockManager`, mode baca, hapus otomatis website kosong | Skenario 1–10 bagian 14 berhasil |
| **T5** Gambar | Photo Picker, `ImageProcessor`, unggah aset, kembalikan foto contoh | Foto terpasang dengan rasio benar; tetap tampil di HP anggota lain setelah sinkron |
| **T6** Tab Gaya & tema | Kontrol warna & slider, `custom.css` di pratinjau, cek keterbacaan + Perbaiki otomatis, undo/redo | Perubahan gaya langsung terlihat; undo/redo bekerja untuk isi dan gaya |
| **T7** Kelengkapan, terbit & ZIP | `CompletenessChecker`, layar Kelengkapan, tombol Terbitkan/Minta diterbitkan (memanggil alur publish), Unduh ZIP | Skenario 13–17 berhasil (setelah fase P2–P4 publish selesai) |
| **T8** Polesan | State kosong/error, mode gelap, aksesibilitas, screenshot | Semua skenario bagian 14 berhasil di HP asli |

---

## 14. Skenario uji di HP asli

| No | Skenario | Hasil yang diharapkan |
|---|---|---|
| 1 | Ketuk template pertama kali (online) | Layar detail dengan progres unduh; Pakai template aktif setelah selesai |
| 2 | Pakai template di ruang kerja OSIS | Website baru dibuat; nama organisasi, logo, Instagram terisi dari profil dengan info "Diambil dari profil organisasi" |
| 3 | Profil tidak punya TikTok | Ikon TikTok tidak tampil di pratinjau; isian berinfo "Kosong, jadi tidak ditampilkan di website" |
| 4 | Buka editor lalu kembali tanpa mengubah apa pun | Website kosong itu hilang dari tab Website |
| 5 | Ubah satu teks, tunggu, buka di HP anggota lain | Perubahan terlihat di HP lain; status "tersimpan di cloud" di HP pertama |
| 6 | HP A sedang mengedit, HP B membuka website yang sama | HP B mode baca dengan banner "Sedang diedit oleh A"; isi di HP B ikut berubah dalam ±15 detik |
| 7 | HP A menutup editor, HP B membuka lagi | HP B bisa mengedit |
| 8 | Admin di HP B menekan Ambil alih | HP B bisa mengedit; HP A berpindah ke mode baca dengan dialog cadangan |
| 9 | HP A mode pesawat saat mengedit 5 menit, lalu online (tidak ada yang mengambil kunci) | Perubahan tersinkron tanpa dialog |
| 10 | Seperti no. 9, tetapi HP B mengambil kunci selama HP A offline | HP A: dialog "sekarang diedit oleh B", perubahan offline menjadi cadangan; Admin bisa memulihkannya |
| 11 | Ketuk judul di pratinjau | Form membuka isian judul |
| 12 | Ubah isian yang terhubung di 3 halaman | Ketiga halaman ikut berubah |
| 13 | Ganti foto utama dari galeri | Foto terpotong 16:9, kecil; tampil juga di HP anggota lain setelah sinkron |
| 14 | Pilih warna teks yang mirip latar | Peringatan "Teksnya susah dibaca" + Perbaiki otomatis berfungsi |
| 15 | Admin menekan Terbitkan saat masih ada isian belum lengkap | Layar Kelengkapan muncul; **Tetap terbitkan** melanjutkan alur terbit |
| 16 | Editor menekan Minta diterbitkan | Permintaan terkirim (lihat `alurFiturPublishWebsite.md`) |
| 17 | Unduh ZIP → Simpan ke HP | ZIP tersimpan; dibuka di laptop tampil benar tanpa atribut penanda |
| 18 | Buka website yang sudah ada saat offline (HP ini pemegang kunci terakhir) | Bisa mengedit; status "tersimpan di HP" |
| 19 | Buat website baru saat offline | Pesan butuh internet + Coba lagi |
| 20 | Mode gelap | Semua layar terbaca; pratinjau website tetap memakai warna asli template |

---

## 15. Aturan kualitas

- Semua teks UI di `strings.xml` (Bahasa Indonesia, gaya `revisiFondasiOrgSpace.md` bagian 8); warna dan ukuran lewat token.
- Nilai dari user **tidak pernah** disisipkan sebagai HTML atau CSS mentah: teks lewat `textContent`, warna dan angka divalidasi di HP **dan** server.
- Pekerjaan berat (unduh, ekstrak, olah gambar, sinkron) di background (`AppExecutors` / WorkManager), tidak di main thread.
- Unit test wajib: `CompletenessChecker`, pembuatan `custom.css`, penggantian `local:` → `asset:`, aturan `EditLockManager` (perpanjang, kedaluwarsa, ambil alih), validasi link, validasi isi di server.
- Null-kan ViewBinding di `onDestroyView()`; jangan simpan referensi View/Context Activity di ViewModel.
