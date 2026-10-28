import type { FieldType } from "./types";

/**
 * Pengaturan tampilan Portal Pengurus dari portal.json template (GET render/{slug} → `portal`). Backend sudah
 * memvalidasi dan hanya mengirim bagian yang valid (PortalConfig.java); di sini tetap dicek ringan karena nilainya
 * masuk ke CSS. Kontrak: artikel /docs/tampilan-portal-pengurus dan OrgSpace-Apps/docs/dokumentasi-project.md bab 33ab.
 */
export interface PortalConfig {
  theme?: { primary?: string; accent?: string; radius?: number };
  layout?: {
    sidebar?: { position?: "left" | "right"; width?: number; resizable?: boolean };
    header?: { variant?: "minimal" | "banner"; image?: string; title?: string; subtitle?: string };
  };
  nav?: NavEntry[];
  modules?: Record<string, ModuleSettings>;
  mobile?: { nav?: "drawer" | "bottom"; bottom?: string[] };
}

export type BuiltinId = "overview" | "news" | "submissions";
type NavItemConfig = { builtin?: BuiltinId; module?: string; label?: string; icon?: string };
export type NavEntry = NavItemConfig | { group: string; items: NavItemConfig[] };

export interface ModuleSettings {
  label?: string;
  icon?: string;
  description?: string;
  view?: "tabel" | "kartu";
  title?: string;
  subtitle?: string;
  image?: string;
  fields?: Record<string, { label?: string; help?: string }>;
  groups?: { title: string; fields: string[] }[];
}

/** Id menu: menu bawaan atau "mod:{nama modul}". */
export type NavId = BuiltinId | `mod:${string}`;

export interface NavLink {
  id: NavId;
  label: string;
  icon: string;
}

export interface NavGroup {
  title?: string;
  items: NavLink[];
}

const BUILTIN: Record<BuiltinId, { label: string; icon: string }> = {
  overview: { label: "Ringkasan", icon: "layout-dashboard" },
  submissions: { label: "Kotak Masuk Form", icon: "inbox" },
  news: { label: "Berita & Agenda", icon: "newspaper" },
};

/** "program_kerja" → "Program Kerja": label bawaan modul dan field tanpa portal.json. */
export function humanize(name: string): string {
  return name
    .split(/[_-]+/)
    .filter(Boolean)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");
}

export function moduleLabel(config: PortalConfig, name: string): string {
  return config.modules?.[name]?.label || humanize(name);
}

/**
 * Susunan menu. Tanpa nav: menu bawaan, lalu semua modul template. Dengan nav: urutan provider; menu yang tidak
 * disebut tetap muncul di grup "Lainnya" agar datanya tidak terkunci. Data modul yang sudah tidak ada di template
 * (mis. setelah ganti versi template) masuk grup terpisah agar masih bisa dilihat dan dihapus.
 */
export function buildNav(
  config: PortalConfig,
  modules: string[],
  orphans: string[],
  canViewSubmissions: boolean
): NavGroup[] {
  const allowed = (id: NavId) => id !== "submissions" || canViewSubmissions;
  const used = new Set<NavId>();
  const link = (entry: NavItemConfig): NavLink | null => {
    const id: NavId | null = entry.builtin ? entry.builtin : entry.module ? `mod:${entry.module}` : null;
    if (!id || used.has(id) || !allowed(id)) return null;
    if (entry.module && !modules.includes(entry.module)) return null;
    used.add(id);
    if (entry.builtin) {
      return { id, label: entry.label || BUILTIN[entry.builtin].label, icon: entry.icon || BUILTIN[entry.builtin].icon };
    }
    const settings = config.modules?.[entry.module!];
    return { id, label: entry.label || moduleLabel(config, entry.module!), icon: entry.icon || settings?.icon || "boxes" };
  };

  const groups: NavGroup[] = [];
  if (config.nav?.length) {
    let loose: NavLink[] = [];
    const flush = () => {
      if (loose.length) groups.push({ items: loose });
      loose = [];
    };
    for (const entry of config.nav) {
      if ("group" in entry) {
        flush();
        const items = entry.items.map(link).filter((l): l is NavLink => l !== null);
        if (items.length) groups.push({ title: entry.group, items });
      } else {
        const l = link(entry);
        if (l) loose.push(l);
      }
    }
    flush();
    const rest = [
      ...(["overview", "submissions", "news"] as BuiltinId[]).map((b) => link({ builtin: b })),
      ...modules.map((m) => link({ module: m })),
    ].filter((l): l is NavLink => l !== null);
    if (rest.length) groups.push({ title: "Lainnya", items: rest });
  } else {
    const main = (["overview", "submissions", "news"] as BuiltinId[])
      .map((b) => link({ builtin: b }))
      .filter((l): l is NavLink => l !== null);
    groups.push({ items: main });
    const data = modules.map((m) => link({ module: m })).filter((l): l is NavLink => l !== null);
    if (data.length) groups.push({ title: "Data Website", items: data });
  }
  const lost = orphans.filter((o) => !modules.includes(o));
  if (lost.length) {
    groups.push({
      title: "Data tidak dipakai template",
      items: lost.map((o) => ({ id: `mod:${o}` as NavId, label: humanize(o), icon: "folder" })),
    });
  }
  return groups;
}

