"use client";

export default function DocsError({ reset }: { error: Error; reset: () => void }) {
  return (
    <div className="space-y-3">
      <p className="font-semibold">Artikel gagal dimuat. Coba lagi beberapa saat lagi.</p>
      <button className="btn-outline" onClick={reset}>Coba lagi</button>
    </div>
  );
}
