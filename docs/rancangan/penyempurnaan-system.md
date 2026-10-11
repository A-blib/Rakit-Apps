# Penyempurnaan System

> **Status:** dokumen **diskusi dan catatan keputusan** (K1–K24). **Bukan untuk dieksekusi agent langsung.** Hasilnya sudah dipindah ke dokumen instruksi pada 12 Oktober 2026 (bagian 3.2): `revisiFondasiOrgSpace.md` (baru), `alurFiturPembuatanWibesite.md`, `alurFiturPublishWebsite.md`, `alurFiturBuatWibeSiteviatemplate.md`, `alurFiturUpload.md`, `alurUntukProvider.md`.
>
> Sumber: dokumen Aris "Daftar tugas task pengembangan system" (12 Oktober 2026).
> **Terakhir diperbarui:** 12 Oktober 2026
>
> **Nama app: OrgSpace** (diputuskan Aris, 12 Oktober 2026). Menggantikan nama sementara "Template App" / contoh domain "rakit" di dokumen lain.

---

## 1. Ringkasan daftar tugas

| No | Tugas | Status diskusi |
|---|---|---|
| T1 | Penyempurnaan fitur **Publish** (rapi, kokoh, mudah dipelihara) | ✅ **Selesai dibahas** (K21, K24, bagian 6) |
| T2 | Dukungan **template website dinamis** (backend + database) | ✅ **Selesai dibahas** (K5, K18) |
| T3 | Portal upload template **statis & dinamis** bagi provider | ✅ **Selesai dibahas**: template + penanda modul (bagian 3.6, 4.6) |
| T4 | **Custom template**: user merakit section (header, hero, footer, dll.), khusus web statis | ✅ **Selesai dibahas** (K19, K20, bagian 5.5) |
| T5 | **Skeleton section** dari provider (layout paten; user hanya ubah teks, gambar, warna) | ✅ **Selesai dibahas**; dibangun setelah v1 (K20 no. 5) |
| T6 | **Payment gateway** (DANA, GoPay), berbayar mulai publish ke-3 | Dikoordinasikan Aris dengan tim (bagian 7) |
| T7 | **Pivot target pengguna** ke organisasi pendidikan (OSIS, BEM, Pramuka, jurnalistik, dll.) + penyesuaian UI/UX/copywriting | ✅ **Selesai dibahas** (K4–K17, K22–K23). Sisa: daftar kategori final menunggu observasi lapangan Aris |

---

## 2. Keputusan yang sudah fix (dari Aris)

| No | Keputusan |
|---|---|
| K1 | Kedua tipe website (statis dan dinamis) bisa **langsung online** saat user menekan **Publish**. Publish juga dihitung sebagai **"Didownload"** untuk provider |
| K2 | ~~Teknologi yang didukung: web **dinamis** = **Next.js + MySQL atau PostgreSQL** + library populer; web **statis** = **HTML, CSS, JS** + library populer~~ → **Digantikan K18** |
| K3 | Payment versi awal: **DANA dan GoPay** |
| K4 | Nama app: **OrgSpace**. Platform tidak hanya membuat website, tetapi juga menyediakan **ruang kelola** bagi organisasi untuk mengelola website mereka (bagian 3.4) |
| K5 | Bagian dinamis website organisasi memakai **modul dinamis bawaan** platform (bagian 4.3), dikelola lewat menu **Konten** dan **Data masuk** |
| K6 | Kepemilikan: website, domain, pembayaran, dan data modul dimiliki **organisasi**, bukan perorangan. Ada fitur **Anggota** (bagian 3.5) |
| K7 | Urutan pembangunan: **v1** ruang kelola dasar + Anggota → **v2** Konten (Berita, Struktur kepengurusan) → **v3** Data masuk (Open recruitment, Kotak aspirasi) |
| K8 | Domain versi awal: **subdomain bawaan OrgSpace (gratis)** atau **domain yang dibeli organisasi sendiri** (mis. `.my.id`, `.id`, `.com`), dihubungkan lewat alur domain sendiri di `alurFiturPublishWebsite.md`. Fitur bantuan khusus domain sekolah/kampus (pesan instruksi untuk operator IT, contoh surat permohonan, status "menunggu pengelola domain", catatan aturan kampus) **ditunda ke versi berikutnya** |
| K9 | Konten khas organisasi (bagian 3.8) bersifat **disarankan**, bukan wajib. Template kategori organisasi tanpa konten **Inti** tetap boleh diterbitkan; pengecekan upload hanya memberi **Peringatan**. Bisa diperketat menjadi wajib di versi berikutnya bila kualitas template menurun |
| K10 | **Profil organisasi diisi sekali** dan otomatis mengisi kolom template yang dihubungkan provider (bagian 3.9). v1: formulir + isi otomatis; v2: pemberitahuan terbit ulang + lepas hubungan per website |
| K11 | **Target user tunggal: organisasi pelajar/mahasiswa di instansi pendidikan.** Kategori template dan tujuan website dirombak menjadi kategori organisasi (bagian 3.10). OrgSpace bukan lagi penyedia template website umum |
| K12 | Data onboarding berpusat pada organisasi: nama organisasi, kategori, nama instansi, jenjang, dan data penting lain untuk sistem (bagian 3.10) |
| K13 | Copywriting dirombak dari bahasa formal/kaku menjadi bahasa yang dekat dan bermakna bagi anak muda di organisasi pelajar (bagian 3.11) |
| K14 | Gaya desain monokrom ala nextjs.org **ditinggalkan**. Desain baru harus ceria, penuh rasa ingin tahu dan semangat berkembang; warna disesuaikan (bagian 3.11) |
| K15 | Peran anggota organisasi cukup tiga: **Pemilik, Admin, Editor** (bagian 3.5) |
| K16 | Nama menu ruang kelola di app: **"Website"**. Tab "Project" di menu bawah diganti menjadi **Website**: berisi daftar website organisasi (draft maupun online); mengetuk satu website membuka halaman kelolanya (bagian 3.4) |
| K17 | Project disimpan dan **disinkron ke cloud sejak v1**, dengan **kunci edit** (satu website hanya diedit satu orang dalam satu waktu). Pilihan "project tetap di HP" ditolak (bagian 3.5) |
| K18 | **Next.js tidak didukung.** Web dinamis = **template statis (HTML/CSS/JS) + penanda modul `data-ws-*`**; seluruh mesin dinamis (penyimpanan, database, formulir) disediakan backend OrgSpace (bagian 4.6). Menggantikan K2 |
| K19 | Skeleton bawaan OrgSpace untuk mode **Rakit sendiri** (T4/T5): **3 varian tampilan per jenis section** (bagian 5.5) |
| K20 | Rancangan "Rakit sendiri" disetujui: 10 jenis section v1, operasi urut/sembunyi/duplikat/hapus, satu halaman + halaman modul, gradien di latar & tombol, v1 skeleton bawaan saja, mode template dan rakit tidak saling dipindah (bagian 5.5) |
| K21 | Publish: website **dibangun di server** dari data cloud; antrean background job; satu proses per website; konten modul langsung terbit dengan jeda penggabungan ± 30 detik; **rollback penuh** (website + isi editor); Editor punya tombol "Minta diterbitkan"; batas 5 website **per organisasi** (bagian 6) |
| K22 | Palet warna app: **A · Biru Semangat** (biru `#3B5BFD` + aksen kuning `#FFC531`, draf; bagian 3.11) |
| K23 | Font app: **Plus Jakarta Sans** (menggantikan Geist Sans; Geist Mono dihapus). Kalimat sapaan: Beranda "Kerja keras {org} layak dilihat banyak orang", intro "Saatnya {org} punya rumah online", onboarding/serah terima "Mulai jejak digital {org} dari sini" (bagian 3.11) |
| K24 | Batas website ruang pribadi **5** (sama dengan organisasi). Sementara; direvisi setelah aturan langganan T6 |

