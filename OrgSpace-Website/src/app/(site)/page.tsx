import Image from "next/image";
import { getLandingTemplates, getLatestUpdates } from "@/lib/api/public";

import { PainPointsList } from "@/components/PainPointsList";
import { SectionThreeShowcase } from "@/components/SectionThreeShowcase";
import { FeaturesSection } from "@/components/FeaturesSection";
import { FeaturedTemplates } from "@/components/FeaturedTemplates";
import { TestimonialsSection } from "@/components/TestimonialsSection";
import { LatestUpdates } from "@/components/LatestUpdates";
import { CtaSection } from "@/components/CtaSection";

export default async function LandingPage() {
  const [featuredTemplates, latestUpdates] = await Promise.all([getLandingTemplates(), getLatestUpdates()]);

  return (
    <div className="relative overflow-hidden bg-white dark:bg-[#07152B] text-foreground dark:text-white w-full transition-colors duration-300">
      {/* =========================================================================
          1. SHOWCASE MOCKUP SMARTPHONE BERPUTAR & INTERAKTIF (HERO UTAMA)
         ========================================================================= */}
      <SectionThreeShowcase />

      {/* =========================================================================
          2. MASIH RIBET KELOLA WEBSITE SENDIRI? (PROBLEM STATEMENT)
          Persis Desain Referensi (Dua Kolom Seimbang: Ilustrasi di Kiri, Teks & 6 Poin di Kanan)
         ========================================================================= */}
      <section className="relative w-full overflow-hidden bg-gradient-to-b from-[#FFFFFF] via-[#F8FAFC] to-[#FFFFFF] dark:from-[#07152B] dark:via-[#091D3A] dark:to-[#07152B] text-[#0B1E36] dark:text-white py-14 sm:py-20 lg:py-24 transition-colors duration-300">
        <div className="relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-12 items-center gap-10 lg:gap-8 xl:gap-14">
            {/* Kolom Kiri: Ilustrasi Orang di Meja Kerja & Sticky Notes */}
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

            {/* Kolom Kanan: Teks & 6 Poin Masalah */}
            <div className="lg:col-span-6 text-left z-10 min-w-0 w-full">
              <div className="inline-flex items-center gap-2 rounded-full border border-rose-200/90 dark:border-rose-800/80 bg-rose-50/80 dark:bg-rose-950/60 px-4 py-1.5 text-xs font-bold text-rose-700 dark:text-rose-300 shadow-xs mb-3.5">
                <span className="size-1.5 rounded-full bg-rose-500 animate-pulse" />
                Kendala Nyata Pengurus
              </div>

              <h2 className="text-3xl sm:text-4xl md:text-[44px] lg:text-[48px] font-black tracking-tight text-[#0B1E36] dark:text-white leading-[1.18] sm:leading-[1.12]">
                Masih ribet kelola<br />
                website sendiri?
              </h2>

              <p className="mt-4 sm:mt-5 text-sm sm:text-base md:text-lg text-[#64748B] dark:text-slate-300 leading-relaxed max-w-xl">
                Banyak pengurus organisasi kesulitan membuat dan mengelola website karena keterbatasan waktu, pergantian kepengurusan, dan ketiadaan anggota yang menguasai koding.
              </p>

              {/* Grid 6 Pain Points dengan animasi staggered dari bawah ke atas */}
              <PainPointsList />
            </div>
          </div>
        </div>
      </section>

      {/* =========================================================================
          3. FITUR & KEUNGGULAN UTAMA (TARGET: #fitur)
         ========================================================================= */}
      <FeaturesSection />

      {/* =========================================================================
          4. TEMPLATE UNGGULAN PILIHAN
         ========================================================================= */}
      <FeaturedTemplates templates={featuredTemplates} />

      {/* =========================================================================
          5. TESTIMONI PENGURUS ORGANISASI (TARGET: #testimoni)
         ========================================================================= */}
      <TestimonialsSection />

      {/* =========================================================================
          7. BERITA TERBARU DARI KAMI
         ========================================================================= */}
      <LatestUpdates updates={latestUpdates} />

      {/* =========================================================================
          8. CTA BANNER PENUTUP (TARGET: #akses-awal)
         ========================================================================= */}
      <CtaSection />
    </div>
  );
}
