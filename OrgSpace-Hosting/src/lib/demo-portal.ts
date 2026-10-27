import type { DemoLogin, PortalApi } from "@/components/PortalPengurus";
import type { TemplatePreviewData } from "./types";

/**
 * Akun Portal demo. Sengaja publik (ditampilkan di layar login): tidak ada di database, tidak membuka akun apa pun,
 * dan hanya dicek oleh createDemoApi di browser.
 */
export const DEMO_LOGIN: DemoLogin = { email: "demo@orgspace.app", password: "demo1234" };

type Row = Record<string, unknown> & { id: string };

const DAY = 24 * 60 * 60 * 1000;

function json(body: unknown, status = 200): Response {
  // Selalu diserialisasi ulang, jadi state React Portal tidak pernah memegang objek milik penyimpanan demo.
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}

/**
 * Pengganti API backend untuk Portal demo (/demo/{id}/admin). Semua data di memori tab ini: tidak ada request ke
 * backend, tidak ada token asli, dan tidak ada yang tersimpan. Bentuk balasan sama dengan endpoint Portal asli.
 */
export function createDemoApi(sample: TemplatePreviewData): PortalApi {
  const daysAgo = (days: number) => new Date(Date.now() - days * DAY).toISOString();
  let seq = 0;
  const newId = () => `demo-${++seq}`;

  const user = { id: "demo-pemilik", name: "Pengurus Demo", email: DEMO_LOGIN.email, role: "owner" };
  let news: Row[] = sample.news.map((n) => ({
    id: n.id,
    title: n.title,
    summary: n.summary,
    content: n.content,
    publishedAt: n.publishedAt,
  }));
  const collections: Record<string, Row[]> = {};
  for (const [name, items] of Object.entries(sample.collections)) {
    collections[name] = items.map((item) => ({ ...item, id: String(item.id ?? newId()) }));
  }
  const formNames = Object.keys(sample.stats).filter((k) => k.startsWith("form.")).map((k) => k.slice(5));
  const submissions = formNames.flatMap((formName) =>
    [1, 2, 3].map((i) => ({
      id: newId(),
      formName,
      data: { nama: `Pengunjung contoh ${i}`, whatsapp: `0800-0000-000${i}`, pesan: "Isian contoh dari formulir website." },
      createdAt: daysAgo(i),
    }))
  );
  let members = [
    { userId: user.id, name: user.name, email: user.email, role: "owner", positionTitle: "Ketua", joinedAt: daysAgo(90) },
    { userId: "demo-2", name: "Sekretaris Contoh", email: "sekretaris@example.com", role: "admin", positionTitle: "Sekretaris", joinedAt: daysAgo(60) },
    { userId: "demo-3", name: "Bendahara Contoh", email: "bendahara@example.com", role: "editor", positionTitle: "Bendahara", joinedAt: daysAgo(45) },
    { userId: "demo-4", name: "Humas Contoh", email: "humas@example.com", role: "editor", positionTitle: "Humas", joinedAt: daysAgo(30) },
  ];

  return async (path, init = {}) => {
    const method = (init.method || "GET").toUpperCase();
    const body = typeof init.body === "string" ? JSON.parse(init.body) : null;
    // path = "{slug}/auth/login", "{slug}/admin/news/{id}", ... atau "render/{slug}".
    const [first, area, resource, a, b] = path.split("?")[0].split("/");

    if (first === "render") {
      return json({ name: sample.name, organizationName: sample.org.nama, modules: sample.modules });
    }
    if (area === "auth" && resource === "login" && method === "POST") {
      const ok = String(body?.email).toLowerCase() === DEMO_LOGIN.email && body?.password === DEMO_LOGIN.password;
      return ok
        ? json({ token: "demo", user })
        : json({ message: "Email atau password demo salah. Pakai akun demo yang tertera di atas." }, 401);
    }
    if (area === "auth" && resource === "me") return json({ user });
    if (area !== "admin") return json({ message: "Tidak tersedia di demo." }, 404);

    switch (resource) {
      case "stats":
        return json({
          submissionsCount: submissions.length,
          newsCount: news.length,
          customItemsCount: Object.values(collections).reduce((sum, items) => sum + items.length, 0),
        });
      case "submissions":
        return json(submissions);
      case "news":
        if (method === "POST") {
          const item = { id: newId(), ...body, publishedAt: new Date().toLocaleDateString("id-ID", { day: "numeric", month: "long", year: "numeric" }) };
          news = [item, ...news];
          return json(item, 201);
        }
        if (method === "DELETE") {
          news = news.filter((n) => n.id !== a);
          return json({ message: "Berita dihapus." });
        }
        return json(news);
      case "collections": {
        if (!a) return json(collections);
        const items = (collections[a] ||= []);
        if (b === "order" && method === "PUT") {
          const ids: string[] = body?.ids || [];
          collections[a] = ids.map((id) => items.find((i) => i.id === id)).filter((i): i is Row => !!i);
          return json(collections[a]);
        }
        if (!b && method === "POST") {
          const item = { ...body, id: newId() };
          items.push(item);
          return json(item, 201);
        }
        const index = items.findIndex((i) => i.id === b);
        if (index < 0) return json({ message: "Item tidak ditemukan." }, 404);
        if (method === "PUT") {
          items[index] = { ...body, id: b };
          return json(items[index]);
        }
        if (method === "DELETE") {
          items.splice(index, 1);
          return json({ message: "Item dihapus." });
        }
        break;
      }
      case "media": {
        const file = init.body instanceof FormData ? init.body.get("file") : null;
        // Foto hanya tampil di tab ini (alamat blob:), tidak pernah diunggah ke mana pun.
        return file instanceof Blob ? json({ url: URL.createObjectURL(file) }) : json({ message: "Pilih file gambar." }, 400);
      }
      case "members": {
        if (!a) return json(members);
        if (a === user.id) return json({ message: "Peran pemilik tidak bisa diubah atau dicabut." }, 400);
        if (method === "PUT") members = members.map((m) => (m.userId === a ? { ...m, role: body?.role } : m));
        if (method === "DELETE") members = members.filter((m) => m.userId !== a);
        return json({ message: "Berhasil." });
      }
      case "invitations":
        return json({
          code: `DEMO-${Math.random().toString(36).slice(2, 8).toUpperCase()}`,
          role: body?.role,
          expiresAt: new Date(Date.now() + 7 * DAY).toISOString(),
        }, 201);
    }
    return json({ message: "Tidak tersedia di demo." }, 404);
  };
}
