"use client";

import { useEffect, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { ThemeToggle } from "./ThemeToggle";

/**
 * Header situs publik dengan efek Sticky & Glassmorphic Blur,
 * dukungan Dark Mode penuh, dan menu navigasi responsif.
 */
export function SiteHeader() {
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    function handleScroll() {
      if (window.scrollY > 20) {
        setIsScrolled(true);
      } else {
        setIsScrolled(false);
      }
    }

    handleScroll();

    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <header
      className={`sticky top-0 z-40 w-full transition-all duration-300 ${
        isScrolled
          ? "bg-white/85 dark:bg-[#07152B]/90 backdrop-blur-md shadow-xs border-b border-[#E2E8F0]/80 dark:border-[#1E3E6B]/80"
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
          <span className="text-xl sm:text-2xl font-black tracking-tight text-[#0B1E36] dark:text-white transition-colors group-hover:text-[#0099FF] dark:group-hover:text-cyan">
            OrgSpace
          </span>
        </Link>

        {/* Menu Navigasi Tengah/Kanan (Desktop) */}
        <div className="hidden md:flex items-center gap-8 lg:gap-12 text-sm lg:text-base font-semibold">
          <Link
            href="/#fitur"
            className="text-[#0B1E36] dark:text-slate-200 hover:text-[#0099FF] dark:hover:text-cyan transition-colors relative py-1 after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-[#0099FF] dark:after:bg-cyan after:scale-x-0 hover:after:scale-x-100 after:transition-transform after:duration-200"
          >
            Fitur
          </Link>
          <Link
            href="/#template-unggulan"
            className="text-[#0B1E36] dark:text-slate-200 hover:text-[#0099FF] dark:hover:text-cyan transition-colors relative py-1 after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-[#0099FF] dark:after:bg-cyan after:scale-x-0 hover:after:scale-x-100 after:transition-transform after:duration-200"
          >
            Template
          </Link>
          <Link
            href="/#testimoni"
            className="text-[#0B1E36] dark:text-slate-200 hover:text-[#0099FF] dark:hover:text-cyan transition-colors relative py-1 after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-[#0099FF] dark:after:bg-cyan after:scale-x-0 hover:after:scale-x-100 after:transition-transform after:duration-200"
          >
            Testimoni
          </Link>
        </div>

        {/* Tombol CTA Kanan & Dark Mode Toggle */}
        <div className="flex items-center gap-2 sm:gap-3.5 shrink-0">
          {/* Theme Toggle Button */}
          <ThemeToggle />

          <Link
            href="#akses-awal"
            className="inline-flex h-9 sm:h-10 items-center justify-center rounded-full bg-[#0B1E36] dark:bg-cyan dark:text-navy px-4 sm:px-5.5 text-xs sm:text-sm font-bold text-white shadow-sm hover:bg-[#12263F] dark:hover:bg-cyan-light hover:shadow-md hover:scale-105 active:scale-95 transition-all duration-200 whitespace-nowrap"
          >
            Mulai Gratis
          </Link>

          {/* Hamburger Button untuk Smartphone */}
          <button
            type="button"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="flex md:hidden size-9 items-center justify-center rounded-xl border border-slate-200 dark:border-slate-700 bg-white/80 dark:bg-slate-800 text-[#0B1E36] dark:text-white hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
            aria-label="Buka menu navigasi"
          >
            {mobileMenuOpen ? (
              <svg className="size-5" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                  clipRule="evenodd"
                />
              </svg>
            ) : (
              <svg className="size-5" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z"
                  clipRule="evenodd"
                />
              </svg>
            )}
          </button>
        </div>
      </nav>

      {/* Dropdown Menu Navigasi Smartphone */}
      {mobileMenuOpen && (
        <div className="md:hidden border-b border-slate-200/90 dark:border-[#1E3E6B] bg-white/95 dark:bg-[#0C2E5B]/98 backdrop-blur-md px-4 py-4 space-y-2 shadow-lg animate-in slide-in-from-top-2 duration-200">
          <Link
            href="/#fitur"
            onClick={() => setMobileMenuOpen(false)}
            className="block rounded-xl px-4 py-2.5 text-sm font-bold text-[#0B1E36] dark:text-slate-100 hover:bg-slate-50 dark:hover:bg-white/10 hover:text-[#0099FF] dark:hover:text-cyan transition-colors"
          >
            Fitur
          </Link>
          <Link
            href="/#template-unggulan"
            onClick={() => setMobileMenuOpen(false)}
            className="block rounded-xl px-4 py-2.5 text-sm font-bold text-[#0B1E36] dark:text-slate-100 hover:bg-slate-50 dark:hover:bg-white/10 hover:text-[#0099FF] dark:hover:text-cyan transition-colors"
          >
            Template
          </Link>
          <Link
            href="/#testimoni"
            onClick={() => setMobileMenuOpen(false)}
            className="block rounded-xl px-4 py-2.5 text-sm font-bold text-[#0B1E36] dark:text-slate-100 hover:bg-slate-50 dark:hover:bg-white/10 hover:text-[#0099FF] dark:hover:text-cyan transition-colors"
          >
            Testimoni
          </Link>
          <div className="pt-2 border-t border-slate-100 dark:border-[#1E3E6B]">
            <Link
              href="#akses-awal"
              onClick={() => setMobileMenuOpen(false)}
              className="flex w-full items-center justify-center rounded-xl bg-[#0099FF] dark:bg-cyan dark:text-navy py-2.5 text-sm font-bold text-white shadow-xs hover:bg-[#0084FF] transition-colors"
            >
              Mulai Gratis Sekarang
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
