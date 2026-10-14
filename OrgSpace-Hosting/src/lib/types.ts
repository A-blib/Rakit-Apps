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
}
