# Fase P-revisi: terbitkan website ke orgspace.app/{nama-website}

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `backend/.../config/AppProperties.java` | Properti baru `app.sites.hosting-url` (`SITES_HOSTING_URL`); `urlOf(slug)` = `{hosting-url}/{slug}` |
| `backend/.../site/SitePublishService.java` | `checkSlug()` untuk saran/cek nama website; `findTargetOrg()` dipakai bersama publish & cek; slug otomatis tidak pernah berakhir strip |
| `backend/.../site/SitePublishController.java` | Endpoint `GET /api/projects/publish-direct/slug?name&organizationId&slug` |
| `backend/.../site/dto/SlugCheckResponse.java` | Bentuk jawaban cek nama: `slug, locked, available, message, url` |
| `backend/.../site/PublicSiteController.java` | Hapus berita & prestasi contoh; tanggal berita asli; judul SEO dari nama website; `resolve-domain` hanya domain kustom |
| `backend/src/main/resources/application.yml`, `.env.example` | `SITES_HOSTING_URL` (bawaan `http://localhost:3001`) |
| `backend/src/test/.../SitePublishIntegrationTest.java` | Test baru: saran → terbit dengan nama pilihan → terkunci → nama dipakai/dicadangkan/salah ditolak → tanpa data contoh |
| `OrgSpace-Hosting/src/middleware.ts` | Hapus rewrite subdomain `{slug}.orgspace.app` |
| `android/.../ui/editor/ExportDialog.java`, `dialog_export.xml` | Field **Nama website** + cek otomatis 500 ms setelah berhenti mengetik |
| `android/.../ui/editor/TemplateEditorViewModel.java` | `checkSlug()`, `publishDirect(slug)` |
| `android/.../data/remote/...` | `SlugCheckDto`, `WebsiteApi.checkSlug`, `WebsiteRepository.checkSlug` |
| `android/tools/keep-adb-reverse.*` | Ikut meneruskan port 3001 (OrgSpace-Hosting) |

| `backend/.../site/dto/DirectPublishRequest.java` + `SitePublishService.publishDirect` | `projectId` opsional: website yang sudah ada selalu terbit di ruang kerjanya sendiri (perbaikan bug BEM → My Organisasi) |
| `android/.../ui/editor/TemplateEditorViewModel.java` | Kirim `projectId` bila editor dibuka dari halaman kelola website, dan setelah terbit pertama |

## Alasan keputusan
- Website lama dikenali lewat `projectId`, bukan nama → ganti nama di editor tidak membuat project/website baru, dan ruang kerja aktif di HP tidak bisa memindahkannya (alternatif: tetap cari lewat nama di ruang kerja aktif, itulah sumber bug).
- Alamat path `orgspace.app/{slug}` → satu domain, satu sertifikat HTTPS, tidak butuh wildcard DNS (alternatif: subdomain `{slug}.orgspace.app`, tidak dipilih Aris).
- Slug terkunci setelah terbit pertama → link yang sudah dibagikan tidak putus (alternatif: boleh ganti nama, tidak dipilih karena link lama mati).
- Cek nama lewat satu endpoint GET yang juga memberi saran → app cukup satu panggilan saat dialog dibuka (alternatif: dua endpoint terpisah, lebih banyak kode).
- Cek nama gagal (offline) tidak memblokir tombol → server tetap memvalidasi saat terbit, jadi aman.

## Cara menjalankan & mengetes
1. Backend: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
2. Hosting: `cd ../OrgSpace-Hosting && npm run dev` (port 3001)
3. HP: `adb reverse tcp:8080 tcp:8080` dan `adb reverse tcp:3001 tcp:3001` (atau `./tools/keep-adb-reverse.sh`)
4. Buka template → Edit → Export → Publish Langsung. Ubah nama website, lihat helper "Link: ...". Terbitkan, lalu buka link di Chrome HP.

## Hasil tes
- `./mvnw test`: 337 test, lulus (setelah memperbaiki `PortalIntegrationTest` yang masih mengharapkan versi skema 26; migrasi V27 domain kustom membuatnya 27).
- `./gradlew assembleDebug` dan `./gradlew testDebugUnitTest`: berhasil.
- Uji manual lewat API di laptop: saran `web-osis-uji`; `admin` ditolak "dicadangkan"; terbit → `http://localhost:3001/osis-uji-0425` tampil (HTTP 200, judul "Web OSIS Uji", tanpa data contoh); slug tidak ada → 404.
- Uji di HP (RMX3151): nama `admin` ditolak; terbit `sma-nusantara-uji` lalu terbuka di Chrome HP; nama terkunci saat terbit ulang (v2).
- Bug ruang kerja di HP: terbit dari BEM → masuk BEM; buka website BEM dari tab Website, ganti nama, terbit ulang → tetap project yang sama di BEM (v2, tanpa duplikat); daftar My Organisasi tidak memuatnya.
- Test baru `existingWebsiteAlwaysPublishesInItsOwnWorkspace`: ruang kerja aktif pindah + nama diganti → tetap di BEM; orang luar ditolak.

