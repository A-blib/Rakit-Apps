# Fitur Terbitkan Website (Auto Online + Domain Sendiri)

> **Untuk siapa dokumen ini:** programmer junior atau model AI yang akan membangun fitur ini.
> **Status:** rancangan siap dieksekusi **setelah Aris meminta**. **Revisi besar 12 Oktober 2026** mengikuti `penyempurnaanSystem.md` (K6, K8, K13, K15, K21, K24).
> **Prasyarat:**
> - `revisiFondasiOrgSpace.md` fase R1–R2 (organisasi, peran, desain, bahasa)
> - `alurFiturBuatWibeSiteviatemplate.md`: data project **di cloud** (`projects`, aset, kunci edit) sudah berjalan, karena website sekarang **dibangun server** dari data itu
>
> **Perubahan terbesar dari versi sebelumnya:** HP **tidak lagi membuat dan mengunggah ZIP**. Server membangun website dari data project di cloud, lewat antrean, dan setiap versi menyimpan salinan data project sehingga **kembali ke versi lama juga mengembalikan isi editor**.

---

## 0. Baca dulu sebelum mulai

1. Baca **`AGENTS.md`** dan **`revisiFondasiOrgSpace.md`**. Semua aturan kerja tetap berlaku: kerja per fase, laporan format 13.4, catatan belajar, komentar Bahasa Indonesia, **jangan commit rahasia**, jangan menebak versi, **jangan menginstal software sistem** (tulis perintahnya untuk Aris, jelaskan, tunggu, lalu verifikasi).
2. Di teks UI selalu pakai kata **"Terbitkan"**, bukan "Publish". Nama di kode boleh tetap `publish` / `site`.
3. Dokumen terkait:
   - `alurFiturBuatWibeSiteviatemplate.md`: data project di cloud, aset, kunci edit, cara nilai diterapkan ke HTML.
   - `alurFiturPembuatanWibesite.md`: tab Website dan kerangka halaman kelola website.
   - `alurFiturUpload.md` bagian 5.6: aturan validasi file (dipakai ulang di tahap Periksa).
4. **Library:** yang perlu ditanyakan ke Aris dulu ada di bagian 9.1. Selebihnya cukup Spring Boot, JDK, dan library Android yang sudah ada.
5. **Tanyakan ke Aris** jika ada yang ambigu. Kerjakan per fase (bagian 13); setelah satu fase selesai, tes, laporkan, lalu **berhenti dan tunggu konfirmasi**.

---

## 1. Tujuan

1. Admin/Pemilik menekan **Terbitkan** → server membangun website dari data project di cloud → website **langsung online** di subdomain gratis, mis. `https://osis-smansa.{DOMAIN_SITUS}`, tanpa pengaturan apa pun.
2. Proses berjalan di **antrean** (background). App boleh ditutup; status terlihat di app: Mengantre → Membangun → Online / Gagal.
3. **Kembali ke versi lama** mengembalikan website online **dan** isi editor.
4. Organisasi yang punya domain sendiri (mis. `www.osissmansa.my.id`) bisa **menyambungkannya**, lengkap dengan HTTPS otomatis.
5. Website dimiliki **ruang kerja** (organisasi/ruang pribadi), dan hak setiap aksi mengikuti **peran** (Pemilik, Admin, Editor).
6. Kode rapi, kokoh, dan mudah dirawat: tahapan bangun yang jelas dan teruji, satu proses per website, website lama tetap online jika ada yang gagal.

Konsepnya sama dengan Vercel: `vercel.com` untuk Vercel sendiri, `vercel.app` untuk website user.

---

## 2. Konsep dan arsitektur

### 2.1 Placeholder yang wajib diisi Aris

Jangan menulis nilai ini langsung di kode. Simpan di konfigurasi (`AppProperties` + `.env`, bagian 7.1).

| Placeholder | Arti | Contoh (belum dibeli) |
|---|---|---|
| `{DOMAIN_UTAMA}` | Domain OrgSpace (landing, dashboard, API) | `orgspace.id` |
| `{DOMAIN_SITUS}` | Domain **khusus website organisasi** (terpisah dari domain utama demi keamanan) | `orgspace.site` |
| `{VPS_IP}` | IP publik VPS | `103.x.x.x` |
| `{SITES_ROOT}` | Folder penyimpanan website di VPS | `/srv/sites` |

### 2.2 Gambaran besar (1 VPS)

```
Cloudflare DNS
├── {DOMAIN_UTAMA}, api.{DOMAIN_UTAMA}, dashboard.{DOMAIN_UTAMA}   → VPS (proxied)
├── *.{DOMAIN_SITUS}                                               → VPS (proxied, wildcard)
└── custom.{DOMAIN_SITUS}                                          → VPS (DNS only, untuk domain milik organisasi)

VPS
├── Caddy (port 80/443)          ← pintu depan semua domain
│   ├── api.{DOMAIN_UTAMA}       → Spring Boot :8080
│   ├── *.{DOMAIN_SITUS}         → file di {SITES_ROOT}/hosts/{host}
│   └── domain milik organisasi  → file di {SITES_ROOT}/hosts/{host}  (HTTPS on-demand)
├── Spring Boot                  ← antrean bangun, menulis file, mengatur domain
└── PostgreSQL                   ← projects, sites, site_builds, site_releases, site_domains, ...
```

### 2.3 Cara Caddy menemukan file website (kunci fitur ini)

> **Diganti (26 Oktober 2026):** website kini disajikan OrgSpace-Hosting (`orgspace.app/{slug}`), bukan Caddy
> `file_server` ke `hosts/`. Symlink `hosts/` tidak dibuat lagi. Lihat `docs/dokumentasi-project.md` 33c dan
> `docs/deploy/Caddyfile`.

Setiap hostname yang aktif punya **satu symlink** di folder `hosts/`. Caddy cukup membaca folder sesuai hostname yang diminta browser. **Caddy tidak perlu di-reload** saat ada website baru.

```
{SITES_ROOT}/
├── data/
│   └── {siteId}/
│       ├── releases/
│       │   ├── 3/        ← isi website versi 3 (index.html, css/, img/, ...)
│       │   └── 4/        ← versi terbaru
│       └── redirect/     ← folder pengalihan setelah ganti nama (bagian 7.5)
├── tmp/                  ← folder kerja sementara saat membangun
└── hosts/
    ├── osis-smansa.orgspace.site   → symlink ke ../data/{siteId}/releases/4
    └── www.osissmansa.my.id        → symlink ke ../data/{siteId}/releases/4
```

| Aksi | Yang terjadi di disk |
|---|---|
| Terbit pertama | Bangun ke `tmp/`, pindahkan ke `data/{siteId}/releases/1/`, buat symlink `hosts/{slug}.{DOMAIN_SITUS}` |
| Terbit ulang | Bangun ke `tmp/`, pindahkan ke `releases/{n+1}/`, lalu **ganti symlink secara atomik** (buat symlink sementara lalu `rename`) sehingga pengunjung tidak pernah melihat website setengah jadi |
| Domain sendiri aktif | Buat symlink `hosts/{hostname}` ke rilis yang sama |
| Matikan sementara / ditangguhkan | Hapus semua symlink `hosts/` milik website itu; file di `data/` tetap disimpan |
| Hapus website | Hapus symlink dan folder `data/{siteId}/` |

### 2.4 Dua jenis bangun

| Jenis | Kapan | Sumber isi | Hasil |
|---|---|---|---|
| `publish` | Admin/Pemilik menekan **Terbitkan** | **Isi editor terbaru** (data project di cloud) | Rilis baru + **salinan data project baru** (snapshot) |
| `refresh` | Data di luar editor berubah: konten modul (v2), Pengaturan situs | **Snapshot rilis yang sedang online** + data modul/pengaturan terbaru | Rilis baru dengan snapshot yang **sama** seperti rilis aktif |

Aturan penting: bangun `refresh` **tidak boleh** memakai isi editor, supaya draft yang belum siap tidak ikut online.

---

## 3. Keputusan

### 3.1 Final

