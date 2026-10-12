# Fase 11: Backend Dashboard Provider

Sumber rancangan: [`docs/rancangan/alur-provider.md`](../rancangan/alur-provider.md) (salinan `Instruksi dan alur/alurUntukProvider.md`).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `V7__simplify_provider_status.sql` | Status provider hanya `active`/`suspended`; data lama menjadi `active`; kolom `rejection_reason` dihapus |
| `V8`–`V11` | Tabel `templates`, `template_checks`, `template_check_issues`, `template_events`, `notifications` |
| `provider/ProviderStatus`, `ProviderProfile`, `user/UserService`, `UserResponse`, `DummyDataSeeder` | Menyesuaikan status baru (provider langsung aktif, tanpa alasan penolakan) |
| `template/*` (Entity, enum, repository) | `Template`, `TemplateCheck`, `TemplateCheckIssue`, `TemplateStatus`, `CheckStatus`, `IssueSeverity`, `TemplateEventType` |
| `template/ProviderTemplateQueries.java` | SQL agregat: daftar + filter/urutan/cari/paginasi, jumlah per status, ringkasan, tren harian, populer, perlu tindakan, simpan event |
| `template/ProviderTemplateService`, `TemplateEventService`, `TemplateController` | Template Anda, detail + pengecekan terakhir, event dilihat/didownload |
| `provider/ProviderAccess`, `StatsPeriod`, `ProviderDashboardService`, `ProviderProfileService`, `ProviderController` | Penjaga akses, periode 7/30 hari (WIB), Beranda, Profil |
| `notification/*` | Notifikasi di dalam app: list, unread-count, tandai dibaca |
| `config/AppProperties` + `application.yml` | `app.timezone` (bawaan Asia/Jakarta), `app.seed.demo-templates` (bawaan false) |
| `common/exception/ErrorCode` | `PROVIDER_REQUIRED`, `PROVIDER_SUSPENDED` |
| `security/SecurityConfig`, `CurrentUser` | Event boleh tanpa login; `/api/dev/**` (hanya ada di profile dev); `optionalId()` |
| `seed/DemoTemplateSeeder`, `DevDemoController` | Seeder demo opsional + `DELETE /api/dev/demo-templates` |
| Test: `ProviderDashboardIntegrationTest` (12), `DemoTemplateSeederTest` (1), tambahan `FlywayMigrationTest` (2), penyesuaian test lama | |
| `docs/rancangan/alur-provider.md`, `AGENTS.md` bagian 15, `README.md`, `docs/dokumentasi-project.md` bab 21 | |

## Alasan keputusan
- Grafik dibuat sendiri (keputusan Aris) → tidak butuh MPAndroidChart yang sudah tidak diperbarui dan hanya ada di JitPack.
- Push FCM ditunda (keputusan Aris) → pemicunya (hasil pengecekan) baru ada setelah Upload; versi ini cukup notifikasi di dalam app.
- Data lama `pending`/`approved`/`rejected` → `active` (keputusan Aris), `rejection_reason` dihapus karena tidak ada penolakan lagi.
- Satu endpoint Beranda (`/providers/me/dashboard`) selain endpoint usulan `/stats/downloads` → layar Beranda cukup satu request untuk semua bagiannya.
- "Perlu tindakan" dihitung dari kondisi saat ini, bukan disimpan → kartu otomatis hilang saat masalahnya beres (rancangan 4.4).
- Badge tab Beranda = jumlah "Perlu tindakan" (rancangan 3.3); `unread-count` notifikasi tetap tersedia untuk daftar notifikasi.
- Profil "lengkap" = bio dan keahlian terisi → dipakai checklist provider baru dan kartu `PROFILE_INCOMPLETE`.
- Statistik memakai SQL (`JdbcClient`) → perhitungan agregat lebih jelas dan efisien di SQL.
- Tanggal dikelompokkan di `Asia/Jakarta` → download dini hari WIB tidak tergeser ke tanggal UTC kemarin.
- Dedup download dijaga index unik + `ON CONFLICT DO NOTHING` → aman walau app mengirim ulang event yang sama.
- Template provider lain → `404` → keberadaan template tidak bocor.
- Kategori template memakai enum `WebsitePurpose` → pilihannya sama persis dengan tujuan website di onboarding.
- Seeder demo `@Profile("dev")` + `@ConditionalOnProperty` + `@Order(2)` → mati bawaan, tidak pernah ada di prod, tidak mengganggu `DummyDataSeeder`.

