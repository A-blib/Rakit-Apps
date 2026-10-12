import Link from "next/link";
import { requireAdmin } from "@/lib/auth/session";
import { logout } from "../actions";

const MENU = [
  { href: "/dashboard", label: "Ringkasan" },
  { href: "/dashboard/feedback", label: "Kritik & saran" },
  { href: "/dashboard/users", label: "Pengguna" },
  { href: "/dashboard/traffic", label: "Lalu lintas API" },
  { href: "/dashboard/docs", label: "Dokumentasi" },
  { href: "/dashboard/system", label: "Sistem" },
];

/** Semua halaman di grup (panel) hanya untuk admin; pemeriksaannya di sini (requireAdmin). */
export default async function PanelLayout({ children }: LayoutProps<"/dashboard">) {
  const { admin } = await requireAdmin();
  return (
    <div className="flex flex-1 flex-col md:flex-row">
      <aside className="border-b border-border bg-surface md:w-60 md:border-r md:border-b-0">
        <div className="p-4">
          <Link href="/" className="text-lg font-bold">OrgSpace</Link>
          <p className="text-xs text-muted">Dashboard internal</p>
        </div>
        <nav className="flex gap-1 overflow-x-auto px-2 pb-2 md:flex-col">
          {MENU.map((item) => (
            <Link key={item.href} href={item.href} className="rounded-field px-3 py-2 text-sm whitespace-nowrap hover:bg-surface-subtle">
              {item.label}
            </Link>
          ))}
        </nav>
        <div className="hidden border-t border-border p-4 text-sm md:block">
          <p className="font-semibold">{admin.displayName}</p>
          <p className="truncate text-xs text-muted">{admin.email}</p>
          <form action={logout} className="mt-3">
            <button className="link text-sm">Keluar</button>
          </form>
        </div>
      </aside>
      <main className="min-w-0 flex-1 p-4 md:p-8">{children}</main>
    </div>
  );
}
