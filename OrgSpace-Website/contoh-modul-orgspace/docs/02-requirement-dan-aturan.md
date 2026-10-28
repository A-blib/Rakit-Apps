# Requirement Project & Aturan Pengecekan

Semua aturan di halaman ini diperiksa otomatis saat Upload langkah 2. **Error** menghentikan upload; **peringatan** boleh dilanjutkan tapi sebaiknya diperbaiki. Setiap temuan menyebut kodenya (mis. `NO_INDEX`), file, baris, dan cara memperbaikinya.

## 1. Bentuk project

- **HTML + CSS + JavaScript biasa** (vanilla). Teks dan gambar utama ditulis langsung di HTML, bukan dibuat JavaScript.
- Hasil build alat seperti Vite, Astro, Eleventy, atau Hugo boleh, asalkan yang diunggah adalah **folder hasil build** (mis. `dist/`) yang berisi HTML jadi.
- **Belum didukung**: hasil build React/Vue/Svelte/Angular/Next.js/Nuxt yang isi halamannya dibuat JavaScript, dan template yang butuh server (PHP).

```text
template-anda.zip
├── index.html        ← WAJIB di akar ZIP (bukan template-anda/index.html)
├── tentang.html      ← halaman lain (opsional)
├── css/style.css
├── js/main.js        ← opsional
├── img/...           ← gambar
└── fonts/...         ← font lokal (opsional)
```

Nama folder `css/`, `js/`, `img/` hanya saran; yang wajib adalah `index.html` di akar dan semua path **relatif** (`css/style.css`, bukan `/css/style.css`).

## 2. Batas ukuran

| Batas | Nilai | Tingkat |
|---|---|---|
| Ukuran ZIP | 20 MB | Error |
| Total setelah diekstrak | 50 MB | Error |
| Jumlah file | 500 | Error |
| Ukuran satu file | 10 MB | Error |
| Halaman HTML | 10 | Error |
| Satu gambar | > 1 MB | Peringatan |
| Berat satu halaman | > 5 MB | Peringatan |
| Satu file CSS | > 1 MB | Peringatan |
| Gambar base64 di HTML | > 20 KB | Peringatan |

## 3. Jenis file

- **Boleh**: `html`, `htm`, `css`, `js`, `mjs`, `png`, `jpg`, `jpeg`, `webp`, `gif`, `svg`, `ico`, `woff`, `woff2`, `ttf`, `otf`, `txt`, `md`, `json` (data lokal, mis. `fetch("data/menu.json")`).
- **Diabaikan** (boleh ikut): `package.json`, lockfile, `*.config.js`, `tsconfig.json`, README, LICENSE.
- **Ditolak**: PHP; `scss`/`sass`/`less` yang dipakai tanpa di-build; kode sumber `ts`/`tsx`/`jsx`/`vue`/`svelte` sebagai pengganti hasil build.

## 4. Library dari CDN

- Host yang diizinkan: `cdn.jsdelivr.net`, `cdnjs.cloudflare.com`, `unpkg.com`, `cdn.tailwindcss.com`, `code.jquery.com`. Google Fonts boleh (peringatan karena butuh internet).
- Library yang diizinkan: **Tailwind**, **Bootstrap**, **Bulma**, **Alpine.js** (+ collapse, intersect, focus), **jQuery**, **AOS**, **Swiper**, **Splide**, **GSAP**, **Font Awesome**, **Bootstrap Icons**, **Lucide**, **Remix Icon**.
- Tulis **versi lengkap** di URL: `https://cdn.jsdelivr.net/npm/swiper@11.1.4/swiper-bundle.min.js`. Tanpa versi atau `@latest` ditolak.
- Muat library dengan `<script src>` / `<link>` di HTML, bukan `import` dari CDN di dalam file JS.
- Saat template dikirim, file library **disalin ke dalam paket template**, jadi website tetap jalan walau CDN bermasalah.
- File library yang disertakan langsung di ZIP juga boleh. Versi terkenal (mis. jQuery 3.7.1, Swiper 11.1.4) dikenali otomatis.

## 5. Keamanan

