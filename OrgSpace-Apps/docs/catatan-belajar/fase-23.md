# Fase 23 (T5): Tab Gaya, tema & undo/redo

Sumber rancangan: [`alur-buat-website-via-template.md`](../rancangan/alur-buat-website-via-template.md) bagian 6.2, 6.5, 11.4.

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `core/template/CustomCss` | Membuat `custom.css`: `:root{--primary:…}` + `[data-key="…"]{…!important}` (preview) atau `.ws-…` (export). Hanya properti yang diizinkan manifest, warna hex valid, angka dalam rentang |
| `core/template/ColorContrast` | Rasio kontras WCAG; menerima `#rgb`, `#rrggbb`, `rgb()/rgba()` dari halaman |
| `ui/editor/EditorForm` (tab Gaya) | Tema website (paling atas), gaya per section: kotak warna + dialog pemilih, Slider ukuran huruf/sudut mulai dari ukuran asli, tombol ↺ kembalikan, peringatan "Kontras teks rendah" |
| `ui/editor/ColorPickerDialog` | Palet warna + input hex + "Asli" |
| `assets/editor/editor.js` (`computed`) | Ukuran huruf, sudut, warna teks & latar yang sedang berlaku (untuk posisi awal slider dan cek kontras) |
| `ui/editor/TemplateEditorViewModel` (undo/redo) | Riwayat maks 50 langkah; ketikan beruntun di isian yang sama dalam 1,5 detik digabung jadi satu langkah |
| `CustomCssTest`, `ColorContrastTest` | Selector preview/export, input jahat tidak masuk CSS, normalisasi warna & ukuran, kontras |

## Alasan keputusan
- **Gaya ditulis ke satu lapisan CSS paling akhir dengan `!important`** (rancangan 11.4) → file CSS template tidak pernah diubah; menghapus `custom.css` mengembalikan tampilan asli.
- **Input user tidak pernah ditulis mentah**: `red;}body{display:none` atau `url(javascript:…)` ditolak diam-diam → tidak bisa merusak website atau menyisipkan kode.
- **Batas kontras 4,5** (WCAG AA teks biasa) seperti rancangan; peringatan hanya muncul jika user mengubah warna, dan tidak memblokir.
- **Latar transparan dicari ke pembungkus terdekat yang berwarna** → kontras dihitung terhadap warna yang benar-benar terlihat.
- **Satu langkah undo per "sesi ketik"** → tanpa penggabungan, undo menghapus satu huruf per ketukan.

## Cara menjalankan & mengetes
1. Profil Sekolah → tab Gaya → Tema website → Warna utama → pilih hijau → header & judul semua halaman berubah.
2. Section Hero → Judul utama → Ukuran huruf: slider mulai dari ukuran asli; geser → judul membesar.
3. Pilih warna teks yang hampir sama dengan latar → muncul "Kontras teks rendah"; perubahan tetap tersimpan.
4. ↶ beberapa kali → kembali ke warna asli; ↷ → maju lagi.

## Hasil tes
- `CustomCssTest`, `ColorContrastTest` lulus. Uji di HP menunggu Fase 26.

## Konsep yang dipelajari
- **CSS cascade & `!important`**: aturan yang dimuat terakhir dan bertanda !important mengalahkan aturan template.
- **Variabel CSS (`--primary`)**: satu nilai dipakai banyak elemen; mengubah satu variabel mengubah semuanya.
- **Undo stack**: menyimpan salinan nilai sebelum berubah; undo = ambil salinan teratas.

## Latihan untuk Aris
1. Tambahkan warna `#0F766E` ke `try_color_presets` dan pakai di tema.
2. Buat test baru di `CustomCssTest` untuk `border-radius` di luar rentang manifest.

## Yang perlu Aris lakukan
- Tidak ada.

## Rencana fase berikutnya
- Fase 24 (T6): kelengkapan & status.
