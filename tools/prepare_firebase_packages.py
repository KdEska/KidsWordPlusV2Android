"""Prepare Firebase Storage files locally; never upload or change source packages."""
from pathlib import Path
import argparse, hashlib, json, plistlib, shutil, zipfile
from urllib.parse import quote

ROOT = Path(__file__).resolve().parents[1]
PREFIX = 'language-packs/v2'

def digest(data): return hashlib.sha256(data).hexdigest()
def media_url(bucket, path):
    return f'https://firebasestorage.googleapis.com/v0/b/{quote(bucket, safe="")}/o/{quote(path, safe="")}?alt=media'

def prepare(channel, output):
    config = plistlib.loads((ROOT/'KidsWordPlusV2/Configuration/GoogleService-Info.plist').read_bytes())
    bucket = config['STORAGE_BUCKET']
    catalog = json.loads((ROOT/'language-packs-v2/catalog.json').read_text())
    selected = [e for e in catalog['packages'] if e['language'] in ('base', 'tr', 'en', 'fr', 'de', 'es', 'pt', 'zh', 'ja', 'ko', 'it', 'hi', 'ar', 'ru')]
    assert len(selected) == 14 and {e['language'] for e in selected} == {'base', 'tr', 'en', 'fr', 'de', 'es', 'pt', 'zh', 'ja', 'ko', 'it', 'hi', 'ar', 'ru'}
    prepared = []
    for entry in selected:
        archive = ROOT/'language-packs-v2'/entry['localArchive']
        raw = archive.read_bytes()
        assert len(raw) == entry['downloadBytes'] and digest(raw) == entry['archiveSha256'], 'Archive mismatch'
        with zipfile.ZipFile(archive) as z:
            manifest_raw = z.read('manifest.json')
        assert digest(manifest_raw) == entry['manifestSha256'], 'Manifest mismatch'
        manifest = json.loads(manifest_raw)
        assert manifest['dependencies'] == entry.get('dependencies', [])
        if channel == 'release' and (not manifest.get('releaseReady') or manifest.get('releaseBlockers')):
            raise ValueError(f"{entry['language']} is not release ready: {manifest.get('releaseBlockers', [])}")
        object_path = f"{PREFIX}/archives/{entry['language']}-{entry['archiveSha256']}.zip"
        result = {k:v for k,v in entry.items() if k != 'localArchive'}
        result.update(availability='published' if channel == 'release' else 'draft', downloadUrl=media_url(bucket, object_path))
        prepared.append((archive, object_path, result))
    # All release gates and hashes pass before any output is written.
    for source, object_path, _ in prepared:
        dest = output/'upload'/object_path; dest.parent.mkdir(parents=True, exist_ok=True)
        if not dest.exists() or digest(dest.read_bytes()) != digest(source.read_bytes()): shutil.copy2(source, dest)
    catalog_path = f'{PREFIX}/' + ('catalog.json' if channel == 'release' else 'preview/catalog.json')
    result = {'schemaVersion':'2.3', 'catalogVersion':catalog['catalogVersion']+'-firebase-'+channel,
              'packages':[e for _,_,e in prepared]}
    dest = output/'upload'/catalog_path; dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(result, ensure_ascii=False, indent=2)+'\n')
    plan = {'bucket':bucket, 'channel':channel, 'catalogObject':catalog_path, 'catalogURL':media_url(bucket,catalog_path),
            'archives':[{'source':str((output/'upload'/path).relative_to(ROOT)) if output.is_relative_to(ROOT) else str(output/'upload'/path),
                         'object':path, 'sha256':entry['archiveSha256'], 'bytes':entry['downloadBytes']} for _,path,entry in prepared],
            'uploadOrder':'archives first, catalog last', 'uploaded':False}
    (output/f'{channel}-upload-plan.json').write_text(json.dumps(plan,ensure_ascii=False,indent=2)+'\n')
    print(f'Prepared {channel}: {len(prepared)} archives, catalog {dest}; no upload performed.')

if __name__ == '__main__':
    parser=argparse.ArgumentParser(); parser.add_argument('--channel',choices=['preview','release'],default='preview')
    parser.add_argument('--output',type=Path,default=ROOT/'firebase-storage')
    args=parser.parse_args();prepare(args.channel,args.output.resolve())
