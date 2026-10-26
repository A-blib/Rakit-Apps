tolong kamu exsekusi dan buat lah template web dinamis ini teman

dan nanti coba kamu gunakan fitur fitur yang ada di orgspace-apps buat uplod as template agar bisa di gunakan oleh user yang mau bua web site

lalu coba kamu online kan juga web nya dan lakukan test

ini dia gambaran nya

buat lah webisite dinamis mengukuti arah ini

# Blueprint & Arsitektur UI/UX Website Creative Crew

**Tema Visual:** Black & Yellow (Hitam `#121212` & Kuning _Cyber/Mustard_)
**Vibe:** Modern, Profesional, Berjiwa Muda
**Struktur Layout:** Multi-page (Halaman Terpisah)

---

## 🎨 STRUKTUR HALAMAN FRONTEND (PUBLIC)

Website dipisah menjadi beberapa halaman (_routing_) khusus agar konten tidak menumpuk di _Landing Page_ dan navigasi lebih terarah.

### 1. Halaman Utama (`/home` atau Landing Page)

- **Hero Section (Fokus Utama):**
  - **Visual:** Latar belakang _full-width_ menggunakan **foto grup seluruh kru beresolusi tinggi** (_in-action_ atau kasual). Terdapat _overlay vignette_ (gradasi gelap di area teks) agar teks terbaca sempurna.
  - **Teks Utama:** "WE ARE THE **CREATIVE FORCE** ON CAMPUS" (Teks tebal, kata "CREATIVE FORCE" berwarna kuning terang).
  - **Call to Action (CTA):** Dua tombol utama: "Start a Project" (mengarahkan ke `/collab`) dan "Meet The Crew" (mengarahkan ke `/crew`).
- **Highlight Section:** Hanya menampilkan 3-4 "Featured Projects" terbaik dan cuplikan singkat divisi yang ada.

### 2. Halaman Kerja Sama & Layanan (`/collab`)

- **Fungsi:** Halaman khusus (_Dedicated Page_) bagi organisasi/klien kampus untuk mengajukan permintaan _project_.
- **Bagian 1 (Penjelasan Alur):** Visualisasi _step-by-step_ kolaborasi (misal: _1. Submit Brief -> 2. Review -> 3. Production -> 4. Handover_).
- **Bagian 2 (Form Pengajuan Lengkap):** Formulir dengan layout 2 kolom yang meminta input:
  - Nama Organisasi / Klien & Kontak PIC.
  - Jenis Layanan (Dropdown: Video Production, Desain Grafis, Liputan Event, dll).
  - Tenggat Waktu (_Deadline_ yang diharapkan).
  - Detail Kebutuhan (_Brief_ teks panjang).
- **Interaksi:** Input _field_ memiliki _outline_ yang menyala kuning saat diketik. Setelah klik "Submit", muncul halaman pop-up atau pengalihan ke halaman _Thank You_ berisi estimasi SLA balasan.

### 3. Halaman Portofolio (`/works`)

- **Fungsi:** Etalase lengkap seluruh karya organisasi.
- **Tampilan:** Layout _Masonry Grid_ (seperti Pinterest).
- **Navigasi:** Menu _filter_ di bagian atas (All, Video, Design, Event).
- **Interaksi:** Saat _thumbnail_ karya di-klik, pengunjung masuk ke halaman detail spesifik (`/works/[id]`) yang berisi _case study_ proyek, _gallery_ aset, dan _credit title_ anggota yang mengerjakan.

### 4. Halaman Kru & Profil (`/crew`)

- **Fungsi:** Memperkenalkan anggota aktif berdasarkan divisi.
- **Tampilan:** Grid foto potret anggota.
- **Interaksi:** Foto _default_ hitam-putih (_Grayscale_). Saat disorot kursor, foto berubah _full color_ dan menampilkan tautan portofolio/sosmed individu.

---

## ⚙️ STRUKTUR HALAMAN BACKEND (ADMIN DASHBOARD)

Sistem internal menggunakan _Sidebar Navigation_ di sisi kiri untuk perpindahan menu yang cepat dan area konten di kanan.

### 1. Dasbor Metrik (`/admin/dashboard`)

- **Fokus:** Tampilan ringkasan (Overview).
- **Tampilan:** _Cards_ berisi angka metrik: Total _Request_ Kolaborasi Baru (_Unread_), Total Proyek Aktif Bulan Ini, dan Jumlah Alat Produksi yang Sedang Keluar/Dipinjam.

### 2. Inbox Kolaborasi (`/admin/inbox`)

- **Fokus:** Ruang kerja khusus untuk memproses form pengajuan dari halaman `/collab`.
- **Tampilan:** Layout **Kanban Board** (mirip Trello) dengan kolom status: _New Request_, _On Review_, _In Progress_, _Completed_.
- **Aksi:** Admin dapat membaca _brief_ lengkap di dalam _card_ tiket, menugaskan PIC, lalu menggeser tiket ke kolom status selanjutnya secara _drag-and-drop_.

### 3. Manajemen Portofolio (`/admin/projects`)

- **Fokus:** _Content Management System_ (CMS) untuk memperbarui halaman `/works`.
- **Tampilan:** Tabel daftar karya.
- **Aksi:** Tombol "Add New Work" untuk mengunggah aset multimedia, menulis teks _case study_, melakukan _tag_ anggota yang berkontribusi, dan _toggle switch_ (Publish / Draft).

### 4. Inventaris & Peminjaman (`/admin/gear`)

- **Fokus:** Melacak fisik alat produksi.
- **Tampilan:** Daftar aset (Kamera, Lensa, Audio) dengan indikator ketersediaan (Tersedia, Dipinjam, Maintenance).
- **Lokasi Penyimpanan:** Modul spesifik yang mencatat lokasi detail barang (misalnya: _Lemari Utama, Laci Bawah_) agar pengurus mudah mengambil/mengembalikan barang.
- **Log:** Kalender riwayat siapa yang sedang meminjam barang dan jadwal pengembalian.

### 5. Database Kru & Rekrutmen (`/admin/crew`)

- **Fokus:** Manajemen SDM internal.
- **Tampilan:** Dibagi dua _tab_. _Tab_ "Roster" untuk daftar anggota aktif saat ini (bisa _edit_ foto dan posisi divisi), dan _Tab_ "Recruitment" (aktif saat musim _Oprec_ untuk memantau data CV/portofolio pelamar baru).
