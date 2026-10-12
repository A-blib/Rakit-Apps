# Fase 16: Android tab Upload dan langkah 1–3

Sumber rancangan: [`docs/rancangan/alur-fitur-upload.md`](../rancangan/alur-fitur-upload.md) bagian 3, 4, 5.4, 5.8, 5.9, 5.11, dan 6. Penjelasan cara kerjanya ada di [`dokumentasi-project.md` bab 26](../dokumentasi-project.md#26-upload-di-android-langkah-13).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| Backend `upload/DraftService`, `UploadController`, `V14__template_thumbnail_source.sql`, `TemplateThumbnailController` | Simpan otomatis Info template, thumbnail (dicek dari isi file), langkah wizard, unduh ZIP, peringatan WebView HP, pengaturan upload, thumbnail publik/pemilik |
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | `androidx.webkit` 1.17.1 (disetujui di rancangan 7.9) |
| `AndroidManifest.xml` | Izin `ACCESS_NETWORK_STATE` untuk peringatan data seluler |
| `data/remote/api/UploadApi`, `dto/*Upload*`, `DraftDto`, `TechInfoDto`, `HelpArticleDto`, ... | Endpoint Upload + artikel Panduan |
| `data/repository/UploadRepository` | Upload per potongan dengan lanjut otomatis (5 kali, jeda 2–32 detik), situs lokal, thumbnail, laporan |
| `core/upload/ZipQuickCheck`, `ZipPaths`, `FileSizes` | Cek kilat di HP: RAR, bukan ZIP, rusak, tanpa index.html, terlalu besar (5 file terbesar) |
| `core/upload/LocalSiteStore` | Ekstrak ZIP ke folder pribadi app, aman dari zip slip & zip bomb |
| `core/upload/ThumbnailImages`, `core/network/ThumbnailLoader` | Thumbnail persegi 720 px JPEG < 1 MB; pemuat gambar + cache tanpa library baru |
| `ui/upload/SiteWebView`, `LocalSitePathHandler` | WebView aman: file lewat WebViewAssetLoader, host di luar daftar diblokir, pindah situs diblokir |
| `ui/upload/OffscreenPage`, `DeviceChecker`, `ThumbnailCapture`, `SectionInfo`, `assets/upload/sections.js` | WebView tersembunyi: tahap C (error JS, tampilan melebar), potret thumbnail, deteksi section berlapis |
| `ui/provider/ProviderUploadFragment`, `ProviderUploadViewModel` | Tab Upload Kondisi A/B, kuota draft, hapus, checklist sebelum upload |
| `ui/upload/UploadCheckFragment`, `UploadCheckViewModel`, `UploadCheckState` | Langkah 2: cek kilat → upload + progres → daftar tahap → tahap C → hasil |
| `ui/upload/UploadInfoFragment`, `UploadInfoViewModel` | Langkah 3: thumbnail (otomatis/section/gambar, HP/Desktop), nama, kategori, deskripsi, kata kunci, info teknis, simpan otomatis |
| `ui/upload/UploadMarkFragment` | Langkah 4 sementara (diisi di Fase 17) |
| `ui/upload/HelpArticleFragment`, `IssueListBinder`, `ReportIssueDialog`, `ZipPicker`, `UploadNav`, `WizardHeader` | Artikel Panduan, daftar masalah + tautan, laporan keliru, pemilih ZIP, navigasi wizard |
| `ui/provider/TemplateDetailFragment`, `ProviderHomeFragment` | Tombol "Upload file perbaikan"/"Lanjutkan draft", tautan Panduan/Laporkan; checklist upload tanpa "Segera hadir" |
| `ui/guide/Guide`, `strings.xml`, `strings_upload.xml` | Panduan "Syarat lolos pengecekan" & "Memperbaiki template yang tidak lolos"; semua teks Upload |
| `tools/generate_lottie.py`, `res/raw/coming_soon.json` | Animasi "Segera hadir" dihapus karena tab Upload tidak lagi memakainya |
| Test | Backend `UploadIntegrationTest.draftInfoThumbnailStepAndDeviceWarnings`; Android `ZipQuickCheckTest` (6 test) |

## Alasan keputusan
- **Server membuat salinan bernomor, HP tidak memakai jsoup** (keputusan Aris di awal) → di fase ini HP cukup menampilkan ZIP asli; penomoran `data-tpl-id` dibuat di Fase 17.
- **WebView tersembunyi di belakang isi layar**, bukan di luar layar → WebView yang dianggap tak terlihat bisa berhenti menggambar sehingga potret thumbnail kosong.
- **Path handler sendiri** (bukan `InternalStoragePathHandler`) → jenis file `.mjs` harus JavaScript agar modul JS jalan, dan path diawali "/" dibaca dari folder utama ZIP (rancangan 5.6 D).
- **Thumbnail persegi** → thumbnail kartu galeri dan Template Anda berbentuk persegi.
- **Tahap C hanya saat pengecekan selesai di layar itu** → membuka hasil lama tidak perlu menjalankan ulang semua halaman.
- **Simpan otomatis dengan jeda 800 ms dan hanya field yang berubah** → tidak mengirim request di setiap huruf; isian yang gagal tersimpan dimasukkan lagi ke antrean.
- **Pemuat gambar buatan sendiri** → Glide masih masuk daftar "jangan pasang" (AGENTS 4.2); OkHttp yang ada sudah cukup.
- **Animasi `coming_soon` dihapus** → tidak dipakai lagi; lint menandainya sebagai resource tak terpakai.

## Cara menjalankan & mengetes
1. Backend dengan profile dev, `adb reverse tcp:8080 tcp:8080`, lalu Run app di HP. Klik **Sync Now** dulu (ada library baru).
2. Masuk sebagai provider kosong (`dummy8@templateapp.test` / `password123`) → tab **Upload** → Kondisi A.
3. Salin ZIP uji ke HP, mis. `adb push backend/src/test/resources/test-fixtures/_DASAR/bersih.zip /sdcard/Download/toko-kue.zip`, lalu **Pilih file ZIP**.
4. Amati: progres upload → tahap dicentang → "Menjalankan halaman di HP" → "File lolos pengecekan" → **Lanjut**.
5. Langkah 3: thumbnail otomatis muncul; coba Desktop, Dari section, Upload gambar; isi kategori, deskripsi ≥ 20 karakter, kata kunci → **Lanjut** aktif.
6. ✕ → "Pekerjaanmu tersimpan sebagai draft" → tab Upload menampilkan draft "Langkah 4/6".
7. Ulangi dengan `BASE_HREF/gagal.zip` → "Belum memenuhi standar" → Pelajari cara memperbaikinya, Ini keliru? Laporkan, Upload file perbaikan.
8. Coba file `.rar` (ganti nama file apa pun menjadi `x.rar`) → pesan RAR + "Lihat caranya".

## Hasil tes
- Backend `./mvnw test`: **lulus, 229 test**.
- Android `./gradlew assembleDebug`: berhasil. `testDebugUnitTest`: **lulus, 53 test** (6 baru). `lint`: **0 masalah**.
- Uji di HP: **belum dilakukan**, karena HP belum tersambung ke laptop saat fase ini selesai. Langkah uji ada di atas.

## Konsep yang dipelajari
- **ActivityResult (OpenDocument / GetContent)**: membuka pemilih file sistem dan menerima hasilnya tanpa izin baca penyimpanan.
- **WebViewAssetLoader**: menyajikan file lokal lewat alamat https palsu, lebih aman daripada `file://`.
- **shouldInterceptRequest**: titik untuk memeriksa (dan memblokir) setiap permintaan WebView.
- **evaluateJavascript**: menjalankan JS di halaman dan menerima hasilnya sebagai string JSON.
- **Debounce**: menunda aksi sampai pengguna berhenti mengetik.
- **Content URI**: file dari pemilih dibaca lewat `ContentResolver`, bukan lewat path biasa.

## Latihan untuk Aris
1. Ubah jeda simpan otomatis (`SAVE_DELAY_MS`) menjadi 2 detik, lalu perhatikan teks "Menyimpan…/Tersimpan".
2. Tambahkan nama section baru (mis. `['promo', 'Promo']`) di `assets/upload/sections.js`, lalu coba "Dari section" pada template yang punya `class="promo"`.
3. Matikan WiFi saat upload ZIP 5 MB → amati "Sinyal putus. Menyambung lagi…", nyalakan lagi, upload lanjut dari posisi terakhir.

## Yang perlu Aris lakukan
- Klik **Sync Now** di Android Studio.
- Sambungkan HP (wireless debugging) agar uji langkah di atas bisa dilakukan.

## Rencana fase berikutnya
- **Fase 17**: editor Tandai bagian: salinan HTML bernomor dari server, slide per section, ketuk elemen + bottom sheet, gaya, hubungkan isian, simpan + cadangan di HP.
