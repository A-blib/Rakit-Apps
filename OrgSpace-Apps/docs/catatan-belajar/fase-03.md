# Fase 03: Google, GitHub, penyambungan akun

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `auth/SocialProfile.java` | Satu bentuk data akun Google/GitHub yang sudah diverifikasi |
| `security/GoogleTokenVerifier.java` | Memeriksa idToken Google (tanda tangan, penerbit, kedaluwarsa, audience = Web Client ID) |
| `security/GitHubOAuthClient.java` | URL login GitHub, tukar `code` → token GitHub, ambil `/user` + `/user/emails` (RestClient, timeout 10 detik) |
| `auth/AuthTicket*.java` | Tiket sekali pakai `GITHUB_STATE` / `LOGIN_RESULT` / `LINK` (hash, masa berlaku, payload jsonb) |
| `auth/AccountLinkingService.java` | Semua cabang bagian 6.3: masuk, akun baru, minta penyambungan, selesaikan penyambungan, sambung manual |
| `auth/GitHubAuthService.java` | Alur GitHub 6.4: authorize-url → callback → redirect deep link |
| `auth/AuthService.java` | Tambah `loginWithGoogle`, `exchangeLoginTicket`, dan `linkToken` di login email |
| `auth/AuthController.java` | `POST /auth/google`, `POST /auth/github/authorize-url`, `GET /auth/github/callback`, `POST /auth/github/exchange` |
| `user/IdentityService.java`, `user/IdentityController.java` | `GET/POST/DELETE /users/me/identities...` |
| `user/UserRepository.java` | `findByIdForUpdate` (kunci baris saat melepas identitas) |
| `common/response/ErrorResponse.java`, `common/exception/ApiException.java` | Field `linkToken` untuk `ACCOUNT_LINK_REQUIRED` |
| Test: `AccountLinkingServiceTest`, `GitHubOAuthClientTest`, `SocialAuthIntegrationTest` | 12 unit test cabang 6.3, 5 test GitHub client, 10 integration test skenario 5–8 |
| `README.md` | Panduan lengkap setup OAuth Google & GitHub + troubleshooting login |
| `docs/dokumentasi-project.md` | Bab baru "Login Google & GitHub, penyambungan akun" |

## Alasan keputusan
- Identitas dikunci dengan `providerUserId` (Google `sub`, GitHub ID angka), bukan email → email bisa diganti user, ID tidak.
- Penyambungan **tidak** otomatis walau email sama → mencegah pengambilalihan akun lewat email palsu. User harus masuk dengan metode lama dulu (alternatif: sambung otomatis jika email terverifikasi; tidak dipilih karena instruksi 6.3 melarang, dan masih berisiko kalau verifikasi email di provider lain lemah).
- Email belum terverifikasi tidak disimpan ke `users.email` → kalau disimpan, email itu "terkunci" (unik) dan pemilik aslinya tidak bisa mendaftar.
- Tiket dibuat dengan `REQUIRES_NEW` → tiket LINK tetap tersimpan walau request diakhiri error `ACCOUNT_LINK_REQUIRED` (alternatif: `noRollbackFor` di semua method masuk; tidak dipilih karena bisa ikut menyimpan perubahan lain yang seharusnya dibatalkan).
- `GitHubAuthService` tidak `@Transactional` → setiap langkah punya transaksi sendiri, sehingga error bisa ditangkap dan diubah menjadi redirect `?error=KODE`. Kalau dibungkus satu transaksi, exception yang ditangkap tetap menandai transaksi "harus rollback" dan berakhir dengan `UnexpectedRollbackException`.
- Deep link membawa tiket 2 menit, bukan token → token tidak pernah muncul di URL.
- `RestClient.builder()` dibuat langsung di `GitHubOAuthClient` → Spring Boot 4 memisahkan auto-config RestClient ke modul `spring-boot-restclient` yang tidak ada di project. Library baru tidak ditambahkan; cukup pakai bawaan spring-web.
- Konstruktor kedua `GitHubOAuthClient(properties, builder)` khusus untuk test → server GitHub palsu bisa dipasang tanpa mengubah kode produksi. Karena ada dua konstruktor, konstruktor yang dipakai Spring ditandai `@Autowired`.
- `IDENTITY_IN_USE` juga dipakai saat akun sudah punya Google/GitHub lain → database melarang dua identitas dengan provider sama di satu akun (`UNIQUE(user_id, provider)`).
- Kalau `GITHUB_CLIENT_ID` kosong, `authorize-url` membalas `500` dengan log jelas → ini salah konfigurasi server, bukan kesalahan user.

