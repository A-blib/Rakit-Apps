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
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Pengguna <span className="text-base font-normal text-muted">({list.totalItems})</span></h1>
      <form className="flex flex-wrap gap-2">
        <input className="input max-w-xs" name="q" defaultValue={q} placeholder="Cari nama atau email" />
        <select className="input w-auto" name="role" defaultValue={role ?? ""}>
          <option value="">Semua</option>
          <option value="creator">Pembuat website</option>
          <option value="provider">Penyedia template</option>
          <option value="admin">Admin</option>
        </select>
        <button className="btn-outline">Cari</button>
      </form>
      <div className="card overflow-x-auto p-0">
        <table className="table">
          <thead>
            <tr><th>Nama</th><th>Login</th><th>Organisasi</th><th>Website</th><th>Terakhir aktif</th><th>Daftar</th></tr>
          </thead>
          <tbody>
            {list.items.map((user) => (
              <tr key={user.id}>
                <td>
                  {user.displayName} {user.admin && <span className="badge">admin</span>}
                  {user.providerStatus && <span className="badge ml-1">provider {user.providerStatus}</span>}
                  <p className="text-xs text-muted">{user.email}</p>
                </td>
                <td>{user.loginMethods.map((m) => METHOD_LABEL[m]).join(", ")}</td>
                <td>{user.organizations}</td>
                <td>{user.websites}</td>
                <td>{user.lastActiveDay ? formatDay(user.lastActiveDay) : "–"}</td>
                <td className="whitespace-nowrap">{formatDateTime(user.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <Pagination page={list.page} totalPages={list.totalPages} hrefFor={href} />
    </div>
  );
}
