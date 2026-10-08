# Rakit

**Rakit** adalah app Android untuk merakit website tanpa coding. Satu akun punya dua mode: **pembuat website** (memilih template, mengedit teks/warna/foto, lalu export ke HTML/CSS/JS) dan **penyedia template** (mengunggah template untuk dipakai orang lain). User bisa langsung mencoba sebagai tamu; login hanya diminta saat butuh fitur online.

### Status fitur

| Fitur | Status |
|---|---|
| Persiapan lingkungan & struktur repo | Sudah (Fase 00) |
| Backend: fondasi (konfigurasi, migrasi database, format error, Swagger UI) | Sudah (Fase 01) |
| Backend: auth email (daftar, masuk, refresh token dengan rotasi, keluar, `/users/me`) | Sudah (Fase 02) |
| Backend: login Google & GitHub, penyambungan akun, metode login terhubung | Sudah (Fase 03), perlu kredensial OAuth (lihat "Setup OAuth") |
| Backend: onboarding pembuat website & provider, beralih mode, data dummy | Sudah (Fase 04) |
| Android: fondasi (tema & token desain, font Geist, jaringan, penyimpanan token terenkripsi, katalog komponen) | Sudah (Fase 05) |
| Android: splash, layar awal otomatis (bagian 6.1), intro 3 halaman, Dashboard Pembuat Website & Provider "Segera hadir" dengan Lottie | Sudah (Fase 06) |
| Android: Masuk & Daftar dengan email (validasi form, pesan error 6.2, coba lagi saat offline) | Sudah (Fase 07) |
| Android: login Google (Credential Manager), GitHub (Custom Tabs + deep link), dialog penyambungan akun | Sudah (Fase 08) |
| Android: onboarding (pilih peran, form pembuat website & provider), menu profil, beralih mode, pengaturan, metode login terhubung, keluar | Sudah (Fase 09) |
| Polesan akhir: tombol Coba lagi di semua aksi online, screenshot terang/gelap, lint bersih, uji skenario 13.2 di HP | Sudah (Fase 10) |
| Backend provider: status `active`/`suspended`, Beranda (ringkasan, tren download, populer, perlu tindakan), Template Anda (filter/urutan/cari), Profil, event dilihat/didownload, notifikasi, seeder demo | Sudah (Fase 11) |
| Android Dashboard Provider: bottom navigation, Beranda (perlu tindakan + badge, checklist provider baru, ringkasan 7/30 hari, grafik area, populer, panduan), Template Anda (cari, filter, urutan, paginasi), detail + hasil pengecekan, Profil + Edit profil | Sudah (Fase 12) |
| Backend galeri Template publik (`GET /api/templates`), edit profil pembuat website | Sudah (Fase 13) |
| Android Dashboard Pembuat Website: Beranda (Mulai, Lanjutkan project, Template untuk anda, panduan), tab Project (Room: cari, filter, urutan, ganti nama, duplikat, hapus), tombol +, galeri Template, Profil + edit, editor template/custom "Segera hadir" | Sudah (Fase 14) |
| Backend Upload: ZIP per potongan (bisa dilanjutkan), mesin pengecekan 67 aturan + ZIP uji, draft & kuota, laporan "Ini keliru?", artikel Panduan | Sudah (Fase 15) |
| Android Upload langkah 1–3: tab Upload (draft, perlu diperbaiki), pilih ZIP + cek kilat, upload dengan progres & lanjut otomatis, daftar tahap pengecekan, cek di WebView HP, "Belum memenuhi standar" + Panduan + laporan keliru, Info template + thumbnail | Sudah (Fase 16), menunggu uji di HP |
| Upload langkah 4: editor Tandai bagian (slide per section, ketuk elemen, gaya, hubungkan isian, saran, koreksi section, undo/redo, cadangan di HP) | Sudah (Fase 17), menunggu uji di HP |
| Upload langkah 5–6: Coba sebagai pengguna (preview langsung, gaya, tema, uji isi panjang) dan Kirim (pengecekan akhir, salin library CDN, sisipkan atribut penandaan, tayang di galeri) | Sudah (Fase 18), menunggu uji di HP |
| Push notification (FCM), versi template, penandaan tingkat section | Segera hadir (menunggu diskusi, rancangan Upload bagian 13–14) |
| Editor website (template & custom mode), export ZIP | Segera hadir (layar editor sudah ada, isinya menunggu diskusi) |
| Galeri template, editor, export, publish, lupa password | Segera hadir (belum dibangun) |

> 🏷️ **Nama app: Rakit.** Nama ini hanya untuk tampilan. Nama teknis lama tetap dipakai: package `com.aris.templateapp`, deep link `templateapp://`, database `templateapp`, dan email dummy `@templateapp.test`. Mengganti nama teknis berarti membuat ulang OAuth Client Android (package + SHA-1), mengubah callback GitHub, dan memindahkan database, padahal pengguna tidak pernah melihatnya.

> 👥 **Ikut mengembangkan bersama tim / memakai Windows?** Ikuti [`docs/panduan-kolaborator.md`](docs/panduan-kolaborator.md): langkah lengkap dari nol untuk **Windows** dan Linux, cara mendaftarkan SHA-1 & Test user Google, berbagi kredensial dengan aman, dan alur kerja Git tim. README ini berisi rangkuman untuk Linux (Ubuntu).

---

## Tampilan

Diambil dari HP asli (realme, Android 13) dengan akun dummy. File lengkap ada di [`docs/screenshots/`](docs/screenshots/).