/** Menu bawah HP: pilihan provider (maks. 4), atau 4 menu pertama. Menu tanpa izin dilewati. */
export function bottomNav(config: PortalConfig, groups: NavGroup[]): NavLink[] {
  const all = groups.flatMap((g) => g.items);
  const picked = config.mobile?.bottom?.length
    ? config.mobile.bottom
        .map((id) => all.find((l) => l.id === id || l.id === `mod:${id}`))
        .filter((l): l is NavLink => !!l)
    : all;
  return picked.slice(0, 4);
}

/** Field form per grup. Field yang tidak masuk grup mana pun dikumpulkan di akhir. */
export function fieldGroups(settings: ModuleSettings | undefined, fields: string[]): { title?: string; fields: string[] }[] {
  const groups: { title?: string; fields: string[] }[] = (settings?.groups || [])
    .map((g) => ({ title: g.title, fields: g.fields.filter((f) => fields.includes(f)) }))
    .filter((g) => g.fields.length);
  const grouped = new Set(groups.flatMap((g) => g.fields));
  const rest = fields.filter((f) => !grouped.has(f));
  if (rest.length) groups.push({ title: groups.length ? "Lainnya" : undefined, fields: rest });
  return groups;
}

/** Field judul, subjudul, dan gambar untuk kartu/tabel. Tanpa pengaturan: teks pertama, field kedua, gambar pertama. */
export function cardFields(settings: ModuleSettings | undefined, fields: Record<string, FieldType>) {
  const names = Object.keys(fields);
  const textual = names.filter((n) => fields[n] !== "gambar" && fields[n] !== "link");
  const title = settings?.title || textual[0];
  return {
    title,
    subtitle: settings?.subtitle || textual.find((n) => n !== title),
    image: settings?.image || names.find((n) => fields[n] === "gambar"),
  };
}

const HEX = /^#[0-9a-fA-F]{6}$/;

/**
 * Campuran dua warna hex, `weight` = porsi warna pertama (0–1). Dihitung di sini, bukan dengan CSS color-mix(),
 * karena color-mix belum ada di Chrome < 111 yang masih dipakai sebagian HP pengurus.
 */
export function mix(a: string, b: string, weight: number): string {
  const channel = (hex: string, i: number) => parseInt(hex.slice(1 + i * 2, 3 + i * 2), 16);
  return (
    "#" +
    [0, 1, 2]
      .map((i) => Math.round(channel(a, i) * weight + channel(b, i) * (1 - weight)))
      .map((v) => v.toString(16).padStart(2, "0"))
      .join("")
      .toUpperCase()
  );
}

/**
 * CSS yang menimpa token warna OrgSpace (globals.css) dan radius Tailwind. Mode gelap memakai warna provider yang
 * dicampur putih agar tetap terbaca di latar gelap. Kosong jika tidak ada tema.
 */
export function themeCss(theme: PortalConfig["theme"]): string {
  if (!theme) return "";
  const light: string[] = [];
  const dark: string[] = [];
  const p = theme.primary && HEX.test(theme.primary) ? theme.primary : null;
  const a = theme.accent && HEX.test(theme.accent) ? theme.accent : null;
  if (p) {
    light.push(
      `--primary:${p}`,
      `--on-primary:#FFFFFF`,
      `--primary-container:${mix(p, "#FFFFFF", 0.12)}`,
      `--on-primary-container:${mix(p, "#000000", 0.85)}`,
      `--link:${p}`
    );
    dark.push(
      `--primary:${mix(p, "#FFFFFF", 0.55)}`,
      `--on-primary:#04142B`,
      `--primary-container:${mix(p, "#07111F", 0.4)}`,
      `--on-primary-container:#E8F1FD`,
      `--link:${mix(p, "#FFFFFF", 0.5)}`
    );
  }
  if (a) {
    light.push(`--accent:${a}`, `--on-accent:#FFFFFF`);
    dark.push(`--accent:${mix(a, "#FFFFFF", 0.6)}`, `--on-accent:#04142B`);
  }
  const r = theme.radius;
  if (Number.isInteger(r) && r! >= 0 && r! <= 24) {
    // Kelas rounded-lg/xl/2xl Tailwind v4 membaca variabel ini.
    light.push(`--radius-lg:${Math.round(r! * 0.67)}px`, `--radius-xl:${r}px`, `--radius-2xl:${Math.round(r! * 1.33)}px`);
  }
  if (!light.length) return "";
  return `:root{${light.join(";")}}` + (dark.length ? `:root.dark{${dark.join(";")}}` : "");
}
