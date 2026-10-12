# Fase R3: Onboarding ruang kerja & pemilih ruang kerja (Android)

Sumber rancangan: [`docs/rancangan/revisi-fondasi-orgspace.md`](../rancangan/revisi-fondasi-orgspace.md) bagian 3, 5, 5.1, 6 (keputusan K1–K4, K10, K18).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `data/remote/api/OrganizationApi.java`, `dto/Organization*Dto`, `CategoriesDto`, `InvitationCodeRequestDto`, `InvitationPreviewDto`, `LastOrganizationRequestDto` | Endpoint R1 yang dipakai app: daftar & buat ruang kerja, kategori, cek/terima kode undangan, simpan ruang kerja terakhir |
| `data/model/Workspace`, `WorkspaceRole`, `OrgCategories`, `InvitationPreview`; `data/mapper/WorkspaceMapper` | Model ruang kerja di app (nama, logo, peran, pribadi/organisasi) dan pengubah DTO → model |
| `data/model/User`, `UserDto`, `UserMapper` | `lastOrganizationId` ditambah; `creatorProfile` (tujuan website & nama organisasi) dihapus |
| `core/storage/SessionStore` | Menyimpan daftar ruang kerja + ruang kerja aktif agar app bar langsung terisi saat offline |
| `data/repository/WorkspaceRepository` | Sumber data ruang kerja: `refresh`, `create`, `preview`, `accept`, `categories` (disimpan sekali), `select` (+ `PATCH /users/me/last-organization`), `clear` saat keluar akun |
| `ui/workspace/WorkspaceChoiceFragment` + `item_choice_card.xml` | Layar "Mulai jejak digital organisasimu dari sini": buat organisasi, gabung, untuk pribadi |
| `ui/workspace/OrganizationFormFragment`, `OrganizationForm`, `CategoryChips` | Form organisasi; kategori muncul setelah jenjang dipilih dan menyesuaikan jenjang (SMP/SMA/SMK vs perguruan tinggi). Validasi di `OrganizationForm` (logika murni, dites) |
| `ui/workspace/JoinOrganizationFragment` | Gabung lewat kode: **Cek kode** → kartu pratinjau (nama, logo, peran) → isi jabatan (opsional) → **Gabung sekarang** |
| `ui/workspace/PersonalSpaceFragment` | Ruang pribadi: cukup nama tampilan (menggantikan `CreatorFormFragment` lama) |
| `ui/workspace/WorkspaceSetupViewModel` | Urutan aman: buat/gabung ruang kerja dulu, baru `POST /onboarding/creator`. Jika langkah kedua gagal, **Coba lagi** tidak membuat organisasi ganda (`retryPendingFinish`) |
| `ui/workspace/WorkspacePickerSheet`, `WorkspaceViewModel`, `WorkspaceUi`, `item_workspace.xml`, `sheet_workspace_picker.xml` | Nama ruang kerja + ▾ di app bar Dashboard pembuat website → sheet daftar ruang kerja (centang yang aktif), "Kelola ruang kerja ini", "Buat atau gabung organisasi". User lama tanpa ruang kerja otomatis dibawa ke layar pilihan |
| `ui/creator/CreatorDashboardFragment`, `CreatorProfileFragment`, `CreatorProfileEdit*` | App bar memakai nama ruang kerja; Profil menampilkan "Ruang kerja aktif"; Edit profil kreator tinggal nama tampilan |
| `ui/onboarding/RoleSelectFragment`, `OnboardingViewModel`, `nav_graph.xml` | "Pembuat website" → layar pilihan ruang kerja |
| `ui/common/ErrorMessages`, `res/values/strings_workspace.xml` | Pesan untuk 14 kode error baru (mis. kode undangan tidak berlaku, organisasi penuh) dan semua teks layar ruang kerja |
| `test/.../WorkspaceMapperTest`, `OrganizationFormTest` | Tes pemetaan peran/tipe dan aturan form (jenjang wajib, tahun selesai ≥ tahun mulai, dll.) |

## Alasan keputusan
- **Ruang kerja dibuat sebelum onboarding diselesaikan** → backend menolak `ONBOARDING_NEEDS_SPACE` jika user belum punya ruang kerja, jadi tidak ada akun "selesai onboarding" tanpa tempat kerja. (Alternatif: satu endpoint gabungan → lebih sedikit request, tapi form organisasi dan gabung lewat kode jadi tercampur di satu endpoint.)
- **Daftar ruang kerja disimpan di `SessionStore`** → nama ruang kerja di app bar langsung tampil saat app dibuka atau offline, lalu diperbarui diam-diam dari server. (Alternatif: Room → berlebihan untuk daftar kecil yang selalu diganti utuh.)
- **Ruang kerja terakhir disimpan di HP dan di server** (`last_organization_id`) → pindah HP tetap membuka ruang kerja yang sama; HP tetap tahu walau `PATCH` gagal (best-effort).
- **Kategori diambil dari `GET /api/meta/categories`**, bukan ditulis di app → daftar kategori bisa bertambah tanpa rilis app baru.
- **Gabung memakai dua langkah (cek → gabung)** → user melihat nama organisasi dan perannya sebelum benar-benar masuk, sehingga salah ketik kode tidak membuatnya masuk ke organisasi yang salah.
- **Kode dinormalkan di backend** (`osis 7k2p` = `OSIS-7K2P`), app hanya menampilkannya kapital → user tidak perlu peduli strip, spasi, atau huruf kecil.

