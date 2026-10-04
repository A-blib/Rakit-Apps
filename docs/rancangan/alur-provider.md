# Alur untuk Provider

> **Status:** Beranda, Template Anda, dan Profil (versi awal) sudah cukup jelas untuk dijadikan instruksi agent berikutnya. **Upload ditandai "Segera hadir"** karena masih butuh analisa dan diskusi mendalam. Dokumen ini belum untuk dieksekusi agent sampai Aris memintanya.
>
> Terakhir diperbarui: 4 Oktober 2026 (Upload segera hadir; data dummy hanya untuk test otomatis dan seeder demo opsional)

---

## 1. Keputusan yang sudah final

| Keputusan | Isi |
|---|---|
| Provider langsung aktif | User jadi provider segera setelah mengisi form provider dan menyetujui aturan. Tidak ada verifikasi atau persetujuan akun provider oleh admin. |
| Status akun provider | Hanya `active` dan `suspended`. `suspended` dipakai sebagai rem darurat untuk provider yang mengunggah konten berbahaya. |
| Pengecekan template | Tahap awal: **otomatis saja** saat upload, tanpa review manual. Review manual ditambahkan bertahap nanti. |
| Istilah | Karena yang mengecek adalah sistem, pakai "Tidak lolos pengecekan" atau "Perlu perbaikan", bukan "Ditolak". |
| Angka ringkasan | **Aktif**, **Dilihat**, dan **Didownload** (bagian 3.4). |
| Tempat upload & mode tandai | Keduanya di **app Android** (versi awal). Web dashboard untuk laptop bisa menyusul jika dibutuhkan. Detail di bagian 5. |
| Format file template | **ZIP saja** (tanpa RAR) untuk versi awal. Detail di bagian 5.2. |
| Template dengan peringatan | Boleh tayang, tetapi provider **terus diingatkan** sampai peringatannya diperbaiki (bagian 4.8). |
| Notifikasi | Di dalam app (Perlu tindakan + badge tab Beranda) **dan** push notification ke layar HP lewat **Firebase Cloud Messaging** (gratis). Detail di bagian 4.7. |
| Cakupan versi awal Dashboard Provider | Beranda, Template Anda (daftar + filter), dan Profil sederhana dibangun. **Upload = "Segera hadir"** (bagian 5.0). |
| Grafik tren | Tren **Didownload** dari waktu ke waktu dalam bentuk **area chart**, tepat di bawah ringkasan (bagian 3.7). |

---

## 2. Segmen Dashboard Provider

Dashboard Provider memakai **bottom navigation** dengan 4 segmen:

| Segmen | Fungsi singkat | Status diskusi |
|---|---|---|
| **Beranda** | Ringkasan dan hal yang perlu dikerjakan sekarang | Siap (bagian 3 dan 4) |
| **Upload** | Mengunggah template baru atau versi perbaikan | **Segera hadir** di versi awal. Keputusan dasar sudah ada (bagian 5); layout, langkah, dan syarat pengecekan dibahas mendalam nanti |
| **Template Anda** | Daftar template milik provider + filter | Siap untuk versi awal (bagian 6) |
| **Profil** | Profil kreator dan pengaturan | Versi sederhana (bagian 7); detail dibahas nanti |

Prinsip: Beranda hanya berisi **ringkasan** dan **tindakan berikutnya**. Daftar lengkap dan pengelolaan template ada di "Template Anda", supaya kedua segmen tidak tumpang tindih.

---

## 3. Beranda

### 3.1 Isi Beranda (urut dari atas ke bawah)

| No | Bagian | Isi | Tahap |
|---|---|---|---|
| 1 | **Perlu tindakan** | Template yang tidak lolos pengecekan, template tayang dengan peringatan, draft yang belum dikirim, profil yang belum lengkap. Berisi notifikasi dari bagian 4. Disembunyikan jika kosong. | Versi pertama |
| 2 | **Ringkasan angka** | Aktif, Dilihat, Didownload (definisi di bagian 3.4), dengan pilihan periode 7 atau 30 hari | Versi pertama |
| 3 | **Grafik tren download** | Area chart jumlah Didownload per hari, mengikuti periode ringkasan (bagian 3.7) | Versi pertama |
| 4 | **3 template populer** | 3 template teratas berdasarkan jumlah Didownload (bagian 3.8) | Versi pertama |
| 5 | **Panduan menggunakan sistem** | Panduan singkat untuk provider (bagian 3.9) | Versi pertama |
| 6 | **Akses cepat Upload** | Tombol "Upload template baru" (membuka tab Upload "Segera hadir"). "Lanjutkan draft" belum muncul karena belum ada upload (bagian 3.10) | Versi pertama |

Tampilan khusus:

