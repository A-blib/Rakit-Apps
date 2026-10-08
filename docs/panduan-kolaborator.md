# Panduan Kolaborator: Menyiapkan Project dari Nol (Windows & Linux)

Panduan ini untuk **teman yang ikut mengembangkan** Rakit (app pembuat website tanpa coding). Setiap langkah ditulis untuk **Windows 10/11** terlebih dahulu, lalu disertai versi **Linux (Ubuntu)**.

- Pemilik project (Aris) → baca juga bagian [0. Tugas pemilik project](#0-tugas-pemilik-project-aris).
- Kolaborator → mulai dari [1. Pilih peranmu](#1-pilih-peranmu).
- Penjelasan cara kerja kode ada di [`dokumentasi-project.md`](dokumentasi-project.md). README berisi rangkuman perintah.

> **Cara membaca blok perintah**
> - 🪟 **Windows (PowerShell)**: buka *Start* → ketik `PowerShell` → *Windows PowerShell*.
> - 🐧 **Linux (Terminal)**: tekan `Ctrl+Alt+T`.
> - Baris yang diawali `#` adalah komentar penjelasan, bukan perintah.

---

## Daftar isi

0. [Tugas pemilik project (Aris)](#0-tugas-pemilik-project-aris)
1. [Pilih peranmu](#1-pilih-peranmu)
2. [Pasang software](#2-pasang-software)
3. [Ambil kode project (clone)](#3-ambil-kode-project-clone)
4. [Siapkan database PostgreSQL](#4-siapkan-database-postgresql)
5. [Isi pengaturan backend (`.env`)](#5-isi-pengaturan-backend-env)
6. [Jalankan backend](#6-jalankan-backend)
7. [Login Google: SHA-1 laptopmu](#7-login-google-sha-1-laptopmu)
8. [Login GitHub](#8-login-github)
9. [Jalankan app Android di HP](#9-jalankan-app-android-di-hp)
10. [Alur kerja Git bersama](#10-alur-kerja-git-bersama)
11. [Troubleshooting khusus Windows](#11-troubleshooting-khusus-windows)
12. [Checklist akhir](#12-checklist-akhir)

---

## 0. Tugas pemilik project (Aris)

Lakukan ini **sekali** sebelum mengajak teman.

### 0.1 Unggah repo ke GitHub (private)

Saat ini repo hanya ada di laptop Aris (belum punya *remote*).

1. Buka https://github.com/new
   - Repository name: `template-app`
   - Pilih **Private**
   - **Jangan** centang "Add a README", ".gitignore", maupun "license" (repo kita sudah punya isinya)
   - Klik **Create repository**
2. Di terminal, dari folder project:

```bash
# Menghubungkan repo lokal ke GitHub (ganti <username> dengan username GitHub Aris)
git remote add origin https://github.com/<username>/template-app.git
# Ganti nama cabang utama menjadi "main" (standar GitHub), lalu kirim semua commit
git branch -M main
git push -u origin main
```

> Saat `git push` diminta login, **password akun GitHub tidak bisa dipakai**. Pilih login lewat browser (Git Credential Manager di Windows), atau buat *Personal Access Token* di https://github.com/settings/tokens lalu pakai token itu sebagai password.

3. Cek isi repo di GitHub. Pastikan **tidak ada** file `backend/.env` maupun `android/local.properties` (keduanya sudah masuk `.gitignore`).

### 0.2 Undang teman ke repo

GitHub → buka repo → **Settings → Collaborators → Add people** → masukkan username/email teman → peran **Write**.

### 0.3 Undang teman ke Google Cloud (opsional)

Lakukan ini hanya kalau teman perlu ikut mengatur console, mis. menambah client atau Test user sendiri.

Google Cloud Console → menu ☰ → **IAM & Admin → IAM → Grant access** → isi email Google teman di kolom **New principals** (Console berbahasa Indonesia: **Akun utama baru**) → di **Assign roles** pilih **Editor** → **Save**.

> Akses Console tidak otomatis membuat teman bisa login Google di app. Emailnya tetap perlu didaftarkan sebagai Test user (0.4).

### 0.4 Daftarkan teman sebagai Test user

Google Auth Platform → **Audience** → **Test users** → **+ Add users** → email Google teman → **Save**.

> Selama app berstatus *Testing*, hanya email di daftar ini yang bisa memakai tombol "Lanjutkan dengan Google". Maksimal 100 email.

### 0.5 Bagikan nilai rahasia lewat jalur pribadi

| Nilai | Rahasia? | Cara membagikan |
|---|---|---|
| `GOOGLE_WEB_CLIENT_ID` | Tidak | Boleh lewat chat/dokumen tim |
| `GITHUB_CLIENT_ID` | Tidak | Boleh lewat chat/dokumen tim |
| `GITHUB_CLIENT_SECRET` | **Ya** | Lewat chat pribadi (jangan di grup, issue, atau repo). Pilihan lebih aman: teman membuat OAuth App GitHub sendiri (bagian 8.2) |
| `JWT_SECRET` | **Ya** | **Tidak perlu dibagikan.** Setiap orang membuat sendiri untuk laptopnya |
| Password database | **Ya** | **Tidak perlu dibagikan.** Setiap orang punya database sendiri di laptopnya |

---

## 1. Pilih peranmu

| Peran | Yang dikerjakan | Bagian yang perlu dibaca |
|---|---|---|
| **Penguji** (hanya mencoba app) | Pasang APK di HP, mencoba fitur | Minta Aris mendaftarkan emailmu sebagai Test user (0.4) dan mengirim APK. Cukup itu. |
| **Developer backend** | Mengubah kode Spring Boot | 2 (Git, JDK, PostgreSQL, Docker, VS Code), 3–6, 8, 10 |
| **Developer Android** | Mengubah kode app | Semua bagian (app butuh backend yang jalan di laptop) |

---

## 2. Pasang software

### 2.1 Daftar & versi

| Software | Versi | Untuk apa |
|---|---|---|
| Git | terbaru | Mengambil & mengirim kode |
| JDK | **21** (wajib 21, bukan 17/25) | Menjalankan backend & build Android |
| PostgreSQL | 18 | Database development |
| Docker Desktop (Windows) / Docker Engine (Linux) | terbaru | Menjalankan test backend (Testcontainers) |
| VS Code + extension Java & Spring | terbaru | Editor backend |
| Android SDK | platform `android-37.0`, build-tools `36.0.0`, platform-tools | **Wajib** untuk build app Android dan `adb`. Dipasang lewat Android Studio (2.7) **atau** command line tools (2.8) |
| Android Studio | terbaru (stabil) | Editor Android (preview layout, Logcat, debugger). **Disarankan, tidak wajib** |
| Maven & Gradle | **jangan dipasang** | Sudah ada di project (`mvnw`, `gradlew`) |

> **Tips Windows:** semua software bisa dipasang lewat installer biasa (klik Next–Next). Sebagai alternatif, Windows 10/11 punya `winget` (pemasang lewat PowerShell). Perintah `winget` disertakan di setiap langkah sebagai pilihan.

### 2.2 Git

🪟 **Windows**
1. Unduh dari https://git-scm.com/download/win lalu jalankan installer.
2. Pilihan penting saat instalasi (selain ini biarkan default):
   - *Default editor*: pilih **Visual Studio Code** jika sudah terpasang.
   - *Adjusting the name of the initial branch*: **Override** → `main`.
   - *Configuring the line ending conversions*: **Checkout Windows-style, commit Unix-style line endings** (default).
   - *Credential helper*: **Git Credential Manager** (default). Ini membuat login GitHub cukup lewat browser.
3. Alternatif lewat PowerShell: `winget install --id Git.Git -e`
4. Tutup lalu buka lagi PowerShell, kemudian atur identitasmu:

```powershell
git --version                                         # cek terpasang
git config --global user.name "Nama Kamu"             # nama yang muncul di commit
git config --global user.email "email@kamu.com"       # samakan dengan email GitHub
git config --global core.longpaths true               # izinkan path panjang (folder build Android bisa sangat dalam)
```

🐧 **Linux**
```bash
sudo apt update && sudo apt install -y git
git config --global user.name "Nama Kamu"
git config --global user.email "email@kamu.com"
```

### 2.3 JDK 21

🪟 **Windows**
1. Unduh **Eclipse Temurin 21 (LTS)**, file `.msi` untuk Windows x64, dari https://adoptium.net/temurin/releases/?version=21
2. Jalankan installer. Di halaman *Custom Setup*, ubah dua fitur berikut menjadi **"Will be installed on local hard drive"**:
   - **Set JAVA_HOME variable**
   - **Add to PATH**
3. Alternatif: `winget install --id EclipseAdoptium.Temurin.21.JDK -e`
4. **Tutup semua jendela PowerShell**, buka yang baru, lalu cek:

```powershell
java -version          # harus menampilkan "21.x"
echo $env:JAVA_HOME    # harus berisi folder JDK 21, mis. C:\Program Files\Eclipse Adoptium\jdk-21...
```

> Jika yang muncul persis tulisan `$env:JAVA_HOME`, kamu sedang di **Command Prompt (cmd)**, bukan PowerShell. Buka **PowerShell** (awal barisnya `PS C:\...>`), atau di cmd pakai `echo %JAVA_HOME%`. Di cmd, hasil berupa tulisan `%JAVA_HOME%` berarti variabelnya belum diatur.

Jika `JAVA_HOME` kosong, atur manual: *Start* → ketik **"environment variables"** → *Edit the system environment variables* → **Environment Variables…** → di *User variables* klik **New** → Name `JAVA_HOME`, Value = folder JDK (mis. `C:\Program Files\Eclipse Adoptium\jdk-21.0.x-hotspot`) → OK.

🐧 **Linux**
```bash
sudo apt install -y openjdk-21-jdk
sudo update-alternatives --config java      # pilih java-21 jika ada beberapa versi
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc && source ~/.bashrc
java -version
```

### 2.4 PostgreSQL 18

🪟 **Windows**
1. Unduh installer dari https://www.postgresql.org/download/windows/ (tombol *Download the installer* → versi **18**, Windows x86-64).
2. Saat instalasi:
   - *Select Components*: biarkan semua tercentang (PostgreSQL Server, pgAdmin 4, Command Line Tools). *Stack Builder* boleh tidak dicentang.
   - *Password*: buat password untuk superuser **postgres** dan **catat**. Ini dibutuhkan di bagian 4.
   - *Port*: biarkan **5432**.
   - *Locale*: biarkan default.
3. Setelah selesai, di Start Menu muncul **SQL Shell (psql)** dan **pgAdmin 4**.
4. Alternatif: `winget search postgresql`, lalu pasang paket versi 18 yang muncul.

🐧 **Linux**
```bash
sudo apt install -y postgresql
psql --version
```

### 2.5 Docker (hanya untuk menjalankan test backend)

🪟 **Windows**
1. Docker Desktop di Windows memakai **WSL 2**. Pasang dulu lewat PowerShell **sebagai Administrator** (klik kanan PowerShell → *Run as administrator*):
   ```powershell
   wsl --install       # memasang Windows Subsystem for Linux 2; restart laptop setelahnya
   ```
2. Unduh **Docker Desktop** dari https://www.docker.com/products/docker-desktop/ → jalankan installer → centang **Use WSL 2 instead of Hyper-V** → restart jika diminta.
3. Buka Docker Desktop dan tunggu sampai tulisan di kiri bawah menjadi **Engine running**.
4. Alternatif: `winget install --id Docker.DockerDesktop -e`
5. Cek: `docker run hello-world` → muncul tulisan "Hello from Docker!".

> Docker Desktop harus **sudah dibuka** setiap kali menjalankan `mvnw test`.

🐧 **Linux**
```bash
sudo apt install -y docker.io
sudo usermod -aG docker $USER      # agar docker bisa tanpa sudo; LOGOUT lalu LOGIN lagi
docker run hello-world
```

### 2.6 VS Code

🪟 **Windows**: unduh dari https://code.visualstudio.com/ → installer → centang **Add "Open with Code" action** dan **Add to PATH**. Alternatif: `winget install --id Microsoft.VisualStudioCode -e`

🐧 **Linux**: `sudo snap install code --classic`

Extension yang direkomendasikan muncul **otomatis** saat folder `backend/` dibuka (bagian 6).

### 2.7 Android SDK, pilihan A: lewat Android Studio (disarankan)

Yang **wajib** untuk build app Android adalah **Android SDK**, bukan Android Studio. Developer backend dan penguji tidak butuh keduanya.

| Pilihan | Cocok untuk | Kelebihan | Kekurangan |
|---|---|---|---|
| **A. Android Studio** (bagian ini) | Developer Android, terutama yang masih belajar | SDK terpasang otomatis lewat Setup Wizard. Ada preview layout, Logcat, debugger, tombol Run | Unduhan besar, butuh RAM lebih |
| **B. Command line tools** (bagian 2.8) | Yang cukup build & pasang lewat terminal, atau laptop terbatas | Unduhan kecil, ringan | Semua lewat terminal; log lewat `adb logcat`, tanpa preview layout |

Pilih salah satu saja. Kalau ragu, pilih A.

🪟 **Windows**
1. Unduh dari https://developer.android.com/studio → jalankan installer (biarkan default).
2. Alternatif: `winget install --id Google.AndroidStudio -e`
3. Buka Android Studio → **Setup Wizard** → pilih **Standard** → terima semua lisensi → tunggu unduhan SDK selesai (beberapa GB).
4. Lokasi SDK default: `C:\Users\<nama>\AppData\Local\Android\Sdk`
5. Agar `adb` bisa dipakai dari PowerShell, tambahkan ke environment variable:
   - *Start* → ketik **"environment variables"** → *Edit the system environment variables* → **Environment Variables…**
   - *User variables* → **New** → Name `ANDROID_HOME`, Value `%LOCALAPPDATA%\Android\Sdk` → OK
   - *User variables* → pilih **Path** → **Edit** → **New** → `%ANDROID_HOME%\platform-tools` → OK → OK
   - Tutup lalu buka lagi PowerShell, kemudian cek dengan `adb version`.
6. **Driver USB HP** (khusus Windows; Linux tidak perlu):
   - HP Google Pixel: Android Studio → *Settings → Languages & Frameworks → Android SDK → SDK Tools* → centang **Google USB Driver** → Apply.
   - Merek lain (Samsung, Xiaomi, realme, OPPO, vivo): pasang driver USB resmi dari situs merek HP tersebut. Daftar tautannya ada di https://developer.android.com/studio/run/oem-usb
   - Tanpa driver yang benar, HP tidak muncul di `adb devices`. Jika repot, pakai **wireless debugging** (bagian 9.3), yang tidak butuh driver.

🐧 **Linux**
```bash
sudo snap install android-studio --classic
# Setelah Setup Wizard (Standard) selesai:
echo 'export ANDROID_HOME=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export PATH=$ANDROID_HOME/platform-tools:$PATH' >> ~/.bashrc
source ~/.bashrc
sudo apt install -y android-sdk-platform-tools-common   # aturan udev agar HP dikenali lewat USB
```

### 2.8 Android SDK, pilihan B: tanpa Android Studio (command line tools)

Lewati bagian ini jika sudah memakai pilihan A.

Paket SDK yang dibutuhkan project:

| Paket | Fungsi |
|---|---|
| `cmdline-tools;latest` | Berisi `sdkmanager`, pengunduh paket SDK lain |
| `platform-tools` | `adb`, untuk memasang dan menjalankan app di HP |
| `platforms;android-37.0` | Library Android untuk compile (sesuai `compileSdk` 37) |
| `build-tools;36.0.0` | Alat pengemas APK |

🪟 **Windows**
1. Buka https://developer.android.com/studio → gulir ke bawah ke **Command line tools only** → unduh versi **Windows**.
2. Buat folder `C:\Users\<nama>\AppData\Local\Android\Sdk\cmdline-tools\latest`.
3. Ekstrak ZIP. Isinya folder `cmdline-tools` berisi `bin`, `lib`, dst. Pindahkan **isi** folder itu ke dalam `...\cmdline-tools\latest`, sehingga ada file `...\cmdline-tools\latest\bin\sdkmanager.bat`.
   > Struktur `cmdline-tools\latest\bin` wajib persis begitu. Jika `bin` berada langsung di `cmdline-tools\bin`, `sdkmanager` menolak jalan dengan error *Could not determine SDK root*.
4. Atur environment variable (*Start* → **"environment variables"** → *Edit the system environment variables* → **Environment Variables…**):
   - *User variables* → **New** → Name `ANDROID_HOME`, Value `%LOCALAPPDATA%\Android\Sdk` → OK
   - *User variables* → **Path** → **Edit** → tambahkan dua baris: `%ANDROID_HOME%\cmdline-tools\latest\bin` dan `%ANDROID_HOME%\platform-tools` → OK → OK
5. Tutup semua PowerShell, buka yang baru, lalu pasang paket:

```powershell
# Menerima lisensi SDK (ketik y lalu Enter untuk setiap lisensi). Tanpa ini Gradle menolak build.
sdkmanager --licenses
# Mengunduh paket yang dibutuhkan project
sdkmanager "platform-tools" "platforms;android-37.0" "build-tools;36.0.0"
# Khusus HP Google Pixel: driver USB (merek lain pakai driver dari situs mereknya, lihat 2.7 no. 6)
sdkmanager "extras;google;usb_driver"
# Cek hasil
sdkmanager --list_installed
adb version
```

🐧 **Linux**
1. Unduh **Command line tools only** versi **Linux** dari https://developer.android.com/studio, lalu:

```bash
# Menyusun folder SDK dengan struktur yang diharapkan sdkmanager: cmdline-tools/latest/bin
mkdir -p ~/Android/Sdk/cmdline-tools
cd ~/Downloads && unzip commandlinetools-linux-*_latest.zip      # menghasilkan folder cmdline-tools/
mv cmdline-tools ~/Android/Sdk/cmdline-tools/latest

# Menyimpan lokasi SDK dan PATH agar sdkmanager & adb bisa dipanggil dari terminal mana pun
echo 'export ANDROID_HOME=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH' >> ~/.bashrc
source ~/.bashrc

sdkmanager --licenses                                             # terima lisensi (y)
sdkmanager "platform-tools" "platforms;android-37.0" "build-tools;36.0.0"
sudo apt install -y android-sdk-platform-tools-common             # aturan udev agar HP dikenali lewat USB
adb version
```

2. `sdkmanager` butuh Java. JDK 21 dari bagian 2.3 sudah cukup.

Setelah itu, lanjutkan ke bagian 9. Untuk pilihan B, `local.properties` dibuat manual (lihat 9.1) dan app dijalankan lewat terminal (9.5).

---

## 3. Ambil kode project (clone)

> 🪟 **Penting untuk Windows:** simpan project di folder **pendek tanpa spasi**, mis. `C:\dev\template-app`. Folder seperti `C:\Users\Nama Kamu\Desktop\Rakit Apps` (ada spasi, path panjang) sering membuat build Android gagal di Windows.

🪟 **Windows (PowerShell)**
```powershell
mkdir C:\dev -Force                     # buat folder kerja (abaikan jika sudah ada)
cd C:\dev
git clone https://github.com/<username-aris>/template-app.git
cd template-app
dir                                     # harus terlihat: android, backend, docs, README.md, ...
```

🐧 **Linux**
```bash
mkdir -p ~/dev && cd ~/dev
git clone https://github.com/<username-aris>/template-app.git
cd template-app && ls
```

> Saat clone pertama kali, Windows membuka jendela login GitHub di browser. Login dengan akun yang sudah diundang Aris.

**Soal akhir baris (CRLF vs LF).** Repo sudah punya `.gitattributes`, jadi Git otomatis menjaga `mvnw` dan `gradlew` tetap LF, sementara `.cmd` dan `.bat` tetap CRLF. Jangan ubah pengaturan ini.

---

## 4. Siapkan database PostgreSQL

Setiap orang punya database **sendiri** di laptopnya, dengan nama `templateapp` dan user `templateapp`. Password-nya bebas, cukup catat untuk bagian 5.

🪟 **Windows**: buka **SQL Shell (psql)** dari Start Menu. Tekan Enter untuk *Server, Database, Port, Username* (biarkan default), lalu masukkan password **postgres** yang dibuat saat instalasi. Setelah muncul `postgres=#`, ketik:

```sql
CREATE USER templateapp WITH PASSWORD 'ganti_password_ini';
CREATE DATABASE templateapp OWNER templateapp;
\q
```

> Alternatif tanpa mengetik SQL: buka **pgAdmin 4** → *Servers → PostgreSQL 18* → klik kanan *Login/Group Roles → Create* (nama `templateapp`, tab *Definition* isi password, tab *Privileges* aktifkan *Can login?*) → lalu klik kanan *Databases → Create* (nama `templateapp`, *Owner* `templateapp`).

🐧 **Linux**
```bash
sudo -u postgres psql -c "CREATE USER templateapp WITH PASSWORD 'ganti_password_ini';"
sudo -u postgres createdb -O templateapp templateapp
```

Cek koneksi (Windows dari SQL Shell dengan Username `templateapp`; Linux):
```bash
psql -h localhost -U templateapp -d templateapp -c "select 1"
```

---

## 5. Isi pengaturan backend (`.env`)

File `backend/.env` berisi pengaturan pribadi laptopmu dan **tidak pernah di-commit**.

### 5.1 Salin dari contoh

🪟 **Windows**
```powershell
cd C:\dev\template-app\backend
Copy-Item .env.example .env
notepad .env          # atau buka di VS Code
```

🐧 **Linux**
```bash
cd ~/dev/template-app/backend
cp .env.example .env
nano .env
```

### 5.2 Buat `JWT_SECRET` acak (minimal 32 karakter)

🪟 **Windows (PowerShell)**
```powershell
# Membuat 48 byte acak yang aman (kriptografis), lalu mengubahnya menjadi teks Base64
$bytes = New-Object byte[] 48
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

🐧 **Linux**
```bash
openssl rand -base64 48
```

Salin hasilnya ke baris `JWT_SECRET=`.

### 5.3 Isi semua nilai

Tulis **tanpa tanda kutip** dan **tanpa spasi** di sekitar `=`:

```properties
DB_URL=jdbc:postgresql://localhost:5432/templateapp
DB_USERNAME=templateapp
DB_PASSWORD=password-database-kamu
JWT_SECRET=hasil-langkah-5.2
JWT_ACCESS_TTL_MINUTES=15
JWT_REFRESH_TTL_DAYS=30
GOOGLE_WEB_CLIENT_ID=<dari Aris>
GITHUB_CLIENT_ID=<dari Aris, atau OAuth App milikmu (bagian 8.2)>
GITHUB_CLIENT_SECRET=<dari Aris lewat chat pribadi, atau milikmu>
GITHUB_REDIRECT_URI=http://localhost:8080/api/auth/github/callback
APP_DEEP_LINK=templateapp://auth/callback
```

> 🪟 Notepad kadang menyimpan dengan encoding "UTF-8 with BOM" dan bisa membuat baris pertama tidak terbaca. Saat *Save As*, pilih **Encoding: UTF-8** (tanpa BOM). Lebih aman lagi edit lewat VS Code.

---

## 6. Jalankan backend

### 6.1 Dari terminal

🪟 **Windows (PowerShell)**
```powershell
cd C:\dev\template-app\backend
# Backend membaca pengaturan dari environment variable, jadi isi .env dimuat dulu ke jendela PowerShell ini
# (ulangi setiap membuka PowerShell baru; VS Code melakukannya otomatis lewat launch configuration).
Get-Content .env | Where-Object { $_ -match '^\s*[A-Za-z_][A-Za-z0-9_]*=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), "Process")
}
# Pakai mvnw.cmd (bukan ./mvnw). Tanda kutip wajib di PowerShell karena ada titik di parameter.
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

🐧 **Linux**
```bash
cd ~/dev/template-app/backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Tunggu sampai muncul `Started TemplateAppApplication`. Run pertama mengunduh library cukup lama (beberapa menit). Pada run pertama dengan database kosong, log juga menampilkan `Seeder selesai: 10 user dummy dibuat`.

Buka http://localhost:8080/swagger-ui.html di browser, lalu coba login `dummy1@templateapp.test` / `password123`.

Hentikan backend dengan `Ctrl+C`.

### 6.2 Dari VS Code

1. *File → Open Folder…* → pilih folder **`backend`** (bukan folder root project).
2. Klik **Install** pada notifikasi "Do you want to install the recommended extensions?".
3. Tunggu ikon Java di status bar bawah selesai memuat ("Java: Ready").
4. Jalankan dengan salah satu cara berikut:
   - *Terminal → Run Task… → **backend: jalankan (dev)***. Task ini otomatis memakai `mvnw.cmd` di Windows.
   - Tab **Run and Debug** (ikon ▷ dengan serangga) → pilih **Backend: debug (dev)** → **F5**. Cara ini memungkinkan breakpoint.

### 6.3 Test backend

Docker Desktop (Windows) atau Docker (Linux) harus jalan.

🪟 `.\mvnw.cmd test`  🐧 `./mvnw test`

Hasil yang benar: `BUILD SUCCESS` dan semua test lulus.

**Fitur Upload (Fase 15–18).** Tidak ada software baru yang wajib dipasang:

- ZIP yang diupload, salinan bernomor, thumbnail, dan paket template tersimpan di folder `backend/uploads/` (otomatis dibuat, tidak di-commit). Folder lain bisa dipilih dengan `UPLOAD_STORAGE_DIR` di `.env`.
- Saat template dikirim, backend mengunduh library dari CDN (jsDelivr, cdnjs, unpkg). Laptop yang menjalankan backend butuh internet saat itu.
- ZIP uji pengecekan ada di `backend/src/test/resources/test-fixtures/`. Jika kamu menambah/mengubah aturan pengecekan, buat ulang ZIP-nya dengan Python 3 (🪟 `py tools\buat-zip-uji.py`  🐧 `python3 tools/buat-zip-uji.py`, dari folder `backend`). Python **tidak** dibutuhkan hanya untuk menjalankan test.
- Di Android Studio klik **Sync Now** setelah menarik perubahan ini, karena ada library baru `androidx.webkit`.

**Buat website via template (Fase 19–26).** Tidak ada software baru yang wajib dipasang:

- Di Android Studio klik **Sync Now** setelah menarik perubahan ini, karena ada library baru **WorkManager** dan **jsoup**.
- Database HP naik ke versi 2 secara otomatis (project lama tetap ada).
- Agar 3 template demo di galeri bisa dipakai di editor, jalankan backend dengan seeder demo (`--app.seed.demo-templates=true`). Akun demo yang sudah ada otomatis dilengkapi paket contohnya saat backend start; tidak perlu menghapus data.
- Gambar contoh paket demo dibuat dengan Python 3 (🪟 `py tools\buat-gambar-demo.py`  🐧 `python3 tools/buat-gambar-demo.py`, dari folder `backend`), hanya jika gambarnya ingin diubah. Butuh library Pillow.

---

## 7. Login Google: SHA-1 laptopmu

Login Google di HP hanya berhasil kalau **sidik jari (SHA-1) kunci debug laptop yang membangun APK** sudah didaftarkan di Google Cloud. Setiap laptop punya kunci debug sendiri, jadi **setiap developer Android perlu mengirim SHA-1-nya ke Aris**.

### 7.1 Ambil SHA-1

Kunci debug dibuat otomatis saat build debug pertama, baik lewat Android Studio maupun `gradlew` di terminal. Jalankan app sekali dulu (bagian 9) kalau file-nya belum ada.

🪟 **Windows (PowerShell)**
```powershell
# keytool ikut terpasang bersama JDK 21 (bagian 2.3)
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | Select-String "SHA1"
```

Jika `keytool` tidak dikenali, pakai yang ada di dalam Android Studio:
```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | Select-String "SHA1"
```

Atau lewat Gradle (dari folder `android`): `.\gradlew.bat signingReport`, lalu cari baris `SHA1:` di bagian `Variant: debug`.

🐧 **Linux**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

Hasilnya seperti `SHA1: 1B:A6:02:...:0B:AE`. **Kirim ke Aris.** SHA-1 tidak rahasia.

### 7.2 Aris mendaftarkan SHA-1 teman

Google Auth Platform → **Clients → + Create client** → *Application type* **Android**:
- Name: `templateapp-android-debug-<nama-teman>`
- Package name: `com.aris.templateapp`
- SHA-1: dari teman
- **Create**

Satu client Android = satu SHA-1. Package name **sama** untuk semua orang.

---

## 8. Login GitHub

### 8.1 Pakai OAuth App milik Aris (paling mudah)

Isi `GITHUB_CLIENT_ID` dan `GITHUB_CLIENT_SECRET` di `backend/.env` dengan nilai dari Aris (dikirim lewat chat pribadi). Callback `http://localhost:8080/...` berlaku di laptop siapa pun.

### 8.2 Atau buat OAuth App sendiri (lebih aman, secret tidak perlu dibagikan)

1. https://github.com/settings/developers → **OAuth Apps → New OAuth App**
2. Isi:
   - Application name: `Rakit (dev - <nama>)`
   - Homepage URL: `http://localhost:8080`
   - **Authorization callback URL** (di tampilan baru GitHub bernama **Redirect URL**): `http://localhost:8080/api/auth/github/callback`
   - *Enable Device Flow*: jangan dicentang
3. **Register application** → salin **Client ID** → **Generate a new client secret** → salin secret-nya (hanya ditampilkan sekali).
4. Isi keduanya di `backend/.env`.

---

## 9. Jalankan app Android di HP

### 9.1 Buka project & isi `local.properties`

1. Android Studio → **Open** → pilih folder **`android`** (bukan folder root) → tunggu **Gradle Sync** selesai.
2. Pastikan Gradle memakai JDK 21: *File → Settings* (Windows/Linux) → *Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK* → pilih **JDK 21** (mis. "temurin-21" atau `/usr/lib/jvm/java-21-openjdk-amd64`).
3. File `android/local.properties` dibuat otomatis oleh Android Studio. **Tanpa Android Studio (pilihan B, 2.8):** lewati langkah 1–2 dan buat file ini sendiri dengan isi di bawah, sesuaikan `sdk.dir` dengan lokasi SDK-mu. Tambahkan baris Client ID Web Google di paling bawah:

```properties
# Windows (dibuat otomatis; garis miring terbalik memang ditulis dobel):
sdk.dir=C\:\\Users\\NamaKamu\\AppData\\Local\\Android\\Sdk
# Linux:
# sdk.dir=/home/namakamu/Android/Sdk

GOOGLE_WEB_CLIENT_ID=<Client ID Web dari Aris>
```

4. Klik **Sync Now**, atau *File → Sync Project with Gradle Files*.

### 9.2 Siapkan HP (USB)

1. HP: *Pengaturan → Tentang ponsel* → ketuk **Nomor build** 7 kali → kembali → buka **Opsi pengembang** → aktifkan **USB debugging**.
2. Colok HP dengan kabel **data**, lalu pilih mode USB **Transfer file** (bukan *Tethering USB*).
3. Setujui pop-up **"Izinkan USB debugging?"** (centang *Selalu izinkan*).
4. Cek:
   ```
   adb devices         # harus ada satu baris berstatus "device"
   ```

### 9.3 Alternatif tanpa kabel: wireless debugging (Android 11+)

Cara ini tidak butuh driver USB di Windows dan tidak terganggu kabel yang longgar.

1. HP dan laptop terhubung ke **Wi-Fi yang sama**.
2. HP: *Opsi pengembang → Debugging nirkabel* → aktifkan → ketuk **Sambungkan perangkat dengan kode penyambungan**.
3. Laptop:
   ```
   adb pair 192.168.x.x:PORT-PAIRING      # IP:port dan kode 6 digit yang tampil di HP
   adb connect 192.168.x.x:PORT           # IP:port di halaman utama Debugging nirkabel (berbeda dengan port pairing)
   adb devices
   ```

### 9.4 Sambungkan HP ke backend di laptop

Backend harus sudah jalan (bagian 6), lalu:
```
adb reverse tcp:8080 tcp:8080
```

Perintah ini membuat `localhost:8080` di HP diteruskan ke backend di laptop. **Wajib diulang** setiap HP dicabut-colok, HP restart, atau `adb` di-restart.

**Supaya tidak perlu mengulang manual**, jalankan penjaga yang memasang ulang `adb reverse` setiap 3 detik ke semua sambungan HP. Biarkan jendelanya terbuka selama mengembangkan app (terutama saat memakai wireless debugging, yang sering putus-sambung):

🪟 **Windows** (dari folder `android`):
```powershell
powershell -ExecutionPolicy Bypass -File tools\keep-adb-reverse.ps1
```

🐧 **Linux** (dari folder `android`):
```bash
./tools/keep-adb-reverse.sh
```

> 🪟 Karena memakai `adb reverse`, **Windows Firewall tidak perlu dibuka**: koneksinya lewat jalur adb, bukan lewat jaringan Wi-Fi.

### 9.5 Run

- Android Studio: pilih HP di daftar perangkat (atas) → klik **Run ▶**.
- Atau dari terminal (folder `android`). Ini satu-satunya cara jika memakai pilihan B (2.8); log app dilihat dengan `adb logcat --pid=<pid>` (Linux: `adb logcat --pid=$(adb shell pidof -s com.aris.templateapp)`):

🪟 **Windows**
```powershell
.\gradlew.bat installDebug
adb shell am start -n com.aris.templateapp/.MainActivity
```

🐧 **Linux**
```bash
./gradlew installDebug
adb shell am start -n com.aris.templateapp/.MainActivity
```

> Jangan menjalankan `gradlew` dari terminal bersamaan dengan Build/Run di Android Studio.

---

## 10. Alur kerja Git bersama

```
main ───●───────────●──────────●───────►   (selalu bisa dijalankan)
         \         /           /
          ●──●──●─┘  fitur-a  /
           \                 /
            ●──●──●─────────┘  fitur-b
```

1. Sebelum mulai, ambil perubahan terbaru:
   ```
   git checkout main
   git pull
   ```
2. Buat cabang baru untuk setiap pekerjaan:
   ```
   git checkout -b fase-09-onboarding
   ```
3. Commit kecil-kecil dengan format pesan project (Bahasa Indonesia):
   ```
   git add <file-yang-diubah>
   git commit -m "fase-09: tambah layar pilih peran"
   ```
4. Kirim cabang ke GitHub: `git push -u origin fase-09-onboarding`
5. Buka GitHub → **Compare & pull request** → minta teman me-review → **Merge** ke `main`.

**Aturan tim:**
- **Jangan pernah commit rahasia**: `backend/.env`, `android/local.properties`, file `*.jks`/`*.keystore`. Sebelum commit, selalu cek `git status`.
- Jalankan `mvnw test` (backend) atau `gradlew testDebugUnitTest` (Android) sebelum membuat pull request.
- Ada file migrasi database baru? Pakai nomor berikutnya (`V7__...sql`). **Jangan mengubah** file `V1`–`V6` yang sudah ada.
- Selesai mengubah file build Android? Ingatkan teman untuk klik **Sync Now** setelah `git pull`.

---

## 11. Troubleshooting khusus Windows

| Masalah | Penyebab & solusi |
|---|---|
| `'.\mvnw' is not recognized` / `./mvnw` tidak jalan | Di Windows pakai `.\mvnw.cmd`. Di VS Code, task sudah otomatis memakai `mvnw.cmd`. |
| `Unknown lifecycle phase ".run.profiles=dev"` | PowerShell memotong parameter di titik. Beri tanda kutip: `"-Dspring-boot.run.profiles=dev"`. |
| `java` / `keytool` / `adb` is not recognized | PATH belum benar, atau PowerShell belum dibuka ulang setelah instalasi. Cek bagian 2.3 dan 2.7/2.8, lalu tutup dan buka lagi PowerShell/VS Code/Android Studio. |
| `JAVA_HOME is set to an invalid directory` / versi Java bukan 21 | Atur `JAVA_HOME` ke folder JDK 21 (bagian 2.3), lalu buka ulang terminal. Cek dengan `java -version`. |
| `/usr/bin/env: 'sh\r': No such file or directory` (di WSL/Linux) | `mvnw`/`gradlew` tersimpan dengan akhir baris CRLF. Jalankan `git rm --cached -r . && git reset --hard` (perubahan lokal yang belum di-commit akan hilang; simpan dulu). `.gitattributes` mencegah hal ini terulang. |
| `Filename too long` saat clone atau build | `git config --global core.longpaths true`, dan simpan project di path pendek (`C:\dev\template-app`). |
| Build Android gagal aneh / path dengan spasi | Pindahkan project ke folder tanpa spasi (bagian 3). |
| `password authentication failed for user "templateapp"` | `DB_PASSWORD` di `.env` berbeda dengan password saat `CREATE USER` (bagian 4). |
| `Connection to localhost:5432 refused` | Service PostgreSQL belum jalan: *Start* → **Services** → cari `postgresql-x64-18` → **Start**. |
| Test gagal: `Could not find a valid Docker environment` | Buka Docker Desktop dan tunggu *Engine running*. |
| `Port 8080 was already in use` | Cari prosesnya: `netstat -ano \| findstr :8080`, lalu hentikan: `taskkill /PID <angka-terakhir> /F`. Atau tutup terminal/VS Code lain yang sedang menjalankan backend. |
| HP tidak muncul di `adb devices` (Windows) | Driver USB belum terpasang (bagian 2.7 no. 6). Ganti kabel atau port. Atau pakai wireless debugging (bagian 9.3). |
| HP `unauthorized` | Buka kunci HP dan setujui pop-up. Jika tidak muncul: *Opsi pengembang → Cabut otorisasi debug USB*, lalu colok ulang. |
| App di HP: "Tidak ada koneksi…" padahal internet HP lancar | Pesan ini berarti app tidak bisa menjangkau **backend di laptop** (bukan internet). Jalankan penjaga `tools\keep-adb-reverse.ps1` (bagian 9.4) dan pastikan backend jalan. Jika HP tersambung lewat USB **dan** Wi-Fi sekaligus, `adb reverse` tanpa `-s` ditolak ("more than one device"); penjaga sudah menangani ini. |
| Login Google: `DEVELOPER_ERROR` / "No credentials available" | SHA-1 laptopmu belum didaftarkan (bagian 7), `GOOGLE_WEB_CLIENT_ID` salah/kosong, atau emailmu belum menjadi Test user (0.4). |
| GitHub: "redirect_uri is not associated with this application" | Callback/Redirect URL di OAuth App harus persis `http://localhost:8080/api/auth/github/callback` (`http`, ada `:8080`, tanpa `/` di akhir). |
| Backend gagal start: `Could not resolve placeholder 'DB_URL'` (Windows, dari terminal) | Isi `.env` belum dimuat ke PowerShell. Jalankan perintah `Get-Content .env ...` di bagian 6.1 dulu, atau jalankan lewat VS Code (Run and Debug). |
| `.env` tidak terbaca / app gagal start `JWT_SECRET minimal 32 karakter` | Pastikan file bernama persis `.env` (bukan `.env.txt`; di File Explorer aktifkan *View → File name extensions*), disimpan UTF-8 tanpa BOM, dan backend dijalankan dari folder `backend`. |

---

## 12. Checklist akhir

Developer backend:
- [ ] `java -version` → 21
- [ ] `psql` bisa login sebagai `templateapp`
- [ ] `backend/.env` terisi (tanpa tanda kutip)
- [ ] Backend jalan, Swagger UI terbuka, login `dummy1@templateapp.test` / `password123` berhasil
- [ ] `mvnw test` lulus (Docker jalan)

Developer Android (tambahan):
- [ ] Android SDK terpasang (`adb version` jalan, `ANDROID_HOME` terisi)
- [ ] Pilihan A: Android Studio membuka folder `android` tanpa error setelah Sync. Pilihan B: `gradlew assembleDebug` berhasil dari terminal
- [ ] `adb devices` menampilkan HP berstatus `device`
- [ ] `adb reverse tcp:8080 tcp:8080` sudah dijalankan
- [ ] App terpasang dan bisa masuk dengan akun dummy
- [ ] SHA-1 sudah dikirim ke Aris dan didaftarkan (untuk login Google)
- [ ] Email Google sudah menjadi Test user
