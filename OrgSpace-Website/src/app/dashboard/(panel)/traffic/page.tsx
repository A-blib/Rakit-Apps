import Link from "next/link";
import type { Metadata } from "next";
import { BarChart } from "@/components/BarChart";
import { StatCard } from "@/components/StatCard";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatNumber } from "@/lib/format";
import { PERIODS, parsePeriod } from "../../period";

export const metadata: Metadata = { title: "Lalu lintas API" };

export default async function TrafficPage({ searchParams }: PageProps<"/dashboard/traffic">) {
  const period = parsePeriod((await searchParams).period);
  const traffic = await adminApi.traffic(await adminToken(), period);
  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">Lalu lintas API</h1>
          <p className="text-sm text-muted">Data hari ini bisa tertinggal ±1 menit (ditulis berkala oleh backend).</p>
        </div>
        <nav className="flex gap-2">
          {PERIODS.map((item) => (
            <Link key={item.value} href={`?period=${item.value}`}
                  className={item.value === period ? "badge" : "badge bg-transparent text-muted"}>{item.label}</Link>
          ))}
        </nav>
      </header>
      <section className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard label="Request" value={traffic.requests} />
        <StatCard label="Error server (5xx)" value={traffic.errors} />
        <StatCard label="Error klien (4xx)" value={traffic.clientErrors} />
      </section>
      <div className="card">
        <h2 className="font-semibold">Request per hari</h2>
        <div className="mt-4">
          <BarChart points={traffic.days.map((d) => ({ date: d.date, count: d.requests }))} label="Request per hari" />
        </div>
      </div>
      <div className="card overflow-x-auto p-0">
        <table className="table">
          <thead>
            <tr><th>Endpoint</th><th>Request</th><th>5xx</th><th>4xx</th><th>Rata-rata</th><th>Terlama</th></tr>
          </thead>
          <tbody>
            {traffic.topRoutes.map((route) => (
              <tr key={`${route.method} ${route.route}`}>
                <td className="font-mono text-xs">{route.method} {route.route}</td>
                <td>{formatNumber(route.requests)}</td>
                <td className={route.errors > 0 ? "text-error" : undefined}>{formatNumber(route.errors)}</td>
                <td>{formatNumber(route.clientErrors)}</td>
                <td>{route.avgMs} ms</td>
                <td>{route.maxMs} ms</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
