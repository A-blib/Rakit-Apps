# Fase W4: Galeri Template dengan kategori organisasi

Sumber rancangan: [`alur-pembuatan-website.md`](../rancangan/alur-pembuatan-website.md) bagian 6 dan [`alur-fitur-upload.md`](../rancangan/alur-fitur-upload.md) bagian 6.1 & 15.1–15.2 (keputusan K9, K11).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `backend/.../db/migration/V20__template_org_categories.sql` | Kategori template diganti kategori organisasi (data lama dipetakan: sekolah → OSIS / MPK, organisasi → UKM, lainnya → Ekskul / Komunitas lain), kolom baru `content_tags` |
| `backend/.../template/ContentTag` | 9 pilihan "Isi template", 3 di antaranya konten Inti (Profil, Struktur kepengurusan, Kontak) |
| `backend/.../organization/MetaController`, `CategoriesResponse` | `GET /api/meta/categories` kini juga mengirim `contentTags` |
| `backend/.../template/Template`, galeri (`GalleryQueries`, DTO), `TemplatePackageService` | `category` bertipe `OrgCategory`; kartu & detail galeri membawa `contentTags` |
| `backend/.../upload/DraftService`, `DraftInfoRequest/Response`, `publish/SubmitRules` | Info template menyimpan Isi template (kode dikenal saja, urut resmi); Kirim butuh minimal satu |
| `backend/.../upload/publish/PublishRunner` | Peringatan `ORG_CORE_CONTENT` jika konten Inti belum dicentang (template tetap tayang) |
| `backend/.../organization/OrganizationSummaryResponse` | Ringkasan ruang kerja membawa `category` & `level` (untuk galeri) |
| `backend/.../seed/DemoTemplateSeeder` | Template demo diberi Isi template; database lama dilengkapi saat backend start |
| `backend/.../user/WebsitePurpose` (dihapus) | Kategori "tujuan website" lama tidak dipakai lagi |
| `android/.../data/model/CategoryLabels`, `OrgCategories`, `CategoriesDto`, `SessionStore`, `WorkspaceRepository` | Daftar kategori & Isi template dari server, disimpan di HP agar label tetap tampil saat offline |
| `android/.../data/model/Workspace`, `OrganizationSummaryDto`, `WorkspaceMapper` | Ruang kerja tahu kategori & jenjangnya |
| `android/.../ui/creator/GalleryFragment`, `GalleryViewModel`, `fragment_gallery.xml` | Chip "Semua" + kategori sesuai jenjang ruang kerja aktif (dibuat dari kode) |
| `android/.../ui/creator/GalleryAdapter`, `item_gallery_*.xml` | Label "Punya: …", "N kali dipakai", placeholder thumbnail biru |
| `android/.../ui/creator/CreatorHomeViewModel` | "Template untuk {org}": kategori organisasi dulu, dilengkapi semua kategori (pakai `Recommendations.merge` yang sudah dites) |
| `android/.../ui/template/TemplateDownloadFragment` | Layar detail menampilkan "Punya: …" |
| `android/.../ui/upload/UploadInfoFragment`, `UploadInfoViewModel`, `ContentTagSuggester` (+ tes), `fragment_upload_info.xml` | Kategori organisasi & Isi template di langkah Info; saran ✦ dari nama halaman template |
| `android/.../ui/provider/TemplateUi`, `ProviderTemplatesFragment` | Label & filter kategori di Template Anda memakai daftar server |

## Alasan keputusan
- **Kategori template = kategori organisasi** → "Template untuk OSIS Smansa" cukup mencocokkan kategori ruang kerja dengan kategori template.
- **Daftar kategori disimpan di HP, label dibaca dari `CategoryLabels`** → satu sumber (server), tetapi kartu tetap berlabel saat offline. (Alternatif: tulis ulang 11 kategori di strings.xml → harus update app setiap kategori berubah.)
- **Chip galeri dibuat dari kode** → jumlah kategori bergantung jenjang dan daftar server.
- **Saran Isi template hanya ditandai ✦, tidak otomatis dicentang** → kreator yang paling tahu isi templatenya; nama halaman hanya petunjuk.
- **ORG_CORE_CONTENT hanya Peringatan** → sesuai rancangan 15.2; template tetap tayang.
- **Data kategori lama dipetakan sekenanya** → semuanya data uji/demo.

## Cara menjalankan & mengetes
1. Jalankan ulang backend (V20). Install app.
2. Masuk `dummy4` (OSIS, jenjang SMA): Beranda → "Template untuk OSIS Smansa" berisi Profil Sekolah (OSIS / MPK) lalu dilengkapi template lain.
3. Tab Template: chip Semua · OSIS / MPK · Pramuka · Jurnalistik / Media · … (tanpa BEM/Himpunan/UKM). Kartu: "OSIS / MPK · 47 kali dipakai" dan "Punya: …".
4. Pindah ke BEM UNU (kampus): chip berganti ke kategori kampus.
5. Masuk `demo-provider@templateapp.test` → Upload: di langkah Info, pilih kategori organisasi dan Isi template (chip ✦ = saran).

## Hasil tes
- Backend `./mvnw test`: **lulus, 293 test** (UploadIntegrationTest: Isi template wajib, kode tidak dikenal ditolak, urutan rapi, Peringatan ORG_CORE_CONTENT; meta `contentTags`).
- Android `./gradlew testDebugUnitTest`: **lulus, 114 test** (+ `ContentTagSuggesterTest`). `lint`: **0 masalah**.
- Uji di HP (12 Oktober 2026, dummy4):
  - Skenario 9 ✅: chip hanya kategori sekolah; "Template untuk OSIS Smansa" ✅; filter OSIS / MPK hanya menampilkan Profil Sekolah ✅; label "Punya: …" & "kali dipakai" ✅.
  - **Belum:** pindah ke ruang kerja kampus (BEM) dan langkah Info upload di HP. Uji dihentikan karena HP dipakai aplikasi lain.

## Konsep yang dipelajari
- **Migrasi data + constraint**: CHECK lama dilepas → data dipetakan dengan `UPDATE ... CASE` → CHECK baru dipasang. Urutan ini penting agar data lama tidak menolak constraint baru.
- **`text[]` + `<@`**: `content_tags <@ ARRAY[...]` memastikan semua isi array termasuk daftar yang sah.
- **View dibuat dari kode**: `new Chip(context)` + `setTag(nilai)` untuk menyimpan kode server di chip, lalu dibaca kembali saat dipilih.
- **EnumSet** di backend: menyimpan pilihan unik dan otomatis berurutan sesuai urutan enum.

## Latihan untuk Aris
1. Tambahkan kata "pengumuman" ke saran "Berita kegiatan" di `ContentTagSuggester`, lalu tambahkan tesnya.
2. Lewat Swagger, panggil `GET /api/templates?category=osis_mpk` dan bandingkan dengan `category=pramuka`.
3. Ubah label "Kontak + media sosial" di `ContentTag` (backend) menjadi "Kontak & medsos", restart backend, buka app: label di galeri ikut berubah tanpa build ulang app.

## Yang perlu Aris lakukan
- Jalankan ulang backend (V20). Tidak ada perubahan Gradle (tidak perlu Sync Now).

## Rencana fase berikutnya
- **T1–T8** (`alur-buat-website-via-template.md` revisi): project tersimpan di cloud dengan kunci edit, editor tersinkron, isi otomatis dari Profil organisasi.
