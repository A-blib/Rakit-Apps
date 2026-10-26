import { SiteRenderData } from "./types";
import { BACKEND_API_URL as BACKEND_URL } from "./config";

/**
 * Mengambil data publik website berdasarkan slug untuk di-render.
 * Menggunakan cache: 'no-store' agar perubahan langsung tayang (sinkronisasi instan).
 */
export async function getSiteRenderData(slug: string): Promise<SiteRenderData | null> {
  try {
    const cleanSlug = encodeURIComponent(slug.trim().toLowerCase());
    const res = await fetch(`${BACKEND_URL}/api/public/sites/render/${cleanSlug}`, {
      cache: "no-store",
      headers: {
        Accept: "application/json",
      },
    });

    if (!res.ok) {
      if (res.status === 404 || res.status === 400) {
        return null;
      }
      console.error(`Gagal memuat situs '${slug}': HTTP ${res.status}`);
      return null;
    }

    return (await res.json()) as SiteRenderData;
  } catch (err) {
    console.error(`Kesalahan koneksi saat memuat situs '${slug}':`, err);
    return null;
  }
}

/**
 * Menyelesaikan custom domain pengguna ke slug website OrgSpace.
 */
export async function resolveCustomDomain(domain: string): Promise<string | null> {
  try {
    const res = await fetch(
      `${BACKEND_URL}/api/public/sites/resolve-domain?domain=${encodeURIComponent(domain)}`,
      { cache: "no-store" }
    );
    if (!res.ok) return null;
    const data = await res.json();
    return data.slug && data.slug.length > 0 ? data.slug : null;
  } catch (err) {
    console.error("Gagal menyelesaikan domain:", err);
    return null;
  }
}

/**
 * Mengirim isian formulir publik ke backend Spring Boot.
 * Di browser, memanggil route proxy internal same-origin agar aman tanpa masalah CORS.
 */
export async function submitSiteForm(
  slug: string,
  formName: string,
  formData: Record<string, any>
): Promise<{ success: boolean; message: string }> {
  const cleanSlug = encodeURIComponent(slug);
  const cleanForm = encodeURIComponent(formName);

  const url =
    typeof window !== "undefined"
      ? `/api/forms/${cleanSlug}/${cleanForm}`
      : `${BACKEND_URL}/api/public/sites/${cleanSlug}/forms/${cleanForm}`;

  const res = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(formData),
  });

  const json = await res.json();
  if (!res.ok) {
    throw new Error(json.message || "Gagal mengirim formulir.");
  }

  return json;
}
