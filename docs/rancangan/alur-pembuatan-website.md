# Alur Fitur Pembuatan Website (Dashboard Pembuat Website)

> **Status:** instruksi untuk agent, **revisi besar 12 Oktober 2026** mengikuti keputusan `penyempurnaanSystem.md` (K4–K24). Versi sebelumnya (4 Oktober 2026) dianggap usang.
> **Kerjakan setelah** fase R1–R4 di `revisiFondasiOrgSpace.md` selesai (organisasi, peran, desain, bahasa).
>
> **Dokumen terkait:**
> - `revisiFondasiOrgSpace.md`: ruang kerja, peran, onboarding, desain, bahasa
> - `alurFiturBuatWibeSiteviatemplate.md`: editor mode **Pakai template**, data project di cloud, kunci edit
> - `alurFiturPublishWebsite.md`: Terbitkan, Riwayat versi, Domain, Pengaturan situs, QR code
> - `penyempurnaanSystem.md` bagian 5.5: rancangan mode **Rakit sendiri** (dokumen instruksinya dibuat terpisah nanti)

---

## 0. Baca dulu sebelum mulai

1. Semua aturan kerja `AGENTS.md` bagian 3 tetap berlaku (per fase, laporan 13.4, catatan belajar, komentar Bahasa Indonesia, tanpa rahasia, versi library dari sumber resmi).
2. Desain dan teks mengikuti `revisiFondasiOrgSpace.md` bagian 7–8 (palet Biru Semangat, Plus Jakarta Sans, bahasa "kamu").
3. Dokumen ini mengatur **kerangka dashboard**: menu bawah, Beranda, tab Website, halaman kelola website (kerangkanya), galeri Template, tab Profil, layar Organisasi. Isi detail layar editor dan publish ada di dokumen terkait.

---

## 1. Keputusan yang sudah final

| Keputusan | Isi |
|---|---|
| Ruang kerja | Semua website berada di dalam **ruang kerja** (organisasi atau ruang pribadi). Dashboard selalu menampilkan isi ruang kerja aktif. Pemilih ruang kerja di app bar (K6) |
| Dua cara membuat website | **Pakai template** (mulai dari template kreator) dan **Rakit sendiri** (menyusun dari section bawaan OrgSpace, K19–K20). Istilah lama "Custom" diganti "Rakit sendiri" |
| Satu format, satu mesin | Kedua cara memakai format data project yang sama dan **dibangun oleh server** saat diterbitkan (K21). Tampilan tools editornya berbeda |
| Mode tidak bisa dipindah | Website dari template tidak bisa diubah ke Rakit sendiri, dan sebaliknya (K20) |
| Menu bawah | `Beranda · Website · [ + ] · Template · Profil`. Tab **Project** diganti **Website** (K16) |
| Kartu website | Ketuk kartu di tab Website → **halaman kelola website** (bagian 5). Kartu "Lanjutkan" di Beranda → langsung ke editor |
| Status website | **Draft** (belum pernah terbit), **Online**, **Online · ada perubahan belum diterbitkan**, **Dimatikan sementara**, **Ditangguhkan** (bagian 4.2). Status lama `ready`/`exported` dihapus |
| Penyimpanan | Autosave di HP (cache) lalu disinkron ke cloud (K17). Detail di `alurFiturBuatWibeSiteviatemplate.md` |
| Batas | Website **online** maks **5 per ruang kerja** (organisasi maupun pribadi, K21/K24; nilai konfigurasi). Website draft tidak dibatasi jumlahnya, tetapi total penyimpanan gambar per ruang kerja dibatasi (nilai konfigurasi; angka final ditetapkan bersama T6) |
| Nama awal | Pakai template: **nama template**. Rakit sendiri: **"Website {org}"** (mis. "Website OSIS Smansa") |
| Kalimat sapaan | Beranda: **"Kerja keras {org} layak dilihat banyak orang"** (K23) |
| Kategori | Kategori organisasi dari `GET /api/meta/categories` (`revisiFondasiOrgSpace.md` bagian 6). Kategori lama (Sekolah, UMKM, Instansi, Pribadi) dihapus |
| Panduan | Section Panduan tetap ada di Beranda; isinya diganti (bagian 3.4) |

