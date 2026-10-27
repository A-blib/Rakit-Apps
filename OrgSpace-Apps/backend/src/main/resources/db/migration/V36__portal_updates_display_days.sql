-- Lama info terbaru tampil di portal publik, dihitung sejak pertama tayang (published_at). null = tanpa batas.
-- Lewat masanya, info otomatis tidak tampil di landing/detail, tapi tetap ada di dashboard (bisa diperpanjang).
ALTER TABLE portal_updates
    ADD COLUMN display_days integer CHECK (display_days BETWEEN 1 AND 3650);
