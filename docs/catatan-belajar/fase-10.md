# Fase 10: Polesan & cek akhir

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `res/drawable/ic_add`, `ic_close`, `ic_lock`, `ic_login`, `ic_refresh`, 4 string | Dihapus karena tidak dipakai. `./gradlew lint` sekarang **0 error, 0 warning** |
| `res/color/selector_button_outlined_text.xml`, `values/styles.xml` | Tombol sekunder nonaktif tampil abu-abu (sebelumnya terlihat sama dengan tombol aktif) |
| `ui/profile/ProfileSheet.java`, `ProfileViewModel.java` | Error koneksi saat beralih mode diberi tombol **Coba lagi** |
| `ui/onboarding/OnboardingUi.java`, `CreatorFormFragment.java`, `ProviderFormFragment.java` | Error koneksi saat mengirim form diberi tombol **Coba lagi** |
| `docs/screenshots/*.png` (22 file) | 11 layar × mode terang & gelap, diambil dari HP asli dengan akun dummy |
| `README.md` | Bagian "Tampilan" (tabel screenshot), status Fase 10 |
| `docs/dokumentasi-project.md` | Status dokumen |

## Alasan keputusan
- Screenshot memakai akun dummy → email pribadi Aris tidak ikut masuk ke repo.
- Screenshot diperkecil ke lebar 540px → total ±1,6 MB, repo tetap ringan.
- Mode gelap/terang HP diganti dari laptop (`adb shell cmd uimode night yes/no`) → setiap layar diambil dalam dua mode tanpa mengutak-atik HP.
- "Coba lagi" menjalankan ulang aksi terakhir → spesifikasi skenario 12: aksi online yang gagal karena koneksi wajib punya tombol coba lagi.
- Skenario 4 diuji dengan umur access token 1 menit lewat environment variable `JWT_ACCESS_TTL_MINUTES=1` saat menjalankan backend → `.env` tidak perlu diubah dan tidak ada risiko lupa mengembalikan.

## Cara menjalankan & mengetes
1. `cd backend && ./mvnw test` lalu `cd android && ./gradlew assembleDebug testDebugUnitTest lint`.
2. Jalankan backend + `./tools/keep-adb-reverse.sh` + `./gradlew installDebug`, lalu ikuti tabel skenario di bawah.
3. Skenario 4: hentikan backend, jalankan ulang dengan `JWT_ACCESS_TTL_MINUTES=1 ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`, login di app, tunggu lebih dari 1 menit, lalu lakukan aksi online.

## Hasil tes

### Test otomatis
- Backend `./mvnw test`: **63 test lulus** (tidak ada perubahan backend sejak run terakhir).
- Android `./gradlew testDebugUnitTest`: **26 test lulus**.
- Android `./gradlew lint`: **0 error, 0 warning**.
- Pemeriksaan layout: tidak ada warna hex, ukuran dp/sp, atau teks yang ditulis langsung di layout maupun di kode.

### Skenario bagian 13.2 di HP asli (realme RMX3151, Android 13)
| No | Skenario | Hasil |
|---|---|---|
| 1 | Pasang & buka pertama kali | ✅ Intro 3 halaman → dashboard tamu dengan tombol Masuk; dibuka lagi tanpa intro (Fase 06) |
| 2 | Daftar dengan email | ✅ Akun dibuat (Fase 07). Akun yang belum onboarding dibawa ke Pilih peran → form → dashboard dengan avatar (diuji Fase 09 dengan akun Google baru dan `dummy2`; daftar email memakai aturan navigasi yang sama, `HomeNavigator`) |
| 3 | Tutup & buka setelah login | ✅ Langsung ke dashboard mode terakhir (Fase 07) |
| 4 | Access token kedaluwarsa | ✅ TTL 1 menit, tunggu 75 detik, beralih mode tetap berhasil; database mencatat 1 refresh token dirotasi diam-diam |
| 5 | Masuk Google akun baru | ✅ Akun "Aris123 Muslim" dibuat otomatis, nama dari Google, lalu onboarding (Fase 08–09) |
| 6 | Akun Google lalu masuk lewat form email | ⚠️ Diuji backend (`SocialAuthIntegrationTest.googleAccountCannotUseEmailLogin`) dan teks Android ada (`error_use_social_login`); **belum dicoba di HP** |
| 7 | Google → GitHub email sama → sambung → GitHub berikutnya ke akun sama | ✅ Lengkap: dialog penyambungan (Fase 08) dan login GitHub setelah keluar langsung ke akun yang sama (Fase 09) |
| 8 | Email GitHub berbeda/privat | ⚠️ Diuji backend (`gitHubWithPrivateEmailCreatesSeparateAccountThatCanBeLinkedManually`); **tidak bisa dicoba di HP** dengan akun GitHub Aris karena emailnya sama dengan Google. Layar Metode login terhubung (Sambungkan/Lepaskan) sudah dicek tampilannya |
| 9 | Jadi penyedia template | ✅ Form provider → Dashboard Provider + banner "Akunmu sedang diverifikasi." |
| 10 | Beralih mode, tutup, buka | ✅ App membuka mode terakhir |
| 11 | Status provider via SQL | ✅ pending/rejected/approved/suspended semuanya sesuai (Fase 07) |
| 12 | Offline saat sudah login | ✅ App terbuka memakai data tersimpan; aksi online menampilkan "Tidak ada koneksi…" + **Coba lagi** |
| 13 | Keluar | ✅ Kembali ke dashboard tamu + "Kamu sudah keluar."; masuk lagi berjalan normal |
| 14 | Mode gelap | ✅ 11 layar dicek & disimpan sebagai screenshot terang/gelap, termasuk animasi Lottie |
| 15 | Tanpa `adb reverse` | ✅ Error koneksi jelas + Coba lagi; setelah `adb reverse` dipasang lagi, Coba lagi berhasil |

