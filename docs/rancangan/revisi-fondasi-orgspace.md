# Revisi Fondasi: OrgSpace (Organisasi, Onboarding, Desain, Bahasa)

> **Status:** instruksi untuk agent (siap dieksekusi setelah Aris memberi tanda mulai).
> **Dibuat:** 12 Oktober 2026 · **Sumber keputusan:** `penyempurnaanSystem.md` (K4–K24).
> **Hubungan dengan `AGENTS.md`:** `AGENTS.md` sudah selesai dieksekusi (Fase 00–10) dan **tidak diedit**. Dokumen ini adalah **revisi di atasnya**. Jika isi dokumen ini berbeda dengan `AGENTS.md`, **ikuti dokumen ini** untuk bagian yang disebut di tabel bagian 1.

---

## 0. Baca dulu sebelum mulai

1. Baca **`AGENTS.md`** sampai habis. Semua **aturan kerja** di bagian 3 tetap berlaku: kerja per fase, berhenti dan laporkan dengan format 13.4, catatan belajar, komentar Bahasa Indonesia, jangan commit rahasia, jangan menebak versi library, jangan menginstal software sistem, commit kecil.
2. Baca `penyempurnaanSystem.md` bagian 2 (daftar keputusan K1–K24) untuk memahami **alasan** perubahan.
3. **Jangan menambah library** di luar yang disebut di bagian 9 tanpa bertanya ke Aris.
4. Kerjakan fase di bagian 10 berurutan. Setelah satu fase selesai, **berhenti, laporkan, tunggu konfirmasi**.
5. Data produksi **belum ada** (app belum dirilis). Migrasi boleh mengubah atau menghapus kolom lama; data development diisi ulang lewat seeder. **Konfirmasi dulu ke Aris** sebelum menjalankan migrasi yang menghapus kolom.

---

## 1. Ringkasan perubahan terhadap `AGENTS.md`

| Bagian `AGENTS.md` | Perubahan | Dokumen ini |
|---|---|---|
| Judul / nama app | Nama tampilan app menjadi **OrgSpace**. Package `com.aris.templateapp` **tidak diganti** | 2 |
| 1 Produk | Target tunggal: **organisasi pelajar/mahasiswa** di instansi pendidikan. Ada **ruang kerja organisasi** dengan anggota dan peran | 3 |
| 2.2 Jangan bangun | Sebagian dibuka (lihat tabel 1.1) | 1.1 |
| 6.6 Onboarding | Form pembuat website diganti: buat organisasi / gabung organisasi / ruang pribadi | 5 |
| 7.2 V5 `creator_profiles` | Kolom `website_purpose` dan `organization_name` dihapus; digantikan tabel `organizations` | 4.2 |
| 7.3 Endpoint onboarding creator | Body berubah | 4.4, 5 |
| 9 Desain | Gaya monokrom ala nextjs.org **ditinggalkan**. Palet **Biru Semangat**, font **Plus Jakarta Sans**, sudut membulat | 7 |
| Teks UI (`strings.xml`) | Bahasa dekat dengan anak muda organisasi | 8 |
| 4.2 Android "jangan pasang" | Room, WorkManager, Glide boleh dipasang saat fitur yang membutuhkannya dibangun | 9 |

### 1.1 Hal dari `AGENTS.md` 2.2 yang sekarang **boleh** dibangun

| Hal | Dibangun di dokumen |
|---|---|
| Tabel `projects`, penyimpanan project, sinkron | `alurFiturBuatWibeSiteviatemplate.md` (cloud + kunci edit) |
| Room, WorkManager | `alurFiturBuatWibeSiteviatemplate.md` |
| Upload `MultipartFile` | Dokumen ini (logo organisasi, bagian 4.4) dan dokumen fitur yang menyebutnya |
| Galeri template, editor, preview WebView, export | `alurFiturPembuatanWibesite.md`, `alurFiturBuatWibeSiteviatemplate.md` |
| Publish website menjadi link | `alurFiturPublishWebsite.md` |
| Upload template oleh provider | `alurFiturUpload.md` |

Hal yang **tetap tidak dibangun**: panel admin web, pembayaran (T6, menunggu keputusan tim Aris), verifikasi/persetujuan provider oleh admin, lupa password (tetap dialog "Segera hadir").

