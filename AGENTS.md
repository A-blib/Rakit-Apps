# AGENTS.md

Bangun **App Template Website** mengikuti instruksi di file ini.

- Baca file ini sampai habis sebelum menulis kode apa pun.
- Jadikan bagian 13 (hasil akhir yang diharapkan) sebagai target akhir pekerjaan.
- Jika permintaan Aris di chat berbeda dengan file ini, ikuti permintaan Aris, lalu sebutkan bagian file ini yang berbeda.
- Jika memakai Claude Code, salin atau ganti nama file ini menjadi `CLAUDE.md` di root repo.

---

## 1. Pahami produknya

Bangun app Android untuk membuat website tanpa coding. Sediakan **satu akun dengan dua mode**:

- **Pembuat website**: memilih template atau menyusun section, mengedit teks/warna/foto, lalu export jadi HTML/CSS/JS.
- **Penyedia template (provider)**: mengunggah template untuk dipakai user lain.

Ikuti keputusan produk berikut tanpa mengubahnya:

| Keputusan | Terapkan |
|---|---|
| Coba tanpa login | Masukkan user sebagai **tamu** saat app dibuka. Minta login hanya saat user butuh fitur online. |
| Metode masuk | Sediakan **Google**, **GitHub**, dan **email + password**. Pakai menu Daftar khusus untuk jalur email. |
| Penyambungan akun | Sambungkan identitas Google, GitHub, dan email yang memiliki **email terverifikasi yang sama** menjadi satu akun, setelah user membuktikan kepemilikan akun lama. |
| Satu akun, dua mode | Buka **mode terakhir** yang dipakai user. Sediakan cara beralih mode. |
| Offline dulu | Rancang fitur inti agar nanti jalan di HP; fitur cloud menyusul. |

Rujuk rancangan lengkap (alur, tools, arsitektur) di `docs/rancangan-app-template-website.pdf`.

---

## 2. Batasi cakupan pekerjaan

### 2.1 Bangun sekarang

1. **Backend Spring Boot**: autentikasi (email, Google, GitHub), refresh token, penyambungan akun, metode login terhubung, data user, onboarding, dan perpindahan mode.
2. **App Android**:
   - Splash dan logika layar awal
   - Intro singkat saat app pertama kali dibuka
   - **Dashboard Pembuat Website**: hanya layar **"Segera hadir"**
   - **Dashboard Provider**: hanya layar **"Segera hadir"** + banner status provider
   - Layar Masuk (Google, GitHub, email) dan layar Daftar
   - Onboarding akun baru (pilih peran, form pembuat website, form provider)
   - Menu profil (beralih mode, jadi penyedia template, pengaturan, keluar)
   - Pengaturan: metode login terhubung, keluar
   - Dark mode, state loading/kosong/error
   - Tampilan semua layar sesuai **bagian 9**
3. **Dokumentasi**: `README.md` lengkap (bagian 11) dan panduan setup lingkungan (bagian 10).

Buat layout dan tampilan awal sebagus dan serapi mungkin, tetapi pusatkan semua nilai desain di token tema (bagian 9.2). Detail desain akan diubah setelah Aris selesai mendiskusikan fitur pembuat website dan provider.

### 2.2 Jangan bangun sekarang

Jangan membuat kode, tabel, endpoint, atau layar untuk hal berikut. Alurnya masih didiskusikan Aris.

- Galeri template, editor section, preview WebView, export ZIP, penyimpanan project (Room, file JSON), sinkron (WorkManager), folder `engine/`
- Fitur provider: upload template, mode tandai, review, statistik
- Panel admin dan web dashboard
- Publish website menjadi link
- Lupa password (cukup tampilkan dialog "Segera hadir")
- Pemindahan project tamu ke akun
- Tabel `section_layouts`, `templates`, `projects`, `assets`; library `jsoup`; upload `MultipartFile`; konfigurasi CORS

Jika sebuah tugas terasa membutuhkan salah satu hal di atas, berhenti dan tanyakan ke Aris.

### 2.3 Isi layar "Segera hadir"

| Layar | Isi |
|---|---|
| Dashboard Pembuat Website | Judul "Buat website pertamamu", animasi Lottie, teks "Fitur pembuatan website segera hadir.", tombol utama nonaktif berlabel "Segera hadir". Di app bar: tombol **Masuk** untuk tamu, atau avatar + menu profil untuk user yang sudah login. |
| Dashboard Provider | Banner status provider (bagian 6.6), animasi Lottie, teks "Dashboard provider segera hadir." |

---

## 3. Ikuti aturan kerja (WAJIB)

Prioritaskan pemahaman Aris di atas sekadar app jadi, karena Aris sedang belajar Java, Android SDK, dan Spring Boot.

1. **Kerjakan per fase** (bagian 12). Setelah satu fase selesai, berhenti, laporkan, dan tunggu konfirmasi sebelum lanjut.
2. **Laporkan setiap fase** memakai format bagian 13.4, dengan isi minimal:
   - File yang dibuat/diubah dan fungsinya (singkat)
   - Alasan keputusan teknis, termasuk alternatif yang tidak dipilih
   - Cara menjalankan dan mengetes hasilnya secara manual
   - 1–3 latihan kecil untuk Aris coba sendiri agar memahami konsep fase itu
3. **Tulis catatan belajar** di `docs/catatan-belajar/fase-XX.md` dengan isi yang sama seperti laporan (format bagian 13.4).
4. **Tulis komentar kode** dalam Bahasa Indonesia. Jelaskan *alasan*, jangan mengulang apa yang sudah terlihat dari kode. Jangan mengomentari setiap baris.
5. **Jangan menambah library** di luar daftar bagian 4 tanpa bertanya.
6. **Jangan menebak nomor versi.** Pakai versi stabil terbaru dari sumber resmi (start.spring.io, Maven Central, Google Maven, developer.android.com). Catat versi yang dipakai di `README.md`.
7. **Jangan pernah commit rahasia** (client secret, JWT secret, password database, `local.properties`, `.env`).
8. **Tanyakan dulu** jika instruksi ambigu atau bertentangan. Jangan berasumsi.
9. **Commit kecil** per langkah yang berarti. Tulis pesan commit dalam Bahasa Indonesia dengan format `fase-03: tambah endpoint refresh token`.
10. **Jangan menginstal software sistem.** Tuliskan perintah instalasi untuk Aris, jelaskan fungsi setiap perintah, tunggu Aris menjalankannya, lalu verifikasi hasilnya (bagian 10).
11. **Perbarui `README.md`** di setiap fase yang mengubah cara setup atau cara menjalankan project.
12. **Kerjakan semuanya lewat terminal.** Siapkan folder `android/` untuk dibuka Aris di **Android Studio** dan folder `backend/` untuk **VS Code**. Jalankan app di **HP Android asli**, bukan emulator (bagian 4.4).

---

## 4. Gunakan tech stack berikut

### 4.1 Android (pasang sekarang)

| Kebutuhan | Gunakan |
|---|---|
| Bahasa | Java (source/target compatibility 17) |
| UI | XML Views + **Material Components** (Material 3). Jangan pakai Jetpack Compose. |
| Tema | `themes.xml`, `colors.xml`, `dimens.xml`, `styles.xml`, `values-night/` |
| Animasi | **Lottie** dengan gaya garis monokrom (bagian 9) |
| Font | **Geist Sans** dan **Geist Mono** (lisensi SIL OFL) di `res/font/`, sertakan file lisensinya |
| Akses view | **ViewBinding**. Jangan pakai `findViewById`. |
| Arsitektur | **ViewModel + LiveData** (MVVM) |
| Navigasi | **Navigation Component**, satu Activity |
| DI | **Hilt** |
| Splash | `androidx.core:core-splashscreen` |
| Intro | ViewPager2 |
| Jaringan | **Retrofit + OkHttp + Gson** |
| Login Google | **Credential Manager** (`androidx.credentials`) + **Google ID library** (`com.google.android.libraries.identity.googleid`) |
| Login GitHub | **Custom Tabs** (`androidx.browser`) + deep link |
| Simpan token | Enkripsi AES-GCM dengan kunci di **Android Keystore**, simpan hasilnya di SharedPreferences |
| Background | `ExecutorService` lewat `AppExecutors` |
| Test | JUnit, `androidx.arch.core:core-testing` |

