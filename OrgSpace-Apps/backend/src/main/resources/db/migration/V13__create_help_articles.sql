-- Artikel Panduan per aturan pengecekan (alur-fitur-upload.md bagian 5.11).
-- Disimpan di server agar bisa diperbarui tanpa update app. Satu artikel per kode aturan; aturan yang belum punya
-- artikel membuka halaman Panduan umum di app. Artikel dicicil mulai dari aturan yang paling sering memicu Error.
CREATE TABLE help_articles (
    code          VARCHAR(50)  PRIMARY KEY,
    title         VARCHAR(150) NOT NULL,
    why           TEXT         NOT NULL,
    wrong_example TEXT,
    right_example TEXT,
    how_to_fix    TEXT         NOT NULL,
    tips          TEXT,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO help_articles (code, title, why, wrong_example, right_example, how_to_fix, tips) VALUES
('NO_INDEX', 'File index.html tidak ada',
 'index.html adalah halaman utama yang dibuka pertama kali, baik di app maupun di hosting. Tanpa file ini website tidak tahu harus mulai dari mana.',
 'toko-kue.zip
└── toko-kue/
    └── dist/
        └── index.html',
 'toko-kue.zip
├── index.html
├── css/
└── img/',
 'Buka folder yang berisi index.html (mis. dist/), pilih semua isinya, lalu buat ZIP dari situ. Boleh juga satu folder pembungkus berisi index.html.',
 'Nama file harus huruf kecil semua: index.html, bukan Index.html.'),

('SOURCE_NOT_BUILT', 'Kode sumber, bukan hasil build',
 'Project Vite/React/Sass harus di-build dulu menjadi HTML, CSS, dan JS biasa. Server tidak menjalankan npm install atau build karena itu berarti menjalankan kode sembarangan di server.',
 'src/main.ts
package.json
node_modules/',
 'dist/index.html
dist/assets/index-a1b2c3.js
dist/assets/index-d4e5f6.css',
 'Di laptopmu jalankan npm run build, lalu ZIP isi folder dist/ (bukan folder project).',
 'Untuk Vite, tambahkan base: ''./'' di vite.config.js agar path file relatif.'),

('CASE_MISMATCH', 'Huruf besar/kecil nama file tidak cocok',
 'Windows dan macOS tidak membedakan huruf besar/kecil nama file, tetapi hosting Linux dan Android membedakannya. Di laptopmu gambar tampil, di HP pembuat website tidak.',
 '<img src="img/Hero.jpg">   (filenya img/hero.jpg)',
 '<img src="img/hero.jpg">',
 'Ganti nama file atau path di HTML agar sama persis, sebaiknya huruf kecil semua, lalu ZIP ulang.',
 'Biasakan nama file huruf kecil, tanpa spasi, memakai tanda hubung: foto-produk-1.jpg.'),

('JS_RENDERED_CONTENT', 'Konten utama dibuat oleh JavaScript',
 'Pembuat website mengedit teks dan gambar yang tertulis di HTML. Jika isi halaman baru dibuat JavaScript saat dibuka, tidak ada yang bisa ditandai dan diedit.',
 '<body>
  <div id="root"></div>
  <script src="app.js"></script>
</body>',
 '<body>
  <h1>Toko Kue Bu Ani</h1>
  <p>Kue rumahan setiap pagi.</p>
  <script src="app.js"></script>
</body>',
 'Tulis teks dan gambar langsung di HTML. JavaScript cukup dipakai untuk interaksi (menu, slider).',
 'Hasil build SPA React/Vue/Svelte belum didukung.'),

('COMPONENT_FRAMEWORK', 'Template React/Vue/Svelte belum didukung',
 'Framework komponen melakukan hidrasi saat halaman dibuka dan bisa menimpa kembali teks yang sudah diedit pembuat website. Masalah ini baru ketahuan setelah website online.',
 '<script id="__NEXT_DATA__">…</script>',
 'HTML, CSS, dan JS biasa (boleh memakai Alpine.js untuk interaksi).',
 'Buat ulang template dengan HTML/CSS/JS biasa, atau alat build yang menghasilkan HTML statis (Vite + plugin include, Eleventy).',
 NULL),

('CDN_NOT_ALLOWED', 'Library belum diizinkan',
 'Siapa pun bisa menerbitkan paket di npm, termasuk paket jahat, dan CDN akan menyajikannya. Karena itu yang dipercaya adalah library-nya, bukan sekadar CDN-nya.',
 '<script src="https://cdn.jsdelivr.net/npm/xyz-slider@1.0.0/xyz.js"></script>',
 '<script src="vendor/xyz.js"></script>   (file disertakan di ZIP)',
 'Sertakan file library di ZIP (mis. folder vendor/), atau pakai library dari daftar resmi: Tailwind, Bootstrap, Bulma, Alpine.js, jQuery, AOS, Swiper, Splide, GSAP, Font Awesome, Bootstrap Icons, Lucide, Remix Icon.',
 'Library yang disertakan di ZIP tetap dipindai seperti JavaScript milikmu.'),

('CDN_VERSION_UNCLEAR', 'Versi library tidak jelas',
 'Link tanpa versi atau @latest bisa berubah diam-diam saat library merilis versi baru, sehingga template yang kemarin rapi tiba-tiba rusak.',
 'https://unpkg.com/alpinejs@latest/dist/cdn.min.js',
 'https://cdn.jsdelivr.net/npm/alpinejs@3.14.1/dist/cdn.min.js',
 'Tulis versi lengkap tiga angka (mis. @3.14.1).',
 NULL),

('MISSING_ASSET', 'File CSS/JS yang dirujuk tidak ada',
 'Tanpa file CSS tampilan berantakan, tanpa file JS menu dan slider tidak jalan.',
 '<link rel="stylesheet" href="css/styles.css">   (filenya css/style.css)',
 '<link rel="stylesheet" href="css/style.css">',
 'Periksa ejaan path dan pastikan filenya ikut masuk ZIP.',
 'Buka index.html langsung dari folder ZIP-mu di browser laptop untuk mengecek sebelum upload.'),

('BROKEN_PAGE_LINK', 'Link ke halaman yang tidak ada',
 'Pengunjung yang mengklik menu akan melihat halaman kosong/error.',
 '<a href="kontak.html">Kontak</a>   (tidak ada kontak.html)',
 '<a href="kontak.html">Kontak</a>   (kontak.html ada di ZIP)',
 'Tambahkan halamannya ke ZIP, atau perbaiki/hapus link-nya.',
 NULL),

('FORM_EXTERNAL_ACTION', 'Form mengirim data ke situs lain',
 'Template dipakai banyak orang. Form yang mengirim ke server milik pembuat template bisa mengumpulkan data pengunjung website orang lain.',
 '<form action="https://contoh.com/kirim">',
 '<a href="https://wa.me/6281234567890">Pesan via WhatsApp</a>',
 'Ganti form dengan link WhatsApp (https://wa.me/...) atau mailto:.',
 NULL),

('EXT_FETCH', 'JavaScript mengirim data ke situs lain',
 'Template harus statis. Kode yang mengirim/mengambil data dari server luar bisa melacak atau mencuri data pengunjung.',
 'fetch(''https://contoh.com/api'', { method: ''POST'' })',
 'fetch(''data/menu.json'')   (file lokal di ZIP)',
 'Hapus kode itu, atau simpan datanya sebagai file di ZIP.',
 NULL),

('TRACKER', 'Ada analytics atau pelacak',
 'Pelacak di template akan melacak pengunjung website milik orang lain yang memakai template-mu.',
 '<script src="https://www.googletagmanager.com/gtag/js?id=G-XXXX"></script>',
 NULL,
 'Hapus script Google Analytics, Facebook Pixel, Hotjar, dan sejenisnya.',
 'Pemilik website bisa memasang analytics-nya sendiri setelah website online.'),

('BASE_HREF', 'Ada tag <base href>',
 'Tag ini membuat semua path relatif diambil dari situs lain, bukan dari file di ZIP.',
 '<base href="https://contoh.com/">',
 NULL,
 'Hapus baris <base href=...>.',
 NULL),

('ZIP_TOO_LARGE', 'Ukuran ZIP melebihi batas',
 'Template statis yang sehat biasanya 2–6 MB. ZIP di atas 20 MB hampir pasti berisi foto yang belum dikompres, video, atau node_modules.',
 NULL, NULL,
 'Kompres foto (mis. squoosh.app), ubah ke WebP, dan hapus video serta folder node_modules.',
 'Gambar web idealnya 100–500 KB.'),

('IMAGE_TOO_LARGE', 'Gambar terlalu besar',
 'Gambar besar membuat website lambat dibuka di HP dan boros kuota pengunjung.',
 'hero.jpg 4 MB', 'hero.webp 300 KB',
 'Kompres gambar atau ubah ke WebP, lalu ZIP ulang.',
 NULL),

('ROOT_ABSOLUTE_PATH', 'Path diawali tanda /',
 'Path seperti /assets/index.js dibaca dari akar domain, sehingga rusak jika website dipasang di subfolder (mis. GitHub Pages).',
 '<script src="/assets/index.js"></script>',
 '<script src="assets/index.js"></script>',
 'Pakai path relatif. Untuk Vite, tambahkan base: ''./'' di vite.config.js lalu build ulang.',
 NULL);
