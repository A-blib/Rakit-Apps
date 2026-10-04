#!/usr/bin/env python3
"""
Membuat animasi Lottie bergaya garis monokrom (bagian 9.3) dari bentuk sederhana: garis, kotak, lingkaran.

Kenapa dibuat dengan skrip, bukan diunduh?
- Instruksi melarang memakai aset milik pihak lain (mis. Next.js/Vercel) dan meminta gaya garis monokrom.
- Bentuknya sederhana, jadi lebih mudah dibuat dan diubah lewat kode daripada lewat aplikasi desain.

Warna garis di file ini hitam. Di app, warnanya diganti mengikuti tema (color_foreground) lewat
"dynamic properties" Lottie (LottieTint.java), sehingga animasi tetap terbaca di mode gelap.

Cara menjalankan (dari folder android/):
    python3 tools/generate_lottie.py
Hasilnya ditulis ke app/src/main/res/raw/*.json.
"""
import json
import os

SIZE = 240          # kanvas 240x240
FPS = 30
STROKE = 6          # tebal garis (di kanvas 240); terlihat tipis setelah diperkecil di layar
BLACK = [0, 0, 0, 1]
EASE_IN = {"x": [0.4], "y": [0]}
EASE_OUT = {"x": [0.2], "y": [1]}

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")


# ---------- pembantu: nilai statis & animasi ----------

def static(value):
    return {"a": 0, "k": value}


def animated(keyframes):
    """keyframes: daftar (frame, nilai). Nilai berupa list angka."""
    frames = []
    for index, (t, value) in enumerate(keyframes):
        frame = {"t": t, "s": value if isinstance(value, list) else [value]}
        if index < len(keyframes) - 1:
            frame["i"] = EASE_OUT
            frame["o"] = EASE_IN
        frames.append(frame)
    return {"a": 1, "k": frames}


# ---------- pembantu: bentuk ----------

def rect(x, y, w, h, radius=8):
    return {"ty": "rc", "d": 1, "s": static([w, h]), "p": static([x, y]), "r": static(radius), "nm": "rect"}


def ellipse(x, y, w, h):
    return {"ty": "el", "d": 1, "s": static([w, h]), "p": static([x, y]), "nm": "ellipse"}


def line(points, closed=False):
    zeros = [[0, 0] for _ in points]
    return {"ty": "sh", "d": 1, "nm": "path",
            "ks": static({"i": zeros, "o": zeros, "v": points, "c": closed})}


def stroke(width=STROKE):
    return {"ty": "st", "c": static(BLACK), "o": static(100), "w": static(width),
            "lc": 2, "lj": 2, "ml": 4, "nm": "stroke"}


def trim(start_frame, end_frame, hold_until=None, erase_at=None):
    """Garis 'tergambar' dari 0% ke 100% antara start_frame dan end_frame (lalu opsional terhapus lagi)."""
    # Keyframe harus berurutan dan tidak boleh ada dua keyframe di frame yang sama.
    end_keys = [(0, 0)] + ([(start_frame, 0)] if start_frame > 0 else []) + [(end_frame, 100)]
    if erase_at is not None:
        end_keys += [(hold_until, 100)]
        start_keys = [(0, 0), (hold_until, 0), (erase_at, 100)]
        start_value = animated(start_keys)
    else:
        start_value = static(0)
    return {"ty": "tm", "s": start_value, "e": animated(end_keys), "o": static(0), "m": 1, "nm": "trim"}


def group_transform(position=(0, 0), opacity=100):
    return {"ty": "tr", "p": static(list(position)), "a": static([0, 0]), "s": static([100, 100]),
            "r": static(0), "o": static(opacity), "sk": static(0), "sa": static(0), "nm": "transform"}


def group(items, name="group"):
    return {"ty": "gr", "nm": name, "it": items + [group_transform()]}


def layer(index, name, shapes, total_frames, position=None, rotation=None, opacity=None):
    return {
        "ddd": 0, "ind": index, "ty": 4, "nm": name, "sr": 1, "ao": 0, "bm": 0,
        "ip": 0, "op": total_frames, "st": 0,
        "ks": {
            "o": opacity or static(100),
            "r": rotation or static(0),
            "p": position or static([SIZE / 2, SIZE / 2, 0]),
            "a": static([SIZE / 2, SIZE / 2, 0]),
            "s": static([100, 100, 100]),
        },
        "shapes": shapes,
    }