| Keputusan | Isi |
|---|---|
| Auto online | Terbitkan = online otomatis di `{slug}.{DOMAIN_SITUS}`. Tidak ada langkah manual per website |
| Dibangun di server (K21) | Server menyusun HTML dari data project di cloud. HP tidak mengunggah ZIP |
| Antrean | Semua bangun lewat antrean di database (bagian 7.6). Satu proses berjalan per website |
| Rollback penuh (K21) | Setiap rilis menyimpan snapshot data project; kembali ke versi lama juga mengembalikan isi editor |
| Pemilik website | **Ruang kerja** (K6). Kolom `organization_id` |
| Hak per peran (K15) | Terbitkan, matikan, kembalikan versi, domain, Pengaturan situs: **Admin & Pemilik**. Hapus website: **Pemilik**. Editor: tombol **Minta diterbitkan** |
| Domain terpisah | Website organisasi **tidak pernah** memakai `{DOMAIN_UTAMA}` |
| Hanya statis | Hasil bangun hanya file statis (HTML/CSS/JS/gambar/font). Tidak ada kode server dari user maupun kreator template |
| Server tetap memeriksa | Hasil bangun tetap melewati tahap **Periksa** sebelum ditulis (jangan percaya isi project begitu saja) |
| Domain (K8) | Subdomain gratis + domain yang dibeli organisasi sendiri. Fitur bantuan domain sekolah/kampus **tidak dibangun** |
| Export ZIP | Tetap ada, **dibuat server** lewat tahapan bangun yang sama (bagian 7.12) |
| Nama dicadangkan | Ada daftar nama subdomain yang tidak boleh dipakai (bagian 7.4) |

### 3.2 Default (boleh diubah Aris; laporkan jika berubah)

| No | Pertanyaan | Default yang dibangun |
|---|---|---|
| D1 | Maksimal website per ruang kerja | **5** untuk organisasi, **5** untuk ruang pribadi (K21, K24; sementara sampai aturan langganan T6). Dihitung dari website yang **punya alamat** (online, dimatikan, ditangguhkan). Konfigurasi `SITES_MAX_PER_ORG`, `SITES_MAX_PER_PERSONAL` |
| D2 | Domain sendiri gratis atau berbayar? | **Gratis di versi ini**. Batas **1 domain sendiri per website** |
| D3 | Boleh ganti nama subdomain? | **Boleh**. Nama lama **dikunci 30 hari** untuk ruang kerja yang sama dan pengunjung link lama **dialihkan otomatis** selama 30 hari. Maks **3 kali per 30 hari** per website (bagian 7.5) |
| D4 | Berapa rilis lama disimpan? | **3 rilis terakhir** + rilis yang sedang aktif jika berbeda. Bisa **dikembalikan** (bagian 7.8) |
| D5 | Terbit dihitung "Didownload" untuk kreator template? | **Ya.** Terbit pertama **atau** export pertama (mana yang lebih dulu) dihitung satu kali per website |
| D6 | Jeda penggabungan bangun `refresh` | **30 detik** sejak perubahan terakhir, paling lama **2 menit** sejak perubahan pertama (K21) |
| D7 | Notifikasi "Minta diterbitkan" | Banner di Beranda dan halaman kelola website Admin/Pemilik (polling saat app dibuka). **Tanpa** push notification di versi ini |
| D8 | Batas waktu satu bangun | **120 detik**; lewat dari itu dianggap gagal (`BUILD_TIMEOUT`) |

---

## 4. Cakupan

### 4.1 Bangun

- Persiapan infrastruktur (perintah untuk Aris, bagian 6)
- Backend: tabel, antrean bangun, tahapan bangun, endpoint terbit/kelola, rollback penuh, Pengaturan situs, Minta diterbitkan, export ZIP, verifikasi domain, endpoint `ask` untuk Caddy, job terjadwal (bagian 7)
- Android: layar Terbitkan pertama kali, status bangun, bagian **Ringkasan** (termasuk QR code), **Riwayat versi**, **Domain**, **Pengaturan situs** di halaman kelola website, Minta diterbitkan (bagian 8)
- Penangguhan website oleh tim OrgSpace lewat SQL (bagian 7.15)

### 4.2 Jangan bangun sekarang

- Pembayaran / paket berbayar (menunggu keputusan T6)
- API formulir untuk Data masuk + CORS (v3)
- Bangun ulang otomatis karena konten modul (v2). **Mekanisme `refresh` dan jeda penggabungan dibangun sekarang** dan dipakai oleh Pengaturan situs; modul tinggal memanggilnya nanti
- Statistik pengunjung (v2)
- Fitur bantuan domain sekolah/kampus (K8)
- Push notification
- Pindah penyimpanan ke Cloudflare R2 / storage lain
- Panel admin web untuk moderasi (sementara lewat SQL)
- Pembelian domain dari dalam app

---

## 5. Alur user (ringkas)

```
Halaman kelola website / Editor → [Terbitkan]                    (Admin/Pemilik)
   ├─ terbit pertama → pilih alamat (cek tersedia)
   ├─ masih ada isian belum lengkap → konfirmasi (tidak memblokir)
   ├─ masuk antrean → status: Mengantre → Membangun → Online ✓ / Gagal + alasan
   └─ layar Berhasil: link, Salin, Bagikan, Buka, QR code, [Pakai domain sendiri]

Editor menekan [Minta diterbitkan] (opsional tulis catatan)
   └─ Admin/Pemilik melihat banner → buka halaman kelola → periksa → [Terbitkan]

Website yang sudah online:
   ├─ badge Online + "Ada perubahan belum diterbitkan" jika isi editor lebih baru
   ├─ Riwayat versi → [Kembalikan] → website + isi editor kembali ke versi itu
   ├─ Domain → instruksi DNS → [Periksa] → aktif
   ├─ Pengaturan situs → simpan → dibangun ulang otomatis (refresh)
   └─ [Matikan sementara] / [Hidupkan lagi]
```

---

## 6. Persiapan infrastruktur (dikerjakan Aris, sekali)

Agent **tidak** menjalankan perintah ini. Tulis perintahnya untuk Aris lengkap dengan penjelasan satu kalimat per perintah, tunggu Aris menjalankannya, lalu bantu verifikasi. Periksa ulang perintah terhadap dokumentasi resmi terbaru (Caddy, Cloudflare, Ubuntu) sebelum memberikannya.

### 6.1 DNS di Cloudflare

| Tipe | Nama | Isi | Proxy |
|---|---|---|---|
| A | `*.{DOMAIN_SITUS}` | `{VPS_IP}` | **Proxied** (awan oranye) |
| A | `custom.{DOMAIN_SITUS}` | `{VPS_IP}` | **DNS only** (awan abu-abu) |

- `*.{DOMAIN_SITUS}` melayani semua subdomain gratis. HTTPS di sisi pengunjung ditangani Cloudflare.
- `custom.{DOMAIN_SITUS}` adalah target CNAME untuk domain milik organisasi. Harus **DNS only** agar Caddy bisa membuat sertifikat HTTPS untuk domain itu secara langsung.
- Mode SSL/TLS Cloudflare: **Full (strict)**. Pasang **Cloudflare Origin Certificate** (wildcard `*.{DOMAIN_SITUS}`) di VPS untuk koneksi Cloudflare → Caddy.

### 6.2 Folder

```bash
sudo mkdir -p /srv/sites/data /srv/sites/hosts /srv/sites/tmp   # tempat file website
sudo chown -R templateapp:templateapp /srv/sites                 # dimiliki user Linux yang menjalankan Spring Boot
```

Caddy cukup punya izin **baca** ke `/srv/sites/data` dan `/srv/sites/hosts`. Spring Boot punya izin **tulis**. Folder `tmp/` harus berada di **partisi yang sama** dengan `data/` agar pemindahan folder bersifat atomik.

### 6.3 Caddyfile (gambaran; agent menyesuaikan dengan versi Caddy terbaru)

