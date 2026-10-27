# AGENTS.md — OrgSpace-Hosting

Sistem ke-3: **Sistem Hosting Publik ("Tempat Web User Nebeng")**.

## Aturan Pengembangan

1. **Pemisahan Sistem:**
   - Folder ini terisolasi dari `OrgSpace-Website/` (portal korporat) dan `OrgSpace-Apps/` (builder + backend).
   - Jangan memasukkan logika admin, CMS, atau auth user OrgSpace ke dalam folder ini.
2. **Koneksi Backend:**
   - Semua data website diambil dari endpoint publik backend Spring Boot:
     - `GET /api/public/sites/render/{slug}`
     - `GET /api/public/sites/resolve-domain?domain={domain}`
     - `POST /api/public/sites/{slug}/forms/{formName}`
     - `GET /api/public/sites/{slug}/assets/**` (juga untuk file HTML template)
3. **Penyajian website (keputusan Aris, 19 Oktober 2026):**
   - Mode `template`: file asli template disajikan oleh `src/app/[slug]/route.ts` dan `src/app/[slug]/[...path]/route.ts`
     (`src/lib/site-page.ts`) dengan header `Content-Security-Policy: sandbox ...` tanpa `allow-same-origin`.
     JavaScript template jalan, tapi tidak bisa membaca data `orgspace.app`, Portal Pengurus, atau website lain.
     Jangan menambah `allow-same-origin` dan jangan menyajikan halaman template tanpa header ini.
   - Modul `data-ws-*` diisi oleh script di `src/lib/site-runtime.ts` yang disisipkan ke halaman, lalu event
     `orgspace:siap` dikirim dan `window.orgspace.data` dibuka untuk script provider (kontrak publik untuk provider:
     jangan diubah tanpa memperbarui Panduan Modul Dinamis). Halaman publik selalu tampil sebagai tamu; sesi
     pengurus hanya di `/[slug]/admin`.
   - Mode `assembled` (rakit sendiri): `CustomRenderer` di `/rakit/[slug]`.
   - Preview template galeri: `/demo/[id]/` (`src/app/demo/**`, `servePreviewPage`/`servePreviewAsset`). File dari
     `GET /api/public/templates/{id}/files/**`, data contoh dari `GET /api/public/templates/{id}/preview`. Header
     sandbox sama dengan website terbit; form tidak dikirim (`data.preview`). Tanpa login, token, atau data user.
     Slug `demo` dicadangkan di backend (`RESERVED_SLUGS`), jadi tidak bentrok dengan website user.
   - Portal Pengurus = `src/components/PortalPengurus.tsx`, dipakai `/[slug]/admin` (API backend) dan
     `/demo/[id]/admin` (API palsu di memori, `src/lib/demo-portal.ts`, login demo publik `DEMO_LOGIN`). Ubah Portal di
     komponen itu agar demo ikut sama. Portal demo tidak boleh memanggil backend atau memakai `localStorage` token.
   - Test tanpa library: `npm test` (`node --test src/lib/*.test.mts`).
   - `/api/forms/...` sengaja ber-CORS `*` (dipanggil dari halaman sandbox, tanpa cookie/token).
4. **Port Default:**
   - Server development berjalan pada port **3001** (`next dev -p 3001`).

<!-- BEGIN:nextjs-agent-rules -->

## This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` (resolved from this file's directory; in monorepos the `next` package may not be visible from the repo root) before writing any code. Heed deprecation notices.

This block is written and re-added by `next dev` — verify at `node_modules/next/dist/server/lib/generate-agent-files.js`. Removing it from a diff only re-creates the uncommitted change; committing it with your work keeps the tree clean.

<!-- END:nextjs-agent-rules -->
