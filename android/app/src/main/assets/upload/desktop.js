/*
 * Tampilan Desktop di HP (alur-fitur-upload.md bagian 7.4): halaman dibuat selebar 1280 px CSS agar @media versi
 * desktop aktif, lalu WebView memperkecilnya agar muat. Dipasang sebelum halaman dimuat, dan meta viewport milik
 * template diganti begitu muncul.
 */
(function () {
  var WIDTH = 1280;
  function fix() {
    var meta = document.querySelector('meta[name="viewport"]');
    if (!meta) {
      if (!document.head) { return; }
      meta = document.createElement('meta');
      meta.setAttribute('name', 'viewport');
      document.head.appendChild(meta);
    }
    if (meta.getAttribute('content') !== 'width=' + WIDTH) { meta.setAttribute('content', 'width=' + WIDTH); }
  }
  var observer = new MutationObserver(fix);
  observer.observe(document.documentElement || document, { childList: true, subtree: true });
  window.addEventListener('load', function () { fix(); observer.disconnect(); });
  fix();
})();
