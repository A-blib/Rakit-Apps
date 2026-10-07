# Fase 15: Backend Upload dan mesin pengecekan file

Sumber rancangan: [`docs/rancangan/alur-fitur-upload.md`](../rancangan/alur-fitur-upload.md) bagian 3–5 (langkah 1 Pilih file dan langkah 2 Pengecekan file). Penjelasan cara kerjanya ada di [`dokumentasi-project.md` bab 25](../dokumentasi-project.md#25-backend-upload-dan-mesin-pengecekan).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `AGENTS.md` bagian 17, `docs/rancangan/alur-fitur-upload.md` | Catatan bahwa rancangan Upload mulai dieksekusi + keputusan awal (jsoup, upload per potongan, pembagian 4 fase) |
| `pom.xml` | Tambah **jsoup 1.23.2** (pembaca HTML) |
| `db/migration/V12__upload.sql` | Kolom upload di `templates` (deskripsi, kata kunci, info teknis, nama/ukuran ZIP, langkah wizard), `template_checks.stage`, `template_check_issues.rule_version/snippet`, tabel `upload_sessions` dan `check_reports`, jenis notifikasi baru |
| `db/migration/V13__create_help_articles.sql` | Tabel `help_articles` + 16 artikel Panduan pertama |
| `config/AppProperties` (`Upload`, `Limits`, ...), `application.yml` (`app.upload`) | Semua batas ukuran, CDN terpercaya, daftar library, hash library terkenal, iframe yang boleh, pola pelacak |
| `upload/check/CheckRule` | Daftar 67 aturan (kode, tingkat E/P, versi, judul, fatal atau tidak) |
| `upload/check/ZipArchive`, `ZipReader`, `TemplateFiles` | Pembaca ZIP buatan sendiri yang aman: password, symlink, zip slip, zip bomb, folder pembungkus |
| `upload/check/CheckContext`, `Ref`, `Texts`, `JsScanner`, `Findings`, `Finding` | Bahan bersama: halaman ter-parse, penyelesai path, pembuang komentar & cek kurung, penampung masalah |
| `upload/check/StructureRules`, `HtmlRules`, `ExternalRules`, `ReferenceRules`, `CssRules`, `ScriptRules`, `SizeRules` | Aturan bagian 5.6 A–G |
| `upload/check/TemplateChecker`, `CheckStage`, `TechInfo` | Orkestrasi tahap A → B, tahap untuk layar progres, info teknis otomatis |
| `upload/UploadSession`, `UploadStorage`, `UploadService`, `UploadController` | Upload per potongan 1 MB yang bisa dilanjutkan, upload perbaikan, halaman awal Upload, hapus draft |
| `upload/TemplateCheckRunner`, `UploadCompletedEvent`, `UploadAsyncConfig` | Pengecekan di thread latar belakang setelah upload selesai + notifikasi |
| `upload/CheckReport`, `UploadService.report` | "Ini keliru? Laporkan" |
| `upload/UploadMaintenance` | Tiap malam: pengingat & penghapusan draft 30 hari, bersihkan sesi upload yang terbengkalai |
| `help/HelpController`, `HelpArticle` | `GET /api/help/articles/{kode}` (publik) |
| `template/CheckResponses`, `dto/IssueResponse`, `dto/CheckResponse` | Masalah kini membawa `id`, `title`, `ruleVersion`, `reported`; pengecekan membawa `stage` |
| `template/ProviderTemplateQueries` | Upload yang belum mengisi Info template tampil dengan nama file ZIP |
| `tools/buat-zip-uji.py`, `src/test/resources/test-fixtures/` | Generator 131 ZIP uji (gagal/lolos per aturan + template bersih) |
| `CheckRuleFixturesTest`, `UploadIntegrationTest` | Test semua aturan dan alur upload dari ujung ke ujung |

## Alasan keputusan
- **jsoup untuk membaca HTML** (keputusan Aris) → HTML provider sering tidak rapi; parser sungguhan lebih akurat daripada regex dan bisa menyebut nomor baris (`setTrackPosition`). Alternatif yang tidak dipilih: regex (rapuh), HtmlUnit/Chromium (berat, menjalankan JS).
- **Upload per potongan 1 MB tanpa library** (keputusan Aris) → app mengirim `PUT ...?offset=N`; jika sinyal putus, app menanyakan `receivedSize` lalu melanjutkan. Alternatif: protokol tus (butuh library), multipart sekali kirim (tidak bisa dilanjutkan).
- **Pembaca ZIP buatan sendiri** → `java.util.zip.ZipFile` tidak memberi tahu symlink dan tidak memberi kontrol penuh atas byte yang diekstrak. Formatnya sederhana (daftar isi di ujung file + data deflate), jadi cukup ±200 baris. Alternatif: Apache Commons Compress (library baru).
- **Mesin pengecekan tanpa Spring/database** → bisa diuji dengan ZIP uji dalam 1 detik tanpa Docker.
- **Pengecekan di thread latar belakang** (`@Async` + `@TransactionalEventListener`) → request upload langsung selesai; app memantau tahap lewat `GET .../check`. Event baru diproses **setelah** transaksi tersimpan, sehingga thread pengecek pasti menemukan template & ZIP-nya.
- **ZIP uji dibuat dari skrip Python** → 131 ZIP biner sulit di-review; skripnya bisa dibaca dan dijalankan ulang. ZIP-nya tetap di-commit sesuai bagian 5.10. Aturan ukuran diuji dengan batas yang diperkecil di test, agar repo tidak berisi ZIP 20 MB.
- **`.json` diperbolehkan** → daftar bagian 5.6 B tidak menyebutnya, padahal contoh `lolos.zip` di bagian 5.10 memakai `fetch('data/menu.json')`. Saya anggap file data statis. `package.json`, `*.config.js`, `.map`, file titik (`.gitignore`), README/LICENSE diabaikan sebagai file build/konfigurasi.
- **Versi CDN "jelas" = tiga angka lengkap** (`@3.14.1`) → `@3` atau `@3.x` juga bisa berubah diam-diam seperti `@latest`. Tailwind Play CDN tidak dicek versinya karena rancangan hanya memberinya Peringatan.
- **Redirect JS hanya ditolak jika otomatis** → `location.href = 'https://wa.me/...'` di dalam handler klik adalah tombol "Pesan via WhatsApp" yang sah. Yang ditolak: di tingkat paling luar, di dalam `DOMContentLoaded`/`load`/`setTimeout`, atau IIFE.
- **Link tersembunyi**: pembungkus `display:none` tidak dihitung → menu HP/dropdown memang sering disembunyikan dulu; yang ditolak adalah link yang disembunyikan sendiri atau lewat ukuran 0/posisi -9999px.
- **Kuota draft = draft + upload yang sedang dicek** → upload yang sedang dicek hampir pasti menjadi draft; tanpa ini provider bisa melewati batas 5 dengan mengupload banyak sekaligus.
- **Error pengecekan karena bug server** dicatat sebagai `CHECK_INTERNAL_ERROR` dengan pesan "bukan karena file-mu" → provider tidak disalahkan, dan stack trace hanya di log server.

## Cara menjalankan & mengetes
1. `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (Flyway menjalankan V12–V13 otomatis).
2. Swagger UI → masuk sebagai provider (mis. `dummy8@templateapp.test` / `password123`) → **Authorize**.
3. `POST /api/providers/me/uploads/sessions` dengan `{"fileName":"toko-kue.zip","totalSize":<ukuran>}`.
4. Kirim isi ZIP lewat terminal (Swagger UI tidak nyaman untuk body biner):
   ```bash
   curl -X PUT "http://localhost:8080/api/providers/me/uploads/sessions/<ID>?offset=0" \
        -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/octet-stream" \
        --data-binary @src/test/resources/test-fixtures/_DASAR/bersih.zip
   ```
5. `POST .../sessions/<ID>/complete` → `GET /api/providers/me/uploads/<templateId>/check` beberapa kali: `stage` berpindah sampai `done`, `status` menjadi `draft`.
6. Ulangi dengan ZIP gagal (mis. `test-fixtures/BASE_HREF/gagal.zip`) → `check_failed`, daftar error lengkap, kartu di Beranda "Perlu tindakan".
7. `GET /api/help/articles/CASE_MISMATCH` tanpa login.

## Hasil tes
- `./mvnw test`: **lulus, 226 test** (140 baru: 133 ZIP uji + 7 integration test upload).
- Uji manual: alur langkah 3–7 di atas lewat Swagger + curl (lihat bagian berikutnya di laporan fase 16 untuk uji dari HP).

## Konsep yang dipelajari
- **Upload per potongan (chunked upload)**: file dikirim sepotong-sepotong; server mencatat sudah sampai byte ke berapa, sehingga upload bisa dilanjutkan.
- **Zip slip & zip bomb**: nama file `../../x` bisa menulis ke luar folder; ZIP kecil bisa mengembang jadi raksasa. Penangkalnya: tolak path aneh, hitung byte yang benar-benar keluar.
- **Event setelah commit**: `@TransactionalEventListener` menjalankan kode hanya jika transaksi berhasil tersimpan.
- **`@Async`**: method berjalan di thread lain; pemanggil tidak menunggu.
- **Parser vs regex**: parser memahami struktur (tag, atribut, baris); regex hanya mencocokkan pola teks.
- **Test berbasis data (fixture)**: satu test yang sama dijalankan untuk banyak file contoh (`@TestFactory`).

## Latihan untuk Aris
1. Tambahkan library `Animate.css` ke `allowed-libraries` di `application.yml`, lalu buat ZIP yang memuatnya dari jsDelivr dan pastikan lolos.
2. Buka `tools/buat-zip-uji.py`, buat kasus `lolos-2.zip` baru untuk `EXT_FETCH` (mis. `fetch('./data/promo.json')`), jalankan skrip dan `./mvnw test -Dtest=CheckRuleFixturesTest`.
3. Di `psql`, lihat isi `check_reports` setelah melaporkan satu Error lewat Swagger, lalu ubah statusnya menjadi `dibaca`.

## Yang perlu Aris lakukan
- Tidak ada perintah sistem baru. Folder `backend/uploads/` dibuat otomatis (sudah di `.gitignore`).
- Opsional: `UPLOAD_STORAGE_DIR` di `backend/.env` jika ZIP ingin disimpan di folder lain.

## Rencana fase berikutnya
- **Fase 16**: Android halaman awal Upload (Kondisi A/B), wizard layar penuh, langkah 1 (pemilih file, cek kilat di HP, upload per potongan dengan progres), langkah 2 (daftar tahap, "Belum memenuhi standar", laporkan, artikel Panduan), langkah 3 (Info template + thumbnail), backend Info template & thumbnail.
