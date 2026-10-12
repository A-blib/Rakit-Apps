# Portal Web OrgSpace (Landing, Dokumentasi, Monitoring, Kritik & Saran)

> Sumber: `Website OrgSpace/Ringkasan Proyek_ OrgSpace Web Portal (Promo, Docs, & Monitoring).docx` (Aris, 12 Oktober 2026).
> Dokumen ini adalah **kontrak** antara backend (dibuat Aris) dan web (dikerjakan tim web). Fondasi di sini sudah
> dibangun dan dites; tim web menyempurnakan tampilan dan isi di atasnya.

---

## 0. Aturan kerja tim web (WAJIB dibaca)

| Aturan | Isi |
|---|---|
| Pembagian tugas | **Aris** membuat fondasi: endpoint backend, tabel, tipe data (`web/src/lib/api/types.ts`), klien API (`web/src/lib/api/`), sesi login (`web/src/lib/auth/`, `web/src/proxy.ts`), struktur halaman. **Tim web** mengerjakan tampilan, isi teks, dan komponen di atas fondasi itu. |
| Fitur baru | **Koordinasi dengan Aris dulu.** Aris membuat fondasinya (endpoint + tipe + kerangka halaman), baru tim web mengerjakan. Jangan membuat endpoint, tabel, atau mengubah kontrak sendiri. |
| Boleh langsung | Mengubah tampilan halaman di `web/src/app/**`, komponen di `web/src/components/**`, token desain di `globals.css` (selaras dengan app), teks landing page, menambah halaman statis tanpa data baru. |
| Harus tanya Aris | Mengubah `lib/api/**`, `lib/auth/**`, `proxy.ts`, `next.config.ts`; menambah library npm; menyalakan Cache Components; menyimpan token di browser (localStorage); memanggil backend langsung dari browser. |
| Backend | Folder `backend/` milik Aris. Tim web hanya menjalankannya untuk development. |
| Cabang Git | Buat cabang sendiri dari `fitur/orgspace` (mis. `web/landing-rina`), lalu pull request ke `fitur/orgspace`. |

---

## 1. Cakupan

| Bagian | Isi | Siapa yang membaca |
|---|---|---|
| Landing page `/` | Hero + ajakan, angka OrgSpace, fitur, ajakan akhir, footer | Publik (SEO) |
| Pusat dokumentasi `/docs` | Menu kategori + artikel Markdown dari database | Publik (SEO) |
| Dashboard `/dashboard` | Ringkasan statistik, grafik harian, metode daftar, platform | Tim internal (admin) |
| Kritik & saran `/dashboard/feedback` | Daftar + filter, percakapan, balas, ubah status | Tim internal |
| Pengguna `/dashboard/users` | Tabel pengguna, cari, filter peran | Tim internal |
| Lalu lintas API `/dashboard/traffic` | Request per hari, error, endpoint paling sering | Tim internal |
| Kelola dokumentasi `/dashboard/docs` | Buat/ubah/hapus/tayangkan artikel | Tim internal |
| Sistem `/dashboard/system` | Database, versi skema, uptime | Tim internal |

Belum dibangun (butuh diskusi dulu): login dashboard dengan Google, peran staf bertingkat (mis. hanya-baca), menu
Kritik & saran di app Android, notifikasi email balasan feedback, ekspor CSV, showcase website organisasi di landing.

---

## 2. Arsitektur

```
Browser ──► Next.js (web/, port 3000) ──► Spring Boot REST API (backend/, port 8080) ──► PostgreSQL
            │ Server Component / Server Action memanggil API dengan header Authorization
            │ proxy.ts memperbarui access token (refresh) sebelum halaman /dashboard dirender
            └ token disimpan di cookie httpOnly (os_access, os_refresh) — tidak bisa dibaca JavaScript browser
```

- **Tidak ada backend baru.** Web adalah klien REST API yang sama dengan app Android.
- Browser **tidak pernah** memanggil backend langsung (tidak perlu CORS, token tidak bocor ke JavaScript).
- Halaman publik di-cache Next.js (landing 5 menit, docs 1 menit). Dashboard selalu data terbaru.
- Setiap request web membawa header `X-OrgSpace-Client: web` agar statistik platform benar.

### 2.1 Akun admin

- Admin = akun OrgSpace biasa dengan `users.is_admin = true`. Login dashboard memakai **email + password**.
- Semua endpoint `/api/admin/**` dijaga `AdminGuard` (403 `ADMIN_ONLY`). Status admin dibaca dari database setiap
  request, jadi mencabut hak admin langsung berlaku.
- Development: seeder menjadikan `dummy1@templateapp.test` (password `password123`) admin jika belum ada admin.
- Menjadikan akun lain admin (SQL, dijalankan Aris):

```sql
UPDATE users SET is_admin = true WHERE lower(email) = 'nama@contoh.com';
```

### 2.2 Sesi & refresh token