Set minSdk **26**, compileSdk dan targetSdk ke versi stabil terbaru. Pakai package `com.aris.templateapp`.

### 4.2 Android (jangan pasang sekarang)

Room, WorkManager, WebView engine, Photo Picker, Glide, `java.util.zip` untuk export.

### 4.3 Backend (pasang sekarang)

| Kebutuhan | Gunakan |
|---|---|
| Framework | **Spring Boot** (versi stabil terbaru) + Spring Web |
| JDK & build | Java **21** (LTS) + **Maven** |
| Database | **PostgreSQL** + **Spring Data JPA** (Hibernate) |
| Migrasi & seed | **Flyway**; set `spring.jpa.hibernate.ddl-auto=validate` |
| Data dummy | **Datafaker**, hanya di profile `dev` |
| Validasi | **Bean Validation** (`spring-boot-starter-validation`) |
| Keamanan | **Spring Security** + **BCrypt** |
| JWT | **JJWT** |
| Verifikasi Google | **Google API Client** (`GoogleIdTokenVerifier`) |
| GitHub OAuth | **RestClient** bawaan Spring untuk memanggil API GitHub |
| Pendukung | **Lombok**, **Spring Boot DevTools**, **springdoc-openapi** (Swagger UI) |
| Test | Spring Boot Starter Test, **Testcontainers** (PostgreSQL) |

Pakai package `com.aris.templateapp`.

### 4.4 Atur lingkungan kerja: terminal, Android Studio, VS Code, HP asli

| Alat | Folder | Peran |
|---|---|---|
| Terminal | seluruh repo | Tulis dan ubah file, jalankan build, test, dan perintah `adb` |
| Android Studio (Aris) | `android/` | Aris membaca kode Android, melihat preview layout, Logcat, debugger, dan menekan Run ke HP |
| VS Code (Aris) | `backend/` | Aris membaca dan mengedit kode backend, menjalankan dan men-debug Spring Boot |
| HP Android asli | – | Jalankan dan demokan app di sini. Jangan pakai emulator. |

**Android Studio**

- Jangan mengoperasikan Android Studio. Kerjakan semuanya lewat terminal (`./gradlew`, `adb`).
- Arahkan Aris memakai fitur Android Studio saat membantu belajar atau mengetes, misalnya "buka Logcat lalu filter `com.aris.templateapp`" atau "klik Sync Now".
- Di Fase 05, minta Aris membuat project Android awal lewat wizard Android Studio dengan pengaturan: *New Project → Empty Views Activity*, Name `TemplateApp`, Package `com.aris.templateapp`, Save location `Rakit Apps/android`, Language **Java**, Minimum SDK **API 26**, Build configuration language **Kotlin DSL**. Tunggu sampai Aris selesai, lalu lanjutkan dari struktur itu.
- Pertahankan file build **Kotlin DSL** + version catalog (`gradle/libs.versions.toml`) dari wizard. Tambahkan komentar singkat yang menjelaskan sintaksnya, karena Aris belum belajar Kotlin.
- Setiap kali mengubah `build.gradle.kts`, `settings.gradle.kts`, atau `libs.versions.toml`, ingatkan Aris untuk klik **"Sync Now"** di Android Studio.
- Jangan menjalankan `./gradlew` saat Aris sedang build/Run di Android Studio. Tanyakan dulu jika ragu.
- Pastikan Android Studio dan terminal memakai **JDK 21** yang sama. Tulis langkahnya di README: *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*.
- Gunakan hasil Gradle sebagai patokan benar/salah: `./gradlew assembleDebug`, `./gradlew testDebugUnitTest`, `./gradlew lint`.
- Jangan commit folder `.idea/`.
- Set `org.gradle.jvmargs=-Xmx2g` di `android/gradle.properties` agar Gradle tidak memakan RAM berlebihan (laptop Aris RAM 16 GB).

**HP asli**

Jalankan app dari terminal dengan urutan berikut:

```bash
adb devices                                   # pastikan HP terdeteksi dengan status "device"
adb reverse tcp:8080 tcp:8080                 # teruskan localhost:8080 di HP ke backend di laptop
./gradlew installDebug                        # build lalu pasang ke HP
adb shell am start -n com.aris.templateapp/.MainActivity
adb logcat --pid=$(adb shell pidof -s com.aris.templateapp)   # tampilkan log app saja
```

- Hubungkan app ke backend development lewat **`adb reverse`**, sehingga app cukup memanggil `http://localhost:8080/api/`. Cara ini tidak bergantung pada IP WiFi, tidak perlu membuka firewall, dan memungkinkan callback GitHub memakai `localhost`.
- Jalankan ulang `adb reverse` setiap kali HP dicabut atau `adb` di-restart. Tulis aturan ini dengan jelas di README.
- Izinkan Aris menekan **Run** di Android Studio dengan memilih HP-nya; `adb reverse` tetap wajib dijalankan.
- Sebutkan **scrcpy** (opsional) di README untuk menampilkan layar HP di laptop saat demo atau mengambil screenshot.

**VS Code**

- Siapkan folder **`backend/`** untuk dibuka di VS Code. Buat dan commit file di **`backend/.vscode/`**:
  - `extensions.json`: Extension Pack for Java, Spring Boot Extension Pack, EditorConfig
  - `settings.json`: pengaturan bersama tanpa path pribadi
  - `tasks.json`: `backend: jalankan (dev)` → `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`, `backend: test` → `./mvnw test`
  - `launch.json`: debug Spring Boot dengan profile `dev`
- Muat environment variable backend dari `backend/.env` (jangan di-commit). Jelaskan di README cara memuatnya dari terminal dan lewat `launch.json` (`envFile`).

---

## 5. Susun struktur repository

Buat satu repo (monorepo) berisi dua project:

```
Rakit Apps/
├── AGENTS.md
├── README.md                     ← cara setup, menjalankan, versi library
├── docs/
│   ├── rancangan-app-template-website.pdf
│   └── catatan-belajar/          ← catatan per fase
├── backend/                      ← Spring Boot (Maven), dibuka di VS Code
│   └── .vscode/                  ← extensions, settings, tasks, launch
└── android/                      ← project Android (Gradle), dibuka di Android Studio
```

Masukkan ke `.gitignore`: `local.properties`, `.env`, `backend/uploads/`, `*.keystore`, `*.jks`, folder build, `.idea/`. Tetap commit file di `backend/.vscode/` yang disebut di bagian 4.4.

### 5.1 Struktur backend

```
backend/
├── pom.xml
├── .env.example                      ← daftar environment variable tanpa nilai rahasia
└── src/
    ├── main/java/com/aris/templateapp/
    │   ├── TemplateAppApplication.java
    │   ├── config/
    │   │   ├── OpenApiConfig.java
    │   │   └── AppProperties.java     ← @ConfigurationProperties: jwt, google, github, deepLink
    │   ├── security/
    │   │   ├── SecurityConfig.java
    │   │   ├── JwtService.java
    │   │   ├── JwtAuthFilter.java
    │   │   ├── CurrentUser.java       ← helper mengambil userId dari token
    │   │   ├── GoogleTokenVerifier.java
    │   │   └── GitHubOAuthClient.java
    │   ├── common/
    │   │   ├── exception/
    │   │   │   ├── ApiException.java          ← berisi errorCode + HttpStatus
    │   │   │   └── GlobalExceptionHandler.java
    │   │   └── response/
    │   │       └── ErrorResponse.java
    │   ├── auth/
    │   │   ├── AuthController.java
    │   │   ├── AuthService.java
    │   │   ├── AccountLinkingService.java      ← logika penyambungan akun
    │   │   ├── RefreshTokenService.java
    │   │   ├── UserIdentity.java / UserIdentityRepository.java
    │   │   ├── RefreshToken.java / RefreshTokenRepository.java
    │   │   ├── AuthTicket.java / AuthTicketRepository.java
    │   │   └── dto/
    │   ├── user/
    │   │   ├── User.java / UserRepository.java
    │   │   ├── CreatorProfile.java / CreatorProfileRepository.java
    │   │   ├── UserService.java
    │   │   ├── UserController.java
    │   │   ├── IdentityController.java        ← metode login terhubung
    │   │   └── dto/
    │   ├── provider/
    │   │   └── ProviderProfile.java / ProviderProfileRepository.java   ← hanya profil & status
    │   └── seed/
    │       └── DummyDataSeeder.java           ← @Profile("dev")
    ├── main/resources/
    │   ├── application.yml
    │   ├── application-dev.yml
    │   ├── application-prod.yml
    │   └── db/migration/
    └── test/java/com/aris/templateapp/
```