### 1.1 Keputusan default (boleh diubah Aris; laporkan jika berubah)

| No | Hal | Default yang dibangun |
|---|---|---|
| G1 | Tamu (belum masuk) | Boleh melihat Beranda versi tamu, galeri Template, dan layar detail template. **Membuat atau menyimpan website butuh masuk** dan punya ruang kerja, karena website disimpan di cloud (K17). Saat tamu menekan "Pakai template" / "Rakit sendiri", tampilkan layar Masuk, lalu onboarding, lalu kembali ke aksi semula |
| G2 | Urutan default tab Website | Terakhir diubah |
| G3 | Hak Editor di tab Website | Melihat semua website, membuka editor, membuat draft baru. Tidak melihat tombol Hapus, Domain, Pengaturan situs (`revisiFondasiOrgSpace.md` 3.1) |

---

## 2. Kerangka dashboard

### 2.1 App bar

```
┌──────────────────────────────────┐
│ [logo] OSIS Smansa ▾        (A)  │   ← pemilih ruang kerja · avatar
└──────────────────────────────────┘
```

- **Pemilih ruang kerja**: logo + nama singkat + ikon panah bawah. Ketuk → bottom sheet:
  - daftar ruang kerja saya (logo, nama, badge peran: Pemilik / Admin / Editor), tanda centang di yang aktif;
  - **Kelola organisasi** (membuka layar Organisasi, bagian 7) — tidak tampil di ruang pribadi;
  - **+ Buat organisasi baru**, **Gabung organisasi**, **Buat ruang pribadi** (hanya jika belum punya).
- Pindah ruang kerja memanggil `PATCH /api/users/me/last-organization` dan memuat ulang semua tab.
- Tamu: app bar menampilkan "OrgSpace" + tombol **Masuk**.
- Mode provider (Ruang kreator) **tidak** memakai pemilih ruang kerja.

### 2.2 Menu bawah

```
Beranda · Website · [ + ] · Template · Profil
```

| Menu | Fungsi |
|---|---|
| **Beranda** | Sapaan, lanjutkan website, mulai, template untuk organisasi, panduan |
| **Website** | Daftar website di ruang kerja aktif (draft & online) |
| **[ + ]** | Tombol menonjol: bottom sheet **Pakai template** / **Rakit sendiri** |
| **Template** | Galeri template |
| **Profil** | Akun pribadi (bukan profil organisasi) |

**Gaya tombol +:** bulat/pil berisi `color_primary`, ikon `add` (Material Symbols Rounded) warna `color_on_primary`, sedikit lebih besar dan sedikit naik dari baris menu bawah, tanpa bayangan.

### 2.3 Alur masuk ke editor

```
Tombol + / kartu Mulai
├─ Pakai template ─→ tab Template ─→ detail template ─→ [Pakai template] ─→ EDITOR TEMPLATE (website baru)
└─ Rakit sendiri ─→ pilih tema dasar ─→ EDITOR RAKIT SENDIRI (website baru)

Kartu "Lanjutkan" di Beranda ─→ editor sesuai mode website
Kartu di tab Website ─→ HALAMAN KELOLA WEBSITE ─→ [Edit tampilan] ─→ editor sesuai mode
```

- Kolom `mode` website (`template` / `assembled`) menentukan editor yang dibuka.
- Jika website sedang **dikunci** anggota lain, editor terbuka dalam **mode baca** dengan banner "Sedang diedit oleh {nama}" (aturan kunci di `alurFiturBuatWibeSiteviatemplate.md`).
- Editor Rakit sendiri: sampai dokumen instruksinya dibuat, layar editornya tetap **"Segera hadir"** (bagian 9). Tombolnya tetap tampil agar alur navigasi sudah benar.

---

## 3. Beranda

