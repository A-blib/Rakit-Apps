import Link from "next/link";
import type { Metadata } from "next";
import { ErrorBox } from "@/components/ErrorBox";
import { getDocTree } from "@/lib/api/public";

export const metadata: Metadata = { title: "Dokumentasi" };

export default async function DocsHome() {
  let tree;
  try {
    tree = await getDocTree();
  } catch (error) {
    // Backend mati (termasuk saat `next build`): halaman dibuat ulang otomatis setelah masa cache habis.
    return <ErrorBox error={error} />;
  }
  return (
    <article>
      <h1 className="text-3xl font-bold">Dokumentasi OrgSpace</h1>
      <p className="mt-2 text-muted">Panduan membuat website organisasi tanpa coding, langkah demi langkah.</p>
      <div className="mt-8 grid gap-4 md:grid-cols-2">
        {tree.categories.flatMap((category) =>
          category.articles.map((article) => (
            <Link key={article.id} href={`/docs/${article.slug}`} className="card hover:border-border-strong">
              <p className="text-xs font-semibold text-muted">{category.label}</p>
              <p className="mt-1 font-semibold">{article.title}</p>
              {article.summary && <p className="mt-1 text-sm text-muted">{article.summary}</p>}
            </Link>
          )),
        )}
      </div>
    </article>
  );
}
