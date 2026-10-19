# Contoh template: Portal Organisasi Lengkap

Template contoh yang memakai **semua modul dinamis** OrgSpace. Dipakai untuk menguji upload provider → galeri →
terbit → Portal Pengurus (19 Oktober 2026).

| Modul | Atribut | Diisi dari |
|---|---|---|
| Login pengurus | `data-ws-auth="login"`, `"logout"`, `"status"` | Portal Pengurus (`/{slug}/admin`) |
| Khusus pengurus | `data-ws-role="admin,editor"` | Peran pengurus yang sedang masuk |
| Pengumuman | `data-ws-list="pengumuman"` + `data-ws-limit="6"` | Tab Berita |
| Prestasi (modul kustom) | `data-ws-collection="prestasi"` | Tab Modul Kustom (field otomatis: foto, tingkat, judul, nama, tahun) |
| Agenda (modul kustom) | `data-ws-collection="agenda"` + `data-ws-link="link_daftar"` | Tab Modul Kustom |
| Pendaftaran & aspirasi | `<form data-ws-form="pendaftaran">`, `"aspirasi"` | Kotak Masuk |

Cara upload: ZIP isi folder ini (bukan foldernya), lalu upload dari app sebagai provider.