---

## 2. Identitas app

| Hal | Nilai |
|---|---|
| Nama tampilan (`app_name`) | **OrgSpace** |
| Package / `applicationId` | Tetap `com.aris.templateapp` (mengganti package memutus konfigurasi OAuth Android + SHA-1 dan tidak terlihat user) |
| Nama di app bar | "OrgSpace" (teks, font Plus Jakarta Sans Bold) |
| README | Judul dan deskripsi project diganti menjadi OrgSpace |
| Deep link | Tetap `templateapp://auth/callback` (tidak terlihat user; mengubahnya mengharuskan ganti konfigurasi GitHub OAuth) |

---

## 3. Konsep: ruang kerja

- Setiap pembuat website bekerja di dalam **ruang kerja**.
- Ada dua jenis ruang kerja:
  - **Organisasi** (`type = organization`): banyak anggota, ada peran, ada serah terima.
  - **Ruang pribadi** (`type = personal`): hanya satu orang (pemilik). Menu Anggota dan Serah terima disembunyikan.
- Satu akun boleh menjadi anggota beberapa organisasi dan punya paling banyak **satu** ruang pribadi.
- Website, domain, dan data modul **dimiliki ruang kerja**, bukan user (K6).
- App mengingat **ruang kerja terakhir** yang dibuka (`users.last_organization_id` + cache di `SessionStore`).
- Mode **provider** tidak berubah: template tetap milik kreatornya; ruang kerja tidak dipakai di mode provider.

### 3.1 Peran (K15)

| Aksi | Pemilik | Admin | Editor |
|---|---|---|---|
| Lihat website dan isinya | ✓ | ✓ | ✓ |
| Edit tampilan, simpan draft | ✓ | ✓ | ✓ |
| Kelola Konten (v2) | ✓ | ✓ | ✓ |
| Terbitkan / matikan sementara / kembalikan versi | ✓ | ✓ | — (punya tombol "Minta diterbitkan") |
| Domain, ganti nama subdomain, Pengaturan situs | ✓ | ✓ | — |
| Lihat dan unduh Data masuk (v3) | ✓ | ✓ | — |
| Ubah Profil organisasi | ✓ | ✓ | — |
| Undang anggota, ubah peran Admin/Editor, keluarkan anggota | ✓ | ✓ (tidak bisa mengubah/mengeluarkan Pemilik) | — |
| Hapus website | ✓ | — | — |
| Serah terima (jadikan anggota lain Pemilik) | ✓ | — | — |
| Hapus organisasi | ✓ | — | — |
| Keluar dari organisasi | ✓ (hanya jika masih ada Pemilik lain) | ✓ | ✓ |

- Organisasi **selalu** punya minimal satu Pemilik (`LAST_OWNER`).
- Pengecekan peran dilakukan di **backend** pada setiap endpoint; tampilan Android hanya menyembunyikan tombol.

### 3.2 Keputusan default (boleh diubah Aris; laporkan jika berubah)

| No | Hal | Default yang dibangun |
|---|---|---|
| F1 | Cara mengundang | **Kode undangan** 8 karakter (mis. `OSIS-7K2P`) + tombol **Bagikan** (teks berisi kode dan cara memakainya). Link `https://{DOMAIN_UTAMA}/undangan/{kode}` ditambahkan setelah website utama OrgSpace jadi |
| F2 | Masa berlaku undangan | **7 hari**, bisa dipakai beberapa orang, bisa dicabut kapan saja |
| F3 | Peran default anggota baru | **Editor**; pembuat undangan boleh memilih Admin |
| F4 | Batas anggota per organisasi | **30** (nilai konfigurasi `ORG_MAX_MEMBERS`) |
| F5 | Hapus organisasi | Hanya jika tidak ada website online; data dihapus permanen setelah konfirmasi ketik nama organisasi |

---

## 4. Backend

### 4.1 Konfigurasi baru (`.env.example` + `AppProperties`)

```
ORG_MAX_MEMBERS=30
ORG_INVITE_TTL_DAYS=7
ORG_LOGO_MAX_KB=1024
ORG_LOGO_DIR=./data/org-logos          # development; di VPS mis. /srv/orgspace/org-logos
```

### 4.2 Migrasi Flyway (nomor versi melanjutkan migrasi terakhir)

