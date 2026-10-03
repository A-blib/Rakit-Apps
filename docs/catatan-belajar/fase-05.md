# Fase 05: Fondasi Android & desain

## Yang dikerjakan
| File | Fungsi |
|---|---|
| (koreksi template) | Wizard menghasilkan template **Empty Activity (Compose + Kotlin)**. Atas persetujuan Aris, project diubah ke Java + XML Views: file `.kt`, `ui/theme/`, plugin & library Compose dihapus. Pengaturan wizard lain (AGP 9.4.1, SDK 37, Kotlin DSL, version catalog) tetap dipakai |
| `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts` | Semua library bagian 4.1 (versi stabil terbaru), Java 17, ViewBinding, BuildConfig `API_BASE_URL` & `GOOGLE_WEB_CLIENT_ID`, komentar penjelas sintaks Kotlin DSL |
| `gradle.properties`, `gradle/gradle-daemon-jvm.properties`, wrapper | Memori Gradle 2 GB, daemon Gradle memakai JDK 21 (wizard memasang 25), Gradle 9.8.0 |
| `res/values{,-night}/colors.xml`, `dimens.xml`, `type.xml`, `styles.xml`, `themes.xml`, `color/selector_*.xml` | Token desain 9.2 + gaya komponen 9.3 + tema Material 3 |
| `res/font/` + `assets/licenses/geist-OFL.txt` | Geist Sans (Regular/Medium/SemiBold/Bold) & Geist Mono (Regular/Medium) v1.7.2 + lisensi OFL |
| `res/drawable/ic_*.xml` (22 ikon) | Material Symbols Outlined, dikonversi dari SVG resmi |
| `res/drawable/bg_*.xml` | Latar badge, banner status (4 warna semantik), app bar bergaris bawah |
| `res/values/strings.xml` | Teks umum, pesan error 6.2, teks dashboard |
| `TemplateApp.java`, `MainActivity.java`, `activity_main.xml`, `nav_graph.xml` | Hilt, satu Activity + NavHost, edge-to-edge, snackbar "sesi berakhir" |
| `core/di/` | `NetworkModule` (OkHttp, Retrofit, API), `AppModule` (Gson), qualifier `RefreshClient` |
| `core/network/` | `AuthInterceptor`, `TokenAuthenticator`, `ApiErrorParser` |
| `core/storage/` | `TokenStorage` (AES-GCM + Android Keystore), `SessionStore` |
| `core/util/` | `Resource`, `Event`, `AppExecutors` |
| `data/` | Model (`User`, `UserRole`, `ProviderStatus`, `LoginMethod`, `ApiError`), DTO, `AuthApi`, `UserApi`, `UserMapper` |
| `ui/creator/CreatorDashboardFragment` + layout | Kerangka awal dashboard "Segera hadir" (dilengkapi di Fase 06) |
| `ui/common/StatusBannerView` + `view_status_banner.xml` | Komponen banner status yang dipakai ulang |
| `src/debug/...ComponentCatalogActivity` + layout/strings/styles | Katalog komponen khusus debug, dengan ikon launcher sendiri |
| `src/{main,debug}/res/xml/network_security_config.xml` | HTTP polos hanya ke localhost di build debug |
| `AndroidManifest.xml`, `data_extraction_rules.xml`, `keepRules/rules.keep` | `singleTask`, izin internet, backup dimatikan & token dikecualikan, aturan R8 untuk DTO |
| Test: `ApiErrorParserTest`, `AuthInterceptorTest`, `EventTest`, `UserMapperTest` | 10 unit test |
| `README.md`, `docs/dokumentasi-project.md` | Panduan menjalankan app di HP, versi library Android, troubleshooting; bab 13–16 dokumentasi |

## Alasan keputusan
- Template diubah sendiri, bukan wizard ulang → lebih cepat dan pengaturan wizard yang sudah benar tetap dipakai (alternatif: Aris membuat ulang dengan "Empty Views Activity"; ditawarkan, Aris memilih diubah sendiri).
- Gradle daemon JDK 21 → instruksi meminta JDK yang sama untuk backend, terminal, dan Android Studio (wizard memasang 25).
- Gradle 9.8.0 (dinaikkan dari 9.6.0) → lint memberi tahu ada versi stabil lebih baru, dan aturan project memakai versi stabil terbaru.
- `TextAppearance` menunjuk file font per ketebalan → pemilihan ketebalan dari satu keluarga font (`textFontWeight`) baru penuh di Android 9; minSdk 26.
- Warna ikon status bar diatur `EdgeToEdge.enable()` → atribut `windowLightNavigationBar` butuh API 27, dan EdgeToEdge sudah otomatis menyesuaikan mode terang/gelap.
- Fokus input = garis biru 2dp → spesifikasi meminta "border kuat + cincin biru"; Material hanya bisa satu warna garis per keadaan, jadi cincin biru yang dipilih karena paling jelas menandai fokus.
- `TokenAuthenticator` `synchronized` + memakai client refresh terpisah → mencegah refresh ganda (yang di backend dianggap pencurian) dan mencegah putaran tanpa akhir.
- Saat refresh gagal karena offline, sesi **tidak** dihapus → user tetap login dan app bisa dibuka offline (skenario 12). Sesi hanya dihapus kalau backend menolak refresh token (401/400).
- Cache user disimpan sebagai JSON DTO → nama field DTO dijaga R8, sehingga cache tetap terbaca setelah app di-update.
- `allowBackup="false"` + token dikecualikan dari pemindahan data → token terenkripsi kunci Keystore HP ini, tidak berguna di HP lain.
- Katalog komponen di `src/debug` dengan launcher sendiri → tidak ikut di build rilis dan tidak mengganggu alur app.
- `CreatorDashboardFragment` dibuat sekarang sebagai layar awal sementara → syarat fase ini "app terbuka di HP" butuh satu layar; Fase 06 menambahkan startup, intro, dan isinya.
- Library Kotlin bawaan AGP 9 tidak dimatikan → opsi `android.builtInKotlin=false` sudah deprecated, dan library androidx sendiri sudah memakai Kotlin stdlib.

