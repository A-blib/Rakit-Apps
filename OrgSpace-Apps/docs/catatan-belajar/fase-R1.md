# Fase R1: Backend ruang kerja organisasi (OrgSpace)

Sumber rancangan: [`docs/rancangan/revisi-fondasi-orgspace.md`](../rancangan/revisi-fondasi-orgspace.md) bagian 3–4 dan 6. Alasan keputusannya ada di [`penyempurnaan-system.md`](../rancangan/penyempurnaan-system.md) (K4–K6, K11–K12, K15).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `AGENTS.md` bagian 19, `docs/rancangan/*` | Salinan dokumen revisi + keputusan awal Aris (arsip kerja Gemini, hapus kolom, library disetujui, branch `fitur/orgspace`) |
| `db/migration/V17__create_organizations.sql` | Tabel `organizations`, `organization_members`, `organization_invitations`, `activity_logs`; kolom `users.last_organization_id`; hapus `creator_profiles.website_purpose` & `organization_name` |
| `organization/OrgCategory`, `OrgLevel` | 11 kategori + 4 jenjang, termasuk aturan kategori mana yang boleh untuk sekolah/kampus |
| `organization/MemberRole`, `OrgAction`, `OrganizationAccess` | Tabel peran 3.1 di satu tempat: `require(orgId, userId, aksi)` + aturan ubah peran/keluarkan/undang/keluar |
| `organization/Organization*` (entity + repository) | Data ruang kerja, anggota (primary key gabungan lewat `@IdClass`), undangan, riwayat |
| `organization/OrganizationService` | Buat (pembuat jadi Pemilik), daftar, profil, ubah profil (kontak dinormalkan), logo, hapus (ketik ulang nama), ruang kerja terakhir |
| `organization/MemberService` | Daftar anggota, ubah peran/jabatan, keluarkan, keluar, serah terima |
| `organization/InvitationService`, `InvitationCodes`, `AttemptLimiter` | Kode `XXXX-XXXX` (hash saja yang disimpan), pratinjau, gabung, cabut, batas 10 percobaan/menit |
| `organization/OrgLogoStorage` | Cek isi file (bukan nama), potong persegi, perkecil maks 512 px, simpan ulang sebagai PNG |
| `organization/SocialLinks` | Link/`@username` Instagram, TikTok, YouTube, dan nomor WhatsApp dinormalkan |
| `organization/OrganizationController`, `InvitationController`, `MetaController` | Endpoint bagian 4.4 + `GET /api/meta/categories` (publik) |
| `user/UserService`, `UserController`, `dto/*` | Onboarding creator kini hanya `{displayName}` dan ditolak `ONBOARDING_NEEDS_SPACE` sebelum punya ruang kerja; `UserResponse` + `lastOrganizationId`, `creatorProfile` dihapus; `PATCH /users/me/last-organization` |
| `common/exception/*` | 14 kode error baru; `ApiException` bisa membawa `fieldErrors`; error multipart dirapikan |
| `seed/OrganizationSeeder` | 3 organisasi, 2 ruang pribadi, 2 kode undangan aktif + 1 kedaluwarsa, beberapa riwayat |
| `config/AppProperties`, `application.yml`, `.env.example` | `app.org`: `ORG_MAX_MEMBERS`, `ORG_INVITE_TTL_DAYS`, `ORG_LOGO_MAX_KB`, `ORG_LOGO_DIR` |
| Test: `OrganizationAccessTest`, `SocialLinksTest`, `OrganizationIntegrationTest`, `DummyDataSeederTest`, `OnboardingIntegrationTest`, `GalleryIntegrationTest` | Test peran per baris tabel, alur lengkap anggota, dan penyesuaian test lama |

## Alasan keputusan
- **Aturan peran sebagai enum `OrgAction` + method statis** → satu tempat untuk semua fitur (publish, konten, data masuk nanti memakai konstanta yang sama) dan bisa dites tanpa database. (Alternatif: `@PreAuthorize` per endpoint → aturan tersebar dan butuh query peran di ekspresi SpEL.)
- **Semua baris anggota dikunci (`PESSIMISTIC_WRITE`) sebelum aturan "minimal satu Pemilik" dan "batas anggota"** → dua request bersamaan (mis. dua Pemilik keluar bersamaan) tidak bisa sama-sama lolos. (Alternatif: hitung tanpa kunci → bisa menghasilkan organisasi tanpa Pemilik.)
- **Kode undangan 8 karakter dari alfabet tanpa 0/O/1/I, disimpan sebagai hash** → mudah dibacakan di grup dan kebocoran database tidak membocorkan kode. Kode salah, dicabut, dan kedaluwarsa dibalas sama (`INVITATION_INVALID`) agar penebak tidak tahu kode mana yang pernah ada. Link `https://.../undangan/{kode}` menyusul setelah website utama ada (F1).
- **Batas percobaan disimpan di memori** → cukup untuk satu server dan tanpa library tambahan. (Alternatif: tabel percobaan atau Redis → berlebihan untuk batas per menit.)
- **Logo disimpan sebagai PNG, bukan WebP** (beda dengan rancangan 4.4) → Java bawaan tidak bisa menulis WebP, dan library tambahan perlu izin. PNG 512 px tetap kecil. Gambar selalu dibaca lalu ditulis ulang, jadi file asli tidak pernah disajikan.
- **`PATCH /organizations/{id}` mengirim ulang semua isian** → sama dengan form edit lain di app; mengosongkan TikTok cukup dengan mengirim teks kosong. (Alternatif: null = tidak berubah → butuh cara khusus untuk mengosongkan.)
- **Media sosial disimpan sebagai username** → template nanti tinggal menyusun link (`https://instagram.com/{username}`), dan isian "@osis" maupun link lengkap menghasilkan data yang sama.
- **Pengecekan `ORG_HAS_ONLINE_SITES` saat hapus organisasi belum ada** → tabel website baru dibuat di fase publish; kodenya sudah didaftarkan dan pengecekannya ditambahkan di fase itu.
- **`WebsitePurpose` masih dipakai kategori template** → kategori galeri/upload diganti kategori organisasi di fase W4 bersama revisi Upload, agar satu fase tidak mengubah terlalu banyak.

