import type { Metadata } from "next";
import Link from "next/link";
import Image from "next/image";
import { LoginForm } from "./LoginForm";
import { ThemeToggle } from "./ThemeToggle";
import { ConstellationCanvas } from "./ConstellationCanvas";

export const metadata: Metadata = {
  title: "Masuk Dashboard · OrgSpace",
  description: "Portal masuk dashboard manajemen OrgSpace khusus tim internal.",
};

const REASONS: Record<string, string> = {
  expired: "Sesi masuk telah berakhir. Silakan masuk kembali.",
  "not-admin": "Akun ini bukan tim internal OrgSpace.",
};

export default async function LoginPage({ searchParams }: PageProps<"/dashboard/login">) {
  const reason = (await searchParams).reason;
  const message = typeof reason === "string" ? REASONS[reason] : undefined;

  return (
    <main className="relative flex min-h-screen flex-1 flex-col items-center justify-center overflow-hidden bg-[#07152B] px-4 py-12">
      {/* Background Wallpaper Konstelasi & Jaringan Simpul Cyber */}
      <div
        className="pointer-events-none absolute inset-0 z-0 bg-cover bg-center bg-no-repeat opacity-95 transition-opacity"
        style={{ backgroundImage: "url('/login-bg.webp')" }}
      />

      {/* Lapisan Partikel Interaktif Berpendar */}
      <ConstellationCanvas />

      {/* Pendaran Cahaya Ambient Cyan & Biru di Latar Belakang */}
      <div className="pointer-events-none absolute inset-0 z-0 overflow-hidden" aria-hidden="true">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[540px] h-[540px] rounded-full bg-cyan/10 blur-[130px]" />
        <div className="absolute top-1/4 left-1/4 w-[380px] h-[380px] rounded-full bg-blue/10 blur-[100px]" />
      </div>

      {/* Konten Utama */}
      <div className="relative z-10 w-full max-w-[430px] animate-fade-in">
        {/* Brand Header */}
        <div className="mb-6 flex flex-col items-center text-center">
          <Link
            href="/"
            className="group relative flex flex-col items-center transition-transform hover:scale-105"
            aria-label="Beranda OrgSpace"
          >
            {/* Pendaran halus di belakang logo */}
            <div className="absolute -inset-2 rounded-full bg-cyan/25 blur-xl opacity-80 group-hover:opacity-100 transition-opacity" />
            <Image
              src="/orgspace-cyber-logo.png"
              alt="OrgSpace"
              width={96}
              height={92}
              priority
              className="relative object-contain drop-shadow-[0_0_20px_rgba(0,201,242,0.5)]"
            />
          </Link>
          <h1 className="mt-4 text-2xl font-bold tracking-tight text-white drop-shadow-[0_2px_10px_rgba(0,0,0,0.5)]">
            Dashboard OrgSpace
          </h1>
          <p className="mt-1 text-xs text-slate-300/80">
            Khusus tim internal. Masuk dengan akun admin.
          </p>
        </div>

        {/* Card Frosted Glass */}
        <div className="card-glass rounded-2xl p-7 sm:p-8">
          {message && (
            <div
              className="mb-5 flex items-start gap-2.5 rounded-xl border border-amber-500/40 bg-amber-950/60 p-3 text-xs text-amber-200 backdrop-blur-md"
              role="status"
            >
              <svg className="mt-0.5 size-4 shrink-0 text-amber-400" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z"
                  clipRule="evenodd"
                />
              </svg>
              <span>{message}</span>
            </div>
          )}

          <LoginForm />
        </div>

        {/* Tautan Navigasi Bawah */}
        <div className="mt-6 flex flex-col items-center gap-2 text-center text-xs">
          <Link
            href="/"
            className="inline-flex items-center gap-1.5 text-slate-400 transition-colors hover:text-cyan"
          >
            <span>← Kembali ke portal publik</span>
          </Link>
        </div>
      </div>

      {/* Kontrol Pojok Kiri Bawah: Pengalih Tema */}
      <div className="fixed bottom-5 left-5 z-20">
        <ThemeToggle />
      </div>

      {/* Kontrol Pojok Kanan Bawah: Protokol Keamanan OrgSpace */}
      <div className="fixed bottom-5 right-5 z-20">
        <div
          className="flex size-9 items-center justify-center rounded-full border border-slate-700/60 bg-[#091b30]/85 text-cyan backdrop-blur-md shadow-lg transition-all hover:border-cyan/50 hover:shadow-[0_0_15px_rgba(0,201,242,0.3)] cursor-help"
          title="Sistem Keamanan OrgSpace Terenkripsi"
        >
          <svg className="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 2L3 7v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-9-5z" />
          </svg>
        </div>
      </div>
    </main>
  );
}
