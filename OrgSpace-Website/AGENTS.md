# AGENTS.md — OrgSpace-Website (portal web)

Instruksi untuk agent AI (Claude Code, Codex, Cursor, dll.) yang bekerja di folder `OrgSpace-Website/`.
Pengguna Claude Code: jalankan agent dari folder ini dan salin file ini menjadi `CLAUDE.md` (tidak di-commit).

Folder ini hanya berisi web. Backend (sumber data) dan app Android ada di folder saudara `../OrgSpace-Apps/`
(satu repo `Rakit-Apps`). Kerjakan HANYA file di dalam `OrgSpace-Website/`.

## 1. Baca dulu

1. `../OrgSpace-Apps/docs/rancangan/portal-web.md` — cakupan, arsitektur, aturan, dan **kontrak API** (wajib, sampai habis).
2. `README.md` di folder ini — cara menjalankan backend + web.
3. Dokumentasi Next.js **versi yang terpasang** ada di `node_modules/next/dist/docs/`. Next.js 16 berbeda dari
   versi yang mungkin kamu ingat: `middleware.ts` sudah diganti **`proxy.ts`**, `params`/`searchParams`/`cookies()`
   bersifat **async** (`await`), dan tipe `PageProps<'/rute'>` / `LayoutProps<'/rute'>` dibuat otomatis
   (`npx next typegen`). Cek dokumen itu sebelum menulis kode yang memakai API Next.js.

## 2. Pembagian kerja (WAJIB)

Fondasi dibuat **Aris**; tim web membangun di atasnya. Tujuannya agar web dan app bisa dikerjakan paralel tanpa
saling merusak.

| Boleh langsung dikerjakan | Harus koordinasi dengan Aris dulu (berhenti & tanyakan ke pengguna) |
|---|---|
| Tampilan halaman di `src/app/**` (tata letak, teks, gaya) | Fitur baru yang butuh data/endpoint baru |
| Komponen di `src/components/**` | Mengubah `src/lib/api/**` (kontrak & klien API) |
| Token desain di `src/app/globals.css` (tetap selaras dengan app) | Mengubah `src/lib/auth/**`, `src/proxy.ts`, `next.config.ts` |
| Halaman statis baru tanpa data baru | Menambah dependency npm |
| Isi teks landing page | Menyimpan token di browser (localStorage/sessionStorage) atau memanggil backend dari browser |
| | Mengubah apa pun di `../OrgSpace-Apps/` (backend, app Android, dokumentasi) |

Jika sebuah permintaan butuh hal di kolom kanan: **jangan dikerjakan**. Jelaskan ke pengguna bahwa fondasinya
perlu dibuat Aris dulu, dan tuliskan ringkas apa yang dibutuhkan (endpoint, field, alasan).

## 3. Aturan kode

- Semua data lewat fungsi di `src/lib/api/` (`public.ts`, `admin.ts`); jangan memanggil `fetch` ke backend langsung.
- Tipe data hanya dari `src/lib/api/types.ts`. Jangan membuat tipe tandingan untuk respons API.
- Halaman dashboard memakai `adminToken()` dari `src/lib/auth/session.ts`; layout `(panel)` sudah memastikan admin.
- Aksi yang mengubah data = **Server Action** di `src/app/dashboard/actions.ts` (koordinasi dulu untuk aksi baru).
- Warna/ukuran: pakai token (`bg-surface`, `text-muted`, `bg-primary`, `rounded-card`, kelas `btn-primary`, `card`,
  dst.). Jangan menulis kode warna (#xxxxxx) di halaman.
- Teks UI dalam Bahasa Indonesia; label status/kategori dari `src/lib/format.ts`.
- Komentar kode dalam Bahasa Indonesia, jelaskan *alasan*.
- Jangan memakai logo, ikon, atau aset milik Next.js/Vercel.
- Sebelum commit: `npm run lint` dan `npm run build` harus bersih.
- Jangan commit `.env.local`, `node_modules/`, `.next/`, dan folder `instruksi/`.

## 4. Git

- Cabang kerja dari `develop`: `git checkout -b web/<topik>-<nama>`.
- Pesan commit Bahasa Indonesia: `web: rapikan hero landing page`.
- Pull request ke `develop`; Aris yang me-review perubahan yang menyentuh fondasi.

<!-- BEGIN:nextjs-agent-rules -->

## This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` (resolved from this file's directory; in monorepos the `next` package may not be visible from the repo root) before writing any code. Heed deprecation notices.

This block is written and re-added by `next dev` — verify at `node_modules/next/dist/server/lib/generate-agent-files.js`. Removing it from a diff only re-creates the uncommitted change; committing it with your work keeps the tree clean.

<!-- END:nextjs-agent-rules -->
