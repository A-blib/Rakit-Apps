import type { StatsPeriod } from "@/lib/api/types";

/** Periode dari query ?period= (bawaan 30d). */
export function parsePeriod(value: string | string[] | undefined): StatsPeriod {
  return value === "7d" || value === "90d" ? value : "30d";
}

export const PERIODS: { value: StatsPeriod; label: string }[] = [
  { value: "7d", label: "7 hari" },
  { value: "30d", label: "30 hari" },
  { value: "90d", label: "90 hari" },
];