### 3.1 Isi Beranda (urut dari atas)

| No | Bagian | Isi |
|---|---|---|
| 1 | **Sapaan** | Judul **"Kerja keras {org} layak dilihat banyak orang"**, subjudul "Tunjukkan kegiatan dan karya kalian lewat website sendiri.", tombol **Mulai sekarang** (membuka bottom sheet +). Tampil besar jika ruang kerja belum punya website; jika sudah punya, diganti baris kecil **"Hai, {nama depan}"** (tanpa emoji) |
| 2 | **Lanjutkan** | 1 kartu besar (website terakhir diubah di ruang kerja ini) + maks 3 kartu kecil yang bisa digeser + "Lihat semua →" ke tab Website. Kartu menampilkan status dan, jika ada, "Sedang diedit oleh {nama}". Disembunyikan jika belum ada website |
| 3 | **Mulai** | Dua kartu: **Pakai template** dan **Rakit sendiri**. Besar jika belum ada website; satu baris jika sudah ada |
| 4 | **Template untuk {org}** | 6 template populer di kategori ruang kerja aktif (ruang pribadi: semua kategori). "Lihat semua →" membuka tab Template dengan kategori terpilih |
| 5 | **Panduan** | Artikel singkat statis di app (bagian 3.4) |

### 3.2 Beranda menyesuaikan kondisi

| Kondisi | Yang tampil |
|---|---|
| Tamu | App bar "OrgSpace" + **Masuk**. Sapaan generik: **"Saatnya organisasimu punya rumah online"** + tombol **Mulai sekarang** (→ Masuk). Lalu Template populer + Panduan |
| Ruang kerja belum punya website | Sapaan besar → Mulai → Template untuk {org} → Panduan |
| Ruang kerja sudah punya website | Sapaan kecil → Lanjutkan → Mulai (satu baris) → Template untuk {org} → Panduan |
| Ada website online dengan perubahan belum diterbitkan dan user Admin/Pemilik | Banner kecil di atas: "{n} website punya perubahan yang belum diterbitkan." + tombol **Lihat** |
| Ada permintaan "Minta diterbitkan" dari Editor (Admin/Pemilik) | Banner: "{nama} minta website {nama website} diterbitkan." + tombol **Periksa** (membuka halaman kelola website) |

### 3.3 Gambaran layar

```
┌──────────────────────────────────┐
│ [logo] OSIS Smansa ▾        (A)  │
├──────────────────────────────────┤
│ Hai, Rina                        │
│                                  │
│ LANJUTKAN                        │
│ ┌──────────────────────────────┐ │
│ │ [thumbnail]                  │ │
│ │ Website OSIS 2026            │ │
│ │ ● Online · ada perubahan     │ │
│ │ Diubah 2 jam lalu oleh Budi  │ │
│ └──────────────────────────────┘ │
│ [kecil] [kecil]  →  Lihat semua →│
│                                  │
│ MULAI                            │
│ [Pakai template] [Rakit sendiri] │
│                                  │
│ TEMPLATE UNTUK OSIS SMANSA       │
│ [ ] [ ] [ ]  →     Lihat semua → │
│                                  │
│ PANDUAN                          │
│ Cara menerbitkan website       › │
│ Ajak pengurus lain             › │
├──────────────────────────────────┤
│ Beranda  Website (+) Template Profil │
└──────────────────────────────────┘
```

### 3.4 Panduan (statis di app, bisa dibaca offline)

| Judul | Isi singkat |
|---|---|
| Cara menerbitkan website | Dari editor sampai website online dan dibagikan lewat link/QR code |
| Pakai template atau Rakit sendiri? | Kapan memilih yang mana |
| Ajak pengurus lain | Kode undangan, peran Pemilik/Admin/Editor |
| Saat ganti kepengurusan | Langkah serah terima agar website tetap aman |
| Pakai domain sendiri | Membeli domain (mis. `.my.id`) lalu menghubungkannya |
| Mengonlinekan sendiri file ZIP | Untuk yang ingin hosting di luar OrgSpace (Export ZIP) |