### Checklist bagian 14
- [x] Tidak ada kode/tabel/endpoint fitur bagian 2.2 (galeri, editor, upload, panel admin, dll.)
- [x] Tidak ada rahasia di repo; `.env.example` dan contoh `local.properties` ada di README
- [x] Semua test backend lulus (63)
- [x] `assembleDebug`, `testDebugUnitTest` (26), `lint` (0 error, 0 warning) berhasil
- [~] Project `android/` terbuka tanpa error di Android Studio (dikonfirmasi Aris di Fase 05; **belum dicek ulang** setelah Fase 06–10). `backend/` dari VS Code: task & launch sudah disiapkan, **belum dicoba Aris**
- [x] App jalan di HP asli (debug) dengan backend lokal lewat `adb reverse`
- [x] Tamu → Masuk → onboarding → dashboard sesuai mode
- [x] Penyambungan Google ↔ GitHub di HP; email ↔ Google lewat test backend
- [x] Refresh token diam-diam (skenario 4); sesi berakhir ditangani (kembali ke tamu + pesan)
- [x] Semua layar monokrom, border tipis, tanpa bayangan, font Geist
- [x] Tidak ada warna/ukuran/teks langsung di layout (diperiksa dengan grep)
- [x] Mode gelap terbaca di semua layar, termasuk Lottie
- [x] Tidak ada aset/logo Next.js/Vercel (animasi dibuat sendiri; logo Google/GitHub dari pedoman resmi masing-masing)
- [~] README memenuhi bagian 11; **belum dicoba diikuti dari nol** oleh orang lain (cocok untuk teman pertama yang memakai `panduan-kolaborator.md`)
- [x] Catatan belajar `fase-00.md` sampai `fase-10.md`
- [~] Skenario 13.2: 13 dari 15 berhasil di HP; skenario 6 dan 8 diuji lewat test backend (lihat tabel)
- [x] Screenshot terang & gelap di `docs/screenshots/` dan tampil di README

## Konsep yang dipelajari
- Uji end-to-end di HP asli: menggabungkan backend, `adb reverse`, dan pemeriksaan database untuk membuktikan alur benar-benar terjadi (mis. rotasi refresh token).
- Lint `UnusedResources`: resource yang tidak dipakai menambah ukuran APK dan membingungkan pembaca kode.
- State `enabled` pada tombol butuh warna berbeda (selector), bukan hanya logika.
- Environment variable lebih kuat daripada file konfigurasi Spring, jadi berguna untuk pengaturan sementara saat uji.

## Latihan untuk Aris
1. Coba skenario 6 sendiri: Keluar, lalu di layar Masuk isi `aris123muslim@gmail.com` + password apa saja. Pesan apa yang muncul?
2. Buka `docs/screenshots/` dan bandingkan `09-menu-profil-terang.png` dengan `-gelap.png`. Token warna mana yang berbeda? (lihat `values/colors.xml` vs `values-night/colors.xml`)
3. Jalankan `./gradlew lint`, lalu buka `app/build/reports/lint-results-debug.html` di browser. Hapus satu `contentDescription` di sebuah layout, jalankan lint lagi, dan lihat apa yang muncul. Setelah itu kembalikan.

## Yang perlu Aris lakukan
- Buka `android/` di Android Studio (Sync) dan `backend/` di VS Code (F5 "Backend: debug (dev)") untuk memastikan keduanya tanpa error.
- Coba skenario 6 di HP (latihan 1).
- Push repo ke GitHub private dan undang teman (`docs/panduan-kolaborator.md` bagian 0).

## Rencana berikutnya
- Cakupan saat ini (bagian 2.1) selesai. Fitur berikutnya (galeri template, editor, export, upload provider, panel admin) menunggu diskusi alur dari Aris, sesuai bagian 2.2.