| Layar | Terang | Gelap |
|---|---|---|
| Intro | <img src="docs/screenshots/01-intro-terang.png" width="220"> | <img src="docs/screenshots/01-intro-gelap.png" width="220"> |
| Beranda pembuat website (tamu, belum punya project) | <img src="docs/screenshots/02-dashboard-tamu-terang.png" width="220"> | <img src="docs/screenshots/02-dashboard-tamu-gelap.png" width="220"> |
| Masuk | <img src="docs/screenshots/03-masuk-terang.png" width="220"> | <img src="docs/screenshots/03-masuk-gelap.png" width="220"> |
| Daftar | <img src="docs/screenshots/04-daftar-terang.png" width="220"> | <img src="docs/screenshots/04-daftar-gelap.png" width="220"> |
| Pilih peran | <img src="docs/screenshots/05-pilih-peran-terang.png" width="220"> | <img src="docs/screenshots/05-pilih-peran-gelap.png" width="220"> |
| Form pembuat website | <img src="docs/screenshots/06-form-pembuat-website-terang.png" width="220"> | <img src="docs/screenshots/06-form-pembuat-website-gelap.png" width="220"> |
| Form provider | <img src="docs/screenshots/07-form-provider-terang.png" width="220"> | <img src="docs/screenshots/07-form-provider-gelap.png" width="220"> |
| Dashboard provider: Beranda (akun demo) | <img src="docs/screenshots/08-dashboard-provider-terang.png" width="220"> | <img src="docs/screenshots/08-dashboard-provider-gelap.png" width="220"> |
| Beranda: ringkasan, grafik tren, populer | <img src="docs/screenshots/12-beranda-grafik-terang.png" width="220"> | <img src="docs/screenshots/12-beranda-grafik-gelap.png" width="220"> |
| Beranda provider baru (checklist) | <img src="docs/screenshots/15-beranda-provider-baru-terang.png" width="220"> | <img src="docs/screenshots/15-beranda-provider-baru-gelap.png" width="220"> |
| Template Anda | <img src="docs/screenshots/13-template-anda-terang.png" width="220"> | <img src="docs/screenshots/13-template-anda-gelap.png" width="220"> |
| Detail template + hasil pengecekan | <img src="docs/screenshots/14-detail-template-terang.png" width="220"> | <img src="docs/screenshots/14-detail-template-gelap.png" width="220"> |
| Beranda pembuat website (punya project) | <img src="docs/screenshots/16-beranda-pembuat-website-terang.png" width="220"> | <img src="docs/screenshots/16-beranda-pembuat-website-gelap.png" width="220"> |
| Project | <img src="docs/screenshots/17-project-terang.png" width="220"> | <img src="docs/screenshots/17-project-gelap.png" width="220"> |
| Galeri Template | <img src="docs/screenshots/18-galeri-template-terang.png" width="220"> | <img src="docs/screenshots/18-galeri-template-gelap.png" width="220"> |
| Profil pembuat website | <img src="docs/screenshots/19-profil-pembuat-website-terang.png" width="220"> | <img src="docs/screenshots/19-profil-pembuat-website-gelap.png" width="220"> |
| Editor "Segera hadir" | <img src="docs/screenshots/20-editor-segera-hadir-terang.png" width="220"> | <img src="docs/screenshots/20-editor-segera-hadir-gelap.png" width="220"> |
| Pengaturan | <img src="docs/screenshots/10-pengaturan-terang.png" width="220"> | <img src="docs/screenshots/10-pengaturan-gelap.png" width="220"> |
| Metode login terhubung | <img src="docs/screenshots/11-metode-login-terang.png" width="220"> | <img src="docs/screenshots/11-metode-login-gelap.png" width="220"> |

---

## Struktur repo

```
Rakit Apps/
├── AGENTS.md             ← instruksi pengembangan untuk agent AI (pengguna Claude Code: salin menjadi CLAUDE.md, tidak di-commit)
├── README.md               ← file ini
├── docs/
│   ├── rancangan-app-template-website.pdf   ← alur besar app
│   ├── catatan-belajar/    ← catatan belajar per fase
│   └── screenshots/        ← tangkapan layar app (terang & gelap)
├── backend/                ← Spring Boot (Maven), dibuka di VS Code
└── android/                ← project Android (Gradle), dibuka di Android Studio
```

---

## Prasyarat

| Tools | Versi yang dipakai | Keperluan |
|---|---|---|
| Ubuntu | 26.04.1 LTS | Sistem operasi laptop |
| Git | 2.53.0 | Version control |
| JDK | OpenJDK 21 (`openjdk-21-jdk` 21.0.12) | Build backend + Gradle JDK Android |
| PostgreSQL | 18.6 | Database development |
| Docker Engine | `docker.io` 29.1.3 | Testcontainers saat test backend |
| VS Code | 1.140.0 | Editor backend |
| Android Studio | 2026.2.1 (snap) | Editor Android, Android SDK, `adb` |
| scrcpy (opsional) | 3.3.4 | Menampilkan layar HP di laptop |
| HP Android | Android 8.0 (API 26) ke atas | Menjalankan app (tanpa emulator) |

Maven dan Gradle **tidak perlu diinstal**: project memakai wrapper `./mvnw` dan `./gradlew`.

### Versi library backend

| Library | Versi | Sumber |
|---|---|---|
| Spring Boot (Web MVC, Data JPA, Security, Validation, Flyway, DevTools) | 4.1.1 | start.spring.io |
| Maven (lewat wrapper) | 3.9.16 | start.spring.io |
| Hibernate ORM | 7.4.5.Final | dikelola Spring Boot |
| Flyway | 12.4.0 | dikelola Spring Boot |
| Driver PostgreSQL | 42.7.13 | dikelola Spring Boot |
| Testcontainers | 2.0.5 | dikelola Spring Boot |
| JJWT | 0.13.0 | Maven Central |
| Google API Client | 2.9.1 | Maven Central |
| springdoc-openapi (Swagger UI) | 3.1.1 | Maven Central |
| Datafaker | 2.7.0 | Maven Central |
| jsoup (membaca HTML template upload) | 1.23.2 | Maven Central |
| Lombok | dikelola Spring Boot | – |

