# Fase W1: Kerangka dashboard pembuat website OrgSpace

Sumber rancangan: [`docs/rancangan/alur-pembuatan-website.md`](../rancangan/alur-pembuatan-website.md) bagian 1, 2, 8, 9 (revisi 12 Oktober 2026; keputusan K6, K16, K19–K20, G1).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `backend/.../user/UserController`, `UserService`, `dto/UpdateMeRequest` | `PATCH /api/users/me {displayName}` menggantikan `PATCH /api/users/me/creator-profile` (rancangan bagian 8) |
| `backend/.../gallery/GalleryIntegrationTest` | Tes ubah nama memakai endpoint baru |
| `res/menu/menu_creator_tabs.xml`, `res/drawable/ic_web.xml`, `strings.xml` | Tab **Project** → **Website** (ikon jendela browser, Material Symbols Rounded); "Custom" → **Rakit sendiri** |
| `res/layout/fragment_creator_dashboard.xml`, `CreatorDashboardFragment` | App bar: logo kecil (atau huruf depan) + **nama singkat** + ▾. Tamu yang menekan + / Mulai dibawa ke layar Masuk |
| `ui/creator/CreateProjectSheet` | "Rakit sendiri" membuka editor "Segera hadir" berjudul **"Website {org}"** |
| `ui/editor/ComingSoonEditorFragment`, `strings.xml` | Teks layar "Segera hadir" editor Rakit sendiri sesuai rancangan bagian 9 |
| `ui/workspace/WorkspacePickerSheet`, `sheet_workspace_picker.xml` | Kelola organisasi / ruang pribadi, **Buat organisasi baru**, **Gabung organisasi**, **Buat ruang pribadi** (hanya jika belum punya) |
| `ui/workspace/WorkspaceRows` (baru) | Satu cara menampilkan daftar ruang kerja, dipakai pemilih dan tab Profil |
| `ui/workspace/WorkspaceViewModel` | `getWorkspaces()` dan `select()` untuk tab Profil |
| `ui/creator/CreatorProfileFragment`, `fragment_creator_profile.xml` | Tab Profil = akun pribadi: kepala + **Ubah nama tampilan**, **Ruang kerja saya** (ketuk = pindah), menu. Kartu "Info pembuat website" dan ringkasan project/export dihapus |
| `ui/creator/CreatorProfileViewModel` (dihapus) | Ringkasan dari Room tidak dipakai lagi (status "diexport" dihapus di rancangan baru) |
| `data/remote/api/UserApi`, `UserRepository`, `CreatorProfileEditViewModel`, `fragment_creator_profile_edit.xml` | Memanggil endpoint baru; judul "Ubah nama tampilan" dan keterangan baru |
| `res/values/dimens.xml`, `styles.xml`, `drawable/bg_workspace_initial_small.xml`, `item_workspace.xml` | Ukuran logo app bar 28dp, sudut `ShapeAppearance.App.ExtraSmall`; logo di daftar ruang kerja ikut membulat |

## Alasan keputusan
- **"Kelola" tetap tampil di ruang pribadi** (berlabel "Kelola ruang pribadi"). Ini menyimpang dari rancangan 2.1, yang menyembunyikannya. Alasannya, ruang pribadi butuh tempat untuk mengubah profil/logo (dipakai isi otomatis template) dan menghapusnya, dan layar R4 sudah mendukungnya. (Alternatif: sembunyikan → ruang pribadi tidak bisa diubah atau dihapus sama sekali.)
- **Pemilih membuka form langsung, tanpa layar pilihan** → sesuai rancangan (tiga tombol terpisah) dan satu ketukan lebih pendek. Ketiga form keluar lewat `HomeNavigator.navigateHome`, jadi aman dibuka dari mana saja.
- **Nama singkat di app bar, nama lengkap di pemilih** → "OSIS SMA Negeri 1 Bandung" terpotong di app bar, sedangkan "OSIS Smansa" muat.
- **Tamu diminta masuk saat menekan +** (G1) → website kini milik ruang kerja di cloud, jadi tamu tidak punya tempat menyimpannya.
- **Endpoint diganti, bukan ditambah** → `creator-profile` tidak lagi punya arti (profil kreator sudah tidak berisi data organisasi). Hanya app ini yang memakainya, jadi tidak perlu masa transisi.

