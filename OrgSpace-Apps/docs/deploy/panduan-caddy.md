# Panduan memasang Caddy di VPS OrgSpace

Caddy adalah pintu depan server: menerima semua request dari internet (port 80/443), mengurus HTTPS, lalu meneruskan
request ke program yang tepat berdasarkan **domain** yang dibuka. Dipasang **sekali** saat menyiapkan VPS; website yang
diterbitkan user setelahnya tidak butuh pengaturan apa pun.

```
api.orgspace.app              → Caddy → Backend Spring Boot   127.0.0.1:8080  (app Android, portal web)
orgspace.app/{slug}           → Caddy → OrgSpace-Hosting      127.0.0.1:3001  (pengunjung website)
domain organisasi (osis.sch.id)→ Caddy → OrgSpace-Hosting     127.0.0.1:3001  (HTTPS dibuat otomatis)
web.orgspace.app              → Vercel (tidak lewat Caddy)                    (portal web)
```

Perintah di bawah diperiksa terhadap dokumentasi resmi Caddy (caddyserver.com/docs/install, /running,
/command-line) pada 26 Oktober 2026. Contoh domain memakai `orgspace.app`; ganti dengan domain milikmu.

## 0. Syarat sebelum mulai

- VPS Ubuntu/Debian dengan IP publik, akses `ssh` dan `sudo`.
- Backend (`:8080`) dan OrgSpace-Hosting (`:3001`) sudah bisa jalan di VPS. Portal web ada di Vercel.
  Cek dari dalam VPS:
  ```bash
  curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/api/meta/categories   # harus 200
  curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:3001/                      # 307 (dialihkan ke portal)
  ```
- Belum ada program lain yang memakai port 80/443 (mis. nginx/apache): `sudo ss -ltnp | grep -E ':(80|443) '` harus kosong.

## 1. Arahkan domain ke VPS (DNS)

Di panel DNS (Cloudflare/registrar), buat record berikut ke IP publik VPS:

| Tipe | Nama | Isi | Untuk |
|---|---|---|---|
| A | `@` (orgspace.app) | IP VPS | website organisasi |
| A | `www` | IP VPS | website organisasi (www) |
| A | `api` | IP VPS | backend |
| CNAME | `web` | nilai yang ditampilkan Vercel | portal web (bukan ke VPS) |
| A | `custom` | IP VPS | target CNAME domain milik organisasi (`SITES_CUSTOM_TARGET`) |

- Jika memakai Cloudflare, mulai dengan **DNS only** (awan abu-abu) agar Caddy bisa membuat sertifikat HTTPS sendiri.
  Record `custom` wajib DNS only, karena domain organisasi diarahkan ke sana dan sertifikatnya dibuat Caddy.
- Cek: `dig +short api.orgspace.app` harus menampilkan IP VPS. Perubahan DNS bisa butuh beberapa menit.

## 2. Buka hanya port yang perlu (firewall)

```bash
sudo ufw allow OpenSSH        # jangan sampai terkunci dari ssh
sudo ufw allow 80/tcp         # HTTP: dipakai Caddy untuk verifikasi sertifikat dan dialihkan ke HTTPS
sudo ufw allow 443/tcp        # HTTPS
sudo ufw enable
sudo ufw status               # 8080, 3001, 5432 TIDAK boleh ada di daftar
```

Port 8080/3001/5432 sengaja tertutup dari internet: semua request wajib lewat Caddy.

## 3. Pasang Caddy

```bash
sudo apt install --yes debian-keyring debian-archive-keyring apt-transport-https curl
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | sudo tee /etc/apt/sources.list.d/caddy-stable.list
sudo chmod o+r /usr/share/keyrings/caddy-stable-archive-keyring.gpg
sudo chmod o+r /etc/apt/sources.list.d/caddy-stable.list
sudo apt update
sudo apt install caddy
```

| Perintah | Fungsi |
|---|---|
| `apt install ... curl` | alat yang dibutuhkan untuk menambah repository baru |
| `curl ... gpg.key \| gpg --dearmor` | kunci tanda tangan resmi Caddy, agar apt hanya menerima paket asli |
| `curl ... debian.deb.txt \| tee` | mendaftarkan repository resmi Caddy ke apt |
| `chmod o+r` | apt perlu bisa membaca kedua file itu |
| `apt update && apt install caddy` | memasang Caddy; paket ini langsung menjalankan service `caddy` (systemd) |

Cek: `caddy version` dan `systemctl status caddy` (harus `active (running)`).

## 4. Pasang Caddyfile OrgSpace

Dari repo (`docs/deploy/Caddyfile`) ke VPS, mis. dengan `scp` dari laptop:

```bash
scp docs/deploy/Caddyfile user@IP_VPS:/tmp/Caddyfile
```

Lalu di VPS:

```bash
sudo cp /etc/caddy/Caddyfile /etc/caddy/Caddyfile.bawaan   # cadangan file contoh bawaan paket
sudo mv /tmp/Caddyfile /etc/caddy/Caddyfile
```

## 5. Isi nama domain (variabel)

Caddyfile memakai `{$SITES_DOMAIN}` dan `{$API_DOMAIN}`. Nilainya disimpan di satu file:

