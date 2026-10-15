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
    <div className="space-y-7 max-w-7xl">
      {/* Page Header with Segmented Period Control */}
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-5">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">
            Ringkasan Operasional
          </h1>
          <p className="mt-0.5 text-xs text-muted">
            Monitoring pertumbuhan pengguna, penerbitan website, dan aktivitas sistem.
          </p>
        </div>

        {/* Enterprise Segmented Control */}
        <nav className="inline-flex rounded-field border border-border bg-surface-subtle p-0.5" aria-label="Pilih Periode">
          {PERIODS.map((item) => {
            const isCurrent = item.value === period;
            return (
              <Link
                key={item.value}
                href={`?period=${item.value}`}
                className={`rounded-[6px] px-3 py-1.5 text-xs transition-all duration-150 ${
                  isCurrent
                    ? "bg-surface font-semibold text-foreground shadow-xs"
                    : "text-muted hover:text-foreground"
                }`}
              >
                {item.label}
              </Link>
            );
          })}
        </nav>
      </header>

      {/* KPI Stat Cards Grid */}
      <section aria-labelledby="section-kpi" className="space-y-3">
        <h2 id="section-kpi" className="text-xs font-semibold uppercase tracking-wider text-muted">
          Metrik Periode Berjalan
        </h2>
        <div className="grid grid-cols-2 gap-3.5 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard label="Pendaftar baru" value={p.newUsers.current} previous={p.newUsers.previous} />
          <StatCard label="Pengguna aktif" value={p.activeUsers.current} previous={p.activeUsers.previous} />
          <StatCard label="Website baru" value={p.newWebsites.current} previous={p.newWebsites.previous} />
          <StatCard label="Website diterbitkan" value={p.publishes.current} previous={p.publishes.previous} />
          <StatCard label="Organisasi baru" value={p.newOrganizations.current} previous={p.newOrganizations.previous} />
          <StatCard label="Template dipakai" value={p.templateDownloads.current} previous={p.templateDownloads.previous} />
          <StatCard label="Kritik & saran" value={p.feedback.current} previous={p.feedback.previous} />
          <StatCard label="Request API" value={p.apiRequests.current} previous={p.apiRequests.previous} />
        </div>
      </section>

      {/* Daily Timeseries Charts */}
      <section aria-labelledby="section-charts" className="space-y-3">
        <h2 id="section-charts" className="text-xs font-semibold uppercase tracking-wider text-muted">
          Aktivitas Harian
        </h2>
        <div className="grid gap-4 lg:grid-cols-2">
          {/* Signups Chart */}
          <div className="card flex flex-col justify-between">
            <div className="flex items-start justify-between border-b border-border/60 pb-3">
              <div>
                <h3 className="text-sm font-semibold text-foreground">Pendaftaran Pengguna</h3>
                <p className="text-xs text-muted">Tren pendaftaran akun per hari</p>
              </div>
              <span className="rounded-badge bg-surface-subtle px-2 py-0.5 text-xs font-bold font-mono text-navy dark:text-foreground border border-border">
                Total {formatNumber(signups.total)}
              </span>
            </div>
            <div className="mt-4">
              <BarChart points={signups.points} label="Pendaftar per hari" />
            </div>
          </div>

          {/* Active Users Chart */}
          <div className="card flex flex-col justify-between">
            <div className="flex items-start justify-between border-b border-border/60 pb-3">
              <div>
                <h3 className="text-sm font-semibold text-foreground">Pengguna Aktif</h3>
                <p className="text-xs text-muted">Aktivitas token & API berkala</p>
              </div>
              <div className="flex gap-2 text-[11px] text-muted">
                <span>Hari ini: <strong className="text-foreground font-mono">{formatNumber(overview.active.today)}</strong></span>
                <span>·</span>
                <span>7h: <strong className="text-foreground font-mono">{formatNumber(overview.active.last7Days)}</strong></span>
              </div>
            </div>
            <div className="mt-4">
              <BarChart points={active.points} label="Pengguna aktif per hari" />
            </div>
          </div>
        </div>
      </section>

      {/* Distribution & Cumulative Totals */}
      <section aria-labelledby="section-breakdown" className="space-y-3">
        <h2 id="section-breakdown" className="text-xs font-semibold uppercase tracking-wider text-muted">
          Distribusi & Status Kumulatif
        </h2>
        <div className="grid gap-4 lg:grid-cols-3">
          {/* Sign-up Methods */}
          <div className="card">
            <h3 className="text-sm font-semibold text-foreground border-b border-border/60 pb-2.5">
              Metode Autentikasi
            </h3>
            <dl className="mt-3 divide-y divide-border/50 text-xs">
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Email + Password</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(methods.email)}</dd>
              </div>
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Google OAuth</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(methods.google)}</dd>
              </div>
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">GitHub OAuth</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(methods.github)}</dd>
              </div>
            </dl>
          </div>

          {/* Active Platforms */}
          <div className="card">
            <h3 className="text-sm font-semibold text-foreground border-b border-border/60 pb-2.5">
              Platform Klien
            </h3>
            <dl className="mt-3 divide-y divide-border/50 text-xs">
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Aplikasi Android</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(platforms.android)}</dd>
              </div>
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Portal Web</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(platforms.web)}</dd>
              </div>
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Lainnya / API Langsung</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(platforms.other)}</dd>
              </div>
            </dl>
          </div>

          {/* Cumulative Totals */}
          <div className="card">
            <h3 className="text-sm font-semibold text-foreground border-b border-border/60 pb-2.5">
              Total Database Saat Ini
            </h3>
            <dl className="mt-3 divide-y divide-border/50 text-xs">
              <div className="flex items-center justify-between py-1.5">
                <dt className="text-muted">Total Pengguna Terdaftar</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(overview.totals.users)}</dd>
              </div>
              <div className="flex items-center justify-between py-1.5">
                <dt className="text-muted">Organisasi Aktif</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(overview.totals.organizations)}</dd>
              </div>
              <div className="flex items-center justify-between py-1.5">
                <dt className="text-muted">Proyek Website Dibuat</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(overview.totals.websites)}</dd>
              </div>
              <div className="flex items-center justify-between py-1.5">
                <dt className="text-muted">Website Online Diterbitkan</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(overview.totals.sitesOnline)}</dd>
              </div>
              <div className="flex items-center justify-between py-1.5">
                <dt className="text-muted">Template Tayang di Galeri</dt>
                <dd className="font-semibold font-mono text-foreground">{formatNumber(overview.totals.templatesPublished)}</dd>
              </div>
              <div className="flex items-center justify-between py-1.5">
                <dt className="text-muted">Feedback Belum Selesai</dt>
                <dd className="font-semibold font-mono text-foreground">
                  <span className={overview.totals.feedbackOpen > 0 ? "text-amber-600 font-bold" : ""}>
                    {formatNumber(overview.totals.feedbackOpen)}
                  </span>
                </dd>
              </div>
            </dl>
          </div>
        </div>
      </section>
    </div>
  );
}
