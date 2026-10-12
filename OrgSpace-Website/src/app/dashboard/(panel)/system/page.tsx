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
  const rows: [string, string][] = [
    ["Database", system.databaseOk ? "Terhubung" : "GAGAL"],
    ["Versi skema (Flyway)", system.schemaVersion ?? "–"],
    ["Profil aktif", system.activeProfiles.join(", ") || "default"],
    ["Berjalan sejak", `${formatDateTime(system.startedAt)} (${uptime(system.uptimeSeconds)})`],
    ["Zona waktu statistik", system.timezone],
    ["Java", system.javaVersion],
  ];
  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold">Sistem</h1>
      <dl className="card divide-y divide-border p-0">
        {rows.map(([label, value]) => (
          <div key={label} className="flex justify-between gap-4 px-5 py-3 text-sm">
            <dt className="text-muted">{label}</dt>
            <dd className={value === "GAGAL" ? "font-semibold text-error" : undefined}>{value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}
