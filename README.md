# OrgSpace

Repo ini berisi dua bagian OrgSpace:

| Folder | Isi | Mulai dari |
|---|---|---|
| [`OrgSpace-Apps/`](OrgSpace-Apps/) | Backend Spring Boot (REST API + database) dan app Android | [`OrgSpace-Apps/README.md`](OrgSpace-Apps/README.md) |
| [`OrgSpace-Website/`](OrgSpace-Website/) | Portal web Next.js: landing page, pusat dokumentasi, dashboard monitoring & kritik saran | [`OrgSpace-Website/README.md`](OrgSpace-Website/README.md) |

Web memakai REST API yang sama dengan app (backend di `OrgSpace-Apps/backend/`). Kontrak API portal web:
[`OrgSpace-Apps/docs/rancangan/portal-web.md`](OrgSpace-Apps/docs/rancangan/portal-web.md).

## Ambil kode

```bash
# Windows: jalankan di folder pendek tanpa spasi, mis. C:\dev
git clone https://github.com/A-blib/Rakit-Apps.git OrgSpace
cd OrgSpace
git checkout develop
```

| Alat | Buka folder |
|---|---|
| Android Studio | `OrgSpace-Apps/android` |
| VS Code (backend) | `OrgSpace-Apps/backend` |
| VS Code (web) | `OrgSpace-Website` |

Panduan setup lengkap (Windows & Linux): [`OrgSpace-Apps/docs/panduan-kolaborator.md`](OrgSpace-Apps/docs/panduan-kolaborator.md).
Agent AI di Windows: [`OrgSpace-Apps/SETUP-WINDOWS.md`](OrgSpace-Apps/SETUP-WINDOWS.md).

## Aturan singkat

- Kerja di cabang sendiri dari `develop`, pull request ke `develop`. Pesan commit Bahasa Indonesia.
- Tim web hanya mengubah `OrgSpace-Website/`. Fitur web baru yang butuh data/endpoint baru dikoordinasikan dengan Aris
  dulu (lihat `OrgSpace-Website/AGENTS.md`).
- Jangan commit rahasia: `OrgSpace-Apps/backend/.env`, `OrgSpace-Apps/android/local.properties`, `OrgSpace-Website/.env.local`, keystore.
