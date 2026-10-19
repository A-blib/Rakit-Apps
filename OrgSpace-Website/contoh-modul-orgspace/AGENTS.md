# AGENTS.md — Membuat template website dinamis OrgSpace

Kamu adalah AI agent (Claude Code, Codex, Cursor, dll.) yang membantu **penyedia template OrgSpace** membuat template
website organisasi (OSIS, BEM, UKM, komunitas, dll.). Template ini nanti diupload ke aplikasi OrgSpace, dipakai
organisasi lain, lalu tayang di `orgspace.app/nama-website`.

## 0. Pahami produknya dulu

- OrgSpace **tidak punya** fitur agenda/prestasi/galeri bawaan. Fitur seperti itu kamu buat sendiri di template:
  **data** (modul `data-ws-collection`) + **tampilan** (HTML/CSS) + **perilaku** (JavaScript).
- Yang disediakan OrgSpace: penyimpanan data, form isian di Portal Pengurus (dibuat otomatis dari atribut `data-ws-*`),
  pengisian kartu, berita bawaan, formulir, statistik, profil organisasi, dan halaman Portal Pengurus (`/admin`).
- Setelah template dipakai, **layout dikunci**. Pembuat website hanya mengganti teks/foto/warna yang ditandai provider;
  pengurus hanya mengisi data modul. Jadi semua section, halaman, dan modul yang dibutuhkan harus sudah ada di template.
- Tidak ada kode server dari provider. Fitur yang butuh logika server (pembayaran, login pengunjung, komentar, ambil
  data dari API luar) **tidak bisa** dibuat. Lihat `docs/04-membuat-fitur-kustom.md` bagian 3 untuk alternatifnya.

## 1. Baca dulu (urut)

1. `docs/01-panduan-modul-dinamis.md`: semua atribut `data-ws-*` (Portal Pengurus, pengumuman, modul kustom, jenis
   field, statistik, profil organisasi, formulir, JavaScript + data modul).
2. `docs/04-membuat-fitur-kustom.md`: cara merancang fitur kustom, resep JavaScript (agenda, filter, pencarian,
   lightbox, FAQ, statistik), batas yang tidak didukung, dan langkah setelah upload.
3. `template-sinergi/`: contoh **lengkap** yang sudah lolos pengecekan: 2 halaman, 7 modul kustom, pengumuman,
   2 formulir, statistik, profil organisasi, tema gelap, animasi. Rujukan utama untuk pola HTML dan `js/main.js`.
4. `contoh-template/`: contoh **sederhana** satu halaman. Baca jika butuh versi minimal.
5. `docs/02-aturan-wajib-template.md` dan `docs/03-requirement-dan-tools.md`: aturan ZIP, CDN, keamanan, batas ukuran.

Tiru cara pemakaian atribut dan pola script-nya, **bukan** desainnya. Jika ada yang bertentangan, `docs/01` dan bagian 3
file ini yang berlaku.

## 2. Tugasmu

1. Tanyakan ke pengguna (jika belum dijelaskan):
   - jenis organisasi dan gaya desain;
   - halaman yang diinginkan (satu halaman panjang, atau dipecah mis. Beranda, Agenda, Prestasi, Tentang);
   - fitur/data yang perlu diisi pengurus (agenda, prestasi, galeri, pengurus, FAQ, dll.) dan formulir yang dibutuhkan.
2. Tulis **rencana** dalam tabel (halaman → section → jenis isi → modul/field), seperti `docs/04` bagian 4. Tunjukkan ke
   pengguna sebelum menulis kode jika permintaannya masih umum.
3. Buat template baru di folder **`template/`** (jangan mengubah `contoh-template/` dan `template-sinergi/`):
   ```
   template/
   ├── index.html       ← wajib di sini (akar ZIP nanti)
   ├── agenda.html ...  ← halaman lain (opsional, maks. 10 halaman total)
   ├── css/style.css
   ├── js/main.js       ← opsional
   └── img/...          ← gambar contoh milik sendiri atau berlisensi bebas
   ```
4. Pasang modul dinamis dengan atribut `data-ws-*` dan fitur interaktif dengan pola script di `docs/04` bagian 5.
5. Jalankan checklist di bagian 4, perbaiki sampai semua terpenuhi.
6. Buat ZIP dari **isi** folder `template/` (bagian 5), lalu tulis **laporan untuk pengguna** (bagian 6).

