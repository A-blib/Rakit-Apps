-- Domain yang masih "pending" (belum terbukti lewat DNS) tidak boleh menghalangi pemilik asli mendaftarkannya.
-- Jadi hostname hanya wajib unik untuk domain AKTIF; klaim pending boleh lebih dari satu website.
ALTER TABLE site_domains DROP CONSTRAINT site_domains_hostname_key;
DROP INDEX idx_site_domains_hostname_active;
CREATE UNIQUE INDEX uq_site_domains_hostname_active ON site_domains (hostname) WHERE status = 'active';
CREATE INDEX idx_site_domains_hostname ON site_domains (hostname);
