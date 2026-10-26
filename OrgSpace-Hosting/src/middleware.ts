import { NextRequest, NextResponse } from "next/server";
import { BACKEND_API_URL, HOSTING_DOMAIN, PORTAL_URL } from "@/lib/config";

// Domain utama dari NEXT_PUBLIC_HOSTING_DOMAIN (bukan ditulis "orgspace.app"), plus alamat laptop.
const DEFAULT_HOSTS = new Set([
  "localhost:3001",
  "127.0.0.1:3001",
  HOSTING_DOMAIN,
  `www.${HOSTING_DOMAIN}`,
]);

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
    const cleanHost = host.toLowerCase().split(":")[0];
    
    // Cek mapping custom domain via backend
    try {
      const backendUrl = BACKEND_API_URL;
      const res = await fetch(
        `${backendUrl}/api/public/sites/resolve-domain?domain=${encodeURIComponent(cleanHost)}`,
        { cache: "no-store" }
      );
      if (res.ok) {
        const data = await res.json();
        if (data.slug) {
          // Link di halaman yang sudah memuat /{slug} tidak boleh jadi /{slug}/{slug}.
          const base = `/${data.slug}`;
          const target = pathname === base || pathname.startsWith(`${base}/`) ? pathname : `${base}${pathname}`;
          return NextResponse.rewrite(new URL(target, request.url));
        }
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