### Versi tools & library Android

| Tools / library | Versi |
|---|---|
| Gradle (wrapper) | 9.8.0 |
| Android Gradle Plugin | 9.4.1 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 |
| Java (source/target) | 17 (dibangun dengan JDK 21) |
| AppCompat | 1.8.0 |
| Material Components | 1.14.0 |
| ConstraintLayout | 2.2.2 |
| Activity / Fragment | 1.13.0 / 1.9.1 |
| Navigation | 2.10.2 |
| Lifecycle (ViewModel, LiveData) | 2.11.0 |
| Core / Core SplashScreen | 1.19.1 / 1.2.0 |
| ViewPager2 | 1.1.0 |
| Hilt | 2.60.1 |
| Retrofit / OkHttp / Gson | 3.0.0 / 5.5.0 / 2.14.0 |
| Credentials / Google ID | 1.6.0 / 1.2.1 |
| Browser (Custom Tabs) | 1.10.0 |
| Room (database project di HP) | 2.8.5 |
| androidx.webkit (WebViewAssetLoader untuk Upload) | 1.17.1 |
| JUnit / arch core-testing | 4.13.2 / 2.2.0 |
| Font Geist Sans & Geist Mono | 1.7.2 (SIL OFL 1.1, lisensi di `app/src/main/assets/licenses/geist-OFL.txt`) |
| Ikon | Material Symbols Outlined (Apache 2.0), disalin sebagai vector drawable `ic_*.xml` |

---

## Persiapan lingkungan Ubuntu

Cek dulu apa yang sudah terpasang:

```bash
java -version; psql --version; docker --version; git --version; code --version; adb version
```

### 1. JDK 21

Laptop sudah punya JDK 25, tetapi project ini memakai JDK 21 (LTS) agar sama dengan versi yang didukung Spring Boot dan Android Gradle Plugin.

```bash
sudo apt update                              # memperbarui daftar paket agar versi yang diambil terbaru
sudo apt install -y openjdk-21-jdk           # memasang JDK 21 berdampingan dengan JDK 25 (tidak menghapus yang lama)
sudo update-alternatives --config java       # memilih java default; pilih nomor yang berisi java-21
sudo update-alternatives --config javac      # memilih javac default; pilih nomor yang berisi java-21
```

Tambahkan ke `~/.bashrc` agar `./mvnw` dan `./gradlew` memakai JDK 21:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

### 2. Docker Engine (untuk Testcontainers)

```bash
sudo apt install -y docker.io                # memasang Docker Engine dari repo Ubuntu
sudo usermod -aG docker $USER                # agar perintah docker bisa jalan tanpa sudo; logout-login setelah ini
```

> Catatan keamanan: anggota grup `docker` setara dengan akses root. Ini wajar untuk laptop development pribadi.
> Jika ingin versi Docker terbaru, ikuti panduan resmi https://docs.docker.com/engine/install/ubuntu/.

### 3. Android Studio + Android SDK

```bash
sudo snap install android-studio --classic   # memasang Android Studio versi stabil terbaru dari Snap Store
```

Buka Android Studio → jalankan **Setup Wizard** → pilih **Standard** → tunggu SDK selesai diunduh (ke `~/Android/Sdk`). Tidak perlu membuat emulator.

Tambahkan ke `~/.bashrc`, lalu jalankan `source ~/.bashrc`:

```bash
export ANDROID_HOME=$HOME/Android/Sdk                  # lokasi Android SDK, dibaca Gradle dan tools Android
export PATH=$ANDROID_HOME/platform-tools:$PATH         # adb dari SDK ditaruh di DEPAN agar tidak kalah dengan /usr/bin/adb
```

Cek dengan `which adb`; hasilnya harus `~/Android/Sdk/platform-tools/adb`. Paket udev di langkah 4 ikut memasang `/usr/bin/adb`; jika dua adb berbeda versi dipakai bergantian, server adb saling dimatikan dan HP bisa "hilang" dari Android Studio.

Wizard Standard ikut mengunduh emulator dan system image (±3,6 GB). Project ini tidak memakai emulator, jadi boleh dihapus lewat *SDK Manager* (hapus centang *Android Emulator* dan semua *System Image*).

### 4. Aturan udev (deteksi HP lewat USB) + scrcpy

```bash
sudo apt install -y android-sdk-platform-tools-common  # memasang aturan udev agar HP Android dikenali tanpa sudo
sudo apt install -y scrcpy                             # opsional: menampilkan & mengontrol layar HP dari laptop
```

Setelah memasang aturan udev, cabut lalu colok ulang HP.

---

## Alat kerja

| Alat | Folder | Dipakai untuk |
|---|---|---|
| Terminal (agent AI) | seluruh repo | Menulis kode, build, test, perintah `adb` |
| Android Studio | `android/` | Membaca kode, preview layout, Logcat, debugger, Run ke HP |
| VS Code | `backend/` | Membaca/mengedit kode backend, menjalankan & debug Spring Boot |
| HP Android asli | – | Menjalankan dan mendemokan app (tanpa emulator) |

- **Samakan Gradle JDK**: di Android Studio buka *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*, pilih JDK 21 (`/usr/lib/jvm/java-21-openjdk-amd64`). Terminal memakai `JAVA_HOME` yang sama.
- **Sync Now**: setiap kali `build.gradle.kts`, `settings.gradle.kts`, atau `gradle/libs.versions.toml` berubah, klik **Sync Now** di bar kuning Android Studio.
- Jangan menjalankan `./gradlew` dari terminal bersamaan dengan Build/Run di Android Studio.