---

## 4. Tab Website

### 4.1 Isi layar (urut dari atas)

1. Kolom **search** (nama website)
2. **Chip filter** + jumlah: `Semua (6) · Draft (2) · Online (3) · Ada perubahan (1)` (Dimatikan dan Ditangguhkan ikut "Semua")
3. **Urutan**: Terakhir diubah (default) · Terbaru dibuat · Nama A–Z
4. **Daftar website**: list satu kolom, thumbnail di kiri, info di kanan
5. Info batas: "3 dari 5 website online" (teks kecil di bawah filter)

### 4.2 Status website

| Status | Arti | Badge |
|---|---|---|
| Draft | Belum pernah diterbitkan | `Draft` (abu-abu, `color_muted`) |
| Online | Terbit dan versi online sama dengan isi editor | `Online` (`color_success`) |
| Online · ada perubahan | Terbit, tetapi isi editor lebih baru dari versi online | `Online` + teks kecil "Ada perubahan belum diterbitkan" (`color_warning`) |
| Dimatikan sementara | Dimatikan oleh Admin/Pemilik | `Dimatikan` (outlined) |
| Ditangguhkan | Ditangguhkan tim OrgSpace | `Ditangguhkan` (`color_error`) |

Status dihitung **server** dari data website + rilis (bukan dihitung di HP), sehingga sama di semua HP anggota.

**Kelengkapan** ("3 isian belum diisi") tetap ditampilkan sebagai info di kartu; aturan hitungnya di `alurFiturBuatWibeSiteviatemplate.md`.

### 4.3 Kartu website

| Isi | Keterangan |
|---|---|
| Thumbnail | Tampilan atas website, diambil app dari pratinjau editor (WebView) lalu diunggah saat sinkron (detail di `alurFiturBuatWibeSiteviatemplate.md`) |
| Nama website | Maks 2 baris |
| Badge status | Bagian 4.2 |
| Label mode | `Template` atau `Rakit sendiri` (style Label) |
| Alamat | Jika pernah terbit: `osis-smansa.{DOMAIN_SITUS}` |
| Waktu | "Diubah 2 jam lalu oleh Budi" |
| Kunci | Jika sedang diedit orang lain: ikon gembok + "Sedang diedit oleh Rina" |
| Info tambahan | "3 isian belum diisi" bila ada |

**Ketuk kartu** → halaman kelola website (bagian 5).

**Menu ⋮ di kartu:** Ganti nama · Duplikat · Export ZIP · Hapus (hanya Pemilik; konfirmasi ketik nama website jika website pernah online).

### 4.4 Kondisi khusus

| Kondisi | Tampilan |
|---|---|
| Belum ada website | Lottie + **"Belum ada website di {org}"** + "Mulai dari template atau rakit sendiri." + tombol **Mulai sekarang** |
| Hasil search/filter kosong | "Tidak ada website yang cocok." + tombol **Hapus filter** |
| Offline | Daftar dari cache (Room) dengan banner "Kamu sedang offline. Data terakhir disimpan {waktu}." Aksi yang butuh internet (Terbitkan, Hapus, Duplikat) menampilkan pesan koneksi |
| Batas website online tercapai | Pesan saat menerbitkan (lihat `alurFiturPublishWebsite.md`) |

### 4.5 Gambaran layar

```
┌──────────────────────────────────┐
│ [logo] OSIS Smansa ▾        (A)  │
├──────────────────────────────────┤
│ Website                          │
│ [ Cari website...            ]   │
│ (Semua 6)(Draft 2)(Online 3)(Ada perubahan 1)
│ 3 dari 5 website online          │
│                                  │
│ ┌──────────────────────────────┐ │
│ │ [thumb] Website OSIS 2026  ⋮ │ │
│ │         Online · Template    │ │
│ │         osis-smansa.….site   │ │
│ │         Ada perubahan belum  │ │
│ │         diterbitkan          │ │
│ └──────────────────────────────┘ │
│ ┌──────────────────────────────┐ │
│ │ [thumb] Pensi 2026         ⋮ │ │
│ │         Draft · Rakit sendiri│ │
│ │         [gembok] Sedang diedit Budi│ │
│ └──────────────────────────────┘ │
└──────────────────────────────────┘
```

