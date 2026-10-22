import Link from "next/link";
import type { Metadata } from "next";
import { BarChart } from "@/components/BarChart";
import { ExportReportToolbar } from "@/components/ExportReportToolbar";
import { StatCard } from "@/components/StatCard";
import { adminApi } from "@/lib/api/admin";
import { getPopularTemplates } from "@/lib/api/public";
import { adminToken } from "@/lib/auth/session";
import { formatNumber } from "@/lib/format";
import { PERIODS, parsePeriod } from "../period";

export const metadata: Metadata = { title: "Ringkasan Operasional" };

/** Monitoring utama: pertumbuhan pengguna, aktivitas, dan konten bergaya Linear / Vercel. */
export default async function OverviewPage({ searchParams }: PageProps<"/dashboard">) {
  const period = parsePeriod((await searchParams).period);
  const token = await adminToken();
  const [overview, signups, active, methods, platforms, feedbackCounts, popularTemplates] = await Promise.all([
    adminApi.overview(token, period),
    adminApi.timeseries(token, "signups", period),
    adminApi.timeseries(token, "active_users", period),
    adminApi.signupMethods(token, period),
    adminApi.activePlatforms(token, period),
    adminApi.feedbackCounts(token),
    getPopularTemplates(4).catch(() => ({ items: [], totalItems: 0, page: 0, size: 4, totalPages: 0 })),
  ]);
  const p = overview.period;

  // Perhitungan persentase metode autentikasi
  const totalMethods = Math.max(1, methods.email + methods.google + methods.github);
  const pctEmail = Math.round((methods.email / totalMethods) * 100);
  const pctGoogle = Math.round((methods.google / totalMethods) * 100);
  const pctGithub = Math.round((methods.github / totalMethods) * 100);

  // Perhitungan persentase platform klien
  const totalPlatforms = Math.max(1, platforms.android + platforms.web + platforms.other);
  const pctAndroid = Math.round((platforms.android / totalPlatforms) * 100);
  const pctWeb = Math.round((platforms.web / totalPlatforms) * 100);
  const pctOther = Math.round((platforms.other / totalPlatforms) * 100);

  // Perhitungan rasio kesuksesan publikasi website
  const newWebsites = p.newWebsites.current;
  const publishedWebsites = p.publishes.current;
  const publishSuccessRate = newWebsites > 0 ? Math.min(100, Math.round((publishedWebsites / newWebsites) * 100)) : 0;

  const currentPeriodLabel = PERIODS.find((item) => item.value === period)?.label ?? period;

  const statsForExport = [
    { label: "Pendaftar baru", current: p.newUsers.current, previous: p.newUsers.previous },
    { label: "Pengguna aktif", current: p.activeUsers.current, previous: p.activeUsers.previous },
    { label: "Website baru", current: p.newWebsites.current, previous: p.newWebsites.previous },
    { label: "Website diterbitkan", current: p.publishes.current, previous: p.publishes.previous },
    { label: "Rasio sukses deploy (%)", current: publishSuccessRate, previous: 0 },
    { label: "Organisasi baru", current: p.newOrganizations.current, previous: p.newOrganizations.previous },
    { label: "Template dipakai", current: p.templateDownloads.current, previous: p.templateDownloads.previous },
    { label: "Kritik & saran", current: p.feedback.current, previous: p.feedback.previous },
    { label: "Request API", current: p.apiRequests.current, previous: p.apiRequests.previous },
  ];

  const timeseriesForExport = [
    { label: "Pendaftar Baru", points: signups.points },
    { label: "Pengguna Aktif", points: active.points },
  ];

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header Dokumen Khusus Cetak / Simpan PDF */}
      <div className="hidden print:block border-b-2 border-slate-900 pb-4 mb-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">OrgSpace — Laporan Operasional Platform</h1>
            <p className="text-xs text-slate-600 mt-1">
              Laporan resmi metrik dan kinerja platform internal OrgSpace.
            </p>
          </div>
          <div className="text-right text-xs text-slate-600 font-mono">
            <p className="font-semibold text-slate-900">Periode: {currentPeriodLabel} ({period})</p>
            <p>Status: Ringkasan Resmi Internal</p>
          </div>
        </div>
      </div>

      {/* Page Header with Precision Segmented Period Control & Export Toolbar */}
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/80 pb-5">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-foreground">
              Ringkasan Operasional
            </h1>
            <span className="rounded-md bg-surface-subtle border border-border px-2 py-0.5 text-[10px] font-mono font-semibold text-muted">
              LIVE
            </span>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          {/* Export Toolbar (CSV & PDF) */}
          <ExportReportToolbar
            periodLabel={currentPeriodLabel}
            periodValue={period}
            stats={statsForExport}
            timeseries={timeseriesForExport}
          />

          {/* Linear-Style Segmented Control */}
          <nav
            className="inline-flex rounded-xl border border-border bg-surface-subtle p-1"
            aria-label="Pilih Periode Waktu"
          >
            {PERIODS.map((item) => {
              const isCurrent = item.value === period;
              return (
                <Link
                  key={item.value}
                  href={`?period=${item.value}`}
                  className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors duration-150 ${
                    isCurrent
                      ? "bg-surface font-semibold text-foreground border border-border dark:border-border"
                      : "text-muted hover:text-foreground"
                  }`}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </div>
      </header>

      {/* Tindakan Cepat (Quick Actions) */}
      <section aria-label="Tindakan Cepat Admin" className="flex flex-wrap items-center gap-2 print:hidden bg-surface-subtle/50 p-2.5 rounded-xl border border-border">
        <span className="text-[11px] font-semibold text-muted uppercase tracking-wider px-2">Tindakan Cepat:</span>
        <Link
          href="/dashboard/feedback?unread=true"
          className="btn-outline h-8 px-2.5 text-xs gap-1.5 hover:border-primary hover:text-primary transition-colors bg-surface"
        >
          <span className="size-2 rounded-full bg-amber-500" />
          <span>Tinjau Kritik & Saran</span>
          {feedbackCounts.unread > 0 && (
            <span className="rounded-full bg-amber-500/20 text-amber-700 dark:text-amber-300 px-1.5 py-0.2 text-[10px] font-bold font-mono">
              {feedbackCounts.unread} baru
            </span>
          )}
        </Link>
        <Link
          href="/dashboard/docs/new"
          className="btn-outline h-8 px-2.5 text-xs gap-1.5 hover:border-primary hover:text-primary transition-colors bg-surface"
        >
          <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
            <path d="M10.75 4.75a.75.75 0 00-1.5 0v4.5h-4.5a.75.75 0 000 1.5h4.5v4.5a.75.75 0 001.5 0v-4.5h4.5a.75.75 0 000-1.5h-4.5v-4.5z" />
          </svg>
          <span>Buat Panduan / Dokumen</span>
        </Link>
        <Link
          href="/dashboard/users"
          className="btn-outline h-8 px-2.5 text-xs gap-1.5 hover:border-primary hover:text-primary transition-colors bg-surface"
        >
          <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
            <path d="M7 8a3 3 0 100-6 3 3 0 000 6zM14.5 9a2.5 2.5 0 100-5 2.5 2.5 0 000 5zM1.615 16.428a1.224 1.224 0 01-.569-1.175 6.002 6.002 0 0111.908 0c.058.467-.172.92-.57 1.174A9.953 9.953 0 017 18a9.953 9.953 0 01-5.385-1.572zM14.5 16h-.106c.07-.297.088-.611.048-.933a7.47 7.47 0 00-1.588-3.755 4.502 4.502 0 015.874 4.163.75.75 0 01-.728.525z" />
          </svg>
          <span>Kelola Pengguna</span>
        </Link>
        <Link
          href="/dashboard/traffic"
          className="btn-outline h-8 px-2.5 text-xs gap-1.5 hover:border-primary hover:text-primary transition-colors bg-surface"
        >
          <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M12.315 2c2.43 0 4.609 1.027 6.14 2.68a.75.75 0 01-1.09 1.03 7.25 7.25 0 00-5.05-2.21c-3.738 0-6.84 2.808-7.21 6.428a.75.75 0 01-1.492-.152C4.053 5.568 7.808 2 12.315 2z" clipRule="evenodd" />
          </svg>
          <span>Kinerja & Error API</span>
        </Link>
      </section>

      {/* KPI Stat Cards Grid */}
      <section aria-labelledby="section-kpi" className="space-y-3.5">
        <div className="flex items-center justify-between">
          <h2 id="section-kpi" className="text-xs font-semibold uppercase tracking-wider text-muted">
            Metrik Periode Berjalan
          </h2>
          <span className="text-[11px] font-mono text-muted">
            Perbandingan vs periode sebelumnya
          </span>
        </div>

        <div className="grid grid-cols-2 gap-3.5 sm:grid-cols-3 lg:grid-cols-3">
          <StatCard label="Pendaftar baru" value={p.newUsers.current} previous={p.newUsers.previous} />
          <StatCard label="Pengguna aktif" value={p.activeUsers.current} previous={p.activeUsers.previous} />
          <StatCard label="Website baru" value={p.newWebsites.current} previous={p.newWebsites.previous} />
          <StatCard label="Website diterbitkan" value={p.publishes.current} previous={p.publishes.previous} />
          <StatCard
            label="Rasio Sukses Deploy"
            value={`${publishSuccessRate}%`}
            badge={
              <span className="text-[10px] text-muted font-mono">
                {publishedWebsites} dari {newWebsites} website telah online
              </span>
            }
          />
          <StatCard label="Organisasi baru" value={p.newOrganizations.current} previous={p.newOrganizations.previous} />
          <StatCard label="Template dipakai" value={p.templateDownloads.current} previous={p.templateDownloads.previous} />
          <StatCard
            label="Kritik & saran"
            value={p.feedback.current}
            previous={p.feedback.previous}
            badge={
              <div className="flex flex-wrap items-center gap-1 text-[10px]">
                <span className="rounded bg-amber-500/10 text-amber-700 dark:text-amber-400 border border-amber-500/20 px-1 py-0.2 font-semibold">
                  {feedbackCounts.newCount} Baru
                </span>
                <span className="rounded bg-sky-500/10 text-sky-700 dark:text-sky-400 border border-sky-500/20 px-1 py-0.2 font-semibold">
                  {feedbackCounts.inProgress} Proses
                </span>
                <span className="rounded bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border border-emerald-500/20 px-1 py-0.2 font-semibold">
                  {feedbackCounts.resolved} Selesai
                </span>
              </div>
            }
          />
          <StatCard label="Request API" value={p.apiRequests.current} previous={p.apiRequests.previous} />
        </div>
      </section>

      {/* Daily Timeseries Charts */}
      <section aria-labelledby="section-charts" className="space-y-3.5">
        <h2 id="section-charts" className="text-xs font-semibold uppercase tracking-wider text-muted">
          Tren Aktivitas Harian
        </h2>

        <div className="grid gap-4.5 lg:grid-cols-2">
          {/* Signups Chart Card */}
          <div className="rounded-xl border border-border bg-surface p-5 transition-colors duration-150 hover:border-border-strong">
            <div className="flex items-start justify-between border-b border-border pb-3.5">
              <div>
                <h3 className="text-sm font-semibold text-foreground">Pendaftaran Pengguna</h3>
                <p className="text-xs text-muted mt-0.5">Tren registrasi akun harian</p>
              </div>
              <span className="inline-flex items-center gap-1.5 rounded-lg border border-border bg-surface-subtle px-2.5 py-1 text-xs font-mono font-semibold text-foreground">
                <span className="text-muted text-[10px]">TOTAL</span>
                {formatNumber(signups.total)}
              </span>
            </div>
            <div className="mt-4">
              <BarChart points={signups.points} label="Pendaftar per hari" color="sky" />
            </div>
          </div>

          {/* Active Users Chart Card */}
          <div className="rounded-xl border border-border bg-surface p-5 transition-colors duration-150 hover:border-border-strong">
            <div className="flex items-start justify-between border-b border-border pb-3.5">
              <div>
                <h3 className="text-sm font-semibold text-foreground">Pengguna Aktif</h3>
                <p className="text-xs text-muted mt-0.5">Aktivitas akses token & API berkala</p>
              </div>
              <div className="flex items-center gap-2 rounded-lg border border-border bg-surface-subtle px-2.5 py-1 text-xs font-mono">
                <span className="text-muted text-[10px]">HARI INI</span>
                <span className="font-semibold text-foreground">{formatNumber(overview.active.today)}</span>
              </div>
            </div>
            <div className="mt-4">
              <BarChart points={active.points} label="Pengguna aktif per hari" color="emerald" />
            </div>
          </div>
        </div>
      </section>

      {/* Breakdown & Cumulative Status */}
      <section aria-labelledby="section-breakdown" className="space-y-3.5">
        <h2 id="section-breakdown" className="text-xs font-semibold uppercase tracking-wider text-muted">
          Distribusi & Status Kumulatif
        </h2>

        <div className="grid gap-4.5 lg:grid-cols-3">
          {/* Sign-up Methods Card with Visual Segmented Progress Bar */}
          <div className="rounded-xl border border-border bg-surface p-5 transition-colors duration-150 hover:border-border-strong flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between border-b border-border pb-3">
                <h3 className="text-sm font-semibold text-foreground">Metode Autentikasi</h3>
                <span className="text-[10px] font-mono text-muted">{formatNumber(totalMethods)} total</span>
              </div>

              {/* Segmented Progress Bar dengan warna nyaman di mata */}
              <div className="mt-4 flex h-2 w-full overflow-hidden rounded-full bg-surface-subtle">
                <div style={{ width: `${pctEmail}%` }} className="bg-sky-500 transition-colors" title={`Email: ${pctEmail}%`} />
                <div style={{ width: `${pctGoogle}%` }} className="bg-blue-600 transition-colors" title={`Google: ${pctGoogle}%`} />
                <div style={{ width: `${pctGithub}%` }} className="bg-violet-500 transition-colors" title={`GitHub: ${pctGithub}%`} />
              </div>

              {/* Distribution Legend List */}
              <dl className="mt-4 divide-y divide-border/60 text-xs">
                <div className="flex items-center justify-between py-2">
                  <div className="flex items-center gap-2">
                    <span className="size-2 rounded-full bg-sky-500 shrink-0" />
                    <dt className="text-muted">Email + Password</dt>
                  </div>
                  <dd className="font-semibold font-mono text-foreground">
                    {formatNumber(methods.email)} <span className="text-muted font-normal text-[10px]">({pctEmail}%)</span>
                  </dd>
                </div>
                <div className="flex items-center justify-between py-2">
                  <div className="flex items-center gap-2">
                    <span className="size-2 rounded-full bg-blue-600 shrink-0" />
                    <dt className="text-muted">Google OAuth</dt>
                  </div>
                  <dd className="font-semibold font-mono text-foreground">
                    {formatNumber(methods.google)} <span className="text-muted font-normal text-[10px]">({pctGoogle}%)</span>
                  </dd>
                </div>
                <div className="flex items-center justify-between py-2">
                  <div className="flex items-center gap-2">
                    <span className="size-2 rounded-full bg-violet-500 shrink-0" />
                    <dt className="text-muted">GitHub OAuth</dt>
                  </div>
                  <dd className="font-semibold font-mono text-foreground">
                    {formatNumber(methods.github)} <span className="text-muted font-normal text-[10px]">({pctGithub}%)</span>
                  </dd>
                </div>
              </dl>
            </div>
          </div>

          {/* Active Platforms Card with Visual Segmented Progress Bar */}
          <div className="rounded-xl border border-border bg-surface p-5 transition-colors duration-150 hover:border-border-strong flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between border-b border-border pb-3">
                <h3 className="text-sm font-semibold text-foreground">Platform Klien</h3>
                <span className="text-[10px] font-mono text-muted">{formatNumber(totalPlatforms)} request</span>
              </div>

              {/* Segmented Progress Bar */}
              <div className="mt-4 flex h-2 w-full overflow-hidden rounded-full bg-surface-subtle">
                <div style={{ width: `${pctAndroid}%` }} className="bg-emerald-500 transition-colors" title={`Android: ${pctAndroid}%`} />
                <div style={{ width: `${pctWeb}%` }} className="bg-sky-500 transition-colors" title={`Web: ${pctWeb}%`} />
                <div style={{ width: `${pctOther}%` }} className="bg-amber-500 transition-colors" title={`Lainnya: ${pctOther}%`} />
              </div>

              {/* Distribution Legend List */}
              <dl className="mt-4 divide-y divide-border/60 text-xs">
                <div className="flex items-center justify-between py-2">
                  <div className="flex items-center gap-2">
                    <span className="size-2 rounded-full bg-emerald-500 shrink-0" />
                    <dt className="text-muted">Aplikasi Android</dt>
                  </div>
                  <dd className="font-semibold font-mono text-foreground">
                    {formatNumber(platforms.android)} <span className="text-muted font-normal text-[10px]">({pctAndroid}%)</span>
                  </dd>
                </div>
                <div className="flex items-center justify-between py-2">
                  <div className="flex items-center gap-2">
                    <span className="size-2 rounded-full bg-sky-500 shrink-0" />
                    <dt className="text-muted">Portal Web</dt>
                  </div>
                  <dd className="font-semibold font-mono text-foreground">
                    {formatNumber(platforms.web)} <span className="text-muted font-normal text-[10px]">({pctWeb}%)</span>
                  </dd>
                </div>
                <div className="flex items-center justify-between py-2">
                  <div className="flex items-center gap-2">
                    <span className="size-2 rounded-full bg-amber-500 shrink-0" />
                    <dt className="text-muted">Lainnya / API Langsung</dt>
                  </div>
                  <dd className="font-semibold font-mono text-foreground">
                    {formatNumber(platforms.other)} <span className="text-muted font-normal text-[10px]">({pctOther}%)</span>
                  </dd>
                </div>
              </dl>
            </div>
          </div>

          {/* Cumulative Totals Card */}
          <div className="rounded-xl border border-border bg-surface p-5 transition-colors duration-150 hover:border-border-strong flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between border-b border-border pb-3">
                <h3 className="text-sm font-semibold text-foreground">Total Database</h3>
                <span className="rounded bg-surface-subtle border border-border px-1.5 py-0.5 text-[10px] font-mono text-muted uppercase">
                  Kumulatif
                </span>
              </div>

              <dl className="mt-3 divide-y divide-border/60 text-xs">
                <div className="flex items-center justify-between py-2">
                  <dt className="text-muted">Pengguna Terdaftar</dt>
                  <dd className="font-semibold font-mono text-foreground">{formatNumber(overview.totals.users)}</dd>
                </div>
                <div className="flex items-center justify-between py-2">
                  <dt className="text-muted">Organisasi Aktif</dt>
                  <dd className="font-semibold font-mono text-foreground">{formatNumber(overview.totals.organizations)}</dd>
                </div>
                <div className="flex items-center justify-between py-2">
                  <dt className="text-muted">Proyek Website</dt>
                  <dd className="font-semibold font-mono text-foreground">{formatNumber(overview.totals.websites)}</dd>
                </div>
                <div className="flex items-center justify-between py-2">
                  <dt className="text-muted">Website Online</dt>
                  <dd className="font-semibold font-mono text-foreground flex items-center gap-1.5">
                    <span className="size-1.5 rounded-full bg-emerald-500" />
                    {formatNumber(overview.totals.sitesOnline)}
                  </dd>
                </div>
                <div className="flex items-center justify-between py-2">
                  <dt className="text-muted">Template Galeri</dt>
                  <dd className="font-semibold font-mono text-foreground">{formatNumber(overview.totals.templatesPublished)}</dd>
                </div>
                <div className="flex items-center justify-between py-2">
                  <dt className="text-muted">Feedback Belum Selesai</dt>
                  <dd className="font-semibold font-mono text-foreground">
                    {overview.totals.feedbackOpen > 0 ? (
                      <span className="rounded-md bg-amber-500/10 text-amber-700 dark:text-amber-400 border border-amber-500/20 px-1.5 py-0.5 font-bold">
                        {formatNumber(overview.totals.feedbackOpen)} terbuka
                      </span>
                    ) : (
                      <span className="text-muted">0</span>
                    )}
                  </dd>
                </div>
              </dl>
            </div>
          </div>
        </div>
      </section>

      {/* Grid: Top Performa Template & Log Aktivitas Terkini */}
      <section aria-labelledby="section-insights" className="grid gap-4.5 lg:grid-cols-2">
        {/* Top Performa Template Widget */}
        <div className="rounded-xl border border-border bg-surface p-5 transition-colors duration-150 hover:border-border-strong flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between border-b border-border pb-3">
              <div>
                <h3 className="text-sm font-semibold text-foreground">Top Performa Template</h3>
                <p className="text-xs text-muted mt-0.5">Template UI yang paling diminati & dipakai komunitas</p>
              </div>
              <span className="rounded bg-surface-subtle border border-border px-2 py-0.5 text-[10px] font-mono text-muted uppercase">
                Populer
              </span>
            </div>

            {popularTemplates.items.length === 0 ? (
              <div className="mt-6 flex flex-col items-center justify-center rounded-xl border border-dashed border-border/80 bg-surface-subtle/30 py-8 text-center">
                <svg className="size-8 text-muted/60" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z" />
                </svg>
                <p className="mt-2 text-xs font-medium text-foreground">Belum ada template yang dipublikasikan</p>
                <p className="text-[11px] text-muted max-w-xs mt-0.5">
                  Saat penyedia template merilis desain ke galeri, statistik unduhan dan tayangan akan muncul di sini.
                </p>
              </div>
            ) : (
              <ul className="mt-3.5 divide-y divide-border/60 text-xs">
                {popularTemplates.items.map((tpl, idx) => (
                  <li key={tpl.id} className="flex items-center justify-between py-2.5">
                    <div className="flex items-center gap-2.5 min-w-0">
                      <span className="flex size-5.5 shrink-0 items-center justify-center rounded-full bg-surface-subtle text-[10px] font-bold font-mono text-muted border border-border">
                        {idx + 1}
                      </span>
                      <div className="truncate">
                        <p className="font-semibold text-foreground truncate">{tpl.name}</p>
                        <p className="text-[10px] text-muted truncate">Oleh {tpl.creatorName}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-3 shrink-0 font-mono text-[11px]">
                      <span className="text-muted" title="Jumlah unduhan">
                        📥 {formatNumber(tpl.downloads)}
                      </span>
                      <span className="rounded bg-primary-container text-on-primary-container px-1.5 py-0.5 text-[10px] font-semibold">
                        {tpl.category}
                      </span>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="mt-4 pt-3 border-t border-border/60 flex items-center justify-between text-[11px] text-muted">
            <span>Total Template: <strong className="text-foreground">{formatNumber(overview.totals.templatesPublished)}</strong></span>
            <span>Total Pemakaian: <strong className="text-foreground">{formatNumber(p.templateDownloads.current)}</strong></span>
          </div>
        </div>

        {/* Log Aktivitas Terkini (Recent Activity Feed) */}
        <div className="rounded-xl border border-border bg-surface p-5 transition-colors duration-150 hover:border-border-strong flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between border-b border-border pb-3">
              <div>
                <h3 className="text-sm font-semibold text-foreground">Log Aktivitas Terkini</h3>
                <p className="text-xs text-muted mt-0.5">Feed timeline aksi penting dan status operasional sistem</p>
              </div>
              <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800 px-2 py-0.5 text-[10px] font-mono font-semibold">
                <span className="size-1.5 rounded-full bg-emerald-500 animate-pulse" />
                REAL-TIME
              </span>
            </div>

            {/* Timeline Feed */}
            <div className="mt-3.5 space-y-3">
              <div className="flex items-start gap-3 text-xs">
                <div className="mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
                  <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-foreground font-medium">
                    Sistem operasional berjalan normal
                  </p>
                  <p className="text-[11px] text-muted">
                    {formatNumber(overview.totals.users)} akun terdaftar &bull; {formatNumber(overview.totals.sitesOnline)} website aktif online
                  </p>
                </div>
                <span className="text-[10px] font-mono text-muted shrink-0">Hari ini</span>
              </div>

              <div className="flex items-start gap-3 text-xs">
                <div className="mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full bg-sky-500/10 text-sky-600 dark:text-sky-400">
                  <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" />
                  </svg>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-foreground font-medium">
                    {formatNumber(p.newUsers.current)} registrasi pengguna baru
                  </p>
                  <p className="text-[11px] text-muted">
                    Pertumbuhan akun pada periode {currentPeriodLabel.toLowerCase()}
                  </p>
                </div>
                <span className="text-[10px] font-mono text-muted shrink-0">Periode aktif</span>
              </div>

              <div className="flex items-start gap-3 text-xs">
                <div className="mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full bg-amber-500/10 text-amber-600 dark:text-amber-400">
                  <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zm-5 0H8v2h2V9z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-foreground font-medium">
                    {feedbackCounts.newCount} kritik & saran baru menunggu ditinjau
                  </p>
                  <p className="text-[11px] text-muted">
                    Total {feedbackCounts.all} tiket masuk &bull; {feedbackCounts.resolved} berhasil diselesaikan
                  </p>
                </div>
                <span className="text-[10px] font-mono text-muted shrink-0">
                  {feedbackCounts.unread > 0 ? "Belum dibaca" : "Terkendali"}
                </span>
              </div>

              <div className="flex items-start gap-3 text-xs">
                <div className="mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full bg-purple-500/10 text-purple-600 dark:text-purple-400">
                  <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M12.315 2c2.43 0 4.609 1.027 6.14 2.68a.75.75 0 01-1.09 1.03 7.25 7.25 0 00-5.05-2.21c-3.738 0-6.84 2.808-7.21 6.428a.75.75 0 01-1.492-.152C4.053 5.568 7.808 2 12.315 2z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-foreground font-medium">
                    Sinkronisasi telemetri API & pengguna harian
                  </p>
                  <p className="text-[11px] text-muted">
                    Pencatatan statistik otomatis memori ke PostgreSQL setiap 60 detik
                  </p>
                </div>
                <span className="text-[10px] font-mono text-muted shrink-0">Otomatis</span>
              </div>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-border/60 flex items-center justify-between text-[11px]">
            <span className="text-muted">Aktivitas organisasi & deployment</span>
            <Link href="/dashboard/feedback" className="text-primary hover:underline font-medium">
              Lihat Kritik & Saran &rarr;
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
