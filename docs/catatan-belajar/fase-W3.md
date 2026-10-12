# Fase W3: Beranda OrgSpace

Sumber rancangan: [`alur-pembuatan-website.md`](../rancangan/alur-pembuatan-website.md) bagian 3 dan keputusan G1 (keputusan K23 untuk kalimat sapaan).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `ui/creator/CreatorHomeFragment`, `fragment_creator_home.xml` | Beranda baru. Tamu: "Saatnya organisasimu punya rumah online". Belum ada website: "Kerja keras {org} layak dilihat banyak orang" + Mulai besar. Sudah ada: "Hai, {nama depan}", Lanjutkan, Mulai satu baris. Juga Template untuk {org} / Template populer, dan Panduan |
| `ui/creator/CreatorHomeViewModel` | Mengambil website terakhir diubah dari server (`WebsiteRepository`), jumlah website "ada perubahan", dan permintaan terbit terbuka (Admin/Pemilik) |
| `ui/website/WebsiteSmallAdapter`, `WebsiteUi.compactStatus/info` | Kartu kecil Lanjutkan dan baris "Online · ada perubahan · Diubah 2 jam lalu oleh Budi" |
| `ui/common/StatusBannerView`, `view_status_banner.xml` | Banner kini bisa punya tombol aksi (**Periksa**, **Lihat**) |
| `ui/creator/PendingStart`, `CreateProjectSheet`, `CreatorDashboardFragment.start/openWebsites` | Tamu memilih Pakai template / Rakit sendiri → Masuk → onboarding → pilihan itu dijalankan otomatis (G1). Banner "Lihat" membuka tab Website dengan chip "Ada perubahan" |
| `ui/website/WebsitesViewModel`, `WebsitesFragment` | ViewModel tab Website dimiliki shell dashboard, agar bisa dipilihkan chip dari Beranda |
| `ui/guide/Guide`, `strings_home.xml` | 6 Panduan baru: Cara menerbitkan website*, Pakai template atau Rakit sendiri?, Ajak pengurus lain, Saat ganti kepengurusan, Pakai domain sendiri*, Mengonlinekan sendiri file ZIP* (*Segera hadir sampai fiturnya ada) |
| `src/debug/...SampleProjects*`, `DebugBindings`, `strings_debug.xml`, `ProjectRepository`/`ProjectDao`, `ProjectSmallAdapter` (dihapus) | Tombol debug "Isi/Hapus project contoh" dihapus (rancangan bagian 10: data contoh sekarang dari seeder server) |

## Alasan keputusan
- **Kartu Lanjutkan membuka halaman kelola website, bukan editor.** Ini menyimpang sementara dari rancangan 2.3. Alasannya, editor yang tersimpan di cloud baru dibangun di fase T4. Membuka editor lama (berbasis project di HP) akan membuat website tidak tersinkron.
- **Sapaan besar/kecil tidak dipilih sebelum data datang** (`hasWebsites` null) → Beranda tidak "berkedip" dari sapaan besar ke kecil saat dibuka.
- **Panduan yang fiturnya belum ada ditandai "Segera hadir"** → isi panduan tidak boleh menjanjikan tombol yang belum bisa ditekan (Terbitkan, domain, ZIP dari server).
- **Pilihan tamu disimpan di ViewModel milik Activity** → tetap ada melewati layar Masuk dan onboarding, tanpa perlu disimpan ke penyimpanan HP. (Alternatif: argumen navigasi → harus diteruskan lewat banyak layar onboarding.)
- **Rekomendasi masih semua kategori**; judulnya sudah "Template untuk {org}". Kategori organisasi di galeri dibangun di W4.

## Cara menjalankan & mengetes
1. Backend dev jalan (data dari W2), `./gradlew installDebug`.
2. Tamu: Beranda menampilkan "Saatnya organisasimu punya rumah online", Template populer, dan Panduan. **Mulai sekarang → Pakai template** → layar Masuk → masuk → app membuka tab Template.
3. `dummy5` (Editor OSIS): "Hai, Budi", Lanjutkan berisi 3 website, tanpa banner.
4. `dummy4` (Admin OSIS): banner "Budi Kurniawan minta website … diterbitkan" (**Periksa** → halaman kelola) dan "1 website punya perubahan…" (**Lihat** → tab Website chip Ada perubahan).
5. Ruang kerja tanpa website (mis. ruang pribadi): sapaan besar "Kerja keras {nama} …" dan kartu Mulai besar.

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 112 test** (1 tes contoh project debug ikut dihapus). `lint`: **0 masalah**. Instrumented test dikompilasi ulang (`ProjectDaoTest` tanpa bagian project contoh).
- Uji di HP (12 Oktober 2026):
  - Skenario 1 (tamu) ✅, kecuali "Template populer" tidak muncul untuk tamu → diperbaiki (`CreatorHomeViewModel` kini selalu memuat saat pertama kali), **belum dicek ulang di HP**.
  - Skenario 2 (tamu → Pakai template → Masuk → galeri) ✅
  - Skenario 8 (Editor minta terbit → banner di HP Admin, Periksa & Lihat) ✅
  - Editor: "Hai, Budi", Lanjutkan, tanpa banner ✅
- Ditemukan & diperbaiki: Beranda sempat menampilkan sapaan tamu untuk user yang sudah masuk (`CurrentUserViewModel` milik Beranda belum memuat user) → `reload()` di `onResume`.
- Uji dihentikan karena HP sedang dipakai aplikasi lain.

## Konsep yang dipelajari
- **Cakupan ViewModel**: `new ViewModelProvider(this)` (per layar), `requireParentFragment()` (dibagi tab dalam dashboard), `requireActivity()` (bertahan melewati navigasi antar layar).
- **Keadaan "belum tahu"**: memakai `Boolean` null selain true/false agar UI bisa menunggu, bukan menebak.
- **Komponen yang bisa diperluas**: menambah tombol opsional ke `StatusBannerView` tanpa mengubah pemakaian lama (bawaannya tersembunyi).

## Latihan untuk Aris
1. Ubah `RECENT_LIMIT` di `CreatorHomeViewModel` menjadi 3, lalu lihat jumlah kartu kecil di Beranda.
2. Tulis isi Panduan "Cara menerbitkan website" di `strings_home.xml` (sebagai `guide_publish_body`), lalu sambungkan di `Guide.PUBLISH`. Kapan sebaiknya panduan ini dibuka untuk user?
3. Buat unit test untuk `CreatorHomeFragment.firstName` ("Rina Pratama" → "Rina", " Budi " → "Budi").

## Yang perlu Aris lakukan
- Tidak ada perubahan Gradle (tidak perlu Sync Now).
- Beri tahu jika HP sudah bebas, supaya uji "Template populer untuk tamu" bisa saya lanjutkan.

## Rencana fase berikutnya
- **W4 Galeri Template**: kategori organisasi baru (`GET /api/meta/categories`), chip sesuai jenjang ruang kerja, label isi template ("Punya: Struktur · Berita · Kontak"), "dipakai N kali", seeder demo berkategori organisasi, rekomendasi "Template untuk {org}" per kategori.
