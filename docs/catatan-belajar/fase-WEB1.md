# Fase WEB1: Fondasi portal web (landing, dokumentasi, monitoring, kritik & saran)

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `backend/.../db/migration/V21__portal_web.sql` | Tabel `feedback`, `feedback_messages`, `doc_articles` (+6 artikel awal), `user_daily_activity`, `api_traffic_daily` |
| `backend/.../admin/AdminGuard.java`, `AdminWebConfig.java` | Semua `/api/admin/**` khusus akun `is_admin` (403 `ADMIN_ONLY`) |
| `backend/.../admin/AdminController.java`, `AdminUserQueries.java` | `/admin/me`, daftar & detail pengguna, kesehatan sistem |
| `backend/.../monitoring/UsageTrackingFilter.java`, `UsageRecorder.java`, `ClientPlatform.java` | Mencatat lalu lintas API & pengguna aktif per platform, ditulis berkala ke database |
| `backend/.../monitoring/StatsService.java`, `StatsMetric.java`, `StatsRange.java`, `AdminStatsController.java` | Ringkasan, grafik harian 10 metrik, metode daftar, platform, lalu lintas |
| `backend/.../feedback/*` | Kritik & saran: kirim, daftar milikku, percakapan, balas (pengirim & tim), status, jumlah per status |
| `backend/.../docs/*` | Pusat dokumentasi: menu & artikel publik, kelola artikel (admin) |
| `backend/.../portal/PortalController.java` | `/public/overview` untuk landing page |
| `backend/.../seed/PortalDemoSeeder.java` | dev: admin `dummy1`, feedback contoh, statistik 30 hari |
| `backend/.../security/JwtAuthFilter.java` | Menitipkan ID user ke atribut request untuk pencatat monitoring |
| `backend/.../security/SecurityConfig.java` | `GET /api/public/**` tanpa login |
| `backend/.../common/response/PageResponse.java` | Bentuk daftar bernomor halaman untuk web |
| `backend/src/test/.../portal/PortalIntegrationTest.java` | 6 test: akses admin, percakapan feedback, validasi & batas, docs, monitoring, landing |
| repo **OrgSpace Website** | Project Next.js 16 (awalnya folder `web/`, dipindah ke repo sendiri atas permintaan Aris): landing, `/docs`, dashboard (login, ringkasan, feedback, pengguna, lalu lintas, docs, sistem) |
| `src/lib/api/*` (repo website) | Kontrak tipe (`types.ts`) + klien API (`http.ts`, `public.ts`, `admin.ts`) |
| `src/lib/auth/*`, `src/proxy.ts` (repo website) | Sesi cookie httpOnly, refresh token otomatis, `requireAdmin` |
| `AGENTS.md`, `README.md` (repo website) | Aturan tim & agent AI, cara setup Windows/Linux |
| `docs/rancangan/portal-web.md` | Rancangan & kontrak API portal web |

## Alasan keputusan
- Web di repo terpisah **OrgSpace Website**, bersebelahan dengan repo ini (keputusan Aris, agar struktur lebih rapi) → tim web hanya meng-clone apa yang mereka ubah; kontrak API sumber utamanya tetap di repo ini dan disalin ke `docs/kontrak-api.md` repo website (sebelumnya: folder `web/` di monorepo).
- Statistik dicatat di memori lalu UPSERT tiap 60 detik → beban database kecil (alternatif: INSERT per request — terlalu berat; Spring Boot Actuator + Prometheus — butuh library & server tambahan).
- Rute dicatat sebagai pola `/api/projects/{id}` → tidak ada data pribadi, jumlah baris kecil (alternatif: path asli — setiap ID jadi baris baru).
- Admin = flag `users.is_admin` yang dicek ke database setiap request → pencabutan hak langsung berlaku (alternatif: klaim di JWT — baru berlaku setelah token kedaluwarsa).
- Token di cookie httpOnly + Server Component/Server Action (pola BFF) → token tidak bisa dicuri script, backend tidak perlu CORS (alternatif: token di localStorage + fetch dari browser — rawan XSS dan butuh CORS).
- Refresh token di `proxy.ts` → Server Component tidak boleh menulis cookie; proxy berjalan sebelum render dan boleh (alternatif: refresh di halaman — token baru hilang dan refresh token lama terpakai dua kali, sehingga semua sesi dicabut backend).
- Artikel dokumentasi di database (Markdown) → bisa diubah tim dari dashboard tanpa deploy ulang dan nanti bisa ditampilkan di app (alternatif: file MDX di repo — perlu developer untuk setiap perubahan teks).
- `cacheComponents` Next.js dimatikan → pola `fetch` + `revalidate` lebih mudah dipahami tim (alternatif: Cache Components — konsep baru `use cache`/Suspense yang belum perlu).
- Grafik batang tanpa library → fondasi tanpa dependency tambahan; tim boleh mengusulkan library grafik.

