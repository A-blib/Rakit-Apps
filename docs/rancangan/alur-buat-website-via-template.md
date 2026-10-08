# Fitur Buat Website via Template (Template Mode)

> **Untuk siapa dokumen ini:** programmer junior atau model AI yang akan membangun fitur ini.
> **Status:** rancangan siap dieksekusi **setelah Aris meminta**. Jangan mulai membangun sebelum ada perintah dari Aris.
> **Terakhir diperbarui:** 8 Oktober 2026

---

## 0. Baca dulu sebelum mulai

1. Baca **`AGENTS.md`** sampai habis. Semua aturan kerja di sana tetap berlaku: kerja per fase, laporan format 13.4, catatan belajar, komentar Bahasa Indonesia, jangan commit rahasia, jangan menebak versi library, token desain bagian 9, dan seterusnya.
2. Dokumen ini **melengkapi** dokumen lain. Jika ada yang bertentangan, ikuti dokumen ini untuk fitur template mode, lalu laporkan perbedaannya ke Aris:
   - `alurFiturPembuatanWibesite.md`: Dashboard Pembuat Website (Beranda, tab Project, tab Template, Profil, tabel `projects`, halaman Editor "Segera hadir").
   - `alurFiturUpload.md`: sisi provider (cara template dibuat, ditandai, dan dipaket).
3. **Jangan menambah library** di luar daftar bagian 11 tanpa bertanya ke Aris.
4. **Tanyakan ke Aris** jika ada instruksi yang ambigu atau bertentangan. Jangan berasumsi.
5. Kerjakan per fase (bagian 13). Setelah satu fase selesai: tes, laporkan, lalu **berhenti dan tunggu konfirmasi**.

---

## 1. Tujuan fitur

Membuat pembuat website (user non-teknis) bisa **membuat website dari template provider**:

1. memilih template,
2. mengganti isi (teks, gambar, link, tombol) dan gaya (warna, sudut, ukuran huruf, tema) **hanya pada bagian yang diizinkan provider**,
3. lalu mengekspornya menjadi **file ZIP berisi HTML/CSS/JS** yang siap dionlinekan.

Semua pekerjaan mengedit berjalan **offline** di HP. Internet hanya dibutuhkan untuk mengunduh paket template pertama kali dan untuk mengirim event statistik.

---

## 2. Alur besar (disetujui Aris)

```
Galeri Template → klik template
        │
        ▼
1. Unduh paket template ke HP (sekali, butuh internet)
        │
        ▼
2. EDITOR TEMPLATE MODE
   ├─ isi teks, ganti foto, atur gaya
   ├─ preview HP / Desktop, pindah halaman
   └─ autosave ke project di HP
        │
        ▼
3. Cek kelengkapan → status Draft / Siap export
        │
        ▼
4. EXPORT → ZIP HTML/CSS/JS → simpan / bagikan
        │
        ▼
5. (Panduan) Cara mengonlinekan website
```

---

## 3. Keputusan yang sudah final

| Keputusan | Isi |
|---|---|
| Masuk ke editor | Klik template di tab Template atau "Template untuk anda" → layar Unduh (bagian 5) → editor. Jika paket sudah ada di HP, layar Unduh dilewati |
| Project baru | Isi template **disalin** menjadi project baru. Template asli di HP tidak pernah diubah |
| Kapan project tersimpan | Baru dibuat di database **setelah perubahan pertama**. Keluar tanpa mengubah apa pun → tidak ada project tersimpan |
| Simpan | **Autosave** setiap ada perubahan (tanpa tombol Simpan) |
| Nama awal project | Sama dengan nama template; jika sudah ada, tambahkan nomor: "Toko Kue (2)". Bisa diganti kapan saja |
| Status project | `draft` → `ready` (Siap export) → `exported`. Project yang diedit setelah export tetap `exported` + teks "Ada perubahan sejak export terakhir" |
| Yang bisa diubah | **Hanya elemen yang ditandai provider**: isi dan gaya yang diizinkan. Tata letak selalu terkunci |
| Gaya | Ditulis ke file `custom.css` yang dimuat paling akhir. File CSS template tidak diubah |
| Offline | Mengedit, preview, cek kelengkapan, dan export berjalan tanpa internet |
| Statistik provider | "Dilihat" dikirim saat layar Unduh/detail dibuka; "Didownload" dikirim saat **export pertama** project berbasis template provider (bagian 10) |

### 3.1 Keputusan default (boleh diubah Aris, laporkan jika berubah)

