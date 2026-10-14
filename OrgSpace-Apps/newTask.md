Periksa _workspace_ saat ini terlebih dahulu untuk memahami _tech stack_ serta cara kerja **Aplikasi Builder** (Mode Template + Mode Custom + tombol Publish) dan **Web Monitoring**.

**Tugasmu:** Rancang dan bangun secara mandiri sistem ke-3: **Sistem Hosting Publik (`namaSystem.app/[namawebuser]`)** sebagai tempat website user tayang otomatis setelah menekan tombol Publish.

**Langkah Eksekusi:**

1. **Auto-Adapt:** Deteksi skema DB, endpoint API, dan format data output saat tombol "Publish" ditekan di Aplikasi Builder (baik mode Template maupun Custom).
2. **Backend/API:** Buat atau hubungkan endpoint penerima data Publish dan endpoint pengambilan data publik berdasarkan `slug` (`namawebuser`).
3. **Dynamic Route (`/[namawebuser]`):** Ambil dan tampilkan data web berdasarkan `slug`. Tampilkan halaman 404 jika tidak ditemukan atau belum publish.
4. **Dual Renderer:** Buat `TemplateRenderer` (untuk mode template) dan `CustomRenderer` (untuk mode custom) yang langsung menyesuaikan struktur data dari Aplikasi Builder.
5. **SEO & Sinkronisasi Instan:** Pasang meta tag dinamis per web user dan pastikan perubahan langsung tayang saat user klik Publish ulang.

Jangan tanya spesifikasi teknis kepadaku—langsung sesuaikan dengan kode yang ada, jaga _separation of concerns_, buat daftar _task_-nya sendiri, dan langsung implementasikan kodenya.
