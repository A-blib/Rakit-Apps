# Membuat Fitur Kustom

OrgSpace tidak punya fitur "agenda", "prestasi", atau "galeri" bawaan. Semua fitur seperti itu **dibuat provider sendiri** di
dalam template, dari tiga bahan:

| Bahan | Isinya | Ditulis dengan |
|---|---|---|
| **Data** | Daftar yang diisi pengurus lewat Portal Pengurus | Atribut `data-ws-collection` + `data-ws-field` (lihat `01-panduan-modul-dinamis.md`) |
| **Tampilan** | Bentuk kartu, halaman, menu | HTML + CSS |
| **Perilaku** | Urutkan, filter, cari, hitung mundur, lightbox, slider | JavaScript yang membaca `window.orgspace.data` |

OrgSpace hanya menyediakan mesinnya: tempat menyimpan data, form isian di Portal Pengurus, dan pengisian kartu otomatis.
Contoh nyata semua resep di halaman ini ada di `template-sinergi/` (lihat `js/main.js`).

## 1. Siapa mengerjakan apa

| Pihak | Yang dikerjakan | Di mana |
|---|---|---|
| **Provider** (kamu / AI agent) | Membuat halaman, section, modul, desain, dan JavaScript. Menandai teks/gambar yang boleh diganti | File template + mode Penyedia Template di app |
| **Pembuat website** | Memilih template, mengganti teks/foto/warna yang ditandai, lalu menerbitkan | Editor di app |
| **Pengurus organisasi** | Mengisi, mengedit, menghapus, dan mengurutkan data modul; membaca isian form | Portal Pengurus `nama-website/admin` |
| **Pengunjung** | Melihat website dan mengirim form | `orgspace.app/nama-website` |

Pembuat website dan pengurus **tidak bisa mengubah layout atau menambah section/modul baru**. Apa pun yang ingin bisa diisi
mereka harus sudah disiapkan provider di template.

## 2. Pilih jenis isi yang tepat

| Jika isinya... | Pakai | Contoh |
|---|---|---|
| Satu teks atau gambar yang jarang berubah | **Isian Tandai** (elemen biasa yang nanti ditandai di app) | Judul hero, visi, foto utama, teks footer |
| Daftar yang bisa bertambah/berkurang | **Modul kustom** `data-ws-collection` | Pengurus, program, prestasi, agenda, galeri, FAQ, alumni |
| Berita/pengumuman | **Modul bawaan** `data-ws-list="pengumuman"` | Kabar terbaru |
| Kiriman dari pengunjung | **Formulir** `data-ws-form` | Pendaftaran, aspirasi, kontak |
| Angka jumlah data | **Statistik** `data-ws-stat` | "12 pengurus", "40 pendaftar" |
| Nama, logo, kontak organisasi | **Profil organisasi** `data-ws-org` | Header, footer, tombol WhatsApp |
| Warna dan bentuk | **Variabel CSS** di `:root` (nanti didaftarkan sebagai tema di app) | `--primary`, `--accent`, `--radius` |

Jangan mengetik daftar pengurus atau angka statistik langsung di HTML. Isi seperti itu harus berupa modul atau statistik
agar pengurus bisa memperbaruinya tanpa terbit ulang.

## 3. Bisa vs tidak bisa

**Bisa**: fitur apa pun yang bentuknya "daftar item ber-field", ditampilkan semenarik mungkin, plus interaksi di browser.
Mis. katalog produk koperasi, jadwal piket, kalender lomba, daftar alumni, sponsor, menu kantin, timeline sejarah.

**Tidak bisa** (butuh logika server; hanya OrgSpace yang bisa menambahkannya sebagai modul bawaan):

| Tidak didukung | Alternatif di template |
|---|---|
| Halaman detail per item (`agenda/123`) | Kartu yang bisa dibuka (accordion/dialog) berisi field `deskripsi`, atau `data-ws-link` ke link luar |
| Relasi antar-modul (agenda milik pengurus X) | Simpan sebagai teks biasa, mis. field `penanggung_jawab` |
| Login/akun pengunjung, member area | – |
| Pembayaran, iuran, tiket | Field link ke formulir/pembayaran luar (`data-ws-link`) |
| Voting, like, komentar | Formulir `data-ws-form` (hasilnya dibaca pengurus di Kotak Masuk) |
| Unggah file oleh pengunjung | Formulir hanya mengirim teks; minta link Google Drive di isian teks |
| Ambil data dari API luar (`fetch("https://...")`) | Ditolak pengecekan upload (`EXT_FETCH`). Pakai modul, atau file lokal di ZIP (`fetch("data/menu.json")`) |