| No | Pertanyaan | Default yang dibangun |
|---|---|---|
| D1 | Boleh export saat status masih Draft? | **Boleh**, dengan dialog konfirmasi "Masih ada X isian yang belum lengkap. Tetap export?" |
| D2 | Isi dan Gaya digabung atau dipisah? | **Dipisah** menjadi dua tab: **Isi** dan **Gaya** |
| D3 | Perlu layar detail template terpisah? | **Tidak.** Layar Unduh sekaligus menjadi detail singkat template |

---

## 4. Cakupan

### 4.1 Bangun

- Layar Unduh paket (bagian 5)
- Editor template mode: tab Isi, tab Gaya, pemilih halaman, toggle HP/Desktop, preview layar penuh, undo/redo, autosave (bagian 6)
- Ganti gambar: Photo Picker, potong tengah otomatis sesuai rasio, kompres (bagian 6.6)
- Cek kelengkapan dan status project (bagian 7)
- Export ZIP: simpan ke HP dan bagikan (bagian 8)
- Layar selesai + tautan Panduan "Cara mengonlinekan website" (bagian 9)
- Event statistik "Dilihat" dan "Didownload" dengan antrean offline (bagian 10)
- Backend: endpoint detail & unduh paket, endpoint event (bagian 12)
- Mengganti halaman **Editor Template Mode "Segera hadir"** (`alurFiturPembuatanWibesite.md` bagian 6.1) dengan editor sungguhan

### 4.2 Jangan bangun sekarang

- Editor **custom mode** (tetap "Segera hadir")
- **Versi template** (provider memperbarui template; project lama ikut update atau tidak)
- Potong gambar manual (geser/zoom area potong) → butuh library tambahan
- Gaya **warna hover** dan **jenis huruf** (V1+ di `alurFiturUpload.md` bagian 7.11)
- Sinkron project ke cloud, pindah project tamu ke akun
- Publish website menjadi link (hosting oleh app)

Jika sebuah tugas terasa membutuhkan salah satu hal di atas, **berhenti dan tanyakan ke Aris**.

---

## 5. Layar 1: Unduh paket template

```
┌──────────────────────────────┐
│ ←  Template                  │
├──────────────────────────────┤
│ ┌──────────────────────────┐ │
│ │       [thumbnail]        │ │
│ └──────────────────────────┘ │
│ Toko Kue Modern              │
│ oleh Rina Studio             │
│ [UMKM] [3 halaman] [4,2 MB]  │
│ Deskripsi template ...       │
│                              │
│ ┌──────────────────────────┐ │
│ │ Menyiapkan template      │ │
│ │ Diunduh sekali, setelah  │ │
│ │ itu bisa dipakai offline.│ │
│ │ ▓▓▓▓▓▓▓▓░░░░░            │ │
│ │ 2,6 dari 4,2 MB          │ │
│ └──────────────────────────┘ │
│ [ Batal ]                    │
└──────────────────────────────┘
```

| Aturan | Isi |
|---|---|
| Data detail | Dari `GET /api/templates/{id}` (bagian 12): nama, kreator, kategori, deskripsi, kata kunci, info teknis, thumbnail, ukuran paket, versi |
| Event "Dilihat" | Kirim `view` saat layar ini dibuka (bagian 10) |
| Paket sudah ada di HP (versi sama) | Lewati layar ini, langsung buka editor |
| Unduh | Mulai otomatis saat layar dibuka. Tampilkan progres (MB) |
| Batal | Hentikan unduhan, hapus file sementara, kembali ke galeri |
| Data seluler + paket > 10 MB | Dialog sebelum mengunduh: "Template ini 18 MB. Kamu sedang memakai data seluler. Lanjutkan?" |
| Offline | `StateView` error: "Butuh internet untuk mengunduh template pertama kali." + tombol **Coba lagi** |
| Gagal di tengah | Pesan error + **Coba lagi**. Unduhan boleh dimulai ulang dari awal di versi ini |
| Selesai | Ekstrak ke folder template (bagian 11.2), simpan ke tabel `template_packages`, lalu buka editor dengan **project baru yang belum tersimpan** |
| Keamanan ekstrak | Tolak entri ZIP dengan path berbahaya (`../`, path absolut) — cegah *zip slip* |

---

## 6. Layar 2: Editor template mode

### 6.1 Tata letak (tiga zona)