> **Catatan:** K2 (Next.js + DB dari provider) sudah **digantikan K18**: bagian dinamis website organisasi dipenuhi lewat modul bawaan OrgSpace.

---

## 3. T7: Pivot target pengguna (dibahas pertama)

**Target baru:** organisasi, ekstrakurikuler, dan komunitas di instansi pendidikan (kampus, SMA, SMK). Contoh: BEM, OSIS, Pramuka, tim media/jurnalistik kampus.

**Alasan dibahas pertama:** pivot ini mengubah kategori template, onboarding, copywriting, desain, harga, dan memunculkan kebutuhan baru (kepemilikan bersama). Lebih murah diubah sekarang daripada setelah fitur lain dibangun.

### 3.1 Dampak ke rancangan yang sudah ada

| Bagian | Penyesuaian yang diusulkan |
|---|---|
| Kategori template & tujuan website (onboarding) | **Diputuskan (K11):** kategori organisasi (bagian 3.10; daftar final menunggu observasi) |
| Data onboarding pembuat website | **Diputuskan (K12):** bagian 3.10 |
| **Kepemilikan bersama** | **Diputuskan (K6, K15, K17):** organisasi, fitur Anggota, serah terima, sinkron cloud + kunci edit (bagian 3.5) |
| Copywriting | **Diputuskan (K13, K23):** bagian 3.11 |
| Desain | **Diputuskan (K14, K22, K23):** ceria, palet Biru Semangat, Plus Jakarta Sans (bagian 3.11) |
| Harga | Usulan: harga per **periode kepengurusan** (dikoordinasikan Aris dengan tim, bagian 7.3) |
| Domain sendiri | **Diputuskan (K8):** versi awal hanya subdomain bawaan OrgSpace + domain yang dibeli organisasi sendiri. Alur khusus domain sekolah/kampus (`.sch.id`/`.ac.id`) ditunda ke versi berikutnya |
| Kebutuhan konten khas | Struktur kepengurusan, program kerja, berita kegiatan, agenda, galeri, open recruitment, kotak aspirasi, link media sosial. **Rincian dan keputusan K9 di bagian 3.8** |

### 3.2 Dokumen yang perlu direvisi setelah T7 diputuskan

> ✅ **Sudah diterapkan 12 Oktober 2026.** Perubahan untuk `AGENTS.md` ditulis di dokumen baru **`revisiFondasiOrgSpace.md`** (AGENTS.md tidak diedit). Daftar di bawah dipertahankan sebagai jejak.

- `AGENTS.md`: nilai `website_purpose` di onboarding (sudah dieksekusi → perlu migrasi data/enum baru); bagian 9 (token warna, font, radius) dan `strings.xml` mengikuti K13/K14. Karena AGENTS.md sudah selesai dieksekusi, perubahan ini dibuat sebagai dokumen revisi terpisah, bukan mengedit AGENTS.md
- `alurFiturPembuatanWibesite.md`: kategori galeri, "Template untuk anda", copywriting
- `alurFiturUpload.md`: kategori template di Info template
- `alurFiturBuatWibeSiteviatemplate.md` dan `alurFiturPublishWebsite.md`: kepemilikan website (user → organisasi/tim)
- `alurFiturPublishWebsite.md`: contoh domain `rakit.id` / `rakit.site` dan record TXT `_rakit-verify` diganti mengikuti nama **OrgSpace** (mis. `_orgspace-verify`) setelah domain final dipilih
- Nama tampilan app (`app_name`) menjadi **OrgSpace**. Nama package `com.aris.templateapp` **tidak perlu diganti** (tidak terlihat user; mengganti package berisiko dan memutus konfigurasi OAuth Android/SHA-1)
- `alurFiturPembuatanWibesite.md`: Beranda & daftar Project mengikuti ruang kerja aktif; tambah halaman ruang kelola (bagian 3.4)
- `alurFiturPembuatanWibesite.md`: tab **Project** diganti **Website** (K16); status project/website digabung (draft → online)
- `alurFiturPublishWebsite.md`: revisi besar sesuai K21 (bagian 6): proses 7.5 diganti build di server, 7.5a rollback penuh, 8.x layar status antrean, QR code, Pengaturan situs, hak terbit per peran
- `alurFiturBuatWibeSiteviatemplate.md`: tabel Room `projects` menjadi cache dari data cloud; tambah endpoint project, aset, dan kunci edit (K17)
- `alurFiturUpload.md`: jenis tanda "Daftar konten", validasi modul, uji 0/1/20 item (bagian 3.6), diterapkan saat v2; Peringatan konten Inti untuk kategori organisasi + label isi template (bagian 3.8); pilihan "Hubungkan ke profil organisasi" di langkah Tandai + cek elemen tersembunyi saat kolom profil kosong di langkah Coba (bagian 3.9)

### 3.3 Pertanyaan terbuka T7

1. Arah kategori diputuskan (K11). Sisa: konfirmasi daftar final di bagian 3.10
2. ~~Akun dimiliki perorangan atau organisasi?~~ → **Diputuskan: organisasi (K6)**
3. ~~Bagaimana serah terima saat ganti kepengurusan?~~ → **Diputuskan: lewat fitur Anggota + serah terima (bagian 3.5)**
4. ~~Apakah gaya desain tetap monokrom?~~ → **Diputuskan: diganti (K14); palet A Biru Semangat (K22); font Plus Jakarta Sans (K23)**
5. ~~Project editor di HP atau sinkron ke cloud?~~ → **Diputuskan: sinkron ke cloud + kunci edit sejak v1 (K17)**
6. ~~Tiga peran (Pemilik, Admin, Editor) sudah cukup?~~ → **Diputuskan: cukup (K15)**
7. ~~Nama menu ruang kelola?~~ → **Diputuskan: "Website" (K16)**

