# OrgSpace-Website (portal web)

Landing page, pusat dokumentasi, dan dashboard internal (monitoring + kritik & saran) untuk OrgSpace.
Dibangun dengan **Next.js 16** (App Router, TypeScript, Tailwind CSS 4) dan mengambil semua data dari REST API
Spring Boot di folder saudara [`../OrgSpace-Apps/backend/`](../OrgSpace-Apps/backend/) (repo yang sama). Rancangan & kontrak API:
[`../OrgSpace-Apps/docs/rancangan/portal-web.md`](../OrgSpace-Apps/docs/rancangan/portal-web.md).
Aturan kerja tim & agent AI: [`AGENTS.md`](AGENTS.md).

## Versi yang dipakai

| Paket | Versi |
|---|---|
| Node.js | 24 LTS (minimal 20.9) |
| next | 16.4.0 |
| react / react-dom | 19.3.0 |
| tailwindcss | 4 |
| react-markdown / remark-gfm | 10.1.0 / 4.0.1 (merender artikel dokumentasi) |

## 1. Pasang software

🪟 **Windows**
1. **Git**, **JDK 21**, **PostgreSQL**: ikuti [`../OrgSpace-Apps/docs/panduan-kolaborator.md`](../OrgSpace-Apps/docs/panduan-kolaborator.md)
   bagian 2 (cukup Git, JDK, PostgreSQL, VS Code — Android Studio & Docker **tidak wajib** untuk tim web).
2. **Node.js 24 LTS**: unduh installer dari https://nodejs.org (pilih *LTS*), atau di PowerShell:
   `winget install --id OpenJS.NodeJS.LTS -e`. Tutup lalu buka lagi PowerShell, cek: `node -v` dan `npm -v`.

🐧 **Linux (Ubuntu)**
1. Git, JDK 21, PostgreSQL: lihat [`../OrgSpace-Apps/README.md`](../OrgSpace-Apps/README.md) bagian "Persiapan lingkungan Ubuntu".
2. Node.js 24 LTS lewat nvm: ikuti https://github.com/nvm-sh/nvm#installing-and-updating, lalu `nvm install 24`.

## 2. Ambil kode

Web dan app ada di **satu repo** (`Rakit-Apps`), dalam dua folder:

```
OrgSpace/                ← hasil clone repo Rakit-Apps
├── OrgSpace-Apps/       ← backend (sumber data) + app Android, milik Aris
└── OrgSpace-Website/    ← folder ini
```

```bash
# Windows: jalankan di C:\dev (path pendek tanpa spasi)
git clone https://github.com/A-blib/Rakit-Apps.git OrgSpace
cd OrgSpace
git checkout develop
git checkout -b web/<topik>-<namamu>
```

## 3. Jalankan backend (sumber data)

Ikuti [`../OrgSpace-Apps/docs/panduan-kolaborator.md`](../OrgSpace-Apps/docs/panduan-kolaborator.md) bagian 4–6: buat database PostgreSQL, salin
`backend/.env.example` menjadi `backend/.env` lalu isi, kemudian jalankan dengan profile **dev**:

```powershell
# Windows (PowerShell), dari folder OrgSpace\OrgSpace-Apps\backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

```bash
# Linux, dari folder OrgSpace/OrgSpace-Apps/backend (muat .env dulu)
set -a; source .env; set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Profile dev otomatis mengisi data contoh: 10 user dummy, kritik & saran, serta statistik 30 hari.
Swagger UI: http://localhost:8080/swagger-ui.html

### Akun admin dashboard

Akun berikut bisa dipakai untuk masuk ke dashboard di http://localhost:3000/dashboard/login.

| Email | Password | Keterangan |
|---|---|---|
| `admin@admin.com` | `admin123` | Akun administrator default sistem |
| `dummy1@templateapp.test` | `password123` | Akun dummy bawaan seeder (otomatis dibuat saat backend pertama kali jalan) |

> **Penting:** salin (copy-paste) email dan password di atas, jangan ketik manual.
> Kesalahan umum: domain email adalah **`.test`** (bukan `.com`), dan password huruf kecil semua tanpa spasi.

## 4. Jalankan web

```bash
cd OrgSpace-Website        # dari folder OrgSpace
npm install
cp .env.example .env.local      # Windows PowerShell: copy .env.example .env.local
npm run dev
```

Buka http://localhost:3000 (landing), http://localhost:3000/docs, http://localhost:3000/dashboard.