```
┌──────────────────────────────┐
│ ← Toko Kue Modern ✎  ↶ 👁 ⋮  │  ← app bar
│   Draft · tersimpan          │
│ [Beranda ▾]       [HP][Desk] │  ← pemilih halaman + tampilan
├──────────────────────────────┤
│ ┌──────────────────────────┐ │  ← preview (WebView)
│ │ [Dapur Mama Rina]        │ │     elemen aktif disorot
│ │ Kue rumahan, ...         │ │
│ └──────────────────────────┘ │
├──────────────────────────────┤
│           ───                │  ← bottom sheet (bisa ditarik)
│ (Header ✓)(Hero ·2)(About)   │  ← chip section + progres
│ [   Isi   |   Gaya   ]       │  ← tab
│ Nama toko *                  │
│ [ Dapur Mama Rina    ] 15/30 │
│ Deskripsi *                  │
│ [ Kue rumahan, ...   ]       │
│ ⚠ Masih teks contoh          │
│ Foto hero · 16:9             │
│ [ 🖼 Ganti foto ]             │
└──────────────────────────────┘
```

### 6.2 App bar

| Elemen | Fungsi |
|---|---|
| ← | Kembali ke layar asal (galeri / Beranda / tab Project). Autosave sudah berjalan, jadi tidak perlu dialog |
| Nama project ✎ | Ketuk → dialog ganti nama |
| Status + "tersimpan" | Badge status (`DRAFT` / `SIAP EXPORT` / `DIEXPORT`) dan teks kecil "tersimpan" / "menyimpan…" |
| ↶ / ↷ | Undo / redo. Riwayat di memori selama editor terbuka, maks 50 langkah |
| 👁 | Preview layar penuh (tanpa form). Toggle HP/Desktop tetap tersedia; link antar-halaman berfungsi |
| ⋮ | Cek kelengkapan · Export · Kembalikan ke isi template (dengan konfirmasi) · Hapus project (dengan konfirmasi) |

### 6.3 Bagian atas preview

- **Pemilih halaman**: hanya muncul jika template punya lebih dari 1 halaman.
- **Toggle HP / Desktop**: Desktop = WebView dengan lebar virtual 1280px lalu diperkecil (sama dengan sisi provider, `alurFiturUpload.md` bagian 7.4).

### 6.4 Tab Isi

| Aturan | Isi |
|---|---|
| Chip section | Urutan sesuai paket. Tanda `✓` jika semua isian wajib di section itu lengkap, `·N` jika masih ada N isian belum lengkap. Ketuk → form berpindah ke section itu dan preview menggulir ke section tersebut |
| Urutan isian | Sesuai `order` di manifest; jika tidak ada, sesuai urutan di halaman |
| Komponen per jenis | Teks pendek → `TextInputLayout` 1 baris + penghitung; Paragraf → multi-baris + penghitung; Gambar → tombol "Ganti foto" + thumbnail; Link → input URL; Tombol → dua input (teks tombol + link) |
| Label & petunjuk | Dari manifest (`label`, `hint`). Isian wajib diberi tanda `*` |
| Batas | `maxLength` ditegakkan saat mengetik. Rasio gambar ditampilkan, mis. "16:9" |
| Teks/foto contoh | Jika nilai masih sama dengan isi contoh dari provider → tanda kuning "Masih teks contoh" / "Masih foto contoh" |
| Sinkron dua arah | Ketuk isian → elemennya disorot di preview. Ketuk elemen yang bisa diedit di preview → form membuka isian tersebut. Elemen yang tidak bisa diedit tidak bereaksi |
| Isian terhubung | Satu isian dengan beberapa elemen (HP/desktop, lintas halaman) → satu input, semua elemen berubah |
| Validasi link | Hanya `https://`, `http://`, `mailto:`, `tel:`, dan `https://wa.me/...`. Tolak `javascript:` dan skema lain |
| Bantuan WhatsApp | Untuk link/tombol, sediakan pilihan "Nomor WhatsApp" yang otomatis menjadi `https://wa.me/62…` |

### 6.5 Tab Gaya

```
TEMA WEBSITE
Warna utama            [■]
Sudut                  14 px  ───●────
SECTION HERO
Warna judul            [■]
Ukuran judul           40 px  ──●──   (32–48 px)
⚠ Kontras teks rendah
```

| Aturan | Isi |
|---|---|
| Tema website | Tampil paling atas, hanya jika paket punya `theme` (variabel CSS `:root`) |
| Gaya per section | Dikelompokkan per section, hanya gaya yang diizinkan (`styles` di manifest) |
| Komponen | Warna → kotak warna + dialog pemilih warna (palet + input hex); Ukuran huruf & sudut → `Slider` Material dengan rentang dari manifest |
| Peringatan kontras | Jika rasio kontras teks vs latar < 4,5 → teks peringatan kuning. Tidak memblokir |
| Reset per gaya | Tekan lama / ikon ↺ → kembali ke nilai asli template |
| Hasil | Setiap perubahan memperbarui `custom.css` di preview secara langsung (bagian 11.4) |

