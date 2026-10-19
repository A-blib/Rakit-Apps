import { getSiteRenderData } from "./api";
import { MODULE_RUNTIME, STORAGE_SHIM } from "./site-runtime";

const BACKEND_URL = process.env.BACKEND_API_URL || "http://localhost:8080";

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

function notFound(): Response {
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

/** Halaman HTML template (index.html, tentang.html, ...) beserta modul dinamisnya. */
export async function servePage(slug: string, file: string): Promise<Response> {
  const site = await getSiteRenderData(slug);
  if (!site || site.status !== "online") return notFound();
  // Mode rakit sendiri dirender React (CustomRenderer), bukan file template.
  if (site.mode === "assembled") return new Response(null, { status: 307, headers: { Location: `/rakit/${slug}` } });

  const res = await fetch(assetUrl(slug, file), { cache: "no-store" });
  if (!res.ok) return notFound();
  let html = await res.text();

  const data = {
    slug: site.slug,
    news: (site.news || []).map((n) => ({
      judul: n.title,
      ringkasan: n.summary,
      konten: n.content,
      tanggal: n.publishedAt,
      foto: n.imageUrl,
      detail: `#berita-${n.id}`,
    })),
    collections: site.collections || {},
  };

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
    },
  });
}

/** Foto modul yang diunggah lewat Portal Pengurus. Isinya tidak pernah berubah (foto baru = id baru). */
export async function serveMedia(slug: string, id: string): Promise<Response> {
  if (!/^[0-9a-f-]{36}$/i.test(id)) return notFound();
  const res = await fetch(`${BACKEND_URL}/api/public/sites/${encodeURIComponent(slug)}/media/${id}`, { cache: "no-store" });
  if (!res.ok || !res.body) return notFound();
  return new Response(res.body, {
    headers: {
      "Content-Type": res.headers.get("content-type") || "application/octet-stream",
      "Content-Security-Policy": SANDBOX,
      "Cache-Control": "public, max-age=31536000, immutable",
      "X-Content-Type-Options": "nosniff",
    },
  });
}

/** File pendukung template (CSS, JS, gambar, font) apa adanya. SVG bisa berisi script, jadi ikut di-sandbox. */
export async function serveAsset(slug: string, path: string): Promise<Response> {
  const res = await fetch(assetUrl(slug, path), { cache: "no-store" });
  if (!res.ok || !res.body) return notFound();
  return new Response(res.body, {
    headers: {
      "Content-Type": res.headers.get("content-type") || "application/octet-stream",
      "Content-Security-Policy": SANDBOX,
      "Cache-Control": "public, max-age=300",
      "X-Content-Type-Options": "nosniff",
    },
  });
}