def animation(name, total_frames, layers):
    # Layer pertama di daftar Lottie digambar paling atas; urutan tidak penting karena semuanya garis.
    return {"v": "5.7.4", "fr": FPS, "ip": 0, "op": total_frames, "w": SIZE, "h": SIZE,
            "nm": name, "ddd": 0, "assets": [], "layers": layers}


def bob(total_frames, distance=4):
    """Gerakan naik-turun pelan agar animasi terasa hidup tanpa mencolok."""
    c = SIZE / 2
    return animated([(0, [c, c, 0]), (total_frames / 2, [c, c - distance, 0]), (total_frames, [c, c, 0])])


# ---------- animasi ----------

def browser_frame():
    """Jendela browser: kotak + garis judul + tiga titik."""
    return [
        group([rect(120, 120, 176, 136, 12), stroke()], "window"),
        group([line([[32, 82], [208, 82]]), stroke()], "title-bar"),
        group([ellipse(48, 67, 6, 6), ellipse(62, 67, 6, 6), ellipse(76, 67, 6, 6), stroke(4)], "dots"),
    ]


def coming_soon():
    """Jendela browser yang 'mengetik' baris-baris konten berulang: website sedang disiapkan."""
    total = 120
    content = [
        group([line([[52, 108], [150, 108]]), stroke(), trim(10, 40, 90, 110)], "line-1"),
        group([line([[52, 130], [188, 130]]), stroke(), trim(25, 55, 90, 110)], "line-2"),
        group([line([[52, 152], [120, 152]]), stroke(), trim(40, 70, 90, 110)], "line-3"),
        group([rect(170, 160, 28, 20, 4), stroke(), trim(55, 80, 90, 110)], "button"),
    ]
    return animation("coming_soon", total, [
        layer(1, "content", content, total, position=bob(total)),
        layer(2, "frame", browser_frame(), total, position=bob(total)),
    ])


def empty():
    """Kotak/baki kosong yang naik-turun pelan."""
    total = 90
    shapes = [
        group([line([[60, 110], [80, 160], [160, 160], [180, 110]]), stroke()], "tray"),
        group([line([[60, 110], [95, 110], [105, 128], [135, 128], [145, 110], [180, 110]]), stroke()], "rim"),
        group([line([[100, 72], [140, 72]]), stroke(4), trim(0, 30, 60, 85)], "dash"),
    ]
    return animation("empty", total, [layer(1, "tray", shapes, total, position=bob(total, 6))])


LINEAR_IN = {"x": [1], "y": [1]}
LINEAR_OUT = {"x": [0], "y": [0]}


def sampled(fn, total, step=2):
    """Animasi dari fungsi fn(frame) → list angka, dicuplik setiap `step` frame dengan perpindahan linear.
    Dipakai untuk gerakan yang berulang dengan fase berbeda (garis angin), yang sulit ditulis sebagai beberapa
    keyframe saja."""
    frames = []
    times = list(range(0, total + 1, step))
    for index, t in enumerate(times):
        frame = {"t": t, "s": fn(t)}
        if index < len(times) - 1:
            frame["i"] = LINEAR_IN
            frame["o"] = LINEAR_OUT
        frames.append(frame)
    return {"a": 1, "k": frames}


