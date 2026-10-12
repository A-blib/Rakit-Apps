# Fase R4: Kelola organisasi (Android)

Sumber rancangan: [`docs/rancangan/revisi-fondasi-orgspace.md`](../rancangan/revisi-fondasi-orgspace.md) bagian 3.1, 4.4, 6 (keputusan K2, K3, K10, K18, K19).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `data/remote/api/OrganizationApi.java` + DTO baru | Ubah profil, unggah logo (multipart), hapus (`@HTTP DELETE` dengan body nama konfirmasi), anggota, ubah peran/jabatan, keluarkan, keluar, serah terima, undangan (buat/daftar/cabut), riwayat aktivitas per halaman |
| `data/model/Organization`, `Member`, `Invitation`, `Activity` | Model; `Organization.canManage()` (Admin ke atas) dan `isOwner()` dipakai untuk menampilkan menu sesuai peran |
| `data/repository/OrganizationRepository` | Semua aksi kelola organisasi. Aksi yang mengubah daftar ruang kerja (nama/logo, keluar, hapus, serah terima) memuat ulang `WorkspaceRepository` agar app bar ikut berubah |
| `core/template/ImageProcessor.logoPng(uri)` | Logo dibaca sudah diperkecil, diputar sesuai EXIF, dipotong persegi 512 px, lalu dikirim sebagai PNG |
| `ui/organization/OrganizationFragment` + `fragment_organization.xml`, `item_menu_row.xml`, `view_org_logo.xml` | Layar organisasi (dari pemilih ruang kerja → **Kelola ruang kerja ini**): logo, "Peranmu: …", jumlah anggota, menu sesuai peran, Keluar, Hapus |
| `ui/organization/OrganizationProfileFragment` + ViewModel | Profil: identitas, jenjang/kategori, periode, kontak & media sosial, **Ganti logo** (Photo Picker). Editor hanya melihat (banner info, input nonaktif, tanpa tombol Simpan) |
| `ui/organization/MembersFragment` + `MembersViewModel`, `item_member.xml`, `item_invitation.xml` | Daftar pengurus + undangan aktif; menu titik tiga per anggota: jadikan Admin/Editor, ubah jabatan, keluarkan; cabut undangan |
| `ui/organization/InviteSheet` + `sheet_invite.xml` | Pilih peran → **Buat kode undangan** → kode tampil sekali dengan **Salin** dan **Bagikan** (share sheet Android berisi pesan ajakan) |
| `ui/organization/TransferOwnershipFragment` | Serah terima: pilih Pemilik baru + peranmu setelahnya (Admin, Editor, atau keluar) |
| `ui/organization/ActivityFragment`, `ActivityViewModel`, `ui/workspace/ActivityText` (+ `ActivityTextTest`) | Riwayat aktivitas berupa kalimat ("Agus mengubah peran Budi menjadi Editor"), dengan tombol muat lagi |
| `ui/organization/OrgUi`, `OrganizationArgs`, `dialog_text_input.xml` | Bantuan bersama: logo/huruf depan, format tanggal Indonesia, dialog satu isian, kembali ke dashboard setelah keluar/hapus |
| `res/navigation/nav_graph.xml` | 5 layar baru dengan argumen `organizationId` |

## Alasan keputusan
- **Menu disaring di app, aturan tetap di backend** → Editor tidak melihat tombol yang pasti ditolak, tapi keamanan tidak bergantung pada app (backend `OrganizationAccess` tetap menolak). (Alternatif: tampilkan semua tombol lalu tunggu error → membingungkan.)
- **Logo diperkecil di HP sebelum diunggah** → foto kamera 10 MB jadi ±300 KB, unggah cepat di kuota pelajar. Backend tetap memotong dan menulis ulang PNG agar file aneh tidak tersimpan apa adanya.
- **Kode undangan hanya tampil sekali** → yang disimpan server hanya hash-nya, sama seperti password. Karena itu ada tombol Salin dan Bagikan langsung di sheet.
- **Bagikan memakai share sheet Android** (`Intent.ACTION_SEND`) → pengurus bisa mengirim lewat WhatsApp, Telegram, dll. tanpa library tambahan.
- **Hapus organisasi wajib mengetik ulang nama** → aksi permanen dan menghapus website semua anggota; mengetik nama lebih sulit terjadi tanpa sengaja daripada satu tombol "Ya".
- **Ruang pribadi memakai layar yang sama** dengan menu Anggota/Serah terima/Keluar disembunyikan dan label "Profil ruang pribadi" / "Hapus ruang pribadi" → satu kode untuk dua jenis ruang kerja.

## Cara menjalankan & mengetes
1. Backend dev jalan, `adb reverse tcp:8080 tcp:8080`, `./gradlew installDebug`.
2. Masuk sebagai Editor (mis. `dummy5`), pilih **OSIS SMA Negeri 1 Bandung** → ▾ → **Kelola ruang kerja ini** → **Profil organisasi**: ada banner "Hanya Admin dan Pemilik…", tanpa tombol Simpan. Di **Anggota** tidak ada tombol Undang.
3. Masuk sebagai Pemilik (mis. `dummy3`): **Anggota → Undang anggota → Admin → Buat kode undangan** → **Salin** (muncul "Kode disalin.") dan **Bagikan** (share sheet terbuka).
4. Dengan akun lain, gabung memakai kode itu. Kembali ke Anggota: anggota baru muncul. Ketuk namanya → jadikan Editor, ubah jabatan. Cabut undangan.
5. Sebagai Pemilik tunggal, tekan **Keluar dari organisasi** → ditolak "Jadikan anggota lain Pemilik dulu sebelum keluar."
6. **Profil organisasi → Ganti logo** → pilih foto → logo persegi muncul di profil, layar organisasi, dan pemilih. Isi Instagram dengan link `https://instagram.com/xxx` → tersimpan sebagai `xxx`.
7. **Serah terima** → pilih anggota → "Tetap sebagai Admin" → peranmu berubah jadi Admin, menu Serah terima & Hapus hilang.
8. **Riwayat aktivitas**: semua aksi di atas tercatat dengan waktu relatif.

