# AGENTS.md — Portal Web OrgSpace (`web/`)

Instruksi untuk agent AI (Claude Code, Codex, Cursor, dll.) yang bekerja di folder `web/`.
Pengguna Claude Code: salin file ini menjadi `web/CLAUDE.md` (tidak di-commit) atau buka agent dari folder `web/`.

## 1. Baca dulu

1. `../docs/rancangan/portal-web.md` — cakupan, arsitektur, aturan, dan **kontrak API** (wajib, sampai habis).
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
| | Mengubah apa pun di `../backend/` atau `../android/` |

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
- Jangan commit `.env.local`, `node_modules/`, `.next/`.

## 4. Git

- Cabang kerja dari `fitur/orgspace`: `git checkout -b web/<topik>-<nama>`.
- Pesan commit Bahasa Indonesia: `web: rapikan hero landing page`.
- Pull request ke `fitur/orgspace`; Aris yang me-review perubahan yang menyentuh fondasi.