## 4. Rancang dulu sebelum menulis kode

Buat tabel seperti ini, lalu baru tulis HTML-nya:

| Halaman | Section | Jenis | Modul / field |
|---|---|---|---|
| `index.html` | Hero | Isian Tandai + statistik | `data-ws-stat="pengurus"`, `data-ws-stat="agenda"` |
| `index.html` | Agenda terdekat | Modul (3 item) | `agenda`: nama_kegiatan, tanggal (`tanggal`), lokasi, link_daftar (link) |
| `agenda.html` | Semua agenda | Modul (semua) | `agenda` (sama) |
| `index.html` | Kabar terbaru | Modul bawaan | `pengumuman` |
| `tentang.html` | Struktur pengurus | Modul | `struktur`: foto (gambar), nama, jabatan, divisi |
| `index.html` | Daftar anggota | Formulir | `pendaftaran`: nama, kelas, whatsapp, alasan |

Nama modul dan field: huruf kecil, angka, `-` atau `_`, 2–40 karakter. Nama yang sama = data yang sama di semua halaman.

## 5. Resep

### Resep 1: Fitur di halaman sendiri + ringkasan di beranda

Satu modul boleh dipakai di beberapa halaman. Datanya tetap satu dan diisi sekali di Portal.

```html
<!-- index.html: 3 agenda saja + link ke halaman lengkap -->
<div data-ws-collection="agenda" data-ws-limit="3">
  <article class="card" data-ws-item>
    <h3 data-ws-field="nama_kegiatan">Nama kegiatan</h3>
    <p data-ws-field="tanggal" data-ws-type="tanggal">1 Januari 2026</p>
  </article>
  <p data-ws-empty>Belum ada agenda.</p>
</div>
<a href="agenda.html">Lihat semua agenda</a>

<!-- agenda.html: semua agenda, kartu lebih lengkap -->
<div data-ws-collection="agenda">
  <article class="card" data-ws-item>
    <h3 data-ws-field="nama_kegiatan">Nama kegiatan</h3>
    <p><span data-ws-field="tanggal" data-ws-type="tanggal">1 Januari 2026</span> · <span data-ws-field="lokasi">Lokasi</span></p>
    <p data-ws-field="deskripsi" data-ws-type="paragraf">Deskripsi kegiatan.</p>
    <a href="#" data-ws-link="link_daftar">Daftar kegiatan</a>
  </article>
  <p data-ws-empty>Belum ada agenda.</p>
</div>
```

- Maksimal **10 halaman HTML** per template. Setiap halaman harus bisa dibuka dari menu (link relatif `agenda.html`).
- Gabungan field dari semua halaman yang muncul di Portal. Di contoh ini Portal menampilkan nama_kegiatan, tanggal,
  lokasi, deskripsi, dan link_daftar.

### Pola dasar script yang butuh data

Data modul diisi OrgSpace **setelah** script template jalan. Pakai pola ini di setiap template:

```js
(function () {
  var started = false;
  function start() {
    if (started) return;
    started = true;
    var data = window.orgspace && window.orgspace.data ? window.orgspace.data : null; // null di preview/export
    // ...panggil fitur di sini
  }
  document.addEventListener("orgspace:siap", start); // website tayang: setelah modul terisi
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", function () { setTimeout(start, 0); });
  else setTimeout(start, 0); // preview/laptop: jalan dengan isi contoh
})();
```

Kartu yang tidak dipakai disembunyikan OrgSpace dengan `style.display = "none"`. Saat memilih kartu, lewati yang
tersembunyi:

```js
function visibleItems(list) {
  return Array.prototype.filter.call(list.querySelectorAll("[data-ws-item]"), function (item) {
    return item.style.display !== "none";
  });
}
```

### Resep 2: Agenda terurut + "hari lagi"

Tanggal di kartu sudah berupa teks "19 Oktober 2026". Untuk menghitung, pakai data mentah `2026-10-19`. Urutan data mentah
sama dengan urutan kartu.

