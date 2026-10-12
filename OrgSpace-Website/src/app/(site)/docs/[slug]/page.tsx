import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Markdown } from "@/components/Markdown";
import { ApiError } from "@/lib/api/http";
import { getDoc } from "@/lib/api/public";

async function load(slug: string) {
  try {
    return await getDoc(slug);
  } catch (error) {
    if (error instanceof ApiError && error.code === "DOC_NOT_FOUND") {
      notFound();
    }
    throw error;
  }
}

export async function generateMetadata({ params }: PageProps<"/docs/[slug]">): Promise<Metadata> {
  const doc = await load((await params).slug);
  return { title: doc.title, description: doc.summary ?? undefined };
}

export default async function DocPage({ params }: PageProps<"/docs/[slug]">) {
  const doc = await load((await params).slug);
  return (
    <article>
      <p className="text-sm font-semibold text-muted">{doc.categoryLabel}</p>
      <h1 className="mt-1 text-3xl font-bold">{doc.title}</h1>
      {doc.summary && <p className="mt-2 text-lg text-muted">{doc.summary}</p>}
      <div className="mt-8">
        <Markdown>{doc.body}</Markdown>
      </div>
    </article>
  );
}
