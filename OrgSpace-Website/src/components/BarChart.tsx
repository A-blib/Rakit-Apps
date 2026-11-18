import { AreaChart, type AreaChartProps } from "./AreaChart";

/**
 * BarChart sekarang dirender sebagai modern Area Chart dengan kurva halus,
 * area gradient fill, dan hover tooltip interaktif.
 */
export function BarChart(props: AreaChartProps) {
  return <AreaChart {...props} />;
}