| Perintah | Fungsi |
|---|---|
| `npm run dev` | Server development (perubahan langsung terlihat) |
| `npm run lint` | Cek kode (wajib bersih sebelum commit) |
| `npm run build` | Build produksi (wajib berhasil sebelum pull request) |
| `npx next typegen` | Membuat ulang tipe `PageProps`/`LayoutProps` jika editor menandai merah |

> `npm install` mungkin menampilkan peringatan `allow-scripts` untuk `unrs-resolver`. Aman diabaikan (hanya dipakai
> ESLint). Jangan menjalankan `npm audit fix --force` (bisa menurunkan versi Next.js).

## 5. Struktur

```
src/
├── proxy.ts                    ← FONDASI: perbarui access token sebelum /dashboard dirender
├── app/
│   ├── layout.tsx, globals.css ← font Plus Jakarta Sans + TOKEN DESAIN (terang & gelap)
│   ├── (site)/                 ← halaman publik: header + footer
│   │   ├── page.tsx            ← landing page
│   │   └── docs/               ← pusat dokumentasi (menu + artikel Markdown)
│   └── dashboard/
│       ├── login/              ← masuk (email + password akun admin)
│       ├── actions.ts          ← FONDASI: Server Action (login, keluar, balas feedback, simpan artikel)
│       └── (panel)/            ← khusus admin: ringkasan, feedback, pengguna, lalu lintas, docs, sistem
├── components/                 ← komponen tampilan (BarChart, StatCard, Markdown, Pagination, ErrorBox)
└── lib/
    ├── api/                    ← FONDASI: types.ts (kontrak), http.ts, public.ts, admin.ts
    ├── auth/                   ← FONDASI: cookie sesi, refresh token, requireAdmin
    └── format.ts               ← label Bahasa Indonesia & format angka/tanggal
```

File bertanda **FONDASI** hanya diubah setelah koordinasi dengan Aris (lihat `AGENTS.md`). Folder `instruksi/`
(ringkasan proyek dari Aris) tidak di-commit.

## 6. Troubleshooting

| Gejala | Penyebab & solusi |
|---|---|
| "Server OrgSpace tidak bisa dihubungi" | Backend belum jalan atau `ORGSPACE_API_URL` salah. Cek http://localhost:8080/swagger-ui.html |
| "Email atau password salah" | Pastikan email persis `dummy1@templateapp.test` (domain `.test`, bukan `.com`) dan password `password123` (huruf kecil, tanpa spasi). Salin dari README, jangan ketik manual. Cek juga bahwa keyboard tidak auto-capitalize huruf pertama password |
| Dashboard selalu kembali ke login | Akun bukan admin (`?reason=not-admin`) — pakai `dummy1@templateapp.test`, atau minta Aris menjadikan akunmu admin |
| "Akun ini memakai login Google/GitHub" | Dashboard baru mendukung email + password |
| Grafik kosong | Data statistik contoh hanya dibuat di profile `dev` saat tabelnya kosong |
| Editor menandai `PageProps` merah | Jalankan `npx next typegen` (atau `npm run dev` sekali) |
| Port 3000 dipakai | `npm run dev -- -p 3001` |

## 7. Paket contoh modul (unduhan dokumentasi)

Artikel **Panduan Modul Dinamis** (`/docs/modul-sistem-dan-kustom`, isinya di `src/lib/docs-data.ts`) menautkan
`public/downloads/contoh-modul-orgspace.zip`. Sumber isi ZIP ada di folder `contoh-modul-orgspace/`
(template contoh, `docs/`, `AGENTS.md`/`CLAUDE.md` untuk AI agent). Setelah mengubah folder itu, buat ulang ZIP-nya:

```bash
rm -f public/downloads/contoh-modul-orgspace.zip
zip -qr public/downloads/contoh-modul-orgspace.zip contoh-modul-orgspace
```

Pastikan `contoh-modul-orgspace/contoh-template/` dan `template-sinergi/` tetap lolos pengecekan upload (ZIP isi foldernya
lalu upload dari app).

File `contoh-modul-orgspace/docs/*.md` adalah sumber isi artikel provider. Body artikel di `src/lib/docs-data.ts` harus sama
persis (salin isinya, escape `` ` `` dan `${`):

| File | Artikel |
|---|---|
| `01-panduan-modul-dinamis.md` | `modul-sistem-dan-kustom` (ditambah bagian "Unduh paket contoh" khusus web) |
| `02-aturan-wajib-template.md` | `menjadi-penyedia-template` |
| `03-requirement-dan-tools.md` | `requirement-project-dan-tools` |
| `04-membuat-fitur-kustom.md` | `membuat-fitur-kustom` |
