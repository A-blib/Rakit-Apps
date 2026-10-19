import Link from "next/link";
import type { Metadata } from "next";
import { ErrorBox } from "@/components/ErrorBox";
import { getDocTree } from "@/lib/api/public";

export const metadata: Metadata = {
  title: "Pusat Dokumentasi OrgSpace",
  description:
    "Panduan lengkap spesifikasi template, perkakas yang didukung, katalog modul sistem & kustom, serta cara meng-online-kan website bagi pembuat website.",
};

export default async function DocsHome() {
  let tree;
  try {
    tree = await getDocTree();
  } catch (error) {
    return <ErrorBox error={error} />;
  }

  return (
    <article className="space-y-12">
      <div>
        <h1 className="text-3xl font-bold">Dokumentasi OrgSpace</h1>
        <p className="mt-2 text-muted">
          Panduan teknis spesifikasi template, perkakas yang didukung, modul dinamis, serta cara membuat & meng-online-kan website.
        </p>

        {/* Indeks Cepat Artikel */}
        <div className="mt-8 grid gap-4 md:grid-cols-2">
          {tree.categories.flatMap((category) =>
            category.articles.map((article) => (
              <Link
                key={article.id}
                href={`/docs/${article.slug}`}
                className="card hover:border-border-strong transition-colors"
              >
                <p className="text-xs font-semibold text-muted">{category.label}</p>
                <p className="mt-1 font-semibold text-foreground">{article.title}</p>
                {article.summary && <p className="mt-1 text-sm text-muted">{article.summary}</p>}
              </Link>
            )),
          )}
        </div>
      </div>

      {/* Paket referensi provider: isinya sama dengan artikel provider, plus contoh template & instruksi AI agent. */}
      <div className="card">
        <p className="text-xs font-semibold text-muted">Penyedia Template (Provider)</p>
        <p className="mt-1 font-semibold text-foreground">Paket contoh & instruksi AI agent</p>
        <p className="mt-1 text-sm text-muted">
          Template contoh yang sudah lolos pengecekan, semua dokumentasi provider, dan AGENTS.md agar AI agent bisa
          langsung membuat template OrgSpace.
        </p>
        <a href="/downloads/contoh-modul-orgspace.zip" download className="mt-3 inline-block font-semibold text-primary">
          Unduh contoh-modul-orgspace.zip
        </a>
      </div>
    </article>
  );
}