| Bagian | Isi | Tahap |
|---|---|---|
| **Checklist provider baru** | Muncul menggantikan bagian 2–4 jika provider belum punya template: lengkapi profil → baca panduan → upload template pertama (langkah ini berlabel "Segera hadir" selama Upload belum tersedia), dengan progres | Versi pertama |

Bagian yang ditunda ke versi berikutnya:

| Bagian | Isi |
|---|---|
| **Kategori yang dibutuhkan** | Kategori website yang paling banyak dipilih pembuat website (dari data tujuan website saat onboarding), ditampilkan dalam bentuk agregat tanpa data pribadi |
| **Aktivitas terbaru** | Contoh: "Template X tayang", "Website dari template Y didownload 12 kali minggu ini" |
| **Pengumuman** | Pengumuman dari pengelola |

**Status pengecekan tidak ditampilkan di Beranda.** Status setiap template (draft, sedang dicek, tayang, tidak lolos, dinonaktifkan) dilihat di segmen **Template Anda**. Template yang bermasalah tetap muncul di "Perlu tindakan".

### 3.2 Beranda menyesuaikan kondisi provider

| Kondisi | Yang ditampilkan |
|---|---|
| `active`, belum punya template | Checklist provider baru + tombol "Upload template pertama" |
| `active`, sudah punya template | Beranda lengkap (bagian 3.1) |
| `suspended` | Mode provider terkunci, user diarahkan ke mode pembuat website dengan pesan "Mode provider dinonaktifkan. Hubungi admin untuk informasi lebih lanjut." |

### 3.3 Gambaran layar

```
┌──────────────────────────────────┐
│ [Nama App]      MODE PROVIDER (A)│
├──────────────────────────────────┤
│ Halo, Aris Studio                │
│                                  │
│ PERLU TINDAKAN                 2 │
│ ┌──────────────────────────────┐ │
│ │ ✕ Profil Sekolah           → │ │
│ │   TIDAK LOLOS · 2 ERROR      │ │
│ ├──────────────────────────────┤ │
│ │ ! UMKM Kuliner             → │ │
│ │   TAYANG · 3 PERINGATAN      │ │
│ └──────────────────────────────┘ │
│                                  │
│ RINGKASAN             [7 hari ▾] │
│ ┌─────────┬─────────┬─────────┐  │
│ │ 4       │ 312     │ 48      │  │
│ │ Aktif   │ Dilihat │Download │  │
│ └─────────┴─────────┴─────────┘  │
│                                  │
│ TREN DOWNLOAD                    │
│ 12┤               ╭─╮            │
│  8┤      ╭─╮     ╱░░░╲           │
│  4┤ ╭───╱░░░╲___╱░░░░░╲          │
│  0┼──────────────────────        │
│    Sen Sel Rab Kam Jum Sab Min   │
│                                  │
│ TEMPLATE POPULER                 │
│ 1 [▢] Profil Sekolah     30 ↓  → │
│ 2 [▢] UMKM Kuliner       12 ↓  → │
│ 3 [▢] Portofolio Minimal  6 ↓  → │
│                                  │
│ PANDUAN                          │
│ ┌──────────────────────────────┐ │
│ │ Cara menyiapkan template   → │ │
│ │ Menandai bagian yang         │ │
│ │ bisa diedit                → │ │
│ │ Syarat lolos pengecekan    → │ │
│ └──────────────────────────────┘ │
│                                  │
│ [    + Upload template baru    ] │
│ [      Lanjutkan draft (1)     ] │
├──────────────────────────────────┤
│ Beranda² Upload Template  Profil │
└──────────────────────────────────┘
```

Angka kecil di tab Beranda adalah jumlah item di "Perlu tindakan". `[▢]` adalah thumbnail template.

### 3.4 Definisi angka ringkasan

| Angka | Arti | Kapan bertambah |
|---|---|---|
| **Aktif** | Jumlah template milik provider yang sedang tayang di galeri | Kondisi saat ini; tidak terpengaruh pilihan periode. Draft, tidak lolos pengecekan, dan dinonaktifkan tidak dihitung. |
| **Dilihat** | Berapa kali halaman detail template milik provider dibuka pembuat website | Saat pembuat website membuka detail template di galeri |
| **Didownload** | Berapa kali website **jadi** dari template milik provider diexport menjadi file ZIP (HTML, CSS, JS) | Saat pembuat website menekan **Export** pada project yang dibuat dari template provider |

Pilihan periode 7 atau 30 hari hanya mengubah angka Dilihat dan Didownload. Panah perbandingan dengan periode sebelumnya bersifat opsional.

**Alasan memakai "Didownload" (bukan "Dipakai")**: output akhir app adalah file ZIP website jadi. Angka ini menunjukkan berapa banyak website yang benar-benar selesai dibuat dari template provider, sehingga lebih bermakna daripada sekadar template dipilih.

