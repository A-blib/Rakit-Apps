import { NextRequest, NextResponse } from "next/server";
import { BACKEND_API_URL, HOSTING_DOMAIN, PORTAL_URL } from "@/lib/config";

// Domain utama dari NEXT_PUBLIC_HOSTING_DOMAIN (bukan ditulis "orgspace.app"), plus alamat laptop.
const DEFAULT_HOSTS = new Set([
  "localhost:3001",
  "127.0.0.1:3001",
  HOSTING_DOMAIN,
  `www.${HOSTING_DOMAIN}`,
]);

// Hasil cek domain kustom disimpan sebentar: satu halaman memuat puluhan file (css, js, gambar) dari domain yang
// sama, dan tanpa ini setiap file menanyai backend. Domain yang baru aktif/dilepas terlihat paling lambat 60 detik.
const DOMAIN_TTL_MS = 60_000;
const DOMAIN_CACHE_MAX = 1000;
const domainCache = new Map<string, { slug: string | null; until: number }>();

async function slugForDomain(host: string): Promise<string | null> {
  const now = Date.now();
  const hit = domainCache.get(host);
  if (hit && hit.until > now) return hit.slug;
  const res = await fetch(`${BACKEND_API_URL}/api/public/sites/resolve-domain?domain=${encodeURIComponent(host)}`, {
    cache: "no-store",
  });
  // Gangguan backend tidak disimpan, agar domain tidak "hilang" selama TTL karena error sesaat.
  if (!res.ok) return null;
  const data = await res.json();
  const slug: string | null = data.slug || null;
  if (domainCache.size >= DOMAIN_CACHE_MAX) domainCache.clear();
  domainCache.set(host, { slug, until: now + DOMAIN_TTL_MS });
  return slug;
}

/**
 * Middleware Custom Domain. Link bawaan berbentuk path (orgspace.app/{slug}), jadi host selain
 * DEFAULT_HOSTS hanya dikenali sebagai domain kustom aktif lalu di-rewrite ke /{slug}.
 */
export async function middleware(request: NextRequest) {
  const host = request.headers.get("host") || "";
  const { pathname } = request.nextUrl;

  // Aset Next.js dan API internal (mis. kiriman form) tidak pernah milik website user.
  if (pathname.startsWith("/_next") || pathname.startsWith("/api")) {
    return NextResponse.next();
  }

  // Host lain: cek apakah domain kustom milik sebuah website. File template (css/, img/) ikut diarahkan.
  if (host && !DEFAULT_HOSTS.has(host.toLowerCase())) {
    // Titik di akhir (osis.id.) adalah bentuk sah nama domain di DNS.
    const cleanHost = host.toLowerCase().split(":")[0].replace(/\.$/, "");

    try {
      const slug = await slugForDomain(cleanHost);
      if (slug) {
        // Link di halaman yang sudah memuat /{slug} tidak boleh jadi /{slug}/{slug}.
        const base = `/${slug}`;
        const target = pathname === base || pathname.startsWith(`${base}/`) ? pathname : `${base}${pathname}`;
        return NextResponse.rewrite(new URL(target, request.url));
      }
    } catch (e) {
      console.error("Gagal memeriksa custom domain:", e);
    }
  }

  // Jika root hosting (orgspace.app / localhost:3001) diakses tanpa slug:
  // Redirect otomatis ke Portal Utama (OrgSpace-Website), persis seperti perilaku vercel.app -> vercel.com.
  // Sertakan opsi ?info=true jika pengembang ingin melihat status gateway.
  if (pathname === "/") {
    const isInfoRequested = request.nextUrl.searchParams.get("info") === "true";
    if (!isInfoRequested) {
      const portalUrl = PORTAL_URL;
      return NextResponse.redirect(new URL(portalUrl), 307);
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
