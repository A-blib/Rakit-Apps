"use client";

import { use } from "react";
import { PortalPengurus } from "@/components/PortalPengurus";
import { PUBLIC_BACKEND_URL } from "@/lib/config";

/** Portal Pengurus website asli: memanggil backend dengan token pengurus situs ini. */
export default function AdminPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = use(params);
  return (
    <PortalPengurus
      slug={slug}
      siteHref={`/${slug}`}
      api={(path, init) => fetch(`${PUBLIC_BACKEND_URL}/api/public/sites/${path}`, init)}
    />
  );
}