### 3.5 Aturan penghitungan "Didownload" (versi awal)

1. **Hanya project dari template provider yang dihitung.** Project yang dirakit sendiri dari section bawaan, atau dari template bawaan app, tidak menambah angka provider mana pun.
2. **Satu project dihitung satu kali**, yaitu pada export pertamanya. Export ulang setelah revisi tidak menambah angka.
3. **Export oleh pemilik template sendiri tidak dihitung.**
4. **Export terjadi offline**, jadi app menyimpan event di antrean lokal lalu mengirimnya ke server saat online (saat app dibuka berikutnya atau saat koneksi tersedia). Event berisi `template_id`, `project_id`, `install_id`, dan waktu export.
5. **Tamu tetap dihitung** lewat `install_id`, yaitu ID acak per instalasi app yang tidak terhubung ke identitas siapa pun.
6. Event yang belum terkirim (misalnya app dihapus sebelum online) tidak tercatat. Untuk versi awal, selisih kecil ini bisa diterima.

### 3.6 Gambaran data

```
template_events
  id
  template_id
  type          → view / download
  project_id    → hanya untuk download; unik bersama type, agar satu project dihitung sekali
  install_id    → ID acak per instalasi app
  user_id       → boleh kosong (tamu)
  occurred_at   → waktu kejadian di HP
  received_at   → waktu diterima server
```

Endpoint: `POST /api/templates/{id}/events` dengan body `{ type, projectId?, installId, occurredAt }`. Server mengabaikan event ganda (`project_id` + `type` sama) dan event dari pemilik template.

### 3.7 Grafik tren download

| Aspek | Ketentuan |
|---|---|
| Posisi | Tepat di bawah ringkasan angka |
| Data | Jumlah **Didownload** per hari (aturan penghitungan bagian 3.5) |
| Bentuk | **Area chart**: garis + area berwarna di bawah garis. Satu mode saja. |
| Periode | Mengikuti pilihan periode ringkasan: 7 hari (7 titik) atau 30 hari (30 titik) |
| Sumbu | X = tanggal (7 hari: nama hari; 30 hari: tanggal), Y = jumlah download, dimulai dari 0 |
| Interaksi | Ketuk titik untuk melihat tanggal dan jumlah download hari itu |
| Gaya | Garis warna `color_foreground`, area warna yang sama dengan gradasi memudar ke bawah, grid horizontal tipis `color_border`, label sumbu Geist Mono, tanpa bayangan. Mengikuti mode terang dan gelap. |
| Kondisi kosong | Jika belum ada download di periode itu, tampilkan garis datar di 0 dengan teks "Belum ada download di periode ini." |
| Hari tanpa download | Tetap ditampilkan sebagai titik bernilai 0, bukan dilewati, agar bentuk tren jujur |

Catatan:
- Candlestick tidak dipakai. Candlestick dirancang untuk data harga (open, high, low, close per periode), sedangkan download per hari hanya satu angka, sehingga area chart lebih tepat dan lebih mudah dibaca provider.
- Library usulan: **MPAndroidChart** (`LineChart` dengan `setDrawFilled(true)`), karena mendukung Java + XML Views. Library ini sudah lama tidak diperbarui, jadi kompatibilitasnya perlu dicek saat implementasi. Tambahkan ke daftar tech stack saat fitur provider masuk instruksi agent.
- Endpoint usulan: `GET /api/providers/me/stats/downloads?period=7d|30d` → `[{ date, count }]`, dengan hari tanpa download tetap dikirim bernilai 0.

### 3.8 Tiga template populer

| Aspek | Ketentuan |
|---|---|
| Posisi | Di bawah grafik tren download |
| Urutan | 3 template dengan jumlah **Didownload** terbanyak pada periode yang dipilih di ringkasan (7 atau 30 hari). Jika jumlahnya sama, yang lebih banyak Dilihat diurutkan lebih dulu. |
| Isi tiap baris | Nomor peringkat, thumbnail, nama template, jumlah Didownload pada periode itu |
| Interaksi | Ketuk baris untuk membuka detail template di segmen Template Anda |
| Kurang dari 3 | Tampilkan yang ada saja |
| Belum ada download | Sembunyikan bagian ini, atau tampilkan teks "Belum ada template yang didownload di periode ini." |
| Yang dihitung | Hanya template berstatus tayang |

### 3.9 Panduan menggunakan sistem

| Aspek | Ketentuan |
|---|---|
| Posisi | Di bawah 3 template populer |
| Bentuk | Daftar kartu panduan singkat. Ketuk untuk membuka halaman panduan lengkap. |
| Isi awal | 1. Cara menyiapkan template (struktur folder dan cara membuat ZIP di Windows, Ubuntu, dan Mac) · 2. Menandai bagian yang bisa diedit · 3. Syarat lolos pengecekan (error vs peringatan) · 4. Memperbaiki template yang tidak lolos · 5. Membaca angka Dilihat dan Didownload |
| Sumber konten | Versi awal: disimpan di dalam app (teks di resource). Nanti bisa diambil dari server supaya bisa diperbarui tanpa update app. |
| Penanda | Kartu yang belum pernah dibuka diberi titik kecil "baru" |