### 5.2 Struktur Android

```
android/app/src/main/java/com/aris/templateapp/
├── TemplateApp.java                  ← @HiltAndroidApp
├── MainActivity.java                 ← satu-satunya Activity, splash, deep link GitHub
├── core/
│   ├── di/          NetworkModule.java, AppModule.java
│   ├── network/     AuthInterceptor.java, TokenAuthenticator.java, ApiErrorParser.java
│   ├── storage/     TokenStorage.java, SessionStore.java (user tersimpan, mode terakhir, flag intro)
│   └── util/        Resource.java, AppExecutors.java, Event.java
├── data/
│   ├── model/       User.java, UserRole.java, ProviderStatus.java, LoginMethod.java, ApiError.java
│   ├── remote/
│   │   ├── api/     AuthApi.java, UserApi.java
│   │   └── dto/
│   ├── mapper/
│   └── repository/  AuthRepository.java, UserRepository.java
└── ui/
    ├── common/      StateView.java, ComingSoonView.java
    ├── startup/     StartupViewModel.java          ← menentukan layar pertama
    ├── intro/       IntroFragment.java, IntroAdapter.java
    ├── auth/        LoginFragment.java, RegisterFragment.java, AuthViewModel.java,
    │                GoogleSignInHelper.java, GitHubSignInHelper.java, LinkAccountDialog.java
    ├── onboarding/  RoleSelectFragment.java, CreatorFormFragment.java,
    │                ProviderFormFragment.java, OnboardingViewModel.java
    ├── creator/     CreatorDashboardFragment.java  ← "Segera hadir"
    ├── provider/    ProviderDashboardFragment.java ← "Segera hadir" + banner status
    ├── profile/     ProfileSheet.java, ProfileViewModel.java
    └── settings/    SettingsFragment.java, LinkedMethodsFragment.java, SettingsViewModel.java

android/app/src/main/res/
├── layout/       activity_, fragment_, item_, sheet_, view_
├── navigation/   nav_graph.xml
├── values/       colors, dimens, strings, themes, styles
├── values-night/ colors, themes
├── drawable/
├── font/         Geist Sans & Geist Mono (nama file huruf kecil, mis. geist_semibold.ttf) + lisensi OFL
└── raw/          Lottie: coming_soon.json, empty.json, loading.json

android/app/src/debug/res/xml/network_security_config.xml   ← izinkan HTTP hanya untuk localhost
```

---

## 6. Terapkan alur aplikasi

### 6.1 Tentukan layar pertama (`StartupViewModel`)

```
Pertama kali buka app?            → IntroFragment → CreatorDashboard (tamu)
Ada token tersimpan?
  tidak                           → CreatorDashboard (tamu)
  ya → ambil user (GET /users/me; jika offline pakai cache SessionStore)
       onboardingCompleted = false → RoleSelectFragment
       activeMode = provider dan punya ProviderProfile
          status != suspended      → ProviderDashboard
          status = suspended       → CreatorDashboard + pesan "Akun provider ditangguhkan"
       selain itu                  → CreatorDashboard
GET /users/me mengembalikan 401 dan refresh gagal → hapus token → CreatorDashboard (tamu)
```

Tampilkan layar Masuk hanya saat user menekan **Masuk**, **Jadi penyedia template**, atau fitur online lain nanti. Setelah login berhasil, kembalikan user ke tujuan semula.

### 6.2 Susun layar Masuk

Susun dari atas: tombol **Lanjutkan dengan Google** (paling menonjol), **Lanjutkan dengan GitHub**, pemisah "atau", form email + password (`TextInputLayout`, `endIconMode="password_toggle"`, `autofillHints`), tombol **Masuk**, link "Lupa password?" (dialog "Segera hadir"), link "Belum punya akun? Daftar", teks persetujuan Syarat & Ketentuan.

Tampilkan pesan error berikut:

| Kode dari backend | Pesan ke user |
|---|---|
| `INVALID_CREDENTIALS` | "Email atau password salah." |
| `USE_SOCIAL_LOGIN` | "Akun ini terdaftar dengan {Google/GitHub}. Silakan masuk dengan {metode}." |
| `EMAIL_ALREADY_USED` (daftar) | "Email sudah terdaftar. Silakan masuk." |
| Tidak ada internet | "Tidak ada koneksi. Periksa internet lalu coba lagi." + tombol coba lagi |

### 6.3 Terapkan penyambungan akun (backend, `AccountLinkingService`)

Jalankan logika ini untuk masuk lewat Google dan GitHub:

```
1. Verifikasi identitas (token Google / hasil OAuth GitHub). Gagal → 401 SOCIAL_AUTH_FAILED.
2. Cari user_identities (provider, provider_user_id).
   ketemu → masukkan ke user itu.
3. Tidak ketemu:
   a. Ambil email TERVERIFIKASI dari provider (GitHub: email primary & verified dari /user/emails).
   b. Ada user lain dengan email itu?
      ya  → JANGAN sambungkan otomatis.
            Simpan identitas tertunda sebagai AuthTicket tipe LINK (berlaku 10 menit).
            Balas 409 ACCOUNT_LINK_REQUIRED { linkToken, existingMethods: ["google", ...] }.
      tidak / email tidak ada / tidak terverifikasi → buat user baru + identitas, isNewUser = true.
4. Saat user masuk dengan metode lama sambil mengirim linkToken:
   cek user hasil masuk == user pemilik email di ticket.
   cocok → simpan identitas tertunda ke user itu, tandai ticket terpakai.
   tidak cocok → 403 LINK_USER_MISMATCH.
```

Di Android, saat menerima `ACCOUNT_LINK_REQUIRED`, tampilkan `LinkAccountDialog` dengan teks: "Email ini sudah terdaftar dengan {metode}. Masuk dengan {metode} untuk menyambungkan akun {metode baru}." Setelah user masuk dengan metode lama, kirim `linkToken` di request login tersebut.

### 6.4 Terapkan alur GitHub

Simpan client secret GitHub **hanya** di backend. Terima kode OAuth langsung di backend:

```
1. App   → POST /api/auth/github/authorize-url { linkToken? }
           ← { url }   (backend membuat AuthTicket tipe GITHUB_STATE berisi state acak + linkToken)
2. App membuka url di Custom Tab → user login di GitHub
3. GitHub → GET /api/auth/github/callback?code&state   (ke backend)
4. Backend: validasi state, tukar code menjadi access token GitHub,
            ambil profil (/user) dan email (/user/emails), jalankan logika 6.3,
            buat AuthTicket tipe LOGIN_RESULT (sekali pakai, berlaku 2 menit),
            redirect ke  templateapp://auth/callback?ticket=XXX
            (atau ?error=KODE jika gagal / ?error=ACCOUNT_LINK_REQUIRED&linkToken=...&methods=google)
5. MainActivity menerima deep link → POST /api/auth/github/exchange { ticket } → AuthResponse
```

- Minta scope GitHub `read:user user:email`.
- Gunakan **OAuth App GitHub terpisah** untuk development dan production (satu OAuth App hanya punya satu callback URL).
- Pakai callback development `http://localhost:8080/api/auth/github/callback`. Browser di HP dapat menjangkaunya karena `adb reverse tcp:8080 tcp:8080` (bagian 4.4).
- Untuk menyambungkan GitHub dari Pengaturan (user sudah login): `POST /api/users/me/identities/github/authorize-url` (butuh JWT) → ikat state ke user → callback menyambungkan identitas → redirect ke `templateapp://auth/callback?result=linked` atau `?error=IDENTITY_IN_USE`.

