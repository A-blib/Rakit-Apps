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
- Backend `./mvnw test`: **lulus, 238 test**.
- Android `testDebugUnitTest`: **lulus, 62 test**. `lint`: **0 masalah**. `assembleDebug`: berhasil.
- Uji di HP: **belum dilakukan** (HP tidak tersambung selama Fase 16–18).

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
