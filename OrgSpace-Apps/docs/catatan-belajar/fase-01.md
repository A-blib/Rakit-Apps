# Fase 01: Fondasi backend

## Yang dikerjakan
| File | Fungsi |
|---|---|
| (root repo) | Repo dipindah dari `Rakit Apps/template-app/` ke `Rakit Apps/` atas permintaan Aris; riwayat Git tetap utuh |
| `backend/pom.xml`, `mvnw`, `.mvn/` | Project Maven dari start.spring.io (Spring Boot 4.1.1, Java 21) + JJWT, Google API Client, springdoc, Datafaker |
| `backend/.env.example` | Daftar environment variable tanpa nilai rahasia |
| `application.yml` | Konfigurasi bersama: datasource dari env var, `ddl-auto=validate`, `open-in-view=false`, nilai `app.*` |
| `application-dev.yml` | Membaca `backend/.env` otomatis, log DEBUG |
| `application-prod.yml` | Mematikan Swagger UI dan `/v3/api-docs` |
| `config/AppProperties.java` | `@ConfigurationProperties(prefix = "app")`: jwt, google, github, deepLink, divalidasi saat start |
| `config/OpenApiConfig.java` | Judul Swagger UI + skema `bearerAuth` untuk tombol Authorize (dipakai Fase 02) |
| `security/SecurityConfig.java` | Versi awal: stateless, tanpa CSRF, membuka Swagger UI dan `/error`, sisanya wajib login |
| `common/exception/ErrorCode.java` | Semua kode error API beserta status HTTP dan pesan default |
| `common/exception/ApiException.java` | Exception untuk error yang direncanakan, membawa `ErrorCode` |
| `common/exception/GlobalExceptionHandler.java` | Mengubah exception jadi `ErrorResponse` (validasi, JSON rusak, 404, error tak terduga) |
| `common/response/ErrorResponse.java` | Bentuk JSON error: `code`, `message`, `fieldErrors` |
| `db/migration/V1`–`V6` | Tabel `users`, `user_identities`, `refresh_tokens`, `auth_tickets`, `creator_profiles`, `provider_profiles` |
| `backend/.vscode/` | Rekomendasi extension, settings, task jalankan/test, launch debug profile dev |
| `backend/.editorconfig` | Indentasi 4 spasi (YAML/JSON 2 spasi), akhir baris LF |
| Test (`src/test/`) | `TemplateAppApplicationTests`, `FlywayMigrationTest`, `GlobalExceptionHandlerTest`, `application-test.yml` |
| `.gitignore` | Tambah file buatan extension Java VS Code (`.classpath`, `.project`, `.settings/`, `.factorypath`) |

## Alasan keputusan
- Spring Boot **4.1.1** → versi stabil terbaru di start.spring.io (alternatif: 4.2.0-M2, tidak dipilih karena masih milestone/belum stabil).
- springdoc **3.1.1** → seri 3.x dibuat untuk Spring Boot 4 (alternatif: springdoc 2.x, tidak dipilih karena untuk Spring Boot 3).
- `.env` dibaca lewat `spring.config.import=optional:file:.env[.properties]` di profile dev → cukup `./mvnw spring-boot:run`, tanpa `source .env` (alternatif: wajib `source .env` setiap buka terminal, tidak dipilih karena mudah lupa dan errornya membingungkan).
- `AppProperties` berupa `record` + `@Validated` → nilai wajib yang kosong langsung menggagalkan start dengan pesan jelas (alternatif: `@Value` di banyak class, tidak dipilih karena tersebar dan tidak tervalidasi).
- `ErrorCode` sebagai enum → kode error, status HTTP, dan pesan default ada di satu tempat; app Android cukup membaca `code` (alternatif: menulis string kode di setiap `throw`, tidak dipilih karena rawan salah ketik).
- Unik email lewat `UNIQUE INDEX ... (lower(email))` → "Aris@Mail.com" dan "aris@mail.com" dianggap sama di level database (alternatif: extension `citext`, tidak dipilih karena butuh `CREATE EXTENSION` dengan hak superuser).
- CHECK `(provider = 'local') = (password_hash IS NOT NULL)` → identitas Google/GitHub tidak mungkin punya password, dan identitas email wajib punya password.
- `provider_profiles.specialties` diberi `NOT NULL DEFAULT '{}'` → kode Java tidak perlu membedakan "null" dan "daftar kosong".
- `SecurityConfig` sudah dibuat sekarang (versi minimal) → tanpa ini Spring Security mengunci semua URL termasuk Swagger UI, padahal syarat fase ini Swagger UI terbuka.
- Image test `postgres:18` → sama dengan versi mayor PostgreSQL di laptop (alternatif: `postgres:latest` bawaan generator, tidak dipilih karena versinya bisa berubah diam-diam).
- File `TestTemplateAppApplication` dari generator dihapus → kita memakai PostgreSQL laptop untuk development, jadi file itu tidak terpakai.