### 6.5 Terapkan alur Google

- Pakai `GetSignInWithGoogleOption` dengan `serverClientId` = **Web Client ID** (dari `local.properties` → `BuildConfig.GOOGLE_WEB_CLIENT_ID`).
- Panggil `CredentialManager.getCredentialAsync(...)` dengan callback versi Java, lalu ambil token lewat `GoogleIdTokenCredential.createFrom(...)`.
- Kirim `idToken` ke `POST /api/auth/google`.
- Di backend, verifikasi dengan `GoogleIdTokenVerifier`, audience = Web Client ID, dan terima penyambungan hanya jika `email_verified = true`.
- Jangan pakai library Google Sign-In lama (`play-services-auth` / `GoogleSignInClient`) karena sudah deprecated.

### 6.6 Terapkan onboarding dan mode

- Set akun baru dengan `onboardingCompleted = false` dan `activeMode = creator`.
- `RoleSelectFragment`: tampilkan dua kartu besar, "Pembuat website" dan "Penyedia template", serta teks kecil "Peran bisa ditambah nanti."
- `CreatorFormFragment`: nama tampilan (isi otomatis dari Google/GitHub), tujuan website (ChipGroup: Sekolah, Organisasi, Usaha / UMKM, Instansi, Pribadi / Portofolio, Lainnya), nama organisasi (opsional). Sediakan tombol "Mulai" dan "Lewati"; keduanya menyelesaikan onboarding.
- `ProviderFormFragment`: nama kreator (wajib), bio (opsional, maks 300), link portofolio/GitHub (opsional, wajib URL valid), keahlian (Chip, opsional), checkbox setuju aturan provider (wajib). Set status `pending`.
- Arahkan user lama yang menekan **Jadi penyedia template** langsung ke `ProviderFormFragment` tanpa memilih peran.
- Isi menu profil: nama + mode aktif, tombol **Beralih ke mode provider / mode pembuat website** (hanya jika punya dua peran) atau **Jadi penyedia template** (jika belum provider), Pengaturan, Keluar. Pakai kata "mode", bukan "akun provider".
- Tampilkan banner status provider:

| Status | Banner |
|---|---|
| `pending` | "Akunmu sedang diverifikasi." |
| `approved` | Tanpa banner |
| `rejected` | "Pengajuan provider ditolak: {alasan}." |
| `suspended` | Kunci mode provider dan arahkan user ke mode pembuat website. |

Belum ada panel admin untuk menyetujui provider. Tulis contoh SQL di README untuk mengubah status provider secara manual saat testing.

### 6.7 Terapkan keluar

Panggil `POST /api/auth/logout` (cabut refresh token), hapus token dan cache user di HP, lalu kembali ke CreatorDashboard sebagai tamu. Jika request logout gagal karena offline, tetap hapus token lokal.

---

## 7. Bangun backend sesuai spesifikasi

### 7.1 Sediakan environment variable (`.env.example`)

```
DB_URL=jdbc:postgresql://localhost:5432/templateapp
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=                    # minimal 32 byte acak
JWT_ACCESS_TTL_MINUTES=15
JWT_REFRESH_TTL_DAYS=30
GOOGLE_WEB_CLIENT_ID=
GITHUB_CLIENT_ID=
GITHUB_CLIENT_SECRET=
GITHUB_REDIRECT_URI=http://localhost:8080/api/auth/github/callback
APP_DEEP_LINK=templateapp://auth/callback
```

Baca nilai ini di `application.yml` dengan `${NAMA_VAR}`. Beri prefix `/api` pada semua endpoint.

### 7.2 Buat migrasi Flyway

Pakai `UUID` dengan default `gen_random_uuid()` untuk ID dan `timestamptz` untuk semua waktu.

| File | Isi |
|---|---|
| `V1__create_users.sql` | `users`: id, display_name (varchar 100, not null), email (varchar 255, unik case-insensitive lewat index `lower(email)`), avatar_url, is_admin (default false), active_mode (`creator`/`provider`, default `creator`), onboarding_completed (default false), created_at, updated_at |
| `V2__create_user_identities.sql` | `user_identities`: id, user_id (FK, on delete cascade), provider (`local`/`google`/`github`), provider_user_id (untuk `local` = email huruf kecil), email, email_verified, password_hash (hanya `local`), created_at, UNIQUE(provider, provider_user_id), UNIQUE(user_id, provider) |
| `V3__create_refresh_tokens.sql` | `refresh_tokens`: id, user_id (FK), token_hash (SHA-256, unik), device_name, expires_at, revoked_at, replaced_by (uuid, null), created_at |
| `V4__create_auth_tickets.sql` | `auth_tickets`: id, type (`GITHUB_STATE`/`LOGIN_RESULT`/`LINK`), token_hash (unik), user_id (null), payload (jsonb), expires_at, used_at, created_at |
| `V5__create_creator_profiles.sql` | `creator_profiles`: user_id (PK + FK), website_purpose (`sekolah`/`organisasi`/`umkm`/`instansi`/`pribadi`/`lainnya`, boleh null), organization_name (varchar 150), created_at, updated_at |
| `V6__create_provider_profiles.sql` | `provider_profiles`: user_id (PK + FK), creator_name (varchar 100, not null), bio (varchar 300), portfolio_url, specialties (text[]), status (`pending`/`approved`/`rejected`/`suspended`, default `pending`), rejection_reason, agreed_terms_at (not null), created_at, updated_at |

Pakai CHECK constraint untuk kolom enum. Petakan kolom `jsonb` di Entity dengan `@JdbcTypeCode(SqlTypes.JSON)`.

### 7.3 Buat endpoint

Endpoint publik:

| Method & path | Body | Hasil |
|---|---|---|
| `POST /api/auth/register` | `{ displayName, email, password }` | `AuthResponse` (201) |
| `POST /api/auth/login` | `{ email, password, linkToken? }` | `AuthResponse` |
| `POST /api/auth/google` | `{ idToken, linkToken? }` | `AuthResponse` |
| `POST /api/auth/github/authorize-url` | `{ linkToken? }` | `{ url }` |
| `GET /api/auth/github/callback` | query `code`, `state` | redirect ke deep link |
| `POST /api/auth/github/exchange` | `{ ticket }` | `AuthResponse` |
| `POST /api/auth/refresh` | `{ refreshToken }` | `AuthResponse` |
| `POST /api/auth/logout` | `{ refreshToken }` | 204 |

Endpoint yang butuh JWT:

| Method & path | Body | Hasil |
|---|---|---|
| `GET /api/users/me` | – | `UserResponse` |
| `POST /api/users/me/onboarding/creator` | `{ displayName, websitePurpose?, organizationName? }` | `UserResponse` |
| `POST /api/users/me/onboarding/provider` | `{ creatorName, bio?, portfolioUrl?, specialties?, agreedToTerms }` | `UserResponse` |
| `PATCH /api/users/me/active-mode` | `{ mode }` | `UserResponse` |
| `GET /api/users/me/identities` | – | daftar `{ provider, email, createdAt }` |
| `POST /api/users/me/identities/google` | `{ idToken }` | daftar identitas |
| `POST /api/users/me/identities/github/authorize-url` | – | `{ url }` |
| `DELETE /api/users/me/identities/{provider}` | – | 204; tolak jika identitas terakhir (`LAST_IDENTITY`) |

Gunakan bentuk respons berikut:

```json
// AuthResponse
{ "accessToken": "...", "refreshToken": "...", "expiresIn": 900, "isNewUser": false, "user": { /* UserResponse */ } }

// UserResponse
{ "id": "uuid", "displayName": "Aris", "email": "aris@mail.com", "avatarUrl": null,
  "activeMode": "creator", "onboardingCompleted": true,
  "roles": ["creator", "provider"], "providerStatus": "pending",
  "creatorProfile": { "websitePurpose": "sekolah", "organizationName": "SMK Negeri 1" } }

// ErrorResponse
{ "code": "VALIDATION_ERROR", "message": "Data tidak valid.", "fieldErrors": { "email": "Format email tidak valid" } }
```

