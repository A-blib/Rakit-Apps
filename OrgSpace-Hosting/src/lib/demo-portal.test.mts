// Jalankan: npm test (node --test, tanpa library tambahan).
import assert from "node:assert/strict";
import test from "node:test";
import { createDemoApi, DEMO_LOGIN } from "./demo-portal.ts";

const sample = {
  templateId: "t",
  name: "Template Uji",
  news: [{ id: "n1", title: "Berita", summary: "", content: "", publishedAt: "1 Oktober 2026", imageUrl: null }],
  collections: { prestasi: [{ id: "contoh-1", judul: "A" }, { id: "contoh-2", judul: "B" }] },
  modules: { prestasi: { judul: "teks" as const } },
  stats: { "form.pendaftaran": 25 },
  org: { nama: "Organisasi Contoh" },
};
const post = (body: unknown, method = "POST") => ({ method, body: JSON.stringify(body) });

test("login hanya menerima akun demo", async () => {
  const api = createDemoApi(sample);
  assert.equal((await api("demo/auth/login", post({ username: "demo", password: "salah" }))).status, 401);
  const ok = await api("demo/auth/login", post(DEMO_LOGIN));
  assert.equal(ok.status, 200);
  assert.equal((await ok.json()).user.role, "owner");
});

test("koleksi bisa ditambah, diubah, diurutkan, dan dihapus di memori", async () => {
  const api = createDemoApi(sample);
  const added = await (await api("demo/admin/collections/prestasi", post({ judul: "C" }))).json();
  await api(`demo/admin/collections/prestasi/contoh-1`, post({ judul: "A2" }, "PUT"));
  await api("demo/admin/collections/prestasi/order", post({ ids: [added.id, "contoh-2", "contoh-1"] }, "PUT"));
  await api("demo/admin/collections/prestasi/contoh-2", { method: "DELETE" });
  const all = await (await api("demo/admin/collections")).json();
  assert.deepEqual(all.prestasi.map((i: { judul: string }) => i.judul), ["C", "A2"]);
  const stats = await (await api("demo/admin/stats")).json();
  assert.deepEqual(stats, { submissionsCount: 3, newsCount: 1, customItemsCount: 2 });
});

test("data tiap tab terpisah, dan tidak ada lagi kelola anggota di Portal", async () => {
  const api = createDemoApi(sample);
  assert.equal((await api("demo/admin/members")).status, 404);
  await api("demo/admin/news/n1", { method: "DELETE" });
  assert.equal((await (await api("demo/admin/news")).json()).length, 0);
  assert.equal((await (await createDemoApi(sample)("demo/admin/news")).json()).length, 1);
});
