import type { Metadata } from "next";
import Link from "next/link";
import { Inter } from "next/font/google";
import { LoginForm } from "./LoginForm";
import { ThemeToggle } from "./ThemeToggle";
import { ParticleBackground } from "./ParticleBackground";

const inter = Inter({
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700"],
});

export const metadata: Metadata = {
  title: "Dashboard OrgSpace - Login",
  description: "Khusus tim internal. Masuk dengan akun admin.",
};

const REASONS: Record<string, string> = {
  expired: "Sesi masuk telah berakhir. Silakan masuk kembali.",
  "not-admin": "Akun ini bukan tim internal OrgSpace.",
};

export default async function LoginPage({ searchParams }: PageProps<"/dashboard/login">) {
  const reason = (await searchParams).reason;
  const message = typeof reason === "string" ? REASONS[reason] : undefined;

  return (
    <div
      className={`relative min-h-screen flex items-center justify-center bg-[#f8fafc] dark:bg-[#020617] text-slate-800 dark:text-slate-200 overflow-hidden transition-colors duration-300 ${inter.className}`}
    >
      {/* Background Radial Gradient khusus Light Mode */}
      <div
        className="pointer-events-none absolute inset-0 z-0 opacity-100 dark:opacity-0 transition-opacity duration-300"
        style={{ backgroundImage: "radial-gradient(circle at 50% 0%, #e0f2fe 0%, #f8fafc 70%)" }}
      />

      {/* Particle Background Container */}
      <ParticleBackground />

      {/* Main Content */}
      <main className="relative z-10 w-full px-4 flex flex-col items-center">
        {/* Login Container */}
        <div className="glass-panel w-full max-w-md rounded-2xl p-8 sm:p-10 transition-all duration-300">
          {/* Header */}
          <div className="text-center mb-8">
            {/* Logo Graphic */}
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-xl bg-white/90 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 shadow-lg shadow-cyan-900/10 dark:shadow-cyan-900/30 mb-4 relative overflow-hidden transition-colors">
              <div className="absolute inset-0 bg-gradient-to-br from-cyan-500/10 dark:from-cyan-500/20 to-transparent" />
              <svg
                className="w-8 h-8 text-cyan-500 dark:text-cyan-400 relative z-10"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"
                />
              </svg>
            </div>

            <h1 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight mb-2 transition-colors">
              Dashboard OrgSpace
            </h1>
            <p className="text-sm text-slate-500 dark:text-slate-400 transition-colors">
              Khusus tim internal. Masuk dengan akun admin.
            </p>
          </div>

          {/* Pesan Sesi / Redirect */}
          {message && (
            <div
              className="mb-5 flex items-start gap-2.5 rounded-lg border border-amber-400/50 bg-amber-50 p-3 text-xs text-amber-900 backdrop-blur-sm dark:border-amber-500/30 dark:bg-amber-950/50 dark:text-amber-200"
              role="status"
            >
              <svg className="mt-0.5 h-4 w-4 shrink-0 text-amber-600 dark:text-amber-400" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z"
                  clipRule="evenodd"
                />
              </svg>
              <span>{message}</span>
            </div>
          )}

          {/* Login Form */}
          <LoginForm />
        </div>

        {/* Back to Public Portal Link */}
        <div className="mt-8">
          <Link
            href="/"
            className="text-sm text-slate-500 hover:text-cyan-600 dark:text-slate-400 dark:hover:text-cyan-400 transition-colors flex items-center gap-2"
          >
            &larr; Kembali ke portal publik
          </Link>
        </div>
      </main>

      {/* Dark/Light Mode Toggle */}
      <ThemeToggle />
    </div>
  );
}