```caddyfile
{
    on_demand_tls {
        ask http://127.0.0.1:8080/internal/caddy/ask   # Caddy bertanya ke backend sebelum membuat sertifikat
    }
}

*.{DOMAIN_SITUS} {
    tls /etc/caddy/certs/origin.pem /etc/caddy/certs/origin.key
    root * /srv/sites/hosts/{host}
    file_server
    header {
        X-Content-Type-Options nosniff
        Referrer-Policy strict-origin-when-cross-origin
        -Server
    }
    handle_errors {
        # coba 404.html milik host dulu (dipakai folder pengalihan, bagian 7.5), lalu halaman umum
        root * /srv/sites
        try_files /hosts/{host}/404.html /_pages/tidak-ditemukan.html
        file_server
    }
}

https:// {
    tls {
        on_demand
    }
    root * /srv/sites/hosts/{host}
    file_server
    # header dan handle_errors sama seperti di atas
}
```

- Halaman `/srv/sites/_pages/tidak-ditemukan.html` dan `dinonaktifkan.html` dibuat sederhana, memakai warna dan font OrgSpace, dengan teks ramah ("Website ini tidak ditemukan." / "Website ini sedang tidak aktif.").
- **Wajib diuji:** header `Host` yang aneh (mis. berisi `..`) tidak bisa membaca folder di luar `hosts/` (bagian 12).

### 6.4 Tes manual jalur (sebelum backend siap)

```bash
mkdir -p /srv/sites/data/tes && echo '<h1>Jalur website berhasil</h1>' > /srv/sites/data/tes/index.html
ln -s ../data/tes /srv/sites/hosts/tes.{DOMAIN_SITUS}
```

Buka `https://tes.{DOMAIN_SITUS}` → tulisan tampil dengan HTTPS. Buka `https://asal.{DOMAIN_SITUS}` → halaman "tidak ditemukan". Setelah itu hapus folder dan symlink `tes`.

### 6.5 Lingkungan development (laptop Aris)

- Backend menulis ke folder lokal, mis. `backend/data/sites/` (masukkan ke `.gitignore`).
- Caddy di laptop melayani `http://*.sites.localhost:8081` dari folder itu. Browser modern mengarahkan `*.localhost` ke laptop sendiri tanpa pengaturan DNS.
- Website hasil terbit dari HP (via `adb reverse`) dicek di **browser laptop**: `http://{slug}.sites.localhost:8081`.
- Fitur domain sendiri diuji lewat unit test + uji manual di VPS (tidak bisa diuji penuh di laptop).

---

## 7. Backend

### 7.1 Konfigurasi (`.env` + `AppProperties`)

```
SITES_ROOT=/srv/sites
SITES_DOMAIN=orgspace.site
SITES_CUSTOM_TARGET=custom.orgspace.site
SITES_PUBLIC_IP=103.x.x.x
SITES_MAX_PER_ORG=5
SITES_MAX_PER_PERSONAL=5
SITES_MAX_OUTPUT_MB=50
SITES_MAX_FILES=500
SITES_KEEP_RELEASES=3
SITES_BUILD_WORKERS=1
SITES_BUILD_TIMEOUT_SECONDS=120
SITES_REFRESH_DEBOUNCE_SECONDS=30
SITES_REFRESH_MAX_WAIT_SECONDS=120
SITES_EXPORT_LINK_TTL_MINUTES=10
SITES_VERIFY_TXT_PREFIX=_orgspace-verify
```

Tambahkan ke `.env.example` **tanpa nilai rahasia** dan tulis penjelasannya di README. `SITES_BUILD_WORKERS=1` sengaja kecil agar VPS 2 GB tidak kehabisan memori.

### 7.2 Migrasi Flyway (nomor versi menyesuaikan migrasi terakhir)

Tabel `projects` dan aset dibuat di `alurFiturBuatWibeSiteviatemplate.md`. Dokumen ini menambah:

**`sites`** (satu website yang punya alamat; 1:1 dengan project)

| Kolom | Keterangan |
|---|---|
| `id` | uuid, default `gen_random_uuid()` |
| `project_id` | FK `projects`, **unik**, on delete cascade |
| `organization_id` | FK `organizations`, on delete cascade (disalin dari project agar cek batas cepat) |
| `slug` | varchar(40), **unik**, huruf kecil |
| `status` | `online` / `offline` / `suspended` (CHECK) |
| `current_release_id` | FK `site_releases`, boleh null |
| `suspension_reason` | text, boleh null |
| `seo_title` | varchar(70), boleh null (Pengaturan situs) |
| `seo_description` | varchar(160), boleh null |
| `favicon_asset_id`, `share_image_asset_id` | FK aset project, boleh null |
| `created_at`, `updated_at`, `published_at` | timestamptz |

**`site_releases`**

| Kolom | Keterangan |
|---|---|
| `id` | uuid |
| `site_id` | FK `sites`, on delete cascade |
| `version` | int, unik per site |
| `project_snapshot` | jsonb: salinan data project (isi, gaya, tema, susunan section, referensi aset, versi template/skeleton) saat dibangun |
| `kind` | `publish` / `refresh` / `rollback` |
| `published_by` | FK `users`, boleh null (bangun otomatis) |
| `size_bytes`, `file_count` | angka |
| `created_at` | timestamptz |

Aset yang dirujuk snapshot rilis mana pun yang masih tersimpan **tidak boleh** dihapus oleh pembersihan aset (lihat `alurFiturBuatWibeSiteviatemplate.md`).

**`site_builds`** (antrean)

| Kolom | Keterangan |
|---|---|
| `id` | uuid |
| `site_id` | FK `sites`, on delete cascade; boleh null **hanya** untuk `export` dari project yang belum punya alamat |
| `project_id` | FK `projects`, on delete cascade |
| `kind` | `publish` / `refresh` / `export` |
| `status` | `queued` / `running` / `succeeded` / `failed` (CHECK) |
| `requested_by` | FK `users`, boleh null |
| `not_before` | timestamptz (untuk jeda penggabungan `refresh`) |
| `first_requested_at` | timestamptz |
| `release_id` | FK `site_releases`, boleh null |
| `export_path`, `export_token_hash`, `export_expires_at` | khusus `export` (bagian 7.12) |
| `error_code` | varchar(50), boleh null |
| `error_detail` | jsonb, boleh null (daftar masalah dengan bahasa sederhana) |
| `created_at`, `started_at`, `finished_at` | timestamptz |

Index unik parsial: **maksimal satu** baris `running` per `project_id`, dan **maksimal satu** baris `queued` per (`project_id`, `kind`).

**`site_slug_history`** (nama lama setelah ganti nama): `id`, `site_id` (FK, cascade), `old_slug`, `redirect_until` (= waktu ganti + 30 hari), `released_at` (null sampai dilepas job), `created_at`.

**`site_domains`**: `id`, `site_id` (FK, cascade), `hostname` (varchar 253, **unik**, huruf kecil, punycode), `status` (`pending`/`active`/`failed`/`removed`), `verification_token`, `last_checked_at`, `activated_at`, `failing_since`, `last_error`, `created_at`.

**`publish_requests`** (Minta diterbitkan)

| Kolom | Keterangan |
|---|---|
| `id` | uuid |
| `project_id` | FK `projects`, on delete cascade |
| `organization_id` | FK `organizations` |
| `requested_by` | FK `users` |
| `note` | varchar(200), boleh null |
| `status` | `open` / `done` / `dismissed` |
| `handled_by` | FK `users`, boleh null |
| `created_at`, `handled_at` | timestamptz |

Satu project hanya punya **satu** permintaan `open`; permintaan baru memperbarui yang lama. Terbit yang berhasil menandai permintaan `open` menjadi `done`.

### 7.3 Endpoint (semua butuh JWT kecuali disebut lain)

Pengecekan peran memakai `OrganizationAccess` (`revisiFondasiOrgSpace.md` 4.4).

