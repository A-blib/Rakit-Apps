"use client";

import { useEffect, useRef, useState, type ReactNode } from "react";

interface PainPointItem {
  title: string;
  bg: string;
  icon: ReactNode;
}

/**
 * Daftar 6 kendala umum dalam mengelola website mandiri.
 * Masing-masing poin memiliki ikon warna tematik dan label tegas.
 */
const PAIN_POINTS: PainPointItem[] = [
  {
    title: "Makan Waktu",
    bg: "bg-[#52B4E9]",
    icon: (
      <svg className="h-6 w-6 text-[#0B1E36]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="9" />
        <polyline points="12 7 12 12 15 14" />
      </svg>
    ),
  },
  {
    title: "Butuh Skill Teknis",
    bg: "bg-[#48C774]",
    icon: (
      <svg className="h-6 w-6 text-[#0B1E36]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="16" rx="2" strokeWidth="2" />
        <polyline points="8 10 6 12 8 14" />
        <polyline points="16 10 18 12 16 14" />
        <line x1="13" y1="9" x2="11" y2="15" />
      </svg>
    ),
  },
  {
    title: "Biaya Tidak Sedikit",
    bg: "bg-[#F4D03F]",
    icon: (
      <svg className="h-6 w-6 text-[#0B1E36]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="2" y="6" width="20" height="12" rx="2" />
        <circle cx="12" cy="12" r="3" />
        <path d="M6 12h.01M18 12h.01" />
      </svg>
    ),
  },
  {
    title: "Sulit Dikustomisasi",
    bg: "bg-[#F39C12]",
    icon: (
      <svg className="h-6 w-6 text-[#0B1E36]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10c.83 0 1.5-.67 1.5-1.5 0-.39-.15-.74-.39-1.01-.23-.26-.38-.61-.38-.99 0-.83.67-1.5 1.5-1.5H16c3.31 0 6-2.69 6-6 0-4.97-4.48-9-10-9z" />
        <circle cx="7.5" cy="11.5" r="1.5" fill="currentColor" />
        <circle cx="10.5" cy="7.5" r="1.5" fill="currentColor" />
        <circle cx="14.5" cy="7.5" r="1.5" fill="currentColor" />
        <circle cx="17.5" cy="11.5" r="1.5" fill="currentColor" />
      </svg>
    ),
  },
  {
    title: "Risiko Keamanan",
    bg: "bg-[#EC7063]",
    icon: (
      <svg className="h-6 w-6 text-[#0B1E36]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        <rect x="9.5" y="10.5" width="5" height="4.5" rx="1" />
        <path d="M10.5 10.5V9a1.5 1.5 0 0 1 3 0v1.5" />
      </svg>
    ),
  },
  {
    title: "Tidak Responsif",
    bg: "bg-[#A569BD]",
    icon: (
      <svg className="h-6 w-6 text-[#0B1E36]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="4" y="4" width="16" height="11" rx="2" />
        <rect x="13" y="9" width="7" height="10" rx="1.5" />
        <line x1="8" y1="18" x2="12" y2="18" />
        <circle cx="16.5" cy="16.5" r="0.75" fill="currentColor" />
      </svg>
    ),
  },
];

/**
 * Komponen PainPointsList untuk Section 2.
 * Menampilkan 6 kendala dengan efek animasi sekuensial (staggered) dari bawah ke atas
 * yang terpicu saat bagian ini masuk ke dalam viewport layar pengguna.
 */
export function PainPointsList() {
  const containerRef = useRef<HTMLDivElement>(null);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    // Jika browser tidak mendukung IntersectionObserver, tampilkan konten pada frame berikutnya
    if (!("IntersectionObserver" in window)) {
      requestAnimationFrame(() => setIsVisible(true));
      return;
    }

    const currentEl = containerRef.current;
    if (!currentEl) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        // Picu animasi satu kali saat elemen mulai terlihat di layar
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.disconnect();
        }
      },
      {
        threshold: 0.15,
        rootMargin: "0px 0px -40px 0px",
      }
    );

    observer.observe(currentEl);

    return () => {
      observer.disconnect();
    };
  }, []);

  return (
    <div
      ref={containerRef}
      className="mt-8 sm:mt-10 grid grid-cols-1 sm:grid-cols-2 gap-y-6 sm:gap-y-7 gap-x-6 lg:gap-x-8"
    >
      {PAIN_POINTS.map((point, index) => (
        <div
          key={point.title}
          style={{
            // Jeda bertahap lebih lambat (300ms per poin) agar muncul satu persatu secara perlahan
            transitionDelay: isVisible ? `${index * 300}ms` : "0ms",
          }}
          className={`flex items-center gap-3.5 group cursor-default transition-all duration-1000 ease-[cubic-bezier(0.16,1,0.3,1)] motion-reduce:transform-none motion-reduce:opacity-100 motion-reduce:transition-none ${
            isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-10"
          }`}
        >
          {/* Ikon Bulat Berwarna */}
          <div
            className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-full ${point.bg} shadow-xs group-hover:scale-105 transition-all duration-200`}
          >
            {point.icon}
          </div>

          {/* Teks Poin */}
          <span className="text-base sm:text-lg font-bold text-[#0B1E36] group-hover:text-[#0099FF] transition-colors">
            {point.title}
          </span>
        </div>
      ))}
    </div>
  );
}
