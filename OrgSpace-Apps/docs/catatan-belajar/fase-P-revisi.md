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

## Alasan keputusan
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
- Belum diuji di HP: HP tidak tersambung saat fase ini.

## Konsep yang dipelajari
- Debounce: menunda cek ke server sampai user berhenti mengetik, agar tidak memanggil server di setiap huruf.
- Jawaban basi: jawaban server untuk ketikan lama diabaikan jika isi field sudah berubah.
- Path vs subdomain: path dibaca dari URL (`/[slug]`), subdomain dari header `Host`; path tidak butuh DNS tambahan.

## Latihan untuk Aris
1. Tambahkan `beranda` ke `RESERVED_SLUGS`, lalu cek lewat Swagger bahwa `slug=beranda` ditolak.
2. Ubah jeda `publish_slug_check_delay_ms` menjadi 1500, lalu rasakan bedanya di HP.

## Yang perlu Aris lakukan
- Uji di HP: jalankan Hosting + `adb reverse tcp:3001 tcp:3001`, terbitkan satu website, buka link-nya.
- Di VPS nanti: isi `SITES_HOSTING_URL=https://orgspace.app` di `.env` backend.

## Rencana fase berikutnya
- `organizationName` di data render masih "Ruang Pribadi X" untuk ruang pribadi (dipakai Open Graph `siteName`).
- `CustomRenderer` (mode rakit sendiri) masih berisi teks contoh; menunggu rancangan editor custom mode.
