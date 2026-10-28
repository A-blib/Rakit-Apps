# Caddy di VPS OrgSpace (khusus API)

Sejak 28 Oktober 2026 (keputusan Aris), VPS hanya menjalankan **backend Spring Boot + PostgreSQL**. OrgSpace-Website
dan OrgSpace-Hosting (website organisasi dan domain milik organisasi) di-deploy ke **Vercel** sebagai dua project.
Jadi Caddy hanya melayani satu domain: `api.orgspace.app`.

```
App Android, portal web (Vercel), Hosting (Vercel), Portal Pengurus (browser)
        → https://api.orgspace.app → Caddy (80/443) → Backend 127.0.0.1:8080
```

Langkah lengkap ada di **`Panduan-Hosting-OrgSpace.docx` bab 7**. Ringkasnya:

```bash
sudo cp /opt/orgspace/repo/OrgSpace-Apps/docs/deploy/Caddyfile /etc/caddy/Caddyfile
echo 'API_DOMAIN=api.orgspace.app' | sudo tee /etc/caddy/.env
sudo systemctl edit caddy      # tulis: [Service] lalu EnvironmentFile=/etc/caddy/.env
sudo caddy validate --config /etc/caddy/Caddyfile --envfile /etc/caddy/.env
sudo systemctl restart caddy
```

Uji di laptop sebelum deploy (butuh program `caddy` dan backend di :8080): `docs/deploy/uji-caddy-lokal.sh`.

Yang **tidak** lagi diurus Caddy: HTTPS website `orgspace.app` dan domain organisasi (mis. `osis.sch.id`). Keduanya
diurus Vercel; domain organisasi didaftarkan otomatis oleh backend lewat API Vercel (`VERCEL_TOKEN`).
