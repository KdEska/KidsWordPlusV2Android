"""Create Android WebP derivatives; all reference inputs stay read-only."""
from pathlib import Path
from PIL import Image
import hashlib, json, re, shutil, sys
from concurrent.futures import ThreadPoolExecutor
pool = ThreadPoolExecutor(max_workers=3)
futures = []
project = Path(__file__).resolve().parents[1]
reference = project.parent
output = project / 'app/src/main/assets/ui'
records = []
def convert_one(source, relative, side=512):
    source = reference / source
    target = output / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    reuse = False
    if target.exists() and target.stat().st_mtime >= source.stat().st_mtime and '--force' not in sys.argv:
        try:
            with Image.open(target) as image:
                width, height = image.size
                image.load()
                reuse = True
        except (OSError, ValueError):
            pass
    if not reuse:
        with Image.open(source) as image:
            image = image.convert('RGBA')
            image.thumbnail((side, side), Image.Resampling.LANCZOS)
            temporary = target.with_name(target.stem + '.tmp.webp')
            image.save(temporary, 'WEBP', quality=90, method=4, exact=True)
            temporary.replace(target)
            width, height = image.size
    records.append(dict(source=str(source.relative_to(reference)), target='ui/'+relative, width=width, height=height,
        sourceSha256=hashlib.sha256(source.read_bytes()).hexdigest(), outputSha256=hashlib.sha256(target.read_bytes()).hexdigest(), bytes=target.stat().st_size))
def convert(source, relative, side=512):
    futures.append(pool.submit(convert_one, source, relative, side))
# Keep the same category choices as the current iOS app.
swift = (reference/'KidsWordPlusV2/App/HomeViews.swift').read_text()
section = swift.split('private static let files: [String: String] = [')[1].split(']')[0]
for category, name in re.findall(r'"([a-z_]+)": "([\w.-]+\.png)"', section):
    convert('design/main-category-assets-reference/'+name, 'categories/'+category+'.webp')
for source in sorted((reference/'design/subcategory-assets-reference').rglob('*.png')):
    convert(str(source.relative_to(reference)), 'subcategories/'+str(source.relative_to(reference/'design/subcategory-assets-reference').with_suffix('.webp')))
for directory, target in [('design/navigation-status-icons-reference','navigation'),('design/phase-2-3-assets-reference/phase-2','games'),('design/phase-2-3-assets-reference/phase-3','parent'),('design/chess/menu','chess')]:
    for source in sorted((reference/directory).glob('*.png')):
        convert(str(source.relative_to(reference)), target+'/'+source.stem+'.webp', 768 if 'hero' in source.name else 384)
convert('design/assets-phase-1-3/backgrounds/learning-garden.png','backgrounds/garden.webp',1800)
convert('design/assets-phase-1-3/backgrounds/learning-landscape.png','backgrounds/landscape.webp',1800)
convert('design/assets-phase-1-3/brand/kidswordplus-logo.png','brand/logo.webp',1000)
convert('design/chess/world-hero.png','chess/world.webp',1200)
for name in ['piko-wave','piko-celebrate','piko-controller']:
    convert('design/assets-phase-1-3/characters/'+name+'.png','characters/'+name+'.webp',512)
for future in futures:
    future.result()
pool.shutdown()
records.sort(key=lambda record: record['target'])
icon_source=reference/'KidsWordPlusV2/Assets.xcassets/AppIcon.appiconset/icon.png'
icon_target=project/'app/src/main/res/drawable-nodpi/launcher_art.png'
icon_target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(icon_source,icon_target)
(project/'docs/ui-assets.json').write_text(json.dumps(dict(derivatives=records, launcherSource=str(icon_source.relative_to(reference)),launcherSha256=hashlib.sha256(icon_target.read_bytes()).hexdigest()),ensure_ascii=False,indent=2)+'\n')
print(f'{len(records)} WebP assets, {sum(x["bytes"] for x in records):,} bytes; original launcher copied.')
