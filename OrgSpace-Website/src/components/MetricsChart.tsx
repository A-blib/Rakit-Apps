"use client";

import { useId, useState } from "react";
import { formatDay, formatNumber } from "@/lib/format";
import type { DailyPoint } from "@/lib/api/types";

export type ChartType = "bar" | "line";
export type ChartColor = "sky" | "emerald" | "violet" | "primary";

interface MetricsChartProps {
  points: DailyPoint[];
  label: string;
  initialType?: ChartType;
  showToggle?: boolean;
  color?: ChartColor;
}

const COLOR_THEMES = {
  sky: {
    activeBar:
      "bg-sky-500 hover:bg-sky-400 dark:bg-cyan-400 dark:hover:bg-cyan-300 shadow-sm shadow-sky-500/30",
    zeroBar:
      "bg-sky-500/20 hover:bg-sky-500/40 dark:bg-cyan-500/20 dark:hover:bg-cyan-500/40",
    lineStroke: "#0284c7",
    lineStrokeDarkClass: "dark:stroke-[#00c9f2]",
    lineStop: "#0284c7",
    circleClass: "stroke-sky-600 dark:stroke-cyan",
  },
  emerald: {
    activeBar:
      "bg-emerald-500 hover:bg-emerald-400 dark:bg-emerald-400 dark:hover:bg-emerald-300 shadow-sm shadow-emerald-500/30",
    zeroBar:
      "bg-emerald-500/20 hover:bg-emerald-500/40 dark:bg-emerald-500/20 dark:hover:bg-emerald-500/40",
    lineStroke: "#059669",
    lineStrokeDarkClass: "dark:stroke-[#34d399]",
    lineStop: "#059669",
    circleClass: "stroke-emerald-600 dark:stroke-emerald-400",
  },
  violet: {
    activeBar:
      "bg-indigo-500 hover:bg-indigo-400 dark:bg-violet-400 dark:hover:bg-violet-300 shadow-sm shadow-indigo-500/30",
    zeroBar:
      "bg-indigo-500/20 hover:bg-indigo-500/40 dark:bg-violet-500/20 dark:hover:bg-violet-500/40",
    lineStroke: "#4f46e5",
    lineStrokeDarkClass: "dark:stroke-[#a78bfa]",
    lineStop: "#4f46e5",
    circleClass: "stroke-indigo-600 dark:stroke-violet-400",
  },
  primary: {
    activeBar:
      "bg-sky-500 hover:bg-sky-400 dark:bg-cyan-400 dark:hover:bg-cyan-300 shadow-sm shadow-sky-500/30",
    zeroBar:
      "bg-sky-500/20 hover:bg-sky-500/40 dark:bg-cyan-500/20 dark:hover:bg-cyan-500/40",
    lineStroke: "#0284c7",
    lineStrokeDarkClass: "dark:stroke-[#00c9f2]",
    lineStop: "#0284c7",
    circleClass: "stroke-primary dark:stroke-cyan",
  },
};

/**
 * Komponen grafik metrik harian interaktif Clean Minimalist SaaS.
 * Menampilkan batang metrik dengan warna solid cerah & tegas (Biru Sky untuk Pendaftar,
 * Hijau Emerald untuk Pengguna Aktif) yang menyala dan terlihat jelas di setiap batang.
 */
