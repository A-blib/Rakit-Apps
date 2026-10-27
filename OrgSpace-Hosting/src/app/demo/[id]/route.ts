import { NextRequest, NextResponse } from "next/server";
import { isTemplateId, notFoundPage, servePreviewPage } from "@/lib/site-page";

/** Beranda preview template galeri. "/" di akhir agar path relatif template (css/, img/) benar. */
export async function GET(request: NextRequest, ctx: RouteContext<"/demo/[id]">) {
  const { id } = await ctx.params;
  if (!isTemplateId(id)) return notFoundPage();
  const url = new URL(request.url);
  if (!url.pathname.endsWith("/")) {
    url.pathname += "/";
    return NextResponse.redirect(url, 308);
  }
  return servePreviewPage(id, "index.html");
}
