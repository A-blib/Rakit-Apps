/**
 * Semua alamat dari environment, di satu tempat. Nilai localhost di sini hanya untuk laptop (`npm run dev`);
 * `next build` production menolak jalan jika variabelnya kosong (lihat next.config.ts), agar Hosting tidak tayang
 * sambil diam-diam memanggil localhost.
 */
const orDev = (value: string | undefined, devValue: string) => (value || devValue).replace(/\/+$/, "");

/** Backend untuk kode yang berjalan di server Hosting (di VPS: http://127.0.0.1:8080, tidak lewat internet). */
export const BACKEND_API_URL = orDev(process.env.BACKEND_API_URL, "http://localhost:8080");

/** Backend untuk kode yang berjalan di browser (Portal Pengurus): alamat publik, mis. https://api.orgspace.app. */
export const PUBLIC_BACKEND_URL = orDev(process.env.NEXT_PUBLIC_BACKEND_URL, "http://localhost:8080");

/** Portal web OrgSpace; tujuan saat domain Hosting dibuka tanpa slug. */
export const PORTAL_URL = orDev(process.env.NEXT_PUBLIC_PORTAL_URL, "http://localhost:3000");

/** Domain utama Hosting (mis. orgspace.app); host lain dianggap domain kustom organisasi. */
export const HOSTING_DOMAIN = (process.env.NEXT_PUBLIC_HOSTING_DOMAIN || "localhost:3001").toLowerCase();

/** Wajib diisi saat build production; dibaca next.config.ts. */
export const REQUIRED_IN_PRODUCTION = [
  "BACKEND_API_URL",
  "NEXT_PUBLIC_BACKEND_URL",
  "NEXT_PUBLIC_PORTAL_URL",
  "NEXT_PUBLIC_HOSTING_DOMAIN",
] as const;
