# Fase 09: Onboarding, mode, pengaturan

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `data/remote/api/UserApi.java` + DTO onboarding/mode/identitas | Endpoint onboarding creator/provider, `active-mode`, identitas (lihat, sambung Google/GitHub, lepas) |
| `data/repository/UserRepository.java`, `AuthRepository.logout()` | Request di atas + keluar (selalu menghapus sesi di HP) |
| `data/model/Identity.java` | Metode login tersambung untuk UI |
| `ui/onboarding/*` | `RoleSelectFragment`, `CreatorFormFragment`, `ProviderFormFragment`, `OnboardingViewModel`, `OnboardingFormValidator`, `OnboardingUi` |
| `ui/profile/ProfileSheet.java`, `ProfileViewModel.java`, `sheet_profile.xml` | Menu profil bottom sheet: nama, email, badge mode, beralih mode, jadi penyedia template, pengaturan, keluar |
| `ui/settings/*` | `SettingsFragment`, `LinkedMethodsFragment`, `SettingsViewModel` |
| `ui/common/HomeNavigator.java` | Satu aturan "pulang" untuk setelah login, onboarding, beralih mode, keluar (menggantikan `PostLoginNavigator`) |
| `ui/common/StateView.java`, `view_state.xml` | Tampilan loading/kosong/error + Coba lagi |
| `MainActivity.java` | Sesi berakhir → pindah ke dashboard tamu + pesan |
| `StartupFragment.java`, `nav_graph.xml` | Rute pilih peran, form, pengaturan, metode login |
| Layout `fragment_role_select`, `item_role_card_*`, `fragment_creator_form`, `fragment_provider_form`, `fragment_settings`, `fragment_linked_methods`, `item_linked_method` | Tata letak sesuai 9.4 |
| `values/styles.xml`, `themes.xml`, `strings.xml`, `dimens.xml` | Style baris daftar, judul grup mono, checkbox, bottom sheet; semua teks Fase 09 |
| Test: `OnboardingFormValidatorTest` | 4 unit test |

## Alasan keputusan
- `HomeNavigator` memakai `StartupDecision` → setiap "pulang" mengikuti aturan bagian 6.1 yang sama, tidak ada aturan ganda yang bisa saling berbeda.
- "Lewati" memakai endpoint yang sama dengan "Mulai" → instruksi 6.6: keduanya menyelesaikan onboarding.
- Menu profil berupa bottom sheet `DialogFragment` → sesuai 9.4, dan bertahan saat HP diputar.
- Tombol "Lepaskan" dinonaktifkan untuk metode terakhir (bukan disembunyikan) → user tetap melihat alasan kenapa tidak bisa dilepas; backend tetap menolak (`LAST_IDENTITY`).
- Baris "Email" tanpa tombol sambungkan → belum ada endpoint membuat password untuk akun Google/GitHub (di luar spesifikasi 7.3).
- Keluar tetap berhasil saat offline → instruksi 6.7.
- Baris menu memakai compound drawable → saran lint `UseCompoundDrawables`; layout lebih ringan.
- Pesan "harus menyetujui" disembunyikan begitu checkbox dicentang → hasil uji di HP: sebelumnya pesan tetap tampil sampai tombol ditekan lagi.
- Email panjang di Metode login dipotong di tengah → hasil uji di HP: sebelumnya terpotong jadi dua baris.

## Cara menjalankan & mengetes
1. Backend jalan, `./tools/keep-adb-reverse.sh`, `./gradlew installDebug`.
2. Masuk dengan akun yang belum onboarding (mis. `dummy1@templateapp.test` / `password123`, atau akun Google baru) → Pilih peran → Pembuat website → Mulai.
3. Avatar → Jadi penyedia template → isi → Kirim pengajuan.
4. Avatar → Beralih ke mode …, tutup app, lalu buka lagi.
5. Avatar → Pengaturan → Metode login terhubung.
6. Avatar → Keluar, lalu Masuk lewat GitHub.

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 26 test**. `./gradlew lint`: 0 error, 0 warning selain `UnusedResources`.
- Uji di HP Aris (akun Google "Aris123 Muslim" yang sudah tersambung GitHub, belum onboarding):
  - Startup langsung ke **Pilih peran** ✅ → form pembuat website, nama terisi dari Google, chip "Sekolah", organisasi "SMK Negeri 1" → Mulai → dashboard dengan avatar "A" ✅
  - Menu profil: nama, email, badge "MODE PEMBUAT WEBSITE", Jadi penyedia template, Pengaturan, Keluar ✅
  - **Skenario 9**: Jadi penyedia template → form (nama terisi, 2 keahlian, link GitHub, setuju) → Dashboard Provider + banner "Akunmu sedang diverifikasi." ✅
  - **Skenario 10**: Beralih ke mode pembuat website → tutup → buka → tetap di mode pembuat website ✅
  - Pengaturan → Metode login terhubung: Google & GitHub tersambung (Lepaskan), Email "Belum ada password" ✅
  - **Skenario 13**: Keluar → dashboard tamu + "Kamu sudah keluar." ✅
  - **Skenario 7 (bagian akhir)**: setelah keluar, Masuk lewat GitHub → langsung ke akun yang sama (avatar "A"), tanpa dialog ✅
- Belum diuji di HP: melepas/menyambung ulang metode login dari Pengaturan (logika backend sudah diuji di `SocialAuthIntegrationTest`).

## Konsep yang dipelajari
- BottomSheetDialogFragment & `requireParentFragment()`.
- ChipGroup `singleSelection` vs pilihan ganda.
- Compound drawable (`drawableStartCompat`, `drawableTint`).
- Satu sumber aturan navigasi (`HomeNavigator`) untuk menghindari logika ganda.

## Latihan untuk Aris
1. Login sebagai `dummy2@templateapp.test`, pilih Penyedia template, lalu kirim tanpa mencentang persetujuan. Pesan apa yang muncul, dan dari baris mana?
2. Di `ProfileSheet.bindUser`, ubah syarat "Beralih mode" menjadi selalu tampil, lalu coba dengan akun yang hanya creator. Apa yang terjadi saat diketuk? (Petunjuk: `MODE_NOT_ALLOWED`.) Setelah itu kembalikan seperti semula.
3. Matikan backend, lalu tekan Keluar. Apakah tetap keluar? Cari alasannya di `AuthRepository.logout()`.

## Yang perlu Aris lakukan
- Tidak ada yang wajib.

## Rencana fase berikutnya
- Fase 10: state kosong/error, aksesibilitas, konsistensi desain, screenshot mode terang & gelap di `docs/screenshots/` (tampil di README), uji skenario 13.2 yang tersisa (8, 12, 14, 15), checklist bagian 14.