| Method & path | Body | Hasil | Peran |
|---|---|---|---|
| `GET /api/sites/slug-availability?slug=` | – | `{ available, reason?, suggestions[] }` | anggota |
| `POST /api/projects/{projectId}/publish` | `{ slug? }` (wajib saat terbit pertama) | `BuildResponse` (202). Membuat `sites` jika belum ada, lalu memasukkan bangun `publish` ke antrean | admin |
| `GET /api/builds/{buildId}` | – | `BuildResponse` | anggota |
| `GET /api/sites/{id}` | – | `SiteResponse` | anggota |
| `PATCH /api/sites/{id}` | `{ slug }` | Ganti nama subdomain (bagian 7.5) | admin |
| `PATCH /api/sites/{id}/settings` | `{ seoTitle?, seoDescription?, faviconAssetId?, shareImageAssetId? }` | `SiteResponse`; memasukkan bangun `refresh` | admin |
| `POST /api/sites/{id}/unpublish` | – | Status `offline`, symlink dihapus ("Matikan sementara") | admin |
| `POST /api/sites/{id}/republish` | – | Kembali `online` memakai rilis aktif ("Hidupkan lagi") | admin |
| `GET /api/sites/{id}/releases` | – | Daftar rilis: `version`, `kind`, `createdAt`, `publishedBy { name }`, `sizeBytes`, `active` | anggota |
| `POST /api/sites/{id}/releases/{version}/restore` | – | Kembalikan website **dan** isi editor (bagian 7.8). Hasil `SiteResponse` | admin |
| `DELETE /api/projects/{projectId}` | `{ confirmName? }` | 204. Menghapus website (project + site + file). `confirmName` wajib jika pernah terbit | owner |
| `POST /api/projects/{projectId}/publish-requests` | `{ note? }` | `PublishRequestResponse` | editor |
| `GET /api/organizations/{orgId}/publish-requests?status=open` | – | daftar permintaan | admin |
| `POST /api/publish-requests/{id}/dismiss` | – | 204 | admin |
| `POST /api/projects/{projectId}/exports` | – | `BuildResponse` (202) untuk export ZIP (bagian 7.12) | anggota |
| `GET /api/exports/{buildId}/download?token=` | – | File ZIP (link sekali pakai, berlaku `SITES_EXPORT_LINK_TTL_MINUTES`) | link bertoken |
| `POST /api/sites/{id}/domains` | `{ hostname }` | `DomainResponse` berisi instruksi DNS (bagian 7.10) | admin |
| `POST /api/sites/{id}/domains/{domainId}/verify` | – | `DomainResponse` | admin |
| `DELETE /api/sites/{id}/domains/{domainId}` | – | 204 | admin |
| `GET /internal/caddy/ask?domain=` | – | **Hanya dari 127.0.0.1**, tanpa JWT. `200` jika domain `active`, selain itu `404` | – |

```json
// SiteResponse
{ "id": "uuid", "projectId": "uuid", "slug": "osis-smansa", "url": "https://osis-smansa.orgspace.site",
  "status": "online", "version": 4, "publishedAt": "...", "publishedBy": { "name": "Rina" },
  "hasUnpublishedChanges": true,
  "settings": { "seoTitle": "OSIS SMA Negeri 1 Bandung", "seoDescription": "...", "faviconUrl": null, "shareImageUrl": null },
  "domains": [ { "id": "uuid", "hostname": "www.osissmansa.my.id", "status": "active" } ],
  "latestBuild": { "id": "uuid", "kind": "publish", "status": "succeeded" } }

// BuildResponse
{ "id": "uuid", "siteId": "uuid", "kind": "publish", "status": "running",
  "queuePosition": 0, "errorCode": null, "problems": [], "releaseVersion": null }
```

- `hasUnpublishedChanges` = waktu ubah isi editor project lebih baru dari waktu snapshot rilis aktif dibuat.

**Kode error baru:** `SLUG_RENAME_LIMIT`, `SLUG_TAKEN`, `SLUG_INVALID`, `SLUG_RESERVED`, `SLUG_REQUIRED`, `SITE_LIMIT_REACHED`, `SITE_SUSPENDED`, `SITE_NOT_PUBLISHED`, `BUILD_IN_PROGRESS` (hanya untuk restore/hapus saat sedang membangun), `BUILD_FAILED`, `BUILD_TIMEOUT`, `SITE_CONTENT_INVALID`, `OUTPUT_TOO_LARGE`, `PROJECT_LOCKED`, `RELEASE_NOT_FOUND`, `EXPORT_LINK_INVALID`, `DOMAIN_INVALID`, `DOMAIN_TAKEN`, `DOMAIN_LIMIT_REACHED`, `DOMAIN_DNS_NOT_READY`, `DOMAIN_TXT_MISMATCH`. (`NOT_OWNER` lama digantikan `ROLE_NOT_ALLOWED` / `NOT_ORG_MEMBER`.)

### 7.4 Aturan nama subdomain (slug)

- 3–40 karakter, hanya `a–z`, `0–9`, dan `-`; tidak diawali/diakhiri `-`; tidak ada `--` berurutan.
- Disimpan dan dibandingkan dalam huruf kecil.
- **Nama dicadangkan** (simpan di konfigurasi, mudah ditambah): `www`, `api`, `admin`, `dashboard`, `app`, `mail`, `smtp`, `ftp`, `cdn`, `static`, `assets`, `custom`, `demo`, `tes`, `test`, `support`, `help`, `status`, `blog`, `docs`, `login`, `auth`, `account`, `orgspace`, dan kata menyesatkan (mis. `bank`, `resmi`, `official`, `pemerintah`, `kemdikbud`).
- Nama awal otomatis: dari `short_name` organisasi (mis. "OSIS Smansa" → `osis-smansa`), atau nama website jika ruang pribadi.
- Saran otomatis jika nama terpakai: `osis-smansa-2026`, `osis-smansa-bandung`, dll. (memakai periode dan kota dari Profil organisasi bila ada).
- Nama yang masih ada di `site_slug_history` dengan `released_at` kosong dianggap **terpakai** untuk ruang kerja lain (`SLUG_TAKEN`), tetapi **boleh diambil kembali** oleh website asalnya.

### 7.5 Ganti nama subdomain

Yang diganti adalah **nama subdomain gratis** (`osis-smansa` di `osis-smansa.{DOMAIN_SITUS}`), **bukan** domain yang dibeli organisasi.

Proses `PATCH /api/sites/{id}`:

1. Cek peran (admin), status bukan `suspended`, slug baru valid dan tersedia (bagian 7.4).
2. Cek batas: maks **3 kali ganti nama per 30 hari** per website (`SLUG_RENAME_LIMIT`).
3. Buat symlink `hosts/{slugBaru}.{SITES_DOMAIN}` ke rilis saat ini.
4. Ganti symlink `hosts/{slugLama}.{SITES_DOMAIN}` agar menunjuk ke **folder pengalihan** `data/{siteId}/redirect/`.
5. Simpan baris `site_slug_history` (`redirect_until` = sekarang + 30 hari), perbarui `sites.slug`, tulis `activity_logs` (`site.renamed`).
6. Jika website mengambil kembali nama lamanya: hapus baris riwayat itu dan perlakukan sebagai ganti nama biasa.

**Folder pengalihan** `data/{siteId}/redirect/` berisi `index.html` dan `404.html` yang sama: halaman "Website ini pindah ke alamat baru" + tautan, dengan pengalihan otomatis yang mempertahankan path:

```html
<meta http-equiv="refresh" content="0; url=https://{slugBaru}.{SITES_DOMAIN}/">
<script>location.replace('https://{slugBaru}.{SITES_DOMAIN}' + location.pathname + location.search + location.hash);</script>
```

- Caddy `handle_errors` mencoba `{host}/404.html` lebih dulu, sehingga link lama ke halaman mana pun (mis. `/berita/rapat-akbar`) tetap dialihkan.
- Jika website diganti nama lagi, semua folder pengalihan milik website itu diperbarui ke nama terbaru.
- Job terjadwal harian: baris dengan `redirect_until` lewat → hapus symlink nama lama, isi `released_at`, hapus folder pengalihan jika tidak dipakai lagi.

Di app, dialog ganti nama menampilkan: "Alamat lama tetap mengarah ke alamat baru selama 30 hari. Setelah itu, alamat lama bisa dipakai orang lain."

### 7.6 Antrean bangun

Antrean disimpan di tabel `site_builds` dan dijalankan oleh **worker di dalam Spring Boot** (tanpa library antrean tambahan):

