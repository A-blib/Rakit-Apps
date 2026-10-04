# Fase 13: Backend galeri Template & profil pembuat website

Sumber rancangan: [`docs/rancangan/alur-pembuatan-website.md`](../rancangan/alur-pembuatan-website.md) (salinan `Instruksi dan alur/alurFiturPembuatanWibesite.md`). Penjelasan cara kerjanya ada di [`dokumentasi-project.md` bab 23](../dokumentasi-project.md#23-backend-galeri-template--profil-pembuat-website).

## Yang dikerjakan
| File | Fungsi |
|---|---|
| `gallery/GalleryController` | `GET /api/templates` (publik): filter kategori, cari, urutan `popular`/`newest`, paginasi |
| `gallery/GalleryService` | Validasi parameter (kategori, urutan, `page ≥ 0`, `size` 1–50) |
| `gallery/GalleryQueries` | SQL galeri: hanya template `published` milik provider `active`, jumlah download, nama kreator |
| `gallery/dto/GalleryTemplateResponse`, `GalleryPageResponse` | Bentuk JSON galeri |
| `user/UserController`, `UserService`, `user/dto/CreatorProfileUpdateRequest` | `PATCH /api/users/me/creator-profile` |
| `security/SecurityConfig` | `GET /api/templates` boleh tanpa login |
| `GalleryIntegrationTest` (8 test) | Galeri (status, provider ditangguhkan, urutan, filter, pencarian `%`, paginasi, size, parameter salah, event dilihat oleh tamu) + edit profil pembuat website |
| `AGENTS.md` bagian 16, `docs/rancangan/alur-pembuatan-website.md`, `alur-provider.md` 3.4 | Rancangan + keputusan Aris |

## Alasan keputusan
- Galeri list satu kolom, "Dilihat" saat template diklik, avatar → tab Profil di kedua dashboard → **keputusan Aris** (bagian 7 rancangan).
- Template dari provider yang ditangguhkan disembunyikan dari galeri → penangguhan adalah rem darurat untuk konten berbahaya (alur-provider bagian 1). Rancangan tidak menyebutnya; ini keputusan saya, mudah dibalik bila Aris tidak setuju.
- Satu endpoint untuk galeri dan "Template untuk anda" (sesuai rancangan 6.2) → tidak ada endpoint khusus yang perlu dirawat.
- `size` maksimal 50 → endpoint publik tidak boleh bisa meminta seluruh isi tabel dalam satu request.
- Endpoint edit profil terpisah dari onboarding → mengedit profil tidak boleh mengubah mode aktif (alternatif yang tidak dipilih: memakai ulang `POST /onboarding/creator`, yang selalu memindahkan user ke mode pembuat website).
- "Dilihat" memakai `POST /api/templates/{id}/events` yang sudah ada → backend tidak perlu diubah.

## Cara menjalankan & mengetes
1. `cd backend && ./mvnw test` (Docker harus aktif).
2. Jalankan backend dengan data demo (README "Data demo provider").
3. Buka `http://localhost:8080/api/templates` di browser (tanpa login) → 3 template demo yang tayang.
4. Swagger → grup **Galeri** → coba `category=umkm`, `q=profil`, `sort=newest`, `size=1&page=1`.
5. Swagger → login → Authorize → `PATCH /api/users/me/creator-profile`.

## Hasil tes
- `./mvnw test`: **lulus, 86 test** (8 test baru di `GalleryIntegrationTest`).
- Uji manual ke backend dev: `GET /api/templates?size=3` mengembalikan Profil Sekolah (47 download), UMKM Kuliner (30), Portofolio Minimal (20), semuanya oleh "Studio Demo", berurutan sesuai jumlah download.

## Konsep yang dipelajari
- Endpoint publik di tengah API yang dilindungi: `requestMatchers(HttpMethod.GET, "/api/templates").permitAll()` hanya membuka satu method + path.
- `JOIN` untuk menyaring berdasarkan tabel lain (status provider) dan subquery untuk menghitung download.
- `NULLS LAST` saat mengurutkan kolom yang boleh kosong.
- Isolasi test: test galeri mengosongkan tabel `templates` dulu, karena galeri membaca template milik semua provider, termasuk sisa test lain.

## Latihan untuk Aris
1. Buka `http://localhost:8080/api/templates?sort=newest` di browser. Bandingkan urutannya dengan `sort=popular`. Kolom apa yang menentukan masing-masing urutan?
2. Jalankan `UPDATE provider_profiles SET status = 'suspended' WHERE user_id = (SELECT id FROM users WHERE email = 'demo-provider@templateapp.test');`, lalu buka galeri lagi. Apa yang terjadi? Kembalikan dengan `status = 'active'`.
3. Coba `http://localhost:8080/api/templates?size=100`. Kenapa ditolak, dan di file mana batasnya ditulis?

## Yang perlu Aris lakukan
- Tidak ada.

## Rencana fase berikutnya
- Fase 14: Android Dashboard Pembuat Website. Bottom navigation Beranda · Project · **+** · Template · Profil, Room untuk project, tab Project lengkap, galeri Template, "Template untuk anda", panduan, Profil + edit, dua halaman editor "Segera hadir", tombol debug "Isi/Hapus project contoh", dan avatar → tab Profil di kedua dashboard.
