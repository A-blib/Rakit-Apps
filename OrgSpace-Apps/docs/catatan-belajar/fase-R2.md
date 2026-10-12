# Fase R2: Desain & bahasa OrgSpace

Sumber rancangan: [`docs/rancangan/revisi-fondasi-orgspace.md`](../rancangan/revisi-fondasi-orgspace.md) bagian 2, 7, 8, 9 (keputusan K13, K14, K22, K23).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `res/values/colors.xml`, `values-night/colors.xml` | Palet "Biru Semangat" lengkap (17 token + tint), versi terang & biru malam |
| `res/values/themes.xml` | Pemetaan token ke atribut Material 3 (`colorPrimary` biru, `colorSecondary` kuning, dst.); font tidak lagi dipasang lewat tema |
| `res/values/type.xml` | Plus Jakarta Sans: Display ExtraBold, Headline Bold, Label SemiBold tanpa kapital, `AppBarTitle`, `Default` |
| `res/values/dimens.xml` | Padding layar 20dp, radius 12/16/24dp, `radius_button` (pil), `radius_pill` |
| `res/values/styles.xml`, `res/color/selector_*.xml` | Tombol pil biru, tombol sekunder bergaris biru, input putih fokus biru, chip terpilih biru muda, badge biasa & `Badge.Accent` kuning, bottom nav dengan pil aktif, snackbar beraksen kuning |
| `res/font/plus_jakarta_sans*.ttf/.xml`, `assets/licenses/plus-jakarta-sans-OFL.txt` | Font Plus Jakarta Sans 2.7.1 dari repositori resmi Tokotype; Geist + lisensinya dihapus |
| `res/drawable/ic_*.xml` | 43 ikon diganti Material Symbols **Rounded** (diambil otomatis dari sumber resmi Google) |
| `res/drawable/ic_launcher_*.xml`, `res/mipmap-*` | Ikon app baru: huruf "O" putih + titik kuning di atas biru; gambar robot Android bawaan dihapus |
| `res/drawable/bg_*.xml` | Badge, avatar kuning, titik halaman & titik "baru", sudut 16dp |
| `tools/generate_lottie.py`, `res/raw/*.json` | Lottie berwarna (garis biru + isian kuning); intro baru `intro_home`, `intro_team`, `intro_period` |
| `ui/common/LottieTint.java` | Mewarnai elemen `stroke-primary`/`fill-accent`/… dengan token tema |
| `res/values/strings*.xml` | Nama app **OrgSpace**, intro, pilih peran, pesan error, "kreator template", "Mode kreator" (tabel 8.2) |
| `AreaChartView`, `ColorPickerDialog`, `ProviderDashboardFragment`, `ProjectUi` | Warna dari kode ikut palet baru (garis grafik biru, badge jumlah kuning) |
| `src/debug/.../ComponentCatalogActivity` + layout/strings katalog | Menampilkan token & badge baru |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.8.1 (lint memberi tahu versi baru) |

## Alasan keputusan
- **Font tidak dipasang lewat `android:fontFamily` di tema** → atribut tema dianggap atribut view dan menimpa font dari `textAppearance`, sehingga semua judul tampil Regular (terlihat saat dicek di HP). Pengganti: `android:textAppearance = TextAppearance.App.Default` di tema. (Alternatif: `android:textViewStyle` berisi font → tetap menimpa textAppearance karena style juga lebih kuat.)
- **Token `color_foreground` tetap untuk teks; tombol & elemen aktif memakai token baru `color_primary`** → nama token lama tidak berubah arti, jadi layout lama tetap benar. (Alternatif: mengubah `color_foreground` jadi biru → semua teks ikut biru.)
- **Lottie diwarnai lewat nama elemen** (`stroke-primary`, `fill-accent`) → dua warna bisa diatur terpisah dan ikut mode gelap. (Alternatif: satu `COLOR_FILTER` untuk semua layer seperti sebelumnya → hanya bisa satu warna.)
- **Ikon diganti dengan skrip dari URL resmi Material Symbols** → 43 ikon berganti seragam, bentuk viewBox sama (960) sehingga cukup mengganti `pathData`.
- **Ikon launcher dibuat sebagai vector**, bukan PNG → minSdk 26 sudah mendukung adaptive icon; satu file untuk semua kepadatan layar.
- **Sapaan Beranda belum memakai nama organisasi** ("Kerja keras organisasimu …") → nama organisasi baru tersedia di app setelah R3; kalimat dengan `{org}` dipasang di fase W3.

## Cara menjalankan & mengetes
1. Klik **Sync Now** di Android Studio (wrapper Gradle berubah).
2. `cd android && ./gradlew installDebug`, lalu buka **Katalog komponen** (ikon terpisah, hanya build debug).
3. Periksa tipografi (Display tebal), warna, tombol pil, chip terpilih biru muda, badge "Segera hadir" kuning, animasi loading berwarna.
4. Ketuk **Ganti terang / gelap**: semua tetap terbaca.
5. Buka app biasa: nama "OrgSpace", ikon launcher baru, intro 3 halaman baru (hapus data app dulu untuk melihat intro).

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 94 test**.
- `./gradlew lint`: **0 masalah** (setelah Gradle 9.8.1 dan font family dipakai `TextAppearance.App.Default`).
- `./gradlew assembleDebug`: berhasil.
- Uji di HP (realme RMX3151): katalog komponen di mode gelap & terang, Dashboard kreator (state error koneksi, bottom nav, badge "Mode kreator", avatar kuning) → sesuai. Ditemukan & diperbaiki: ketebalan font tidak terpakai (lihat Alasan keputusan).

## Konsep yang dipelajari
- **Urutan prioritas atribut TextView**: atribut di layout/tema > style > textAppearance. Karena itu font di tema bisa "mengalahkan" font di textAppearance.
- **Dynamic properties Lottie**: `KeyPath("**", "stroke-primary")` = "elemen bernama stroke-primary di layer mana pun"; `LottieProperty.STROKE_COLOR`/`COLOR` mengganti warna garis/isian saat app berjalan.
- **Adaptive icon**: ikon launcher terdiri dari latar + gambar depan 108dp; launcher memotong bentuknya (bulat, kotak membulat), jadi gambar penting harus di zona aman 66dp di tengah.
- **Token desain**: semua warna/ukuran di satu tempat, jadi ganti gaya seluruh app cukup dengan mengubah `colors.xml`, `dimens.xml`, `type.xml`, `styles.xml`.

## Latihan untuk Aris
1. Ubah `color_accent` di `values/colors.xml` menjadi oranye (`#FF9F1C`), install, lalu lihat badge "Segera hadir" dan animasi loading.
2. Di `tools/generate_lottie.py`, ubah pintu rumah di `intro_home()` menjadi `fill(role="primary")`, jalankan `python3 tools/generate_lottie.py`, lalu lihat intro.
3. Tambahkan `<item name="android:fontFamily">@font/plus_jakarta_sans</item>` ke `Base.Theme.App`, install, dan amati judul katalog. Kenapa jadi tipis? Hapus lagi.

## Yang perlu Aris lakukan
- Klik **Sync Now** di Android Studio (Gradle wrapper 9.8.1).
- Uninstall atau hapus data app jika ingin melihat intro baru dari awal.

## Rencana fase berikutnya
- **R3**: onboarding baru (buat organisasi / gabung lewat kode / ruang pribadi), form organisasi dengan kategori per jenjang, pemilih ruang kerja di app bar, ruang kerja terakhir di `SessionStore`.
