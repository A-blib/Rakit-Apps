import { apiFetch } from "./http";
import { API_URL, CLIENT_HEADER } from "./config";
import type { DocDetail, DocTree, GalleryTemplate, LandingHero, PageResponse, PortalUpdate, PublicOverview } from "./types";

/** Endpoint tanpa login: landing page & pusat dokumentasi. Di-cache Next.js beberapa menit. */

import { FALLBACK_DOC_ARTICLES, FALLBACK_DOC_TREE, mergeDocTree } from "../docs-data";

export function getOverview() {
  return apiFetch<PublicOverview>("/public/overview", { revalidate: 300 });
}

/** Isi hero saat backend mati: sama dengan nilai awal di backend (V33__create_landing_hero.sql). */
const FALLBACK_HERO: LandingHero = {
  headline: "Bikin Website Impian,",
  headlineHighlight: "Tanpa Coding!",
  subheadline:
    "Dari UMKM, organisasi, hingga portofolio pribadi—rakit website-mu dalam hitungan menit dengan mudah, cepat, dan profesional.",
  primaryCtaLabel: "Mulai Rakit Sekarang – Gratis!",
  secondaryCtaLabel: "Lihat Demo",
  secondaryCtaUrl: "/docs",
  highlights: ["Tanpa Kartu Kredit", "Siap Online 5 Menit", "Hosting & SSL Otomatis"],
  imageAlt: "Mockup Workspace Rakit Website di Meja Kerja dengan Laptop dan Sticky Notes",
  imageUpdatedAt: null,
  updatedAt: "1970-01-01T00:00:00Z",
};

export async function getLandingHero(): Promise<LandingHero> {
  try {
    return await apiFetch<LandingHero>("/public/landing/hero", { revalidate: 60 });
  } catch {
    return FALLBACK_HERO;
  }
}

/** Gambar hero unggahan (PNG) apa adanya, untuk diteruskan rute /hero-image; browser tidak memanggil backend. */
export function getHeroImage() {
  // no-store: cache data Next.js menolak item > 2 MB; cache browser diatur rute /hero-image (URL membawa ?v=).
  return fetch(`${API_URL}/public/landing/hero/image`, { headers: CLIENT_HEADER, cache: "no-store" });
}

export async function getDocTree(): Promise<DocTree> {
  try {
    const serverTree = await apiFetch<DocTree>("/public/docs", { revalidate: 60 });
    return mergeDocTree(serverTree);
  } catch {
    return FALLBACK_DOC_TREE;
  }
}

export async function getDoc(slug: string): Promise<DocDetail> {
  try {
    return await apiFetch<DocDetail>(`/public/docs/${encodeURIComponent(slug)}`, { revalidate: 60 });
  } catch (error) {
    if (FALLBACK_DOC_ARTICLES[slug]) {
      return FALLBACK_DOC_ARTICLES[slug];
    }
    throw error;
  }
}

/** Template unggulan pilihan admin untuk landing page (maks 12, urutan pilihan admin). Backend mati = kosong. */
export async function getLandingTemplates(): Promise<GalleryTemplate[]> {
  try {
    return await apiFetch<GalleryTemplate[]>("/public/landing/templates", { revalidate: 300 });
  } catch {
    return [];
  }
}

/** Galeri template per halaman ("wadah"), untuk admin memilih template unggulan. size 1–50 (batas backend). */
export function getTemplates(params: {
  q?: string;
  category?: string;
  sort?: "popular" | "newest";
  page?: number;
  size?: number;
}) {
  return apiFetch<PageResponse<GalleryTemplate>>("/templates", { query: params });
}

/** Thumbnail template apa adanya, untuk diteruskan rute /template-thumbnail/[id]. */
/** Info terbaru yang tayang untuk landing (terbaru dulu, tanpa isi lengkap). Backend mati = kosong. */
export async function getLatestUpdates(limit = 3): Promise<PortalUpdate[]> {
  try {
    return await apiFetch<PortalUpdate[]>("/public/updates", { revalidate: 60, query: { limit } });
  } catch {
    return [];
  }
}

/** Detail info yang tayang (dengan isi lengkap). ApiError UPDATE_NOT_FOUND jika draft atau tidak ada. */
export function getUpdate(id: string) {
  return apiFetch<PortalUpdate>(`/public/updates/${encodeURIComponent(id)}`, { revalidate: 60 });
}

/** Gambar info (PNG) apa adanya, untuk diteruskan rute /update-image/[id]. */
export function getUpdateImage(id: string) {
  return fetch(`${API_URL}/public/updates/${encodeURIComponent(id)}/image`, { headers: CLIENT_HEADER, cache: "no-store" });
}

export function getTemplateThumbnail(id: string) {
  return fetch(`${API_URL}/templates/${encodeURIComponent(id)}/thumbnail`, { headers: CLIENT_HEADER, cache: "no-store" });
}

export function getPopularTemplates(size = 6) {
  return apiFetch<PageResponse<GalleryTemplate>>("/templates", { revalidate: 300, query: { sort: "popular", size } });
}
