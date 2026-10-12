#!/usr/bin/env python3
"""
Membuat animasi Lottie berwarna OrgSpace (revisi-fondasi-orgspace.md 7.5): garis biru + isian kuning,
dari bentuk sederhana: garis, kotak, lingkaran.

Kenapa dibuat dengan skrip, bukan diunduh?
- Aset pihak lain bisa berhak cipta, dan gayanya harus seragam dengan token warna app.
- Bentuknya sederhana, jadi lebih mudah dibuat dan diubah lewat kode daripada lewat aplikasi desain.

Setiap garis/isian diberi nama "stroke-primary", "fill-primary", "stroke-accent", atau "fill-accent".
Warna di file ini hanya contoh; di app, LottieTint.java mewarnai ulang elemen bernama itu dengan color_primary
dan color_accent lewat "dynamic properties" Lottie, sehingga animasi ikut berganti di mode gelap.

Cara menjalankan (dari folder android/):
    python3 tools/generate_lottie.py
Hasilnya ditulis ke app/src/main/res/raw/*.json.
"""
import json
import os

SIZE = 240          # kanvas 240x240
FPS = 30
STROKE = 6          # tebal garis (di kanvas 240); terlihat tipis setelah diperkecil di layar
# Warna contoh (palet terang). Hanya terlihat di pratinjau file JSON; app selalu mewarnai ulang.
PRIMARY = [0.231, 0.357, 0.992, 1]   # #3B5BFD
ACCENT = [1.0, 0.773, 0.192, 1]      # #FFC531
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


def stroke(width=STROKE, role="primary"):
    color = PRIMARY if role == "primary" else ACCENT
    return {"ty": "st", "c": static(color), "o": static(100), "w": static(width),
            "lc": 2, "lj": 2, "ml": 4, "nm": "stroke-" + role}


def fill(opacity=100, role="accent"):
    color = PRIMARY if role == "primary" else ACCENT
    return {"ty": "fl", "c": static(color), "o": static(opacity), "r": 1, "nm": "fill-" + role}


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