### 3.4 Konsep OrgSpace: ruang kelola organisasi (K4)

OrgSpace bukan hanya alat pembuat website, tetapi juga tempat organisasi **mengelola website setelah online**, mirip dashboard Vercel/Netlify versi sederhana untuk OSIS, BEM, dan sejenisnya.

- **Ruang organisasi ada sejak onboarding** (mis. "OSIS SMAN 1"), bukan baru muncul setelah publish. Sebelum publish isinya project; setelah publish, project menjadi **situs online** di ruang yang sama.
- Ada juga **ruang pribadi** (organisasi berisi satu orang) untuk website pribadi/portofolio; menu Anggota disembunyikan di sini.
- **Edit tidak langsung online.** Alur: edit tampilan → tersimpan sebagai **draft** → tekan **"Terbitkan perubahan"** → website online berubah dalam beberapa detik. Setiap terbit menjadi satu versi di Riwayat versi. Alasan: pengunjung tidak melihat hasil setengah jadi, salah edit tidak langsung publik, Admin bisa memeriksa hasil Editor.
- Pemilih ruang kerja di app bar ("OSIS SMAN 1 ▾"), terpisah dari tombol beralih mode pembuat website/provider.
- Ruang kelola juga nantinya bisa dibuka lewat **web dashboard** di website utama OrgSpace.
- Export ZIP tetap ada untuk yang ingin hosting sendiri, tetapi bukan jalur utama.

**Menu per situs**

| Menu | Isi | Tahap |
|---|---|---|
| Ringkasan | Status online/offline, link, tombol buka, **QR code**, jumlah pengunjung singkat | v1 |
| Edit tampilan | Buka editor → Terbitkan perubahan | v1 |
| Riwayat versi | Versi yang pernah terbit, siapa yang menerbitkan, "Kembalikan ke versi ini" | v1 |
| Domain | Subdomain gratis, ganti nama subdomain, custom domain | v1 |
| Pengaturan situs | Judul & deskripsi untuk Google, favicon, gambar saat dibagikan ke WhatsApp, matikan sementara, hapus | v1 |
| Konten | Berita, Struktur kepengurusan, (nanti) Agenda, Galeri, Pengumuman (bagian 3.6) | v2 |
| Data masuk | Kiriman formulir pengunjung (bagian 3.7) | v3 |
| Statistik | Pengunjung per hari, halaman terpopuler | v2 |

**Menu per organisasi**

| Menu | Isi | Tahap |
|---|---|---|
| Anggota | Undang lewat link, atur peran, keluarkan anggota | v1 |
| Riwayat aktivitas | "Rina menerbitkan perubahan · 2 jam lalu" | v1 (dasar) |
| Serah terima | Pindahkan kepemilikan ke pengurus baru, ganti periode | v1 |
| Tagihan | Paket, riwayat pembayaran DANA/GoPay | Menunggu T6 |

### 3.5 Fitur Anggota dan kepemilikan bersama (K6)

**Masalah yang diselesaikan:** pengurus berganti tiap tahun; password akun pribadi dibagi-bagi; kerja dibagi per divisi; website, domain, pembayaran, dan data pendaftar harus tetap milik organisasi; akun satu orang bisa hilang.

**Fungsi fitur Anggota:**
1. Banyak orang masuk ke ruang yang sama dengan **akun masing-masing** (tanpa berbagi password).
2. Membatasi hak lewat peran.
3. Pergantian pengurus mulus: Pemilik lama mengundang ketua baru, menjadikannya Pemilik, lalu keluar. Situs, domain, versi, data, dan tagihan tetap utuh.
4. Riwayat aktivitas mencatat siapa melakukan apa.
5. Mengeluarkan anggota langsung mencabut aksesnya.

| Peran | Contoh orang | Boleh |
|---|---|---|
| **Pemilik** | Ketua / pembina | Semua, termasuk hapus situs dan serah terima. Minimal selalu ada satu Pemilik |
| **Admin** | Sekretaris, bendahara | Terbitkan perubahan, domain, bayar, undang anggota, lihat Data masuk |
| **Editor** | Anggota divisi Humas | Edit isi lalu simpan, kelola Konten; tidak bisa menerbitkan, menghapus, atau melihat Data masuk |

**Titik sentuh sistem:**

| Bagian | Perubahan |
|---|---|
| Onboarding | Pilihan "Untuk organisasi" (buat baru / gabung lewat link undangan) atau "Untuk pribadi". `organization_name` di `creator_profiles` pindah menjadi entitas organisasi |
| Beranda & daftar Project | Mengikuti ruang kerja aktif |
| Editor | Tombol publish/export dikunci sesuai peran |
| Publish & hosting | `sites`, `site_domains`, `site_releases` memakai `organization_id`. Batas 5 situs (D1) dihitung per organisasi |
| Pembayaran (T6) | Tagihan milik organisasi; hanya Pemilik/Admin yang membayar |
| Sisi provider | Tidak terpengaruh; template tetap milik kreatornya |
| Backend (tabel baru) | `organizations`, `organization_members` (user_id, organization_id, role), `organization_invitations` (token hash, berlaku terbatas, sekali pakai, pola seperti `auth_tickets`), `activity_logs` |

**Dampak terbesar (diputuskan K17):** project disimpan di **cloud** agar anggota bisa mengedit dari HP masing-masing. Rincian usulan:
- Backend menyimpan project (isi, nilai kolom, gaya) dan asetnya (gambar) per organisasi. Room di HP berperan sebagai **cache** agar tetap bisa dibuka/diedit saat offline, lalu disinkron saat online.
- **Kunci edit:** saat anggota membuka editor, server memberi kunci atas website itu ("Sedang diedit oleh Rina"). Kunci diperpanjang otomatis selama editor aktif dan **lepas sendiri** bila tidak ada aktivitas beberapa menit (mis. HP mati). Admin/Pemilik boleh **mengambil alih** kunci; perubahan pemegang lama yang belum tersinkron disimpan sebagai cadangan, tidak dibuang.
- Anggota lain tetap bisa **melihat** (mode baca) selama website dikunci.
- Edit offline hanya boleh bila HP memegang kunci yang masih berlaku; jika kunci sudah lepas/diambil alih, perubahan offline disimpan sebagai cadangan dan user diberi pilihan.
- Penyimpanan gambar di VPS perlu batas per organisasi (angka ditentukan saat T6/T1).

### 3.6 Menu Konten dan dampaknya ke provider (K5)

**Fungsi:** mengisi isi yang sering berubah (berita, agenda, galeri, pengumuman, struktur kepengurusan) **tanpa membuka editor tampilan**, rasanya seperti posting: Konten → Berita → + Tambah → judul, foto, isi, tanggal → Terbitkan → berita muncul di halaman Berita (urut terbaru, halaman detail dibuat otomatis).