1. `@Scheduled(fixedDelay = 1000)` mengambil satu baris `queued` dengan `not_before <= now()` memakai `SELECT ... FOR UPDATE SKIP LOCKED`, urut `created_at`, **melewati** website yang sedang punya baris `running`.
2. Ubah status menjadi `running`, isi `started_at`, jalankan `SiteBuildService` (bagian 7.7) dengan batas waktu `SITES_BUILD_TIMEOUT_SECONDS`.
3. Selesai → `succeeded` + `release_id`, atau `failed` + `error_code` + `error_detail`.
4. Saat Spring Boot dinyalakan ulang, baris `running` yang tertinggal diubah menjadi `failed` (`BUILD_INTERRUPTED`) agar tidak macet; file di `tmp/` dibersihkan.

**Aturan penggabungan:**

| Situasi | Yang terjadi |
|---|---|
| Terbitkan ditekan, belum ada `queued` `publish` untuk website itu | Buat baris baru, `not_before = now()` |
| Terbitkan ditekan, sudah ada `queued` `publish` | Tidak membuat baris baru; kembalikan baris yang ada |
| Terbitkan ditekan saat ada yang `running` | Buat/kembalikan baris `queued`; dijalankan setelah yang `running` selesai, memakai isi editor **terbaru saat mulai dijalankan** |
| Perubahan yang memicu `refresh` (Pengaturan situs, nanti konten modul) | Jika belum ada `queued` `refresh`: buat dengan `not_before = now() + 30 detik`. Jika sudah ada: `not_before = min(now() + 30 detik, first_requested_at + 2 menit)` |
| Ada `queued` `publish` dan `queued` `refresh` sekaligus | `refresh` dibatalkan saat `publish` berhasil (data terbaru sudah ikut terbangun) |

### 7.7 Tahapan bangun (`SiteBuildService`)

Setiap tahap adalah class/method terpisah dengan masukan dan keluaran jelas, sehingga bisa dites sendiri-sendiri.

| No | Tahap | Kerjakan | Gagal → |
|---|---|---|---|
| 1 | **Kumpulkan** | `publish`: ambil data project terbaru dari cloud (cek kunci edit tidak relevan; data yang dipakai adalah yang sudah tersinkron), simpan sebagai snapshot. `refresh`: ambil snapshot rilis aktif. Lalu ambil paket template/skeleton versi yang dirujuk snapshot, aset, Profil organisasi, Pengaturan situs, (v2) data modul | `BUILD_FAILED` (`SOURCE_MISSING`) |
| 2 | **Susun** | Salin paket template ke folder kerja di `tmp/{buildId}/`; terapkan nilai ke elemen bertanda (aturan sama dengan `alurFiturBuatWibeSiteviatemplate.md` bagian penerapan nilai); tulis `custom.css` gaya; salin aset yang dipakai (sudah dikompres); tambahkan meta judul/deskripsi/favicon/gambar bagikan; (v2) isi `data-ws-list` dan buat halaman daftar/detail; **hapus semua atribut penanda** (`data-key`, `data-edit`, `data-label`, `data-tpl-id`, `data-ws-*`) | `BUILD_FAILED` |
| 3 | **Periksa** | Pakai ulang modul validasi `alurFiturUpload.md` 5.6 untuk hasil jadi: `index.html` ada; hanya jenis file yang diizinkan; tidak ada `<base href>`, `meta refresh` ke situs lain, `<form>` ke domain luar, `<iframe>` selain YouTube/Google Maps, script dari domain di luar daftar CDN; tidak ada sisa atribut penanda; ukuran total ≤ `SITES_MAX_OUTPUT_MB` dan jumlah file ≤ `SITES_MAX_FILES` | `SITE_CONTENT_INVALID` / `OUTPUT_TOO_LARGE` + daftar masalah |
| 4 | **Tulis rilis** | Pindahkan `tmp/{buildId}/` ke `data/{siteId}/releases/{versiBaru}/` (`Files.move`, partisi sama) | `BUILD_FAILED` |
| 5 | **Ganti symlink** | Jika website `online`: ganti symlink subdomain dan semua domain sendiri yang `active` secara atomik (symlink sementara → `Files.move(..., ATOMIC_MOVE, REPLACE_EXISTING)`). Jika `offline`: tidak mengubah symlink (rilis siap untuk saat dihidupkan) | `BUILD_FAILED` |
| 6 | **Catat** | Simpan `site_releases` (snapshot, `kind`, `published_by`), perbarui `sites.current_release_id`, `published_at`, `status = online` (untuk `publish`); tandai `publish_requests` `open` menjadi `done`; tulis `activity_logs` (`site.published` / `site.refreshed`); catat event Didownload jika terbit pertama website berbasis template kreator (D5); hapus rilis lama (simpan `SITES_KEEP_RELEASES` terbaru + rilis aktif) | Rilis tetap valid; error dicatat di log |

**Jaminan:** jika gagal di tahap 1–5, folder `tmp/{buildId}/` dihapus dan symlink lama tetap menunjuk rilis lama. **Website online tidak pernah rusak** karena bangun yang gagal.

**Pesan gagal untuk user** (`error_detail`) ditulis dengan bahasa sederhana, mis. "Gambar di bagian Galeri terlalu besar. Ganti dengan gambar yang lebih kecil, lalu terbitkan lagi." Kode teknis hanya di log server.

### 7.8 Kembalikan ke versi sebelumnya (rollback penuh)

Proses `POST /api/sites/{id}/releases/{version}/restore`:

1. Cek peran (admin), status bukan `suspended`, tidak ada bangun `running` untuk website itu (`BUILD_IN_PROGRESS`).
2. Cek **kunci edit** project: jika sedang dipegang anggota lain → `PROJECT_LOCKED` ("Sedang diedit oleh {nama}. Minta dia menutup editor dulu, atau ambil alih dari editor."). Pemegang kunci boleh melakukan restore.
3. Pastikan rilis `{version}` ada dan foldernya masih ada (`RELEASE_NOT_FOUND`).
4. Ganti symlink subdomain **dan** semua domain sendiri yang `active` ke `releases/{version}/` secara atomik. Symlink nama lama yang sedang dialihkan tidak diubah.
5. **Kembalikan isi editor:** salin `project_snapshot` rilis itu ke data project di cloud sebagai isi editor terbaru (versi data project naik satu, sehingga HP lain tahu ada perubahan saat sinkron). Isi editor sebelum restore disimpan sebagai **cadangan** satu langkah (bisa dibatalkan lewat tombol "Batalkan" di snackbar selama 10 detik, dan tetap bisa dikembalikan selama rilisnya masih tersimpan jika pernah terbit).
6. Perbarui `sites.current_release_id`. Jika website `offline`, status tetap `offline`.
7. Tulis `activity_logs` (`site.restored`, berisi versi asal dan tujuan). Rilis yang lebih baru **tidak dihapus**.

Aturan tambahan:
- Terbit berikutnya membuat versi baru dengan nomor `max(version) + 1`.
- Setelah restore, `hasUnpublishedChanges = false` (isi editor sama dengan rilis aktif).

### 7.9 Pengaturan situs

| Kolom | Aturan |
|---|---|
| Judul untuk Google (`seo_title`) | Maks 70 karakter. Default: nama organisasi + " — " + nama instansi |
| Deskripsi (`seo_description`) | Maks 160 karakter. Default: kosong (tidak ditulis) |
| Favicon | Aset gambar project, dipotong persegi dan dibuat ukuran 32/180/192 px. Default: logo organisasi |
| Gambar saat dibagikan | Aset gambar, dipotong 1200×630. Default: logo organisasi di latar warna utama tema |

- Diterapkan di tahap **Susun** sebagai `<title>`, `<meta name="description">`, `<link rel="icon">`, dan meta Open Graph (`og:title`, `og:description`, `og:image`, `og:url`).
- Menyimpan pengaturan memicu bangun `refresh` (bagian 2.4), jadi **tidak** ikut menerbitkan draft editor.

### 7.10 Domain sendiri

**Menambahkan** (`POST /api/sites/{id}/domains`):

1. Normalisasi input: hapus `http(s)://`, path, port, spasi; huruf kecil; ubah domain non-ASCII ke punycode (`java.net.IDN`).
2. Tolak (`DOMAIN_INVALID`): alamat IP, `localhost`, domain tanpa titik, domain di bawah `{DOMAIN_UTAMA}` atau `{SITES_DOMAIN}`.
3. Tolak jika hostname sudah dipakai website lain (`DOMAIN_TAKEN`) atau website sudah punya 1 domain (`DOMAIN_LIMIT_REACHED`, D2).
4. Simpan dengan status `pending` dan buat `verification_token` acak.
5. Kembalikan instruksi:

