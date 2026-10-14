import { notFound } from "next/navigation";
import type { Metadata } from "next";
import { getSiteRenderData } from "@/lib/api";
import TemplateRenderer from "@/components/TemplateRenderer";
import CustomRenderer from "@/components/CustomRenderer";

interface PageProps {
  params: Promise<{
    slug: string;
  }>;
}

/**
 * Pembuatan Meta Tag Dinamis (SEO) per Web User.
 * Langsung terisi dari data judul, deskripsi, dan gambar share yang diatur di builder.
 */
export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const { slug } = await params;
  const site = await getSiteRenderData(slug);

  if (!site || site.status !== "online") {
    return {
      title: "Website Tidak Ditemukan • OrgSpace",
      description: "Website yang Anda cari tidak tersedia atau belum diterbitkan.",
      robots: { index: false, follow: false },
    };
  }

  const title = site.seo?.title || `${site.name} — ${site.organizationName}`;
  const description =
    site.seo?.description || `Website resmi ${site.organizationName} diterbitkan melalui OrgSpace.`;

  return {
    title,
    description,
    openGraph: {
      title,
      description,
      type: "website",
      siteName: site.organizationName,
      images: site.seo?.shareImageUrl ? [{ url: site.seo.shareImageUrl }] : undefined,
    },
    icons: site.seo?.faviconUrl ? [{ url: site.seo.faviconUrl }] : undefined,
  };
}

/**
 * Halaman Dinamis Web User (/[slug] atau /[namawebuser]).
 * Mengadaptasi mode proyek (Template vs Custom/Rakit) secara otomatis.
 */
export default async function UserSitePage({ params }: PageProps) {
  const { slug } = await params;
  const site = await getSiteRenderData(slug);

  if (!site || site.status !== "online") {
    notFound();
  }

  // Pilih renderer berdasarkan mode proyek
  if (site.mode === "assembled") {
    return <CustomRenderer site={site} />;
  }

  return <TemplateRenderer site={site} />;
}
