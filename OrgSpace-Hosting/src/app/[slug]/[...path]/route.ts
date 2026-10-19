import { NextRequest } from "next/server";
import { serveAsset, servePage } from "@/lib/site-page";

/** Halaman lain (tentang.html) dan file pendukung template (css/, js/, img/). /{slug}/admin ditangani halaman Portal. */
export async function GET(_request: NextRequest, ctx: RouteContext<"/[slug]/[...path]">) {
  const { slug, path } = await ctx.params;
  const file = path.join("/");
  if (file.endsWith("/") || !/\.[a-z0-9]+$/i.test(file)) return servePage(slug, `${file.replace(/\/$/, "")}/index.html`);
  return /\.html?$/i.test(file) ? servePage(slug, file) : serveAsset(slug, file);
}
