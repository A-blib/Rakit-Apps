import Link from "next/link";
import { ErrorBox } from "@/components/ErrorBox";
import { getDocTree } from "@/lib/api/public";

/** Pusat dokumentasi: menu kategori di kiri, artikel di kanan. Isi dari GET /public/docs. */
export default async function DocsLayout({ children }: LayoutProps<"/docs">) {
  let tree;
  try {
    tree = await getDocTree();
  } catch (error) {
    return <div className="mx-auto max-w-6xl px-4 py-10"><ErrorBox error={error} /></div>;
  }
  return (
    <div className="mx-auto grid max-w-6xl gap-8 px-4 pt-20 pb-10 md:grid-cols-[240px_1fr]">
      <aside>
        <nav aria-label="Menu dokumentasi" className="space-y-6 text-sm">
          {tree.categories.map((category) => (
            <div key={category.value}>
              <p className="mb-2 font-semibold">{category.label}</p>
              <ul className="space-y-1">
                {category.articles.map((article) => (
                  <li key={article.id}>
                    <Link href={`/docs/${article.slug}`} className="text-muted hover:text-foreground">
                      {article.title}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </nav>
      </aside>
      <div className="min-w-0">{children}</div>
    </div>
  );
}
