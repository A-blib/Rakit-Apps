import { apiFetch } from "./http";
import type { DocDetail, DocTree, GalleryTemplate, PageResponse, PublicOverview } from "./types";

/** Endpoint tanpa login: landing page & pusat dokumentasi. Di-cache Next.js beberapa menit. */

import { FALLBACK_DOC_ARTICLES, FALLBACK_DOC_TREE, mergeDocTree } from "../docs-data";

export function getOverview() {
  return apiFetch<PublicOverview>("/public/overview", { revalidate: 300 });
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

export function getPopularTemplates(size = 6) {
  return apiFetch<PageResponse<GalleryTemplate>>("/templates", { revalidate: 300, query: { sort: "popular", size } });
}
