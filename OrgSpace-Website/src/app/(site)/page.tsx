import Link from "next/link";
import Image from "next/image";
import { getOverview } from "@/lib/api/public";
import type { PublicOverview } from "@/lib/api/types";
import { formatNumber } from "@/lib/format";
import { LeadGenerationForm } from "@/components/LeadGenerationForm";

/**
 * 4 Manfaat Utama Aplikasi Rakit (Value Proposition).
 * Menggunakan ikon minimalis Electric Cyan dan kartu Glassmorphism interaktif.
 */
const FEATURES = [
  {
    title: "Template Siap Pakai",
    badge: "50+ Desain Khusus",
    body: "Tak perlu bingung mendesain dari nol. Pilih dari puluhan struktur khusus yang dirancang presisi untuk publikasi OSIS, BEM, kabinet kerja, portfolio komunitas, hingga event akbar.",
    icon: (
      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
      </svg>
    ),
  },
  {
    title: "Edit Semuanya dari HP",
    badge: "Sentuh & Sesuaikan",
    body: "Ganti teks profil, perbarui foto dokumentasi, dan ubah palet warna semudah mengunggah story medsos. Antarmuka sentuh intuitif yang dirancang cepat dan tanpa hambatan teknis.",
    icon: (
      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
      </svg>
    ),
  },
  {
    title: "Kolaborasi Tim Pengurus",
    badge: "Akses Multi-Admin",
    body: "Bagikan akses peran Pemilik, Admin, dan Editor ke seluruh divisi pengurus. Delegasikan pembaruan artikel dan agenda tanpa khawatir setelan sistem terganggu.",
    icon: (
      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
      </svg>
    ),
  },
  {
    title: "Otomatis Online",
    badge: "Hosting & SSL Gratis",
    body: "Lupakan sewa server rumit atau mengatur DNS. Website langsung terbit dengan alamat resmi organisasimu, terlindungi sertifikat SSL otomatis, dan siap diakses publik seketika.",
    icon: (
      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
        <path strokeLinecap="round" strokeLinejoin="round" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
      </svg>
    ),
  },
];

