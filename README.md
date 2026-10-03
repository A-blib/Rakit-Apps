# App Template Website

App Android untuk membuat website tanpa coding. Satu akun punya dua mode: **pembuat website** (memilih template, mengedit teks/warna/foto, lalu export ke HTML/CSS/JS) dan **penyedia template** (mengunggah template untuk dipakai orang lain). User bisa langsung mencoba sebagai tamu; login hanya diminta saat butuh fitur online.

### Status fitur

| Fitur | Status |
|---|---|
| Persiapan lingkungan & struktur repo | Sudah (Fase 00) |
| Backend: auth email, Google, GitHub, penyambungan akun, onboarding, mode | Belum (Fase 01–04) |
| Android: splash, intro, masuk/daftar, onboarding, profil, pengaturan | Belum (Fase 05–09) |
| Dashboard Pembuat Website & Dashboard Provider | Segera hadir (hanya layar "Segera hadir") |
| Galeri template, editor, export, publish, lupa password | Segera hadir (belum dibangun) |

---

## Struktur repo

```
template-app/
├── AGENTS.md / CLAUDE.md   ← instruksi pengembangan untuk agent AI
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

_Diisi lengkap di Fase 03._

## Menjalankan backend

_Diisi di Fase 01._

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

_Diisi di Fase 05._

## Akun dummy

_Diisi di Fase 04._

## Contoh SQL status provider

_Diisi di Fase 04._

## Troubleshooting

| Masalah | Solusi |
|---|---|
| `adb devices` kosong atau `no permissions` | Pastikan kabel USB data, pasang `android-sdk-platform-tools-common` (aturan udev), cabut-colok ulang HP. |
| Status `unauthorized` | Buka kunci HP, setujui pop-up "Allow USB debugging". Jika tidak muncul: *Opsi pengembang → Revoke USB debugging authorizations*, lalu colok ulang. |
| `java -version` masih 25 | Jalankan `sudo update-alternatives --config java` dan pilih java-21; cek `JAVA_HOME`. |
| `docker: permission denied` | Logout lalu login lagi setelah `usermod -aG docker $USER`. |
| HP putus-sambung terus; `journalctl -k \| grep usb` berisi `error -71` | Masalah fisik, bukan setting: ganti kabel USB data, pindah port laptop (jangan lewat hub), bersihkan lubang USB HP. |
| HP tiba-tiba hilang dari Android Studio / `adb server version doesn't match` | Ada dua `adb` berbeda versi. Pastikan `which adb` menunjuk ke `~/Android/Sdk/platform-tools/adb`, lalu `adb kill-server && adb devices`. |

_Daftar ini dilengkapi di fase berikutnya._

## Catatan belajar

Lihat [`docs/catatan-belajar/`](docs/catatan-belajar/).
