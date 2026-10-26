"""Validate package integrity, imported source mapping and existing content preservation."""
import csv
import json
import zipfile
from pathlib import Path
from split_shared_package import digest, write
from import_vocabulary_250 import ROOT, OUT, WORK
import hashlib

def main():
    catalog = json.loads((OUT/'catalog.json').read_text())
    base = next(e for e in catalog['packages'] if e['language']=='base')
    dependency = dict(language='base', version=base['version'], manifestSha256=base['manifestSha256'])
    rows = list(csv.DictReader((ROOT/'design/vocabulary-catalog/progress.csv').open(encoding='utf-8-sig')))
    summary = {}
    for e in catalog['packages']:
        lang=e['language']; folder=OUT/'packages'/lang
        m=json.loads((folder/'manifest.json').read_text())
        old=json.loads((WORK/'before'/lang/'manifest.json').read_text())
        assert digest(folder/'manifest.json')==e['manifestSha256']
        assert m['packageVersion']==e['version']
        assert m['dependencies']==e['dependencies']==([] if lang=='base' else [dependency])
        files={f['path']:f for f in m['files']}
        for f in old['files']:
            if f['path']!='database.json': assert files[f['path']]==f, (lang, f['path'])
        archive=OUT/e['localArchive']
        assert archive.stat().st_size==e['downloadBytes'] and digest(archive)==e['archiveSha256']
        with zipfile.ZipFile(archive) as z:
            assert set(z.namelist())==set(files)|{'manifest.json'}
            assert z.read('manifest.json')==(folder/'manifest.json').read_bytes()
            for name,f in files.items():
                assert (folder/name).stat().st_size==f['bytes']
                assert digest(folder/name)==f['sha256']==hashlib.sha256(z.read(name)).hexdigest()
        if lang=='base': continue
        db=json.loads((folder/'database.json').read_text())
        previous=json.loads((WORK/'before'/lang/'database.json').read_text())
        assert db['words'][:len(previous['words'])]==previous['words']
        if lang not in ('tr','en'): assert db==previous
        else:
            assert len(db['words'])==len(previous['words'])+250==db['wordCount']==e['wordCount']
            assert db['contentVersion']==m['packageVersion']
            words={w['conceptId']:w for w in db['words']};assert len(words)==len(db['words'])
            for row in rows:
                w=words[row['ID']]
                assert w['spokenText']==w['displayText']==row['Türkçe' if lang=='tr' else 'İngilizce']
                assert digest(folder/w['audio']['path'])==digest(ROOT/'audio-output/vocabulary-250'/lang/(row['ID']+'_'+lang+'.mp3'))
                assert digest(OUT/'packages/base'/w['image']['path'])==digest(ROOT/'design/vocabulary-catalog'/row['asset'])
                assert not (folder/w['image']['path']).exists()
            summary[lang]=dict(words=len(words), addedWords=250, addedAudio=250)
        print(lang, 'PASS', flush=True)
    report=dict(result='PASS', packages=summary, addedSharedImages=250,
                preservedExistingContent=True, allArchivesVerified=True)
    write(WORK/'validation.json', report)
    print(json.dumps(report))

if __name__=='__main__': main()
