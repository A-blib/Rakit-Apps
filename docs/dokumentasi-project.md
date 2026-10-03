# Dokumentasi Project: App Template Website

Dokumen ini menjelaskan **cara kerja project dari dalam**: bagian-bagiannya, cara bagian itu saling terhubung, dan alasan dibuat seperti itu. Bacalah dari atas ke bawah. Setiap bagian merujuk ke file aslinya supaya bisa langsung dibuka di VS Code atau Android Studio.

- Cara menyiapkan dan menjalankan project → `README.md`
- Catatan per fase (yang dikerjakan, latihan) → `docs/catatan-belajar/`
- Dokumen ini → **memahami** project

> Status dokumen: diperbarui sampai **Fase 02** (auth email). Bagian yang belum dibangun ditandai _(belum)_.

---

## Daftar isi

1. [Gambaran besar](#1-gambaran-besar)
2. [Struktur backend](#2-struktur-backend)
3. [Lapisan kode: perjalanan satu request](#3-lapisan-kode-perjalanan-satu-request)
4. [Konfigurasi dan profile](#4-konfigurasi-dan-profile)
5. [Database](#5-database)
6. [Keamanan: login, JWT, refresh token](#6-keamanan-login-jwt-refresh-token)
7. [Format error](#7-format-error)
8. [Daftar endpoint](#8-daftar-endpoint)
9. [Test](#9-test)
10. [Resep: menambah endpoint baru](#10-resep-menambah-endpoint-baru)
11. [Glosarium](#11-glosarium)

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
├── security/     SecurityConfig, JwtService, JwtAuthFilter, CurrentUser, TokenGenerator
├── common/
│   ├── exception/    ErrorCode, ApiException, GlobalExceptionHandler
│   ├── response/     ErrorResponse
│   ├── persistence/  PersistableEnum (+ converter) untuk enum ↔ teks database
│   └── util/         Emails (normalisasi email)
├── auth/         daftar/masuk/refresh/keluar + identitas login + refresh token
│   └── dto/          RegisterRequest, LoginRequest, RefreshTokenRequest, AuthResponse
├── user/         User, profil pembuat website, /users/me
│   └── dto/          UserResponse
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
       ├─< auth_tickets        (tiket sekali pakai: state GitHub, hasil login, link) (belum dipakai)
       ├── creator_profiles    (1 : 0..1, primary key = user_id)
       └── provider_profiles   (1 : 0..1, primary key = user_id)
```

| Tabel | Fungsi | Catatan penting |
|---|---|---|
| `users` | Satu akun | `email` unik tanpa beda huruf besar/kecil (index `lower(email)`); `active_mode` default `creator` |
| `user_identities` | Cara masuk: `local` (email+password), `google`, `github` | `password_hash` hanya untuk `local` (dijaga CHECK); `UNIQUE(provider, provider_user_id)` |
| `refresh_tokens` | Sesi login per perangkat | Hanya hash SHA-256 yang disimpan; `replaced_by` terisi saat dirotasi |
| `auth_tickets` | Tiket sementara _(belum dipakai, Fase 03)_ | Hash, sekali pakai, ada masa berlaku |
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

## 7. Format error

Semua error berbentuk sama (`ErrorResponse`):

```json
{ "code": "VALIDATION_ERROR", "message": "Data tidak valid.", "fieldErrors": { "email": "Format email tidak valid" } }
{ "code": "USE_SOCIAL_LOGIN", "message": "Akun ini terdaftar dengan Google. Silakan masuk dengan Google.", "existingMethods": ["google"] }
```

- `code`: dibaca oleh app untuk memilih pesan dan tindakan. Daftarnya ada di `ErrorCode.java`, lengkap dengan status HTTP-nya.
- `message`: pesan cadangan dalam Bahasa Indonesia.
- `fieldErrors` / `existingMethods`: hanya muncul kalau relevan.

Cara kerjanya:
- Service melempar `new ApiException(ErrorCode.EMAIL_ALREADY_USED)`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) menangkap exception itu dan menulis `ErrorResponse` dengan status dari `ErrorCode`.
- Error tak terduga (bug) menjadi `500 INTERNAL_ERROR`. Detailnya hanya ditulis ke log server, tidak pernah dikirim ke app.
- Error 401 dari `SecurityConfig` terjadi **sebelum** controller, jadi ditulis sendiri oleh `unauthorizedEntryPoint` dengan bentuk yang sama.

---

## 8. Daftar endpoint

Coba semuanya di Swagger UI: http://localhost:8080/swagger-ui.html

| Method & path | Login? | Body | Hasil | Status |
|---|---|---|---|---|
| `POST /api/auth/register` | – | `{displayName, email, password}` | `201 AuthResponse` | ✅ Fase 02 |
| `POST /api/auth/login` | – | `{email, password}` | `AuthResponse` | ✅ Fase 02 (`linkToken` di Fase 03) |
| `POST /api/auth/refresh` | – | `{refreshToken}` | `AuthResponse` | ✅ Fase 02 |
| `POST /api/auth/logout` | – | `{refreshToken}` | `204` | ✅ Fase 02 |
| `GET /api/users/me` | ✔ | – | `UserResponse` | ✅ Fase 02 |
| `POST /api/auth/google` | – | `{idToken, linkToken?}` | `AuthResponse` | _(belum, Fase 03)_ |
| `POST /api/auth/github/authorize-url` · `GET .../callback` · `POST .../exchange` | – | | | _(belum, Fase 03)_ |
| `GET/POST/DELETE /api/users/me/identities...` | ✔ | | | _(belum, Fase 03)_ |
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

## 9. Test

Jalankan semua test dengan `cd backend && ./mvnw test` (Docker harus aktif).

| Jenis | Contoh | Ciri | Kecepatan |
|---|---|---|---|
| Unit test | `JwtServiceTest`, `GlobalExceptionHandlerTest` | Tanpa Spring penuh, tanpa database; objek dibuat manual | Sangat cepat |
| Integration test | `AuthFlowIntegrationTest`, `FlywayMigrationTest` | `@SpringBootTest` + PostgreSQL asli di Docker (Testcontainers) | Lebih lambat (start app + container) |

- `TestcontainersConfiguration` menyalakan container `postgres:18`. `@ServiceConnection` otomatis mengarahkan app ke container itu, jadi database laptop tidak tersentuh.
- `@ActiveProfiles("test")` membaca `src/test/resources/application-test.yml`, yang berisi JWT secret khusus test.
- Integration test memakai `MockMvc` untuk mengirim request HTTP palsu ke controller tanpa membuka port sungguhan.
- Setiap test auth memakai email acak (`uniqueEmail()`), jadi data antar-test tidak bentrok.

---

## 10. Resep: menambah endpoint baru

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

## 11. Glosarium

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
