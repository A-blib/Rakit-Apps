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
- Belum selesai diuji di HP (layar terkunci): "Dilihat" sebagai tamu, Profil versi tamu, Edit profil pembuat website, mode terang, screenshot. (Saat login sebagai akun demo, "Dilihat" memang tidak bertambah karena event dari pemilik template diabaikan backend.)

## Konsep yang dipelajari
- Room: Entity, DAO, Database, `TypeConverter`, LiveData yang otomatis diperbarui, `switchMap`.
- Annotation processor & kenapa ia bisa gagal karena lingkungan build (kasus "musl").
- Source set `debug` dan `@BindsOptionalOf` untuk fitur khusus build debug.
- Instrumented test (androidTest) vs unit test.
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