### 6.6 Ganti gambar

1. Ketuk "Ganti foto" → buka **Photo Picker** Android (`ActivityResultContracts.PickVisualMedia`, hanya gambar).
2. Proses di background (`AppExecutors`):
   - Baca gambar, perbaiki orientasi EXIF.
   - Jika isian punya `aspectRatio` → **potong tengah otomatis** sesuai rasio.
   - Perkecil hingga sisi terpanjang maks **1920 px**.
   - Simpan sebagai **WebP kualitas 80** ke folder gambar project (bagian 11.2), nama `{fieldKey}-{timestamp}.webp`.
   - Hapus file gambar lama milik isian itu (jika ada dan tidak dipakai di undo stack aktif).
3. Tampilkan pratinjau di form dan preview. Selama proses → indikator loading pada isian.
4. Pilihan **"Kembalikan foto contoh"** tersedia di menu isian.

Potong manual (geser area potong) **tidak** dibangun sekarang (bagian 4.2).

### 6.7 Autosave dan pembuatan project

| Aturan | Isi |
|---|---|
| Perubahan pertama | Buat baris di `projects` (bagian 11.1) + folder project, lalu simpan nilai |
| Perubahan berikutnya | Simpan `values.json` dengan **debounce 500 ms** di background; perbarui `updated_at` dan status (bagian 7) |
| Thumbnail project | Diperbarui saat editor ditutup (tangkapan bagian atas preview halaman utama) |
| Gagal simpan | Snackbar "Gagal menyimpan. Coba lagi" + coba ulang otomatis sekali |
| Keluar tanpa perubahan | Tidak ada yang disimpan |

---

## 7. Layar 3: Cek kelengkapan dan status

### 7.1 Aturan kelengkapan

| Kondisi isian | Kelompok |
|---|---|
| Isian **wajib** kosong | **Perlu dilengkapi** |
| Isian **wajib** masih berisi teks/foto contoh provider | **Perlu dilengkapi** |
| Isian **opsional** masih berisi contoh | **Saran** (tidak mempengaruhi status) |
| Link/tombol dengan URL tidak valid | **Perlu dilengkapi** |

- `missing_count` = jumlah "Perlu dilengkapi".
- Status: `missing_count > 0` → `draft`; `= 0` → `ready`. Jika `last_exported_at` ada → tetap `exported` (dengan teks "Ada perubahan sejak export terakhir" jika `updated_at > last_exported_at`).
- Dihitung ulang setiap autosave.

### 7.2 Layar

```
┌──────────────────────────────┐
│ ←  Kelengkapan               │
├──────────────────────────────┤
│ 9/12 isian lengkap           │
│ ▓▓▓▓▓▓▓▓▓░░░   [DRAFT]       │
│ PERLU DILENGKAPI             │
│ ◌ Deskripsi · masih contoh  ›│
│ ◌ Nomor WhatsApp · kosong   ›│
│ ◌ Foto hero · masih contoh  ›│
│ SARAN (opsional)             │
│ ⓘ Galeri foto 3 masih contoh │
├──────────────────────────────┤
│ [ Lengkapi sekarang ]        │
│ [ Export tetap sebagai draft]│
└──────────────────────────────┘
```

| Aturan | Isi |
|---|---|
| Cara membuka | Menu ⋮ → Cek kelengkapan, atau otomatis saat menekan Export ketika status `draft` |
| Ketuk baris | Kembali ke editor, buka halaman + section + isian tersebut |
| Lengkapi sekarang | Lompat ke isian pertama yang belum lengkap |
| Export tetap sebagai draft | Sesuai keputusan D1. Lanjut ke layar Export |
| Semua lengkap | Tampilkan "Semua isian wajib sudah lengkap" + tombol **Export** |

---

## 8. Layar 4: Export

```
┌──────────────────────────────┐
│ ✕  Export website            │
├──────────────────────────────┤
│ [thumb] Dapur Mama Rina      │
│         SIAP EXPORT · 3 hlm  │
│ Nama file                    │
│ [ dapur-mama-rina.zip    ]   │
│ ISI ZIP                      │
│ index.html, tentang.html, …  │
│ css/ · js/ · img/ · vendor/  │
│ custom.css · ± 3,8 MB        │
│ ☑ Kompres foto agar ringan   │
├──────────────────────────────┤
│ [ ⤓ Simpan ke HP ]           │
│ [ ⇪ Bagikan ]                │
└──────────────────────────────┘
```

