"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

interface NavItem {
  href: string;
  label: string;
  badge?: string;
  icon: (active: boolean) => React.ReactNode;
}

interface NavGroup {
  title: string;
  items: NavItem[];
}

const NAV_GROUPS: NavGroup[] = [
  {
    title: "UTAMA",
    items: [
      {
        href: "/dashboard",
        label: "Ringkasan",
        icon: (active) =>
          active ? (
            <svg className="size-5 shrink-0 fill-current" viewBox="0 0 24 24">
              <path d="M11.25 4.533A9.707 9.707 0 003 14.25a9.75 9.75 0 009.75 9.75 9.707 9.707 0 009.717-8.25H11.25V4.533z" />
              <path d="M14.25 1.533v9.967h9.967A9.75 9.75 0 0014.25 1.533z" />
            </svg>
          ) : (
            <svg className="size-5 shrink-0 stroke-current" fill="none" viewBox="0 0 24 24" strokeWidth={1.75}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 6a7.5 7.5 0 107.5 7.5h-7.5V6z" />
              <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 10.5H21A7.5 7.5 0 0013.5 3v7.5z" />
            </svg>
          ),
      },
      {
        href: "/dashboard/feedback",
        label: "Kritik & Saran",
        icon: (active) =>
          active ? (
            <svg className="size-5 shrink-0 fill-current" viewBox="0 0 24 24">
              <path fillRule="evenodd" d="M4.804 21.644A6.707 6.707 0 006 21.75a6.721 6.721 0 003.583-1.029c.774.182 1.584.279 2.417.279 5.322 0 9.75-3.97 9.75-9s-4.428-9-9.75-9c-5.322 0-9.75 3.97-9.75 9 0 2.409 1.025 4.587 2.674 6.192.232.226.277.428.254.543a3.73 3.73 0 01-.814 1.686.75.75 0 00.444 1.223zM8.25 10.5a.75.75 0 01.75-.75h6a.75.75 0 010 1.5H9a.75.75 0 01-.75-.75zm0 3.5a.75.75 0 01.75-.75h3a.75.75 0 010 1.5H9a.75.75 0 01-.75-.75z" clipRule="evenodd" />
            </svg>
          ) : (
            <svg className="size-5 shrink-0 stroke-current" fill="none" viewBox="0 0 24 24" strokeWidth={1.75}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H8.25m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H12m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a.75.75 0 01-.84-.84c.15-.693.39-1.353.705-1.956C3.805 16.732 3 14.49 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z" />
            </svg>
          ),
      },
      {
        href: "/dashboard/users",
        label: "Manajemen Akun",
        icon: (active) =>
          active ? (
            <svg className="size-5 shrink-0 fill-current" viewBox="0 0 24 24">
              <path d="M4.5 6.375a4.125 4.125 0 118.25 0 4.125 4.125 0 01-8.25 0zM14.25 8.625a3.375 3.375 0 116.75 0 3.375 3.375 0 01-6.75 0zM1.5 19.125a7.125 7.125 0 0114.25 0v.003l-.001.119a12.33 12.33 0 01-7.124 2.253A12.33 12.33 0 011.5 19.247v-.122zM16.5 18.065a5.618 5.618 0 012.625.31 4.125 4.125 0 01-4.821 2.82c.877-.852 1.62-1.91 2.196-3.13z" />
            </svg>
          ) : (
            <svg className="size-5 shrink-0 stroke-current" fill="none" viewBox="0 0 24 24" strokeWidth={1.75}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
            </svg>
          ),
      },
      {
        href: "/dashboard/websites",
        label: "Organisasi & Website",
        icon: (active) =>
          active ? (
            <svg className="size-5 shrink-0 fill-current" viewBox="0 0 24 24">
              <path fillRule="evenodd" d="M3.75 3a.75.75 0 00-.75.75v16.5c0 .414.336.75.75.75h16.5a.75.75 0 00.75-.75V3.75a.75.75 0 00-.75-.75H3.75zM9.53 8.47a.75.75 0 010 1.06L7.06 12l2.47 2.47a.75.75 0 11-1.06 1.06l-3-3a.75.75 0 010-1.06l3-3a.75.75 0 011.06 0zm4.94 0a.75.75 0 011.06 0l3 3a.75.75 0 010 1.06l-3 3a.75.75 0 11-1.06-1.06L16.94 12l-2.47-2.47a.75.75 0 010-1.06z" clipRule="evenodd" />
            </svg>
          ) : (
            <svg className="size-5 shrink-0 stroke-current" fill="none" viewBox="0 0 24 24" strokeWidth={1.75}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 21a9.004 9.004 0 008.716-6.747M12 21a9.004 9.004 0 01-8.716-6.747M12 21c2.485 0 4.5-4.03 4.5-9S14.485 3 12 3m0 18c-2.485 0-4.5-4.03-4.5-9S9.515 3 12 3m0 0a8.997 8.997 0 017.843 4.582M12 3a8.997 8.997 0 00-7.843 4.582m15.686 0A11.953 11.953 0 0112 10.5c-2.998 0-5.74-1.1-7.843-2.918m15.686 0A8.959 8.959 0 0121 12c0 .778-.099 1.533-.284 2.253m0 0A17.919 17.919 0 0112 16.5c-3.162 0-6.133-.815-8.716-2.247m0 0A9.015 9.015 0 013 12c0-1.605.42-3.113 1.157-4.418" />
            </svg>
          ),
      },
    ],
  },
  {
    title: "OPERASIONAL",
    items: [
      {
        href: "/dashboard/traffic",
        label: "Lalu Lintas API",
        icon: (active) =>
          active ? (
            <svg className="size-5 shrink-0 fill-current" viewBox="0 0 24 24">
              <path fillRule="evenodd" d="M15.22 6.22a.75.75 0 011.06 0l4.25 4.25a.75.75 0 010 1.06l-4.25 4.25a.75.75 0 11-1.06-1.06L17.44 12H9.75a.75.75 0 010-1.5h7.69l-2.22-2.22a.75.75 0 010-1.06zM8.78 17.78a.75.75 0 01-1.06 0l-4.25-4.25a.75.75 0 010-1.06l4.25-4.25a.75.75 0 011.06 1.06L6.56 11.5h7.69a.75.75 0 010 1.5H6.56l2.22 2.22a.75.75 0 010 1.06z" clipRule="evenodd" />
            </svg>
          ) : (
            <svg className="size-5 shrink-0 stroke-current" fill="none" viewBox="0 0 24 24" strokeWidth={1.75}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 18L9 11.25l4.306 4.307a11.95 11.95 0 015.814-5.519l2.74-1.22m0 0l-5.94-2.28m5.94 2.28l-2.28 5.941" />
            </svg>
          ),
      },
      {
        href: "/dashboard/docs",
        label: "Dokumentasi",
        icon: (active) =>
          active ? (
            <svg className="size-5 shrink-0 fill-current" viewBox="0 0 24 24">
              <path d="M12 3.75a6.715 6.715 0 00-4.5 1.765A6.715 6.715 0 003 3.75v14.25a6.75 6.75 0 014.5 1.5 6.75 6.75 0 014.5-1.5v-14.25zM12 3.75a6.715 6.715 0 014.5 1.765A6.715 6.715 0 0121 3.75v14.25a6.75 6.75 0 00-4.5 1.5 6.75 6.75 0 00-4.5-1.5v-14.25z" />
            </svg>
          ) : (
            <svg className="size-5 shrink-0 stroke-current" fill="none" viewBox="0 0 24 24" strokeWidth={1.75}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
            </svg>
          ),
      },
      {
        href: "/dashboard/system",
        label: "Kesehatan Sistem",
        icon: (active) =>
          active ? (
            <svg className="size-5 shrink-0 fill-current" viewBox="0 0 24 24">
              <path fillRule="evenodd" d="M2.25 5.25a3 3 0 013-3h13.5a3 3 0 013 3v2.25a3 3 0 01-3 3H5.25a3 3 0 01-3-3V5.25zm16.5 1.5a.75.75 0 100-1.5.75.75 0 000 1.5zM2.25 13.5a3 3 0 013-3h13.5a3 3 0 013 3v2.25a3 3 0 01-3 3H5.25a3 3 0 01-3-3V13.5zm16.5 1.5a.75.75 0 100-1.5.75.75 0 000 1.5z" clipRule="evenodd" />
            </svg>
          ) : (
            <svg className="size-5 shrink-0 stroke-current" fill="none" viewBox="0 0 24 24" strokeWidth={1.75}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5.25 14.25h13.5m-13.5 0a3 3 0 01-3-3m3 3a3 3 0 100 6h13.5a3 3 0 100-6m-16.5-3a3 3 0 013-3h13.5a3 3 0 013 3m-19.5 0a4.5 4.5 0 01.9-2.7L5.75 5.1a2.25 2.25 0 011.8-1.1h8.9a2.25 2.25 0 011.8 1.1l2.1 3.45a4.5 4.5 0 01.9 2.7m-16.5 0h16.5" />
            </svg>
          ),
      },
    ],
  },
];

