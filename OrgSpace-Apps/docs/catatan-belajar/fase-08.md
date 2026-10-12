# Fase 08: Google & GitHub di Android

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `data/remote/api/AuthApi.java`, DTO `GoogleLoginRequestDto`, `GitHubAuthorizeRequestDto`, `UrlResponseDto`, `TicketExchangeRequestDto` | Endpoint `/auth/google`, `/auth/github/authorize-url`, `/auth/github/exchange` |
| `data/repository/AuthRepository.java` | `loginWithGoogle`, `gitHubAuthorizeUrl`, `exchangeGitHubTicket` |
| `ui/auth/GoogleSignInHelper.java` | Credential Manager + `GetSignInWithGoogleOption` → idToken |
| `ui/auth/GitHubSignInHelper.java` | Membuka URL login GitHub di Custom Tab |
| `ui/auth/AuthDeepLinks.java`, `MainActivity.java`, `AndroidManifest.xml` | Intent filter `templateapp://auth/callback`, `onNewIntent`, meneruskan deep link ke layar |
| `ui/auth/LinkRequest.java`, `LinkAccountDialog.java` | Dialog penyambungan akun (satu atau beberapa metode lama) |
| `ui/auth/AuthViewModel.java` | Google, GitHub, callback, `pendingLink` + `linkToken`, event dialog & "lanjutkan dengan" |
| `ui/auth/SocialAuthBinder.java` | Logika tombol Google/GitHub & penyambungan yang sama untuk layar Masuk dan Daftar |
| `fragment_login.xml`, `fragment_register.xml`, `nav_graph.xml` | Banner info penyambungan, aksi Daftar → Masuk (dengan `linkToken`) |
| `ui/common/ErrorMessages.java`, `strings.xml` | Pesan `SOCIAL_AUTH_FAILED`, `TICKET_INVALID`, `LINK_USER_MISMATCH`, `LINK_TOKEN_INVALID`, `IDENTITY_IN_USE`, teks dialog |
| `android/tools/keep-adb-reverse.sh` + `.ps1` | Penjaga `adb reverse` (Linux & Windows) |
| Test: `LinkRequestTest` | Unit test parsing metode penyambungan |
| `README.md`, `docs/panduan-kolaborator.md`, `docs/dokumentasi-project.md` | Penjaga adb reverse, troubleshooting "Tidak ada koneksi", bab 19 |

## Alasan keputusan
- Credential Manager + `GetSignInWithGoogleOption` → sesuai bagian 6.5; library Google Sign-In lama sudah deprecated.
- Deep link diteruskan lewat singleton `AuthDeepLinks` (LiveData + Event) → deep link bisa datang kapan saja (bahkan saat app baru dibuka ulang), dan layar mana pun yang sedang menunggu bisa menerimanya tepat sekali.
- `LinkAccountDialog` berupa `DialogFragment` → tetap ada saat HP diputar (alternatif `AlertDialog` biasa akan hilang).
- `linkToken` hanya dikirim untuk metode yang termasuk `existingMethods` → mencegah token terkirim ke login yang tidak terkait.
- Logika tombol sosial dipisah ke `SocialAuthBinder` → layar Masuk dan Daftar tidak menduplikasi kode yang sama.
- `Stream.toList()` diganti `Collectors.toList()` → lint menemukan `toList()` baru tersedia di Android 14 (API 34) dan akan membuat app **crash** di HP Android 13 Aris.
- `GetCredentialException.getType()` diganti nama class error → `getType()` adalah API internal library (ditandai lint `RestrictedApi`).
- Skrip penjaga `adb reverse` → wireless debugging di HP Aris sering putus-sambung, dan setiap kali itu `adb reverse` hilang.

## Cara menjalankan & mengetes
1. Kredensial OAuth sudah diisi (`backend/.env`, `android/local.properties`); lihat README → Setup OAuth.
2. Jalankan backend, lalu `cd android && ./tools/keep-adb-reverse.sh` (biarkan berjalan), lalu `./gradlew installDebug`.
3. Masuk → **Lanjutkan dengan Google** → pilih akun yang terdaftar sebagai Test user.
4. Keluar (sementara: pasang ulang app), lalu Masuk → **Lanjutkan dengan GitHub** → Authorize → dialog "Sambungkan akun" → Masuk dengan Google.
5. Cek database: `select provider, email from user_identities;`

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 22 test**. `./gradlew lint`: 0 error, setelah 3 error lint diperbaiki (2× `Stream.toList` butuh API 34, 1× API terbatas).
- Uji di HP Aris dengan akun Google dan GitHub sungguhan:
  - **Skenario 5** (Google, akun baru): akun "Aris123 Muslim" dibuat otomatis tanpa menu Daftar, nama dari Google, email terverifikasi ✅
  - **Skenario 7** (Google → GitHub dengan email sama): dialog "Sambungkan akun" muncul → Masuk dengan Google → identitas `github` tersimpan di **akun yang sama** (database: 1 user, 2 identitas google+github; tiket `LINK` tercatat dipakai) ✅
  - Bagian akhir skenario 7 ("setelah keluar, masuk GitHub langsung ke akun yang sama") **belum diuji**, karena tombol Keluar baru dibuat di Fase 09.
- Kendala saat uji dan solusinya:
  - Halaman GitHub "redirect_uri is not associated with this application" → ada typo di Redirect URL OAuth App GitHub; diperbaiki Aris di pengaturan GitHub.
  - App "Tidak ada koneksi" padahal internet lancar → `adb reverse` hilang karena wireless debugging putus-sambung → dibuat `keep-adb-reverse.sh`.
  - Penjaga versi pertama gagal saat HP tersambung lewat USB **dan** Wi-Fi sekaligus ("more than one device") → diperbaiki: `adb -s <serial> reverse` untuk setiap sambungan.
  - App tampil sebagai user lama ("U") saat backend tak terjangkau → ini perilaku offline yang benar (memakai salinan user). Saat backend terjangkau lagi, sesi user yang sudah dihapus otomatis berakhir dan app kembali ke mode tamu.

## Konsep yang dipelajari
- Credential Manager & idToken.
- Deep link, intent filter, `BROWSABLE`.
- `launchMode="singleTask"` & `onNewIntent`.
- Custom Tabs.
- DialogFragment & `requireParentFragment()`.
- Lint `NewApi`: method Java baru belum tentu ada di Android versi lama (minSdk). Jangan mengandalkan "di laptop jalan".

## Latihan untuk Aris
1. Di Logcat Android Studio, filter `GoogleSignIn`, lalu tekan Lanjutkan dengan Google dan tutup lembar pilih akunnya. Apakah muncul pesan? Cari baris kode yang membuatnya diam.
2. Jalankan `adb shell am start -a android.intent.action.VIEW -d "templateapp://auth/callback?error=TICKET_INVALID"` saat layar Masuk terbuka. Pesan apa yang muncul? Ikuti alurnya dari `MainActivity.handleDeepLink`.
3. Di `nav_graph.xml`, cari `action_register_to_login`. Kenapa ada `popUpTo` ke `loginFragment`?

## Yang perlu Aris lakukan
- Tidak ada. Kredensial sudah lengkap dan login Google/GitHub sudah berhasil di HP.
- Saat mengembangkan app di HP lewat Wi-Fi, biarkan `./tools/keep-adb-reverse.sh` berjalan.

## Rencana fase berikutnya
- Fase 09: pilih peran, form creator/provider, menu profil (bottom sheet), beralih mode, Pengaturan → metode login terhubung (sambung/lepas), keluar. Setelah itu, uji bagian akhir skenario 7 dan skenario 8–10, 13.
