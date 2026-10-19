# Panduan Modul Dinamis

Modul dinamis membuat template HTML biasa bisa diisi data oleh pengurus organisasi: pengumuman, prestasi, agenda, formulir pendaftaran, dan tombol login pengurus. Provider **tidak menulis backend, API, atau JavaScript**. Cukup tambahkan atribut `data-ws-*` ke elemen HTML.

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
| `data-ws-auth="login"` | `<a>` | Tombol masuk Portal Pengurus. Tersembunyi setelah pengurus masuk. |
| `data-ws-auth="logout"` | `<button>` / `<a>` | Tombol keluar. Tersembunyi untuk tamu. |
| `data-ws-auth="status"` | `<span>` / `<strong>` | Diganti nama pengurus yang sedang masuk. Isi aslinya (mis. "Tamu") tampil untuk tamu. |
| `data-ws-role="admin,editor"` | elemen apa saja | Hanya tampil untuk pengurus dengan peran tersebut. |
| `data-ws-list="nama"` | pembungkus daftar | Daftar data. Sama persis dengan `data-ws-collection`. |
| `data-ws-collection="nama"` | pembungkus daftar | Daftar data. Sama persis dengan `data-ws-list`. |
| `data-ws-item` | 1 kartu contoh | Kartu yang diulang untuk setiap data. |
| `data-ws-field="nama_field"` | elemen di dalam kartu | Diisi teks data. Pada `<img>` yang diisi adalah `src`. |
| `data-ws-link="nama_field"` | `<a>` di dalam kartu | Diisi `href` dari data. |
| `data-ws-empty` | di dalam pembungkus | Tampil hanya jika belum ada data. |
| `data-ws-limit="6"` | pembungkus daftar | Batas jumlah kartu yang tampil (1–100). |
| `data-ws-form="nama"` | `<form>` | Isian dikirim ke Kotak Masuk Portal Pengurus. |
| `data-ws-form-alert` | elemen di dalam form | Tempat pesan berhasil/gagal setelah form dikirim. |

## 1. Login pengurus

```html
<span>Halo, <strong data-ws-auth="status">Tamu</strong></span>
<a href="admin" data-ws-auth="login">Masuk Pengurus</a>
<button type="button" data-ws-auth="logout">Keluar</button>

<div data-ws-role="admin,editor">
  Mode pengurus aktif. Kelola data di <a href="admin">Portal Pengurus</a>.
</div>
```

- Link `href="admin"` otomatis diarahkan ke Portal Pengurus website itu (`/nama-website/admin`).
- Peran yang bisa dipakai di `data-ws-role`: `owner`, `admin`, `editor`. Pemilik (`owner`) ikut dianggap `admin`. Pisahkan beberapa peran dengan koma.
- `data-ws-role` hanya menyembunyikan tampilan. Jangan menaruh informasi rahasia di dalamnya, karena isinya tetap ada di kode halaman.

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

## Aturan keamanan

Semua website tayang di satu domain (`orgspace.app/...`). Agar template tidak bisa membaca sesi pengurus website lain:

- Atribut `onclick`, `onload`, `onerror`, dan semua atribut `on...` lain **dihapus** saat website diterbitkan.
- Link `javascript:...` **dihapus** saat website diterbitkan.
- JavaScript dari template **tidak dijalankan** di website yang tayang. Pakai CSS untuk efek tampilan (`:hover`, `details`/`summary`, animasi CSS).

## Cek sebelum upload

- [ ] Setiap `data-ws-list` / `data-ws-collection` punya **tepat satu** `data-ws-item`.
- [ ] Setiap `data-ws-item` punya minimal satu `data-ws-field`.
- [ ] `data-ws-field`, `data-ws-link`, dan `data-ws-empty` berada di dalam daftarnya, bukan di luar.
- [ ] `data-ws-limit` berisi angka 1–100.
- [ ] Setiap isian form punya atribut `name`.
- [ ] `data-ws-auth` hanya bernilai `login`, `logout`, atau `status`.
- [ ] Tidak ada `onclick=...` atau `href="javascript:..."`.

## Masalah yang sering terjadi

| Gejala | Penyebab & solusi |
|---|---|
| Upload gagal: "tidak memiliki elemen data-ws-item" | Tambahkan `data-ws-item` pada satu kartu contoh di dalam daftar. |
| Upload gagal: "hanya 1 kartu contoh yang diizinkan" | Hapus kartu contoh lain; OrgSpace yang mengulang kartunya. |
| Daftar menampilkan isi contoh, bukan data | Pengurus belum mengisi data, atau nama field di Portal berbeda dengan `data-ws-field`. |
| Foto contoh tetap tampil | Data tidak punya foto, atau alamat fotonya bukan `http(s)://`. |
| Isian form tidak masuk Kotak Masuk | Form belum punya `data-ws-form`, atau isiannya tidak punya `name`. |
| Tombol Keluar tidak terlihat | Memang tersembunyi untuk tamu; masuk dulu lewat Portal Pengurus. |
| Menu atau slider buatan JavaScript tidak jalan | JavaScript template tidak dijalankan; ganti dengan CSS. |