---

## 5. Halaman kelola website (kerangka)

Halaman yang terbuka saat kartu di tab Website diketuk. Dokumen ini hanya mengatur **kerangka dan navigasinya**; isi tiap bagian ada di dokumen terkait.

```
┌──────────────────────────────────┐
│ ←  Website OSIS 2026          ⋮  │
├──────────────────────────────────┤
│ [thumbnail besar]                │
│ ● Online   osis-smansa.….site    │
│ [Edit tampilan]   [Terbitkan]    │
├──────────────────────────────────┤
│ Ringkasan                      › │
│ Riwayat versi                  › │
│ Domain                         › │
│ Pengaturan situs               › │
│ Konten          (Segera hadir)   │  ← v2
│ Statistik       (Segera hadir)   │  ← v2
│ Data masuk      (Segera hadir)   │  ← v3
└──────────────────────────────────┘
```

| Bagian | Isi | Tahap | Dokumen |
|---|---|---|---|
| Kepala | Thumbnail, status, alamat, **Edit tampilan**, **Terbitkan** (Admin/Pemilik) atau **Minta diterbitkan** (Editor) | v1 | `alurFiturPublishWebsite.md` |
| Ringkasan | Status, link, Salin, Bagikan, Buka, **QR code**, terakhir diterbitkan oleh siapa | v1 | `alurFiturPublishWebsite.md` |
| Riwayat versi | Daftar versi + siapa yang menerbitkan + Kembalikan | v1 | `alurFiturPublishWebsite.md` |
| Domain | Subdomain, ganti nama, domain sendiri | v1 | `alurFiturPublishWebsite.md` |
| Pengaturan situs | Judul & deskripsi Google, favicon, gambar saat dibagikan, Matikan sementara | v1 | `alurFiturPublishWebsite.md` |
| Konten | Berita, Struktur kepengurusan | v2 | dokumen modul (belum dibuat) |
| Statistik | Pengunjung | v2 | belum dibuat |
| Data masuk | Kiriman formulir | v3 | belum dibuat |

- Untuk website **Draft**, kepala menampilkan "Belum terbit" dan tombol **Terbitkan pertama kali**; Riwayat versi dan Domain menampilkan keadaan kosong yang menjelaskan "Muncul setelah website diterbitkan."
- Bagian yang tidak boleh diakses peran user **tidak ditampilkan** (G3).
- Menu ⋮: Ganti nama · Duplikat · Export ZIP · Hapus.

---

## 6. Galeri Template (tab Template + "Template untuk {org}")

**Sumber data:** backend, tabel `templates` milik sisi provider (`alurUntukProvider.md`), hanya status `published`.

| Aturan | Isi |
|---|---|
| Login | Tidak perlu. Tamu boleh melihat galeri dan detail template |
| Internet | Butuh internet. Offline: `StateView` error dengan teks koneksi (`revisiFondasiOrgSpace.md` 8.2) + **Coba lagi** |
| Ketuk template | Membuka **layar detail/unduh template** (`alurFiturBuatWibeSiteviatemplate.md` bagian 5). Angka "Dilihat" dihitung saat layar ini terbuka |
| Kategori | Dari `GET /api/meta/categories`. Chip: **Semua** + kategori sesuai jenjang ruang kerja aktif (tamu/ruang pribadi: semua kategori) |
| Label isi template | Kartu menampilkan konten khas yang dimiliki template, mis. *"Punya: Struktur · Berita · Kontak"* (K9; data dari `alurFiturUpload.md`) |
| Filter modul | **v2**: chip "Mendukung Berita", "Mendukung Struktur" (K18) |
| Thumbnail kosong | Placeholder ilustrasi bergaya app (biru/kuning), bukan abu-abu monokrom |
| Data dummy | Seeder demo sisi provider (profile `dev`, `app.seed.demo-templates=true`) diperbarui memakai kategori organisasi baru |

