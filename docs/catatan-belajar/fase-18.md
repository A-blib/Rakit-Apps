# Fase 18: Coba sebagai pengguna dan Kirim

Sumber rancangan: [`docs/rancangan/alur-fitur-upload.md`](../rancangan/alur-fitur-upload.md) bagian 8, 9, dan 10.5. Penjelasan cara kerjanya ada di [`dokumentasi-project.md` bab 28](../dokumentasi-project.md#28-coba-kirim-dan-paket-template).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| Backend `upload/publish/PublishService`, `SubmitRules`, `SubmitRequest` | `POST /uploads/{id}/submit`: syarat info lengkap, minimal 3 isian, konfirmasi hak pakai aset |
| Backend `upload/publish/PublishRunner`, `SubmittedEvent` | Pengecekan akhir di latar belakang: aturan file + tandaan dicek lagi → paket → `published` (atau kembali ke `draft` dengan masalah) + notifikasi |
| Backend `upload/publish/PackageBuilder`, `LibraryFetcher`, `HttpLibraryFetcher` | Paket template: salin library CDN ke `vendor/` (beserta font/gambar dari CSS-nya), ganti link ke lokal, sisipkan `data-edit/data-key/data-label/data-section`, buang nomor `data-tpl-id` |
| Backend `UploadStorage.writePackage`, `gallery/GalleryQueries` | `package.zip`; pencarian galeri di nama, deskripsi, dan kata kunci |
| Android `assets/upload/try.js` | Terapkan isian percobaan ke preview: teks, link, gambar, gaya lewat satu lapisan `<style>`, tema `:root` |
| `ui/upload/UploadTryFragment`, `TryViewModel`, `TryForm`, `TrySession`, `fragment_upload_try.xml`, `item_try_field.xml`, `arrays_upload.xml` | Langkah 5: preview + form bottom sheet per section, pilihan warna + peringatan kontras, slider ukuran huruf/sudut, Ganti foto, Uji isi panjang, Isi asli, Ubah tandaan ini |
| `ui/upload/UploadSendFragment`, `SendViewModel`, `fragment_upload_send.xml` | Langkah 6: pratinjau kartu galeri, ringkasan, peringatan, hak pakai aset, status sampai tayang |
| `ui/provider/ProviderTabRequest`, `ProviderDashboardFragment` | "Lihat Template Anda" setelah tayang membuka tab Template Anda (filter Tayang) |
| `ui/creator/GalleryAdapter`, `ui/provider/TemplateAdapter`, `TemplateDetailFragment` + layout | Thumbnail asli tampil di galeri, Template Anda, dan detail |
| `ui/upload/UploadMarkFragment` | "Ubah tandaan ini" dari Coba langsung membuka elemennya |
| Test | Backend `PackageBuilderTest` (4), `UploadIntegrationTest.submitPublishesTemplateToGalleryWithMarkingAttributes` |

## Alasan keputusan
- **Preview Coba memakai salinan bernomor + lapisan `<style>`** → sama dengan cara `custom.css` di template sungguhan (bagian 7.11): file provider tidak diubah, gaya pembuat website menang lewat `!important`.
- **Nilai percobaan disimpan di memori (`TrySession`)** → tetap ada saat bolak-balik Tandai ⇄ Coba (bagian 7.12) tetapi tidak pernah dikirim ke server.
- **Gambar percobaan sebagai data URL** → bisa dipasang langsung di WebView tanpa diupload.
- **Library CDN disalin mengikuti susunan folder CDN** (`vendor/font-awesome@6.5.2/css/all.min.css`) → rujukan relatif di CSS library (`../webfonts/…`) tetap benar, jadi ikon juga tampil offline. Google Fonts tidak disalin (bagian 10.5).
- **Pengunduh library dipisah (`LibraryFetcher`)** → test tidak butuh internet; jika CDN tidak terjangkau, template kembali ke draft dengan pesan "coba Kirim lagi", bukan tayang dengan link CDN.
- **Peringatan dari WebView HP (tahap C) disalin ke pengecekan akhir** → tidak hilang saat Kirim.
- **Warna cepat di `arrays_upload.xml`** → ini warna untuk website pembuat (bukan tampilan app), tetap di resource, bukan di kode.

## Cara menjalankan & mengetes
1. Lanjut dari Fase 17 (minimal 3 isian tersimpan) → **Coba**.
2. Ketik di form → preview langsung berubah. Coba warna, ukuran huruf, Ganti foto, HP/Desktop, pindah halaman lewat link.
3. **Uji isi panjang** → semua teks diisi sampai batas, gambar diganti rasio 3:1 / 1:3. **Isi asli** → kembali.
4. **Ubah tandaan ini** → editor Tandai terbuka di elemen itu.
5. **Lanjut: Kirim** → centang hak pakai → **Kirim** → "Template tayang!".
6. Tab Template galeri (mode pembuat website) → template muncul dengan thumbnail; cari dengan salah satu kata kunci.

## Hasil tes
- Backend `./mvnw test`: **lulus, 238 test** (setelah uji HP: tetap lulus, 0 gagal).
- Android `testDebugUnitTest`: **lulus, 62 test**. `lint`: **0 masalah**. `assembleDebug`: berhasil.
- Uji manual di backend dev (database lokal, akun `dummy8`) dengan template sungguhan **Tailwind Toolbox Landing Page**: upload → info → 3 isian (judul, deskripsi, foto) → Kirim → `published` dalam beberapa detik. Isi `package.zip`: `vendor/tailwindcss@2.2.19/dist/tailwind.min.css` (2,9 MB, diunduh dari unpkg), link di `index.html` sudah lokal, Google Fonts tetap dari internet, `data-key` judul/deskripsi/foto tersisip. Template muncul di `GET /api/templates?q=tailwind`. Data uji ini sudah saya hapus lagi dari database dev.
- Uji di HP (8 Oktober 2026, realme RMX3151, akun `demo-provider`): alur Upload lengkap **berhasil**. Rinciannya di bagian "Uji di HP & perbaikan" di bawah.

### Uji di HP & perbaikan (8 Oktober 2026)

Yang dicoba di HP, semuanya berhasil:

1. Tab Upload menampilkan **Perlu diperbaiki → Lanjutkan draft (2 dari 5) → Upload template baru** (rancangan 3.1).
2. Draft "Instansi Desa" dibuka tepat di langkah 3. Tombol **Lanjut** baru aktif setelah deskripsi dan kata kunci terisi (6.4).
3. **Tandai**: ketuk link menu → sheet terbuka sebagian (elemen tetap terlihat) → simpan → muncul tawaran "Bagian ini sama di 2 halaman" → pilih *Semua halaman*. Di slide Hero, **Tandai semua** menambah 3 isian. Simpan → "Semua tandaan tersimpan".
4. **Coba**: mengubah Judul langsung mengubah preview.
5. **Kirim**: centang hak pakai → "Template tayang!". `package.zip` berisi `data-section`, `data-edit`, `data-key`, `data-label`; isian menu header tersisip di **kedua** halaman. Template muncul di `GET /api/templates?q=desa`.
6. **Upload file perbaikan** untuk "Landing Event" → lolos → langsung ke Info (tanpa peringatan, layar hasil dilewati). Kartu "tidak lolos" hilang dari Beranda.
7. ZIP tanpa `index.html` ditolak cek kilat di HP: "File belum bisa diupload".
8. Keluar wizard (✕) menampilkan "Pekerjaanmu tersimpan sebagai draft…".

Temuan yang diperbaiki:

| Temuan | Perbaikan |
|---|---|
| Toggle **HP / Desktop** (Info, Tandai, Coba) tidak menunjukkan pilihan aktif | Style baru `Widget.App.Button.Toggle`: terpilih diisi warna foreground seperti chip terpilih |
| Isian jenis **Link** di Coba tampil kosong; alamat asli tidak terlihat | `RakitTry.original` sudah mengirim `href`; kini dipakai sebagai isi awal input alamat (Link & Tombol) |
| Form Coba tertutup memotong tombol "Lanjut menandai" | `try_sheet_peek` 200dp → 252dp |
| `<p>` pendek ditebak "Teks pendek", padahal pembuat website biasanya mengisinya lebih panjang | `mark.js`: `<p>` dan `<blockquote>` selalu ditebak **Paragraf** |
| `Slider.setTickVisible` sudah deprecated | Diganti `setTickVisibilityMode(TICK_VISIBILITY_HIDDEN)` |
| Test seeder demo gagal compile setelah seeder diberi ZIP sungguhan | Test diperbarui dan menunggu pengecekan async "Organisasi Pemuda" selesai, agar hasilnya tidak bergantung waktu |

Perbaikan dari sesi sebelumnya (belum di-commit, ikut diuji di sini): template demo yang masih di wizard diberi ZIP sungguhan (`seed/DemoSite.java`), header wizard memakai teks pendek `5/6`, tombol Kirim/Coba disusun vertikal, hasil cek tanpa peringatan langsung lanjut ke Info, Beranda provider dimuat ulang saat kembali dari wizard.

### Uji mandiri putaran kedua (8 Oktober 2026)

Data demo direset, lalu dicoba: detail error + **Pelajari cara memperbaikinya** (Panduan) + **Ini keliru? Laporkan** (tersimpan di `check_reports`), thumbnail HP/Desktop/Dari section, aturan kata kunci (huruf kecil, duplikat ditolak, maks 20), label wajib di Tandai, Urungkan/Ulangi, Daftar section, pindah halaman, Uji isi panjang, Isi asli, Kirim nonaktif sebelum dicentang, hapus draft lewat ⋮, dan mode terang.

| Temuan | Perbaikan |
|---|---|
| Error demo "Landing Event" karangan (mis. "index.html tidak ditemukan") tidak cocok dengan ZIP-nya | Seeder menjalankan mesin pengecekan atas ZIP demo; kini muncul `BASE_HREF` yang memang ada di file |
| Thumbnail **Dari section** tetap memotret bagian atas halaman jika halaman pendek, dan scrollbar ikut terpotret | `OffscreenPage.capture` mengukur posisi gulir yang benar-benar tercapai lalu menggeser sisanya saat menggambar; scrollbar dimatikan |
| Ganti HP/Desktop setelah memilih section kembali ke bagian atas halaman | Nama section diingat dan dipotret ulang di lebar baru |
| Preview Coba kadang macet di "Memuat…" | Halaman selesai sebelum jeda 600 ms animasi, lalu animasi muncul belakangan. `state.setVisibility(GONE)` diganti `state.hide()` (juga di Tandai & Pengecekan) |
| Urutan form Coba mengikuti urutan menandai (Deskripsi sebelum Judul) | Isian diurutkan menurut posisi di halaman, di Android (`MarkingEditor`) dan backend (`MarkingData.inPageOrder`) |
| Kartu draft di langkah Tandai tanpa progres isian (rancangan 3.1) | Bar progres + "N isian" |
| Tombol ⋮ di Daftar section hanya 20dp dan tanpa label pembaca layar | Area sentuh 48dp + `contentDescription` |
| Judul masalah dan penjelasannya terlihat sama tebal | Penjelasan memakai berat reguler; judul memakai style baru `TextAppearance.App.Body.SemiBold` |

Catatan: HP realme Aris memakai pengaturan **ketebalan font** (`font_variation_settings=550`) yang menyamakan ketebalan semua font. Di HP itu, perbedaan regular/semibold memang hampir tidak terlihat.

### Uji dengan template Kedai Kopi Senja (8 Oktober 2026)

Aris mengupload `contoh-landing-page/` dan menandai 31 isian. Langkah **Coba** membuat app tidak merespons (ANR).

| Temuan | Penyebab | Perbaikan |
|---|---|---|
| App beku lalu ditutup sistem saat membuka Coba | Teks asli diambil dengan 31 panggilan JavaScript terpisah, dan setiap jawaban yang datang setelah semuanya terkumpul membangun ulang seluruh form | `RakitTry.originals(ids)` mengambil semua sekaligus; form dibangun sekali |
| Form 31 isian masih menahan layar ±3 detik | Setiap pemilih warna membuat 11 `Chip` (±550 Chip) | Contoh warna memakai `View` bulat ringan; form dibangun 3 isian per frame |
| Isian Paragraf menunjuk 3 `<article>` card yang judul dan deskripsinya juga ditandai | Belum ada aturan tandaan bersarang | **Keputusan Aris: tolak saat menyimpan.** Teks/Paragraf/Tombol tidak boleh membungkus isian lain, dan elemen di dalam isian jenis itu tidak bisa ditandai. Gambar dan Link boleh membungkus. Dicek di sheet Tandai (peringatan merah, Simpan nonaktif), saran "Tandai semua", dan server (`MarkingValidator.validateNesting`, saat Simpan dan Kirim) |
| Alasan server menolak Simpan tidak terlihat | Snackbar umum "Tandaan gagal disimpan" | Pesan `VALIDATION_ERROR` dari server tampil di dialog |

Cara mengukur macetnya UI: `adb logcat | grep "Skipped .* frames"` (dari ±285 frame menjadi ±40), dan stack trace thread utama lewat `jdb` (`adb forward tcp:8700 jdwp:<pid>`, lalu `suspend` + `where`).

## Konsep yang dipelajari
- **CSS `!important` dan urutan stylesheet**: aturan yang dimuat terakhir dan bertanda `!important` mengalahkan aturan lain.
- **Data URL**: gambar ditulis langsung sebagai teks base64 di dalam atribut `src`.
- **Rasio kontras**: rumus luminansi untuk menilai apakah teks terbaca di atas latarnya.
- **Interface + pengganti di test (fake)**: kode yang butuh internet dibungkus interface agar test bisa memakai versi palsu.
- **BottomSheetBehavior**: panel yang bisa ditarik naik/turun di atas layar.

## Latihan untuk Aris
1. Tambahkan satu warna cepat baru di `arrays_upload.xml`, lalu coba di langkah Coba.
2. Di `PackageBuilderTest`, tambahkan kasus library dari unpkg dan pastikan path lokalnya benar.
3. Kirim template, lalu buka `backend/uploads/templates/<id>/package.zip` dan cari atribut `data-key` di `index.html`.

## Yang perlu Aris lakukan
- Klik **Sync Now**, sambungkan HP, lalu uji alur penuh Fase 16–18 (langkah di catatan fase 16, 17, dan di atas).
- Putuskan font `.eot` (masih Error sesuai rancangan).

## Rencana berikutnya
- Uji alur Upload lengkap di HP dan perbaiki temuan.
- Yang masih menunggu diskusi (rancangan bagian 13–14): perbaikan otomatis, versi template, penandaan tingkat section, render di server, push FCM.
