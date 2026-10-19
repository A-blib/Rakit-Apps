# Panduan Modul Dinamis

Modul dinamis membuat template HTML biasa bisa diisi data oleh pengurus organisasi: pengumuman, prestasi, agenda, formulir pendaftaran, dan tombol ke Portal Pengurus. Provider **tidak perlu menulis backend, API, atau database**. Cukup tambahkan atribut `data-ws-*` ke elemen HTML. JavaScript template sendiri (menu, slider, tab, dark mode) tetap boleh dan ikut jalan.

Contoh lengkapnya ada di folder `contoh-template/` paket ini.

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
    <span data-ws-field="tanggal">Tanggal</span> · <span data-ws-field="lokasi">Lokasi</span>
    <a href="#daftar" data-ws-link="link_daftar">Ikuti</a>
  </div>
  <div data-ws-empty>Belum ada agenda terdekat.</div>
</div>
```

Contoh di atas membuat dua modul di Portal Pengurus: `prestasi` (foto, tingkat, judul, nama, tahun) dan `agenda` (nama_kegiatan, tanggal, lokasi, link_daftar).

- Field yang tidak diisi pengurus tetap menampilkan isi contoh dari template. Jadi isi contoh sebaiknya netral, mis. foto default.
- `<img data-ws-field>` hanya menerima alamat gambar `http://`, `https://`, atau `/...`.
- `data-ws-link` hanya menerima `http://`, `https://`, `mailto:`, `tel:`, `/...`, atau `#...`. Alamat lain diabaikan dan `href` asli dari template yang dipakai.

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
| Modul Kustom | Daftar modul dari template beserta field-nya; tambah dan hapus data |
| Pengurus & Hak Akses | Tambah pengurus (hanya akun OrgSpace yang sudah terdaftar) dan atur perannya |

Sesi Portal Pengurus berlaku 8 jam dan **hanya untuk satu website itu**.

## JavaScript template

JavaScript template **dijalankan**: hamburger menu, dark mode, slider, tab, accordion, Tailwind CDN, Alpine, Swiper, AOS, dan library lain dari CDN yang diizinkan.

Semua website tayang di satu domain (`orgspace.app/...`), jadi setiap halaman dijalankan di **sandbox** browser: script template tidak bisa membaca data `orgspace.app`, Portal Pengurus, atau website lain. Akibatnya:

| Hal | Perilaku |
|---|---|
| `localStorage`, `sessionStorage`, cookie | Bisa dipakai, tapi **hanya sementara**: hilang saat halaman ditutup atau pindah halaman. Mis. pilihan dark mode tidak diingat. |
| Status login pengurus di halaman publik | Tidak tersedia (lihat bagian 1). |
| `fetch()` ke API luar | Jalan jika API itu mengizinkan semua origin (CORS `*`). |
| Atribut `onclick`, `onload`, `onerror`, dst. dan link `javascript:` | **Dihapus** saat website diterbitkan. Pasang event di file JS dengan `addEventListener`. |

## Banyak halaman

Template boleh punya beberapa halaman (`index.html`, `tentang.html`, `kontak.html`). Hubungkan dengan link relatif biasa, mis. `<a href="tentang.html">`. Hasilnya `orgspace.app/nama-website/tentang.html`. Modul dan formulir bekerja di semua halaman.

## Cek sebelum upload

- [ ] Setiap `data-ws-list` / `data-ws-collection` punya **tepat satu** `data-ws-item`.
- [ ] Setiap `data-ws-item` punya minimal satu `data-ws-field`.
- [ ] `data-ws-field`, `data-ws-link`, dan `data-ws-empty` berada di dalam daftarnya, bukan di luar.
- [ ] `data-ws-limit` berisi angka 1–100.
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