**`organizations`**

| Kolom | Tipe / aturan |
|---|---|
| `id` | uuid, default `gen_random_uuid()` |
| `type` | `organization` / `personal` (CHECK) |
| `name` | varchar(100), not null |
| `short_name` | varchar(40), null (dipakai untuk `{org}` di kalimat sapaan; jika kosong pakai `name`) |
| `category` | lihat bagian 6 (CHECK); null untuk `personal` |
| `level` | `smp` / `sma` / `smk` / `perguruan_tinggi` (CHECK); null untuk `personal` |
| `institution_name` | varchar(150), null untuk `personal` |
| `city` | varchar(100), null |
| `period_start_year`, `period_end_year` | smallint, null; `end >= start` (CHECK) |
| `logo_url` | text, null |
| `address` | varchar(255), null |
| `email` | varchar(255), null |
| `whatsapp` | varchar(20), null |
| `instagram`, `tiktok`, `youtube` | varchar(100), null (disimpan sebagai username/URL yang sudah dinormalisasi) |
| `created_by` | uuid FK `users` |
| `created_at`, `updated_at` | timestamptz |

Index unik parsial: satu ruang pribadi per user → `UNIQUE (created_by) WHERE type = 'personal'`.

**`organization_members`**

| Kolom | Tipe / aturan |
|---|---|
| `organization_id` | uuid FK, on delete cascade |
| `user_id` | uuid FK, on delete cascade |
| `role` | `owner` / `admin` / `editor` (CHECK) |
| `position_title` | varchar(60), null (jabatan, mis. "Ketua", "Sekretaris") |
| `joined_at` | timestamptz |
| PK | (`organization_id`, `user_id`) |

**`organization_invitations`**

| Kolom | Tipe / aturan |
|---|---|
| `id` | uuid |
| `organization_id` | uuid FK, on delete cascade |
| `code_hash` | SHA-256 dari kode undangan (unik); kode asli **tidak** disimpan |
| `role` | `admin` / `editor` |
| `created_by` | uuid FK `users` |
| `expires_at`, `revoked_at`, `created_at` | timestamptz |

**`activity_logs`**

| Kolom | Tipe / aturan |
|---|---|
| `id` | uuid |
| `organization_id` | uuid FK, on delete cascade |
| `actor_user_id` | uuid FK `users`, null (aksi sistem) |
| `action` | varchar(50), mis. `member.joined`, `member.role_changed`, `org.ownership_transferred`, `site.published` |
| `target_type`, `target_id` | varchar(30), uuid; null jika tidak ada |
| `metadata` | jsonb (`@JdbcTypeCode(SqlTypes.JSON)`) |
| `created_at` | timestamptz; index (`organization_id`, `created_at` desc) |

**Perubahan tabel lama**

- `users`: tambah `last_organization_id` (uuid FK `organizations`, on delete set null).
- `creator_profiles`: hapus kolom `website_purpose` dan `organization_name` (konfirmasi Aris dulu, bagian 0 no. 5). Tabel tetap ada sebagai penanda peran pembuat website.

### 4.3 Seeder (`@Profile("dev")`)

Perbarui `DummyDataSeeder` (hanya jika tabel `organizations` kosong):
- 3 organisasi: OSIS (sma), BEM (perguruan_tinggi), Pramuka (smp), masing-masing 3–5 anggota dengan peran campuran dan profil terisi.
- 2 ruang pribadi.
- 2 undangan aktif dan 1 kedaluwarsa. Tulis **kode undangan aktif** di README bagian akun dummy.
- Beberapa baris `activity_logs`.

### 4.4 Endpoint (semua butuh JWT)

