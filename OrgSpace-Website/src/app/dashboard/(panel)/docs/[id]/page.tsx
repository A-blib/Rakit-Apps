import Link from "next/link";
import { notFound } from "next/navigation";
import { adminApi } from "@/lib/api/admin";
import { ApiError } from "@/lib/api/http";
import { adminToken } from "@/lib/auth/session";
import { deleteDoc } from "../../../actions";
import { DocForm } from "../DocForm";

export default async function EditDocPage({ params }: PageProps<"/dashboard/docs/[id]">) {
  const { id } = await params;
  let doc;
  try {
    doc = await adminApi.doc(await adminToken(), id);
  } catch (error) {
    if (error instanceof ApiError && error.code === "DOC_NOT_FOUND") notFound();
    throw error;
  }
  return (
    <div className="space-y-6">
      <Link href="/dashboard/docs" className="link text-sm">← Dokumentasi</Link>
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Ubah artikel</h1>
        <form action={deleteDoc.bind(null, doc.id)}>
          <button className="btn-outline text-error">Hapus</button>
        </form>
      </header>
      <DocForm doc={doc} />
    </div>
  );
}