| Kejadian | Yang terjadi |
|---|---|
| Login | Server Action `login` → `POST /auth/login` → cek `GET /admin/me` → bukan admin: refresh token langsung dicabut + pesan error; admin: cookie `os_access` (15 menit) + `os_refresh` (30 hari) |
| Buka halaman `/dashboard/**` | `proxy.ts`: access token sisa < 60 detik → `POST /auth/refresh` → cookie baru (refresh token dirotasi) |
| Refresh gagal | Cookie dihapus → `/dashboard/login?reason=expired` |
| Keluar | `POST /auth/logout` (cabut refresh token) + hapus cookie |

> ⚠️ Refresh token backend **sekali pakai**. Dipakai dua kali = dianggap dicuri → semua sesi user dicabut. Karena itu
> refresh hanya boleh lewat `refreshTokens()` di `lib/auth/refresh.ts` (refresh bersamaan untuk token yang sama dipakai
> bersama). Jika web nanti dijalankan di lebih dari satu server, bahas dulu dengan Aris.

---

## 3. Data (migrasi `V21__portal_web.sql`)

| Tabel | Isi |
|---|---|
| `feedback` | Satu percakapan kritik & saran: pengirim, `sender_role` (creator/provider), organisasi (opsional), `category` (bug/saran/pertanyaan/keluhan/lainnya), judul, pesan pertama, nilai 1–5, `status` (new/in_progress/resolved/closed), `source` (android/web), versi app, info perangkat, `user_unread`, `staff_unread`, `last_message_at` |
| `feedback_messages` | Pesan lanjutan (balasan staf / tanggapan pengirim) |
| `doc_articles` | Artikel dokumentasi: `slug` unik, `category`, `audience` (all/creator/provider), judul, ringkasan, isi Markdown, urutan, `status` (draft/published), `published_at` |
| `user_daily_activity` | Pengguna aktif: 1 baris per user per hari per platform (android/web/other) |
| `api_traffic_daily` | Lalu lintas API per hari per endpoint (pola, mis. `/api/projects/{id}`): jumlah request, 5xx, 4xx, total & maks durasi |

### 3.1 Cara statistik dicatat

- `UsageTrackingFilter` (paling luar, sebelum Spring Security) mencatat setiap request `/api/**`: pola endpoint,
  status, durasi, user (jika login), platform. **Tidak** mencatat isi request, query, atau IP.
- Hitungan dikumpulkan di memori (`UsageRecorder`) lalu ditulis ke database tiap `PORTAL_USAGE_FLUSH_SECONDS`
  (bawaan 60 detik) dan saat backend dimatikan normal. Data hari ini bisa tertinggal ±1 menit.
- "Hari" memakai zona `APP_TIMEZONE` (bawaan Asia/Jakarta).
- Platform: header `X-OrgSpace-Client` (android/web); request app lama dikenali dari User-Agent OkHttp.
- Pengguna aktif = user yang memanggil API minimal sekali di hari itu (tamu tidak dihitung).

---

## 4. Aturan kritik & saran

| Situasi | Hasil |
|---|---|
| Kirim | Status `new`, `staff_unread = true`. Maks `PORTAL_FEEDBACK_DAILY_LIMIT` (10) per user per 24 jam → 429 `TOO_MANY_ATTEMPTS` |
| Peran pengirim | `senderRole` di body, atau mode aktif akun. `provider` tanpa profil provider → 403 `PROVIDER_REQUIRED` |
| Organisasi | Opsional; harus anggota → selain itu 403 `NOT_ORG_MEMBER` |
| Staf membalas | `new` → `in_progress`; pengirim mendapat `user_unread = true` |
| Pengirim menanggapi | `resolved` → `in_progress` (dibuka lagi); `closed` → 409 `FEEDBACK_CLOSED` |
| Nama staf | Pengirim melihat "Tim OrgSpace", bukan nama akun staf |
| Info perangkat & email pengirim | Hanya terlihat admin |
| Feedback orang lain | 404 `FEEDBACK_NOT_FOUND` (keberadaannya tidak dibocorkan) |

---

## 5. Endpoint

Semua berprefix `/api`. Contoh lengkap & coba langsung: Swagger UI `http://localhost:8080/swagger-ui.html` (grup
"Publik: …", "Admin: …", "Kritik & saran"). Tipe TypeScript: `web/src/lib/api/types.ts`.

### 5.1 Publik (tanpa login)

| Method & path | Hasil |
|---|---|
| `GET /public/overview` | `{ stats: { organizations, websitesOnline, templates, providers }, androidDownloadUrl }` (cache 5 menit) |
| `GET /public/docs` | `{ categories: [{ value, label, articles: [DocSummary] }] }` — hanya artikel tayang, kategori kosong tidak dikirim |
| `GET /public/docs/{slug}` | `DocDetail` (isi Markdown); 404 `DOC_NOT_FOUND` jika tidak ada / masih draft |
| `GET /templates?sort=popular&size=6` | Galeri template (sudah ada sejak fase 13) |

### 5.2 Pengirim kritik & saran (login, dipakai app Android nanti)

