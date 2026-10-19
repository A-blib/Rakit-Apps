# OrgSpace-Hosting

Sistem ke-3 dalam ekosistem OrgSpace: **Sistem Hosting Publik ("Tempat Web User Nebeng")**.

Proyek ini dibangun menggunakan **Next.js (App Router) + TypeScript + Tailwind CSS**, bertindak sebagai gateway penayangan website publik hasil karya pengguna yang dibuat dari Aplikasi Builder (Android).

---

## 1. Arsitektur Tiga Sistem

| Sistem | Lokasi | Domain / Port | Fungsi |
|---|---|---|---|
| **OrgSpace-Apps** | `OrgSpace-Apps/` | Backend `:8080` + App Android | Builder website (Mode Template & Custom) & API |
| **OrgSpace-Website** | `OrgSpace-Website/` | `orgspace.com` (`:3000`) | Portal korporat, dokumentasi, & monitoring |
| **OrgSpace-Hosting** | `OrgSpace-Hosting/` | `orgspace.app` (`:3001`) | Hosting publik web user, dual-renderer, custom domain |

Alasan pemisahan ke sistem mandiri:
- **Keamanan:** Mencegah kebocoran cookie admin/sesi portal korporat dari script atau konten website buatan user.
- **Kinerja:** Mendukung Server-Side Rendering (SSR) dan dynamic routing berkecepatan tinggi tanpa membebani portal korporat.
- **Custom Domain:** Memungkinkan penanganan custom domain mandiri melalui middleware Next.js.

---

## 2. Fitur Utama

1. **Auto-Adapt:** Membaca data publik rilis website dari backend Spring Boot (`/api/public/sites/render/{slug}`).
2. **Dynamic Route (`/[slug]` atau `/[namawebuser]`):** Menyajikan website secara dinamis per pengguna dan menampilkan halaman 404 berdesain rapi jika website tidak ditemukan atau belum aktif.
3. **Penyajian website:**
   - **Mode template**: file asli template (`/{slug}/`, `/{slug}/tentang.html`, `css/`, `js/`, `img/`) disajikan dengan header `Content-Security-Policy: sandbox`. JavaScript template (menu, slider, Tailwind, dll.) jalan, tapi terisolasi dari Portal Pengurus dan website lain. Modul `data-ws-*` diisi script OrgSpace yang disisipkan (`src/lib/site-runtime.ts`).
   - **Mode rakit sendiri**: `CustomRenderer` di `/rakit/{slug}`.
4. **SEO & Sinkronisasi Instan:**
   - `generateMetadata` dinamis (title, description, Open Graph tags).
   - Pengambilan data instan (`cache: 'no-store'`) sehingga website langsung terbarukan saat tombol *Publish* ditekan di aplikasi.
5. **Formulir Interaktif (`DynamicForm`):** Pengunjung dapat langsung mengisi dan mengirimkan pendaftaran atau pesan ke backend (`POST /api/public/sites/{slug}/forms/{formName}`).
6. **Custom Domain & Subdomain Support:** Middleware memeriksa header `Host` dan otomatis mengarahkan ke slug yang sesuai.

---

## 3. Cara Menjalankan

### Persiapan:
Pastikan backend Spring Boot di `OrgSpace-Apps/backend` sudah berjalan di port `8080`.

### Menjalankan Server Hosting:
```bash
cd OrgSpace-Hosting
npm install
npm run dev
```

Akses gateway di browser:
- Halaman Gateway: `http://localhost:3001/`
- Halaman Web User: `http://localhost:3001/web-dinamis-organisasi`

### Mengetes di HP Fisik Android:
Jalankan penerusan port melalui adb:
```bash
adb reverse tcp:3001 tcp:3001
```
Buka browser Google Chrome di HP Anda dan ketikkan `http://localhost:3001/web-dinamis-organisasi`.
