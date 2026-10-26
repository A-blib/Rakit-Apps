import Link from "next/link";
import { redirect } from "next/navigation";
import { PORTAL_URL, PUBLIC_BACKEND_URL } from "@/lib/config";
import { Globe, Server, CheckCircle2, ArrowRight, ShieldCheck, Cpu, ExternalLink } from "lucide-react";

export default async function HostingGatewayPage({
  searchParams,
}: {
  searchParams?: Promise<{ info?: string }>;
}) {
  const params = await searchParams;
  const portalUrl = PORTAL_URL;

  // Perilaku seperti Vercel (vercel.app -> vercel.com):
  // Jika root diakses tanpa parameter ?info=true, arahkan langsung ke Portal Utama
  if (params?.info !== "true") {
    redirect(portalUrl);
  }

  const backendUrl = PUBLIC_BACKEND_URL;

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 font-sans flex flex-col justify-between">
      <header className="border-b border-slate-200 bg-white px-6 py-4">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-blue-600 text-white font-black flex items-center justify-center text-lg">
              H
            </div>
            <div>
              <div className="font-extrabold text-base text-slate-900">OrgSpace Hosting</div>
              <div className="text-xs text-slate-500">Public User Site Gateway (Sistem ke-3)</div>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <a
              href={portalUrl}
              className="inline-flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-lg bg-blue-50 text-blue-700 hover:bg-blue-100 transition"
            >
              <span>Ke Portal Utama</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
            <div className="flex items-center gap-2 text-xs font-semibold px-3 py-1.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              Hosting Online :3001
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto w-full px-6 py-16 space-y-12">
        <div className="text-center space-y-4">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-200">
            <Cpu className="w-3.5 h-3.5" />
            Layanan Hosting Mandiri untuk Web Pengguna
          </div>
          <h1 className="text-4xl sm:text-5xl font-black text-slate-900 tracking-tight">
            Tempat Website Pengguna Tayang &amp; &ldquo;Nebeng&rdquo;
          </h1>
          <p className="text-base sm:text-lg text-slate-600 max-w-2xl mx-auto leading-relaxed">
            Sistem mandiri terisolasi berkinerja tinggi untuk melayani website hasil rakitan pengguna OrgSpace dengan dukungan rute dinamis (<code className="text-blue-600 font-mono text-sm bg-blue-50 px-2 py-0.5 rounded">/[namawebuser]</code>), dual renderer (Template &amp; Custom), SEO otomatis, serta Custom Domain.
          </p>
        </div>

        {/* 3 Sistem Arsitektur */}
        <div className="grid md:grid-cols-3 gap-5">
          <div className="p-6 rounded-2xl bg-white border border-slate-200 space-y-3 shadow-sm">
            <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center font-bold">
              1
            </div>
            <h3 className="font-bold text-base text-slate-900">OrgSpace-Apps</h3>
            <p className="text-xs text-slate-500 leading-relaxed">
              Aplikasi mobile Android Builder + Backend Spring Boot (:8080) untuk mengedit dan menerbitkan website.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-white border border-slate-200 space-y-3 shadow-sm">
            <div className="w-10 h-10 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center font-bold">
              2
            </div>
            <h3 className="font-bold text-base text-slate-900">OrgSpace-Website</h3>
            <p className="text-xs text-slate-500 leading-relaxed">
              Portal korporat (<code className="font-mono">orgspace.com</code>) untuk landing page resmi, panduan, dan monitoring dashboard.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-white border-2 border-blue-500 space-y-3 shadow-md shadow-blue-500/10">
            <div className="w-10 h-10 rounded-xl bg-blue-600 text-white flex items-center justify-center font-bold">
              3
            </div>
            <h3 className="font-bold text-base text-slate-900">OrgSpace-Hosting</h3>
            <p className="text-xs text-slate-600 leading-relaxed font-medium">
              Sistem hosting publik ini (<code className="font-mono text-blue-600">orgspace.app/[slug]</code>) yang terpisah demi keamanan isolasi cookie dan performa SSR.
            </p>
          </div>
        </div>

        {/* Contoh Website Tayang */}
        <div className="p-8 rounded-3xl bg-white border border-slate-200 space-y-6 shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-100 pb-4">
            <div>
              <h2 className="text-lg font-bold text-slate-900">Website Pengguna yang Baru Diterbitkan</h2>
              <p className="text-xs text-slate-500 mt-0.5">Dapat langsung dibuka melalui link dinamis di bawah</p>
            </div>
            <span className="text-xs text-slate-400 font-mono">Backend: {backendUrl}</span>
          </div>

          <div className="grid sm:grid-cols-2 gap-4">
            <Link
              href="/web-dinamis-organisasi"
              className="p-5 rounded-2xl bg-slate-50 border border-slate-200 hover:border-blue-400 hover:bg-blue-50/50 transition group flex items-center justify-between"
            >
              <div className="space-y-1">
                <span className="text-[11px] font-semibold text-blue-600 uppercase tracking-wider">
                  Mode Template
                </span>
                <div className="font-bold text-sm text-slate-900 group-hover:text-blue-700 transition">
                  Web Dinamis Organisasi
                </div>
                <div className="text-xs text-slate-500 font-mono">/web-dinamis-organisasi</div>
              </div>
              <ArrowRight className="w-4 h-4 text-slate-400 group-hover:text-blue-600 transition group-hover:translate-x-1" />
            </Link>

            <Link
              href="/osis-smansa"
              className="p-5 rounded-2xl bg-slate-50 border border-slate-200 hover:border-blue-400 hover:bg-blue-50/50 transition group flex items-center justify-between"
            >
              <div className="space-y-1">
                <span className="text-[11px] font-semibold text-purple-600 uppercase tracking-wider">
                  Mode Assembled / Rakit
                </span>
                <div className="font-bold text-sm text-slate-900 group-hover:text-purple-700 transition">
                  OSIS SMANSA
                </div>
                <div className="text-xs text-slate-500 font-mono">/osis-smansa</div>
              </div>
              <ArrowRight className="w-4 h-4 text-slate-400 group-hover:text-purple-600 transition group-hover:translate-x-1" />
            </Link>
          </div>
        </div>
      </main>

      <footer className="border-t border-slate-200 bg-white py-6 text-center text-xs text-slate-400">
        OrgSpace Hosting Gateway &copy; {new Date().getFullYear()} • Terhubung ke Spring Boot API
      </footer>
    </div>
  );
}
