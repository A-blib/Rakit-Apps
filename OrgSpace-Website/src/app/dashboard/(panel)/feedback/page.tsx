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
  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold">Kritik & saran</h1>
        <p className="text-sm text-muted">{counts.unread} percakapan punya pesan yang belum dibaca tim.</p>
      </header>
      <nav className="flex flex-wrap gap-2" aria-label="Status">
        {tabs.map((tab) => (
          <Link key={tab.label} href={href({ status: tab.value, page: undefined })}
                className={tab.value === status ? "badge" : "badge bg-transparent text-muted"}>
            {tab.label} ({tab.count})
          </Link>
        ))}
      </nav>
      <form className="flex flex-wrap gap-2">
        {status && <input type="hidden" name="status" value={status} />}
        <input className="input max-w-xs" name="q" defaultValue={q} placeholder="Cari judul, isi, pengirim" />
        <select className="input w-auto" name="role" defaultValue={role ?? ""}>
          <option value="">Semua pengirim</option>
          <option value="creator">Pembuat website</option>
          <option value="provider">Penyedia template</option>
        </select>
        <select className="input w-auto" name="category" defaultValue={category ?? ""}>
          <option value="">Semua jenis</option>
          {Object.entries(FEEDBACK_CATEGORY_LABEL).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
        <button className="btn-outline">Terapkan</button>
      </form>
      <div className="card overflow-x-auto p-0">
        <table className="table">
          <thead>
            <tr><th>Judul</th><th>Pengirim</th><th>Jenis</th><th>Status</th><th>Pesan terakhir</th></tr>
          </thead>
          <tbody>
            {list.items.map((item) => (
              <tr key={item.id} className={item.unread ? "font-semibold" : undefined}>
                <td>
                  <Link className="link" href={`/dashboard/feedback/${item.id}`}>{item.subject}</Link>
                  {item.unread && <span className="badge ml-2">baru</span>}
                </td>
                <td>
                  {item.sender?.displayName}
                  <p className="text-xs font-normal text-muted">{SENDER_ROLE_LABEL[item.senderRole]}</p>
                </td>
                <td>{FEEDBACK_CATEGORY_LABEL[item.category]}</td>
                <td>{FEEDBACK_STATUS_LABEL[item.status]}</td>
                <td className="whitespace-nowrap">{formatDateTime(item.lastMessageAt)}</td>
              </tr>
            ))}
            {list.items.length === 0 && (
              <tr><td colSpan={5} className="py-8 text-center text-muted">Belum ada kritik & saran.</td></tr>
            )}
          </tbody>
        </table>
      </div>
      <Pagination page={list.page} totalPages={list.totalPages} hrefFor={(p) => href({ page: p })} />
    </div>
  );
}
