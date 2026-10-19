-- Panduan modul kini satu artikel di portal web: "Panduan Modul Dinamis" (slug modul-sistem-dan-kustom,
-- OrgSpace-Website/src/lib/docs-data.ts) beserta ZIP contoh. Dua artikel lama isinya tumpang-tindih dan sebagian
-- sudah usang, jadi dijadikan draft (tidak dihapus, bisa diterbitkan lagi dari dashboard).
UPDATE doc_articles SET status = 'draft' WHERE slug IN ('modul-bawaan-orgspace', 'panduan-modul-kustom');
