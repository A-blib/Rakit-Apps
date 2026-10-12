import type { Metadata } from "next";
import { Plus_Jakarta_Sans } from "next/font/google";
import "./globals.css";

// Font merek OrgSpace (sama dengan app Android), lisensi SIL OFL.
const jakarta = Plus_Jakarta_Sans({
  variable: "--font-jakarta",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: { default: "OrgSpace — Website organisasi tanpa coding", template: "%s · OrgSpace" },
  description: "Buat website OSIS, BEM, UKM, dan komunitas langsung dari HP. Pilih template, isi, terbitkan.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="id" className={`${jakarta.variable} h-full antialiased`}>
      <body className="flex min-h-full flex-col font-sans">{children}</body>
    </html>
  );
}