```js
function markAgenda(data) {
  var list = document.querySelector('[data-ws-collection="agenda"]');
  var rows = data && data.collections.agenda ? data.collections.agenda : [];
  if (!list || !rows.length) return;
  var items = visibleItems(list), today = new Date();
  today.setHours(0, 0, 0, 0);
  var dated = [];
  rows.forEach(function (row, i) {
    if (!items[i] || !/^\d{4}-\d{2}-\d{2}$/.test(row.tanggal || "")) return;
    var date = new Date(row.tanggal + "T00:00:00");
    items[i].classList.toggle("lewat", date < today);
    var tag = document.createElement("span");
    var days = Math.round((date - today) / 86400000);
    tag.textContent = days < 0 ? "Selesai" : days === 0 ? "Hari ini" : days + " hari lagi";
    items[i].appendChild(tag);
    dated.push({ date: date, item: items[i] });
  });
  var empty = list.querySelector("[data-ws-empty]");
  dated.sort(function (a, b) { return a.date - b.date; })
       .forEach(function (d) { list.insertBefore(d.item, empty); });
}
```

### Resep 3: Chip filter yang dibuat dari isi data

```html
<div class="chips" data-filter="prestasi" data-filter-field="tingkat"></div>
<div data-ws-collection="prestasi">
  <article data-ws-item>
    <span data-ws-field="tingkat">Nasional</span>
    <h3 data-ws-field="judul">Juara 1 Lomba</h3>
  </article>
</div>
```

```js
function buildFilters() {
  document.querySelectorAll(".chips[data-filter]").forEach(function (chips) {
    var list = document.querySelector('[data-ws-collection="' + chips.dataset.filter + '"]');
    if (!list) return;
    var items = visibleItems(list), field = chips.dataset.filterField;
    var valueOf = function (item) {
      var el = item.querySelector('[data-ws-field="' + field + '"]');
      return el ? el.textContent.trim() : "";
    };
    var values = [];
    items.forEach(function (it) { var v = valueOf(it); if (v && values.indexOf(v) < 0) values.push(v); });
    if (values.length < 2) return; // tidak perlu filter
    ["Semua"].concat(values).forEach(function (v, i) {
      var chip = document.createElement("button");
      chip.type = "button";
      chip.textContent = v;
      chip.setAttribute("aria-pressed", String(i === 0));
      chip.addEventListener("click", function () {
        chips.querySelectorAll("button").forEach(function (c) { c.setAttribute("aria-pressed", String(c === chip)); });
        items.forEach(function (it) { it.hidden = i !== 0 && valueOf(it) !== v; });
      });
      chips.appendChild(chip);
    });
  });
}
```

### Resep 4: Pencarian

```html
<input type="search" id="cari" placeholder="Cari nama atau jabatan" aria-label="Cari pengurus">
<p id="tidak-ketemu" hidden>Tidak ada yang cocok.</p>
```

```js
function setupSearch() {
  var input = document.getElementById("cari");
  var list = document.querySelector('[data-ws-collection="struktur"]');
  if (!input || !list) return;
  var items = visibleItems(list);
  input.addEventListener("input", function () {
    var q = input.value.trim().toLowerCase(), shown = 0;
    items.forEach(function (it) {
      it.hidden = it.textContent.toLowerCase().indexOf(q) < 0;
      if (!it.hidden) shown++;
    });
    document.getElementById("tidak-ketemu").hidden = shown > 0;
  });
}
```

### Resep 5: Galeri + lightbox tanpa library

```html
<div data-ws-collection="galeri">
  <button class="shot" type="button" data-ws-item>
    <img src="img/galeri-1.jpg" alt="Foto kegiatan" data-ws-field="foto">
    <span data-ws-field="keterangan">Keterangan foto</span>
  </button>
</div>
<dialog id="lightbox">
  <img id="lightbox-img" alt="">
  <p id="lightbox-caption"></p>
  <button type="button" data-close>Tutup</button>
</dialog>
```

```js
var box = document.getElementById("lightbox");
document.addEventListener("click", function (e) {
  var shot = e.target.closest(".shot");
  if (shot && box.showModal) {
    var img = shot.querySelector("img");
    document.getElementById("lightbox-img").src = img.src;
    document.getElementById("lightbox-img").alt = img.alt;
    document.getElementById("lightbox-caption").textContent = shot.textContent.trim();
    box.showModal();
  }
  if (e.target === box || e.target.closest("[data-close]")) box.close();
});
```

