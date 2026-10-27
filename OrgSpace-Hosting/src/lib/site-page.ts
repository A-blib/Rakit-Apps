import { getSiteRenderData } from "./api";
import type { NewsItem, TemplatePreviewData } from "./types";
import { MODULE_RUNTIME, STORAGE_SHIM } from "./site-runtime";
import { BACKEND_API_URL as BACKEND_URL } from "./config";

/**
 * Semua website user tayang di satu domain (orgspace.app/{slug}). Header sandbox membuat browser menjalankan halaman
 * sebagai origin anonim: JavaScript template tetap jalan, tapi tidak bisa membaca cookie/storage orgspace.app,
 * Portal Pengurus, atau website lain. Tanpa allow-same-origin, sengaja.
 */
const SANDBOX =
  "sandbox allow-scripts allow-forms allow-popups allow-popups-to-escape-sandbox allow-modals allow-downloads";

const NOT_FOUND_HTML = `<!doctype html><html lang="id"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1"><title>Website tidak ditemukan • OrgSpace</title>
<meta name="robots" content="noindex"></head>
<body style="font-family:system-ui,sans-serif;display:grid;place-items:center;min-height:100vh;margin:0;padding:24px;text-align:center">
<div><h1>Website belum tayang</h1><p>Alamat ini belum diterbitkan atau sedang dinonaktifkan oleh pemiliknya.</p></div>
</body></html>`;

/** Id template galeri (UUID); dicek sebelum dipakai di URL backend. */
export const isTemplateId = (id: string) => /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id);

export function notFoundPage(): Response {
  return new Response(NOT_FOUND_HTML, {
    status: 404,
    headers: { "Content-Type": "text/html; charset=utf-8", "Content-Security-Policy": SANDBOX },
  });
}

/** JSON yang aman ditanam di dalam <script>: "<" di-escape agar isi data tidak bisa menutup tag script. */
function embedJson(value: unknown): string {
  return JSON.stringify(value).replace(/</g, "\\u003c");
}

function assetUrl(slug: string, path: string): string {
  const encoded = path.split("/").map(encodeURIComponent).join("/");
  return `${BACKEND_URL}/api/public/sites/${encodeURIComponent(slug)}/assets/${encoded}`;
}

/** Berita dalam nama field modul (judul, ringkasan, ...) yang dipakai template. */
function moduleNews(news: NewsItem[]) {
  return news.map((n) => ({
    judul: n.title,
    ringkasan: n.summary,
    konten: n.content,
    tanggal: n.publishedAt,
    foto: n.imageUrl,
    detail: `#berita-${n.id}`,
  }));
}

/** Halaman template + runtime modul, selalu dengan header sandbox. Dipakai website terbit dan preview template. */
function pageResponse(html: string, data: unknown, extraHeaders: Record<string, string> = {}): Response {
  const head = `<script>${STORAGE_SHIM}</script>`;
  html = /<head[^>]*>/i.test(html) ? html.replace(/<head[^>]*>/i, (tag) => tag + head) : head + html;
  const tail = `<script type="application/json" id="orgspace-data">${embedJson(data)}</script><script>${MODULE_RUNTIME}</script>`;
  html = /<\/body>/i.test(html) ? html.replace(/<\/body>(?![\s\S]*<\/body>)/i, () => tail + "</body>") : html + tail;

  return new Response(html, {
    headers: {
      "Content-Type": "text/html; charset=utf-8",
      "Content-Security-Policy": SANDBOX,
      "Cache-Control": "no-store",
      "X-Content-Type-Options": "nosniff",
      ...extraHeaders,
    },
  });
}

/** File pendukung apa adanya. SVG bisa berisi script, jadi ikut di-sandbox. */
async function fileResponse(source: string, cacheControl: string): Promise<Response> {
  const res = await fetch(source, { cache: "no-store" });
  if (!res.ok || !res.body) return notFoundPage();
  return new Response(res.body, {
    headers: {
      "Content-Type": res.headers.get("content-type") || "application/octet-stream",
      "Content-Security-Policy": SANDBOX,
      "Cache-Control": cacheControl,
      "X-Content-Type-Options": "nosniff",
      // Halaman sandbox ber-origin anonim: font (@font-face) dan script modul dari situ wajib lolos CORS.
      // File template memang publik dan tanpa cookie, jadi aman dibuka untuk semua origin.
      "Access-Control-Allow-Origin": "*",
    },
  });
}