### 8.1 Proses membuat ZIP (di background)

1. Salin isi paket template (bagian 11.2) ke folder sementara.
2. Untuk setiap halaman HTML, parse dengan **jsoup** (bagian 11.5) lalu terapkan nilai dari `values.json` ke setiap elemen `[data-key]`:
   - Teks / paragraf → isi teks (selalu sebagai **teks biasa**, bukan HTML; cegah penyisipan script).
   - Gambar `<img>` → ganti `src` (dan hapus `srcset`) ke `img/user/{file}.webp`; gambar latar CSS → `style="background-image:url(...)"`.
   - Link → ganti `href`. Tombol → teks + `href`.
3. Salin gambar project ke `img/user/`.
4. Buat `custom.css` dari nilai gaya + tema (bagian 11.4) dan tambahkan `<link rel="stylesheet" href="custom.css">` sebagai **link terakhir** di `<head>` setiap halaman. Lewati jika tidak ada gaya yang diubah.
5. **Bersihkan atribut khusus app** dari HTML: `data-edit`, `data-key`, `data-label`, `data-section`, `data-tpl-id`. Hasil export harus HTML bersih.
6. Hapus file yang tidak dipakai dari paket (mis. `manifest.json`, gambar contoh yang sudah diganti dan tidak dirujuk lagi).
7. Kompres foto jika opsi dicentang (opsi ini aktif secara bawaan).
8. Buat ZIP dengan `java.util.zip`. Struktur di dalam ZIP: `index.html` di folder paling luar.
9. Tampilkan progres. Jika gagal → pesan error + **Coba lagi**.

### 8.2 Tujuan file

| Tombol | Cara kerja |
|---|---|
| **Simpan ke HP** | `ACTION_CREATE_DOCUMENT` (pemilih lokasi Android, berfungsi di semua versi Android 8+). Nama bawaan = nama file di form |
| **Bagikan** | Simpan ZIP di cache app, bagikan lewat `FileProvider` + `ACTION_SEND` (WhatsApp, Drive, email, dll.) |

### 8.3 Setelah ZIP berhasil

- Set `status = exported`, `last_exported_at = sekarang`.
- Jika ini **export pertama** dan project punya `source_template_id` → kirim event `download` (bagian 10).
- Buka layar Selesai (bagian 9).

### 8.4 Nama file

Dari nama project: huruf kecil, spasi → tanda hubung, hanya `a–z 0–9 -`, ditambah `.zip`. Mis. "Dapur Mama Rina" → `dapur-mama-rina.zip`.

---

## 9. Layar 5: Selesai + Panduan

```
┌──────────────────────────────┐
│          ✓ Website siap      │
│ dapur-mama-rina.zip tersimpan│
│ di folder yang kamu pilih.   │
│ ┌──────────────────────────┐ │
│ │ LANGKAH BERIKUTNYA       │ │
│ │ Online-kan websitemu,    │ │
│ │ gratis                   │ │
│ │ [ Buka panduan → ]       │ │
│ └──────────────────────────┘ │
│ [ Bagikan ZIP ]              │
│ [ Kembali ke project ]       │
└──────────────────────────────┘
```

- **Buka panduan** → artikel Panduan "Cara mengonlinekan file ZIP" (konten statis di app, `alurFiturPembuatanWibesite.md` bagian 3.4). Isi minimal: Netlify Drop dan GitHub Pages, langkah sederhana bergambar.
- **Kembali ke project** → kembali ke editor.

---

## 10. Event statistik untuk provider

| Event | Kapan dikirim | Body `POST /api/templates/{id}/events` |
|---|---|---|
| `view` | Layar Unduh/detail template dibuka | `{ type: "view", installId, occurredAt }` |
| `download` | Export **pertama** dari project yang `source_template_id`-nya template ini | `{ type: "download", projectId, installId, occurredAt }` |

- `installId` = UUID acak yang dibuat sekali per instalasi app, disimpan di `SessionStore`. Tidak terhubung ke identitas siapa pun.
- Jika offline, simpan event di antrean lokal (tabel `pending_events`) dan kirim dengan **WorkManager** saat ada koneksi.
- Event dikirim juga untuk tamu (tanpa login). Jika login, sertakan JWT seperti biasa.
- Server mengabaikan event ganda (`projectId` + `type` sama) dan event dari pemilik template (sesuai `alurUntukProvider.md` bagian 3.5–3.6).

---

## 11. Data dan teknis

### 11.1 Room (di HP)

