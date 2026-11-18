import type { Metadata, Viewport } from "next";
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

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
};

const THEME_SCRIPT = `
(function() {
  try {
    var stored = localStorage.getItem('orgspace_theme');
    var isDark = stored ? stored === 'dark' : window.matchMedia('(prefers-color-scheme: dark)').matches;
    if (isDark) {
      document.documentElement.classList.add('dark');
      document.documentElement.classList.remove('light');
      document.documentElement.setAttribute('data-theme', 'dark');
    } else {
      document.documentElement.classList.add('light');
      document.documentElement.classList.remove('dark');
      document.documentElement.setAttribute('data-theme', 'light');
    }
  } catch(e) {}
})();
`;

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="id" className={`${jakarta.variable} h-full antialiased scroll-smooth`} suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: THEME_SCRIPT }} />
      </head>
      <body className="flex min-h-full flex-col font-sans overflow-x-hidden bg-background text-foreground transition-colors duration-200">
        {children}
      </body>
    </html>
  );
}
