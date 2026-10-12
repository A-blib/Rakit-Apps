"use client";

/** Data dashboard gagal dimuat (mis. backend mati). */
export default function DashboardError({ error, reset }: { error: Error; reset: () => void }) {
  return (
    <div className="m-8 max-w-xl space-y-3 rounded-card border border-error/40 bg-error/10 p-5">
      <p className="font-semibold text-error">Data dashboard gagal dimuat.</p>
      <p className="text-sm">{error.message}</p>
      <button className="btn-outline" onClick={reset}>Coba lagi</button>
    </div>
  );
}