**`projects`** (sudah dirancang di `alurFiturPembuatanWibesite.md` bagian 4.6; pastikan kolom berikut ada):

| Kolom | Keterangan |
|---|---|
| `id` | UUID string |
| `name` | Nama project |
| `mode` | `template` |
| `source_template_id` | ID template asal |
| `template_version` | Versi paket yang dipakai |
| `status` | `draft` / `ready` / `exported` |
| `missing_count` | Jumlah isian belum lengkap |
| `thumbnail_path`, `content_path` | Lokasi thumbnail dan `values.json` |
| `created_at`, `updated_at`, `last_exported_at` | Waktu |

**`template_packages`** (baru):

| Kolom | Keterangan |
|---|---|
| `template_id` + `version` | Kunci gabungan |
| `name`, `creator_name`, `category` | Untuk tampilan offline |
| `path` | Folder paket di HP |
| `size_bytes` | Ukuran |
| `downloaded_at`, `last_used_at` | Waktu |

Paket yang tidak dipakai project mana pun dan tidak dibuka 30 hari boleh dihapus otomatis.

**`pending_events`** (baru): `id`, `template_id`, `type`, `project_id`, `occurred_at`, `attempts`.

### 11.2 Struktur folder di HP (penyimpanan internal app)

```
files/
├── templates/{templateId}/{version}/     ← paket hasil unduh (TIDAK PERNAH diubah)
│   ├── manifest.json
│   ├── index.html, tentang.html, ...
│   ├── css/ js/ img/ vendor/
└── projects/{projectId}/
    ├── values.json                       ← semua nilai isian, gaya, tema
    ├── thumbnail.webp
    └── images/                           ← gambar pilihan user (WebP)
```

### 11.3 Format data

**`manifest.json`** (dibuat server saat template dipublikasikan; lihat `alurFiturUpload.md` bagian 11):

```json
{
  "templateId": "uuid",
  "version": 1,
  "pages": [ { "file": "index.html", "name": "Beranda" } ],
  "sections": [ { "id": "s2", "page": "index.html", "name": "Hero" } ],
  "theme": [ { "var": "--primary", "label": "Warna utama", "type": "color", "default": "#2563eb" } ],
  "fields": [
    {
      "key": "nama_toko",
      "label": "Nama toko",
      "type": "text",
      "hint": "Tulis nama usahamu",
      "maxLength": 30,
      "required": true,
      "order": 1,
      "sectionId": "s1",
      "sample": "Toko Kue Bu Ani",
      "styles": [
        { "prop": "color" },
        { "prop": "font-size", "min": 32, "max": 48, "unit": "px", "default": 40 }
      ]
    },
    {
      "key": "foto_hero",
      "label": "Foto hero",
      "type": "image",
      "aspectRatio": "16:9",
      "required": true,
      "sectionId": "s2",
      "sample": "img/hero.jpg"
    }
  ]
}
```

- Elemen di HTML paket sudah memiliki `data-key="..."` yang cocok dengan `fields[].key`. Satu `key` bisa ada di beberapa elemen dan beberapa halaman (isian terhubung).
- `sample` = isi contoh provider, dipakai untuk aturan "masih teks/foto contoh".
- `type`: `text`, `paragraph`, `image`, `link`, `button`.

**`values.json`** (milik project):

```json
{
  "fields": {
    "nama_toko": { "text": "Dapur Mama Rina" },
    "foto_hero": { "image": "images/foto_hero-1728370000.webp" },
    "tombol_pesan": { "text": "Pesan via WA", "href": "https://wa.me/62812..." }
  },
  "styles": {
    "nama_toko": { "color": "#1e3a8a", "font-size": 40 }
  },
  "theme": { "--primary": "#16a34a" }
}
```

Isian yang tidak ada di `values.json` = masih memakai isi template.

### 11.4 Membuat `custom.css`

```css
:root { --primary: #16a34a; }
[data-key="nama_toko"] { color: #1e3a8a !important; font-size: 40px !important; }
```

- Di **preview**, selector memakai `[data-key="..."]` dan CSS disuntikkan sebagai `<style id="app-custom-css">`.
- Di **export**, karena atribut `data-key` dihapus (bagian 8.1 langkah 5), beri setiap elemen yang punya gaya sebuah class unik terlebih dahulu (mis. `class="... ws-nama_toko"`), lalu selector di `custom.css` memakai class tersebut.
- Nilai warna wajib divalidasi (hex `#rrggbb`), angka wajib di dalam rentang manifest. Jangan pernah menulis input mentah user ke CSS.

### 11.5 Preview (WebView)

