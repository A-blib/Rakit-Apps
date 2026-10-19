# Paket Contoh Modul OrgSpace

Paket untuk penyedia template yang ingin membuat **template website dinamis** OrgSpace, sendiri atau dibantu AI agent.

```
contoh-modul-orgspace/
├── AGENTS.md          ← instruksi untuk AI agent (Codex, Cursor, dll.)
├── CLAUDE.md          ← sama, untuk Claude Code
├── BACA-SAYA.md       ← file ini
├── docs/
│   ├── 01-panduan-modul-dinamis.md   ← semua modul data-ws-*
│   ├── 02-aturan-wajib-template.md   ← aturan agar lolos pengecekan upload
│   └── 03-requirement-dan-tools.md   ← batas ZIP, struktur folder, CDN
├── contoh-template/   ← template contoh sederhana yang memakai semua modul (sudah lolos pengecekan)
└── template-sinergi/  ← template lengkap & interaktif: 2 halaman, 8 modul, statistik, animasi (sudah lolos pengecekan)
```

## Cara 1: dibuatkan AI agent

1. Ekstrak ZIP ini, lalu buka foldernya di editor yang punya AI agent (Claude Code, Cursor, Codex, dll.).
2. Beri perintah, misalnya:

   > Baca AGENTS.md lalu buatkan template website untuk OSIS SMA bergaya modern dan cerah. Pakai modul
   > pengumuman, prestasi, galeri kegiatan, dan formulir pendaftaran anggota.

3. Agent membuat folder `template/` dan file `template-saya.zip`.
4. Upload `template-saya.zip` dari aplikasi OrgSpace (mode Penyedia Template → tab Upload).

## Cara 2: membuat sendiri

1. Salin `contoh-template/` menjadi folder baru, ubah desainnya.
2. Jangan hapus atribut `data-ws-*`. Aturan lengkapnya ada di `docs/01-panduan-modul-dinamis.md`.
3. ZIP **isi** foldernya (`index.html` di paling atas ZIP), lalu upload dari aplikasi.

## Mencoba contoh apa adanya

ZIP isi folder `contoh-template/` lalu upload. Template ini memakai: tombol Portal Pengurus, pengumuman, modul
kustom `prestasi` dan `agenda`, formulir `pendaftaran` dan `aspirasi`, serta menu HP dengan JavaScript (`js/main.js`).

Dokumentasi terbaru selalu ada di portal OrgSpace: **Dokumentasi → Penyedia template → Panduan Modul Dinamis**.

## Template lengkap: Sinergi

`template-sinergi/` menunjukkan seberapa bebas tampilan template OrgSpace: hero animasi, kartu statistik dari database
(`data-ws-stat`), profil organisasi (`data-ws-org`), modul program/prestasi/agenda/galeri/testimoni/FAQ/struktur,
form pendaftaran 2 langkah, kotak aspirasi, tema gelap, dan animasi saat di-scroll, semuanya tanpa library.
Warna utama, warna aksen, dan sudut diatur lewat variabel `:root` sehingga bisa diganti pembuat website di editor.
