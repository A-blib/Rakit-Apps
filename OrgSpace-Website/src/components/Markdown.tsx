import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

/**
 * Isi artikel dokumentasi (Markdown dari backend). HTML mentah di dalam Markdown TIDAK dirender (bawaan
 * react-markdown), jadi isi artikel tidak bisa menyisipkan script. Blok kode yang lebar digeser ke samping
 * di dalam kotaknya sendiri agar halaman tidak melebar di layar HP.
 */
export function Markdown({ children }: { children: string }) {
  return (
    <div className="space-y-4 leading-relaxed [&_a]:text-link [&_blockquote]:border-l-4 [&_blockquote]:border-border-strong [&_blockquote]:pl-4 [&_blockquote]:text-muted [&_code]:rounded [&_code]:bg-surface-subtle [&_code]:px-1 [&_pre]:overflow-x-auto [&_pre]:rounded-card [&_pre]:border [&_pre]:border-border [&_pre]:bg-surface-subtle [&_pre]:p-4 [&_pre]:text-sm [&_pre]:leading-relaxed [&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_h2]:mt-8 [&_h2]:text-xl [&_h2]:font-bold [&_h3]:font-semibold [&_ol]:list-decimal [&_ol]:pl-6 [&_table]:w-full [&_table]:text-sm [&_td]:border [&_td]:border-border [&_td]:p-2 [&_th]:border [&_th]:border-border [&_th]:bg-surface-subtle [&_th]:p-2 [&_th]:text-left [&_ul]:list-disc [&_ul]:pl-6">
      <ReactMarkdown remarkPlugins={[remarkGfm]}>{children}</ReactMarkdown>
    </div>
  );
}
