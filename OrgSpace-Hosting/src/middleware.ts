import { NextRequest, NextResponse } from "next/server";

const DEFAULT_HOSTS = new Set([
  "localhost:3001",
  "127.0.0.1:3001",
  "orgspace.app",
  "www.orgspace.app",
]);

/**
 * Middleware Custom Domain. Link bawaan berbentuk path (orgspace.app/{slug}), jadi host selain
 * DEFAULT_HOSTS hanya dikenali sebagai domain kustom aktif lalu di-rewrite ke /{slug}.
 */
export async function middleware(request: NextRequest) {
  const host = request.headers.get("host") || "";
  const { pathname } = request.nextUrl;

  // Lewati static assets dan API internal Next.js
  if (
    pathname.startsWith("/_next") ||
    pathname.startsWith("/api") ||
    pathname.includes(".")
  ) {
    return NextResponse.next();
  }

  // Host lain: cek apakah domain kustom milik sebuah website
  if (host && !DEFAULT_HOSTS.has(host.toLowerCase())) {
    const cleanHost = host.toLowerCase().split(":")[0];
    
    // Cek mapping custom domain via backend
    try {
      const backendUrl = process.env.BACKEND_API_URL || "http://localhost:8080";
      const res = await fetch(
        `${backendUrl}/api/public/sites/resolve-domain?domain=${encodeURIComponent(cleanHost)}`,
        { cache: "no-store" }
      );
      if (res.ok) {
        const data = await res.json();
        if (data.slug) {
          return NextResponse.rewrite(
            new URL(`/${data.slug}${pathname === "/" ? "" : pathname}`, request.url)
          );
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
      const portalUrl = process.env.NEXT_PUBLIC_PORTAL_URL || "http://localhost:3000";
      return NextResponse.redirect(new URL(portalUrl), 307);
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
