# OrgSpace

Repo ini berisi tiga bagian OrgSpace:

| Folder | Isi | Mulai dari |
|---|---|---|
| [`OrgSpace-Apps/`](OrgSpace-Apps/) | Backend Spring Boot (REST API + database) dan app Android | [`OrgSpace-Apps/README.md`](OrgSpace-Apps/README.md) |
| [`OrgSpace-Website/`](OrgSpace-Website/) | Portal web Next.js (`orgspace.com`): landing page, pusat dokumentasi, dashboard monitoring & kritik saran | [`OrgSpace-Website/README.md`](OrgSpace-Website/README.md) |
| [`OrgSpace-Hosting/`](OrgSpace-Hosting/) | Sistem hosting publik Next.js (`orgspace.app/[namawebuser]` / `:3001`): tempat website pengguna tayang, dual-renderer & custom domain | [`OrgSpace-Hosting/README.md`](OrgSpace-Hosting/README.md) |

Web portal dan web hosting memakai REST API dari backend di `OrgSpace-Apps/backend/`.

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
| VS Code (web portal) | `OrgSpace-Website` |
| VS Code (web hosting) | `OrgSpace-Hosting` |

Panduan setup lengkap (Windows & Linux): [`OrgSpace-Apps/docs/panduan-kolaborator.md`](OrgSpace-Apps/docs/panduan-kolaborator.md).
Agent AI di Windows: [`OrgSpace-Apps/SETUP-WINDOWS.md`](OrgSpace-Apps/SETUP-WINDOWS.md).

## Aturan singkat

- Kerja di cabang sendiri dari `develop`, pull request ke `develop`. Pesan commit Bahasa Indonesia.
- Jangan menggabungkan hosting publik user ke dalam portal web `OrgSpace-Website/` demi keamanan sesi admin dan isolasi domain.
- Jangan commit rahasia: `OrgSpace-Apps/backend/.env`, `OrgSpace-Apps/android/local.properties`, `OrgSpace-Website/.env.local`, `OrgSpace-Hosting/.env.local`, keystore.
