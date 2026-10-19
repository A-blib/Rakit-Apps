# Panduan Modul Dinamis

Modul dinamis membuat template HTML biasa bisa diisi data oleh pengurus organisasi: pengumuman, prestasi, agenda, formulir pendaftaran, dan tombol ke Portal Pengurus. Provider **tidak perlu menulis backend, API, atau database**. Cukup tambahkan atribut `data-ws-*` ke elemen HTML. JavaScript template sendiri (menu, slider, tab, dark mode) tetap boleh dan ikut jalan.

Contoh sederhana ada di folder `contoh-template/`, contoh lengkap & interaktif di `template-sinergi/`. Cara merancang fitur sendiri (agenda, galeri, filter, pencarian) beserta resep JavaScript-nya ada di `docs/04-membuat-fitur-kustom.md`.

## Cara kerjanya

1. Provider menandai bagian template dengan `data-ws-*`, lalu mengupload template.
2. Pengecekan upload memeriksa semua atribut. Kesalahan ditampilkan lengkap dengan baris dan cara memperbaikinya.
3. Pembuat website memakai template itu lalu menerbitkannya ke `orgspace.app/nama-website`.
4. Pengurus mengisi data lewat **Portal Pengurus** di `orgspace.app/nama-website/admin`.
5. Setiap kali halaman dibuka, data terbaru langsung tampil. **Tidak perlu menerbitkan ulang.**

> Modul hanya bekerja di website yang tayang di OrgSpace. Di file ZIP hasil export, bagian modul tetap menampilkan isi contoh dari template.

## Daftar atribut

| Atribut | Dipasang di | Fungsi |
|---|---|---|
| `data-ws-auth="login"` | `<a>` | Tombol menuju Portal Pengurus website itu. |
| `data-ws-auth="logout"` | `<button>` / `<a>` | Disembunyikan di halaman publik (lihat bagian 1). |
| `data-ws-auth="status"` | `<span>` / `<strong>` | Isi aslinya (mis. "Tamu") yang tampil (lihat bagian 1). |
| `data-ws-role="admin,editor"` | elemen apa saja | Disembunyikan di halaman publik (lihat bagian 1). |
| `data-ws-list="nama"` | pembungkus daftar | Daftar data. Sama persis dengan `data-ws-collection`. |
| `data-ws-collection="nama"` | pembungkus daftar | Daftar data. Sama persis dengan `data-ws-list`. |
| `data-ws-item` | 1 kartu contoh | Kartu yang diulang untuk setiap data. |
| `data-ws-field="nama_field"` | elemen di dalam kartu | Diisi teks data. Pada `<img>` yang diisi adalah `src`. |
| `data-ws-link="nama_field"` | `<a>` di dalam kartu | Diisi `href` dari data. |
| `data-ws-type="tanggal"` | elemen `data-ws-field` | Jenis isian di Portal Pengurus: `teks` (bawaan), `paragraf`, `angka`, atau `tanggal` (lihat bagian 3). |
| `data-ws-stat="prestasi"` | elemen apa saja | Diisi **angka** dari database: jumlah item modul, pengurus, berita, atau isian form (lihat bagian 3a). |
| `data-ws-org="nama"` | elemen apa saja | Diisi profil organisasi: nama, logo, kontak, alamat (lihat bagian 3a). |
| `data-ws-empty` | di dalam pembungkus | Tampil hanya jika belum ada data. |
| `data-ws-limit="6"` | pembungkus daftar | Batas jumlah kartu yang tampil (1–100). |
| `data-ws-form="nama"` | `<form>` | Isian dikirim ke Kotak Masuk Portal Pengurus. |
| `data-ws-form-alert` | elemen di dalam form | Tempat pesan berhasil/gagal setelah form dikirim. |

## 1. Tombol Portal Pengurus

```html
<a href="admin" data-ws-auth="login">Masuk Pengurus</a>
```

- `href="admin"` dan `data-ws-auth="login"` sama-sama membuka Portal Pengurus website itu (`/nama-website/admin`).
- Pengurus mengelola semua data di Portal Pengurus. **Halaman publik website selalu tampil sebagai tamu**: demi keamanan, halaman template tidak bisa membaca sesi login pengurus.
- Karena itu `data-ws-auth="logout"` dan `data-ws-role` disembunyikan di halaman publik, dan `data-ws-auth="status"` tetap menampilkan isi aslinya. Atribut ini masih diterima agar template lama tetap lolos pengecekan, tapi tidak perlu dipakai di template baru.

## 2. Pengumuman (modul bawaan)

Nama `pengumuman`, `berita`, dan `kegiatan` memakai data yang sama: yang diisi pengurus di tab **Berita** Portal Pengurus.

