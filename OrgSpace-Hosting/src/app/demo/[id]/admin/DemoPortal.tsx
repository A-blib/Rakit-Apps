"use client";

import { useState } from "react";
import { PortalPengurus } from "@/components/PortalPengurus";
import { createDemoApi, DEMO_LOGIN } from "@/lib/demo-portal";
import type { TemplatePreviewData } from "@/lib/types";

export function DemoPortal({ templateId, sample }: { templateId: string; sample: TemplatePreviewData }) {
  // Dibuat sekali per tab: data demo hidup di memori dan hilang saat halaman ditutup atau dimuat ulang.
  const [api] = useState(() => createDemoApi(sample));
  return <PortalPengurus slug="demo" siteHref={`/demo/${templateId}/`} api={api} demo={DEMO_LOGIN} />;
}