| | Edit tampilan | Konten |
|---|---|---|
| Mengatur | Desain, susunan section, teks tetap | Isi yang terus bertambah |
| Frekuensi | Jarang | Sering |
| Siapa | Admin | Editor juga bisa |

**Modul v2:** Berita dan Struktur kepengurusan. Agenda, Galeri, Pengumuman menyusul.

**Dampak ke sisi provider (`alurFiturUpload.md`, diterapkan saat v2):**
1. Template menyatakan modul yang didukung ("Mendukung: Berita, Struktur kepengurusan"); jadi label/filter di galeri. Modul **opsional**; template tanpa modul berlabel "Tanpa modul".
2. Jenis tanda baru di langkah Tandai: **"Daftar konten"**. Provider menandai **satu kartu contoh** (mis. `data-ws-list="berita"`), lalu memetakan bagian kartu ke kolom modul (judul, foto, tanggal, ringkasan). **Kolom modul ditetapkan sistem**, provider tidak membuat kolom sendiri.
3. Pengecekan otomatis tambahan: area modul ada dan berisi satu kartu contoh; kolom wajib terpetakan; halaman detail tersedia (jika tidak, sistem memakai **halaman detail bawaan** yang mengikuti warna dan font template).
4. Langkah Coba menguji data dummy **0 item** (tampilan kosong rapi), **1 item**, **20 item** (ada batas + "Lihat semua"), **judul sangat panjang / tanpa foto**. Hasil rusak → Peringatan/Error seperti tahap C.

### 3.7 Menu Data masuk (K5, v3)

**Fungsi:** menampung data yang dikirim **pengunjung** lewat formulir di website (arah pengunjung → organisasi, kebalikan Konten). Contoh: open recruitment, kotak aspirasi (bisa anonim), pendaftaran acara/lomba, kontak.

**Fitur:** daftar kiriman per formulir + penanda baru, detail, status (Baru / Diproses / Selesai, bisa diatur), filter & cari, unduh Excel/CSV, buka/tutup formulir berdasarkan tanggal ("Pendaftaran ditutup"), notifikasi HP (opsional).

**Pertanyaan formulir** disusun organisasi di app (form builder sederhana: teks singkat, paragraf, pilihan, email, nomor HP). Template provider hanya menyediakan **tempat formulir** dengan gaya yang menyatu dengan desainnya.

**Keamanan & data pribadi (wajib):**
- Hanya Admin dan Pemilik yang bisa melihat dan mengunduh.
- Anti spam: batas kiriman per perangkat + pengecekan bot sederhana.
- Teks persetujuan di formulir (data hanya dipakai organisasi untuk keperluan tersebut).
- Tombol hapus semua data setelah periode selesai; data tidak disimpan selamanya.
- Unggah file (CV, foto) **ditunda**.

### 3.8 Konten khas website organisasi (K9)

**Tiga lapis sistem** (agar tidak tertukar):

| Lapis | Pertanyaan | Dilihat oleh | Contoh |
|---|---|---|---|
| 1. Isi website | Apa yang tampil di website? | Pengunjung | Halaman Struktur, Berita, formulir Open recruitment |
| 2. Ruang kelola | Dari mana isinya diatur? | Pengurus (app) | Edit tampilan, Konten, Data masuk |
| 3. Kepemilikan/Anggota | Siapa yang boleh mengatur? | Pengurus (app) | Pemilik, Admin, Editor |

Bagian ini membahas **lapis 1**. Template dari provider menyediakan **desain dan tempat** untuk konten; **isinya** diisi organisasi.

| Konten | Isi | Cara diisi di OrgSpace | Tahap | Tingkat |
|---|---|---|---|---|
| Profil / visi-misi | Teks, logo | Editor tampilan | v1 | **Inti** |
| Struktur kepengurusan | Foto, nama, jabatan, divisi | Modul Konten | v2 (v1: section statis) | **Inti** |
| Kontak + media sosial | Instagram, TikTok, YouTube, WhatsApp, email | Kolom link di template | v1 | **Inti** |
| Program kerja | Program per divisi + status | Section statis; nanti bisa jadi modul | v1 | Disarankan |
| Berita kegiatan | Judul, foto, isi, tanggal | Modul Konten | v2 | Disarankan |
| Galeri | Album foto (dikompres otomatis) | Modul Konten | Setelah v2 | Disarankan |
| Agenda | Acara, tanggal, tempat; yang lewat otomatis turun | Modul Konten | Setelah v2 | Opsional |
| Open recruitment | Formulir pendaftaran | Data masuk | v3 | Opsional |
| Kotak aspirasi | Saran, bisa anonim | Data masuk | v3 | Opsional |

**Penerapan K9:**
- Galeri menampilkan label isi template, mis. *"Punya: Struktur · Berita · Open recruitment"*, dan bisa difilter.
- Saat upload ke kategori organisasi, konten Inti yang tidak ada memunculkan **Peringatan** (bukan Error).
- Daftar ini menjadi "section yang disarankan" di panduan provider dan acuan jenis section bawaan pertama untuk T4/T5.

### 3.9 Profil organisasi diisi sekali (K10)

Data dasar organisasi diketik **sekali** di *Pengaturan organisasi → Profil*, lalu otomatis mengisi kolom yang cocok di semua website organisasi.

**Kolom profil (ditetapkan sistem):** nama organisasi, nama singkat, logo, nama instansi, alamat, email, WhatsApp, Instagram, TikTok, YouTube. Sebagian terisi dari onboarding.

**Cara kerja:**
1. Di langkah Tandai, provider boleh memilih *"Hubungkan ke profil organisasi → {kolom}"* untuk kolom template. **Opsional**; kolom yang tidak dihubungkan diisi manual.
2. Saat organisasi membuat project dari template, kolom terhubung **langsung terisi** dari profil.
3. Di editor, kolom terhubung diberi tanda *"Diambil dari profil organisasi"*. Tombol **"Ubah khusus di website ini"** melepas hubungan kolom itu saja (mis. website acara pensi memakai nama acara).
4. Profil diubah → semua website ikut berubah di **draft** → info *"N website perlu diterbitkan ulang"* + tombol Terbitkan (mengikuti K4, tidak langsung online).

**Aturan tambahan:**
- Kolom profil kosong (mis. tidak punya TikTok) → elemen terkait di template **disembunyikan**, bukan link kosong. Dicek di langkah Coba provider.
- Logo diminta persegi/transparan; sistem memotong dan mengecilkan otomatis.

