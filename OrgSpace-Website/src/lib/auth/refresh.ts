import { API_URL, CLIENT_HEADER } from "@/lib/api/config";
import type { AuthResponse } from "@/lib/api/types";

/**
 * Refresh token backend sekali pakai (dirotasi). Jika dua request memakai refresh token yang SAMA, backend
 * menganggapnya pencurian dan mencabut semua sesi user. Karena itu refresh yang sedang berjalan untuk token yang sama
 * dipakai bersama (satu proses server Next.js).
 *
 * Batasan: jika nanti web dijalankan di beberapa server sekaligus, dibutuhkan sticky session atau penyimpanan
 * bersama — bahas dengan Aris sebelum deploy seperti itu.
 */
const inFlight = new Map<string, Promise<AuthResponse | null>>();

export function refreshTokens(refreshToken: string): Promise<AuthResponse | null> {
  const running = inFlight.get(refreshToken);
  if (running) {
    return running;
  }
  const promise = (async () => {
    try {
      const response = await fetch(`${API_URL}/auth/refresh`, {
        method: "POST",
        headers: { ...CLIENT_HEADER, "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
        cache: "no-store",
      });
      return response.ok ? ((await response.json()) as AuthResponse) : null;
    } catch {
      return null;
    } finally {
      // Hasil disimpan sebentar agar request yang datang sedikit terlambat tetap memakai hasil yang sama.
      setTimeout(() => inFlight.delete(refreshToken), 10_000);
    }
  })();
  inFlight.set(refreshToken, promise);
  return promise;
}
