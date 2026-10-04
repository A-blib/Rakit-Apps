# Dokumentasi Project: App Template Website

Dokumen ini menjelaskan **cara kerja project dari dalam**: bagian-bagiannya, cara bagian itu saling terhubung, dan alasan dibuat seperti itu. Bacalah dari atas ke bawah. Setiap bagian merujuk ke file aslinya supaya bisa langsung dibuka di VS Code atau Android Studio.

- Cara menyiapkan dan menjalankan project → `README.md` (Linux) dan `docs/panduan-kolaborator.md` (Windows & Linux, untuk anggota tim)
- Catatan per fase (yang dikerjakan, latihan) → `docs/catatan-belajar/`
- Dokumen ini → **memahami** project

> Status dokumen: diperbarui sampai **Fase 12** (Dashboard Provider di Android). Rancangan fitur provider: [`rancangan/alur-provider.md`](rancangan/alur-provider.md). Screenshot setiap layar ada di README bagian "Tampilan".

---

## Daftar isi

1. [Gambaran besar](#1-gambaran-besar)
2. [Struktur backend](#2-struktur-backend)
3. [Lapisan kode: perjalanan satu request](#3-lapisan-kode-perjalanan-satu-request)
4. [Konfigurasi dan profile](#4-konfigurasi-dan-profile)
5. [Database](#5-database)
6. [Keamanan: login, JWT, refresh token](#6-keamanan-login-jwt-refresh-token)
7. [Login Google & GitHub, penyambungan akun](#7-login-google--github-penyambungan-akun)
8. [Onboarding, mode, dan data dummy](#8-onboarding-mode-dan-data-dummy)
9. [Format error](#9-format-error)
10. [Daftar endpoint](#10-daftar-endpoint)
11. [Test](#11-test)
12. [Resep: menambah endpoint baru](#12-resep-menambah-endpoint-baru)

**Bagian Android**

13. [Struktur app Android](#13-struktur-app-android)
14. [Tema dan token desain](#14-tema-dan-token-desain)
15. [Jaringan: Retrofit, token, dan refresh otomatis](#15-jaringan-retrofit-token-dan-refresh-otomatis)
16. [Penyimpanan sesi di HP](#16-penyimpanan-sesi-di-hp)
17. [Saat app dibuka: splash, layar awal, intro, dashboard](#17-saat-app-dibuka-splash-layar-awal-intro-dashboard)
18. [Masuk & daftar dengan email](#18-masuk--daftar-dengan-email)
19. [Login Google & GitHub di Android](#19-login-google--github-di-android)
20. [Onboarding, menu profil, pengaturan, keluar](#20-onboarding-menu-profil-pengaturan-keluar)

**Fitur provider**

21. [Backend Dashboard Provider](#21-backend-dashboard-provider)
22. [Dashboard Provider di Android](#22-dashboard-provider-di-android)
23. [Glosarium](#23-glosarium)

---

## 1. Gambaran besar

```
┌─────────────────────┐   HTTP + JSON    ┌──────────────────────────┐   SQL    ┌──────────────┐
│  App Android (HP)   │ ───────────────► │  Backend Spring Boot     │ ───────► │  PostgreSQL  │
│  Java, XML Views    │ ◄─────────────── │  (laptop, port 8080)     │ ◄─────── │  templateapp │
└─────────────────────┘                  └──────────────────────────┘          └──────────────┘
          │  adb reverse tcp:8080 tcp:8080          │
          └─ HP memanggil http://localhost:8080 ────┘   (diteruskan lewat kabel USB ke laptop)
```

- **App Android** (Java + XML Views, mulai Fase 05) menampilkan layar dan menyimpan token login di HP secara terenkripsi.
- **Backend** menyimpan akun, memeriksa password, dan membuat token. Semua endpoint diawali `/api`.
- **PostgreSQL** menyimpan data. Struktur tabelnya hanya diubah lewat file migrasi Flyway.

Satu akun bisa dimasuki lewat beberapa cara (email, Google, GitHub) dan punya dua mode (pembuat website, penyedia template).

---

## 2. Struktur backend

```
backend/src/main/java/com/aris/templateapp/
├── TemplateAppApplication.java   ← titik mulai app (method main)
├── config/       pengaturan: AppProperties, OpenApiConfig (Swagger), TimeConfig (Clock)
├── security/     SecurityConfig, JwtService, JwtAuthFilter, CurrentUser, TokenGenerator,
│                 GoogleTokenVerifier, GitHubOAuthClient (penghubung ke Google & GitHub)
├── common/
│   ├── exception/    ErrorCode, ApiException, GlobalExceptionHandler
│   ├── response/     ErrorResponse
│   ├── persistence/  PersistableEnum (+ converter) untuk enum ↔ teks database
│   └── util/         Emails (normalisasi email)
├── auth/         daftar/masuk/refresh/keluar, identitas login, refresh token, tiket,
│   │             AccountLinkingService (penyambungan), GitHubAuthService (alur GitHub)
│   └── dto/          RegisterRequest, LoginRequest, GoogleLoginRequest, AuthResponse, ...
├── user/         User, profil pembuat website, /users/me, metode login terhubung (IdentityController)
│   └── dto/          UserResponse, IdentityResponse
├── provider/     ProviderProfile (profil penyedia template + status)
└── seed/         DummyDataSeeder (10 user dummy, hanya profile dev)
```

Kode **dikelompokkan per fitur** (`auth/`, `user/`, `provider/`), bukan per jenis (`controllers/`, `services/`). Dengan cara ini, semua yang berhubungan dengan login ada di satu folder.

Resource (`backend/src/main/resources/`):

| File | Isi |
|---|---|
| `application.yml` | Konfigurasi bersama semua profile |
| `application-dev.yml` / `-prod.yml` | Tambahan khusus profile dev / prod |
| `db/migration/V*.sql` | Migrasi Flyway (struktur tabel) |

---

## 3. Lapisan kode: perjalanan satu request

Setiap fitur memakai lapisan yang sama:

```
Request HTTP
   │
   ▼
[Filter keamanan]  JwtAuthFilter: ada token sah? → tandai "sudah login"
   │
   ▼
[Controller]       AuthController / UserController
   │               - menerima JSON → DTO (record), memvalidasi dengan @Valid
   │               - TIDAK berisi logika bisnis
   ▼
[Service]          AuthService, RefreshTokenService, UserService
   │               - logika bisnis + @Transactional
   │               - melempar ApiException jika ada aturan yang dilanggar
   ▼
[Repository]       UserRepository, UserIdentityRepository, ...
   │               - interface Spring Data JPA; query dibuat otomatis dari nama method
   ▼
[Entity]           User, UserIdentity, RefreshToken, ...  ↔  tabel database
```

Hasilnya dikembalikan lewat jalur yang sama: Entity → diubah jadi **DTO** di service → dikirim controller sebagai JSON.

**Kenapa Entity tidak dikirim langsung?** Entity bisa berisi data rahasia (mis. `passwordHash`) dan relasi LAZY yang memicu query tak terduga. DTO hanya berisi field yang memang boleh dilihat app.

### Contoh: `POST /api/auth/login`

1. `JwtAuthFilter` melihat tidak ada header `Authorization`, jadi request diteruskan sebagai tamu. Ini boleh, karena `/api/auth/**` publik di `SecurityConfig`.
2. `AuthController.login` mengubah JSON jadi `LoginRequest`. `@Valid` memeriksa field wajib. Kalau gagal, `GlobalExceptionHandler` membalas `400 VALIDATION_ERROR`.
3. `AuthService.login`:
   - menormalkan email (`Emails.normalize`: huruf kecil, tanpa spasi)
   - mencari user; kalau tidak ada → `INVALID_CREDENTIALS`
   - mencari identitas `local`; kalau tidak ada → `USE_SOCIAL_LOGIN` (akun dibuat lewat Google/GitHub)
   - mencocokkan password dengan hash BCrypt; kalau salah → `INVALID_CREDENTIALS`
   - membuat access token (`JwtService`) dan refresh token (`RefreshTokenService`)
   - menyusun `UserResponse` (`UserService.toResponse`)
4. Controller mengembalikan `AuthResponse`, lalu Spring mengubahnya menjadi JSON.

### Dependency injection

Class bertanda `@Service`, `@Component`, `@Configuration`, `@RestController` dibuat sekali oleh Spring (disebut **bean**). Spring lalu "menyuntikkan" bean itu ke konstruktor class lain. Contohnya `AuthService` menerima `UserRepository`, `JwtService`, dan seterusnya lewat konstruktor. `@RequiredArgsConstructor` (Lombok) membuat konstruktor itu otomatis dari semua field `final`.

---

## 4. Konfigurasi dan profile

```
application.yml (selalu dibaca)
   + application-dev.yml   ← jika profile dev aktif  (membaca backend/.env, log DEBUG)
   + application-prod.yml  ← jika profile prod aktif (Swagger dimatikan)
   + application-test.yml  ← saat test (src/test/resources)
```

- Nilai rahasia **tidak** ditulis di yml, tapi dibaca dari environment variable: `${DB_PASSWORD}`. Bentuk `${NAMA:default}` berarti "pakai default kalau variabelnya tidak ada".
- Di profile dev, `spring.config.import: optional:file:.env[.properties]` membaca `backend/.env` sebagai daftar properti.
- Semua pengaturan `app.*` dikumpulkan di **`AppProperties`** (record). Class lain cukup meminta `AppProperties` lewat konstruktor. `@Validated` membuat app gagal start kalau, misalnya, `JWT_SECRET` kurang dari 32 karakter.
- **`Clock`** (`TimeConfig`) adalah sumber "waktu sekarang". Class lain tidak memanggil `Instant.now()` langsung, supaya test bisa memakai jam palsu.

---

## 5. Database

### Tabel dan relasi

```
users ─┬─< user_identities     (1 user : banyak identitas; maks 1 per provider)
       ├─< refresh_tokens      (1 user : banyak sesi/perangkat)
       ├─< auth_tickets        (tiket sekali pakai: state GitHub, hasil login, link)
       ├── creator_profiles    (1 : 0..1, primary key = user_id)
       ├── provider_profiles   (1 : 0..1, primary key = user_id)
       ├─< templates ──┬─< template_checks ──< template_check_issues
       │               └─< template_events   (dilihat / didownload)
       └─< notifications
```

| Tabel | Fungsi | Catatan penting |
|---|---|---|
| `users` | Satu akun | `email` unik tanpa beda huruf besar/kecil (index `lower(email)`); `active_mode` default `creator` |
| `user_identities` | Cara masuk: `local` (email+password), `google`, `github` | `password_hash` hanya untuk `local` (dijaga CHECK); `UNIQUE(provider, provider_user_id)` |
| `refresh_tokens` | Sesi login per perangkat | Hanya hash SHA-256 yang disimpan; `replaced_by` terisi saat dirotasi |
| `auth_tickets` | Tiket sementara (lihat bagian 7) | Hash, sekali pakai, ada masa berlaku; `payload` jsonb ↔ `Map` di Java |
| `creator_profiles` | Data mode pembuat website | Ada = user punya peran `creator` |
| `provider_profiles` | Data mode penyedia template + status `active`/`suspended` | Ada = user punya peran `provider` |
| `templates` | Template milik provider (Fase 11) | `status` draft/checking/published/check_failed/disabled; `warning_count` disimpan agar filter "Perlu perbaikan" cepat |
| `template_checks` + `template_check_issues` | Hasil pengecekan otomatis per versi + daftar error/peringatan | Pengecekan terakhir = `version` tertinggi |
| `template_events` | Kejadian dilihat & didownload | Index unik `(project_id, type)`: satu project dihitung sekali |
| `notifications` | Notifikasi di dalam app | `resolved_at` diisi saat masalahnya beres |

### Aturan migrasi Flyway

- File `V1__...sql` sampai `V11__...sql` dijalankan berurutan **sekali saja**. Riwayatnya dicatat di tabel `flyway_schema_history`.
- **Jangan pernah mengubah file migrasi yang sudah dijalankan.** Flyway menyimpan checksum setiap file. Kalau file berubah, app menolak start. Untuk mengubah struktur tabel, buat file baru `V7__...sql`.
- `spring.jpa.hibernate.ddl-auto=validate`: Hibernate hanya **mengecek** Entity cocok dengan tabel, tidak membuat atau mengubah tabel.

### Entity dan enum

- Entity (`@Entity`) memetakan satu baris tabel menjadi satu objek Java. Nama kolom `display_name` otomatis cocok dengan field `displayName`.
- Kolom enum di database berisi teks huruf kecil (`creator`, `google`), sesuai CHECK constraint. Di Java, enum memakai huruf besar (`ActiveMode.CREATOR`). Penghubungnya adalah `PersistableEnum` + `JpaConverter` di setiap enum. `@JsonValue` membuat JSON juga memakai huruf kecil (`"activeMode": "creator"`).
- Relasi `@ManyToOne(fetch = LAZY)`: data `User` milik sebuah `UserIdentity` baru diambil dari database saat benar-benar dipakai.

---

## 6. Keamanan: login, JWT, refresh token

### Dua jenis token

| | Access token (JWT) | Refresh token |
|---|---|---|
| Bentuk | `xxxxx.yyyyy.zzzzz` (JWT HS256, berisi `sub` = ID user) | 43 karakter acak |
| Umur | 15 menit | 30 hari |
| Dipakai untuk | Header `Authorization: Bearer <token>` di setiap request | Hanya untuk meminta access token baru |
| Disimpan di server? | **Tidak**. Server cukup memeriksa tanda tangannya | **Ya**, tapi hanya hash SHA-256-nya |
| Bisa dicabut? | Tidak (karena itu umurnya pendek) | Ya (keluar, rotasi) |

**Kenapa dua token?** Access token dikirim di setiap request, jadi umurnya dibuat pendek. Kalau sampai bocor, token itu cepat tidak berlaku. Refresh token jarang dikirim dan bisa dicabut, jadi user tidak perlu sering login ulang.

### Pemeriksaan setiap request (`JwtAuthFilter` + `SecurityConfig`)

```
Request ──► JwtAuthFilter
              ├─ header "Bearer <token>" sah?  → SecurityContext berisi UUID user
              └─ tidak ada / tidak sah          → tetap diteruskan sebagai tamu
          ──► SecurityConfig.authorizeHttpRequests
              ├─ /api/auth/**, Swagger, /error  → boleh tanpa login
              └─ selain itu, tamu               → 401 {"code":"UNAUTHORIZED"} (unauthorizedEntryPoint)
          ──► Controller → CurrentUser.id() mengambil UUID user
```

### Password

- Password disimpan sebagai hash **BCrypt** (`PasswordEncoder` di `SecurityConfig`). BCrypt sengaja lambat dan memakai "garam" acak, jadi hash yang sama tidak pernah muncul dua kali.
- Panjang password 8–72 karakter. BCrypt hanya membaca 72 byte pertama.
- Saat email tidak ditemukan, `AuthService` tetap menjalankan BCrypt terhadap hash palsu. Tujuannya supaya lama respons "email tidak ada" dan "password salah" sama, sehingga penyerang tidak bisa menebak email yang terdaftar dari waktu respons.

### Rotasi refresh token (`RefreshTokenService`)

```
Masuk          → token A dibuat
Refresh(A)     → A dicabut (replaced_by = B), token B dibuat      ✔
Refresh(B)     → B dicabut (replaced_by = C), token C dibuat      ✔
Refresh(A) lagi→ A sudah pernah ditukar! Kemungkinan dicuri.
                 → SEMUA token user dicabut (C ikut mati), balas 401 ✘
                 → pemilik asli harus login ulang; pencuri juga tidak bisa lanjut
Keluar(C)      → C dicabut (tanpa replaced_by)
```

Detail implementasi yang penting:
- `findByTokenHash` memakai **kunci baris** (`PESSIMISTIC_WRITE`). Kalau dua request refresh dengan token yang sama datang bersamaan, request kedua menunggu, lalu melihat token itu sudah dirotasi.
- `@Transactional(noRollbackFor = ApiException.class)`: pencabutan semua token harus tetap tersimpan walaupun method lalu melempar error 401. Tanpa ini, transaksi di-rollback dan pencabutannya batal.

---

## 7. Login Google & GitHub, penyambungan akun

### Satu bentuk data: `SocialProfile`

Google dan GitHub memberi data dengan bentuk yang berbeda. Penghubungnya mengubah data itu menjadi satu bentuk yang sama, yaitu `SocialProfile(provider, providerUserId, email, emailVerified, displayName, avatarUrl)`:

| Penghubung | Masukan | Yang dicek |
|---|---|---|
| `GoogleTokenVerifier` | `idToken` dari Credential Manager di HP | Tanda tangan Google, penerbit, kedaluwarsa, **audience = Web Client ID kita** |
| `GitHubOAuthClient` | `code` dari callback GitHub | Menukar `code` → access token GitHub (pakai client secret), lalu `/user` dan `/user/emails` (email **primary** + status **verified**) |

Setelah itu, `AccountLinkingService` hanya bekerja dengan `SocialProfile`, tanpa peduli datanya dari Google atau GitHub.

Yang dipakai sebagai kunci identitas adalah **`providerUserId`** (Google: `sub`, GitHub: ID angka), bukan email. Email bisa diganti user, tapi ID itu tidak berubah.

### Keputusan masuk (`AccountLinkingService.signIn`)

```
SocialProfile masuk
  │
  ├─ identitas (provider, providerUserId) sudah ada? ──ya──► masuk ke pemiliknya (isNewUser=false)
  │
  └─ belum ada
       │
       ├─ email TERVERIFIKASI & sudah dipakai user lain?
       │      └─ ya ──► JANGAN sambungkan otomatis.
       │               Simpan identitas di tiket LINK (10 menit), balas
       │               409 ACCOUNT_LINK_REQUIRED { linkToken, existingMethods }
       │
       └─ tidak / email kosong / belum terverifikasi
              └──► buat user baru + identitas (isNewUser=true)
                   (email yang belum terverifikasi TIDAK disimpan ke users.email)
```

**Kenapa tidak disambungkan otomatis?** Bayangkan seseorang membuat akun GitHub dan menulis email milikmu (tanpa verifikasi). Kalau backend langsung menyambungkan berdasarkan email, orang itu bisa masuk ke akunmu. Karena itu, penyambungan hanya terjadi setelah user **membuktikan** dirinya pemilik akun lama dengan cara masuk memakai metode lama.

### Menyelesaikan penyambungan (`completePendingLink`)

```
App menerima ACCOUNT_LINK_REQUIRED (linkToken, existingMethods=["google"])
  → tampilkan dialog "Email ini sudah terdaftar dengan Google. Masuk dengan Google untuk menyambungkan GitHub."
  → user masuk dengan Google sambil mengirim linkToken (POST /auth/google { idToken, linkToken })
  → backend: user hasil masuk == pemilik email di tiket?
        ya    → identitas GitHub disimpan ke akun itu, tiket ditandai terpakai
        tidak → 403 LINK_USER_MISMATCH
  → masuk GitHub berikutnya langsung ke akun yang sama
```

`linkToken` bisa dikirim lewat ketiga cara masuk: `/auth/login` (email), `/auth/google`, dan `/auth/github/authorize-url` (dibawa di state sampai callback).

### Alur GitHub (`GitHubAuthService`)

```
 App                         Backend                              GitHub
  │ POST /auth/github/authorize-url ─►│ buat tiket GITHUB_STATE (10 mnt)  │
  │◄──────────── { url } ─────────────│                                   │
  │ buka url di Custom Tab ───────────┼──────────────────────────────────►│ user login & setuju
  │                                   │◄── GET /auth/github/callback?code&state
  │                                   │ cek state, tukar code, logika 7 di atas
  │                                   │ buat tiket LOGIN_RESULT (2 mnt)
  │◄── redirect templateapp://auth/callback?ticket=XXX ───────────────────│
  │ POST /auth/github/exchange {ticket} ─►│                               │
  │◄──────────── AuthResponse ────────│                                   │
```

- **State** mencegah serangan CSRF: callback hanya diterima kalau state-nya dibuat oleh backend kita dan belum pernah dipakai.
- **Client secret GitHub hanya ada di backend.** App tidak pernah melihatnya.
- **Kenapa deep link membawa tiket, bukan token?** URL deep link bisa terbaca app lain atau tercatat di riwayat. Tiket hanya berumur 2 menit, sekali pakai, dan baru ditukar menjadi token lewat request biasa.
- Kalau gagal, redirect berisi `?error=KODE`. Untuk penyambungan: `?error=ACCOUNT_LINK_REQUIRED&linkToken=...&methods=google`.
- `GitHubAuthService` sengaja **tidak** `@Transactional`. Setiap langkah punya transaksinya sendiri, jadi error di satu langkah bisa ditangkap lalu diubah menjadi redirect.

### Tiket sekali pakai (`AuthTicketService`)

| Jenis | Umur | Dibuat saat | Dipakai saat |
|---|---|---|---|
| `GITHUB_STATE` | 10 menit | `authorize-url` | callback GitHub |
| `LOGIN_RESULT` | 2 menit | callback GitHub berhasil | `/auth/github/exchange` |
| `LINK` | 10 menit | `ACCOUNT_LINK_REQUIRED` | login berikutnya yang membawa `linkToken` |

- Sama seperti refresh token, yang disimpan hanya **hash** tiketnya.
- `issue` memakai `@Transactional(propagation = REQUIRES_NEW)`: tiket langsung tersimpan di transaksi tersendiri. Ini penting karena tiket LINK dibuat tepat sebelum service melempar `ACCOUNT_LINK_REQUIRED`. Tanpa transaksi tersendiri, error itu akan me-rollback tiketnya.

### Metode login terhubung (Pengaturan)

- `GET /users/me/identities`: daftar metode (`local`, `google`, `github`).
- `POST /users/me/identities/google` dan `.../github/authorize-url`: menyambungkan secara manual. Kecocokan email **tidak** diperlukan, karena user sudah membuktikan kepemilikan kedua akun dengan masuk ke keduanya (skenario 8: email GitHub berbeda atau privat).
- Ditolak dengan `IDENTITY_IN_USE` kalau akun Google/GitHub itu sudah dipakai akun lain, atau akun ini sudah punya Google/GitHub yang lain.
- `DELETE /users/me/identities/{provider}`: ditolak dengan `LAST_IDENTITY` kalau itu metode terakhir. Baris user dikunci (`findByIdForUpdate`) supaya dua request hapus yang bersamaan tidak sama-sama lolos.

---

## 8. Onboarding, mode, dan data dummy

### Peran dan mode

- **Peran** (`roles`) adalah apa yang **dimiliki** user. `creator` ada kalau user punya baris di `creator_profiles`, `provider` ada kalau punya baris di `provider_profiles`. Peran tidak disimpan sebagai kolom terpisah, tapi dihitung dari ada tidaknya profil (`UserService.toResponse`).
- **Mode** (`users.active_mode`) adalah dashboard yang sedang **dibuka**. Nilainya disimpan di server, jadi app membuka mode terakhir walaupun HP diganti.

```
Akun baru: onboardingCompleted=false, activeMode=creator, roles=[]
   │
   ├─ POST /users/me/onboarding/creator   (tombol "Mulai" ATAU "Lewati")
   │     → creator_profiles dibuat/diperbarui, onboardingCompleted=true, activeMode=creator
   │
   └─ POST /users/me/onboarding/provider  (juga dari menu "Jadi penyedia template" user lama)
         → provider_profiles dibuat (status active), onboardingCompleted=true, activeMode=provider
         → kedua kalinya: 409 PROVIDER_PROFILE_EXISTS
```

### Aturan beralih mode (`PATCH /users/me/active-mode`)

| Mode tujuan | Boleh jika | Kalau tidak |
|---|---|---|
| `creator` | Selalu. Dashboard ini juga dipakai tamu dan jadi tujuan saat provider ditangguhkan | – |
| `provider` | Punya profil provider **dan** status bukan `suspended` | `403 MODE_NOT_ALLOWED` |

Status provider hanya **`active`** (langsung setelah mengisi form, tanpa verifikasi) dan **`suspended`** (rem darurat). Perubahan status dilakukan lewat SQL, contohnya ada di README. Migrasi `V7` mengubah data lama `pending`/`approved`/`rejected` menjadi `active`. Kalau status berubah menjadi `suspended` saat `activeMode` masih `provider`, backend tidak mengubahnya diam-diam. App yang membaca `providerStatus = suspended` lalu membuka mode pembuat website dengan pesan (bagian 6.1 instruksi).

### Validasi form provider (`ProviderOnboardingRequest`)

Validasi ditulis sebagai anotasi di DTO, lalu dijalankan oleh `@Valid` di controller:

- `creatorName` wajib, maksimal 100
- `bio` maksimal 300
- `portfolioUrl` harus `http(s)://...` (`@Pattern`)
- `specialties` maksimal 10 item, masing-masing tidak kosong dan maksimal 50 (anotasi di **dalam** tipe generik: `List<@NotBlank @Size(max = 50) String>`)
- `agreedToTerms` harus `true` (`@AssertTrue`)

Semua pesan kesalahan dikirim dalam `fieldErrors`, supaya form di app bisa menandai field yang salah.

### Mencegah balapan (race condition)

`becomeProvider` mengambil user dengan `findByIdForUpdate` (kunci baris). Kalau dua request "daftar provider" datang bersamaan, request kedua menunggu sampai yang pertama selesai, lalu melihat profilnya sudah ada (`PROVIDER_PROFILE_EXISTS`). Tanpa kunci, keduanya bisa lolos pengecekan, dan request kedua berakhir dengan error database `500`.

### Data dummy (`DummyDataSeeder`)

- Class ini mengimplementasikan `ApplicationRunner`, yaitu kode yang dijalankan Spring sekali setelah app selesai start.
- `@Profile("dev")` membuatnya hanya aktif di laptop development, tidak pernah di production atau test.
- Seeder hanya berjalan kalau tabel `users` kosong, jadi data yang sudah ada tidak pernah tertimpa.
- Nama dibuat oleh Datafaker dari daftar nama Indonesia (Datafaker belum punya locale Indonesia), dengan `new Random(42)` supaya hasilnya selalu sama.
- Daftar akun dan password-nya ada di README bagian "Akun dummy".

---

## 9. Format error

Semua error berbentuk sama (`ErrorResponse`):

```json
{ "code": "VALIDATION_ERROR", "message": "Data tidak valid.", "fieldErrors": { "email": "Format email tidak valid" } }
{ "code": "USE_SOCIAL_LOGIN", "message": "Akun ini terdaftar dengan Google. Silakan masuk dengan Google.", "existingMethods": ["google"] }
{ "code": "ACCOUNT_LINK_REQUIRED", "message": "Email ini sudah terdaftar dengan Google. ...", "existingMethods": ["google"], "linkToken": "..." }
```

- `code`: dibaca oleh app untuk memilih pesan dan tindakan. Daftarnya ada di `ErrorCode.java`, lengkap dengan status HTTP-nya.
- `message`: pesan cadangan dalam Bahasa Indonesia.
- `fieldErrors` / `existingMethods` / `linkToken`: hanya muncul kalau relevan.

Cara kerjanya:
- Service melempar `new ApiException(ErrorCode.EMAIL_ALREADY_USED)`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) menangkap exception itu dan menulis `ErrorResponse` dengan status dari `ErrorCode`.
- Error tak terduga (bug) menjadi `500 INTERNAL_ERROR`. Detailnya hanya ditulis ke log server, tidak pernah dikirim ke app.
- Error 401 dari `SecurityConfig` terjadi **sebelum** controller, jadi ditulis sendiri oleh `unauthorizedEntryPoint` dengan bentuk yang sama.

---

## 10. Daftar endpoint

Coba semuanya di Swagger UI: http://localhost:8080/swagger-ui.html

| Method & path | Login? | Body | Hasil | Status |
|---|---|---|---|---|
| `POST /api/auth/register` | – | `{displayName, email, password}` | `201 AuthResponse` | ✅ Fase 02 |
| `POST /api/auth/login` | – | `{email, password, linkToken?}` | `AuthResponse` | ✅ Fase 02–03 |
| `POST /api/auth/refresh` | – | `{refreshToken}` | `AuthResponse` | ✅ Fase 02 |
| `POST /api/auth/logout` | – | `{refreshToken}` | `204` | ✅ Fase 02 |
| `GET /api/users/me` | ✔ | – | `UserResponse` | ✅ Fase 02 |
| `POST /api/auth/google` | – | `{idToken, linkToken?}` | `AuthResponse` / `409 ACCOUNT_LINK_REQUIRED` | ✅ Fase 03 |
| `POST /api/auth/github/authorize-url` | – | `{linkToken?}` | `{url}` | ✅ Fase 03 |
| `GET /api/auth/github/callback` | – | query `code`, `state`, `error` | `302` ke deep link | ✅ Fase 03 |
| `POST /api/auth/github/exchange` | – | `{ticket}` | `AuthResponse` | ✅ Fase 03 |
| `GET /api/users/me/identities` | ✔ | – | `[{provider, email, createdAt}]` | ✅ Fase 03 |
| `POST /api/users/me/identities/google` | ✔ | `{idToken}` | daftar identitas | ✅ Fase 03 |
| `POST /api/users/me/identities/github/authorize-url` | ✔ | – | `{url}` | ✅ Fase 03 |
| `DELETE /api/users/me/identities/{provider}` | ✔ | – | `204` / `409 LAST_IDENTITY` | ✅ Fase 03 |
| `POST /api/users/me/onboarding/creator` | ✔ | `{displayName, websitePurpose?, organizationName?}` | `UserResponse` | ✅ Fase 04 |
| `POST /api/users/me/onboarding/provider` | ✔ | `{creatorName, bio?, portfolioUrl?, specialties?, agreedToTerms}` | `UserResponse` / `409 PROVIDER_PROFILE_EXISTS` | ✅ Fase 04 |
| `PATCH /api/users/me/active-mode` | ✔ | `{mode}` | `UserResponse` / `403 MODE_NOT_ALLOWED` | ✅ Fase 04 |
| `GET /api/providers/me/dashboard?period=7d\|30d` | ✔ provider | – | Beranda: perlu tindakan, ringkasan, tren, populer | ✅ Fase 11 |
| `GET /api/providers/me/stats/downloads?period=` | ✔ provider | – | `[{date, count}]` | ✅ Fase 11 |
| `GET /api/providers/me/templates?status=&category=&sort=&q=&page=` | ✔ provider | – | daftar + jumlah per status | ✅ Fase 11 |
| `GET /api/providers/me/templates/{id}` | ✔ provider | – | detail + pengecekan terakhir | ✅ Fase 11 |
| `GET` / `PATCH /api/providers/me/profile` | ✔ provider | `{creatorName, bio?, portfolioUrl?, specialties?}` | profil + ringkasan | ✅ Fase 11 |
| `POST /api/templates/{id}/events` | – (tamu boleh) | `{type, projectId?, installId, occurredAt}` | `202` | ✅ Fase 11 |
| `GET /api/templates/{id}/checks/latest` | ✔ pemilik | – | hasil pengecekan | ✅ Fase 11 |
| `GET /api/notifications` · `/unread-count` · `PATCH /{id}/read` | ✔ | – | notifikasi | ✅ Fase 11 |
| `DELETE /api/dev/demo-templates` | – (profile dev saja) | – | `204` | ✅ Fase 11 |

Bentuk respons:

```json
// AuthResponse
{ "accessToken": "eyJ...", "refreshToken": "q3V...", "expiresIn": 900, "isNewUser": true, "user": { ... } }

// UserResponse
{ "id": "uuid", "displayName": "Aris", "email": "aris@mail.com", "avatarUrl": null,
  "activeMode": "creator", "onboardingCompleted": false,
  "roles": [], "providerStatus": null, "creatorProfile": null }
```

`roles` berisi `"creator"` kalau user punya `creator_profiles`, dan `"provider"` kalau punya `provider_profiles`. Sebelum onboarding, isinya kosong.

---

## 11. Test

Jalankan semua test dengan `cd backend && ./mvnw test` (Docker harus aktif).

| Jenis | Contoh | Ciri | Kecepatan |
|---|---|---|---|
| Unit test | `JwtServiceTest`, `GlobalExceptionHandlerTest` | Tanpa Spring penuh, tanpa database; objek dibuat manual | Sangat cepat |
| Unit test + mock | `AccountLinkingServiceTest` | Repository diganti **mock Mockito** (`@Mock`), jadi yang diuji hanya logika keputusan | Sangat cepat |
| Unit test + server palsu | `GitHubOAuthClientTest` | `MockRestServiceServer` meniru balasan GitHub, tanpa internet | Cepat |
| Integration test | `AuthFlowIntegrationTest`, `SocialAuthIntegrationTest`, `OnboardingIntegrationTest`, `DummyDataSeederTest`, `FlywayMigrationTest` | `@SpringBootTest` + PostgreSQL asli di Docker (Testcontainers) | Lebih lambat (start app + container) |

- `TestcontainersConfiguration` menyalakan container `postgres:18`. `@ServiceConnection` otomatis mengarahkan app ke container itu, jadi database laptop tidak tersentuh.
- `@ActiveProfiles("test")` membaca `src/test/resources/application-test.yml`, yang berisi JWT secret khusus test.
- Integration test memakai `MockMvc` untuk mengirim request HTTP palsu ke controller tanpa membuka port sungguhan.
- Setiap test auth memakai email acak (`uniqueEmail()`), jadi data antar-test tidak bentrok.
- `SocialAuthIntegrationTest` memakai `@MockitoBean GoogleTokenVerifier` (diganti total oleh mock) dan `@MockitoSpyBean GitHubOAuthClient` (objek asli, hanya `fetchProfile` yang dipalsukan). Dengan begitu, seluruh alur berjalan nyata tanpa menghubungi Google atau GitHub.

- `DummyDataSeederTest` memakai `@SpringBootTest(properties = "test.context=seeder")`. Properti yang berbeda membuat Spring menyiapkan context baru dengan database container sendiri, jadi tabel `users` dijamin kosong. Seeder dibuat manual (`new DummyDataSeeder(...)`) karena bean aslinya hanya ada di profile dev, lalu dijalankan di dalam `TransactionTemplate`.

**Mock vs spy:** mock adalah objek palsu yang semua method-nya kosong kecuali yang diatur dengan `when(...)`. Spy adalah objek asli yang hanya sebagian method-nya diganti dengan `doReturn(...)`.

---

## 12. Resep: menambah endpoint baru

Contoh: `GET /api/users/me/sessions` (daftar perangkat yang sedang login).

1. **DTO respons** di `user/dto/`: `record SessionResponse(UUID id, String deviceName, Instant createdAt)`.
2. **Repository**: tambah method di `RefreshTokenRepository`, mis. `List<RefreshToken> findByUserIdAndRevokedAtIsNull(UUID userId)`. Spring Data membuat query-nya dari nama method.
3. **Service**: method `@Transactional(readOnly = true)` yang memanggil repository lalu mengubah Entity → DTO.
4. **Controller**: `@GetMapping("/sessions")` di `UserController`, ambil user lewat `currentUser.id()`.
5. **Keamanan**: path di luar `/api/auth/**` otomatis wajib login. Tidak perlu mengubah `SecurityConfig`.
6. **Error**: untuk aturan yang dilanggar, lempar `ApiException` dengan `ErrorCode` yang sesuai (tambahkan kode baru di `ErrorCode` kalau perlu).
7. **Test**: tambah skenario di integration test, lalu jalankan `./mvnw test`.
8. **Swagger**: beri `@Operation(summary = "...")` supaya endpoint terbaca jelas.

Kalau butuh kolom atau tabel baru, buat migrasi `V7__...sql` terlebih dahulu, lalu sesuaikan Entity-nya.

---

## 13. Struktur app Android

```
android/
├── gradle/libs.versions.toml     ← daftar versi semua library (version catalog)
├── build.gradle.kts              ← plugin tingkat project
├── gradle.properties             ← memori Gradle 2 GB, configuration cache
└── app/
    ├── build.gradle.kts          ← SDK, BuildConfig (API_BASE_URL, GOOGLE_WEB_CLIENT_ID), dependency
    └── src/
        ├── main/                 ← kode & resource app (debug dan rilis)
        │   ├── java/com/aris/templateapp/
        │   │   ├── TemplateApp.java        @HiltAndroidApp
        │   │   ├── MainActivity.java       satu-satunya Activity
        │   │   ├── core/di/                NetworkModule, AppModule, RefreshClient
        │   │   ├── core/network/           AuthInterceptor, TokenAuthenticator, ApiErrorParser
        │   │   ├── core/storage/           TokenStorage, SessionStore
        │   │   ├── core/util/              Resource, Event, AppExecutors
        │   │   ├── data/model/             User, UserRole, ProviderStatus, LoginMethod, ApiError
        │   │   ├── data/remote/api|dto/    interface Retrofit + bentuk JSON
        │   │   ├── data/mapper/            UserMapper (DTO → model)
        │   │   └── ui/                     layar (Fragment) per fitur + ui/common
        │   ├── res/                        layout, values, font, drawable, navigation, ...
        │   └── assets/licenses/            lisensi font Geist (OFL)
        ├── debug/                ← hanya ikut di build debug: katalog komponen, izin HTTP localhost
        └── test/                 ← unit test JVM (./gradlew testDebugUnitTest)
```

### Satu Activity, banyak Fragment

`MainActivity` hanya berisi `NavHostFragment`. Setiap layar adalah **Fragment**, dan perpindahan antarlayar diatur oleh **Navigation Component** lewat `res/navigation/nav_graph.xml`. Keuntungannya, animasi, tombol kembali, dan pengiriman data antarlayar ditangani di satu tempat.

### Pola MVVM

```
Fragment (tampilan)  ──mengamati──►  LiveData<Resource<T>>  ◄──diisi──  ViewModel
     │                                                                     │
     └── klik tombol ─────────────── memanggil method ────────────────────►│
                                                                           ▼
                                                              Repository (Fase 06+)
                                                               ├─ Retrofit (backend)
                                                               └─ TokenStorage / SessionStore
```

- **Fragment** hanya menampilkan data dan meneruskan klik. Tidak ada logika bisnis di sini.
- **ViewModel** menyimpan state layar. State ini tetap ada walau HP diputar (Fragment dibuat ulang, ViewModel tidak). ViewModel **tidak boleh** menyimpan View, Fragment, atau Context Activity, supaya tidak bocor memori.
- **`Resource<T>`** membungkus status `LOADING` / `SUCCESS` / `ERROR`, jadi setiap layar menangani tiga keadaan itu dengan cara yang sama.
- **`Event<T>`** dipakai untuk hal yang hanya boleh terjadi sekali (pindah layar, snackbar). Tanpa ini, LiveData akan mengirim ulang nilai yang sama saat layar dibuat ulang.

### ViewBinding

Setiap layout `fragment_xxx.xml` otomatis punya class `FragmentXxxBinding` berisi semua View ber-`id`, jadi tidak perlu `findViewById`. Di Fragment, binding **wajib di-null-kan di `onDestroyView()`**. View Fragment bisa dihancurkan (mis. saat pindah layar) sementara Fragment-nya masih hidup di back stack. Kalau binding tidak di-null-kan, View lama tetap tertahan di memori.

### Hilt (dependency injection)

- `@HiltAndroidApp` di `TemplateApp` menyalakan Hilt. `@AndroidEntryPoint` di Activity/Fragment membuat field `@Inject` diisi otomatis.
- Class milik kita cukup diberi `@Inject` di konstruktornya (mis. `TokenStorage`).
- Objek dari library (Retrofit, OkHttp, Gson) dibuat di **module** (`NetworkModule`, `AppModule`) dengan method `@Provides`.
- `@Singleton` berarti hanya ada satu objek selama app hidup.

---

## 14. Tema dan token desain

Semua nilai desain didefinisikan **sekali** di `res/values/` lalu dipakai lewat nama. Layout tidak menulis kode warna, ukuran, atau teks langsung.

| File | Isi |
|---|---|
| `values/colors.xml` + `values-night/colors.xml` | Token warna terang & gelap dengan **nama yang sama**. Android memilih otomatis sesuai mode HP |
| `values/dimens.xml` | Spasi (kelipatan 4dp), radius, tinggi tombol, ukuran teks |
| `values/type.xml` | `TextAppearance.App.Display/Headline/Title/Body/BodySmall/Label/Button` |
| `values/styles.xml` | Gaya komponen: `Widget.App.Button` (+ `.Outlined`, `.Text`), `TextInputLayout`, `Card`, `Chip`, `Toolbar`, `Badge`, dll. |
| `values/themes.xml` | `Theme.App`: memetakan token ke atribut Material 3 dan memasang style komponen sebagai default |
| `color/selector_*.xml` | Warna yang berubah sesuai keadaan (terpilih, fokus, nonaktif) |
| `font/` | Geist Sans (4 ketebalan) + Geist Mono (2 ketebalan) |
| `drawable/ic_*.xml` | Ikon Material Symbols Outlined (warna otomatis mengikuti `?attr/colorOnSurface`) |

Cara tema bekerja: `Theme.App` menetapkan, misalnya, `materialButtonStyle = Widget.App.Button`. Akibatnya setiap `<Button>` di layout otomatis menjadi tombol utama monokrom. Untuk varian lain cukup tulis `style="@style/Widget.App.Button.Outlined"`.

- **Mode gelap:** parent tema `Theme.Material3.DayNight` + warna dari `values-night/`. Warna ikon status bar diatur `EdgeToEdge.enable()`.
- **Edge-to-edge:** mulai Android 15, konten digambar sampai ke balik status bar. `MainActivity` menambahkan padding seukuran status bar, navigation bar, dan keyboard (`WindowInsetsCompat`).
- **Font per ketebalan:** setiap `TextAppearance` menunjuk file font langsung (`@font/geist_semibold`), karena memilih ketebalan dari satu keluarga font baru didukung penuh mulai Android 9, sedangkan minSdk kita 8.0.
- **Katalog komponen** (`src/debug/.../ComponentCatalogActivity`): satu layar berisi semua token dan komponen, ditambah tombol ganti terang/gelap. Pakai katalog ini untuk memeriksa perubahan desain.

---

## 15. Jaringan: Retrofit, token, dan refresh otomatis

```
UserApi.me()  ──►  OkHttpClient
                     ├─ AuthInterceptor: tempel "Authorization: Bearer <access token>"
                     │                   (kecuali /api/auth/...)
                     ▼
                  backend ──► 200 ✔
                     │
                     └─► 401 ──► TokenAuthenticator (synchronized)
                                   ├─ token sudah diganti thread lain? → ulangi dengan token terbaru
                                   ├─ POST /auth/refresh (lewat client TERPISAH)
                                   │     ✔ simpan token baru → ulangi request (user tidak sadar)
                                   │     ✘ 401/400 → hapus sesi + event "sesi berakhir"
                                   └─ offline → biarkan sesi, request gagal sebagai error jaringan
```

- **Retrofit** mengubah interface Java (`@GET("users/me") Call<UserDto> me()`) menjadi request HTTP. Gson mengubah JSON ↔ DTO.
- **Kenapa `synchronized`?** Refresh token dirotasi di backend (dipakai sekali). Kalau dua request sama-sama me-refresh dengan token yang sama, request kedua dianggap pencurian dan semua sesi dicabut. Dengan `synchronized`, request kedua menunggu, lalu memakai token yang sudah diperbarui request pertama.
- **Kenapa client terpisah (`@RefreshClient`)?** Kalau `/auth/refresh` sendiri membalas 401 dan lewat client yang sama, authenticator akan terpanggil lagi tanpa akhir.
- **`ApiErrorParser`** mengubah kegagalan menjadi `ApiError`: `ErrorResponse` JSON → `code` dari backend; `IOException` → `NETWORK_ERROR`; lainnya → `UNKNOWN_ERROR`. Layar memilih teks dari `strings.xml` berdasarkan `code`.
- **DTO vs model:** `data/remote/dto` mengikuti bentuk JSON persis. `data/model` adalah bentuk yang enak dipakai UI (enum, Set). `UserMapper` menjadi jembatan keduanya, jadi kalau JSON berubah, cukup mapper yang diubah.
- **R8 (build rilis)** mengganti nama field menjadi pendek (a, b, c). Gson bergantung pada nama field, karena itu `keepRules/rules.keep` menjaga nama field di paket `dto`.

---

## 16. Penyimpanan sesi di HP

| Class | Isi | Cara simpan |
|---|---|---|
| `TokenStorage` | access token & refresh token | **Terenkripsi** AES-GCM; kuncinya di **Android Keystore**; hasilnya di SharedPreferences `secure_tokens` |
| `SessionStore` | salinan user terakhir (JSON `UserDto`), mode terakhir, penanda intro sudah dilihat, event "sesi berakhir" | SharedPreferences `session` (tidak rahasia) |

**Android Keystore:** area aman milik sistem. Kunci AES dibuat di sana dan tidak pernah bisa dibaca keluar, bahkan oleh app kita sendiri. App hanya bisa meminta Keystore untuk mengenkripsi atau mendekripsi. Akibatnya, kalau seseorang menyalin file `secure_tokens.xml` ke HP lain, isinya tidak bisa dibuka.

- **AES-GCM** butuh IV (angka acak sekali pakai) yang berbeda setiap enkripsi. IV disimpan bersama hasil enkripsi (`iv:ciphertext` dalam Base64).
- Kalau dekripsi gagal (kunci hilang, data rusak), token dihapus dan user dianggap belum login. App tidak crash.
- **Backup:** `allowBackup="false"`, dan `data_extraction_rules.xml` mengecualikan `secure_tokens.xml` dari backup cloud dan pemindahan antar-HP, karena token itu tidak bisa dibuka di HP lain.
- **Salinan user** dipakai supaya app tetap bisa dibuka saat offline (skenario 12 bagian 13.2). Data ini disimpan dalam bentuk DTO, karena nama field DTO dijaga tetap sama oleh aturan R8.

---

## 17. Saat app dibuka: splash, layar awal, intro, dashboard

### Urutan kejadian

```
Ikon app diketuk
  → Splash sistem (Theme.App.Starting: latar + ikon jendela browser)
  → MainActivity.onCreate
       installSplashScreen()  ← sebelum super.onCreate
       super.onCreate()
       StartupViewModel       ← SETELAH super.onCreate (Hilt butuh Activity yang siap)
       splash ditahan selama keputusan layar pertama belum ada
  → nav_graph mulai di StartupFragment (tanpa tampilan, tertutup splash)
       mengamati StartupViewModel → navigate(...) + popUpTo: StartupFragment dibuang dari back stack
  → Intro / Dashboard Pembuat Website / Dashboard Provider
```

### Keputusan layar pertama (bagian 6.1)

`StartupViewModel` mengurus urutan pengecekan, sedangkan `StartupDecision.forUser(user)` adalah **fungsi murni** (tanpa Android) yang memutuskan tujuan. Karena murni, keputusan ini bisa diuji dengan unit test biasa (`StartupDecisionTest`).

```
intro belum pernah dilihat?             → Intro
tidak ada token?                         → Dashboard Pembuat Website (tamu)
ada token → GET /users/me (thread latar)
    berhasil                             → forUser(user terbaru)
    gagal 401 & refresh gagal            → sesi sudah dihapus TokenAuthenticator → tamu
    offline / server error               → forUser(salinan user di HP)
forUser:
    onboarding belum selesai             → Pilih peran (Fase 09; sementara ke Dashboard Pembuat Website)
    mode provider & punya peran provider
        bukan suspended                  → Dashboard Provider
        suspended                        → Dashboard Pembuat Website + banner "ditangguhkan"
    selain itu                           → Dashboard Pembuat Website
```

**Kenapa ViewModel milik Activity?** `MainActivity` (untuk menahan splash) dan `StartupFragment` (untuk pindah layar) harus melihat keputusan yang **sama**. `new ViewModelProvider(requireActivity())` di Fragment mengambil objek ViewModel yang sama dengan milik Activity.

### Navigasi tanpa Safe Args

Pindah layar memakai ID aksi di `nav_graph.xml` (`R.id.action_startup_to_intro`), dan argumen dikirim lewat `Bundle` (`CreatorDashboardFragment.args(...)`). Plugin Safe Args (yang membuat class `...Directions`) tidak dipakai karena tidak ada di daftar library project.

### Intro

- `ViewPager2` + `IntroAdapter`. ViewPager2 memakai Adapter yang sama seperti RecyclerView, dan setiap halaman adalah satu item.
- Indikator halaman berupa titik yang dibuat dari kode. Titik aktif berbentuk pil panjang lewat selector `bg_page_dot` dengan `state_selected`.
- Di halaman terakhir, tombol "Lanjut" berubah menjadi "Mulai" dan "Lewati" disembunyikan.
- Penanda `intro_seen` disimpan di `SessionStore`, sehingga intro hanya muncul sekali.

### Dashboard "Segera hadir"

- `CurrentUserViewModel` membaca salinan user di HP setiap kali layar tampil (`onStart`). Kalau hasilnya null, user adalah tamu.
- `AppBarAccount` mengatur sisi kanan app bar: tombol **Masuk** untuk tamu, atau **avatar** (huruf depan nama, area sentuh 48dp) untuk user yang sudah login.
- Dashboard Pembuat Website masih "Segera hadir". Dashboard Provider sudah berisi data sungguhan sejak Fase 12 (bab 22).
- Status provider hanya `active` dan `suspended`. Provider yang `suspended` tidak pernah masuk Dashboard Provider: `StartupDecision` membukakan Dashboard Pembuat Website dengan `StatusBannerView` merah "Mode provider dinonaktifkan. Hubungi admin untuk informasi lebih lanjut."

### Animasi Lottie

- File `res/raw/*.json` dibuat oleh skrip `android/tools/generate_lottie.py` dari bentuk sederhana (garis, kotak, lingkaran). Gaya garisnya monokrom dan bukan aset milik pihak lain.
- Efek "menggambar garis" memakai **trim path**: bagian garis yang terlihat dianimasikan dari 0% sampai 100%.
- Di JSON garisnya hitam. `LottieTint.applyForeground(view)` mengganti warna semua layer menjadi `color_foreground` lewat *dynamic properties*, jadi animasi ikut tema terang/gelap.
- Animasi disembunyikan dari pembaca layar (`importantForAccessibility="no"`) karena hanya berfungsi sebagai hiasan.

---

## 18. Masuk & daftar dengan email

### Alur satu kali tekan "Masuk"

```
LoginFragment ── klik ──► AuthViewModel.login(email, password)
                              │ 1. AuthFormValidator (di HP, tanpa jaringan) → error per field? berhenti
                              │ 2. loading = true, tombol nonaktif ("Memuat…")
                              ▼  thread latar (AppExecutors.networkIO)
                          AuthRepository.login → POST /auth/login
                              ├─ berhasil: TokenStorage.saveTokens + SessionStore.saveUser
                              │            → success (Event) → PostLoginNavigator
                              └─ gagal:    failure (Event) → AuthFormBinder.showFailure
```

- **Validasi dua lapis.** HP memeriksa dulu (cepat, tanpa internet), lalu backend memeriksa lagi (wajib, karena request bisa saja dikirim tanpa lewat app). Aturan keduanya sama: email valid, password 8–72 karakter.
- **Event untuk sukses/gagal.** Kalau HP diputar, layar dibuat ulang dan mengamati LiveData lagi. Dengan `Event`, snackbar error dan perpindahan layar tidak terjadi dua kali.
- **Mencegah klik ganda.** Selama `loading`, request baru diabaikan dan tombol dinonaktifkan.
- **Coba lagi.** ViewModel menyimpan request terakhir (`Supplier`), jadi tombol "Coba lagi" di snackbar cukup memanggil `retry()`.

### Pesan error (bagian 6.2) — `ErrorMessages`

| `code` dari backend / app | Ditampilkan |
|---|---|
| `INVALID_CREDENTIALS` | "Email atau password salah." |
| `USE_SOCIAL_LOGIN` | "Akun ini terdaftar dengan {Google/GitHub}. Silakan masuk dengan {metode}." (nama metode dari `existingMethods`) |
| `EMAIL_ALREADY_USED` | "Email sudah terdaftar. Silakan masuk." |
| `NETWORK_ERROR` (dibuat app) | "Tidak ada koneksi. Periksa internet lalu coba lagi." + tombol **Coba lagi** |
| `VALIDATION_ERROR` + `fieldErrors` | Pesan langsung di bawah input yang salah |
| lainnya | Pesan dari server, atau "Terjadi kesalahan…" |

### Setelah berhasil — `PostLoginNavigator`

Tujuan dipilih dengan aturan yang **sama** seperti saat app dibuka (`StartupDecision.forUser`). User yang terakhir memakai mode provider langsung masuk ke Dashboard Provider. Semua layar sebelumnya dibuang (`setPopUpTo(nav_graph, true)`), jadi tombol kembali tidak membuka layar Masuk lagi. Dashboard lalu membaca ulang user (`onStart`), sehingga tombol Masuk berganti menjadi avatar.

### Tombol Google & GitHub

- Logo asli: "G" empat warna dari pedoman branding Google (warnanya tidak boleh diubah, jadi `iconTint="@null"`), dan logo GitHub dari Primer Octicons (satu warna, ikut warna teks tombol).
- Di layar Masuk, kedua tombol ada **di atas** form email (bagian 6.2). Di layar Daftar, letaknya **di bawah** sebagai alternatif.
- Aksi tombolnya dijelaskan di bab 19.

### Snackbar monokrom

`Widget.App.Snackbar` membalik warna: latarnya `color_foreground`, teks dan tombol aksinya `color_background`. Dengan begitu snackbar tetap kontras di mode terang maupun gelap, tanpa warna ungu bawaan Material.

---

## 19. Login Google & GitHub di Android

### Google (Credential Manager)

```
Tombol Google → GoogleSignInHelper.signIn(activity)
   → CredentialManager menampilkan lembar "pilih akun Google" milik sistem
   → GetSignInWithGoogleOption(serverClientId = GOOGLE_WEB_CLIENT_ID)
   → hasil: GoogleIdTokenCredential.getIdToken()
   → AuthViewModel.signInWithGoogle(idToken) → POST /auth/google
```

- `serverClientId` **wajib** berupa Client ID tipe **Web**. Google memasukkannya ke `aud` (audience) idToken, dan backend hanya menerima idToken dengan audience yang sama.
- Client **Android** (package + SHA-1) tidak ditulis di kode. Google memakainya untuk memastikan permintaan datang dari APK yang ditandatangani kunci terdaftar. Karena itu, setiap laptop developer perlu didaftarkan SHA-1-nya (lihat `panduan-kolaborator.md` bagian 7).
- User menutup lembar pilih akun → `GetCredentialCancellationException` → app diam saja, tanpa pesan error.

### GitHub (Custom Tabs + deep link)

```
Tombol GitHub → POST /auth/github/authorize-url → { url }
   → GitHubSignInHelper.open(url): Custom Tab (Chrome di dalam app)
   → user login GitHub → GitHub memanggil backend (localhost:8080 lewat adb reverse)
   → backend redirect ke templateapp://auth/callback?ticket=...
   → Android membuka MainActivity (intent-filter deep link, launchMode singleTask → onNewIntent)
   → MainActivity → AuthDeepLinks.publish(uri) → layar Masuk/Daftar → AuthViewModel.onGitHubCallback
   → POST /auth/github/exchange { ticket } → masuk
```

- **Intent filter** di `AndroidManifest.xml` (`scheme="templateapp" host="auth" path="/callback"`, kategori `BROWSABLE`) membuat browser boleh membuka app lewat alamat itu.
- **`singleTask`** membuat Activity yang sudah terbuka dipakai ulang. Deep link datang lewat `onNewIntent`, sehingga Custom Tab otomatis tertutup dan tidak ada Activity ganda.
- **`AuthDeepLinks`** (singleton) menyimpan deep link di LiveData. Layar yang sedang tampil langsung menerimanya, dan layar yang baru tampil setelah deep link datang juga tetap menerimanya. `Event` memastikan deep link diproses sekali saja.

### Penyambungan akun di app (`LinkAccountDialog`)

```
Backend: 409 ACCOUNT_LINK_REQUIRED { linkToken, existingMethods: ["google"] }
      atau deep link ?error=ACCOUNT_LINK_REQUIRED&linkToken=...&methods=google
  → AuthViewModel memancarkan LinkRequest(linkToken, existingMethods, newMethod)
  → LinkAccountDialog: "Email ini sudah terdaftar dengan Google. Masuk dengan Google untuk menyambungkan akun GitHub."
  → user pilih "Masuk dengan Google" → pendingLink disimpan + banner info di layar
  → login Google berikutnya otomatis membawa linkToken → backend menyambungkan GitHub ke akun itu
```

- `linkToken` hanya dikirim kalau metode yang dipakai termasuk `existingMethods` (`AuthViewModel.linkTokenFor`).
- Kalau metode lama adalah **email + password**: di layar Masuk, user cukup mengisi form. Di layar Daftar, app pindah ke layar Masuk dengan membawa `linkToken` sebagai argumen.
- Dialog berupa `DialogFragment` yang mengambil `AuthViewModel` milik layar induk (`requireParentFragment()`), sehingga tetap tampil saat HP diputar.

### `adb reverse` saat pengembangan

Login GitHub memakai **dua** jalur ke backend lewat `adb reverse`: app (`/auth/github/authorize-url`, `/exchange`) **dan** Custom Tab (callback dari GitHub). Kalau `adb reverse` hilang, app menampilkan "Tidak ada koneksi", padahal internet HP lancar. Skrip `android/tools/keep-adb-reverse.sh` (Windows: `.ps1`) memasangnya ulang setiap 3 detik ke semua sambungan HP.

---

## 20. Onboarding, menu profil, pengaturan, keluar

### Peta layar

```
Startup ──(onboarding belum selesai)──► Pilih peran ─┬─► Form pembuat website ─(Mulai/Lewati)─► Dashboard Pembuat Website
                                                     └─► Form provider ─(Kirim)─────────────► Dashboard Provider (langsung aktif)

Dashboard ── avatar ──► Menu profil (bottom sheet)
                          ├─ Beralih ke mode …            (hanya jika punya dua peran)
                          ├─ Jadi penyedia template        (hanya jika belum provider) ─► Form provider
                          ├─ Pengaturan ─► Metode login terhubung (sambungkan / lepaskan)
                          └─ Keluar ─► Dashboard Pembuat Website (tamu)
```

### `HomeNavigator`: satu aturan "pulang"

Setelah masuk, onboarding, beralih mode, atau keluar, app memanggil `HomeNavigator.navigateHome(fragment, user)`. Tujuannya dipilih dengan `StartupDecision.forUser(user)`, aturan yang sama seperti saat app dibuka. Kalau `user` null, artinya tamu. Semua layar sebelumnya dibuang dari back stack, sehingga tombol kembali tidak membuka form yang sudah selesai.

### Onboarding

- `OnboardingViewModel` dipakai oleh kedua form. Nama diisi otomatis dari salinan user di HP (nama dari Google/GitHub/daftar).
- `OnboardingFormValidator` memakai aturan yang **sama** dengan backend: nama ≤ 100, bio ≤ 300, URL harus `http(s)://`, persetujuan wajib.
- Tombol **Lewati** memanggil endpoint yang sama dengan **Mulai**, tapi hanya mengirim nama. Field `null` tidak dikirim oleh Gson.
- Chip tujuan website memakai `singleSelection`, sedangkan chip keahlian bisa dipilih lebih dari satu. Teks chip keahlian dikirim apa adanya.

### Menu profil (`ProfileSheet`)

- Berupa `BottomSheetDialogFragment` yang dibuka dari avatar lewat `getChildFragmentManager()` milik dashboard. Karena itu, sheet bisa memakai `requireParentFragment()` untuk bernavigasi lewat dashboard di belakangnya.
- Item ditampilkan sesuai peran user (bagian 6.6): "Beralih mode" hanya muncul kalau punya peran creator **dan** provider. "Jadi penyedia template" hanya muncul kalau belum punya peran provider.
- Setiap baris berupa satu `TextView` dengan ikon `app:drawableStartCompat` (lebih ringan daripada ImageView + TextView di dalam LinearLayout).

### Keluar (bagian 6.7)

`AuthRepository.logout()` memanggil `POST /auth/logout` untuk mencabut refresh token, lalu **selalu** menghapus token dan salinan user di HP, walaupun request gagal karena offline. Setelah itu app kembali ke Dashboard Pembuat Website sebagai tamu, dengan pesan "Kamu sudah keluar."

### Sesi berakhir

Kalau refresh token ditolak, `TokenAuthenticator` menghapus sesi lalu mengirim event. `MainActivity` menangkap event itu, pindah ke Dashboard Pembuat Website sebagai tamu, dan menampilkan "Sesi berakhir. Silakan masuk lagi."

### Metode login terhubung

- `SettingsViewModel` memuat `GET /users/me/identities`, dan `StateView` menampilkan keadaan memuat atau error + "Coba lagi".
- Satu baris per metode (Google, GitHub, email):
  - **Tersambung** → tombol **Lepaskan**. Tombol ini nonaktif kalau tinggal satu metode, karena backend juga menolaknya dengan `LAST_IDENTITY`.
  - **Google/GitHub belum tersambung** → tombol **Sambungkan**. Google lewat Credential Manager, GitHub lewat Custom Tab dengan hasil deep link `?result=linked`.
  - **Email belum ada** → tanpa tombol, karena belum ada endpoint untuk membuat password pada akun Google/GitHub.
- Melepas metode meminta konfirmasi dulu lewat dialog.

---

## 21. Backend Dashboard Provider

Rancangan lengkapnya ada di [`rancangan/alur-provider.md`](rancangan/alur-provider.md). Versi ini belum punya Upload, sehingga semua bagian yang menampilkan data dibangun sebagai **mesin siap pakai**. Datanya baru berasal dari test dan seeder demo, tapi langsung berfungsi begitu Upload tersedia.

### Siapa boleh memanggil `/providers/me/**`

`ProviderAccess.requireActive(userId)` dipanggil di awal setiap service provider:
- tidak punya profil provider → `403 PROVIDER_REQUIRED`
- status `suspended` → `403 PROVIDER_SUSPENDED` ("Mode provider dinonaktifkan. Hubungi admin…")

### Kenapa SQL biasa (`ProviderTemplateQueries`)

Statistik berisi hitungan dan pengelompokan (`COUNT … FILTER`, `GROUP BY`, `generate_series`). Bentuk seperti ini lebih jelas ditulis sebagai SQL lewat `JdbcClient` daripada lewat Entity JPA. Bagian SQL yang disambung (kondisi filter, urutan) hanya diambil dari daftar tetap di enum. Nilai dari user (kategori, kata kunci, halaman) selalu dikirim sebagai **parameter**, sehingga aman dari SQL injection. Tanda `%` dan `_` di kata kunci juga di-escape, supaya dicari sebagai huruf biasa.

### Angka Beranda

| Angka | Cara dihitung |
|---|---|
| **Aktif** | Jumlah template berstatus `published` (tidak terpengaruh periode) |
| **Dilihat** / **Didownload** | Jumlah `template_events` jenis `view`/`download` milik template provider sejak awal periode |
| **Periode** | `StatsPeriod`: 7 atau 30 hari terakhir **termasuk hari ini**, dihitung di zona waktu app (`app.timezone`, bawaan `Asia/Jakarta`) |

**Grafik tren:** `generate_series` membuat satu baris untuk **setiap tanggal** dalam periode, lalu digabung (LEFT JOIN) dengan hitungan download per tanggal. Tanggal tanpa download tetap muncul dengan nilai 0. Tanggal setiap event dihitung dengan `occurred_at AT TIME ZONE 'Asia/Jakarta'`, jadi download pukul 00.30 WIB masuk ke tanggal WIB yang benar, bukan tanggal UTC kemarin.

**Template populer:** hanya yang `published` dan punya download di periode itu. Urutannya berdasarkan download, lalu jumlah dilihat, maksimal 3.

**Perlu tindakan:** daftar ini dihitung dari **kondisi saat ini**, bukan disimpan. Isinya template `check_failed` (dengan jumlah error dari pengecekan terakhir), template tayang yang masih punya peringatan, draft, dan profil yang belum lengkap (bio atau keahlian kosong). Karena itu kartu otomatis hilang begitu masalahnya beres.

### Event dilihat/didownload (`TemplateEventService`)

- Boleh dipanggil **tanpa login**, karena tamu dihitung lewat `installId` (ID acak per instalasi app).
- Diabaikan (tetap dibalas `202`): template yang tidak tayang, dan event dari pemilik template sendiri.
- Download untuk `projectId` yang sama hanya tersimpan sekali. Ini dijaga oleh **index unik** di database dan `INSERT … ON CONFLICT DO NOTHING`, jadi tidak bisa terhitung ganda walaupun app mengirim ulang.
- `occurredAt` adalah waktu di HP, karena export bisa terjadi offline lalu dikirim belakangan. Waktu yang lebih dari 1 hari di masa depan ditolak.

### Template Anda

- Filter status: `all`, `published`, `needs_fix` (= tidak lolos + tayang dengan peringatan), `checking`, `draft`, `disabled`. Respons juga membawa **jumlah per status** untuk chip, misalnya "Tayang (4)". Filter kategori dan pencarian ikut diterapkan pada jumlah itu, tapi filter status tidak, supaya semua chip tetap terisi.
- Urutan: `updated` (bawaan), `downloads`, `views`, `name`. Paginasi 20 per halaman.
- Template milik provider lain dibalas `404` (bukan `403`), supaya keberadaannya tidak bocor.

### Seeder demo (`DemoTemplateSeeder`)

Hanya aktif kalau dua syarat terpenuhi: profile `dev` **dan** `app.seed.demo-templates=true` (`@ConditionalOnProperty`). `@Order(2)` memastikan seeder ini berjalan setelah `DummyDataSeeder`, yang membutuhkan tabel users masih kosong. Data demo dihapus dengan `DELETE /api/dev/demo-templates`: akun demo dihapus, dan semua datanya ikut terhapus lewat `ON DELETE CASCADE`.

Pelajaran dari test: template disimpan lewat JPA, sedangkan event ditulis lewat SQL langsung. Hibernate menunda penulisan ke database sampai akhir transaksi, jadi harus dipanggil `templateRepository.flush()` lebih dulu. Tanpa itu, foreign key `template_id` belum dikenali database.

### Yang sengaja belum dibuat

Upload, mode tandai, pengecekan otomatis sungguhan, push notification (FCM), dan pengingat berkala. Semuanya menunggu diskusi segmen Upload (bagian 9 rancangan).

---

## 22. Dashboard Provider di Android

Semua file ada di `android/app/src/main/java/com/aris/templateapp/ui/provider/`, kecuali grafik (`ui/common/AreaChartView.java`). Datanya diambil lewat `ProviderApi` → `ProviderRepository` (bab 21 untuk sisi backend).

### Peta layar

```
ProviderDashboardFragment  (shell: app bar + bottom navigation, ada di nav_graph)
 ├─ ProviderHomeFragment       tab Beranda          ┐
 ├─ ProviderUploadFragment     tab Upload (segera)  │ Fragment ANAK, ditukar dengan show/hide
 ├─ ProviderTemplatesFragment  tab Template Anda    │
 └─ ProviderProfileFragment    tab Profil           ┘

Dibuka DI ATAS shell lewat ProviderNav (tujuan di nav_graph, tombol kembali → tab yang sama):
 templateDetailFragment · guideFragment · providerProfileEditFragment · settingsFragment · linkedMethodsFragment
```

**Kenapa tab = Fragment anak dengan show/hide, bukan NavHost kedua?** Dengan show/hide, setiap tab dibuat sekali lalu hanya disembunyikan, jadi isi dan posisi scroll tetap utuh saat pindah tab. Layar lanjutan tetap memakai Navigation **utama**: `NavHostFragment.findNavController(fragmentAnak)` mencari ke atas sampai NavHostFragment terdekat, yaitu milik Activity. Kalau ada NavHost kedua di dalam shell, menu profil (`ProfileSheet`) akan menemukan NavHost yang salah saat membuka Pengaturan.

**Tab terpilih diingat** di field `selectedTab` dan `onSaveInstanceState`. Saat kembali dari detail, view shell dibuat ulang, tetapi Fragment anaknya masih tersimpan di `childFragmentManager`, sehingga tab yang sama langsung tampil lagi.

### Siapa memiliki data (ViewModel)

| ViewModel | Diambil dari | Alasan |
|---|---|---|
| `ProviderDashboardViewModel` | shell (`requireParentFragment()` di tab Beranda) | Shell butuh jumlah "Perlu tindakan" untuk **badge** tab Beranda; satu request dipakai bersama |
| `ProviderTemplatesViewModel` | **Activity** (`requireActivity()`) | "Filter terakhir diingat selama app terbuka" (rancangan 6.3), walau user keluar-masuk Dashboard Provider |
| `ProviderProfileViewModel` + `ProfileViewModel` | tab Profil | `ProfileViewModel` (menu profil) dipakai ulang untuk beralih mode & keluar |
| `TemplateDetailViewModel`, `ProviderProfileEditViewModel` | layarnya sendiri | |

**Memuat ulang diam-diam:** saat kembali ke tab (dari detail, edit profil, atau latar belakang), data diambil lagi tanpa layar loading penuh. Kalau gagal, data lama tetap tampil dan muncul snackbar. **Nomor request** (`generation`) dipakai supaya jawaban request lama, misalnya periode 7 hari yang datang setelah user memilih 30 hari, tidak menimpa data yang lebih baru.

**Mode provider ditangguhkan** saat app terbuka: `/providers/me/dashboard` membalas `PROVIDER_SUSPENDED`. ViewModel lalu memperbarui salinan user (`fetchMe`), dan shell membuka Dashboard Pembuat Website dengan banner "Mode provider dinonaktifkan…" (rancangan 3.2).

### Beranda

| Kondisi | Yang tampil |
|---|---|
| Belum punya template (`hasTemplates = false`) | Checklist provider baru: lengkapi profil → baca panduan → upload ("Segera hadir"), dengan progres "n dari 3" |
| Sudah punya template | Ringkasan (chip 7/30 hari), grafik tren, 3 template populer |
| Selalu | Perlu tindakan (disembunyikan jika kosong), Panduan, tombol Upload |

Baris di dalam kartu (Perlu tindakan, checklist, panduan) memakai satu layout serbaguna, `item_row.xml`, yang ditambahkan dari kode. Daftarnya pendek dan tetap, jadi tidak perlu RecyclerView.

Penanda "baru" pada panduan disimpan oleh `GuideStore` (SharedPreferences, per HP). Langkah "Baca panduan" di checklist selesai kalau salah satu panduan **yang sudah berisi** pernah dibuka.

### Grafik custom (`AreaChartView`)

Grafik ini digambar sendiri di `onDraw(Canvas)` (keputusan Aris: tanpa library):
1. **Skala Y** dihitung `niceMax()`: nilai terbesar dibulatkan ke atas menjadi 1/2/5 × 10ⁿ × 4. Hasilnya, 4 garis grid selalu berlabel bilangan bulat, misalnya 23 → sumbu 0–40.
2. **Garis** dibuat dengan `Path.lineTo` antar-titik. **Area** memakai path yang sama, ditutup ke dasar grafik, lalu diisi `LinearGradient` warna foreground dari alpha 70 ke 0.
3. **Label** memakai Geist Mono. Untuk 7 hari ditampilkan nama hari; untuk 30 hari tanggal setiap 5 hari, ditambah hari terakhir (`DownloadTrendChart`).
4. **Ketuk/geser**: `onTouchEvent` mencari titik terdekat dari posisi jari, lalu menggambar garis vertikal + tooltip "Rab, 30 Sep · 5 download". `requestDisallowInterceptTouchEvent(true)` mencegah ScrollView ikut menggeser layar saat jari menggeser grafik.
5. Semua warna diambil dari `@color/...`, jadi grafik otomatis ikut mode gelap.

`DownloadTrendChart` (data → titik) dan `niceMax` adalah fungsi murni, sehingga diuji dengan unit test biasa.

### Template Anda

- **Pencarian** menunggu 400 ms setelah user berhenti mengetik (`Handler.postDelayed`), supaya tidak mengirim request untuk setiap huruf. Tombol cari di keyboard langsung menjalankannya.
- **Chip status** menampilkan jumlah dari respons (`counts`). Dropdown kategori dan urutan memakai `PopupMenu`.
- **Paginasi**: `RecyclerView.OnScrollListener` memanggil `loadMore()` saat tersisa 5 kartu di bawah layar. `ConcatAdapter` menggabungkan `TemplateAdapter` (kartu) dengan `LoadingFooterAdapter` (indikator di bawah). `ListAdapter` + `DiffUtil` hanya menggambar kartu yang baru.
- **Keadaan**: kerangka (skeleton) saat halaman pertama dimuat; "Belum ada template…" + Buka panduan; "Tidak ada template dengan filter ini." + Hapus filter; error + Coba lagi. `StateView` sekarang bisa menampilkan satu tombol aksi selain "Coba lagi".
- `TemplateListState` adalah objek **tidak berubah** yang dibuat baru setiap kali ada perubahan, jadi layar cukup menggambar ulang dari satu objek.

### Detail, Profil, Edit profil

- Detail menampilkan status + waktu pengecekan ("Tidak lolos · 2 error · 4 Okt 11.40"), angka total, lalu daftar error (merah) dan peringatan (oranye) beserta file, baris, dan saran. Tombol aksi menunggu fitur Upload.
- Edit profil memakai **layout yang sama** dengan form provider onboarding (`fragment_provider_form.xml`). Judul dan tombol diganti, checkbox aturan disembunyikan, dan validasinya memakai `OnboardingFormValidator`. Keahlian yang tidak ada di daftar chip ditambahkan sebagai chip baru supaya tidak hilang saat disimpan.

### Teks & angka

`TemplateUi` mengubah nilai backend menjadi teks: status (`check_failed` → "Tidak lolos"), kategori, "Diperbarui 2 hari lalu" (`RelativeTime`, diuji), angka dengan pemisah ribuan Indonesia (1.234), dan tanggal "4 Okt 11.40" menurut zona waktu HP.

---

## 23. Glosarium

| Istilah | Arti singkat |
|---|---|
| Bean | Objek yang dibuat dan dikelola Spring, lalu disuntikkan ke class lain |
| DTO | Data Transfer Object: class/record khusus untuk data yang masuk/keluar lewat API |
| Entity | Class Java yang mewakili satu baris tabel database |
| Repository | Interface untuk membaca/menulis Entity; implementasinya dibuat otomatis oleh Spring Data |
| `@Transactional` | Semua perubahan database di method itu berhasil bersama atau batal bersama |
| JWT | JSON Web Token: token bertanda tangan yang bisa diperiksa tanpa menyimpan apa pun di server |
| Hash | Sidik jari satu arah dari sebuah teks; tidak bisa dikembalikan ke teks aslinya |
| BCrypt | Algoritma hash khusus password yang sengaja lambat |
| Rotasi token | Setiap kali refresh token dipakai, token itu diganti dengan yang baru |
| Flyway | Alat yang menjalankan file SQL migrasi secara berurutan |
| Testcontainers | Library yang menyalakan service sungguhan (PostgreSQL) di Docker untuk test |
| Profile | Kumpulan konfigurasi untuk lingkungan tertentu (`dev`, `prod`, `test`) |
| OAuth | Cara "masuk dengan akun lain" (Google/GitHub) tanpa app kita pernah melihat password akun itu |
| idToken | JWT buatan Google yang menyatakan "user ini adalah X", ditandatangani Google |
| State (OAuth) | Nilai acak yang dibawa bolak-balik selama login GitHub, untuk memastikan alurnya dimulai dari app kita |
| Deep link | Alamat seperti `templateapp://...` yang membuka app Android tertentu |
| Mock / spy | Objek palsu / setengah palsu di test untuk menggantikan bagian yang lambat atau eksternal |
| Fragment | Satu layar (atau bagian layar) di dalam Activity, dengan siklus hidupnya sendiri |
| ViewModel | Penyimpan state layar yang tetap hidup walau layar dibuat ulang (mis. HP diputar) |
| LiveData | Wadah data yang memberi tahu layar setiap kali isinya berubah, hanya saat layar aktif |
| ViewBinding | Class otomatis berisi semua View ber-id dari sebuah layout |
| Hilt | Library dependency injection untuk Android (dibangun di atas Dagger) |
| Interceptor / Authenticator (OkHttp) | Kode yang menyisipi setiap request / dipanggil saat server membalas 401 |
| Android Keystore | Tempat penyimpanan kunci kriptografi yang dijaga sistem Android |
| R8 | Pengecil & pengacak kode untuk build rilis |
| Edge-to-edge | Konten app digambar sampai ke balik status bar dan navigation bar |
| Version catalog | File `libs.versions.toml` berisi semua versi library di satu tempat |
| Splash screen | Layar pembuka yang ditampilkan sistem sampai app siap |
| Back stack | Tumpukan layar yang dikunjungi; tombol kembali mengambil layar teratas |
| Lottie | Format animasi vektor berbasis JSON yang diputar oleh library Lottie |
| Trim path | Efek Lottie yang hanya menampilkan sebagian garis, dipakai untuk animasi "menggambar" |
| Credential Manager | API Android untuk login (akun Google, passkey, sandi tersimpan) lewat lembar milik sistem |
| Custom Tab | Browser (mis. Chrome) yang terbuka di dalam app, berbagi login dengan browser HP |
| Intent filter | Deklarasi di manifest tentang alamat/aksi yang bisa membuka sebuah Activity |
| DialogFragment | Dialog yang dikelola seperti Fragment, sehingga bertahan saat layar dibuat ulang |
| Bottom sheet | Panel yang muncul dari bawah layar, dipakai untuk menu profil |
| Compound drawable | Ikon yang ditempel langsung di sisi TextView (`drawableStart`), tanpa ImageView terpisah |
| `generate_series` | Fungsi PostgreSQL yang membuat deret nilai (mis. satu baris per tanggal) |
| `COUNT(*) FILTER (WHERE …)` | Menghitung hanya baris yang memenuhi syarat, beberapa hitungan sekaligus dalam satu query |
| `ON CONFLICT DO NOTHING` | Lewati INSERT yang melanggar index unik, tanpa error |
| `flush()` (JPA) | Memaksa Hibernate menulis perubahan yang tertahan ke database sekarang juga |
| Bottom navigation | Baris tab di bawah layar untuk berpindah antar-bagian utama app |
| Fragment anak | Fragment di dalam Fragment lain, dikelola `getChildFragmentManager()` |
| Custom View | Class turunan `View` yang menggambar sendiri isinya di `onDraw(Canvas)` |
| `Path` / `LinearGradient` | Bentuk garis bebas / isian warna bergradasi untuk menggambar di Canvas |
| Paginasi | Memuat data sedikit demi sedikit per halaman (di sini 20 per halaman) |
| `DiffUtil` | Pembanding dua daftar untuk mencari item yang berubah, supaya RecyclerView hanya menggambar ulang yang perlu |
| Debounce | Menunggu jeda tertentu setelah aksi terakhir sebelum bertindak (mis. pencarian saat mengetik) |
| Skeleton | Kartu abu-abu berbentuk isi yang tampil selama data dimuat |