## Cara menjalankan & mengetes
1. `cd android && ./gradlew assembleDebug testDebugUnitTest lint`
2. `./gradlew installDebug`, lalu buka **Template App** dan **Katalog komponen** di HP.
3. Di katalog, tekan **Ganti terang / gelap**, lalu periksa semua komponen di kedua mode.
4. Buka folder `android/` di Android Studio → tunggu Sync → tidak ada error merah.

## Hasil tes
- `./gradlew assembleDebug`: **berhasil**, tanpa warning compile.
- `./gradlew testDebugUnitTest`: **lulus, 10 test** (ApiErrorParserTest 5, AuthInterceptorTest 2, UserMapperTest 2, EventTest 1).
- `./gradlew lint`: **0 error**. Warning yang tersisa hanya `UnusedResources` (33): string, ikon, dan ukuran yang disiapkan untuk layar Fase 06–09. Warning ini akan hilang saat layar-layar itu dibuat.
- `./gradlew installDebug` ke realme RMX3151 (Android 13): **terpasang**. `MainActivity` terbuka tanpa crash, proses app berjalan, dan tidak ada error di logcat.
- **Belum diperiksa secara visual:** saat uji, HP terkunci dan layarnya mati, jadi screenshot masih hitam. Tampilan katalog di mode terang/gelap perlu dicek setelah HP dibuka kuncinya.
- **Belum dicek:** membuka project di Android Studio setelah perubahan (perlu Aris).

## Konsep yang dipelajari
- Version catalog & Kotlin DSL : versi library di satu file, dan file build ditulis dengan sintaks Kotlin.
- BuildConfig : konstanta yang dibuat Gradle saat build, bisa berbeda untuk debug dan rilis.
- Source set `main` / `debug` / `test` : kode di `debug/` hanya ikut di build debug.
- Tema & style Material 3 : atribut tema memetakan token desain ke semua komponen sekaligus.
- `values-night/` : resource alternatif untuk mode gelap.
- Hilt `@Module` / `@Provides` / `@Inject` / `@Singleton` / `@Qualifier`.
- OkHttp Interceptor vs Authenticator.
- Android Keystore & AES-GCM.
- Edge-to-edge & window insets.

## Latihan untuk Aris
1. Ubah `color_link` di `values/colors.xml` menjadi warna lain, jalankan app, lalu buka **Katalog komponen**. Komponen mana saja yang ikut berubah? Setelah itu kembalikan nilainya.
2. Di Android Studio, buka `app/build.gradle.kts`, cari `buildConfigField("String", "API_BASE_URL", ...)`, lalu build sekali. Cari class `BuildConfig` (*Navigate → Class* → `BuildConfig`) dan lihat isinya.
3. Baca `TokenAuthenticator.authenticate`, lalu jelaskan dengan kata-katamu sendiri apa yang terjadi kalau dua request mendapat 401 bersamaan.

## Yang perlu Aris lakukan
- **Buka kunci HP** dan biarkan layarnya menyala. Lebih mudah lagi kalau *Opsi pengembang → Tetap aktif (Stay awake)* diaktifkan, supaya layar tidak mati selama HP dicas lewat USB. Dengan begitu agent bisa memeriksa tampilan lewat screenshot.
- Buka folder `android/` di Android Studio. Kalau diminta, klik **Sync Now**, lalu pastikan tidak ada error. **Tutup lagi** sebelum agent menjalankan build dari terminal.
- (Opsional) Tambahkan `GOOGLE_WEB_CLIENT_ID=...` di `android/local.properties` setelah membuat kredensial OAuth.

## Rencana fase berikutnya
- Fase 06: splash (`core-splashscreen`), `StartupViewModel` (alur 6.1), intro 3 halaman (ViewPager2), Dashboard Pembuat Website & Dashboard Provider lengkap sesuai 9.4 dengan animasi Lottie.
