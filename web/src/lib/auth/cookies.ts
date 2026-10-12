/**
 * Nama & aturan cookie sesi dashboard. Token disimpan di cookie httpOnly (tidak bisa dibaca JavaScript browser),
 * dikirim hanya ke server Next.js, lalu server yang memanggil backend dengan header Authorization.
 */
export const ACCESS_COOKIE = "os_access";
export const REFRESH_COOKIE = "os_refresh";

/** Umur cookie refresh = umur refresh token backend (JWT_REFRESH_TTL_DAYS, bawaan 30 hari). */
export const REFRESH_MAX_AGE_SECONDS = 30 * 24 * 60 * 60;

export function cookieOptions(maxAgeSeconds: number) {
  return {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax" as const,
    path: "/",
    maxAge: maxAgeSeconds,
  };
}

/**
 * Detik tersisa sebelum JWT kedaluwarsa (dibaca dari klaim `exp` tanpa verifikasi tanda tangan — hanya untuk
 * memutuskan kapan refresh; keabsahan token tetap diperiksa backend). Token rusak dianggap sudah kedaluwarsa.
 */
export function secondsUntilExpiry(token: string | undefined, nowMs = Date.now()): number {
  if (!token) {
    return -1;
  }
  try {
    const payload = JSON.parse(Buffer.from(token.split(".")[1], "base64url").toString("utf8")) as { exp?: number };
    return typeof payload.exp === "number" ? payload.exp - Math.floor(nowMs / 1000) : -1;
  } catch {
    return -1;
  }
}