**Isi tab Template (urut dari atas):**
1. Search (nama template)
2. Chip kategori
3. Urutan: Populer (default) · Terbaru
4. Daftar template, list satu kolom, paginasi 20, dimuat lanjut saat scroll

**Kartu template:** thumbnail, nama (maks 2 baris), "oleh {nama kreator}", label kategori, label isi template, jumlah dipakai (mis. "120 kali dipakai").

**"Template untuk {org}" di Beranda:**

| Kondisi | Isi |
|---|---|
| Ruang kerja organisasi | 6 template terpopuler di kategori organisasi; jika kurang dari 6, dilengkapi dari semua kategori tanpa duplikat |
| Ruang pribadi / tamu | 6 template terpopuler semua kategori (judul section: "Template populer") |
| Offline / kosong | Section disembunyikan |

**Endpoint:** `GET /api/templates?category=&q=&sort=popular|newest&page=&size=` (publik), kategori memakai kode baru.

**Kondisi khusus:** belum ada template → Lottie + "Belum ada template" + "Template dari kreator akan muncul di sini."; hasil kosong → "Tidak ada template yang cocok." + **Hapus filter**.

---

## 7. Layar Organisasi (dari pemilih ruang kerja → Kelola organisasi)

Isi detail dan aturannya di `revisiFondasiOrgSpace.md` (bagian 3–5, fase R4). Kerangka:

| Menu | Isi | Peran yang melihat |
|---|---|---|
| Profil organisasi | Nama, nama singkat, logo, jenjang, kategori, instansi, kota, periode, alamat, email, WhatsApp, Instagram, TikTok, YouTube. Dipakai untuk isi otomatis template (K10) | Semua (ubah: Admin/Pemilik) |
| Anggota | Daftar, undang (kode + Bagikan), ubah peran, keluarkan | Semua (aksi sesuai peran) |
| Riwayat aktivitas | "Rina menerbitkan Website OSIS 2026 · 2 jam lalu" | Semua |
| Serah terima | Jadikan anggota lain Pemilik | Pemilik |
| Tagihan | **Segera hadir** (menunggu T6) | Admin/Pemilik |
| Keluar dari organisasi | Konfirmasi | Semua |

---

## 8. Tab Profil (akun pribadi)

| Bagian | Isi |
|---|---|
| Kepala | Avatar, nama tampilan (bisa diubah), email, badge mode **Pembuat website** |
| Ruang kerja saya | Daftar organisasi + ruang pribadi, dengan peran; ketuk → pindah ruang kerja |
| Menu | **Beralih ke mode kreator** / **Jadi kreator template** · Pengaturan (metode login terhubung, tema) · Keluar |
| Tamu | Kartu "Kamu belum masuk" + **Masuk**; menu Pengaturan |

- Info "Tujuan website" dan "Nama organisasi" di profil lama **dihapus** (sekarang ada di Profil organisasi).
- Endpoint ubah nama: `PATCH /api/users/me` `{ displayName }` → `UserResponse` (menggantikan `PATCH /api/users/me/creator-profile`).
- `ProfileSheet` dari `AGENTS.md` digantikan tab ini; avatar di app bar membuka tab Profil.

---

## 9. Yang belum dibangun di dokumen ini

| Hal | Status |
|---|---|
| Editor **Rakit sendiri** | Rancangan disetujui (`penyempurnaanSystem.md` 5.5). Layar editornya **"Segera hadir"** sampai dokumen instruksinya dibuat |
| Konten, Statistik (v2), Data masuk (v3) | Ditampilkan "Segera hadir" di halaman kelola website |
| Tagihan | Menunggu keputusan T6 |
| Web dashboard di website utama OrgSpace | Belum dibahas |

