import Link from "next/link";
import { DocForm } from "../DocForm";

export default function NewDocPage() {
  return (
    <div className="space-y-6">
      <Link href="/dashboard/docs" className="link text-sm">← Dokumentasi</Link>
      <h1 className="text-2xl font-bold">Artikel baru</h1>
      <DocForm />
    </div>
  );
}
