#!/usr/bin/env python3
"""Membuat gambar contoh (JPG) untuk paket template demo di
src/main/resources/seed/template-packages/*/site/img/.

Gambar dibuat sendiri (bentuk dan gradasi sederhana) agar bebas hak cipta.
Jalankan dari folder backend:  python3 tools/buat-gambar-demo.py
"""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent / "src/main/resources/seed/template-packages"


def gradient(size, top, bottom):
    w, h = size
    img = Image.new("RGB", size)
    draw = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(h - 1, 1)
        color = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        draw.line([(0, y), (w, y)], fill=color)
    return img, draw


def save(img, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, "JPEG", quality=78, optimize=True, progressive=True)


def cake(path, plate, cream, berry):
    img, d = gradient((800, 800), (250, 236, 222), (236, 206, 180))
    d.ellipse([140, 560, 660, 660], fill=plate)
    d.rectangle([220, 360, 580, 600], fill=(196, 132, 84))
    d.rectangle([220, 330, 580, 380], fill=cream)
    for x in range(250, 560, 70):
        d.ellipse([x, 290, x + 50, 340], fill=berry)
    save(img, path)


def person(path, bg, shirt):
    img, d = gradient((800, 800), bg, tuple(max(c - 40, 0) for c in bg))
    d.ellipse([290, 170, 510, 390], fill=(224, 182, 150))
    d.ellipse([170, 420, 630, 900], fill=shirt)
    save(img, path)


def main():
    kuliner = ROOT / "umkm-kuliner/site/img"
    img, d = gradient((1600, 900), (120, 62, 40), (226, 150, 92))
    d.ellipse([980, 260, 1460, 740], fill=(250, 240, 228))
    d.rectangle([1060, 420, 1380, 600], fill=(170, 100, 60))
    d.rectangle([1060, 390, 1380, 440], fill=(255, 248, 238))
    for x in range(1080, 1360, 70):
        d.ellipse([x, 350, x + 46, 396], fill=(200, 30, 60))
    save(img, kuliner / "hero.jpg")
    cake(kuliner / "produk-1.jpg", (255, 255, 255), (255, 244, 230), (190, 30, 60))
    cake(kuliner / "produk-2.jpg", (240, 240, 240), (120, 70, 40), (250, 200, 60))

    sekolah = ROOT / "profil-sekolah/site/img"
    img, d = gradient((1600, 900), (150, 196, 235), (225, 238, 250))
    d.rectangle([0, 720, 1600, 900], fill=(110, 160, 90))
    d.rectangle([400, 380, 1200, 720], fill=(240, 232, 214))
    d.polygon([(360, 390), (800, 200), (1240, 390)], fill=(160, 50, 40))
    for x in range(460, 1150, 140):
        d.rectangle([x, 450, x + 80, 530], fill=(90, 130, 180))
    d.rectangle([740, 590, 860, 720], fill=(120, 80, 50))
    save(img, sekolah / "gedung.jpg")
    person(sekolah / "kepala-sekolah.jpg", (210, 222, 236), (30, 58, 138))

    person(ROOT / "portofolio-minimal/site/img/foto.jpg", (230, 230, 230), (40, 40, 40))


if __name__ == "__main__":
    main()
