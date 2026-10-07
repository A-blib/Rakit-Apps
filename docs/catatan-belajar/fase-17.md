# Fase 17: Editor Tandai bagian

Sumber rancangan: [`docs/rancangan/alur-fitur-upload.md`](../rancangan/alur-fitur-upload.md) bagian 7 dan 11. Penjelasan cara kerjanya ada di [`dokumentasi-project.md` bab 27](../dokumentasi-project.md#27-editor-tandai-bagian).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| Backend `V15__template_marking.sql`, `Template.marking`/`markingFieldCount` | Tandaan disimpan sebagai JSON + jumlah isian |
| Backend `upload/marking/TemplateNumbering` | Nomor `data-tpl-id` di setiap elemen HTML asli (jsoup, tanpa mengubah spasi) |
| Backend `upload/marking/MarkingService`, `MarkingController`, `MarkingValidator`, `MarkingData` | ZIP salinan bernomor (`/work-package`), simpan tandaan dengan pengecekan (label, kunci, elemen benar-benar ada di HTML asli, gaya, tema) |
| Android `assets/upload/mark.js`, `desktop.js` | Skrip di WebView: tangkap ketukan, sorotan & label, slide, daftar elemen, saran, teks mirip, visibilitas, sidik jari section; viewport 1280 px untuk Desktop |
| `ui/upload/MarkWebView` | WebView mode tandai + jembatan JS yang hanya menerima nomor elemen |
| `ui/upload/MarkingEditor` | Data tandaan, kunci otomatis, hubungkan elemen, koreksi section, saran, tema, undo/redo, penghitung "belum disimpan" |
| `ui/upload/MarkEditorViewModel`, `core/upload/MarkingBackupStore` | Muat draft + tandaan + salinan bernomor; cadangan otomatis di HP; Simpan |
| `ui/upload/UploadMarkFragment`, `fragment_upload_mark.xml` | Layar tiga zona: halaman, HP/Desktop, penghitung · slide · saran, toolbar, strip, Simpan/Coba |
| `ui/upload/MarkElementSheet`, `sheet_mark_element.xml` | Bottom sheet Tandai elemen: jenis, label, petunjuk, maks karakter, wajib, rasio gambar, gaya, hubungkan, induk/anak, jadikan section, hapus tandaan |
| `ui/upload/MarkPanels` | Daftar elemen (termasuk yang tersembunyi), panel semua isian + tema global, daftar section (ganti nama, gabung, hapus) |
| `ui/upload/PageScanner` | Pindai halaman lain di WebView tersembunyi: section + sidik jari untuk "tandai sekali untuk semua halaman" |
| `core/storage/EditorTourStore`, Panduan "Menandai bagian yang bisa diedit" | Tur 4 langkah pertama kali; isi Panduan |
| `ui/upload/UploadTryFragment` | Langkah 5 sementara (diisi di Fase 18) |
| Test | Backend `TemplateNumberingTest` (3), `UploadIntegrationTest.workPackageIsNumberedAndMarkingIsValidatedOnSave`; Android `MarkingEditorTest` (9) |

## Alasan keputusan
- **Nomor dibuat server dengan jsoup, HP mengunduh salinannya** → server dan HP pasti memakai nomor yang sama, dan saat Kirim server menemukan elemen yang sama lagi.
- **Jembatan JS hanya `select(nomor)` dan `selectGenerated()`** (bagian 7.9) → JavaScript provider berjalan di halaman yang sama; data lain diminta Java lewat `evaluateJavascript`.
- **Satu WebView, area lain diredupkan dan gulir dibatasi** (bagian 7.2) → tinggi bingkai = tinggi section × skala, maksimal setinggi ruang tengah; section panjang bisa digulir di dalam bingkai.
- **Batas section dihitung dari elemen awal tiap section** (section pertama dari atas halaman, terakhir sampai ujung) → menggabung = menghapus section berikutnya, memecah = menyisipkan section di elemen terpilih; tidak ada celah tanpa section.
- **Strip slide berupa chip bernomor + ✓** → thumbnail per section membutuhkan potret setiap section (berat di HP biasa). Status ✓/terpilih tetap terlihat. Bisa diganti thumbnail jika nanti dibutuhkan.
- **Sidik jari section = HTML tanpa nomor** → header yang sama persis di beberapa halaman punya sidik jari sama; elemen dipasangkan menurut urutannya.
- **Undo/redo menyimpan salinan JSON** → data tandaan kecil, cara ini sederhana dan pasti benar.
- **Maks karakter disarankan 1,5× panjang teks asli** (minimal +10); **ukuran huruf ±20%**; **sudut 0–32 px** (bagian 7.11).

## Cara menjalankan & mengetes
1. Lanjutkan dari Fase 16: draft "Langkah 4/6" di tab Upload → ketuk.
2. Tur 4 langkah muncul sekali. Strip bawah berisi section (Header, Hero, …).
3. Ketuk judul → bottom sheet → label "Judul utama" → Simpan → label hijau ✓ di slide, penghitung naik, "● 1 perubahan belum disimpan".
4. Coba Induk/Anak, Daftar elemen (ikon daftar), Saran "Tandai semua", Undo/Redo.
5. Desktop → slide memakai lebar 1280 px. Elemen yang hanya tampil di satu tampilan berlabel HANYA HP/HANYA DESKTOP.
6. Template multi-halaman dengan header sama: menandai teks header menawarkan "Bagian ini sama di N halaman".
7. Simpan → "Semua tandaan tersimpan"; tombol Coba aktif.
8. Ubah sesuatu lalu tutup paksa app → buka lagi → "Pulihkan perubahan?".

## Hasil tes
- Backend `./mvnw test`: **lulus, 233 test**.
- Android `testDebugUnitTest`: **lulus, 62 test** (9 baru). `lint`: **0 masalah**. `assembleDebug`: berhasil.
- Uji di HP: **belum dilakukan** (HP tidak tersambung).

## Konsep yang dipelajari
- **@JavascriptInterface**: method Java yang bisa dipanggil JavaScript; harus dibatasi karena semua script di halaman bisa memanggilnya.
- **addDocumentStartJavaScript**: menjalankan skrip sebelum halaman mulai dimuat.
- **Event capture** (`addEventListener(..., true)`): skrip kita menangkap ketukan sebelum skrip template.
- **Undo dengan snapshot**: simpan keadaan sebelum perubahan, kembalikan saat undo.
- **Sidik jari (hash)**: angka pendek yang mewakili isi; isi sama → hash sama.

## Latihan untuk Aris
1. Ubah rentang ukuran huruf bawaan dari ±20% menjadi ±30% di `MarkElementSheet.FONT_RANGE`.
2. Tambahkan nama tebakan section baru di `sections.js`, lalu lihat namanya di strip.
3. Di `MarkingEditorTest`, tambahkan test bahwa `uniqueKey("123 Promo")` menghasilkan kunci yang diawali huruf.

## Yang perlu Aris lakukan
- Sambungkan HP dan uji langkah di atas.

## Rencana fase berikutnya
- **Fase 18**: Coba sebagai pengguna, Kirim (pengecekan akhir, salin library CDN, sisipkan data-key/data-edit/data-label/data-section), tayang di galeri.