Gunakan kode error: `VALIDATION_ERROR`, `INVALID_CREDENTIALS`, `USE_SOCIAL_LOGIN`, `EMAIL_ALREADY_USED`, `SOCIAL_AUTH_FAILED`, `ACCOUNT_LINK_REQUIRED`, `LINK_USER_MISMATCH`, `LINK_TOKEN_INVALID`, `IDENTITY_IN_USE`, `LAST_IDENTITY`, `TICKET_INVALID`, `REFRESH_TOKEN_INVALID`, `PROVIDER_PROFILE_EXISTS`, `MODE_NOT_ALLOWED`, `UNAUTHORIZED`, `NOT_FOUND`, `INTERNAL_ERROR`.

### 7.4 Terapkan aturan keamanan

- Wajibkan password minimal 8 karakter dan hash dengan BCrypt.
- Buat access token JWT (HS256) dengan klaim `sub` = userId dan umur 15 menit.
- Buat refresh token berupa string acak 32 byte. Simpan hanya **hash SHA-256**-nya. Set umur 30 hari. **Rotasi** setiap kali dipakai. Jika refresh token yang sudah dirotasi dipakai lagi, cabut semua refresh token milik user itu.
- Simpan semua ticket (state GitHub, hasil login, link) dalam bentuk hash, sekali pakai, dan dengan masa berlaku.
- Jangan pernah mengembalikan Entity langsung; selalu ubah ke DTO.
- Jangan menulis token, password, atau client secret ke log.
- Normalisasi email ke huruf kecil.

### 7.5 Buat seeder (`@Profile("dev")`)

Isi data hanya jika tabel `users` kosong: 10 user dummy (Datafaker, locale Indonesia jika tersedia), sebagian dengan `creator_profiles`, dan 3 dengan `provider_profiles` berstatus berbeda. Beri semua user dummy identitas `local` dengan password yang sama, lalu tulis password itu di README.

---

## 8. Bangun Android sesuai spesifikasi

- Set `BuildConfig.API_BASE_URL`: debug = `http://localhost:8080/api/` (HP terhubung lewat `adb reverse`), release = placeholder `https://api.example.com/api/`.
- Baca `BuildConfig.GOOGLE_WEB_CLIENT_ID` dari `local.properties`, dan beri contohnya di README.
- Izinkan HTTP polos hanya di build **debug** lewat `src/debug/res/xml/network_security_config.xml`.
- Daftarkan deep link `templateapp://auth/callback` di intent-filter `MainActivity` dengan `launchMode="singleTask"`.
- Pasang header `Authorization: Bearer` lewat `AuthInterceptor` ke semua request kecuali `/auth/*`.
- Tangkap 401 dengan `TokenAuthenticator` (OkHttp Authenticator), panggil `/auth/refresh` **satu kali** (synchronized, cegah refresh ganda), lalu ulangi request. Jika refresh gagal, hapus sesi dan kirim event "sesi berakhir".
- Ubah `ErrorResponse` menjadi `ApiError(code, message, fieldErrors)` di `ApiErrorParser`.
- Pakai `Resource` (LOADING / SUCCESS / ERROR) dan `StateView` di semua layar yang memuat data.
- Simpan semua teks UI di `strings.xml` (Bahasa Indonesia), semua warna dan ukuran di `colors.xml` / `dimens.xml`.
- Gunakan tema berbasis `Theme.Material3.DayNight.NoActionBar` dengan dark mode di `values-night/`.
- Buat area sentuh minimal 48dp dan beri `contentDescription` pada ikon.
- Null-kan ViewBinding di Fragment pada `onDestroyView()`.
- Jangan menyimpan referensi View, Fragment, atau Context Activity di ViewModel.

---

## 9. Terapkan desain UI (terinspirasi nextjs.org)

### 9.1 Ikuti arah desain

Jadikan gaya **nextjs.org** sebagai arah visual seluruh app: tegas, monokrom, rapi, dan profesional, **bukan** bergaya gaming.

- **Monokrom**: pakai hitam, putih, dan abu-abu. Pakai warna lain hanya untuk makna (link/fokus, sukses, peringatan, error).
- **Kontras tinggi dan tipografi kuat**: buat judul besar dan tebal dengan jarak huruf sedikit dirapatkan.
- **Garis tipis, bukan bayangan**: pakai border 1dp abu-abu untuk pemisah dan kartu, elevasi 0.
- **Sudut kecil**: pakai radius 6–8dp.
- **Ruang kosong**: beri ruang yang lega, rata kiri, hierarki jelas.
- **Detail mono**: pakai Geist Mono untuk label kecil, badge, dan metadata.

Jangan pakai gradien mencolok, efek glow/neon, warna-warni, bayangan tebal, ilustrasi kartun ramai, emoji sebagai ikon, atau nuansa gaming.

Ambil **gayanya saja**. Jangan memakai logo, ikon segitiga, nama, ilustrasi, atau aset apa pun milik Next.js/Vercel.

### 9.2 Definisikan token desain (nilai awal)

Definisikan semua nilai ini sekali di `res/values/` (dan `values-night/`), lalu pakai lewat tema dan style. Jangan menulis warna atau ukuran langsung di layout.

**Warna**

| Token | Terang | Gelap | Pakai untuk |
|---|---|---|---|
| `color_background` | `#FFFFFF` | `#000000` | Latar layar |
| `color_surface` | `#FFFFFF` | `#0A0A0A` | Kartu, sheet, dialog |
| `color_surface_subtle` | `#FAFAFA` | `#111111` | Latar input, area sekunder |
| `color_foreground` | `#0A0A0A` | `#EDEDED` | Teks utama, tombol utama |
| `color_muted` | `#666666` | `#A1A1A1` | Teks sekunder, placeholder |
| `color_border` | `#EAEAEA` | `#2E2E2E` | Garis kartu, pemisah, input |
| `color_border_strong` | `#C9C9C9` | `#454545` | Hover/terpilih, input fokus |
| `color_link` | `#0068D6` | `#52A8FF` | Link dan cincin fokus |
| `color_success` | `#0A7D3A` | `#4CC38A` | Status berhasil, provider disetujui |
| `color_warning` | `#A35200` | `#F5A524` | Status menunggu verifikasi |
| `color_error` | `#C9252D` | `#FF6166` | Error, ditolak, ditangguhkan |

Petakan ke atribut Material 3 di `themes.xml`: `colorPrimary` = foreground, `colorOnPrimary` = background, `colorSurface` = surface, `colorOutline` = border, `colorError` = error. Matikan surface tint Material 3 agar permukaan tetap netral.

**Tipografi** (Geist Sans, kecuali disebut lain)

| Style | Ukuran | Berat | Jarak huruf |
|---|---|---|---|
| `TextAppearance.App.Display` | 32sp | Bold | -0.02 |
| `TextAppearance.App.Headline` | 24sp | SemiBold | -0.01 |
| `TextAppearance.App.Title` | 18sp | SemiBold | 0 |
| `TextAppearance.App.Body` | 15sp | Regular | 0 |
| `TextAppearance.App.BodySmall` | 13sp | Regular | 0 |
| `TextAppearance.App.Label` | 12sp | Medium, **Geist Mono**, huruf kapital | 0.06 |

**Ukuran dan bentuk**

| Token | Nilai |
|---|---|
| Grid spasi | kelipatan 4dp: 4, 8, 12, 16, 24, 32, 48 |
| Padding samping layar | 24dp |
| Radius kecil (tombol, input, chip) | 6dp |
| Radius sedang (kartu, banner) | 8dp |
| Radius besar (bottom sheet atas) | 12dp |
| Tebal border | 1dp |
| Tinggi tombol | 48dp |
| Elevasi | 0 untuk semua komponen kecuali dialog |

### 9.3 Gaya komponen

