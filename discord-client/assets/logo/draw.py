# JZCord logo (concept B), plus the other concepts that were considered.
#   python3 draw.py <fonts dir> <out dir>                 concepts + preview
#   python3 draw.py <fonts dir> <out dir> final           jzcord.png
#   python3 draw.py <fonts dir> <out dir> android <res>   launcher icons
# Fonts (Google Fonts, OFL/Apache): LilitaOne-Regular.ttf, LuckiestGuy-Regular.ttf
import sys, math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

FONTS = sys.argv[1]
OUT = sys.argv[2]
LILITA = f"{FONTS}/LilitaOne-Regular.ttf"
LUCKY = f"{FONTS}/LuckiestGuy-Regular.ttf"

N = 1024
PINK = (255, 10, 140)
WHITE = (252, 252, 252)
BLACK = (0, 0, 0)
DARK = (17, 18, 24)
DARKER = (8, 8, 13)

def _layers_bg():
    grad = Image.new("RGBA", (N, N))
    gd = ImageDraw.Draw(grad)
    for y in range(N):
        t = y / N
        gd.line([(0, y), (N, y)], fill=tuple(int(DARK[i] * (1 - t) + DARKER[i] * t) for i in range(3)) + (255,))
    slashes = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    sd = ImageDraw.Draw(slashes)
    for (x, y, w, a) in [(-80, 120, 70, 150), (760, 860, 90, 170), (820, 40, 40, 90), (-40, 900, 40, 90)]:
        sd.polygon([(x, y), (x + 420, y - 230), (x + 420 + w, y - 230), (x + w, y)], fill=PINK + (a,))
    grad.alpha_composite(slashes.filter(ImageFilter.GaussianBlur(3)))
    return grad

def background(radius=220):
    """Dark rounded square with the pink slashes of the JZS banner."""
    bg = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    grad = Image.new("RGBA", (N, N))
    gd = ImageDraw.Draw(grad)
    for y in range(N):
        t = y / N
        gd.line([(0, y), (N, y)], fill=tuple(int(DARK[i] * (1 - t) + DARKER[i] * t) for i in range(3)) + (255,))
    slashes = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    sd = ImageDraw.Draw(slashes)
    for (x, y, w, a) in [(-80, 120, 70, 150), (760, 860, 90, 170), (820, 40, 40, 90), (-40, 900, 40, 90)]:
        sd.polygon([(x, y), (x + 420, y - 230), (x + 420 + w, y - 230), (x + w, y)], fill=PINK + (a,))
    slashes = slashes.filter(ImageFilter.GaussianBlur(3))
    grad.alpha_composite(slashes)
    mask = Image.new("L", (N, N), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, N - 1, N - 1], radius=radius, fill=255)
    bg.paste(grad, (0, 0), mask)
    return bg

