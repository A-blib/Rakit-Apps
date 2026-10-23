"use client";

import { useState, type FormEvent } from "react";

/**
 * Komponen Formulir Akses Awal / Demo untuk aplikasi Rakit.
 * Menggunakan efek frosted glass, border focus ring menyala (Cyan/Orange),
 * dan feedback interaktif saat registrasi berhasil.
 */
export function LeadGenerationForm() {
  const [orgName, setOrgName] = useState("");
  const [email, setEmail] = useState("");
  const [submitted, setSubmitted] = useState(false);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!email) return;
    setSubmitted(true);
  }

  return (
    <div className="card-glass relative mx-auto max-w-2xl overflow-hidden p-5 sm:p-8 md:p-12">
      {/* Ambient background glow properly clipped without negative offsets */}
      <div aria-hidden="true" className="pointer-events-none absolute inset-0 overflow-hidden rounded-card select-none">
        <div className="absolute top-0 right-0 h-48 w-48 sm:h-64 sm:w-64 rounded-full bg-rakit-cyan/15 blur-2xl sm:blur-3xl" />
        <div className="absolute bottom-0 left-0 h-48 w-48 sm:h-64 sm:w-64 rounded-full bg-rakit-orange/15 blur-2xl sm:blur-3xl" />
      </div>

      {submitted ? (
        <div className="relative z-1 py-8 text-center">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full border border-rakit-cyan/40 bg-rakit-cyan/10 text-rakit-cyan shadow-[0_0_30px_rgba(0,242,254,0.3)]">
            <svg
              className="h-8 w-8"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              strokeWidth="2"
            >
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h3 className="text-2xl font-bold text-white">Selamat Bergabung! 🎉</h3>
          <p className="mt-3 text-muted">
            Undangan beta eksklusif untuk <strong className="text-white">{orgName || "organisasimu"}</strong> akan segera kami kirimkan ke <strong className="text-rakit-cyan">{email}</strong>.
          </p>
          <button
            type="button"
            onClick={() => {
              setSubmitted(false);
              setOrgName("");
              setEmail("");
            }}
            className="btn-glass-outline mt-6 text-xs"
          >
            Daftarkan Organisasi Lain
          </button>
        </div>
      ) : (
        <div className="relative z-1">
          <div className="text-center">
            <span className="inline-flex items-center gap-1.5 rounded-full border border-rakit-orange/40 bg-rakit-orange/10 px-3.5 py-1 text-xs font-bold tracking-wide text-rakit-orange-glow">
              <span>🚀</span> PROGRAM BETA EKSKLUSIF
            </span>
            <h2 className="mt-4 text-2xl sm:text-3xl md:text-4xl font-extrabold tracking-tight text-white">
              Jadilah yang Pertama Mencoba Rakit!
            </h2>
            <p className="mt-3 text-sm text-muted md:text-base">
              Tinggalkan email organisasimu untuk mendapatkan undangan beta testing eksklusif, reservasi domain unik, dan akses template premium gratis saat kami rilis.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="mt-8 space-y-5">
            <div>
              <label htmlFor="org-name" className="mb-2 block text-xs font-semibold tracking-wider text-muted uppercase">
                Nama Organisasi / Komunitas
              </label>
              <input
                id="org-name"
                type="text"
                required
                value={orgName}
                onChange={(e) => setOrgName(e.target.value)}
                placeholder="Contoh: BEM Universitas Indonesia / OSIS SMA 1"
                className="input-glass"
              />
            </div>

            <div>
              <label htmlFor="org-email" className="mb-2 block text-xs font-semibold tracking-wider text-muted uppercase">
                Alamat Email Resmi Pengurus
              </label>
              <input
                id="org-email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="pengurus@organisasimu.id"
                className="input-glass"
              />
            </div>

            <button type="submit" className="btn-orange-glow w-full py-3.5 sm:py-4 text-sm sm:text-base">
              Daftar Akses Awal Sekarang 🚀
            </button>

            <p className="text-center text-xs text-muted">
              🔒 Kami menjaga privasi organisasimu. Bebas spam, hanya kabar penting seputar peluncuran.
            </p>
          </form>
        </div>
      )}
    </div>
  );
}