| Komponen | Terapkan |
|---|---|
| Tombol utama | Isi `color_foreground`, teks `color_background`, radius 6dp, tanpa bayangan, teks tidak kapital semua. |
| Tombol sekunder | Outlined, border `color_border`, teks `color_foreground`. |
| Tombol teks / link | Warna `color_link`, tanpa garis bawah kecuali saat ditekan. |
| Input | `TextInputLayout` outlined, border `color_border`, fokus `color_border_strong` + cincin `color_link`, label di atas. |
| Kartu | `MaterialCardView` outlined, stroke 1dp `color_border`, elevasi 0. Kartu terpilih: stroke 2dp `color_foreground`. |
| Chip | Outlined; saat terpilih isi `color_foreground` dengan teks `color_background`. |
| App bar | Datar, elevasi 0, garis bawah 1dp `color_border`, nama app berupa teks (Geist SemiBold). |
| Badge | Pill outlined dengan teks `TextAppearance.App.Label`, contoh "SEGERA HADIR". |
| Banner status | Kotak outlined radius 8dp, ikon + teks warna semantik, latar tint sangat tipis. |
| Ikon | Material Symbols **Outlined**, 20–24dp, warna mengikuti teks. |
| Lottie | Line art monokrom mengikuti `color_foreground`. Warnai ulang sesuai tema dengan dynamic properties Lottie. |
| Tombol Google & GitHub | Ikuti pedoman branding resmi masing-masing (logo asli, teks "Lanjutkan dengan Google/GitHub"). Google = outlined, GitHub = tombol utama. |

### 9.4 Tata letak layar (awal)

| Layar | Susun |
|---|---|
| Intro (3 halaman) | Judul Display rata kiri (contoh: "Bikin website. Tanpa coding."), subjudul `color_muted`, ilustrasi Lottie garis, indikator halaman kecil, tombol "Lanjut" dan "Lewati" di bawah. |
| Dashboard Pembuat Website | App bar + tombol Masuk/avatar, badge "SEGERA HADIR", judul "Buat website pertamamu", teks penjelasan, Lottie, tombol utama nonaktif. Opsional: pola grid titik sangat tipis di latar (`color_border`, opasitas rendah). |
| Dashboard Provider | Sama seperti di atas, dengan banner status provider di paling atas. |
| Masuk | Judul "Masuk", subjudul singkat, tombol Google, tombol GitHub, pemisah garis tipis bertulis "atau", form email + password, tombol Masuk, link lupa password dan daftar, teks persetujuan kecil di bawah. |
| Daftar | Judul "Buat akun", form nama, email, password, tombol Daftar, tombol Google/GitHub sebagai alternatif. |
| Pilih peran | Judul "Kamu mau pakai app ini sebagai apa?", dua kartu outlined besar (judul, deskripsi, ikon panah), teks kecil "Peran bisa ditambah nanti." |
| Form onboarding | Satu kolom, label di atas input, chip tujuan website dalam baris yang bisa turun, tombol utama di bawah + tombol "Lewati" (khusus creator). |
| Menu profil | Bottom sheet: nama + email, badge mode aktif, daftar aksi dengan pemisah garis tipis. |
| Pengaturan | Daftar bergrup dengan judul grup memakai style Label (mono). |

Pastikan semua layar rapi di lebar 360dp dan di mode gelap.

---

## 10. Siapkan lingkungan dan tools

### 10.1 Bagi tugas setup

| Jenis setup | Kerjakan dengan cara |
|---|---|
| Software sistem di Ubuntu (JDK, Docker, Android Studio, aturan udev, scrcpy) | Tulis perintah + penjelasannya untuk Aris, tunggu konfirmasi, lalu verifikasi. |
| Android SDK | Pandu Aris menjalankan Setup Wizard Android Studio (pilih Standard). |
| HP asli | Pandu Aris mengaktifkan Developer options dan USB debugging. |
| Project Android awal | Minta Aris membuat lewat wizard Android Studio (bagian 4.4). |
| Database & user PostgreSQL | Tulis perintahnya untuk dijalankan Aris. |
| OAuth Google & GitHub | Tulis langkah per langkah untuk Aris. |
| Extension VS Code | Rekomendasikan lewat `backend/.vscode/extensions.json`. |
| Setup di dalam project (Maven wrapper, dependency, konfigurasi, `.env.example`, `application-*.yml`, contoh `local.properties`, font, Lottie, `.vscode/`) | Kerjakan langsung. |
| Verifikasi | Jalankan hanya perintah cek yang tidak mengubah apa pun (`java -version`, `psql --version`, `docker --version`, `adb devices`, dll). |

Cek dulu apa yang sudah terpasang sebelum menulis perintah instalasi. Jangan meminta instal ulang. **VS Code, PostgreSQL, dan Git sudah terpasang** di laptop Aris.

### 10.2 Siapkan daftar kebutuhan

| Tools | Keperluan |
|---|---|
| Git | Version control (sudah ada) |
| JDK 21 | Build backend, sekaligus Gradle JDK untuk Android |
| PostgreSQL | Database development (sudah ada) |
| Docker Engine | Menjalankan Testcontainers saat test backend |
| VS Code + extension Java & Spring Boot | Editor backend (VS Code sudah ada) |
| Android Studio + Android SDK (lewat Setup Wizard) | Editor Android, SDK, `adb` |
| `ANDROID_HOME` & `platform-tools` di PATH | Menjalankan `adb` dan `./gradlew` dari terminal |
| Aturan udev Android | Mendeteksi HP asli lewat USB |
| HP Android asli | Android 8.0 (API 26) ke atas, ada akun Google, kabel USB data |
| scrcpy (opsional) | Menampilkan layar HP di laptop |
| Maven & Gradle | **Jangan instal**; pakai wrapper `./mvnw` dan `./gradlew` |

Jangan siapkan emulator maupun KVM.

### 10.3 Gunakan perintah acuan untuk Ubuntu

Periksa ulang perintah di bawah terhadap dokumentasi resmi terbaru sebelum memberikannya ke Aris.

```bash
# Cek yang sudah terpasang
java -version; psql --version; docker --version; git --version; code --version

# JDK 21
sudo apt update
sudo apt install -y openjdk-21-jdk

# Database & user untuk project (PostgreSQL sudah terpasang)
sudo -u postgres psql -c "CREATE USER templateapp WITH PASSWORD 'ganti_password_ini';"
sudo -u postgres createdb -O templateapp templateapp

# Docker (ikuti panduan resmi Docker Engine untuk Ubuntu jika ingin versi terbaru)
sudo apt install -y docker.io
sudo usermod -aG docker $USER          # logout-login setelah ini

# Android Studio, lalu buka dan jalankan Setup Wizard (pilih Standard) untuk mengunduh SDK
sudo snap install android-studio --classic

# Tambahkan ke ~/.bashrc agar adb bisa dipakai dari terminal, lalu jalankan: source ~/.bashrc
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools

# HP asli lewat USB + mirror layar (opsional)
sudo apt install -y android-sdk-platform-tools-common scrcpy
```

Jelaskan dalam satu kalimat fungsi dan alasan setiap perintah.

Tulis langkah menyiapkan HP di README: *Pengaturan → Tentang ponsel →* ketuk *Nomor build* 7 kali → buka *Opsi pengembang* → aktifkan *USB debugging* → colok ke laptop → izinkan pop-up "Allow USB debugging" → cek `adb devices`. Sebutkan juga *Wireless debugging* (`adb pair`, `adb connect`) sebagai alternatif tanpa kabel.

### 10.4 Pandu setup OAuth

Tulis langkah lengkap berikut di README:

1. **Google Cloud Console**: buat project, atur OAuth consent screen (data dasar: email, profil), buat **OAuth Client ID tipe Web** (untuk backend & `serverClientId`), buat **OAuth Client ID tipe Android** (package `com.aris.templateapp` + SHA-1 debug dari `./gradlew signingReport`).
2. **GitHub**: buat **OAuth App** khusus development dengan callback `http://localhost:8080/api/auth/github/callback`, salin Client ID, lalu buat Client Secret.

---

## 11. Tulis README.md

Tulis dan rawat `README.md` di root repo dalam Bahasa Indonesia, untuk pembaca yang baru pertama kali membuka project. Masukkan minimal:

