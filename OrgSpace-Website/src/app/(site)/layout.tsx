import Link from "next/link";

/** Kerangka halaman publik (landing & docs): header + footer. */
export default function SiteLayout({ children }: LayoutProps<"/">) {
  return (
    <>
      <header className="sticky top-0 z-10 border-b border-border bg-surface/90 backdrop-blur">
        <nav className="mx-auto flex h-16 max-w-6xl items-center gap-6 px-4">
          <Link href="/" className="text-lg font-bold">OrgSpace</Link>
          <Link href="/#fitur" className="text-sm text-muted hover:text-foreground">Fitur</Link>
          <Link href="/docs" className="text-sm text-muted hover:text-foreground">Dokumentasi</Link>
          <span className="flex-1" />
          <Link href="/dashboard" className="text-sm text-muted hover:text-foreground">Masuk tim</Link>
        </nav>
      </header>
      <main className="flex-1">{children}</main>
      <footer className="border-t border-border bg-surface">
        <div className="mx-auto flex max-w-6xl flex-wrap gap-6 px-4 py-8 text-sm text-muted">
          <span>© {new Date().getFullYear()} OrgSpace</span>
          <Link href="/docs" className="hover:text-foreground">Dokumentasi</Link>
          <Link href="/docs/kirim-kritik-dan-saran" className="hover:text-foreground">Kritik & saran</Link>
        </div>
      </footer>
    </>
  );
}
