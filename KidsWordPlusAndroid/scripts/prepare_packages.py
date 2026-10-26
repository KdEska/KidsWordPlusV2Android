"""Prepare complete local development packages; reference content stays read-only."""
import argparse
import hashlib
import json
import shutil
from pathlib import Path
root = Path(__file__).resolve().parents[2]
source = root / 'language-packs-v2'
assets = root / 'KidsWordPlusAndroid/app/src/debug/assets/packages'
assets.mkdir(parents=True, exist_ok=True)
catalog = json.loads((source / 'catalog.json').read_text())
parser = argparse.ArgumentParser()
parser.add_argument('--bundle', action='store_true', help='Copy ZIPs for -PbundleLocalPackages=true builds')
args = parser.parse_args()
entries = []
for original in catalog['packages']:
    if original['language'] not in ('base','tr','en','fr','de','es','pt','zh','ja','ko','it','hi','ar','ru'):
        continue
    entry = dict(original)
    archive = source / entry['localArchive']
    digest = hashlib.file_digest(archive.open('rb'), 'sha256').hexdigest()
    if digest != entry['archiveSha256'] or archive.stat().st_size != entry['downloadBytes']:
        raise ValueError(f'Archive verification failed: {archive.name}')
    name = f"{entry['language']}-{digest}.zip"
    target = assets / name
    if args.bundle and (not target.exists() or hashlib.file_digest(target.open('rb'), 'sha256').hexdigest() != digest):
        shutil.copyfile(archive, target)
    entry['assetArchive'] = name
    entry.pop('localArchive', None)
    entry['downloadUrl'] = None
    entries.append(entry)
(assets / 'catalog.json').write_text(json.dumps({'schemaVersion': '2.3', 'packages': entries}, ensure_ascii=False, indent=2))
print('Complete Debug packages verified and prepared: base + 13 learning languages.')
