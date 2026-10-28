// Jalankan: npm test (node --test, tanpa library tambahan).
import assert from "node:assert/strict";
import test from "node:test";
import { bottomNav, buildNav, cardFields, fieldGroups, humanize, mix, themeCss } from "./portal-config.ts";

const ids = (groups: ReturnType<typeof buildNav>) => groups.map((g) => [g.title ?? "", g.items.map((i) => i.id)]);

test("tanpa portal.json: menu bawaan lalu setiap modul jadi menu sendiri", () => {
  const groups = buildNav({}, ["agenda", "program_kerja"], [], true);
  assert.deepEqual(ids(groups), [
    ["", ["overview", "submissions", "news"]],
    ["Data Website", ["mod:agenda", "mod:program_kerja"]],
  ]);
  assert.equal(groups[1].items[1].label, "Program Kerja");
});

test("nav provider dipakai; menu yang tidak disebut masuk Lainnya; Kotak Masuk hilang untuk Editor", () => {
  const groups = buildNav(
    {
      nav: [
        { module: "agenda", label: "Jadwal", icon: "calendar" },
        { group: "Profil", items: [{ module: "struktur" }, { builtin: "submissions" }] },
        { module: "tidak-ada" },
      ],
    },
    ["agenda", "struktur", "galeri"],
    [],
    false
  );
  assert.deepEqual(ids(groups), [
    ["", ["mod:agenda"]],
    ["Profil", ["mod:struktur"]],
    ["Lainnya", ["overview", "news", "mod:galeri"]],
  ]);
  assert.equal(groups[0].items[0].label, "Jadwal");
});

test("data modul yang sudah tidak ada di template tetap punya menu", () => {
  const groups = buildNav({}, ["agenda"], ["agenda", "alumni"], true);
  assert.deepEqual(groups.at(-1)?.items.map((i) => i.id), ["mod:alumni"]);
});

test("menu bawah HP: pilihan provider, maksimal 4", () => {
  const groups = buildNav({}, ["a", "b", "c"], [], true);
  assert.deepEqual(bottomNav({ mobile: { bottom: ["b", "overview"] } }, groups).map((l) => l.id), ["mod:b", "overview"]);
  assert.equal(bottomNav({}, groups).length, 4);
});

test("grup field: sisa field dikumpulkan di akhir", () => {
  assert.deepEqual(fieldGroups({ groups: [{ title: "Utama", fields: ["nama", "x"] }] }, ["nama", "foto", "lokasi"]), [
    { title: "Utama", fields: ["nama"] },
    { title: "Lainnya", fields: ["foto", "lokasi"] },
  ]);
  assert.deepEqual(fieldGroups(undefined, ["nama"]), [{ title: undefined, fields: ["nama"] }]);
});

test("kartu memilih judul, subjudul, dan gambar otomatis", () => {
  assert.deepEqual(cardFields(undefined, { foto: "gambar", nama: "teks", tanggal: "tanggal", link: "link" }), {
    title: "nama",
    subtitle: "tanggal",
    image: "foto",
  });
});

test("tema hanya menerima warna hex dan radius yang wajar", () => {
  assert.equal(themeCss({ primary: "red;}body{display:none" }), "");
  const css = themeCss({ primary: "#1E40AF", radius: 6 });
  assert.match(css, /^:root\{--primary:#1E40AF;/);
  assert.match(css, /--radius-xl:6px/);
  // Hex biasa (tanpa color-mix) agar jalan di Chrome lama.
  assert.match(css, /:root\.dark\{--primary:#[0-9A-F]{6};/);
  assert.doesNotMatch(css, /color-mix/);
  assert.equal(humanize("nama_kegiatan"), "Nama Kegiatan");
});

test("campuran warna dihitung sebagai hex", () => {
  assert.equal(mix("#000000", "#FFFFFF", 0.5), "#808080");
  assert.equal(mix("#1E40AF", "#FFFFFF", 1), "#1E40AF");
  assert.equal(mix("#1E40AF", "#FFFFFF", 0), "#FFFFFF");
});
