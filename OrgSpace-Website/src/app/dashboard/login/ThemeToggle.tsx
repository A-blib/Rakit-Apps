"use client";

/**
 * Tombol pengalih tema Dark/Light mode di halaman login (pojok kiri bawah).
 * Menggunakan pendekatan CSS-driven render untuk menjamin 100% bebas error hidrasi Next.js.
 */
export function ThemeToggle() {
  const toggleTheme = () => {
    const isDark =
      document.documentElement.classList.contains("dark") ||
      (!document.documentElement.classList.contains("light") &&
        window.matchMedia("(prefers-color-scheme: dark)").matches);

    const nextTheme = isDark ? "light" : "dark";

    try {
      localStorage.setItem("orgspace_theme", nextTheme);
    } catch {}

    if (nextTheme === "dark") {
      document.documentElement.classList.add("dark");
      document.documentElement.classList.remove("light");
    } else {
      document.documentElement.classList.add("light");
      document.documentElement.classList.remove("dark");
    }
    window.dispatchEvent(new Event("storage"));
  };

  return (
    <button
      type="button"
      onClick={toggleTheme}
      className="group inline-flex items-center gap-2 rounded-full border border-slate-700/60 bg-[#091b30]/85 px-3.5 py-1.5 text-xs font-medium text-slate-300 backdrop-blur-md shadow-lg transition-colors hover:border-cyan/50 hover:text-white cursor-pointer select-none"
      aria-label="Ganti tema Dark/Light Mode"
    >
      <span className="flex items-center gap-1.5 text-cyan">
        {/* Ikon Bulan (mode terang) */}
        <svg className="size-3.5 theme-icon-light" viewBox="0 0 20 20" fill="currentColor">
          <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z" />
        </svg>
        {/* Ikon Matahari (mode gelap) */}
        <svg className="size-3.5 text-amber-400 theme-icon-dark" viewBox="0 0 20 20" fill="currentColor">
          <path
            fillRule="evenodd"
            d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 0zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z"
            clipRule="evenodd"
          />
        </svg>
      </span>
      <span>Dark/Light Mode</span>
    </button>
  );
}