Isi detail tiap panduan ditulis setelah segmen Upload selesai dibahas, karena sebagian besar panduan bergantung pada aturan upload dan pengecekan.

### 3.10 Akses cepat Upload

| Aspek | Ketentuan |
|---|---|
| Posisi | Paling bawah Beranda, sebelum bottom navigation |
| Tombol utama | **"Upload template baru"** → membuka segmen Upload |
| Tombol kedua | **"Lanjutkan draft (n)"** → muncul hanya jika ada draft; membuka draft terakhir, atau daftar draft jika lebih dari satu |
| Gaya | Tombol utama terisi `color_foreground`, tombol kedua outlined |

Istilah yang dipakai adalah **template**, bukan "project", karena "project" sudah dipakai untuk website milik pembuat website.

---

## 4. Sub-fitur: notifikasi template yang tidak memenuhi syarat

### 4.1 Kapan notifikasi muncul

| Situasi | Contoh |
|---|---|
| Template baru tidak lolos pengecekan | Provider upload, sistem menemukan error, template tidak ditayangkan |
| Hasil pengecekan selesai saat provider sudah pindah layar | Pengecekan berjalan di background, provider sudah keluar dari layar Upload |
| Template yang sudah tayang menjadi tidak memenuhi syarat | Aturan pengecekan diperketat lalu template lama dicek ulang, atau nanti ada laporan dari user |
| Template dinonaktifkan | Pengelola menurunkan template karena bermasalah |

Pengecekan dijalankan **di background di server**. Saat upload, status template menjadi "Sedang dicek", lalu hasilnya dikirim sebagai notifikasi.

### 4.2 Tingkat masalah

| Tingkat | Akibat | Contoh |
|---|---|---|
| **Error** | Template **tidak tayang** sampai diperbaiki | `index.html` tidak ada, ukuran melebihi batas, jenis file tidak diizinkan, tidak ada bagian yang bisa diedit, `data-key` ganda, script dari domain luar |
| **Peringatan** | Template **tetap tayang**, tetapi disarankan diperbaiki | Gambar terlalu besar, gambar tanpa teks alternatif, tidak ada meta viewport |

Setiap masalah wajib memiliki: **pesan yang jelas**, **letak masalah** (file dan baris jika ada), dan **saran cara memperbaiki**. Daftar syarat lengkap ditetapkan saat membahas segmen Upload.

### 4.3 Status template

| Status | Arti |
|---|---|
| `draft` | Disimpan, belum dikirim untuk dicek |
| `checking` | Sedang dicek sistem |
| `published` | Lolos pengecekan, tayang di galeri (boleh ada peringatan) |
| `check_failed` | Tidak lolos pengecekan, tidak tayang |
| `disabled` | Dinonaktifkan pengelola |

### 4.4 Siklus

```
Upload → Sedang dicek ─┬─ lolos ─────────────→ Tayang + aktivitas "Template tayang"
                       ├─ lolos + peringatan ─→ Tayang + kartu peringatan di "Perlu tindakan"
                       └─ ada error ──────────→ Tidak tayang + kartu error di "Perlu tindakan"

Upload versi perbaikan → dicek ulang → jika lolos, kartu lama hilang otomatis
```

Kartu di "Perlu tindakan" hilang dengan sendirinya saat masalahnya beres. Provider tidak perlu menghapusnya manual.

### 4.5 Layar detail hasil pengecekan

Dibuka saat kartu di "Perlu tindakan" ditekan:

```
Profil Sekolah                    v2
TIDAK LOLOS PENGECEKAN · 4 Okt 11.40

ERROR (2)
✕ index.html tidak ditemukan
  Letakkan index.html di folder paling
  atas ZIP, bukan di dalam subfolder.

✕ Script dari domain luar
  assets/main.js baris 12 memuat
  https://contoh.com/x.js
  Simpan script di dalam template.

PERINGATAN (1)
! Gambar tanpa teks alternatif
  index.html baris 40: <img> tanpa alt

[  Upload versi perbaikan  ]
```

### 4.6 Gambaran data backend

```
template_checks        → id, template_id, versi, status (running/passed/failed), selesai_pada
template_check_issues  → id, check_id, tingkat (error/warning), kode, pesan, file, baris, saran
notifications          → id, user_id, jenis, template_id, judul, isi, dibaca_pada, selesai_pada, dibuat_pada
```

