# OrgSpace - Paket Contoh Template Resmi (Provider Reference)

Folder ini berisi **contoh template website resmi** yang dirancang khusus untuk pengembang/desainer (*provider*) yang ingin membuat template untuk platform **OrgSpace**.

Template ini telah diverifikasi dan **100% kompatibel** dengan engine OrgSpace, mematuhi semua kriteria keamanan, serta mendukung modul dinamis bawaan dan modul kustom.

---

## 📁 Struktur Direktori

```text
OrgSpace-Templates/
├── README.md                           ← Panduan lengkap pengembangan template
├── template-organisasi-modern.zip       ← Berkas arsip ZIP siap unggah/uji coba
└── template-organisasi-modern/         ← Sumber kode terbuka (bisa dibuka di VS Code)
    ├── index.html                      ← Halaman beranda utama di akar direktori
    ├── css/
    │   └── style.css                   ← Berkas gaya tampilan modern & responsif
    ├── js/
    │   └── main.js                     ← Script interaktivitas ringan (Vanilla JS)
    └── img/
        ├── logo.png                    ← Logo organisasi contoh
        ├── favicon.png                 ← Ikon website
        └── berita-default.jpg          ← Foto sampul berita contoh
```

---

## 🚀 Fitur Unggulan di Template Ini

1. **Responsif & Mobile-First**: Tata letak otomatis menyesuaikan layar ponsel cerdas, tablet, dan desktop dengan font Google (*Plus Jakarta Sans*).
2. **Bilah Status & Portal Pengurus (`data-ws-auth="login"`)**: Tautan navigasi otomatis mengarah ke portal admin organisasi `/[slug]/admin`.
3. **Modul Kustom Dinamis Bebas (`data-ws-collection="prestasi"`)**: Menampilkan daftar pencapaian/prestasi yang datanya diisi oleh pengurus melalui portal admin.
4. **Modul Berita Bawaan (`data-ws-list="berita"`)**: Menampilkan daftar warta dan agenda organisasi lengkap dengan foto, tanggal, dan ringkasan.
5. **Modul Formulir Pendaftaran (`data-ws-form="pendaftaran"`)**: Formulir interaktif headless yang otomatis terhubung ke database tanpa perlu setup backend.

---

## 🎬 Template Creative Crew (multi halaman, Black & Yellow)

`creative-crew/` (ZIP siap upload: `creative-crew.zip`) adalah template untuk UKM kreatif/media kampus, dibuat dari
brief `CreativeCrew.md`. Lolos pengecekan upload **tanpa error maupun peringatan**.

| Halaman | Isi | Modul |
|---|---|---|
| `index.html` | Hero foto kru + vignette, statistik, marquee layanan, 4 featured project, divisi, ajakan | `karya` (limit 4), `divisi`, `data-ws-stat` |
| `works.html` | Portofolio masonry + filter kategori dari data | `karya` |
| `karya.html` | Studi kasus satu karya (`karya.html?id=...`): sampul, cerita, credit title, galeri, tombol video, karya sebelum/berikutnya | `karya` |
| `crew.html` | Kru grayscale → berwarna saat disorot + link portofolio/Instagram, filter divisi, form open recruitment | `kru`, form `oprec` |
| `collab.html` | Alur 4 langkah, layanan, form pengajuan 2 kolom (pilihan layanan dari data), pop-up terima kasih + SLA | `layanan`, form `kolaborasi` |
| `gear.html` | Status alat (Tersedia/Dipinjam/Maintenance), lokasi, peminjam, tanggal kembali, tanda terlambat | `alat` |

Bagian **Admin Dashboard** di brief tidak dibuat di template karena Portal Pengurus adalah bawaan OrgSpace:
dasbor metrik = tab Ringkasan, inbox kolaborasi & rekrutmen = Kotak Masuk Form (tanpa kanban), CMS portofolio /
inventaris / roster = tab Modul Kustom (`karya`, `alat`, `kru`).

---

## 🛠️ Cara Mengedit dan Mengemas Ulang Menjadi ZIP

Jika temanmu ingin membuat template baru dengan memodifikasi template ini:

1. **Buka folder `template-organisasi-modern/` di VS Code**:
   ```bash
   code OrgSpace-Templates/template-organisasi-modern
   ```
2. **Ubah tampilan, warna, atau struktur HTML/CSS**:
   - Selalu gunakan **path relatif** (misal `css/style.css`, `img/logo.png`).
   - Jangan gunakan framework SPA berat (React/Next/Vue client-side).
3. **Kemas kembali menjadi file ZIP**:
   Pastikan file `index.html` berada **langsung di akar ZIP**, bukan terbungkus folder lain:
   ```bash
   cd OrgSpace-Templates/template-organisasi-modern
   zip -r ../template-organisasi-modern.zip *
   ```

---

## 📖 Dokumentasi Lengkap Provider

Buka pusat panduan resmi di portal web:
- **Dokumentasi Lengkap:** [http://localhost:3000/docs](http://localhost:3000/docs)
- **Requirement & Tools:** [http://localhost:3000/docs/requirement-project-dan-tools](http://localhost:3000/docs/requirement-project-dan-tools)
- **Modul Bawaan OrgSpace:** [http://localhost:3000/docs/modul-bawaan-orgspace](http://localhost:3000/docs/modul-bawaan-orgspace)
- **Panduan Modul Kustom:** [http://localhost:3000/docs/panduan-modul-kustom](http://localhost:3000/docs/panduan-modul-kustom)