```html
<div data-ws-list="pengumuman" data-ws-limit="6">
  <article data-ws-item>
    <img src="img/pengumuman.jpg" alt="Foto pengumuman" data-ws-field="foto">
    <span data-ws-field="tanggal">1 Januari 2026</span>
    <h3 data-ws-field="judul">Judul pengumuman</h3>
    <p data-ws-field="ringkasan">Ringkasan singkat isi pengumuman.</p>
  </article>
  <div data-ws-empty>Belum ada pengumuman.</div>
</div>
```

| Field | Isi |
|---|---|
| `judul` | Judul pengumuman |
| `ringkasan` | Ringkasan singkat |
| `konten` | Isi lengkap |
| `tanggal` | Tanggal terbit, mis. "19 Oktober 2026" |
| `foto` | Foto pengumuman (jika pengurus mengisinya) |
| `detail` | Link ke pengumuman; pakai dengan `data-ws-link="detail"` |

## 3. Modul kustom (buat sendiri)

Nama selain `pengumuman`, `berita`, dan `kegiatan` menjadi **modul kustom**. Nama modul dan nama field bebas: huruf kecil, angka, `-` atau `_`, 2–40 karakter. Portal Pengurus membaca template dan langsung menyiapkan form isian dengan field yang sama.

```html
<div data-ws-collection="prestasi" data-ws-limit="9">
  <div data-ws-item>
    <img src="img/prestasi.jpg" alt="Foto prestasi" data-ws-field="foto">
    <span data-ws-field="tingkat">Nasional</span>
    <h3 data-ws-field="judul">Juara 1 Lomba</h3>
    <p><span data-ws-field="nama">Nama anggota</span> · <span data-ws-field="tahun">2026</span></p>
  </div>
  <div data-ws-empty>Belum ada prestasi yang dicatat.</div>
</div>

<div data-ws-collection="agenda">
  <div data-ws-item>
    <h3 data-ws-field="nama_kegiatan">Nama kegiatan</h3>
    <span data-ws-field="tanggal" data-ws-type="tanggal">Tanggal</span> · <span data-ws-field="lokasi">Lokasi</span>
    <a href="#daftar" data-ws-link="link_daftar">Ikuti</a>
  </div>
  <div data-ws-empty>Belum ada agenda terdekat.</div>
</div>
```

Contoh di atas membuat dua modul di Portal Pengurus: `prestasi` (foto, tingkat, judul, nama, tahun) dan `agenda` (nama_kegiatan, tanggal, lokasi, link_daftar).

Jenis isian di Portal Pengurus mengikuti elemennya:

| Di template | Isian di Portal | Tampil di website |
|---|---|---|
| `<img data-ws-field>` | Unggah foto (diperkecil otomatis) | `src` gambar |
| `data-ws-link` | Link (`https://...`) | `href` |
| `data-ws-type="paragraf"` | Teks panjang | Teks, baris baru dipertahankan |
| `data-ws-type="angka"` | Angka | Teks |
| `data-ws-type="tanggal"` | Pilih tanggal | "19 Oktober 2026" |
| tanpa `data-ws-type` | Teks singkat | Teks |

- Pengurus bisa menambah, mengedit, menghapus, dan **mengatur urutan** data. Urutan di Portal sama dengan urutan kartu di website.
- Field yang tidak diisi pengurus tetap menampilkan isi contoh dari template. Jadi isi contoh sebaiknya netral, mis. foto default.
- `<img data-ws-field>` hanya menerima alamat gambar `http://`, `https://`, atau `/...`.
- `data-ws-link` hanya menerima `http://`, `https://`, `mailto:`, `tel:`, `/...`, atau `#...`. Alamat lain diabaikan dan `href` asli dari template yang dipakai.

## 3a. Statistik & profil organisasi (langsung dari database)

Kartu angka seperti "12 pengurus aktif" atau "40 pendaftar" tidak perlu diketik pengurus: OrgSpace menghitungnya dari
database setiap kali halaman dibuka.

```html
<div class="stat"><strong data-ws-stat="pengurus">12</strong> pengurus aktif</div>
<div class="stat"><strong data-ws-stat="prestasi">18</strong> prestasi</div>
<div class="stat"><strong data-ws-stat="form.pendaftaran">40</strong> pendaftar</div>
```

| `data-ws-stat` | Angka |
|---|---|
| `pengurus` | Jumlah anggota ruang kerja OrgSpace (pengurus yang punya akun) |
| `berita` | Jumlah pengumuman/berita |
| `form.{nama-form}` | Jumlah isian form itu, mis. `form.pendaftaran` (hanya angka, isi form tetap rahasia) |
| nama modul kustom | Jumlah item modul itu, mis. `prestasi`, `agenda`, `anggota` |

Nama bawaan (`pengurus`, `berita`, `form.*`) dipakai lebih dulu jika modul kustom memakai nama yang sama. Nama yang
belum punya data tampil `0`.

