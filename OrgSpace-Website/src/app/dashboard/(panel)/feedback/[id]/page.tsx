import Link from "next/link";
import { notFound } from "next/navigation";
import { adminApi } from "@/lib/api/admin";
import { ApiError } from "@/lib/api/http";
import { adminToken } from "@/lib/auth/session";
import {
  FEEDBACK_CATEGORY_LABEL,
  FEEDBACK_STATUS_LABEL,
  SENDER_ROLE_LABEL,
  formatDateTime,
} from "@/lib/format";
import { setFeedbackStatus } from "../../../actions";
import { ReplyForm } from "./ReplyForm";

export default async function FeedbackDetailPage({ params }: PageProps<"/dashboard/feedback/[id]">) {
  const { id } = await params;
  let feedback;
  try {
    feedback = await adminApi.feedbackDetail(await adminToken(), id);
  } catch (error) {
    if (error instanceof ApiError && error.code === "FEEDBACK_NOT_FOUND") notFound();
    throw error;
  }

  return (
    <div className="max-w-3xl space-y-6 mx-auto">
      <Link
        href="/dashboard/feedback"
        className="inline-flex items-center gap-1.5 text-xs font-medium text-muted hover:text-foreground transition-colors duration-150"
      >
        ← Kembali ke Kritik & saran
      </Link>

      <header className="border-b border-border/80 pb-4">
        <div className="flex items-center gap-2 flex-wrap mb-2">
          <span className="badge-neutral text-[10px]">
            {FEEDBACK_CATEGORY_LABEL[feedback.category]}
          </span>
          <span className="badge-neutral text-[10px]">
            {SENDER_ROLE_LABEL[feedback.senderRole]}
          </span>
          {feedback.rating && (
            <span className="badge-warning text-[10px] font-mono">
              ★ Nilai {feedback.rating}/5
            </span>
          )}
        </div>
        <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-foreground">
          {feedback.subject}
        </h1>
        <p className="mt-1 text-xs text-muted">
          Dari <span className="font-semibold text-foreground">{feedback.sender.displayName}</span> ({feedback.sender.email ?? "tanpa email"})
          {feedback.organizationName ? ` · ${feedback.organizationName}` : ""} · via {feedback.source}
          {feedback.appVersion ? ` · app v${feedback.appVersion}` : ""}
          {feedback.deviceInfo ? ` · ${feedback.deviceInfo}` : ""}
        </p>
      </header>

      <div className="flex items-center justify-between card p-3.5 bg-surface-subtle/50">
        <form action={setFeedbackStatus.bind(null, feedback.id)} className="flex items-center gap-2">
          <label className="text-xs font-medium text-foreground" htmlFor="status">Ubah Status:</label>
          <select className="input h-8.5 w-auto text-xs py-1" id="status" name="status" defaultValue={feedback.status}>
            {Object.entries(FEEDBACK_STATUS_LABEL).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
          <button className="btn-outline h-8.5 px-3 text-xs">Simpan Perubahan</button>
        </form>
      </div>

      <ol className="space-y-3">
        {feedback.messages.map((message) => (
          <li
            key={message.id}
            className={`card p-4 transition-colors duration-150 ${
              message.fromStaff
                ? "ml-6 border-primary/30 bg-primary-container/15"
                : "mr-6 bg-surface"
            }`}
          >
            <div className="flex items-center justify-between border-b border-border/60 pb-2 mb-2">
              <p className="text-xs font-semibold text-foreground">
                {message.fromStaff ? (
                  <span className="inline-flex items-center gap-1.5 text-primary dark:text-cyan">
                    <span className="size-1.5 rounded-full bg-primary dark:bg-cyan" />
                    {message.authorName} (Tim Internal)
                  </span>
                ) : (
                  <span>{message.authorName}</span>
                )}
              </p>
              <span className="text-[11px] font-mono text-muted">
                {formatDateTime(message.createdAt)}
              </span>
            </div>
            <p className="text-xs text-foreground/90 leading-relaxed whitespace-pre-wrap">{message.body}</p>
          </li>
        ))}
      </ol>

      {feedback.status === "closed" ? (
        <div className="rounded-lg border border-border bg-surface-subtle p-3.5 text-center text-xs text-muted">
          Percakapan ditutup. Ubah status untuk membalas kembali.
        </div>
      ) : (
        <div className="card p-5">
          <ReplyForm id={feedback.id} />
        </div>
      )}
    </div>
  );
}