export function SidebarNav() {
  const pathname = usePathname();

  return (
    <nav className="flex gap-2 overflow-x-auto p-2.5 md:flex-col md:gap-4 md:px-3 md:py-4" aria-label="Menu navigasi dashboard">
      {NAV_GROUPS.map((group) => (
        <div key={group.title} className="flex gap-1 md:flex-col">
          {/* Label Kategori: Gaya Uppercase, Semi-bold (600), 11-12px, tracking-wider */}
          <div className="hidden md:block px-3 pb-1.5 pt-1 text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider select-none">
            {group.title}
          </div>

          {/* Group Items */}
          <div className="flex gap-1 md:flex-col md:space-y-0.5">
            {group.items.map((item) => {
              const isActive =
                item.href === "/dashboard"
                  ? pathname === "/dashboard"
                  : pathname.startsWith(item.href);

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`group relative flex items-center justify-between rounded-lg px-3 py-2 transition-colors duration-150 ${
                    isActive
                      ? "text-sm font-semibold text-white bg-sky-600 dark:bg-sky-600 shadow-xs"
                      : "text-sm font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-100 dark:text-slate-300 dark:hover:text-white dark:hover:bg-white/5"
                  }`}
                >
                  <div className="flex items-center gap-3">
                    {/* Active Left Indicator Dot */}
                    <span
                      className={`hidden md:block size-1.5 rounded-full transition-all duration-150 ${
                        isActive
                          ? "bg-white scale-100 opacity-100"
                          : "bg-transparent scale-0 opacity-0 group-hover:bg-slate-400/40 group-hover:scale-75 group-hover:opacity-100"
                      }`}
                    />
                    {item.icon(isActive)}
                    <span>{item.label}</span>
                  </div>

                  {item.badge && (
                    <span
                      className={`ml-2 rounded px-1.5 py-0.5 text-[10px] font-mono font-semibold ${
                        isActive
                          ? "bg-white/20 text-white"
                          : "bg-surface-subtle border border-border text-slate-500 dark:text-slate-400"
                      }`}
                    >
                      {item.badge}
                    </span>
                  )}
                </Link>
              );
            })}
          </div>
        </div>
      ))}
    </nav>
  );
}