Profil organisasi diisi dari halaman Profil organisasi di app:

```html
<img src="img/logo.svg" alt="Logo" data-ws-org="logo">
<span data-ws-org="nama">Nama Organisasi</span>
<a href="#" data-ws-org="whatsapp">WhatsApp</a>
```

Pilihan: `nama`, `singkatan`, `kategori`, `institusi`, `kota`, `alamat`, `email`, `whatsapp`, `instagram`, `tiktok`,
`youtube`, `logo`.

- Pada `<img>` yang diisi `src`. Pada `<a>`, `email`/`whatsapp`/`instagram` otomatis menjadi link (`mailto:`, `wa.me`, Instagram).
- Profil yang belum diisi organisasi **disembunyikan** agar teks contoh tidak tampil di website asli. Logo yang belum
  ada tetap memakai logo template.
- Di ruang kerja pribadi hanya `nama` yang tersedia.

## 4. Formulir

```html
<form data-ws-form="pendaftaran">
  <div data-ws-form-alert></div>
  <label for="nama">Nama lengkap</label>
  <input id="nama" name="nama" required>
  <label for="email">Email</label>
  <input id="email" name="email" type="email" required>
  <label for="alasan">Alasan bergabung</label>
  <textarea id="alasan" name="alasan"></textarea>
  <button type="submit">Kirim pendaftaran</button>
</form>
```

- Setiap `<input>`, `<textarea>`, dan `<select>` wajib punya `name`. Nama itulah yang tampil di Kotak Masuk.
- Satu halaman boleh punya beberapa form dengan nama berbeda, mis. `pendaftaran` dan `aspirasi`.
- Hanya form ber-`data-ws-form` yang dikirim ke OrgSpace. Form lain (mis. pencarian) tidak disentuh.
- Atribut seperti `required` dan `type="email"` tetap diperiksa browser sebelum form dikirim.
- Batas: maksimal 30 isian per form, masing-masing maksimal 5.000 karakter.
- Tanpa `data-ws-form-alert`, pesan berhasil tampil sebagai notifikasi kecil di pojok layar.
- Elemen `data-ws-form` yang **tidak berisi** `<form>` akan diisi form pendaftaran bawaan (nama, kelas, WhatsApp, pesan).

## Portal Pengurus

Alamat: `orgspace.app/nama-website/admin`. Pengurus masuk dengan email dan password akun OrgSpace, dan harus menjadi anggota organisasi pemilik website.

| Tab | Isi |
|---|---|
| Ringkasan | Jumlah isian form, berita, item modul kustom, dan pengurus |
| Kotak Masuk Form | Semua isian dari setiap `data-ws-form` |
| Berita & Agenda | Tambah dan hapus pengumuman |
| Modul Kustom | Daftar modul dari template beserta field-nya; tambah, edit, hapus, urutkan data, dan unggah foto |
| Pengurus & Hak Akses | Tambah pengurus (hanya akun OrgSpace yang sudah terdaftar) dan atur perannya |

Sesi Portal Pengurus berlaku 8 jam dan **hanya untuk satu website itu**.

## JavaScript template

JavaScript template **dijalankan**: hamburger menu, dark mode, slider, tab, accordion, Tailwind CDN, Alpine, Swiper, AOS, dan library lain dari CDN yang diizinkan.

Semua website tayang di satu domain (`orgspace.app/...`), jadi setiap halaman dijalankan di **sandbox** browser: script template tidak bisa membaca data `orgspace.app`, Portal Pengurus, atau website lain. Akibatnya:

| Hal | Perilaku |
|---|---|
| `localStorage`, `sessionStorage`, cookie | Bisa dipakai, tapi **hanya sementara**: hilang saat halaman ditutup atau pindah halaman. Mis. pilihan dark mode tidak diingat. |
| Status login pengurus di halaman publik | Tidak tersedia (lihat bagian 1). |
| `fetch()` / XHR ke situs luar | **Ditolak** saat pengecekan upload (`EXT_FETCH`). Pakai modul `data-ws-*`, atau `fetch` ke file lokal di ZIP (`data/menu.json`). |
| Atribut `onclick`, `onload`, `onerror`, dst. dan link `javascript:` | **Dihapus** saat website diterbitkan. Pasang event di file JS dengan `addEventListener`. |

## JavaScript + data modul

Script template berjalan **sebelum** OrgSpace mengisi modul. Jika script-mu perlu kartu atau data modul (filter, slider, grafik, peta), tunggu event `orgspace:siap`:

