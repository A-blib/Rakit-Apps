# Fase 21 (T3): Editor — preview & tab Isi

Sumber rancangan: [`alur-buat-website-via-template.md`](../rancangan/alur-buat-website-via-template.md) bagian 3, 6.1–6.4, 6.7, 11.3, 11.5. Penjelasan: [`dokumentasi-project.md` bab 30](../dokumentasi-project.md#30-editor-template-mode-di-android).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `ui/editor/TemplateEditorFragment` | Layar editor: app bar (nama ✎, status, tersimpan), pemilih halaman, HP/Desktop, preview, bottom sheet (chip section, tab Isi/Gaya, form), mode fokus keyboard, preview layar penuh, menu ⋮ |
| `ui/editor/TemplateEditorViewModel` | Memuat paket/project, menyimpan nilai di memori, autosave (jeda 500 ms), membuat project saat **perubahan pertama**, nama unik "Toko Kue (2)", ganti nama, hapus |
| `ui/editor/EditorPreview`, `assets/editor/editor.js` | WebView + `WebViewAssetLoader`; skrip suntik menerapkan nilai lewat `textContent`, menyorot elemen, melaporkan ketukan `RakitBridge.tap(key)` |
| `ui/editor/EditorForm`, `layout/item_editor_field.xml` | Form per jenis isian: teks (batas karakter + penghitung), paragraf, link (+ bantuan nomor WhatsApp), tombol (teks + link), gambar; tanda "Masih teks contoh" |
| `data/model/ProjectValues`, `core/template/ProjectStore` | Isi `values.json` dan folder project (`values.json`, `thumbnail.webp`, `images/`); tulis lewat file sementara lalu rename |
| `core/template/LinkRules` | Link yang diizinkan: `https://`, `http://`, `mailto:`, `tel:`, `https://wa.me/…`; bantuan nomor WhatsApp → `wa.me/62…` |
| `data/local/ProjectDao`, `data/repository/ProjectRepository` | Nama unik, simpan isi + status, thumbnail, tandai diexport; Duplikat & Hapus kini ikut menyalin/menghapus folder project |
| `ui/upload/SiteWebView`, `LocalSitePathHandler`, `MarkWebView` | Dipakai ulang: `configure` menerima penyaji file sendiri (paket + gambar project) |
| `ui/editor/EditorNav`, `nav_graph.xml` | Editor template sungguhan menggantikan "Segera hadir"; kartu project template membuka project-nya |
| `ui/editor/ComingSoonEditorFragment`, `strings.xml` | Versi "Segera hadir" editor template dihapus (custom mode tetap) |

## Alasan keputusan
- **ID project dipesan sejak editor dibuka, tetapi baris database baru dibuat saat simpan pertama** → semua simpanan beruntun menuju project yang sama (thread disk hanya satu), dan foto yang dipilih sebelum project tersimpan masuk ke folder yang benar. (Alternatif: membuat project saat editor dibuka → bertentangan dengan keputusan "keluar tanpa perubahan = tidak ada project".)
- **Preview diubah lewat `evaluateJavascript` per isian**, tidak memuat ulang halaman → mengetik terasa langsung.
- **Isi contoh disembunyikan sebentar (`visibility:hidden`) sampai nilai project diterapkan** → tidak ada kedipan teks contoh saat halaman dibuka.
- **Jembatan JS → Java hanya `tap(key)` dan key dicek terhadap manifest** → JavaScript template ikut berjalan di halaman yang sama, jadi pintunya dibuat sesempit mungkin.
- **Link yang belum valid tetap tersimpan di form tetapi tidak dipasang ke preview/export** → teks yang sedang diketik tidak hilang, tapi `javascript:` tidak pernah masuk ke website.
- **Thumbnail diambil dari preview yang sedang tampil saat editor ditutup, hanya jika itu halaman utama** → sederhana dan tanpa WebView tambahan; halaman lain tidak menimpa thumbnail beranda.
- **Form dibangun bertahap 3 isian per frame** → pelajaran ANR dari langkah Coba (Fase 18).

## Cara menjalankan & mengetes
1. Buka template → editor. Ubah "Nama toko" → preview berubah saat mengetik; status "menyimpan…" lalu "tersimpan".
2. Kembali → tab Project: project baru "UMKM Kuliner" berstatus Draft. Buka editor lagi tanpa mengubah apa pun dari template lain → tidak ada project baru.
3. Ketuk judul di preview → form membuka isian judul. Ketuk chip "Kontak" → preview menggulir.
4. Profil Sekolah: ubah "Nama sekolah" → ganti halaman ke Tentang/Kontak, nama ikut berubah.
5. Tutup app sepenuhnya, buka project lagi → nilai tetap ada.

## Hasil tes
- `./gradlew testDebugUnitTest`: lulus (94 test setelah Fase 25); `lint`: 0 masalah.
- Uji di HP: menunggu HP tersambung (Fase 26).

## Konsep yang dipelajari
- **Autosave dengan debounce**: simpan dijadwalkan 500 ms setelah ketikan terakhir; ketikan baru menggeser jadwal itu.
- **Penulisan atomik**: tulis ke file sementara lalu rename, agar file tidak pernah setengah tertulis.
- **`textContent` vs `innerHTML`**: textContent menulis teks apa adanya, sehingga `<script>` yang diketik user tidak dijalankan.
- **`@JavascriptInterface`**: method Java yang bisa dipanggil JavaScript halaman; semua inputnya harus dianggap tidak tepercaya.

## Latihan untuk Aris
1. Ubah `SAVE_DELAY_MS` menjadi 3000 dan perhatikan kapan "tersimpan" muncul.
2. Buka `files/projects/<id>/values.json` lewat Device Explorer Android Studio setelah mengubah beberapa isian.
3. Tambahkan skema `sms:` ke `LinkRules` beserta test-nya.

## Yang perlu Aris lakukan
- Tidak ada perintah baru. Run ulang app dari Android Studio.

## Rencana fase berikutnya
- Fase 22 (T4): ganti foto (Photo Picker, potong tengah, WebP).
