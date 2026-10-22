import Link from "next/link";
import { Logo } from "@/components/Logo";
import { ThemeToggle } from "@/components/ThemeToggle";
import { requireAdmin } from "@/lib/auth/session";
import { logout } from "../actions";
import { SidebarNav } from "./SidebarNav";

/** Layout panel admin bergaya Clean Minimalist SaaS (Linear / Vercel style) yang ringan & nyaman di mata. */
export default async function PanelLayout({ children }: LayoutProps<"/dashboard">) {
  const { admin } = await requireAdmin();

  // Inisial nama admin untuk avatar
  const initials = admin.displayName
    .split(" ")
    .map((n) => n[0])
    .filter(Boolean)
    .slice(0, 2)
    .join("")
    .toUpperCase() || "AD";

  return (
    <div className="flex min-h-screen flex-1 flex-col bg-background md:flex-row text-foreground">
      {/* Sidebar Navigasi Kiri */}
      <aside className="flex flex-col border-b border-border/80 bg-surface md:w-64 md:border-r md:border-b-0 shrink-0 print:hidden">
        {/* Brand header */}
        <div className="flex items-center justify-between border-b border-border/80 px-4.5 py-4">
          <Link href="/" className="flex items-center gap-2.5 transition-opacity hover:opacity-90" title="Ke Beranda Publik">
            <Logo size="sm" />
          </Link>
          <span className="rounded-md bg-surface-subtle border border-border px-1.5 py-0.5 text-[10px] font-mono font-semibold tracking-wider uppercase text-muted">
            ADMIN
          </span>
        </div>

        {/* Navigation Groups */}
        <div className="flex-1 overflow-y-auto">
          <SidebarNav />
        </div>

        {/* User profile & logout footer */}
        <div className="border-t border-border/80 p-3 bg-surface-subtle/40">
          <div className="flex items-center gap-3 rounded-lg p-2 transition-colors duration-150 hover:bg-surface-subtle">
            {/* Avatar circle dengan status dot */}
            <div className="relative flex size-8.5 shrink-0 items-center justify-center rounded-lg bg-surface-subtle text-xs font-bold font-mono text-foreground border border-border">
              {initials}
              <span className="absolute -bottom-0.5 -right-0.5 size-2.5 rounded-full bg-emerald-500 ring-2 ring-surface" title="Aktif" />
            </div>

            <div className="min-w-0 flex-1">
              <p className="truncate text-xs font-semibold text-foreground leading-tight">
                {admin.displayName}
              </p>
              <p className="truncate text-[11px] text-muted leading-tight mt-0.5 font-mono">
                {admin.email}
              </p>
            </div>
          </div>

          <form action={logout} className="mt-2 pt-2 border-t border-border/60">
            <button
              type="submit"
              className="flex w-full items-center gap-2 rounded-lg px-2.5 py-1.5 text-xs text-muted transition-colors duration-150 hover:bg-rose-500/10 hover:text-rose-600 dark:hover:text-rose-400 cursor-pointer"
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

      {/* Main Content Area */}
      <div className="flex flex-1 flex-col min-w-0 w-full">
        {/* Top Header Bar — Tanpa backdrop-blur berat agar 60fps mulus di semua device */}
        <header className="sticky top-0 z-20 flex h-14 items-center justify-between border-b border-border/80 bg-surface px-5 md:px-8 print:hidden">
          {/* Breadcrumbs */}
          <div className="flex items-center gap-2 text-xs text-muted font-medium">
            <span className="text-foreground font-semibold">Portal Internal</span>
          </div>

          {/* Right Controls */}
          <div className="flex items-center gap-3">
            {/* Live Operational Status Indicator */}
            <div className="hidden sm:inline-flex items-center gap-1.5 rounded-full border border-border bg-surface-subtle px-2.5 py-1 text-[11px] font-medium text-muted">
              <span className="size-2 rounded-full bg-emerald-500" />
              <span>Sistem Normal</span>
            </div>

            {/* Public Site Link */}
            <Link
              href="/"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 rounded-lg border border-border bg-surface px-2.5 py-1.5 text-xs font-medium text-muted transition-colors duration-150 hover:border-border-strong hover:bg-surface-subtle hover:text-foreground"
            >
              <span>Situs Publik</span>
              <svg className="size-3 text-muted/70" viewBox="0 0 20 20" fill="currentColor">
                <path d="M11 3a1 1 0 100 2h2.586l-6.293 6.293a1 1 0 101.414 1.414L15 6.414V9a1 1 0 102 0V4a1 1 0 00-1-1h-5z" />
                <path d="M5 5a2 2 0 00-2 2v8a2 2 0 002 2h8a2 2 0 002-2v-3a1 1 0 10-2 0v3H5V7h3a1 1 0 000-2H5z" />
              </svg>
            </Link>

            {/* Dark/Light Theme Toggle */}
            <ThemeToggle />
          </div>
        </header>

        {/* Content Body */}
        <main className="min-w-0 flex-1 w-full p-5 md:p-8 lg:p-9">{children}</main>
      </div>
    </div>
  );
}