| Jenis domain | Record yang ditambahkan di pengelola domain |
|---|---|
| Subdomain (disarankan), mis. `www.osissmansa.my.id` | `CNAME  www  →  {SITES_CUSTOM_TARGET}` |
| Domain utama, mis. `osissmansa.my.id` | `A  @  →  {SITES_PUBLIC_IP}` |
| **Bukti kepemilikan (wajib)** | `TXT  {SITES_VERIFY_TXT_PREFIX}.{hostname}  →  {verification_token}` |

Tambahkan saran: "Pakai `www.` lalu arahkan domain utama ke `www` dari pengelola domainmu."

**Memverifikasi** (`POST .../verify`):

1. Cari record TXT `{SITES_VERIFY_TXT_PREFIX}.{hostname}` lewat DNS JNDI bawaan JDK (`com.sun.jndi.dns`) di balik interface `DnsResolver`; harus sama dengan token (`DOMAIN_TXT_MISMATCH`).
2. Resolve `hostname` dan pastikan salah satu IP = `{SITES_PUBLIC_IP}` (`DOMAIN_DNS_NOT_READY`).
3. Lolos → `status = active`, buat symlink `hosts/{hostname}` ke rilis saat ini, tulis `activity_logs` (`domain.activated`).
4. Sertifikat HTTPS dibuat **otomatis oleh Caddy** saat domain pertama kali dibuka, setelah Caddy bertanya ke `/internal/caddy/ask`.

Pesan saat DNS belum siap: "Pengaturan DNS biasanya butuh beberapa menit sampai beberapa jam. Coba periksa lagi nanti, ya."

**Pemeriksaan ulang terjadwal** (sekali sehari):
- Domain `active` yang DNS-nya tidak lagi mengarah ke `{SITES_PUBLIC_IP}` → isi `failing_since`. Jika gagal **7 hari berturut-turut** → `status = failed`, hapus symlink, tulis `activity_logs` (tampil di Riwayat aktivitas dan bagian Domain).
- Domain `pending` lebih dari 7 hari → `removed`.

### 7.11 Endpoint `ask` untuk Caddy

- Hanya menerima request dari `127.0.0.1` (selain itu 403) dan dikecualikan dari JWT di `SecurityConfig`.
- `200` hanya jika `hostname` ada di `site_domains` dengan status `active` dan website-nya `online`. Selain itu `404`.
- Tujuannya: mencegah orang lain mengarahkan domain sembarangan ke VPS lalu membuat Caddy meminta ribuan sertifikat.

### 7.12 Export ZIP (dibuat server)

1. `POST /api/projects/{projectId}/exports` memasukkan bangun jenis khusus `export` ke antrean (memakai isi editor terbaru, tahap 1–3 sama dengan `publish`).
2. Hasil tahap 3 dikompres menjadi ZIP di `tmp/exports/{buildId}.zip`; **tidak** menulis rilis dan **tidak** mengganti symlink.
3. `BuildResponse` berisi `downloadUrl` bertoken sekali pakai (berlaku `SITES_EXPORT_LINK_TTL_MINUTES`). File dihapus setelah diunduh atau saat kedaluwarsa (job terjadwal).
4. Event Didownload dicatat jika ini export/terbit pertama (D5).


### 7.13 Batas jumlah website

- Saat terbit pertama: hitung `sites` milik ruang kerja dengan status `online`/`offline`/`suspended`. Jika ≥ batas (D1) → `SITE_LIMIT_REACHED`.
- Pesan di app: "Ruang kerja ini sudah punya 5 website. Hapus atau matikan website yang tidak dipakai dulu, ya." (Matikan sementara **tidak** mengurangi hitungan; hanya Hapus.)

### 7.14 Job terjadwal

| Job | Jadwal | Kerjakan |
|---|---|---|
| Worker antrean | tiap 1 detik | Bagian 7.6 |
| Pengalihan nama lama | harian | Bagian 7.5 |
| Periksa domain | harian | Bagian 7.10 |
| Bersihkan `tmp/` | tiap jam | Hapus folder kerja dan file export lebih dari 1 jam |
| Pantau disk | tiap jam | Jika ruang kosong `{SITES_ROOT}` < 15%, tulis peringatan di log (README menjelaskan cara memantaunya) |
| Penangguhan | tiap 5 menit | Bagian 7.15 |

### 7.15 Penangguhan (moderasi tanpa panel admin)

```sql
UPDATE sites SET status = 'suspended', suspension_reason = 'Laporan konten menyesatkan' WHERE slug = 'contoh';
```

- Job tiap 5 menit (atau langsung bila diubah lewat service) menghapus semua symlink website `suspended` → pengunjung melihat "Website ini sedang tidak aktif".
- Di app: badge **Ditangguhkan** + alasan; Terbitkan dan Hidupkan lagi ditolak (`SITE_SUSPENDED`). Pesan: "Website ini ditangguhkan tim OrgSpace. Alasan: {alasan}. Hubungi kami jika menurutmu ini keliru."
- Tampilkan alamat email laporan penyalahgunaan di landing `{DOMAIN_UTAMA}`.

---

## 8. Android

### 8.1 Data di HP

- Cache Room daftar website (`alurFiturPembuatanWibesite.md` bagian 10) ditambah kolom: `site_id`, `site_slug`, `site_url`, `site_status`, `has_unpublished_changes`, `latest_build_status`, `published_at`.
- Sumber kebenaran tetap **server**; cache diperbarui saat tab Website / halaman kelola dibuka dan online.

### 8.2 Titik masuk

| Tempat | Aksi |
|---|---|
| Halaman kelola website (kepala) | **Terbitkan** / **Terbitkan pertama kali** (Admin/Pemilik) atau **Minta diterbitkan** (Editor) |
| Editor → app bar | Tombol **Terbitkan** (Admin/Pemilik) atau **Minta diterbitkan** (Editor) |
| Kartu website (tab Website) | Badge status; menu ⋮ → Export ZIP |
| Beranda | Banner permintaan terbit (Admin/Pemilik) |

### 8.3 Layar Terbitkan pertama kali

```
┌──────────────────────────────────┐
│ ✕  Terbitkan website             │
├──────────────────────────────────┤
│ Alamat website                   │
│ [ osis-smansa  ].orgspace.site   │
│ ✓ Bisa dipakai                   │
│                                  │
│ Saran: osis-smansa-2026          │
│                                  │
│ ⓘ Gratis. Domain sendiri bisa    │
│   ditambah setelah terbit.       │
│ ⚠ Masih ada 2 isian belum        │
│   lengkap. Lihat ›               │
├──────────────────────────────────┤
│ [ Terbitkan ]                    │
└──────────────────────────────────┘
```

| Aturan | Isi |
|---|---|
| Nama awal | Bagian 7.4 |
| Cek ketersediaan | `GET /api/sites/slug-availability`, debounce 400 ms; tampilkan ✓ / alasan / saran |
| Isian belum lengkap | Peringatan, **tidak memblokir** (D1 di `alurFiturBuatWibeSiteviatemplate.md`) |
| Perubahan belum tersinkron | Jika HP masih punya perubahan yang belum terkirim ke cloud, sinkronkan dulu (tampilkan "Menyimpan perubahan..."), baru panggil `publish` |
| Offline | `StateView` error: "Butuh internet untuk menerbitkan website." + **Coba lagi** |

Terbit ulang tidak memakai layar ini: tombol **Terbitkan** di halaman kelola langsung membuka dialog konfirmasi singkat ("Terbitkan perubahan terbaru ke osis-smansa.orgspace.site?") lalu masuk antrean.

### 8.4 Status bangun dan hasil

Setelah masuk antrean, tampilkan **bottom sheet status** (bisa ditutup; status tetap terlihat di kepala halaman kelola dan kartu website):

```
┌──────────────────────────────────┐
│ ● Mengantre...                   │  → ● Lagi dibangun...  → ✓ Website online
│ Kamu boleh menutup layar ini.    │
└──────────────────────────────────┘
```

