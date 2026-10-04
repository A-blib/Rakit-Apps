-- Rancangan provider terbaru (docs/rancangan/alur-provider.md): provider langsung aktif setelah mengisi form,
-- tanpa verifikasi admin. Status hanya 'active' dan 'suspended' (rem darurat untuk konten berbahaya).
-- Data lama pending/approved/rejected menjadi active; suspended tetap.

ALTER TABLE provider_profiles DROP CONSTRAINT chk_provider_profiles_status;

UPDATE provider_profiles SET status = 'active' WHERE status IN ('pending', 'approved', 'rejected');

ALTER TABLE provider_profiles
    ADD CONSTRAINT chk_provider_profiles_status CHECK (status IN ('active', 'suspended')),
    ALTER COLUMN status SET DEFAULT 'active';

-- Tidak ada lagi penolakan pengajuan provider, jadi alasan penolakan tidak diperlukan.
ALTER TABLE provider_profiles DROP COLUMN rejection_reason;
