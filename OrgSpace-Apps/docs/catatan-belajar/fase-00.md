# Fase 00: Persiapan lingkungan & repo

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `template-app/` | Folder monorepo berisi `backend/`, `android/`, `docs/` |
| `AGENTS.md` | Instruksi pengembangan untuk agent AI. Pengguna Claude Code menyalinnya menjadi `CLAUDE.md` di laptop masing-masing (file itu tidak di-commit sejak Oktober 2026) |
| `.gitignore` | Mencegah rahasia (`.env`, `local.properties`, keystore), hasil build, dan `.idea/` ikut ter-commit; `backend/.vscode/` tetap di-commit |
| `README.md` | README awal: status fitur, struktur repo, prasyarat, persiapan Ubuntu, alat kerja, setup database, menyiapkan HP |
| `docs/rancangan-app-template-website.pdf` | Salinan alur besar app dari folder "Instruksi dan alur" |
| `docs/catatan-belajar/fase-00.md` | Catatan ini |
| `docs/screenshots/.gitkeep` | Menjaga folder screenshot tetap ada di Git walau masih kosong |

## Alasan keputusan
- Memasang JDK 21 **berdampingan** dengan JDK 25 → Spring Boot dan Android Gradle Plugin paling stabil di LTS 21; JDK 25 tidak perlu dihapus (alternatif: menghapus JDK 25 — tidak dipilih karena bisa merusak program lain yang memakainya).
- Docker dari paket `docker.io` Ubuntu → cukup untuk Testcontainers dan satu perintah saja (alternatif: repo resmi Docker — lebih baru tapi langkahnya lebih panjang; tetap ditulis sebagai opsi di README).
- Android Studio lewat Snap → selalu versi stabil terbaru dan update otomatis (alternatif: unduh tar.gz manual — harus update sendiri).
- `.vscode/` di-ignore secara umum, kecuali `backend/.vscode/` → pengaturan VS Code bersama ikut repo, pengaturan pribadi di folder lain tidak.
- Folder `android/` dibiarkan kosong → di Fase 05 Aris membuatnya lewat wizard Android Studio sesuai instruksi.

## Cara menjalankan & mengetes
1. Jalankan perintah instalasi di README bagian "Persiapan lingkungan Ubuntu".
2. Cek: `java -version` (harus 21), `docker run hello-world`, `adb devices` (HP berstatus `device`).
3. Cek repo: `git status` tidak menampilkan file rahasia.

## Hasil tes
- Belum ada test kode di fase ini.
- Cek awal (sebelum Aris memasang): Java 25 ✔ (perlu 21 ✘), PostgreSQL 18.6 ✔, Git 2.53 ✔, VS Code 1.140 ✔, Docker ✘, adb ✘, Android Studio ✘, scrcpy ✘.
- Verifikasi akhir (3 Okt 2026): Java 21.0.12 ✔ (`JAVA_HOME` → java-21), PostgreSQL 18.6 ✔ (login `templateapp` ke DB `templateapp` berhasil), Docker 29.1.3 ✔ (service aktif & enabled, jalan tanpa sudo, `hello-world` sudah dijalankan), Git 2.53 ✔, VS Code 1.140 ✔, Android Studio 2026.2.1 ✔, Android SDK ✔ (platform android-37, build-tools, platform-tools), scrcpy ✔, `adb devices` → realme RMX3151 (Android 13, API 33) berstatus `device` ✔, `backend/.env` ter-ignore Git ✔.
- Kendala yang ditemui:
  - HP putus-sambung dengan `error -71` di log kernel → penyebabnya sambungan fisik USB; beres setelah kabel/port diganti.
  - Ada dua `adb` (`/usr/bin/adb` dari paket udev dan adb dari SDK) → `platform-tools` SDK dipindah ke **depan** PATH di `~/.bashrc` (cadangan: `~/.bashrc.bak-fase00`).

## Konsep yang dipelajari
- Monorepo : satu repository Git berisi beberapa project (backend + android), sehingga perubahan yang saling terkait bisa di-commit bersama.
- `.gitignore` : daftar pola file yang tidak dilacak Git; dipakai agar rahasia dan file hasil build tidak masuk repo.
- `update-alternatives` : cara Ubuntu memilih versi default sebuah program ketika ada beberapa versi terpasang.
- `JAVA_HOME` : environment variable yang dibaca Maven/Gradle untuk menentukan JDK mana yang dipakai.
- `adb` (Android Debug Bridge) : alat untuk berbicara dengan HP dari laptop (pasang app, lihat log, meneruskan port).

- `journalctl -k` : membaca log kernel; tempat melihat apakah perangkat USB benar-benar terdeteksi dan kenapa putus.
- Urutan `PATH` : shell mencari program dari folder paling kiri; folder yang ditaruh di depan menang jika ada nama program yang sama.

## Latihan untuk Aris
1. Jalankan `ls /usr/lib/jvm/` lalu jelaskan kenapa ada lebih dari satu folder Java.
2. Buat file `coba.env` di root repo, jalankan `git status`, dan perhatikan apakah file itu muncul. Lalu hapus file tersebut.
3. Jalankan `adb shell getprop ro.build.version.sdk` untuk melihat API level HP-mu, pastikan ≥ 26.

## Yang perlu Aris lakukan
- Semua langkah persiapan sudah selesai dan terverifikasi.
- Opsional: hapus emulator & system image lewat SDK Manager untuk menghemat ±3,6 GB.
- Buka terminal baru (atau `source ~/.bashrc`) agar `adb` dari SDK yang dipakai.

## Rencana fase berikutnya
- Fase 01: inisialisasi Spring Boot (dependency bagian 4.3), `backend/.vscode/`, profile dev/prod, `AppProperties`, migrasi Flyway V1–V6, `GlobalExceptionHandler`, Swagger UI.
