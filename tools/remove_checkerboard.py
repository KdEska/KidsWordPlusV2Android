from pathlib import Path
from collections import deque
from PIL import Image, ImageFilter
import sys

for filename in sys.argv[1:]:
    path = Path(filename)
    image = Image.open(path).convert("RGBA")
    rgb = image.convert("RGB")
    w, h = image.size
    px = rgb.load()
    mask = Image.new("L", (w, h), 255)
    mp = mask.load()
    seen = bytearray(w * h)
    q = deque()
    for x in range(w):
        q.append((x, 0)); q.append((x, h - 1))
    for y in range(h):
        q.append((0, y)); q.append((w - 1, y))

    def background(x, y):
        r, g, b = px[x, y]
        # Generated transparency previews may bake either a very light or a
        # medium-gray checkerboard into the RGB pixels. Only flood-fill neutral
        # pixels connected to the canvas boundary, so enclosed white details
        # within the subject remain intact.
        return max(r, g, b) - min(r, g, b) < 20 and min(r, g, b) > 52

    while q:
        x, y = q.popleft()
        i = y * w + x
        if seen[i] or not background(x, y):
            continue
        seen[i] = 1
        mp[x, y] = 0
        if x: q.append((x - 1, y))
        if x + 1 < w: q.append((x + 1, y))
        if y: q.append((x, y - 1))
        if y + 1 < h: q.append((x, y + 1))

    mask = mask.filter(ImageFilter.GaussianBlur(0.8))
    image.putalpha(mask)
    image.save(path)