---

## Setup database

PostgreSQL sudah terpasang. Buat user dan database khusus project (ganti password dengan milikmu, lalu simpan di `backend/.env`):

```bash
sudo -u postgres psql -c "CREATE USER templateapp WITH PASSWORD 'ganti_password_ini';"   # membuat user database khusus app
sudo -u postgres createdb -O templateapp templateapp                                    # membuat database milik user tersebut
```

---

## Setup OAuth Google & GitHub

Login Google dan GitHub butuh "kartu identitas" app di Google dan GitHub. Langkah ini cukup dilakukan sekali. Hasilnya ditulis di `backend/.env` (jangan di-commit), dan untuk Android nanti juga di `android/local.properties`.

### A. Google (Google Cloud Console)

1. Buka https://console.cloud.google.com → pilih project di kiri atas → **New Project** → nama mis. `templateapp-dev` → **Create**.
2. Menu **Google Auth Platform** (dulu bernama "OAuth consent screen") → **Get started**:
   - *App information*: nama app `Rakit`, user support email = emailmu. (Project yang sudah terlanjur memakai nama lama cukup diganti di *Branding*; Client ID tidak berubah.)
   - *Audience*: **External**.
   - *Contact information*: emailmu → setuju kebijakan → **Create**.
3. **Audience** → bagian *Test users* → **Add users** → tambahkan email Google yang akan dipakai mencoba. Selama status app masih *Testing*, hanya email di daftar ini yang bisa login.
4. **Data Access** → **Add or remove scopes** → centang `openid`, `.../auth/userinfo.email`, `.../auth/userinfo.profile` → **Update** → **Save**.
5. **Clients** → **Create client** → *Application type* **Web application** → nama `templateapp-backend` → **Create**. Salin **Client ID**-nya (akhiran `.apps.googleusercontent.com`).
   - Client ID ini dipakai di **dua** tempat: `GOOGLE_WEB_CLIENT_ID` di `backend/.env`, dan nanti `GOOGLE_WEB_CLIENT_ID` di `android/local.properties` (sebagai `serverClientId`).
   - Client secret dari client Web **tidak dipakai**, karena backend hanya memverifikasi idToken.
6. **Clients** → **Create client** → *Application type* **Android**:
   - Package name: `com.aris.templateapp`
   - SHA-1 certificate fingerprint: SHA-1 dari **debug keystore** laptopmu (cara mengambilnya ada di bawah).
   - **Create**. Client Android ini tidak perlu disalin ke mana pun. Google memakainya untuk memastikan permintaan login datang dari app bertanda tangan sah.

Cara mengambil SHA-1 debug:

```bash
# Setelah project Android ada (Fase 05):
cd android && ./gradlew signingReport          # cari baris "SHA1:" pada Variant: debug

# Atau langsung dari keystore debug (dibuat otomatis oleh Android Studio saat build pertama):
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

> **SHA-1 debug vs rilis.** Setiap laptop punya debug keystore sendiri, jadi SHA-1-nya berbeda-beda. APK rilis ditandatangani dengan keystore rilis yang SHA-1-nya juga berbeda. Setiap SHA-1 yang dipakai (debug di tiap laptop, rilis, dan Play App Signing kalau nanti upload ke Play Store) perlu dibuatkan **client Android sendiri** dengan package yang sama. Kalau SHA-1 belum didaftarkan, login Google di HP gagal dengan error `DEVELOPER_ERROR` / "No credentials available".

### B. GitHub (OAuth App development)

1. Buka https://github.com/settings/developers → **OAuth Apps** → **New OAuth App**.
2. Isi:
   - Application name: `Rakit (dev)` (boleh diganti kapan saja di pengaturan OAuth App; Client ID & secret tidak berubah)
   - Homepage URL: `http://localhost:8080`
   - Authorization callback URL: `http://localhost:8080/api/auth/github/callback`
3. **Register application** → salin **Client ID** → klik **Generate a new client secret** → salin secret-nya. Secret hanya ditampilkan sekali.
4. Tulis keduanya di `backend/.env`:

   ```
   GITHUB_CLIENT_ID=Ov23li...
   GITHUB_CLIENT_SECRET=...
   ```

Satu OAuth App GitHub hanya bisa punya satu callback URL, jadi nanti untuk production buat **OAuth App terpisah** dengan callback domain server.

Callback memakai `localhost` dan tetap bisa dijangkau dari browser HP, karena `adb reverse tcp:8080 tcp:8080` meneruskan `localhost:8080` di HP ke laptop.

### C. Isi `backend/.env` lalu restart backend

```
GOOGLE_WEB_CLIENT_ID=1234567890-abc.apps.googleusercontent.com
GITHUB_CLIENT_ID=Ov23li...
GITHUB_CLIENT_SECRET=...
```

Mencoba GitHub tanpa HP: jalankan backend, panggil `POST /api/auth/github/authorize-url` di Swagger, lalu buka `url` hasilnya di browser laptop. Setelah login GitHub, browser diarahkan ke `templateapp://auth/callback?ticket=...`. Browser laptop tidak bisa membuka alamat itu, tapi nilai `ticket` terlihat di address bar. Tukar tiketnya lewat `POST /api/auth/github/exchange` dalam waktu 2 menit.

## Menjalankan backend

### 1. Isi environment variable

```bash
cd backend
cp .env.example .env          # lalu isi DB_USERNAME, DB_PASSWORD, dan JWT_SECRET
openssl rand -base64 48       # contoh cara membuat JWT_SECRET acak (minimal 32 karakter)
```

Tulis nilai di `.env` **tanpa tanda kutip**. File ini sudah masuk `.gitignore`, jangan pernah di-commit.

