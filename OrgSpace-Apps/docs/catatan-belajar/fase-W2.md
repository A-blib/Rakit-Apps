# Fase W2: Tab Website & halaman kelola website

Sumber rancangan: [`alur-pembuatan-website.md`](../rancangan/alur-pembuatan-website.md) bagian 4, 5, 10, data dari [`alur-buat-website-via-template.md`](../rancangan/alur-buat-website-via-template.md) 12.1 dan [`alur-publish-website.md`](../rancangan/alur-publish-website.md) 7.2 (keputusan K6, K15–K17, G2, G3).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `backend/.../db/migration/V18__create_projects.sql` | `projects` (isi jsonb + `content_version`), `project_assets`, `project_locks`, `project_content_backups` (skema lengkap T2, dipakai mulai sekarang) |
| `backend/.../db/migration/V19__create_sites.sql` | `sites`, `site_releases`, `publish_requests`, dibuat lebih awal agar status website bisa dihitung |
| `backend/.../project/Project`, `ProjectMode`, `ProjectRepository`, `WebsiteStatus` | Entity & enum website |
| `backend/.../project/WebsiteQueries` | SQL daftar: status, "ada perubahan", kunci anggota lain, jumlah per chip, pencarian aman (`ESCAPE`) |
| `backend/.../project/WebsiteService`, `WebsiteController`, `dto/*` | Daftar, detail, ganti nama, duplikat (nama "(2)" + salin aset), hapus (aturan Pemilik / draft kosong milik sendiri), Minta diterbitkan, daftar & tutup permintaan |
| `backend/.../project/ContentAssets` (+ tes) | Mengganti `asset:{id}` di isi website berapa pun dalamnya |
| `backend/.../config/AppProperties.Sites`, `application.yml`, `.env.example` | `SITES_DOMAIN`, `SITES_URL_SCHEME`, batas website beralamat |
| `backend/.../seed/WebsiteSeeder` | 8 website contoh dengan semua status (OSIS, BEM; Pramuka kosong) |
| `backend/.../project/WebsiteIntegrationTest` | Status & jumlah, kunci, cari/urut/paginasi, hak per peran, duplikat + aset, hapus, permintaan terbit |
| `android/.../data/remote/api/WebsiteApi`, `dto/Website*Dto`, `PublishRequest*Dto`, `ConfirmNameRequestDto` | Panggilan API |
| `android/.../data/model/Website`, `WebsiteStatus`, `WebsitePage`, `WebsiteDetail`, `PublishRequest`, `WebsiteQuery` | Model + aturan filter/urut untuk offline |
| `android/.../data/local/WebsiteEntity`, `WebsiteDao`, `AppDatabase` (v3) | Cache Room tab Website (migrasi otomatis 2 → 3) |
| `android/.../data/mapper/WebsiteMapper`, `data/repository/WebsiteRepository` | Server dulu, cache saat offline; cache dihapus saat keluar akun |
| `android/.../ui/website/WebsitesFragment`, `WebsitesViewModel`, `WebsiteAdapter`, `item_website.xml`, `fragment_websites.xml` | Tab Website (menggantikan tab Project lama) |
| `android/.../ui/website/WebsiteManageFragment`, `WebsiteManageViewModel`, `fragment_website_manage.xml`, `menu_website_manage.xml` | Halaman kelola website |
| `android/.../ui/website/WebsiteUi`, `WebsiteDialogs`, `WebsiteArgs` | Badge status, "Diubah 2 jam lalu oleh Budi", dialog ganti nama/hapus |
| `android/.../ui/workspace/ActivityText`, `strings_website.xml`, ikon baru | Kalimat riwayat aktivitas website & teks/ikon layar |

## Alasan keputusan
- **Tabel `sites`/`site_releases` dibuat di W2, bukan menunggu fase publish** → status (Online, ada perubahan, Dimatikan, Ditangguhkan) dihitung dari tabel ini; tanpa itu tab Website hanya bisa menampilkan "Draft". Antrean bangun dan domain tetap ditunda ke fase P. (Alternatif: kolom status di `projects` → harus diselaraskan manual setiap terbit/rollback, mudah tidak sinkron.)
- **Status dihitung di SQL server** → semua HP anggota melihat status yang sama (rancangan 4.2).
- **Cache hanya untuk daftar tanpa filter** → hasil pencarian tidak menimpa cache; saat offline filter diterapkan di HP oleh `WebsiteQuery` dengan aturan yang sama (dites).
- **Pencarian dikirim setelah jeda 400 ms** → tidak memanggil server setiap huruf.
- **Hapus website pernah terbit wajib ketik nama**; draft cukup konfirmasi → sesuai rancangan 4.3; pembuat boleh menghapus draft kosongnya sendiri (dipakai fitur "hapus otomatis website kosong" di T4).
- **Terbitkan, Ringkasan, Riwayat versi, Domain, Pengaturan situs, Edit tampilan (mode template) menampilkan "Segera hadir"** → isinya milik fase P dan T; navigasinya sudah benar (rancangan 2.3, 5).
- **Tombol Terbitkan & Edit tampilan bertumpuk** → "Terbitkan pertama kali" terlalu panjang untuk setengah layar 360dp (terlihat saat uji HP).
- **Tanpa SwipeRefreshLayout** → library itu tidak ada di daftar yang disetujui; daftar dimuat ulang setiap tab dibuka lagi.

