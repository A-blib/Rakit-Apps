-- Tiket GOOGLE_NONCE: nonce sekali pakai yang ditanam Google ke dalam ID token (GetSignInWithGoogleOption.setNonce).
-- Tanpa ini ID token Google bisa dipakai ulang oleh siapa pun yang mendapat salinannya sampai kedaluwarsa (~1 jam).
ALTER TABLE auth_tickets DROP CONSTRAINT chk_auth_tickets_type;
ALTER TABLE auth_tickets ADD CONSTRAINT chk_auth_tickets_type
    CHECK (type IN ('GITHUB_STATE', 'LOGIN_RESULT', 'LINK', 'LINK_RESULT', 'GOOGLE_NONCE'));

-- Pembersihan tiket kedaluwarsa (AuthTicketService.deleteExpired) mencari menurut waktu.
CREATE INDEX idx_auth_tickets_expires_at ON auth_tickets (expires_at);
