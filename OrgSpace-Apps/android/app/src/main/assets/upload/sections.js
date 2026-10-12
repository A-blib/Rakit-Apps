/*
 * Deteksi section untuk tampilan slide (alur-fitur-upload.md bagian 7.1). Dijalankan di WebView setelah halaman
 * tampil dan gambar selesai dimuat. Hasil: array JSON [{name, top, height, tag, id}] dalam piksel CSS.
 *
 * Urutan cara (berlapis):
 *   1. tanda dari provider: data-section="Hero"
 *   2. tag semantik tingkat atas: header, nav, section, footer (main dianggap pembungkus)
 *   3. membaca tampilan: turun melewati pembungkus, ambil blok selebar halaman yang tersusun ke bawah
 *   4. semua gagal: seluruh halaman menjadi 1 section "Halaman"
 */
(function () {
  var docWidth = document.documentElement.clientWidth || window.innerWidth;
  var docHeight = Math.max(document.documentElement.scrollHeight, document.body ? document.body.scrollHeight : 0);
  var NAMES = [
    ['hero', 'Hero'], ['banner', 'Hero'], ['jumbotron', 'Hero'], ['masthead', 'Hero'],
    ['navbar', 'Navigasi'], ['nav', 'Navigasi'], ['menu', 'Navigasi'], ['header', 'Header'],
    ['about', 'Tentang'], ['tentang', 'Tentang'], ['service', 'Layanan'], ['layanan', 'Layanan'],
    ['feature', 'Fitur'], ['fitur', 'Fitur'], ['product', 'Produk'], ['produk', 'Produk'], ['menu-', 'Menu'],
    ['gallery', 'Galeri'], ['galeri', 'Galeri'], ['portfolio', 'Portofolio'], ['portofolio', 'Portofolio'],
    ['testimon', 'Testimoni'], ['team', 'Tim'], ['tim', 'Tim'], ['pricing', 'Harga'], ['harga', 'Harga'],
    ['faq', 'FAQ'], ['blog', 'Blog'], ['news', 'Berita'], ['berita', 'Berita'], ['cta', 'Ajakan'],
    ['contact', 'Kontak'], ['kontak', 'Kontak'], ['footer', 'Footer']
  ];

  function box(el) {
    var r = el.getBoundingClientRect();
    return { top: r.top + window.scrollY, height: r.height, width: r.width };
  }

  function visible(el) {
    var s = window.getComputedStyle(el);
    var b = box(el);
    return s.display !== 'none' && s.visibility !== 'hidden' && b.height > 0 && b.width > 0;
  }

  function guessName(el, index) {
    var marked = el.getAttribute('data-section');
    if (marked) { return marked; }
    var hint = ((el.id || '') + ' ' + (typeof el.className === 'string' ? el.className : '')).toLowerCase();
    for (var i = 0; i < NAMES.length; i++) {
      if (hint.indexOf(NAMES[i][0]) >= 0) { return NAMES[i][1]; }
    }
    var tag = el.tagName.toLowerCase();
    if (tag === 'header') { return 'Header'; }
    if (tag === 'footer') { return 'Footer'; }
    if (tag === 'nav') { return 'Navigasi'; }
    return 'Section ' + (index + 1);
  }

  function result(elements) {
    var list = [];
    elements.forEach(function (el, i) {
      var b = box(el);
      list.push({ name: guessName(el, i), top: Math.max(0, Math.round(b.top)), height: Math.round(b.height),
                  tag: el.tagName.toLowerCase(), id: el.getAttribute('data-tpl-id') });
    });
    list.sort(function (a, b) { return a.top - b.top; });
    return list;
  }

  function wide(el) {
    var b = box(el);
    return b.width >= docWidth * 0.85 && b.height >= 40;
  }

  // Pembungkus: <main>, atau elemen yang berisi 2+ blok lebar dan tingginya hampir setinggi halaman.
  function isWrapper(el) {
    var tag = el.tagName.toLowerCase();
    if (tag === 'main') { return true; }
    var blocks = Array.prototype.filter.call(el.children, function (c) { return visible(c) && wide(c); });
    return blocks.length >= 2 && box(el).height >= docHeight * 0.6;
  }

  function flatten(elements) {
    var out = [];
    elements.forEach(function (el) {
      if (isWrapper(el)) {
        var inner = Array.prototype.filter.call(el.children, function (c) { return visible(c) && wide(c); });
        out = out.concat(flatten(inner));
      } else {
        out.push(el);
      }
    });
    return out;
  }

  function noOverlap(elements) {
    var sorted = elements.slice().sort(function (a, b) { return box(a).top - box(b).top; });
    var out = [];
    var bottom = -1;
    sorted.forEach(function (el) {
      var b = box(el);
      if (b.top >= bottom - 4) {
        out.push(el);
        bottom = b.top + b.height;
      }
    });
    return out;
  }

  // 1. data-section
  var marked = Array.prototype.filter.call(document.querySelectorAll('[data-section]'), visible);
  if (marked.length >= 1) { return JSON.stringify(result(noOverlap(marked))); }

  // 2. tag semantik tingkat atas
  var semantic = Array.prototype.filter.call(document.body.querySelectorAll('header, nav, section, footer'),
    function (el) { return visible(el) && !el.parentElement.closest('header, nav, section, footer'); });
  if (semantic.length >= 2) { return JSON.stringify(result(noOverlap(semantic))); }

  // 3. membaca tampilan
  var level = document.body;
  for (var depth = 0; depth < 6; depth++) {
    var kids = Array.prototype.filter.call(level.children, visible);
    if (kids.length === 1) { level = kids[0]; } else { break; }
  }
  var blocks = flatten(Array.prototype.filter.call(level.children, function (c) { return visible(c) && wide(c); }));
  blocks = noOverlap(blocks);
  if (blocks.length >= 2) { return JSON.stringify(result(blocks)); }

  // 4. seluruh halaman
  return JSON.stringify([{ name: 'Halaman', top: 0, height: docHeight, tag: 'body', id: null }]);
})();
