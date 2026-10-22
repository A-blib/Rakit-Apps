import Link from "next/link";
import type { Metadata } from "next";
import { Pagination } from "@/components/Pagination";
import { adminApi } from "@/lib/api/admin";
import type { FeedbackCategory, FeedbackStatus, SenderRole } from "@/lib/api/types";
import { adminToken } from "@/lib/auth/session";
import {
  FEEDBACK_CATEGORY_LABEL,
  FEEDBACK_STATUS_LABEL,
  SENDER_ROLE_LABEL,
  formatDateTime,
} from "@/lib/format";

export const metadata: Metadata = { title: "Kritik & saran" };

function pick<T extends string>(value: string | string[] | undefined, allowed: readonly T[]): T | undefined {
  return typeof value === "string" && (allowed as readonly string[]).includes(value) ? (value as T) : undefined;
}

export default async function FeedbackPage({ searchParams }: PageProps<"/dashboard/feedback">) {
  const query = await searchParams;
  const status = pick<FeedbackStatus>(query.status, ["new", "in_progress", "resolved", "closed"]);
  const role = pick<SenderRole>(query.role, ["creator", "provider"]);
  const category = pick<FeedbackCategory>(query.category, ["bug", "saran", "pertanyaan", "keluhan", "lainnya"]);
  const q = typeof query.q === "string" ? query.q : undefined;
  const page = Math.max(0, Number(query.page ?? 0) || 0);
  const token = await adminToken();
  const [list, counts] = await Promise.all([
    adminApi.feedback(token, { status, role, category, q, page }),
    adminApi.feedbackCounts(token),
  ]);

  const href = (changes: Record<string, string | number | undefined>) => {
    const params = new URLSearchParams();
    const merged = { status, role, category, q, ...changes };
    for (const [key, value] of Object.entries(merged)) {
      if (value !== undefined && value !== "") params.set(key, String(value));
    }
    return `?${params.toString()}`;
  };

  const tabs: { value?: FeedbackStatus; label: string; count: number }[] = [
    { label: "Semua", count: counts.all },
    { value: "new", label: "Baru", count: counts.newCount },
    { value: "in_progress", label: "Diproses", count: counts.inProgress },
    { value: "resolved", label: "Selesai", count: counts.resolved },
    { value: "closed", label: "Ditutup", count: counts.closed },
  ];

  const getStatusBadge = (s: FeedbackStatus) => {
    switch (s) {
      case "new":
        return "badge text-[10px]";
      case "in_progress":
        return "badge-warning text-[10px]";
      case "resolved":
        return "badge-success text-[10px]";
      case "closed":
        return "badge-neutral text-[10px]";
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-foreground">
            Kritik & Saran Pengguna
          </h1>
        </div>

        {counts.unread > 0 && (
          <div className="inline-flex items-center gap-1.5 rounded-lg border border-primary/20 bg-primary-container px-3 py-1.5 text-xs font-semibold text-on-primary-container">
            <span className="size-2 rounded-full bg-primary" />
            <span>{counts.unread} pesan belum dibaca tim</span>
          </div>
        )}
      </div>

      {/* Status Segmented Tabs */}
      <nav className="flex flex-wrap items-center gap-1.5" aria-label="Filter Status">
        {tabs.map((tab) => {
          const isActive = tab.value === status;
          return (
            <Link
              key={tab.label}
              href={href({ status: tab.value, page: undefined })}
              className={`inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-colors duration-150 ${
                isActive
                  ? "bg-surface font-semibold text-foreground border border-border dark:border-border"
                  : "bg-surface-subtle text-muted hover:bg-surface-hover hover:text-foreground border border-transparent"
              }`}
            >
              <span>{tab.label}</span>
              <span className={`rounded-md px-1.5 py-0.2 text-[10px] font-mono ${
                isActive ? "bg-surface-subtle text-foreground" : "bg-surface text-muted border border-border/60"
              }`}>
                {tab.count}
              </span>
            </Link>
          );
        })}
      </nav>

      {/* Filter toolbar */}
      <form className="flex flex-wrap items-center gap-2.5">
        {status && <input type="hidden" name="status" value={status} />}
        <div className="relative flex-1 min-w-[200px] max-w-sm">
          <input
            className="input pl-9 text-xs"
            name="q"
            defaultValue={q}
            placeholder="Cari judul, pesan, atau pengirim..."
          />
          <svg className="pointer-events-none absolute left-3 top-3 size-4 text-muted/70" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z" clipRule="evenodd" />
          </svg>
        </div>

        <select className="input w-auto text-xs" name="role" defaultValue={role ?? ""}>
          <option value="">Semua Peran Pengirim</option>
          <option value="creator">Pembuat website</option>
          <option value="provider">Penyedia template</option>
        </select>

        <select className="input w-auto text-xs" name="category" defaultValue={category ?? ""}>
          <option value="">Semua Kategori</option>
          {Object.entries(FEEDBACK_CATEGORY_LABEL).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>

        <button type="submit" className="btn-outline h-10 px-3.5 text-xs font-medium">
          Filter
        </button>

        {(q || role || category) && (
          <Link href={href({ q: undefined, role: undefined, category: undefined })} className="btn-ghost h-10 px-3 text-xs text-muted">
            Reset
          </Link>
        )}
      </form>

      {/* Data Table */}
      <div className="card overflow-hidden p-0">
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Topik & Subjek</th>
                <th>Pengirim</th>
                <th>Kategori</th>
                <th>Status</th>
                <th>Pembaruan Terakhir</th>
              </tr>
            </thead>
            <tbody>
              {list.items.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-xs text-muted">
                    Tidak ada percakapan kritik & saran yang ditemukan.
                  </td>
                </tr>
              ) : (
                list.items.map((item) => (
                  <tr key={item.id} className={item.unread ? "bg-primary-container/20 font-medium" : undefined}>
                    <td>
                      <div className="flex items-center gap-2">
                        {item.unread && (
                          <span className="size-2 shrink-0 rounded-full bg-primary" title="Belum dibaca tim" />
                        )}
                        <Link
                          href={`/dashboard/feedback/${item.id}`}
                          className="font-medium text-xs text-foreground hover:text-primary transition-colors duration-150"
                        >
                          {item.subject}
                        </Link>
                      </div>
                    </td>
                    <td>
                      <p className="text-xs font-semibold text-foreground">{item.sender?.displayName || "Pengguna Anonim"}</p>
                      <p className="text-[11px] text-muted">{SENDER_ROLE_LABEL[item.senderRole] ?? item.senderRole}</p>
                    </td>
                    <td>
                      <span className="badge-neutral text-[10px]">
                        {FEEDBACK_CATEGORY_LABEL[item.category] ?? item.category}
                      </span>
                    </td>
                    <td>
                      <span className={getStatusBadge(item.status)}>
                        {FEEDBACK_STATUS_LABEL[item.status] ?? item.status}
                      </span>
                    </td>
                    <td className="whitespace-nowrap text-xs text-muted font-mono">
                      {formatDateTime(item.lastMessageAt)}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <Pagination page={list.page} totalPages={list.totalPages} hrefFor={(p) => href({ page: p })} />
    </div>
  );
}
