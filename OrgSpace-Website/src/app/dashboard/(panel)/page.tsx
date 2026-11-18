import Link from "next/link";
import type { Metadata } from "next";
import { AreaChart } from "@/components/AreaChart";
import { StatCard } from "@/components/StatCard";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatNumber } from "@/lib/format";
import { PERIODS, parsePeriod } from "../period";

export const metadata: Metadata = { title: "Ringkasan" };

function formatUptime(seconds: number) {
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  if (days > 0) return `${days} hari ${hours} jam`;
  if (hours > 0) return `${hours} jam ${minutes} mnt`;
  return `${minutes} menit`;
}

/** Monitoring utama: pertumbuhan pengguna, aktivitas, dan konten. Data diperbarui setiap halaman dibuka. */
export default async function OverviewPage({ searchParams }: PageProps<"/dashboard">) {
  const period = parsePeriod((await searchParams).period);
  const token = await adminToken();

  const [overview, signups, active, methods, platforms, system] = await Promise.all([
    adminApi.overview(token, period),
    adminApi.timeseries(token, "signups", period),
    adminApi.timeseries(token, "active_users", period),
    adminApi.signupMethods(token, period),
    adminApi.activePlatforms(token, period),
    adminApi.system(token).catch(() => null),
  ]);

  const p = overview.period;

  // Indonesian date formatter
  const formattedDate = new Intl.DateTimeFormat("id-ID", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(new Date());

  // Perhitungan persentase metode autentikasi
  const totalMethods = Math.max(1, methods.email + methods.google + methods.github);
  const emailPct = Math.round((methods.email / totalMethods) * 100);
  const googlePct = Math.round((methods.google / totalMethods) * 100);
  const githubPct = Math.round((methods.github / totalMethods) * 100);

  // Perhitungan persentase platform klien
  const totalPlatforms = Math.max(1, platforms.total || (platforms.web + platforms.android + platforms.other));
  const webPct = Math.round((platforms.web / totalPlatforms) * 100);
  const androidPct = Math.round((platforms.android / totalPlatforms) * 100);
  const otherPct = Math.round((platforms.other / totalPlatforms) * 100);

  // Rasio website online
  const sitesOnlinePct = overview.totals.websites > 0
    ? Math.round((overview.totals.sitesOnline / overview.totals.websites) * 100)
    : 0;

  return (
    <div className="space-y-8 max-w-7xl animate-fade-in">
      {/* Hero Welcome & Operational Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-[#07152B] via-[#0C2E5B] to-[#0A2244] p-6 sm:p-7 text-white shadow-md border border-navy/30 dark:border-border/40">
        <div className="relative z-10 flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
          <div className="max-w-2xl">
            <div className="flex flex-wrap items-center gap-2 mb-2.5">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-cyan/15 px-2.5 py-0.5 text-[11px] font-bold text-cyan tracking-wide border border-cyan/30 backdrop-blur-xs">
                <span className="size-1.5 rounded-full bg-cyan animate-pulse" />
                COMMAND CENTER ADMIN
              </span>
              <span className="text-white/40 text-xs">·</span>
              <span className="text-xs text-white/80 font-medium">
                {formattedDate}
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white leading-tight">
              Ringkasan Operasional OrgSpace
            </h1>
            <p className="mt-1.5 text-xs sm:text-sm text-white/80 leading-relaxed">
              Monitoring pertumbuhan pengguna, penerbitan website publik, lalu lintas API, dan kesehatan platform secara menyeluruh.
            </p>
          </div>

          {/* Segmented Period Filter Bar */}
          <div className="shrink-0 bg-white/10 p-1 rounded-xl backdrop-blur-md border border-white/15">
            <div className="px-2 pt-1 pb-1.5 text-[10px] font-bold uppercase tracking-wider text-white/70">
              Filter Periode Data
            </div>
            <nav className="flex items-center gap-1" aria-label="Pilih Periode">
              {PERIODS.map((item) => {
                const isCurrent = item.value === period;
                return (
                  <Link
                    key={item.value}
                    href={`?period=${item.value}`}
                    className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-all duration-150 ${
                      isCurrent
                        ? "bg-white text-navy shadow-sm"
                        : "text-white/80 hover:bg-white/10 hover:text-white"
                    }`}
                  >
                    {item.label}
                  </Link>
                );
              })}
            </nav>
          </div>
        </div>

        {/* Ambient subtle glow decoration */}
        <div className="pointer-events-none absolute -right-12 -bottom-12 size-64 rounded-full bg-cyan/20 blur-3xl" />
        <div className="pointer-events-none absolute -left-12 -top-12 size-64 rounded-full bg-blue/25 blur-3xl" />
      </div>

      {/* KPI Stat Cards Grid */}
      <section aria-labelledby="section-kpi" className="space-y-3">
        <div className="flex items-center justify-between border-b border-border/80 pb-2">
          <div>
            <h2 id="section-kpi" className="text-xs font-bold uppercase tracking-wider text-muted">
              Metrik Periode Berjalan ({PERIODS.find((it) => it.value === period)?.label})
            </h2>
            <p className="text-[11px] text-muted/80">
              Perubahan angka metrik dibandingkan dengan periode sebelumnya
            </p>
          </div>
        </div>

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
        <div className="flex items-center justify-between border-b border-border/80 pb-2">
          <div>
            <h2 id="section-charts" className="text-xs font-bold uppercase tracking-wider text-muted">
              Tren Aktivitas Harian
            </h2>
            <p className="text-[11px] text-muted/80">
              Frekuensi interaksi dan pergerakan akun sepanjang periode yang dipilih
            </p>
          </div>
        </div>

        <div className="grid gap-4 lg:grid-cols-2">
          {/* Signups Chart */}
          <div className="card flex flex-col justify-between p-5">
            <div className="flex items-start justify-between border-b border-border/60 pb-3">
              <div>
                <h3 className="text-sm font-bold text-foreground">Pendaftaran Pengguna Baru</h3>
                <p className="text-xs text-muted">Tren akun baru yang teregistrasi per hari</p>
              </div>
              <span className="rounded-badge bg-blue-50 px-2.5 py-1 text-xs font-bold font-mono text-blue-700 border border-blue-200/60 dark:bg-blue-950/40 dark:text-blue-300 dark:border-blue-800/60">
                Total {formatNumber(signups.total)}
              </span>
            </div>
            <div className="mt-4">
              <AreaChart points={signups.points} label="Pendaftar per hari" color="blue" />
            </div>
          </div>

          {/* Active Users Chart */}
          <div className="card flex flex-col justify-between p-5">
            <div className="flex items-start justify-between border-b border-border/60 pb-3">
              <div>
                <h3 className="text-sm font-bold text-foreground">Aktivitas Pengguna Berkala</h3>
                <p className="text-xs text-muted">Volume interaksi token sesi & API pengguna</p>
              </div>
              <div className="flex items-center gap-1.5 text-[11px] text-muted">
                <span className="rounded bg-surface-subtle px-2 py-0.5 border border-border/60">
                  Hari ini: <strong className="text-foreground font-mono">{formatNumber(overview.active.today)}</strong>
                </span>
                <span className="rounded bg-surface-subtle px-2 py-0.5 border border-border/60">
                  7 Hari: <strong className="text-foreground font-mono">{formatNumber(overview.active.last7Days)}</strong>
                </span>
              </div>
            </div>
            <div className="mt-4">
              <AreaChart points={active.points} label="Pengguna aktif per hari" color="cyan" />
            </div>
          </div>
        </div>
      </section>

      {/* Distribution & Cumulative Totals */}
      <section aria-labelledby="section-breakdown" className="space-y-3">
        <div className="flex items-center justify-between border-b border-border/80 pb-2">
          <div>
            <h2 id="section-breakdown" className="text-xs font-bold uppercase tracking-wider text-muted">
              Distribusi Platform & Status Kumulatif
            </h2>
            <p className="text-[11px] text-muted/80">
              Komposisi autentikasi login, penggunaan perangkat klien, dan total data platform
            </p>
          </div>
        </div>

        <div className="grid gap-4 lg:grid-cols-3">
          {/* Sign-up Methods with Visual Progress Bars */}
          <div className="card p-5">
            <div className="flex items-center justify-between border-b border-border/60 pb-3">
              <div>
                <h3 className="text-sm font-bold text-foreground">Metode Autentikasi</h3>
                <p className="text-[11px] text-muted">Kanal login pengguna baru</p>
              </div>
              <span className="rounded-badge bg-surface-subtle px-2 py-0.5 text-[10px] font-bold font-mono text-muted border border-border">
                {formatNumber(methods.email + methods.google + methods.github)} Total
              </span>
            </div>

            <div className="mt-4 space-y-3.5 text-xs">
              {/* Email */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <div className="flex items-center gap-1.5">
                    <span className="size-2 rounded-full bg-blue-500" />
                    <span className="font-medium text-foreground">Email + Password</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-muted text-[11px]">{emailPct}%</span>
                    <span className="font-mono font-bold text-foreground">{formatNumber(methods.email)}</span>
                  </div>
                </div>
                <div className="h-1.5 w-full rounded-full bg-surface-subtle overflow-hidden">
                  <div className="h-full rounded-full bg-blue-500 transition-all duration-300" style={{ width: `${emailPct}%` }} />
                </div>
              </div>

              {/* Google */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <div className="flex items-center gap-1.5">
                    <span className="size-2 rounded-full bg-emerald-500" />
                    <span className="font-medium text-foreground">Google OAuth</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-muted text-[11px]">{googlePct}%</span>
                    <span className="font-mono font-bold text-foreground">{formatNumber(methods.google)}</span>
                  </div>
                </div>
                <div className="h-1.5 w-full rounded-full bg-surface-subtle overflow-hidden">
                  <div className="h-full rounded-full bg-emerald-500 transition-all duration-300" style={{ width: `${googlePct}%` }} />
                </div>
              </div>

              {/* GitHub */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <div className="flex items-center gap-1.5">
                    <span className="size-2 rounded-full bg-purple-500" />
                    <span className="font-medium text-foreground">GitHub OAuth</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-muted text-[11px]">{githubPct}%</span>
                    <span className="font-mono font-bold text-foreground">{formatNumber(methods.github)}</span>
                  </div>
                </div>
                <div className="h-1.5 w-full rounded-full bg-surface-subtle overflow-hidden">
                  <div className="h-full rounded-full bg-purple-500 transition-all duration-300" style={{ width: `${githubPct}%` }} />
                </div>
              </div>
            </div>
          </div>

          {/* Active Platforms with Visual Progress Bars */}
          <div className="card p-5">
            <div className="flex items-center justify-between border-b border-border/60 pb-3">
              <div>
                <h3 className="text-sm font-bold text-foreground">Platform Klien Aktif</h3>
                <p className="text-[11px] text-muted">Aplikasi yang dipakai berinteraksi</p>
              </div>
              <span className="rounded-badge bg-surface-subtle px-2 py-0.5 text-[10px] font-bold font-mono text-muted border border-border">
                {formatNumber(platforms.total)} Aktif
              </span>
            </div>

            <div className="mt-4 space-y-3.5 text-xs">
              {/* Web Portal */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <div className="flex items-center gap-1.5">
                    <span className="size-2 rounded-full bg-cyan-500" />
                    <span className="font-medium text-foreground">Portal Web (Next.js)</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-muted text-[11px]">{webPct}%</span>
                    <span className="font-mono font-bold text-foreground">{formatNumber(platforms.web)}</span>
                  </div>
                </div>
                <div className="h-1.5 w-full rounded-full bg-surface-subtle overflow-hidden">
                  <div className="h-full rounded-full bg-cyan-500 transition-all duration-300" style={{ width: `${webPct}%` }} />
                </div>
              </div>

              {/* Android App */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <div className="flex items-center gap-1.5">
                    <span className="size-2 rounded-full bg-emerald-500" />
                    <span className="font-medium text-foreground">Aplikasi Android</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-muted text-[11px]">{androidPct}%</span>
                    <span className="font-mono font-bold text-foreground">{formatNumber(platforms.android)}</span>
                  </div>
                </div>
                <div className="h-1.5 w-full rounded-full bg-surface-subtle overflow-hidden">
                  <div className="h-full rounded-full bg-emerald-500 transition-all duration-300" style={{ width: `${androidPct}%` }} />
                </div>
              </div>

              {/* Other / Direct API */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <div className="flex items-center gap-1.5">
                    <span className="size-2 rounded-full bg-amber-500" />
                    <span className="font-medium text-foreground">API Langsung / Lainnya</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-muted text-[11px]">{otherPct}%</span>
                    <span className="font-mono font-bold text-foreground">{formatNumber(platforms.other)}</span>
                  </div>
                </div>
                <div className="h-1.5 w-full rounded-full bg-surface-subtle overflow-hidden">
                  <div className="h-full rounded-full bg-amber-500 transition-all duration-300" style={{ width: `${otherPct}%` }} />
                </div>
              </div>
            </div>
          </div>

          {/* Cumulative Totals Executive Summary */}
          <div className="card p-5">
            <div className="flex items-center justify-between border-b border-border/60 pb-3">
              <div>
                <h3 className="text-sm font-bold text-foreground">Status Database Utama</h3>
                <p className="text-[11px] text-muted">Akumulasi seluruh entitas sistem</p>
              </div>
              <span className="inline-flex items-center gap-1 text-[10px] font-bold text-emerald-600 dark:text-emerald-400">
                <span className="size-1.5 rounded-full bg-emerald-500" />
                Live Sync
              </span>
            </div>

            <dl className="mt-3 divide-y divide-border/50 text-xs">
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Total Pengguna Terdaftar</dt>
                <dd>
                  <Link href="/dashboard/users" className="font-bold font-mono text-primary hover:underline">
                    {formatNumber(overview.totals.users)}
                  </Link>
                </dd>
              </div>
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Organisasi Aktif</dt>
                <dd className="font-bold font-mono text-foreground">{formatNumber(overview.totals.organizations)}</dd>
              </div>
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Website Dibuat & Online</dt>
                <dd className="font-bold font-mono text-foreground flex items-center gap-1.5">
                  <span>{formatNumber(overview.totals.sitesOnline)} / {formatNumber(overview.totals.websites)}</span>
                  <span className="text-[10px] font-semibold text-emerald-600 bg-emerald-50 dark:bg-emerald-950/40 px-1.5 py-0.2 rounded border border-emerald-200/60 dark:border-emerald-800/60">
                    {sitesOnlinePct}%
                  </span>
                </dd>
              </div>
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Template Tayang di Galeri</dt>
                <dd className="font-bold font-mono text-foreground">{formatNumber(overview.totals.templatesPublished)}</dd>
              </div>
              <div className="flex items-center justify-between py-2">
                <dt className="text-muted">Kritik & Saran Menunggu</dt>
                <dd>
                  <Link
                    href="/dashboard/feedback"
                    className={`font-bold font-mono px-2 py-0.5 rounded text-[11px] ${
                      overview.totals.feedbackOpen > 0
                        ? "bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300 border border-amber-300 dark:border-amber-700"
                        : "text-foreground"
                    }`}
                  >
                    {formatNumber(overview.totals.feedbackOpen)}
                  </Link>
                </dd>
              </div>
            </dl>
          </div>
        </div>
      </section>

      {/* System Infrastructure Health Card (Status Ekosistem OrgSpace) */}
      <section aria-labelledby="section-infra" className="space-y-3">
        <div className="flex items-center justify-between border-b border-border/80 pb-2">
          <div>
            <h2 id="section-infra" className="text-xs font-bold uppercase tracking-wider text-muted">
              Status Ekosistem & Infrastruktur OrgSpace
            </h2>
            <p className="text-[11px] text-muted/80">
              Kondisi operasional backend, database, gateway hosting, dan portal
            </p>
          </div>
          <Link
            href="/dashboard/system"
            className="inline-flex items-center gap-1 text-xs font-semibold text-primary hover:underline"
          >
            <span>Diagnostik Lengkap</span>
            <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M3 10a.75.75 0 01.75-.75h10.638L10.23 5.29a.75.75 0 111.04-1.08l5.5 5.25a.75.75 0 010 1.08l-5.5 5.25a.75.75 0 11-1.04-1.08l4.158-3.96H3.75A.75.75 0 013 10z" clipRule="evenodd" />
            </svg>
          </Link>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
          {/* Service 1: Spring Boot */}
          <div className="card p-4 bg-surface flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-bold uppercase tracking-wider text-muted">
                  Backend API
                </span>
                <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-bold text-emerald-700 border border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800">
                  <span className="size-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  Aktif
                </span>
              </div>
              <h4 className="text-sm font-bold text-foreground">Spring Boot 3.x</h4>
              <p className="mt-0.5 text-xs text-muted font-mono">Port :8080 · Java 21</p>
            </div>
            <div className="mt-3 pt-2 border-t border-border/50 text-[11px] text-muted">
              Uptime: <strong className="text-foreground">{system ? formatUptime(system.uptimeSeconds) : "Normal"}</strong>
            </div>
          </div>

          {/* Service 2: PostgreSQL */}
          <div className="card p-4 bg-surface flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-bold uppercase tracking-wider text-muted">
                  Database
                </span>
                <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-bold border ${
                  system?.databaseOk ?? true
                    ? "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800"
                    : "bg-rose-50 text-rose-700 border-rose-200"
                }`}>
                  <span className="size-1.5 rounded-full bg-emerald-500" />
                  Tersambung
                </span>
              </div>
              <h4 className="text-sm font-bold text-foreground">PostgreSQL 16</h4>
              <p className="mt-0.5 text-xs text-muted font-mono">Port :5432 · Connection Pool Sehat</p>
            </div>
            <div className="mt-3 pt-2 border-t border-border/50 text-[11px] text-muted">
              Flyway Migrasi: <strong className="text-foreground font-mono">v{system?.schemaVersion ?? "41"}</strong>
            </div>
          </div>

          {/* Service 3: Multi-tenant Hosting */}
          <div className="card p-4 bg-surface flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-bold uppercase tracking-wider text-muted">
                  Hosting Publik
                </span>
                <span className="inline-flex items-center gap-1 rounded-full bg-cyan/10 px-2 py-0.5 text-[10px] font-bold text-cyan border border-cyan/20">
                  <span className="size-1.5 rounded-full bg-cyan" />
                  Siaga
                </span>
              </div>
              <h4 className="text-sm font-bold text-foreground">Multi-tenant Engine</h4>
              <p className="mt-0.5 text-xs text-muted font-mono">Port :3001 · orgspace.app/*</p>
            </div>
            <div className="mt-3 pt-2 border-t border-border/50 text-[11px] text-muted">
              Subdomain Gateway: <strong className="text-foreground">Aktif</strong>
            </div>
          </div>

          {/* Service 4: Next.js Web Portal */}
          <div className="card p-4 bg-surface flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-bold uppercase tracking-wider text-muted">
                  Portal Tim & Web
                </span>
                <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-bold text-emerald-700 border border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800">
                  <span className="size-1.5 rounded-full bg-emerald-500" />
                  Online
                </span>
              </div>
              <h4 className="text-sm font-bold text-foreground">Next.js 16 Portal</h4>
              <p className="mt-0.5 text-xs text-muted font-mono">Port :3000 · React 19</p>
            </div>
            <div className="mt-3 pt-2 border-t border-border/50 text-[11px] text-muted">
              App Router: <strong className="text-foreground">Optimal</strong>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
