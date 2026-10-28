-- Kode undangan organisasi sekali pakai dan berlaku 1 jam (keputusan Aris, 28 Oktober 2026). Kode yang sudah dipakai
-- dicatat siapa dan kapan, agar tidak bisa dipakai orang kedua (mis. kode yang tersebar di grup).
ALTER TABLE organization_invitations
    ADD COLUMN used_at timestamptz,
    ADD COLUMN used_by uuid REFERENCES users (id) ON DELETE SET NULL;
