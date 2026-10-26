import Link from "next/link";
import { Logo } from "@/components/Logo";
import { requireAdmin } from "@/lib/auth/session";
import { logout } from "../actions";
import { SidebarNav } from "./SidebarNav";
import { DashboardThemeToggle } from "./DashboardThemeToggle";

/** Semua halaman di grup (panel) hanya untuk admin; pemeriksaannya di sini (requireAdmin). */
export default async function PanelLayout({ children }: LayoutProps<"/dashboard">) {
  const { admin } = await requireAdmin();

  // Ambil inisial nama admin untuk avatar
  const initials =
    admin.displayName
      .split(" ")
      .map((n) => n[0])
      .filter(Boolean)
      .slice(0, 2)
      .join("")
      .toUpperCase() || "AD";

  return (
    <div className="flex min-h-screen flex-1 flex-col bg-background md:h-screen md:overflow-hidden md:flex-row transition-colors duration-200">
      {/* Sidebar: Fixed di desktop (tidak ikut scroll halaman konten utama) */}
      <aside className="flex flex-col border-b border-border bg-surface md:sticky md:top-0 md:h-screen md:w-64 md:border-r md:border-b-0 shrink-0 z-20 select-none transition-colors duration-200">
        {/* Brand header dengan logo transparan yang menyatu natural */}
        <div className="flex items-center justify-between border-b border-border/80 px-4 py-3.5 shrink-0">
          <Link
            href="/"
            className="group flex items-center gap-2.5 transition-transform hover:scale-[1.02] active:scale-[0.98]"
            title="Ke Beranda OrgSpace"
          >
            <Logo size="sm" withSubtitle />
          </Link>
          <span className="rounded-badge bg-cyan/10 px-2 py-0.5 text-[10px] font-bold tracking-wider uppercase text-cyan border border-cyan/20">
            Internal
          </span>
        </div>

        {/* Navigation list: Scrollable hanya di dalam sidebar jika menu panjang */}
        <div className="flex-1 overflow-y-auto min-h-0">
          <SidebarNav />
        </div>

        {/* User profile & logout section: Selalu menempel di bagian bawah sidebar */}
        <div className="border-t border-border/80 p-3 bg-surface-subtle/30 shrink-0">
          <div className="flex items-center gap-2.5 px-2 py-1.5">
            <div className="flex size-8 shrink-0 items-center justify-center rounded-full bg-navy text-[11px] font-bold text-white shadow-xs">
              {initials}
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-xs font-semibold text-foreground leading-tight">{admin.displayName}</p>
              <p className="truncate text-[11px] text-muted leading-tight">{admin.email}</p>
            </div>
          </div>
          <form action={logout} className="mt-2 pt-2 border-t border-border/60">
            <button
              type="submit"
              className="flex w-full items-center gap-2 rounded-field px-2 py-1.5 text-xs text-muted transition-colors hover:bg-surface-subtle hover:text-error cursor-pointer"
            >
              <svg className="size-3.5 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z"
                  clipRule="evenodd"
                />
              </svg>
              <span>Keluar Sesi</span>
            </button>
          </form>
        </div>
      </aside>

      {/* Main Content Area: Scroll mandiri di desktop */}
      <div className="flex flex-1 flex-col min-w-0 md:h-screen md:overflow-y-auto">
        {/* Top bar sticky */}
        <header className="sticky top-0 z-10 flex h-13 items-center justify-between border-b border-border bg-surface/90 px-6 backdrop-blur shrink-0 transition-colors duration-200">
          <div className="flex items-center gap-2 text-xs text-muted">
            <span className="font-medium text-foreground">Portal Tim</span>
            <span>/</span>
            <span className="text-muted">Monitoring & Manajemen</span>
          </div>

          <div className="flex items-center gap-2.5">
            {/* Tombol Pengalih Mode Terang / Gelap Dashboard */}
            <DashboardThemeToggle />

            <Link
              href="/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 rounded-field border border-border bg-surface px-2.5 py-1 text-xs font-medium text-muted transition-colors hover:bg-surface-subtle hover:text-foreground"
            >
              <span>Buka Situs Publik</span>
              <svg className="size-3 text-muted/70" viewBox="0 0 20 20" fill="currentColor">
                <path d="M11 3a1 1 0 100 2h2.586l-6.293 6.293a1 1 0 101.414 1.414L15 6.414V9a1 1 0 102 0V4a1 1 0 00-1-1h-5z" />
                <path d="M5 5a2 2 0 00-2 2v8a2 2 0 002 2h8a2 2 0 002-2v-3a1 1 0 10-2 0v3H5V7h3a1 1 0 000-2H5z" />
              </svg>
            </Link>
          </div>
        </header>

        {/* Content */}
        <main className="min-w-0 flex-1 p-5 md:p-8">{children}</main>
      </div>
    </div>
  );
}