Cara `.env` dibaca:
- **Profile `dev`** otomatis membaca `backend/.env` (lewat `spring.config.import` di `application-dev.yml`), jadi tidak perlu `source` apa pun. Syaratnya perintah dijalankan dari folder `backend/`.
- **VS Code** (launch configuration) membaca file yang sama lewat `"envFile"` di `.vscode/launch.json`.
- Jika ingin memuat manual ke terminal: `set -a; source .env; set +a`.

### 2. Jalankan

Dari terminal (di folder `backend/`):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # start backend dengan profile dev di port 8080
```

Dari VS Code (buka **folder `backend/`**, bukan root repo):
- Task: *Terminal → Run Task… → `backend: jalankan (dev)`*
- Debug: tab *Run and Debug* → pilih **Backend: debug (dev)** → tekan F5 (bisa memasang breakpoint).

Saat start, Flyway otomatis menjalankan migrasi `V1`–`V6` di `src/main/resources/db/migration/`. Di log akan muncul `Successfully applied 6 migrations` (pertama kali) atau `Schema "public" is up to date`.

### 3. Swagger UI

Buka http://localhost:8080/swagger-ui.html untuk melihat dan mencoba endpoint. Swagger UI dimatikan di profile `prod`.

Contoh mencoba alur auth email:
1. **Auth → `POST /api/auth/register`** → *Try it out* → isi body, mis. `{"displayName":"Aris","email":"aris@mail.com","password":"rahasia123"}` → *Execute*. Hasilnya `201` berisi `accessToken` dan `refreshToken`.
2. Salin nilai `accessToken`, klik tombol **Authorize** (ikon gembok, kanan atas), tempel token (tanpa kata `Bearer`), lalu *Authorize*.
3. **User → `GET /api/users/me`** → *Execute* → `200` berisi data user.
4. **`POST /api/auth/refresh`** dengan `{"refreshToken":"..."}` → dapat pasangan token baru. Coba kirim refresh token **lama** sekali lagi → `401 REFRESH_TOKEN_INVALID`, dan token yang baru ikut dicabut (deteksi pemakaian ulang).
5. **`POST /api/auth/logout`** dengan refresh token → `204`.

Access token berlaku 15 menit (`JWT_ACCESS_TTL_MINUTES`). Untuk mencoba token kedaluwarsa, ubah sementara nilainya di `.env` menjadi `1`.

### 4. Test

```bash
./mvnw test     # Docker harus aktif: Testcontainers menyalakan PostgreSQL sementara khusus untuk test
```

Test pertama kali agak lama karena Docker mengunduh image `postgres:18`.

**ZIP uji pengecekan upload.** Setiap aturan pengecekan punya `gagal.zip` dan `lolos.zip` di `src/test/resources/test-fixtures/<KODE>/`. Jika aturan ditambah/diubah, perbarui `tools/buat-zip-uji.py` lalu buat ulang ZIP-nya (butuh Python 3):

```bash
python3 tools/buat-zip-uji.py
./mvnw test -Dtest=CheckRuleFixturesTest   # cepat, tanpa Docker
```

**Folder upload.** ZIP yang diupload provider, salinan bernomor (`work.zip`), thumbnail, dan paket template tayang (`package.zip`) disimpan di `backend/uploads/` (sudah di `.gitignore`). Ganti lokasinya dengan `UPLOAD_STORAGE_DIR` di `.env` jika perlu. Batas ukuran, daftar CDN, dan library yang diizinkan ada di `application.yml` bagian `app.upload`.

### Profile

| Profile | Dipakai untuk | Isi khusus |
|---|---|---|
| `dev` | Laptop development | Membaca `.env`, log `DEBUG` untuk `com.aris.templateapp` |
| `prod` | Server production | Swagger UI & `/v3/api-docs` dimatikan |
| `test` | `./mvnw test` | JWT secret khusus test, database dari Testcontainers |

## Menyiapkan HP

1. Buka *Pengaturan → Tentang ponsel* → ketuk *Nomor build* 7 kali sampai muncul "Anda sekarang developer".
2. Buka *Opsi pengembang* (biasanya di *Pengaturan → Sistem*) → aktifkan **USB debugging**.
3. Colok HP ke laptop dengan kabel USB **data** (bukan kabel charge saja).
4. Di HP muncul pop-up "Allow USB debugging?" → centang *Always allow from this computer* → **Allow**.
5. Cek di terminal:

   ```bash
   adb devices
   ```

   Hasil yang benar: satu baris berisi serial HP dengan status `device`. Jika `unauthorized`, buka kunci HP dan setujui pop-up.

**Alternatif tanpa kabel (Wireless debugging, Android 11+):** di *Opsi pengembang* aktifkan *Wireless debugging* → *Pair device with pairing code*, lalu:

```bash
adb pair <ip>:<port-pairing>     # masukkan kode pairing yang tampil di HP
adb connect <ip>:<port>          # port yang tampil di layar utama Wireless debugging
```

## Menjalankan app Android di HP

### 1. Isi `android/local.properties`

File ini dibuat otomatis oleh Android Studio (berisi `sdk.dir`) dan **tidak di-commit**. Tambahkan Client ID Web Google (lihat "Setup OAuth"):

```properties
sdk.dir=/home/<user>/Android/Sdk
GOOGLE_WEB_CLIENT_ID=1234567890-abc.apps.googleusercontent.com
```

Nilai ini dibaca `app/build.gradle.kts` menjadi `BuildConfig.GOOGLE_WEB_CLIENT_ID`. Setelah mengubahnya, klik **Sync Now** (atau build ulang dari terminal).

### 2. Sambungkan HP ke backend

```bash
adb devices                       # pastikan HP berstatus "device"
adb reverse tcp:8080 tcp:8080     # localhost:8080 di HP diteruskan ke backend di laptop
```

> **Wajib diulang** setiap kali HP dicabut-colok, HP restart, atau `adb` di-restart. Tanpa ini app menampilkan "Tidak ada koneksi".
>
> Agar otomatis, jalankan `./tools/keep-adb-reverse.sh` dari folder `android/` (Windows: `tools\keep-adb-reverse.ps1`) dan biarkan berjalan. Skrip ini memasang ulang `adb reverse` setiap 3 detik ke semua sambungan HP. Ini sangat membantu saat memakai wireless debugging yang sering putus-sambung.

Build debug memanggil `http://localhost:8080/api/` (`BuildConfig.API_BASE_URL`). HTTP tanpa HTTPS hanya diizinkan untuk `localhost` di build debug (`src/debug/res/xml/network_security_config.xml`).