- Tanpa analytics/pelacak (Google Analytics/Tag Manager, Meta Pixel, Hotjar, Clarity) dan tanpa SDK yang mengambil data dari server (mis. Firebase).
- JavaScript tidak boleh mengambil atau mengirim data ke situs lain (`fetch("https://...")`, XHR, WebSocket, `sendBeacon`). Data dinamis memakai modul `data-ws-*` (dokumentasi OrgSpace: `/docs/modul-sistem-dan-kustom`).
- Form tidak boleh `action` ke situs lain, dan halaman tidak boleh berpindah otomatis ke situs lain.
- `iframe` hanya dari YouTube (`youtube.com`, `youtube-nocookie.com`) dan Google Maps.
- Dilarang kode diacak (obfuscated), Service Worker, pola penambang kripto, `<base href>`, dan atribut khusus app (`data-edit`, `data-key`, `data-label`, `data-tpl-id`).
- **Saat website terbit**, atribut `onclick`/`onload`/`onerror` dsb. dan link `javascript:` **dihapus otomatis**. Pasang event dengan `addEventListener` di file JS.

## 6. Daftar lengkap pemeriksaan

### Struktur ZIP

| Kode | Tingkat | Arti |
|---|---|---|
| `ZIP_INVALID` | Error | ZIP rusak atau tidak bisa dibuka |
| `ZIP_ENCRYPTED` | Error | ZIP dikunci password |
| `NO_INDEX` | Error | `index.html` tidak ada di akar ZIP |
| `ZIP_TOO_LARGE`, `EXTRACTED_TOO_LARGE`, `TOO_MANY_FILES`, `FILE_TOO_LARGE`, `TOO_MANY_PAGES` | Error | Melewati batas di bagian 2 |
| `UNSAFE_PATH` | Error | Nama file berbahaya di dalam ZIP (mis. `../`) |
| `SOURCE_NOT_BUILT`, `UNBUILT_RESOURCE` | Error | Kode sumber atau SCSS/LESS yang belum di-build |
| `SOURCE_FILES_INCLUDED` | Peringatan | File sumber ikut terunggah (tidak dipakai halaman) |
| `CASE_MISMATCH` | Error | Huruf besar/kecil nama file berbeda dengan yang dipanggil |
| `FILE_NAME_STYLE` | Peringatan | Nama file memakai spasi atau karakter khusus |
| `FILE_TYPE_NOT_ALLOWED` | Error | Jenis file tidak diizinkan |
| `SERVER_SCRIPT` | Error | Template butuh server (PHP) |

### HTML

| Kode | Tingkat | Arti |
|---|---|---|
| `HTML_UNREADABLE` | Error | File HTML kosong atau tidak bisa dibaca |
| `JS_RENDERED_CONTENT` | Error | Konten utama dibuat oleh JavaScript |
| `JS_PARTIAL_CONTENT` | Peringatan | Sebagian konten dibuat oleh JavaScript (tidak bisa ditandai) |
| `COMPONENT_FRAMEWORK` | Error | Template React/Vue/Svelte belum didukung |
| `TOO_FEW_EDITABLE` | Error | Kurang dari 3 teks/gambar yang bisa ditandai |
| `FORM_EXTERNAL_ACTION` | Error | Form mengirim data ke situs lain |
| `AUTO_REDIRECT` | Error | Halaman berpindah otomatis ke situs lain |
| `RESERVED_ATTRIBUTE` | Error | Memakai atribut khusus app |
| `BASE_HREF` | Error | Ada tag `<base href>` |
| `PLUGIN_TAG` | Error | Memakai teknologi plugin lama (`<object>`, `<embed>`, `<applet>`) |
| `DYNAMIC_MODULE_INVALID` | Error | Penanda `data-ws-*` tidak valid (lihat Panduan Modul Dinamis (dokumentasi OrgSpace: `/docs/modul-sistem-dan-kustom`)) |
| `NO_VIEWPORT`, `NO_TITLE`, `IMG_NO_ALT`, `CHARSET`, `DOCTYPE_LANG`, `DUPLICATE_ID` | Peringatan | Tanpa meta viewport, `<title>`, `alt` gambar, UTF-8, `<!DOCTYPE html>`/`lang`, atau ada `id` ganda |

