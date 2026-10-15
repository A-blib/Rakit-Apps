import Link from "next/link";
import type { Metadata } from "next";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { DOC_CATEGORY_LABEL, formatDateTime } from "@/lib/format";

export const metadata: Metadata = { title: "Kelola dokumentasi" };

export default async function AdminDocsPage() {
  const docs = await adminApi.docs(await adminToken());

  return (
    <div className="space-y-6 max-w-7xl">
      {/* Header */}
      <header className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">
              Pusat Dokumentasi & Panduan
            </h1>
            <span className="rounded-badge bg-primary-container px-2 py-0.5 text-xs font-bold font-mono text-on-primary-container">
              {docs.length}
            </span>
          </div>
          <p className="mt-1 text-xs text-muted">
            Kelola artikel bantuan, panduan pengurus, dan dokumentasi API publik.
          </p>
        </div>

        <Link href="/dashboard/docs/new" className="btn-primary h-9.5 px-4 text-xs font-semibold inline-flex items-center gap-1.5 self-start sm:self-auto">
          <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
            <path d="M10.75 4.75a.75.75 0 00-1.5 0v4.5h-4.5a.75.75 0 000 1.5h4.5v4.5a.75.75 0 001.5 0v-4.5h4.5a.75.75 0 000-1.5h-4.5v-4.5z" />
          </svg>
          <span>Tulis Artikel Baru</span>
        </Link>
      </header>

      {/* Docs Table */}
      <div className="card overflow-hidden p-0">
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Judul & Path Artikel</th>
                <th>Kategori</th>
                <th className="text-center">Urutan</th>
                <th>Status Publikasi</th>
                <th>Terakhir Diperbarui</th>
              </tr>
            </thead>
            <tbody>
              {docs.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-xs text-muted">
                    Belum ada artikel dokumentasi yang dibuat.
                  </td>
                </tr>
              ) : (
                docs.map((doc) => (
                  <tr key={doc.id}>
                    <td>
                      <div>
                        <Link
                          className="font-medium text-xs text-foreground hover:text-primary transition-colors"
                          href={`/dashboard/docs/${doc.id}`}
                        >
                          {doc.title}
                        </Link>
                        <p className="font-mono text-[11px] text-muted mt-0.5">/docs/{doc.slug}</p>
                      </div>
                    </td>
                    <td>
                      <span className="badge-neutral text-[10px]">
                        {DOC_CATEGORY_LABEL[doc.category] ?? doc.category}
                      </span>
                    </td>
                    <td className="text-center font-mono text-xs text-muted">{doc.sortOrder}</td>
                    <td>
                      {doc.status === "published" ? (
                        <span className="badge-success text-[10px]">Tayang</span>
                      ) : (
                        <span className="badge-neutral text-[10px]">Draft</span>
                      )}
                    </td>
                    <td className="whitespace-nowrap text-xs text-muted font-mono">
                      {formatDateTime(doc.updatedAt)}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
