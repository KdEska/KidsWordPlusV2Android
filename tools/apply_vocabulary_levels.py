"""Apply reviewed concept levels to 13 databases and rebuild verified local archives.
No network operations; existing installations use the app-bundled concept map.
"""
from pathlib import Path
import hashlib,json,zipfile
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'language-packs-v2'
def sha(p):
 with p.open('rb') as f:return hashlib.file_digest(f,'sha256').hexdigest()
def write(p,x):p.write_text(json.dumps(x,ensure_ascii=False,indent=2)+'\n')
def main():
 levels=json.loads((ROOT/'planning/vocabulary-levels/levels.json').read_text())['levels'];catalog=json.loads((OUT/'catalog.json').read_text())
 for e in catalog['packages']:
  if e['language']=='base':continue
  folder=OUT/'packages'/e['language'];db=json.loads((folder/'database.json').read_text())
  existing=json.loads((folder/'manifest.json').read_text())
  if all(w.get('difficulty')==levels[w['conceptId']] for w in db['words']) and '-levels-' in existing['packageVersion']:
   print(e['language'],'already classified',flush=True);continue
  for w in db['words']:w['difficulty']=levels[w['conceptId']]
  write(folder/'database.json',db)
  m=json.loads((folder/'manifest.json').read_text());old=m['packageVersion'].split('-levels-')[0];m['packageVersion']=old+'-levels-'+sha(folder/'database.json')[:12]
  m['files']=[{'path':p.relative_to(folder).as_posix(),'bytes':p.stat().st_size,'sha256':sha(p)} for p in sorted(folder.rglob('*')) if p.is_file() and p.name!='manifest.json' and not any(x.startswith('.') for x in p.relative_to(folder).parts)]
  m['installedPayloadBytes']=sum(x['bytes'] for x in m['files']);write(folder/'manifest.json',m)
  archive=OUT/'archives'/f"{e['language']}-{m['packageVersion']}.zip"
  with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
   for name in [f['path'] for f in m['files']]+['manifest.json']:z.write(folder/name,name)
  e.update(version=m['packageVersion'],localArchive=str(archive.relative_to(OUT)),downloadBytes=archive.stat().st_size,archiveSha256=sha(archive),manifestSha256=sha(folder/'manifest.json'),installedBytes=m['installedPayloadBytes']+(folder/'manifest.json').stat().st_size)
  print(e['language'],len(db['words']),flush=True)
 catalog['catalogVersion']=catalog['catalogVersion'].split('-levels-')[0]+'-levels-v1';write(OUT/'catalog.json',catalog)
 android={'schemaVersion':catalog['schemaVersion'],'packages':[]}
 for e in catalog['packages']:
  item={k:v for k,v in e.items() if k!='localArchive'};item['assetArchive']=f"{e['language']}-{e['archiveSha256']}.zip";android['packages'].append(item)
 write(ROOT/'KidsWordPlusAndroid/app/src/debug/assets/packages/catalog.json',android)
if __name__=='__main__':main()
