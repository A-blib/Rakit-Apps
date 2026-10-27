import Link from "next/link";
import type { Metadata } from "next";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { UPDATE_CATEGORY_LABEL, formatDateTime } from "@/lib/format";
import { setUpdatePublished } from "../../actions";
import { DeleteUpdateButton } from "./DeleteUpdateButton";

export const metadata: Metadata = { title: "Info terbaru" };

/** CONTOH AWAL untuk tim web: daftar info terbaru (draft & tayang). Landing menampilkan 3 yang tayang terbaru. */
export default async function AdminUpdatesPage() {
  const updates = await adminApi.updates(await adminToken());

  return (
    <div className="space-y-6 max-w-7xl">
      <header className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">Info Terbaru</h1>
          <p className="mt-1 text-xs text-muted">
            Update fitur, optimasi, dan desain ulang. 3 info tayang terbaru muncul di landing page.
          </p>
        </div>
        <Link href="/dashboard/info/new" className="btn-primary h-9.5 px-4 text-xs font-semibold self-start sm:self-auto">
          Tulis Info Baru
        </Link>
      </header>

      <div className="card overflow-hidden p-0">
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Judul</th>
                <th>Kategori</th>
                <th>Status</th>
                <th>Tampil Sampai</th>
                <th>Terakhir Diperbarui</th>
                <th className="text-right">Aksi</th>
              </tr>
            </thead>
            <tbody>
              {updates.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-xs text-muted">
                    Belum ada info. Landing page menampilkan &quot;Belum ada informasi terbaru.&quot;
                  </td>
                </tr>
              ) : (
                updates.map((update) => (
                  <tr key={update.id}>
                    <td>
                      <Link
                        className="font-medium text-xs text-foreground hover:text-primary transition-colors"
                        href={`/dashboard/info/${update.id}`}
                      >
                        {update.title}
                      </Link>
                    </td>
                    <td>
                      <span className="badge-neutral text-[10px]">{UPDATE_CATEGORY_LABEL[update.category]}</span>
                    </td>
                    <td>
                      {update.expired ? (
                        <span className="badge-neutral text-[10px]">Berakhir</span>
                      ) : update.status === "published" ? (
                        <span className="badge-success text-[10px]">Tayang</span>
                      ) : (
                        <span className="badge-neutral text-[10px]">Disembunyikan</span>
                      )}
                    </td>
                    <td className="whitespace-nowrap text-xs text-muted font-mono">
                      {update.expiresAt ? formatDateTime(update.expiresAt) : update.displayDays ? `${update.displayDays} hari setelah tayang` : "Tanpa batas"}
                    </td>
                    <td className="whitespace-nowrap text-xs text-muted font-mono">{formatDateTime(update.updatedAt)}</td>
                    <td>
                      <div className="flex items-center justify-end gap-2">
                        <form action={setUpdatePublished.bind(null, update.id, update.status !== "published")}>
                          <button
                            type="submit"
                            className="btn-outline h-8 px-3 text-xs"
                            aria-label={`${update.status === "published" ? "Sembunyikan" : "Tampilkan"} info ${update.title}`}
                          >
                            {update.status === "published" ? "Sembunyikan" : "Tampilkan"}
                          </button>
                        </form>
                        <Link href={`/dashboard/info/${update.id}`} className="btn-outline h-8 px-3 text-xs" aria-label={`Ubah info ${update.title}`}>
                          Ubah
                        </Link>
                        <DeleteUpdateButton
                          id={update.id}
                          title={update.title}
                          className="btn-outline h-8 px-3 text-xs text-error border-red-200 hover:border-red-400 hover:bg-red-50 dark:border-red-900/60 dark:hover:bg-red-950/40"
                        />
                      </div>
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
