import Link from "next/link";
import { DocForm } from "../DocForm";

export default function NewDocPage() {
  return (
    <div className="max-w-3xl space-y-6">
      <Link href="/dashboard/docs" className="inline-flex items-center gap-1.5 text-xs font-medium text-muted hover:text-navy transition-colors">
        ← Kembali ke Dokumentasi
      </Link>
      <header className="border-b border-border/80 pb-4">
        <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">
          Tulis Artikel Baru
        </h1>
        <p className="mt-1 text-xs text-muted">
          Buat artikel panduan untuk pengguna platform OrgSpace dalam format Markdown.
        </p>
      </header>
      <DocForm />
    </div>
  );
}
