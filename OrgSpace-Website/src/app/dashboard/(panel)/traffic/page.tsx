import Link from "next/link";
import type { Metadata } from "next";
import { BarChart } from "@/components/BarChart";
import { StatCard } from "@/components/StatCard";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatNumber } from "@/lib/format";
import { PERIODS, parsePeriod } from "../../period";

export const metadata: Metadata = { title: "Lalu lintas API" };

const METHOD_STYLE: Record<string, string> = {
  GET: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800",
  POST: "bg-sky-50 text-sky-700 border-sky-200 dark:bg-sky-950/40 dark:text-sky-300 dark:border-sky-800",
  PUT: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-800",
  PATCH: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-800",
  DELETE: "bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800",
};

export default async function TrafficPage({ searchParams }: PageProps<"/dashboard/traffic">) {
  const period = parsePeriod((await searchParams).period);
  const traffic = await adminApi.traffic(await adminToken(), period);

  return (
    <div className="space-y-6 max-w-7xl">
      {/* Header */}
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">
            Lalu Lintas & Kinerja API
          </h1>
          <p className="mt-1 text-xs text-muted">
            Volume pemanggilan endpoint, latensi rata-rata, dan pemantauan error server.
          </p>
        </div>

        {/* Segmented Control */}
        <nav className="inline-flex rounded-field border border-border bg-surface-subtle p-0.5" aria-label="Periode">
          {PERIODS.map((item) => (
            <Link
              key={item.value}
              href={`?period=${item.value}`}
              className={`rounded-[6px] px-3 py-1.5 text-xs transition-all ${
                item.value === period
                  ? "bg-surface font-semibold text-foreground shadow-xs"
                  : "text-muted hover:text-foreground"
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </header>

      {/* KPI Cards */}
      <section className="grid grid-cols-1 gap-3.5 sm:grid-cols-3">
        <StatCard label="Total Request Diterima" value={traffic.requests} />
        <StatCard label="Error Server (5xx)" value={traffic.errors} />
        <StatCard label="Error Klien (4xx)" value={traffic.clientErrors} />
      </section>

      {/* Timeseries Bar Chart */}
      <div className="card">
        <div className="border-b border-border/60 pb-3">
          <h2 className="text-sm font-semibold text-foreground">Volume Request per Hari</h2>
          <p className="text-xs text-muted">Fluktuasi beban request API sepanjang periode yang dipilih</p>
        </div>
        <div className="mt-4">
          <BarChart
            points={traffic.days.map((d) => ({ date: d.date, count: d.requests }))}
            label="Request per hari"
          />
        </div>
      </div>

      {/* Top Endpoints Table */}
      <div className="space-y-2.5">
        <h2 className="text-xs font-semibold uppercase tracking-wider text-muted">
          Endpoint Paling Sering Dipanggil
        </h2>
        <div className="card overflow-hidden p-0">
          <div className="overflow-x-auto">
            <table className="table">
              <thead>
                <tr>
                  <th>Method & Route</th>
                  <th className="text-right">Total Request</th>
                  <th className="text-right">5xx (Server)</th>
                  <th className="text-right">4xx (Klien)</th>
                  <th className="text-right">Rata-rata</th>
                  <th className="text-right">Maksimal</th>
                </tr>
              </thead>
              <tbody>
                {traffic.topRoutes.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-xs text-muted">
                      Belum ada data request pada periode ini.
                    </td>
                  </tr>
                ) : (
                  traffic.topRoutes.map((route) => {
                    const badgeClass = METHOD_STYLE[route.method] ?? "bg-surface-subtle text-foreground border-border";
                    return (
                      <tr key={`${route.method} ${route.route}`}>
                        <td>
                          <div className="flex items-center gap-2">
                            <span className={`rounded border px-1.5 py-0.2 font-mono text-[10px] font-bold ${badgeClass}`}>
                              {route.method}
                            </span>
                            <span className="font-mono text-xs text-foreground font-medium">
                              {route.route}
                            </span>
                          </div>
                        </td>
                        <td className="text-right font-mono text-xs font-semibold">
                          {formatNumber(route.requests)}
                        </td>
                        <td className="text-right font-mono text-xs">
                          {route.errors > 0 ? (
                            <span className="font-bold text-rose-600 dark:text-rose-400">
                              {formatNumber(route.errors)}
                            </span>
                          ) : (
                            <span className="text-muted/60">0</span>
                          )}
                        </td>
                        <td className="text-right font-mono text-xs text-muted">
                          {formatNumber(route.clientErrors)}
                        </td>
                        <td className="text-right font-mono text-xs text-muted">
                          {route.avgMs} ms
                        </td>
                        <td className="text-right font-mono text-xs text-muted">
                          {route.maxMs} ms
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