- Polling `GET /api/builds/{id}` tiap 2 detik selama sheet atau halaman kelola terbuka; berhenti saat `succeeded`/`failed`.
- **Berhasil:**

```
┌──────────────────────────────────┐
│          ✓ Website online        │
│ https://osis-smansa.orgspace.site│
│ [Salin] [Bagikan] [Buka]         │
│ ┌──────────┐                     │
│ │ QR code  │ [Unduh QR]          │
│ └──────────┘                     │
│ ┌──────────────────────────────┐ │
│ │ Punya domain sendiri?        │ │
│ │ [ Pakai domain sendiri ]     │ │
│ └──────────────────────────────┘ │
│ [ Kembali ]                      │
└──────────────────────────────────┘
```

- **Gagal:** judul "Website belum berhasil diterbitkan", daftar masalah dari `problems` (bahasa sederhana), tombol **Buka editor** dan **Coba lagi**. Website lama tetap online (tulis kalimat ini di layar agar user tenang).
- **Buka** → Custom Tabs. **Bagikan** → menu bagikan Android (teks: "Kunjungi website {org}: {url}").

### 8.5 Bagian Ringkasan (halaman kelola)

| Isi | Keterangan |
|---|---|
| Status | Badge + "Terakhir diterbitkan {waktu} oleh {nama}" |
| Alamat | Subdomain + domain sendiri yang aktif; Salin, Bagikan, Buka |
| QR code | Dibuat di HP dari URL (alamat utama: domain sendiri jika aktif, selain itu subdomain); **Unduh QR** menyimpan PNG ke galeri (untuk poster/mading) |
| Perubahan | "Ada perubahan belum diterbitkan" + tombol Terbitkan (Admin/Pemilik) |
| Matikan sementara / Hidupkan lagi | Admin/Pemilik, dengan dialog konfirmasi |

### 8.6 Riwayat versi

```
┌──────────────────────────────────┐
│ ←  Riwayat versi                 │
├──────────────────────────────────┤
│ Versi 4 · Sedang online          │
│ 12 Okt 2026, 14.20 · oleh Rina   │
│ ──────────────────────────────── │
│ Versi 3                          │
│ 10 Okt 2026, 09.05 · oleh Budi   │
│                  [ Kembalikan ]  │
│ ──────────────────────────────── │
│ Versi 2 · Pengaturan situs       │
│ 8 Okt 2026, 19.40 · otomatis     │
│                  [ Kembalikan ]  │
├──────────────────────────────────┤
│ ⓘ 3 versi terakhir disimpan.     │
└──────────────────────────────────┘
```

- **Kembalikan** → dialog: "Website dan isi editor akan kembali seperti versi 3. Perubahan yang belum diterbitkan akan disimpan sebagai cadangan." → **[Kembalikan ke versi 3]** **[Batal]**.
- Berhasil → snackbar "Versi 3 sekarang online" + **Batalkan** (10 detik).
- `PROJECT_LOCKED` → pesan dengan nama pemegang kunci.
- Editor (peran) melihat daftar tanpa tombol Kembalikan.

### 8.7 Domain sendiri

```
┌──────────────────────────────────┐
│ ←  Domain                        │
├──────────────────────────────────┤
│ Alamat gratis                    │
│ osis-smansa.orgspace.site [Ganti]│
│                                  │
│ Domain sendiri                   │
│ [ www.osissmansa.my.id     ]     │
│ [ Lanjut ]                       │
│                                  │
│ Langkah 1 · Arahkan domain       │
│ CNAME  www → custom.orgspace.site [Salin]
│                                  │
│ Langkah 2 · Bukti pemilik        │
│ TXT _orgspace-verify.www → a8f3… [Salin]
│                                  │
│ Status: Menunggu DNS             │
│ [ Periksa sekarang ]             │
│ Lihat panduan per registrar ›    │
└──────────────────────────────────┘
```

- Status: **Menunggu DNS** → **Aktif** (`color_success`) atau **Gagal** + alasan.
- Setelah aktif: `https://www.osissmansa.my.id` dengan Salin/Buka, dan tombol **Lepas domain**.
- **Ganti** alamat gratis → dialog ganti nama (bagian 7.5).
- Panduan per registrar: konten statis di app untuk beberapa registrar populer (termasuk penyedia `.my.id`).

### 8.8 Pengaturan situs

Form: Judul untuk Google (penghitung 70), Deskripsi (penghitung 160), Favicon (pilih gambar; default logo), Gambar saat dibagikan (pilih gambar; pratinjau kartu WhatsApp sederhana). Tombol **Simpan** → snackbar "Disimpan. Website diperbarui dalam beberapa saat." Hanya Admin/Pemilik.

### 8.9 Minta diterbitkan (Editor)

- Tombol **Minta diterbitkan** → dialog dengan kolom catatan opsional ("Contoh: berita rapat sudah aku tambahkan") → **Kirim**.
- Berhasil → snackbar "Permintaan terkirim ke Admin." Tombol berubah menjadi **Permintaan terkirim** (nonaktif) sampai diterbitkan atau ditolak.
- Admin/Pemilik melihat banner di Beranda dan halaman kelola: "{nama} minta website ini diterbitkan: '{catatan}'" + **Terbitkan** / **Abaikan**.

---

## 9. Keamanan dan library

### 9.1 Library yang perlu disetujui Aris dulu

| Library | Di | Untuk | Catatan |
|---|---|---|---|
| **jsoup** | Backend | Menerapkan nilai ke HTML di tahap Susun dan memeriksa hasil di tahap Periksa | Tanyakan dulu (sebelumnya direncanakan di Android; sekarang dipindah ke server) |
| **ZXing core** (`com.google.zxing:core`) | Android | Membuat QR code tanpa internet | Tanyakan dulu |

### 9.2 Risiko dan pengaman

| Risiko | Pengaman |
|---|---|
| Draft ikut online tanpa sengaja | Bangun `refresh` hanya memakai snapshot rilis aktif (bagian 2.4) |
| Website rusak karena bangun gagal | Bangun di `tmp/`, symlink diganti paling akhir dan atomik |
| VPS kehabisan memori | Satu worker, batas waktu bangun, batas ukuran hasil |
| Website organisasi membaca cookie/login OrgSpace | Website hanya di `{DOMAIN_SITUS}`; tidak ada cookie di domain itu; daftarkan `{DOMAIN_SITUS}` ke **Public Suffix List** saat sudah publik |
| Header `Host` dipakai membaca folder lain | Caddy hanya melayani `*.{DOMAIN_SITUS}` dan domain `active`; uji dengan `Host` berisi `..` |
| Mengklaim domain orang lain | Wajib record TXT berisi token |
| Penyalahgunaan sertifikat on-demand | Endpoint `ask` hanya menyetujui domain `active` |
| Isi berbahaya dari user/kreator template | Nilai user tidak pernah disisipkan sebagai HTML/CSS mentah; tahap Periksa; penangguhan lewat SQL |
| Anggota tanpa hak menerbitkan | Pengecekan peran di backend pada setiap endpoint |
| Link export bocor | Token sekali pakai, berlaku 10 menit |
| Disk penuh | Batas ukuran, batas website per ruang kerja, hapus rilis lama, pantau disk |
| Rahasia bocor | JWT secret, Origin Certificate key, `.env` **tidak pernah** di-commit |

---

## 10. Test

- **Unit test:** validasi slug + nama dicadangkan; nama awal dari `short_name`; normalisasi hostname (IDN); setiap tahap `SiteBuildService` terpisah (Kumpulkan memilih sumber benar untuk `publish` vs `refresh`; Susun menghapus semua atribut penanda; Periksa menolak `<base href>`, form ke luar, script CDN tak dikenal); ganti symlink atomik (folder sementara JUnit); aturan penggabungan antrean (tabel 7.6, termasuk batas 2 menit); pembersihan rilis (rilis aktif tidak terhapus); restore (snapshot kembali ke project, cadangan dibuat, `PROJECT_LOCKED`); ganti nama subdomain (folder pengalihan, kunci 30 hari, batas 3 kali); hak per peran untuk setiap endpoint.
- **Integration test (Testcontainers):** terbit pertama → terbit ulang → ubah Pengaturan situs (refresh tidak membawa draft) → restore → matikan → hidupkan → hapus; dua permintaan Terbitkan bersamaan hanya menghasilkan satu bangun berjalan; worker dinyalakan ulang saat `running` → `BUILD_INTERRUPTED`; batas 5 website per ruang kerja; Minta diterbitkan → Terbitkan → permintaan `done`; export ZIP + link sekali pakai; endpoint `ask` (200/404, tolak selain 127.0.0.1); verifikasi domain dengan `DnsResolver` **di-mock**.
- **Data uji** di `backend/src/test/resources/publish-fixtures/`: paket template valid, paket berisi `<base href>`, paket berisi form ke luar, project dengan gambar terlalu besar (dibuat saat test, bukan di-commit).

