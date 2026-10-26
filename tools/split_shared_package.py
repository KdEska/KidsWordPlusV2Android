"""Build schema 2.3 base + language archives without changing words or audio.
Existing standalone archives are retained; source metadata is backed up on first run.
"""
from pathlib import Path
import hashlib
import json
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'language-packs-v2'

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')

def inventory(folder):
    return [{'path': p.relative_to(folder).as_posix(), 'bytes': p.stat().st_size, 'sha256': digest(p)}
            for p in sorted(folder.rglob('*')) if p.is_file() and p.name != 'manifest.json'
            and not any(part.startswith('.') for part in p.relative_to(folder).parts)]

def archive_entry(folder, manifest, native_name, **extra):
    write(folder / 'manifest.json', manifest)
    path = OUT / 'archives' / (manifest['language'] + '-' + manifest['packageVersion'] + '.zip')
    path.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED) as z:
        for name in [f['path'] for f in manifest['files']] + ['manifest.json']:
            z.write(folder / name, name)
    return dict(packageId=manifest['packageId'], language=manifest['language'], nativeName=native_name,
                version=manifest['packageVersion'], availability='draft', downloadUrl=None,
                localArchive=str(path.relative_to(OUT)), downloadBytes=path.stat().st_size,
                archiveSha256=digest(path), manifestSha256=digest(folder / 'manifest.json'),
                installedBytes=sum(f['bytes'] for f in manifest['files']) + (folder / 'manifest.json').stat().st_size,
                dependencies=manifest['dependencies'], **extra)

def build():
    catalog = json.loads((OUT / 'catalog.json').read_text())
    entries = [e for e in catalog['packages'] if e['language'] != 'base']
    backup = OUT / 'production/shared-package-migration/original-metadata'
    if not backup.exists():
        backup.mkdir(parents=True)
        shutil.copy2(OUT / 'catalog.json', backup / 'catalog.json')
        for entry in entries:
            folder = backup / entry['language']; folder.mkdir()
            for name in ['database.json', 'manifest.json']:
                shutil.copy2(OUT / 'packages' / entry['language'] / name, folder / name)
    base = OUT / 'packages/base'; base.mkdir(exist_ok=True)
    databases = {e['language']: json.loads((OUT / 'packages' / e['language'] / 'database.json').read_text()) for e in entries}
    shared = {}
    for lang, db in databases.items():
        folder = OUT / 'packages' / lang
        for word in db['words']:
            media = word['image']
            path = media.get('overridePath') or media['path']
            if media.get('variant') != 'shared' or (word['categoryId'], word['subcategoryId']) == ('school', 'alphabet'):
                continue
            if not path.startswith('images/') or '..' in Path(path).parts: raise ValueError(path)
            source = folder / path
            if not source.is_file(): source = base / path
            if source.is_file():
                previous = shared.get(path)
                if previous and digest(previous) != digest(source): raise ValueError('Conflicting shared image: ' + path)
                shared[path] = source
    for path, source in shared.items():
        target = base / path
        target.parent.mkdir(parents=True, exist_ok=True)
        if source != target: shutil.copy2(source, target)
    files = inventory(base)
    base_version = '1-shared-' + hashlib.sha256(json.dumps(files, sort_keys=True).encode()).hexdigest()[:12]
    base_manifest = dict(schemaVersion='2.3', packageId='shared.base', packageVersion=base_version,
                         language='base', minimumReaderSchemaVersion='2.3', standalone=True, dependencies=[],
                         releaseReady=False, releaseBlockers=['editorial_review_required'],
                         installedPayloadBytes=sum(f['bytes'] for f in files), files=files)
    base_entry = archive_entry(base, base_manifest, 'Temel dil paketi')
    dependency = dict(language='base', version=base_version, manifestSha256=base_entry['manifestSha256'])
    result = [base_entry]
    for entry in entries:
        lang = entry['language']; folder = OUT / 'packages' / lang; db = databases[lang]
        moved = set(); local = set()
        for word in db['words']:
            media = word['image']; path = media.get('overridePath') or media['path']
            is_local = media.get('variant') != 'shared' or (word['categoryId'], word['subcategoryId']) == ('school', 'alphabet')
            if not is_local and path in shared:
                media['package'] = 'base'; media['available'] = True; moved.add(path)
            else:
                previous_package = media.pop('package', None); local.add(path)
                if previous_package == 'base' and not (folder / path).exists():
                    (folder / path).parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(base / path, folder / path)
                media['available'] = (folder / path).is_file()
        # Delete only verified duplicates after the base copy is complete.
        for path in moved - local:
            old = folder / path
            if old.exists():
                if digest(old) != digest(base / path): raise ValueError('Image changed during split: ' + path)
                old.unlink()
        db['schemaVersion'] = '2.3'
        old_version = db.pop('contentVersion').split('-shared-')[0]
        db['contentVersion'] = old_version + '-shared-' + hashlib.sha256(json.dumps(db, sort_keys=True).encode()).hexdigest()[:12]
        write(folder / 'database.json', db)
        manifest = json.loads((folder / 'manifest.json').read_text())
        files = inventory(folder)
        manifest.update(schemaVersion='2.3', packageVersion=db['contentVersion'], minimumReaderSchemaVersion='2.3',
                        standalone=False, dependencies=[dependency], files=files,
                        installedPayloadBytes=sum(f['bytes'] for f in files))
        report = json.loads((OUT / 'reports' / (lang + '.json')).read_text())
        report['missingImages'] = sorted({w['image'].get('overridePath') or w['image']['path'] for w in db['words'] if not w['image']['available']})
        report['sharedImageCount'] = len(moved)
        report['localImageCount'] = len(local)
        write(OUT / 'reports' / (lang + '.json'), report)
        manifest['releaseBlockers'] = [b for b in manifest.get('releaseBlockers',[]) if b != 'missing_images' or report['missingImages']]
        result.append(archive_entry(folder, manifest, entry['nativeName'], wordCount=len(db['words']),
                                    missingAudioCount=entry.get('missingAudioCount',0), missingImageCount=len(report['missingImages']),
                                    reviewRecordCount=entry.get('reviewRecordCount',0)))
        print(lang, 'shared images', len(moved), 'local images', len(local), 'archive bytes', result[-1]['downloadBytes'])
    catalog.update(schemaVersion='2.3', catalogVersion='2-shared-draft', packages=result)
    write(OUT / 'catalog.json', catalog)
    print('base images', len(shared), 'archive bytes', base_entry['downloadBytes'])

if __name__ == '__main__': build()