## Cara menjalankan & mengetes
1. Backend dev: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (migrasi V21 + seeder portal berjalan otomatis).
2. Swagger UI → grup "Publik: …", "Admin: …", "Kritik & saran". Login `dummy1@templateapp.test` / `password123`, salin `accessToken` ke tombol Authorize.
3. Web: `cd web && npm install && cp .env.example .env.local && npm run dev` → http://localhost:3000.
4. Buka `/dashboard`, masuk dengan akun dummy1, buka Kritik & saran → balas → ubah status.
5. Coba masuk dengan `dummy5@templateapp.test` → ditolak "bukan tim internal".

## Hasil tes
- `./mvnw test`: lulus, 321 test (6 test baru di `PortalIntegrationTest`).
- `npm run lint` dan `npm run build` (web): bersih.
- Uji manual (backend dev + `next start`): landing 200 dengan angka, `/docs/apa-itu-orgspace` merender tabel Markdown, slug tidak ada → 404, `/dashboard` tanpa login → `/dashboard/login`, 7 halaman dashboard 200 dengan cookie admin, hanya cookie refresh → proxy memperbarui token (Set-Cookie baru), akun non-admin → `/dashboard/login?reason=not-admin`.
- Belum dicoba: form login lewat browser sungguhan (Server Action) — logikanya sama dengan yang dites lewat cookie.

## Konsep yang dipelajari
- **Interceptor vs Filter** : Filter membungkus seluruh request (termasuk Spring Security), interceptor hanya di sekitar controller. Pencatat lalu lintas memakai filter agar request 401 ikut terhitung; penjaga admin memakai interceptor agar error-nya diproses `GlobalExceptionHandler`.
- **UPSERT** : `INSERT ... ON CONFLICT DO UPDATE` menambah baris baru atau menjumlahkan ke baris yang sudah ada dalam satu perintah.
- **BFF (Backend for Frontend)** : server Next.js menjadi perantara browser ↔ API, sehingga rahasia (token) tetap di server.
- **ISR / revalidate** : halaman publik dibuat sekali lalu dibuat ulang otomatis setelah N detik, cepat dan ramah SEO.

## Latihan untuk Aris
1. Tambahkan metrik `new_providers` di `StatsMetric` (hitung dari `provider_profiles.created_at`), lalu coba di Swagger.
2. Di dashboard, jadikan akunmu sendiri admin lewat SQL, lalu cabut lagi dan lihat dashboard langsung menolak.
3. Ubah `PORTAL_USAGE_FLUSH_SECONDS=10`, panggil beberapa endpoint, dan amati `api_traffic_daily` bertambah.

## Yang perlu Aris lakukan
- Tidak ada perubahan Gradle (Sync Now tidak perlu).
- Opsional: isi `PORTAL_ANDROID_DOWNLOAD_URL` di `backend/.env` saat link unduh app sudah ada.
- Kirim link https://github.com/A-blib/OrgSpace-Website ke tim web; mereka mulai dari `README.md` repo itu (backend: repo ini, branch `fitur/orgspace`).
- Menu Kritik & saran di app Android (memakai `/api/feedback`) dibuat saat pengembangan app dilanjutkan.

## Rencana fase berikutnya
- Menunggu perubahan desain layout dari Aris; setelah "lanjutkan pengembangan sebelumnya", lanjut T3–T8 (editor cloud) lalu P2–P7 (publish).
