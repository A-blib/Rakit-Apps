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
  if (typeof window === "undefined") return "light";
  const stored = localStorage.getItem("orgspace_theme");
  if (stored) return stored;
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

function getServerSnapshot(): string {
  return "light";
}

export function ThemeToggle({ className = "" }: { className?: string }) {
  const theme = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const isDark = theme === "dark";

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
      // Ignore strict private browsing exceptions
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
      className={`relative inline-flex size-9 sm:size-10 items-center justify-center rounded-full border border-slate-200/90 bg-white/80 dark:border-slate-700/80 dark:bg-slate-800/80 text-foreground transition-all duration-200 hover:scale-105 active:scale-95 shadow-xs hover:border-[#0099FF] dark:hover:border-cyan cursor-pointer select-none backdrop-blur-xs ${className}`}
      aria-label={`Beralih ke mode ${isDark ? "terang" : "gelap"}`}
      title={`Beralih ke mode ${isDark ? "terang" : "gelap"}`}
    >
      {isDark ? (
        // Ikon Matahari bercahaya untuk Dark Mode
        <svg
          className="size-4.5 text-amber-400 transition-transform duration-300 rotate-0 hover:rotate-45"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2.2"
            d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
          />
        </svg>
      ) : (
        // Ikon Bulan sabit untuk Light Mode
        <svg
          className="size-4.5 text-[#0B1E36] transition-transform duration-300 -rotate-12 hover:rotate-0"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2.2"
            d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
          />
        </svg>
      )}
    </button>
  );
}
