import type { Metadata } from "next";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatDateTime } from "@/lib/format";

export const metadata: Metadata = { title: "Sistem" };

function uptime(seconds: number) {
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return `${days} hari ${hours} jam ${minutes} menit`;
}

export default async function SystemPage() {
  const system = await adminApi.system(await adminToken());

  return (
    <div className="max-w-3xl space-y-6">
      {/* Header */}
      <div className="border-b border-border/80 pb-4">
        <h1 className="text-xl font-bold tracking-tight text-navy dark:text-foreground sm:text-2xl">
          Status & Diagnostik Sistem
        </h1>
        <p className="mt-1 text-xs text-muted">
          Informasi lingkungan runtime backend, koneksi database PostgreSQL, dan migrasi Flyway.
        </p>
      </div>

      {/* Health Overview Banner */}
      <div className="card flex items-center justify-between p-4 bg-surface">
        <div className="flex items-center gap-3">
          <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 dark:bg-emerald-950/40 dark:text-emerald-400 border border-emerald-200">
            <svg className="size-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
            </svg>
          </div>
          <div>
            <p className="text-sm font-semibold text-foreground">Backend Beroperasi Normal</p>
            <p className="text-xs text-muted">Uptime: {uptime(system.uptimeSeconds)}</p>
          </div>
        </div>
        <span className="badge-success text-xs font-semibold px-2.5 py-1">
          Sehat
        </span>
      </div>

      {/* Detailed Technical Parameters */}
      <div className="card overflow-hidden p-0">
        <div className="border-b border-border bg-surface-subtle/50 px-5 py-3 text-xs font-semibold uppercase tracking-wider text-muted">
          Parameter Lingkungan Runtime
        </div>
        <dl className="divide-y divide-border/60 text-xs">
          <div className="flex items-center justify-between px-5 py-3">
            <dt className="text-muted font-medium">Status Koneksi Database</dt>
            <dd className="font-semibold">
              {system.databaseOk ? (
                <span className="inline-flex items-center gap-1.5 text-emerald-700 dark:text-emerald-400">
                  <span className="size-2 rounded-full bg-emerald-500" />
                  PostgreSQL Terhubung
                </span>
              ) : (
                <span className="inline-flex items-center gap-1.5 text-rose-600 font-bold">
                  <span className="size-2 rounded-full bg-rose-500" />
                  Gagal Terhubung
                </span>
              )}
            </dd>
          </div>

          <div className="flex items-center justify-between px-5 py-3">
            <dt className="text-muted font-medium">Versi Skema Migrasi (Flyway)</dt>
            <dd className="font-mono font-semibold text-navy dark:text-cyan bg-surface-subtle px-2 py-0.5 rounded border border-border">
              v{system.schemaVersion ?? "–"}
            </dd>
          </div>

          <div className="flex items-center justify-between px-5 py-3">
            <dt className="text-muted font-medium">Profil Spring Boot Aktif</dt>
            <dd className="font-mono text-foreground font-medium">
              {system.activeProfiles.join(", ") || "default"}
            </dd>
          </div>

          <div className="flex items-center justify-between px-5 py-3">
            <dt className="text-muted font-medium">Waktu Server Dimulai</dt>
            <dd className="font-mono text-muted">
              {formatDateTime(system.startedAt)}
            </dd>
          </div>

          <div className="flex items-center justify-between px-5 py-3">
            <dt className="text-muted font-medium">Zona Waktu Sistem</dt>
            <dd className="font-mono text-foreground">{system.timezone}</dd>
          </div>

          <div className="flex items-center justify-between px-5 py-3">
            <dt className="text-muted font-medium">Versi Java Runtime (JDK)</dt>
            <dd className="font-mono text-foreground">{system.javaVersion}</dd>
          </div>
        </dl>
      </div>
    </div>
  );
}
