# Menandai Bagian Template

Di langkah **Tandai** (Upload langkah 4), kamu memilih elemen yang boleh diganti pembuat website. Elemen yang tidak ditandai tidak bisa diubah, dan **tata letak selalu terkunci**.

## Dua jenis isi di template OrgSpace

| Jenis | Siapa yang mengisi | Contoh | Cara membuat |
|---|---|---|---|
| **Isian Tandai** | Pembuat website, di editor app, lalu diterbitkan | Judul hero, visi-misi, foto utama, teks footer | Elemen HTML biasa, ditandai di app |
| **Data modul** | Pengurus, di Portal Pengurus, tanpa terbit ulang | Berita, agenda, prestasi, galeri, isian formulir | Atribut `data-ws-*` (dokumentasi OrgSpace: `/docs/modul-sistem-dan-kustom`) di HTML |

## Cara menandai

1. Tandai per **halaman** dan per **section** (ditampilkan seperti slide). Ganti tampilan **HP / Desktop** untuk elemen yang hanya tampil di salah satunya.
2. Ketuk elemen. Pakai **Induk ↑** / **Anak ↓** untuk memperluas atau mempersempit pilihan, atau pilih dari daftar elemen (untuk menu tersembunyi, isi tab, dan elemen kecil).
3. Atur isian di lembar bawah, lalu **Simpan**. Tandaan disimpan ke server lewat tombol Simpan, dengan cadangan otomatis di HP.

| Pengaturan | Keterangan |
|---|---|
| Jenis | **Teks pendek**, **Paragraf**, **Gambar**, **Link**, atau **Tombol** (teks + link). Ditebak otomatis |
| Label | Wajib. Nama isian di editor, mis. "Judul utama" |
| Petunjuk | Opsional. Bantuan untuk pembuat website, mis. "Maks 3 kata" |
| Maks karakter / wajib / rasio gambar | Disarankan otomatis dari isi asli agar tata letak tidak rusak |
| Juga boleh diubah (gaya) | Warna teks, warna latar, ukuran huruf (dengan rentang), sudut |
| Hubungkan ke isian lain | Teks yang sama di beberapa tempat (versi HP & desktop, header di semua halaman) menjadi satu isian |
| Hubungkan ke profil organisasi | Nama, singkatan, logo, institusi, alamat, email, WhatsApp, Instagram, TikTok, YouTube terisi otomatis. Centang **Sembunyikan jika kosong** untuk kontak dan media sosial |

## Tema

Variabel CSS di `:root` (mis. `--primary`, `--accent`, `--radius`) bisa didaftarkan sebagai **tema** dengan jenis **warna** atau **ukuran**. Pembuat website mengubahnya di tab Gaya → **Tema website**, dan seluruh template ikut berubah.

```css
:root {
  --primary: #2563eb;
  --accent: #f59e0b;
  --radius: 12px;
}
.tombol { background: var(--primary); border-radius: var(--radius); }
```

## Aturan

- Minimal **3 isian** sebelum template bisa dikirim, dan minimal satu pilihan **isi template** di langkah Info.
- **Teks pendek, Paragraf, dan Tombol** mengganti seluruh isi elemen, jadi tidak boleh membungkus elemen lain yang ditandai. **Gambar** dan **Link** hanya mengubah `src`/`href`, jadi boleh membungkus.
- Elemen yang dibuat JavaScript (tidak ada di HTML asli) **tidak bisa ditandai**. Tulis isinya langsung di HTML.
- Elemen modul dinamis **tidak bisa ditandai**, karena isinya diisi otomatis saat website tayang:
  - elemen ber-`data-ws-field`, `data-ws-stat`, atau `data-ws-org` (beserta isinya);
  - **Link/Tombol** pada elemen ber-`data-ws-link` atau `data-ws-auth` (jenis Teks pendek masih boleh);
  - Teks/Paragraf/Tombol yang **membungkus** modul dinamis.
- Label formulir, teks `data-ws-empty`, dan teks statis lain di dalam kartu tetap boleh ditandai.

## Mencoba hasilnya

Di langkah **Coba sebagai pengguna**, kamu melihat form edit persis seperti pembuat website. Isian yang kurang pas bisa langsung dibuka kembali di langkah Tandai.
