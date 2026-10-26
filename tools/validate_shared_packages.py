"""Verify schema 2.3 artifacts and migration against backed-up 2.2 metadata."""
from pathlib import Path
import hashlib
import json
import zipfile

ROOT = Path(__file__).resolve().parents[1] / 'language-packs-v2'

def sha(data):
    return hashlib.sha256(data).hexdigest()

def validate():
    catalog = json.loads((ROOT / 'catalog.json').read_text())
    assert catalog['schemaVersion'] == '2.3'
    entries = {e['language']: e for e in catalog['packages']}
    assert len(entries) == len(catalog['packages'])
    base = entries['base']
    dependency = dict(language='base', version=base['version'], manifestSha256=base['manifestSha256'])
    summary = {}
    for lang, entry in entries.items():
        folder = ROOT / 'packages' / lang
        manifest_bytes = (folder / 'manifest.json').read_bytes()
        manifest = json.loads(manifest_bytes)
        assert sha(manifest_bytes) == entry['manifestSha256']
        assert manifest['schemaVersion'] == '2.3'
        assert manifest['packageVersion'] == entry['version']
        assert manifest['dependencies'] == entry['dependencies'] == ([] if lang == 'base' else [dependency])
        files = {f['path']: f for f in manifest['files']}
        assert len(files) == len(manifest['files'])
        for name, info in files.items():
            assert not name.startswith('/') and '..' not in Path(name).parts
            data = (folder / name).read_bytes()
            assert len(data) == info['bytes'] and sha(data) == info['sha256'], (lang, name)
        archive = ROOT / entry['localArchive']
        assert sha(archive.read_bytes()) == entry['archiveSha256']
        assert archive.stat().st_size == entry['downloadBytes']
        with zipfile.ZipFile(archive) as z:
            assert set(z.namelist()) == set(files) | {'manifest.json'}
            assert z.read('manifest.json') == manifest_bytes
            for name, info in files.items():
                assert sha(z.read(name)) == info['sha256']
        if lang == 'base':
            assert all(name.startswith('images/') for name in files)
            summary[lang] = dict(images=len(files), downloadBytes=entry['downloadBytes'])
            continue
        db = json.loads((folder / 'database.json').read_text())
        backup = ROOT / 'production/shared-package-migration/original-metadata' / lang
        old = json.loads((backup / 'database.json').read_text())
        old_files = {f['path']: f for f in json.loads((backup / 'manifest.json').read_text())['files']}
        assert len(db['words']) == len(old['words'])
        shared = set(); local = set(); missing = set()
        for previous, word in zip(old['words'], db['words']):
            assert {k:v for k,v in previous.items() if k != 'image'} == {k:v for k,v in word.items() if k != 'image'}
            assert {k:v for k,v in previous['image'].items() if k not in ('available','package')} == {k:v for k,v in word['image'].items() if k not in ('available','package')}
            media = word['image']; path = media.get('overridePath') or media['path']
            shared_image = media.get('package') == 'base'
            image_file = (ROOT / 'packages/base' if shared_image else folder) / path
            (shared if shared_image else local).add(path)
            assert image_file.is_file() == media['available']
            if not image_file.is_file(): missing.add(path)
            if path in old_files:
                assert sha(image_file.read_bytes()) == old_files[path]['sha256'], (lang, path)
            if shared_image: assert not (folder / path).exists(), (lang, 'duplicate', path)
        for name, info in old_files.items():
            if name.startswith('audio/'):
                assert files[name]['sha256'] == info['sha256'], (lang, name)
        if lang in ('tr', 'en'): assert not missing
        summary[lang] = dict(words=len(db['words']), sharedImages=len(shared), localImagePaths=len(local), missingImages=len(missing), downloadBytes=entry['downloadBytes'])
    output = ROOT / 'production/shared-package-migration/validation.json'
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(dict(result='PASS', packages=summary), ensure_ascii=False, indent=2)+'\n')
    print('PASS: all manifests, archives and payload hashes; unchanged word records/audio/image pixels; no duplicate shared images; language dependencies and localized image roots.')
    print(json.dumps(summary, ensure_ascii=False))

if __name__ == '__main__': validate()