## Cara menjalankan & mengetes
1. Jalankan ulang backend dev → Flyway V18–V19 + `WebsiteSeeder`. Install app.
2. Masuk `dummy5` → tab **Website** di OSIS Smansa: 6 website, chip "Semua (6) · Draft (2) · Online (2) · Ada perubahan (1)", "4 dari 5 website online".
3. Ketuk chip, ketik di pencarian, ganti urutan.
4. Ketuk "Website OSIS 2026" → halaman kelola Editor: tanpa Domain/Pengaturan situs, tombol **Minta diterbitkan** → isi catatan → banner "Permintaan terbit sudah dikirim".
5. Masuk `dummy4` (Admin OSIS): kartu "Pensi 2026" menampilkan gembok "Sedang diedit oleh …". Menu ⋮: Ganti nama, Duplikat (muncul "… (2)").
6. Masuk `dummy3` (Pemilik): menu ⋮ punya **Hapus**; website online meminta ketik nama.
7. Offline: matikan backend → buka tab Website lagi → daftar dari cache + banner kuning.

## Hasil tes
- Backend `./mvnw test`: **lulus, 293 test** (termasuk `WebsiteIntegrationTest` 3, `ContentAssetsTest` 2).
- Android `./gradlew testDebugUnitTest`: **lulus, 113 test** (`WebsiteQueryTest`, `WebsiteMapperTest`, `ActivityTextTest`). `lint`: **0 masalah**. `assembleDebug`: berhasil.
- Uji di HP (12 Oktober 2026), skenario bagian 12:
  - 3: ruang pribadi tanpa website → "Belum ada website di …" + Mulai sekarang ✅
  - 4: OSIS dengan seeder → status beragam, jumlah chip benar ✅
  - 5: pindah ke BEM → daftar berganti ✅; chip Online & pencarian "dies" ✅
  - 7: Editor membuka halaman kelola → tanpa Domain & Pengaturan situs, tombol Minta diterbitkan, permintaan terkirim + banner ✅
  - Admin: menu ⋮ tanpa Hapus, Duplikat membuat "Dies Natalis (2)" ✅
  - 10 (sebagian): backend dimatikan → cache + banner "Kamu sedang offline. Data terakhir disimpan baru saja." ✅
  - 6: kunci anggota lain diuji lewat integration test & API (kartu dummy4 menampilkan "Budi Kurniawan"); belum diuji dengan dua HP.
- Ditemukan & diperbaiki: tombol "Terbitkan pertama kali" terpecah tiga baris → tombol dibuat bertumpuk.

## Konsep yang dipelajari
- **LEFT JOIN untuk status turunan**: website tanpa baris `sites` tetap muncul (kolomnya NULL), dan NULL itu sendiri berarti "draft".
- **`count(*) FILTER (WHERE ...)`**: menghitung beberapa kelompok dalam satu query, untuk jumlah di chip.
- **Ambil `size + 1` baris** untuk tahu masih ada halaman berikutnya tanpa query `count` tambahan.
- **Room AutoMigration**: menambah tabel cukup menaikkan `version` dan menulis `@AutoMigration(from, to)`; Room membandingkan skema di `app/schemas/`.
- **Generation counter** di ViewModel: hasil request lama diabaikan jika user sudah mengganti filter, sehingga daftar tidak "melompat".

## Latihan untuk Aris
1. Lewat SQL, ubah `sites.status` "Website OSIS 2026" menjadi `offline`, lalu buka tab Website: chip Online berkurang dan badge jadi "Dimatikan". Kembalikan lagi.
2. Tambahkan urutan "Nama Z–A" di `WebsiteQueries.Sort` (backend) dan `WebsiteQuery.Sort` (Android), lengkap dengan tes di `WebsiteQueryTest`.
3. Di `WebsiteIntegrationTest`, tambahkan tes bahwa Editor bisa mengganti nama website milik orang lain, tetapi tidak bisa menghapusnya.

## Yang perlu Aris lakukan
- Jalankan ulang backend (VS Code) agar migrasi V18–V19 dan seeder website berjalan. Tidak perlu mengubah `.env` (variabel `SITES_*` punya nilai bawaan).
- Tidak ada perubahan Gradle (tidak perlu Sync Now). Database Room di HP naik ke versi 3 otomatis.

## Rencana fase berikutnya
- **W3 Beranda**: sapaan "Kerja keras {org} layak dilihat banyak orang", Lanjutkan dari server, Mulai, Template untuk {org}, Panduan baru, banner perubahan & permintaan terbit; hapus tombol debug "Isi project contoh".
