import Link from "next/link";

/** Navigasi halaman untuk PageResponse. `hrefFor(page)` membuat URL dengan filter yang sama. */
export function Pagination({ page, totalPages, hrefFor }: { page: number; totalPages: number; hrefFor: (page: number) => string }) {
  if (totalPages <= 1) {
    return null;
  }
  return (
    <nav className="mt-5 flex items-center justify-between border-t border-border pt-4 text-xs" aria-label="Navigasi halaman">
      <div className="text-muted">
        Menampilkan halaman <strong className="text-foreground font-mono">{page + 1}</strong> dari{" "}
        <strong className="text-foreground font-mono">{totalPages}</strong>
      </div>
      <div className="flex items-center gap-2">
        {page > 0 ? (
          <Link
            className="btn-outline h-8 px-2.5 text-xs inline-flex items-center gap-1"
            href={hrefFor(page - 1)}
          >
            <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
            <span>Sebelumnya</span>
          </Link>
        ) : (
          <button disabled className="btn-outline h-8 px-2.5 text-xs opacity-40 cursor-not-allowed">
            Sebelumnya
          </button>
        )}

        {page + 1 < totalPages ? (
          <Link
            className="btn-outline h-8 px-2.5 text-xs inline-flex items-center gap-1"
            href={hrefFor(page + 1)}
          >
            <span>Berikutnya</span>
            <svg className="size-3" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
            </svg>
          </Link>
        ) : (
          <button disabled className="btn-outline h-8 px-2.5 text-xs opacity-40 cursor-not-allowed">
            Berikutnya
          </button>
        )}
      </div>
    </nav>
  );
}