`document.addEventListener` + `closest()` tetap berfungsi untuk kartu yang baru ditambahkan OrgSpace, jadi resep ini
tidak perlu menunggu `orgspace:siap`.

### Resep 6: FAQ / detail yang bisa dibuka tanpa JavaScript

Pengganti "halaman detail per item":

```html
<div data-ws-collection="faq">
  <details data-ws-item>
    <summary data-ws-field="pertanyaan">Pertanyaan</summary>
    <p data-ws-field="jawaban" data-ws-type="paragraf">Jawaban.</p>
  </details>
</div>
```

### Resep 7: Statistik dan profil organisasi

```html
<p><strong data-ws-stat="pengurus">12</strong> pengurus ·
   <strong data-ws-stat="prestasi">18</strong> prestasi ·
   <strong data-ws-stat="form.pendaftaran">40</strong> pendaftar</p>

<footer>
  <img src="img/logo.svg" alt="Logo organisasi" data-ws-org="logo">
  <span data-ws-org="nama">Nama Organisasi</span>
  <div data-org-group>
    <a href="#" data-ws-org="whatsapp">WhatsApp</a>
    <a href="#" data-ws-org="instagram">Instagram</a>
  </div>
</footer>
```

Profil yang belum diisi organisasi disembunyikan. Jika semua kontak kosong, sembunyikan juga pembungkusnya:

```js
document.querySelectorAll("[data-org-group]").forEach(function (g) {
  var items = g.querySelectorAll("[data-ws-org]");
  g.hidden = items.length > 0 && Array.prototype.every.call(items, function (el) { return el.style.display === "none"; });
});
```

### Resep 8: Animasi angka statistik

```js
function countUp() {
  if (!("IntersectionObserver" in window)) return;
  var io = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (!entry.isIntersecting) return;
      io.unobserve(entry.target);
      var el = entry.target, target = parseInt(el.textContent, 10) || 0, t0 = null;
      requestAnimationFrame(function frame(t) {
        if (t0 === null) t0 = t;
        var p = Math.min((t - t0) / 1200, 1);
        el.textContent = String(Math.round(target * p));
        if (p < 1) requestAnimationFrame(frame);
      });
    });
  }, { threshold: 0.6 });
  document.querySelectorAll("[data-ws-stat]").forEach(function (el) { io.observe(el); });
}
```

Panggil di `start()` agar angka yang dihitung adalah angka dari database, bukan angka contoh.

## 6. Siapkan untuk pembuat website

- **Teks dan gambar di luar modul** (judul hero, deskripsi, visi, misi, foto utama, teks tombol, footer) ditulis langsung
  di HTML. Setelah upload, provider menandainya di mode **Tandai** di app agar pembuat website bisa menggantinya.
  Beri `id` yang jelas (`hero-judul`, `visi-teks`) agar mudah dikenali saat menandai.
- **Warna dan bentuk** ditulis sebagai variabel di `:root` dan dipakai di seluruh CSS:
  ```css
  :root { --primary: #4f46e5; --accent: #f59e0b; --radius: 16px; }
  .btn { background: var(--primary); border-radius: var(--radius); }
  ```
  Saat Tandai, variabel ini didaftarkan sebagai tema sehingga pembuat website bisa memilih warnanya sendiri.
- Teks contoh netral ("Nama Organisasi", "Judul pengumuman"), karena semua akan diganti.

## 7. Setelah ZIP jadi

Di app OrgSpace: mode **Penyedia Template** → tab **Upload**:

1. **Pilih ZIP**. Pengecekan otomatis menampilkan error (wajib diperbaiki) dan peringatan (sebaiknya diperbaiki)
   lengkap dengan file, baris, dan cara memperbaikinya.
2. **Info**: nama, kategori organisasi, deskripsi, maksimal 5 kata kunci.
3. **Thumbnail**: gambar sampul di galeri.
4. **Tandai**: pilih teks/gambar yang boleh diganti pembuat website (minimal 3), kelompokkan per section, dan daftarkan
   variabel tema.
5. **Coba** lalu **Kirim**. Template tayang di galeri setelah lolos pengecekan akhir.
