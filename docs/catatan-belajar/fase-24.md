# Fase 24 (T6): Kelengkapan & status project

Sumber rancangan: [`alur-buat-website-via-template.md`](../rancangan/alur-buat-website-via-template.md) bagian 7.

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `core/template/CompletenessChecker` | Aturan 7.1: wajib kosong / wajib masih contoh / link tidak valid → **Perlu dilengkapi**; opsional masih contoh → **Saran**; status `draft`/`ready`/`exported` |
| `ui/editor/CompletenessDialog`, `layout/dialog_completeness.xml` | Layar Kelengkapan: "9/12 isian lengkap", progres, badge, daftar, Lengkapi sekarang, Export tetap sebagai draft (dengan konfirmasi D1) |
| `ui/editor/TemplateEditorFragment` | Chip section bertanda ✓ / ·N, tanda "Masih teks contoh" per isian, badge status di app bar, "Ada perubahan sejak export terakhir", Export saat Draft membuka Kelengkapan dulu |
| `values/themes.xml` | `Theme.App.FullScreenDialog` untuk Kelengkapan dan Export |
| `CompletenessCheckerTest` | 8 test untuk semua cabang aturan |

## Alasan keputusan
- **Kelengkapan & Export sebagai dialog layar penuh di atas editor**, bukan layar navigasi baru → memakai ViewModel editor yang sama, termasuk nilai yang belum tersimpan. (Alternatif: layar terpisah yang membaca ulang `values.json` → bisa ketinggalan ketikan terakhir.)
- **"Masih contoh" untuk link/tombol = link belum diganti dan menunjuk ke luar website** → nomor WhatsApp contoh (`wa.me/6281234567890`) wajib diganti, tapi link ke halaman sendiri (`kontak.html`) dianggap sudah benar, dan teks tombol "Daftar sekarang" boleh tetap. Ini tafsiran yang belum tertulis di rancangan; Aris bisa mengubahnya.
- **Mengetik ulang teks contoh persis sama tetap dihitung "masih contoh"** → yang dinilai isinya, bukan apakah kolom pernah disentuh.
- **Link tidak valid di isian opsional tetap "Perlu dilengkapi"** → link rusak di website lebih buruk daripada link contoh.

## Cara menjalankan & mengetes
1. Editor baru → chip section menunjukkan ·N; status DRAFT.
2. ⋮ → Cek kelengkapan → daftar "Perlu dilengkapi"; ketuk satu baris → editor membuka isian itu (pindah halaman jika perlu).
3. Lengkapi semua isian wajib → status SIAP EXPORT, chip ✓.
4. Saat masih Draft, ⋮ → Export → layar Kelengkapan muncul; "Export tetap sebagai draft" → konfirmasi → layar Export.

## Hasil tes
- `CompletenessCheckerTest` (8 test) lulus. Uji di HP menunggu Fase 26.

## Konsep yang dipelajari
- **Logika murni (pure function)**: `check(manifest, values)` tidak bergantung pada Android, sehingga mudah diuji di laptop.
- **DialogFragment + ViewModel induk**: dialog meminjam ViewModel milik fragment induk lewat `requireParentFragment()`.

## Latihan untuk Aris
1. Tambahkan aturan: isian teks yang lebih pendek dari 3 huruf dianggap "Perlu dilengkapi", lalu tulis test-nya.
2. Ubah teks "Masih teks contoh" di `strings_template.xml` dan lihat hasilnya di editor.

## Yang perlu Aris lakukan
- Putuskan apakah aturan "masih contoh" untuk tombol/link (lihat Alasan keputusan) sudah sesuai.

## Rencana fase berikutnya
- Fase 25 (T7): export ZIP, simpan & bagikan, layar Selesai, event download.
