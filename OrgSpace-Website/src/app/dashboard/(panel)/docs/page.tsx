import Link from "next/link";
import type { Metadata } from "next";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { DOC_CATEGORY_LABEL, formatDateTime } from "@/lib/format";

export const metadata: Metadata = { title: "Kelola dokumentasi" };

export default async function AdminDocsPage() {
  const docs = await adminApi.docs(await adminToken());
  return (
    <div className="space-y-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Dokumentasi</h1>
        <Link href="/dashboard/docs/new" className="btn-primary">Artikel baru</Link>
      </header>
      <div className="card overflow-x-auto p-0">
        <table className="table">
          <thead><tr><th>Judul</th><th>Kategori</th><th>Urutan</th><th>Status</th><th>Diubah</th></tr></thead>
          <tbody>
            {docs.map((doc) => (
              <tr key={doc.id}>
                <td>
                  <Link className="link" href={`/dashboard/docs/${doc.id}`}>{doc.title}</Link>
                  <p className="font-mono text-xs text-muted">/docs/{doc.slug}</p>
                </td>
                <td>{DOC_CATEGORY_LABEL[doc.category]}</td>
                <td>{doc.sortOrder}</td>
                <td>{doc.status === "published" ? "Tayang" : "Draft"}</td>
                <td className="whitespace-nowrap">{formatDateTime(doc.updatedAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