```js
function mulai() {
  // Data modul (salinan): window.orgspace.data
  //   .slug          nama website
  //   .news          pengumuman/berita: [{ judul, ringkasan, konten, tanggal, foto, detail }]
  //   .collections   modul kustom: { prestasi: [{ judul, tingkat, ... }], agenda: [...] }
  //   .stats         angka: { pengurus: 12, berita: 5, prestasi: 18, "form.pendaftaran": 40 }
  //   .org           profil organisasi: { nama, kategori, kota, whatsapp, logo, ... }
  var prestasi = window.orgspace.data.collections.prestasi || [];
  console.log(prestasi.length + " prestasi");
}
if (window.orgspace && window.orgspace.siap) mulai();
else document.addEventListener("orgspace:siap", mulai);
```

- Setelah event ini, kartu `data-ws-item` sudah terisi dan bisa dipilih dengan `querySelectorAll`.
- Contoh lengkap: **filter prestasi per tingkat** di `contoh-template/js/main.js` pada paket unduhan.
- Contoh template lengkap & interaktif: `template-sinergi/` (statistik naik saat di-scroll, chip filter dari data, agenda
  diurutkan per tanggal + hitung mundur, lightbox galeri, form 2 langkah, pencarian pengurus, tema gelap).
- Tanggal modul (`data-ws-type="tanggal"`) di data mentah berbentuk `2026-10-19`; teks di kartu sudah diubah menjadi
  "19 Oktober 2026". Pakai data mentah untuk menghitung atau mengurutkan.
- Event ini hanya ada di website yang tayang di OrgSpace. Di preview app atau file ZIP export, `window.orgspace` tidak ada, jadi tulis script yang tetap aman tanpa data (mis. filter tidak dibuat).

## Banyak halaman

Template boleh punya beberapa halaman (`index.html`, `tentang.html`, `kontak.html`). Hubungkan dengan link relatif biasa, mis. `<a href="tentang.html">`. Hasilnya `orgspace.app/nama-website/tentang.html`. Modul dan formulir bekerja di semua halaman.

- Maksimal 10 halaman HTML, dan setiap halaman harus bisa dibuka dari menu.
- Modul yang sama boleh dipakai di beberapa halaman dan datanya tetap satu. Mis. beranda menampilkan 3 agenda (`data-ws-limit="3"`) dengan link ke `agenda.html` yang menampilkan semuanya. Portal Pengurus menampilkan gabungan field dari semua halaman.
- Halaman detail per item (mis. `agenda/123`) tidak tersedia. Pakai kartu yang bisa dibuka (`<details>` atau dialog) atau `data-ws-link` ke link luar.

## Cek sebelum upload

- [ ] Setiap `data-ws-list` / `data-ws-collection` punya **tepat satu** `data-ws-item`.
- [ ] Setiap `data-ws-item` punya minimal satu `data-ws-field`.
- [ ] `data-ws-field`, `data-ws-link`, dan `data-ws-empty` berada di dalam daftarnya, bukan di luar.
- [ ] `data-ws-limit` berisi angka 1–100.
- [ ] `data-ws-stat` memakai `pengurus`, `berita`, `form.{nama}`, atau nama modul kustom; `data-ws-org` memakai nama dari daftar bagian 3a.
- [ ] Setiap isian form punya atribut `name`.
- [ ] Tombol Portal Pengurus memakai `<a href="admin" data-ws-auth="login">`.
- [ ] Tidak ada `onclick=...` atau `href="javascript:..."`; event dipasang dengan `addEventListener` di file JS.
- [ ] Script tidak bergantung pada data yang harus tersimpan permanen di `localStorage` atau cookie.

## Masalah yang sering terjadi

| Gejala | Penyebab & solusi |
|---|---|
| Upload gagal: "tidak memiliki elemen data-ws-item" | Tambahkan `data-ws-item` pada satu kartu contoh di dalam daftar. |
| Upload gagal: "hanya 1 kartu contoh yang diizinkan" | Hapus kartu contoh lain; OrgSpace yang mengulang kartunya. |
| Daftar menampilkan isi contoh, bukan data | Pengurus belum mengisi data, atau nama field di Portal berbeda dengan `data-ws-field`. |
| Foto contoh tetap tampil | Data tidak punya foto, atau alamat fotonya bukan `http(s)://`. |
| Isian form tidak masuk Kotak Masuk | Form belum punya `data-ws-form`, atau isiannya tidak punya `name`. |
| Tombol Keluar atau bagian khusus pengurus tidak terlihat | Memang disembunyikan di halaman publik; pengurus bekerja di Portal Pengurus. |
| Pilihan dark mode hilang setelah halaman ditutup | Penyimpanan browser di website hanya sementara (sandbox). |
| Tombol dengan `onclick` tidak bereaksi | Atribut `on...` dihapus saat terbit; pasang event dengan `addEventListener` di file JS. |
| Script hanya melihat kartu contoh, bukan data asli | Jalankan script setelah event `orgspace:siap` (bagian JavaScript + data modul). |