## Cara menjalankan & mengetes
1. Ikuti README bagian **Setup OAuth Google & GitHub** dan isi `backend/.env`.
2. Jalankan backend (`./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` atau F5 di VS Code).
3. GitHub tanpa HP: di Swagger panggil `POST /api/auth/github/authorize-url`, buka `url` di browser laptop, lalu login. Salin `ticket` dari address bar (`templateapp://auth/callback?ticket=...`) dan tukar lewat `POST /api/auth/github/exchange`.
4. Google butuh idToken dari HP, jadi baru bisa dicoba manual di Fase 08. Sampai saat itu, perilakunya dijamin oleh test.
5. `./mvnw test`.

## Hasil tes
- `./mvnw test`: **lulus, 51 test**
  - AccountLinkingServiceTest 12
  - SocialAuthIntegrationTest 10
  - AuthFlowIntegrationTest 9
  - GitHubOAuthClientTest 5
  - JwtServiceTest 5
  - FlywayMigrationTest 5
  - GlobalExceptionHandlerTest 4
  - TemplateAppApplicationTests 1
- Cabang 6.3 yang diuji: identitas sudah ada; email terverifikasi milik akun lain → `ACCOUNT_LINK_REQUIRED` (tanpa membuat user); email belum terverifikasi; email kosong; email baru; link cocok; link beda user → `LINK_USER_MISMATCH`; link token salah/basi → `LINK_TOKEN_INVALID`; identitas milik akun lain → `IDENTITY_IN_USE`; provider kedua sejenis → `IDENTITY_IN_USE`; sudah tersambung → tidak ada perubahan; email berbeda bisa disambung manual.
- Skenario 13.2 yang diuji lewat integration test: 5 (akun Google baru), 6 (akun Google tidak bisa login email), 7 (Google → GitHub email sama → sambung → GitHub berikutnya ke akun sama), 8 (GitHub email privat → akun terpisah → sambung manual).
- Uji manual dengan Google/GitHub sungguhan **belum dilakukan**, karena kredensial OAuth belum dibuat.
- Satu kendala saat pengerjaan: semua integration test gagal start karena `GitHubOAuthClient` punya dua konstruktor ("No default constructor found"). Diperbaiki dengan `@Autowired` pada konstruktor utama.

## Konsep yang dipelajari
- OAuth 2.0 authorization code : user login di situs provider, provider memberi `code` sekali pakai, backend menukarnya dengan token memakai client secret.
- State OAuth : nilai acak yang mencegah orang lain "menyuntikkan" callback login ke sesi kita (CSRF).
- idToken : JWT dari Google yang menyatakan identitas user. Backend memeriksanya dengan kunci publik Google.
- Audience (`aud`) : untuk siapa token dibuat. Token untuk app lain harus ditolak.
- Propagasi transaksi (`REQUIRED` vs `REQUIRES_NEW`) : ikut transaksi yang sudah ada, atau membuat transaksi baru yang hasilnya tersimpan sendiri.
- Mock (`@Mock`, `@MockitoBean`) vs spy (`@MockitoSpyBean`) : pengganti palsu penuh vs objek asli yang sebagian method-nya dipalsukan.
- `MockRestServiceServer` : server HTTP palsu untuk menguji kode yang memanggil API luar.

## Latihan untuk Aris
1. Buka `AccountLinkingServiceTest`, lalu jalankan `verifiedEmailOwnedByOtherUserRequiresLinkInsteadOfAutoLinking` dalam mode debug dengan breakpoint di `AccountLinkingService.linkRequired`. Perhatikan isi `pending` sebelum tiket dibuat.
2. Coba ubah `@Transactional(propagation = Propagation.REQUIRES_NEW)` di `AuthTicketService.issue` menjadi `@Transactional`, lalu jalankan `SocialAuthIntegrationTest`. Test mana yang gagal, dan kenapa? Setelah itu kembalikan seperti semula.
3. Setelah membuat OAuth App GitHub, coba alur GitHub dari browser laptop (langkah 3 di atas). Lalu lihat isi tabel: `SELECT type, used_at, expires_at FROM auth_tickets ORDER BY created_at DESC LIMIT 5;`.

## Yang perlu Aris lakukan
- Buat kredensial OAuth mengikuti README bagian **Setup OAuth Google & GitHub** (A: Google Web + Android client, B: GitHub OAuth App), lalu isi `GOOGLE_WEB_CLIENT_ID`, `GITHUB_CLIENT_ID`, dan `GITHUB_CLIENT_SECRET` di `backend/.env`.
- Client Android Google butuh SHA-1 debug. Kalau `~/.android/debug.keystore` belum ada, langkah ini bisa ditunda sampai Fase 05, setelah build Android pertama.

## Rencana fase berikutnya
- Fase 04: `POST /users/me/onboarding/creator`, `POST /users/me/onboarding/provider`, `PATCH /users/me/active-mode` (aturan mode & status provider), `DummyDataSeeder` (profile dev), contoh SQL status provider di README, dan integration test-nya.
