# Fase W6: Pembuatan Template Web Dinamis dengan Modul Sistem & Kustom via ZIP dan Uji GUI

Sumber rancangan: [`alur-fitur-upload.md`](../rancangan/alur-fitur-upload.md) bagian 7, 8, & 15 serta modul sistem dinamis OrgSpace (`data-ws-auth`, `data-ws-list`, `data-ws-form`, dan `data-ws-collection`).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `/tmp/web-dinamis/index.html` | Template web dinamis HTML5 responsif dengan modul bawaan sistem (`data-ws-auth`, `data-ws-list="berita"`, `data-ws-form="pendaftaran"`) serta modul kustom (`data-ws-collection="prestasi"`) |
| `/tmp/web-dinamis/css/style.css` | Styling monokrom modern dan responsif (Geist / Plus Jakarta Sans token) |
| `/tmp/web-dinamis/js/main.js` | Logika interaktif DOM, fallback data dummy lokal, dan listener form pendaftaran |
| `/tmp/web-dinamis/img/*` | Aset visual template: `logo.png`, `hero.png`, `berita.png` |
| `/tmp/web-dinamis-organisasi-inovasi.zip` | Paket arsip ZIP template web dinamis (4.9 KB) |
| `android/.../ui/upload/TryForm.java` | Perbaikan bug pemeriksaan null-safety `field.styles != null` pada form coba template pengguna (Langkah 5) |
| `android/.../core/upload/LocalSiteStore.java` | Pengamanan penghapusan rekursif `deleteRecursively(@Nullable File file)` terhadap nilai null atau file yang tidak ada |

## Alasan keputusan
- **Penggunaan Modul Bawaan Sistem (`data-ws-*`)**:
  - `data-ws-auth`: Mengintegrasikan status sesi autentikasi pengguna secara dinamis saat web dibuka.
  - `data-ws-list="berita"`: Mengizinkan rendering dinamis daftar artikel kabar berita organisasi dari backend CMS.
  - `data-ws-form="pendaftaran"`: Formulir open recruitment anggota yang otomatis dihubungkan ke endpoint penerimaan data sistem OrgSpace.
- **Implementasi Modul Kustom (`data-ws-collection="prestasi"`)**:
  - Menguji kapabilitas custom collections platform OrgSpace menggunakan `data-ws-item`, `data-ws-field="judul"`, `data-ws-field="peraih"`, `data-ws-field="tahun"`, dan `data-ws-empty`.
  - Sistem backend parser secara otomatis mengenali dan mendaftarkan koleksi ini ke dalam `dynamicModules: ["berita", "prestasi", "pendaftaran", "auth"]`.
- **Pengujian Penuh Berbasis GUI di HP Asli**:
  - Mengikuti instruksi untuk menguji alur wizard upload provider dari awal sampai akhir via antarmuka Android fisik (`UploadZipFragment` -> `UploadCheckFragment` -> `UploadInfoFragment` -> `UploadMarkFragment` -> `UploadTryFragment` -> `UploadSendFragment`).
  - Menemukan dan memperbaiki celah runtime NPE pada `TryForm.java` ketika field tidak memiliki deklarasi `styles`.

## Cara menjalankan & mengetes
1. Pastikan backend Spring Boot aktif dengan profil `dev` dan port forwarding adb berjalan:
   ```bash
   adb reverse tcp:8080 tcp:8080
   ```
2. Salin ZIP template ke HP:
   ```bash
   adb push /tmp/web-dinamis-organisasi-inovasi.zip /sdcard/Download/
   ```
3. Buka app di HP, beralih ke **Mode Kreator** (Provider Mode) -> Tab **Upload**.
4. Ikuti wizard 6 langkah:
   - Pilih file ZIP dan periksa integritas template (0 error, status `passed`).
   - Lengkapi nama template ("Web Dinamis Organisasi Inovasi"), kategori ("BEM / DEMA / DPM"), deskripsi, kata kunci, tag konten, dan thumbnail.
   - Tandai bagian (Hero: Judul Organisasi, Deskripsi, Banner).
   - Uji coba interaktif sebagai pengguna (Step 5 Coba).
   - Setujui hak cipta dan kirim template (Step 6 Kirim).
5. Beralih ke **Mode Pembuat Website** -> Tab **Template** (Galeri):
   - Filter kategori "BEM / DEMA / DPM".
   - Tekan template "Web Dinamis Organisasi Inovasi" untuk langsung mengedit tampilan di Project Editor dengan live preview WebView.

## Hasil tes
- Backend Check Runner:
  - Status: `passed` (0 error, 0 warning kritis).
  - Terdeteksi `dynamicModules: ["berita", "prestasi", "pendaftaran", "auth"]`.
- Android Unit Tests:
  - `./gradlew testDebugUnitTest`: **BUILD SUCCESSFUL** (32 actionable tasks sukses).
- Pengujian GUI & End-to-End pada HP Realme (Android 13):
  - Berhasil unggah template via ZIP.
  - Template berhasil dipublikasikan dengan status **Tayang**.
  - Template tampil di galeri pembuat website lengkap dengan tag modul dinamis.
  - Template dapat dibuka di editor pembuat website dan dirender dengan WebView.

## Konsep yang dipelajari
- **Dynamic HTML Modules (`data-ws-*`)**: Atribut data HTML khusus yang dijadikan kontrak antara frontend template statis dengan engine hosting dinamis OrgSpace.
- **Custom Dynamic Collections**: Memungkinkan kreator membuat tipe konten kustom (seperti prestasi, alumni, portofolio) hanya dengan menambahkan atribut `data-ws-collection` dan `data-ws-field` tanpa konfigurasi backend rumit.
- **Null Safety pada Dynamic View Building**: Pada aplikasi Android Java yang menyusun antarmuka secara dinamis berdasarkan data skema JSON dari backend (seperti `field.styles`), pemeriksaan null eksplisit mutlak diperlukan untuk mencegah `NullPointerException`.

## Latihan untuk Aris
1. Coba buka template "Web Dinamis Organisasi Inovasi" di tab **Website** pembuat website, lalu ubah teks judul dan periksa apakah WebView langsung memperbarui teks tersebut secara real-time.
2. Tambahkan satu modul kustom baru di `index.html` (misalnya `data-ws-collection="agenda"` dengan field `tanggal` dan `nama_kegiatan`), lalu lihat apakah backend checker otomatis mendeteksi modul `agenda`.

## Yang perlu Aris lakukan
- Tidak ada tindakan khusus; aplikasi dan backend sudah sinkron dan berjalan di HP Anda.

## Rencana fase berikutnya
- Menyempurnakan integrasi modul interaktif dinamis saat website dibuka di hosting publik.