def text_layer(parts, font, size, stroke, angle=-6, center=(N // 2, N // 2)):
    """Chunky letters (list of (text, color)) with a thick black outline, tilted."""
    f = ImageFont.truetype(font, size)
    layer = Image.new("RGBA", (N * 2, N * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    widths = [d.textlength(t, font=f) for t, _ in parts]
    gap = size * 0.02
    total = sum(widths) + gap * (len(parts) - 1)
    x = N - total / 2
    ascent, descent = f.getmetrics()
    y = N - (ascent + descent) / 2
    # Outline pass first so letters never overlap each other's outline.
    for (t, _), w in zip(parts, widths):
        d.text((x, y), t, font=f, fill=BLACK, stroke_width=stroke, stroke_fill=BLACK)
        x += w + gap
    x = N - total / 2
    for (t, color), w in zip(parts, widths):
        d.text((x, y), t, font=f, fill=color)
        x += w + gap
    layer = layer.rotate(angle, resample=Image.BICUBIC)
    box = (N - center[0], N - center[1], N - center[0] + N, N - center[1] + N)
    return layer.crop(box)

def shadow(layer, offset=(0, 22), blur=10, alpha=170):
    a = layer.split()[3].point(lambda v: v * alpha // 255)
    sh = Image.new("RGBA", layer.size, (0, 0, 0, 0))
    sh.putalpha(a)
    sh = sh.filter(ImageFilter.GaussianBlur(blur))
    out = Image.new("RGBA", layer.size, (0, 0, 0, 0))
    out.alpha_composite(sh, offset)
    return out

def bubble(color, box, tail, outline=26):
    """Chat bubble with a tail, black outline."""
    layer = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    x0, y0, x1, y1 = box
    r = (y1 - y0) // 3
    tx, ty = tail
    tail_poly = [(x0 + 120, y1 - 40), (tx, ty), (x0 + 300, y1 - 20)]
    d.rounded_rectangle([x0 - outline, y0 - outline, x1 + outline, y1 + outline], radius=r + outline, fill=BLACK)
    d.polygon([(px + (-outline if i == 1 else 0), py + (outline if i == 1 else 0)) for i, (px, py) in enumerate(tail_poly)], fill=BLACK)
    d.polygon([(x0 + 120 - outline, y1 - 40), (tx - outline * 1.6, ty + outline * 1.6), (x0 + 300 + outline, y1 - 20)], fill=BLACK)
    d.rounded_rectangle([x0, y0, x1, y1], radius=r, fill=color)
    d.polygon(tail_poly, fill=color)
    return layer

def concept_a():
    """A pink chat bubble holding JZS."""
    img = background()
    b = bubble(PINK, (130, 230, 894, 730), (150, 900))
    b = b.rotate(-4, resample=Image.BICUBIC, center=(N // 2, N // 2))
    img.alpha_composite(shadow(b))
    img.alpha_composite(b)
    t = text_layer([("J", WHITE), ("Z", WHITE), ("S", WHITE)], LILITA, 360, 22, angle=-6, center=(N // 2, 470))
    img.alpha_composite(shadow(t, (0, 14), 6, 140))
    img.alpha_composite(t)
    return img

def concept_b(img=None):
    """JZCord: the JZ wordmark (white J, pink Z) over a CORD pill."""
    img = background() if img is None else img
    t = text_layer([("J", WHITE), ("Z", PINK)], LILITA, 470, 28, angle=-6, center=(N // 2, 410))
    img.alpha_composite(shadow(t))
    img.alpha_composite(t)
    pill = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    pd = ImageDraw.Draw(pill)
    pd.rounded_rectangle([262 - 16, 660 - 16, 762 + 16, 820 + 16], radius=96, fill=BLACK)
    pd.rounded_rectangle([262, 660, 762, 820], radius=80, fill=PINK)
    pill = pill.rotate(-6, resample=Image.BICUBIC, center=(N // 2, 740))
    img.alpha_composite(shadow(pill, (0, 12), 6, 140))
    img.alpha_composite(pill)
    c = text_layer([("CORD", WHITE)], LUCKY, 150, 12, angle=-6, center=(N // 2, 748))
    img.alpha_composite(c)
    return img

def concept_c():
    """Compact monogram: JZ in white, S as a pink bubble — readable at icon size."""
    img = background()
    b = bubble(PINK, (520, 300, 900, 700), (560, 860), outline=24)
    b = b.rotate(-6, resample=Image.BICUBIC, center=(710, 500))
    img.alpha_composite(shadow(b))
    img.alpha_composite(b)
    jz = text_layer([("J", WHITE), ("Z", WHITE)], LILITA, 440, 26, angle=-6, center=(330, 500))
    img.alpha_composite(shadow(jz))
    img.alpha_composite(jz)
    s = text_layer([("S", WHITE)], LILITA, 330, 20, angle=-6, center=(712, 480))
    img.alpha_composite(s)
    return img

if len(sys.argv) > 3 and sys.argv[3] == "android":
    import os
    res = sys.argv[4]
    fg = concept_b(Image.new("RGBA", (N, N), (0, 0, 0, 0)))
    # Adaptive icons: keep the artwork inside the 72/108 safe zone.
    safe = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    k = 0.82
    small = fg.resize((int(N * k), int(N * k)), Image.LANCZOS)
    safe.alpha_composite(small, ((N - small.width) // 2, (N - small.height) // 2))
    bg = _layers_bg()
    legacy = concept_b()
    for density, (legacy_px, layer_px) in {"mdpi": (48, 108), "hdpi": (72, 162), "xhdpi": (96, 216), "xxhdpi": (144, 324), "xxxhdpi": (192, 432)}.items():
        d = f"{res}/mipmap-{density}"
        os.makedirs(d, exist_ok=True)
        legacy.resize((legacy_px, legacy_px), Image.LANCZOS).save(f"{d}/ic_launcher.png")
        safe.resize((layer_px, layer_px), Image.LANCZOS).save(f"{d}/ic_launcher_foreground.png")
        bg.convert("RGB").resize((layer_px, layer_px), Image.LANCZOS).save(f"{d}/ic_launcher_background.png")
    sys.exit(0)
if len(sys.argv) > 3 and sys.argv[3] == "final":
    concept_b().save(f"{OUT}/jzcord.png")
    sys.exit(0)
concepts = {"A": concept_a(), "B": concept_b(), "C": concept_c()}
for name, img in concepts.items():
    img.save(f"{OUT}/concept-{name}.png")

# Preview sheet: each concept large, plus at Discord icon sizes.
W = 3 * 360 + 40
sheet = Image.new("RGBA", (W, 520), (43, 45, 49, 255))
d = ImageDraw.Draw(sheet)
label = ImageFont.truetype(LILITA, 40)
for i, (name, img) in enumerate(concepts.items()):
    x = 20 + i * 360
    sheet.alpha_composite(img.resize((320, 320), Image.LANCZOS), (x, 20))
    d.text((x + 140, 350), name, font=label, fill=WHITE)
    for j, s in enumerate((96, 48, 24)):
        sheet.alpha_composite(img.resize((s, s), Image.LANCZOS), (x + [0, 120, 200][j], 420 + (96 - s) // 2))
sheet.save(f"{OUT}/preview.png")
print("ok")
