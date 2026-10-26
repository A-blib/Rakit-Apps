#!/usr/bin/env bash
# Menguji Caddyfile production (docs/deploy/Caddyfile) di laptop sebelum deploy: arah request, jalur yang ditutup,
# dan HTTPS on-demand domain kustom. Linux/macOS (Windows: Git Bash atau WSL).
#
# Syarat sebelum menjalankan:
#   - backend jalan di :8080   (cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev)
#   - OrgSpace-Hosting jalan di :3001 (cd OrgSpace-Hosting && npm run dev)
#   - program caddy: di PATH, atau CADDY=/path/ke/caddy (rilis resmi github.com/caddyserver/caddy/releases)
# Tidak butuh sudo: port 8880/8443 dan sertifikat lokal Caddy (tidak dipasang ke sistem).
#
# Pakai:  docs/deploy/uji-caddy-lokal.sh <slug-website-online> [domain-kustom-aktif]
# Contoh: docs/deploy/uji-caddy-lokal.sh sinergi-osis osis-sinergi.my.id
set -euo pipefail

SLUG=${1:?"Isi slug website yang online, mis. sinergi-osis"}
DOMAIN=${2:-}
CADDY=${CADDY:-caddy}
HERE=$(cd "$(dirname "$0")" && pwd)
WORK=$(mktemp -d)
PID=""
trap '[ -n "$PID" ] && kill "$PID" 2>/dev/null; rm -rf "$WORK"' EXIT

# Salinan Caddyfile production + pengaturan khusus laptop di blok global (aturan routing tidak diubah).
awk -v data="$WORK/data" '{print} /^\{$/ && !done {
  print "\thttp_port 8880"; print "\thttps_port 8443"; print "\tlocal_certs"; print "\tskip_install_trust"
  print "\tadmin localhost:2029"; print "\tstorage file_system " data; done=1 }' "$HERE/Caddyfile" > "$WORK/Caddyfile"
printf 'SITES_DOMAIN=orgspace.localhost\nAPI_DOMAIN=api.orgspace.localhost\n' > "$WORK/env"

"$CADDY" validate --config "$WORK/Caddyfile" --envfile "$WORK/env" > /dev/null 2>&1 \
  || { echo "GAGAL: Caddyfile tidak valid"; "$CADDY" validate --config "$WORK/Caddyfile" --envfile "$WORK/env"; exit 1; }
"$CADDY" run --config "$WORK/Caddyfile" --envfile "$WORK/env" > "$WORK/caddy.log" 2>&1 &
PID=$!

R=(--resolve orgspace.localhost:8443:127.0.0.1 --resolve api.orgspace.localhost:8443:127.0.0.1
   --resolve domain-asing.invalid:8443:127.0.0.1)
[ -n "$DOMAIN" ] && R+=(--resolve "$DOMAIN:8443:127.0.0.1")
for _ in $(seq 1 30); do curl -sk "${R[@]}" -o /dev/null https://api.orgspace.localhost:8443/ && break; sleep 1; done

status() { curl -sk "${R[@]}" -o /dev/null -w '%{http_code}' "$1" || true; }
FAILED=0
check() { # check <nama> <harapan> <hasil>
  if [ "$2" = "$3" ]; then echo "OK    $1 ($3)"; else echo "GAGAL $1: harapan $2, hasil $3"; FAILED=1; fi
}

check "API di api.orgspace.localhost"          200 "$(status https://api.orgspace.localhost:8443/api/meta/categories)"
check "/sites/* ditutup di domain API"          404 "$(status https://api.orgspace.localhost:8443/sites/$SLUG/index.html)"
check "/internal/* ditutup di domain API"       404 "$(status "https://api.orgspace.localhost:8443/internal/caddy/ask?domain=x")"
check "website orgspace.localhost/$SLUG/"       200 "$(status https://orgspace.localhost:8443/$SLUG/)"
check "orgspace.localhost/ dialihkan ke portal" 307 "$(status https://orgspace.localhost:8443/)"
check "isi website lewat Caddy = langsung Hosting" sama \
  "$([ "$(curl -sk "${R[@]}" https://orgspace.localhost:8443/$SLUG/ | md5sum)" = "$(curl -s localhost:3001/$SLUG/ | md5sum)" ] && echo sama || echo beda)"
check "domain tak terdaftar ditolak (ask)"      000 "$(status https://domain-asing.invalid:8443/)"
if [ -n "$DOMAIN" ]; then
  check "domain kustom $DOMAIN"                 200 "$(status "https://$DOMAIN:8443/")"
  check "isi domain kustom = website $SLUG" sama \
    "$([ "$(curl -sk "${R[@]}" "https://$DOMAIN:8443/" | md5sum)" = "$(curl -sk "${R[@]}" https://orgspace.localhost:8443/$SLUG/ | md5sum)" ] && echo sama || echo beda)"
fi

[ "$FAILED" = 0 ] && echo "Semua uji Caddy lolos." || { echo "Ada uji yang gagal. Log Caddy: $WORK/caddy.log"; trap - EXIT; kill "$PID"; exit 1; }
