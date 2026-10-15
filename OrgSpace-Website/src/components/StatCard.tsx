import { formatNumber, percentChange } from "@/lib/format";

export function StatCard({ label, value, previous }: { label: string; value: number; previous?: number }) {
  const change = previous === undefined ? null : percentChange(value, previous);

  return (
    <div className="card flex flex-col justify-between p-4.5 transition-all">
      <div>
        <p className="text-[11px] font-semibold uppercase tracking-wider text-muted">
          {label}
        </p>
        <p className="mt-1.5 text-2xl font-bold tracking-tight text-navy dark:text-foreground font-mono">
          {formatNumber(value)}
        </p>
      </div>

      {change !== null && (
        <div className="mt-3 flex items-center gap-1.5 pt-2 border-t border-border/50 text-[11px]">
          <span
            className={`inline-flex items-center gap-0.5 rounded px-1.5 py-0.5 font-semibold text-[10px] ${
              change >= 0
                ? "bg-emerald-50 text-emerald-700 border border-emerald-200/60 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800/60"
                : "bg-rose-50 text-rose-700 border border-rose-200/60 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800/60"
            }`}
          >
            {change >= 0 ? (
              <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 17a.75.75 0 01-.75-.75V5.612L5.29 9.77a.75.75 0 01-1.08-1.04l5.25-5.5a.75.75 0 011.08 0l5.25 5.5a.75.75 0 11-1.08 1.04l-3.96-4.158V16.25A.75.75 0 0110 17z" clipRule="evenodd" />
              </svg>
            ) : (
              <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 3a.75.75 0 01.75.75v10.638l3.96-4.158a.75.75 0 111.08 1.04l-5.25 5.5a.75.75 0 01-1.08 0l-5.25-5.5a.75.75 0 111.08-1.04l3.96 4.158V3.75A.75.75 0 0110 3z" clipRule="evenodd" />
              </svg>
            )}
            {Math.abs(change)}%
          </span>
          <span className="text-muted/80">vs periode lalu</span>
        </div>
      )}
    </div>
  );
}
