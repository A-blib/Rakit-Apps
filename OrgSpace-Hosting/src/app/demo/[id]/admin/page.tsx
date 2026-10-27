import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { BACKEND_API_URL } from "@/lib/config";
import { isTemplateId } from "@/lib/site-page";
import type { TemplatePreviewData } from "@/lib/types";
import { DemoPortal } from "./DemoPortal";

export const metadata: Metadata = { title: "Portal Pengurus demo • OrgSpace", robots: { index: false } };

/** Portal Pengurus demo untuk preview template: data contoh dari backend, sesudah itu semua jalan di memori browser. */
export default async function DemoAdminPage({ params }: PageProps<"/demo/[id]/admin">) {
  const { id } = await params;
  if (!isTemplateId(id)) notFound();
  const res = await fetch(`${BACKEND_API_URL}/api/public/templates/${encodeURIComponent(id)}/preview`, { cache: "no-store" });
  if (!res.ok) notFound();
  const sample = (await res.json()) as TemplatePreviewData;
  return <DemoPortal templateId={id} sample={sample} />;
}
