#!/usr/bin/env bash
# Menjaga "adb reverse tcp:8080 tcp:8080" tetap aktif selama mengembangkan app.
#
# Kenapa perlu? adb reverse hilang setiap kali koneksi adb ke HP putus-sambung (sering terjadi pada
# wireless debugging saat layar HP mati atau Wi-Fi hemat daya). Akibatnya app menampilkan
# "Tidak ada koneksi" walaupun internet HP lancar. Skrip ini memasangnya ulang setiap 3 detik
# ke SETIAP perangkat yang tersambung (HP bisa muncul dua kali: lewat kabel USB dan lewat Wi-Fi;
# "adb reverse" tanpa -s ditolak jika ada lebih dari satu).
#
# Cara pakai (dari folder android/):  ./tools/keep-adb-reverse.sh      → hentikan dengan Ctrl+C
# Windows: lihat docs/panduan-kolaborator.md (versi PowerShell).
while true; do
  for serial in $(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }'); do
    adb -s "$serial" reverse tcp:8080 tcp:8080 >/dev/null 2>&1
  done
  sleep 3
done
