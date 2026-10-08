/*
 * Skrip preview editor template mode (alur-buat-website-via-template.md bagian 11.5). Dipasang sebelum halaman dimuat.
 * - menerapkan nilai project ke setiap [data-key] (teks lewat textContent, bukan innerHTML);
 * - memasang <style id="app-custom-css"> paling akhir di <head>;
 * - menyorot elemen yang sedang diedit;
 * - melaporkan ketukan pada elemen [data-key] ke Java lewat RakitBridge.tap(key) (satu-satunya pesan ke Java).
 * Dipanggil Java: RakitEditor.apply({fields: {...}, css: "..."}), RakitEditor.highlight(key, scroll), dst.
 */
(function () {
  if (window.RakitEditor) { return; }
  var ATTR = 'data-key';
  var editing = true;
  var ready = false;

  // Isi contoh disembunyikan sebentar sampai nilai project diterapkan, agar tidak berkedip.
  var hide = document.createElement('style');
  hide.textContent = 'html{visibility:hidden !important}';
  (document.head || document.documentElement).appendChild(hide);
  function reveal() { if (hide.parentNode) { hide.parentNode.removeChild(hide); } }
  setTimeout(reveal, 1500);

  function all(key) {
    return Array.prototype.filter.call(document.querySelectorAll('[' + ATTR + ']'), function (el) {
      return el.getAttribute(ATTR) === key;
    });
  }

  function remember(el) {
    if (!el.__rakitOrig) {
      el.__rakitOrig = { html: el.innerHTML, href: el.getAttribute('href'), src: el.getAttribute('src'),
                         srcset: el.getAttribute('srcset'), bg: el.style.backgroundImage };
    }
    return el.__rakitOrig;
  }

  function setText(el, text) {
    // Hanya teks yang diganti; ikon <i>/<svg> di dalam tombol dipertahankan.
    var icon = el.querySelector('i, svg');
    el.textContent = text;
    if (icon) { el.insertBefore(icon, el.firstChild); el.insertBefore(document.createTextNode(' '), icon.nextSibling); }
  }

  function applyOne(el, type, value) {
    var orig = remember(el);
    var v = value || {};
    if (type !== 'image' && type !== 'link') {
      if (typeof v.text === 'string') { setText(el, v.text); } else { el.innerHTML = orig.html; }
    }
    if (type === 'link' || type === 'button') {
      if (typeof v.href === 'string') { el.setAttribute('href', v.href); }
      else if (orig.href === null) { el.removeAttribute('href'); } else { el.setAttribute('href', orig.href); }
    }
    if (type === 'image') {
      var isImg = el.tagName.toLowerCase() === 'img';
      if (typeof v.image === 'string') {
        if (isImg) { el.setAttribute('src', v.image); el.removeAttribute('srcset'); }
        else { el.style.backgroundImage = 'url("' + v.image.replace(/"/g, '') + '")'; }
      } else if (isImg) {
        if (orig.src !== null) { el.setAttribute('src', orig.src); }
        if (orig.srcset !== null) { el.setAttribute('srcset', orig.srcset); }
      } else {
        el.style.backgroundImage = orig.bg;
      }
    }
  }

  function setCss(css) {
    var style = document.getElementById('app-custom-css');
    if (!style) {
      style = document.createElement('style');
      style.id = 'app-custom-css';
    }
    // Selalu dipindah ke paling akhir agar mengalahkan CSS template, sama seperti custom.css hasil export.
    (document.head || document.documentElement).appendChild(style);
    style.textContent = css || '';
  }

  var marker = document.createElement('style');
  marker.textContent = '[data-rakit-active]{outline:2px solid #0068D6 !important;outline-offset:2px !important}' +
      '[data-rakit-editing] [data-key]{cursor:pointer}';

  document.addEventListener('click', function (e) {
    if (!editing) { return; }
    var target = e.target && e.target.closest ? e.target.closest('[' + ATTR + ']') : null;
    // Saat mengedit, link tidak berpindah halaman: ketukan dipakai untuk memilih isian.
    if (e.target && e.target.closest && e.target.closest('a, button, [onclick]')) { e.preventDefault(); }
    if (target) {
      e.preventDefault();
      e.stopPropagation();
      if (window.RakitBridge) { window.RakitBridge.tap(target.getAttribute(ATTR)); }
    }
  }, true);

  window.RakitEditor = {
    /** data = {fields: {key: {type, text, href, image}}, css: "..."} */
    apply: function (data) {
      if (!document.head.contains(marker)) { document.head.appendChild(marker); }
      var fields = data.fields || {};
      Object.keys(fields).forEach(function (key) {
        var f = fields[key];
        all(key).forEach(function (el) { applyOne(el, f.type, f.value); });
      });
      if (typeof data.css === 'string') { setCss(data.css); }
      ready = true;
      reveal();
      return true;
    },

    setField: function (key, type, value) {
      all(key).forEach(function (el) { applyOne(el, type, value); });
    },

    css: setCss,

    setEditing: function (on) {
      editing = !!on;
      if (on) { document.documentElement.setAttribute('data-rakit-editing', ''); }
      else { document.documentElement.removeAttribute('data-rakit-editing'); this.highlight(null, false); }
    },

    /** Menyorot elemen isian; scroll = gulir ke elemen pertama yang terlihat. */
    highlight: function (key, scroll) {
      Array.prototype.forEach.call(document.querySelectorAll('[data-rakit-active]'), function (el) {
        el.removeAttribute('data-rakit-active');
      });
      if (!key) { return false; }
      var found = null;
      all(key).forEach(function (el) {
        el.setAttribute('data-rakit-active', '');
        if (!found && el.getClientRects().length) { found = el; }
      });
      if (found && scroll) {
        // Jarak di atas elemen, kira-kira tinggi header situs yang menempel.
        window.scrollTo({ top: found.getBoundingClientRect().top + window.scrollY - 88, behavior: 'smooth' });
      }
      return !!found;
    },

    /** Nilai gaya saat ini per isian (piksel CSS & warna rgb), untuk posisi awal slider dan cek kontras. */
    computed: function (keys) {
      var result = {};
      keys.forEach(function (key) {
        var el = all(key).filter(function (e) { return e.getClientRects().length; })[0] || all(key)[0];
        if (!el) { return; }
        var cs = window.getComputedStyle(el);
        var bg = cs.backgroundColor;
        var node = el;
        // Latar transparan: warna latar sebenarnya milik pembungkus terdekat yang berwarna.
        while (node && node.parentElement && (bg === 'rgba(0, 0, 0, 0)' || bg === 'transparent')) {
          node = node.parentElement;
          bg = window.getComputedStyle(node).backgroundColor;
        }
        // Sampai <html> tetap transparan: browser menampilkan latar putih.
        if (bg === 'rgba(0, 0, 0, 0)' || bg === 'transparent') { bg = 'rgb(255, 255, 255)'; }
        result[key] = { fontSize: parseFloat(cs.fontSize) || 0, radius: parseFloat(cs.borderTopLeftRadius) || 0,
                        color: cs.color, background: bg };
      });
      return JSON.stringify(result);
    },

    isReady: function () { return ready; }
  };
})();
