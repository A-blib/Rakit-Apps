import type { Metadata } from "next";
import Image from "next/image";
import { notFound } from "next/navigation";
import { Markdown } from "@/components/Markdown";
import { ApiError } from "@/lib/api/http";
import { getUpdate } from "@/lib/api/public";
import { UPDATE_CATEGORY_LABEL, formatDate, updateImageSrc } from "@/lib/format";

async function load(id: string) {
  try {
    return await getUpdate(id);
  } catch (error) {
    // Draft, sudah dihapus, atau id bukan UUID (400).
    if (error instanceof ApiError && (error.status === 404 || error.status === 400)) {
      notFound();
    }
    throw error;
  }
}

export async function generateMetadata({ params }: PageProps<"/info/[id]">): Promise<Metadata> {
  const update = await load((await params).id);
  return { title: update.title, description: update.summary };
}

/** CONTOH AWAL untuk tim web: halaman detail info terbaru (isi Markdown; HTML mentah tidak dirender). */
export default async function UpdatePage({ params }: PageProps<"/info/[id]">) {
  const update = await load((await params).id);
  const image = updateImageSrc(update);
  // pt-28: header situs menimpa bagian atas main (-mt-16 di layout).
  return (
    <article className="mx-auto max-w-3xl px-4 pt-28 pb-16 sm:px-6">
      <p className="text-sm font-semibold text-muted">
        {UPDATE_CATEGORY_LABEL[update.category]}
        {update.publishedAt && <span className="font-normal"> · {formatDate(update.publishedAt)}</span>}
      </p>
      <h1 className="mt-1 text-3xl font-bold">{update.title}</h1>
      <p className="mt-2 text-lg text-muted">{update.summary}</p>
      {image && (
        <Image
          src={image}
          alt={update.imageAlt ?? ""}
          width={1600}
          height={900}
          unoptimized
          className="mt-8 h-auto w-full rounded-card border border-border"
        />
      )}
      <div className="mt-8">
        <Markdown>{update.content ?? ""}</Markdown>
      </div>
    </article>
  );
}