## Cara menjalankan & mengetes
1. Jalankan ulang backend dev (endpoint baru), `adb reverse tcp:8080 tcp:8080`, `./gradlew installDebug`.
2. Masuk sebagai `dummy3`: app bar menampilkan logo/huruf + "OSIS Smansa" ▾. Menu bawah: Beranda · **Website** · + · Template · Profil.
3. Ketuk ▾: daftar ruang kerja + Kelola organisasi + Buat organisasi baru + Gabung organisasi (+ Buat ruang pribadi jika belum punya).
4. Tab **Profil** → Ruang kerja saya → ketuk ruang kerja lain: app bar dan tanda ✓ berubah.
5. Profil → **Ubah nama tampilan** → simpan → nama berubah (cek juga di Swagger: `GET /api/users/me`).
6. Tombol **+** → **Rakit sendiri** → layar "Editor Rakit sendiri lagi kami siapkan" berjudul "Website {org}".
7. Keluar akun, tekan **+** → layar Masuk.

## Hasil tes
- Backend `GalleryIntegrationTest` (berisi tes ubah nama): **8 lulus**.
- `./gradlew testDebugUnitTest`: **106 lulus**. `./gradlew lint`: **0 masalah**. `assembleDebug`: berhasil.
- Uji di HP (12 Oktober 2026, dummy1): app bar logo + nama singkat ✅, pemilih dengan tombol baru (Buat ruang pribadi tersembunyi karena sudah punya) ✅, pindah ruang kerja dari tab Profil ✅, ubah nama tersimpan di server ✅, + → Rakit sendiri "Website Agus Lestari" ✅, tamu + → Masuk ✅.
- Belum: isi tab Website, Beranda, dan galeri belum mengikuti ruang kerja aktif (dikerjakan W2–W4).

## Konsep yang dipelajari
- **Satu sumber kebenaran untuk UI yang berulang**: `WorkspaceRows` menggambar daftar ruang kerja di dua layar; perubahan tampilan cukup di satu tempat.
- **Observasi dua LiveData sekaligus**: Profil mengamati daftar *dan* ruang kerja aktif, lalu menggambar ulang dari keduanya, sehingga ✓ selalu benar walau salah satu berubah duluan.
- **`ShapeableImageView`**: memotong gambar dengan bentuk dari `shapeAppearanceOverlay` tanpa membuat bitmap baru.
- **Mengganti endpoint dengan aman**: ubah backend, test, app, dan dokumentasi dalam satu fase, lalu cari sisa pemakaian dengan `grep`.

## Latihan untuk Aris
1. Ubah `workspace_switcher_logo_size` di `dimens.xml` menjadi 32dp, install, dan lihat app bar. Kenapa sudutnya tetap rapi?
2. Di `WorkspaceRows`, tampilkan jabatan (atau peran) dalam satu baris, mis. "Admin · Bendahara". Data jabatan belum ada di `Workspace`; telusuri dari `OrganizationSummaryDto` di backend.
3. Lewat Swagger, coba `PATCH /api/users/me` dengan `{"displayName":""}` dan lihat `fieldErrors`-nya.

## Yang perlu Aris lakukan
- Jalankan ulang backend kalau kamu memakai backend dari VS Code (endpoint `PATCH /api/users/me` baru).
- Tidak ada perubahan Gradle (tidak perlu Sync Now).

## Rencana fase berikutnya
- **W2**: endpoint daftar website per ruang kerja (`GET /api/organizations/{id}/websites` + `counts`), tab Website (cari, filter status, urutan, kartu dengan status server, cache offline Room), halaman kelola website (kerangka, bagian "Segera hadir"), seeder website contoh.
