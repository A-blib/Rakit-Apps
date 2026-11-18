"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

interface MenuItem {
  href: string;
  label: string;
  icon: (active: boolean) => React.ReactNode;
  badge?: string;
}

interface MenuSection {
  title: string;
  items: MenuItem[];
}

const MENU_SECTIONS: MenuSection[] = [
  {
    title: "Utama & Analitik",
    items: [
      {
        href: "/dashboard",
        label: "Ringkasan",
        icon: (active) => (
          <svg className={`size-4 shrink-0 transition-colors ${active ? "text-primary" : "text-muted group-hover:text-foreground"}`} viewBox="0 0 20 20" fill="currentColor">
            <path d="M2 10a8 8 0 018-8v8h8a8 8 0 11-16 0z" />
            <path d="M12 2.252A8.014 8.014 0 0117.748 8H12V2.252z" />
          </svg>
        ),
      },
      {
        href: "/dashboard/users",
        label: "Pengguna",
        icon: (active) => (
          <svg className={`size-4 shrink-0 transition-colors ${active ? "text-primary" : "text-muted group-hover:text-foreground"}`} viewBox="0 0 20 20" fill="currentColor">
            <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z" />
          </svg>
        ),
      },
      {
        href: "/dashboard/traffic",
        label: "Lalu Lintas API",
        icon: (active) => (
          <svg className={`size-4 shrink-0 transition-colors ${active ? "text-primary" : "text-muted group-hover:text-foreground"}`} viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M12 7a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0V8.414l-4.293 4.293a1 1 0 01-1.414 0L8 10.414l-4.293 4.293a1 1 0 01-1.414-1.414l5-5a1 1 0 011.414 0L11 10.586 14.586 7H12z" clipRule="evenodd" />
          </svg>
        ),
      },
    ],
  },
  {
    title: "Konten Portal",
    items: [
      {
        href: "/dashboard/landing",
        label: "Konten Landing",
        icon: (active) => (
          <svg className={`size-4 shrink-0 transition-colors ${active ? "text-primary" : "text-muted group-hover:text-foreground"}`} viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
          </svg>
        ),
      },
      {
        href: "/dashboard/info",
        label: "Info Terbaru",
        icon: (active) => (
          <svg className={`size-4 shrink-0 transition-colors ${active ? "text-primary" : "text-muted group-hover:text-foreground"}`} viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M18 3a1 1 0 00-1.447-.894L8.763 6H5a3 3 0 000 6h.28l1.771 5.316A1 1 0 008 18h1a1 1 0 001-1v-4.382l6.553 3.276A1 1 0 0018 15V3z" clipRule="evenodd" />
          </svg>
        ),
      },
      {
        href: "/dashboard/docs",
        label: "Dokumentasi",
        icon: (active) => (
          <svg className={`size-4 shrink-0 transition-colors ${active ? "text-primary" : "text-muted group-hover:text-foreground"}`} viewBox="0 0 20 20" fill="currentColor">
            <path d="M9 4.804A7.968 7.968 0 005.5 4c-1.255 0-2.443.29-3.5.804v10A7.969 7.969 0 015.5 14c1.669 0 3.218.51 4.5 1.385A7.962 7.962 0 0114.5 14c1.255 0 2.443.29-3.5.804V12a1 1 0 11-2 0V4.804z" />
          </svg>
        ),
      },
    ],
  },
  {
    title: "Dukungan & Sistem",
    items: [
      {
        href: "/dashboard/feedback",
        label: "Kritik & Saran",
        icon: (active) => (
          <svg className={`size-4 shrink-0 transition-colors ${active ? "text-primary" : "text-muted group-hover:text-foreground"}`} viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zm-5 0H8v2h2V9z" clipRule="evenodd" />
          </svg>
        ),
      },
      {
        href: "/dashboard/system",
        label: "Status Sistem",
        icon: (active) => (
          <svg className={`size-4 shrink-0 transition-colors ${active ? "text-primary" : "text-muted group-hover:text-foreground"}`} viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M2 5a2 2 0 012-2h12a2 2 0 012 2v2a2 2 0 01-2 2H4a2 2 0 01-2-2V5zm14 1a1 1 0 11-2 0 1 1 0 012 0zM2 13a2 2 0 012-2h12a2 2 0 012 2v2a2 2 0 01-2 2H4a2 2 0 01-2-2v-2zm14 1a1 1 0 11-2 0 1 1 0 012 0z" clipRule="evenodd" />
          </svg>
        ),
      },
    ],
  },
];

export function SidebarNav() {
  const pathname = usePathname();

  return (
    <>
      {/* Mobile Horizontal Navigation */}
      <nav className="flex gap-1 overflow-x-auto px-3 py-2 md:hidden" aria-label="Menu navigasi mobile">
        {MENU_SECTIONS.flatMap((s) => s.items).map((item) => {
          const isActive =
            item.href === "/dashboard" ? pathname === "/dashboard" : pathname.startsWith(item.href);

          return (
            <Link
              key={item.href}
              href={item.href}
              className={`group flex items-center gap-2 rounded-field px-3 py-2 text-xs font-medium whitespace-nowrap transition-colors duration-150 ${
                isActive
                  ? "bg-primary-container text-on-primary-container font-semibold"
                  : "text-foreground/80 hover:bg-surface-subtle hover:text-foreground"
              }`}
            >
              {item.icon(isActive)}
              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>

      {/* Desktop Grouped Navigation */}
      <nav className="hidden md:flex flex-col space-y-4 px-3 py-3" aria-label="Menu navigasi desktop">
        {MENU_SECTIONS.map((section) => (
          <div key={section.title} className="space-y-1">
            <div className="px-3 pt-1 pb-1 text-[10px] font-bold uppercase tracking-wider text-muted/70">
              {section.title}
            </div>
            <div className="space-y-0.5">
              {section.items.map((item) => {
                const isActive =
                  item.href === "/dashboard" ? pathname === "/dashboard" : pathname.startsWith(item.href);

                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`group flex items-center justify-between rounded-field px-3 py-2 text-xs font-medium transition-all duration-150 ${
                      isActive
                        ? "bg-primary/10 text-primary dark:bg-primary/15 font-semibold shadow-2xs border-l-2 border-primary"
                        : "text-foreground/80 hover:bg-surface-subtle hover:text-foreground"
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      {item.icon(isActive)}
                      <span>{item.label}</span>
                    </div>

                    {isActive && (
                      <span className="size-1.5 rounded-full bg-primary" />
                    )}
                  </Link>
                );
              })}
            </div>
          </div>
        ))}
      </nav>
    </>
  );
}