1. **Tentang project**: satu paragraf + status fitur (sudah jalan vs "Segera hadir").
2. **Struktur repo**: penjelasan singkat `backend/`, `android/`, `docs/`.
3. **Prasyarat**: tabel tools + versi yang dipakai.
4. **Persiapan lingkungan Ubuntu**: perintah bagian 10.3 beserta penjelasannya.
5. **Alat kerja**: Android Studio membuka `android/`, VS Code membuka `backend/`, agent bekerja lewat terminal (bagian 4.4), cara menyamakan Gradle JDK, dan kapan klik "Sync Now".
6. **Setup database**: membuat user dan database PostgreSQL.
7. **Setup OAuth Google & GitHub**: langkah bagian 10.4, termasuk catatan SHA-1 debug vs rilis.
8. **Menjalankan backend**:
   - Salin `.env.example` menjadi `.env`, isi nilainya, dan muat environment variable
   - Dari terminal: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
   - Dari VS Code: task `backend: jalankan (dev)` atau launch configuration
   - Alamat Swagger UI
   - Test: `./mvnw test` (Docker harus aktif)
9. **Menyiapkan HP**: Developer options, USB debugging, `adb devices`, wireless debugging.
10. **Menjalankan app Android di HP**:
    - Isi `local.properties` (contoh `GOOGLE_WEB_CLIENT_ID`)
    - Jalankan `adb reverse tcp:8080 tcp:8080` setiap HP dicolok ulang
    - Jalankan lewat Android Studio (pilih HP → Run) atau lewat terminal (perintah bagian 4.4)
    - Lihat log lewat Logcat Android Studio atau `adb logcat`
    - Tampilkan layar HP di laptop dengan scrcpy (opsional)
11. **Akun dummy** dari seeder beserta password-nya.
12. **Contoh SQL** untuk mengubah status provider (approve, reject, suspend).
13. **Troubleshooting**, minimal:
    - App tidak bisa menjangkau backend (lupa `adb reverse`, backend belum jalan, cleartext HTTP)
    - HP tidak terdeteksi atau berstatus `unauthorized` (kabel, udev, pop-up izin)
    - Login Google gagal (SHA-1 belum didaftarkan, Client ID Web/Android tertukar)
    - Callback GitHub tidak kembali ke app (callback URL, deep link, `adb reverse`)
    - Android Studio error setelah file build diubah (klik Sync Now)
    - Gradle JDK berbeda antara terminal dan Android Studio
    - Port 8080 sudah dipakai
    - Error checksum Flyway
    - Test gagal karena Docker tidak aktif
    - Laptop lambat saat build (tutup aplikasi lain, batasi memori Gradle)
14. **Catatan belajar**: tautan ke `docs/catatan-belajar/`.

Perbarui README setiap kali sebuah fase mengubah cara setup atau menjalankan project.

---

## 12. Kerjakan per fase

Untuk setiap fase: kerjakan → tes → perbarui README → tulis catatan belajar → **berhenti dan laporkan**.

| Fase | Kerjakan | Anggap selesai jika |
|---|---|---|
| **00** Persiapan lingkungan & repo | Cek tools yang sudah ada, berikan perintah instalasi dan langkah menyiapkan HP (bagian 10), verifikasi setelah Aris menjalankannya. Buat struktur monorepo, `.gitignore`, README awal, folder `docs/`. | `java`, `psql`, `docker`, `code`, `adb` terverifikasi; Android Studio + SDK terpasang; `adb devices` menampilkan HP berstatus `device`; repo rapi tanpa file rahasia |
| **01** Fondasi backend | Inisialisasi Spring Boot (dependency bagian 4.3), `backend/.vscode/`, profile dev/prod, `AppProperties`, Flyway V1–V6, `GlobalExceptionHandler`, Swagger. | App start dari terminal dan VS Code, migrasi jalan, Swagger UI terbuka |
| **02** Auth email | Register, login, refresh (dengan rotasi), logout, `JwtAuthFilter`, `SecurityConfig`, `GET /users/me`. | Daftar → masuk → `/users/me` → refresh → logout berhasil lewat Swagger; test lulus |
| **03** Google, GitHub, penyambungan | Pandu Aris membuat OAuth Google & GitHub. Buat `GoogleTokenVerifier`, `GitHubOAuthClient`, alur GitHub 6.4, `AccountLinkingService`, endpoint identitas. | Unit test semua cabang 6.3 lulus |
| **04** User, onboarding, mode, seeder | Endpoint onboarding & active-mode, provider profile, `DummyDataSeeder`. | Integration test (Testcontainers) lulus; seeder jalan di dev |
| **05** Fondasi Android & desain | Minta Aris membuat project lewat wizard (bagian 4.4). Lalu buat `gradle.properties`, Hilt, Navigation, ViewBinding, token desain 9.2, font Geist, style komponen 9.3, layar katalog komponen khusus build debug, `strings.xml`, NetworkModule, interceptor, authenticator, TokenStorage, SessionStore, Resource, AppExecutors. | `./gradlew assembleDebug` berhasil; app terbuka di HP; project terbuka tanpa error di Android Studio setelah Sync; katalog komponen benar di mode terang dan gelap |
| **06** Startup & dashboard "Segera hadir" | Splash, `StartupViewModel`, intro, CreatorDashboard & ProviderDashboard sesuai 9.4, Lottie. | Alur 6.1 benar untuk tamu |
| **07** Masuk & daftar email | LoginFragment, RegisterFragment sesuai 9.4, validasi form, pesan error 6.2. | Daftar & masuk ke backend lokal berhasil dari HP (via `adb reverse`) |
| **08** Google & GitHub di Android | Credential Manager, Custom Tabs + deep link, `LinkAccountDialog`. | Daftar Google → masuk GitHub dengan email sama → diminta masuk Google → tersambung → masuk GitHub berikutnya langsung ke akun yang sama |
| **09** Onboarding, mode, pengaturan | Pilih peran, form creator/provider, menu profil, beralih mode, metode login terhubung, keluar. | Semua aturan 6.6 & 6.7 berjalan |
| **10** Polesan & cek akhir | State kosong/error, aksesibilitas, konsistensi desain, screenshot `docs/screenshots/`, README lengkap, uji semua skenario 13.2. | Semua checklist bagian 14 terpenuhi dan semua skenario bagian 13.2 berhasil |

---

## 13. Wujudkan hasil akhir berikut

Gunakan bagian ini sebagai gambaran hasil akhir cakupan saat ini. Setelah Fase 10 selesai, semua poin di bagian ini harus bisa ditunjukkan ke Aris.

### 13.1 Serahkan hasil berikut

| Hasil | Wujud yang diharapkan |
|---|---|
| Repo | Monorepo sesuai bagian 5, riwayat commit rapi per fase, tanpa file rahasia |
| Backend | Spring Boot berjalan dengan profile `dev`, semua endpoint bagian 7.3 aktif, migrasi Flyway V1–V6 terpasang, seeder mengisi data dummy, Swagger UI bisa dipakai untuk mencoba semua endpoint, semua test lulus |
| App Android | APK debug terpasang di HP Aris, semua layar bagian 2.1 bisa dibuka, terhubung ke backend lokal lewat `adb reverse` |
| Konfigurasi alat | `android/` terbuka tanpa error di Android Studio, `backend/` siap dijalankan dan di-debug dari VS Code lewat `backend/.vscode/` |
| `README.md` | Memenuhi bagian 11 dan bisa diikuti dari nol sampai app jalan di HP |
| Catatan belajar | `docs/catatan-belajar/fase-00.md` sampai `fase-10.md` dengan format bagian 13.4 |
| Screenshot | Tangkapan layar setiap layar dalam mode terang dan gelap di `docs/screenshots/`, ditampilkan di README. Ambil dengan `adb exec-out screencap -p > nama-file.png`. |

### 13.2 Pastikan skenario berikut berjalan di HP asli

Uji setiap skenario di HP Aris, lalu tuliskan hasilnya di laporan Fase 10.

