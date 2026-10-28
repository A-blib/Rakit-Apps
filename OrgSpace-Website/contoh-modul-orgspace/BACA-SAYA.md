# Paket Contoh Modul OrgSpace

Paket untuk penyedia template yang ingin membuat **template website dinamis** OrgSpace, sendiri atau dibantu AI agent.

```
contoh-modul-orgspace/
├── AGENTS.md          ← instruksi untuk AI agent (Codex, Cursor, dll.)
├── CLAUDE.md          ← sama, untuk Claude Code
├── BACA-SAYA.md       ← file ini
├── docs/
│   ├── 01-panduan-modul-dinamis.md   ← semua modul data-ws-*
│   ├── 02-requirement-dan-aturan.md  ← batas ZIP, CDN, daftar kode pengecekan upload
│   ├── 03-menandai-bagian.md         ← langkah Tandai di app
│   ├── 04-membuat-fitur-kustom.md    ← cara merancang fitur sendiri + resep JavaScript
│   └── 05-tampilan-portal.md         ← portal.json: tampilan Portal Pengurus (opsional)
├── contoh-template/   ← template contoh sederhana yang memakai semua modul (sudah lolos pengecekan)
└── template-sinergi/  ← template lengkap & interaktif: 2 halaman, 7 modul kustom + pengumuman, statistik, animasi (sudah lolos pengecekan)
```

## Cara 1: dibuatkan AI agent

1. Ekstrak ZIP ini, lalu buka foldernya di editor yang punya AI agent (Claude Code, Cursor, Codex, dll.).
2. Beri perintah, misalnya:

   > Baca AGENTS.md lalu buatkan template website untuk OSIS SMA bergaya modern dan cerah, 3 halaman: Beranda,
   > Agenda, dan Tentang. Pengurus harus bisa mengisi agenda (dengan hitung mundur), prestasi (bisa difilter per
   > tingkat), galeri foto, dan struktur pengurus. Tambahkan formulir pendaftaran anggota dan angka statistik.

3. Agent menunjukkan rencana halaman & modul, membuat folder `template/` dan file `template-saya.zip`, lalu memberi
   daftar elemen yang sebaiknya ditandai di app.
4. Upload `template-saya.zip` dari aplikasi OrgSpace (mode Penyedia Template → tab Upload).

## Cara 2: membuat sendiri

1. Salin `contoh-template/` (sederhana) atau `template-sinergi/` (lengkap) menjadi folder baru, ubah desainnya.
2. Jangan hapus atribut `data-ws-*`. Aturan lengkapnya ada di `docs/01-panduan-modul-dinamis.md`.
3. ZIP **isi** foldernya (`index.html` di paling atas ZIP), lalu upload dari aplikasi.

## Mencoba contoh apa adanya

ZIP isi folder `contoh-template/` lalu upload. Template ini memakai: tombol Portal Pengurus, pengumuman, modul
kustom `prestasi` dan `agenda`, formulir `pendaftaran` dan `aspirasi`, serta menu HP dengan JavaScript (`js/main.js`).

Dokumentasi terbaru selalu ada di portal OrgSpace: **Dokumentasi → Penyedia template** (mulai dari "Menjadi Penyedia
Template" dan "Contoh Template & Paket Unduhan").

## Template lengkap: Sinergi

`template-sinergi/` menunjukkan seberapa bebas tampilan template OrgSpace: hero animasi, kartu statistik dari database
(`data-ws-stat`), profil organisasi (`data-ws-org`), modul program/prestasi/agenda/galeri/testimoni/FAQ/struktur,
form pendaftaran 2 langkah, kotak aspirasi, tema gelap, dan animasi saat di-scroll, semuanya tanpa library.
Warna utama, warna aksen, dan sudut diatur lewat variabel `:root` sehingga bisa diganti pembuat website di editor.
