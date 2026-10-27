"""Remove the wine concept from active local packages and rebuild their verified inventories.
Historical archives/backups remain untouched; catalog points only to revised archives.
Run after regenerating vocabulary to keep the exclusion in exported content.
"""
from pathlib import Path
import hashlib,json,zipfile
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'language-packs-v2'
EXCLUDED='food:830'
def sha(p):return hashlib.file_digest(p.open('rb'),'sha256').hexdigest()
def write(p,d):p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
def inventory(folder):
 return [{'path':p.relative_to(folder).as_posix(),'bytes':p.stat().st_size,'sha256':sha(p)} for p in sorted(folder.rglob('*')) if p.is_file() and p.name!='manifest.json' and not any(x.startswith('.') for x in p.relative_to(folder).parts)]
def main():
 catalog=json.loads((OUT/'catalog.json').read_text());removed={};media=[]
 for entry in catalog['packages']:
  lang=entry['language'];folder=OUT/'packages'/lang
  if lang=='base':continue
  db=json.loads((folder/'database.json').read_text());excluded=[w for w in db['words'] if w['conceptId']==EXCLUDED]
  removed[lang]=len(excluded)
  for w in excluded:
   for key in ['audio','image']:
    m=w[key];media.append((m.get('package',lang),m.get('overridePath') or m['path']))
  db['words']=[w for w in db['words'] if w['conceptId']!=EXCLUDED]
  for key in ['wordCount','activeWordCount','baseConceptCount']:
   if key in db:db[key]-=len(excluded)
  if excluded:
   db['contentVersion']=str(db.get('contentVersion',''))+'-no-wine-v1'
   write(folder/'database.json',db)
  for key in ['wordCount','activeWordCount']:
   if key in entry:entry[key]-=len(excluded)
 # Delete only media no longer referenced by any retained record.
 referenced=set()
 for p in (OUT/'packages').glob('*/database.json'):
  for w in json.loads(p.read_text())['words']:
   for key in ['audio','image']:
    m=w[key];referenced.add((m.get('package',p.parent.name),m.get('overridePath') or m['path']))
 for lang,path in set(media):
  if (lang,path) not in referenced:(OUT/'packages'/lang/path).unlink(missing_ok=True)
 if not any(removed.values()):print('Wine is already absent.');return
 # Base inventory/hash must be finalized before all language dependencies.
 base=next(e for e in catalog['packages'] if e['language']=='base')
 for entry in [base]+[e for e in catalog['packages'] if e['language']!='base']:
  lang=entry['language'];folder=OUT/'packages'/lang;m=json.loads((folder/'manifest.json').read_text())
  m['packageVersion']+='-no-wine-v1'
  m['dependencies']=[] if lang=='base' else [dict(language='base',version=base['version'],manifestSha256=base['manifestSha256'])]
  m['files']=inventory(folder);m['installedPayloadBytes']=sum(x['bytes'] for x in m['files']);write(folder/'manifest.json',m)
  archive=OUT/'archives'/f"{lang}-{m['packageVersion']}.zip"
  with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
   for name in [f['path'] for f in m['files']]+['manifest.json']:z.write(folder/name,name)
  entry.update(version=m['packageVersion'],localArchive=str(archive.relative_to(OUT)),downloadBytes=archive.stat().st_size,archiveSha256=sha(archive),manifestSha256=sha(folder/'manifest.json'),installedBytes=m['installedPayloadBytes']+(folder/'manifest.json').stat().st_size,dependencies=m['dependencies'])
 catalog['catalogVersion']+='-no-wine-v1';write(OUT/'catalog.json',catalog)
 write(OUT/'reports/wine-removal.json',{'conceptId':EXCLUDED,'removedByLanguage':removed,'removedMedia':[{'language':l,'path':p} for l,p in sorted(set(media)) if(l,p)not in referenced]})
 print(removed)
if __name__=='__main__':main()