- Tampilkan file paket lewat **`WebViewAssetLoader`** (`androidx.webkit`), bukan `file://`.
- Gambar project (folder `projects/{id}/images/`) juga dilayani lewat path handler `WebViewAssetLoader`.
- Setelah halaman dimuat, suntikkan script milik app yang:
  - menerapkan nilai dari `values.json` ke setiap `[data-key]` (teks lewat `textContent`, bukan `innerHTML`),
  - memasang `<style id="app-custom-css">`,
  - menyorot elemen aktif,
  - melaporkan ketukan pada elemen `[data-key]` ke Java.
- Setiap perubahan di form → panggil `evaluateJavascript` untuk memperbarui elemen terkait saja (tanpa memuat ulang halaman).
- Jembatan JS ↔ Java **sangat terbatas**: hanya menerima `{ type: "tap", key: "..." }`. Validasi di Java.
- Blokir navigasi ke situs luar dan koneksi ke domain di luar daftar CDN terpercaya (sama dengan sisi provider).

### 11.6 Arsitektur Android

| Kelas | Tugas |
|---|---|
| `TemplateDetailFragment` + `TemplateDetailViewModel` | Layar Unduh (bagian 5) |
| `TemplatePackageRepository` | Unduh, ekstrak, cache paket; tabel `template_packages` |
| `TemplateEditorFragment` + `TemplateEditorViewModel` | Editor; ViewModel menyimpan state form, undo stack, status |
| `ProjectRepository` | Tabel `projects`, `values.json`, gambar project |
| `TemplateRenderer` | Script suntik untuk preview; membuat `custom.css` |
| `ImageProcessor` | Potong tengah, perkecil, WebP |
| `CompletenessChecker` | Aturan bagian 7.1 (murni Java, mudah di-unit-test) |
| `ProjectExporter` | Proses ZIP bagian 8.1 |
| `TemplateEventRepository` + `SendEventsWorker` | Event bagian 10 |

Gunakan pola yang sudah ada di `AGENTS.md`: ViewModel + LiveData, `Resource`, `StateView`, `AppExecutors`, Hilt, ViewBinding, Navigation Component.

### 11.7 Library

| Library | Status | Untuk |
|---|---|---|
| Room | Sudah direncanakan (`AGENTS.md` 4.2) | `projects`, `template_packages`, `pending_events` |
| WorkManager | Sudah direncanakan | Kirim event tertunda |
| `androidx.webkit` | **Disetujui Aris** (6 Okt 2026) | `WebViewAssetLoader` |
| Photo Picker (`androidx.activity`) | Bagian dari AndroidX yang sudah ada | Memilih gambar |
| `androidx.exifinterface` | **Tanyakan ke Aris dulu** | Membaca orientasi foto |
| Glide | Sudah direncanakan | Thumbnail di form dan daftar |
| **jsoup** (di Android) | **Tanyakan ke Aris dulu** | Menerapkan nilai ke HTML saat export |
| `java.util.zip` | Bawaan Java | Ekstrak paket, buat ZIP export |

---

## 12. Backend

| Method & path | Auth | Hasil |
|---|---|---|
| `GET /api/templates/{id}` | Publik | Detail template `published`: nama, kreator, kategori, deskripsi, kata kunci, info teknis, thumbnail, `version`, `packageSizeBytes` |
| `GET /api/templates/{id}/package` | Publik | File ZIP paket template versi terbaru (berisi `manifest.json`, HTML dengan `data-key`, library yang sudah disalin). Dukung header `Content-Length` untuk progres |
| `POST /api/templates/{id}/events` | Publik (JWT opsional) | Simpan event `view` / `download` (`alurUntukProvider.md` bagian 3.6) |

- Template berstatus selain `published` → `404 NOT_FOUND`.
- Untuk testing sebelum fitur Upload provider selesai: **seeder demo** (`app.seed.demo-templates=true`, hanya profile `dev`) wajib menyediakan **minimal 2 paket template contoh** lengkap dengan `manifest.json` (1 template satu halaman, 1 template multi-halaman dengan isian terhubung dan tema). Simpan sumber paket contoh di `backend/src/test/resources/template-packages/`.

---

## 13. Fase pengerjaan