## Cara menjalankan & mengetes
1. `cd backend && ./mvnw test` (Docker harus aktif).
2. Jalankan dengan data demo: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--app.seed.demo-templates=true`.
3. Swagger UI: login `demo-provider@templateapp.test` / `password123` → Authorize → coba grup **Provider**, **Template**, **Notifikasi**.
4. Kosongkan lagi: `curl -X DELETE http://localhost:8080/api/dev/demo-templates`.

## Hasil tes
- `./mvnw test`: **lulus, 78 test** (ProviderDashboardIntegrationTest 12, DemoTemplateSeederTest 1, FlywayMigrationTest 7, AccountLinkingServiceTest 12, OnboardingIntegrationTest 11, SocialAuthIntegrationTest 10, AuthFlowIntegrationTest 9, GitHubOAuthClientTest 5, JwtServiceTest 5, GlobalExceptionHandlerTest 4, DummyDataSeederTest 1, TemplateAppApplicationTests 1).
- Yang diuji: akses (non-provider/suspended), Beranda kosong (7 titik bernilai 0 + checklist), ringkasan/tren/populer untuk 7 & 30 hari, pengelompokan tanggal WIB (00.30 WIB), perlu tindakan + hilang otomatis, filter/jumlah chip/urutan/pencarian (termasuk `%`), paginasi 20, detail + pengecekan terakhir, 404 untuk template orang lain, aturan event (tamu, dedup, pemilik, belum tayang, validasi), profil, notifikasi, seeder demo + hapus cascade, CHECK status baru, index unik event.
- Uji manual ke database dev (data Aris): migrasi V7–V11 berjalan; 4 provider lama menjadi `active`; seeder demo membuat akun demo; `/providers/me/dashboard` mengembalikan perlu tindakan (Landing Event 2 error, Profil Sekolah 3 peringatan, 1 draft), ringkasan 7 hari (3 aktif · 78 dilihat · 23 didownload), tren per hari, dan populer berurutan; chip Semua 7 · Tayang 3 · Perlu perbaikan 2 · Sedang dicek 1 · Draft 1 · Dinonaktifkan 1.
- Kendala yang ditemukan test dan diperbaiki: seeder demo gagal karena template (JPA) belum di-`flush` sebelum event ditulis lewat SQL; pencarian `%` di test gagal karena URL di-encode dua kali (perbaikan di test, bukan di kode).

## Konsep yang dipelajari
- Migrasi yang mengubah data (UPDATE + ganti CHECK constraint + hapus kolom) tanpa mengubah migrasi lama.
- `JdbcClient`, `COUNT(*) FILTER`, `generate_series`, `AT TIME ZONE`, `ON CONFLICT DO NOTHING`, LIKE + `ESCAPE`.
- SQL injection & kenapa nilai user harus menjadi parameter.
- `@ConditionalOnProperty`, `@Order` untuk `ApplicationRunner`.
- Kapan Hibernate menulis ke database (flush) dan kenapa itu penting saat mencampur JPA dengan SQL langsung.

## Latihan untuk Aris
1. Nyalakan seeder demo, login sebagai akun demo di Swagger, lalu panggil `GET /api/providers/me/dashboard?period=30d`. Bandingkan `summary.downloads` dengan jumlah semua angka di `downloadTrend`.
2. Jalankan `UPDATE templates SET warning_count = 0 WHERE name = 'Profil Sekolah';` lalu panggil dashboard lagi. Kartu apa yang hilang dari `actionItems`, dan kenapa tanpa menghapus apa pun?
3. Kirim `POST /api/templates/{id}/events` dua kali dengan `projectId` sama untuk template "UMKM Kuliner". Cek `SELECT count(*) FROM template_events WHERE project_id = '...'`. Berapa hasilnya?

## Yang perlu Aris lakukan
- Tidak ada. (Kredensial Firebase belum dibutuhkan karena push ditunda.)

## Rencana fase berikutnya
- Fase 12: Android Dashboard Provider dengan bottom navigation (Beranda + badge, Upload "Segera hadir", Template Anda, Profil), grafik area custom View, checklist provider baru, filter & pencarian dengan paginasi, detail template + hasil pengecekan, edit profil, layar panduan; status provider di Android menjadi active/suspended.
