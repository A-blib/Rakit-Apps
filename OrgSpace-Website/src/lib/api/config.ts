/**
 * Alamat backend. Hanya dibaca di server Next.js (Server Component, Server Action, proxy) — browser tidak pernah
 * memanggil backend langsung, jadi token admin tidak pernah tersimpan di JavaScript browser.
 */
export const API_URL = (process.env.ORGSPACE_API_URL ?? "http://localhost:8080/api").replace(/\/$/, "");

/** Header agar backend mencatat request ini sebagai platform "web" di statistik pengguna aktif. */
export const CLIENT_HEADER = { "X-OrgSpace-Client": "web" } as const;
