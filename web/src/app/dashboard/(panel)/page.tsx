import Link from "next/link";
import type { Metadata } from "next";
import { BarChart } from "@/components/BarChart";
import { StatCard } from "@/components/StatCard";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatNumber } from "@/lib/format";
import { PERIODS, parsePeriod } from "../period";

export const metadata: Metadata = { title: "Ringkasan" };

/** Monitoring utama: pertumbuhan pengguna, aktivitas, dan konten. Data diperbarui setiap halaman dibuka. */
export default async function OverviewPage({ searchParams }: PageProps<"/dashboard">) {
  const period = parsePeriod((await searchParams).period);
  const token = await adminToken();
  const [overview, signups, active, methods, platforms] = await Promise.all([
    adminApi.overview(token, period),
    adminApi.timeseries(token, "signups", period),
    adminApi.timeseries(token, "active_users", period),
    adminApi.signupMethods(token, period),
    adminApi.activePlatforms(token, period),
  ]);
  const p = overview.period;
  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-2xl font-bold">Ringkasan</h1>
        <nav className="flex gap-2" aria-label="Periode">
          {PERIODS.map((item) => (
            <Link key={item.value} href={`?period=${item.value}`}
                  className={item.value === period ? "badge" : "badge bg-transparent text-muted"}>
              {item.label}
            </Link>
          ))}
        </nav>
      </header>

      <section className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Pendaftar baru" value={p.newUsers.current} previous={p.newUsers.previous} />
        <StatCard label="Pengguna aktif" value={p.activeUsers.current} previous={p.activeUsers.previous} />
        <StatCard label="Website baru" value={p.newWebsites.current} previous={p.newWebsites.previous} />
        <StatCard label="Website diterbitkan" value={p.publishes.current} previous={p.publishes.previous} />
        <StatCard label="Organisasi baru" value={p.newOrganizations.current} previous={p.newOrganizations.previous} />
        <StatCard label="Template dipakai" value={p.templateDownloads.current} previous={p.templateDownloads.previous} />
        <StatCard label="Kritik & saran" value={p.feedback.current} previous={p.feedback.previous} />
        <StatCard label="Request API" value={p.apiRequests.current} previous={p.apiRequests.previous} />
      </section>

      <section className="grid gap-4 lg:grid-cols-2">
        <div className="card">
          <h2 className="font-semibold">Pendaftar per hari</h2>
          <p className="text-sm text-muted">Total {formatNumber(signups.total)}</p>
          <div className="mt-4"><BarChart points={signups.points} label="Pendaftar per hari" /></div>
        </div>
        <div className="card">
          <h2 className="font-semibold">Pengguna aktif per hari</h2>
          <p className="text-sm text-muted">
            Hari ini {formatNumber(overview.active.today)} · 7 hari {formatNumber(overview.active.last7Days)} · 30 hari{" "}
            {formatNumber(overview.active.last30Days)}
          </p>
          <div className="mt-4"><BarChart points={active.points} label="Pengguna aktif per hari" /></div>
        </div>
      </section>

      <section className="grid gap-4 lg:grid-cols-3">
        <div className="card">
          <h2 className="font-semibold">Metode daftar</h2>
          <dl className="mt-3 space-y-1 text-sm">
            <div className="flex justify-between"><dt>Email</dt><dd>{formatNumber(methods.email)}</dd></div>
            <div className="flex justify-between"><dt>Google</dt><dd>{formatNumber(methods.google)}</dd></div>
            <div className="flex justify-between"><dt>GitHub</dt><dd>{formatNumber(methods.github)}</dd></div>
          </dl>
        </div>
        <div className="card">
          <h2 className="font-semibold">Platform pengguna aktif</h2>
          <dl className="mt-3 space-y-1 text-sm">
            <div className="flex justify-between"><dt>Android</dt><dd>{formatNumber(platforms.android)}</dd></div>
            <div className="flex justify-between"><dt>Web</dt><dd>{formatNumber(platforms.web)}</dd></div>
            <div className="flex justify-between"><dt>Lainnya</dt><dd>{formatNumber(platforms.other)}</dd></div>
          </dl>
        </div>
        <div className="card">
          <h2 className="font-semibold">Total saat ini</h2>
          <dl className="mt-3 space-y-1 text-sm">
            <div className="flex justify-between"><dt>Pengguna</dt><dd>{formatNumber(overview.totals.users)}</dd></div>
            <div className="flex justify-between"><dt>Organisasi</dt><dd>{formatNumber(overview.totals.organizations)}</dd></div>
            <div className="flex justify-between"><dt>Website</dt><dd>{formatNumber(overview.totals.websites)}</dd></div>
            <div className="flex justify-between"><dt>Website online</dt><dd>{formatNumber(overview.totals.sitesOnline)}</dd></div>
            <div className="flex justify-between"><dt>Template tayang</dt><dd>{formatNumber(overview.totals.templatesPublished)}</dd></div>
            <div className="flex justify-between"><dt>Feedback terbuka</dt><dd>{formatNumber(overview.totals.feedbackOpen)}</dd></div>
          </dl>
        </div>
      </section>
    </div>
  );
}