```bash
sudo tee /etc/caddy/.env > /dev/null <<'EOF'
SITES_DOMAIN=orgspace.app
API_DOMAIN=api.orgspace.app
EOF
```

Tulis nilai **tanpa tanda kutip**. Lalu beri tahu service `caddy` agar membaca file itu:

```bash
sudo systemctl edit caddy
```

Di editor yang terbuka, tulis di antara komentar paling atas:

```
[Service]
EnvironmentFile=/etc/caddy/.env
```

Simpan dan tutup editor. Perubahan ini disimpan sebagai *override*, jadi tidak hilang saat paket Caddy diperbarui.

## 6. Periksa lalu jalankan

```bash
sudo caddy validate --config /etc/caddy/Caddyfile --envfile /etc/caddy/.env   # harus "Valid configuration"
sudo systemctl restart caddy                                                   # restart (bukan reload) karena EnvironmentFile baru
systemctl status caddy
```

- `--envfile` wajib saat `validate`: tanpa itu domain terbaca kosong dan pemeriksaan gagal atau menyesatkan.
- Setelah ini, untuk perubahan Caddyfile berikutnya cukup `validate` lalu `sudo systemctl reload caddy` (tanpa
  memutus pengunjung). Gunakan `restart` hanya jika isi `/etc/caddy/.env` berubah.

## 7. Uji dari laptop

```bash
curl -I https://api.orgspace.app/api/meta/categories   # 200, sertifikat sah
curl -I https://orgspace.app/                          # 307 ke portal
curl -I https://orgspace.app/NAMA-WEBSITE              # 200/308 untuk website yang sudah terbit
curl -I https://api.orgspace.app/sites/apa-saja        # 404 (jalur lama ditutup)
curl -I https://api.orgspace.app/internal/caddy/ask?domain=x   # 404 (hanya untuk Caddy)
```

Domain organisasi: setelah organisasi mengarahkan domainnya (CNAME ke `custom.orgspace.app` atau A ke IP VPS) dan
domain itu **aktif** di app, buka `https://domain-organisasi` di browser. Kunjungan pertama butuh beberapa detik
karena Caddy membuat sertifikat. Cek izin dari backend: di VPS,
`curl -s -o /dev/null -w '%{http_code}\n' "http://127.0.0.1:8080/internal/caddy/ask?domain=domain-organisasi"` harus 200.

## 8. Pengaturan program lain yang harus cocok

| Program | Variabel | Nilai |
|---|---|---|
| Backend (`backend/.env` prod) | `SITES_HOSTING_URL` | `https://orgspace.app` (link website yang dikirim ke app) |
| Backend | `SITES_CUSTOM_TARGET` | `custom.orgspace.app` |
| Backend | `SITES_PUBLIC_IP` | IP publik VPS |
| OrgSpace-Hosting (`.env.local`) | `BACKEND_API_URL` | `http://127.0.0.1:8080` (dipakai server Hosting, di dalam VPS) |
| OrgSpace-Hosting | `NEXT_PUBLIC_BACKEND_URL` | `https://api.orgspace.app` (dipakai **browser** di Portal Pengurus; ditanam saat `npm run build`) |
| OrgSpace-Hosting | `NEXT_PUBLIC_PORTAL_URL` | `https://web.orgspace.app` |
| App Android release | `API_BASE_URL` | `https://api.orgspace.app/api/` |

## 8a. Uji di laptop dulu (sebelum deploy)

Caddy bukan alat khusus production. Setiap kali Caddyfile berubah, jalankan dulu di laptop (backend :8080 dan
Hosting :3001 harus menyala; tanpa sudo):

```bash
CADDY=/path/ke/caddy docs/deploy/uji-caddy-lokal.sh sinergi-osis osis-sinergi.my.id
```

Skrip memakai salinan Caddyfile ini di port 8880/8443 dengan sertifikat lokal, lalu memeriksa arah request, jalur yang
ditutup, dan HTTPS on-demand domain kustom. Yang tetap hanya bisa diuji di VPS: sertifikat Let's Encrypt asli dan DNS.

## 9. Jika ada masalah

| Gejala | Periksa |
|---|---|
| `systemctl status caddy` gagal | `journalctl -u caddy --no-pager \| less +G` (baris paling bawah = error terbaru) |
| Sertifikat tidak terbit | DNS belum mengarah ke VPS (`dig +short`), port 80 tertutup, atau Cloudflare masih "Proxied" |
| `502 Bad Gateway` | program tujuan mati: cek `curl http://127.0.0.1:3001/` atau `:8080` dari dalam VPS |
| Portal Pengurus gagal masuk / "Failed to fetch" | `NEXT_PUBLIC_BACKEND_URL` Hosting belum `https://api...` (build ulang Hosting setelah mengubahnya) |
| Domain organisasi tidak dapat HTTPS | domain belum `active` di app, atau `ask` tidak menjawab 200 (langkah 7) |
| Domain terbaca kosong saat validate | `--envfile` lupa ditulis, atau `/etc/caddy/.env` memakai tanda kutip |
| Port 80/443 sudah dipakai | `sudo ss -ltnp \| grep -E ':(80\|443) '`, hentikan program itu (mis. `sudo systemctl disable --now nginx`) |