## Cara menjalankan & mengetes
1. Bersihkan sisa migrasi kerja Gemini di database dev (lihat "Yang perlu Aris lakukan"), lalu jalankan backend dev seperti biasa. Flyway menjalankan V17, lalu `OrganizationSeeder` mengisi ruang kerja dummy.
2. Swagger UI → `POST /api/auth/login` dengan `dummy1@templateapp.test` / `password123` → klik **Authorize** dan tempel `accessToken`.
3. `GET /api/meta/categories` → 4 jenjang, 11 kategori.
4. `POST /api/invitations/preview` `{"code":"OSIS-7K2P"}` → nama OSIS + peran Editor. Lalu `POST /api/invitations/accept`.
5. `POST /api/users/me/onboarding/creator` `{"displayName":"Budi"}` → berhasil karena sudah punya ruang kerja.
6. Login `dummy3` (Pemilik OSIS) → `POST /api/organizations/{id}/transfer-ownership` ke dummy4 dengan `keepMeAs: "leave"` → `GET /api/organizations/{id}/activities` dari akun dummy4 mencatat serah terima.
7. Coba `POST /api/organizations/{id}/leave` sebagai Pemilik terakhir → `409 LAST_OWNER`.

## Hasil tes
- `./mvnw test`: **lulus, 288 test** (37 baru: 34 baris tabel peran/aturan anggota, 4 normalisasi kontak & kode, 10 integration organisasi, 1 seeder organisasi; test onboarding & galeri disesuaikan).
- Uji manual Swagger: menunggu database dev dibersihkan (lihat di bawah).

## Konsep yang dipelajari
- **Primary key gabungan (`@IdClass`)**: satu baris anggota dikenali dari pasangan (organisasi, user), bukan ID sendiri. JPA butuh class kunci (`OrganizationMember.Key`) yang berisi dua field yang sama.
- **Penguncian baris (`@Lock(PESSIMISTIC_WRITE)`)**: database menahan request kedua sampai transaksi pertama selesai, sehingga pengecekan "masih ada Pemilik lain?" tidak bisa disalip.
- **Index unik parsial**: `UNIQUE (created_by) WHERE type = 'personal'` hanya berlaku untuk ruang pribadi, jadi satu user boleh membuat banyak organisasi tetapi satu ruang pribadi.
- **Hash untuk kode rahasia**: yang dicocokkan adalah hash kode yang diketik, sama seperti password dan refresh token.
- **Rate limit sliding window**: simpan waktu percobaan terakhir, buang yang lebih tua dari 1 menit, tolak jika sisa ≥ 10.

## Latihan untuk Aris
1. Ubah batas anggota menjadi 5 lewat `.env` (`ORG_MAX_MEMBERS=5`), restart backend, lalu lihat `maxMembers` di `GET /api/organizations/{id}`.
2. Di `OrgCategory`, ubah Paskibra agar boleh untuk kampus. Jalankan `./mvnw test -Dtest=OrganizationAccessTest` dan `GET /api/meta/categories`: apa yang berubah?
3. Tambahkan baris test di `SocialLinksTest` untuk link Instagram dengan `m.instagram.com`. Lulus?

## Yang perlu Aris lakukan
- Backend dev di terminalmu (dijalankan 11 Okt, berisi kode Fase P2 Gemini) gagal restart otomatis setelah kode R1 dikompilasi, karena database dev sudah berisi migrasi V17 lama (`create sites`). Tabel itu kosong. Hentikan backend itu (Ctrl+C), lalu bersihkan (hanya database development):
  ```bash
  sudo -u postgres psql -d templateapp -c "DROP TABLE site_domains, site_releases, sites CASCADE; DELETE FROM flyway_schema_history WHERE version = '17';"
  ```
  Perintah pertama menghapus 3 tabel publish lama (kodenya tersimpan di branch `arsip/publish-p2-gemini`), perintah kedua menghapus catatan migrasinya agar V17 baru bisa berjalan.
- Tambahkan isian `ORG_*` dari `.env.example` ke `backend/.env` (opsional; ada nilai bawaan).

## Rencana fase berikutnya
- **R2**: desain Biru Semangat, font Plus Jakarta Sans, bentuk membulat, teks baru, nama app OrgSpace, katalog komponen, Lottie berwarna.