### Link dan aset

| Kode | Tingkat | Arti |
|---|---|---|
| `MISSING_ASSET` | Error | File CSS/JS yang dipanggil tidak ada |
| `BROKEN_PAGE_LINK` | Error | Link ke halaman yang tidak ada |
| `MISSING_IMAGE`, `BROKEN_ANCHOR`, `ORPHAN_PAGE`, `HOTLINK_IMAGE`, `ROOT_ABSOLUTE_PATH` | Peringatan | Gambar hilang, `#anchor` tanpa `id`, halaman tidak bisa dibuka dari menu, gambar diambil dari situs lain, path diawali `/` |

### CSS

| Kode | Tingkat | Arti |
|---|---|---|
| `CSS_SCRIPT_TRICK` | Error | CSS menjalankan script |
| `CSS_MISSING_URL`, `CSS_SYNTAX`, `NOT_RESPONSIVE`, `CSS_TOO_LARGE` | Peringatan | Gambar/font di CSS tidak ada, kesalahan tulis, tidak menyesuaikan layar HP, file terlalu besar |

### Library luar

| Kode | Tingkat | Arti |
|---|---|---|
| `CDN_NOT_ALLOWED`, `CDN_VERSION_UNCLEAR`, `UNTRUSTED_SOURCE` | Error | Library di luar daftar, versi tidak jelas, atau script dari situs yang bukan CDN terpercaya |
| `JS_CDN_IMPORT` | Error | `import` library dari CDN di dalam file JS |
| `TRACKER`, `REMOTE_DATA_SDK`, `IFRAME_NOT_ALLOWED` | Error | Pelacak, SDK data server, atau iframe yang tidak diizinkan |
| `TAILWIND_PLAY_CDN`, `GOOGLE_FONTS` | Peringatan | Tailwind Play CDN lebih berat; Google Fonts butuh internet |

### JavaScript

| Kode | Tingkat | Arti |
|---|---|---|
| `EXT_FETCH` | Error | Mengirim/mengambil data ke situs lain |
| `HIDDEN_EXTERNAL_LINK` | Error | Link ke situs luar disembunyikan |
| `SERVICE_WORKER`, `OBFUSCATED_JS`, `CRYPTO_MINER` | Error | Service Worker, kode diacak, pola penambang kripto |
| `JS_EVAL`, `STRING_TIMER`, `DOCUMENT_WRITE`, `POPUP_ON_LOAD`, `JS_SYNTAX`, `JS_RUNTIME_ERROR`, `HORIZONTAL_OVERFLOW` | Peringatan | `eval()`, `setTimeout` dengan teks, `document.write()`, popup, kesalahan tulis, error saat dijalankan, elemen melebar melewati layar HP |

### Ukuran

| Kode | Tingkat | Arti |
|---|---|---|
| `IMAGE_TOO_LARGE`, `PAGE_TOO_HEAVY` | Peringatan | Gambar > 1 MB, halaman > 5 MB |

## 7. Saran agar template disukai

- `<meta name="viewport" content="width=device-width, initial-scale=1">` dan desain rapi di lebar 360 px.
- `<!DOCTYPE html>`, `<html lang="id">`, `<meta charset="utf-8">`, dan `<title>` di setiap halaman.
- Warna utama, aksen, dan sudut sebagai variabel `:root` agar bisa didaftarkan sebagai **tema** (lihat Menandai bagian template (dokumentasi OrgSpace: `/docs/menandai-bagian-template`)).
- Gambar WebP/JPG di bawah 500 KB dengan `alt` yang jelas; gambar contoh milik sendiri atau berlisensi bebas.
- Banyak teks dan gambar biasa di luar modul (judul, paragraf, logo, foto) dengan `id` jelas (`hero-judul`), agar mudah ditandai.

## Editor yang disarankan

VS Code (atau editor apa pun) + ekstensi Live Server untuk melihat hasil di browser. Coba juga di layar HP dengan mode responsif browser.