| Method & path | Body | Hasil | Peran minimal |
|---|---|---|---|
| `POST /api/organizations` | `{ type, name, shortName?, category?, level?, institutionName?, city?, periodStartYear?, periodEndYear?, positionTitle? }` | `OrganizationResponse` (201); pembuat menjadi `owner` | – |
| `GET /api/organizations` | – | daftar `{ id, type, name, shortName, logoUrl, myRole }` | anggota |
| `GET /api/organizations/{id}` | – | `OrganizationResponse` | anggota |
| `PATCH /api/organizations/{id}` | kolom profil (bagian 4.2) | `OrganizationResponse` | admin |
| `POST /api/organizations/{id}/logo` | multipart `file` (PNG/JPEG/WebP, ≤ `ORG_LOGO_MAX_KB`) | `OrganizationResponse` | admin |
| `DELETE /api/organizations/{id}` | `{ confirmName }` | 204 | owner |
| `GET /api/organizations/{id}/members` | – | daftar `{ userId, displayName, avatarUrl, role, positionTitle, joinedAt }` | anggota |
| `PATCH /api/organizations/{id}/members/{userId}` | `{ role?, positionTitle? }` | anggota | admin (aturan 3.1) |
| `DELETE /api/organizations/{id}/members/{userId}` | – | 204 | admin (aturan 3.1) |
| `POST /api/organizations/{id}/leave` | – | 204 | anggota |
| `POST /api/organizations/{id}/transfer-ownership` | `{ userId, keepMeAs: "admin" \| "editor" \| "leave" }` | `OrganizationResponse` | owner |
| `POST /api/organizations/{id}/invitations` | `{ role }` | `{ code, expiresAt }` (kode hanya dikirim **sekali**) | admin |
| `GET /api/organizations/{id}/invitations` | – | daftar undangan aktif (tanpa kode) | admin |
| `DELETE /api/organizations/{id}/invitations/{invitationId}` | – | 204 | admin |
| `POST /api/invitations/preview` | `{ code }` | `{ organizationName, logoUrl, role }` | – |
| `POST /api/invitations/accept` | `{ code, positionTitle? }` | `OrganizationResponse` | – |
| `GET /api/organizations/{id}/activities?page=` | – | daftar aktivitas terbaru (20 per halaman) | anggota |
| `PATCH /api/users/me/last-organization` | `{ organizationId }` | 204 | anggota |

`UserResponse` (`AGENTS.md` 7.3) ditambah `lastOrganizationId` dan dihapus `creatorProfile.websitePurpose` / `organizationName`.

**Kode error baru:** `ORG_NOT_FOUND`, `NOT_ORG_MEMBER`, `ROLE_NOT_ALLOWED`, `LAST_OWNER`, `PERSONAL_SPACE_EXISTS`, `CATEGORY_LEVEL_MISMATCH`, `INVITATION_INVALID` (salah/kedaluwarsa/dicabut), `ALREADY_MEMBER`, `ORG_MEMBER_LIMIT`, `ORG_HAS_ONLINE_SITES`, `CONFIRM_NAME_MISMATCH`, `LOGO_INVALID`.

**Aturan:**
- Semua pengecekan peran lewat satu helper, mis. `OrganizationAccess.require(orgId, userId, Role.ADMIN)`, agar konsisten dan mudah dites.
- Kode undangan dibuat acak (alfabet tanpa huruf mirip: tanpa `0/O/1/I`), disimpan sebagai hash; batasi percobaan `preview`/`accept` (mis. 10 kali per menit per user) agar tidak bisa ditebak.
- Logo: periksa tipe dari isi file (bukan nama), ubah ke WebP persegi maks 512 px, simpan di `ORG_LOGO_DIR`. Jangan pernah menyajikan file asli.
- Setiap aksi penting menulis `activity_logs`.

### 4.5 Test

- Unit test `OrganizationAccess` untuk setiap baris tabel 3.1.
- Integration test (Testcontainers): buat organisasi → undang → terima → ubah peran → serah terima → Pemilik lama keluar → `LAST_OWNER` saat Pemilik terakhir mencoba keluar → undangan kedaluwarsa/dicabut ditolak → batas anggota.

---

## 5. Onboarding baru (mengganti `AGENTS.md` 6.6 bagian pembuat website)

```
Pilih peran (tetap: Pembuat website / Kreator template)
└─ Pembuat website
   ├─ "Buat ruang organisasi"  → form organisasi (5.1) → selesai
   ├─ "Gabung organisasi"      → masukkan kode undangan → pratinjau (nama + logo + peran) → isi jabatan (opsional) → selesai
   └─ "Untuk pribadi"          → nama tampilan → ruang pribadi dibuat → selesai
Setelah selesai → Dashboard Pembuat Website di ruang kerja yang baru dibuat/dimasuki
```

