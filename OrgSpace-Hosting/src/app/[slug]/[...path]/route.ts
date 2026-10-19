import { NextRequest } from "next/server";
import { serveAsset, serveMedia, servePage } from "@/lib/site-page";

/**
 * Halaman lain (tentang.html), file pendukung template (css/, js/, img/), dan foto modul dari Portal Pengurus
 * (_media/{id}). /{slug}/admin ditangani halaman Portal.
 */
export async function GET(_request: NextRequest, ctx: RouteContext<"/[slug]/[...path]">) {
  const { slug, path } = await ctx.params;
  if (path.length === 2 && path[0] === "_media") return serveMedia(slug, path[1]);
  const file = path.join("/");
  if (file.endsWith("/") || !/\.[a-z0-9]+$/i.test(file)) return servePage(slug, `${file.replace(/\/$/, "")}/index.html`);
  return /\.html?$/i.test(file) ? servePage(slug, file) : serveAsset(slug, file);
}