| Tahap | Cakupan |
|---|---|
| **v1** | Formulir profil + isi otomatis saat project dibuat + penghubung kolom di langkah Tandai |
| **v2** | Info "perlu diterbitkan ulang" saat profil berubah + tombol "Ubah khusus di website ini" |

### 3.10 Kategori dan data onboarding (K11, K12)

**Usulan jenjang:** SMP/MTs · SMA/MA · SMK · Perguruan tinggi.

**Usulan kategori organisasi** (pilihan yang muncul menyesuaikan jenjang):

| Kategori | Sekolah | Kampus |
|---|---|---|
| OSIS / MPK | ✓ | |
| BEM / DEMA / DPM | | ✓ |
| Himpunan / HMJ / HMPS | | ✓ |
| UKM | | ✓ |
| Pramuka | ✓ | ✓ |
| Jurnalistik / Media | ✓ | ✓ |
| PMR / KSR | ✓ | ✓ |
| Paskibra | ✓ | |
| Kerohanian | ✓ | ✓ |
| Seni & Olahraga | ✓ | ✓ |
| Ekskul / Komunitas lain | ✓ | ✓ |

Kategori ini dipakai bersama oleh: onboarding, kategori template di galeri, kategori template saat provider upload (`alurFiturUpload.md` Info template), dan "Template untuk anda" di Beranda. Menggantikan `website_purpose` lama (sekolah/organisasi/umkm/instansi/pribadi/lainnya).

**Usulan data onboarding organisasi:**

| Kolom | Wajib | Dipakai untuk |
|---|---|---|
| Nama organisasi | Ya | Profil organisasi (K10), isi otomatis template |
| Kategori | Ya | Rekomendasi template |
| Jenjang | Ya | Menyaring kategori & template |
| Nama instansi | Ya | Profil organisasi, isi otomatis template |
| Kota / kabupaten | Tidak | Statistik pengguna per daerah, pencarian nanti |
| Periode kepengurusan (tahun mulai–selesai) | Tidak | Serah terima, pengingat perpanjangan (T6) |
| Jabatan kamu | Tidak | Ditampilkan di daftar Anggota |
| Logo | Tidak (bisa nanti) | Profil organisasi |

Pilihan **"Gabung organisasi yang sudah ada"** (lewat link undangan) tersedia di awal onboarding, sehingga anggota tidak membuat organisasi ganda.

### 3.11 Bahasa dan desain (K13, K14)

**Prinsip bahasa:**
- Sapa dengan "kamu"; kalimat pendek dan hangat.
- Sebut konteks organisasi ("website OSIS-mu", "pengurus", "periode", "proker") bila datanya ada.
- Hindari istilah teknis: "deploy", "hosting", "template engine", "provider" di sisi pembuat website.
- Santai tapi tidak alay; pesan error tetap jelas dan memberi jalan keluar.

| Sebelum | Sesudah (usulan) |
|---|---|
| Bikin website. Tanpa coding. (intro) | **Saatnya {org} punya rumah online** — Satu tempat untuk profil, kegiatan, dan kabar terbaru kalian. |
| Buat website pertamamu (Beranda) | **Kerja keras {org} layak dilihat banyak orang** — Tunjukkan kegiatan dan karya kalian lewat website sendiri. Tombol: "Mulai sekarang" |
| (onboarding / serah terima) | **Mulai jejak digital {org} dari sini** — Website yang bisa terus dikelola dari periode ke periode. |
| Fitur pembuatan website segera hadir. | Lagi kami siapkan. Sebentar lagi kamu bisa mulai! |
| Email atau password salah. | Email atau password-nya belum cocok. Cek lagi, ya. |
| Tidak ada koneksi. Periksa internet lalu coba lagi. | Koneksi internetmu putus. Coba sambungkan lagi, lalu ketuk Coba lagi. |
| Kamu mau pakai app ini sebagai apa? | Kamu datang ke OrgSpace untuk apa? |

**Arah desain (pengganti AGENTS.md bagian 9):**
- Tetap rapi dan bersih (spasi lega, hierarki jelas), tetapi **berwarna, hangat, dan ceria**.
- Satu warna utama yang semangat + satu warna aksen hangat; latar terang yang sedikit berwarna, bukan putih/hitam murni.
- Sudut lebih membulat (radius 12–16dp untuk kartu, tombol berbentuk pil).
- Ilustrasi/Lottie berwarna dengan nuansa sekolah dan kegiatan, tetap tidak bernuansa gaming atau kekanak-kanakan.
- Semua tetap lewat token tema, sehingga perubahan cukup di `colors.xml`, `themes.xml`, `dimens.xml`, dan `res/font/`.
- **Palet dipilih (K22): A · Biru Semangat** — warna utama biru `#3B5BFD`, aksen kuning `#FFC531`, latar terang kebiruan `#F5F7FF`, teks gelap `#1A2150`. Nilai ini masih draf; diturunkan menjadi token lengkap (terang + gelap + sukses/peringatan/error) yang dicek kontrasnya sebelum dijadikan instruksi agent.
- Alasan: biru netral untuk semua kategori organisasi, terasa terpercaya dan dekat dengan pendidikan; kuning sebagai aksen memberi nuansa ceria tanpa kekanak-kanakan. Alternatif tidak dipilih: B Hijau Tumbuh (bentrok dengan warna status sukses/online), C Ungu Kreatif (kurang "resmi" untuk OSIS/BEM/pembina).
- Warna app tidak membatasi warna website organisasi; website tetap memakai tema pilihan masing-masing.
- **Font dipilih (K23): Plus Jakarta Sans** (lisensi SIL OFL, karya Tokotype, Indonesia) untuk judul dan isi, menggantikan Geist Sans. **Geist Mono dihapus**; label kecil dan badge memakai Plus Jakarta Sans tebal. File font + lisensi OFL disertakan di `res/font/`.
- `{org}` pada kalimat sapaan diganti otomatis dengan nama singkat organisasi (Profil organisasi, K10). Hindari ajakan generik seperti "Yuk, bikin website ...".

---

## 4. T2 & T3: Template website dinamis

### 4.1 Permintaan

Sistem tidak hanya melayani website statis, tetapi juga template **dinamis** (sistem utuh dengan backend + database) yang bisa menyimpan dan memproses data user. Provider bisa mengupload template dinamis miliknya (K2: Next.js + MySQL/PostgreSQL), dengan syarat mematuhi requirement sistem.

### 4.2 Analisis: kenapa Next.js + database dari provider berat