## Cara menjalankan & mengetes
1. Pastikan `backend/.env` terisi (`DB_*` dan `JWT_SECRET`; sudah diisi di fase ini).
2. `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` → tunggu `Started TemplateAppApplication`.
3. Buka http://localhost:8080/swagger-ui.html → halaman Swagger tampil (belum ada endpoint).
4. Cek tabel: `psql -h localhost -U templateapp -d templateapp -c "\dt"` → 6 tabel + `flyway_schema_history`.
5. Dari VS Code: buka folder `backend/`, *Run and Debug* → **Backend: debug (dev)** → F5.
6. Test: `./mvnw test` (Docker harus aktif).

## Hasil tes
- `./mvnw test`: **lulus, 10 test** (FlywayMigrationTest 5, GlobalExceptionHandlerTest 4, TemplateAppApplicationTests 1).
- Uji manual (profile dev, database laptop): app start dalam ±3 detik, 6 migrasi berhasil (`flyway_schema_history` V1–V6 `success = t`), `/swagger-ui.html` → 200, `/v3/api-docs` → 200.
- Start dari VS Code belum dicoba langsung oleh agent (butuh GUI); perlu dicoba Aris.
- Warning yang sengaja dibiarkan:
  - `Using generated security password` → muncul karena belum ada sistem login; hilang/diganti di Fase 02.
  - Request ke `/api/...` tanpa login masih dibalas `403` dengan format bawaan Spring → di Fase 02 diganti `401 UNAUTHORIZED` berformat `ErrorResponse`.
  - Warning Mockito/"Java agent loaded dynamically" saat test → berasal dari library test, tidak memengaruhi hasil.

## Konsep yang dipelajari
- Profile Spring : kumpulan konfigurasi yang aktif sesuai lingkungan (`dev`, `prod`, `test`); `application-dev.yml` hanya terbaca jika profile `dev` aktif.
- `${NAMA:default}` : membaca environment variable, memakai `default` jika variabel tidak ada.
- Flyway : menjalankan file SQL `V1__...`, `V2__...` berurutan, sekali saja, dan mencatatnya di `flyway_schema_history`. File lama tidak boleh diubah.
- `ddl-auto=validate` : Hibernate hanya mengecek Entity cocok dengan tabel, tidak membuat/mengubah tabel.
- `@RestControllerAdvice` : satu class yang menangkap exception dari semua controller.
- CHECK constraint : aturan yang dijaga database sendiri, jadi data salah tidak bisa masuk walau ada bug di kode Java.
- Testcontainers : menyalakan database sungguhan di Docker khusus untuk test, lalu membuangnya setelah selesai.

## Latihan untuk Aris
1. Ubah `JWT_SECRET` di `.env` menjadi `pendek`, jalankan backend, baca pesan errornya, lalu kembalikan nilai aslinya. Bagian mana yang membuat app menolak start?
2. Jalankan `psql -h localhost -U templateapp -d templateapp` lalu coba `INSERT INTO users (display_name, active_mode) VALUES ('Coba', 'admin');`. Kenapa ditolak? Constraint mana yang menolaknya?
3. Pasang breakpoint di `GlobalExceptionHandler.handleUnexpected`, lalu jalankan test `unexpectedErrorHidesDetails` dari VS Code (ikon ▷ di samping nama test) dalam mode debug. Lihat isi variabel `ex`.

## Yang perlu Aris lakukan
- Buka folder `backend/` di VS Code, pasang extension yang direkomendasikan (muncul notifikasi di pojok kanan bawah).
- Coba jalankan **Backend: debug (dev)** dari VS Code dan buka Swagger UI.
- `backend/.env` sudah ditambah `JWT_SECRET` acak dan variabel lain (nilai `DB_*` milikmu tidak diubah).

## Rencana fase berikutnya
- Fase 02: register, login, refresh token dengan rotasi, logout, `JwtService`, `JwtAuthFilter`, `SecurityConfig` lengkap (401 berformat `ErrorResponse`), `GET /api/users/me`, beserta test-nya.
