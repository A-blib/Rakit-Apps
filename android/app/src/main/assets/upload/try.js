/*
 * Mode Coba sebagai pengguna (alur-fitur-upload.md bagian 8). Menerapkan isian percobaan ke salinan bernomor:
 * teks, link, gambar, dan gaya (lewat satu lapisan <style>, file provider tidak diubah, bagian 7.11).
 * Dipanggil Java: RakitTry.apply({fields: [...], theme: {...}}).
 */
(function () {
  if (window.RakitTry) { return; }
  var ATTR = 'data-tpl-id';
  var originals = {};

  function byId(id) { return document.querySelector('[' + ATTR + '="' + id + '"]'); }

  function remember(el, id) {
    if (originals[id]) { return; }
    originals[id] = { html: el.innerHTML, href: el.getAttribute('href'), src: el.getAttribute('src'),
                      srcset: el.getAttribute('srcset'), bg: el.style.backgroundImage };
  }

  function setText(el, text) {
    // Hanya mengganti teks; elemen anak seperti ikon <i> di dalam tombol dipertahankan jika ada.
    var icon = el.querySelector('i, svg');
    el.textContent = text;
    if (icon) { el.insertBefore(icon, el.firstChild); el.insertBefore(document.createTextNode(' '), icon.nextSibling); }
  }

  window.RakitTry = {
    apply: function (data) {
      var css = [];
      (data.fields || []).forEach(function (f) {
        f.ids.forEach(function (id) {
          var el = byId(id);
          if (!el) { return; }
          remember(el, id);
          if (f.text !== null && f.text !== undefined && f.type !== 'image') { setText(el, f.text); }
          if (f.href && (f.type === 'link' || f.type === 'button')) { el.setAttribute('href', f.href); }
          if (f.src && f.type === 'image') {
            if (el.tagName.toLowerCase() === 'img') {
              el.setAttribute('src', f.src);
              el.removeAttribute('srcset');
            } else {
              el.style.backgroundImage = 'url("' + f.src + '")';
            }
          }
          var rules = [];
          Object.keys(f.styles || {}).forEach(function (prop) {
            rules.push(prop + ':' + f.styles[prop] + ' !important');
          });
          if (rules.length) { css.push('[' + ATTR + '="' + id + '"]{' + rules.join(';') + '}'); }
        });
      });
      var vars = [];
      Object.keys(data.theme || {}).forEach(function (name) { vars.push(name + ':' + data.theme[name]); });
      if (vars.length) { css.push(':root{' + vars.join(';') + '}'); }
      var style = document.getElementById('rakit-custom');
      if (!style) {
        style = document.createElement('style');
        style.id = 'rakit-custom';
        document.head.appendChild(style);
      }
      // Dimuat paling akhir agar mengalahkan CSS provider, seperti custom.css di template sungguhan.
      document.head.appendChild(style);
      style.textContent = css.join('\n');
      return true;
    },

    /* Nilai asli (teks/link/gambar) sebuah elemen, untuk mengisi form saat pertama kali dibuka. */
    original: function (id) {
      var el = byId(id);
      if (!el) { return JSON.stringify(null); }
      var cs = window.getComputedStyle(el);
      return JSON.stringify({
        text: (el.innerText || el.textContent || '').replace(/\s+/g, ' ').trim(),
        href: el.getAttribute('href') || '',
        fontSize: parseFloat(cs.fontSize) || 0,
        radius: parseFloat(cs.borderTopLeftRadius) || 0
      });
    }
  };
})();
