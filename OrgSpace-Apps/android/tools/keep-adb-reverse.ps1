# Versi Windows (PowerShell) dari keep-adb-reverse.sh.
# Menjaga "adb reverse tcp:8080 tcp:8080" tetap aktif: dipasang ulang setiap 3 detik ke SETIAP perangkat
# yang tersambung, karena adb reverse hilang setiap koneksi ke HP putus-sambung (sering pada wireless debugging).
#
# Cara pakai (dari folder android):
#   powershell -ExecutionPolicy Bypass -File tools\keep-adb-reverse.ps1
# Hentikan dengan Ctrl+C.
while ($true) {
    adb devices | Select-Object -Skip 1 | ForEach-Object {
        $parts = $_ -split "\s+"
        if ($parts.Length -ge 2 -and $parts[1] -eq "device") {
            adb -s $parts[0] reverse tcp:8080 tcp:8080 | Out-Null
        }
    }
    Start-Sleep -Seconds 3
}
