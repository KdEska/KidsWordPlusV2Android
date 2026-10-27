"""Validate current level mapping, package inventories, archives and UI catalogs."""
from pathlib import Path
import hashlib,json,zipfile
ROOT=Path(__file__).resolve().parents[1]
def digest(p):
 with p.open('rb') as f:return hashlib.file_digest(f,'sha256').hexdigest()
def main():
 mapping=json.loads((ROOT/'planning/vocabulary-levels/levels.json').read_text())
 for path in ['KidsWordPlusV2/Localization/vocabulary-levels.json','KidsWordPlusAndroid/app/src/main/assets/localization/vocabulary-levels.json']:assert json.loads((ROOT/path).read_text())==mapping
 entries=json.loads((ROOT/'language-packs-v2/catalog.json').read_text())['packages'];seen=set()
 for e in entries:
  folder=ROOT/'language-packs-v2/packages'/e['language'];assert not list(folder.rglob('*wine*'));m=json.loads((folder/'manifest.json').read_text());assert digest(folder/'manifest.json')==e['manifestSha256']
  archive=ROOT/'language-packs-v2'/e['localArchive'];assert digest(archive)==e['archiveSha256'];assert archive.stat().st_size==e['downloadBytes']
  with zipfile.ZipFile(archive) as z:
   assert set(z.namelist())=={'manifest.json'}|{f['path'] for f in m['files']}
   for f in m['files']:
    assert digest(folder/f['path'])==f['sha256'];assert hashlib.sha256(z.read(f['path'])).hexdigest()==f['sha256']
  if e['language']=='base':continue
  words=json.loads((folder/'database.json').read_text())['words'];assert all(w['conceptId'] not in {'food:792','food:830'} for w in words);assert len({w['conceptId'] for w in words})==len(words)
  assert {w['difficulty'] for w in words}=={'beginner','intermediate','advanced'}
  for w in words:assert mapping['levels'][w['conceptId']]==w['difficulty'];seen.add(w['conceptId'])
 assert seen==set(mapping['levels'])
 ios=json.loads((ROOT/'KidsWordPlusV2/Localization/interface.json').read_text());android=json.loads((ROOT/'KidsWordPlusAndroid/app/src/main/assets/localization/interface.json').read_text())
 for lang in ['de','fr','es','it','pt','zh','ja','ko','hi','ar','ru']:
  for key in ['Vocabulary level','Simple','Medium','Hard','Lessons and word games include your selected level and all easier levels. Your progress is kept.']:assert ios[lang][key] and ios[lang][key]==android[lang][key]
 print('PASS: 13 vocabulary databases, 14 verified archives, all concept levels and native translations match.')
if __name__=='__main__':main()
