import Link from "next/link";
import { Logo } from "@/components/Logo";

/** Kerangka halaman publik (landing & docs): header + footer Clean Enterprise. */
export default function SiteLayout({ children }: LayoutProps<"/">) {
  return (
    <>
      {/* Header */}
      <header className="sticky top-0 z-20 border-b border-border bg-surface/90 backdrop-blur transition-colors">
        <nav className="mx-auto flex h-15 max-w-6xl items-center justify-between gap-6 px-4 sm:px-6">
          <div className="flex items-center gap-8">
            <Link href="/" className="flex items-center transition-opacity hover:opacity-90" aria-label="Beranda OrgSpace">
              <Logo size="sm" />
            </Link>
            <div className="hidden sm:flex items-center gap-5 text-xs font-medium text-muted">
              <Link href="/#fitur" className="transition-colors hover:text-navy dark:hover:text-foreground">
                Fitur Unggulan
              </Link>
              <Link href="/docs" className="transition-colors hover:text-navy dark:hover:text-foreground">
                Dokumentasi
              </Link>
              <Link href="/docs/kirim-kritik-dan-saran" className="transition-colors hover:text-navy dark:hover:text-foreground">
                Kritik & Saran
              </Link>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Link
              href="/dashboard"
              className="btn-outline h-8.5 px-3 text-xs font-medium inline-flex items-center gap-1.5"
            >
              <span>Dashboard Tim</span>
              <svg className="size-3 text-muted" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M3 10a.75.75 0 01.75-.75h10.638L10.23 5.29a.75.75 0 111.04-1.08l5.5 5.25a.75.75 0 010 1.08l-5.5 5.25a.75.75 0 11-1.04-1.08l4.158-3.96H3.75A.75.75 0 013 10z" clipRule="evenodd" />
              </svg>
            </Link>
          </div>
        </nav>
      </header>

      {/* Main Content */}
      <main className="flex-1">{children}</main>

      {/* Corporate Footer */}
      <footer className="border-t border-border bg-surface text-xs text-muted">
        <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <Logo size="sm" />
              <span className="text-muted/60">|</span>
              <span>Platform Pembuatan Website Organisasi</span>
            </div>

            <div className="flex flex-wrap items-center gap-5">
              <Link href="/docs" className="transition-colors hover:text-foreground">
                Panduan
              </Link>
              <Link href="/docs/kirim-kritik-dan-saran" className="transition-colors hover:text-foreground">
                Umpan Balik
              </Link>
              <Link href="/dashboard/login" className="transition-colors hover:text-foreground">
                Masuk Internal
              </Link>
              <span>© {new Date().getFullYear()} OrgSpace</span>
            </div>
          </div>
        </div>
      </footer>
    </>
  );
}
