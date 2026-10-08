# SETUP-WINDOWS: menyiapkan project Rakit di Windows (untuk agent AI)

> **Untuk agent AI (Claude Code, Codex, dll.) yang membantu anggota tim Aris di laptop Windows.**
> Jalankan bagian 1–12 berurutan. Setiap langkah punya **perintah** dan **cek hasil**; lanjut hanya jika cek lulus.
> Semua perintah ditulis untuk **PowerShell** (bukan Command Prompt/cmd).
> Versi manusia yang lebih panjang (Windows + Linux, dengan gambar langkah GUI): [`docs/panduan-kolaborator.md`](docs/panduan-kolaborator.md).

---

## 0. Aturan untuk agent

1. **Tanya dulu sebelum memasang software sistem** (winget, installer). Tunjukkan perintahnya, jelaskan fungsinya dalam satu kalimat, tunggu user setuju.
2. **Langkah yang wajib dikerjakan manusia** (agent tidak bisa/boleh): mengisi password di installer, Setup Wizard Android Studio, login GitHub di browser, menyetujui pop-up "Izinkan USB debugging" di HP, meminta nilai rahasia ke Aris. Berhenti, jelaskan langkahnya, tunggu user selesai, lalu cek.
3. **Jangan pernah commit atau mengirim rahasia** ke mana pun: `backend/.env`, `android/local.properties`, `*.jks`, `*.keystore`. Jangan menampilkan isi `JWT_SECRET`/`GITHUB_CLIENT_SECRET` di chat.
4. **Jangan menjalankan `gradlew` saat user sedang Build/Run di Android Studio.**
5. **Port 8080 bisa sedang dipakai backend milik user.** Cek dulu (`Get-NetTCPConnection -LocalPort 8080`); jangan mematikan proses yang tidak kamu jalankan sendiri.
6. **Jangan pakai `./mvnw` atau `./gradlew`** di Windows. Pakai `.\mvnw.cmd` dan `.\gradlew.bat`.
7. Setelah memasang software yang mengubah PATH, **muat ulang PATH** di sesi PowerShell yang sama (lihat 2.3), atau minta user membuka PowerShell baru.
8. Aturan kerja project ada di `AGENTS.md` (baca sampai habis sebelum mengubah kode).

---

## 1. Gambaran project

| Folder | Isi | Dibuka dengan |
|---|---|---|
| `backend/` | Spring Boot 4.1 (Java 21, Maven wrapper), PostgreSQL, Flyway | VS Code / terminal |
| `android/` | App Android Java + XML Views (Gradle wrapper, Kotlin DSL), minSdk 26, compileSdk 37 | Android Studio / terminal |
| `docs/` | Rancangan, dokumentasi, catatan belajar per fase | — |

App di HP memanggil backend di `http://localhost:8080/api/`; jembatannya `adb reverse tcp:8080 tcp:8080` (tidak perlu IP Wi-Fi atau membuka firewall).

**Target akhir setup:** backend jalan di `localhost:8080`, semua test backend & Android lulus, app terpasang di HP asli dan bisa login dengan akun dummy.

---

## 2. Cek & pasang software

### 2.1 Cek yang sudah ada (jalankan dulu, jangan langsung memasang)

```powershell
# Satu blok: tampilkan versi atau "TIDAK ADA" untuk setiap alat
foreach ($cmd in "git","java","psql","docker","code","adb","sdkmanager","winget") {
    $found = Get-Command $cmd -ErrorAction SilentlyContinue
    if ($found) { "{0,-11} ADA   {1}" -f $cmd, $found.Source } else { "{0,-11} TIDAK ADA" -f $cmd }
}
java -version 2>&1 | Select-Object -First 1      # harus "21.x"
"JAVA_HOME    = $env:JAVA_HOME"
"ANDROID_HOME = $env:ANDROID_HOME"
Test-Path "C:\Program Files\PostgreSQL\18\bin\psql.exe"   # psql sering tidak ada di PATH walau PostgreSQL terpasang
```

### 2.2 Daftar yang dibutuhkan

