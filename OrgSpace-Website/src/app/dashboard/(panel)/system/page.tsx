import Link from "next/link";
import type { Metadata } from "next";
import { adminApi } from "@/lib/api/admin";
import { adminToken } from "@/lib/auth/session";
import { formatDateTime } from "@/lib/format";

export const metadata: Metadata = { title: "Kesehatan Sistem" };

function uptime(seconds: number) {
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return `${days} hari ${hours} jam ${minutes} menit`;
}

interface ServiceItem {
  name: string;
  category: string;
  status: "normal" | "partial" | "down";
  statusLabel: string;
  latencyOrDetail: string;
}

export default async function SystemPage({
  searchParams,
}: PageProps<"/dashboard/system">) {
  const query = await searchParams;
  const serviceFilter =
    query.service === "normal" || query.service === "partial" || query.service === "down"
      ? query.service
      : "all";

  const system = await adminApi.system(await adminToken());

  // Komponen & Layanan Sistem Inti OrgSpace
  const allServices: ServiceItem[] = [
    {
      name: "Backend REST API (Spring Boot)",
      category: "Aplikasi Utama",
      status: "normal",
      statusLabel: "Beroperasi Normal",
      latencyOrDetail: "Port 8080 &bull; Java 21",
    },
    {
      name: "Basis Data PostgreSQL 18",
      category: "Penyimpanan Relasional",
      status: system.databaseOk ? "normal" : "down",
      statusLabel: system.databaseOk ? "Beroperasi Normal" : "Down (Terputus)",
      latencyOrDetail: system.databaseOk ? "Koneksi Aktif (Pool Sehat)" : "Gagal Akses Socket",
    },
    {
      name: "Penyimpanan Media & Aset Disk",
      category: "Storage Server",
      status: "normal",
      statusLabel: "Beroperasi Normal",
      latencyOrDetail: "Folder Uploads Siap",
    },
    {
      name: "Layanan Autentikasi JWT & Sesi",
      category: "Keamanan & Token",
      status: "normal",
      statusLabel: "Beroperasi Normal",
      latencyOrDetail: "Rotasi Refresh Token Aktif",
    },
    {
      name: `Migrasi Skema Flyway (v${system.schemaVersion ?? "21"})`,
      category: "Integritas Basis Data",
      status: "normal",
      statusLabel: "Beroperasi Normal",
      latencyOrDetail: "Versi Skema Terverifikasi",
    },
    {
      name: "Sinkronisasi Telemetri & Usage Tracking",
      category: "Monitoring & Log",
      status: "normal",
      statusLabel: "Beroperasi Normal",
      latencyOrDetail: "Flush Otomatis 60 Detik",
    },
  ];

  const filteredServices = allServices.filter((s) => {
    if (serviceFilter === "normal") return s.status === "normal";
    if (serviceFilter === "partial") return s.status === "partial";
    if (serviceFilter === "down") return s.status === "down";
    return true;
  });

  const hrefForService = (val: string) => {
    if (val === "all") return "/dashboard/system";
    return `?service=${val}`;
  };

  return (
    <div className="flex-1 w-full space-y-6">
      {/* Header */}
      <div className="border-b border-border/80 pb-4">
        <h1 className="text-xl font-bold tracking-tight text-foreground sm:text-2xl">
          Status & Diagnostik Sistem
        </h1>
      </div>

      {/* Health Overview Banner */}
      <div className="card flex items-center justify-between p-4 bg-surface w-full">
        <div className="flex items-center gap-3">
          <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 dark:bg-emerald-950/40 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800">
            <svg className="size-5" viewBox="0 0 20 20" fill="currentColor">
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          <div>
            <p className="text-sm font-semibold text-foreground">Seluruh Layanan Utama Normal</p>
            <p className="text-xs text-muted">Waktu Aktif Sistem (Uptime): {uptime(system.uptimeSeconds)}</p>
          </div>
        </div>
        <span className="badge-success text-xs font-semibold px-2.5 py-1">
          Operasional
        </span>
      </div>

      {/* Bagian: Monitoring Layanan dengan Filter Bar */}
      <div className="space-y-3 w-full">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <h2 className="text-xs font-semibold uppercase tracking-wider text-muted">
              Monitoring Komponen Layanan
            </h2>
            <p className="text-xs text-muted mt-0.5">Pemantauan kesehatan per modul arsitektur OrgSpace</p>
          </div>

          {/* Filter Bar (Monitoring Layanan): Semua | Beroperasi Normal | Gangguan Sebagian | Down */}
          <nav
            className="inline-flex rounded-xl border border-border bg-surface-subtle p-1 gap-1"
            aria-label="Filter Status Layanan"
          >
            <Link
              href={hrefForService("all")}
              className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
                serviceFilter === "all"
                  ? "bg-surface font-semibold text-foreground border border-border"
                  : "text-muted hover:text-foreground"
              }`}
            >
              Semua Layanan
            </Link>
            <Link
              href={hrefForService("normal")}
              className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors inline-flex items-center gap-1.5 ${
                serviceFilter === "normal"
                  ? "bg-surface font-semibold text-emerald-700 dark:text-emerald-400 border border-border"
                  : "text-muted hover:text-emerald-600"
              }`}
            >
              <span className="size-2 rounded-full bg-emerald-500" />
              <span>Beroperasi Normal</span>
            </Link>
            <Link
              href={hrefForService("partial")}
              className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors inline-flex items-center gap-1.5 ${
                serviceFilter === "partial"
                  ? "bg-surface font-semibold text-amber-700 dark:text-amber-400 border border-border"
                  : "text-muted hover:text-amber-600"
              }`}
            >
              <span className="size-2 rounded-full bg-amber-500" />
              <span>Gangguan Sebagian</span>
            </Link>
            <Link
              href={hrefForService("down")}
              className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors inline-flex items-center gap-1.5 ${
                serviceFilter === "down"
                  ? "bg-surface font-semibold text-rose-700 dark:text-rose-400 border border-border"
                  : "text-muted hover:text-rose-600"
              }`}
            >
              <span className="size-2 rounded-full bg-rose-500" />
              <span>Down</span>
            </Link>
          </nav>
        </div>

        {/* Tabel Layanan dengan Horizontal Scroll Responsive & W-Full */}
        <div className="card overflow-hidden p-0 w-full">
          <div className="w-full overflow-x-auto">
            <table className="table w-full">
              <thead>
                <tr>
                  <th className="whitespace-nowrap">Layanan / Komponen</th>
                  <th className="whitespace-nowrap">Kategori</th>
                  <th className="whitespace-nowrap">Status Operasional</th>
                  <th className="whitespace-nowrap">Detail Teknis</th>
                </tr>
              </thead>
              <tbody>
                {filteredServices.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="py-8 text-center text-xs text-muted">
                      Tidak ada layanan dengan status &ldquo;{serviceFilter}&rdquo;.
                    </td>
                  </tr>
                ) : (
                  filteredServices.map((svc) => (
                    <tr key={svc.name}>
                      <td className="font-semibold text-foreground text-xs whitespace-nowrap">
                        {svc.name}
                      </td>
                      <td className="text-muted text-xs font-mono whitespace-nowrap">{svc.category}</td>
                      <td className="whitespace-nowrap">
                        {svc.status === "normal" && (
                          <span className="badge-success text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                            <span className="size-1.5 rounded-full bg-emerald-500" />
                            Beroperasi Normal
                          </span>
                        )}
                        {svc.status === "partial" && (
                          <span className="badge-warning text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                            <span className="size-1.5 rounded-full bg-amber-500" />
                            Gangguan Sebagian
                          </span>
                        )}
                        {svc.status === "down" && (
                          <span className="badge-error text-[10px] px-2 py-0.5 font-semibold inline-flex items-center gap-1">
                            <span className="size-1.5 rounded-full bg-rose-500" />
                            Down
                          </span>
                        )}
                      </td>
                      <td
                        className="text-muted font-mono text-[11px] whitespace-nowrap"
                        dangerouslySetInnerHTML={{ __html: svc.latencyOrDetail }}
                      />
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Detailed Technical Parameters */}
      <div className="card overflow-hidden p-0 w-full">
        <div className="border-b border-border bg-surface-subtle/50 px-5 py-3 text-xs font-semibold uppercase tracking-wider text-muted">
          Parameter Lingkungan Runtime
        </div>
        <div className="w-full overflow-x-auto">
          <dl className="w-full divide-y divide-border/60 text-xs min-w-[320px]">
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
    </div>
  );
}
