"use client";

import { useState } from "react";
import { FormConfig } from "@/lib/types";
import { submitSiteForm } from "@/lib/api";
import { CheckCircle2, AlertCircle, Loader2, Send } from "lucide-react";

interface DynamicFormProps {
  slug: string;
  config: FormConfig;
}

export default function DynamicForm({ slug, config }: DynamicFormProps) {
  const [formData, setFormData] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleChange = (name: string, value: string) => {
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setSuccessMessage(null);
    setErrorMessage(null);

    try {
      const res = await submitSiteForm(slug, config.name, formData);
      setSuccessMessage(res.message || "Pendaftaran atau pesan Anda berhasil dikirim!");
      setFormData({});
    } catch (err: any) {
      setErrorMessage(err.message || "Gagal mengirim data. Silakan coba lagi.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-xl mx-auto my-8 p-6 sm:p-8 bg-surface rounded-2xl border border-border shadow-sm">
      <div className="mb-6">
        <h3 className="text-xl font-bold text-foreground">{config.title}</h3>
        <p className="text-sm text-muted mt-1">
          Silakan lengkapi formulir di bawah ini dengan data yang valid.
        </p>
      </div>

      {successMessage && (
        <div className="mb-6 p-4 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-200 flex items-start gap-3">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
          <div className="text-sm font-medium">{successMessage}</div>
        </div>
      )}

      {errorMessage && (
        <div className="mb-6 p-4 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-200 flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
          <div className="text-sm font-medium">{errorMessage}</div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {config.fields.map((field) => (
          <div key={field.name} className="space-y-1.5">
            <label className="block text-sm font-semibold text-foreground">
              {field.label} {field.required && <span className="text-rose-500">*</span>}
            </label>
            {field.type === "textarea" ? (
              <textarea
                name={field.name}
                required={field.required}
                placeholder={field.placeholder}
                rows={3}
                value={formData[field.name] || ""}
                onChange={(e) => handleChange(field.name, e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-border-strong bg-surface text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition"
              />
            ) : (
              <input
                type={field.type || "text"}
                name={field.name}
                required={field.required}
                placeholder={field.placeholder}
                value={formData[field.name] || ""}
                onChange={(e) => handleChange(field.name, e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-border-strong bg-surface text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition"
              />
            )}
          </div>
        ))}

        <button
          type="submit"
          disabled={loading}
          className="w-full mt-4 h-12 inline-flex items-center justify-center gap-2 rounded-xl bg-primary text-white font-semibold text-sm hover:opacity-95 active:scale-[0.99] transition disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
        >
          {loading ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              Mengirim data...
            </>
          ) : (
            <>
              <Send className="w-4 h-4" />
              Kirim Formulir
            </>
          )}
        </button>
      </form>
    </div>
  );
}
