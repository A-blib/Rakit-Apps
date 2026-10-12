# Fase 19 (T1): Backend paket template & event

Sumber rancangan: [`docs/rancangan/alur-buat-website-via-template.md`](../rancangan/alur-buat-website-via-template.md) bagian 10–12. Penjelasan cara kerjanya ada di [`dokumentasi-project.md` bab 29](../dokumentasi-project.md#29-paket-template-untuk-pembuat-website).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `AGENTS.md` bagian 18, `docs/rancangan/alur-buat-website-via-template.md` | Rancangan baru + keputusan awal Aris (jsoup di Android, ExifInterface bawaan, fase T1–T8 = 19–26, branch `fitur/template-mode`) |
| `db/migration/V16__template_package.sql` | Kolom `templates.package_version` (selalu 1 untuk sekarang) dan `package_size` (null = belum punya paket) |
| `upload/publish/TemplateManifest` | Bentuk `manifest.json` (halaman, section, tema + nilai bawaan, isian + isi contoh) |
| `upload/publish/ManifestBuilder` | Membuat manifest dari paket ber-`data-key` dan menyisipkannya ke ZIP; membaca manifest dari ZIP |
| `upload/publish/PublishRunner` | Kirim kini menambahkan manifest dan mencatat ukuran paket |
| `upload/publish/PackageManifestBackfill` | Saat backend start: paket yang tayang sebelum manifest ada (hasil uji Fase 18) dilengkapi otomatis |
| `gallery/TemplatePackageService`, `GalleryController`, `dto/TemplateDetailResponse` | `GET /api/templates/{id}` (detail) dan `GET /api/templates/{id}/package` (ZIP + `Content-Length` + `X-Template-Version`) |
| `security/SecurityConfig` | Kedua endpoint di atas publik (tamu juga bisa memakai template) |
| `seed/DemoPackages`, `resources/seed/template-packages/*` | 3 paket contoh: **UMKM Kuliner** (1 halaman, 14 isian), **Profil Sekolah** (3 halaman, isian terhubung, 3 variabel tema), **Portofolio Minimal** |
| `seed/DemoTemplateSeeder` | 3 template demo yang tayang kini membawa paket, thumbnail, deskripsi, dan kata kunci sungguhan |
| `tools/buat-gambar-demo.py` | Membuat gambar contoh (JPG) paket demo, bebas hak cipta |
| `ManifestBuilderTest`, `TemplatePackageIntegrationTest` | Test manifest, detail, paket, template tersembunyi, event, backfill, dan paket contoh lolos pengecekan |

## Alasan keputusan
- **Manifest dibuat dari paket jadi (HTML ber-`data-key`), bukan dari HTML asli** → satu cara untuk Kirim baru dan untuk melengkapi paket lama. (Alternatif: membuat manifest di dalam `PackageBuilder` dari nomor `data-tpl-id` → paket Fase 18 yang sudah tayang tidak bisa dilengkapi tanpa Kirim ulang.)
- **Isi contoh (`sample`) diambil server** → HP tidak perlu menebak teks asli; aturan "Masih teks contoh" cukup membandingkan string. Gambar ditulis sebagai path dari folder utama paket (`img/hero.jpg`), walau halamannya di subfolder.
- **Nilai bawaan gaya (ukuran huruf, sudut) tidak ditulis di manifest** → nilai sebenarnya baru diketahui browser setelah CSS diterapkan (sama seperti langkah Coba Fase 18). Nilai bawaan tema diambil dari `:root` CSS (info teknis).
- **Versi paket = 1 dan kolomnya disiapkan sekarang** → fitur "versi template" belum boleh dibangun (rancangan 4.2), tetapi HP sudah mencatat versi sehingga nanti tidak perlu mengubah format.
- **Sumber paket contoh di `src/main/resources/seed/template-packages/`** (rancangan bagian 12 menyebut `src/test/resources/template-packages/`) → seeder dev berjalan dari kode utama, dan file di `src/test` tidak ikut saat backend dijalankan. Test tetap bisa membacanya karena resource utama ikut di classpath test.
- **Elemen di `marking.json` contoh ditunjuk dengan selector CSS** → mudah dibaca/diubah manusia; seeder mengubahnya ke nomor `data-tpl-id`, lalu memakai `PackageBuilder` + `ManifestBuilder` yang sama dengan Kirim sungguhan. (Alternatif: menulis nomor `data-tpl-id` langsung → rapuh, satu tag tambahan menggeser semua nomor.)
- **`Cache-Control: no-store` untuk paket** → URL paket sama untuk semua versi; HP memakai salinan lokalnya sendiri (tabel `template_packages`), bukan cache HTTP.

## Cara menjalankan & mengetes
1. Hapus data demo lama agar paket contoh dibuat: Swagger → `DELETE /api/dev/demo-templates` (atau `curl -X DELETE http://localhost:8080/api/dev/demo-templates`).
2. Jalankan backend dengan seeder demo: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--app.seed.demo-templates=true` (Flyway menjalankan V16 otomatis).
3. Swagger UI → `GET /api/templates` → salin `id` "Profil Sekolah".
4. `GET /api/templates/{id}` → `pages` berisi Beranda/Tentang/Kontak, `packageSizeBytes` berisi angka.
5. Terminal: `curl -sD - http://localhost:8080/api/templates/<ID>/package -o paket.zip` → header `Content-Length` dan `X-Template-Version: 1`; `unzip -p paket.zip manifest.json | python3 -m json.tool`.
6. `POST /api/templates/{id}/events` dengan `{"type":"view","installId":"hp-1","occurredAt":"2026-10-08T10:00:00Z"}` → 202.

## Hasil tes
- `./mvnw test`: **lulus, 251 test** (13 baru: 2 unit manifest, 7 integration paket, sisanya penyesuaian seeder).
- Paket contoh lolos mesin pengecekan tanpa Error (dicek di `allDemoPackagesPassTheChecker`).

## Konsep yang dipelajari
- **Manifest**: file "daftar isi" yang menjelaskan isi paket (halaman, isian, aturan) agar program lain bisa memakainya tanpa menebak.
- **`Content-Length`**: header yang memberi tahu ukuran file; tanpa ini HP tidak bisa menghitung persen progres unduhan.
- **Backfill**: mengisi data lama agar sesuai format baru, dijalankan sekali dan aman diulang (idempoten).
- **Resource classpath**: file di `src/main/resources` ikut dibungkus bersama kode dan dibaca dengan `classpath:...`, baik saat dijalankan dari folder maupun dari JAR.

## Latihan untuk Aris
1. Tambahkan isian `slogan_toko` (jenis `text`, opsional) ke `seed/template-packages/umkm-kuliner/marking.json` untuk elemen baru di `index.html`, hapus data demo, restart, lalu lihat isiannya muncul di `manifest.json`.
2. Ubah `--primary` di `profil-sekolah/site/css/style.css`, lalu cek bahwa `theme[0].default` di manifest ikut berubah.
3. Di `psql`, set `package_size` satu template tayang menjadi `NULL`, restart backend, dan perhatikan log "Paket template … dilengkapi manifest.json".

## Yang perlu Aris lakukan
- Hapus data demo lama lalu restart backend dengan seeder demo (langkah 1–2 di atas), supaya 3 template demo punya paket.
- Template milikmu yang sudah tayang dari uji Fase 18 dilengkapi manifest otomatis saat backend start.

## Rencana fase berikutnya
- **Fase 20 (T2)**: layar Unduh paket di Android, tabel Room `template_packages`, unduh dengan progres, batal, peringatan data seluler, ekstrak aman (zip slip), lewati unduhan jika paket sudah ada.
