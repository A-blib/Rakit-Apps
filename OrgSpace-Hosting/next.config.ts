import type { NextConfig } from "next";

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
