# Fase 12: Dashboard Provider di Android

Sumber rancangan: [`docs/rancangan/alur-provider.md`](../rancangan/alur-provider.md). Penjelasan cara kerjanya ada di [`dokumentasi-project.md` bab 22](../dokumentasi-project.md#22-dashboard-provider-di-android).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `data/model/ProviderStatus`, `User`, `UserDto`, `UserMapper` | Status provider hanya `ACTIVE`/`SUSPENDED`; alasan penolakan dihapus |
| `data/remote/dto/ProviderDashboardDto`, `TemplateListDto`, `TemplateDetailDto`, `ProviderProfileDto`, `ProviderProfileUpdateDto` | Bentuk JSON dari `/providers/me/**` |
| `data/remote/api/ProviderApi`, `data/repository/ProviderRepository`, `core/di/NetworkModule` | Pemanggilan endpoint provider → `Resource` |
| `ui/common/AreaChartView` | Grafik area custom (garis + gradasi, grid, label mono, ketuk untuk tooltip) |
| `ui/provider/ProviderDashboardFragment` + `menu_provider_tabs.xml` | Shell: app bar (badge MODE PROVIDER + avatar) + bottom navigation 4 tab, badge Beranda |
| `ProviderHomeFragment`, `ProviderDashboardViewModel` | Beranda: Perlu tindakan, checklist provider baru, ringkasan 7/30 hari, tren, populer, panduan, tombol Upload |
| `ProviderUploadFragment` | Tab Upload "Segera hadir" + Buka panduan |
| `ProviderTemplatesFragment`, `ProviderTemplatesViewModel`, `TemplateListState`, `TemplateAdapter`, `LoadingFooterAdapter` | Template Anda: cari, chip status + jumlah, kategori, urutan, paginasi 20, skeleton, kosong/error |
| `TemplateDetailFragment`, `TemplateDetailViewModel` | Detail + error/peringatan pengecekan terakhir |
| `ProviderProfileFragment`, `ProviderProfileViewModel`, `ProviderProfileEditFragment`, `ProviderProfileEditViewModel` | Profil + Edit profil (layout form provider dipakai ulang) |
| `Guide`, `GuideStore`, `GuideFragment` | 5 panduan (2 sudah berisi: cara membuat ZIP, cara membaca angka), penanda "baru" |
| `ProviderNav`, `TemplateUi`, `RelativeTime`, `DownloadTrendChart` | Navigasi ke layar lanjutan, teks status/kategori/waktu/angka, data grafik |
| `ui/common/StateView` | Bisa menampilkan satu tombol aksi (mis. "Hapus filter") selain "Coba lagi" |
| `res/navigation/nav_graph.xml` | Tujuan baru: `templateDetailFragment`, `guideFragment`, `providerProfileEditFragment` |
| Test: `DownloadTrendChartTest` (4), `RelativeTimeTest` (3), `AreaChartViewTest` (2), `StartupDecisionTest` disesuaikan | |

## Alasan keputusan
- Grafik dibuat sendiri (keputusan Aris) → tidak menambah dependency, gaya monokrom persis mengikuti token desain, ikut mode gelap otomatis.
- Tab = Fragment anak dengan show/hide → isi & posisi scroll tiap tab tetap utuh; layar lanjutan tetap di Navigation utama sehingga menu profil dan tombol kembali bekerja seperti sebelumnya.
- Data Beranda dimiliki shell → badge tab Beranda dan isi Beranda memakai satu request.
- Filter Template Anda di ViewModel milik Activity → diingat selama app terbuka (rancangan 6.3), tidak disimpan permanen.
- Edit profil memakai layout form provider → rancangan bagian 7 meminta "form yang sama"; validasinya juga sama (`OnboardingFormValidator`).
- Isi panduan yang bergantung pada aturan Upload belum ditulis → tampil "Segera hadir" (rancangan 3.9), yang sudah pasti (ZIP, angka) ditulis lengkap.
- Thumbnail masih ikon pengganti → gambar sampul baru ada setelah Upload, dan app belum memakai library pemuat gambar.
- Teks tombol form provider diganti "Aktifkan mode provider" → tidak ada lagi pengajuan yang ditinjau admin.

## Cara menjalankan & mengetes
1. Jalankan backend dengan data demo: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--app.seed.demo-templates=true`.
2. `adb reverse tcp:8080 tcp:8080`, lalu Run dari Android Studio.
3. Masuk sebagai `demo-provider@templateapp.test` / `password123` → Beranda lengkap (badge 3), Template Anda berisi 7 template.
4. Masuk sebagai `dummy8@templateapp.test` / `password123`, lalu (jika perlu) avatar → "Beralih ke mode provider" → provider kosong (checklist + "Belum ada template").
5. Masuk sebagai `dummy10@templateapp.test` → mode provider dinonaktifkan, langsung ke Dashboard Pembuat Website dengan banner merah.
6. Test & lint: `cd android && ./gradlew testDebugUnitTest lintDebug`.

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 35 test** (termasuk 9 test baru untuk grafik dan waktu relatif).
- `./gradlew lintDebug`: **0 error, 0 peringatan**.
- `./gradlew assembleDebug`: berhasil.
- Uji di HP (realme, Android 13, wireless debugging):
  - Akun Aris (provider tanpa template): checklist "0 dari 3" + kartu "Lengkapi profil kreatormu" + badge 1; setelah panduan dibuka → "1 dari 3" dan titik "baru" hilang; Template Anda kosong + Buka panduan; Upload "Segera hadir"; Profil & Edit profil terisi benar.
  - Akun demo: badge 3, Perlu tindakan (Landing Event 2 error, Profil Sekolah 3 peringatan, Instansi Desa draft), ringkasan 7 hari 3 · 78 · 23 dan 30 hari 3 · 376 · 97, grafik 7/30 hari + tooltip, populer berurutan, "Lihat semua" → filter Perlu perbaikan (2), detail Landing Event (2 error + 1 peringatan), pencarian, filter kategori, Hapus filter, filter tetap diingat setelah kembali dari detail.
  - `dummy10` dibuat `suspended` lewat SQL → app dibuka ulang → Dashboard Pembuat Website + banner "Mode provider dinonaktifkan…".
  - `dummy8`: checklist provider baru. Screenshot terang & gelap diambil (08, 12–15 di `docs/screenshots/`).
- Masalah yang ditemukan saat uji di HP dan sudah diperbaiki:
  - Geser tegak yang dimulai di atas grafik tidak men-scroll layar → grafik hanya mengambil alih geseran mendatar.
  - Label tanggal "30/9" dan "4/10" menumpuk di grafik 30 hari → label yang menabrak label hari terakhir dilewati.
  - Label tab "Template Anda" terpotong → "Template". Label "Segera hadir" berupa pil membuat judul terlipat 3 baris → dipindah ke baris keterangan.
  - Pencarian tanpa hasil menampilkan "Belum ada template…" (jumlah chip ikut tersaring pencarian) → kini "Tidak ada template dengan filter ini." + Hapus filter.
  - Animasi Lottie di `StateView` tak terlihat di mode gelap: warna hilang setiap ganti animasi (bug lama Fase 06) → warna dipasang ulang setiap ganti animasi.
  - Bottom navigation ikut naik di atas keyboard → disembunyikan selama keyboard terbuka; keyboard ditutup saat memilih filter.
  - Tombol kembali di tab selain Beranda langsung menutup app → kembali ke Beranda dulu.
  - Beranda tidak diperbarui saat tab dibuka lagi → dimuat ulang diam-diam.

## Tambahan: tab bisa digeser ke samping (permintaan Aris)
Di luar rancangan, Aris meminta tab Dashboard Provider bisa dipindah dengan menggeser layar ke samping.
- `ProviderDashboardFragment` memakai **ViewPager2** + `ProviderTabsAdapter`; bottom navigation dan halaman saling menyamakan pilihan. ViewPager2 sudah dipakai sejak Fase 06 (intro), jadi tidak ada library baru.
- Tab memakai `onResume` (bukan lagi `onHiddenChanged`) sebagai tanda "tab dibuka lagi".
- Grafik: geser mendatar tetap memilih titik (tidak pindah tab); geser tegak tetap men-scroll layar.
- Baris chip filter: `PagerAwareHorizontalScrollView` menggulir chip dulu, lalu pindah tab saat chip sudah mentok. (Pembungkus `NestedScrollableHost` dari contoh resmi sempat dicoba, tapi tidak bisa melepas geseran dari `HorizontalScrollView` yang sudah mentok, jadi diganti.)
- Uji di HP (akun demo): geser Beranda → Upload → Template → Profil dan kembali; geser di chip menggulir chip lalu pindah ke Profil saat mentok; geser mendatar di grafik memunculkan tooltip tanpa pindah tab; geser tegak dari grafik men-scroll; detail → kembali tetap di tab Template dengan filter & scroll utuh; Metode login terhubung → kembali tetap di Profil; tidak ada crash.

## Konsep yang dipelajari
- Custom View: `onDraw(Canvas)`, `Paint`, `Path`, `LinearGradient`, `onTouchEvent`, `requestDisallowInterceptTouchEvent`.
- Fragment anak (`getChildFragmentManager`), `show/hide`, `onHiddenChanged`.
- Lingkup ViewModel: Fragment, Fragment induk, atau Activity.
- RecyclerView: `ListAdapter` + `DiffUtil`, `ConcatAdapter`, paginasi lewat `OnScrollListener`.
- Debounce pencarian dengan `Handler.postDelayed`.
- Mengabaikan jawaban request yang sudah basi (nomor `generation`).
- `BadgeDrawable` di `BottomNavigationView`.
- ViewPager2 + `FragmentStateAdapter` untuk tab yang bisa digeser; perebutan geseran antara view bersarang (`requestDisallowInterceptTouchEvent`).
- `OnBackPressedCallback` (tombol kembali kustom) dan membaca keadaan keyboard dari `WindowInsetsCompat`.

## Latihan untuk Aris
1. Di `AreaChartView`, ubah alpha gradasi dari 70 menjadi 140 lalu Run. Apa bedanya di mode terang dan gelap? Kembalikan lagi.
2. Login akun demo, buka Template Anda, ketik "profil" di pencarian. Buka Logcat (filter `OkHttp`): berapa request yang terkirim selama mengetik? Coba ubah `SEARCH_DELAY_MS` menjadi 0 dan ulangi.
3. Jalankan `UPDATE templates SET warning_count = 0 WHERE name = 'Profil Sekolah';`, lalu kembali ke Beranda. Berapa angka badge tab Beranda sekarang, dan kenapa berubah tanpa menekan apa pun?

## Yang perlu Aris lakukan
- Masuk lagi dengan akunmu sendiri di HP (pengujian memakai akun demo & dummy).
- Database dev: `dummy10` sekarang `suspended` (diubah lewat SQL saat pengujian, sesuai tabel akun dummy di README).

## Rencana fase berikutnya
- Menunggu instruksi Aris: segmen Upload (rancangan bagian 9) atau fitur pembuatan website.