## 3. Aturan yang wajib dipatuhi

- **HTML + CSS murni.** Jangan membuat hasil build Next.js/React/Vue/Svelte/Angular. Teks dan gambar utama ditulis
  langsung di HTML, bukan dibuat JavaScript (halaman yang isinya dibuat JS ditolak: `JS_RENDERED_CONTENT`).
- **JavaScript boleh** (menu HP, slider, tab, dark mode, filter) di file `js/*.js` atau library dari CDN yang diizinkan.
  Website dijalankan di sandbox browser, jadi:
  - `localStorage`, `sessionStorage`, dan cookie hanya sementara, dan aksesnya bisa melempar error: bungkus dengan
    `try/catch` (lihat `template-sinergi/js/main.js` bagian tema);
  - halaman publik tidak tahu siapa pengurus yang login: jangan pakai `data-ws-auth="logout"`, `"status"`, atau
    `data-ws-role` di template baru. Cukup tombol `<a href="admin" data-ws-auth="login">` ke Portal Pengurus.
- **Dilarang** atribut `on...` (`onclick`, `onload`, `onerror`, dst.) dan link `javascript:`. Keduanya dihapus saat
  website diterbitkan. Pasang event dengan `addEventListener` di file JS.
- **Dilarang** mengambil atau mengirim data ke situs lain dari JS (`fetch("https://...")`, XHR, WebSocket, `sendBeacon`):
  ditolak pengecekan (`EXT_FETCH`). `fetch` ke file lokal di ZIP (`fetch("data/menu.json")`) boleh.
- Dilarang kode yang diacak (obfuscated), Service Worker, dan `import` library dari CDN di dalam file JS (muat library
  dengan `<script src>` di HTML). Hindari `eval()`/`new Function()` (mendapat peringatan).
- Script yang memakai kartu atau data modul harus menunggu event `orgspace:siap` dan membaca `window.orgspace.data`
  (pola di `docs/04` bagian 5). Tanpa `window.orgspace` (preview/export), script tetap tidak boleh error.
- Boleh beberapa halaman (maks. **10**) yang saling terhubung dengan link relatif. Setiap halaman harus bisa dibuka dari
  menu. Modul yang sama boleh dipakai di banyak halaman (mis. 3 agenda di beranda dengan `data-ws-limit="3"`, semua
  agenda di `agenda.html`).
- **Path relatif** untuk aset lokal: `css/style.css`, `img/logo.png` (bukan `/css/...`). Huruf besar/kecil nama file
  harus sama persis dengan yang dipanggil.
- Wajib `<!DOCTYPE html>`, `<html lang="id">`, `<meta charset="utf-8">`, `<title>`, dan
  `<meta name="viewport" content="width=device-width, initial-scale=1">`. Desain rapi di layar HP (360px).
- CDN hanya dari: `cdn.jsdelivr.net`, `cdnjs.cloudflare.com`, `unpkg.com`, `cdn.tailwindcss.com`,
  `code.jquery.com`, serta Google Fonts. Library yang diizinkan: Tailwind, Bootstrap, Bulma, Alpine.js, jQuery, AOS,
  Swiper, Splide, GSAP, Font Awesome, Bootstrap Icons, Lucide, Remix Icon. **Tulis versi lengkap** di URL
  (`swiper@11.1.4`, bukan `swiper` atau `@latest`). Tanpa tracker/analytics. Tanpa library sama sekali juga boleh.
- Iframe hanya YouTube (`youtube.com`, `youtube-nocookie.com`) dan Google Maps.
- Form `data-ws-form` tidak boleh punya `action` ke situs lain. Form hanya mengirim teks (`<input type="file">` diabaikan).
- Jangan memakai atribut khusus app (`data-edit`, `data-key`, `data-label`, `data-tpl-id`) dan jangan memakai `<base href>`.
- Teks di halaman berbahasa Indonesia. Isi contoh bersifat netral (mis. "Nama Organisasi", "Judul pengumuman"),
  karena teks dan gambar akan diganti oleh pembuat website.
- Sediakan cukup banyak teks dan gambar biasa di luar modul (judul, paragraf, logo, foto, teks tombol) dengan `id` yang
  jelas (`hero-judul`, `visi-teks`): elemen inilah yang ditandai provider agar bisa diedit pembuat website (minimal 3).
