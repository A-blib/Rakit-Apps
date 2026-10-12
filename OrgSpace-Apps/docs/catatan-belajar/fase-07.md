# Fase 07: Masuk & daftar email

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `data/remote/api/AuthApi.java`, `dto/LoginRequestDto.java`, `dto/RegisterRequestDto.java` | Endpoint register, login (dengan `linkToken` opsional), logout |
| `data/model/AuthResult.java`, `data/repository/AuthRepository.java` | Daftar/masuk, lalu simpan token terenkripsi + salinan user |
| `ui/auth/AuthFormValidator.java` | Validasi form di HP (aturannya sama dengan backend) |
| `ui/auth/AuthViewModel.java` | Validasi → request di thread latar → LiveData loading/gagal/sukses, coba lagi, cegah klik ganda |
| `ui/auth/LoginFragment.java`, `RegisterFragment.java` + layout | Layar Masuk & Daftar sesuai 9.4 |
| `ui/auth/AuthFormBinder.java`, `ui/common/ErrorMessages.java` | Error per field, snackbar error 6.2, tombol "Coba lagi" saat offline |
| `ui/auth/PostLoginNavigator.java` | Setelah berhasil, buka dashboard sesuai aturan 6.1 dan bersihkan back stack |
| `layout/view_social_buttons.xml`, `view_divider_or.xml`, `drawable/ic_logo_google.xml`, `ic_logo_github.xml` | Tombol Google (bergaris) & GitHub (utama) dengan logo resmi, pemisah "atau" |
| `values/styles.xml`, `themes.xml` | Snackbar monokrom |
| `navigation/nav_graph.xml`, `CreatorDashboardFragment.java` | Tombol Masuk di dashboard → Masuk → Daftar |
| `values/strings.xml` | Semua teks layar Masuk & Daftar |
| Test: `AuthFormValidatorTest` | 5 unit test |

## Alasan keputusan
- Validasi di HP **dan** di backend → HP memberi umpan balik instan, backend tetap menjadi penjaga utama.
- Regex email sederhana alih-alih `Patterns.EMAIL_ADDRESS` → kelas Android tidak bisa dipakai di unit test JVM biasa, dan pemeriksaan sesungguhnya tetap di backend.
- Satu `AuthViewModel` untuk Masuk & Daftar → sesuai struktur bagian 5.2; logika loading/gagal/coba lagi sama untuk keduanya.
- Setelah login, tujuan memakai `StartupDecision` → user mendarat di layar yang sama seperti saat app dibuka (mis. provider ke Dashboard Provider), sesuai "kembalikan user ke tujuan semula".
- Logo Google & GitHub resmi → pedoman branding melarang mengubah logo. Google memakai `iconTint="@null"` agar warnanya asli.
- Snackbar dibalik warnanya (latar foreground) → warna ungu bawaan Material tidak sesuai arah desain monokrom.
- Tombol Google/GitHub belum diberi aksi → dikerjakan di Fase 08 (butuh kredensial OAuth).

## Cara menjalankan & mengetes
1. Jalankan backend (`./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`), lalu `adb reverse tcp:8080 tcp:8080`.
2. `cd android && ./gradlew installDebug`, buka app, lalu tekan **Masuk**.
3. Coba: form kosong, password salah, `dummy9@templateapp.test` / `password123`, Daftar dengan email yang sudah dipakai, dan Daftar akun baru.
4. Matikan backend, lalu coba Masuk → muncul "Tidak ada koneksi…" + **Coba lagi**. Nyalakan backend lagi dan tekan Coba lagi.

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 21 test** (AuthFormValidatorTest 5 + 16 test sebelumnya).
- `./gradlew lint`: 0 error, 0 warning selain `UnusedResources` (15, untuk Fase 08–09).
- Uji di HP (realme RMX3151, Android 13, wireless debugging + `adb reverse`, backend dev di laptop):
  - Form kosong → "Email wajib diisi" & "Password wajib diisi" di bawah input ✅
  - `dummy3` + password salah → snackbar "Email atau password salah." ✅
  - Backend dimatikan → "Tidak ada koneksi. Periksa internet lalu coba lagi." + **Coba lagi**; setelah backend nyala lagi, Coba lagi → berhasil masuk ✅
  - `dummy9` (provider approved) → langsung **Dashboard Provider** dengan avatar "P", tanpa banner ✅
  - Tutup lalu buka app → langsung ke dashboard tanpa login ulang (skenario 3) ✅
  - Status `dummy9` diubah lewat SQL: `pending` → banner kuning, `rejected` → banner merah + alasan, `suspended` → dialihkan ke Dashboard Pembuat Website + pesan (skenario 11) ✅
  - Daftar dengan email `dummy1` → "Email sudah terdaftar. Silakan masuk." ✅
  - Daftar akun baru → Dashboard Pembuat Website dengan avatar "U" ✅ (onboarding menyusul di Fase 09)
  - Google Password Manager menawarkan menyimpan sandi → `autofillHints` terbaca ✅
- Setelah uji: status `dummy9` dikembalikan ke `approved`, akun uji `uji-…@mail.com` dihapus, dan backend yang dijalankan agent dimatikan. Database dev kembali berisi 10 akun dummy.
- Catatan uji: memutus `adb reverse` saja tidak membuat app offline, karena OkHttp memakai ulang koneksi yang sudah terbuka. Untuk menguji "tidak ada koneksi", backend dimatikan.

## Konsep yang dipelajari
- Validasi dua lapis (klien & server).
- `Supplier<T>` : fungsi yang disimpan untuk dijalankan nanti (dipakai untuk "Coba lagi").
- `NavOptions.setPopUpTo` dari kode.
- `<include>` layout untuk komponen yang dipakai ulang.
- `autofillHints` : membantu pengelola sandi mengisi dan menyimpan akun.
- Theme attribute untuk komponen (`snackbarStyle`, `snackbarButtonStyle`).

## Latihan untuk Aris
1. Di `AuthFormValidator`, ubah minimal password menjadi 10, lalu jalankan `AuthFormValidatorTest`. Test mana yang gagal, dan kenapa itu justru bagus? Setelah itu kembalikan nilainya.
2. Login sebagai `dummy10@templateapp.test` (password `password123`). Banner apa yang muncul? Cari teksnya di `strings.xml`.
3. Matikan Wi-Fi HP saat di layar Masuk, lalu tekan Masuk. Pesan apa yang muncul? Ikuti alurnya dari `ApiErrorParser.parse(Throwable)` sampai `ErrorMessages`.

## Yang perlu Aris lakukan (untuk Fase 08)
- Buat kredensial OAuth mengikuti README → **Setup OAuth Google & GitHub**:
  - Google: client **Web**, lalu client **Android** dengan package `com.aris.templateapp` dan SHA-1 debug laptop ini: `1B:A6:02:C5:73:F2:EE:B1:56:C1:72:9E:E1:63:E1:6C:03:96:0B:AE`. Tambahkan email Google-mu di *Test users*.
  - GitHub: OAuth App dengan callback `http://localhost:8080/api/auth/github/callback`.
- Isi `GOOGLE_WEB_CLIENT_ID`, `GITHUB_CLIENT_ID`, dan `GITHUB_CLIENT_SECRET` di `backend/.env`, serta `GOOGLE_WEB_CLIENT_ID` di `android/local.properties`.

## Rencana fase berikutnya
- Fase 08: Credential Manager (Google), Custom Tabs + deep link `templateapp://auth/callback` (GitHub), `LinkAccountDialog` untuk `ACCOUNT_LINK_REQUIRED`.
