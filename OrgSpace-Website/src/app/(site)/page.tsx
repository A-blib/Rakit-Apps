import Link from "next/link";
import Image from "next/image";
import { getOverview } from "@/lib/api/public";
import type { PublicOverview } from "@/lib/api/types";

import { PainPointsList } from "@/components/PainPointsList";
import { SectionThreeShowcase } from "@/components/SectionThreeShowcase";
import { SectionFourWorkspace } from "@/components/SectionFourWorkspace";



export default async function LandingPage() {
  let overview: PublicOverview | null = null;
  try {
    overview = await getOverview();
  } catch {
    // Landing tetap tampil walau backend sedang mati; angka saja yang disembunyikan.
  }

  return (
    <div className="relative overflow-hidden bg-white text-foreground w-full">
      {/* =========================================================================
          1. HERO SECTION (SESUAI DOKUMEN SPESIFIKASI DESAIN RAKIT)
         ========================================================================= */}
      <section className="relative overflow-hidden bg-gradient-to-b from-[#F0F5FA] via-[#F8FAFC] to-[#FFFFFF] text-[#0B1E36] w-full pt-16">
        {/* Ornamen Geometris Pojok Kiri Atas (Tampil Bersih di Tablet & Desktop Sesuai Gambar Referensi) */}
        <div
          aria-hidden="true"
          className="pointer-events-none absolute top-0 left-0 z-0 overflow-hidden hidden sm:block sm:w-36 sm:h-40 md:w-48 md:h-52 lg:w-[220px] lg:h-[240px] select-none sm:opacity-75 md:opacity-100 transition-transform duration-700 hover:scale-[1.01]"
        >
          <svg className="w-full h-full" viewBox="0 0 240 260" fill="none">
            <defs>
              <linearGradient id="navyTL" x1="0" y1="0" x2="60" y2="60" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#051C3F" />
                <stop offset="100%" stopColor="#0B2654" />
              </linearGradient>
              <linearGradient id="cyanFacetTL" x1="60" y1="0" x2="0" y2="84" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#00D4FF" />
                <stop offset="50%" stopColor="#00A8FF" />
                <stop offset="100%" stopColor="#0080FF" />
              </linearGradient>
              <linearGradient id="royalCoreTL" x1="105" y1="0" x2="0" y2="132" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#0088FF" />
                <stop offset="35%" stopColor="#0062E4" />
                <stop offset="100%" stopColor="#003D99" />
              </linearGradient>
              <linearGradient id="orangeGradTL" x1="132" y1="0" x2="90" y2="42" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#FFAE00" />
                <stop offset="50%" stopColor="#FF7500" />
                <stop offset="100%" stopColor="#FF4000" />
              </linearGradient>
              <linearGradient id="lowerStripeTL" x1="58" y1="90" x2="0" y2="160" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#0066E0" />
                <stop offset="100%" stopColor="#003580" />
              </linearGradient>

              {/* Efek Neon Glow Halus */}
              <filter id="glowOrange" x="-30%" y="-30%" width="160%" height="160%">
                <feDropShadow dx="0" dy="2" stdDeviation="4" floodColor="#FF6A00" floodOpacity="0.5" />
                <feDropShadow dx="0" dy="0" stdDeviation="1.5" floodColor="#FFAA00" floodOpacity="0.6" />
              </filter>
              <filter id="glowBlue" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="2" stdDeviation="5" floodColor="#0088FF" floodOpacity="0.3" />
              </filter>
            </defs>

            {/* 1. Corner Navy Triangle */}
            <polygon points="0,0 60,0 0,60" fill="url(#navyTL)" />

            {/* 2. Cyan Diagonal Inner Band */}
            <polygon points="60,0 84,0 0,84 0,60" fill="url(#cyanFacetTL)" />

            {/* 3. Royal Blue Outer Body dengan Step Vertikal */}
            <g filter="url(#glowBlue)">
              <polygon points="84,0 105,0 74,31 74,58 0,132 0,84" fill="url(#royalCoreTL)" />
            </g>

            {/* Garis Aksen Kilau (Specular Highlight Sheen) */}
            <line x1="105" y1="0" x2="74" y2="31" stroke="#E0F7FF" strokeWidth="1.2" strokeLinecap="round" opacity="0.85" />
            <line x1="74" y1="31" x2="74" y2="58" stroke="#8DE4FF" strokeWidth="1.8" strokeLinecap="round" opacity="0.9" />
            <line x1="74" y1="58" x2="0" y2="132" stroke="#00C4FF" strokeWidth="1" opacity="0.65" />

            {/* 4. Strip Oranye Melayang dengan Potongan Vertikal & Glow Hangat */}
            <g filter="url(#glowOrange)">
              <polygon points="122,0 132,0 90,42 90,32" fill="url(#orangeGradTL)" />
              <line x1="122" y1="0" x2="90" y2="32" stroke="#FFE6B3" strokeWidth="0.8" opacity="0.7" />
            </g>

            {/* 5. Strip Biru Melayang Bawah dengan Potongan Vertikal */}
            <polygon points="0,148 58,90 58,102 0,160" fill="url(#lowerStripeTL)" />
            <line x1="0" y1="148" x2="58" y2="90" stroke="#8DE4FF" strokeWidth="1" opacity="0.6" />
          </svg>
        </div>

        {/* Main Content Area (Split Screen 50:50 & Z-Pattern) */}
        <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 pt-16 pb-8 sm:pt-20 sm:pb-12">
          <div className="grid items-center gap-8 lg:grid-cols-12 lg:gap-4">
            {/* Kolom Kiri: Teks & Aksi Persis Gambar Referensi */}
            <div className="lg:col-span-5 text-left z-10 min-w-0">
              {/* 2. Headline Utama: Fase 1 (Muncul pertama secara anggun, delay 150ms) */}
              <h1 className="animate-fade-in-up anim-delay-150 text-2xl xs:text-3xl sm:text-4xl md:text-5xl lg:text-[56px] font-black tracking-tight text-[#0B1E36] leading-[1.18] sm:leading-[1.08]">
                Bikin Website{" "}
                <span className="block sm:inline">Impian, </span>
                <span className="text-[#00A3FF]">
                  Tanpa Coding!
                </span>
              </h1>

              {/* 2. Sub-headline: Fase 2 (Menyusul setelah judul terbaca, delay 450ms) */}
              <p className="animate-fade-in-up anim-delay-450 mt-4 sm:mt-6 max-w-lg text-sm sm:text-base md:text-lg text-[#475569] leading-relaxed">
                Dari UMKM, organisasi, hingga portofolio pribadi—rakit website-mu dalam hitungan menit dengan mudah, cepat, dan profesional.
              </p>

              {/* 3. Dua Tombol Aksi Sejajar: Fase 3 (Mengajak aksi setelah pesan dipahami, delay 750ms) */}
              <div className="animate-fade-in-up anim-delay-750 mt-6 sm:mt-8 flex flex-col sm:flex-row items-stretch sm:items-center gap-3 sm:gap-4 w-full max-w-md">
                {overview?.androidDownloadUrl ? (
                  <a
                    href={overview.androidDownloadUrl}
                    className="relative group overflow-hidden inline-flex h-12 sm:h-13 items-center justify-center rounded-full bg-gradient-to-r from-[#FF7A00] via-[#FF8A00] to-[#FFA000] px-6 sm:px-8 text-sm sm:text-base font-bold text-white shadow-lg shadow-orange-500/25 hover:shadow-orange-500/40 hover:scale-[1.03] active:scale-95 transition-all duration-300"
                  >
                    <span className="relative z-10 flex items-center gap-2">
                      <span>Mulai Rakit Sekarang – Gratis!</span>
                      <span className="transition-transform duration-300 group-hover:translate-x-1 font-bold">→</span>
                    </span>
                    <span
                      aria-hidden="true"
                      className="animate-sheen pointer-events-none absolute inset-0 w-1/2 h-full bg-gradient-to-r from-transparent via-white/30 to-transparent"
                    />
                  </a>
                ) : (
                  <a
                    href="#akses-awal"
                    className="relative group overflow-hidden inline-flex h-12 sm:h-13 items-center justify-center rounded-full bg-gradient-to-r from-[#FF7A00] via-[#FF8A00] to-[#FFA000] px-6 sm:px-8 text-sm sm:text-base font-bold text-white shadow-lg shadow-orange-500/25 hover:shadow-orange-500/40 hover:scale-[1.03] active:scale-95 transition-all duration-300"
                  >
                    <span className="relative z-10 flex items-center gap-2">
                      <span>Mulai Rakit Sekarang – Gratis!</span>
                      <span className="transition-transform duration-300 group-hover:translate-x-1 font-bold">→</span>
                    </span>
                    <span
                      aria-hidden="true"
                      className="animate-sheen pointer-events-none absolute inset-0 w-1/2 h-full bg-gradient-to-r from-transparent via-white/30 to-transparent"
                    />
                  </a>
                )}
                <Link
                  href="/docs"
                  className="inline-flex h-12 sm:h-13 items-center justify-center rounded-full border-2 border-[#0B1E36] bg-transparent px-6 sm:px-8 text-sm sm:text-base font-bold text-[#0B1E36] hover:bg-[#0B1E36] hover:text-white hover:scale-[1.02] active:scale-95 transition-all duration-200"
                >
                  Lihat Demo
                </Link>
              </div>

              {/* Tiga Poin Ringkas di Bawah Tombol (Delay 1050ms) */}
              <div className="animate-fade-in-up anim-delay-1050 mt-5 sm:mt-6 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs sm:text-sm font-medium text-[#475569]">
                <span className="flex items-center gap-1.5">
                  <span className="text-[#00A3FF] font-bold">✓</span> Tanpa Kartu Kredit
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="text-[#00A3FF] font-bold">✓</span> Siap Online 5 Menit
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="text-[#00A3FF] font-bold">✓</span> Hosting & SSL Otomatis
                </span>
              </div>
            </div>

            {/* Kolom Kanan: Visual Utama Meja Kerja & Laptop Mockup (Fase 2.5: delay 550ms, scale-in & float) */}
            <div className="lg:col-span-7 relative flex justify-center lg:justify-end items-center mt-4 lg:mt-0 animate-fade-in-scale anim-delay-550">
              <div className="relative w-full max-w-xl lg:max-w-none animate-float">
                <Image
                  src="/images/orgspace-workspace-hero.png"
                  alt="Mockup Workspace Rakit Website di Meja Kerja dengan Laptop dan Sticky Notes"
                  width={1548}
                  height={1080}
                  className="w-full h-auto object-contain select-none drop-shadow-2xl"
                  priority
                />

                {/* 5. Sticky Notes Interaktif — Wobble & Micro-Rotation saat Hover */}
                {/* Sticky Note Kuning (Ide -> Website Jadi Nyata) */}
                <div
                  className="animate-wobble-hover group absolute top-[15%] right-[10%] sm:right-[11%] w-[18%] sm:w-[17%] aspect-square cursor-pointer transition-transform duration-300"
                  title="Ide Website Jadi Nyata"
                >
                  <span className="sr-only">Sticky Note: Ide Website Jadi Nyata</span>
                  <div className="absolute -top-7 left-1/2 -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity bg-[#0B1E36] text-white text-[10px] font-semibold py-0.5 px-2 rounded whitespace-nowrap shadow-md pointer-events-none z-30">
                    💡 Klik & susun ide instan!
                  </div>
                </div>

                {/* Sticky Note Biru (Emoticon Senyum) */}
                <div
                  className="animate-wobble-hover group absolute top-[19%] right-[1%] sm:right-[2%] w-[12%] sm:w-[11%] aspect-square cursor-pointer transition-transform duration-300"
                  title="Mudah dan Menyenangkan"
                >
                  <span className="sr-only">Sticky Note Senyum</span>
                  <div className="absolute -top-7 left-1/2 -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity bg-[#0B1E36] text-white text-[10px] font-semibold py-0.5 px-2 rounded whitespace-nowrap shadow-md pointer-events-none z-30">
                    😊 Mudah digunakan siapapun!
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Bagian Bawah: 3 Poin Keunggulan Utama (Fase 5: delay 1350ms, 1650ms, 1950ms) */}
          <div className="mt-12 sm:mt-16">
            <div className="grid grid-cols-1 gap-4 sm:gap-6 md:grid-cols-3">
              {/* 1. Tanpa Coding (Kemunculan Pertama: delay 1350ms) */}
              <div className="animate-fade-in-up anim-delay-1350 group/card flex items-center gap-3.5 sm:gap-4.5 rounded-2xl border border-[#E2E8F0] bg-white p-4 sm:p-5 shadow-xs hover:border-[#0099FF]/50 hover:shadow-xl hover:-translate-y-1.5 transition-all duration-300 cursor-default">
                <div className="flex-shrink-0 flex h-12 w-12 sm:h-14 sm:w-14 items-center justify-center rounded-2xl bg-[#EFF6FF] border border-[#DBEAFE] shadow-xs transition-transform duration-300 group-hover/card:scale-110 group-hover/card:rotate-3">
                  <svg className="h-8 w-8 sm:h-10 sm:w-10 shrink-0" viewBox="0 0 48 48" fill="none">
                    <rect x="6" y="6" width="36" height="36" rx="10" fill="#F0F9FF" />
                    <rect x="13" y="13" width="9" height="9" rx="3.5" fill="#00A3FF" />
                    <rect x="26" y="13" width="9" height="9" rx="3.5" fill="#FF8A00" />
                    <rect x="13" y="26" width="9" height="9" rx="3.5" fill="#0066E0" />
                    <circle cx="30.5" cy="30.5" r="4.5" fill="#FF4500" />
                    <g filter="url(#cursorDropShadow)">
                      <path
                        d="M27 22l7 14-3 1-2.5-5.5-3.5 3.5V22z"
                        fill="#0B1E36"
                        stroke="#FFFFFF"
                        strokeWidth="2"
                        strokeLinejoin="round"
                      />
                    </g>
                  </svg>
                </div>
                <div>
                  <h3 className="text-sm sm:text-base font-bold text-[#0B1E36] group-hover/card:text-[#0099FF] transition-colors">
                    Tanpa Coding
                  </h3>
                  <p className="text-xs text-[#475569] mt-0.5 leading-relaxed">Tinggal klik, geser, dan sesuaikan sesukamu.</p>
                </div>
              </div>

              {/* 2. Untuk Siapa Saja (Kemunculan Kedua: delay 1650ms) */}
              <div className="animate-fade-in-up anim-delay-1650 group/card flex items-center gap-3.5 sm:gap-4.5 rounded-2xl border border-[#E2E8F0] bg-white p-4 sm:p-5 shadow-xs hover:border-[#FFAA00]/50 hover:shadow-xl hover:-translate-y-1.5 transition-all duration-300 cursor-default">
                <div className="flex-shrink-0 flex h-12 w-12 sm:h-14 sm:w-14 items-center justify-center rounded-2xl bg-[#EFF6FF] border border-[#DBEAFE] shadow-xs transition-transform duration-300 group-hover/card:scale-110 group-hover/card:-rotate-3">
                  <svg className="h-8 w-8 sm:h-10 sm:w-10 shrink-0" viewBox="0 0 48 48" fill="none">
                    <circle cx="24" cy="24" r="22" fill="#00A3FF" />
                    <circle cx="16" cy="17" r="4.5" fill="#FCD34D" />
                    <path d="M10 27c0-3 2.5-5 6-5s6 2 6 5" fill="#1E293B" />
                    <circle cx="32" cy="17" r="4.5" fill="#F87171" />
                    <path d="M26 27c0-3 2.5-5 6-5s6 2 6 5" fill="#002D72" />
                    <circle cx="24" cy="22" r="5.5" fill="#FBBF24" />
                    <path d="M16 35c0-4.5 3.5-7 8-7s8 2.5 8 7" fill="#002D72" />
                    <path d="M19 18c1-3 9-3 10 0v2c-2-1-8-1-10 0v-2z" fill="#0B1E36" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-sm sm:text-base font-bold text-[#0B1E36] group-hover/card:text-[#FF7500] transition-colors">
                    Untuk Siapa Saja
                  </h3>
                  <p className="text-xs text-[#475569] mt-0.5 leading-relaxed">Cocok untuk UMKM, organisasi, sekolah, hingga bisnis personal.</p>
                </div>
              </div>

              {/* 3. Responsif & Cepat (Kemunculan Ketiga: delay 1950ms) */}
              <div className="animate-fade-in-up anim-delay-1950 group/card flex items-center gap-3.5 sm:gap-4.5 rounded-2xl border border-[#E2E8F0] bg-white p-4 sm:p-5 shadow-xs hover:border-[#00A3FF]/50 hover:shadow-xl hover:-translate-y-1.5 transition-all duration-300 cursor-default">
                <div className="flex-shrink-0 flex h-12 w-12 sm:h-14 sm:w-14 items-center justify-center rounded-2xl bg-[#EFF6FF] border border-[#DBEAFE] shadow-xs transition-transform duration-300 group-hover/card:scale-110 group-hover/card:rotate-3">
                  <svg className="h-8 w-8 sm:h-10 sm:w-10 shrink-0" viewBox="0 0 48 48" fill="none">
                    <rect x="4" y="10" width="30" height="20" rx="3" fill="#0B1E36" />
                    <rect x="6.5" y="12.5" width="25" height="15" rx="1.5" fill="#00A3FF" />
                    <path d="M15 30h8v3h-8zM9 33h20v2H9z" fill="#64748B" />
                    <rect x="9" y="15" width="10" height="10" rx="1" fill="#FFFFFF" fillOpacity="0.85" />
                    <rect x="21" y="15" width="8" height="4" rx="1" fill="#FFAA00" />
                    <rect x="21" y="21" width="8" height="4" rx="1" fill="#FFFFFF" fillOpacity="0.85" />
                    <rect x="27" y="18" width="15" height="21" rx="3" fill="#002D72" stroke="#FFFFFF" strokeWidth="1.5" />
                    <rect x="29" y="20.5" width="11" height="14" rx="1.5" fill="#00A3FF" />
                    <rect x="31" y="23" width="7" height="9" rx="1" fill="#FFAA00" />
                    <circle cx="34.5" cy="36.5" r="1" fill="#FFFFFF" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-sm sm:text-base font-bold text-[#0B1E36] group-hover/card:text-[#0099FF] transition-colors">
                    Responsif & Cepat
                  </h3>
                  <p className="text-xs text-[#475569] mt-0.5 leading-relaxed">Website otomatis terlihat keren di HP, tablet, dan laptop.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* =========================================================================
          SECTION 2: MASIH RIBET KELOLA WEBSITE SENDIRI? (PROBLEM STATEMENT)
          Persis Desain Referensi (Dua Kolom Seimbang: Ilustrasi di Kiri, Teks & 6 Poin di Kanan)
         ========================================================================= */}
      <section className="relative w-full overflow-hidden bg-gradient-to-b from-[#FFFFFF] via-[#F8FAFC] to-[#FFFFFF] text-[#0B1E36] py-14 sm:py-20 lg:py-24">
        <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-12 items-center gap-10 lg:gap-8 xl:gap-14">
            {/* Kolom Kiri: Ilustrasi Orang di Meja Kerja & Sticky Notes (Proporsi Utuh Tanpa Terpotong & Menyatu Mulus) */}
            <div className="lg:col-span-6 w-full flex items-center justify-center lg:justify-start">
              <div className="illustration-container w-full max-w-xl lg:max-w-none bg-transparent p-0 border-none shadow-none flex justify-center">
                <Image
                  src="/images/problem-illustration-clean.png"
                  alt="Ilustrasi kesulitan dan kebingungan mengelola website sendiri"
                  width={1602}
                  height={1536}
                  className="illustration-fade w-full h-auto object-contain select-none"
                  style={{
                    WebkitMaskImage:
                      "linear-gradient(to right, transparent, black 8%, black 78%, transparent), linear-gradient(to bottom, transparent, black 6%, black 85%, transparent)",
                    WebkitMaskComposite: "source-in",
                    maskImage:
                      "linear-gradient(to right, transparent, black 8%, black 78%, transparent), linear-gradient(to bottom, transparent, black 6%, black 85%, transparent)",
                    maskComposite: "intersect",
                  }}
                  priority={false}
                />
              </div>
            </div>

            {/* Kolom Kanan: Teks & 6 Poin Masalah (Persis Gambar Referensi Tanpa Kotak Card) */}
            <div className="lg:col-span-6 text-left z-10 min-w-0 w-full">
              <h2 className="text-3xl sm:text-4xl md:text-[44px] lg:text-[48px] font-black tracking-tight text-[#0B2545] leading-[1.18] sm:leading-[1.12]">
                Masih ribet kelola<br />
                website sendiri?
              </h2>

              <p className="mt-4 sm:mt-5 text-sm sm:text-base md:text-lg text-[#475569] leading-relaxed max-w-xl">
                Banyak UMKM dan organisasi kesulitan membuat dan mengelola website karena keterbatasan waktu, tenaga, dan pengetahuan teknis.
              </p>

              {/* Grid 6 Pain Points dengan animasi staggered dari bawah ke atas */}
              <PainPointsList />
            </div>
          </div>
        </div>
      </section>



      {/* =========================================================================
          3. SHOWCASE MOCKUP SMARTPHONE BERPUTAR & INTERAKTIF (FITUR UTAMA)
         ========================================================================= */}
      <SectionThreeShowcase />

      {/* =========================================================================
          4. SECTION 4: WEBSITE BUILDER CTA WORKSPACE
         ========================================================================= */}
      <SectionFourWorkspace androidDownloadUrl={overview?.androidDownloadUrl} />
    </div>
  );
}