| Software | Versi | Wajib untuk | Perintah winget (minta izin user dulu) |
|---|---|---|---|
| Git | terbaru | semua | `winget install --id Git.Git -e` |
| JDK Temurin | **21** (bukan 17/25) | semua | `winget install --id EclipseAdoptium.Temurin.21.JDK -e` |
| PostgreSQL | 18 | backend | `winget search PostgreSQL` → pasang ID versi 18 (installer meminta **password superuser `postgres`**: minta user mencatatnya) |
| Docker Desktop | terbaru | test backend (Testcontainers) | `wsl --install` (PowerShell **Administrator**, lalu restart), kemudian `winget install --id Docker.DockerDesktop -e` |
| VS Code | terbaru | opsional (editor backend) | `winget install --id Microsoft.VisualStudioCode -e` |
| Android SDK | platform `android-37.0`, build-tools `36.0.0`, platform-tools | app Android & `adb` | lewat Android Studio (2.4) **atau** command line tools (2.5) |
| Android Studio | terbaru | opsional, disarankan untuk developer Android | `winget install --id Google.AndroidStudio -e` |
| Maven, Gradle | — | **jangan dipasang** | sudah ada `mvnw.cmd` & `gradlew.bat` |
| Python 3 + Pillow | — | **tidak perlu** untuk menjalankan | hanya untuk membuat ulang ZIP uji / gambar demo |

### 2.3 Environment variable (setelah JDK & SDK terpasang)

```powershell
# JAVA_HOME (installer Temurin biasanya sudah mengisinya; isi manual hanya jika kosong)
$jdk = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-21*" | Select-Object -First 1
if (-not $env:JAVA_HOME -and $jdk) { [Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk.FullName, "User") }

# ANDROID_HOME + PATH untuk adb & sdkmanager
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
[Environment]::SetEnvironmentVariable("ANDROID_HOME", $sdk, "User")
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
foreach ($p in "$sdk\platform-tools", "$sdk\cmdline-tools\latest\bin", "C:\Program Files\PostgreSQL\18\bin") {
    if ($userPath -notlike "*$p*") { $userPath = "$userPath;$p" }
}
[Environment]::SetEnvironmentVariable("Path", $userPath, "User")

# Muat ulang ke sesi ini (tanpa membuka PowerShell baru)
$env:JAVA_HOME = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
$env:ANDROID_HOME = $sdk
$env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [Environment]::GetEnvironmentVariable("Path", "User")
```

**Cek:** `java -version` → 21, `adb version` jalan (setelah SDK terpasang), `psql --version` jalan.

### 2.4 Android SDK lewat Android Studio (pilihan A, disarankan)

Langkah manusia: buka Android Studio → **Setup Wizard** → **Standard** → terima lisensi → tunggu unduhan selesai. SDK berada di `%LOCALAPPDATA%\Android\Sdk`. Lalu jalankan 2.3.

### 2.5 Android SDK tanpa Android Studio (pilihan B)

1. Minta user mengunduh **Command line tools only (Windows)** dari https://developer.android.com/studio (bagian bawah halaman), atau unduh sendiri jika diizinkan.
2. Ekstrak sehingga ada file `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat` (struktur `cmdline-tools\latest\bin` **wajib** persis begitu).
3. Jalankan 2.3, lalu:

```powershell
sdkmanager --licenses                                                   # user menjawab "y" untuk setiap lisensi
sdkmanager "platform-tools" "platforms;android-37.0" "build-tools;36.0.0"
sdkmanager --list_installed                                             # cek: ketiga paket ada
```

### 2.6 Driver USB HP (khusus Windows)

