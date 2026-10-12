# Fase 04: User, onboarding, mode, seeder

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `user/dto/CreatorOnboardingRequest.java`, `ProviderOnboardingRequest.java`, `ActiveModeRequest.java` | Body request beserta aturan validasinya |
| `user/UserService.java` | `completeCreatorOnboarding`, `becomeProvider`, `changeActiveMode` |
| `user/UserController.java` | `POST /users/me/onboarding/creator`, `POST /users/me/onboarding/provider`, `PATCH /users/me/active-mode` |
| `provider/ProviderProfile.java` | Konstruktor + `isSuspended()` |
| `seed/DummyDataSeeder.java` | 10 user dummy (profile dev, hanya jika tabel `users` kosong) |
| Test: `user/OnboardingIntegrationTest.java`, `seed/DummyDataSeederTest.java` | 11 test onboarding & mode, 1 test seeder |
| `README.md` | Status fitur, daftar akun dummy + password, contoh SQL status provider, troubleshooting seeder |
| `docs/dokumentasi-project.md` | Bab baru "Onboarding, mode, dan data dummy" |

## Alasan keputusan
- Tombol "Mulai" dan "Lewati" memakai endpoint yang sama → instruksi 6.6 menyebut keduanya menyelesaikan onboarding. "Lewati" cukup mengirim `displayName` yang sudah terisi otomatis (alternatif: endpoint `skip` terpisah, tidak dipilih karena menambah endpoint di luar spesifikasi 7.3).
- Onboarding creator bersifat *upsert* (buat atau perbarui) → aman kalau dipanggil ulang, mis. app mengirim dua kali karena koneksi lambat.
- Daftar provider langsung mengubah `activeMode` menjadi `provider` → skenario 9 meminta user langsung melihat Dashboard Provider dengan banner "sedang diverifikasi".
- Mode `creator` selalu boleh, walau user belum punya profil creator → dashboard pembuat website juga dipakai tamu dan menjadi tujuan saat provider ditangguhkan. Mode `provider` hanya ditolak kalau belum punya profil atau berstatus `suspended`. Status `pending`/`rejected` tetap boleh, supaya bannernya terlihat (bagian 6.6).
- Backend tidak mengubah `activeMode` diam-diam saat provider ditangguhkan → keputusan arah layar ada di app (bagian 6.1), dan app perlu tahu statusnya untuk menampilkan pesan.
- `becomeProvider` mengunci baris user → dua request bersamaan tidak bisa membuat dua profil (yang kedua mendapat `PROVIDER_PROFILE_EXISTS`, bukan error 500).
- Validasi URL portofolio dengan `@Pattern` `http(s)://` → `@URL` dari Hibernate Validator juga menerima `ftp://` dan protokol lain.
- Seeder: Datafaker 2.7.0 tidak punya locale Indonesia (sudah dicek di isi jar) → nama dipilih acak oleh Datafaker dari daftar nama Indonesia sendiri. `Random(42)` membuat hasilnya tetap sama setiap kali diisi ulang.
- Komposisi seeder: dummy1–2 belum onboarding, dummy3–7 creator, dummy8–10 creator + provider `pending`/`approved`/`rejected` → setiap kondisi layar punya akun contoh. Status `suspended` dicoba lewat SQL di README (skenario 11).
- Email dummy memakai domain `.test` → domain khusus pengujian yang tidak akan pernah menjadi alamat sungguhan.

## Cara menjalankan & mengetes
1. Kosongkan tabel `users` kalau ingin seeder berjalan (README → "Akun dummy").
2. Jalankan backend dengan profile dev. Di log akan muncul `Seeder selesai: 10 user dummy dibuat`.
3. Di Swagger, login dengan `dummy1@templateapp.test` / `password123`, lalu Authorize, lalu panggil `POST /api/users/me/onboarding/creator`.
4. Login `dummy9@...`, lalu coba `PATCH /api/users/me/active-mode` `{"mode":"creator"}` dan `{"mode":"provider"}`.
5. Jalankan SQL "Tangguhkan" di README untuk `dummy9`, lalu coba lagi mode provider → `403 MODE_NOT_ALLOWED`.
6. `./mvnw test`.