export default async function LandingPage() {
  let overview: PublicOverview | null = null;
  try {
    overview = await getOverview();
  } catch {
    // Landing tetap tampil walau backend sedang mati; angka saja yang disembunyikan.
  }

  return (
    <div className="relative overflow-hidden bg-rakit-navy text-foreground w-full">
      {/* Ambient Radial Mesh Lighting */}
      <div aria-hidden="true" className="pointer-events-none absolute inset-0 overflow-hidden select-none">
        <div className="absolute top-0 left-0 h-64 w-64 sm:h-[500px] sm:w-[500px] rounded-full bg-rakit-cyan/10 blur-[80px] sm:blur-[140px]" />
        <div className="absolute top-[30%] right-0 h-64 w-64 sm:h-[500px] sm:w-[500px] rounded-full bg-rakit-orange/10 blur-[80px] sm:blur-[160px]" />
      </div>

      {/* =========================================================================
          1. HERO SECTION (SESUAI DOKUMEN SPESIFIKASI DESAIN RAKIT)
         ========================================================================= */}
      <section className="relative overflow-hidden bg-white text-[#0B1E36] w-full">
        {/* Gradasi Lembut Latar Belakang Tepi (#FFFFFF ke #F8FAFC / #F0F4F8) */}
        <div
          aria-hidden="true"
          className="pointer-events-none absolute inset-0 bg-gradient-to-b from-[#FFFFFF] via-[#F8FAFC] to-[#F0F4F8] opacity-90"
        />

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

        {/* Ornamen Geometris Pojok Kiri Bawah (Soft Ice-Blue Translucent Facet) */}
        <div
          aria-hidden="true"
          className="pointer-events-none absolute bottom-0 left-0 z-0 overflow-hidden hidden sm:block sm:w-72 sm:h-54 md:w-96 md:h-72 select-none opacity-80"
        >
          <svg className="w-full h-full" viewBox="0 0 400 300" fill="none">
            <defs>
              <linearGradient id="blSoftGrad" x1="0" y1="300" x2="300" y2="150" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#00A3FF" stopOpacity="0.08" />
                <stop offset="60%" stopColor="#0099FF" stopOpacity="0.04" />
                <stop offset="100%" stopColor="#0077FF" stopOpacity="0" />
              </linearGradient>
            </defs>
            <polygon points="0,300 0,160 260,300" fill="url(#blSoftGrad)" />
            <polygon points="0,300 0,220 380,300" fill="#0055D4" fillOpacity="0.03" />
          </svg>
        </div>

        {/* Ornamen Geometris Pojok Kanan Bawah (Pasangan Simetris dengan Efek Neon Glow & Specular Sheen) */}
        <div
          aria-hidden="true"
          className="pointer-events-none absolute bottom-0 right-0 z-0 overflow-hidden hidden sm:block sm:w-36 sm:h-40 md:w-48 md:h-52 lg:w-[220px] lg:h-[240px] select-none sm:opacity-75 md:opacity-100 transition-transform duration-700 hover:scale-[1.01]"
        >
          <svg className="w-full h-full rotate-180" viewBox="0 0 240 260" fill="none">
            <defs>
              <linearGradient id="navyBR" x1="0" y1="0" x2="60" y2="60" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#051C3F" />
                <stop offset="100%" stopColor="#0B2654" />
              </linearGradient>
              <linearGradient id="cyanFacetBR" x1="60" y1="0" x2="0" y2="84" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#00D4FF" />
                <stop offset="50%" stopColor="#00A8FF" />
                <stop offset="100%" stopColor="#0080FF" />
              </linearGradient>
              <linearGradient id="royalCoreBR" x1="105" y1="0" x2="0" y2="132" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#0088FF" />
                <stop offset="35%" stopColor="#0062E4" />
                <stop offset="100%" stopColor="#003D99" />
              </linearGradient>
              <linearGradient id="orangeGradBR" x1="132" y1="0" x2="90" y2="42" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#FFAE00" />
                <stop offset="50%" stopColor="#FF7500" />
                <stop offset="100%" stopColor="#FF4000" />
              </linearGradient>
              <linearGradient id="lowerStripeBR" x1="58" y1="90" x2="0" y2="160" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#0066E0" />
                <stop offset="100%" stopColor="#003580" />
              </linearGradient>

              {/* Efek Neon Glow Halus */}
              <filter id="glowOrangeBR" x="-30%" y="-30%" width="160%" height="160%">
                <feDropShadow dx="0" dy="2" stdDeviation="4" floodColor="#FF6A00" floodOpacity="0.5" />
                <feDropShadow dx="0" dy="0" stdDeviation="1.5" floodColor="#FFAA00" floodOpacity="0.6" />
              </filter>
              <filter id="glowBlueBR" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="2" stdDeviation="5" floodColor="#0088FF" floodOpacity="0.3" />
              </filter>
            </defs>

            {/* 1. Corner Navy Triangle */}
            <polygon points="0,0 60,0 0,60" fill="url(#navyBR)" />

            {/* 2. Cyan Diagonal Inner Band */}
            <polygon points="60,0 84,0 0,84 0,60" fill="url(#cyanFacetBR)" />

            {/* 3. Royal Blue Outer Body dengan Step Vertikal */}
            <g filter="url(#glowBlueBR)">
              <polygon points="84,0 105,0 74,31 74,58 0,132 0,84" fill="url(#royalCoreBR)" />
            </g>

            {/* Garis Aksen Kilau (Specular Highlight Sheen) */}
            <line x1="105" y1="0" x2="74" y2="31" stroke="#E0F7FF" strokeWidth="1.2" strokeLinecap="round" opacity="0.85" />
            <line x1="74" y1="31" x2="74" y2="58" stroke="#8DE4FF" strokeWidth="1.8" strokeLinecap="round" opacity="0.9" />
            <line x1="74" y1="58" x2="0" y2="132" stroke="#00C4FF" strokeWidth="1" opacity="0.65" />

            {/* 4. Strip Oranye Melayang dengan Potongan Vertikal & Glow Hangat */}
            <g filter="url(#glowOrangeBR)">
              <polygon points="122,0 132,0 90,42 90,32" fill="url(#orangeGradBR)" />
              <line x1="122" y1="0" x2="90" y2="32" stroke="#FFE6B3" strokeWidth="0.8" opacity="0.7" />
            </g>

            {/* 5. Strip Biru Melayang dengan Potongan Vertikal */}
            <polygon points="0,148 58,90 58,102 0,160" fill="url(#lowerStripeBR)" />
            <line x1="0" y1="148" x2="58" y2="90" stroke="#8DE4FF" strokeWidth="1" opacity="0.6" />
          </svg>
        </div>

        {/* Main Content Area (Split Screen 50:50 & Z-Pattern) */}
        <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 pt-20 pb-16 md:pt-24 md:pb-20">
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
                    className="relative overflow-hidden group inline-flex h-11 sm:h-13 items-center justify-center gap-2 rounded-full px-5 sm:px-8 text-xs sm:text-base font-bold text-white bg-gradient-to-r from-[#FF6A00] to-[#FFAA00] shadow-[0px_8px_25px_rgba(255,106,0,0.35)] hover:shadow-[0px_16px_40px_rgba(255,106,0,0.55)] hover:scale-[1.03] hover:-translate-y-0.5 active:scale-95 active:translate-y-0 transition-all duration-300 text-center whitespace-nowrap"
                  >
                    {/* Efek Kilau Spekular (Specular Sheen Sweep) */}
                    <span
                      aria-hidden="true"
                      className="pointer-events-none absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/30 to-transparent group-hover:translate-x-full transition-transform duration-1000 ease-in-out"
                    />
                    <span className="relative z-1">Mulai Rakit Sekarang – Gratis!</span>
                    <span className="relative z-1 transition-transform duration-300 group-hover:translate-x-1.5">→</span>
                  </a>
                ) : (
                  <a
                    href="#akses-awal"
                    className="relative overflow-hidden group inline-flex h-11 sm:h-13 items-center justify-center gap-2 rounded-full px-5 sm:px-8 text-xs sm:text-base font-bold text-white bg-gradient-to-r from-[#FF6A00] to-[#FFAA00] shadow-[0px_8px_25px_rgba(255,106,0,0.35)] hover:shadow-[0px_16px_40px_rgba(255,106,0,0.55)] hover:scale-[1.03] hover:-translate-y-0.5 active:scale-95 active:translate-y-0 transition-all duration-300 text-center whitespace-nowrap"
                  >
                    {/* Efek Kilau Spekular (Specular Sheen Sweep) */}
                    <span
                      aria-hidden="true"
                      className="pointer-events-none absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/30 to-transparent group-hover:translate-x-full transition-transform duration-1000 ease-in-out"
                    />
                    <span className="relative z-1">Mulai Rakit Sekarang – Gratis!</span>
                    <span className="relative z-1 transition-transform duration-300 group-hover:translate-x-1.5">→</span>
                  </a>
                )}
                <Link
                  href="/#fitur"
                  className="inline-flex h-11 sm:h-13 items-center justify-center gap-2 rounded-full border-2 border-[#0B1E36] bg-transparent px-5 sm:px-8 text-xs sm:text-base font-bold text-[#0B1E36] hover:bg-[#0B1E36] hover:text-white hover:scale-[1.03] hover:-translate-y-0.5 hover:shadow-lg active:scale-95 active:translate-y-0 transition-all duration-300 text-center whitespace-nowrap"
                >
                  Lihat Demo
                </Link>
              </div>

              {/* Nilai Tambah Mikro: Fase 4 (Penegasan kepercayaan, delay 1050ms) */}
              <div className="animate-fade-in-up anim-delay-1050 mt-6 sm:mt-7 flex flex-wrap items-center gap-y-2 gap-x-4 sm:gap-x-6 text-xs font-semibold text-[#475569]">
                <span className="flex items-center gap-1.5 whitespace-nowrap">
                  <span className="text-[#0099FF] font-bold">✓</span> Tanpa Kartu Kredit
                </span>
                <span className="flex items-center gap-1.5 whitespace-nowrap">
                  <span className="text-[#0099FF] font-bold">✓</span> Siap Online 5 Menit
                </span>
                <span className="flex items-center gap-1.5 whitespace-nowrap">
                  <span className="text-[#0099FF] font-bold">✓</span> Hosting & SSL Otomatis
                </span>
              </div>
            </div>

            {/* Kolom Kanan: Visual Ruang Kerja Mekar Halus (Fase 2-3, delay 550ms) */}
            <div className="lg:col-span-7 relative lg:-mr-6 xl:-mr-12 2xl:-mr-16 overflow-hidden min-w-0 animate-fade-in-scale anim-delay-550">
              {/* Layer 1: Ambient Lighting Lembut di Belakang Laptop */}
              <div aria-hidden="true" className="pointer-events-none absolute inset-0 overflow-hidden select-none">
                <div
                  className="absolute top-0 right-0 h-48 w-48 sm:h-96 sm:w-96 rounded-full bg-gradient-to-br from-[#00A3FF]/15 via-[#8DE4FF]/10 to-transparent blur-2xl sm:blur-3xl animate-pulse-glow"
                />
                <div
                  className="absolute bottom-0 left-10 h-48 w-48 sm:h-80 sm:w-80 rounded-full bg-gradient-to-tr from-[#FFAA00]/15 via-[#FF6A00]/10 to-transparent blur-2xl sm:blur-3xl animate-pulse-glow"
                  style={{ animationDelay: "1.5s" }}
                />
              </div>

              {/* 4. Visual Ruang Kerja dengan Efek Floating Animation (Melayang Halus & Kontinu) */}
              <div
                className="relative group animate-float"
                style={{
                  maskImage: "radial-gradient(ellipse 95% 88% at 53% 50%, black 50%, transparent 95%)",
                  WebkitMaskImage: "radial-gradient(ellipse 95% 88% at 53% 50%, black 50%, transparent 95%)",
                }}
              >
                <Image
                  src="/images/orgspace-workspace-hero.png"
                  alt="Ruang kerja fotorealistik laptop drag-and-drop builder Orgspace, cangkir kopi, buku catatan, tanaman, dan sticky notes"
                  width={918}
                  height={605}
                  priority
                  className="h-auto w-full object-contain select-none transition-transform duration-700 ease-out group-hover:scale-[1.015]"
                />

                {/* 5. Sticky Note 1 (Kuning): Subtle Wobble & Micro-Rotation on Hover */}
                <div
                  title="Ide -> Website -> Jadi Nyata"
                  className="absolute top-[14%] right-[20%] w-[11%] h-[16%] cursor-pointer z-20 group/note rounded-xs transition-transform duration-300 hover:scale-110 animate-wobble-hover"
                  aria-label="Catatan Ide Organisasi"
                >
                  <div className="opacity-0 group-hover/note:opacity-100 transition-opacity duration-300 absolute -top-3 left-1/2 -translate-x-1/2 px-2.5 py-1 bg-[#0B1E36] text-white text-[10px] font-bold rounded-lg whitespace-nowrap shadow-xl pointer-events-none border border-white/20">
                    💡 Ide Jadi Nyata
                  </div>
                </div>

                {/* 5. Sticky Note 2 (Biru): Subtle Wobble & Micro-Rotation on Hover */}
                <div
                  title="OrgSpace: Siap Rilis :)"
                  className="absolute top-[18%] right-[10%] w-[10%] h-[15%] cursor-pointer z-20 group/note rounded-xs transition-transform duration-300 hover:scale-110 animate-wobble-hover"
                  aria-label="Catatan Senyum OrgSpace"
                >
                  <div className="opacity-0 group-hover/note:opacity-100 transition-opacity duration-300 absolute -top-3 left-1/2 -translate-x-1/2 px-2.5 py-1 bg-[#0099FF] text-white text-[10px] font-bold rounded-lg whitespace-nowrap shadow-xl pointer-events-none border border-white/20">
                    😊 Mudah & Cepat
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* =========================================================================
              6. TIGA POIN FITUR DI BAWAH: STAGGERED SLIDE-IN (KEMUNCULAN BERURUTAN SANTAI)
             ========================================================================= */}
          <div className="mt-8 sm:mt-10 grid grid-cols-1 md:grid-cols-3 gap-3.5 sm:gap-6">
            {/* 1. Tanpa Coding (Kemunculan Pertama: delay 1350ms) */}
            <div className="animate-fade-in-up anim-delay-1350 group/card flex items-center gap-3.5 sm:gap-4.5 rounded-2xl border border-[#E2E8F0] bg-white p-4 sm:p-5 shadow-xs hover:border-[#00A3FF]/50 hover:shadow-xl hover:-translate-y-1.5 transition-all duration-300 cursor-default">
              <div className="flex-shrink-0 flex h-12 w-12 sm:h-14 sm:w-14 items-center justify-center rounded-2xl bg-[#EFF6FF] border border-[#DBEAFE] shadow-xs transition-transform duration-300 group-hover/card:scale-110 group-hover/card:rotate-3">
                <svg className="h-8 w-8 sm:h-10 sm:w-10 shrink-0" viewBox="0 0 48 48" fill="none">
                  <rect x="6" y="6" width="16" height="16" rx="4" fill="#00A3FF" />
                  <rect x="26" y="6" width="16" height="16" rx="4" fill="#FFAA00" />
                  <rect x="6" y="26" width="16" height="16" rx="4" fill="#0052CC" />
                  <rect x="26" y="26" width="16" height="16" rx="4" fill="#FF6A00" />
                  <circle cx="14" cy="14" r="3" fill="#FFFFFF" fillOpacity="0.8" />
                  <circle cx="34" cy="14" r="3" fill="#FFFFFF" fillOpacity="0.8" />
                  <g filter="drop-shadow(0px 2px 4px rgba(0,0,0,0.15))">
                    <path
                      d="M20 22l4 14 3.5-4.5 4.5 4.5 3-3-4.5-4.5 5.5-1.5L20 22z"
                      fill="#FFAA00"
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
      </section>

      {/* =========================================================================
          STATISTIK SOSIAL (Jika Data Backend Tersedia)
         ========================================================================= */}
      {overview && (
        <section aria-label="Rakit dalam angka" className="relative mx-auto max-w-6xl px-4 pb-20">
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            {[
              ["Organisasi Terdaftar", overview.stats.organizations],
              ["Website Online Aktif", overview.stats.websitesOnline],
              ["Template Desain", overview.stats.templates],
              ["Kreator Komunitas", overview.stats.providers],
            ].map(([label, value]) => (
              <div key={label} className="card-glass p-5 text-center">
                <p className="text-2xl sm:text-3xl font-extrabold text-white">
                  <span className="gradient-rakit-text">{formatNumber(value as number)}</span>+
                </p>
                <p className="mt-1 text-xs font-medium text-muted uppercase tracking-wider">{label}</p>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* =========================================================================
          2. PENJELASAN MANFAAT (VALUE PROPOSITION & BENEFITS)
         ========================================================================= */}
      <section id="fitur" className="relative mx-auto max-w-6xl px-4 py-16 sm:py-20">
        <div className="text-center max-w-2xl mx-auto mb-12 sm:mb-14">
          <span className="inline-flex items-center gap-1.5 rounded-full border border-rakit-cyan/30 bg-rakit-cyan/10 px-3.5 py-1 text-xs font-bold tracking-wide text-rakit-cyan">
            KEUNGGULAN UTAMA
          </span>
          <h2 className="mt-4 text-2xl sm:text-3xl font-extrabold text-white md:text-4xl">
            Semua yang Dibutuhkan Pengurus Organisasi
          </h2>
          <p className="mt-3 text-sm text-muted md:text-base">
            Dirancang khusus untuk memangkas hambatan teknis pengurus agar fokus membangun karya dan program kerja terbaik.
          </p>
        </div>

        {/* 4 Kartu Fitur Glassmorphism */}
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {FEATURES.map((feature) => (
            <div key={feature.title} className="card-glass-interactive flex flex-col justify-between p-5 sm:p-6">
              <div>
                {/* Ikon Minimalis Biru Terang */}
                <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-xl border border-rakit-cyan/30 bg-rakit-cyan/10 text-rakit-cyan shadow-[0_0_15px_rgba(0,242,254,0.2)]">
                  {feature.icon}
                </div>
                <div className="mb-2">
                  <span className="text-[10px] font-bold text-rakit-orange uppercase tracking-wider">
                    {feature.badge}
                  </span>
                </div>
                <h3 className="text-lg font-bold text-white">{feature.title}</h3>
                <p className="mt-2 text-xs leading-relaxed text-muted">{feature.body}</p>
              </div>

              <div className="mt-6 border-t border-white/5 pt-3">
                <span className="inline-flex items-center gap-1 text-xs font-semibold text-rakit-cyan hover:underline cursor-pointer">
                  Pelajari selengkapnya <span>→</span>
                </span>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* =========================================================================
          3. LEAD GENERATION FORM (FORMULIR AKSES AWAL / DEMO)
         ========================================================================= */}
      <section id="akses-awal" className="relative mx-auto max-w-6xl px-4 py-16 sm:py-20">
        <LeadGenerationForm />
      </section>

      {/* =========================================================================
          4. FINAL CALL-TO-ACTION (BAGIAN BAWAH)
         ========================================================================= */}
      <section className="relative mx-auto max-w-6xl px-4 pt-8 pb-24 sm:pb-28">
        <div className="relative overflow-hidden rounded-3xl border border-rakit-cyan/30 bg-gradient-to-r from-rakit-surface to-rakit-surface-elevated p-6 sm:p-10 md:p-14 shadow-2xl">
          {/* Ambient Lighting Overlay properly contained */}
          <div aria-hidden="true" className="pointer-events-none absolute inset-0 overflow-hidden rounded-3xl select-none">
            <div className="absolute top-0 right-0 h-48 w-48 sm:h-72 sm:w-72 rounded-full bg-rakit-cyan/20 blur-2xl sm:blur-3xl" />
            <div className="absolute bottom-0 left-0 h-48 w-48 sm:h-72 sm:w-72 rounded-full bg-rakit-orange/20 blur-2xl sm:blur-3xl" />
          </div>

          <div className="relative z-1 max-w-3xl">
            <span className="inline-flex items-center gap-1.5 rounded-full border border-rakit-cyan/30 bg-rakit-cyan/10 px-3.5 py-1 text-xs font-bold text-rakit-cyan">
              LANGKAH BERIKUTNYA
            </span>
            <h2 className="mt-4 text-2xl sm:text-3xl font-extrabold text-white md:text-5xl leading-tight">
              Siap merakit masa depan digital organisasimu hari ini?
            </h2>
            <p className="mt-4 text-sm sm:text-base text-muted md:text-lg">
              Buktikan bahwa memiliki website resmi berkelas profesional tidak butuh keahlian koding rumit atau anggaran jutaan rupiah. Mulai langkah pertamamu sekarang.
            </p>

            {/* Tombol Aksi Sejajar (Dual CTA) */}
            <div className="mt-6 sm:mt-8 flex flex-col sm:flex-row items-stretch sm:items-center gap-3 sm:gap-4">
              <a href="#akses-awal" className="btn-orange-glow text-sm sm:text-base font-bold text-center">
                Mulai Merakit Sekarang
              </a>
              <Link href="/docs" className="btn-glass-outline text-sm sm:text-base font-bold text-center">
                Lihat Cara Kerjanya ▷
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
