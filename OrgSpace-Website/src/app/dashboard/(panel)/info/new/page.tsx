import Link from "next/link";
import { UpdateForm } from "../UpdateForm";

export default function NewUpdatePage() {
  return (
    <div className="max-w-3xl space-y-6">
      <Link href="/dashboard/info" className="inline-flex items-center gap-1.5 text-xs font-medium text-muted hover:text-navy transition-colors">
        ← Kembali ke Info Terbaru
      </Link>
      <header className="border-b border-border/80 pb-4">
        <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">Tulis Info Baru</h1>
      </header>
      <UpdateForm />
    </div>
  );
}