/** Halaman HTML template (index.html, tentang.html, ...) beserta modul dinamisnya. */
export async function servePage(slug: string, file: string): Promise<Response> {
  const site = await getSiteRenderData(slug);
  if (!site || site.status !== "online") return notFoundPage();
  // Mode rakit sendiri dirender React (CustomRenderer), bukan file template.
  if (site.mode === "assembled") return new Response(null, { status: 307, headers: { Location: `/rakit/${slug}` } });

  const res = await fetch(assetUrl(slug, file), { cache: "no-store" });
  if (!res.ok) return notFoundPage();
  const data = {
    slug: site.slug,
    news: moduleNews(site.news || []),
    collections: site.collections || {},
    stats: site.stats || {},
    org: site.org || {},
  };

  return pageResponse(await res.text(), data);
}

/**
 * Foto modul dari Portal Pengurus dan foto yang diganti di editor (isinya tidak pernah berubah: foto baru = id baru),
 * serta logo organisasi (_media/logo).
 */
export async function serveMedia(slug: string, id: string): Promise<Response> {
  let source: string;
  if (id === "logo") {
    // Logo organisasi bisa diganti kapan saja, jadi tidak boleh disimpan lama seperti foto modul.
    const site = await getSiteRenderData(slug);
    if (!site || site.status !== "online") return notFoundPage();
    source = `${BACKEND_URL}/api/organizations/${site.organizationId}/logo`;
  } else if (/^[0-9a-f-]{36}$/i.test(id)) {
    source = `${BACKEND_URL}/api/public/sites/${encodeURIComponent(slug)}/media/${id}`;
  } else {
    return notFoundPage();
  }
  const res = await fetch(source, { cache: "no-store" });
  if (!res.ok || !res.body) return notFoundPage();
  return new Response(res.body, {
    headers: {
      "Content-Type": res.headers.get("content-type") || "application/octet-stream",
      "Content-Security-Policy": SANDBOX,
      "Cache-Control": id === "logo" ? "public, max-age=300" : "public, max-age=31536000, immutable",
      "X-Content-Type-Options": "nosniff",
    },
  });
}

/** File pendukung template (CSS, JS, gambar, font). */
export async function serveAsset(slug: string, path: string): Promise<Response> {
  return fileResponse(assetUrl(slug, path), "public, max-age=300");
}

function previewFileUrl(templateId: string, path: string): string {
  const encoded = path.split("/").map(encodeURIComponent).join("/");
  return `${BACKEND_URL}/api/public/templates/${encodeURIComponent(templateId)}/files/${encoded}`;
}

/**
 * Preview template dari galeri (/demo/{id}/) dengan data contoh dari backend. Sandbox-nya sama dengan website
 * terbit, jadi JavaScript template tidak bisa membaca token Portal Pengurus di origin Hosting. Form tidak dikirim.
 */
export async function servePreviewPage(templateId: string, file: string): Promise<Response> {
  const res = await fetch(`${BACKEND_URL}/api/public/templates/${encodeURIComponent(templateId)}/preview`, { cache: "no-store" });
  if (!res.ok) return notFoundPage();
  const sample = (await res.json()) as TemplatePreviewData;
  const page = await fetch(previewFileUrl(templateId, file), { cache: "no-store" });
  if (!page.ok) return notFoundPage();

  const data = {
    slug: `demo/${templateId}`,
    preview: true,
    news: moduleNews(sample.news),
    collections: sample.collections,
    stats: sample.stats,
    org: sample.org,
  };
  return pageResponse(await page.text(), data, { "X-Robots-Tag": "noindex" });
}

export async function servePreviewAsset(templateId: string, path: string): Promise<Response> {
  return fileResponse(previewFileUrl(templateId, path), "public, max-age=300");
}
