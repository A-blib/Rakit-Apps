/* Creative Crew — interaksi template tanpa library.
   Data modul diisi OrgSpace SETELAH script ini jalan, jadi fitur yang butuh data menunggu event "orgspace:siap".
   Di pratinjau/laptop (tanpa OrgSpace) fitur yang sama jalan dengan isi contoh template. */
(function () {
  "use strict";
  var root = document.documentElement;
  root.classList.add("js");
  var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var each = function (list, fn) { Array.prototype.forEach.call(list, fn); };

  function onReady(fn) {
    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", fn);
    else fn();
  }

  /* ================= Tanpa data: menu, header, form ================= */
  onReady(function () {
    var burger = document.getElementById("burger");
    var menu = document.getElementById("menu");
    function setMenu(open) {
      document.body.classList.toggle("menu-open", open);
      if (burger) {
        burger.setAttribute("aria-expanded", String(open));
        burger.setAttribute("aria-label", open ? "Tutup menu" : "Buka menu");
      }
    }
    if (burger) burger.addEventListener("click", function () { setMenu(!document.body.classList.contains("menu-open")); });
    if (menu) menu.addEventListener("click", function (e) { if (e.target.closest("a")) setMenu(false); });
    document.addEventListener("keydown", function (e) { if (e.key === "Escape") setMenu(false); });

    var topbar = document.querySelector(".topbar");
    function onScroll() { if (topbar) topbar.classList.toggle("scrolled", window.scrollY > 24); }
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();

    // Tenggat waktu kolaborasi tidak boleh di masa lalu.
    var deadline = document.getElementById("k-deadline");
    if (deadline) deadline.min = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);

    var brief = document.getElementById("k-brief");
    var counter = document.getElementById("brief-hitung");
    if (brief && counter) {
      var update = function () { counter.textContent = brief.value.length; };
      brief.addEventListener("input", update);
      brief.form.addEventListener("reset", function () { setTimeout(update, 0); });
    }

    // OrgSpace mengosongkan form setelah berhasil terkirim: saat itulah pop-up terima kasih ditampilkan.
    var thanks = document.getElementById("terima-kasih");
    var collabForm = document.getElementById("form-kolaborasi");
    if (thanks && collabForm && typeof thanks.showModal === "function") {
      var sent = false;
      collabForm.addEventListener("submit", function () { sent = true; });
      collabForm.addEventListener("reset", function () {
        if (!sent) return;
        sent = false;
        thanks.showModal();
      });
      thanks.addEventListener("click", function (e) {
        if (e.target === thanks || e.target.closest("[data-close]")) thanks.close();
      });
    }
  });

  /* ================= Fitur yang butuh data modul ================= */
  var started = false;
  function start() {
    if (started) return;
    started = true;
    var data = window.orgspace && window.orgspace.data ? window.orgspace.data : null;
    var rowsOf = function (name) { return data && data.collections ? data.collections[name] || [] : null; };
    // Pratinjau tanpa OrgSpace: kartu contoh tampil, jadi pesan "belum ada data" disembunyikan.
    if (!data) each(document.querySelectorAll("[data-ws-empty]"), function (el) { el.hidden = true; });

    hideEmptyFields(rowsOf);
    linkWorks(rowsOf("karya"));
    showCase(rowsOf("karya"));
    markGear();
    fillServiceOptions(rowsOf("layanan"));
    hideEmptyGroups();
    buildFilters();
    observeReveal();
    countUp();
  }
  document.addEventListener("orgspace:siap", start);
  onReady(function () { setTimeout(start, 0); });

  function visibleItems(container) {
    return Array.prototype.filter.call(container.querySelectorAll("[data-ws-item]"), function (item) {
      return item.style.display !== "none";
    });
  }

  /* Field opsional yang tidak diisi pengurus disembunyikan (OrgSpace membiarkan isi contoh template). */
  var OPTIONAL = {
    karya: ["klien", "tahun", "kredit", "link_video", "foto_2", "foto_3", "foto_4"],
    kru: ["link_portofolio", "instagram", "peran"],
    alat: ["lokasi", "peminjam", "tanggal_kembali"],
    layanan: ["estimasi"]
  };
  function hideEmptyFields(rowsOf) {
    Object.keys(OPTIONAL).forEach(function (name) {
      var rows = rowsOf(name);
      if (!rows) return;
      each(document.querySelectorAll('[data-ws-collection="' + name + '"]'), function (list) {
        visibleItems(list).forEach(function (item, i) {
          var row = rows[i] || {};
          OPTIONAL[name].forEach(function (field) {
            if (String(row[field] == null ? "" : row[field]).trim()) return;
            each(item.querySelectorAll('[data-ws-field="' + field + '"], [data-ws-link="' + field + '"]'), function (el) {
              // Sembunyikan baris/label pembungkusnya juga, mis. "Klien: ..." atau "Estimasi · ...".
              var wrap = el.closest("dl > div, .eta") || el;
              wrap.hidden = true;
            });
          });
          each(item.querySelectorAll(".member-links, .case-gallery"), function (group) {
            group.hidden = !Array.prototype.some.call(group.children, function (c) { return !c.hidden; });
          });
        });
      });
    });
  }

  /* Kartu karya mengarah ke halaman detail karya.html?id=... */
  function linkWorks(rows) {
    if (!rows) return;
    each(document.querySelectorAll('[data-ws-collection="karya"]'), function (list) {
      if (list.id === "detail-karya") return;
      visibleItems(list).forEach(function (item, i) {
        var link = item.matches("a") ? item : item.querySelector("a");
        if (link && rows[i] && rows[i].id) link.href = "karya.html?id=" + encodeURIComponent(rows[i].id);
      });
    });
  }

  /* Halaman detail: tampilkan satu karya sesuai ?id=, plus navigasi karya sebelum/berikutnya. */
  function showCase(rows) {
    var list = document.getElementById("detail-karya");
    if (!list || !rows || !rows.length) return;
    var items = visibleItems(list);
    var id = new URLSearchParams(window.location.search).get("id");
    var index = 0;
    rows.forEach(function (row, i) { if (row.id === id) index = i; });
    items.forEach(function (item, i) { item.hidden = i !== index; });
    // Karya ini langsung terlihat, jadi gambarnya tidak perlu menunggu lazy load.
    if (items[index]) each(items[index].querySelectorAll("img"), function (img) { img.loading = "eager"; });
    var title = rows[index].judul;
    if (title) document.title = title + " — " + document.title.split(" — ").pop();

    var nav = document.getElementById("case-nav");
    if (!nav || rows.length < 2) return;
    var setLink = function (el, row) {
      el.href = "karya.html?id=" + encodeURIComponent(row.id);
      el.querySelector("strong").textContent = row.judul || "Karya";
    };
    setLink(document.getElementById("karya-sebelum"), rows[(index - 1 + rows.length) % rows.length]);
    setLink(document.getElementById("karya-sesudah"), rows[(index + 1) % rows.length]);
    nav.hidden = false;
  }

  /* Gear: warna status, jumlah per status, dan tanda terlambat dikembalikan. */
  function markGear() {
    var list = document.querySelector('[data-ws-collection="alat"]');
    if (!list) return;
    var counts = { ok: 0, out: 0, fix: 0 };
    var today = new Date();
    today.setHours(0, 0, 0, 0);
    var raw = window.orgspace && window.orgspace.data ? (window.orgspace.data.collections.alat || []) : [];
    visibleItems(list).forEach(function (item, i) {
      var badge = item.querySelector('[data-ws-field="status"]');
      var status = badge ? badge.textContent.trim().toLowerCase() : "";
      var kind = /pinjam|keluar|dipakai/.test(status) ? "out" : /rawat|maint|rusak|servis|perbaik/.test(status) ? "fix" : "ok";
      counts[kind]++;
      item.classList.add(kind === "ok" ? "is-ok" : kind === "out" ? "is-out" : "is-fix");
      var back = raw[i] && raw[i].tanggal_kembali;
      if (kind === "out" && /^\d{4}-\d{2}-\d{2}$/.test(back || "") && new Date(back + "T00:00:00") < today) {
        var late = document.createElement("span");
        late.className = "late";
        late.textContent = "Terlambat dikembalikan";
        item.appendChild(late);
      }
    });
    var set = function (id, n) { var el = document.getElementById(id); if (el) el.textContent = n; };
    set("jumlah-tersedia", counts.ok);
    set("jumlah-dipinjam", counts.out);
    set("jumlah-perawatan", counts.fix);
  }

  /* Pilihan "Jenis layanan" di form mengikuti modul layanan yang diisi pengurus. */
  function fillServiceOptions(rows) {
    var select = document.getElementById("k-layanan");
    if (!select || !rows || !rows.length) return;
    var names = rows.map(function (r) { return String(r.nama || "").trim(); }).filter(Boolean);
    if (!names.length) return;
    while (select.options.length > 1) select.remove(1);
    names.concat("Lainnya").forEach(function (name) { select.add(new Option(name, name)); });
  }

  /* Profil organisasi yang belum diisi disembunyikan OrgSpace; judul grupnya ikut disembunyikan. */
  function hideEmptyGroups() {
    each(document.querySelectorAll("[data-org-group]"), function (group) {
      var items = group.querySelectorAll("[data-ws-org]");
      group.hidden = items.length > 0 && Array.prototype.every.call(items, function (el) { return el.style.display === "none"; });
    });
  }

  /* Chip filter dari isi data: <div class="chips" data-filter="karya" data-filter-field="kategori">. */
  function buildFilters() {
    each(document.querySelectorAll(".chips[data-filter]"), function (chips) {
      var list = document.querySelector('[data-ws-collection="' + chips.getAttribute("data-filter") + '"]');
      if (!list) return;
      var field = chips.getAttribute("data-filter-field");
      var items = visibleItems(list);
      var valueOf = function (item) {
        var el = item.querySelector('[data-ws-field="' + field + '"]');
        return el ? el.textContent.trim() : "";
      };
      var values = [];
      var count = {};
      items.forEach(function (item) {
        var v = valueOf(item);
        if (!v) return;
        if (values.indexOf(v) < 0) values.push(v);
        count[v] = (count[v] || 0) + 1;
      });
      while (chips.firstChild) chips.removeChild(chips.firstChild);
      if (values.length < 2) { chips.hidden = true; return; }
      ["Semua"].concat(values).forEach(function (v, i) {
        var chip = document.createElement("button");
        chip.type = "button";
        chip.className = "chip";
        chip.textContent = i === 0 ? "All" : v;
        var n = document.createElement("span");
        n.className = "count";
        n.textContent = i === 0 ? items.length : count[v];
        chip.appendChild(n);
        chip.setAttribute("aria-pressed", String(i === 0));
        chip.addEventListener("click", function () {
          each(chips.children, function (c) { c.setAttribute("aria-pressed", String(c === chip)); });
          items.forEach(function (item) {
            item.hidden = i !== 0 && valueOf(item) !== v;
            if (!item.hidden) item.classList.add("in");
          });
        });
        chips.appendChild(chip);
      });
    });
  }

  /* Muncul halus saat di-scroll; kartu hasil isian modul ikut diamati. */
  function observeReveal() {
    var items = document.querySelectorAll(".reveal:not(.in)");
    if (reduceMotion || !("IntersectionObserver" in window)) {
      each(items, function (el) { el.classList.add("in"); });
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) { entry.target.classList.add("in"); io.unobserve(entry.target); }
      });
    }, { threshold: 0.12 });
    each(items, function (el, i) {
      el.style.transitionDelay = Math.min(i % 4, 3) * 70 + "ms";
      io.observe(el);
    });
  }

  /* Angka statistik naik dari 0 saat terlihat (angka dari database, setelah modul terisi). */
  function countUp() {
    if (reduceMotion || !("IntersectionObserver" in window)) return;
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        io.unobserve(entry.target);
        var el = entry.target;
        var target = parseInt(el.textContent.replace(/\D/g, ""), 10) || 0;
        if (!target) return;
        var t0 = null;
        var frame = function (t) {
          if (t0 === null) t0 = t;
          var p = Math.min((t - t0) / 1400, 1);
          el.textContent = String(Math.round(target * (1 - Math.pow(1 - p, 3))));
          if (p < 1) requestAnimationFrame(frame);
        };
        requestAnimationFrame(frame);
      });
    }, { threshold: 0.6 });
    each(document.querySelectorAll("[data-ws-stat]"), function (el) { io.observe(el); });
  }
})();
