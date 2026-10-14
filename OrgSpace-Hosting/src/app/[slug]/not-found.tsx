import Link from "next/link";
import { Globe, ArrowLeft, AlertCircle } from "lucide-react";

export default function NotFound() {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-6 text-center font-sans">
      <div className="w-full max-w-md p-8 bg-white rounded-3xl border border-slate-200 shadow-xl shadow-slate-200/50 space-y-6">
        <div className="w-16 h-16 rounded-2xl bg-amber-50 border border-amber-200 text-amber-600 flex items-center justify-center mx-auto">
          <Globe className="w-8 h-8" />
        </div>

        <div className="space-y-2">
          <span className="text-xs font-bold uppercase tracking-wider text-amber-600">
            404 • Tidak Ditemukan
          </span>
          <h1 className="text-2xl font-black text-slate-900">
            Website Belum Tayang
          </h1>
          <p className="text-sm text-slate-600 leading-relaxed">
            Alamat website yang Anda tuju belum diterbitkan atau sedang dinonaktifkan oleh pemiliknya.
          </p>
        </div>

        <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 text-left text-xs text-slate-500 space-y-1.5">
          <div className="font-semibold text-slate-700 flex items-center gap-1.5">
            <AlertCircle className="w-3.5 h-3.5 text-blue-600" />
            Tips Pengguna OrgSpace:
          </div>
          <div>• Pastikan Anda sudah menekan tombol <strong>Publish</strong> di aplikasi.</div>
          <div>• Periksa kembali ejaan subdomain atau tautan yang Anda buka.</div>
        </div>

        <div className="pt-2 flex flex-col gap-3">
          <Link
            href="/"
            className="w-full h-11 inline-flex items-center justify-center gap-2 rounded-xl bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 transition shadow-sm"
          >
            <ArrowLeft className="w-4 h-4" />
            Ke Beranda Gateway
          </Link>
        </div>
      </div>

      <footer className="mt-8 text-xs text-slate-400">
        OrgSpace Hosting Gateway • Sistem Penayangan Web Pengguna
      </footer>
    </div>
  );
}
