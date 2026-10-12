import { formatNumber, percentChange } from "@/lib/format";

export function StatCard({ label, value, previous }: { label: string; value: number; previous?: number }) {
  const change = previous === undefined ? null : percentChange(value, previous);
  return (
    <div className="card">
      <p className="text-sm text-muted">{label}</p>
      <p className="mt-1 text-2xl font-bold">{formatNumber(value)}</p>
      {change !== null && (
        <p className={`mt-1 text-xs font-semibold ${change >= 0 ? "text-success" : "text-error"}`}>
          {change >= 0 ? "▲" : "▼"} {Math.abs(change)}% dari periode sebelumnya
        </p>
      )}
    </div>
  );
}
