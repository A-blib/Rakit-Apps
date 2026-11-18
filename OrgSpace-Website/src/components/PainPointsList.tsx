"use client";

import { useEffect, useRef, useState, type ReactNode } from "react";

interface PainPointItem {
  title: string;
  desc: string;
  iconBg: string;
  iconColor: string;
  hoverBorder: string;
  icon: ReactNode;
}

/**
 * Daftar 6 kendala nyata yang dihadapi pengurus organisasi saat membuat website mandiri.
 * Didesain dalam bentuk kartu interaktif modern dengan ikon tematik dan penjelasan singkat.
 */
const PAIN_POINTS: PainPointItem[] = [
  {
    title: "Makan Waktu Berhari-hari",
    desc: "Habis waktu berharga hanya untuk setting server, database, dan layout dari nol.",
    iconBg: "bg-blue-50 dark:bg-blue-950/40 border-blue-200/80 dark:border-blue-800/60",
    iconColor: "text-[#0084FF] dark:text-[#00C9F2]",
    hoverBorder: "hover:border-[#0084FF]/50 dark:hover:border-[#00C9F2]/50 hover:shadow-blue-500/10",
    icon: (
      <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="9" />
        <polyline points="12 7 12 12 15 14" />
      </svg>
    ),
  },
  {
    title: "Butuh Skill Koding Rumit",
    desc: "Harus paham HTML, CSS, JavaScript, hingga urusan konfigurasi hosting.",
    iconBg: "bg-indigo-50 dark:bg-indigo-950/40 border-indigo-200/80 dark:border-indigo-800/60",
    iconColor: "text-indigo-600 dark:text-indigo-400",
    hoverBorder: "hover:border-indigo-400/50 dark:hover:border-indigo-400/50 hover:shadow-indigo-500/10",
    icon: (
      <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="16" rx="2" strokeWidth="2" />
        <polyline points="8 10 6 12 8 14" />
        <polyline points="16 10 18 12 16 14" />
        <line x1="13" y1="9" x2="11" y2="15" />
      </svg>
    ),
  },
  {
    title: "Biaya Server Mahal",
    desc: "Beban sewa hosting tahunan, domain, dan biaya jasa pembuatan yang menguras kas.",
    iconBg: "bg-amber-50 dark:bg-amber-950/40 border-amber-200/80 dark:border-amber-800/60",
    iconColor: "text-amber-600 dark:text-amber-400",
    hoverBorder: "hover:border-amber-400/50 dark:hover:border-amber-400/50 hover:shadow-amber-500/10",
    icon: (
      <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="2" y="6" width="20" height="12" rx="2" />
        <circle cx="12" cy="12" r="3" />
        <path d="M6 12h.01M18 12h.01" />
      </svg>
    ),
  },
  {
    title: "Sulit Dikustomisasi",
    desc: "Format template kaku yang susah disesuaikan dengan struktur dan proker organisasi.",
    iconBg: "bg-orange-50 dark:bg-orange-950/40 border-orange-200/80 dark:border-orange-800/60",
    iconColor: "text-orange-600 dark:text-orange-400",
    hoverBorder: "hover:border-orange-400/50 dark:hover:border-orange-400/50 hover:shadow-orange-500/10",
    icon: (
      <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10c.83 0 1.5-.67 1.5-1.5 0-.39-.15-.74-.39-1.01-.23-.26-.38-.61-.38-.99 0-.83.67-1.5 1.5-1.5H16c3.31 0 6-2.69 6-6 0-4.97-4.48-9-10-9z" />
        <circle cx="7.5" cy="11.5" r="1.5" fill="currentColor" />
        <circle cx="10.5" cy="7.5" r="1.5" fill="currentColor" />
        <circle cx="14.5" cy="7.5" r="1.5" fill="currentColor" />
      </svg>
    ),
  },
  {
    title: "Risiko Keamanan & SSL",
    desc: "Website rawan terkena serangan spam, inject iklan, atau sertifikat SSL yang mati.",
    iconBg: "bg-rose-50 dark:bg-rose-950/40 border-rose-200/80 dark:border-rose-800/60",
    iconColor: "text-rose-600 dark:text-rose-400",
    hoverBorder: "hover:border-rose-400/50 dark:hover:border-rose-400/50 hover:shadow-rose-500/10",
    icon: (
      <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
    ),
  },
  {
    title: "Tampilan Berantakan di HP",
    desc: "Tidak responsif dan sulit dibaca saat dibuka oleh anggota lewat smartphone.",
    iconBg: "bg-purple-50 dark:bg-purple-950/40 border-purple-200/80 dark:border-purple-800/60",
    iconColor: "text-purple-600 dark:text-purple-400",
    hoverBorder: "hover:border-purple-400/50 dark:hover:border-purple-400/50 hover:shadow-purple-500/10",
    icon: (
      <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="4" y="2" width="16" height="20" rx="3" />
        <line x1="9" y1="5" x2="15" y2="5" />
        <circle cx="12" cy="18" r="1" />
      </svg>
    ),
  },
];

export function PainPointsList() {
  const containerRef = useRef<HTMLDivElement>(null);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    if (!("IntersectionObserver" in window)) {
      requestAnimationFrame(() => setIsVisible(true));
      return;
    }

    const currentEl = containerRef.current;
    if (!currentEl) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
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
      className="mt-8 grid grid-cols-1 sm:grid-cols-2 gap-3.5 sm:gap-4"
    >
      {PAIN_POINTS.map((point, index) => (
        <div
          key={point.title}
          style={{
            transitionDelay: isVisible ? `${index * 120}ms` : "0ms",
          }}
          className={`group flex items-start gap-3.5 p-3.5 sm:p-4 rounded-2xl border border-slate-200/90 dark:border-[#1E3E6B] bg-white dark:bg-[#0C2E5B] shadow-xs transition-all duration-300 hover:-translate-y-1 hover:shadow-md ${point.hoverBorder} ${
            isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-6"
          }`}
        >
          {/* Ikon Box Bulat / Rounded */}
          <div
            className={`flex h-10 w-10 sm:h-11 sm:w-11 shrink-0 items-center justify-center rounded-xl border ${point.iconBg} ${point.iconColor} shadow-2xs group-hover:scale-105 transition-transform duration-200`}
          >
            {point.icon}
          </div>

          {/* Teks Judul & Deskripsi */}
          <div className="min-w-0">
            <h3 className="text-sm sm:text-base font-bold text-[#0B1E36] dark:text-white group-hover:text-[#0099FF] dark:group-hover:text-[#00C9F2] transition-colors leading-snug">
              {point.title}
            </h3>
            <p className="mt-1 text-xs text-[#64748B] dark:text-slate-300 leading-relaxed">
              {point.desc}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}