| Kebutuhan | Artinya |
|---|---|
| Menjalankan build (`npm install`, `next build`) | Menjalankan kode milik provider di server sendiri; risiko keamanan yang sebelumnya sengaja dihindari (`alurFiturUpload.md` bagian 10.3) |
| 1 proses Node.js per website | ± 100–300 MB RAM per website; VPS 2 GB hanya muat beberapa website |
| 1 database/skema per website | Isolasi data antar organisasi |
| Isolasi (container, batas RAM/CPU) | Agar satu website tidak menjatuhkan yang lain |
| Update keamanan berkelanjutan | Celah keamanan framework harus ditambal di setiap website yang sudah online, padahal kodenya milik provider |
| Biaya bulanan per website | Tidak cocok dengan publish gratis selamanya |
| Sistem penandaan tidak berlaku | Penandaan bekerja di HTML statis (`data-key`). Di Next.js, isi dibuat oleh React dan biasanya disimpan di database, diedit lewat halaman admin website itu sendiri |

Secara teknis tetap **bisa** (mis. dengan alat self-host PaaS seperti Coolify atau Dokku), tetapi skalanya setara **membangun layanan seperti Vercel sendiri**.

### 4.3 Alternatif yang diusulkan: modul dinamis bawaan

Website tetap statis (HTML/CSS/JS), tetapi bagian dinamisnya memakai **modul yang disediakan platform**, dijalankan oleh **satu backend milik Aris** (Spring Boot + PostgreSQL, data dipisah per website).

| Modul | Contoh pemakaian organisasi |
|---|---|
| Berita / kegiatan | Liputan acara, pengumuman |
| Agenda | Jadwal rapat, lomba, event |
| Open recruitment | Form pendaftaran anggota; data masuk ke app |
| Struktur kepengurusan | Foto dan jabatan pengurus per periode |
| Galeri | Dokumentasi kegiatan |
| Kotak aspirasi / kontak | Saran dari siswa/mahasiswa |

- Provider membuat **tampilan** saja, lalu menandai area modul (mis. `data-ws-module="berita"`).
- Pengurus mengelola isi modul **dari app**.
- Pengunjung merasakan website dinamis; platform tetap aman, murah, dan mudah dirawat.

### 4.4 Perbandingan

| | Next.js + DB dari provider (K2) | Modul dinamis bawaan (usulan) |
|---|---|---|
| Keamanan | Menjalankan kode provider di server | Kode backend hanya milik platform |
| Biaya per website | Tinggi (RAM per proses, DB per website) | Sangat kecil (satu backend bersama) |
| Bisa diedit pengurus non-teknis | Tergantung halaman admin buatan provider | Ya, dari app |
| Fleksibilitas fitur | Tinggi (provider bebas membuat fitur apa pun) | Terbatas pada modul yang disediakan |
| Usaha membangun | Sangat besar | Sedang, bertahap per modul |

### 4.5 Pertanyaan terbuka T2/T3

1. ~~Tetap Next.js + DB dari provider, atau beralih ke modul dinamis bawaan?~~ → **Diputuskan: modul dinamis bawaan (K5); Next.js tidak didukung (K18)**
2. ~~Jika modul bawaan: modul apa saja di versi pertama, dan urutannya?~~ → **Diputuskan (K7):** v2 Berita + Struktur kepengurusan; v3 Open recruitment + Kotak aspirasi
3. ~~Jika tetap Next.js: bagaimana model biaya bulanan per website, isolasi, dan update keamanan?~~ → tidak berlaku (K18)

### 4.6 Solusi akhir template dinamis (K18)

**Prinsip:** provider membuat **tampilan**, OrgSpace menyediakan **mesin**. Analogi: provider membuat file tampilan seperti `.ejs`; OrgSpace memegang render dan database.

**Penanda yang dipakai provider** (dipasang manual atau otomatis lewat langkah Tandai, jenis "Daftar konten"):

```html
<section data-ws-list="berita" data-ws-limit="6">
  <article data-ws-item>                      <!-- satu kartu contoh; sistem yang mengulang -->
    <img data-ws-field="foto" src="contoh.jpg" alt="">
    <span data-ws-field="tanggal">12 Okt 2026</span>
    <h3 data-ws-field="judul">Judul contoh</h3>
    <p data-ws-field="ringkasan">Ringkasan...</p>
    <a data-ws-link="detail">Baca selengkapnya</a>
  </article>
  <p data-ws-empty>Belum ada berita.</p>          <!-- tampil saat data kosong -->
</section>

<div data-ws-form="pendaftaran"></div>          <!-- tempat formulir; pertanyaan disusun organisasi di app -->
```

**Cara website hidup (gabungan):**

| Jenis | Cara kerja | Alasan |
|---|---|---|
| Konten (berita, struktur, agenda, galeri) | **Dibuat saat terbit**: server menggabungkan template + data menjadi halaman HTML statis (index, daftar, detail) → release baru (`alurFiturPublishWebsite.md`) | Cepat, ringan di VPS, terbaca Google, tetap memakai release & rollback |
| Formulir (Data masuk) | **Lewat API saat dikunjungi**: script kecil OrgSpace mengirim isian ke API publik OrgSpace | Data datang dari pengunjung kapan saja |

- Posting/ubah **konten** langsung memicu pembuatan ulang halaman terkait (pengecualian K4, karena konten memang dimaksudkan langsung terbit). Perubahan **desain** tetap draft → Terbitkan.
- JS di browser tetap boleh untuk interaksi tampilan (slider, filter, animasi), memakai library dari daftar resmi.

**Batasan:** fitur dinamis terbatas pada modul OrgSpace. Fitur baru diusulkan ke OrgSpace lalu ditambahkan sebagai modul yang bisa dipakai semua template.

**Alternatif tidak dipilih:** bahasa template Mustache/Handlebars (`{{#berita}}`). Populer dan aman, tetapi provider harus belajar sintaks baru, HTML tidak bisa dicek langsung di browser, dan tidak cocok dengan langkah Tandai.

**Daftar modul belum final:** Aris akan **observasi lapangan** ke organisasi untuk memastikan fitur yang benar-benar dibutuhkan. Daftar modul dan urutan K7 bisa berubah sesuai hasilnya.

---

## 5. T4 & T5: Custom template dan skeleton section

### 5.1 Permintaan

- **T4:** user merakit website statis sendiri dari section (header, hero, footer, dll.). Sumber section: **bawaan sistem** dan **buatan provider**. Hanya untuk web statis.
- **T5:** provider mengupload **skeleton** per section dengan layout paten (mis. hero: gambar di kanan; judul, paragraf, tombol di kiri). User **boleh** mengubah teks, gambar, dan warna; **tidak boleh** mengubah bentuk, posisi, atau struktur layout.

### 5.2 Analisis

- T5 adalah **pemasok bahan** untuk T4; keduanya dirancang bersama.
- Aturan modifikasi user sudah cocok dengan sistem penandaan + gaya yang ada (`alurFiturUpload.md` bagian 7.11).
- Keputusan lama yang sejalan: kerangka bawaan app dulu, kerangka buatan provider menyusul (`alurFiturPembuatanWibesite.md` bagian 1).

