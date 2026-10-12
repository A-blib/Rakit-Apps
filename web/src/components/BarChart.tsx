import { formatDay, formatNumber } from "@/lib/format";
import type { DailyPoint } from "@/lib/api/types";

/**
 * Grafik batang harian sederhana tanpa library (fondasi). Tim UI boleh menggantinya dengan library grafik setelah
 * dibahas dengan Aris; bentuk datanya (DailyPoint[]) tetap.
 */
export function BarChart({ points, label }: { points: DailyPoint[]; label: string }) {
  const max = Math.max(1, ...points.map((p) => p.count));
  return (
    <figure aria-label={label}>
      <div className="flex h-36 items-end gap-0.5">
        {points.map((p) => (
          <div
            key={p.date}
            title={`${formatDay(p.date)}: ${formatNumber(p.count)}`}
            className="flex-1 rounded-t bg-primary"
            style={{ height: `${Math.max(2, (p.count / max) * 100)}%` }}
          />
        ))}
      </div>
      {points.length > 0 && (
        <figcaption className="mt-1 flex justify-between text-xs text-muted">
          <span>{formatDay(points[0].date)}</span>
          <span>{formatDay(points[points.length - 1].date)}</span>
        </figcaption>
      )}
    </figure>
  );
}