## Cara menjalankan & mengetes
1. Jalankan backend dev (V17 + `OrganizationSeeder` mengisi 3 organisasi dan kode undangan, lihat README → "Ruang kerja dummy").
2. `adb reverse tcp:8080 tcp:8080`, lalu `cd android && ./gradlew installDebug`.
3. Masuk sebagai `dummy1@templateapp.test` / `password123` → **Pembuat website** → **Buat ruang organisasi**. Tekan tombol tanpa mengisi apa pun: muncul pesan wajib isi. Pilih jenjang **SMK**: kategori sekolah muncul. Isi, lalu buat.
4. Ketuk nama organisasi di app bar → **Buat atau gabung organisasi** → **Gabung organisasi** → ketik `abcd-efgh` (tidak berlaku), lalu `osis 7k2p` → pratinjau "OSIS SMA Negeri 1 Bandung · Editor" → **Gabung sekarang**.
5. Buka pemilih lagi: dua ruang kerja, centang di yang aktif. Pindah, tutup app, buka lagi → ruang kerja terakhir tetap terpilih.

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 106 test** (gabungan R3 + R4).
- `./gradlew lint`: **0 masalah**. `./gradlew assembleDebug`: berhasil.
- Uji di HP (realme RMX3151, Android 13, 12 Oktober 2026), skenario 1–5 bagian 11 rancangan:
  - dummy1 (belum onboarding) → pilih peran → layar pilihan → buat organisasi SMK → Dashboard dengan nama organisasi di app bar ✅
  - Validasi form kosong, kategori mengikuti jenjang ✅
  - Kode salah & kedaluwarsa (`PRAM-KA26`) → "Kode undangan ini sudah tidak berlaku…" ✅; `osis 7k2p` (huruf kecil + spasi) → pratinjau benar → bergabung sebagai Editor dengan jabatan ✅
  - Pemilih ruang kerja: dua ruang kerja + peran, centang aktif, pindah ruang kerja ✅
  - Keluar dari semua ruang kerja → app membuka layar pilihan → **Untuk pribadi** → ruang pribadi dibuat, menu Anggota tidak tampil ✅
  - Mode gelap & terang ✅
- Ditemukan & diperbaiki saat uji HP (commit `5f6721c`):
  1. **Crash** saat user tanpa ruang kerja membuka app: `toolbar.setNavigationIcon(0)` mencari resource #0. Diganti `setNavigationIcon(null)`.
  2. Pesan "Nama organisasi wajib diisi" tidak hilang setelah diketik → helper `OnboardingUi.clearErrorOnType(...)`.
  3. Kode undangan yang ditempel/diketik huruf kecil tetap tampil kecil → `InputFilter.AllCaps`.

## Konsep yang dipelajari
- **Back stack Navigation**: `setPopUpTo(dashboard, inclusive=true)` membuang dashboard dari tumpukan, jadi layar berikutnya tidak punya "layar sebelumnya". `getPreviousBackStackEntry() == null` dipakai untuk menyembunyikan panah kembali.
- **Resource ID 0**: angka 0 bukan "tanpa gambar", melainkan ID yang tidak ada. Method yang menerima `@DrawableRes int` akan crash; pakai versi yang menerima `Drawable` dengan `null`.
- **Urutan request yang aman diulang (idempotent dari sisi user)**: simpan hasil langkah pertama (organisasi yang sudah dibuat) di ViewModel, sehingga tombol Coba lagi hanya mengulang langkah yang gagal.
- **Cache sederhana di SharedPreferences**: cocok untuk data kecil yang selalu diganti utuh; Room baru dipakai jika perlu query.

## Latihan untuk Aris
1. Di `WorkspaceChoiceFragment`, kembalikan sementara `setNavigationIcon(canGoBack ? R.drawable.ic_arrow_back : 0)`, install, keluar dari semua ruang kerja, lalu lihat crash di Logcat. Baca stack trace-nya sampai menemukan baris di file kita, lalu kembalikan perbaikannya.
2. Tambahkan jenjang baru "SD/MI" di enum `OrgLevel` backend (beserta kategori yang boleh), restart backend, dan lihat chip baru muncul di form tanpa mengubah kode Android.
3. Di `OrganizationFormTest`, tambahkan tes "nama singkat lebih dari 30 karakter ditolak", jalankan `./gradlew testDebugUnitTest`, lalu buat lulus.

## Yang perlu Aris lakukan
- Tidak ada perubahan Gradle di fase ini (tidak perlu Sync Now).
- Database dev sudah memakai V17; kode undangan dummy berlaku 7 hari sejak seeder jalan (cara mengisi ulang ada di README).

## Rencana fase berikutnya
- **R4**: kelola organisasi (profil + logo, anggota, undangan + bagikan, ubah peran/jabatan, keluarkan, keluar, serah terima, hapus, riwayat aktivitas).