### 5.3 Tantangan teknis yang harus dirancang

| Tantangan | Usulan |
|---|---|
| CSS antar section dari provider berbeda saling menimpa | Sistem otomatis memberi awalan unik pada CSS tiap section; aturan global (reset, selector tag polos) dilarang |
| Tampilan hasil rakitan tidak seragam | **Kontrak tema bersama**: semua section wajib memakai variabel standar (`--warna-utama`, `--font-judul`, `--radius`, dll.) |
| JS antar section bentrok | Batasi JS per section (mis. hanya library dari daftar resmi, dibungkus per section) |
| Responsif | Setiap section wajib lolos cek tampilan HP dan desktop |
| Format upload | Perlu ditentukan: 1 ZIP = 1 section, berisi HTML + CSS (+ JS opsional) + metadata jenis section |

### 5.4 Pertanyaan terbuka T4/T5 (semua terjawab di K20)

1. Jenis section apa saja yang didukung di versi pertama?
2. Apakah user boleh mengurutkan, menyembunyikan, dan menduplikat section di custom mode?
3. Satu halaman atau multi-halaman untuk custom mode?

### 5.5 Rancangan "Rakit sendiri" (K19, K20 — disetujui)

**Konsep:** `Website = Header + [Section]... + Footer`; setiap section = **skeleton** (layout terkunci) + **isi** (bebas) + **gaya** (terbatas).

| | Boleh diubah user | Tidak boleh |
|---|---|---|
| Isi | Teks, nama, gambar, link, tombol | — |
| Gaya | Warna, gradien, font (dari daftar), radius | — |
| Elemen opsional | Tampilkan/sembunyikan elemen yang disediakan skeleton | Menambah elemen baru |
| Layout | — | Posisi, susunan, jumlah kolom, grid, CSS bebas |

**Alur user:** [+] → "Rakit sendiri" → pilih tema dasar (palet + font) → halaman awal Header + Hero + Footer (terisi dari Profil organisasi, K10) → "+ Tambah section" → pilih jenis → pilih varian → isi → atur warna → urutkan/sembunyikan/duplikat/hapus → pratinjau HP/Desktop → Terbitkan.

**"Ganti tampilan" tanpa kehilangan isi:** setiap jenis section punya **kolom standar** (mis. Hero: judul, subjudul, gambar, tombol 1, tombol 2), sehingga isi otomatis pindah saat varian diganti.

**Warna dan gradien:**
- **Tema global:** warna utama, aksen, latar, teks, font judul/isi, radius → semua section.
- **Latar per section:** polos / **gradien** (2–3 warna, arah, **slider proporsi** tiap warna, preset gradien) / gambar + lapisan warna.
- Tombol boleh bergradien; teks judul bergradien hanya bila skeleton mendukung.
- **Cek keterbacaan otomatis:** kontras teks vs latar rendah → peringatan + tombol "Perbaiki otomatis".

**Skeleton dari provider (T5):** 1 ZIP = 1 section (`section.html`, `section.css`, `section.js` opsional, info jenis). Aturan wajib: CSS memakai variabel tema (`--os-primary`, `--os-accent`, `--os-bg`, `--os-font-heading`, `--os-radius`, `--os-section-bg`, dll.); sistem memberi awalan unik pada CSS, selector global ditolak; kolom mengikuti daftar standar per jenis; lolos cek HP & Desktop; JS hanya dari daftar resmi. Wizard: Pilih file → Pengecekan → Info → Petakan kolom → Coba → Kirim. Langkah Coba otomatis menguji 3 palet, latar gradien, latar gelap, teks sangat panjang, gambar kosong.

**Hubungan:** Header/Footer/Kontak dari Profil organisasi (K10); Berita/Struktur/Agenda memakai `data-ws-list` (K18); jenis section dari konten khas (K9); hasil muncul di tab Website (K16).

**Keputusan:** **K19 — skeleton bawaan 3 varian per jenis section.** Dengan 10 jenis section usulan, berarti ± **30 skeleton** yang harus didesain dan dibuat tim OrgSpace untuk v1.

**K20 — disetujui Aris (12 Okt 2026):**
1. Jenis section v1 (10): Header, Hero, Profil/Visi-misi, Struktur kepengurusan, Program kerja, Berita, Galeri, Kontak + Media sosial, Ajakan (CTA), Footer.
2. User boleh mengurutkan, menyembunyikan, menduplikat, menghapus section; Header & Footer wajib ada.
3. v1 satu halaman + halaman otomatis dari modul (daftar & detail berita); multi-halaman menyusul.
4. Gradien di latar section dan tombol; teks bergradien hanya bila skeleton mendukung.
5. v1 hanya skeleton bawaan OrgSpace; skeleton buatan provider (T5) di versi berikutnya.
6. Website mode template tidak bisa dipindah ke mode rakit, dan sebaliknya.

---

## 6. T1: Penyempurnaan Publish (K21)

Fondasi `alurFiturPublishWebsite.md` (Cloudflare DNS, Caddy, folder `data/` + symlink `hosts/`, release, domain sendiri, ganti nama subdomain) **tetap dipakai**. Yang berubah:

### 6.1 Website dibangun di server

```
Tekan "Terbitkan"
→ server mengambil data project dari cloud (template/skeleton + isi + gaya + tema)
→ mengambil data modul (berita, struktur) + profil organisasi
→ menyusun halaman HTML jadi (index, /berita, /berita/{judul})
→ cek keamanan → simpan sebagai release baru → ganti symlink atomik → online
```

- Menggantikan alur lama "HP membuat ZIP → upload" (bagian 7.5 lama).
- Setiap release menyimpan **salinan data project** (snapshot), sehingga **rollback penuh**: website **dan** isi editor kembali ke versi itu. Batasan D4 lama hilang.
- Export ZIP tetap ada, **dibuat server** dengan proses yang sama.
- Terbit bisa dilakukan dari HP mana pun oleh anggota yang berhak.

### 6.2 Kokoh dan mudah dirawat

| Prinsip | Penerapan |
|---|---|
| Tahapan jelas | `SiteBuildService`: Kumpulkan data → Susun HTML → Periksa → Tulis release → Ganti symlink → Catat. Tiap tahap punya test; gagal di tengah → website lama tetap online |
| Antrean (background job) | Status di app: Mengantre → Membangun → Online ✓ / Gagal + alasan. App boleh ditutup |
| Satu proses per website | Permintaan baru saat sedang membangun digabung ke proses berikutnya |
| Penggabungan konten | Posting/ubah konten modul **langsung terbit**, dengan jeda penggabungan ± 30 detik (5 berita dalam 1 menit → 1 kali bangun) |
| Pembersihan terjadwal | Hapus release lama, periksa ulang domain sendiri, pantau sisa disk VPS |

### 6.3 Penyesuaian dengan keputusan lain

