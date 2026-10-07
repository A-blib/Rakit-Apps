/*
 * Mode tandai (alur-fitur-upload.md bagian 7). Disuntikkan ke salinan HTML bernomor (setiap elemen asli punya
 * data-tpl-id). JavaScript provider tetap berjalan; skrip ini hanya:
 *   - menangkap ketukan (link tidak berpindah halaman) dan melaporkan NOMOR elemen ke Java lewat RakitBridge,
 *   - menggambar sorotan & label di lapisan terpisah (elemen provider tidak diubah, kecuali header yang menempel),
 *   - menjawab pertanyaan Java lewat evaluateJavascript (describe, slide, suggest, ...).
 * Jembatan ke Java sengaja hanya menerima angka (bagian 7.9); semua data lain diminta Java.
 */
(function () {
  if (window.RakitMark) { return; }
  var ATTR = 'data-tpl-id';
  var layer = null;
  var selectedId = null;
  var marks = [];
  var slideRange = null;

  function byId(id) { return document.querySelector('[' + ATTR + '="' + id + '"]'); }
  function idOf(el) { var v = el && el.getAttribute ? el.getAttribute(ATTR) : null; return v ? parseInt(v, 10) : null; }
  function rect(el) {
    var r = el.getBoundingClientRect();
    return { top: r.top + window.scrollY, left: r.left + window.scrollX, width: r.width, height: r.height };
  }
  function visible(el) {
    if (!el || !el.getBoundingClientRect) { return false; }
    var s = window.getComputedStyle(el);
    var r = el.getBoundingClientRect();
    return s.display !== 'none' && s.visibility !== 'hidden' && r.width > 0 && r.height > 0;
  }
  // innerText kosong untuk elemen tersembunyi, jadi textContent dipakai sebagai cadangan.
  function textOf(el) { return (el.innerText || el.textContent || '').replace(/\s+/g, ' ').trim(); }
  function bgImage(el) {
    var bg = window.getComputedStyle(el).backgroundImage;
    return bg && bg !== 'none' && bg.indexOf('url(') >= 0;
  }

  /* Jenis isian ditebak dari tag (bagian 7.3); provider bisa mengubahnya. */
  function kindOf(el) {
    var tag = el.tagName.toLowerCase();
    var cls = (typeof el.className === 'string' ? el.className : '').toLowerCase();
    if (tag === 'img' || tag === 'picture' || (tag !== 'body' && bgImage(el) && !textOf(el))) { return 'image'; }
    if (tag === 'button' || (tag === 'a' && /btn|button|tombol|cta/.test(cls))) { return 'button'; }
    if (tag === 'a') { return 'link'; }
    return textOf(el).length > 80 ? 'paragraph' : 'text';
  }

  function breadcrumb(el) {
    var parts = [];
    for (var n = el; n && n !== document.body && parts.length < 4; n = n.parentElement) {
      var name = n.tagName.toLowerCase();
      var cls = typeof n.className === 'string' ? n.className.trim().split(/\s+/)[0] : '';
      parts.unshift(cls ? name + '.' + cls : name);
    }
    return parts.join(' › ');
  }

  function ensureLayer() {
    if (layer && layer.parentNode) { return layer; }
    layer = document.createElement('div');
    layer.setAttribute('data-rakit', 'layer');
    layer.style.cssText = 'position:absolute;left:0;top:0;width:0;height:0;z-index:2147483647;pointer-events:none;';
    document.body.appendChild(layer);
    return layer;
  }

  function box(r, color, width, label) {
    var d = document.createElement('div');
    d.style.cssText = 'position:absolute;box-sizing:border-box;border:' + width + 'px solid ' + color + ';' +
      'left:' + r.left + 'px;top:' + r.top + 'px;width:' + r.width + 'px;height:' + r.height + 'px;border-radius:4px;';
    if (label) {
      var t = document.createElement('div');
      t.textContent = label;
      t.style.cssText = 'position:absolute;left:-' + width + 'px;top:-18px;background:' + color + ';color:#fff;' +
        'font:600 11px/16px monospace;padding:0 4px;border-radius:3px;white-space:nowrap;max-width:240px;' +
        'overflow:hidden;text-overflow:ellipsis;';
      d.appendChild(t);
    }
    return d;
  }

  function mask(top, height) {
    var d = document.createElement('div');
    d.style.cssText = 'position:absolute;left:0;width:' + document.documentElement.scrollWidth + 'px;top:' + top +
      'px;height:' + Math.max(0, height) + 'px;background:rgba(128,128,128,0.55);';
    return d;
  }

  function redraw() {
    var l = ensureLayer();
    l.innerHTML = '';
    if (slideRange) {
      var docH = document.documentElement.scrollHeight;
      l.appendChild(mask(0, slideRange.top));
      l.appendChild(mask(slideRange.bottom, docH - slideRange.bottom));
    }
    marks.forEach(function (m) {
      var el = byId(m.id);
      if (el && visible(el)) { l.appendChild(box(rect(el), '#0A7D3A', 2, '✓ ' + m.label)); }
    });
    if (selectedId !== null) {
      var sel = byId(selectedId);
      if (sel && visible(sel)) { l.appendChild(box(rect(sel), '#0068D6', 3, null)); }
    }
  }

  /* Ketukan dipakai untuk menandai: link tidak berpindah halaman, tombol provider tidak bereaksi. */
  document.addEventListener('click', function (e) {
    if (!window.RakitMark.active) { return; }
    e.preventDefault();
    e.stopPropagation();
    var target = e.target;
    if (target && target.closest && target.closest('[data-rakit]')) { return; }
    var id = idOf(target);
    if (id === null) {
      // Elemen tanpa nomor tidak ada di HTML asli: dibuat JavaScript (bagian 7.8).
      if (window.RakitBridge) { window.RakitBridge.selectGenerated(); }
      return;
    }
    if (window.RakitBridge) { window.RakitBridge.select(id); }
  }, true);

  window.addEventListener('scroll', function () {
    if (!slideRange) { return; }
    var maxTop = Math.max(slideRange.top, slideRange.bottom - window.innerHeight);
    if (window.scrollY < slideRange.top - 1) { window.scrollTo(0, slideRange.top); }
    else if (window.scrollY > maxTop + 1) { window.scrollTo(0, maxTop); }
  }, { passive: true });

  window.addEventListener('resize', function () { redraw(); });

  window.RakitMark = {
    active: true,

    /* Header fixed/sticky dibuat tidak menempel selama mode tandai (bagian 7.2). Hanya di salinan di HP. */
    unstick: function () {
      var all = document.body.getElementsByTagName('*');
      for (var i = 0; i < all.length; i++) {
        var pos = window.getComputedStyle(all[i]).position;
        if (pos === 'fixed' || pos === 'sticky') { all[i].style.setProperty('position', 'relative', 'important'); }
      }
      return true;
    },

    describe: function (id) {
      var el = byId(id);
      if (!el) { return JSON.stringify(null); }
      var r = rect(el);
      var img = el.tagName.toLowerCase() === 'img' ? el : null;
      return JSON.stringify({
        id: id,
        tag: el.tagName.toLowerCase(),
        kind: kindOf(el),
        text: textOf(el).slice(0, 300),
        src: img ? (img.getAttribute('src') || '') : '',
        href: el.getAttribute('href') || '',
        breadcrumb: breadcrumb(el),
        top: r.top, height: r.height,
        fontSize: parseFloat(window.getComputedStyle(el).fontSize) || 0,
        ratioW: img ? img.naturalWidth : Math.round(r.width),
        ratioH: img ? img.naturalHeight : Math.round(r.height),
        visible: visible(el),
        hasParent: !!(el.parentElement && idOf(el.parentElement) !== null && el.parentElement !== document.body),
        hasChild: !!el.querySelector('[' + ATTR + ']')
      });
    },

    select: function (id) {
      selectedId = id;
      redraw();
      var el = byId(id);
      if (el && slideRange) {
        var r = rect(el);
        if (r.top < window.scrollY || r.top > window.scrollY + window.innerHeight - 40) {
          window.scrollTo(0, Math.max(slideRange.top, Math.min(r.top - 16, slideRange.bottom - window.innerHeight)));
        }
      }
      return true;
    },

    clearSelection: function () { selectedId = null; redraw(); return true; },

    parentOf: function (id) {
      var el = byId(id);
      var p = el ? el.parentElement : null;
      return p && p !== document.body && p !== document.documentElement ? idOf(p) : null;
    },

    childOf: function (id) {
      var el = byId(id);
      var c = el ? el.querySelector('[' + ATTR + ']') : null;
      return c ? idOf(c) : null;
    },

    /* marks: [{id, label}] elemen yang sudah ditandai. */
    setMarks: function (list) { marks = list || []; redraw(); return true; },

    /* Info tampilan: lebar CSS halaman dan tinggi dokumen. */
    metrics: function () {
      return JSON.stringify({ innerWidth: window.innerWidth, docHeight: document.documentElement.scrollHeight });
    },

    /* Posisi (atas) elemen awal setiap section, dalam piksel CSS. */
    tops: function (ids) {
      return JSON.stringify(ids.map(function (id) {
        var el = id === null ? null : byId(id);
        return el ? Math.round(rect(el).top) : null;
      }));
    },

    /* Menampilkan satu section saja: area lain diredupkan, gulir dibatasi (bagian 7.2). */
    slide: function (top, bottom) {
      slideRange = { top: top, bottom: bottom };
      window.scrollTo(0, top);
      redraw();
      return true;
    },

    /* Elemen yang bisa dipilih di section (termasuk yang tersembunyi), untuk daftar elemen (bagian 7.3). */
    elementsIn: function (top, bottom) {
      var out = [];
      var all = document.body.querySelectorAll('[' + ATTR + ']');
      for (var i = 0; i < all.length && out.length < 200; i++) {
        var el = all[i];
        var tag = el.tagName.toLowerCase();
        if (/^(script|style|noscript|template|br|svg|path|source|meta|link)$/.test(tag)) { continue; }
        var own = Array.prototype.some.call(el.childNodes, function (n) { return n.nodeType === 3 && n.textContent.trim(); });
        if (!own && tag !== 'img' && !bgImage(el)) { continue; }
        var r = rect(el);
        var shown = visible(el);
        if (shown && (r.top < top - 1 || r.top >= bottom)) { continue; }
        if (!shown) {
          var anc = el.parentElement;
          while (anc && !visible(anc)) { anc = anc.parentElement; }
          if (!anc) { continue; }
          var ar = rect(anc);
          if (ar.top < top - 1 || ar.top >= bottom) { continue; }
        }
        out.push({ id: idOf(el), tag: tag, kind: kindOf(el), text: textOf(el).slice(0, 80), hidden: !shown });
      }
      return JSON.stringify(out);
    },

    /* Saran tandai otomatis (bagian 7.10 A4): judul, paragraf, gambar, tombol yang belum ditandai. */
    suggest: function (top, bottom, markedIds) {
      var marked = {};
      (markedIds || []).forEach(function (id) { marked[id] = true; });
      var out = [];
      var counts = { title: 0, text: 0, image: 0, button: 0 };
      var all = document.body.querySelectorAll('h1,h2,h3,p,img,a,button');
      for (var i = 0; i < all.length && out.length < 8; i++) {
        var el = all[i];
        var id = idOf(el);
        if (id === null || marked[id] || !visible(el)) { continue; }
        var r = rect(el);
        if (r.top < top - 1 || r.top >= bottom) { continue; }
        var tag = el.tagName.toLowerCase();
        var kind = kindOf(el);
        if (tag === 'a' && kind === 'link') { continue; }
        if (tag !== 'img' && !textOf(el)) { continue; }
        var label;
        if (/^h[1-3]$/.test(tag)) { label = counts.title++ === 0 ? 'Judul' : 'Judul ' + counts.title; }
        else if (kind === 'image') { label = 'Gambar ' + (++counts.image); }
        else if (kind === 'button') { label = counts.button++ === 0 ? 'Tombol' : 'Tombol ' + counts.button; }
        else { label = counts.text++ === 0 ? 'Deskripsi' : 'Deskripsi ' + counts.text; }
        out.push({ id: id, kind: kind, label: label });
      }
      return JSON.stringify(out);
    },

    /*
     * Elemen tampil yang teksnya sama dengan elemen yang sudah ditandai tapi belum dihubungkan (bagian 7.5),
     * mis. judul versi HP dan versi desktop. matchId = elemen bertanda yang teksnya sama.
     */
    similarUnlinked: function (top, bottom, markedIds) {
      var marked = {};
      var wanted = {};
      (markedIds || []).forEach(function (id) {
        marked[id] = true;
        var el = byId(id);
        var t = el ? textOf(el).toLowerCase() : '';
        if (t.length >= 3 && !wanted[t]) { wanted[t] = id; }
      });
      var out = [];
      var all = document.body.querySelectorAll('h1,h2,h3,h4,p,a,button,span,li');
      for (var i = 0; i < all.length && out.length < 5; i++) {
        var el = all[i];
        var id = idOf(el);
        if (id === null || marked[id] || !visible(el)) { continue; }
        var r = rect(el);
        if (r.top < top - 1 || r.top >= bottom) { continue; }
        var t = textOf(el).toLowerCase();
        if (t && wanted[t] && !el.querySelector('[' + ATTR + ']')) {
          out.push({ id: id, text: textOf(el).slice(0, 60), matchId: wanted[t] });
        }
      }
      return JSON.stringify(out);
    },

    /* Elemen mana yang terlihat di tampilan sekarang (untuk label HANYA HP / HANYA DESKTOP). */
    visibility: function (ids) {
      var out = {};
      ids.forEach(function (id) { var el = byId(id); out[id] = !!(el && visible(el)); });
      return JSON.stringify(out);
    },

    /*
     * Sidik jari section untuk mendeteksi bagian yang sama persis di beberapa halaman (bagian 7.6):
     * isi HTML tanpa nomor data-tpl-id, plus daftar nomor elemen di dalamnya berurutan.
     */
    fingerprint: function (id) {
      var el = byId(id);
      if (!el) { return JSON.stringify(null); }
      var html = el.outerHTML.replace(/ data-tpl-id="\d+"/g, '').replace(/\s+/g, ' ');
      var hash = 0;
      for (var i = 0; i < html.length; i++) { hash = ((hash << 5) - hash + html.charCodeAt(i)) | 0; }
      var ids = [idOf(el)];
      Array.prototype.forEach.call(el.querySelectorAll('[' + ATTR + ']'), function (c) { ids.push(idOf(c)); });
      return JSON.stringify({ hash: String(hash) + ':' + html.length, ids: ids });
    }
  };
})();
