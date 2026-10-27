import { NextRequest } from "next/server";
import { isTemplateId, notFoundPage, servePreviewAsset, servePreviewPage } from "@/lib/site-page";

/** Halaman lain dan file pendukung preview template (tentang.html, css/, img/). */
export async function GET(_request: NextRequest, ctx: RouteContext<"/demo/[id]/[...path]">) {
  const { id, path } = await ctx.params;
  if (!isTemplateId(id)) return notFoundPage();
  const file = path.join("/");
  if (file.endsWith("/") || !/\.[a-z0-9]+$/i.test(file)) return servePreviewPage(id, `${file.replace(/\/$/, "")}/index.html`);
  return /\.html?$/i.test(file) ? servePreviewPage(id, file) : servePreviewAsset(id, file);
}
