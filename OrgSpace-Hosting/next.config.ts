import type { NextConfig } from "next";
import { REQUIRED_IN_PRODUCTION } from "./src/lib/config";

// Build production (`npm run build`) berhenti jika alamat belum diisi, daripada tayang sambil memanggil localhost.
// Variabel NEXT_PUBLIC_* ditanam saat build, jadi mengisinya setelah build tidak menolong.
if (process.env.NODE_ENV === "production") {
  const missing = REQUIRED_IN_PRODUCTION.filter((name) => !process.env[name]);
  if (missing.length > 0) {
    throw new Error(`Environment production belum lengkap: ${missing.join(", ")}. Isi .env.production.local (Panduan Hosting bab 5.3).`);
  }
}

const nextConfig: NextConfig = {
  // Website user disajikan di /{slug}/ (dengan "/") agar path relatif template (css/, tentang.html) benar.
  skipTrailingSlashRedirect: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