def loading():
    """Pesawat kertas terbang: melayang naik-turun & sedikit miring, dengan garis angin mengalir ke belakang.
    Semua gerakan berulang mulus karena panjang animasi (120 frame) habis dibagi siklus garis angin (40 frame)."""
    total = 120
    c = SIZE / 2

    # Pesawat kertas tampak samping, menghadap kanan: sayap (segi empat tertutup) + garis lipatan ke ujung depan.
    plane = [
        group([line([[180, 104], [92, 80], [120, 112], [104, 144]], closed=True), stroke()], "wing"),
        group([line([[120, 112], [180, 104]]), stroke(4)], "fold"),
    ]
    plane_position = animated([(0, [c, c + 4, 0]), (60, [c, c - 8, 0]), (total, [c, c + 4, 0])])
    plane_tilt = animated([(0, -4), (60, 3), (total, -4)])

    # Garis angin: (tinggi, panjang, fase 0..1). Masing-masing bergeser 56 px ke kiri sambil muncul lalu memudar,
    # tetap di dalam kanvas.
    cycle = 40
    streaks = []
    for i, (y, length, phase) in enumerate(((92, 30, 0.0), (114, 48, 0.35), (136, 36, 0.7), (154, 22, 0.15))):
        def progress(t, phase=phase):
            return (t / cycle + phase) % 1.0

        def position(t, progress=progress):
            return [c - 56 * progress(t), c, 0]

        def fade(t, progress=progress):
            p = progress(t)
            # Muncul cepat di awal (0→100 pada 25% pertama), lalu memudar sampai habis di akhir siklus.
            return [100 * p / 0.25 if p < 0.25 else 100 * (1 - p) / 0.75]

        x_end = 96
        shapes = [group([line([[x_end - length, y], [x_end, y]]), stroke(4)], f"wind-{i}")]
        streaks.append(layer(10 + i, f"wind-{i}", shapes, total,
                             position=sampled(position, total), opacity=sampled(fade, total)))

    return animation("loading", total, [layer(1, "plane", plane, total, position=plane_position,
                                              rotation=plane_tilt)] + streaks)


def intro_build():
    """Intro 1: blok-blok section muncul di jendela browser (menyusun website)."""
    total = 120
    blocks = [
        group([rect(120, 108, 136, 26, 4), stroke(), trim(10, 35, 95, 115)], "hero"),
        group([rect(84, 152, 64, 36, 4), stroke(), trim(30, 55, 95, 115)], "card-left"),
        group([rect(156, 152, 64, 36, 4), stroke(), trim(45, 70, 95, 115)], "card-right"),
    ]
    return animation("intro_build", total, [
        layer(1, "blocks", blocks, total, position=bob(total)),
        layer(2, "frame", browser_frame(), total, position=bob(total)),
    ])


def intro_customize():
    """Intro 2: penggeser warna/teks bergerak (mengubah template sesukamu)."""
    total = 120
    rails = [group([line([[56, y], [184, y]]), stroke(4)], f"rail-{i}") for i, y in enumerate((80, 120, 160))]
    knob_layers = []
    for i, (y, a, b) in enumerate(((80, 80, 150), (120, 160, 96), (160, 110, 170))):
        c = SIZE / 2
        position = animated([(0, [c + a - 120, c, 0]), (60, [c + b - 120, c, 0]), (total, [c + a - 120, c, 0])])
        knob = [group([ellipse(120, y, 20, 20), stroke()], f"knob-{i}")]
        knob_layers.append(layer(10 + i, f"knob-{i}", knob, total, position=position))
    return animation("intro_customize", total, knob_layers + [layer(1, "rails", rails, total)])


def intro_share():
    """Intro 3: panah keluar dari kotak (mengunggah/membagikan template)."""
    total = 90
    c = SIZE / 2
    arrow_position = animated([(0, [c, c + 6, 0]), (45, [c, c - 10, 0]), (total, [c, c + 6, 0])])
    box = [group([line([[70, 120], [70, 176], [170, 176], [170, 120]]), stroke()], "box")]
    arrow = [group([line([[120, 150], [120, 64]]), stroke()], "shaft"),
             group([line([[92, 92], [120, 64], [148, 92]]), stroke()], "head")]
    return animation("intro_share", total, [
        layer(1, "arrow", arrow, total, position=arrow_position),
        layer(2, "box", box, total),
    ])


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    files = {
        "coming_soon.json": coming_soon(),
        "empty.json": empty(),
        "loading.json": loading(),
        "intro_build.json": intro_build(),
        "intro_customize.json": intro_customize(),
        "intro_share.json": intro_share(),
    }
    for name, data in files.items():
        with open(os.path.join(OUT_DIR, name), "w") as f:
            json.dump(data, f, separators=(",", ":"))
        print("ditulis:", name)


if __name__ == "__main__":
    main()
