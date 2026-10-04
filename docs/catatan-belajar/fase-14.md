# Fase 14: Dashboard Pembuat Website di Android

Sumber rancangan: [`docs/rancangan/alur-pembuatan-website.md`](../rancangan/alur-pembuatan-website.md). Penjelasan cara kerjanya ada di [`dokumentasi-project.md` bab 24](../dokumentasi-project.md#24-dashboard-pembuat-website-di-android).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | Room 2.8.5 (+ `room.schemaLocation`), core-testing untuk androidTest, shim sqlite opsional |
| `data/local/ProjectEntity`, `ProjectDao`, `ProjectCounts`, `Converters`, `AppDatabase`, `core/di/DatabaseModule` | Tabel `projects` di HP (metadata), query filter/cari/urutan/jumlah per status |
| `data/model/ProjectMode`, `ProjectStatus`, `ProjectFilter`, `data/repository/ProjectRepository` | Mode & status project, filter tab Project, ganti nama/duplikat/hapus |
| `data/remote/api/GalleryApi`, `dto/GalleryPageDto`, `TemplateEventDto`, `data/repository/GalleryRepository` | Galeri publik + event "Dilihat" |
| `core/storage/SessionStore` (`getInstallId`), `UserApi`/`UserRepository` (`updateCreatorProfile`) | ID instalasi; edit profil pembuat website |
| `ui/creator/CreatorDashboardFragment`, `CreatorTabsAdapter`, `CreateProjectSheet`, `StartOptions`, `menu_creator_tabs.xml` | Shell: ViewPager2 + bottom nav + tombol + di tengah |
| `ui/creator/CreatorHomeFragment`, `CreatorHomeViewModel`, `Recommendations` | Beranda: Mulai, Lanjutkan project, Template untuk anda, Panduan |
| `ui/creator/ProjectsFragment`, `ProjectsViewModel`, `ProjectAdapter`, `ProjectSmallAdapter`, `ProjectUi` | Tab Project + menu ⋮ (ganti nama, duplikat, export "segera hadir", hapus) |
| `ui/creator/GalleryFragment`, `GalleryViewModel`, `GalleryListState`, `GalleryAdapter`, `TemplateOpener` | Tab Template + kartu kecil Beranda; klik template → catat "Dilihat" → editor |
| `ui/creator/CreatorProfileFragment`, `CreatorProfileViewModel`, `CreatorProfileEditFragment`, `CreatorProfileEditViewModel` | Profil tamu/login, ringkasan dari Room, edit profil |
| `ui/editor/EditorNav`, `ComingSoonEditorFragment` (+ `TemplateEditor`, `CustomEditor`) | Dua halaman editor "Segera hadir" + aturan editor mana yang dibuka |
| `ui/guide/Guide`, `GuideStore`, `GuideFragment` (dipindah dari `ui/provider`) | Panduan provider & pembuat website (ZIP online, beda mode) |
| `ui/common/KeyboardAwareBottomBar`, `RelativeTime` (dipindah) | Dipakai kedua dashboard |
| `ui/provider/ProviderDashboardFragment` | Avatar → tab Profil; `ProfileSheet` + `sheet_profile.xml` dihapus |
| `ui/settings/SettingsFragment`, `DebugMenu`, `core/di/DebugMenuModule`, `src/debug/.../SampleProjects*`, `DebugBindings` | Grup Akun hanya untuk user login; grup DEBUG isi/hapus project contoh (debug saja) |
| `ui/onboarding/OnboardingUi` | Pemetaan chip tujuan website ↔ nilai backend dipakai bersama |
| Test | `RecommendationsTest`, `ProjectRepositoryTest`, `ProjectEntityTest`, `ProjectFilterTest`, `SampleProjectsTest` (testDebug), `ProjectDaoTest` (androidTest, di HP) |

## Alasan keputusan
- Room → disebut eksplisit di rancangan bagian 4.6 (mengesampingkan "jangan pasang Room" di AGENTS 4.2). Versi 2.8.5 karena masih mendukung Java `annotationProcessor`; Room 3 baru alpha dan khusus Kotlin.
- Tombol + berupa `MaterialButton` di atas slot kosong bottom navigation → BottomNavigationView tidak punya tombol aksi; cara ini tetap memakai komponen standar dan gaya monokrom.
- Galeri list satu kolom, "Dilihat" saat template diklik, avatar → tab Profil → keputusan Aris.
- Ganti nama tidak mengubah `updated_at`; duplikat dari project yang sudah diexport menjadi "Siap export" → keputusan saya, karena rancangan tidak menyebutnya (alasan di bab 24).
- Menu DEBUG lewat `Optional<DebugMenu>` + source set `src/debug/` → kode contoh tidak mungkin ikut ke rilis (alternatif yang tidak dipilih: `if (BuildConfig.DEBUG)`, yang tetap menyertakan kodenya di APK rilis).
- Query Room diuji di HP (androidTest) → JVM laptop tidak punya SQLite Android. Dijalankan lewat `am instrument` agar data di HP tidak terhapus.
- Shim sqlite di luar repo + properti opsional → masalah "musl" khusus path laptop Aris; laptop lain tidak terpengaruh.
- Kategori & jumlah download di kartu galeri digabung menjadi satu baris label → dua kolom teks terpisah membuat jumlah download terlipat aneh pada kategori panjang ("Pribadi / Portofolio").

## Cara menjalankan & mengetes
1. Backend dengan data demo (README "Data demo provider"), `adb reverse tcp:8080 tcp:8080`, lalu Run.
2. Tab Beranda (tamu atau login): kartu Mulai, Template untuk anda, Panduan.
3. *Profil → Pengaturan → DEBUG → Isi project contoh* → Beranda menampilkan Lanjutkan project; tab Project berisi 7 project.
4. Tab Project: cari, chip status, urutan, menu ⋮ (ganti nama, duplikat, export, hapus).
5. Tombol + → Custom → Editor custom "Segera hadir"; tab Template → klik template → Editor template.
6. Test: `./gradlew testDebugUnitTest lint`, dan test DAO di HP (README "Test & pemeriksaan Android").

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 47 test** (12 test baru).
- `ProjectDaoTest` di HP: **lulus, 5 test** (urutan, filter & pencarian `%`, jumlah per status, ganti nama/hapus/project contoh, batas project terbaru).
- `./gradlew lint`: **0 masalah**. Compile debug & release berhasil.
- Uji di HP (akun demo):
  - Beranda kosong (Mulai besar + Template untuk anda + Panduan) dan Beranda berisi project (Lanjutkan project + Mulai satu baris).
  - DEBUG isi project contoh: 7 project.
  - Tab Project: badge status, label mode, waktu, "n isian belum diisi"; menu ⋮ (Export nonaktif untuk Draft); duplikat, ganti nama, hapus dengan konfirmasi.
  - Tombol + → bottom sheet → Custom → editor custom; galeri → klik template → editor template.
  - Profil login: info, ringkasan, beralih mode, Pengaturan dengan grup DEBUG.
- Masalah yang ditemukan saat uji di HP dan sudah diperbaiki:
  - Hasil duplikat tidak terlihat karena disisipkan di atas tanpa menggulir → daftar menggulir ke atas.
  - Tombol "Pakai template" terlipat 2 baris → gaya tombol ringkas.
  - Jumlah download di kartu galeri terlipat aneh → satu baris label.
  - Tanda kutip di dialog Hapus hilang → tanda kutip “ ”.
  - Tombol Simpan di Edit profil hanya setengah lebar → selebar layar.
  - Garis pemisah grup Akun di Pengaturan tertinggal saat tamu → ikut disembunyikan.
- Uji lanjutan di HP:
  - Tamu: Beranda (Mulai besar), Profil versi tamu, Pengaturan tanpa grup Akun.
  - Klik template sebagai tamu → `template_events` bertambah 1 (`type=view`, `install_id` terisi, `user_id` kosong). Saat login sebagai pemilik template (akun demo), angka tidak bertambah, sesuai aturan backend.
  - Galeri offline (backend mati) → error + Coba lagi, dan berhasil setelah backend menyala.
  - `dummy3`: Edit profil (tujuan website → Sekolah) tersimpan dan langsung tampil di Profil.
  - Screenshot terang & gelap: 02 (Beranda tamu), 16–20 di `docs/screenshots/`.
- Catatan perilaku: project tersimpan **per HP**, belum per akun. Rancangan 4.6 belum punya kolom pemilik, sehingga akun lain di HP yang sama melihat project yang sama. Pemindahan project tamu ke akun memang ditunda (AGENTS 2.2). Ini perlu diputuskan bersama diskusi editor.

## Tambahan: pilihan tema di dalam app (permintaan Aris)
- `core/storage/ThemeStore` + `TemplateApp` + Pengaturan → Tampilan → Tema (Ikuti sistem / Terang / Gelap).
- Uji di HP: HP dalam mode gelap, Tema → Terang → app langsung terang dan tetap di Pengaturan. Setelah app ditutup lalu dibuka lagi, app tetap terang.

## Tambahan: animasi loading pahlawan terbang (permintaan Aris)
- Versi pertama berupa pesawat kertas. Aris kurang suka dan meminta Superman yang sedang terbang. Karena Superman adalah karakter berhak cipta DC Comics, yang dibuat adalah **sosok pahlawan berjubah umum** (tanpa logo/kostum khas).
- `android/tools/generate_lottie.py` → `loading()`: siluet bergaya "pil", jubah berkibar (`animated_line`), garis angin (`sampled`), dan skala layer 120%. Hasilnya `res/raw/loading.json` (file animasi lain tidak berubah).
- Contoh `StateView` loading ditambahkan di bagian bawah Katalog komponen (build debug) untuk mengecek animasinya.
- Uji di HP: animasi berjalan dan berulang mulus; warnanya mengikuti tema.

## Tambahan: loading tampil minimal 2 detik (permintaan Aris)
- `StateView` menahan animasi loading minimal `loading_min_duration_ms` (2000 ms, `res/values/integers.xml`) sebelum menampilkan isi, error, atau keadaan kosong. Dipakai Beranda provider, Profil provider, dan Detail template (`state.hide(() -> { tampilkan isi })`).

## Tambahan: loading tiga bagian & layar awal (permintaan Aris)
- `loading.json`: lingkaran berputar → berubah jadi pahlawan → terbang (diulang) → melaju ke kanan. Diatur `ui/common/HeroLoading`.
- Layar awal (`StartupFragment` + `fragment_startup.xml`) menampilkan animasi ini, lalu dashboard mode terakhir naik dari bawah (`res/anim/slide_up_in.xml`, `fade_out.xml`, action di `nav_graph.xml`). Splash sistem tidak ditahan lagi.
- `StateView`: setelah loading, isi layar juga naik dari bawah (`hide(content, bind)`).
- Prefetch Beranda provider di `StartupViewModel`, sehingga loading tidak muncul dua kali.
- Uji di HP (akun Aris, mode provider): rangkaian screenshot saat app dibuka menunjukkan splash → lingkaran → pahlawan → melaju ke kanan → Beranda provider, tanpa loading kedua.

## Konsep yang dipelajari
- Room: Entity, DAO, Database, `TypeConverter`, LiveData yang otomatis diperbarui, `switchMap`.
- Annotation processor & kenapa ia bisa gagal karena lingkungan build (kasus "musl").
- Source set `debug` dan `@BindsOptionalOf` untuk fitur khusus build debug.
- Instrumented test (androidTest) vs unit test.
- Lottie `setMinAndMaxFrame` untuk memutar sebagian animasi; animasi perpindahan layar di Navigation (`enterAnim`/`exitAnim`).
- `AppCompatDelegate.setDefaultNightMode`: memaksa mode terang/gelap untuk app saja.
- Tombol aksi di tengah bottom navigation dengan `clipChildren="false"` dan `translationY`.

## Latihan untuk Aris
1. Buka `ProjectDao.java`, ubah urutan `observeByName` menjadi `DESC`, jalankan `ProjectDaoTest` di HP. Test mana yang gagal? Kembalikan lagi.
2. Isi project contoh, lalu ganti nama "Kantor Desa Sukamaju". Apakah teks "Diedit … lalu"-nya berubah? Cari alasannya di `ProjectRepository.rename`.
3. Di laptop, buka `app/schemas/com.aris.templateapp.data.local.AppDatabase/1.json`. Cocokkan kolomnya dengan field di `ProjectEntity.java`.

## Yang perlu Aris lakukan
- Klik **Sync Now** di Android Studio (ada library baru: Room).
- Buka kunci HP supaya sisa pengujian dan screenshot bisa dilanjutkan.

## Rencana fase berikutnya
- Menunggu diskusi Aris: editor (template & custom mode), kerangka section, format project JSON, export ZIP, serta segmen Upload provider (bagian 9 kedua rancangan).
