"use client";

import { useId, useState, useRef, useMemo, useCallback } from "react";
import { formatDay, formatNumber } from "@/lib/format";
import type { DailyPoint } from "@/lib/api/types";

export interface AreaChartProps {
  points: DailyPoint[];
  label: string;
  color?: "cyan" | "blue" | "emerald" | "purple";
}

const COLOR_MAP = {
  cyan: {
    stroke: "#00C9F2",
    fillTop: "rgba(0, 201, 242, 0.42)",
    fillBottom: "rgba(0, 201, 242, 0.01)",
    glow: "rgba(0, 201, 242, 0.3)",
    badgeBg: "bg-cyan/10 text-cyan border-cyan/30",
  },
  blue: {
    stroke: "#009FE3",
    fillTop: "rgba(0, 159, 227, 0.42)",
    fillBottom: "rgba(0, 159, 227, 0.01)",
    glow: "rgba(0, 159, 227, 0.3)",
    badgeBg: "bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/40 dark:text-blue-300 dark:border-blue-800",
  },
  emerald: {
    stroke: "#10B981",
    fillTop: "rgba(16, 185, 129, 0.42)",
    fillBottom: "rgba(16, 185, 129, 0.01)",
    glow: "rgba(16, 185, 129, 0.3)",
    badgeBg: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800",
  },
  purple: {
    stroke: "#8B5CF6",
    fillTop: "rgba(139, 92, 246, 0.42)",
    fillBottom: "rgba(139, 92, 246, 0.01)",
    glow: "rgba(139, 92, 246, 0.3)",
    badgeBg: "bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-800",
  },
};

/**
 * Modern SVG Area Chart Clean Enterprise dengan kurva spline halus,
 * area gradient fill, interactive hover state, dan tooltip presisi.
 */