export function MetricsChart({
  points,
  label,
  initialType = "bar",
  showToggle = true,
  color = "sky",
}: MetricsChartProps) {
  const [chartType, setChartType] = useState<ChartType>(initialType);
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);
  const gradientId = useId();

  const theme = COLOR_THEMES[color] || COLOR_THEMES.sky;

  if (!points || points.length === 0) {
    return (
      <div className="flex h-44 items-center justify-center rounded-xl border border-dashed border-border/80 bg-surface-subtle/40 text-xs text-muted">
        Tidak ada data untuk periode ini
      </div>
    );
  }

  const max = Math.max(1, ...points.map((p) => p.count));
  const midIndex = Math.floor(points.length / 2);

  // Dimensi SVG koordinat untuk Line Chart
  const svgWidth = 600;
  const svgHeight = 176;
  const padX = 20;
  const padTop = 26;
  const padBottom = 16;
  const chartHeight = svgHeight - padTop - padBottom;
  const chartWidth = svgWidth - 2 * padX;

  const coords = points.map((p, i) => {
    const x = points.length === 1 ? svgWidth / 2 : padX + (i / (points.length - 1)) * chartWidth;
    const y = svgHeight - padBottom - (p.count / max) * chartHeight;
    return { x, y, point: p };
  });

  // Kurva bezier halus (cubic bezier) yang stabil tanpa overshoot
  const linePath = coords.reduce((acc, curr, i, arr) => {
    if (i === 0) return `M ${curr.x},${curr.y}`;
    const prev = arr[i - 1];
    const cp1x = prev.x + (curr.x - prev.x) / 2;
    const cp1y = prev.y;
    const cp2x = prev.x + (curr.x - prev.x) / 2;
    const cp2y = curr.y;
    return `${acc} C ${cp1x},${cp1y} ${cp2x},${cp2y} ${curr.x},${curr.y}`;
  }, "");

  const areaPath =
    coords.length > 0
      ? `${linePath} L ${coords[coords.length - 1].x},${svgHeight - padBottom} L ${coords[0].x},${svgHeight - padBottom} Z`
      : "";

  return (
    <figure aria-label={label} className="w-full">
      {/* Chart Control Toolbar */}
      {showToggle && (
        <div className="flex items-center justify-between mb-3">
          <span className="text-[11px] font-mono text-muted">
            Tipe visualisasi: <strong className="text-foreground capitalize">{chartType === "bar" ? "Batang (Bar)" : "Garis (Line)"}</strong>
          </span>

          <div
            className="inline-flex rounded-lg border border-border bg-surface-subtle p-0.5 print:hidden"
            role="group"
            aria-label="Pilih tipe grafik"
          >
            <button
              type="button"
              onClick={() => setChartType("bar")}
              className={`flex items-center gap-1.5 rounded-md px-2 py-1 text-[11px] font-medium transition-colors cursor-pointer ${
                chartType === "bar"
                  ? "bg-surface font-semibold text-foreground shadow-2xs border border-border"
                  : "text-muted hover:text-foreground"
              }`}
              title="Ubah ke Grafik Batang (Bar Chart)"
            >
              <svg className="size-3.5" viewBox="0 0 20 20" fill="currentColor">
                <path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zm6-4a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zm6-3a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z" />
              </svg>
              <span>Batang</span>
            </button>
            <button
              type="button"
              onClick={() => setChartType("line")}
              className={`flex items-center gap-1.5 rounded-md px-2 py-1 text-[11px] font-medium transition-colors cursor-pointer ${
                chartType === "line"
                  ? "bg-surface font-semibold text-foreground shadow-2xs border border-border"
                  : "text-muted hover:text-foreground"
              }`}
              title="Ubah ke Grafik Garis (Line Chart)"
            >
              <svg className="size-3.5" viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M3 15l5-5 4 4 5-8" />
              </svg>
              <span>Garis</span>
            </button>
          </div>
        </div>
      )}

      {/* Kontainer Grafik */}
      {chartType === "bar" ? (
        /* ================= TAMPILAN BAR CHART (BERWARNA SOLID & TEGAS) ================= */
        <div className="relative flex h-44 w-full items-end gap-1.5 pt-6 pb-1 border-b border-border">
          {/* Garis Grid Referensi */}
          <div className="pointer-events-none absolute inset-x-0 top-6 flex items-center justify-between border-b border-dashed border-border/60 text-[9px] font-mono text-muted">
            <span>{formatNumber(max)}</span>
          </div>
          <div className="pointer-events-none absolute inset-x-0 top-1/2 flex items-center justify-between border-b border-dashed border-border/60 text-[9px] font-mono text-muted">
            <span>{formatNumber(Math.round(max / 2))}</span>
          </div>

          {/* Batang-batang metrik berwarna solid & menyala */}
          {points.map((p, idx) => {
            const heightPercent =
              p.count > 0 ? Math.max(12, (p.count / max) * 100) : 4;
            const barColorClass =
              p.count > 0 ? theme.activeBar : theme.zeroBar;

            return (
              <div
                key={p.date}
                className="group relative flex-1 h-full flex items-end"
                onMouseEnter={() => setHoveredIndex(idx)}
                onMouseLeave={() => setHoveredIndex(null)}
              >
                <div
                  tabIndex={0}
                  aria-label={`${formatDay(p.date)}: ${formatNumber(p.count)}`}
                  className={`w-full rounded-t-[3px] transition-all duration-150 cursor-pointer ${barColorClass}`}
                  style={{ height: `${heightPercent}%` }}
                />
                {/* Floating Tooltip */}
                <div className="pointer-events-none absolute -top-8.5 left-1/2 -translate-x-1/2 z-20 hidden rounded-md border border-border bg-surface px-2 py-0.5 text-[11px] font-medium font-mono text-foreground shadow-xs group-hover:flex items-center gap-1.5 whitespace-nowrap">
                  <span className="text-muted">{formatDay(p.date)}:</span>
                  <span className="font-semibold text-foreground">{formatNumber(p.count)}</span>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        /* ================= TAMPILAN LINE CHART ================= */
        <div className="relative h-44 w-full border-b border-border">
          {/* Garis Grid Referensi */}
          <div className="pointer-events-none absolute inset-x-0 top-6 flex items-center justify-between border-b border-dashed border-border/60 text-[9px] font-mono text-muted">
            <span>{formatNumber(max)}</span>
          </div>
          <div className="pointer-events-none absolute inset-x-0 top-1/2 flex items-center justify-between border-b border-dashed border-border/60 text-[9px] font-mono text-muted">
            <span>{formatNumber(Math.round(max / 2))}</span>
          </div>

          <svg
            viewBox={`0 0 ${svgWidth} ${svgHeight}`}
            className="h-full w-full overflow-visible"
            preserveAspectRatio="none"
          >
            <defs>
              <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={theme.lineStop} stopOpacity="0.28" />
                <stop offset="100%" stopColor={theme.lineStop} stopOpacity="0.0" />
              </linearGradient>
            </defs>

            {/* Area Fill di bawah kurva */}
            <path d={areaPath} fill={`url(#${gradientId})`} />

            {/* Garis Kurva Utama */}
            <path
              d={linePath}
              fill="none"
              stroke={theme.lineStroke}
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              className={theme.lineStrokeDarkClass}
            />

            {/* Titik-titik Data Interaktif */}
            {coords.map((c, i) => {
              const isHovered = hoveredIndex === i;
              return (
                <g key={c.point.date}>
                  {/* Vertical guide line on active hover */}
                  {isHovered && (
                    <line
                      x1={c.x}
                      y1={padTop}
                      x2={c.x}
                      y2={svgHeight - padBottom}
                      stroke="var(--border-strong)"
                      strokeWidth="1"
                      strokeDasharray="3 3"
                    />
                  )}
                  {/* Data dot */}
                  <circle
                    cx={c.x}
                    cy={c.y}
                    r={isHovered ? 5.5 : 3.5}
                    className={`fill-surface transition-all duration-150 ${theme.circleClass}`}
                    strokeWidth={isHovered ? "2.5" : "1.8"}
                  />
                </g>
              );
            })}
          </svg>

          {/* Interactive Hover Layer with Tooltips */}
          <div className="absolute inset-0 flex items-stretch">
            {coords.map((c, i) => (
              <div
                key={c.point.date}
                className="group relative flex-1 h-full cursor-pointer"
                onMouseEnter={() => setHoveredIndex(i)}
                onMouseLeave={() => setHoveredIndex(null)}
                tabIndex={0}
                aria-label={`${formatDay(c.point.date)}: ${formatNumber(c.point.count)}`}
              >
                {/* Floating Tooltip */}
                <div className="pointer-events-none absolute -top-8.5 left-1/2 -translate-x-1/2 z-20 hidden rounded-md border border-border bg-surface px-2 py-0.5 text-[11px] font-medium font-mono text-foreground shadow-xs group-hover:flex items-center gap-1.5 whitespace-nowrap">
                  <span className="text-muted">{formatDay(c.point.date)}:</span>
                  <span className="font-semibold text-foreground">{formatNumber(c.point.count)}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Axis dates */}
      <figcaption className="mt-2.5 flex items-center justify-between text-[11px] font-mono text-muted">
        <span>{formatDay(points[0].date)}</span>
        {points.length > 2 && <span>{formatDay(points[midIndex].date)}</span>}
        <span>{formatDay(points[points.length - 1].date)}</span>
      </figcaption>
    </figure>
  );
}
