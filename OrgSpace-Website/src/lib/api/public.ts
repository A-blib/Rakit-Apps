import { apiFetch } from "./http";
import type { DocDetail, DocTree, GalleryTemplate, PageResponse, PublicOverview } from "./types";

/** Endpoint tanpa login: landing page & pusat dokumentasi. Di-cache Next.js beberapa menit. */

export function getOverview() {
  return apiFetch<PublicOverview>("/public/overview", { revalidate: 300 });
}

export function getDocTree() {
  return apiFetch<DocTree>("/public/docs", { revalidate: 60 });
}

export function getDoc(slug: string) {
  return apiFetch<DocDetail>(`/public/docs/${encodeURIComponent(slug)}`, { revalidate: 60 });
}

export function getPopularTemplates(size = 6) {
  return apiFetch<PageResponse<GalleryTemplate>>("/templates", { revalidate: 300, query: { sort: "popular", size } });
}
