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
     - `GET /api/public/sites/{slug}/assets/**`
3. **Dual Renderer:**
   - Pertahankan dukungan untuk `TemplateRenderer` (mode `template`) dan `CustomRenderer` (mode `assembled`).
4. **Port Default:**
   - Server development berjalan pada port **3001** (`next dev -p 3001`).

<!-- BEGIN:nextjs-agent-rules -->

## This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` (resolved from this file's directory; in monorepos the `next` package may not be visible from the repo root) before writing any code. Heed deprecation notices.

This block is written and re-added by `next dev` — verify at `node_modules/next/dist/server/lib/generate-agent-files.js`. Removing it from a diff only re-creates the uncommitted change; committing it with your work keeps the tree clean.

<!-- END:nextjs-agent-rules -->