Jenis notifikasi awal: `TEMPLATE_CHECK_FAILED`, `TEMPLATE_CHECK_WARNING`, `TEMPLATE_PUBLISHED`, `TEMPLATE_DISABLED`.

Endpoint yang dibutuhkan:

| Method & path | Fungsi |
|---|---|
| `GET /api/notifications` | Daftar notifikasi milik user |
| `GET /api/notifications/unread-count` | Jumlah untuk badge tab Beranda |
| `PATCH /api/notifications/{id}/read` | Tandai sudah dibaca |
| `GET /api/templates/{id}/checks/latest` | Hasil pengecekan terakhir untuk layar detail |

### 4.7 Push notification (Firebase Cloud Messaging)

Selain di dalam app, kejadian penting dikirim sebagai **push notification** yang muncul di layar HP (bilah notifikasi, pop-up, layar kunci) walaupun app sedang ditutup, seperti notifikasi YouTube atau Gmail.

**Kejadian yang dikirim sebagai push**

| Kejadian | Push ke layar HP | Tetap tercatat di dalam app |
|---|---|---|
| Template tidak lolos pengecekan | Ya | Ya |
| Template lolos dan tayang | Ya | Ya |
| Template dinonaktifkan pengelola | Ya | Ya |
| Template tayang dengan peringatan | Ya, sebagai **pengingat berkala** (bagian 4.8), bukan saat hasil cek keluar | Ya |
| Ringkasan statistik | Tidak (versi awal) | – |

Jika user menolak izin notifikasi, semua informasi tetap tersedia di dalam app.

**Aturan**

| Aspek | Ketentuan |
|---|---|
| Biaya | FCM gratis di semua paket Firebase. Tidak memakai produk Firebase lain. |
| Izin (Android 13+) | Minta izin `POST_NOTIFICATIONS` secara kontekstual, yaitu setelah provider pertama kali upload template, dengan teks: "Aktifkan notifikasi supaya kamu tahu saat templatemu selesai dicek?" Jangan minta saat app pertama kali dibuka. |
| Notification channel | `Hasil pengecekan template` (prioritas tinggi, boleh pop-up). Channel lain ditambahkan saat dibutuhkan. |
| Saat ditekan | Membuka app langsung ke layar detail hasil pengecekan template terkait (bagian 4.5) |
| Frekuensi | Hanya kejadian di tabel di atas, satu push per kejadian, tanpa push promosi |
| Token perangkat | App mengirim FCM token ke backend setelah login dan setiap kali token berubah. Token dihapus saat user keluar. |

**Kebutuhan teknis**

- Android: project Firebase, file `google-services.json`, library Firebase Messaging, service penerima pesan, notification channel.
- Backend: **Firebase Admin SDK untuk Java** untuk mengirim pesan. Kunci service account disimpan sebagai rahasia (environment variable / file di luar repo), tidak di-commit.
- Tabel `device_tokens`: id, user_id, token (unik), platform, created_at, last_seen_at.
- Endpoint: `POST /api/devices/token` `{ token }` saat login/token berubah, `DELETE /api/devices/token` saat keluar.
- Push hanya bisa sampai ke HP dengan Google Play services.

### 4.8 Pengingat untuk template dengan peringatan

Template yang hanya punya peringatan (tanpa error) **tetap tayang**, tetapi provider terus diingatkan agar tergerak memperbaikinya.

**Di dalam app (selalu tampil sampai diperbaiki)**

| Tempat | Tampilan |
|---|---|
| Beranda → Perlu tindakan | Kartu "TAYANG · n PERINGATAN" tetap ada dan tidak bisa ditutup sampai peringatannya beres |
| Template Anda | Badge peringatan pada kartu template |
| Detail template | Daftar peringatan + tombol "Upload versi perbaikan" |

**Push notification berkala (channel "Pengingat template", prioritas normal, tanpa pop-up)**

| Kapan | Isi contoh |
|---|---|
| 3 hari setelah tayang | "Profil Sekolah masih punya 3 peringatan. Perbaiki supaya template lebih nyaman dipakai." |
| 10 hari setelah tayang | "Gambar hero di Profil Sekolah 4,2 MB. Gambar yang lebih ringan membuat website lebih cepat dibuka." |
| Setiap 30 hari setelahnya | Pengingat singkat jumlah peringatan yang tersisa |

Aturan pengingat:
- Satu push pengingat **menggabungkan semua template** bermasalah milik provider, bukan satu push per template, supaya tidak menumpuk.
- Pengingat **berhenti otomatis** begitu template diperbaiki (versi baru lolos tanpa peringatan) atau template dinonaktifkan.
- Provider bisa mematikan channel "Pengingat template" dari pengaturan HP; kartu di dalam app tetap ada.
- Isi pengingat menyebut **dampak** perbaikannya bagi pemakai template (lebih cepat dibuka, lebih rapi di HP), bukan sekadar "ada peringatan", agar provider terdorong memperbaiki.

