import Link from "next/link";
import { notFound } from "next/navigation";
import { adminApi } from "@/lib/api/admin";
import { ApiError } from "@/lib/api/http";
import { adminToken } from "@/lib/auth/session";
import { DeleteUpdateButton } from "../DeleteUpdateButton";
import { UpdateForm } from "../UpdateForm";

export default async function EditUpdatePage({ params }: PageProps<"/dashboard/info/[id]">) {
  const { id } = await params;
  let update;
  try {
    update = await adminApi.update(await adminToken(), id);
  } catch (error) {
    if (error instanceof ApiError && (error.status === 404 || error.status === 400)) notFound();
    throw error;
  }
  return (
    <div className="max-w-3xl space-y-6">
      <Link href="/dashboard/info" className="inline-flex items-center gap-1.5 text-xs font-medium text-muted hover:text-navy transition-colors">
        ← Kembali ke Info Terbaru
      </Link>
      <header className="flex items-center justify-between border-b border-border/80 pb-4">
        <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">Ubah Info</h1>
        <DeleteUpdateButton
          id={update.id}
          title={update.title}
          className="btn-outline text-error border-red-200 hover:border-red-400 hover:bg-red-50 dark:border-red-900/60 dark:hover:bg-red-950/40 text-xs h-9 px-3.5"
        />
      </header>
      <UpdateForm update={update} />
    </div>
  );
}
