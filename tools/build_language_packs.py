"""Build independent, auditable learning-language packages; never publish drafts."""
import hashlib
import json
from pathlib import Path
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'language-packs-v2'
SOURCE = ROOT / 'localization-output/kidswordplus_v2_11_languages.draft.json'
ARCHIVE = Path('/Users/kursatdemirci/Projects/KidsWordPlus/BACKUP/sounds_slides.zip')
NAMES = dict(zip(['en','tr','de','fr','es','zh','ja','ko','it','pt','hi'],
                ['English','Türkçe','Deutsch','Français','Español','中文','日本語','한국어','Italiano','Português','हिन्दी']))

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n')

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def build():
    source = json.loads(SOURCE.read_text())
    OUT.mkdir(exist_ok=True)
    catalog = {'schemaVersion': '2.2', 'catalogVersion': '1-draft',
               'uiLanguages': ['tr','en'], 'packages': []}
    archive = zipfile.ZipFile(ARCHIVE) if ARCHIVE.exists() else None
    files = {Path(n).name: n for n in archive.namelist() if not n.endswith('/')} if archive else {}
    for lang in source['Languages']:
        folder = OUT / 'packages' / lang
        folder.mkdir(parents=True, exist_ok=True)
        words, missing_audio, missing_images, review = [], set(), set(), []
        existing_path = folder / 'database.json'
        existing_database = json.loads(existing_path.read_text()) if existing_path.exists() else None
        for record in ([] if existing_database else source['Records']):
            t = record['Translations'][lang]
            audio_path = 'audio/' + t['AudioFile']
            image_path = 'images/' + record['Image']
            # Existing archives contain shared pictures. Each downloadable ZIP remains standalone.
            for relative, name, missing in [(audio_path,t['AudioFile'],missing_audio),
                                            (image_path,record['Image'],missing_images)]:
                target = folder / relative
                target.parent.mkdir(parents=True, exist_ok=True)
                reused = ROOT / 'audio-output/reused_audio_additions' / name
                if name in files:
                    target.write_bytes(archive.read(files[name]))
                elif reused.is_file():
                    shutil.copyfile(reused,target)
                if not target.is_file():
                    missing.add(relative)
            # Existing text is still editorial draft. Clock and alphabet require special review.
            issues = []
            if not t['DisplayText']: issues.append('missing_translation')
            if t['TranslationStatus'] == 'curriculum_review_required': issues.append('language_curriculum')
            if record['Category'] == 'clocks' and lang != 'en': issues.append('clock_grammar_review')
            if issues: review.append({'conceptId': record['Key'], 'issues': issues})
            words.append({'conceptId': record['Key'], 'categoryId': record['Category'],
                          'subcategoryId': record['Subcategory'], 'lessonNumber': record['Lesson'],
                          'lessonPosition': record['LessonPosition'], 'difficulty': record['Difficulty'],
                          'displayText': t['DisplayText'], 'spokenText': t['SpokenText'],
                          'translationStatus': t['TranslationStatus'], 'reviewIssues': issues,
                          'audio': {'path': audio_path, 'available': audio_path not in missing_audio},
                          'image': {'path': image_path, 'available': image_path not in missing_images,
                                    'variant': 'shared', 'overridePath': None}})
        if existing_database:
            words = existing_database['words']
            for word in words:
                for kind, missing in [('audio',missing_audio),('image',missing_images)]:
                    available = (folder / word[kind]['path']).is_file()
                    word[kind]['available'] = available
                    if not available: missing.add(word[kind]['path'])
                issues = list(word.get('reviewIssues', []))
                if not word.get('displayText') and 'missing_translation' not in issues:
                    issues.append('missing_translation')
                if issues: review.append({'conceptId':word['conceptId'],'issues':issues})
        database = existing_database or {'schemaVersion': '2.2', 'contentVersion': '1-draft', 'language': lang,
                    'locale': source['Locales'][lang], 'wordCount': len(words), 'words': words}
        database['wordCount'] = len(words)
        version = database['contentVersion']
        write(folder / 'database.json', database)
        inventory = [{'path': p.relative_to(folder).as_posix(), 'bytes': p.stat().st_size,
                      'sha256': digest(p)} for p in sorted(folder.rglob('*'))
                     if p.is_file() and p.name != 'manifest.json' and not any(part.startswith('.') or part == '__MACOSX' for part in p.relative_to(folder).parts)]
        manifest = {'schemaVersion': '2.2', 'packageId': 'learning.'+lang,
                    'packageVersion': version, 'language': lang, 'locale': database['locale'],
                    'minimumReaderSchemaVersion': '2.2', 'databasePath': 'database.json',
                    'standalone': True, 'dependencies': [], 'releaseReady': False,
                    'releaseBlockers': ['editorial_review_required'] +
                        (['missing_audio'] if missing_audio else []) +
                        (['missing_images'] if missing_images else []) +
                        (['translation_or_curriculum_review'] if review else []),
                    'installedPayloadBytes': sum(f['bytes'] for f in inventory), 'files': inventory}
        write(folder / 'manifest.json', manifest)
        write(OUT / 'reports' / (lang+'.json'), {'language': lang, 'wordCount':len(words),
              'missingAudio': sorted(missing_audio), 'missingImages': sorted(missing_images),
              'missingRequiredAudio': sorted({w['audio']['path'] for w in words
                   if not w['audio']['available'] and w.get('learningEnabled',True)
                   and w['audio'].get('required',True)}),
              'inactiveConceptIds': [w['conceptId'] for w in words if not w.get('learningEnabled',True)],
              'reviewRecords': review})
        zip_path = OUT / 'archives' / (lang+'-'+version+'.zip')
        zip_path.parent.mkdir(exist_ok=True)
        with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as z:
            for relative in [f['path'] for f in inventory] + ['manifest.json']:
                z.write(folder / relative, relative)
        catalog['packages'].append({'packageId':manifest['packageId'], 'language':lang,
              'nativeName':NAMES[lang], 'version':version, 'availability':'draft',
              'downloadUrl':None, 'localArchive':str(zip_path.relative_to(OUT)),
              'downloadBytes':zip_path.stat().st_size, 'archiveSha256':digest(zip_path),
              'manifestSha256':digest(folder/'manifest.json'),
              'installedBytes':sum(f['bytes'] for f in inventory) + (folder/'manifest.json').stat().st_size,
              'wordCount':len(words), 'missingAudioCount':len(missing_audio),
              'missingImageCount':len(missing_images), 'reviewRecordCount':len(review)})
        print(lang, len(words), 'words;', len(missing_audio), 'missing audio;',len(missing_images),'missing images')
    if archive: archive.close()
    write(OUT / 'catalog.json', catalog)

if __name__ == '__main__':
    existing = OUT / 'catalog.json'
    if existing.exists() and json.loads(existing.read_text()).get('schemaVersion') == '2.3':
        from split_shared_package import build as build_shared
        build_shared()
    else:
        build()
