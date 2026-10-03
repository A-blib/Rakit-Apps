# Dokumentasi Project: App Template Website

Dokumen ini menjelaskan **cara kerja project dari dalam**: bagian-bagiannya, cara bagian itu saling terhubung, dan alasan dibuat seperti itu. Bacalah dari atas ke bawah. Setiap bagian merujuk ke file aslinya supaya bisa langsung dibuka di VS Code atau Android Studio.

- Cara menyiapkan dan menjalankan project → `README.md`
- Catatan per fase (yang dikerjakan, latihan) → `docs/catatan-belajar/`
- Dokumen ini → **memahami** project

> Status dokumen: diperbarui sampai **Fase 03** (Google, GitHub, penyambungan akun). Bagian yang belum dibangun ditandai _(belum)_.

---

## Daftar isi

1. [Gambaran besar](#1-gambaran-besar)
2. [Struktur backend](#2-struktur-backend)
3. [Lapisan kode: perjalanan satu request](#3-lapisan-kode-perjalanan-satu-request)
4. [Konfigurasi dan profile](#4-konfigurasi-dan-profile)
5. [Database](#5-database)
6. [Keamanan: login, JWT, refresh token](#6-keamanan-login-jwt-refresh-token)
7. [Login Google & GitHub, penyambungan akun](#7-login-google--github-penyambungan-akun)
8. [Format error](#8-format-error)
9. [Daftar endpoint](#9-daftar-endpoint)
10. [Test](#10-test)
11. [Resep: menambah endpoint baru](#11-resep-menambah-endpoint-baru)
12. [Glosarium](#12-glosarium)

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

- **App Android** _(belum, mulai Fase 05)_ menampilkan layar dan menyimpan token login di HP.
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
└── provider/     ProviderProfile (profil penyedia template + status)
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
       └── provider_profiles   (1 : 0..1, primary key = user_id)
```

| Tabel | Fungsi | Catatan penting |
|---|---|---|
| `users` | Satu akun | `email` unik tanpa beda huruf besar/kecil (index `lower(email)`); `active_mode` default `creator` |
| `user_identities` | Cara masuk: `local` (email+password), `google`, `github` | `password_hash` hanya untuk `local` (dijaga CHECK); `UNIQUE(provider, provider_user_id)` |
| `refresh_tokens` | Sesi login per perangkat | Hanya hash SHA-256 yang disimpan; `replaced_by` terisi saat dirotasi |
| `auth_tickets` | Tiket sementara (lihat bagian 7) | Hash, sekali pakai, ada masa berlaku; `payload` jsonb ↔ `Map` di Java |
| `creator_profiles` | Data mode pembuat website | Ada = user punya peran `creator` |
| `provider_profiles` | Data mode penyedia template + status verifikasi | Ada = user punya peran `provider` |

### Aturan migrasi Flyway

- File `V1__...sql` sampai `V6__...sql` dijalankan berurutan **sekali saja**. Riwayatnya dicatat di tabel `flyway_schema_history`.
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

## 8. Format error

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

## 9. Daftar endpoint

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
| `POST /api/users/me/onboarding/creator` · `/provider` | ✔ | | | _(belum, Fase 04)_ |
| `PATCH /api/users/me/active-mode` | ✔ | `{mode}` | | _(belum, Fase 04)_ |

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

## 10. Test

Jalankan semua test dengan `cd backend && ./mvnw test` (Docker harus aktif).

| Jenis | Contoh | Ciri | Kecepatan |
|---|---|---|---|
| Unit test | `JwtServiceTest`, `GlobalExceptionHandlerTest` | Tanpa Spring penuh, tanpa database; objek dibuat manual | Sangat cepat |
| Unit test + mock | `AccountLinkingServiceTest` | Repository diganti **mock Mockito** (`@Mock`), jadi yang diuji hanya logika keputusan | Sangat cepat |
| Unit test + server palsu | `GitHubOAuthClientTest` | `MockRestServiceServer` meniru balasan GitHub, tanpa internet | Cepat |
| Integration test | `AuthFlowIntegrationTest`, `SocialAuthIntegrationTest`, `FlywayMigrationTest` | `@SpringBootTest` + PostgreSQL asli di Docker (Testcontainers) | Lebih lambat (start app + container) |

- `TestcontainersConfiguration` menyalakan container `postgres:18`. `@ServiceConnection` otomatis mengarahkan app ke container itu, jadi database laptop tidak tersentuh.
- `@ActiveProfiles("test")` membaca `src/test/resources/application-test.yml`, yang berisi JWT secret khusus test.
- Integration test memakai `MockMvc` untuk mengirim request HTTP palsu ke controller tanpa membuka port sungguhan.
- Setiap test auth memakai email acak (`uniqueEmail()`), jadi data antar-test tidak bentrok.
- `SocialAuthIntegrationTest` memakai `@MockitoBean GoogleTokenVerifier` (diganti total oleh mock) dan `@MockitoSpyBean GitHubOAuthClient` (objek asli, hanya `fetchProfile` yang dipalsukan). Dengan begitu, seluruh alur berjalan nyata tanpa menghubungi Google atau GitHub.

**Mock vs spy:** mock adalah objek palsu yang semua method-nya kosong kecuali yang diatur dengan `when(...)`. Spy adalah objek asli yang hanya sebagian method-nya diganti dengan `doReturn(...)`.

---

## 11. Resep: menambah endpoint baru

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

## 12. Glosarium

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