- Alur provider (`ProviderFormFragment`) **tidak berubah**.
- `POST /api/users/me/onboarding/creator` sekarang berbody `{ displayName }` dan **ditolak** dengan `ONBOARDING_NEEDS_SPACE` jika user belum menjadi anggota ruang kerja mana pun. Android memanggil endpoint organisasi/undangan dulu, baru endpoint ini.
- User lama yang sudah selesai onboarding tetapi belum punya ruang kerja: saat membuka Dashboard Pembuat Website, tampilkan layar pilihan yang sama (tanpa langkah pilih peran).
- Kalimat di layar pilihan: judul **"Mulai jejak digital organisasimu dari sini"**, subjudul "Pilih cara kamu mulai. Bisa tambah organisasi lain nanti."

### 5.1 Form organisasi

| Kolom | Wajib | Catatan |
|---|---|---|
| Nama organisasi | Ya | mis. "OSIS SMA Negeri 1 Bandung" |
| Nama singkat | Tidak | mis. "OSIS Smansa"; dipakai di kalimat sapaan |
| Jenjang | Ya | Chip: SMP/MTs · SMA/MA · SMK · Perguruan tinggi |
| Kategori | Ya | Chip; pilihan menyesuaikan jenjang (bagian 6) |
| Nama instansi | Ya | mis. "SMA Negeri 1 Bandung" |
| Kota / kabupaten | Tidak | |
| Periode kepengurusan | Tidak | Tahun mulai – tahun selesai |
| Jabatan kamu | Tidak | mis. "Ketua" |
| Logo | Tidak | Bisa diisi nanti di Profil organisasi |

---

## 6. Kategori dan jenjang (sementara, menunggu hasil observasi lapangan Aris)

| Kode `category` | Label | Jenjang sekolah (`smp`, `sma`, `smk`) | `perguruan_tinggi` |
|---|---|---|---|
| `osis_mpk` | OSIS / MPK | ✓ | |
| `bem_dema_dpm` | BEM / DEMA / DPM | | ✓ |
| `himpunan` | Himpunan / HMJ / HMPS | | ✓ |
| `ukm` | UKM | | ✓ |
| `pramuka` | Pramuka | ✓ | ✓ |
| `jurnalistik_media` | Jurnalistik / Media | ✓ | ✓ |
| `pmr_ksr` | PMR / KSR | ✓ | ✓ |
| `paskibra` | Paskibra | ✓ | |
| `kerohanian` | Kerohanian | ✓ | ✓ |
| `seni_olahraga` | Seni & Olahraga | ✓ | ✓ |
| `lainnya` | Ekskul / Komunitas lain | ✓ | ✓ |

- Daftar ini disimpan di **satu tempat** di backend (enum + tabel pemetaan jenjang) dan disajikan lewat `GET /api/meta/categories` agar Android, galeri template, dan upload provider memakai daftar yang sama. Kategori boleh berubah tanpa update app.
- Validasi kombinasi kategori–jenjang di backend (`CATEGORY_LEVEL_MISMATCH`).
- Kategori yang sama dipakai untuk kategori template di galeri dan upload provider (lihat `alurFiturUpload.md`).

---

## 7. Desain (mengganti `AGENTS.md` bagian 9)

### 7.1 Arah

- **Ceria, hangat, dan penuh semangat**, tetapi tetap rapi: spasi lega, hierarki jelas, rata kiri.
- Satu warna utama (biru) + satu aksen hangat (kuning). Latar terang sedikit kebiruan, bukan putih murni.
- Sudut membulat; tombol berbentuk pil.
- Ilustrasi/Lottie **berwarna** (biru + kuning) bernuansa sekolah dan kegiatan organisasi; tidak bernuansa gaming dan tidak kekanak-kanakan.
- Garis tipis tetap dipakai untuk kartu dan pemisah; bayangan tetap tidak dipakai kecuali dialog.
- Aturan lama yang tetap berlaku: semua nilai lewat token di `res/values/` + `values-night/`, area sentuh minimal 48dp, `contentDescription` pada ikon, rapi di lebar 360dp dan mode gelap.
- Aturan lama yang **dihapus**: monokrom, Geist Sans/Mono, label huruf kapital mono, larangan warna di luar makna.

### 7.2 Token warna (kontras teks sudah dicek ≥ 4.5:1)

