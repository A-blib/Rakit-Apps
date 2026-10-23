import type { NextConfig } from "next";

// Cache Components (bawaan create-next-app 16) sengaja tidak dinyalakan: pola fetch + `revalidate` biasa lebih mudah
// dipahami tim. Nyalakan hanya setelah dibahas dengan Aris (lihat AGENTS.md).
const nextConfig: NextConfig = {
  experimental: {
    // Jalur upgrade otomatis dibatasi ke rilis perbaikan keamanan saja.
    agentUpgrade: "security",
  },
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
