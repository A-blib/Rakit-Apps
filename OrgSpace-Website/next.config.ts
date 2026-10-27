import type { NextConfig } from "next";

// Build production (Vercel) berhenti jika alamat backend belum diisi, daripada tayang sambil memanggil localhost.
if (process.env.NODE_ENV === "production" && !process.env.ORGSPACE_API_URL) {
  throw new Error("Environment production belum lengkap: ORGSPACE_API_URL. Isi di Vercel → Settings → Environment Variables (Panduan Hosting bab 8).");
}

// Cache Components (bawaan create-next-app 16) sengaja tidak dinyalakan: pola fetch + `revalidate` biasa lebih mudah
// dipahami tim. Nyalakan hanya setelah dibahas dengan Aris (lihat AGENTS.md).
const nextConfig: NextConfig = {
  experimental: {
    // Form hero di dashboard mengirim gambar maks 2 MB (batas backend) lewat Server Action; bawaannya hanya 1 MB.
    serverActions: { bodySizeLimit: "3mb" },
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
