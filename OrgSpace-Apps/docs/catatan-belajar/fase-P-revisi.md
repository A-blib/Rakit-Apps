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
- Belum diuji di HP: tampilan setelah pengurus login (nama di status, tombol Keluar, catatan `data-ws-role`).

## Konsep yang dipelajari
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

## Rencana fase berikutnya
- `organizationName` di data render masih "Ruang Pribadi X" untuk ruang pribadi (dipakai Open Graph `siteName`).
- `CustomRenderer` (mode rakit sendiri) masih berisi teks contoh; menunggu rancangan editor custom mode.
