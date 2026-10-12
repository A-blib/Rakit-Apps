# Fase 25 (T7): Export ZIP

Sumber rancangan: [`alur-buat-website-via-template.md`](../rancangan/alur-buat-website-via-template.md) bagian 8–10.

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `core/template/ProjectExporter` | ZIP: salin paket, terapkan nilai dengan **jsoup** (teks selalu teks biasa, ikon tombol dipertahankan), gambar ke `img/user/`, `custom.css` dimuat paling akhir, hapus `data-edit/key/label/section/tpl-id`, buang `manifest.json` & foto contoh yang sudah diganti |
| `core/template/AndroidImageCompressor` | Opsi "Kompres foto agar ringan": JPG/WebP bawaan template > 150 KB diperkecil (maks 1920 px, kualitas 80), nama & format tetap |
| `core/template/ExportNames` | "Dapur Mama Rina" → `dapur-mama-rina.zip` |
| `ui/editor/ExportDialog`, `layout/dialog_export.xml` | Layar Export: ringkasan, nama file, isi ZIP & perkiraan ukuran, opsi kompres, progres, **Simpan ke HP** (`ACTION_CREATE_DOCUMENT`) dan **Bagikan** (`FileProvider` + `ACTION_SEND`) |
| `ui/editor/ExportDoneDialog`, `layout/dialog_export_done.xml` | Layar Selesai: "Website siap", Buka panduan (artikel "Cara mengonlinekan file ZIP" yang sudah ada), Bagikan ZIP, Kembali ke project |
| `ui/editor/TemplateEditorViewModel` | `buildExport` (di thread disk setelah autosave terakhir), `onExportDelivered` (status Diexport + event `download` hanya di export pertama) |
| `AndroidManifest.xml`, `res/xml/file_paths.xml` | `FileProvider` hanya untuk folder `cache/exports/` |
| `TemplateApp` | Saat app dibuka: hapus paket tak terpakai > 30 hari, jadwalkan ulang event tertunda |
| `ProjectExporterTest`, `ExportNamesTest`, `test/resources/template-packages/toko-kue/` | Export dengan paket contoh (2 halaman, subfolder), HTML bersih, link jahat tidak dipasang, kompresor |

## Alasan keputusan
- **jsoup di Android** (keputusan Aris) → HTML template sering tidak rapi; parser sungguhan menjaga struktur dan meng-escape teks. (Alternatif: mengambil DOM dari WebView → ikut membawa perubahan DOM dari script template.)
- **ZIP dibuat dulu di cache, baru disalin ke lokasi pilihan / dibagikan** → satu proses export untuk dua tujuan; jika user batal memilih lokasi, tidak ada file setengah jadi di HP.
- **Project baru dibuat dulu sebelum export** → status "Diexport" dan event download butuh project sungguhan.
- **Event `download` dikirim lewat antrean**, hanya saat `last_exported_at` masih kosong → export offline tetap terhitung setelah online, dan hanya sekali (server juga menolak ganda per project).
- **PNG tidak dikompres** → biasanya logo/ikon yang butuh transparansi; mengubahnya ke JPG akan mengganti nama file dan merusak rujukan.

## Cara menjalankan & mengetes
1. Editor → ⋮ → Export → Simpan ke HP → pilih folder Download → layar Selesai.
2. Salin ZIP ke laptop (`adb pull /sdcard/Download/umkm-kuliner.zip`), ekstrak, buka `index.html` di browser: isi baru tampil, `custom.css` ada, tidak ada atribut `data-*` (cek dengan View Source / `grep data-key`).
3. Export → Bagikan → pilih WhatsApp/Drive.
4. Mode pesawat → Export pertama → nyalakan internet → event `download` terkirim sekali (lihat tabel `template_events` di psql).

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 94 test** (32 baru di fitur ini); `./gradlew lint`: **0 masalah**; `assembleDebug` berhasil.
- Uji di HP: menunggu HP tersambung (Fase 26).

## Konsep yang dipelajari
- **Storage Access Framework (`ACTION_CREATE_DOCUMENT`)**: user memilih lokasi simpan; app mendapat Uri untuk ditulisi tanpa izin penyimpanan.
- **FileProvider**: membagikan file milik app ke app lain lewat `content://` sementara, bukan path file langsung.
- **Escape HTML**: `<b>` yang diketik user ditulis sebagai `&lt;b&gt;` sehingga tampil sebagai teks.

## Latihan untuk Aris
1. Export project, buka ZIP-nya, dan cari class `ws-…` di HTML serta aturannya di `custom.css`.
2. Ubah `MIN_BYTES` di `AndroidImageCompressor` menjadi 10 KB, export ulang, dan bandingkan ukuran ZIP.

## Yang perlu Aris lakukan
- Tidak ada perintah baru.

## Rencana fase berikutnya
- Fase 26 (T8): uji 19 skenario di HP asli, dark mode, screenshot, polesan.
