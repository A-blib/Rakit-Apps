import Link from "next/link";
import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { adminApi } from "@/lib/api/admin";
import { ApiError } from "@/lib/api/http";
import { adminToken } from "@/lib/auth/session";
import { formatDateTime, formatDay } from "@/lib/format";

export const metadata: Metadata = { title: "Detail Akun Pengguna" };

const METHOD_LABEL = { local: "Email & Password", google: "Google OAuth", github: "GitHub OAuth" } as const;

export default async function UserDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  let user;
  try {
    user = await adminApi.user(await adminToken(), id);
  } catch (error) {
    if (error instanceof ApiError && (error.status === 404 || error.code === "NOT_FOUND")) {
      notFound();
    }
    throw error;
  }

  const initials =
    user.displayName
      .split(" ")
      .map((n) => n[0])
      .filter(Boolean)
      .slice(0, 2)
      .join("")
      .toUpperCase() || "U";

  return (
    <div className="max-w-4xl space-y-6 mx-auto">
      {/* Tombol Kembali */}
      <div>
        <Link
          href="/dashboard/users"
          className="inline-flex items-center gap-1.5 text-xs font-medium text-muted hover:text-foreground transition-colors"
        >
          &larr; Kembali ke Manajemen Akun Pengguna
        </Link>
      </div>

      {/* Header Profil Akun */}
      <header className="card flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="flex size-14 shrink-0 items-center justify-center rounded-2xl bg-surface-subtle border border-border text-base font-bold font-mono text-navy dark:text-cyan">
            {initials}
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-xl font-bold tracking-tight text-foreground">
                {user.displayName}
              </h1>
              {user.admin && (
                <span className="rounded px-2 py-0.5 bg-navy text-white text-[11px] font-semibold tracking-wider uppercase">
                  Admin Internal
                </span>
              )}
              {user.providerStatus ? (
                user.providerStatus === "active" ? (
                  <span className="rounded px-2 py-0.5 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800 text-[11px] font-semibold inline-flex items-center gap-1.5">
                    <span className="size-1.5 rounded-full bg-emerald-500" />
                    Penyedia Terverifikasi (Aktif)
                  </span>
                ) : (
                  <span className="rounded px-2 py-0.5 bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300 border border-amber-200 dark:border-amber-800 text-[11px] font-semibold inline-flex items-center gap-1.5">
                    <span className="size-1.5 rounded-full bg-amber-500" />
                    Penyedia (Ditangguhkan)
                  </span>
                )
              ) : !user.admin ? (
                <span className="rounded px-2 py-0.5 bg-surface-subtle text-muted border border-border text-[11px] font-medium">
                  Pembuat Website (Creator)
                </span>
              ) : null}
            </div>
            <p className="text-xs text-muted font-mono mt-1">{user.email}</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className="rounded-lg bg-surface-subtle border border-border px-3 py-1.5 text-xs font-mono text-muted">
            ID: <span className="text-foreground">{user.id.slice(0, 8)}...</span>
          </span>
        </div>
      </header>

      {/* Grid Informasi Rinci Akun */}
      <div className="grid gap-4.5 md:grid-cols-2">
        {/* Kartu 1: Identitas & Akses */}
        <div className="card space-y-4">
          <div className="border-b border-border/60 pb-3">
            <h2 className="text-sm font-semibold text-foreground">Identitas & Hak Akses</h2>
            <p className="text-xs text-muted">Status otorisasi dan peran pengguna dalam sistem</p>
          </div>

          <dl className="divide-y divide-border/60 text-xs">
            <div className="flex items-center justify-between py-2.5">
              <dt className="text-muted">Peran Utama</dt>
              <dd className="font-semibold text-foreground">
                {user.admin ? "Administrator Sistem" : user.providerStatus ? "Penyedia Template" : "Pembuat Website"}
              </dd>
            </div>
            <div className="flex items-center justify-between py-2.5">
              <dt className="text-muted">Status Onboarding</dt>
              <dd className="font-semibold text-foreground">
                {user.onboardingCompleted ? (
                  <span className="badge-success text-[10px] px-2 py-0.5">Selesai</span>
                ) : (
                  <span className="badge-warning text-[10px] px-2 py-0.5">Belum Selesai</span>
                )}
              </dd>
            </div>
            <div className="flex items-center justify-between py-2.5">
              <dt className="text-muted">Status Penyedia</dt>
              <dd className="font-semibold text-foreground">
                {user.providerStatus ? (
                  <span className="capitalize">{user.providerStatus}</span>
                ) : (
                  <span className="text-muted">Bukan Penyedia</span>
                )}
              </dd>
            </div>
            <div className="flex items-center justify-between py-2.5">
              <dt className="text-muted">Metode Masuk Terdaftar</dt>
              <dd className="flex gap-1.5 flex-wrap">
                {user.loginMethods.map((m) => (
                  <span key={m} className="badge-neutral text-[10px] px-2 py-0.5 font-mono">
                    {METHOD_LABEL[m] ?? m}
                  </span>
                ))}
              </dd>
            </div>
          </dl>
        </div>

        {/* Kartu 2: Portofolio & Aset */}
        <div className="card space-y-4">
          <div className="border-b border-border/60 pb-3">
            <h2 className="text-sm font-semibold text-foreground">Portofolio & Kepemilikan</h2>
            <p className="text-xs text-muted">Aset organisasi dan situs yang dikelola akun ini</p>
          </div>

          <dl className="divide-y divide-border/60 text-xs">
            <div className="flex items-center justify-between py-2.5">
              <dt className="text-muted">Organisasi yang Dikelola</dt>
              <dd className="font-semibold font-mono text-foreground text-sm">
                {user.organizations} <span className="text-muted font-normal text-xs">organisasi</span>
              </dd>
            </div>
            <div className="flex items-center justify-between py-2.5">
              <dt className="text-muted">Proyek Website Dibuat</dt>
              <dd className="font-semibold font-mono text-foreground text-sm">
                {user.websites} <span className="text-muted font-normal text-xs">website</span>
              </dd>
            </div>
            <div className="flex items-center justify-between py-2.5">
              <dt className="text-muted">Waktu Pendaftaran Akun</dt>
              <dd className="font-mono text-muted text-right">
                {formatDateTime(user.createdAt)}
              </dd>
            </div>
            <div className="flex items-center justify-between py-2.5">
              <dt className="text-muted">Aktivitas Terakhir Tercatat</dt>
              <dd className="text-foreground font-medium">
                {user.lastActiveDay ? (
                  <span className="inline-flex items-center gap-1.5 font-mono">
                    <span className="size-1.5 rounded-full bg-emerald-500" />
                    {formatDay(user.lastActiveDay)}
                  </span>
                ) : (
                  <span className="text-muted/70">Belum ada aktivitas</span>
                )}
              </dd>
            </div>
          </dl>
        </div>
      </div>

      {/* Kartu 3: Catatan Audit & Kebijakan Internal */}
      <div className="rounded-xl border border-border bg-surface p-5 space-y-3">
        <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-muted">
          <svg className="size-4 text-primary" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clipRule="evenodd" />
          </svg>
          <span>Catatan Tata Kelola Akun (Admin Policy)</span>
        </div>
        <p className="text-xs text-muted leading-relaxed">
          Sesuai rancangan keamanan fondasi OrgSpace, pemberian atau pencabutan hak <strong>Administrator Internal</strong> dilakukan langsung pada level basis data via PostgreSQL demi mencegah eskalasi hak istimewa secara tidak sah. Hubungi tim Aris jika akun ini membutuhkan delegasi peran admin darurat.
        </p>
        <div className="rounded-lg bg-surface-subtle p-3 text-[11px] font-mono text-muted border border-border">
          <code>UUID: {user.id}</code>
        </div>
      </div>
    </div>
  );
}