**Ide pendorong tambahan (versi berikutnya)**: template tanpa peringatan mendapat badge kecil "Rapi" di galeri, sehingga provider punya alasan positif untuk memperbaiki templatenya.

Kebutuhan teknis: tugas terjadwal di backend (misalnya `@Scheduled` di Spring Boot, sekali sehari) yang mencari template tayang dengan peringatan, menghitung jadwal pengingat dari tanggal tayang, lalu mengirim push lewat FCM. Catat pengingat terakhir per provider agar tidak terkirim ganda.

---

## 5. Upload

### 5.0 Versi awal: "Segera hadir"

Fitur Upload butuh analisa dan diskusi mendalam, jadi pada versi awal Dashboard Provider:

| Tempat | Perilaku |
|---|---|
| Tab **Upload** | Layar "Segera hadir": badge SEGERA HADIR, judul "Upload template", Lottie, teks "Fitur upload template segera hadir. Sambil menunggu, pelajari cara menyiapkan template di Panduan.", tombol "Buka panduan" |
| Beranda → tombol "Upload template baru" | Membuka tab Upload (layar "Segera hadir") |
| Beranda → "Lanjutkan draft" | Tidak muncul (belum ada draft) |
| Checklist provider baru | Langkah "Upload template pertama" berlabel "Segera hadir" |
| Template Anda (kondisi kosong) | Menyebut bahwa upload segera hadir (bagian 6.4) |

Konsekuensi: selama Upload belum ada, provider belum punya template, sehingga Beranda dan Template Anda tampil dalam **kondisi kosong** secara bawaan.

Meski kosong, seluruh bagian yang menampilkan data (ringkasan, grafik tren, 3 template populer, Perlu tindakan, daftar dan filter Template Anda, detail template) **dibangun lengkap seperti mesin siap pakai**, sehingga langsung berfungsi begitu Upload tersedia.

**Data dummy untuk pengujian** dipakai di dua tempat:

| Tempat | Fungsi | Aturan |
|---|---|---|
| **Test otomatis** | Memastikan perhitungan ringkasan, data grafik per hari (termasuk hari bernilai 0), urutan template populer, filter, urutan, dan pencarian bekerja benar | Data uji hanya hidup di dalam test (backend: Testcontainers; Android: unit test ViewModel/mapper). Wajib ada. |
| **Seeder demo (opsional)** | Melihat langsung di HP bahwa grafik, template populer, dan daftar tampil benar | Hanya di profile `dev`, **mati secara bawaan**, dinyalakan manual dengan pengaturan `app.seed.demo-templates=true`. Mengisi beberapa template dummy milik satu akun provider demo dengan berbagai status, hasil pengecekan (error dan peringatan), serta event dilihat/didownload tersebar di 30 hari terakhir. Sediakan juga cara menghapusnya kembali (misalnya perintah atau endpoint khusus dev) agar tampilan bisa kembali kosong. |

Akun provider biasa tetap kosong walaupun seeder demo menyala, karena data dummy hanya dimiliki akun provider demo.

Bagian 5.1–5.4 di bawah adalah keputusan dasar yang sudah disepakati untuk dipakai saat Upload dibangun nanti.


### 5.1 Tempat upload dan mode tandai

| Aspek | Keputusan |
|---|---|
| Upload | Dari **app Android** |
| Mode tandai | Di **app Android**, memakai WebView |
| Web dashboard laptop | Tidak dibuat di versi awal. Bisa menyusul jika provider membutuhkan |
| Syarat HP | Sama dengan syarat app: Android 8.0 ke atas. Tidak butuh HP khusus (misalnya touch sampling rate tinggi); kenyamanan dicapai lewat desain layar. |

### 5.2 Cara memasukkan file

- Provider memindahkan template dari laptop ke HP lewat cara apa pun yang ia suka: **Google Drive**, **kirim ke WhatsApp sendiri**, atau **kabel USB**.
- App memakai **pemilih file bawaan Android** (satu tombol "Pilih file ZIP"). Pemilih ini sudah bisa membuka folder Download, folder WhatsApp, file hasil transfer USB, dan **Google Drive secara langsung** tanpa perlu mengunduh dulu.
- Pemilih file disaring agar hanya menampilkan file ZIP.

**Format: ZIP saja.**

| Alasan | Penjelasan |
|---|---|
| Mudah dibuat | Windows, Ubuntu, Mac, dan HP bisa membuat ZIP tanpa software tambahan. Membuat RAR wajib memakai WinRAR. |
| Bisa diekstrak di HP | ZIP bisa diekstrak di Android dengan fitur bawaan Java. RAR tidak punya dukungan bawaan, dan library Java yang ada tidak mendukung format RAR5. |
| Mode tandai butuh ekstrak di HP | Template diekstrak di HP untuk ditampilkan di WebView sebelum diupload. |

