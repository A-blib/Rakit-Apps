"use client";

import { use } from "react";
import { PortalPengurus } from "@/components/PortalPengurus";
import { PUBLIC_BACKEND_URL } from "@/lib/config";

/** Portal Pengurus website asli: memanggil backend dengan token pengurus situs ini. */
export default function AdminPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = use(params);

  const getApiEndpoint = (path: string) => {
    // Jika di browser dengan protokol HTTPS dan backend masih HTTP murni (belum ada SSL custom domain backend),
    // gunakan path relatif (/api/public/sites/...) agar melalui internal proxy Hosting dan tidak diblokir Mixed Content oleh browser.
    if (typeof window !== "undefined" && window.location.protocol === "https:" && PUBLIC_BACKEND_URL.startsWith("http://")) {
      return `/api/public/sites/${path}`;
    }
    return `${PUBLIC_BACKEND_URL}/api/public/sites/${path}`;
  };

  return (
    <PortalPengurus
      slug={slug}
      siteHref={`/${slug}`}
      api={(path, init) => fetch(getApiEndpoint(path), init)}
    />
  );
}

