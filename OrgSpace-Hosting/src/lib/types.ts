import type { PortalConfig } from "./portal-config";

export interface SeoMeta {
  title: string;
  description: string;
  faviconUrl: string | null;
  shareImageUrl: string | null;
}

export interface NewsItem {
  id: string;
  title: string;
  summary: string;
  content: string;
  publishedAt: string;
  imageUrl: string | null;
}

export interface FormField {
  name: string;
  label: string;
  type: string;
  required: boolean;
  placeholder?: string;
}

export interface FormConfig {
  name: string;
  title: string;
  submitEndpoint: string;
  fields: FormField[];
}

export interface FieldValue {
  text?: string;
  href?: string;
  image?: string;
}

export interface FieldStyle {
  color?: string;
  'background-color'?: string;
  'font-size'?: number | string;
  'border-radius'?: number | string;
}

export interface CustomSection {
  id: string;
  type: 'header' | 'hero' | 'profile' | 'structure' | 'programs' | 'news' | 'gallery' | 'form' | 'contact' | 'footer' | string;
  variant?: string;
  name?: string;
  hidden?: boolean;
  fields?: Record<string, FieldValue>;
  styles?: Record<string, FieldStyle>;
}

export interface ProjectSnapshot {
  fields?: Record<string, FieldValue>;
  styles?: Record<string, FieldStyle>;
  theme?: Record<string, string>;
  sections?: CustomSection[];
}

export type FieldType = "teks" | "paragraf" | "angka" | "tanggal" | "link" | "gambar";

export interface SiteRenderData {
  siteId: string;
  projectId: string;
  organizationId: string;
  slug: string;
  name: string;
  organizationName: string;
  mode: 'template' | 'assembled';
  status: 'online' | 'offline' | 'suspended';
  version: number;
  publishedAt: string;
  seo: SeoMeta;
  content: ProjectSnapshot;
  news: NewsItem[];
  forms: FormConfig[];
  collections?: Record<string, Array<Record<string, any>>>;
  compiledHtml: string | null;
  // Nama modul → field → jenis isian (teks, paragraf, angka, tanggal, link, gambar).
  modules?: Record<string, Record<string, FieldType>>;
  // Angka untuk data-ws-stat (pengurus, berita, form.{nama}, {modul}) dan profil umum organisasi untuk data-ws-org.
  stats?: Record<string, number>;
  org?: Record<string, string>;
  // Tampilan Portal Pengurus dari portal.json template (hanya bagian yang valid).
  portal?: PortalConfig;
}

/** Data contoh preview template (GET /api/public/templates/{id}/preview); semua isinya buatan backend. */
export interface TemplatePreviewData {
  templateId: string;
  name: string;
  news: NewsItem[];
  collections: Record<string, Array<Record<string, unknown>>>;
  modules: Record<string, Record<string, FieldType>>;
  stats: Record<string, number>;
  org: Record<string, string>;
  portal?: PortalConfig;
}