- Google Pixel: `sdkmanager "extras;google;usb_driver"` lalu pasang drivernya lewat Device Manager.
- Merek lain (Samsung, Xiaomi, realme, OPPO, vivo): driver resmi dari situs merek (daftar: https://developer.android.com/studio/run/oem-usb).
- Alternatif tanpa driver: **wireless debugging** (bagian 10.2).

---

## 3. Ambil kode

> Simpan di folder **pendek tanpa spasi**. Path panjang/berspasi sering membuat build Android gagal di Windows.

```powershell
git config --global core.longpaths true          # folder build Android bisa sangat dalam
mkdir C:\dev -Force | Out-Null
cd C:\dev
git clone https://github.com/A-blib/Rakit-Apps.git   # repo privat: user harus sudah diundang Aris; login lewat browser saat diminta
cd C:\dev\Rakit-Apps
git branch -a                                    # lihat branch yang tersedia
```

**Branch:** kerja tim di `develop`; setiap anggota punya branch sendiri (`dimas`, `wahana`, `farid`, `aris`). Fitur terbaru yang belum di-merge ada di `fitur/template-mode` (PR #2 ke `develop`). Tanyakan ke user branch mana yang dipakai, lalu `git checkout <branch>`.

**Cek:** `Get-ChildItem` menampilkan `android`, `backend`, `docs`, `AGENTS.md`, `README.md`, `SETUP-WINDOWS.md`.

`.gitattributes` menjaga `mvnw`/`gradlew` tetap LF dan `.cmd`/`.bat`/`.ps1` tetap CRLF. Jangan diubah.

---

## 4. Database PostgreSQL

Setiap laptop punya database sendiri: nama `templateapp`, user `templateapp`. Minta user memilih password untuk user `templateapp` (dan memberi password superuser `postgres` dari saat instalasi).

```powershell
$pg = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
$env:PGPASSWORD = Read-Host "Password superuser postgres"          # diketik user, tidak tampil di chat
$dbPass = Read-Host "Password baru untuk user templateapp"
& $pg -U postgres -h localhost -c "CREATE USER templateapp WITH PASSWORD '$dbPass';"
& $pg -U postgres -h localhost -c "CREATE DATABASE templateapp OWNER templateapp;"
Remove-Item Env:\PGPASSWORD
```

**Cek:**
```powershell
$env:PGPASSWORD = $dbPass; & $pg -U templateapp -h localhost -d templateapp -c "select 1"; Remove-Item Env:\PGPASSWORD
```
Jika `Connection refused`: *Services* → `postgresql-x64-18` → Start.

---

## 5. File `backend/.env` (rahasia, tidak di-commit)

Nilai yang **harus diminta ke Aris lewat chat pribadi** (atau dibuat sendiri, lihat 9):
`GOOGLE_WEB_CLIENT_ID`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`.

```powershell
cd C:\dev\Rakit-Apps\backend
# JWT_SECRET: 48 byte acak, Base64 (minimal 32 karakter)
$bytes = New-Object byte[] 48
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$jwt = [Convert]::ToBase64String($bytes)

$lines = @(
  "DB_URL=jdbc:postgresql://localhost:5432/templateapp",
  "DB_USERNAME=templateapp",
  "DB_PASSWORD=$dbPass",
  "JWT_SECRET=$jwt",
  "JWT_ACCESS_TTL_MINUTES=15",
  "JWT_REFRESH_TTL_DAYS=30",
  "GOOGLE_WEB_CLIENT_ID=",
  "GITHUB_CLIENT_ID=",
  "GITHUB_CLIENT_SECRET=",
  "GITHUB_REDIRECT_URI=http://localhost:8080/api/auth/github/callback",
  "APP_DEEP_LINK=templateapp://auth/callback"
)
# UTF-8 TANPA BOM (BOM membuat baris pertama tidak terbaca). Jangan pakai Out-File/Set-Content di PowerShell 5.
[System.IO.File]::WriteAllLines("$PWD\.env", $lines, (New-Object System.Text.UTF8Encoding $false))
```

Lalu minta user mengisi tiga nilai OAuth di `backend\.env` (tanpa tanda kutip, tanpa spasi di sekitar `=`). Tanpa nilai OAuth, backend tetap jalan dan login **email** tetap bisa dipakai; hanya login Google/GitHub yang gagal.

**Cek:** `git status` **tidak** menampilkan `.env` (sudah di `.gitignore`).

---

## 6. Jalankan backend

### 6.1 Memuat `.env` ke PowerShell (wajib di Windows)

Backend membaca konfigurasi dari **environment variable**, bukan langsung dari file `.env`. VS Code (launch "Backend: debug (dev)") memuatnya otomatis lewat `envFile`; di terminal, muat dulu:

```powershell
cd C:\dev\Rakit-Apps\backend
Get-Content .env | Where-Object { $_ -match '^\s*[A-Za-z_][A-Za-z0-9_]*=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), "Process")
}
"DB_URL = $env:DB_URL"     # cek: terisi
```

Variabel ini hanya berlaku di jendela PowerShell itu. Ulangi setiap membuka terminal baru.

### 6.2 Jalankan

```powershell
# Cek port dulu (aturan 0.5)
Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue

# Tanda kutip WAJIB di PowerShell karena ada titik di parameter.
# Seeder demo (opsional, disarankan): akun demo-provider + 3 template dengan paket contoh untuk editor.
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--app.seed.demo-templates=true"
```

Jalankan sebagai proses latar/terminal terpisah karena perintah ini terus berjalan. Run pertama mengunduh library (beberapa menit). Tunggu log `Started TemplateAppApplication`.

**Cek (terminal lain):**
```powershell
(Invoke-WebRequest http://localhost:8080/api/templates -UseBasicParsing).StatusCode      # 200
(Invoke-WebRequest http://localhost:8080/swagger-ui.html -UseBasicParsing).StatusCode    # 200
$body = '{"email":"dummy1@templateapp.test","password":"password123"}'
(Invoke-RestMethod http://localhost:8080/api/auth/login -Method Post -ContentType "application/json" -Body $body).user.email
```

Akun uji (password semua `password123`): `dummy1`–`dummy10@templateapp.test`, `demo-provider@templateapp.test` (provider berisi data, jika seeder demo dinyalakan).

### 6.3 Test backend (Docker Desktop harus *Engine running*)

```powershell
docker info --format "{{.ServerVersion}}"     # cek Docker hidup
cd C:\dev\Rakit-Apps\backend
.\mvnw.cmd test
```
**Lulus jika:** `BUILD SUCCESS` dan `Tests run: ..., Failures: 0, Errors: 0`.

---

## 7. Android: konfigurasi & build

### 7.1 `android\local.properties` (rahasia, tidak di-commit)

Android Studio membuat file ini otomatis saat project `android` dibuka. Jika memakai pilihan B atau file belum ada:

```powershell
cd C:\dev\Rakit-Apps\android
$sdkEscaped = ("$env:LOCALAPPDATA\Android\Sdk" -replace '\\', '\\') -replace ':', '\:'
$props = @("sdk.dir=$sdkEscaped", "GOOGLE_WEB_CLIENT_ID=<Client ID Web dari Aris>")
if (-not (Test-Path local.properties)) {
    [System.IO.File]::WriteAllLines("$PWD\local.properties", $props, (New-Object System.Text.UTF8Encoding $false))
}
Get-Content local.properties
```
Isi `GOOGLE_WEB_CLIENT_ID` sama dengan yang di `backend\.env`. Tanpa nilai ini app tetap bisa dibuild; hanya login Google yang gagal.

Jika memakai Android Studio: *File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK* = **JDK 21**, lalu **Sync Now**.

### 7.2 Build & cek kualitas

```powershell
cd C:\dev\Rakit-Apps\android
.\gradlew.bat assembleDebug          # APK: app\build\outputs\apk\debug\app-debug.apk
.\gradlew.bat testDebugUnitTest      # unit test
.\gradlew.bat lint                   # harus 0 error
```
Build pertama mengunduh Gradle & library (bisa 10+ menit). `android\gradle.properties` sudah membatasi memori Gradle (`-Xmx2g`).

**Lulus jika:** ketiganya `BUILD SUCCESSFUL`.

---

## 8. HP asli

### 8.1 Langkah manusia di HP

*Pengaturan → Tentang ponsel* → ketuk **Nomor build** 7 kali → *Opsi pengembang* → aktifkan **USB debugging** → colok kabel **data** → pilih mode **Transfer file** → setujui pop-up **Izinkan USB debugging** (centang *Selalu izinkan*).

### 8.2 Sambungkan, pasang, jalankan

```powershell
adb devices                         # harus ada 1 baris berstatus "device" (bukan "unauthorized")
adb reverse tcp:8080 tcp:8080       # WAJIB diulang setiap HP dicabut-colok / adb restart
cd C:\dev\Rakit-Apps\android
.\gradlew.bat installDebug
adb shell am start -n com.aris.templateapp/.MainActivity
```

Agar `adb reverse` tidak perlu diulang manual, jalankan penjaga di terminal terpisah (biarkan terbuka):
```powershell
cd C:\dev\Rakit-Apps\android
powershell -ExecutionPolicy Bypass -File tools\keep-adb-reverse.ps1
```

Log app:
```powershell
adb logcat --pid=$(adb shell pidof -s com.aris.templateapp)
```

**Cek:** di HP, app terbuka → tombol **Masuk** → login `dummy1@templateapp.test` / `password123` berhasil. "Tidak ada koneksi…" berarti backend belum jalan atau `adb reverse` belum aktif (bukan masalah internet HP).

### 8.3 Wireless debugging (tanpa kabel/driver, Android 11+)

HP & laptop di Wi-Fi yang sama → HP: *Opsi pengembang → Debugging nirkabel* → **Sambungkan perangkat dengan kode penyambungan**. User memberi IP:port pairing + kode 6 digit dan IP:port utama:
```powershell
adb pair <IP>:<PORT-PAIRING> <KODE>
adb connect <IP>:<PORT-UTAMA>
adb devices
```
Jika `adb reverse` menolak karena "more than one device", pakai penjaga `keep-adb-reverse.ps1` (menangani semua perangkat).

---

## 9. Login Google & GitHub (opsional, butuh Aris)

**Google:** setiap laptop punya kunci debug sendiri; SHA-1-nya harus didaftarkan Aris di Google Cloud, dan email Google user harus jadi *Test user*.
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | Select-String "SHA1"
```
(File keystore baru ada setelah build debug pertama, bagian 7.2.) Kirim baris `SHA1:` ke Aris. SHA-1 tidak rahasia.

**GitHub:** pakai `GITHUB_CLIENT_ID`/`SECRET` dari Aris, atau user membuat OAuth App sendiri di https://github.com/settings/developers dengan callback persis `http://localhost:8080/api/auth/github/callback`.

---

## 10. Troubleshooting Windows

| Gejala | Solusi |
|---|---|
| `'.\mvnw' is not recognized` / `./mvnw` gagal | Pakai `.\mvnw.cmd` / `.\gradlew.bat`. |
| `Unknown lifecycle phase ".run.profiles=dev"` | Beri tanda kutip: `"-Dspring-boot.run.profiles=dev"`. |
| Backend gagal start: `Could not resolve placeholder 'DB_URL'` / `JWT_SECRET` | `.env` belum dimuat ke sesi PowerShell (6.1). |
| `password authentication failed for user "templateapp"` | `DB_PASSWORD` di `.env` beda dengan password saat `CREATE USER`. |
| Baris pertama `.env` tidak terbaca | File tersimpan UTF-8 dengan BOM; tulis ulang dengan cara di bagian 5. |
| `Port 8080 was already in use` | `Get-NetTCPConnection -LocalPort 8080` → lihat `OwningProcess`. Tanya user sebelum `Stop-Process`. |
| Test gagal `Could not find a valid Docker environment` | Buka Docker Desktop, tunggu *Engine running*. |
| `JAVA_HOME is set to an invalid directory` / Java bukan 21 | Perbaiki `JAVA_HOME` (2.3), buka PowerShell baru. |
| `Filename too long` | `git config --global core.longpaths true`, project di `C:\dev\Rakit-Apps`. |
| `SDK location not found` | `android\local.properties` belum ada/`sdk.dir` salah (7.1). |
| `Failed to install the following SDK components` / lisensi | `sdkmanager --licenses`. |
| HP tidak muncul di `adb devices` | Driver USB (2.6), kabel data, mode *Transfer file*, atau wireless debugging (8.3). |
| HP `unauthorized` | Buka kunci HP, setujui pop-up; jika tidak muncul: *Opsi pengembang → Cabut otorisasi debug USB*, colok ulang. |
| App: "Tidak ada koneksi…" | Backend belum jalan atau `adb reverse` hilang (8.2). |
| Login Google `DEVELOPER_ERROR` | SHA-1 belum didaftarkan / `GOOGLE_WEB_CLIENT_ID` kosong / bukan Test user (9). |
| Android Studio error setelah `git pull` | Klik **Sync Now** (file Gradle berubah). |

---

## 11. Checklist akhir (laporkan ke user)

- [ ] `java -version` → 21; `adb version` jalan
- [ ] Database `templateapp` bisa diakses user `templateapp`
- [ ] `backend\.env` ada, tidak muncul di `git status`
- [ ] Backend jalan: `/api/templates` → 200, Swagger UI terbuka, login `dummy1@templateapp.test` berhasil
- [ ] `.\mvnw.cmd test` lulus (jumlah test & 0 failure)
- [ ] `android\local.properties` ada; `.\gradlew.bat assembleDebug testDebugUnitTest lint` berhasil
- [ ] `adb devices` → `device`; `adb reverse` aktif; app terbuka di HP dan login email berhasil
- [ ] (Opsional) SHA-1 sudah dikirim ke Aris; nilai OAuth terisi

## 12. Setelah setup

- Baca `AGENTS.md` (aturan kerja, tech stack, larangan) dan `README.md` (detail fitur & akun dummy).
- Alur kerja: branch sendiri → commit kecil berbahasa Indonesia (`fase-XX: ...`) → Pull Request ke `develop` → direview Aris.
- Setiap `git pull` yang mengubah file Gradle: **Sync Now** di Android Studio. Migrasi database baru otomatis dijalankan Flyway saat backend start.