export function AreaChart({ points, label, color = "cyan" }: AreaChartProps) {
  const gradientId = useId();
  const containerRef = useRef<HTMLDivElement>(null);
  const [hoveredIdx, setHoveredIdx] = useState<number | null>(null);

  const colors = COLOR_MAP[color] ?? COLOR_MAP.cyan;

  // Dimensi koordinat viewBox SVG
  const width = 600;
  const height = 180;
  const padTop = 18;
  const padBottom = 22;
  const padLeft = 14;
  const padRight = 14;

  const plotWidth = width - padLeft - padRight;
  const plotHeight = height - padTop - padBottom;

  const validPoints = useMemo(() => (points && points.length > 0 ? points : []), [points]);
  const max = Math.max(1, ...validPoints.map((p) => p.count));
  const peak = Math.max(0, ...validPoints.map((p) => p.count));
  const total = validPoints.reduce((acc, p) => acc + p.count, 0);
  const avg = validPoints.length > 0 ? (total / validPoints.length).toFixed(1) : "0";
  const midIndex = Math.floor(validPoints.length / 2);

  // Perhitungan koordinat titik-titik (x, y)
  const coords = useMemo(() => {
    const n = validPoints.length;
    if (n === 0) return [];

    return validPoints.map((p, i) => {
      const x = n === 1 ? width / 2 : padLeft + (i / (n - 1)) * plotWidth;
      const y = (height - padBottom) - (p.count / max) * plotHeight;
      return { x, y, count: p.count, date: p.date };
    });
  }, [validPoints, max, plotWidth, plotHeight, width, height, padLeft, padBottom]);

  // Pembuatan kurva halus (Cubic Bezier Spline)
  const { linePath, areaPath } = useMemo(() => {
    if (coords.length === 0) return { linePath: "", areaPath: "" };
    if (coords.length === 1) {
      const p = coords[0];
      const lPath = `M ${p.x - 20},${p.y} L ${p.x + 20},${p.y}`;
      const aPath = `M ${p.x - 20},${height - padBottom} L ${p.x - 20},${p.y} L ${p.x + 20},${p.y} L ${p.x + 20},${height - padBottom} Z`;
      return { linePath: lPath, areaPath: aPath };
    }

    let d = `M ${coords[0].x.toFixed(1)},${coords[0].y.toFixed(1)}`;

    for (let i = 0; i < coords.length - 1; i++) {
      const curr = coords[i];
      const next = coords[i + 1];
      const dx = next.x - curr.x;

      // Smooth control points
      const cp1x = curr.x + dx * 0.35;
      const cp1y = curr.y;
      const cp2x = next.x - dx * 0.35;
      const cp2y = next.y;

      d += ` C ${cp1x.toFixed(1)},${cp1y.toFixed(1)} ${cp2x.toFixed(1)},${cp2y.toFixed(1)} ${next.x.toFixed(1)},${next.y.toFixed(1)}`;
    }

    const first = coords[0];
    const last = coords[coords.length - 1];
    const baselineY = (height - padBottom).toFixed(1);

    const aD = `${d} L ${last.x.toFixed(1)},${baselineY} L ${first.x.toFixed(1)},${baselineY} Z`;

    return { linePath: d, areaPath: aD };
  }, [coords, height, padBottom]);

  // Pointer move handler untuk interaktivitas hover
  const handlePointerMove = useCallback(
    (e: React.PointerEvent<SVGSVGElement>) => {
      if (!coords.length) return;
      const rect = e.currentTarget.getBoundingClientRect();
      const clientX = e.clientX - rect.left;
      const ratio = Math.max(0, Math.min(1, clientX / rect.width));

      // Cari indeks titik terdekat
      const targetIdx = Math.round(ratio * (coords.length - 1));
      setHoveredIdx(targetIdx);
    },
    [coords.length]
  );

  const handlePointerLeave = useCallback(() => {
    setHoveredIdx(null);
  }, []);

  if (!validPoints.length) {
    return (
      <div className="flex h-44 items-center justify-center rounded-field border border-dashed border-border text-xs text-muted">
        Tidak ada data untuk periode ini
      </div>
    );
  }

  const activePoint = hoveredIdx !== null && coords[hoveredIdx] ? coords[hoveredIdx] : null;

  return (
    <figure aria-label={label} className="w-full select-none" ref={containerRef}>
      {/* Container SVG Area Chart */}
      <div className="relative w-full overflow-hidden pt-2">
        {/* Floating Tooltip HTML on hover */}
        {activePoint && (
          <div
            className="pointer-events-none absolute top-1 z-30 -translate-x-1/2 flex flex-col items-center transition-all duration-75"
            style={{
              left: `${(activePoint.x / width) * 100}%`,
            }}
          >
            <div className="rounded-md bg-deep-navy px-2.5 py-1 text-[11px] font-medium text-white shadow-xl border border-white/20 whitespace-nowrap backdrop-blur-md">
              <div className="flex items-center gap-2">
                <span className="size-2 rounded-full" style={{ backgroundColor: colors.stroke }} />
                <span className="font-mono font-bold text-white text-xs">
                  {formatNumber(activePoint.count)}
                </span>
                <span className="text-white/60 text-[10px]">({formatDay(activePoint.date)})</span>
              </div>
            </div>
            {/* Tooltip Caret */}
            <div className="size-1.5 rotate-45 bg-deep-navy border-r border-b border-white/20 -mt-1" />
          </div>
        )}

        <svg
          viewBox={`0 0 ${width} ${height}`}
          className="w-full h-44 overflow-visible cursor-crosshair touch-none"
          preserveAspectRatio="none"
          onPointerMove={handlePointerMove}
          onPointerLeave={handlePointerLeave}
        >
          <defs>
            {/* Linear Gradient untuk Area Fill */}
            <linearGradient id={`area-grad-${gradientId}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={colors.stroke} stopOpacity="0.45" />
              <stop offset="65%" stopColor={colors.stroke} stopOpacity="0.12" />
              <stop offset="100%" stopColor={colors.stroke} stopOpacity="0.0" />
            </linearGradient>

            {/* Filter subtle drop glow */}
            <filter id={`glow-${gradientId}`} x="-20%" y="-20%" width="140%" height="140%">
              <feDropShadow dx="0" dy="2" stdDeviation="3" floodColor={colors.stroke} floodOpacity="0.35" />
            </filter>
          </defs>

          {/* Grid lines horizontal */}
          <line
            x1={padLeft}
            y1={padTop}
            x2={width - padRight}
            y2={padTop}
            stroke="currentColor"
            className="text-border/40"
            strokeDasharray="4 4"
            strokeWidth="1"
          />
          <line
            x1={padLeft}
            y1={padTop + plotHeight / 2}
            x2={width - padRight}
            y2={padTop + plotHeight / 2}
            stroke="currentColor"
            className="text-border/30"
            strokeDasharray="4 4"
            strokeWidth="1"
          />
          <line
            x1={padLeft}
            y1={height - padBottom}
            x2={width - padRight}
            y2={height - padBottom}
            stroke="currentColor"
            className="text-border/70"
            strokeWidth="1"
          />

          {/* Area Fill */}
          <path
            d={areaPath}
            fill={`url(#area-grad-${gradientId})`}
            className="transition-all duration-300"
          />

          {/* Curved Line Stroke */}
          <path
            d={linePath}
            fill="none"
            stroke={colors.stroke}
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            filter={`url(#glow-${gradientId})`}
            className="transition-all duration-300"
          />

          {/* Vertical Guide Line on Hover */}
          {activePoint && (
            <g className="transition-opacity duration-150">
              <line
                x1={activePoint.x}
                y1={padTop}
                x2={activePoint.x}
                y2={height - padBottom}
                stroke={colors.stroke}
                strokeWidth="1.5"
                strokeDasharray="3 3"
                opacity="0.8"
              />
              {/* Outer Pulse Halo */}
              <circle
                cx={activePoint.x}
                cy={activePoint.y}
                r="7"
                fill={colors.stroke}
                opacity="0.25"
                className="animate-ping"
              />
              {/* Outer Solid Ring */}
              <circle
                cx={activePoint.x}
                cy={activePoint.y}
                r="5.5"
                fill={colors.stroke}
                opacity="0.4"
              />
              {/* Center Dot */}
              <circle
                cx={activePoint.x}
                cy={activePoint.y}
                r="3.5"
                fill="#FFFFFF"
                stroke={colors.stroke}
                strokeWidth="2"
              />
            </g>
          )}

          {/* Dots on peak or key points if not hovering */}
          {!activePoint && coords.length > 0 && peak > 0 && (
            (() => {
              const peakPoint = coords.find((c) => c.count === peak);
              if (!peakPoint) return null;
              return (
                <circle
                  cx={peakPoint.x}
                  cy={peakPoint.y}
                  r="3.5"
                  fill="#FFFFFF"
                  stroke={colors.stroke}
                  strokeWidth="2"
                />
              );
            })()
          )}
        </svg>
      </div>

      {/* Axis dates & summary footer */}
      <figcaption className="mt-2 flex items-center justify-between text-[11px] text-muted border-t border-border/50 pt-2">
        <div className="flex items-center gap-2">
          <span className="font-medium text-foreground/80">{formatDay(validPoints[0].date)}</span>
          {validPoints.length > 2 && (
            <>
              <span className="text-muted/40">···</span>
              <span className="hidden sm:inline font-medium text-muted">{formatDay(validPoints[midIndex].date)}</span>
              <span className="text-muted/40">···</span>
            </>
          )}
          <span className="font-medium text-foreground/80">{formatDay(validPoints[validPoints.length - 1].date)}</span>
        </div>

        <div className="flex items-center gap-2 text-[10px]">
          <span className="inline-flex items-center gap-1 rounded bg-surface-subtle px-1.5 py-0.5 border border-border/60">
            <span className="text-muted">Puncak:</span>
            <strong className="font-mono text-foreground">{formatNumber(peak)}</strong>
          </span>
          <span className="hidden sm:inline-flex items-center gap-1 rounded bg-surface-subtle px-1.5 py-0.5 border border-border/60">
            <span className="text-muted">Rata-rata:</span>
            <strong className="font-mono text-foreground">{avg}/hari</strong>
          </span>
        </div>
      </figcaption>
    </figure>
  );
}