Bantuan untuk user yang terbiasa RAR:
- Teks bantuan di layar upload: "Upload template dalam format ZIP. Cara membuat ZIP di Windows: klik kanan folder → Kirim ke → Folder terkompresi (zip)."
- Jika file RAR dipilih: "Format RAR belum didukung. Simpan ulang template sebagai ZIP." + tombol **"Lihat caranya"** yang membuka panduan.
- Cara membuat ZIP masuk ke Panduan di Beranda (bagian 3.9).
- Versi berikutnya (jika banyak yang meminta): RAR diekstrak di server lalu dikirim kembali sebagai ZIP. Konsekuensinya, upload RAR wajib online sebelum mode tandai.

### 5.3 Cara kerja mode tandai (gambaran teknis)

```
1. Provider memilih file ZIP
2. App mengekstrak ZIP ke folder sementara di HP
3. WebView membuka index.html → website tampil utuh
4. App menyisipkan script "mode tandai":
   - elemen yang bisa dipilih (judul, paragraf, gambar, tombol) diberi garis tebal
   - saat diketuk, script mengirim info elemen ke kode Java
5. Java menampilkan bottom sheet: Bisa diedit? / Jenis: Teks atau Gambar / Nama untuk user
6. Semua tandaan disimpan sebagai data
7. Upload ZIP + data tandaan ke server → server menyisipkan data-edit, data-key, data-label → pengecekan otomatis
```

Kenyamanan di layar HP:
- Preview bisa di-zoom dan di-scroll
- Tombol **Tampilan HP / Tampilan desktop**. Tampilan desktop adalah **simulasi di dalam HP**: WebView memakai lebar layar sekitar 1280px sehingga website tampil versi desktop, lalu diperkecil agar muat. Efek hover tidak bisa dicoba. Fitur "Buka preview di laptop" lewat link bisa menyusul di versi berikutnya.
- Elemen yang bisa dipilih diberi garis tebal; elemen yang sudah ditandai diberi warna berbeda dan label namanya
- Daftar tandaan di bawah preview untuk mengecek dan mengubah semua tandaan

Batasan:
- Elemen yang dibuat oleh JavaScript tidak bisa ditandai karena tidak ada di file HTML aslinya
- Template yang memuat font atau script dari internet butuh koneksi saat preview
- JavaScript milik provider ikut berjalan di WebView, jadi jembatan antara script dan Java harus dibuat aman agar script template tidak bisa memanggil kode app

### 5.4 Draft (usulan, dikonfirmasi saat merancang layout Upload)

- Draft dibuat setelah file ZIP berhasil terupload, lalu langkah berikutnya tersimpan otomatis.
- Draft disimpan di server agar bisa dilanjutkan dari HP lain dan tidak hilang jika app dihapus.
- Maksimal 5 draft per provider; draft yang tidak disentuh 30 hari dihapus otomatis dengan pemberitahuan sebelumnya.
- Template yang tidak lolos pengecekan bukan draft; muncul di "Perlu tindakan" dengan tombol "Upload versi perbaikan".
- Tombol "Lanjutkan draft" di Beranda: tidak muncul jika 0 draft, langsung membuka draft jika 1, membuka daftar draft jika lebih dari 1.

---

## 6. Template Anda (versi awal)

### 6.1 Isi

Daftar semua template milik provider, dengan filter. Fitur pengelolaan (edit info, versi, menonaktifkan sendiri) dibahas nanti.

### 6.2 Kartu template di daftar

| Elemen | Isi |
|---|---|
| Thumbnail | Gambar sampul template |
| Nama | Nama template |
| Badge status | TAYANG / SEDANG DICEK / TIDAK LOLOS / DRAFT / DINONAKTIFKAN (status bagian 4.3) |
| Badge peringatan | "n PERINGATAN" jika tayang dengan peringatan |
| Kategori | Misalnya Sekolah, UMKM |
| Angka | Dilihat dan Didownload (total sejak tayang) |
| Waktu | "Diperbarui 2 hari lalu" |

Ketuk kartu → **detail template sederhana**: thumbnail besar, nama, kategori, status, angka Dilihat dan Didownload, daftar error/peringatan dari hasil pengecekan terakhir (bagian 4.5). Tombol aksi (upload versi perbaikan, edit, nonaktifkan) belum ada di versi awal.

### 6.3 Filter, urutan, dan pencarian

| Kontrol | Pilihan |
|---|---|
| Filter status (chip, satu pilihan) | Semua · Tayang · Perlu perbaikan (tidak lolos + tayang dengan peringatan) · Sedang dicek · Draft · Dinonaktifkan. Setiap chip menampilkan jumlahnya, misalnya "Tayang (4)". |
| Filter kategori (dropdown) | Semua kategori · Sekolah · Organisasi · Usaha / UMKM · Instansi · Pribadi / Portofolio · Lainnya |
| Urutkan | Terbaru diperbarui (bawaan) · Download terbanyak · Dilihat terbanyak · Nama A–Z |
| Cari | Kolom pencarian berdasarkan nama template |

