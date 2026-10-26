"use client";

import { useEffect, useSyncExternalStore } from "react";

function subscribe(callback: () => void) {
  window.addEventListener("storage", callback);
  window.addEventListener("orgspace_theme_change", callback);
  return () => {
    window.removeEventListener("storage", callback);
    window.removeEventListener("orgspace_theme_change", callback);
  };
}

function getSnapshot(): string {
  if (typeof window === "undefined") return "dark";
  return localStorage.getItem("orgspace_theme") ?? "dark";
}

function getServerSnapshot(): string {
  return "dark";
}

export function ThemeToggle() {
  const theme = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const isDark = theme !== "light";

  // Sinkronisasi kelas tema ke tag <html> saat inisialisasi dan saat berganti mode
  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add("dark");
      document.documentElement.classList.remove("light");
      document.documentElement.setAttribute("data-theme", "dark");
    } else {
      document.documentElement.classList.add("light");
      document.documentElement.classList.remove("dark");
      document.documentElement.setAttribute("data-theme", "light");
    }
  }, [isDark]);

  const toggleTheme = () => {
    const nextTheme = isDark ? "light" : "dark";
    try {
      localStorage.setItem("orgspace_theme", nextTheme);
    } catch {
      // LocalStorage fallback jika dalam mode strict private
    }

    if (nextTheme === "dark") {
      document.documentElement.classList.add("dark");
      document.documentElement.classList.remove("light");
      document.documentElement.setAttribute("data-theme", "dark");
    } else {
      document.documentElement.classList.add("light");
      document.documentElement.classList.remove("dark");
      document.documentElement.setAttribute("data-theme", "light");
    }

    window.dispatchEvent(new Event("orgspace_theme_change"));
    window.dispatchEvent(new Event("storage"));
  };

  return (
    <button
      type="button"
      onClick={toggleTheme}
      className={`absolute bottom-6 left-6 z-20 flex items-center gap-2 px-3 py-1.5 rounded-full backdrop-blur-sm text-xs font-medium transition-colors cursor-pointer select-none ${
        isDark
          ? "bg-slate-800/80 border border-slate-700/50 text-slate-300 hover:text-white hover:bg-slate-700 shadow-lg"
          : "bg-white/80 border border-slate-200 text-slate-600 hover:text-slate-900 hover:bg-slate-50 shadow-sm"
      }`}
      aria-label="Ganti Tema Dark/Light Mode"
      title={`Beralih ke mode ${isDark ? "terang" : "gelap"}`}
    >
      {isDark ? (
        // Ikon Bulan (Mode Gelap)
        <svg className="w-4 h-4 text-cyan-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
          />
        </svg>
      ) : (
        // Ikon Matahari (Mode Terang)
        <svg className="w-4 h-4 text-cyan-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
          />
        </svg>
      )}
      <span>Dark/Light Mode</span>
    </button>
  );
}
