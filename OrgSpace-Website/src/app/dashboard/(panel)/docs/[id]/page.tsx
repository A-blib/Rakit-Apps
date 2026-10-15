import Link from "next/link";
import { notFound } from "next/navigation";
import { adminApi } from "@/lib/api/admin";
import { ApiError } from "@/lib/api/http";
import { adminToken } from "@/lib/auth/session";
import { deleteDoc } from "../../../actions";
import { DocForm } from "../DocForm";

export default async function EditDocPage({ params }: PageProps<"/dashboard/docs/[id]">) {
  const { id } = await params;
  let doc;
  try {
    doc = await adminApi.doc(await adminToken(), id);
  } catch (error) {
    if (error instanceof ApiError && error.code === "DOC_NOT_FOUND") notFound();
    throw error;
  }
  return (
    <div className="max-w-3xl space-y-6">
      <Link href="/dashboard/docs" className="inline-flex items-center gap-1.5 text-xs font-medium text-muted hover:text-navy transition-colors">
        ← Kembali ke Dokumentasi
      </Link>
      <header className="flex items-center justify-between border-b border-border/80 pb-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">
            Ubah Artikel Panduan
          </h1>
          <p className="mt-1 text-xs text-muted font-mono">
            /docs/{doc.slug}
          </p>
        </div>
        <form action={deleteDoc.bind(null, doc.id)}>
          <button className="btn-outline text-error border-red-200 hover:border-red-400 hover:bg-red-50 dark:border-red-900/60 dark:hover:bg-red-950/40 text-xs h-9 px-3.5">
            Hapus Artikel
          </button>
        </form>
      </header>
      <DocForm doc={doc} />
    </div>
  );
}
