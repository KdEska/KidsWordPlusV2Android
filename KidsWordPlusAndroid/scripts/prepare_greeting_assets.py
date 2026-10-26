"""Prepare aligned iOS greeting poses; reference PNGs and atlases stay read-only."""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
from PIL import Image
import hashlib, json

project = Path(__file__).resolve().parents[1]
reference = project.parent / 'design/splash'
output = project / 'app/src/main/assets/ui/greetings'
poses = ('wave-out', 'wave-middle', 'wave-in', 'wink')

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def prepare(folder):
    records = []
    target = output / ('piko-default' if folder.name == 'piko' else folder.name)
    target.mkdir(parents=True, exist_ok=True)
    if folder.name == 'piko':
        images = {}
        for pose in poses:
            with Image.open(folder / f'{pose}.png') as original:
                images[pose] = original.convert('RGBA')
        sources = {f'{pose}.png': sha(folder / f'{pose}.png') for pose in poses}
    else:
        spec = json.loads((folder / 'frames.json').read_text())
        assert spec['canvas'] == 512 and spec['scale'] > 0
        assert {f['name'] for f in spec['frames']} == set(poses)
        with Image.open(folder / 'atlas.png') as atlas:
            atlas = atlas.convert('RGBA')
            images = {}
            for frame in spec['frames']:
                x, y, w, h = (frame[k] for k in ('x', 'y', 'width', 'height'))
                assert 0 <= x < x+w <= atlas.width and 0 <= y < y+h <= atlas.height
                cell = atlas.crop((x, y, x+w, y+h))
                scale = spec['scale']
                dx = spec['anchor'] - frame['anchorX'] * scale
                dy = spec['baseline'] - frame['baselineY'] * scale
                images[frame['name']] = cell.transform((512, 512), Image.Transform.AFFINE,
                    (1/scale, 0, -dx/scale, 0, 1/scale, -dy/scale), Image.Resampling.BICUBIC)
        sources = {name: sha(folder / name) for name in ('atlas.png', 'frames.json')}
    for pose, image in images.items():
        image.thumbnail((512, 512), Image.Resampling.LANCZOS)
        path = target / f'{pose}.webp'
        image.save(path, 'WEBP', quality=92, method=4, exact=True)
        records.append(dict(character=target.name, pose=pose, path=str(path.relative_to(project / 'app/src/main/assets')),
            width=image.width, height=image.height, bytes=path.stat().st_size, sha256=sha(path),
            reference=str(folder.relative_to(project.parent)), sourceHashes=sources))
    return records

if __name__ == '__main__':
    folders = [p for p in sorted(reference.iterdir()) if p.is_dir() and (p.name == 'piko' or (p/'frames.json').exists())]
    with ThreadPoolExecutor(max_workers=3) as pool:
        records = [r for group in pool.map(prepare, folders) for r in group]
    assert len(records) == 84 and len({r['character'] for r in records}) == 21
    (project / 'docs/greeting-assets.json').write_text(json.dumps(records, ensure_ascii=False, indent=2)+'\n')
    print(f"Prepared {len(records)} poses, {sum(r['bytes'] for r in records)} bytes")