### Domain kustom (revisi)
- Unit test `SiteDomainServiceTest` (8): `.sch.id` utama → A @; `sch.id` saja ditolak; domain aktif di website lain menghalangi; bypass diabaikan tanpa izin server.
- Uji di HP: `sch.id` ditolak dengan pesan; `osis-nusantara-uji.sch.id` → instruksi `A @ 103.175.216.42` + `TXT _orgspace-verify`; Periksa → DNS belum ada → "Aktifkan Langsung" (debug) → Domain Aktif; halaman website menampilkan domain; Lepas Domain → kembali "Belum Ada".
- Uji Hosting di laptop (header Host): `/` dan `/sma-nusantara-uji` di domain kustom → 200; Caddy ask domain aktif 200, domain asing 404; host asing → dialihkan ke portal.
- Belum diuji: domain sungguhan dengan DNS asli (butuh VPS + domain).

### Modul dinamis (revisi)
- Template contoh "Portal Organisasi Lengkap" (semua modul + modul kustom `prestasi` & `agenda`) diupload sebagai provider lewat API: cek lolos, 6 modul terdeteksi, terbit di galeri.
- Di HP: template dipilih dari galeri, nama organisasi diubah, terbit ke `osis-uji-modul`; tamu tidak melihat tombol Keluar & catatan khusus pengurus; pengumuman tampil dari database.
- Situs uji `osis-modul-api` (akun `pengurus-uji@orgspace.test`): login portal, token portal ditolak API app & website lain, tambah pengumuman/prestasi/agenda, form pendaftaran & aspirasi masuk Kotak Masuk, form terlalu panjang/nama aneh ditolak, tambah pengurus email tak terdaftar ditolak.
- Di HP: prestasi & agenda tampil; foto/link `javascript:` dari data diabaikan; form pendaftaran dikirim dari Chrome HP dan masuk Kotak Masuk.
- Di HP (lanjutan): login Portal Pengurus `osis-modul-api` berhasil; tab Modul Kustom otomatis berisi field prestasi (foto, tingkat, judul, nama, tahun); prestasi baru dari portal langsung tampil di website tanpa terbit ulang; website setelah login menampilkan "Halo, Pengurus Uji", tombol Keluar, dan catatan `data-ws-role`; tombol Masuk tersembunyi; Keluar mengembalikan tampilan tamu.
- Ditemukan & diperbaiki: link `href="admin"` di template membuka `/admin` (alamat website tidak berakhiran `/`); sekarang diarahkan ke `/{slug}/admin`.

### JavaScript template di sandbox (uji HP, 19 Oktober 2026)
- Template uji `uji-js` (Tailwind CDN, hamburger, dark mode + localStorage, tab, 2 halaman, modul, form) lolos cek upload dan terbit.
- Di HP: halaman bergaya Tailwind; teks uji "JavaScript aktif · origin: null" (sandbox aktif); menu terbuka; dark mode berganti tanpa error (pilihan hilang saat pindah halaman, sesuai rancangan); tab berpindah; `/uji-js/tentang.html` terbuka dengan CSS & JS; pengumuman terisi; form aspirasi dari halaman sandbox masuk Kotak Masuk; Masuk Pengurus membuka `/uji-js/admin`.
- Website lama (`osis-modul-api`) tetap tampil dan modulnya terisi.

## Konsep yang dipelajari
- Header `Content-Security-Policy: sandbox` tanpa `allow-same-origin` membuat halaman berjalan sebagai origin "null": JavaScript jalan, tapi tidak bisa membaca cookie/storage situs asal.
- Satu domain untuk banyak website (path) berarti localStorage dipakai bersama: token yang disimpan di sana harus dibatasi untuk satu website saja.
- Akhiran publik (public suffix): `.sch.id` dan `.my.id` dimiliki pengelola domain, jadi `osis.sch.id` adalah domain utama milik sekolah, bukan subdomain.
- Debounce: menunda cek ke server sampai user berhenti mengetik, agar tidak memanggil server di setiap huruf.
- Jawaban basi: jawaban server untuk ketikan lama diabaikan jika isi field sudah berubah.
- Path vs subdomain: path dibaca dari URL (`/[slug]`), subdomain dari header `Host`; path tidak butuh DNS tambahan.

## Latihan untuk Aris
1. Tambahkan `beranda` ke `RESERVED_SLUGS`, lalu cek lewat Swagger bahwa `slug=beranda` ditolak.
2. Ubah jeda `publish_slug_check_delay_ms` menjadi 1500, lalu rasakan bedanya di HP.

## Yang perlu Aris lakukan
- Uji di HP: jalankan Hosting + `adb reverse tcp:3001 tcp:3001`, terbitkan satu website, buka link-nya.
- Di VPS nanti: isi `SITES_HOSTING_URL=https://orgspace.app` di `.env` backend.