### 3. Jalankan app

Dari Android Studio: buka folder `android/`, pilih HP di daftar perangkat (atas), lalu klik **Run ▶**.

Dari terminal (di folder `android/`):

```bash
./gradlew installDebug                                           # build lalu pasang ke HP
adb shell am start -n com.aris.templateapp/.MainActivity         # buka app
adb logcat --pid=$(adb shell pidof -s com.aris.templateapp)      # tampilkan log app saja
```

Saat pertama kali dibuka, app menampilkan intro 3 halaman, lalu Dashboard Pembuat Website sebagai tamu. Untuk melihat intro lagi, hapus app lalu pasang ulang (`adb uninstall com.aris.templateapp` lalu `./gradlew installDebug`). Beberapa HP, termasuk realme, menolak `adb shell pm clear`.

Animasi Lottie (`app/src/main/res/raw/*.json`) dibuat oleh skrip `android/tools/generate_lottie.py`. Kalau ingin mengubah animasi, ubah skripnya lalu jalankan `python3 tools/generate_lottie.py` dari folder `android/`.

Build debug memasang **dua ikon** di HP: **Rakit** (app) dan **Katalog komponen** (semua komponen & token desain; tombol "Ganti terang / gelap" untuk memeriksa mode gelap). Katalog tidak ada di build rilis.

### 4. Log & layar

- **Logcat** di Android Studio: tab *Logcat* di bawah → filter `package:com.aris.templateapp`.
- **scrcpy** (opsional) untuk menampilkan & mengontrol layar HP di laptop: `scrcpy`.
- Screenshot: `adb exec-out screencap -p > nama-file.png`.

### 5. Test & pemeriksaan Android

```bash
./gradlew assembleDebug        # build APK debug
./gradlew testDebugUnitTest    # unit test (JVM laptop, tanpa HP)
./gradlew lint                 # pemeriksaan kode & resource; laporan di app/build/reports/lint-results-debug.html
```

Test query Room (tab Project) berjalan **di HP**, karena butuh SQLite Android sungguhan:

```bash
./gradlew installDebug installDebugAndroidTest
adb shell am instrument -w -e class com.aris.templateapp.data.local.ProjectDaoTest \
    com.aris.templateapp.test/androidx.test.runner.AndroidJUnitRunner
adb uninstall com.aris.templateapp.test     # hapus lagi APK test-nya
```

`./gradlew connectedDebugAndroidTest` juga bisa dipakai, tetapi perintah itu menghapus app setelah selesai, sehingga data login dan project di HP ikut hilang.

**Tema terang/gelap:** app mengikuti mode HP secara bawaan. Untuk memaksa salah satunya, buka *Profil → Pengaturan → Tampilan → Tema*.

**Project contoh (build debug):** *Profil → Pengaturan → DEBUG → Isi project contoh* mengisi 7 project dengan status dan mode yang beragam. *Hapus project contoh* hanya menghapus project contoh tersebut. Menu ini tidak ada di build rilis.

Jangan menjalankan `./gradlew` dari terminal bersamaan dengan Build/Run di Android Studio.

## Akun dummy

Saat backend start dengan profile `dev` dan tabel `users` masih **kosong**, `DummyDataSeeder` membuat 10 akun berikut. Semuanya memakai password yang sama: **`password123`**.

| Email | Kondisi | Cocok untuk mencoba |
|---|---|---|
| `dummy1@templateapp.test`, `dummy2@templateapp.test` | Belum onboarding | Alur pilih peran → form |
| `dummy3@templateapp.test` … `dummy7@templateapp.test` | Pembuat website, onboarding selesai | Dashboard pembuat website, "Jadi penyedia template" |
| `dummy8@templateapp.test`, `dummy9@templateapp.test` | Pembuat website + provider **active** (belum punya template) | Dashboard Provider kosong + checklist provider baru, beralih mode |
| `dummy10@templateapp.test` | Pembuat website + provider **suspended** | Mode provider terkunci → diarahkan ke mode pembuat website |

> Database yang sudah diisi **sebelum Fase 11** memakai data seeder lama: migrasi V7 mengubah status lama `pending`/`approved`/`rejected` menjadi `active`, sehingga `dummy10` ikut aktif. Jadikan `suspended` lagi dengan SQL di bagian "Contoh SQL status provider".

Nama tampilannya nama Indonesia acak dengan seed tetap, jadi hasilnya selalu sama setiap kali database diisi ulang.

Untuk mengisi ulang dari awal (hanya database development):

```bash
sudo -u postgres psql -d templateapp -c "TRUNCATE users CASCADE;"   # kosongkan semua user (data terkait ikut terhapus)
# lalu jalankan ulang backend dengan profile dev
```

## Data demo provider (opsional)

