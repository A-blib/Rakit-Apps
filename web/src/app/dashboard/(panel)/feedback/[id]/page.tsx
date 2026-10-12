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
    <div className="max-w-3xl space-y-6">
      <Link href="/dashboard/feedback" className="link text-sm">← Kritik & saran</Link>
      <header>
        <p className="text-sm text-muted">
          {FEEDBACK_CATEGORY_LABEL[feedback.category]} · {SENDER_ROLE_LABEL[feedback.senderRole]}
          {feedback.rating ? ` · Nilai ${feedback.rating}/5` : ""}
        </p>
        <h1 className="text-2xl font-bold">{feedback.subject}</h1>
        <p className="mt-1 text-sm text-muted">
          {feedback.sender.displayName} ({feedback.sender.email ?? "tanpa email"})
          {feedback.organizationName ? ` · ${feedback.organizationName}` : ""} · via {feedback.source}
          {feedback.appVersion ? ` · app ${feedback.appVersion}` : ""}
          {feedback.deviceInfo ? ` · ${feedback.deviceInfo}` : ""}
        </p>
      </header>

      <form action={setFeedbackStatus.bind(null, feedback.id)} className="flex items-center gap-2">
        <label className="text-sm" htmlFor="status">Status</label>
        <select className="input w-auto" id="status" name="status" defaultValue={feedback.status}>
          {Object.entries(FEEDBACK_STATUS_LABEL).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
        <button className="btn-outline">Simpan</button>
      </form>

      <ol className="space-y-3">
        {feedback.messages.map((message) => (
          <li key={message.id}
              className={`card ${message.fromStaff ? "ml-8 border-primary/40 bg-primary-container/40" : "mr-8"}`}>
            <p className="text-xs text-muted">
              {message.fromStaff ? `${message.authorName} (tim)` : message.authorName} · {formatDateTime(message.createdAt)}
            </p>
            <p className="mt-2 whitespace-pre-wrap">{message.body}</p>
          </li>
        ))}
      </ol>

      {feedback.status === "closed"
        ? <p className="text-sm text-muted">Percakapan ditutup. Ubah status untuk membalas lagi.</p>
        : <ReplyForm id={feedback.id} />}
    </div>
  );
}
