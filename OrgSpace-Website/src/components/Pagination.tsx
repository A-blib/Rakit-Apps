import Link from "next/link";

/** Navigasi halaman untuk PageResponse. `hrefFor(page)` membuat URL dengan filter yang sama. */
export function Pagination({ page, totalPages, hrefFor }: { page: number; totalPages: number; hrefFor: (page: number) => string }) {
  if (totalPages <= 1) {
    return null;
  }
  return (
    <nav className="mt-4 flex items-center gap-3 text-sm" aria-label="Halaman">
      {page > 0 ? <Link className="btn-outline" href={hrefFor(page - 1)}>Sebelumnya</Link> : null}
      <span className="text-muted">Halaman {page + 1} dari {totalPages}</span>
      {page + 1 < totalPages ? <Link className="btn-outline" href={hrefFor(page + 1)}>Berikutnya</Link> : null}
    </nav>
  );
}