Selama fitur Upload belum ada, semua akun provider tidak punya template, jadi Dashboard Provider tampil kosong. Untuk melihat grafik, template populer, dan daftar berisi data, nyalakan **seeder demo**:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--app.seed.demo-templates=true
```

Seeder membuat akun **`demo-provider@templateapp.test`** / **`password123`** berisi:
- 7 template berbagai status (tayang, tayang dengan peringatan, tidak lolos, sedang dicek, draft, dinonaktifkan)
- hasil pengecekan (error & peringatan) dan notifikasi
- ZIP situs contoh sungguhan untuk 3 template yang masih di wizard Upload, jadi tab **Upload** bisa langsung dicoba: "Landing Event" (tidak lolos, bisa diupload ulang), "Organisasi Pemuda" (dicek otomatis saat backend start, lalu menjadi draft), "Instansi Desa" (draft langkah 3)
- event dilihat/didownload tersebar di 30 hari terakhir

Seeder ini hanya ada di profile `dev`, mati secara bawaan, dan tidak dibuat ulang kalau akun demo sudah ada. Akun provider lain tetap kosong.

Untuk menghapusnya lagi (supaya kembali kosong), panggil `DELETE /api/dev/demo-templates` di Swagger UI (grup **Dev**) atau lewat terminal:

```bash
curl -X DELETE http://localhost:8080/api/dev/demo-templates
```

Perintah ini juga menghapus file ZIP demo di `backend/uploads/`. Restart backend (dengan flag di atas) untuk membuat data demo yang baru.

## Contoh SQL status provider

Provider langsung **aktif** setelah mengisi form (tidak ada verifikasi). Status hanya `active` dan `suspended`, dan karena panel admin belum ada, perubahan status dilakukan manual. Masuk dulu ke database:

```bash
psql -h localhost -U templateapp -d templateapp
```

```sql
-- Lihat semua provider beserta statusnya
SELECT u.email, p.creator_name, p.status
FROM provider_profiles p JOIN users u ON u.id = p.user_id;

-- Tangguhkan (rem darurat: mode provider terkunci, user diarahkan ke mode pembuat website)
UPDATE provider_profiles SET status = 'suspended', updated_at = now()
WHERE user_id = (SELECT id FROM users WHERE email = 'dummy9@templateapp.test');

-- Aktifkan kembali
UPDATE provider_profiles SET status = 'active', updated_at = now()
WHERE user_id = (SELECT id FROM users WHERE email = 'dummy9@templateapp.test');
```

Nilai status selain `active` dan `suspended` ditolak oleh database (CHECK constraint).

### Laporan "Ini keliru?" dari provider

Belum ada panel admin, jadi laporan pengecekan keliru dibaca langsung dari database:

```sql
-- Aturan yang paling sering dilaporkan
SELECT rule_code, count(*) FROM check_reports WHERE status = 'baru' GROUP BY rule_code ORDER BY 2 DESC;

-- Detail laporan
SELECT created_at, rule_code, rule_version, location, snippet, reason FROM check_reports ORDER BY created_at DESC;

