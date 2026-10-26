"""Import approved bilingual vocabulary media and rebuild local package archives."""
import csv
import hashlib
import json
from pathlib import Path
import shutil
import zipfile
from split_shared_package import inventory, write, digest

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'language-packs-v2'
WORK = OUT / 'production/vocabulary-250'
CATEGORIES = ['actions', 'opposites', 'feelings', 'daily_routines', 'positions_directions',
              'properties', 'time_sequence', 'social_skills']

def main():
    rows = list(csv.DictReader((ROOT / 'design/vocabulary-catalog/progress.csv').open(encoding='utf-8-sig')))
    assert len(rows) == 250 and len({r['ID'] for r in rows}) == 250
    catalog = json.loads((OUT / 'catalog.json').read_text())
    backup = WORK / 'before'
    if backup.exists():
        catalog = json.loads((backup / 'catalog.json').read_text())
    # Validate every source before changing any package.
    for r in rows:
        assert r['status'] == 'generated'
        assert (ROOT / 'design/vocabulary-catalog' / r['asset']).is_file()
        for lang in ['tr', 'en']:
            assert (ROOT / 'audio-output/vocabulary-250' / lang / (r['ID'] + '_' + lang + '.mp3')).is_file()
    for entry in catalog['packages']:
        folder = OUT / 'packages' / entry['language']
        for name in ['manifest.json', 'database.json']:
            if (folder / name).exists() and not (backup / entry['language'] / name).exists():
                target = backup / entry['language'] / name
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(folder / name, target)
    if not (backup / 'catalog.json').exists():
        shutil.copy2(OUT / 'catalog.json', backup / 'catalog.json')
    for r in rows:
        dest = OUT / 'packages/base/images/vocabulary-250' / (r['ID'] + '.png')
        if dest.exists():
            assert digest(dest) == digest(ROOT / 'design/vocabulary-catalog' / r['asset'])
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(ROOT / 'design/vocabulary-catalog' / r['asset'], dest)
    for lang, field in [('tr', 'Türkçe'), ('en', 'İngilizce')]:
        folder = OUT / 'packages' / lang
        db = json.loads((backup / lang / 'database.json').read_text())
        assert not any(w['conceptId'].startswith('CAT-') for w in db['words'])
        for r in rows:
            _, category, position = r['ID'].split('-')
            category = CATEGORIES[int(category)-1]
            position = int(position)
            audio = 'audio/' + r['ID'] + '_' + lang + '.mp3'
            shutil.copy2(ROOT / 'audio-output/vocabulary-250' / lang / Path(audio).name, folder / audio)
            db['words'].append(dict(conceptId=r['ID'], categoryId=category, subcategoryId=category,
                lessonNumber=(position-1)//10+1, lessonPosition=(position-1)%10+1,
                difficulty='beginner', displayText=r[field], spokenText=r[field],
                translationStatus='user_supplied', reviewIssues=[],
                audio=dict(path=audio, available=True, textAlignmentStatus='generated_from_spoken_text'),
                image=dict(path='images/vocabulary-250/'+r['ID']+'.png', package='base', available=True,
                    variant='shared', overridePath=None, source='vocabulary_250_generated',
                    mappingStatus='exact_catalog_id', visualReviewStatus='user_approved'),
                character=r['Karakter'], sceneDescription=r['Görsel sahnesi']))
        db['wordCount'] = len(db['words'])
        db['activeWordCount'] = sum(w.get('learningEnabled', True) for w in db['words'])
        db['baseConceptCount'] = db.get('baseConceptCount', len(db['words'])-250-db.get('languageSpecificConceptCount',0)) + 250
        db['contentVersion'] = ('11' if lang == 'tr' else '5') + '-' + lang + '-vocabulary-250-draft'
        write(folder / 'database.json', db)
        report = json.loads((OUT / 'reports' / (lang+'.json')).read_text())
        report.update(wordCount=len(db['words']), sharedImageCount=len({w['image']['path'] for w in db['words'] if w['image'].get('package') == 'base'}))
        write(OUT / 'reports' / (lang+'.json'), report)
    base_dependency = None
    for entry in sorted(catalog['packages'], key=lambda e: e['language'] != 'base'):
        lang = entry['language']; folder = OUT / 'packages' / lang
        manifest = json.loads((folder / 'manifest.json').read_text())
        files = inventory(folder)
        fingerprint = hashlib.sha256(json.dumps(files, sort_keys=True).encode()).hexdigest()[:12]
        version = ('2-shared-vocabulary-' if lang == 'base' else entry['version'].split('-vocab250-')[0]+'-vocab250-') + fingerprint
        if lang in ('tr', 'en'):
            version = json.loads((folder / 'database.json').read_text())['contentVersion']
        manifest.update(packageVersion=version, files=files,
            installedPayloadBytes=sum(f['bytes'] for f in files), dependencies=[] if lang == 'base' else [base_dependency])
        write(folder / 'manifest.json', manifest)
        archive = OUT / 'archives' / (lang+'-'+version+'.zip')
        with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as z:
            for name in [f['path'] for f in files]+['manifest.json']: z.write(folder/name, name)
        entry.update(version=version, localArchive=str(archive.relative_to(OUT)),
            archiveSha256=digest(archive), manifestSha256=digest(folder/'manifest.json'),
            downloadBytes=archive.stat().st_size,
            installedBytes=manifest['installedPayloadBytes']+(folder/'manifest.json').stat().st_size,
            dependencies=manifest['dependencies'], availability='draft', downloadUrl=None)
        if lang == 'base':
            base_dependency = dict(language='base', version=version, manifestSha256=entry['manifestSha256'])
        elif lang in ('tr','en'):
            entry['wordCount'] = json.loads((folder/'database.json').read_text())['wordCount']
        print(lang, 'rebuilt', flush=True)
    catalog['catalogVersion'] += '-vocabulary-250'
    write(OUT / 'catalog.json', catalog)
    write(WORK / 'import-report.json', dict(addedImages=250, addedAudio=500,
        addedWordsPerLanguage=250, categories=CATEGORIES, status='imported_pending_validation'))

if __name__ == '__main__': main()
