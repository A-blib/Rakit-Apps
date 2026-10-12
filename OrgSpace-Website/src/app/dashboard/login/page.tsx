import type { Metadata } from "next";
import { LoginForm } from "./LoginForm";

export const metadata: Metadata = { title: "Masuk dashboard" };

const REASONS: Record<string, string> = {
  expired: "Sesi berakhir. Silakan masuk lagi.",
  "not-admin": "Akun ini bukan tim internal OrgSpace.",
};

export default async function LoginPage({ searchParams }: PageProps<"/dashboard/login">) {
  const reason = (await searchParams).reason;
  const message = typeof reason === "string" ? REASONS[reason] : undefined;
  return (
    <main className="flex flex-1 items-center justify-center px-4 py-16">
      <div className="card w-full max-w-sm">
        <h1 className="text-2xl font-bold">Dashboard OrgSpace</h1>
        <p className="mt-1 text-sm text-muted">Khusus tim internal. Masuk dengan akun admin.</p>
        {message && <p className="mt-4 rounded-field bg-surface-subtle p-3 text-sm">{message}</p>}
        <div className="mt-6"><LoginForm /></div>
      </div>
    </main>
  );
}