**Isi layar "Segera hadir" Editor Rakit sendiri:** app bar (kembali + nama website), badge **Segera hadir** (`color_accent`), judul "Editor Rakit sendiri lagi kami siapkan", teks "Sebentar lagi kamu bisa menyusun website dari section siap pakai: hero, struktur, berita, dan lainnya.", Lottie berwarna, tombol **Kembali** (sekunder). Membuka layar ini **tidak** membuat website baru.

---

## 10. Data dan backend

| Hal | Isi |
|---|---|
| Daftar website | `GET /api/organizations/{id}/websites?status=&q=&sort=&page=` (anggota). Detail data project ada di `alurFiturBuatWibeSiteviatemplate.md` |
| Jumlah per status | Dikirim bersama daftar (`counts`) untuk chip filter |
| Cache | Room menyimpan daftar website per ruang kerja untuk tampilan offline |
| Data contoh | Seeder `dev` membuat 3–6 website contoh (draft, online, online + perubahan, dimatikan) di organisasi dummy. Tombol debug "Isi project contoh" di Android **dihapus** (data sekarang di server) |
| Test | Backend: filter/search/urutan/hitungan status/hak per peran (Testcontainers). Android: unit test ViewModel Beranda, tab Website, pemilih ruang kerja |

---

## 11. Fase pengerjaan

| Fase | Kerjakan | Anggap selesai jika |
|---|---|---|
| **W1** Kerangka dashboard | Menu bawah baru, app bar + pemilih ruang kerja, tab Profil baru, layar Organisasi (menyambung layar R4) | Pindah ruang kerja memuat ulang semua tab; tab Profil menampilkan ruang kerja saya |
| **W2** Tab Website & halaman kelola (kerangka) | Endpoint daftar website, tab Website lengkap (search, filter, urutan, kartu, cache offline), halaman kelola dengan bagian "Segera hadir" untuk yang belum dibangun | Skenario 3–7 bagian 12 berhasil |
| **W3** Beranda | Sapaan, Lanjutkan, Mulai, Template untuk {org}, Panduan, banner perubahan/permintaan terbit | Skenario 1–2, 8 berhasil |
| **W4** Galeri Template | Kategori baru, label isi template, layar detail (menyambung `alurFiturBuatWibeSiteviatemplate.md`) | Skenario 9–10 berhasil |

Setiap fase: perbarui README, tulis catatan belajar `docs/catatan-belajar/fase-WX.md`.

---

## 12. Skenario uji di HP asli

| No | Skenario | Hasil yang diharapkan |
|---|---|---|
| 1 | Tamu membuka app | Sapaan "Saatnya organisasimu punya rumah online", tombol Masuk, Template populer |
| 2 | Tamu menekan Mulai sekarang → Pakai template | Layar Masuk → onboarding → kembali ke galeri Template |
| 3 | Ruang kerja baru tanpa website, buka tab Website | State kosong "Belum ada website di {org}" + Mulai sekarang |
| 4 | Seeder aktif, buka tab Website | Daftar website dengan status beragam; chip filter menampilkan jumlah benar |
| 5 | Pindah ke ruang kerja lain | Daftar website berganti sesuai ruang kerja |
| 6 | Anggota A membuka editor; anggota B membuka tab Website | Kartu di HP B menampilkan "Sedang diedit oleh A" |
| 7 | Editor membuka halaman kelola website | Tidak ada menu Domain dan Pengaturan situs; tombol "Minta diterbitkan" tampil |
| 8 | Editor menekan Minta diterbitkan; Admin membuka Beranda | Banner permintaan tampil di HP Admin |
| 9 | Ruang kerja OSIS (jenjang SMA), buka tab Template | Chip kategori hanya kategori sekolah; "Template untuk OSIS Smansa" berisi kategori OSIS / MPK |
| 10 | Matikan internet, buka tab Website lalu tab Template | Tab Website menampilkan cache + banner offline; tab Template menampilkan pesan koneksi + Coba lagi |
| 11 | Mode gelap | Semua layar dashboard terbaca jelas |