| Token | Terang | Gelap | Pakai untuk |
|---|---|---|---|
| `color_background` | `#F5F7FF` | `#0E1230` | Latar layar |
| `color_surface` | `#FFFFFF` | `#161B3D` | Kartu, sheet, dialog |
| `color_surface_subtle` | `#EEF1FF` | `#1D2350` | Latar input, area sekunder |
| `color_foreground` | `#1A2150` | `#EEF1FF` | Teks utama |
| `color_muted` | `#55608F` | `#A9B2DA` | Teks sekunder, placeholder |
| `color_border` | `#DDE3FF` | `#2C3366` | Garis kartu, pemisah |
| `color_border_strong` | `#7B89CC` | `#6670B8` | Border input, kartu terpilih |
| `color_primary` | `#3B5BFD` | `#7C93FF` | Tombol utama, elemen aktif, cincin fokus |
| `color_on_primary` | `#FFFFFF` | `#0E1230` | Teks di atas `color_primary` |
| `color_primary_container` | `#E3E9FF` | `#26338F` | Chip terpilih, latar badge mode |
| `color_on_primary_container` | `#1B2A8A` | `#E3E9FF` | Teks di atas `color_primary_container` |
| `color_accent` | `#FFC531` | `#FFD45E` | Badge, sorotan, avatar default |
| `color_on_accent` | `#3D2A00` | `#3D2A00` | Teks di atas `color_accent` |
| `color_link` | `#2F4DE0` | `#8EA2FF` | Link |
| `color_success` | `#16803C` | `#4CC38A` | Status online / berhasil |
| `color_warning` | `#A35200` | `#F5A524` | Peringatan |
| `color_error` | `#C9252D` | `#FF6166` | Error, ditangguhkan |

Pemetaan Material 3 di `themes.xml`: `colorPrimary` = primary, `colorOnPrimary` = on_primary, `colorPrimaryContainer` = primary_container, `colorOnPrimaryContainer` = on_primary_container, `colorSecondary` = accent, `colorOnSecondary` = on_accent, `colorSurface` = surface, `colorOnSurface` = foreground, `colorOutline` = border, `colorError` = error, `android:colorBackground` = background. Surface tint tetap dimatikan.

### 7.3 Tipografi (Plus Jakarta Sans)

- Sumber: Plus Jakarta Sans (Tokotype), lisensi **SIL OFL 1.1**. Unduh dari repositori resmi atau Google Fonts; simpan di `res/font/` dengan nama huruf kecil (`plus_jakarta_sans_regular.ttf`, `_medium`, `_semibold`, `_bold`, `_extrabold`) + font family XML + file `OFL.txt`. Hapus file Geist beserta lisensinya.

| Style | Ukuran | Berat | Jarak huruf |
|---|---|---|---|
| `TextAppearance.App.Display` | 32sp | ExtraBold | -0.01 |
| `TextAppearance.App.Headline` | 24sp | Bold | -0.01 |
| `TextAppearance.App.Title` | 18sp | SemiBold | 0 |
| `TextAppearance.App.Body` | 15sp | Regular | 0 |
| `TextAppearance.App.BodySmall` | 13sp | Regular | 0 |
| `TextAppearance.App.Label` | 12sp | SemiBold (bukan huruf kapital semua) | 0.02 |

### 7.4 Ukuran dan bentuk

| Token | Nilai |
|---|---|
| Grid spasi | kelipatan 4dp: 4, 8, 12, 16, 24, 32, 48 |
| Padding samping layar | 20dp |
| Radius kecil (input, chip) | 12dp |
| Radius tombol | pil (setengah tinggi tombol) |
| Radius kartu, banner | 16dp |
| Radius bottom sheet atas | 24dp |
| Tebal border | 1dp |
| Tinggi tombol | 48dp |
| Elevasi | 0 untuk semua komponen kecuali dialog |

### 7.5 Komponen

