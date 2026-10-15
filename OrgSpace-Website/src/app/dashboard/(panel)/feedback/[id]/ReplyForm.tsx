"use client";

import { useActionState } from "react";
import { replyFeedback, type FormState } from "../../../actions";

export function ReplyForm({ id }: { id: string }) {
  const [state, action, pending] = useActionState<FormState, FormData>(replyFeedback.bind(null, id), {});

  return (
    <form action={action} className="space-y-3 pt-2">
      <label className="label" htmlFor="body">
        Balas sebagai Tim OrgSpace
      </label>
      <textarea
        className="textarea"
        id="body"
        name="body"
        placeholder="Tulis tanggapan atau solusi untuk pengirim..."
        maxLength={4000}
        required
      />
      {(state.fieldErrors?.body || state.error) && (
        <p role="alert" className="text-xs text-rose-600 font-medium">
          {state.fieldErrors?.body ?? state.error}
        </p>
      )}
      <button type="submit" className="btn-primary" disabled={pending}>
        {pending ? "Mengirim Balasan..." : "Kirim Balasan"}
      </button>
    </form>
  );
}
