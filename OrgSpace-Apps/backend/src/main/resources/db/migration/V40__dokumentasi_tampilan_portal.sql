-- Artikel baru untuk kreator: portal.json mengatur tampilan Portal Pengurus (keputusan Aris, 28 Oktober 2026).
-- Cadangan yang sama ada di OrgSpace-Website/src/lib/docs-data.ts dan paket
-- OrgSpace-Website/contoh-modul-orgspace/docs/05-tampilan-portal.md.

INSERT INTO doc_articles (slug, category, audience, title, summary, body, sort_order, status, published_at)
VALUES ('tampilan-portal-pengurus', 'provider', 'provider', 'Mengatur Tampilan Portal Pengurus', 'File portal.json opsional: warna, susunan menu, header banner, kartu/tabel, label field, dan menu HP untuk Portal Pengurus.', $doc$Portal Pengurus (`orgspace.app/nama-website/admin`) adalah dashboard tempat pengurus mengisi data modul website. Semua website memakai Portal yang sama. Lewat file **`portal.json`** kamu bisa mengatur **tampilan** Portal agar serasi dengan template: warna, menu, header, dan cara data ditampilkan. **Fitur Portal tidak berubah**: kamu hanya memilih dari pilihan yang disediakan OrgSpace, tidak menulis kode.

`portal.json` **opsional**. Tanpa file ini Portal tetap rapi: setiap modul `data-ws-collection` otomatis menjadi menu sendiri di sidebar.

## Letak file

Taruh `portal.json` di folder paling luar ZIP, sejajar `index.html`:

```text
template-saya.zip
├── index.html
├── agenda.html
├── css/  js/  img/
└── portal.json
```

File ini dicek saat upload. Jika ada yang salah, pengecekan gagal dengan kode **`PORTAL_CONFIG_INVALID`** dan pesan yang menyebut bagian mana yang keliru. Perbaiki lalu upload ulang, atau hapus file ini untuk memakai tampilan standar.

## Contoh lengkap

Contoh dari template **Pramuka Tunas Rimba**:

```json
{
  "version": 1,
  "theme": { "primary": "#2F6B3F", "accent": "#6B4F2A", "radius": 10 },
  "layout": {
    "sidebar": { "position": "left", "width": 268, "resizable": true },
    "header": {
      "variant": "banner",
      "image": "img/hero.jpg",
      "title": "Pangkalan Tunas Rimba",
      "subtitle": "Kelola agenda, jadwal latihan, pembina, prestasi, dan galeri."
    }
  },
  "nav": [
    { "builtin": "overview", "label": "Ringkasan", "icon": "compass" },
    { "group": "Kegiatan", "items": [
      { "builtin": "news", "label": "Kabar Kegiatan", "icon": "megaphone" },
      { "module": "agenda", "label": "Agenda", "icon": "calendar" },
      { "module": "jadwal", "label": "Jadwal Latihan", "icon": "clipboard-list" }
    ]},
    { "builtin": "submissions", "label": "Pendaftar Baru", "icon": "inbox" }
  ],
  "modules": {
    "agenda": {
      "description": "Kegiatan mendatang yang tampil di beranda.",
      "view": "kartu",
      "title": "judul",
      "subtitle": "tanggal",
      "fields": {
        "judul": { "label": "Nama kegiatan" },
        "waktu": { "label": "Jam", "help": "Contoh: 07.00–12.00 WIB" }
      },
      "groups": [
        { "title": "Waktu & tempat", "fields": ["judul", "tanggal", "waktu", "lokasi"] },
        { "title": "Detail", "fields": ["golongan", "deskripsi"] }
      ]
    },
    "jadwal": { "view": "tabel" }
  },
  "mobile": { "nav": "bottom", "bottom": ["overview", "agenda", "news", "submissions"] }
}
```

Semua bagian boleh dihilangkan. Bagian yang tidak ditulis memakai tampilan standar.

## Pilihan yang tersedia

### `theme`: warna dan sudut

| Kunci | Nilai | Keterangan |
|---|---|---|
| `primary` | Hex 6 digit, mis. `#2F6B3F` | Warna tombol, menu aktif, dan aksen. Harus cukup gelap: teks putih di atasnya wajib terbaca (kontras minimal 3:1) |
| `accent` | Hex 6 digit | Warna aksen kedua. Aturan kontras sama |
| `radius` | Angka 0–24 | Sudut kartu, tombol, dan isian (px) |

Mode gelap memakai versi lebih terang dari warnamu secara otomatis. Warna merah untuk hapus/error dan hijau untuk sukses tidak bisa diganti.

### `layout.sidebar`: menu samping (desktop)

| Kunci | Nilai |
|---|---|
| `position` | `left` atau `right` |
| `width` | Angka 200–360 (px), lebar awal |
| `resizable` | `true` agar pengurus bisa menggeser lebar menu. Lebar hasil geser disimpan di browser masing-masing pengurus |

### `layout.header`: header halaman

| Kunci | Nilai |
|---|---|
| `variant` | `minimal` (tanpa header besar) atau `banner` |
| `image` | Path gambar di ZIP (png/jpg/webp), mis. `img/hero.jpg`. Dipakai sebagai latar banner, otomatis digelapkan agar teks terbaca |
| `title` | Teks 1–60 karakter. Bawaan: nama organisasi |
| `subtitle` | Teks 1–140 karakter |

### `nav`: susunan menu

Daftar menu dari atas ke bawah. Setiap isi berupa salah satu:

| Bentuk | Contoh |
|---|---|
| Menu bawaan | `{ "builtin": "overview" }`. Pilihan: `overview` (Ringkasan), `news` (Berita), `submissions` (Kotak Masuk Form) |
| Menu modul | `{ "module": "agenda" }`. Nama harus ada sebagai `data-ws-collection` di HTML |
| Grup | `{ "group": "Profil", "items": [ ...menu... ] }`. Satu tingkat saja |

Setiap menu boleh diberi `label` (maks. 40 karakter) dan `icon`. Setiap menu hanya boleh muncul sekali. Menu yang tidak kamu sebut tetap muncul di grup **Lainnya**, agar data pengurus tidak pernah terkunci. Kotak Masuk Form tetap tersembunyi untuk peran Editor walau kamu menaruhnya di `nav`.

**Ikon yang tersedia:** `layout-dashboard`, `inbox`, `newspaper`, `calendar`, `users`, `user`, `trophy`, `award`, `star`, `image`, `camera`, `target`, `flag`, `book-open`, `graduation-cap`, `briefcase`, `handshake`, `heart`, `map-pin`, `megaphone`, `clipboard-list`, `file-text`, `folder`, `list`, `boxes`, `tent`, `compass`, `music`, `sparkles`, `mail`, `link`.

### `modules`: halaman tiap modul

Kunci = nama modul. Isi:

| Kunci | Nilai |
|---|---|
| `label` | Judul halaman modul (maks. 40 karakter) |
| `icon` | Ikon modul (dipakai jika `nav` tidak memberi ikon) |
| `description` | Penjelasan singkat di bawah judul (maks. 140 karakter) |
| `view` | `kartu` (bawaan) atau `tabel`. Di HP selalu kartu |
| `title`, `subtitle` | Nama field untuk judul dan subjudul kartu |
| `image` | Nama field gambar (`<img data-ws-field>`) untuk foto kartu |
| `fields` | Per field: `label` (maks. 40) dan `help` (teks bantuan, maks. 140) |
| `groups` | Kelompok field di formulir: `[{ "title": "...", "fields": ["..."] }]`. Field yang tidak masuk grup ditaruh di akhir |

Tanpa `label`, nama field ditampilkan rapi otomatis: `nama_kegiatan` menjadi "Nama Kegiatan".

### `mobile`: tampilan HP

| Kunci | Nilai |
|---|---|
| `nav` | `drawer` (bawaan, menu geser dari samping) atau `bottom` (menu bawah seperti app) |
| `bottom` | Maks. 4 menu untuk menu bawah: `overview`, `news`, `submissions`, atau nama modul. Menu lain ada di tombol **Lainnya** |

## Yang tidak bisa diatur

Login Portal, tombol keluar, nama dan peran pengurus, tombol "Lihat Website", dan mode gelap selalu tampil sama di semua website. Ini menjaga pengurus selalu tahu sedang berada di Portal OrgSpace yang asli. `portal.json` juga tidak bisa menambah fitur baru, menjalankan JavaScript, atau memuat CSS sendiri.

## Mencoba hasilnya

Setelah template tayang, buka Portal demo di `/demo/{id}/admin` (atau tombol login pengurus `data-ws-auth="login"` di preview template). Portal demo memakai `portal.json` yang sama dengan website asli, dengan data contoh.
$doc$, 60, 'published', now())
ON CONFLICT (slug) DO UPDATE SET category = EXCLUDED.category, audience = EXCLUDED.audience, title = EXCLUDED.title,
    summary = EXCLUDED.summary, body = EXCLUDED.body, sort_order = EXCLUDED.sort_order, status = 'published',
    published_at = COALESCE(doc_articles.published_at, now()), updated_at = now();
