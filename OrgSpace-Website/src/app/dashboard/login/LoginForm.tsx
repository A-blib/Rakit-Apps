"use client";

import { useActionState, useState } from "react";
import { login, type FormState } from "../actions";

export function LoginForm() {
  const [state, action, pending] = useActionState<FormState, FormData>(login, {});
  const [showPassword, setShowPassword] = useState(false);

  return (
    <form action={action} className="space-y-4">
      {/* Email Field */}
      <div>
        <label
          className="mb-2 block text-xs font-bold uppercase tracking-wider text-slate-300"
          htmlFor="email"
        >
          EMAIL ADMIN
        </label>
        <div className="relative">
          <input
            className="input-cyber pr-10"
            id="email"
            name="email"
            type="email"
            autoComplete="username"
            placeholder="nama@organisasi.id"
            required
            autoFocus
          />
          <span className="pointer-events-none absolute inset-y-0 right-3.5 flex items-center text-slate-400">
            <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path d="M3 4a2 2 0 00-2 2v1.161l8.441 4.221a1.25 1.25 0 001.118 0L19 7.162V6a2 2 0 00-2-2H3z" />
              <path d="M19 8.839l-7.77 3.885a2.75 2.75 0 01-2.46 0L1 8.839V14a2 2 0 002 2h14a2 2 0 002-2V8.839z" />
            </svg>
          </span>
        </div>
      </div>

      {/* Password Field */}
      <div>
        <label
          className="mb-2 block text-xs font-bold uppercase tracking-wider text-slate-300"
          htmlFor="password"
        >
          PASSWORD
        </label>
        <div className="relative">
          <input
            className="input-cyber pr-10"
            id="password"
            name="password"
            type={showPassword ? "text" : "password"}
            autoComplete="current-password"
            placeholder="••••••••"
            required
          />
          <button
            type="button"
            onClick={() => setShowPassword((prev) => !prev)}
            aria-label={showPassword ? "Sembunyikan password" : "Tampilkan password"}
            className="absolute inset-y-0 right-1 flex items-center px-2.5 text-slate-400 hover:text-cyan transition-colors cursor-pointer"
          >
            {showPassword ? (
              // Eye Slash Icon
              <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path
                  fillRule="evenodd"
                  d="M3.28 2.22a.75.75 0 00-1.06 1.06l14.5 14.5a.75.75 0 101.06-1.06l-1.745-1.745a10.029 10.029 0 003.3-4.38 1.651 1.651 0 000-1.185A10.004 10.004 0 009.999 3a9.956 9.956 0 00-4.744 1.194L3.28 2.22zM7.752 6.69l1.692 1.69a2.5 2.5 0 013.178 3.179l1.63 1.629a4.5 4.5 0 00-6.5-6.5zm-3.23 2.193A11.472 11.472 0 001.664 10.59a1.652 1.652 0 000 1.185A10.004 10.004 0 0010 17a9.958 9.958 0 004.288-.971l-1.536-1.536a8.46 8.46 0 01-2.752.507 8.5 8.5 0 01-7.072-4.41 8.51 8.51 0 011.83-2.348l-1.236-1.237z"
                  clipRule="evenodd"
                />
              </svg>
            ) : (
              // Eye Icon
              <svg className="size-4.5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path d="M10 12.5a2.5 2.5 0 100-5 2.5 2.5 0 000 5z" />
                <path
                  fillRule="evenodd"
                  d="M.664 10.59a1.651 1.651 0 010-1.186A10.004 10.004 0 0110 3c4.257 0 7.893 2.66 9.336 6.41.147.381.146.804 0 1.186A10.004 10.004 0 0110 17c-4.257 0-7.893-2.66-9.336-6.41zM14 10a4 4 0 11-8 0 4 4 0 018 0z"
                  clipRule="evenodd"
                />
              </svg>
            )}
          </button>
        </div>
      </div>

      {/* Error Message */}
      {state.error && (
        <div
          role="alert"
          className="flex items-start gap-2.5 rounded-xl border border-rose-500/40 bg-rose-950/60 p-3 text-xs text-rose-200 backdrop-blur-md"
        >
          <svg className="mt-0.5 size-4 shrink-0 text-rose-400" viewBox="0 0 20 20" fill="currentColor">
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z"
              clipRule="evenodd"
            />
          </svg>
          <span className="leading-relaxed">{state.error}</span>
        </div>
      )}

      {/* Submit Button */}
      <button
        type="submit"
        className="btn-cyber mt-3"
        disabled={pending}
      >
        {pending ? (
          <>
            <svg className="size-4 animate-spin text-[#041021]" viewBox="0 0 24 24" fill="none">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
            </svg>
            <span>Memverifikasi...</span>
          </>
        ) : (
          <span>Masuk ke Dashboard</span>
        )}
      </button>

      {/* Development Account Hint */}
      <div className="pt-4 text-center text-xs text-slate-400 leading-relaxed">
        <span>Akun default:</span>{" "}
        <span className="text-slate-200 font-medium">admin@admin.com</span>
        {" / "}
        <span className="text-slate-200 font-medium">admin123</span>
      </div>
    </form>
  );
}
