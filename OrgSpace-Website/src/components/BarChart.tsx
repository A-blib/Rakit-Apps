import { MetricsChart, type ChartType, type ChartColor } from "./MetricsChart";
import type { DailyPoint } from "@/lib/api/types";

/**
 * Komponen grafik metrik harian Clean Minimalist SaaS.
 * Mendukung warna batang hidup tematik (sky, emerald, violet) serta pergantian antara Bar dan Line chart.
 */
export function BarChart(props: {
  points: DailyPoint[];
  label: string;
  initialType?: ChartType;
  showToggle?: boolean;
  color?: ChartColor;
}) {
  return <MetricsChart {...props} />;
}
