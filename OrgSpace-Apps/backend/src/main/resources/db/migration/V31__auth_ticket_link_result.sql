-- Tiket LINK_RESULT: hasil login GitHub dari Pengaturan (menyambungkan GitHub). Dulu callback langsung menyambungkan
-- identitas, sehingga URL "sambungkan GitHub" milik orang lain bisa menyambungkan GitHub korban ke akun penyerang.
-- Sekarang penyambungan baru terjadi saat app yang memulai alur menukar tiket ini (dengan code_verifier + JWT).
ALTER TABLE auth_tickets DROP CONSTRAINT chk_auth_tickets_type;
ALTER TABLE auth_tickets ADD CONSTRAINT chk_auth_tickets_type
    CHECK (type IN ('GITHUB_STATE', 'LOGIN_RESULT', 'LINK', 'LINK_RESULT'));
