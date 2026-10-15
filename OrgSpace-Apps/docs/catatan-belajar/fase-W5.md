# Fase W5: Integrasi Edit Tampilan Website Online

Sumber rancangan: [`alur-pembuatan-website.md`](../rancangan/alur-pembuatan-website.md) bagian 4.2 & 5.1 (Pengelolaan website online dan penyuntingan tampilan).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `android/.../data/remote/dto/WebsiteDetailDto.java` | Menambahkan field `templateVersion` pada response detail website |
| `android/.../data/model/WebsiteDetail.java` | Menyimpan `sourceTemplateId`, `templateVersion`, dan `content` (peta isian template dari server) |
| `android/.../data/mapper/WebsiteMapper.java` | Memetakan template id, versi template, dan map konten website dari DTO ke model |
| `android/.../data/local/ProjectDao.java`, `ProjectRepository.java` | Query pencarian project lokal berdasarkan nama (`findByNameSync`) dan sinkronisasi draft |
| `android/.../core/template/ProjectStore.java` | Fungsi `writeFromMap()` untuk menyimpan map konten dari server langsung ke `values.json` lokal |
| `android/.../ui/editor/EditorNav.java` | Helper `openWebsiteEditor(Fragment, WebsiteDetail)` untuk navigasi langsung ke `TemplateEditorFragment` |
| `android/.../ui/editor/TemplateEditorViewModel.java` | Logika inisialisasi editor untuk website online: membuat draft lokal di Room jika belum ada, memuat konten server ke `values.json`, dan menghubungkan template yang terpasang |
| `android/.../ui/website/WebsiteManageFragment.java` | Mengaktifkan tombol "Edit tampilan" (`binding.editButton`) untuk membuka editor template, serta `onResume()` untuk memuat ulang data saat kembali |
| `android/.../WebsiteMapperTest.java` | Unit test pemetaan detail website termasuk versi template dan map konten |
| `android/.../WebsiteEditorBehaviorTest.java` | Behavior unit test untuk `ProjectStore.writeFromMap()`, serialisasi JSON, dan integritas data template |

## Alasan keputusan
- **Memanfaatkan engine editor template yang sudah ada (`TemplateEditorFragment`)**: Alih-alih membuat editor terpisah khusus website online, website online menggunakan editor template yang sama dengan project lokal. Hal ini menjamin konsistensi pengalaman pengguna, live preview WebView, validasi form, dan auto-save.
- **Auto-create draft lokal saat membuka editor**: Website yang dibuat secara online atau dipublikasikan mungkin belum tersimpan di Room database lokal perangkat. Saat tombol "Edit tampilan" ditekan, ViewModel secara otomatis mendaftarkan `ProjectEntity` di Room dan menulis konten ke `values.json` lokal sehingga editor langsung siap digunakan tanpa error "Project tidak ditemukan".
- **Sinkronisasi konten server ke `values.json`**: Ketika website online dibuka di HP baru atau belum ada cache nilai lokal, konten field dari `WebsiteDetail.content` diekstraksi dan ditulis ke `values.json` melalui `ProjectStore.writeFromMap()`.
- **Navigasi terpusat di `EditorNav`**: Logika navigasi dan pengiriman argument (`ARG_PROJECT_ID`, `ARG_TITLE`, `ARG_TEMPLATE_ID`, `ARG_TEMPLATE_VERSION`) dibungkus rapi di `EditorNav.openWebsiteEditor()` agar konsisten dan mudah dipanggil dari fragmen mana pun.

## Cara menjalankan & mengetes
1. Pastikan backend aktif (`./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`) dan forwarding port adb aktif (`adb reverse tcp:8080 tcp:8080`).
2. Jalankan app di HP Android: `./gradlew installDebug` lalu buka app.
3. Di tab **Website**, pilih website online (misal: "Web Dinamis Organisasi").
4. Di halaman kelola website (`WebsiteManageFragment`), tekan tombol **Edit tampilan**.
5. Verifikasi GUI:
   - Layar `TemplateEditorFragment` terbuka dengan judul sesuai nama website.
   - Live preview WebView menampilkan template dan teks yang sudah diisi ("Generasi Emas Masa Dep", "Organisasi yang paling keren", dll).
   - Chip section ("Header ✓", "Hero ✓", "Section 3 ✓") dan form isian terisi sesuai konten website.
   - Tekan tombol Back (kembali), app kembali ke halaman kelola website secara mulus.

## Hasil tes
- `./gradlew testDebugUnitTest`: **BUILD SUCCESSFUL** (semua 32 actionable unit test lulus, termasuk `WebsiteEditorBehaviorTest` dan `WebsiteMapperTest`).
- Uji GUI pada HP Realme (Android 13):
  - Berhasil menavigasi dari daftar website -> kelola website -> editor template.
  - Tampilan live preview WebView merender HTML/CSS dinamis template organisasi.
  - Form editor menampilkan isian yang sudah tersimpan di server.
  - Navigasi kembali (Back) berfungsi normal tanpa crash.

## Konsep yang dipelajari
- **Single Source of Truth dengan Fallback**: Memadukan penyimpanan lokal (Room + file system JSON) dan remote server (REST API). Jika data lokal belum ada, sistem mengunduh dari server dan menyimpannya secara lokal sehingga komponen downstream (editor) tetap bekerja secara offline-first.
- **Serialization & Deserialization Map to JSON**: Memformat struktur `Map<String, Object>` dari Gson Retrofit kembali ke format JSON yang kompatibel dengan template engine menggunakan `GsonBuilder`.
- **Navigation Component Argument Passing**: Melewatkan identifier project dan template metadata melalui `Bundle` navigasi untuk menginisialisasi `ViewModel` secara deklaratif.

## Latihan untuk Aris
1. Buka `WebsiteManageFragment.java` dan telusuri bagaimana `openEditor()` memanggil `EditorNav.openWebsiteEditor(this, detail)`.
2. Buka `TemplateEditorViewModel.java` pada method `loadProject(...)` dan perhatikan bagaimana kondisi `projects.findSync(id) == null` ditangani secara aman dengan membuat `ProjectEntity` baru.
3. Coba ubah teks "Nama Organisasi" di dalam editor pada HP Anda, tunggu sebentar hingga auto-save berjalan, lalu cek apakah perubahan tersimpan di file `values.json` project.
