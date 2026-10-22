"use client";

import { useEffect, useState } from "react";
import Image from "next/image";
import Link from "next/link";

/**
 * Header situs publik dengan efek Sticky & Glassmorphic Blur.
 * - Saat di posisi paling atas: latar belakang transparan menyatu dengan hero.
 * - Saat digulir (scroll): menempel di atas dengan latar blur semi-transparan (glassmorphism) dan border halus.
 */
export function SiteHeader() {
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    function handleScroll() {
      if (window.scrollY > 20) {
        setIsScrolled(true);
      } else {
        setIsScrolled(false);
      }
    }

    // Set status awal jika dimuat dalam keadaan sudah di-scroll
    handleScroll();

    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <header
      className={`sticky top-0 z-40 w-full transition-all duration-300 ${
        isScrolled
          ? "bg-white/85 backdrop-blur-md shadow-xs border-b border-[#E2E8F0]/80"
          : "bg-transparent border-b border-transparent"
      }`}
    >
      <nav className="mx-auto flex h-16 w-full max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Logo Hexagon 3D Emblem OrgSpace */}
        <Link href="/" className="flex items-center gap-2 group shrink-0">
          <div className="relative flex h-8 w-8 sm:h-10 sm:w-10 items-center justify-center transition-transform duration-300 group-hover:scale-110 shrink-0">
            <Image
              src="/images/orgspace-emblem.png"
              alt="Logo OrgSpace"
              width={40}
              height={40}
              className="h-7 w-auto sm:h-9 object-contain select-none"
              priority
            />
          </div>
          <span className="text-xl sm:text-2xl font-black tracking-tight text-[#0B1E36] transition-colors group-hover:text-[#0099FF]">
            OrgSpace
          </span>
        </Link>

        {/* Menu Navigasi Tengah/Kanan Sesuai Gambar Referensi */}
        <div className="hidden md:flex items-center gap-8 lg:gap-12 text-sm lg:text-base font-semibold">
          <Link
            href="/#fitur"
            className="text-[#0B1E36] hover:text-[#0099FF] transition-colors relative py-1 after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-[#0099FF] after:scale-x-0 hover:after:scale-x-100 after:transition-transform after:duration-200"
          >
            Fitur
          </Link>
          <Link
            href="/#harga"
            className="text-[#0B1E36] hover:text-[#0099FF] transition-colors relative py-1 after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-[#0099FF] after:scale-x-0 hover:after:scale-x-100 after:transition-transform after:duration-200"
          >
            Harga
          </Link>
          <Link
            href="/#testimoni"
            className="text-[#0B1E36] hover:text-[#0099FF] transition-colors relative py-1 after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-[#0099FF] after:scale-x-0 hover:after:scale-x-100 after:transition-transform after:duration-200"
          >
            Testimoni
          </Link>
        </div>

        {/* Tombol CTA Kanan: Mulai Gratis dengan Hover Elevation */}
        <div className="flex items-center gap-2 sm:gap-4 shrink-0">
          <Link
            href="#akses-awal"
            className="inline-flex h-9 sm:h-11 items-center justify-center rounded-full bg-[#0B1E36] px-4 sm:px-6 text-xs sm:text-sm font-bold text-white shadow-sm hover:bg-[#12263F] hover:shadow-md hover:scale-105 active:scale-95 transition-all duration-200 whitespace-nowrap"
          >
            Mulai Gratis
          </Link>
        </div>
      </nav>
    </header>
  );
}
