-- Revisi OrgSpace (alur-fitur-upload.md 15.1): kategori template memakai kategori organisasi (sama dengan
-- organizations.category), ditambah "Isi template" (content_tags) untuk label "Punya: ..." di galeri.

ALTER TABLE templates DROP CONSTRAINT chk_templates_category;
ALTER TABLE templates ALTER COLUMN category TYPE VARCHAR(30);

-- Kategori lama (tujuan website) dipetakan seadanya: data lama hanya data uji & demo.
UPDATE templates
   SET category = CASE category
                      WHEN 'sekolah' THEN 'osis_mpk'
                      WHEN 'organisasi' THEN 'ukm'
                      ELSE 'lainnya'
                  END
 WHERE category IS NOT NULL;

ALTER TABLE templates ADD CONSTRAINT chk_templates_category CHECK (category IN (
    'osis_mpk', 'bem_dema_dpm', 'himpunan', 'ukm', 'pramuka', 'jurnalistik_media',
    'pmr_ksr', 'paskibra', 'kerohanian', 'seni_olahraga', 'lainnya'));

ALTER TABLE templates ADD COLUMN content_tags TEXT[] NOT NULL DEFAULT '{}';
ALTER TABLE templates ADD CONSTRAINT chk_templates_content_tags CHECK (content_tags <@ ARRAY[
    'profil', 'struktur', 'kontak', 'proker', 'berita', 'galeri', 'agenda', 'oprec', 'aspirasi']::TEXT[]);
