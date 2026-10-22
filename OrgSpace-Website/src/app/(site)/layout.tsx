import Link from "next/link";
import { SiteHeader } from "@/components/SiteHeader";

/** Kerangka halaman publik (landing & docs): header + footer. */
export default function SiteLayout({ children }: LayoutProps<"/">) {
  return (
    <>
      <SiteHeader />
      <main className="flex-1 -mt-16 w-full overflow-x-hidden">{children}</main>
      <footer className="border-t border-[#F0F4F8] bg-white w-full overflow-hidden">
        <div className="mx-auto flex max-w-6xl flex-wrap justify-between gap-6 px-4 py-8 text-sm text-[#475569]">
          <span>© {new Date().getFullYear()} Rakit • OrgSpace</span>
          <div className="flex items-center gap-6">
            <Link href="/docs" className="hover:text-[#0B1E36]">
              Dokumentasi
            </Link>
            <Link href="/docs/kirim-kritik-dan-saran" className="hover:text-[#0B1E36]">
              Kritik & saran
            </Link>
            <Link href="/dashboard" className="hover:text-[#0B1E36]">
              Dashboard Tim
            </Link>
          </div>
        </div>
      </footer>
    </>
  );
}
