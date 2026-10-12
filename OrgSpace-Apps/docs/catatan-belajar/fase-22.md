# Fase 22 (T4): Ganti gambar

Sumber rancangan: [`alur-buat-website-via-template.md`](../rancangan/alur-buat-website-via-template.md) bagian 6.6.

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `core/template/ImageProcessor` | Baca foto, perbaiki orientasi EXIF, potong tengah sesuai rasio isian, perkecil maks 1920 px, simpan **WebP kualitas 80** sebagai `images/{key}-{waktu}.webp` |
| `ui/editor/TemplateEditorFragment` | **Photo Picker** (`PickVisualMedia`, hanya gambar), indikator loading di isian, "Kembalikan foto contoh", hapus file lama yang tidak dipakai lagi |
| `ui/editor/EditorPreview` | Path khusus `/_rakit/project/…` menyajikan gambar dari folder project (bukan folder paket) |
| `ui/editor/TemplateEditorViewModel.imagesInUse` | File yang masih dipakai nilai sekarang atau riwayat undo/redo tidak dihapus |
| `res/drawable/ic_image.xml` (+ `ic_visibility`, `ic_download`, `ic_share`) | Ikon Material Symbols Outlined resmi |
| `ImageProcessorTest` | Potong tengah 16:9, 1:1, 4:3, tanpa rasio; ukuran baca (inSampleSize) |

## Alasan keputusan
- **`android.media.ExifInterface` bawaan** (keputusan Aris) → bisa membaca dari stream sejak API 24 dan minSdk app 26, jadi tidak perlu `androidx.exifinterface`.
- **Dibaca sudah diperkecil (`inSampleSize`)** → foto kamera 50 MP (±200 MB di memori) tidak membuat app kehabisan memori.
- **WebP lossy 80** → ukuran biasanya 5–10× lebih kecil dari JPG kamera dengan kualitas yang masih bagus di layar.
- **File lama tidak langsung dihapus jika masih ada di riwayat undo** → urungkan tetap bisa mengembalikan foto sebelumnya.
- **Photo Picker** → tidak butuh izin baca penyimpanan; di Android lama otomatis memakai pemilih dokumen.

## Cara menjalankan & mengetes
1. Editor UMKM Kuliner → Foto utama → Ganti foto → pilih foto tegak dari galeri.
2. Preview menampilkan foto terpotong 16:9; thumbnail kecil di form ikut berubah.
3. Device Explorer → `files/projects/<id>/images/` → file `.webp` berukuran ratusan KB.
4. "Kembalikan foto contoh" → foto contoh kembali; urungkan → foto pilihan kembali.

## Hasil tes
- Unit test `ImageProcessorTest` lulus. Uji di HP menunggu Fase 26.

## Konsep yang dipelajari
- **EXIF orientation**: kamera menyimpan foto apa adanya + catatan "putar 90°"; app yang tidak membacanya menampilkan foto miring.
- **inSampleSize**: membaca gambar dengan melewati piksel (1/2, 1/4, …) agar hemat memori.
- **Center crop**: memotong sisi yang berlebih secara seimbang sehingga bagian tengah foto tetap terlihat.

## Latihan untuk Aris
1. Ubah `MAX_SIDE` menjadi 1280, ganti foto lagi, dan bandingkan ukuran file `.webp`-nya.
2. Tambahkan test `cropRect` untuk rasio "3:4".

## Yang perlu Aris lakukan
- Tidak ada.

## Rencana fase berikutnya
- Fase 23 (T5): tab Gaya, tema, peringatan kontras, undo/redo.
