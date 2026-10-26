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

export function DashboardThemeToggle() {
  const theme = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const isDark = theme !== "light";

  // Sinkronisasi kelas tema ke tag <html> saat inisialisasi dan saat berpindah mode
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
      // Abaikan jika mode strict private
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
      className="inline-flex items-center gap-1.5 rounded-field border border-border bg-surface px-2.5 py-1 text-xs font-medium text-foreground transition-all hover:bg-surface-subtle hover:border-border-strong cursor-pointer select-none shadow-xs"
      aria-label="Ganti Tema Dashboard"
      title={`Beralih ke mode ${isDark ? "terang" : "gelap"}`}
    >
      {isDark ? (
        // Ikon Matahari untuk beralih ke Mode Terang
        <svg className="size-3.5 text-amber-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
          />
        </svg>
      ) : (
        // Ikon Bulan untuk beralih ke Mode Gelap
        <svg className="size-3.5 text-cyan-600 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
          />
        </svg>
      )}
      <span className="hidden sm:inline text-[11px]">
        {isDark ? "Mode Terang" : "Mode Gelap"}
      </span>
    </button>
  );
}
