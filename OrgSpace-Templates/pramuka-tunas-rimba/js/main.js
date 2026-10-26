/* Pramuka Tunas Rimba — interaksi template tanpa library.
   Data modul diisi OrgSpace SETELAH script ini jalan, jadi fitur yang butuh data menunggu event "orgspace:siap".
   Di pratinjau/laptop (tanpa OrgSpace) fitur yang sama jalan dengan kartu contoh template. */
(function () {
  "use strict";
  document.documentElement.classList.add("js");
  var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var each = function (list, fn) { Array.prototype.forEach.call(list, fn); };
  var $ = function (id) { return document.getElementById(id); };
  var MONTHS = ["Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"];
  var DAYS = ["minggu", "senin", "selasa", "rabu", "kamis", "jumat", "sabtu"];

  function onReady(fn) {
    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", fn);
    else fn();
  }

  /* ================= Tanpa data: menu, header, form, pojok latihan ================= */
  onReady(function () {
    var burger = $("burger");
    var menu = $("menu");
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
    var heroBg = $("hero-bg");
    var toTop = $("to-top");
    var ticking = false;
    function onScroll() {
      ticking = false;
      var y = window.scrollY;
      if (topbar) topbar.classList.toggle("scrolled", y > 24);
      if (toTop) toTop.classList.toggle("show", y > 600);
      // Parallax: latar hero bergerak lebih lambat dari isi halaman.
      if (heroBg && !reduceMotion && y < window.innerHeight * 1.2) heroBg.style.transform = "translateY(" + (y * 0.3) + "px)";
    }
    window.addEventListener("scroll", function () {
      if (!ticking) { ticking = true; requestAnimationFrame(onScroll); }
    }, { passive: true });
    onScroll();
    if (toTop) toTop.addEventListener("click", function () { window.scrollTo({ top: 0, behavior: reduceMotion ? "auto" : "smooth" }); });

    setupChecklist();
    setupJoinForm();
    setupOath();
    setupDarma();
    setupMorse();
  });

  /* Checklist perlengkapan berkemah: hitung yang sudah dicentang. */
  function setupChecklist() {
    var list = $("checklist");
    var result = $("checklist-hasil");
    if (!list || !result) return;
    var boxes = list.querySelectorAll("input[type=checkbox]");
    function update() {
      var done = Array.prototype.filter.call(boxes, function (b) { return b.checked; }).length;
      result.textContent = done === boxes.length ? "Ransel siap! Selamat berkemah." : done + " dari " + boxes.length + " perlengkapan siap";
    }
    each(boxes, function (b) { b.addEventListener("change", update); });
    update();
  }

  /* Formulir pendaftaran: hitung karakter, saran golongan dari umur, dan pop-up terima kasih. */
  function setupJoinForm() {
    var form = $("form-daftar");
    if (!form) return;
    var reason = $("d-alasan");
    var counter = $("alasan-hitung");
    if (reason && counter) {
      var count = function () { counter.textContent = reason.value.length; };
      reason.addEventListener("input", count);
      form.addEventListener("reset", function () { setTimeout(count, 0); });
    }
    var birth = $("d-lahir");
    var rank = $("d-golongan");
    var hint = $("golongan-saran");
    if (birth && rank && hint) {
      birth.max = new Date().toISOString().slice(0, 10);
      birth.addEventListener("change", function () {
        var d = new Date(birth.value + "T00:00:00");
        if (isNaN(d)) return;
        var now = new Date();
        var age = now.getFullYear() - d.getFullYear() - (now < new Date(now.getFullYear(), d.getMonth(), d.getDate()) ? 1 : 0);
        var name = age <= 10 ? "Siaga" : age <= 15 ? "Penggalang" : age <= 20 ? "Penegak" : "Pandega";
        hint.textContent = "Usia " + age + " tahun: golongan " + name + ".";
        if (!rank.value) rank.value = name;
      });
    }
    // OrgSpace mengosongkan form setelah berhasil terkirim: saat itulah pop-up terima kasih ditampilkan.
    var thanks = $("terima-kasih");
    if (thanks && typeof thanks.showModal === "function") {
      var sent = false;
      form.addEventListener("submit", function () { sent = true; });
      form.addEventListener("reset", function () {
        if (!sent) return;
        sent = false;
        thanks.showModal();
      });
      thanks.addEventListener("click", function (e) {
        if (e.target === thanks || e.target.closest("[data-close]")) thanks.close();
      });
    }
  }

  /* Tri Satya: tab Penggalang vs Penegak & Pandega. */
  function setupOath() {
    var tabs = document.querySelectorAll('.tabs [role="tab"]');
    each(tabs, function (tab) {
      tab.addEventListener("click", function () {
        each(tabs, function (t) {
          var active = t === tab;
          t.setAttribute("aria-selected", String(active));
          var panel = $(t.getAttribute("aria-controls"));
          if (panel) panel.hidden = !active;
        });
      });
    });
  }

  /* Dasa Darma: kartu bisa dibalik + kuis "darma ke berapa?". Teks soal dibaca dari kartu, jadi ikut isi editan. */
  function setupDarma() {
    var cards = document.querySelectorAll(".darma-card");
    each(cards, function (card) {
      card.addEventListener("click", function () {
        card.setAttribute("aria-pressed", String(card.getAttribute("aria-pressed") !== "true"));
      });
    });
    var question = $("quiz-question");
    var options = $("quiz-options");
    var next = $("quiz-next");
    var score = $("quiz-score");
    if (!question || !options || !cards.length) return;
    var right = 0, total = 0, answer = 0, answered = false, last = -1;
    var buttons = options.querySelectorAll("button");
    function ask() {
      var n = cards.length;
      do { answer = 1 + Math.floor(Math.random() * n); } while (answer === last && n > 1);
      last = answer;
      var text = $("darma-" + answer);
      question.textContent = "“" + (text ? text.textContent.trim() : "") + "” adalah darma ke berapa?";
      var picks = [answer];
      while (picks.length < Math.min(buttons.length, n)) {
        var p = 1 + Math.floor(Math.random() * n);
        if (picks.indexOf(p) < 0) picks.push(p);
      }
      picks.sort(function () { return Math.random() - 0.5; });
      each(buttons, function (b, i) {
        b.className = "";
        b.disabled = false;
        b.hidden = i >= picks.length;
        b.textContent = "Darma ke-" + picks[i];
        b.dataset.value = picks[i];
      });
      answered = false;
    }
    each(buttons, function (b) {
      b.addEventListener("click", function () {
        if (answered) return;
        answered = true;
        total++;
        var ok = Number(b.dataset.value) === answer;
        if (ok) right++;
        each(buttons, function (x) {
          x.disabled = true;
          if (Number(x.dataset.value) === answer) x.className = "right";
        });
        if (!ok) b.className = "wrong";
        score.textContent = "Skor " + right + " dari " + total + (ok ? " · Tepat!" : " · Jawabannya darma ke-" + answer);
      });
    });
    if (next) next.addEventListener("click", ask);
    ask();
  }

  /* Sandi Morse: terjemahan langsung, bunyi lewat Web Audio, dan lampu isyarat. */
  var MORSE = {
    A: ".-", B: "-...", C: "-.-.", D: "-..", E: ".", F: "..-.", G: "--.", H: "....", I: "..", J: ".---", K: "-.-",
    L: ".-..", M: "--", N: "-.", O: "---", P: ".--.", Q: "--.-", R: ".-.", S: "...", T: "-", U: "..-", V: "...-",
    W: ".--", X: "-..-", Y: "-.--", Z: "--..", 0: "-----", 1: ".----", 2: "..---", 3: "...--", 4: "....-",
    5: ".....", 6: "-....", 7: "--...", 8: "---..", 9: "----."
  };
  function toMorse(text) {
    return text.toUpperCase().split(" ").filter(Boolean).map(function (word) {
      return word.split("").map(function (c) { return MORSE[c] || ""; }).filter(Boolean).join(" ");
    }).filter(Boolean).join(" / ");
  }
  function setupMorse() {
    var input = $("morse-input");
    var output = $("morse-output");
    var play = $("morse-play");
    var lamp = $("morse-lamp");
    if (!input || !output) return;
    function render() { output.textContent = toMorse(input.value) || "…"; }
    input.addEventListener("input", render);
    render();

    var audio = null;
    var playing = false;
    var UNIT = 0.09;
    function signal(code) {
      if (playing) return;
      var Ctx = window.AudioContext || window.webkitAudioContext;
      if (Ctx && !audio) audio = new Ctx();
      playing = true;
      if (play) play.disabled = true;
      var t = audio ? audio.currentTime + 0.05 : 0;
      var start = t;
      var flashes = [];
      code.split("").forEach(function (s) {
        if (s === "." || s === "-") {
          var len = (s === "." ? 1 : 3) * UNIT;
          if (audio) {
            var osc = audio.createOscillator();
            var gain = audio.createGain();
            osc.frequency.value = 620;
            gain.gain.setValueAtTime(0, t);
            gain.gain.linearRampToValueAtTime(0.25, t + 0.005);
            gain.gain.setValueAtTime(0.25, t + len - 0.005);
            gain.gain.linearRampToValueAtTime(0, t + len);
            osc.connect(gain).connect(audio.destination);
            osc.start(t);
            osc.stop(t + len);
          }
          flashes.push([t - start, len]);
          t += len + UNIT;
        } else if (s === " ") {
          t += UNIT * 2;
        } else if (s === "/") {
          t += UNIT * 4;
        }
      });
      flashes.forEach(function (f) {
        setTimeout(function () { if (lamp) lamp.classList.add("on"); }, f[0] * 1000 + 50);
        setTimeout(function () { if (lamp) lamp.classList.remove("on"); }, (f[0] + f[1]) * 1000 + 50);
      });
      setTimeout(function () {
        playing = false;
        if (play) play.disabled = false;
      }, (t - start) * 1000 + 120);
    }
    if (play) play.addEventListener("click", function () { signal(toMorse(input.value)); });
    var table = $("morse-table");
    if (table) table.addEventListener("click", function (e) {
      var btn = e.target.closest("[data-letter]");
      if (!btn) return;
      var letter = btn.getAttribute("data-letter");
      input.value = (input.value + letter).slice(0, 60);
      render();
      signal(MORSE[letter]);
    });
  }

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
    hideEmptyNewsPhotos(data ? data.news : null);
    hideEmptyGroups();
    setupAgenda(rowsOf("agenda"));
    markToday();
    setupAwards();
    buildFilters();
    setupLightbox();
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
    agenda: ["waktu", "lokasi", "golongan", "deskripsi"],
    jadwal: ["golongan", "tempat"],
    prestasi: ["peraih", "deskripsi", "tingkat"],
    pembina: ["keterangan"],
    galeri: ["kategori"]
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
            each(item.querySelectorAll('[data-ws-field="' + field + '"]'), function (el) { el.hidden = true; });
          });
        });
      });
    });
  }

  /* Berita tanpa foto: area gambar disembunyikan, bukan menampilkan foto contoh template. */
  var NEWS = ["berita", "kegiatan", "pengumuman"];
  function hideEmptyNewsPhotos(news) {
    if (!news) return;
    each(document.querySelectorAll("[data-ws-list],[data-ws-collection]"), function (list) {
      var name = (list.getAttribute("data-ws-list") || list.getAttribute("data-ws-collection") || "").toLowerCase();
      if (NEWS.indexOf(name) < 0) return;
      visibleItems(list).forEach(function (item, i) {
        if (String((news[i] || {}).foto || "").trim()) return;
        var media = item.querySelector(".media");
        if (media) media.hidden = true;
      });
    });
  }

  /* Profil organisasi yang belum diisi disembunyikan OrgSpace; judul grupnya ikut disembunyikan. */
  function hideEmptyGroups() {
    each(document.querySelectorAll("[data-org-group]"), function (group) {
      var items = group.querySelectorAll("[data-ws-org]");
      group.hidden = items.length > 0 && Array.prototype.every.call(items, function (el) { return el.style.display === "none"; });
    });
  }

  /* Agenda: kotak tanggal, tanda "sudah lewat", filter, dan hitung mundur ke agenda terdekat. */
  function setupAgenda(rows) {
    var today = new Date();
    today.setHours(0, 0, 0, 0);
    var upcoming = [];
    each(document.querySelectorAll('[data-ws-collection="agenda"]'), function (list) {
      visibleItems(list).forEach(function (item, i) {
        // Data asli dari Portal (YYYY-MM-DD); di pratinjau pakai atribut datetime kartu contoh.
        var sample = item.querySelector("time[datetime]");
        var raw = rows ? String((rows[i] && rows[i].tanggal) || "") : sample ? sample.getAttribute("datetime") : "";
        var date = /^\d{4}-\d{2}-\d{2}$/.test(raw) ? new Date(raw + "T00:00:00") : null;
        var box = item.querySelector(".agenda-date");
        if (!date) { if (box) box.hidden = true; return; }
        box.querySelector(".day").textContent = date.getDate();
        box.querySelector(".month").textContent = MONTHS[date.getMonth()] + " " + date.getFullYear();
        var past = date < today;
        item.classList.toggle("past", past);
        item.dataset.when = past ? "past" : "soon";
        if (!past) {
          var title = item.querySelector('[data-ws-field="judul"]');
          var time = item.querySelector('[data-ws-field="waktu"]');
          var hour = time && !time.hidden ? /(\d{1,2})[.:](\d{2})/.exec(time.textContent) : null;
          if (hour) date.setHours(Number(hour[1]), Number(hour[2]));
          upcoming.push({ date: date, title: title ? title.textContent.trim() : "Agenda" });
        }
      });
    });
    setupAgendaFilter();
    upcoming.sort(function (a, b) { return a.date - b.date; });
    startCountdown(upcoming[0]);
  }

  function setupAgendaFilter() {
    var group = $("agenda-filter");
    var list = document.querySelector('#daftar-agenda [data-ws-collection="agenda"]');
    if (!group || !list) return;
    var items = visibleItems(list);
    var buttons = group.querySelectorAll("button");
    function apply(when) {
      each(buttons, function (b) { b.setAttribute("aria-pressed", String(b.dataset.when === when)); });
      items.forEach(function (item) {
        item.hidden = when !== "all" && item.dataset.when !== when;
        if (!item.hidden) item.classList.add("in");
      });
    }
    each(buttons, function (b) { b.addEventListener("click", function () { apply(b.dataset.when); }); });
    var hasSoon = items.some(function (item) { return item.dataset.when === "soon"; });
    apply(hasSoon || !items.length ? "soon" : "all");
  }

  function startCountdown(next) {
    var box = $("countdown");
    if (!box) return;
    var title = $("countdown-title");
    var when = $("countdown-when");
    var parts = { d: $("cd-hari"), h: $("cd-jam"), m: $("cd-menit"), s: $("cd-detik") };
    if (!next) {
      if (when) when.textContent = "Belum ada agenda terjadwal. Pantau terus halaman agenda, ya!";
      return;
    }
    if (title) title.textContent = next.title;
    if (when) when.textContent = next.date.toLocaleDateString("id-ID", { weekday: "long", day: "numeric", month: "long", year: "numeric" });
    var pad = function (n) { return n < 10 ? "0" + n : String(n); };
    function tick() {
      var left = Math.max(0, next.date - new Date());
      var sec = Math.floor(left / 1000);
      parts.d.textContent = pad(Math.floor(sec / 86400));
      parts.h.textContent = pad(Math.floor(sec % 86400 / 3600));
      parts.m.textContent = pad(Math.floor(sec % 3600 / 60));
      parts.s.textContent = pad(sec % 60);
      if (left > 0) setTimeout(tick, 1000 - (Date.now() % 1000));
    }
    tick();
  }

  /* Jadwal latihan: tandai kartu yang harinya sama dengan hari ini. */
  function markToday() {
    var today = DAYS[new Date().getDay()];
    each(document.querySelectorAll('[data-ws-collection="jadwal"] [data-ws-item]'), function (item) {
      var day = item.querySelector('[data-ws-field="hari"]');
      var name = day ? day.textContent.toLowerCase().replace(/[^a-z]/g, "") : "";
      item.classList.toggle("today", name === today);
    });
  }

  /* Papan prestasi: jumlah tingkat kejuaraan dan tahun prestasi terbaru. */
  function setupAwards() {
    var list = document.querySelector('#papan [data-ws-collection="prestasi"]');
    if (!list) return;
    var items = visibleItems(list);
    var levels = {};
    var newest = 0;
    items.forEach(function (item) {
      var level = item.querySelector('[data-ws-field="tingkat"]');
      if (level && !level.hidden && level.textContent.trim()) levels[level.textContent.trim().toLowerCase()] = true;
      var year = item.querySelector('[data-ws-field="tahun"]');
      var y = year ? parseInt(year.textContent, 10) : 0;
      if (y > newest) newest = y;
    });
    var set = function (id, v) { var el = $(id); if (el) el.textContent = v; };
    set("jumlah-tingkat", Object.keys(levels).length);
    set("tahun-terbaru", newest || "–");
  }

  /* Chip filter dari isi data: <div class="chips" data-filter="galeri" data-filter-field="kategori">. */
  function buildFilters() {
    each(document.querySelectorAll(".chips[data-filter]"), function (chips) {
      var list = document.querySelector('[data-ws-collection="' + chips.getAttribute("data-filter") + '"]');
      if (!list) return;
      var field = chips.getAttribute("data-filter-field");
      var items = visibleItems(list);
      var valueOf = function (item) {
        var el = item.querySelector('[data-ws-field="' + field + '"]');
        return el && !el.hidden ? el.textContent.trim() : "";
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
        chip.textContent = v;
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

  /* Galeri: ketuk foto untuk memperbesar, geser dengan tombol atau panah keyboard. */
  function setupLightbox() {
    var box = $("lightbox");
    var list = document.querySelector('[data-ws-collection="galeri"]');
    if (!box || !list || typeof box.showModal !== "function") return;
    var img = $("lb-img");
    var caption = $("lb-caption");
    var index = 0;
    var shots = function () { return visibleItems(list).filter(function (s) { return !s.hidden; }); };
    function show(i) {
      var all = shots();
      if (!all.length) return;
      index = (i + all.length) % all.length;
      var shot = all[index];
      var src = shot.querySelector("img");
      var title = shot.querySelector('[data-ws-field="judul"]');
      img.src = src.currentSrc || src.src;
      img.alt = title ? title.textContent.trim() : "Foto kegiatan";
      caption.textContent = img.alt;
    }
    list.addEventListener("click", function (e) {
      var shot = e.target.closest("[data-ws-item]");
      if (!shot) return;
      show(shots().indexOf(shot));
      box.showModal();
    });
    list.addEventListener("keydown", function (e) {
      if (e.key !== "Enter" && e.key !== " ") return;
      var shot = e.target.closest("[data-ws-item]");
      if (!shot) return;
      e.preventDefault();
      show(shots().indexOf(shot));
      box.showModal();
    });
    $("lb-prev").addEventListener("click", function () { show(index - 1); });
    $("lb-next").addEventListener("click", function () { show(index + 1); });
    $("lb-close").addEventListener("click", function () { box.close(); });
    box.addEventListener("click", function (e) { if (e.target === box) box.close(); });
    box.addEventListener("keydown", function (e) {
      if (e.key === "ArrowLeft") show(index - 1);
      if (e.key === "ArrowRight") show(index + 1);
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
