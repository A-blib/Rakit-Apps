"use client";

import { useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { LeadGenerationForm } from "@/components/LeadGenerationForm";

interface SectionFourWorkspaceProps {
  androidDownloadUrl?: string | null;
}

/**
 * Section 4: Website Builder CTA Workspace.
 * 1. Background 100% PURE satu gambar fotografi ruang kerja nyata (section4-pure-workspace-v3.png):
 *    - Meja kayu, laptop menghadap ke depan dengan layar bercahaya, cangkir kopi, dan tanaman di sisi KIRI.
 *    - Jendela kaca besar dengan pemandangan skyline perkotaan terang di latar belakang.
 *    - Sisi KANAN berupa area terbuka luas yang terang alami (clean negative space).
 * 2. TANPA GARIS-GARIS: Bebas dari ornamen pita sudut, garis pembatas kaku, atau garis potongan.
 * 3. TANPA GAMBAR/KOTAK TEMPELAN: Teks, tombol aksi, dan keunggulan menyatu langsung secara alami
 *    di atas latar belakang dengan pencahayaan ambient lembut tanpa kartu tebal berpembatas.
 */
export function SectionFourWorkspace({ androidDownloadUrl }: SectionFourWorkspaceProps) {
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <section
      id="akses-awal"
      className="relative w-full overflow-hidden text-[#0B1E36] py-16 sm:py-20 lg:py-28 min-h-[580px] lg:min-h-[660px] flex items-center"
    >
      {/* 1. Pure Full Background Fotografi Workspace Nyata Menyatu */}
      <div aria-hidden="true" className="pointer-events-none absolute inset-0 z-0 select-none overflow-hidden">
        <Image
          src="/images/section4-pure-workspace-v3.png"
          alt="Latar Belakang Ruang Kerja Modern Rakit"
          fill
          className="object-cover object-left lg:object-center"
          priority={false}
        />
        {/* Soft daylight ambient fill di layar kecil untuk keterbacaan teks maksimal */}
        <div className="absolute inset-0 bg-gradient-to-r from-white/40 via-white/70 to-white/90 lg:from-transparent lg:via-transparent lg:to-white/30 pointer-events-none" />
      </div>

      <div className="relative z-10 mx-auto max-w-7xl w-full px-4 sm:px-6 lg:px-8 py-4 sm:py-8">
        <div className="grid items-center gap-10 lg:grid-cols-12 lg:gap-8 xl:gap-12">
          {/* Kolom Kiri: Ruang lega untuk memperlihatkan visual meja & laptop di background secara utuh */}
          <div className="hidden lg:block lg:col-span-6 relative min-h-[440px] pointer-events-none" />

          {/* Kolom Kanan: Tulisan menyatu langsung secara alami (Pure Background, tanpa kotak card tempelan, tanpa garis-garis) */}
          <div className="lg:col-span-6 relative z-10 w-full py-2 sm:py-4">
            {/* Ambient Daylight Backdrop Glow */}
            <div
              aria-hidden="true"
              className="pointer-events-none absolute -inset-6 sm:-inset-10 rounded-full bg-white/75 blur-3xl select-none"
            />
            <div
              aria-hidden="true"
              className="pointer-events-none absolute -top-8 -right-8 h-64 w-64 rounded-full bg-[#E0F2FE]/60 blur-3xl select-none"
            />

            <div className="relative z-10">
              {/* Pill Badge */}
              <div className="inline-flex items-center gap-2 rounded-full border border-[#BAE6FD] bg-white/90 px-4 py-1.5 text-xs sm:text-sm font-bold text-[#0284C7] shadow-xs backdrop-blur-md">
                <span className="text-[#0088FF] text-sm leading-none">✦</span>
                <span>Bergabung Sekarang & Mulai Bangun Website-mu</span>
              </div>

              {/* Headline Utama */}
              <h2 className="mt-5 text-3xl sm:text-4xl md:text-[42px] lg:text-[46px] font-black tracking-tight text-[#0B1E36] leading-[1.14] sm:leading-[1.10]">
                Website Profesional<br />
                <span className="text-[#0088FF]">Bisa Kamu Buat Hari Ini!</span>
              </h2>

              {/* Subtitle / Deskripsi */}
              <p className="mt-4 sm:mt-5 text-sm sm:text-base md:text-lg text-[#334155] leading-relaxed max-w-xl font-medium">
                Jangan tunggu nanti. Mulai langkah pertamamu bersama Rakit dan wujudkan website impianmu dengan lebih mudah, cepat, dan tanpa coding.
              </p>

              {/* Dua Tombol Aksi Sejajar (Dual CTA) */}
              <div className="mt-7 sm:mt-8 flex flex-col sm:flex-row items-stretch sm:items-center gap-3.5 sm:gap-4 w-full">
                {androidDownloadUrl ? (
                  <a
                    href={androidDownloadUrl}
                    className="group relative overflow-hidden inline-flex h-12 sm:h-13 items-center justify-center rounded-full bg-gradient-to-r from-[#FF5E00] via-[#FF7500] to-[#FFA000] px-7 sm:px-8 text-sm sm:text-base font-bold text-white shadow-lg shadow-orange-500/25 hover:shadow-orange-500/40 hover:scale-[1.03] active:scale-95 transition-all duration-300 text-center"
                  >
                    <span className="relative z-10 flex items-center gap-2">
                      <span>Mulai Gratis</span>
                      <span className="transition-transform duration-300 group-hover:translate-x-1 font-bold">→</span>
                    </span>
                    <span
                      aria-hidden="true"
                      className="animate-sheen pointer-events-none absolute inset-0 w-1/2 h-full bg-gradient-to-r from-transparent via-white/30 to-transparent"
                    />
                  </a>
                ) : (
                  <button
                    type="button"
                    onClick={() => setIsModalOpen(true)}
                    className="group relative overflow-hidden inline-flex h-12 sm:h-13 items-center justify-center rounded-full bg-gradient-to-r from-[#FF5E00] via-[#FF7500] to-[#FFA000] px-7 sm:px-8 text-sm sm:text-base font-bold text-white shadow-lg shadow-orange-500/25 hover:shadow-orange-500/40 hover:scale-[1.03] active:scale-95 transition-all duration-300 cursor-pointer text-center"
                  >
                    <span className="relative z-10 flex items-center gap-2">
                      <span>Mulai Gratis</span>
                      <span className="transition-transform duration-300 group-hover:translate-x-1 font-bold">→</span>
                    </span>
                    <span
                      aria-hidden="true"
                      className="animate-sheen pointer-events-none absolute inset-0 w-1/2 h-full bg-gradient-to-r from-transparent via-white/30 to-transparent"
                    />
                  </button>
                )}

                <Link
                  href="/docs"
                  className="inline-flex h-12 sm:h-13 items-center justify-center rounded-full border-2 border-[#0B1E36] bg-white/70 backdrop-blur-xs px-7 sm:px-8 text-sm sm:text-base font-bold text-[#0B1E36] hover:bg-[#0B1E36] hover:text-white hover:scale-[1.02] active:scale-95 transition-all duration-200 text-center"
                >
                  Lihat Demo
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modal Akses Awal / Demo */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="relative w-full max-w-xl">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="absolute -top-3 -right-3 z-30 flex h-9 w-9 items-center justify-center rounded-full bg-white text-[#0B1E36] font-bold shadow-xl hover:bg-[#F1F5F9] active:scale-95 transition-all cursor-pointer"
              aria-label="Tutup modal pendaftaran"
            >
              ✕
            </button>
            <LeadGenerationForm />
          </div>
        </div>
      )}
    </section>
  );
}
