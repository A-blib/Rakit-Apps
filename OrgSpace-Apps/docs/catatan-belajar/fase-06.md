# Fase 06: Startup & dashboard "Segera hadir"

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `values/themes.xml` (`Theme.App.Starting`), `drawable/ic_splash*.xml`, `AndroidManifest.xml` | Splash `core-splashscreen`: latar `color_background` + ikon jendela browser |
| `MainActivity.java` | `installSplashScreen()`, splash ditahan sampai layar pertama diputuskan |
| `ui/startup/StartupDecision.java` | Fungsi murni keputusan layar pertama (bagian 6.1) |
| `ui/startup/StartupViewModel.java`, `StartupFragment.java` | Urutan pengecekan (intro → token → `/users/me` → cache) dan perpindahan layar |
| `data/repository/UserRepository.java` | `fetchMe()` (backend + simpan cache), `getCachedUser()`, `hasSession()` |
| `ui/intro/IntroFragment.java`, `IntroAdapter.java`, `fragment_intro.xml`, `item_intro_page.xml`, `bg_page_dot.xml` | Intro 3 halaman, indikator, Lewati/Lanjut/Mulai |
| `ui/creator/CreatorDashboardFragment.java` + layout | Dashboard sesuai 9.4: badge, judul, teks, Lottie, tombol nonaktif, banner "ditangguhkan" |
| `ui/provider/ProviderDashboardFragment.java` + layout | Dashboard Provider + banner status (pending/rejected/suspended) |
| `ui/common/CurrentUserViewModel.java`, `AppBarAccount.java`, `view_app_bar_account.xml`, `bg_avatar.xml` | Tombol Masuk (tamu) / avatar (login) di app bar |
| `ui/common/LottieTint.java` | Mewarnai ulang Lottie mengikuti tema |
| `android/tools/generate_lottie.py` → `res/raw/*.json` | 6 animasi garis monokrom: coming_soon, empty, loading, intro_build, intro_customize, intro_share |
| `navigation/nav_graph.xml` | startup → intro / dashboard creator / dashboard provider, dengan `popUpTo` |
| `libs.versions.toml`, `app/build.gradle.kts` | Library Lottie 6.7.1 |
| Backend `UserResponse` + `UserService` + `DummyDataSeederTest` | Field baru `providerRejectionReason` untuk banner "ditolak: {alasan}" |
| Test: `StartupDecisionTest` | 6 unit test cabang 6.1 |

## Alasan keputusan
- Keputusan layar pertama dipisah menjadi `StartupDecision` (fungsi murni) → bisa diuji tanpa HP (alternatif: semua logika di ViewModel, tidak dipilih karena butuh Android/Hilt untuk menguji).
- ViewModel startup milik Activity → splash (di Activity) dan perpindahan layar (di Fragment) memakai keputusan yang sama.
- Layar startup berupa Fragment kosong di bawah splash → Navigation Component tetap menjadi satu-satunya pengatur layar; startup dibuang dari back stack dengan `popUpTo`.
- Navigasi memakai ID aksi + `Bundle`, bukan Safe Args → Safe Args adalah plugin tambahan di luar daftar library bagian 4.
- Offline dengan token tersimpan memakai salinan user → app tetap terbuka saat offline (skenario 12).
- `ROLE_SELECT` sementara diarahkan ke Dashboard Pembuat Website → layar pilih peran dibuat di Fase 09. Perilaku sementara ini dikomentari di `StartupFragment`.
- Animasi Lottie dibuat dari skrip sendiri → instruksi melarang aset pihak lain dan meminta gaya garis monokrom. Skripnya ikut di repo agar bisa diubah.
- Lottie diwarnai lewat `COLOR_FILTER` pada semua layer → satu baris kode untuk semua animasi, dan warnanya ikut mode terang/gelap.
- Backend menambah `providerRejectionReason` → banner bagian 6.6 butuh alasan penolakan, yang sebelumnya tidak ada di `UserResponse`. Field hanya terisi jika status `rejected`.
- Avatar berupa huruf depan nama → `avatarUrl` belum dipakai, karena memuat gambar dari internet butuh library (Glide) yang ada di daftar "jangan pasang sekarang".

## Cara menjalankan & mengetes
1. `cd android && ./gradlew testDebugUnitTest lint installDebug`
2. Simulasi pertama kali dibuka: `adb uninstall com.aris.templateapp`, lalu `./gradlew installDebug` dan buka app.
3. Geser atau tekan Lanjut sampai halaman 3, lalu tekan Mulai → Dashboard Pembuat Website dengan tombol Masuk.
4. Tutup app sepenuhnya dan buka lagi → langsung ke dashboard tanpa intro.
5. Coba mode gelap/terang HP: animasi ikut berganti warna.

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 16 test** (StartupDecisionTest 6, ApiErrorParserTest 5, AuthInterceptorTest 2, UserMapperTest 2, EventTest 1).
- Backend `./mvnw test`: **lulus, 63 test**. `DummyDataSeederTest` kini juga memeriksa `providerRejectionReason`.
- `./gradlew lint`: 0 error. Saran `PluralsCandidate` diabaikan secara eksplisit (Bahasa Indonesia tidak punya bentuk jamak). Sisanya hanya `UnusedResources` (28) untuk layar Fase 07–09.
- Uji di HP (realme RMX3151, Android 13, mode gelap, lewat wireless debugging):
  - Pasang baru → splash → intro halaman 1–3 (animasi berjalan, indikator berpindah, "Mulai" di halaman 3) → Dashboard Pembuat Website dengan tombol **Masuk**, badge, judul, Lottie, dan tombol nonaktif ✅
  - Tutup lalu buka lagi → langsung ke dashboard, intro tidak muncul (skenario 1) ✅
- Kendala yang ditemui dan diperbaiki: crash saat dibuka (`StartupViewModel` diminta sebelum `super.onCreate`) → ViewModel diambil setelah `super.onCreate`.
- **Belum diuji di HP:** Dashboard Provider dan banner "ditangguhkan" butuh login, yang baru tersedia di Fase 07. Keputusannya sudah diuji unit test.

## Konsep yang dipelajari
- SplashScreen API : `installSplashScreen()` + `setKeepOnScreenCondition`.
- Fungsi murni : hasil hanya bergantung pada input, sehingga mudah diuji.
- ViewModel scope : milik Fragment vs milik Activity (dipakai bersama).
- Back stack & `popUpTo` : membuang layar lama agar tombol kembali tidak kembali ke sana.
- ViewPager2 + Adapter.
- Selector `state_selected` : tampilan berubah sesuai keadaan View.
- Lottie trim path & dynamic properties.

## Latihan untuk Aris
1. Di `StartupDecisionTest`, tambahkan test: user `PROVIDER` dengan status `REJECTED` dan mode provider harus ke Dashboard Provider. Jalankan dengan ikon ▷ di Android Studio.
2. Ubah teks `intro_title_2` di `strings.xml`, jalankan ulang app (uninstall dulu supaya intro muncul), lalu lihat hasilnya.
3. Buka `tools/generate_lottie.py`, ubah `STROKE = 6` menjadi `4`, jalankan `python3 tools/generate_lottie.py`, lalu install ulang. Apa yang berubah?

## Yang perlu Aris lakukan
- Tidak ada yang wajib. Kalau mau mencoba, ikuti "Cara menjalankan & mengetes" di atas.

## Rencana fase berikutnya
- Fase 07: layar Masuk & Daftar email sesuai 9.4, validasi form, pesan error 6.2, `AuthRepository`, simpan token, lalu uji daftar & masuk ke backend lokal dari HP lewat `adb reverse`.