- Warna utama, warna aksen, dan radius ditulis sebagai variabel CSS di `:root` dan dipakai di seluruh CSS, agar bisa
  didaftarkan sebagai tema yang dipilih pembuat website.
- Gambar ≤ 500 KB per file (lebih dari 1 MB mendapat peringatan), beri `alt` yang jelas. File apa pun maks. 10 MB.

## 4. Checklist sebelum selesai

- [ ] `template/index.html` ada, aset dipanggil dengan path relatif, dan semua file yang dipanggil memang ada.
- [ ] Setiap link antarhalaman menuju halaman yang ada; tidak lebih dari 10 halaman HTML.
- [ ] Setiap `data-ws-list` / `data-ws-collection` punya **tepat satu** `data-ws-item`.
- [ ] Setiap `data-ws-item` punya minimal satu `data-ws-field`.
- [ ] `data-ws-field`, `data-ws-link`, dan `data-ws-empty` berada di dalam daftarnya; setiap daftar punya `data-ws-empty`.
- [ ] Nama modul, field, dan form: huruf kecil, angka, `-` atau `_`, 2–40 karakter.
- [ ] `pengumuman` / `berita` / `kegiatan` hanya memakai field: `judul`, `ringkasan`, `konten`, `tanggal`, `foto`,
      `detail` (pakai `data-ws-link="detail"`). Untuk agenda dengan field sendiri, pakai nama lain (mis. `agenda`).
- [ ] `data-ws-limit` (jika ada) bernilai 1–100.
- [ ] Field modul kustom berisi teks panjang, angka, atau tanggal diberi `data-ws-type="paragraf"`, `"angka"`,
      atau `"tanggal"`; foto memakai `<img data-ws-field>`; link memakai `<a data-ws-link>`.
- [ ] Tombol Portal Pengurus berupa `<a href="admin" data-ws-auth="login">`.
- [ ] Angka statistik memakai `data-ws-stat` (pengurus, berita, form.{nama}, nama modul) dan profil organisasi
      memakai `data-ws-org` (nama, logo, kontak), bukan angka/teks yang diketik mati.
- [ ] Setiap `<input>`, `<textarea>`, `<select>` di dalam `<form data-ws-form="...">` punya atribut `name`.
- [ ] Tidak ada `on...=`, `javascript:`, `fetch`/XHR ke situs lain, `eval`, atau path absolut.
- [ ] Script data memakai pola `orgspace:siap` + fallback, melewati kartu tersembunyi, dan tidak error tanpa
      `window.orgspace`; akses `localStorage` dibungkus `try/catch`.
- [ ] URL CDN memakai versi lengkap dari library yang diizinkan.
- [ ] Setiap gambar punya `alt`; halaman tetap rapi di lebar 360px tanpa scroll ke samping.
- [ ] Warna dan radius utama berupa variabel `:root`; elemen yang akan ditandai punya `id` yang jelas.

## 5. Membuat ZIP

`index.html` harus berada di **akar** ZIP, bukan di dalam folder `template/`.

```bash
cd template && zip -r ../template-saya.zip . && cd ..
```

Windows (PowerShell):

```powershell
Compress-Archive -Path template\* -DestinationPath template-saya.zip -Force
```

## 6. Laporan untuk pengguna

Setelah ZIP jadi, tulis ringkasan ini agar pengguna bisa langsung mengunggah dan menandai:

1. **Halaman & modul**: tabel halaman → section → modul dan field-nya (yang nanti muncul di Portal Pengurus).
2. **Saran isian Tandai**: daftar `id` elemen yang sebaiknya ditandai, labelnya, jenisnya (teks, paragraf, gambar,
   tombol/link), dan section-nya. Contoh: `hero-judul` → "Judul utama" (teks, section Hero).
3. **Variabel tema**: daftar variabel `:root` yang sebaiknya didaftarkan (mis. `--primary` "Warna utama", jenis warna).
4. **Cara upload**: di aplikasi OrgSpace beralih ke **mode Penyedia Template** → tab **Upload** → pilih
   `template-saya.zip` → isi Info → Thumbnail → Tandai → Coba → Kirim. Pengecekan otomatis menampilkan kesalahan
   (jika ada) beserta file, baris, dan cara memperbaikinya.
5. **Untuk pengurus nanti**: data modul diisi di `orgspace.app/nama-website/admin` dan langsung tampil tanpa terbit ulang.
