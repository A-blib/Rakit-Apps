# Fase 02: Auth email

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `user/User.java`, `auth/UserIdentity.java`, `auth/RefreshToken.java`, `user/CreatorProfile.java`, `provider/ProviderProfile.java` (+ repository) | Entity untuk tabel Fase 01 |
| `common/persistence/PersistableEnum*.java`, enum `IdentityProvider`, `ActiveMode`, `WebsitePurpose`, `ProviderStatus` | Enum Java huruf besar ↔ teks huruf kecil di database dan JSON |
| `security/JwtService.java` | Membuat & memeriksa access token JWT HS256 (`sub` = ID user, 15 menit) |
| `security/JwtAuthFilter.java` | Membaca `Authorization: Bearer ...` di setiap request |
| `security/CurrentUser.java` | Mengambil ID user yang sedang login |
| `security/TokenGenerator.java` | Token acak 32 byte + hash SHA-256 |
| `security/SecurityConfig.java` | Endpoint auth publik, sisanya wajib login, 401 berformat `ErrorResponse`, bean BCrypt |
| `config/TimeConfig.java` | Bean `Clock` agar waktu bisa dipalsukan di test |
| `auth/AuthService.java`, `auth/RefreshTokenService.java`, `auth/AuthController.java`, `auth/dto/*` | Daftar, masuk, refresh dengan rotasi, keluar |
| `user/UserService.java`, `user/UserController.java`, `user/dto/UserResponse.java` | `GET /api/users/me` |
| `common/util/Emails.java` | Normalisasi email (huruf kecil, tanpa spasi) |
| `common/exception/ApiException.java`, `common/response/ErrorResponse.java` | Tambah `existingMethods` untuk `USE_SOCIAL_LOGIN` |
| `TemplateAppApplication.java` | Mematikan user bawaan Spring Security (password acak di log hilang) |
| Test: `security/JwtServiceTest.java`, `auth/AuthFlowIntegrationTest.java` | 5 unit test JWT + 9 integration test alur auth |
| `docs/dokumentasi-project.md` | **Baru**: dokumentasi menyeluruh untuk belajar |

## Alasan keputusan
- Access token tanpa data selain ID user → nama/mode bisa berubah, jadi selalu dibaca dari database (alternatif: menaruh nama & mode di token, tidak dipilih karena data di token bisa basi selama 15 menit).
- `signWith(key, Jwts.SIG.HS256)` ditulis eksplisit → `JWT_SECRET` 64 karakter membuat JJWT otomatis memilih HS512, padahal spesifikasi meminta HS256.
- Rotasi + deteksi pemakaian ulang memakai kolom `replaced_by` → bisa membedakan "dicabut karena logout" (cukup ditolak) dan "dicabut karena sudah ditukar" (curiga dicuri, cabut semua).
- Kunci baris (`PESSIMISTIC_WRITE`) saat refresh → dua request refresh bersamaan tidak bisa menghasilkan dua token baru dari satu token lama (alternatif: optimistic locking dengan kolom version, tidak dipilih karena butuh kolom baru di migrasi).
- `noRollbackFor = ApiException.class` di rotasi → pencabutan semua sesi tetap tersimpan walau responsnya error.
- Hash palsu saat email tidak ditemukan → mencegah menebak email terdaftar dari lamanya respons.
- Register menolak email yang sudah dipakai akun Google/GitHub (`EMAIL_ALREADY_USED`) → mencegah dua akun dengan email sama; menyambungkan akun dilakukan lewat alur penyambungan di Fase 03.
- `USE_SOCIAL_LOGIN` membawa `existingMethods: ["google"]` → app bisa menulis "Silakan masuk dengan Google" dari `strings.xml` sendiri.
- `device_name` diisi dari header `User-Agent` → nanti bisa dipakai untuk menampilkan daftar perangkat.
- `roles` dihitung dari ada/tidaknya `creator_profiles` dan `provider_profiles` → tidak perlu kolom tambahan yang bisa tidak sinkron.
- Enum lewat `PersistableEnum` + `@Converter(autoApply = true)` → nama konstanta Java tetap konvensional (huruf besar), database sesuai CHECK constraint (alternatif: `@Enumerated(STRING)`, tidak dipilih karena akan menyimpan `CREATOR`, bukan `creator`).
- `UserDetailsServiceAutoConfiguration` dimatikan → app tidak memakai user bawaan Spring; login sepenuhnya lewat JWT.

