import { NextRequest, NextResponse } from "next/server";
import { servePage } from "@/lib/site-page";

/** Beranda website user. Alamat diberi "/" di akhir agar path relatif template (css/, img/, tentang.html) benar. */
export async function GET(request: NextRequest, ctx: RouteContext<"/[slug]">) {
  const { slug } = await ctx.params;
  // URL biasa, bukan request.nextUrl: nextUrl membuang "/" di akhir saat diubah menjadi teks.
  const url = new URL(request.url);
  if (!url.pathname.endsWith("/")) {
    url.pathname += "/";
    return NextResponse.redirect(url, 308);
  }
  return servePage(slug, "index.html");
}