| No | Skenario | Hasil yang diharapkan |
|---|---|---|
| 1 | Pasang app lalu buka pertama kali | Intro 3 halaman muncul, lalu Dashboard Pembuat Website "Segera hadir" sebagai tamu dengan tombol **Masuk**. Saat app dibuka lagi, intro tidak muncul. |
| 2 | Daftar dengan email | Akun dibuat, onboarding muncul (pilih peran → form pembuat website), lalu masuk ke Dashboard Pembuat Website dengan avatar di app bar. |
| 3 | Tutup app lalu buka lagi setelah login | Langsung masuk ke dashboard mode terakhir tanpa login ulang. |
| 4 | Biarkan access token kedaluwarsa (atur sementara TTL pendek untuk tes) | Request tetap berhasil karena refresh token diperbarui diam-diam. |
| 5 | Masuk dengan Google memakai akun baru | Akun dibuat otomatis tanpa menu Daftar, nama terisi dari Google, onboarding muncul. |
| 6 | Akun dibuat lewat Google, lalu coba masuk lewat form email | Muncul pesan "Akun ini terdaftar dengan Google. Silakan masuk dengan Google." |
| 7 | Akun dibuat lewat Google, lalu masuk lewat GitHub dengan email terverifikasi yang sama | Muncul dialog penyambungan → user masuk dengan Google → GitHub tersambung. Setelah keluar, masuk lewat GitHub langsung ke akun yang sama dengan data yang sama. |
| 8 | Email GitHub berbeda atau privat | Akun baru dibuat. Dari Pengaturan → Metode login terhubung, user bisa menyambungkan GitHub secara manual ke akun yang benar. |
| 9 | Tekan **Jadi penyedia template** | Form provider muncul tanpa pilih peran, lalu Dashboard Provider "Segera hadir" dengan banner "Akunmu sedang diverifikasi." |
| 10 | Beralih mode, tutup app, buka lagi | App membuka mode terakhir yang dipilih. |
| 11 | Ubah status provider lewat SQL ke `approved`, lalu `suspended` | `approved`: banner hilang. `suspended`: mode provider terkunci dan user diarahkan ke mode pembuat website dengan pesan. |
| 12 | Matikan internet saat sudah login, buka app | App tetap terbuka memakai data tersimpan. Aksi online menampilkan "Tidak ada koneksi..." dengan tombol coba lagi. |
| 13 | Keluar dari akun | Kembali ke Dashboard Pembuat Website sebagai tamu. Masuk lagi berjalan normal. |
| 14 | Ganti HP ke mode gelap | Semua layar, termasuk animasi Lottie, terbaca jelas dan konsisten. |
| 15 | Cabut HP lalu colok lagi tanpa `adb reverse` | App menampilkan error koneksi yang jelas. Setelah `adb reverse` dijalankan ulang, app normal kembali. |

### 13.3 Hasilkan tampilan seperti ini

- Kesan pertama: tegas, monokrom, profesional, mirip nuansa nextjs.org, tanpa kesan gaming (bagian 9).
- Setiap layar memakai token dan style dari bagian 9.2 dan 9.3; tidak ada layar yang terlihat "beda sendiri".
- Dashboard "Segera hadir" terlihat sengaja dirancang, bukan layar kosong: badge, judul, penjelasan, dan animasi tersusun rapi.
- Layar Masuk jelas urutannya: Google, GitHub, lalu email.
- Teks ringkas, jelas, dan seluruhnya dalam Bahasa Indonesia.

### 13.4 Laporkan dengan format ini

Gunakan format berikut untuk laporan di chat dan untuk `docs/catatan-belajar/fase-XX.md`:

```markdown
# Fase XX: <judul fase>

## Yang dikerjakan
| File | Fungsi |
|---|---|
| ... | ... |

## Alasan keputusan
- <keputusan> → <alasan> (alternatif yang tidak dipilih: <alternatif> karena <alasan>)

## Cara menjalankan & mengetes
1. ...
2. ...

## Hasil tes
- `./mvnw test` / `./gradlew testDebugUnitTest`: <lulus/gagal, jumlah test>
- Uji manual: <apa yang dicoba dan hasilnya>

## Konsep yang dipelajari
- <konsep> : <penjelasan singkat dengan bahasa sederhana>

## Latihan untuk Aris
1. <latihan kecil yang bisa dikerjakan sendiri>

## Yang perlu Aris lakukan
- <mis. klik Sync Now, jalankan perintah X, isi .env>

## Rencana fase berikutnya
- ...
```

### 13.5 Jaga kualitas kode

- Pakai bahasa Inggris untuk nama class, method, variabel, dan endpoint. Pakai Bahasa Indonesia untuk teks UI, komentar, pesan commit, README, dan catatan belajar.
- Jangan tinggalkan kode mati, kode yang dikomentari, atau `TODO` tanpa penjelasan.
- Pastikan build backend dan Android bersih dari error; jelaskan di laporan jika ada warning yang sengaja dibiarkan.
- Jangan menulis rahasia, URL, warna, ukuran, atau teks UI langsung di kode; pakai konfigurasi dan resource.
- Tulis test untuk logika penting: autentikasi, rotasi refresh token, penyambungan akun (semua cabang bagian 6.3), onboarding, dan perpindahan mode.

---

## 14. Periksa checklist akhir

- [ ] Tidak ada kode/tabel/endpoint untuk fitur di bagian 2.2
- [ ] Tidak ada rahasia di repo; `.env.example` dan contoh `local.properties` ada di README
- [ ] Semua test backend lulus (`./mvnw test`)
- [ ] `./gradlew assembleDebug`, `./gradlew testDebugUnitTest`, dan `./gradlew lint` berhasil
- [ ] Project `android/` terbuka di Android Studio tanpa error setelah Sync; project `backend/` berjalan dari VS Code
- [ ] App jalan di HP asli (debug) dengan backend lokal lewat `adb reverse`
- [ ] Mode tamu → Masuk → onboarding → dashboard sesuai mode
- [ ] Penyambungan Google ↔ GitHub ↔ email berjalan dan aman
- [ ] Refresh token berjalan diam-diam; sesi berakhir ditangani rapi
- [ ] Semua layar mengikuti bagian 9: monokrom, border tipis, tanpa bayangan, font Geist
- [ ] Tidak ada warna, ukuran, atau teks yang ditulis langsung di layout (semua lewat `res/values`)
- [ ] Dark mode terbaca di semua layar, termasuk animasi Lottie
- [ ] Tidak ada aset atau logo milik Next.js/Vercel
- [ ] README memenuhi semua poin bagian 11 dan sudah dicoba diikuti dari awal
- [ ] Catatan belajar tiap fase ada di `docs/catatan-belajar/` dengan format bagian 13.4
- [ ] Semua skenario bagian 13.2 berhasil di HP asli dan hasilnya tercatat di laporan Fase 10
- [ ] Screenshot mode terang dan gelap ada di `docs/screenshots/` dan tampil di README

---

## 15. Tambahan: fitur provider (Oktober 2026)

Aris menambahkan rancangan **`docs/rancangan/alur-provider.md`** (salinan dari `Instruksi dan alur/alurUntukProvider.md`). Untuk hal yang dibahasnya, dokumen itu **mengesampingkan** bagian 2.2, 6.6, dan 7 di file ini:

- Status provider hanya `active` dan `suspended`; provider langsung aktif setelah mengisi form (tanpa verifikasi admin). Data lama `pending`/`approved`/`rejected` menjadi `active`.
- Dashboard Provider memakai bottom navigation: Beranda, Upload (**"Segera hadir"**), Template Anda, Profil.
- Tabel `templates`, `template_checks`, `template_check_issues`, `template_events`, `notifications` boleh dibuat (sebagai "mesin siap pakai"; data hanya dari test & seeder demo opsional sampai Upload ada).
- Grafik tren download dibuat sebagai **custom View sendiri** (tanpa MPAndroidChart).
- **Push notification (Firebase Cloud Messaging) ditunda** sampai segmen Upload dibangun; versi ini hanya notifikasi di dalam app.
- Bagian 9 dokumen itu (Upload, Template Anda lanjutan, Profil lanjutan) **belum boleh dibangun**.