## Hasil tes
- `./gradlew testDebugUnitTest`: **lulus, 106 test** (termasuk `ActivityTextTest`).
- `./gradlew lint`: **0 masalah**. `./gradlew assembleDebug`: berhasil. Backend `./mvnw test`: 288 lulus (R1).
- Uji di HP (realme RMX3151, Android 13, 12 Oktober 2026), skenario 6–13 bagian 11 rancangan:
  - Editor: profil hanya-baca, tanpa tombol Undang, tanpa menu anggota ✅
  - Pemilik: buat kode Admin, Salin, Bagikan (share sheet + pesan "Halo! Yuk gabung ke …") ✅
  - Anggota baru (dummy2 lewat API) muncul; jadikan Editor, ubah jabatan jadi "Wakil Ketua", cabut undangan (dengan konfirmasi) ✅
  - Pemilik terakhir keluar → ditolak dengan pesan jelas ✅
  - Ganti logo → terpotong persegi 512 px, tampil di app ✅; Instagram link → username, WhatsApp `0812-…` → `62812…` ✅
  - Serah terima ke anggota lain, tetap Admin → menu menyesuaikan; Admin tidak bisa membuka menu Pemilik ✅
  - Admin keluar → kembali ke dashboard dengan ruang kerja lain + pesan "Kamu sudah keluar dari …" ✅
  - Riwayat aktivitas mencatat 8 aksi berurutan ✅
  - Mode gelap & terang (lewat Pengaturan → Tema) ✅
- Ditemukan & diperbaiki saat uji HP (commit `5f6721c`):
  1. Riwayat menampilkan "0 menit yang lalu" → kini "Baru saja".
  2. Daftar Anggota tidak berubah setelah kembali dari WhatsApp (anggota baru tidak tampil) → dimuat ulang di `onResume`.
  3. Ruang pribadi berlabel "Profil organisasi"/"Hapus organisasi" → "Profil ruang pribadi"/"Hapus ruang pribadi".
  4. Sudut gambar logo tidak membulat seperti kotak huruf depan → `ShapeableImageView` dengan `ShapeAppearance.App.Small`.
  - Catatan: pada percobaan pertama, logo yang terunggah bukan foto yang dipilih. Penyebabnya, saya mengetuk grid Photo Picker sebelum selesai dimuat. Percobaan ulang benar, dan kode app hanya meneruskan URI dari picker.

## Konsep yang dipelajari
- **Multipart upload di Retrofit**: `MultipartBody.Part.createFormData("file", "logo.png", RequestBody.create(bytes, PNG))` + `@Multipart`/`@Part` di interface API.
- **`@HTTP(method = "DELETE", hasBody = true)`**: anotasi `@DELETE` biasa tidak boleh punya body; anotasi `@HTTP` dipakai saat butuh body (nama konfirmasi).
- **Share sheet**: `Intent.createChooser(new Intent(ACTION_SEND).setType("text/plain").putExtra(EXTRA_TEXT, …))`. App tujuan dipilih user, app kita tidak perlu tahu WhatsApp.
- **`onResume` vs `onViewCreated`**: `onViewCreated` hanya jalan saat view dibuat; kembali dari app lain hanya memicu `onResume`. Data yang bisa berubah di luar app dimuat ulang di sana.
- **Pembatasan di UI ≠ keamanan**: menyembunyikan tombol hanya membantu user; yang menjaga data tetap backend.

## Latihan untuk Aris
1. Saat ini perubahan jabatan tidak tercatat di riwayat. Di backend, catat aksi `member.position_changed` (lihat cara `member.role_changed` dicatat di `MemberService`), lalu di Android tambahkan kalimatnya di `ui/workspace/ActivityText` ("{aktor} mengubah jabatan {anggota}") beserta tes di `ActivityTextTest`.
2. Ubah pesan Bagikan di `strings_workspace.xml` (cari `invite_share_text`) agar menyebut peran yang diundang (Admin/Editor), install, lalu coba Bagikan ke Catatan.
3. Coba lewat Swagger: sebagai Editor panggil `PATCH /api/organizations/{id}` dan perhatikan error `ROLE_NOT_ALLOWED`. Kenapa ini tetap penting walau tombol Simpan sudah disembunyikan di app?

## Yang perlu Aris lakukan
- Tidak ada perubahan Gradle (tidak perlu Sync Now).
- Logo uji yang saya unggah tersimpan di `backend/data/org-logos/` (di-ignore git); organisasi uji "OSIS SMK Negeri 4 Uji" milik dummy2 boleh dihapus.

## Rencana fase berikutnya
- **W1–W4** (`alur-pembuatan-website.md` revisi): menu bawah Beranda · Website · + · Template · Profil, halaman kelola website, galeri kategori organisasi, sapaan `{org}` di Beranda, Isi template & kategori di Upload.