| Komponen | Terapkan |
|---|---|
| Tombol utama | Isi `color_primary`, teks `color_on_primary`, pil, teks tidak kapital semua |
| Tombol sekunder | Outlined, border `color_border_strong`, teks `color_primary` |
| Tombol teks / link | `color_link` |
| Input | `TextInputLayout` outlined, latar `color_surface`, border `color_border_strong`, fokus `color_primary`, label di atas |
| Kartu | `MaterialCardView`, latar `color_surface`, stroke 1dp `color_border`, radius 16dp, elevasi 0. Terpilih: stroke 2dp `color_primary` |
| Chip | Outlined; terpilih: isi `color_primary_container`, teks `color_on_primary_container` |
| App bar | Latar `color_background`, elevasi 0, judul "OrgSpace" (Bold) atau nama ruang kerja + ikon panah bawah (pemilih ruang kerja) |
| Badge | Pil; mode/peran: `color_primary_container`; sorotan ("Baru", "Segera hadir"): `color_accent` + `color_on_accent` |
| Banner status | Kotak radius 16dp, ikon + teks warna semantik, latar tint tipis |
| Ikon | Material Symbols **Rounded**, 20–24dp |
| Lottie | Garis + isian berwarna `color_primary` dan `color_accent`; warnai ulang untuk mode gelap lewat dynamic properties |
| Tombol Google & GitHub | Tetap mengikuti pedoman branding resmi masing-masing |
| Katalog komponen (debug) | Perbarui agar menampilkan semua token dan komponen baru di mode terang dan gelap |

---

## 8. Bahasa (mengganti teks UI `AGENTS.md`)

### 8.1 Prinsip

- Sapa dengan **"kamu"**; kalimat pendek dan hangat.
- Sebut **nama organisasi** bila datanya ada (`{org}` = `short_name`, jika kosong `name`).
- Hindari istilah teknis di sisi pembuat website: "deploy", "hosting", "template engine", "provider" (pakai "kreator template").
- Santai tapi **tidak alay**; pesan error tetap jelas dan memberi jalan keluar.
- Hindari ajakan generik seperti "Yuk, bikin website ...".
- Pakai kata **"Terbitkan"**, bukan "Publish", di semua teks UI.

### 8.2 Teks yang diganti

| Tempat | Teks lama | Teks baru |
|---|---|---|
| Intro hal. 1 | Bikin website. Tanpa coding. | **Saatnya organisasimu punya rumah online** · Satu tempat untuk profil, kegiatan, dan kabar terbaru kalian. |
| Intro hal. 2 | (bebas) | **Kerjakan bareng pengurus lain** · Ajak anggota, bagi tugas, dan kelola website dari HP masing-masing. |
| Intro hal. 3 | (bebas) | **Terus berlanjut dari periode ke periode** · Saat ganti kepengurusan, website dan datanya tetap aman. |
| Beranda / Dashboard pembuat website | Buat website pertamamu | **Kerja keras {org} layak dilihat banyak orang** · Tunjukkan kegiatan dan karya kalian lewat website sendiri. Tombol: **Mulai sekarang** |
| Dashboard "Segera hadir" (bila masih dipakai) | Fitur pembuatan website segera hadir. | Lagi kami siapkan. Sebentar lagi kamu bisa mulai! |
| Pilih peran | Kamu mau pakai app ini sebagai apa? | **Kamu datang ke OrgSpace untuk apa?** · Kartu: "Bikin dan kelola website organisasi" / "Bikin template untuk organisasi lain". Teks kecil: "Peran lain bisa ditambah nanti." |
| Error `INVALID_CREDENTIALS` | Email atau password salah. | Email atau password-nya belum cocok. Cek lagi, ya. |
| Error `USE_SOCIAL_LOGIN` | Akun ini terdaftar dengan {metode}... | Akun ini dibuat lewat {metode}. Masuk pakai {metode}, ya. |
| Error `EMAIL_ALREADY_USED` | Email sudah terdaftar. Silakan masuk. | Email ini sudah punya akun. Langsung masuk aja. |
| Tidak ada internet | Tidak ada koneksi. Periksa internet lalu coba lagi. | Koneksi internetmu putus. Sambungkan lagi, lalu ketuk **Coba lagi**. |
| Provider suspended | Mode provider dinonaktifkan. Hubungi admin... | Mode kreator template sedang dinonaktifkan. Hubungi tim OrgSpace untuk info lebih lanjut. |
| Menu profil | Jadi penyedia template | **Jadi kreator template** |
| Menu profil | Beralih ke mode provider | **Beralih ke mode kreator** |
| Dashboard provider | Dashboard provider | **Ruang kreator** |
| Badge | SEGERA HADIR | Segera hadir |

