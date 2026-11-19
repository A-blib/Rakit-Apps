"use client";

import Link from "next/link";

export function CtaSection() {
  return (
    <section id="akses-awal" className="relative w-full overflow-hidden bg-white dark:bg-[#07152B] py-16 sm:py-24 scroll-mt-20 transition-colors duration-300">
      <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#0B1E36] via-[#0D2444] to-[#07152B] border border-slate-800/40 dark:border-[#1E3E6B] p-8 sm:p-12 lg:p-16 text-white shadow-2xl shadow-slate-900/10">
          {/* Ambient Lighting Gradients */}
          <div aria-hidden="true" className="pointer-events-none absolute inset-0 select-none overflow-hidden">
            <div className="absolute -top-24 -right-24 h-72 w-72 sm:h-96 sm:w-96 rounded-full bg-[#0099FF]/20 blur-3xl" />
            <div className="absolute -bottom-24 -left-24 h-72 w-72 sm:h-96 sm:w-96 rounded-full bg-indigo-500/15 blur-3xl" />
          </div>

          <div className="relative z-10 max-w-3xl mx-auto text-center">
            {/* Eyebrow Badge */}
            <div className="inline-flex items-center gap-2 rounded-full border border-blue-400/30 bg-blue-500/10 px-4 py-1.5 text-xs font-bold text-[#00C9F2] shadow-xs mb-4">
              <span className="size-1.5 rounded-full bg-[#00C9F2] animate-ping" />
              Siap Digunakan Hari Ini
            </div>

            {/* Main Headline */}
            <h2 className="text-3xl sm:text-4xl md:text-5xl font-black tracking-tight leading-[1.15] text-white">
              Waktunya Organisasimu Tampil Profesional di Dunia Digital
            </h2>

            {/* Subtitle */}
            <p className="mt-4 sm:mt-5 text-sm sm:text-base md:text-lg text-slate-300 leading-relaxed max-w-2xl mx-auto">
              Tidak perlu lagi pusing memikirkan sewa server tahunan atau mencari anggota yang paham koding. Pilih template, sesuaikan dari HP, dan website resmimu langsung aktif!
            </p>

            {/* CTA Buttons */}
            <div className="mt-8 sm:mt-10 flex flex-col sm:flex-row items-center justify-center gap-3.5 sm:gap-4">
              <a
                href="/downloads/orgspace.apk"
                download="orgspace.apk"
                className="w-full sm:w-auto inline-flex h-12 sm:h-13 items-center justify-center rounded-2xl bg-[#0099FF] px-8 text-sm sm:text-base font-bold text-white shadow-lg shadow-blue-500/30 hover:bg-[#0084FF] hover:shadow-xl hover:shadow-blue-500/40 hover:scale-105 active:scale-95 transition-all duration-200"
              >
                Mulai Gratis (Unduh APK)
              </a>
              <Link
                href="/docs"
                className="w-full sm:w-auto inline-flex h-12 sm:h-13 items-center justify-center rounded-2xl border border-white/20 bg-white/5 backdrop-blur-sm px-6 text-sm sm:text-base font-bold text-white hover:bg-white/10 hover:border-white/30 transition-all duration-200"
              >
                Lihat Panduan & Dokumentasi
              </Link>
            </div>

            {/* Value Checkmarks */}
            <div className="mt-10 pt-8 border-t border-white/10 grid grid-cols-2 sm:grid-cols-4 gap-4 text-center">
              <div className="flex items-center justify-center gap-1.5 text-xs font-semibold text-slate-300">
                <svg className="size-4 text-emerald-400 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clipRule="evenodd" />
                </svg>
                <span>Gratis tanpa kartu kredit</span>
              </div>
              <div className="flex items-center justify-center gap-1.5 text-xs font-semibold text-slate-300">
                <svg className="size-4 text-emerald-400 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clipRule="evenodd" />
                </svg>
                <span>Setup dalam 5 menit</span>
              </div>
              <div className="flex items-center justify-center gap-1.5 text-xs font-semibold text-slate-300">
                <svg className="size-4 text-emerald-400 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clipRule="evenodd" />
                </svg>
                <span>Bisa dikelola lewat HP</span>
              </div>
              <div className="flex items-center justify-center gap-1.5 text-xs font-semibold text-slate-300">
                <svg className="size-4 text-emerald-400 shrink-0" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clipRule="evenodd" />
                </svg>
                <span>Domain instan orgspace.app</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