## Hasil tes
- `./mvnw test`: **lulus, 63 test**
  - AccountLinkingServiceTest 12
  - OnboardingIntegrationTest 11
  - SocialAuthIntegrationTest 10
  - AuthFlowIntegrationTest 9
  - FlywayMigrationTest 5
  - GitHubOAuthClientTest 5
  - JwtServiceTest 5
  - GlobalExceptionHandlerTest 4
  - DummyDataSeederTest 1
  - TemplateAppApplicationTests 1
- Uji manual ke backend dev (port 8080 dicek dulu: kosong):
  - seeder membuat 10 user
  - login dummy1 → belum onboarding, `roles: []`
  - dummy8/9/10 → mode provider dengan status `pending`/`approved`/`rejected`
  - onboarding creator dummy1 berhasil
  - mode provider tanpa profil → `403 MODE_NOT_ALLOWED`
  - `authorize-url` GitHub → `500` karena `GITHUB_CLIENT_ID` memang belum diisi
- Setelah uji manual, data dummy1 dikembalikan ke kondisi "belum onboarding", dan refresh token hasil uji dihapus. Database dev sekarang hanya berisi 10 akun dummy dalam kondisi awal.
- Sebelum seeder dijalankan, akun uji `coba-…@mail.com` buatan agent di Fase 02 dihapus, supaya tabel `users` kosong.

## Konsep yang dipelajari
- `ApplicationRunner` : kode yang dijalankan sekali setelah app selesai start.
- `@Profile("dev")` : bean hanya dibuat kalau profile tertentu aktif.
- Bean Validation lanjutan : `@AssertTrue`, `@Pattern`, dan validasi elemen list `List<@NotBlank String>`.
- Upsert : "buat kalau belum ada, perbarui kalau sudah ada" (`findById(...).orElseGet(...)`).
- Race condition & kunci baris : dua request bersamaan bisa sama-sama lolos pengecekan; `SELECT ... FOR UPDATE` (`PESSIMISTIC_WRITE`) membuat yang kedua menunggu.
- `TransactionTemplate` : menjalankan kode di dalam transaksi tanpa anotasi `@Transactional` (dipakai di test).
- Context caching di test : Spring memakai ulang context (dan database container) untuk test dengan konfigurasi yang sama; konfigurasi berbeda berarti context baru.

## Latihan untuk Aris
1. Login sebagai `dummy10@templateapp.test` di Swagger, lalu lihat `providerStatus` di `/users/me`. Ubah statusnya ke `approved` lewat SQL di README, panggil `/users/me` lagi, dan bandingkan.
2. Coba kirim `POST /api/users/me/onboarding/provider` dengan `"specialties": ["", "UMKM"]`. Field apa yang muncul di `fieldErrors`? Cari anotasi mana yang menolaknya.
3. Tambahkan nama depan baru ke `FIRST_NAMES` di `DummyDataSeeder`, kosongkan tabel users (README), lalu jalankan backend lagi. Apakah nama dummy berubah? Kenapa?

## Yang perlu Aris lakukan
- **Untuk lanjut ke Fase 05:** buat project Android lewat wizard Android Studio dengan pengaturan:
  - *New Project → Empty Views Activity*
  - Name: `TemplateApp`
  - Package name: `com.aris.templateapp`
  - Save location: `/home/m-ariza-fi-i-muslimin/Desktop/Rakit Apps/android`
  - Language: **Java**
  - Minimum SDK: **API 26**
  - Build configuration language: **Kotlin DSL**
  - Klik **Finish**, tunggu Gradle Sync selesai, lalu tutup Android Studio sebelum memberi kabar (supaya build dari terminal tidak bentrok).
- (Opsional) Buat kredensial OAuth (README → Setup OAuth). Login Google di HP baru dicoba di Fase 08.

## Rencana fase berikutnya
- Fase 05: fondasi Android (`gradle.properties`, Hilt, Navigation, ViewBinding, token desain, font Geist, style komponen, katalog komponen debug, `strings.xml`, NetworkModule, interceptor, authenticator, TokenStorage, SessionStore, Resource, AppExecutors), setelah Aris membuat project lewat wizard.
