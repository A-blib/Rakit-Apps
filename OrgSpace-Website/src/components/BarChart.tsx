import { formatDay, formatNumber } from "@/lib/format";
import type { DailyPoint } from "@/lib/api/types";

/**
 * Grafik batang metrik harian Clean Enterprise dengan hover state dan garis grid halus.
 */
export function BarChart({ points, label }: { points: DailyPoint[]; label: string }) {
  if (!points || points.length === 0) {
    return (
      <div className="flex h-36 items-center justify-center rounded-field border border-dashed border-border text-xs text-muted">
        Tidak ada data untuk periode ini
      </div>
    );
  }

  const max = Math.max(1, ...points.map((p) => p.count));
  const midIndex = Math.floor(points.length / 2);

  return (
    <figure aria-label={label} className="w-full">
      {/* Chart container with horizontal subtle grid lines */}
      <div className="relative flex h-40 w-full items-end gap-1 pt-4 pb-1 border-b border-border">
        {/* Subtle reference line at 50% and 100% */}
        <div className="pointer-events-none absolute inset-x-0 top-4 border-b border-border/30" />
        <div className="pointer-events-none absolute inset-x-0 top-1/2 border-b border-border/30" />

        {points.map((p) => {
          const heightPercent = Math.max(3, (p.count / max) * 100);
          return (
            <div
              key={p.date}
              className="group relative flex-1 h-full flex items-end"
            >
              <div
                tabIndex={0}
                aria-label={`${formatDay(p.date)}: ${formatNumber(p.count)}`}
                className="w-full rounded-t-xs bg-primary/85 transition-all duration-150 group-hover:bg-navy dark:group-hover:bg-cyan focus:bg-navy"
                style={{ height: `${heightPercent}%` }}
              />
              {/* Tooltip on hover */}
              <div className="pointer-events-none absolute -top-8 left-1/2 -translate-x-1/2 z-20 hidden rounded bg-deep-navy px-1.5 py-0.5 text-[10px] font-medium text-white shadow-xs group-hover:block whitespace-nowrap">
                {formatDay(p.date)}: {formatNumber(p.count)}
              </div>
            </div>
          );
        })}
      </div>

      {/* Axis dates */}
      <figcaption className="mt-2 flex items-center justify-between text-[11px] text-muted">
        <span>{formatDay(points[0].date)}</span>
        {points.length > 2 && <span>{formatDay(points[midIndex].date)}</span>}
        <span>{formatDay(points[points.length - 1].date)}</span>
      </figcaption>
    </figure>
  );
}
