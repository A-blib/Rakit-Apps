"use client";

import { useActionState } from "react";
import { replyFeedback, type FormState } from "../../../actions";

export function ReplyForm({ id }: { id: string }) {
  const [state, action, pending] = useActionState<FormState, FormData>(replyFeedback.bind(null, id), {});
  return (
    <form action={action} className="space-y-2">
      <label className="label" htmlFor="body">Balas sebagai Tim OrgSpace</label>
      <textarea className="textarea" id="body" name="body" maxLength={4000} required />
      {(state.fieldErrors?.body || state.error) && (
        <p role="alert" className="text-sm text-error">{state.fieldErrors?.body ?? state.error}</p>
      )}
      <button className="btn-primary" disabled={pending}>{pending ? "Mengirim..." : "Kirim balasan"}</button>
    </form>
  );
}
