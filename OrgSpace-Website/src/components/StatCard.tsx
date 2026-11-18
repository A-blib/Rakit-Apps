import { formatNumber, percentChange } from "@/lib/format";

interface StatCardProps {
  label: string;
  value: number;
  previous?: number;
  icon?: React.ReactNode;
}

function getStatCardMeta(label: string) {
  const l = label.toLowerCase();
  if (l.includes("pendaftar")) {
    return {
      bg: "bg-blue-50 text-[#0099FF] dark:bg-blue-950/40 dark:text-blue-400 border-blue-200/60 dark:border-blue-800/60",
      icon: (
        <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor">
          <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6zM16 7a1 1 0 10-2 0v1h-1a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V7z" />
        </svg>
      ),
    };
  }
  if (l.includes("aktif")) {
    return {
      bg: "bg-emerald-50 text-emerald-600 dark:bg-emerald-950/40 dark:text-emerald-400 border-emerald-200/60 dark:border-emerald-800/60",
      icon: (
        <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor">
          <path fillRule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clipRule="evenodd" />
        </svg>
      ),
    };
  }
  if (l.includes("website baru")) {
    return {
      bg: "bg-indigo-50 text-indigo-600 dark:bg-indigo-950/40 dark:text-indigo-400 border-indigo-200/60 dark:border-indigo-800/60",
      icon: (
        <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor">
          <path fillRule="evenodd" d="M4.083 9h11.834a8 8 0 01-11.834 0zm11.834 2H4.083a8 8 0 0011.834 0zM10 2a8 8 0 100 16 8 8 0 000-16z" clipRule="evenodd" />
        </svg>
      ),
    };
  }
  if (l.includes("diterbitkan")) {
    return {
      bg: "bg-sky-50 text-sky-600 dark:bg-sky-950/40 dark:text-sky-400 border-sky-200/60 dark:border-sky-800/60",
      icon: (
        <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor">
          <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
        </svg>
      ),
    };
  }
  if (l.includes("organisasi")) {
    return {
      bg: "bg-violet-50 text-violet-600 dark:bg-violet-950/40 dark:text-violet-400 border-violet-200/60 dark:border-violet-800/60",
      icon: (
        <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor">
          <path fillRule="evenodd" d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4zm3 1h2v2H7V5zm4 0h2v2h-2V5zm-4 4h2v2H7V9zm4 0h2v2h-2V9zm-4 4h2v2H7v-2zm4 0h2v2h-2v-2z" clipRule="evenodd" />
        </svg>
      ),
    };
  }
  if (l.includes("template")) {
    return {
      bg: "bg-amber-50 text-amber-600 dark:bg-amber-950/40 dark:text-amber-400 border-amber-200/60 dark:border-amber-800/60",
      icon: (
        <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor">
          <path d="M7 3a1 1 0 000 2h6a1 1 0 100-2H7zM4 7a1 1 0 011-1h10a1 1 0 110 2H5a1 1 0 01-1-1zM2 11a2 2 0 012-2h12a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2v-4z" />
        </svg>
      ),
    };
  }
  if (l.includes("kritik") || l.includes("saran") || l.includes("feedback")) {
    return {
      bg: "bg-rose-50 text-rose-600 dark:bg-rose-950/40 dark:text-rose-400 border-rose-200/60 dark:border-rose-800/60",
      icon: (
        <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor">
          <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zm-5 0H8v2h2V9z" clipRule="evenodd" />
        </svg>
      ),
    };
  }
  return {
    bg: "bg-teal-50 text-teal-600 dark:bg-teal-950/40 dark:text-teal-400 border-teal-200/60 dark:border-teal-800/60",
    icon: (
      <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor">
        <path fillRule="evenodd" d="M2 5a2 2 0 012-2h12a2 2 0 012 2v2a2 2 0 01-2 2H4a2 2 0 01-2-2V5zm14 1a1 1 0 11-2 0 1 1 0 012 0zM2 13a2 2 0 012-2h12a2 2 0 012 2v2a2 2 0 01-2 2H4a2 2 0 01-2-2v-2zm14 1a1 1 0 11-2 0 1 1 0 012 0z" clipRule="evenodd" />
      </svg>
    ),
  };
}

export function StatCard({ label, value, previous, icon }: StatCardProps) {
  const change = previous === undefined ? null : percentChange(value, previous);
  const meta = getStatCardMeta(label);

  return (
    <div className="card group relative flex flex-col justify-between p-4.5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md hover:border-primary/50">
      {/* Top Header: Label & Icon */}
      <div className="flex items-start justify-between gap-2">
        <p className="text-[11px] font-bold uppercase tracking-wider text-muted">
          {label}
        </p>
        <div
          className={`flex size-8 shrink-0 items-center justify-center rounded-xl border ${meta.bg} shadow-2xs group-hover:scale-110 transition-transform duration-200`}
        >
          {icon || meta.icon}
        </div>
      </div>

      {/* Main Metric Value */}
      <div className="mt-2">
        <p className="text-2xl sm:text-3xl font-black tracking-tight text-navy dark:text-foreground font-mono">
          {formatNumber(value)}
        </p>
      </div>

      {/* Delta vs Previous Period */}
      {change !== null ? (
        <div className="mt-3 flex items-center gap-1.5 pt-2 border-t border-border/50 text-[11px]">
          <span
            className={`inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 font-bold text-[10px] ${
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
      ) : (
        <div className="mt-3 pt-2 border-t border-border/50 text-[11px] text-muted/60">
          Statistik kumulatif
        </div>
      )}
    </div>
  );
}
