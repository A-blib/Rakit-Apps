import { formatNumber, percentChange } from "@/lib/format";

interface StatCardProps {
  label: string;
  value: number | string;
  previous?: number;
  icon?: React.ReactNode;
  badge?: React.ReactNode;
}

/**
 * Mendapatkan ikon default berdasarkan nama label jika ikon kustom tidak disertakan.
 */
function getDefaultIcon(label: string) {
  const normalized = label.toLowerCase();
  if (normalized.includes("pendaftar") || normalized.includes("user")) {
    return (
      <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
        <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z" />
      </svg>
    );
  }
  if (normalized.includes("aktif")) {
    return (
      <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
        <path fillRule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clipRule="evenodd" />
      </svg>
    );
  }
  if (normalized.includes("diterbitkan") || normalized.includes("publish")) {
    return (
      <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
      </svg>
    );
  }
  if (normalized.includes("website") || normalized.includes("situs")) {
    return (
      <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
        <path fillRule="evenodd" d="M4.25 2A2.25 2.25 0 002 4.25v11.5A2.25 2.25 0 004.25 18h11.5A2.25 2.25 0 0018 15.75V4.25A2.25 2.25 0 0015.75 2H4.25zm4.03 6.28a.75.75 0 00-1.06-1.06L4.97 9.47a.75.75 0 000 1.06l2.25 2.25a.75.75 0 001.06-1.06L6.56 10l1.72-1.72zm3.44-1.06a.75.75 0 111.06 1.06L11.06 10l1.72 1.72a.75.75 0 11-1.06 1.06l-2.25-2.25a.75.75 0 010-1.06l2.25-2.25z" clipRule="evenodd" />
      </svg>
    );
  }
  if (normalized.includes("organisasi")) {
    return (
      <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
        <path fillRule="evenodd" d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4zm3 1h2v2H7V5zm4 0h2v2h-2V5zm-4 4h2v2H7V9zm4 0h2v2h-2V9zm-4 4h2v2H7v-2zm4 0h2v2h-2v-2z" clipRule="evenodd" />
      </svg>
    );
  }
  if (normalized.includes("template")) {
    return (
      <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
        <path d="M10.75 2.75a.75.75 0 00-1.5 0v8.614L6.295 8.235a.75.75 0 10-1.09 1.03l4.25 4.5a.75.75 0 001.09 0l4.25-4.5a.75.75 0 00-1.09-1.03l-2.955 3.129V2.75z" />
        <path d="M3.5 12.75a.75.75 0 00-1.5 0v2.5A2.75 2.75 0 004.75 18h10.5A2.75 2.75 0 0018 15.25v-2.5a.75.75 0 00-1.5 0v2.5c0 .69-.56 1.25-1.25 1.25H4.75c-.69 0-1.25-.56-1.25-1.25v-2.5z" />
      </svg>
    );
  }
  if (normalized.includes("saran") || normalized.includes("kritik") || normalized.includes("feedback")) {
    return (
      <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
        <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zm-5 0H8v2h2V9z" clipRule="evenodd" />
      </svg>
    );
  }
  // Default: API / Request
  return (
    <svg className="size-4" viewBox="0 0 20 20" fill="currentColor">
      <path fillRule="evenodd" d="M2 5a2 2 0 012-2h12a2 2 0 012 2v2a2 2 0 01-2 2H4a2 2 0 01-2-2V5zm14 1a1 1 0 11-2 0 1 1 0 012 0zM2 13a2 2 0 012-2h12a2 2 0 012 2v2a2 2 0 01-2 2H4a2 2 0 01-2-2v-2zm14 1a1 1 0 11-2 0 1 1 0 012 0z" clipRule="evenodd" />
    </svg>
  );
}

/**
 * Kartu metrik KPI Linear / Vercel style:
 * - Anti-glare di Light Mode & lembut di mata
 * - Ringan komputasi (tanpa reflow atau shadow berat)
 * - Tipografi tabular monospace yang presisi & nyaman dibaca
 */
export function StatCard({ label, value, previous, icon, badge }: StatCardProps) {
  const numericValue = typeof value === "number" ? value : undefined;
  const change = previous === undefined || numericValue === undefined ? null : percentChange(numericValue, previous);
  const activeIcon = icon ?? getDefaultIcon(label);

  return (
    <div className="group relative flex flex-col justify-between rounded-xl border border-border bg-surface p-4.5 transition-colors duration-150 hover:border-border-strong">
      {/* Top Header: Label & Icon */}
      <div className="flex items-center justify-between gap-2">
        <p className="text-xs font-medium text-muted truncate">
          {label}
        </p>
        <span className="flex size-7 shrink-0 items-center justify-center rounded-lg bg-surface-subtle text-muted transition-colors duration-150 group-hover:text-foreground">
          {activeIcon}
        </span>
      </div>

      {/* Main Metric Value & Custom Badges */}
      <div className="mt-3">
        <p className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground font-mono">
          {typeof value === "number" ? formatNumber(value) : value}
        </p>
        {badge && <div className="mt-2">{badge}</div>}
      </div>

      {/* Footer Comparison Badge */}
      <div className="mt-3.5 flex items-center gap-1.5 pt-2.5 border-t border-border/60 text-[11px]">
        {change !== null ? (
          <>
            <span
              className={`inline-flex items-center gap-0.5 rounded-md px-1.5 py-0.5 font-semibold text-[10px] font-mono ${
                change > 0
                  ? "bg-emerald-50 text-emerald-700 border border-emerald-200/70 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800/60"
                  : change < 0
                  ? "bg-rose-50 text-rose-700 border border-rose-200/70 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800/60"
                  : "bg-surface-subtle text-muted border border-border"
              }`}
            >
              {change > 0 ? (
                <svg className="size-2.5 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 17a.75.75 0 01-.75-.75V5.612L5.29 9.77a.75.75 0 01-1.08-1.04l5.25-5.5a.75.75 0 011.08 0l5.25 5.5a.75.75 0 11-1.08 1.04l-3.96-4.158V16.25A.75.75 0 0110 17z" clipRule="evenodd" />
                </svg>
              ) : change < 0 ? (
                <svg className="size-2.5 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 3a.75.75 0 01.75.75v10.638l3.96-4.158a.75.75 0 111.08 1.04l-5.25 5.5a.75.75 0 01-1.08 0l-5.25-5.5a.75.75 0 111.08-1.04l3.96 4.158V3.75A.75.75 0 0110 3z" clipRule="evenodd" />
                </svg>
              ) : null}
              {change > 0 ? `+${change}%` : `${change}%`}
            </span>
            <span className="text-muted truncate">vs periode lalu</span>
          </>
        ) : (
          <span className="text-[10px] text-muted">Data kumulatif</span>
        )}
      </div>
    </div>
  );
}