---

## 11. Bahasa

Semua teks mengikuti `revisiFondasiOrgSpace.md` bagian 8. Ringkasan teks di dokumen ini:

| Situasi | Teks |
|---|---|
| Tombol utama | Terbitkan · Terbitkan pertama kali · Minta diterbitkan |
| Status | Mengantre... · Lagi dibangun... · Website online · Belum berhasil diterbitkan |
| Gagal | "Website belum berhasil diterbitkan. Tenang, website yang lama tetap online." |
| Batas | "Ruang kerja ini sudah punya 5 website. Hapus website yang tidak dipakai dulu, ya." |
| Matikan | "Matikan sementara? Pengunjung tidak bisa membuka website ini sampai kamu menghidupkannya lagi." |
| Hapus | "Hapus website ini? Alamat dan semua versinya akan hilang dan tidak bisa dikembalikan. Ketik nama website untuk melanjutkan." |
| Dikunci | "Sedang diedit oleh {nama}." |

---

## 12. Skenario uji

| No | Skenario | Hasil yang diharapkan |
|---|---|---|
| 1 | Editor membuka halaman kelola website | Tombol Terbitkan tidak ada; tombol Minta diterbitkan ada; API `publish` menolak `ROLE_NOT_ALLOWED` |
| 2 | Pilih alamat yang sudah dipakai | "Alamat ini sudah dipakai" + saran |
| 3 | Pilih nama dicadangkan (`admin`) | Ditolak dengan alasan yang jelas |
| 4 | Admin terbit pertama | Status Mengantre → Lagi dibangun → Website online; `https://{slug}.{DOMAIN_SITUS}` terbuka; QR code bisa diunduh dan dipindai |
| 5 | Tutup app saat Lagi dibangun, buka lagi | Status terbaru tampil di halaman kelola dan kartu website |
| 6 | Edit lalu terbitkan ulang | Pengunjung melihat versi baru; tidak pernah tampil halaman rusak |
| 7 | Ubah isi editor tanpa terbit, lalu ubah Pengaturan situs | Website diperbarui dengan judul baru **tanpa** membawa perubahan editor; status tetap "Ada perubahan belum diterbitkan" |
| 8 | Ubah Pengaturan situs 3 kali dalam 20 detik | Hanya satu bangun `refresh` yang berjalan |
| 9 | Paksa bangun gagal (mis. gambar sangat besar) | Pesan gagal dengan bahasa sederhana; website lama tetap online |
| 10 | Matikan sementara lalu hidupkan lagi | Alamat menampilkan "sedang tidak aktif", lalu kembali normal |
| 11 | Terbitkan website ke-6 di satu ruang kerja | Ditolak dengan pesan batas |
| 12 | Ganti nama subdomain | Alamat baru aktif; alamat lama (termasuk `/berita/...`) dialihkan |
| 12a | Ruang kerja lain mencoba memakai nama lama dalam 30 hari | Ditolak `SLUG_TAKEN` |
| 12b | Setelah 30 hari (ubah `redirect_until` lewat SQL) | Pengalihan berhenti; nama lama bisa dipakai ruang kerja lain |
| 12c | Ganti nama 4 kali dalam 30 hari | Ditolak `SLUG_RENAME_LIMIT` |
| 13 | Riwayat versi → Kembalikan versi 3 | Website online kembali ke versi 3; editor menampilkan isi versi 3; snackbar Batalkan berfungsi |
| 14 | Kembalikan saat anggota lain sedang mengedit | Pesan "Sedang diedit oleh {nama}" |
| 15 | Setelah kembali ke versi lama, terbitkan | Versi baru dengan nomor berikutnya |
| 16 | Editor menekan Minta diterbitkan dengan catatan | Admin melihat banner berisi catatan; setelah Admin menerbitkan, banner hilang |
| 17 | Export ZIP | ZIP terunduh; dibuka di browser laptop tampil benar tanpa atribut penanda |
| 18 | Tambah domain sendiri, DNS belum diatur | Status Menunggu DNS + pesan sabar |
| 19 | Atur CNAME + TXT `_orgspace-verify` dengan benar, Periksa | Status Aktif; domain terbuka dengan HTTPS |
| 20 | Domain yang sama diklaim ruang kerja lain | `DOMAIN_TAKEN` |
| 21 | Ubah status ke `suspended` lewat SQL | Website "sedang tidak aktif"; app menampilkan Ditangguhkan + alasan dan menolak Terbitkan |
| 22 | Request dengan header `Host: ../../etc` | Tidak ada file di luar `hosts/` yang terbaca |
| 23 | Terbitkan saat offline | Pesan butuh internet + Coba lagi |
| 24 | Terbit pertama website dari template kreator | Event Didownload tercatat sekali |
| 25 | Restart backend saat bangun berjalan | Bangun ditandai gagal (terputus); bisa diterbitkan ulang; website lama tetap online |

---

## 13. Fase pengerjaan

| Fase | Kerjakan | Anggap selesai jika |
|---|---|---|
| **P1** Infrastruktur | Tulis perintah & penjelasan untuk Aris (bagian 6), bantu verifikasi tes manual | `tes.{DOMAIN_SITUS}` tampil dengan HTTPS; subdomain lain "tidak ditemukan" |
| **P2** Antrean & tahapan bangun | Minta persetujuan jsoup; migrasi `sites`, `site_releases`, `site_builds`; worker antrean; `SiteBuildService` 6 tahap; endpoint `publish` + `builds`; test | Skenario 4–6, 9, 25 lulus lewat Swagger + test |
| **P3** Kelola website (backend) | Slug & ganti nama, matikan/hidupkan, rilis & restore, Pengaturan situs + `refresh`, batas website, Minta diterbitkan, export ZIP, hapus, hak per peran | Skenario 1–3, 7–8, 10–17, 24 lulus lewat Swagger + test |
| **P4** Android terbit | Minta persetujuan ZXing; layar Terbitkan pertama kali, sheet status, hasil + QR, Ringkasan, Riwayat versi, Pengaturan situs, Minta diterbitkan, banner Admin | Skenario 1–17, 23 berhasil dari HP asli |
| **P5** Domain sendiri (backend) | `site_domains`, verifikasi TXT + IP, endpoint `ask`, pemeriksaan terjadwal, `DnsResolver` mock | Unit & integration test lulus |
| **P6** Domain sendiri (Android) | Bagian Domain + panduan registrar | Skenario 18–20 berhasil di VPS dengan domain uji milik Aris |
| **P7** Moderasi & polesan | Penangguhan, halaman netral, job terjadwal, README (SQL, pantau disk, backup `/srv/sites`) | Skenario 21–22 berhasil; README diperbarui |

---

## 14. Yang perlu dibahas berikutnya (jangan dibangun)

- **Fitur berbayar** (T6): sedang dikoordinasikan Aris dengan tim. Arah usulan: harga per periode kepengurusan, invoice untuk LPJ, pengingat perpanjangan, turun ke paket gratis bila tidak diperpanjang
- API publik formulir untuk Data masuk + CORS (v3)
- Bangun `refresh` dari konten modul (v2; mekanismenya sudah ada)
- Statistik pengunjung (v2)
- Fitur bantuan domain sekolah/kampus (K8)
- Push notification untuk Minta diterbitkan
- Pindah file website ke Cloudflare R2 / storage + CDN saat disk VPS mulai penuh
- Tombol "Laporkan website" publik dan panel moderasi