| Fase | Kerjakan | Anggap selesai jika |
|---|---|---|
| **T1** Backend paket & event | Endpoint bagian 12, seeder 2 paket contoh, test | Detail + unduh paket + event berhasil lewat Swagger; integration test lulus |
| **T2** Unduh & cache paket | Layar Unduh, `TemplatePackageRepository`, tabel `template_packages`, ekstrak aman | Klik template dari galeri → paket terunduh & terekstrak; klik kedua kali langsung lewat; offline menampilkan error yang benar |
| **T3** Editor: preview & tab Isi | WebView + `WebViewAssetLoader`, script suntik, form per section, sinkron dua arah, pemilih halaman, HP/Desktop, autosave, pembuatan project | Mengubah teks langsung terlihat di preview; project muncul di tab Project setelah perubahan pertama; tutup-buka app nilai tetap ada |
| **T4** Gambar | Photo Picker, `ImageProcessor`, kembalikan foto contoh | Foto terpasang dengan rasio benar, ukuran file kecil (WebP) |
| **T5** Tab Gaya & tema | Kontrol warna & slider, `custom.css` di preview, peringatan kontras, undo/redo | Perubahan gaya langsung terlihat; undo/redo bekerja untuk isi dan gaya |
| **T6** Kelengkapan & status | `CompletenessChecker`, layar Kelengkapan, badge status | Status berubah otomatis sesuai aturan bagian 7.1; unit test semua cabang lulus |
| **T7** Export | `ProjectExporter`, Simpan ke HP, Bagikan, layar Selesai, event `download` + WorkManager | ZIP hasil export dibuka di browser laptop tampil benar, tanpa atribut `data-*`, dengan `custom.css`; event terkirim sekali |
| **T8** Polesan | State kosong/error, dark mode, aksesibilitas, screenshot | Semua skenario bagian 14 berhasil di HP asli |

---

## 14. Skenario uji di HP asli

| No | Skenario | Hasil yang diharapkan |
|---|---|---|
| 1 | Klik template pertama kali (online) | Layar Unduh tampil dengan progres, lalu editor terbuka |
| 2 | Klik template yang sama lagi | Langsung ke editor tanpa mengunduh |
| 3 | Klik template baru saat offline | Pesan "Butuh internet untuk mengunduh template pertama kali" + Coba lagi |
| 4 | Buka editor lalu kembali tanpa mengubah apa pun | Tidak ada project baru di tab Project |
| 5 | Ubah satu teks lalu kembali | Project muncul di tab Project dengan status Draft dan nama sesuai template |
| 6 | Ketuk judul di preview | Form langsung membuka isian judul |
| 7 | Ubah "Nama toko" yang terhubung di 3 halaman | Ketiga halaman ikut berubah |
| 8 | Ketik melebihi batas karakter | Input berhenti di batas; penghitung menunjukkan maks |
| 9 | Ganti foto hero dari galeri | Foto terpotong 16:9, tampil di preview, ukuran kecil |
| 10 | Ubah warna utama di tab Gaya | Seluruh elemen yang memakai `--primary` berubah |
| 11 | Pilih warna teks yang sangat mirip latar | Muncul peringatan kontras, tetap bisa disimpan |
| 12 | Lengkapi semua isian wajib | Status berubah menjadi Siap export |
| 13 | Tekan Export saat masih Draft | Layar Kelengkapan muncul; "Export tetap sebagai draft" tetap bisa dipakai |
| 14 | Export → Simpan ke HP | ZIP tersimpan; dibuka di laptop tampil benar, tanpa `data-*`, ada `custom.css` |
| 15 | Export → Bagikan ke WhatsApp | Menu bagikan Android muncul dan file terkirim |
| 16 | Export pertama saat offline, lalu online | Event `download` terkirim setelah online, hanya sekali |
| 17 | Edit lagi setelah export | Status tetap Diexport + "Ada perubahan sejak export terakhir" |
| 18 | Mode gelap | Semua layar terbaca; preview website tetap memakai warna asli template |
| 19 | Mode pesawat selama mengedit | Mengedit, preview, dan export tetap berjalan |

---

## 15. Aturan kualitas

- Semua teks UI di `strings.xml` (Bahasa Indonesia); warna dan ukuran di `colors.xml` / `dimens.xml`; ikuti token desain `AGENTS.md` bagian 9.
- Nilai dari user **tidak pernah** disisipkan sebagai HTML atau CSS mentah: teks lewat `textContent` / jsoup `text()`, warna dan angka divalidasi.
- Pekerjaan berat (unduh, ekstrak, olah gambar, export) di background lewat `AppExecutors` / WorkManager, tidak di main thread.
- Unit test wajib untuk: `CompletenessChecker`, pembuatan `custom.css`, penamaan file export, penerapan nilai saat export (pakai paket contoh), validasi link.
- Null-kan ViewBinding di `onDestroyView()`; jangan simpan referensi View/Context Activity di ViewModel.
