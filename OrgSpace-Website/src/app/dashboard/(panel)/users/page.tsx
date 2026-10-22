import Link from "next/link";
import type { Metadata } from "next";
import { Pagination } from "@/components/Pagination";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatDateTime, formatDay } from "@/lib/format";

export const metadata: Metadata = { title: "Manajemen Akun Pengguna" };

const METHOD_LABEL = { local: "Email", google: "Google", github: "GitHub" } as const;

export default async function UsersPage({ searchParams }: PageProps<"/dashboard/users">) {
  const query = await searchParams;
  const q = typeof query.q === "string" ? query.q : undefined;
  const role = query.role === "creator" || query.role === "provider" || query.role === "admin" ? query.role : undefined;
  const status =
    query.status === "active" || query.status === "pending" || query.status === "suspended"
      ? query.status
      : "all";
  const page = Math.max(0, Number(query.page ?? 0) || 0);

  const list = await adminApi.users(await adminToken(), { q, role, page });

  // Filter status akun
  const filteredItems = list.items.filter((user) => {
    if (status === "active") {
      return user.onboardingCompleted && user.providerStatus !== "suspended";
    }
    if (status === "pending") {
      return !user.onboardingCompleted;
    }
    if (status === "suspended") {
      return user.providerStatus === "suspended";
    }
    return true;
  });

  const hrefForPage = (p: number) =>
    `?${new URLSearchParams({
      ...(q ? { q } : {}),
      ...(role ? { role } : {}),
      ...(status !== "all" ? { status } : {}),
      page: String(p),
    })}`;

  const hrefForStatus = (s: string) => {
    const params = new URLSearchParams();
    if (q) params.set("q", q);
    if (role) params.set("role", role);
    if (s !== "all") params.set("status", s);
    return `?${params.toString()}`;
  };

  return (
    <div className="space-y-6 max-w-7xl">
      {/* Header */}
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-4">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-xl font-bold tracking-tight text-foreground sm:text-2xl">
              Manajemen Akun Pengguna
            </h1>
            <span className="rounded-badge bg-primary-container px-2 py-0.5 text-xs font-bold font-mono text-on-primary-container">
              {list.totalItems} akun
            </span>
          </div>
        </div>
      </div>

      {/* Filter Bar Status & Toolbar */}
      <div className="space-y-3">
        {/* Status Filter Bar Segmented Control */}
        <div className="flex flex-wrap items-center justify-between gap-3">
          <nav
            className="inline-flex rounded-xl border border-border bg-surface-subtle p-1 gap-1"
            aria-label="Filter Status Akun"
          >
            <Link
              href={hrefForStatus("all")}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors duration-150 ${
                status === "all"
                  ? "bg-surface font-semibold text-foreground border border-border"
                  : "text-muted hover:text-foreground"
              }`}
            >
              Semua Akun
            </Link>
            <Link
              href={hrefForStatus("active")}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors duration-150 inline-flex items-center gap-1.5 ${
                status === "active"
                  ? "bg-surface font-semibold text-emerald-700 dark:text-emerald-400 border border-border"
                  : "text-muted hover:text-emerald-600"
              }`}
            >
              <span className="size-2 rounded-full bg-emerald-500" />
              <span>Aktif</span>
            </Link>
            <Link
              href={hrefForStatus("pending")}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors duration-150 inline-flex items-center gap-1.5 ${
                status === "pending"
                  ? "bg-surface font-semibold text-amber-700 dark:text-amber-400 border border-border"
                  : "text-muted hover:text-amber-600"
              }`}
            >
              <span className="size-2 rounded-full bg-amber-500" />
              <span>Menunggu Verifikasi</span>
            </Link>
            <Link
              href={hrefForStatus("suspended")}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors duration-150 inline-flex items-center gap-1.5 ${
                status === "suspended"
                  ? "bg-surface font-semibold text-rose-700 dark:text-rose-400 border border-border"
                  : "text-muted hover:text-rose-600"
              }`}
            >
              <span className="size-2 rounded-full bg-rose-500" />
              <span>Ditangguhkan (Suspended)</span>
            </Link>
          </nav>

          <span className="text-[11px] font-mono text-muted">
            Menampilkan <strong className="text-foreground">{filteredItems.length}</strong> akun
          </span>
        </div>

        {/* Search & Role Filter Toolbar */}
        <form className="flex flex-wrap items-center gap-2.5">
          <div className="relative flex-1 min-w-[220px] max-w-sm">
            <input
              className="input pl-9 text-xs"
              name="q"
              defaultValue={q}
              placeholder="Cari nama atau email..."
            />
            <svg
              className="pointer-events-none absolute left-3 top-3 size-4 text-muted/70"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z"
                clipRule="evenodd"
              />
            </svg>
          </div>

          <select className="input w-auto text-xs" name="role" defaultValue={role ?? ""}>
            <option value="">Semua Peran</option>
            <option value="creator">Pembuat website</option>
            <option value="provider">Penyedia template</option>
            <option value="admin">Admin internal</option>
          </select>

          {status !== "all" && <input type="hidden" name="status" value={status} />}

          <button type="submit" className="btn-outline h-10 px-3.5 text-xs font-medium cursor-pointer">
            Terapkan
          </button>

          {(q || role || status !== "all") && (
            <Link
              href="/dashboard/users"
              className="btn-ghost h-10 px-3 text-xs text-muted hover:text-foreground"
            >
              Reset Filter
            </Link>
          )}
        </form>
      </div>

      {/* Data Table */}
      <div className="card overflow-hidden p-0">
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Pengguna</th>
                <th>Status Akun</th>
                <th>Metode Masuk</th>
                <th className="text-center">Organisasi</th>
                <th className="text-center">Website</th>
                <th>Aktif Terakhir</th>
                <th>Waktu Daftar</th>
                <th className="text-right">Aksi</th>
              </tr>
            </thead>
            <tbody>
              {filteredItems.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-xs text-muted">
                    Tidak ada pengguna yang cocok dengan kriteria filter saat ini.
                  </td>
                </tr>
              ) : (
                filteredItems.map((user) => {
                  const initials =
                    user.displayName
                      .split(" ")
                      .map((n) => n[0])
                      .slice(0, 2)
                      .join("")
                      .toUpperCase() || "U";

                  // Status Badge Konsisten:
                  // Hijau: Aktif | Kuning/Oranye: Menunggu Verifikasi | Merah: Ditangguhkan
                  const isSuspended = user.providerStatus === "suspended";
                  const isPending = !user.onboardingCompleted;

                  return (
                    <tr key={user.id}>
                      {/* Kolom Pengguna */}
                      <td>
                        <div className="flex items-center gap-2.5">
                          <div className="flex size-7.5 shrink-0 items-center justify-center rounded-full bg-surface-subtle border border-border text-[10px] font-bold text-navy dark:text-cyan">
                            {initials}
                          </div>
                          <div>
                            <div className="flex items-center gap-1.5 flex-wrap">
                              <span className="font-semibold text-foreground text-xs leading-tight">
                                {user.displayName}
                              </span>
                              {user.admin && (
                                <span className="rounded px-1.5 py-0.5 bg-navy text-white text-[10px] font-semibold tracking-wider uppercase">
                                  Admin
                                </span>
                              )}
                              {user.providerStatus && !isSuspended && (
                                <span className="rounded px-1.5 py-0.5 bg-primary-container text-on-primary-container text-[10px] font-medium">
                                  Provider
                                </span>
                              )}
                            </div>
                            <p className="text-[11px] text-muted font-mono leading-tight mt-0.5">{user.email}</p>
                          </div>
                        </div>
                      </td>

                      {/* Kolom Status Akun (Konsistensi Warna: Hijau / Kuning / Merah) */}
                      <td>
                        {isSuspended ? (
                          <span className="badge-error text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                            <span className="size-1.5 rounded-full bg-rose-500" />
                            Ditangguhkan
                          </span>
                        ) : isPending ? (
                          <span className="badge-warning text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                            <span className="size-1.5 rounded-full bg-amber-500" />
                            Menunggu Verifikasi
                          </span>
                        ) : (
                          <span className="badge-success text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                            <span className="size-1.5 rounded-full bg-emerald-500" />
                            Aktif
                          </span>
                        )}
                      </td>

                      {/* Kolom Metode Masuk */}
                      <td>
                        <div className="flex gap-1 flex-wrap">
                          {user.loginMethods.map((m) => (
                            <span key={m} className="badge-neutral text-[10px] px-1.5 py-0.2">
                              {METHOD_LABEL[m] ?? m}
                            </span>
                          ))}
                        </div>
                      </td>

                      {/* Kolom Organisasi & Website */}
                      <td className="text-center font-mono text-xs">{user.organizations}</td>
                      <td className="text-center font-mono text-xs">{user.websites}</td>

                      {/* Kolom Aktif Terakhir */}
                      <td className="text-xs text-muted">
                        {user.lastActiveDay ? (
                          <span className="inline-flex items-center gap-1.5">
                            <span className="size-1.5 rounded-full bg-emerald-500" />
                            {formatDay(user.lastActiveDay)}
                          </span>
                        ) : (
                          <span className="text-muted/60">–</span>
                        )}
                      </td>

                      {/* Kolom Waktu Daftar */}
                      <td className="whitespace-nowrap text-xs text-muted font-mono">
                        {formatDateTime(user.createdAt)}
                      </td>

                      {/* Kolom Aksi */}
                      <td className="text-right whitespace-nowrap">
                        <Link
                          href={`/dashboard/users/${user.id}`}
                          className="btn-outline h-7.5 px-2.5 text-[11px] font-medium hover:border-primary hover:text-primary transition-colors inline-flex items-center gap-1"
                        >
                          <span>Kelola</span>
                          <svg className="size-3 text-muted" viewBox="0 0 20 20" fill="currentColor">
                            <path
                              fillRule="evenodd"
                              d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </Link>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      <Pagination page={list.page} totalPages={list.totalPages} hrefFor={hrefForPage} />
    </div>
  );
}