## Cara menjalankan & mengetes
1. `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (atau F5 di VS Code).
2. Buka http://localhost:8080/swagger-ui.html, lalu ikuti langkah "Contoh mencoba alur auth email" di README.
3. Test otomatis: `./mvnw test`.

## Hasil tes
- `./mvnw test`: **lulus, 24 test** (AuthFlowIntegrationTest 9, JwtServiceTest 5, FlywayMigrationTest 5, GlobalExceptionHandlerTest 4, TemplateAppApplicationTests 1).
- Uji manual dengan curl ke backend dev:
  - register → `201`
  - login → `/users/me` → `200` dengan data user
  - refresh → `200`
  - refresh token lama dipakai ulang → `401 REFRESH_TOKEN_INVALID`
  - `/users/me` tanpa token → `401 UNAUTHORIZED`
  - Swagger menampilkan 5 endpoint
- Catatan: saat uji manual, port 8080 ternyata sudah dipakai backend yang (kemungkinan) Aris jalankan dari VS Code. Uji curl berjalan ke backend itu, dan DevTools-nya sudah memuat kode baru. Setelah uji selesai, proses itu ikut dihentikan. Jalankan ulang dari VS Code bila perlu. Di database dev ada satu akun uji `coba-<angka>@mail.com`.
- Log test diperiksa: tidak ada token JWT yang tercetak, dan "generated security password" sudah hilang.

## Konsep yang dipelajari
- JWT : token berisi data + tanda tangan; server cukup memeriksa tanda tangannya, tanpa menyimpan token.
- Refresh token & rotasi : token berumur panjang untuk meminta access token baru; diganti setiap kali dipakai.
- Hash SHA-256 vs BCrypt : SHA-256 cepat (cocok untuk token acak yang panjang), BCrypt sengaja lambat (cocok untuk password yang bisa ditebak).
- Filter Spring Security : kode yang dijalankan sebelum controller untuk setiap request.
- `SecurityContext` : "kantong" per request yang menyimpan siapa user yang sedang login.
- `@Transactional` + rollback : perubahan database dibatalkan otomatis saat ada exception, kecuali diatur lain (`noRollbackFor`).
- Spring Data derived query : `findByUserIdAndProvider` otomatis menjadi query SQL dari nama method-nya.
- MockMvc : mengirim request HTTP palsu ke app di dalam test.

## Latihan untuk Aris
1. Di Swagger, daftar → refresh dua kali berturut-turut memakai refresh token yang **sama**. Lalu cek di `psql`: `SELECT revoked_at, replaced_by FROM refresh_tokens ORDER BY created_at DESC LIMIT 5;`. Jelaskan kenapa semua barisnya tercabut.
2. Salin `accessToken` dan tempel di https://jwt.io (cukup bagian payload, aman karena ini token dev). Temukan `sub`, `iat`, `exp`. Kenapa token itu tidak bisa diubah tanpa `JWT_SECRET`?
3. Tambahkan satu test di `AuthFlowIntegrationTest`: login dengan email yang memakai huruf besar dan spasi di depan (mis. `"  ARIS@MAIL.COM"`) harus tetap berhasil. Jalankan dengan ikon ▷ di VS Code.

## Yang perlu Aris lakukan
- Jalankan ulang backend dari VS Code kalau tadi sedang dipakai.
- Baca `docs/dokumentasi-project.md` bagian 3 dan 6.

## Rencana fase berikutnya
- Fase 03: `GoogleTokenVerifier`, `GitHubOAuthClient`, alur GitHub (authorize-url → callback → exchange), `AccountLinkingService` (semua cabang 6.3 + unit test), `linkToken` di login, endpoint identitas. Aris perlu membuat OAuth Client Google & OAuth App GitHub (panduan disertakan); kode dan unit test bisa dikerjakan tanpa menunggu.