## Lanjutan: Portal Pengurus modul kustom lengkap
| File | Fungsi |
|---|---|
| `backend/.../site/PublicSiteController.java` | Jenis field di `templateModules`, `cleanItemData`, endpoint edit, urutan, unggah & sajikan foto |
| `backend/.../project/ProjectService.java` | `saveSiteMedia`, `siteMedia` (pakai `saveAsset` yang sudah ada) |
| `backend/.../security/SecurityConfig.java` | PUT/DELETE `/api/public/sites/**` lewat filter; dicek token portal di controller |
| `OrgSpace-Hosting/src/app/[slug]/admin/page.tsx` | Form per jenis field, unggah foto, edit, naik/turun |
| `OrgSpace-Hosting/src/lib/site-page.ts`, `[...path]/route.ts` | `/{slug}/_media/{id}` |
| `OrgSpace-Hosting/src/lib/site-runtime.ts` | Format `tanggal`, baris baru `paragraf` |
| `OrgSpace-Website/contoh-modul-orgspace/**`, `src/lib/docs-data.ts`, ZIP unduhan | Dokumentasi `data-ws-type` |

- Keputusan: foto disimpan di `project_assets` (alternatif tabel baru, tidak dipilih: kuota & deduplikasi sudah ada). Urutan memakai `sort_order` yang sudah ada di `site_custom_items` (tanpa migrasi).
- Tes: `SiteModulesTest` (jenis field, pembersihan isian) dan `SitePublishIntegrationTest.portalEditsReordersAndUploadsPhotoForCustomModule` lulus. Uji manual lewat curl di `osis-modul-api`: foto 200 lewat Hosting, edit & urutan tersimpan dan tampil di data halaman.
- Latihan: tambahkan `data-ws-type="paragraf"` pada field cerita prestasi di template contoh, unggah ulang, lalu lihat isian Portal berubah menjadi kotak teks panjang.

## Lanjutan: versi awal selesai (statistik, foto editor, template Sinergi, uji HP)
| File | Fungsi |
|---|---|
| `backend/.../site/PublicSiteController.java` | `stats`, `org`, modul dari semua halaman rilis |
| `backend/.../site/SitePublishService.java`, `SitePublishController.java` | Publish Langsung multipart: foto editor disimpan & dirujuk `asset:{id}` |
| `backend/.../site/SiteBuildService.java` | `asset:{id}` → `/{slug}/_media/{id}` (publik) |
| `backend/.../upload/check/HtmlRules.java` | Validasi `data-ws-stat` & `data-ws-org` |
| `android/.../ui/editor/TemplateEditorViewModel.java`, `WebsiteRepository.java`, `WebsiteApi.java` | Kirim file foto editor saat Publish Langsung |
| `OrgSpace-Hosting/src/lib/site-runtime.ts`, `site-page.ts` | Isi `data-ws-stat`/`data-ws-org`, logo `_media/logo`, perbaikan regex |
| `OrgSpace-Website/contoh-modul-orgspace/template-sinergi/**` | Template contoh lengkap & interaktif |

- Keputusan: statistik hanya angka (jumlah isian form, bukan isinya) → aman tampil publik tanpa pengaturan tambahan (alternatif: saklar per angka di Portal, ditunda karena versi awal untuk tugas kuliah).
- Keputusan: `pengurus` = anggota ruang kerja OrgSpace; jumlah anggota organisasi yang lebih besar dibuat sebagai modul kustom (mis. `anggota`) dan dihitung lewat `data-ws-stat="anggota"`.
- Tes: `./mvnw test` 349 lulus; `./gradlew testDebugUnitTest` lulus.
- Uji HP (Realme RMX3151, 19 Oktober 2026): galeri menampilkan Sinergi → editor: ganti judul, foto (dari Photo Picker), warna utama ungu → Publish Langsung `sinergi-osis` → website di Chrome HP: judul, foto, warna, statistik dari database, chip filter dari data, agenda urut per tanggal + "Berikutnya" + hitung mundur, lightbox, form 2 langkah terkirim (statistik pendaftar naik), pencarian pengurus, halaman Tentang. Batas 5 website per ruang kerja muncul dengan pesan jelas (uji memakai `SITES_MAX_PER_PERSONAL=20`).
- Domain kustom `osis-sinergi.my.id` (aktivasi bypass DNS dev, header Host): beranda, tentang, admin, foto → 200; Caddy ask 200, domain asing 404.
- Belum diuji di HP: UI Portal Pengurus (edit, urutkan, unggah foto) — HP sedang dipakai; API-nya lulus integration test.
- Latihan: tambahkan kartu `<b data-ws-stat="galeri">0</b> foto` di template Sinergi, unggah ulang, lalu tambah foto galeri di Portal dan lihat angkanya naik.

## Rencana fase berikutnya
- `organizationName` di data render masih "Ruang Pribadi X" untuk ruang pribadi (dipakai Open Graph `siteName`).
- `CustomRenderer` (mode rakit sendiri) masih berisi teks contoh; menunggu rancangan editor custom mode.