| Bagian | Baru |
|---|---|
| Pemilik website | Organisasi (K6) |
| Hak terbit | Admin & Pemilik (K15). Editor punya tombol **"Minta diterbitkan"** (v1) → notifikasi ke Admin |
| Batas website (D1) | **5 per organisasi** dan **5 untuk ruang pribadi** (K24; sementara, direvisi setelah aturan langganan T6) |
| Domain | Sesuai K8 |
| Layar setelah terbit | Link, Salin, Bagikan, Buka + **QR code** (bisa diunduh) |
| Pengaturan situs | Judul & deskripsi Google, favicon, gambar saat dibagikan ke WhatsApp |
| Riwayat | Versi + siapa yang menerbitkan + Riwayat aktivitas |
| Formulir (v3) | API publik untuk Data masuk + aturan CORS saat v3 |
| Nonaktifkan | Berganti label "Matikan sementara" (K13) |

### 6.4 Pertanyaan terbuka T1

1. ~~Batas website untuk ruang pribadi?~~ → **Diputuskan: 5 untuk sementara (K24)**; direvisi setelah Aris berkoordinasi soal langganan (T6)

---

## 7. T6: Payment gateway

### 7.1 Permintaan

Integrasi pembayaran (awal: DANA dan GoPay). Publish **pertama dan kedua gratis**; mulai **publish ketiga** berbayar. Kode harus rapi, konsisten, terstruktur, stabil, aman, dan mudah dipelihara.

### 7.2 Analisis

| Hal | Catatan |
|---|---|
| Cara integrasi | DANA dan GoPay paling mudah disediakan **lewat payment gateway** (mis. Midtrans, Xendit). Integrasi langsung biasanya butuh syarat merchant lebih berat. Lewat gateway, QRIS juga tersedia sehingga semua e-wallet bisa dipakai |
| Google Play Billing | Tidak wajib karena APK didistribusikan lewat website sendiri; cek ulang jika kelak masuk Play Store |
| Keamanan | Server key hanya di backend; status bayar hanya dari **webhook terverifikasi**; proses idempotent; semua transaksi tercatat |
| Uji coba | Gunakan mode **sandbox** gateway selama pengembangan |

### 7.3 Pertanyaan terbuka T6

> Aris akan mengoordinasikan aturan fitur berbayar dengan rekan timnya. Usulan arah dari diskusi: harga **per periode kepengurusan**, invoice untuk LPJ, pengingat perpanjangan, dan bila tidak diperpanjang turun ke paket gratis (tidak langsung dihapus).

1. "Publish ke-3" maksudnya **website ke-3** (update gratis) atau **setiap klik Publish** termasuk Perbarui? (Usulan: website ke-3; update tetap gratis.)
2. Bayar **sekali** atau **berlangganan** (per bulan / per tahun / per periode kepengurusan)?
3. Berapa harganya, dan apakah web dinamis lebih mahal dari statis?
4. Bagaimana dengan batas **5 website per akun** di `alurFiturPublishWebsite.md` (D1)?
5. Apakah provider mendapat **bagi hasil**?

---

## 8. Usulan urutan diskusi

| Urutan | Tugas | Alasan |
|---|---|---|
| 1 | **T7** Pivot | Mengubah fondasi: kategori, onboarding, kepemilikan, desain |
| 2 | **T2/T3** Dinamis | Keputusan terbesar; mempengaruhi Publish dan Payment |
| 3 | **T4/T5** Custom & skeleton | Butuh kategori dan kontrak tema dari keputusan sebelumnya |
| 4 | **T6** Payment | Butuh kejelasan apa yang dijual |
| 5 | **T1** Penyempurnaan Publish | Menggabungkan hasil semua keputusan di atas |

---

## 9. Log keputusan

| Tanggal | Keputusan |
|---|---|
| 12 Okt 2026 | Dokumen dibuat dari daftar tugas Aris. K1–K3 dicatat. Diskusi dimulai dari T7 |
| 12 Okt 2026 | Nama app diputuskan **OrgSpace**. Disetujui: konsep ruang kelola (K4), modul dinamis bawaan via menu Konten & Data masuk (K5), kepemilikan organisasi + fitur Anggota (K6), urutan v1→v2→v3 (K7). Dampak ke dokumen lain dicatat di 3.2 dan 3.6, belum diterapkan |
| 12 Okt 2026 | T6 (fitur berbayar) dikoordinasikan Aris dengan rekan tim. K8: domain versi awal = subdomain bawaan + domain beli sendiri; alur domain sekolah/kampus ditunda |
| 12 Okt 2026 | K9: konten khas organisasi bersifat disarankan (Peringatan saat upload, bukan Error). Rincian di 3.8 |
| 12 Okt 2026 | K10: profil organisasi diisi sekali (bagian 3.9), v1 minimal, sisanya v2 |
| 12 Okt 2026 | K11–K14: target tunggal organisasi pendidikan, onboarding berpusat organisasi, copywriting anak muda, desain monokrom diganti ceria. Usulan detail di 3.10–3.11 menunggu konfirmasi |
| 12 Okt 2026 | K15: tiga peran (Pemilik, Admin, Editor) cukup |
| 12 Okt 2026 | K16: nama menu "Website" (menggantikan tab Project). K17: project sinkron ke cloud + kunci edit sejak v1 |
| 12 Okt 2026 | K18: Next.js tidak didukung; web dinamis = template + penanda modul `data-ws-*`, mesin dari OrgSpace (menggantikan K2). T2/T3 selesai dibahas. Aris akan observasi lapangan untuk daftar modul |
| 12 Okt 2026 | T4/T5 mulai dibahas. K19: skeleton bawaan 3 varian per jenis section. Rancangan "Rakit sendiri" di 5.5 |
| 12 Okt 2026 | K20: seluruh usulan "Rakit sendiri" disetujui. T4/T5 selesai dibahas |
| 12 Okt 2026 | K21: penyempurnaan Publish disetujui (build di server, antrean, penggabungan konten, rollback penuh, Minta diterbitkan, batas per organisasi) |
| 12 Okt 2026 | K22: palet warna app A · Biru Semangat |
| 12 Okt 2026 | K23: font Plus Jakarta Sans; kalimat sapaan Beranda/intro/onboarding diganti |
| 12 Okt 2026 | K24: batas ruang pribadi 5 (sementara, menunggu T6). T1 selesai dibahas |
| 12 Okt 2026 | Semua keputusan K1–K24 diterapkan ke dokumen instruksi. Dibuat `revisiFondasiOrgSpace.md`; direvisi `alurFiturPembuatanWibesite.md`, `alurFiturPublishWebsite.md`, `alurFiturBuatWibeSiteviatemplate.md`, `alurFiturUpload.md`, `alurUntukProvider.md` |
