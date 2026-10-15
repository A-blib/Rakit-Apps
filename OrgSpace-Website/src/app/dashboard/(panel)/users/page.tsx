import Link from "next/link";
import type { Metadata } from "next";
import { Pagination } from "@/components/Pagination";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatDateTime, formatDay } from "@/lib/format";

export const metadata: Metadata = { title: "Pengguna" };

const METHOD_LABEL = { local: "Email", google: "Google", github: "GitHub" } as const;

export default async function UsersPage({ searchParams }: PageProps<"/dashboard/users">) {
  const query = await searchParams;
  const q = typeof query.q === "string" ? query.q : undefined;
  const role = query.role === "creator" || query.role === "provider" || query.role === "admin" ? query.role : undefined;
  const page = Math.max(0, Number(query.page ?? 0) || 0);
  const list = await adminApi.users(await adminToken(), { q, role, page });
  const href = (p: number) => `?${new URLSearchParams({ ...(q ? { q } : {}), ...(role ? { role } : {}), page: String(p) })}`;

  return (
    <div className="space-y-6 max-w-7xl">
      {/* Header */}
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-4">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">
              Pengguna Terdaftar
            </h1>
            <span className="rounded-badge bg-primary-container px-2 py-0.5 text-xs font-bold font-mono text-on-primary-container">
              {list.totalItems}
            </span>
          </div>
          <p className="mt-1 text-xs text-muted">
            Kelola data akun pengguna, peran sistem, dan aktivitas login terakhir.
          </p>
        </div>
      </div>

      {/* Filter toolbar */}
      <form className="flex flex-wrap items-center gap-2.5">
        <div className="relative flex-1 min-w-[220px] max-w-sm">
          <input
            className="input pl-9 text-xs"
            name="q"
            defaultValue={q}
            placeholder="Cari nama atau email..."
          />
          <svg className="pointer-events-none absolute left-3 top-3 size-4 text-muted/70" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z" clipRule="evenodd" />
          </svg>
        </div>

        <select className="input w-auto text-xs" name="role" defaultValue={role ?? ""}>
          <option value="">Semua Peran</option>
          <option value="creator">Pembuat website</option>
          <option value="provider">Penyedia template</option>
          <option value="admin">Admin internal</option>
        </select>

        <button type="submit" className="btn-outline h-10 px-3.5 text-xs font-medium">
          Terapkan
        </button>

        {(q || role) && (
          <Link href="/dashboard/users" className="btn-ghost h-10 px-3 text-xs text-muted hover:text-foreground">
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
                <th>Pengguna</th>
                <th>Metode Masuk</th>
                <th className="text-center">Organisasi</th>
                <th className="text-center">Website</th>
                <th>Aktif Terakhir</th>
                <th>Waktu Daftar</th>
              </tr>
            </thead>
            <tbody>
              {list.items.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-xs text-muted">
                    Tidak ada pengguna yang cocok dengan kriteria pencarian.
                  </td>
                </tr>
              ) : (
                list.items.map((user) => {
                  const initials = user.displayName
                    .split(" ")
                    .map((n) => n[0])
                    .slice(0, 2)
                    .join("")
                    .toUpperCase() || "U";

                  return (
                    <tr key={user.id}>
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
                                <span className="rounded px-1.5 py-0.2 bg-navy text-white text-[10px] font-semibold tracking-wider uppercase">
                                  Admin
                                </span>
                              )}
                              {user.providerStatus && (
                                <span className="rounded px-1.5 py-0.2 bg-primary-container text-on-primary-container text-[10px] font-medium">
                                  Provider ({user.providerStatus})
                                </span>
                              )}
                            </div>
                            <p className="text-[11px] text-muted font-mono leading-tight mt-0.5">{user.email}</p>
                          </div>
                        </div>
                      </td>
                      <td>
                        <div className="flex gap-1 flex-wrap">
                          {user.loginMethods.map((m) => (
                            <span key={m} className="badge-neutral text-[10px] px-1.5 py-0.2">
                              {METHOD_LABEL[m] ?? m}
                            </span>
                          ))}
                        </div>
                      </td>
                      <td className="text-center font-mono text-xs">{user.organizations}</td>
                      <td className="text-center font-mono text-xs">{user.websites}</td>
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
                      <td className="whitespace-nowrap text-xs text-muted font-mono">
                        {formatDateTime(user.createdAt)}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      <Pagination page={list.page} totalPages={list.totalPages} hrefFor={href} />
    </div>
  );
}
