# Fase 20 (T2): Unduh & cache paket template di HP

Sumber rancangan: [`alur-buat-website-via-template.md`](../rancangan/alur-buat-website-via-template.md) bagian 5, 10, 11.1–11.2. Penjelasan cara kerjanya: [`dokumentasi-project.md` bab 30](../dokumentasi-project.md#30-editor-template-mode-di-android).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | Tambah **WorkManager 2.12.0** (kirim event tertunda) dan **jsoup 1.23.2** (export, dipakai Fase 25) |
| `data/local/TemplatePackageEntity`, `TemplatePackageDao` | Tabel `template_packages` (kunci `template_id` + `version`, folder, ukuran, waktu unduh/dipakai) |
| `data/local/PendingEventEntity`, `PendingEventDao` | Tabel `pending_events`: antrean event statistik saat offline |
| `data/local/ProjectEntity`, `AppDatabase` | Kolom `projects.template_version`; database versi 2 dengan **AutoMigration 1→2** |
| `data/model/TemplateManifest` | Bentuk `manifest.json` di HP (dibaca Gson) |
| `data/remote/api/GalleryApi`, `dto/GalleryTemplateDetailDto`, `dto/TemplateEventDto` | `GET templates/{id}`, `GET templates/{id}/package` (`@Streaming`), event dengan `projectId` |
| `core/template/TemplateFiles` | Lokasi folder: `files/templates/{id}/{versi}/` dan `files/projects/{id}/` |
| `core/template/PackageExtractor` | Ekstrak ZIP paket; path berbahaya atau isi > 80 MB membuat seluruh paket **ditolak** |
| `core/template/DownloadHandle` | Pegangan unduhan yang bisa dibatalkan (memutus request yang sedang menunggu) |
| `data/repository/TemplatePackageRepository` | Detail, unduh dengan progres → file sementara → folder sementara → folder final; hapus paket lama |
| `data/repository/TemplateEventRepository`, `core/template/SendEventsWorker` | Event "Dilihat"/"Didownload" masuk antrean lalu dikirim WorkManager saat online |
| `ui/template/TemplateDownloadFragment`, `TemplateDownloadViewModel`, `DownloadState`, `TemplateNav` | Layar Unduh: detail singkat, progres "2,6 dari 4,2 MB", Batal, dialog data seluler (> 10 MB), offline, Coba lagi |
| `ui/creator/TemplateOpener`, `GalleryRepository` | Klik template kini membuka layar Unduh; pencatatan "Dilihat" pindah ke antrean |
| `layout/fragment_template_download.xml`, `values/strings_template.xml`, `nav_graph.xml` | Tampilan & teks layar Unduh |
| `PackageExtractorTest` | 5 test: paket valid, zip slip, path absolut, terlalu besar, tanpa manifest |

## Alasan keputusan
- **Paket dari server yang berisi path aneh ditolak seluruhnya** (bukan dilewati seperti ZIP provider) → paket buatan server seharusnya selalu bersih; jika tidak, ada yang salah dan lebih aman berhenti. (Alternatif: lewati entrinya → paket bisa terpasang setengah.)
- **Unduh → file sementara → ekstrak ke folder `.tmp-…` → rename** → jika unduhan putus atau app ditutup, folder paket lama/kosong tidak pernah setengah jadi.
- **"Dilihat" lewat antrean juga** (dulu langsung dikirim dan diabaikan jika offline) → rancangan bagian 10 meminta antrean offline untuk kedua event. Membuka layar tidak pernah menunggu jaringan.
- **Worker mengambil repository lewat `@EntryPoint`** → tidak perlu library tambahan `androidx.hilt:hilt-work`.
- **AutoMigration Room** → perubahan versi 2 hanya menambah tabel/kolom yang boleh kosong; Room membuat SQL migrasinya dari riwayat `app/schemas/`. Data project lama tetap utuh.
- **Versi paket**: karena fitur versi template belum dibangun, paket mana pun yang sudah tersimpan dianggap "versi sama" (unduhan dilewati).
- **Nama class `TemplateDownloadFragment`** (rancangan menyebut `TemplateDetailFragment`) → nama itu sudah dipakai layar detail milik provider.

## Cara menjalankan & mengetes
1. Backend dev dengan seeder demo (lihat Fase 19). Template demo yang tayang sudah punya paket.
2. Klik **Sync Now** di Android Studio (file Gradle berubah), lalu Run ke HP; `adb reverse tcp:8080 tcp:8080`.
3. Tab Template → ketuk "Profil Sekolah" → layar Unduh dengan progres → editor terbuka.
4. Kembali, ketuk template yang sama → langsung ke editor tanpa unduh.
5. Matikan backend/wifi, ketuk template lain → "Butuh internet untuk mengunduh template pertama kali." + Coba lagi.

## Hasil tes
- `./gradlew testDebugUnitTest`: lulus (bagian dari 94 test setelah Fase 25).
- Uji di HP: menunggu HP tersambung (lihat Fase 26).

## Konsep yang dipelajari
- **Streaming download**: isi respons dibaca sepotong-sepotong (`@Streaming`) agar file besar tidak dimuat utuh ke memori.
- **Zip slip**: nama entri `../../x` di ZIP bisa menulis ke luar folder; dicegah dengan memeriksa path kanonik.
- **WorkManager**: penjadwal pekerjaan latar yang tetap jalan walau app ditutup, dengan syarat (mis. ada internet).
- **Room AutoMigration**: Room membandingkan skema lama & baru lalu menulis migrasinya sendiri.

## Latihan untuk Aris
1. Ubah `MOBILE_CONFIRM_BYTES` di `TemplateDownloadViewModel` menjadi 10 KB, matikan wifi di HP (pakai data seluler), lalu buka template baru: dialog data seluler harus muncul.
2. Di Android Studio → App Inspection → Database Inspector, lihat isi tabel `template_packages` setelah mengunduh.
3. Buat ZIP berisi `../luar.txt` dan uji `PackageExtractor` dengan test baru.

## Yang perlu Aris lakukan
- Klik **Sync Now** (WorkManager & jsoup ditambahkan).

## Rencana fase berikutnya
- Fase 21 (T3): editor — preview, tab Isi, sinkron dua arah, autosave.