-- Tandai sudah dibaca / aturan sudah diperbaiki / tidak berubah
UPDATE check_reports SET status = 'dibaca' WHERE id = '<id>';
```

## Troubleshooting

| Masalah | Solusi |
|---|---|
| Compile Android gagal: `UnsatisfiedLinkError … libc.musl-x86_64.so.1` atau `osinfo: Linux-Musl` | Library SQLite yang dipakai Room saat compile salah menebak jenis Linux, karena path folder home mengandung kata "musl" (mis. `/home/...muslimin`). Buat jar shim berisi library Linux biasa, lalu daftarkan di `~/.gradle/gradle.properties` (bukan di repo): lihat langkah di bawah tabel ini. |
| `adb devices` kosong atau `no permissions` | Pastikan kabel USB data, pasang `android-sdk-platform-tools-common` (aturan udev), cabut-colok ulang HP. |
| Status `unauthorized` | Buka kunci HP, setujui pop-up "Allow USB debugging". Jika tidak muncul: *Opsi pengembang → Revoke USB debugging authorizations*, lalu colok ulang. |
| `java -version` masih 25 | Jalankan `sudo update-alternatives --config java` dan pilih java-21; cek `JAVA_HOME`. |
| `docker: permission denied` | Logout lalu login lagi setelah `usermod -aG docker $USER`. |
| `adb: more than one device/emulator` padahal hanya satu HP | HP yang sama tersambung dua kali (lewat `adb connect IP:port` dan lewat mDNS). Jalankan `adb disconnect IP:port`. |
| Seeder tidak membuat akun dummy (log: `Seeder dilewati`) | Tabel `users` sudah berisi data. Seeder sengaja hanya berjalan di database kosong; lihat bagian "Akun dummy" untuk mengosongkannya. |
| `Port 8080 was already in use` | Ada backend lain yang masih jalan. Cari dengan `ss -ltnp \| grep 8080`, lalu hentikan prosesnya (atau tutup terminal/VS Code yang menjalankannya). |
| `Validate failed: Migrations have failed validation` / checksum mismatch | File migrasi yang **sudah pernah dijalankan** diubah. Jangan ubah file `V*` lama; buat file `V7__...` baru. Khusus database development, bisa reset: `sudo -u postgres psql -c "DROP DATABASE templateapp;"` lalu `sudo -u postgres createdb -O templateapp templateapp`. |
| Test gagal: `Could not find a valid Docker environment` | Docker belum jalan: `sudo systemctl start docker`, cek dengan `docker ps`. |
| App gagal start: `JWT_SECRET minimal 32 karakter` / `Could not resolve placeholder 'DB_URL'` | `.env` belum diisi atau perintah tidak dijalankan dari folder `backend/`. |
| Login Google gagal: `401 SOCIAL_AUTH_FAILED` dari backend | `GOOGLE_WEB_CLIENT_ID` di `.env` kosong atau berbeda dengan `serverClientId` di app. Keduanya harus Client ID **Web** (bukan Android). Lihat log backend: `GOOGLE_WEB_CLIENT_ID belum diisi` atau `Verifikasi idToken Google gagal`. |
| Login Google gagal di HP: `DEVELOPER_ERROR` / "No credentials available" | SHA-1 debug laptop ini belum didaftarkan sebagai client **Android** (package `com.aris.templateapp`), atau Client ID Web/Android tertukar. Pastikan juga email Google-nya ada di *Test users*. |
| `POST /auth/github/authorize-url` membalas `500` | `GITHUB_CLIENT_ID` belum diisi di `.env` (lihat log: `GITHUB_CLIENT_ID belum diisi`). |
| Callback GitHub tidak kembali ke app / halaman "redirect_uri is not associated" | Callback URL di OAuth App GitHub harus persis `http://localhost:8080/api/auth/github/callback` (sama dengan `GITHUB_REDIRECT_URI`); jalankan ulang `adb reverse tcp:8080 tcp:8080`; deep link `templateapp://auth/callback` dibuat di Fase 08. |
| Deep link berisi `?error=TICKET_INVALID` | Halaman login GitHub dibiarkan terbuka lebih dari 10 menit (state kedaluwarsa) atau callback dibuka dua kali. Mulai lagi dari tombol login. |
| Android Studio penuh garis merah / "Gradle files have changed" setelah file build berubah | Klik **Sync Now** di bar kuning atas (atau *File → Sync Project with Gradle Files*). |
| Gradle JDK berbeda antara terminal dan Android Studio (error "Unsupported class file major version" / build berbeda hasil) | Android Studio: *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK* pilih JDK 21; terminal: `echo $JAVA_HOME` harus `/usr/lib/jvm/java-21-openjdk-amd64`. `gradle/gradle-daemon-jvm.properties` meminta JDK 21. |
| App di HP: "Tidak ada koneksi" padahal backend jalan (dan internet HP lancar) | Pesan ini berarti app tidak menjangkau backend di laptop. Jalankan `./tools/keep-adb-reverse.sh`, atau ulangi `adb reverse tcp:8080 tcp:8080` (jika HP tersambung lewat USB dan Wi-Fi sekaligus, pakai `adb -s <serial> reverse ...`); cek backend di laptop `curl localhost:8080/v3/api-docs`; pastikan memakai build **debug** (rilis menolak HTTP). |
| `adb: device unauthorized` / HP tidak muncul | Lihat baris troubleshooting HP di atas; buka kunci HP lalu setujui pop-up. |
| `adb server version (...) doesn't match this client` | Dua `adb` berbeda versi berebut server; pakai yang dari SDK (`which adb`) lalu `adb kill-server && adb devices`. |
| Laptop lambat saat build | Tutup aplikasi berat lain; memori Gradle sudah dibatasi 2 GB di `android/gradle.properties` (`org.gradle.jvmargs=-Xmx2g`); jangan build dari terminal dan Android Studio bersamaan. |
| HP terdeteksi di `lsusb` tetapi tidak muncul di `adb devices` (log adb: `write terminated: Connection timed out`) | Jalankan adb dengan backend USB bawaan Linux: `adb kill-server && ADB_LIBUSB=0 adb start-server`. Agar permanen: `echo 'export ADB_LIBUSB=0' >> ~/.bashrc`. Pastikan juga mode USB HP **Transfer file**, bukan *Tethering USB* (cek juga *Opsi pengembang → Konfigurasi USB default*). |
| HP putus-sambung terus; `journalctl -k \| grep usb` berisi `error -71` | Masalah fisik, bukan setting: ganti kabel USB data, pindah port laptop (jangan lewat hub), bersihkan lubang USB HP. |
| HP tiba-tiba hilang dari Android Studio / `adb server version doesn't match` | Ada dua `adb` berbeda versi. Pastikan `which adb` menunjuk ke `~/Android/Sdk/platform-tools/adb`, lalu `adb kill-server && adb devices`. |

_Daftar ini dilengkapi di fase berikutnya._

### Langkah shim sqlite untuk path yang mengandung "musl"

Hanya perlu jika compile gagal dengan pesan di tabel atas. File dibuat di luar repo, jadi laptop lain tidak terpengaruh.

```bash
mkdir -p ~/.gradle/sqlite-native/shim/org/sqlite/native/Linux-Musl/x86_64
cd ~/.gradle/sqlite-native/shim
JAR=$(find ~/.gradle/caches -name "sqlite-jdbc-*.jar" | head -1)        # ikut terunduh bersama room-compiler
unzip -o -j "$JAR" org/sqlite/native/Linux/x86_64/libsqlitejdbc.so -d org/sqlite/native/Linux-Musl/x86_64
jar cf ../sqlite-musl-shim.jar org                                       # library Linux biasa di folder "musl"
echo "rakit.sqliteShimJar=$HOME/.gradle/sqlite-native/sqlite-musl-shim.jar" >> ~/.gradle/gradle.properties
cd - && ./gradlew --stop                                                # Gradle daemon membaca ulang pengaturan
```

`android/app/build.gradle.kts` menambahkan jar itu ke annotation processor hanya jika properti `rakit.sqliteShimJar` ada.

## Catatan belajar & dokumentasi

- [`docs/panduan-kolaborator.md`](docs/panduan-kolaborator.md): menyiapkan project dari nol di Windows & Linux untuk anggota tim baru.
- [`docs/dokumentasi-project.md`](docs/dokumentasi-project.md): penjelasan arsitektur, alur, data, dan endpoint project secara menyeluruh (bahan belajar utama).
- [`docs/catatan-belajar/`](docs/catatan-belajar/): catatan per fase (apa yang dikerjakan, alasan, latihan).
