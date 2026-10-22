"use client";

import { useState } from "react";
import type { DailyPoint } from "@/lib/api/types";

export interface MetricSummaryItem {
  label: string;
  current: number;
  previous: number;
}

export interface MetricSeriesItem {
  label: string;
  points: DailyPoint[];
}

interface ExportReportToolbarProps {
  periodLabel: string;
  periodValue: string;
  stats: MetricSummaryItem[];
  timeseries?: MetricSeriesItem[];
  filenamePrefix?: string;
}

/**
 * Toolbar ekspor laporan operasional untuk admin.
 * Menyediakan opsi Ekspor CSV (spreadsheet) dan Cetak/Simpan PDF (format dokumen cetak rapi).
 */
export function ExportReportToolbar({
  periodLabel,
  periodValue,
  stats,
  timeseries = [],
  filenamePrefix = "laporan-operasional-orgspace",
}: ExportReportToolbarProps) {
  const [isExporting, setIsExporting] = useState(false);

  function exportToCSV() {
    setIsExporting(true);
    try {
      const now = new Date();
      const timestamp = now.toLocaleString("id-ID");
      const lines: string[] = [];

      // Header Laporan
      lines.push(`"LAPORAN OPERASIONAL ORGSPACE"`);
      lines.push(`"Periode","${periodLabel} (${periodValue})"`);
      lines.push(`"Waktu Ekspor","${timestamp}"`);
      lines.push(`"Dicetak Oleh","Admin Internal OrgSpace"`);
      lines.push(``);

      // Bagian 1: Ringkasan Metrik KPI
      lines.push(`"--- RINGKASAN METRIK KPI ---"`);
      lines.push(`"Indikator Metrik","Periode Berjalan","Periode Sebelumnya","Perubahan Relatif"`);
      for (const stat of stats) {
        const diff = stat.current - stat.previous;
        let pctStr = "0%";
        if (stat.previous > 0) {
          const pct = Math.round((diff / stat.previous) * 100);
          pctStr = `${pct > 0 ? "+" : ""}${pct}%`;
        } else if (stat.current > 0) {
          pctStr = "+100%";
        }
        lines.push(`"${stat.label}","${stat.current}","${stat.previous}","${pctStr}"`);
      }
      lines.push(``);

      // Bagian 2: Data Rinci Deret Waktu (Timeseries)
      if (timeseries.length > 0) {
        lines.push(`"--- DATA DERET WAKTU HARIAN ---"`);
        lines.push(`"Tanggal","Kategori Metrik","Jumlah"`);
        for (const series of timeseries) {
          for (const pt of series.points) {
            lines.push(`"${pt.date}","${series.label}","${pt.count}"`);
          }
        }
      }

      const csvContent = "\uFEFF" + lines.join("\r\n"); // UTF-8 BOM untuk kompatibilitas Excel
      const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      const dateSlug = now.toISOString().slice(0, 10);
      link.setAttribute("href", url);
      link.setAttribute("download", `${filenamePrefix}-${periodValue}-${dateSlug}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } finally {
      setIsExporting(false);
    }
  }

  function printReport() {
    window.print();
  }

  return (
    <div className="flex items-center gap-2 print:hidden">
      {/* Tombol Ekspor CSV */}
      <button
        type="button"
        onClick={exportToCSV}
        disabled={isExporting}
        className="btn-outline h-9 px-3 text-xs gap-1.5 cursor-pointer hover:border-primary hover:text-primary transition-colors"
        title="Unduh seluruh data metrik dan deret waktu ke dalam file CSV (Excel)"
      >
        <svg className="size-3.5 text-muted" viewBox="0 0 20 20" fill="currentColor">
          <path
            fillRule="evenodd"
            d="M4.5 2A1.5 1.5 0 003 3.5v13A1.5 1.5 0 004.5 18h11a1.5 1.5 0 001.5-1.5V7.621a1.5 1.5 0 00-.44-1.06l-4.12-4.122A1.5 1.5 0 0011.378 2H4.5zm4.75 6.75a.75.75 0 011.5 0v3.69l1.22-1.22a.75.75 0 111.06 1.06l-2.5 2.5a.75.75 0 01-1.06 0l-2.5-2.5a.75.75 0 111.06-1.06l1.22 1.22V8.75z"
            clipRule="evenodd"
          />
        </svg>
        <span>Ekspor CSV</span>
      </button>

      {/* Tombol Cetak / PDF */}
      <button
        type="button"
        onClick={printReport}
        className="btn-outline h-9 px-3 text-xs gap-1.5 cursor-pointer hover:border-primary hover:text-primary transition-colors"
        title="Buka pratinjau cetak browser untuk menyimpan laporan sebagai dokumen PDF"
      >
        <svg className="size-3.5 text-muted" viewBox="0 0 20 20" fill="currentColor">
          <path
            fillRule="evenodd"
            d="M5 2.75C5 1.784 5.784 1 6.75 1h6.5c.966 0 1.75.784 1.75 1.75v3.5A1.75 1.75 0 0116.75 8H17a1 1 0 011 1v6a1 1 0 01-1 1h-1.25v1.25a1.75 1.75 0 01-1.75 1.75H7A1.75 1.75 0 015.25 17.25V16H4a1 1 0 01-1-1V9a1 1 0 011-1h.25A1.75 1.75 0 016.75 6.25v-3.5zm1.75.25a.25.25 0 00-.25.25v3.5c0 .138.112.25.25.25h6.5a.25.25 0 00.25-.25v-3.5a.25.25 0 00-.25-.25h-6.5zM6.75 14.5a.25.25 0 00-.25.25v2.5c0 .138.112.25.25.25h6.5a.25.25 0 00.25-.25v-2.5a.25.25 0 00-.25-.25h-6.5z"
            clipRule="evenodd"
          />
        </svg>
        <span>Cetak / PDF</span>
      </button>
    </div>
  );
}