Semua teks tetap di `strings.xml`. Pesan baru di dokumen fitur lain mengikuti prinsip 8.1.

---

## 9. Library

| Library | Status |
|---|---|
| Room, WorkManager, Glide | Boleh dipasang saat fitur yang membutuhkannya dibangun (dicatat di dokumen fitur) |
| Font Plus Jakarta Sans | Aset font (bukan library kode) |
| Material Symbols Rounded | Ikon vektor dari sumber resmi Google |
| Library lain | **Tanyakan ke Aris dulu** |

---

## 10. Fase pengerjaan

| Fase | Kerjakan | Anggap selesai jika |
|---|---|---|
| **R1** Backend organisasi | Migrasi 4.2, `OrganizationAccess`, endpoint 4.4, `GET /api/meta/categories`, seeder 4.3, test 4.5 | Semua test lulus; alur undang → terima → serah terima berhasil lewat Swagger |
| **R2** Desain & bahasa | Token 7.2, font 7.3, bentuk 7.4, komponen 7.5, teks 8.2, nama app, katalog komponen, Lottie berwarna | `./gradlew assembleDebug` dan `lint` berhasil; katalog komponen benar di mode terang & gelap; tidak ada sisa Geist atau warna monokrom lama |
| **R3** Onboarding & ruang kerja Android | Layar pilihan 5, form organisasi 5.1, gabung lewat kode, ruang pribadi, pemilih ruang kerja di app bar, `SessionStore` menyimpan ruang kerja terakhir | Skenario 1–5 bagian 11 berhasil di HP |
| **R4** Profil organisasi & Anggota | Layar Profil organisasi (termasuk unggah logo), daftar Anggota, undang (kode + Bagikan), ubah peran, keluarkan, keluar, serah terima, Riwayat aktivitas dasar | Skenario 6–12 bagian 11 berhasil di HP |

Setiap fase: perbarui README (akun dummy, kode undangan, langkah baru) dan tulis catatan belajar `docs/catatan-belajar/fase-RX.md` (format `AGENTS.md` 13.4).

---

## 11. Skenario uji di HP asli

| No | Skenario | Hasil yang diharapkan |
|---|---|---|
| 1 | Akun baru → Pembuat website → Buat ruang organisasi | Form organisasi tampil; kategori berubah sesuai jenjang; setelah simpan masuk dashboard dengan nama organisasi di app bar |
| 2 | Pilih jenjang SMA lalu kategori BEM (dipaksa lewat API) | Ditolak `CATEGORY_LEVEL_MISMATCH` |
| 3 | Akun baru → Untuk pribadi | Ruang pribadi dibuat; menu Anggota tidak tampil |
| 4 | Buat ruang pribadi kedua lewat API | Ditolak `PERSONAL_SPACE_EXISTS` |
| 5 | Punya 2 ruang kerja, pindah lewat pemilih di app bar, tutup app, buka lagi | App membuka ruang kerja terakhir |
| 6 | Admin membuat undangan → Bagikan | Teks berisi kode dan cara memakainya terbuka di pilihan berbagi (mis. WhatsApp) |
| 7 | Akun lain → Gabung organisasi → masukkan kode | Pratinjau nama + logo + peran tampil; setelah gabung masuk sebagai Editor |
| 8 | Pakai kode yang sudah dicabut / kedaluwarsa | Pesan jelas: "Kode undangan ini sudah tidak berlaku. Minta kode baru ke pengurus, ya." |
| 9 | Editor membuka Profil organisasi | Bisa melihat, tidak bisa mengubah; tombol ubah tidak tampil dan API menolak `ROLE_NOT_ALLOWED` |
| 10 | Pemilik serah terima ke anggota lain lalu keluar | Anggota itu menjadi Pemilik; Riwayat aktivitas mencatat serah terima |
| 11 | Pemilik terakhir mencoba keluar | Ditolak dengan pesan "Jadikan anggota lain Pemilik dulu sebelum keluar." |
| 12 | Unggah logo berukuran besar / file bukan gambar | Logo besar otomatis dikecilkan; file bukan gambar ditolak dengan pesan jelas |
| 13 | Ganti HP ke mode gelap | Semua layar baru dan lama terbaca jelas dengan palet gelap |
