# AGENTS.md — Membuat template website dinamis OrgSpace

Kamu adalah AI agent (Claude Code, Codex, Cursor, dll.) yang membantu **penyedia template OrgSpace** membuat template
website organisasi (OSIS, BEM, UKM, komunitas, dll.). Template ini nanti diupload ke aplikasi OrgSpace, dipakai
organisasi lain, lalu tayang di `orgspace.app/nama-website`.

## 1. Baca dulu (urut)

1. `docs/01-panduan-modul-dinamis.md` — semua atribut `data-ws-*` (login, pengumuman, modul kustom, formulir).
2. `contoh-template/index.html` dan `contoh-template/css/style.css` — contoh nyata yang **sudah lolos** pengecekan
   upload dan memakai semua modul. Tiru cara pemakaian atributnya, bukan desainnya.
3. `docs/02-aturan-wajib-template.md` dan `docs/03-requirement-dan-tools.md` — aturan ZIP, struktur folder, CDN.

Jika ada yang bertentangan, `docs/01-panduan-modul-dinamis.md` dan bagian 3 file ini yang berlaku.

## 2. Tugasmu

1. Tanyakan ke pengguna (jika belum dijelaskan): jenis organisasi, gaya desain, dan bagian/modul yang diinginkan.
2. Buat template baru di folder **`template/`** (jangan mengubah `contoh-template/`):
   ```
   template/
   ├── index.html      ← wajib di sini (akar ZIP nanti)
   ├── css/style.css
   ├── js/main.js       ← opsional
   └── img/...          ← gambar contoh milik sendiri atau berlisensi bebas
   ```
3. Pasang modul dinamis sesuai kebutuhan dengan atribut `data-ws-*` dari panduan.
4. Jalankan checklist di bagian 4, perbaiki sampai semua terpenuhi.
5. Buat ZIP dari **isi** folder `template/` (lihat bagian 5) dan beri tahu pengguna cara mengunggahnya.

## 3. Aturan yang wajib dipatuhi

- **HTML + CSS murni.** Jangan membuat hasil build Next.js/React/Vue/Svelte/Angular.
- **JavaScript boleh** (menu HP, slider, tab, dark mode) di file `js/*.js` atau library dari CDN yang diizinkan.
  Website dijalankan di sandbox browser, jadi:
  - `localStorage`, `sessionStorage`, dan cookie hanya sementara (hilang saat halaman ditutup/pindah halaman);
  - halaman publik tidak tahu siapa pengurus yang login: jangan pakai `data-ws-auth="logout"`, `"status"`, atau
    `data-ws-role` di template baru. Cukup tombol `<a href="admin" data-ws-auth="login">` ke Portal Pengurus.
- **Dilarang** atribut `on...` (`onclick`, `onload`, `onerror`, dst.) dan link `javascript:`. Keduanya dihapus saat
  website diterbitkan. Pasang event dengan `addEventListener` di file JS (lihat `contoh-template/js/main.js`).
- Boleh beberapa halaman (`index.html`, `tentang.html`, ...) yang saling terhubung dengan link relatif.
- Script yang memakai kartu atau data modul (filter, slider, grafik) harus menunggu event `orgspace:siap` dan
  membaca `window.orgspace.data` (lihat `docs/01-panduan-modul-dinamis.md` bagian "JavaScript + data modul" dan
  `contoh-template/js/main.js`). Tanpa `window.orgspace` (preview/export), script tetap tidak boleh error.
- **Path relatif** untuk aset lokal: `css/style.css`, `img/logo.png` (bukan `/css/...`).
- Wajib `<meta name="viewport" content="width=device-width, initial-scale=1">` dan desain rapi di layar HP (≤ 400px).
- CDN hanya dari: `cdn.jsdelivr.net`, `cdnjs.cloudflare.com`, `unpkg.com`, `cdn.tailwindcss.com`,
  `code.jquery.com`, serta Google Fonts. Tanpa tracker/analytics.
- Iframe hanya YouTube (`youtube.com`, `youtube-nocookie.com`) dan Google Maps.
- Teks di halaman berbahasa Indonesia. Isi contoh bersifat netral (mis. "Nama Organisasi", "Judul pengumuman"),
  karena teks dan gambar akan diganti oleh pembuat website.
- Sediakan cukup banyak teks dan gambar biasa (judul, paragraf, logo, foto) di luar modul: elemen inilah yang nanti
  ditandai provider agar bisa diedit pembuat website (minimal 3).
- Gambar ≤ 500 KB per file, beri `alt` yang jelas.

## 4. Checklist sebelum selesai

- [ ] `template/index.html` ada, aset dipanggil dengan path relatif, dan semua file yang dipanggil memang ada.
- [ ] Setiap `data-ws-list` / `data-ws-collection` punya **tepat satu** `data-ws-item`.
- [ ] Setiap `data-ws-item` punya minimal satu `data-ws-field`.
- [ ] `data-ws-field`, `data-ws-link`, dan `data-ws-empty` berada di dalam daftarnya.
- [ ] Nama modul, field, dan form: huruf kecil, angka, `-` atau `_`, 2–40 karakter.
- [ ] `pengumuman` / `berita` / `kegiatan` hanya memakai field: `judul`, `ringkasan`, `konten`, `tanggal`, `foto`,
      `detail` (pakai `data-ws-link="detail"`).
- [ ] `data-ws-limit` (jika ada) bernilai 1–100.
- [ ] Tombol Portal Pengurus berupa `<a href="admin" data-ws-auth="login">`.
- [ ] Setiap `<input>`, `<textarea>`, `<select>` di dalam `<form data-ws-form="...">` punya atribut `name`.
- [ ] Tidak ada `on...=`, `javascript:`, atau path absolut; script tidak butuh data tersimpan permanen di browser.
- [ ] Halaman tetap rapi di lebar 360px.

## 5. Membuat ZIP

`index.html` harus berada di **akar** ZIP, bukan di dalam folder `template/`.

```bash
cd template && zip -r ../template-saya.zip . && cd ..
```

Windows (PowerShell):

```powershell
Compress-Archive -Path template\* -DestinationPath template-saya.zip -Force
```

Lalu pengguna mengunggah `template-saya.zip` dari aplikasi OrgSpace: beralih ke **mode Penyedia Template**, buka tab **Upload**.
Pengecekan otomatis akan menampilkan kesalahan (jika ada) beserta baris dan cara memperbaikinya.