| Method & path | Body / query | Hasil |
|---|---|---|
| `POST /feedback` | `{ category, subject, message, rating?, senderRole?, organizationId?, appVersion?, deviceInfo? }` | 201 `FeedbackDetail` |
| `GET /feedback` | `page, size` | `PageResponse<FeedbackSummary>` milik sendiri |
| `GET /feedback/unread-count` | – | `{ count }` |
| `GET /feedback/{id}` | – | `FeedbackDetail` (menghapus tanda balasan baru) |
| `POST /feedback/{id}/messages` | `{ body }` | `FeedbackDetail` |

### 5.3 Admin (`/admin/**`, akun `is_admin`)

| Method & path | Body / query | Hasil |
|---|---|---|
| `GET /admin/me` | – | `{ id, displayName, email, avatarUrl }` |
| `GET /admin/system` | – | database, versi skema, uptime, profil, zona waktu |
| `GET /admin/stats/overview` | `period` = 7d/30d/90d | total, angka periode vs periode sebelumnya, DAU/WAU/MAU |
| `GET /admin/stats/timeseries` | `metric`, `period` | titik harian (hari kosong = 0). `metric`: signups, active_users, new_organizations, new_websites, publishes, template_views, template_downloads, feedback, api_requests, api_errors |
| `GET /admin/stats/signup-methods` | `period` | `{ email, google, github }` (metode login pertama) |
| `GET /admin/stats/active-platforms` | `period` | `{ android, web, other, total }` |
| `GET /admin/stats/traffic` | `period` | total, per hari, 15 endpoint teratas |
| `GET /admin/users` | `q, role (creator/provider/admin), page, size` | `PageResponse<AdminUser>` |
| `GET /admin/users/{id}` | – | `AdminUser` |
| `GET /admin/feedback` | `status, role, category, unread, q, page, size` | `PageResponse<FeedbackSummary>` (belum dibaca tim di atas) |
| `GET /admin/feedback/counts` | – | `{ all, newCount, inProgress, resolved, closed, unread }` |
| `GET /admin/feedback/{id}` | – | `FeedbackDetail` (menghapus tanda belum dibaca tim) |
| `POST /admin/feedback/{id}/messages` | `{ body }` | `FeedbackDetail` |
| `PATCH /admin/feedback/{id}` | `{ status }` | `FeedbackDetail` |
| `GET /admin/docs` | – | semua artikel (termasuk draft) |
| `GET /admin/docs/{id}` | – | `DocDetail` |
| `POST /admin/docs` | `DocRequest` | 201; 409 `DOC_SLUG_TAKEN` |
| `PUT /admin/docs/{id}` | `DocRequest` (semua field) | `DocDetail` |
| `DELETE /admin/docs/{id}` | – | 204 |

### 5.4 Kode error baru

`ADMIN_ONLY` (403), `FEEDBACK_NOT_FOUND` (404), `FEEDBACK_CLOSED` (409), `DOC_NOT_FOUND` (404), `DOC_SLUG_TAKEN` (409).
Kode lain yang mungkin muncul: `UNAUTHORIZED`, `VALIDATION_ERROR` (+ `fieldErrors`), `TOO_MANY_ATTEMPTS`,
`INVALID_CREDENTIALS`, `USE_SOCIAL_LOGIN`.

---

## 6. Konfigurasi

| Variabel | Di | Bawaan | Fungsi |
|---|---|---|---|
| `ORGSPACE_API_URL` | `web/.env.local` | `http://localhost:8080/api` | Alamat backend (hanya dibaca server Next.js) |
| `PORTAL_ANDROID_DOWNLOAD_URL` | `backend/.env` | kosong | Link tombol unduh app di landing; kosong = tombol "segera hadir" |
| `PORTAL_FEEDBACK_DAILY_LIMIT` | `backend/.env` | 10 | Batas feedback baru per user per 24 jam |
| `PORTAL_USAGE_FLUSH_SECONDS` | `backend/.env` | 60 | Jeda tulis statistik dari memori ke database |

---

## 7. Desain

Selaras dengan app (revisi-fondasi-orgspace.md bagian 7): palet **Biru Semangat** dan font **Plus Jakarta Sans**.
Semua warna adalah token di `web/src/app/globals.css` (terang + gelap otomatis), dipakai lewat kelas Tailwind
(`bg-surface`, `text-muted`, `border-border`, `bg-primary`, `rounded-card`, …) dan kelas komponen (`btn-primary`,
`btn-outline`, `card`, `input`, `badge`, `table`). Tampilan MVP sengaja sederhana; penyempurnaan oleh tim desain.

---

## 8. Data contoh (profile dev)

`PortalDemoSeeder` (hanya `dev`) mengisi: admin `dummy1@templateapp.test`, 5 kritik & saran dengan berbagai status
dan balasan, serta riwayat pengguna aktif & lalu lintas API 30 hari (angka acak tetap). Diisi hanya jika tabelnya
kosong. Mengosongkan untuk mengisi ulang:

```sql
DELETE FROM feedback; DELETE FROM user_daily_activity; DELETE FROM api_traffic_daily;
```