Filter terakhir yang dipilih diingat selama app terbuka. Kartu "Perlu tindakan" di Beranda yang ditekan membuka detail template; tombol "Lihat semua" (opsional) membuka Template Anda dengan filter "Perlu perbaikan".

### 6.4 Kondisi khusus

| Kondisi | Tampilan |
|---|---|
| Belum punya template | Lottie + "Belum ada template. Fitur upload segera hadir." + tombol "Buka panduan" |
| Filter tidak menemukan apa pun | "Tidak ada template dengan filter ini." + tombol "Hapus filter" |
| Gagal memuat (offline) | "Tidak ada koneksi. Periksa internet lalu coba lagi." + tombol coba lagi |
| Memuat | Kartu kerangka (skeleton) sesuai gaya monokrom |

### 6.5 Gambaran layar

```
┌──────────────────────────────────┐
│ Template Anda                    │
├──────────────────────────────────┤
│ [ Cari nama template...      🔍 ]│
│ (Semua 6)(Tayang 4)(Perlu        │
│  perbaikan 2)(Draft 1) →         │
│ Kategori [Semua ▾] Urut [Terbaru▾]│
│                                  │
│ ┌──────────────────────────────┐ │
│ │[▢] Profil Sekolah            │ │
│ │    TAYANG · 3 PERINGATAN     │ │
│ │    Sekolah · 180 dilihat ·   │ │
│ │    30 download · 2 hari lalu │ │
│ ├──────────────────────────────┤ │
│ │[▢] UMKM Kuliner              │ │
│ │    TAYANG                    │ │
│ │    UMKM · 90 dilihat ·       │ │
│ │    12 download · 5 hari lalu │ │
│ ├──────────────────────────────┤ │
│ │[▢] Landing Event             │ │
│ │    TIDAK LOLOS · 2 ERROR     │ │
│ │    Lainnya · 1 minggu lalu   │ │
│ └──────────────────────────────┘ │
├──────────────────────────────────┤
│ Beranda  Upload  Template  Profil│
└──────────────────────────────────┘
```

### 6.6 Kebutuhan backend

- Tabel `templates` (minimal): id, provider_id, nama, kategori, thumbnail_url, status, jumlah_peringatan, created_at, updated_at, published_at.
- Endpoint: `GET /api/providers/me/templates?status=&category=&sort=&q=&page=` → daftar + jumlah per status untuk chip.
- Endpoint: `GET /api/providers/me/templates/{id}` → detail + hasil pengecekan terakhir.
- Pakai paginasi (misalnya 20 per halaman) dan RecyclerView dengan pemuatan lanjutan saat scroll.

---

## 7. Profil (versi awal sederhana)

Detail dibahas lebih lanjut nanti. Versi awal cukup:

| Bagian | Isi |
|---|---|
| Kepala profil | Avatar (dari Google/GitHub atau inisial), nama kreator, bio, link portofolio, chip keahlian |
| Tombol "Edit profil" | Form yang sama dengan form provider saat onboarding: nama kreator (wajib), bio (maks 300), link portofolio (URL valid), keahlian |
| Ringkasan singkat | Jumlah template tayang dan total download |
| Akun | Email, metode login terhubung (membuka layar yang sudah ada di Pengaturan) |
| Aksi | Beralih ke mode pembuat website · Pengaturan · Keluar |

Endpoint: `GET /api/providers/me/profile`, `PATCH /api/providers/me/profile`.

Yang ditunda untuk diskusi berikutnya: tampilan profil publik kreator di galeri, foto sampul, tautan sosial media, statistik lengkap.

---

## 8. Keputusan yang masih terbuka

Tidak ada untuk saat ini.

---

## 9. Yang perlu dibahas berikutnya

> **Belum didiskusikan secara matang.** Jangan membangun apa pun dari daftar di bawah ini. Kerjakan dulu yang sudah jelas di dokumen ini (Beranda, notifikasi, Template Anda versi awal, Profil versi awal, dan layar "Segera hadir" untuk Upload). Bagian ini baru dikerjakan setelah Aris selesai mendiskusikannya.

1. **Upload** (diskusi mendalam): layout dan urutan langkah, mode tandai, daftar syarat pengecekan otomatis (termasuk aturan JavaScript), draft (bagian 5.4), versi template
2. **Template Anda lanjutan**: edit info, upload versi perbaikan, menonaktifkan template sendiri, riwayat versi
3. **Profil lanjutan**: profil publik kreator di galeri dan pengaturan khusus provider
