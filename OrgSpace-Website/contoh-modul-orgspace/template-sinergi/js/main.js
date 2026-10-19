/* Sinergi — interaksi template. Tanpa library: menu HP, tema gelap, animasi scroll, hitung angka statistik,
   filter kategori, hitung mundur agenda, lightbox galeri, slider testimoni, form 2 langkah, dan pencarian pengurus.
   Data modul diisi OrgSpace SETELAH script ini jalan, jadi fitur yang butuh data menunggu event "orgspace:siap". */
(function () {
  "use strict";
  var root = document.documentElement;
  root.classList.add("js");
  var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  /* ---------- Tema terang/gelap ---------- */
  var THEME_KEY = "sinergi-tema";
  function readTheme() {
    try { return localStorage.getItem(THEME_KEY); } catch (e) { return null; }
  }
  function applyTheme(theme) {
    root.setAttribute("data-theme", theme);
    try { localStorage.setItem(THEME_KEY, theme); } catch (e) { /* penyimpanan tidak tersedia */ }
  }
  applyTheme(readTheme() || (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light"));

  function onReady(fn) {
    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", fn);
    else fn();
  }

  onReady(function () {
    var themeBtn = document.getElementById("tema");
    if (themeBtn) themeBtn.addEventListener("click", function () {
      applyTheme(root.getAttribute("data-theme") === "dark" ? "light" : "dark");
    });

    /* ---------- Menu HP ---------- */
    var burger = document.getElementById("burger");
    var menu = document.getElementById("menu");
    function setMenu(open) {
      document.body.classList.toggle("menu-open", open);
      if (burger) burger.setAttribute("aria-expanded", String(open));
    }
    if (burger) burger.addEventListener("click", function () {
      setMenu(!document.body.classList.contains("menu-open"));
    });
    if (menu) menu.addEventListener("click", function (e) {
      if (e.target.closest("a")) setMenu(false);
    });
    document.addEventListener("keydown", function (e) { if (e.key === "Escape") setMenu(false); });

    /* ---------- Header, progress bar, tombol ke atas, menu aktif ---------- */
    var topbar = document.querySelector(".topbar");
    var progress = document.getElementById("progress");
    var toTop = document.getElementById("ke-atas");
    function onScroll() {
      var y = window.scrollY;
      var max = document.documentElement.scrollHeight - window.innerHeight;
      if (topbar) topbar.classList.toggle("scrolled", y > 8);
      if (progress) progress.style.width = (max > 0 ? (y / max) * 100 : 0) + "%";
      if (toTop) toTop.classList.toggle("show", y > 600);
    }
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();
    if (toTop) toTop.addEventListener("click", function () { window.scrollTo({ top: 0, behavior: "smooth" }); });

    var navLinks = Array.prototype.slice.call(document.querySelectorAll('.nav a[href^="#"]'));
    if ("IntersectionObserver" in window && navLinks.length) {
      var spy = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
          if (!entry.isIntersecting) return;
          navLinks.forEach(function (a) { a.classList.toggle("active", a.getAttribute("href") === "#" + entry.target.id); });
        });
      }, { rootMargin: "-45% 0px -50% 0px" });
      navLinks.forEach(function (a) {
        var target = document.getElementById(a.getAttribute("href").slice(1));
        if (target) spy.observe(target);
      });
    }

    /* ---------- Efek mengetik di hero ---------- */
    var typing = document.getElementById("typing");
    if (typing && !reduceMotion) {
      var words = (typing.getAttribute("data-words") || "").split("|").filter(Boolean);
      var w = 0, c = words.length ? words[0].length : 0, deleting = true;
      var tick = function () {
        var word = words[w] || "";
        c += deleting ? -1 : 1;
        typing.textContent = word.slice(0, Math.max(c, 0));
        var wait = deleting ? 45 : 90;
        if (!deleting && c >= word.length) { deleting = true; wait = 1600; }
        else if (deleting && c <= 0) { deleting = false; w = (w + 1) % words.length; wait = 300; }
        setTimeout(tick, wait);
      };
      if (words.length > 1) setTimeout(tick, 2000);
    }

    /* ---------- Form pendaftaran 2 langkah + penghitung karakter ---------- */
    Array.prototype.forEach.call(document.querySelectorAll("form"), function (form) {
      var steps = form.querySelectorAll("fieldset.step");
      var dots = form.querySelectorAll(".steps span");
      if (steps.length) {
        var show = function (i) {
          Array.prototype.forEach.call(steps, function (s, k) { s.classList.toggle("on", k === i); });
          Array.prototype.forEach.call(dots, function (d, k) { d.classList.toggle("on", k <= i); });
        };
        form.addEventListener("click", function (e) {
          var current = Array.prototype.indexOf.call(steps, form.querySelector("fieldset.step.on"));
          if (e.target.closest("[data-next]")) {
            var fields = steps[current].querySelectorAll("input, select, textarea");
            for (var i = 0; i < fields.length; i++) {
              if (!fields[i].checkValidity()) { fields[i].reportValidity(); fields[i].classList.add("invalid"); return; }
              fields[i].classList.remove("invalid");
            }
            show(current + 1);
          } else if (e.target.closest("[data-prev]")) {
            show(current - 1);
          }
        });
        // OrgSpace mengosongkan form setelah terkirim: kembali ke langkah pertama.
        form.addEventListener("reset", function () { show(0); });
      }
      Array.prototype.forEach.call(form.querySelectorAll("[data-count-for]"), function (counter) {
        var input = document.getElementById(counter.getAttribute("data-count-for"));
        if (!input) return;
        var max = input.getAttribute("maxlength");
        var update = function () { counter.textContent = input.value.length + (max ? "/" + max : ""); };
        input.addEventListener("input", update);
        form.addEventListener("reset", function () { setTimeout(update, 0); });
      });
    });

    /* ---------- Lightbox galeri ---------- */
    var box = document.getElementById("lightbox");
    if (box && typeof box.showModal === "function") {
      document.addEventListener("click", function (e) {
        var shot = e.target.closest(".shot");
        if (shot) {
          var img = shot.querySelector("img");
          var caption = shot.querySelector(".shot-caption");
          document.getElementById("lightbox-img").src = img.currentSrc || img.src;
          document.getElementById("lightbox-img").alt = img.alt;
          document.getElementById("lightbox-caption").textContent = caption ? caption.textContent : "";
          box.showModal();
        }
      });
      box.addEventListener("click", function (e) {
        if (e.target === box || e.target.closest("[data-close]")) box.close();
      });
    }

    /* ---------- Slider testimoni ---------- */
    var slider = document.getElementById("slider");
    Array.prototype.forEach.call(document.querySelectorAll("[data-slide]"), function (btn) {
      btn.addEventListener("click", function () {
        if (!slider) return;
        var card = slider.querySelector("[data-ws-item]");
        var step = card ? card.getBoundingClientRect().width + 20 : 320;
        slider.scrollBy({ left: step * Number(btn.getAttribute("data-slide")), behavior: "smooth" });
      });
    });

    /* ---------- Kartu prestasi: ketuk untuk membaca cerita lengkap ---------- */
    document.addEventListener("click", function (e) {
      var card = e.target.closest(".prize-card");
      if (card) card.classList.toggle("open");
    });
    document.addEventListener("keydown", function (e) {
      if (e.key === "Enter" && e.target.classList && e.target.classList.contains("prize-card")) e.target.classList.toggle("open");
    });
  });

  /* ================= Fitur yang butuh data modul ================= */
  var started = false;
  function start() {
    if (started) return;
    started = true;
    var data = window.orgspace && window.orgspace.data ? window.orgspace.data : null;
    hideEmptyGroups();
    buildFilters();
    markAgenda(data);
    setupSearch();
    observeReveal();
    countUp();
  }
  // Di OrgSpace event ini datang setelah modul terisi. Di pratinjau editor/laptop tidak ada data OrgSpace,
  // jadi DOMContentLoaded menjalankan fitur yang sama dengan isi contoh template.
  document.addEventListener("orgspace:siap", start);
  onReady(function () { setTimeout(start, 0); });

  function visibleItems(container) {
    return Array.prototype.filter.call(container.querySelectorAll("[data-ws-item]"), function (item) {
      return item.style.display !== "none";
    });
  }

  /* Muncul halus saat di-scroll. Kartu hasil isian modul ikut diamati. */
  function observeReveal() {
    var items = document.querySelectorAll(".reveal:not(.in)");
    if (reduceMotion || !("IntersectionObserver" in window)) {
      Array.prototype.forEach.call(items, function (el) { el.classList.add("in"); });
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) { entry.target.classList.add("in"); io.unobserve(entry.target); }
      });
    }, { threshold: 0.12 });
    Array.prototype.forEach.call(items, function (el, i) {
      el.style.transitionDelay = Math.min(i % 4, 3) * 80 + "ms";
      io.observe(el);
    });
  }

  /* Angka statistik naik dari 0 saat terlihat. */
  function countUp() {
    var nums = document.querySelectorAll("[data-ws-stat]");
    var run = function (el) {
      var target = parseInt(el.textContent.replace(/\D/g, ""), 10) || 0;
      if (reduceMotion || target === 0) return;
      var t0 = null;
      var frame = function (t) {
        if (t0 === null) t0 = t;
        var p = Math.min((t - t0) / 1200, 1);
        el.textContent = String(Math.round(target * (1 - Math.pow(1 - p, 3))));
        if (p < 1) requestAnimationFrame(frame);
      };
      requestAnimationFrame(frame);
    };
    if (!("IntersectionObserver" in window)) return;
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) { run(entry.target); io.unobserve(entry.target); }
      });
    }, { threshold: 0.6 });
    Array.prototype.forEach.call(nums, function (el) { io.observe(el); });
  }

  /* Chip filter dibuat dari isi data: <div class="chips" data-filter="prestasi" data-filter-field="tingkat">. */
  function buildFilters() {
    Array.prototype.forEach.call(document.querySelectorAll(".chips[data-filter]"), function (chips) {
      var container = document.querySelector('[data-ws-collection="' + chips.getAttribute("data-filter") + '"]');
      if (!container) return;
      var field = chips.getAttribute("data-filter-field");
      var items = visibleItems(container);
      var valueOf = function (item) {
        var el = item.querySelector('[data-ws-field="' + field + '"]');
        return el ? el.textContent.trim() : "";
      };
      var values = [];
      items.forEach(function (item) {
        var v = valueOf(item);
        if (v && values.indexOf(v) < 0) values.push(v);
      });
      while (chips.firstChild) chips.removeChild(chips.firstChild);
      if (values.length < 2) return;
      ["Semua"].concat(values).forEach(function (v, i) {
        var chip = document.createElement("button");
        chip.type = "button";
        chip.className = "chip";
        chip.textContent = v;
        chip.setAttribute("aria-pressed", String(i === 0));
        chip.addEventListener("click", function () {
          Array.prototype.forEach.call(chips.children, function (c) { c.setAttribute("aria-pressed", String(c === chip)); });
          items.forEach(function (item) {
            item.hidden = i !== 0 && valueOf(item) !== v;
            if (!item.hidden) item.classList.add("in");
          });
        });
        chips.appendChild(chip);
      });
    });
  }

  /* Agenda: tandai yang sudah lewat, sorot agenda berikutnya, dan tampilkan hitung mundur di hero. */
  function markAgenda(data) {
    var list = document.querySelector('[data-ws-collection="agenda"]');
    var raw = data && data.collections ? data.collections.agenda || [] : [];
    if (!list || !raw.length) return;
    var items = visibleItems(list);
    var today = new Date();
    today.setHours(0, 0, 0, 0);
    var next = null;
    var dated = [];
    raw.forEach(function (row, i) {
      var item = items[i];
      var date = /^\d{4}-\d{2}-\d{2}$/.test(row.tanggal || "") ? new Date(row.tanggal + "T00:00:00") : null;
      if (!item || !date) return;
      dated.push({ date: date, item: item });
      if (date < today) item.classList.add("lewat");
      else if (!next || date < next.date) next = { date: date, item: item, row: row };
    });
    // Kartu agenda disusun menurut tanggal (terdekat di atas), apa pun urutan isian di Portal Pengurus.
    var empty = list.querySelector("[data-ws-empty]");
    dated.sort(function (a, b) { return a.date - b.date; }).forEach(function (d) { list.insertBefore(d.item, empty); });
    var label = document.getElementById("hitung-mundur");
    if (!next) {
      if (label) label.textContent = "semua agenda sudah selesai";
      return;
    }
    next.item.classList.add("berikutnya");
    var tag = document.createElement("span");
    tag.className = "status-tag";
    tag.textContent = "Berikutnya";
    var title = next.item.querySelector("h3");
    if (title) title.appendChild(tag);
    if (label) {
      var days = Math.round((next.date - today) / 86400000);
      label.textContent = (next.row.nama_kegiatan || "Kegiatan") + " · " + (days === 0 ? "hari ini" : days + " hari lagi");
    }
  }

  /* OrgSpace menyembunyikan profil organisasi yang belum diisi; judul grupnya (mis. "Hubungi kami") ikut disembunyikan. */
  function hideEmptyGroups() {
    Array.prototype.forEach.call(document.querySelectorAll("[data-org-group]"), function (group) {
      var items = group.querySelectorAll("[data-ws-org]");
      group.hidden = items.length > 0 && Array.prototype.every.call(items, function (el) { return el.style.display === "none"; });
    });
  }

  /* Pencarian pengurus di halaman Tentang. */
  function setupSearch() {
    var input = document.getElementById("cari-pengurus");
    var list = document.querySelector('[data-ws-collection="struktur"]');
    var none = document.getElementById("tidak-ketemu");
    if (!input || !list) return;
    var items = visibleItems(list);
    input.addEventListener("input", function () {
      var q = input.value.trim().toLowerCase();
      var shown = 0;
      items.forEach(function (item) {
        var match = item.textContent.toLowerCase().indexOf(q) >= 0;
        item.hidden = !match;
        if (match) { shown++; item.classList.add("in"); }
      });
      if (none) none.hidden = shown > 0;
    });
  }
})();