def layer(index, name, shapes, total_frames, position=None, rotation=None, opacity=None, scale=100):
    return {
        "ddd": 0, "ind": index, "ty": 4, "nm": name, "sr": 1, "ao": 0, "bm": 0,
        "ip": 0, "op": total_frames, "st": 0,
        "ks": {
            "o": opacity or static(100),
            "r": rotation or static(0),
            "p": position or static([SIZE / 2, SIZE / 2, 0]),
            "a": static([SIZE / 2, SIZE / 2, 0]),
            "s": static([scale, scale, 100]),
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


def empty():
    """Kotak/baki kosong yang naik-turun pelan."""
    total = 90
    shapes = [
        group([line([[60, 110], [80, 160], [160, 160], [180, 110]]), stroke()], "tray"),
        group([line([[60, 110], [95, 110], [105, 128], [135, 128], [145, 110], [180, 110]]), stroke()], "rim"),
        group([line([[100, 72], [140, 72]]), stroke(4, "accent"), trim(0, 30, 60, 85)], "dash"),
        group([ellipse(120, 52, 10, 10), fill()], "spark"),
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


def path_shape(points, closed=False):
    """Bentuk garis yang titik-titiknya bisa dianimasikan (untuk jubah yang berkibar)."""
    zeros = [[0, 0] for _ in points]
    return {"i": zeros, "o": zeros, "v": points, "c": closed}


def animated_line(keyframes, closed=False):
    """keyframes: daftar (frame, titik-titik). Semua daftar titik harus sama panjangnya."""
    frames = []
    for index, (t, points) in enumerate(keyframes):
        frame = {"t": t, "s": [path_shape(points, closed)]}
        if index < len(keyframes) - 1:
            frame["i"] = EASE_OUT
            frame["o"] = EASE_IN
        frames.append(frame)
    return {"ty": "sh", "d": 1, "nm": "path", "ks": {"a": 1, "k": frames}}


# Bagian-bagian animasi loading (frame). Harus sama dengan konstanta di HeroLoading.java.
LOADING_INTRO_END = 45      # 0–45: lingkaran berputar lalu berubah menjadi pahlawan
LOADING_LOOP_END = 165      # 45–165: terbang di tempat, diulang selama data dimuat (120 frame = 4 detik)
LOADING_OUTRO_END = 195     # 165–195: melaju ke kanan sampai keluar layar


def loading():
    """Loading tiga bagian (permintaan Aris): lingkaran berputar → berubah menjadi pahlawan berjubah yang terbang di
    tempat (bagian ini diulang) → saat data siap, pahlawan melaju ke kanan keluar layar. Sosok umum bergaya garis,
    bukan karakter berhak cipta (tanpa logo/kostum tokoh tertentu)."""
    total = LOADING_OUTRO_END
    intro, loop_end = LOADING_INTRO_END, LOADING_LOOP_END
    c = SIZE / 2

    # ---- 1. Lingkaran berputar (seperti loading biasa), lalu mengecil & memudar saat pahlawan muncul ----
    arc = [group([ellipse(120, 120, 80, 80), stroke(8),
                  {"ty": "tm", "s": static(0), "e": static(28), "o": static(0), "m": 1, "nm": "trim"}], "arc")]
    arc_layer = layer(1, "arc", arc, intro, rotation=animated([(0, 0), (intro, 540)]),
                      opacity=animated([(0, 100), (28, 100), (40, 0)]))
    arc_layer["ks"]["s"] = animated([(0, [100, 100, 100]), (28, [100, 100, 100]), (40, [20, 20, 100])])

    # ---- 2. Pahlawan: anggota badan garis tebal berujung bulat, kepala & kepalan lingkaran penuh ----
    head = group([ellipse(174, 100, 24, 24), fill(role="primary")], "head")
    fist = group([ellipse(204, 96, 13, 13), fill(role="primary")], "fist")
    arm_front = group([line([[156, 106], [200, 98]]), stroke(9)], "arm-front")
    body = group([line([[154, 112], [118, 119]]), stroke(20)], "body")
    leg_front = group([line([[118, 118], [68, 120]]), stroke(12)], "leg-front")
    leg_back = group([line([[118, 123], [72, 134]]), stroke(10)], "leg-back")

    # Jubah berkibar: ujungnya naik-turun bergantian setiap 15 frame, dari pahlawan muncul sampai akhir.
    cape_frames = []
    for i, t in enumerate(range(28, total + 1, 15)):
        up = -5 if i % 2 == 0 else 5
        cape_frames.append((t, [[158, 104], [126, 92 + up / 2], [90, 86 + up], [56, 96 - up], [86, 104 + up / 2],
                                [120, 110]]))
    cape = group([animated_line(cape_frames, closed=True), fill(), stroke(4, "accent")], "cape")
    hero = [head, fist, arm_front, body, leg_front, leg_back, cape]

    mid = (intro + loop_end) / 2
    hero_layer = layer(2, "hero", hero, total, rotation=animated([(28, 8), (intro, -3), (mid, 2), (loop_end, -3),
                                                                  (total, -8)]))
    hero_layer["ip"] = 28
    # Muncul dengan membesar (28→45), melayang naik-turun (45→165), lalu melaju ke kanan sambil sedikit naik.
    hero_layer["ks"]["p"] = animated([(28, [c, c + 4, 0]), (intro, [c, c + 4, 0]), (mid, [c, c - 8, 0]),
                                      (loop_end, [c, c + 4, 0]), (total, [c + 260, c - 16, 0])])
    hero_layer["ks"]["s"] = animated([(28, [0, 0, 100]), (intro, [120, 120, 100])])
    hero_layer["ks"]["o"] = animated([(28, 0), (36, 100)])

    # ---- Garis angin (hanya di bagian terbang di tempat) ----
    cycle = 40
    streaks = []
    for i, (y, length, phase) in enumerate(((100, 22, 0.0), (116, 32, 0.35), (132, 26, 0.7), (146, 18, 0.15))):
        def progress(t, phase=phase):
            return ((t - intro) / cycle + phase) % 1.0

        def position(t, progress=progress):
            return [c - 30 * progress(t), c, 0]

        def fade(t, progress=progress):
            if t < intro + 6 or t > loop_end:
                return [0]
            p = progress(t)
            return [100 * p / 0.25 if p < 0.25 else 100 * (1 - p) / 0.75]

        x_end = 44
        shapes = [group([line([[x_end - length, y], [x_end, y]]), stroke(4, "accent")], f"wind-{i}")]
        streaks.append(layer(10 + i, f"wind-{i}", shapes, total,
                             position=sampled(position, total), opacity=sampled(fade, total)))

    # Garis laju saat melaju ke kanan: dua garis panjang memanjang di belakang pahlawan lalu memudar.
    dash = []
    for i, y in enumerate((112, 128)):
        dash.append(group([line([[20, y], [150, y]]), stroke(4, "accent"),
                           {"ty": "tm", "s": animated([(loop_end, 100), (total, 0)]), "e": static(100),
                            "o": static(0), "m": 1, "nm": "trim"}], f"speed-{i}"))
    speed = layer(20, "speed", dash, total, opacity=animated([(loop_end, 0), (loop_end + 6, 100), (total, 0)]))
    speed["ip"] = loop_end

    return animation("loading", total, [hero_layer, speed, arc_layer] + streaks)


def intro_home():
    """Intro 1 "rumah online": jendela browser berisi rumah kecil + bendera kuning yang berkibar."""
    total = 120
    house = [
        group([line([[84, 128], [120, 98], [156, 128]]), stroke(), trim(8, 30)], "roof"),
        group([line([[92, 122], [92, 160], [148, 160], [148, 122]]), stroke(), trim(18, 42)], "walls"),
        group([rect(120, 146, 18, 28, 4), fill()], "door"),
        group([line([[156, 98], [156, 66]]), stroke(4)], "pole"),
    ]
    flag_frames = []
    for i, t in enumerate(range(0, total + 1, 20)):
        wave = 4 if i % 2 == 0 else -4
        flag_frames.append((t, [[156, 66], [172, 68 + wave / 2], [186, 66], [186, 82], [172, 84 + wave / 2], [156, 82]]))
    flag = [group([animated_line(flag_frames, closed=True), fill()], "flag")]
    return animation("intro_home", total, [
        layer(1, "flag", flag, total, position=bob(total)),
        layer(2, "house", house, total, position=bob(total)),
        layer(3, "frame", browser_frame(), total, position=bob(total)),
    ])


def person(x, y, role="primary"):
    """Sosok sederhana: kepala lingkaran + bahu setengah lingkaran."""
    return [
        group([ellipse(x, y, 26, 26), fill(role=role)], "head"),
        group([line([[x - 22, y + 44], [x - 18, y + 28], [x, y + 20], [x + 18, y + 28], [x + 22, y + 44]]),
               stroke(role=role)], "shoulders"),
    ]


def intro_team():
    """Intro 2 "kerjakan bareng": tiga pengurus muncul bergantian, terhubung garis kerja sama."""
    total = 120
    links = [group([line([[72, 104], [120, 84]]), stroke(4, "accent"), trim(30, 50)], "link-left"),
             group([line([[120, 84], [168, 104]]), stroke(4, "accent"), trim(45, 65)], "link-right")]
    layers = [layer(1, "links", links, total)]
    for i, (x, y, at) in enumerate(((72, 104, 0), (120, 80, 12), (168, 104, 24))):
        c = SIZE / 2
        member = layer(10 + i, f"member-{i}", person(x, y, "accent" if i == 1 else "primary"), total,
                       position=animated([(at, [c, c + 12, 0]), (at + 15, [c, c, 0])]),
                       opacity=animated([(at, 0), (at + 12, 100)]))
        layers.append(member)
    return animation("intro_team", total, layers)


def intro_period():
    """Intro 3 "dari periode ke periode": kalender berganti tahun, website (bintang) tetap di tengah."""
    total = 120
    c = SIZE / 2
    calendar = [
        group([rect(120, 128, 132, 104, 14), stroke()], "page"),
        group([line([[54, 104], [186, 104]]), stroke(4)], "header"),
        group([line([[92, 66], [92, 86]]), stroke(4)], "ring-left"),
        group([line([[148, 66], [148, 86]]), stroke(4)], "ring-right"),
    ]
    # Halaman bulan "dibalik" naik ke atas dan memudar, berulang: tanda pergantian periode.
    sheet = [group([rect(120, 140, 108, 44, 8), fill(60)], "sheet")]
    star = [group([line([[120, 122], [126, 136], [141, 137], [129, 146], [133, 160], [120, 152], [107, 160],
                         [111, 146], [99, 137], [114, 136]], closed=True), fill(role="primary")], "star")]
    return animation("intro_period", total, [
        layer(1, "star", star, total, opacity=animated([(50, 0), (70, 100)])),
        layer(2, "sheet", sheet, total, position=animated([(0, [c, c, 0]), (45, [c, c - 30, 0])]),
              opacity=animated([(0, 100), (45, 0)])),
        layer(3, "calendar", calendar, total, position=bob(total)),
    ])


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    files = {
        "empty.json": empty(),
        "loading.json": loading(),
        "intro_home.json": intro_home(),
        "intro_team.json": intro_team(),
        "intro_period.json": intro_period(),
    }
    for name, data in files.items():
        with open(os.path.join(OUT_DIR, name), "w") as f:
            json.dump(data, f, separators=(",", ":"))
        print("ditulis:", name)


if __name__ == "__main__":
    main()
