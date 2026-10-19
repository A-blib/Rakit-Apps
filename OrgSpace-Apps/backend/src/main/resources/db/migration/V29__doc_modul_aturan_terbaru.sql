-- Aturan modul dinamis setelah revisi 19 Oktober 2026 (Portal Pengurus & keamanan hosting).
UPDATE doc_articles
SET body = body || '

---

## 5. Aturan Tambahan (Revisi Oktober 2026)

- **Pengumuman = Berita.** `data-ws-list="pengumuman"`, `"berita"`, dan `"kegiatan"` sama-sama diisi dari tab **Berita** di Portal Pengurus. Nama modul lain (mis. `prestasi`, `agenda`) menjadi **Modul Kustom**: nama modul dan field-nya otomatis muncul di Portal Pengurus.
- **`data-ws-limit="6"`** membatasi jumlah kartu yang tampil (1–100).
- **`data-ws-role="admin,editor"`** hanya tampil untuk pengurus yang sedang masuk dengan peran tersebut. Pemilik ikut dianggap admin. Tamu tidak melihatnya.
- **`data-ws-auth="logout"`** otomatis tersembunyi untuk tamu, dan tombol `login` tersembunyi setelah pengurus masuk.
- **Hanya `<form data-ws-form="...">`** (atau form di dalam elemen ber-`data-ws-form`) yang dikirim ke Kotak Masuk. Form lain (mis. pencarian) tidak disentuh.
- **Atribut `on...` (onclick, onerror, dst.) dan link `javascript:` dihapus saat website diterbitkan.** Semua website tayang di satu domain, jadi skrip seperti ini bisa mencuri sesi pengurus website lain. Gunakan link biasa.
- Link dari data pengurus (`data-ws-link`) hanya dipakai jika diawali `http`, `https`, `mailto:`, `tel:`, `/`, atau `#`.
- Formulir pengunjung dibatasi 30 isian, masing-masing maksimal 5.000 karakter. Nama form: huruf kecil, angka, `-` atau `_`.
'
WHERE slug = 'modul-bawaan-orgspace';
