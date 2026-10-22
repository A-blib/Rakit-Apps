import Link from "next/link";
import type { Metadata } from "next";
import { BarChart } from "@/components/BarChart";
import { ExportReportToolbar } from "@/components/ExportReportToolbar";
import { StatCard } from "@/components/StatCard";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatNumber } from "@/lib/format";
import { PERIODS, parsePeriod } from "../../period";

export const metadata: Metadata = { title: "Lalu Lintas & Kinerja API" };

const METHOD_STYLE: Record<string, string> = {
  GET: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800",
  POST: "bg-sky-50 text-sky-700 border-sky-200 dark:bg-sky-950/40 dark:text-sky-300 dark:border-sky-800",
  PUT: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-800",
  PATCH: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-800",
  DELETE: "bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800",
};

export default async function TrafficPage({ searchParams }: PageProps<"/dashboard/traffic">) {
  const query = await searchParams;
  const period = parsePeriod(query.period);
  const statusFilter =
    query.status === "normal" || query.status === "error" || query.status === "throttled"
      ? query.status
      : "all";

  const traffic = await adminApi.traffic(await adminToken(), period);

  // Filter rute endpoint berdasarkan status kesehatan API
  const filteredRoutes = traffic.topRoutes.filter((route) => {
    if (statusFilter === "normal") {
      return route.errors === 0 && route.clientErrors === 0;
    }
    if (statusFilter === "error") {
      return route.errors > 0;
    }
    if (statusFilter === "throttled") {
      return route.clientErrors > 0;
    }
    return true;
  });

  const currentPeriodLabel = PERIODS.find((item) => item.value === period)?.label ?? period;

  const hrefForStatus = (s: string) => {
    const params = new URLSearchParams();
    params.set("period", period);
    if (s !== "all") params.set("status", s);
    return `?${params.toString()}`;
  };

  const trafficStats = [
    { label: "Total Request Diterima", current: traffic.requests, previous: 0 },
    { label: "Error Server (5xx)", current: traffic.errors, previous: 0 },
    { label: "Error Klien (4xx)", current: traffic.clientErrors, previous: 0 },
  ];

  const trafficSeries = [
    {
      label: "Request API Harian",
      points: traffic.days.map((d) => ({ date: d.date, count: d.requests })),
    },
  ];

  return (
    <div className="space-y-6 max-w-7xl">
      {/* Header Dokumen Cetak / Simpan PDF */}
      <div className="hidden print:block border-b-2 border-slate-900 pb-4 mb-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              OrgSpace — Laporan Lalu Lintas & Kinerja API
            </h1>
            <p className="text-xs text-slate-600 mt-1">
              Pemantauan beban endpoint, latensi rata-rata, dan tingkat error sistem.
            </p>
          </div>
          <div className="text-right text-xs text-slate-600 font-mono">
            <p className="font-semibold text-slate-900">Periode: {currentPeriodLabel} ({period})</p>
            <p>Total Request: {formatNumber(traffic.requests)}</p>
          </div>
        </div>
      </div>

      {/* Header Harian & Toolbar */}
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-foreground sm:text-2xl">
            Lalu Lintas & Kinerja API
          </h1>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <ExportReportToolbar
            periodLabel={currentPeriodLabel}
            periodValue={period}
            stats={trafficStats}
            timeseries={trafficSeries}
            filenamePrefix="laporan-lalu-lintas-api"
          />

          {/* Segmented Control Periode */}
          <nav className="inline-flex rounded-field border border-border bg-surface-subtle p-0.5" aria-label="Periode">
            {PERIODS.map((item) => (
              <Link
                key={item.value}
                href={`?period=${item.value}${statusFilter !== "all" ? `&status=${statusFilter}` : ""}`}
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
        </div>
      </header>

      {/* KPI Cards */}
      <section className="grid grid-cols-1 gap-3.5 sm:grid-cols-3">
        <StatCard label="Total Request Diterima" value={traffic.requests} />
        <StatCard
          label="Error Server (5xx)"
          value={traffic.errors}
          badge={
            traffic.errors > 0 ? (
              <span className="badge-error text-[10px] px-1.5 py-0.2 font-semibold">
                Memerlukan Investigasi
              </span>
            ) : (
              <span className="badge-success text-[10px] px-1.5 py-0.2 font-semibold">
                0% Error Server
              </span>
            )
          }
        />
        <StatCard
          label="Error Klien (4xx)"
          value={traffic.clientErrors}
          badge={
            traffic.clientErrors > 0 ? (
              <span className="badge-warning text-[10px] px-1.5 py-0.2 font-semibold">
                Throttled / Bad Request
              </span>
            ) : (
              <span className="badge-success text-[10px] px-1.5 py-0.2 font-semibold">
                Bersih
              </span>
            )
          }
        />
      </section>

      {/* Timeseries Bar / Line Chart */}
      <div className="card">
        <div className="border-b border-border/60 pb-3">
          <h2 className="text-sm font-semibold text-foreground">Volume Request per Hari</h2>
        </div>
        <div className="mt-4">
          <BarChart
            points={traffic.days.map((d) => ({ date: d.date, count: d.requests }))}
            label="Request per hari"
            color="violet"
          />
        </div>
      </div>

      {/* Top Endpoints Table dengan Filter Bar Status */}
      <div className="space-y-3">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <h2 className="text-xs font-semibold uppercase tracking-wider text-muted">
              Endpoint Paling Sering Dipanggil
            </h2>
            <p className="text-xs text-muted mt-0.5">
              Analisis beban request, latensi rata-rata, dan pemantauan kegagalan endpoint
            </p>
          </div>

          {/* Filter Bar: Semua | Normal (200 OK) | Error | Limit Tercapai (Throttled) */}
          <nav
            className="inline-flex rounded-xl border border-border bg-surface-subtle p-1 gap-1"
            aria-label="Filter Status Endpoint"
          >
            <Link
              href={hrefForStatus("all")}
              className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
                statusFilter === "all"
                  ? "bg-surface font-semibold text-foreground border border-border"
                  : "text-muted hover:text-foreground"
              }`}
            >
              Semua
            </Link>
            <Link
              href={hrefForStatus("normal")}
              className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors inline-flex items-center gap-1.5 ${
                statusFilter === "normal"
                  ? "bg-surface font-semibold text-emerald-700 dark:text-emerald-400 border border-border"
                  : "text-muted hover:text-emerald-600"
              }`}
            >
              <span className="size-2 rounded-full bg-emerald-500" />
              <span>Normal (200 OK)</span>
            </Link>
            <Link
              href={hrefForStatus("error")}
              className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors inline-flex items-center gap-1.5 ${
                statusFilter === "error"
                  ? "bg-surface font-semibold text-rose-700 dark:text-rose-400 border border-border"
                  : "text-muted hover:text-rose-600"
              }`}
            >
              <span className="size-2 rounded-full bg-rose-500" />
              <span>Error (5xx)</span>
            </Link>
            <Link
              href={hrefForStatus("throttled")}
              className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors inline-flex items-center gap-1.5 ${
                statusFilter === "throttled"
                  ? "bg-surface font-semibold text-amber-700 dark:text-amber-400 border border-border"
                  : "text-muted hover:text-amber-600"
              }`}
            >
              <span className="size-2 rounded-full bg-amber-500" />
              <span>Limit Tercapai (Throttled / 4xx)</span>
            </Link>
          </nav>
        </div>

        <div className="card overflow-hidden p-0">
          <div className="overflow-x-auto">
            <table className="table">
              <thead>
                <tr>
                  <th>Method & Route</th>
                  <th>Status Diagnostik</th>
                  <th className="text-right">Total Request</th>
                  <th className="text-right">5xx (Server)</th>
                  <th className="text-right">4xx (Klien)</th>
                  <th className="text-right">Rata-rata</th>
                  <th className="text-right">Maksimal</th>
                </tr>
              </thead>
              <tbody>
                {filteredRoutes.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="py-8 text-center text-xs text-muted">
                      Tidak ada endpoint yang cocok dengan kriteria filter status &ldquo;{statusFilter}&rdquo; pada periode ini.
                    </td>
                  </tr>
                ) : (
                  filteredRoutes.map((route) => {
                    const badgeClass =
                      METHOD_STYLE[route.method] ?? "bg-surface-subtle text-foreground border-border";

                    // Status di Tabel Sesuai Panduan Desain:
                    // Hijau: Normal (200 OK) | Kuning/Oranye: 4xx Throttled | Merah: 5xx Error
                    const hasServerError = route.errors > 0;
                    const hasClientError = route.clientErrors > 0;

                    return (
                      <tr key={`${route.method} ${route.route}`}>
                        <td>
                          <div className="flex items-center gap-2">
                            <span
                              className={`rounded border px-1.5 py-0.2 font-mono text-[10px] font-bold ${badgeClass}`}
                            >
                              {route.method}
                            </span>
                            <span className="font-mono text-xs text-foreground font-medium">
                              {route.route}
                            </span>
                          </div>
                        </td>

                        {/* Status Diagnostik Konsisten */}
                        <td>
                          {hasServerError ? (
                            <span className="badge-error text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                              <span className="size-1.5 rounded-full bg-rose-500" />
                              Error Server ({route.errors})
                            </span>
                          ) : hasClientError ? (
                            <span className="badge-warning text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                              <span className="size-1.5 rounded-full bg-amber-500" />
                              Throttled / 4xx ({route.clientErrors})
                            </span>
                          ) : (
                            <span className="badge-success text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                              <span className="size-1.5 rounded-full bg-emerald-500" />
                              Normal (200 OK)
                            </span>
                          )}
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
                        <td className="text-right font-mono text-xs">
                          {route.clientErrors > 0 ? (
                            <span className="font-bold text-amber-600 dark:text-amber-400">
                              {formatNumber(route.clientErrors)}
                            </span>
                          ) : (
                            <span className="text-muted/60">0</span>
                          )}
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
