"""Import archive images by exact names or explicit semantic aliases; preserve existing files."""
import json,hashlib,zipfile,io
from pathlib import Path
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];PACKS=ROOT/'language-packs-v2';OUT=PACKS/'reports/image-import';OUT.mkdir(parents=True,exist_ok=True)
BASE=Path('/Users/kursatdemirci/Projects/KidsWordPlus/BACKUP')
archives=[zipfile.ZipFile(BASE/name) for name in ['Android KidsWordPlusPro 27_02_2023.zip','IOS KidsWordPlusPro 27_02_2023.zip']]
index={}
for z in archives:
 for n in z.namelist():
  if '__MACOSX' not in n and Path(n).suffix.lower() in ['.jpg','.jpeg','.png','.webp']:
   index.setdefault(Path(n).name,[]).append((z,n))
alias={'slide_school_pencil.jpg':'slide_my_home_pencil.jpg','slide_school_ruler.jpg':'slide_my_home_ruler.jpg','slide_farm_farmer.jpg':'slide_professions_farmer.jpg','slide_professions_astronaut.jpg':'slide_discover_space_astronaut.jpg'}
cache={};report=[];missing=[];conflicts=[]
def choose(name):
 if name in cache:return cache[name]
 candidates=[]
 for z,n in index.get(name,[]):
  data=z.read(n)
  try:
   with Image.open(io.BytesIO(data)) as im:im.load();width,height=im.size
  except Exception:continue
  candidates.append({'archive':Path(z.filename).name,'entry':n,'data':data,'width':width,'height':height,'sha256':hashlib.sha256(data).hexdigest()})
 if not candidates:return None
 candidates.sort(key=lambda c:c['width']*c['height'],reverse=True)
 chosen=candidates[0]
 if len({c['sha256'] for c in candidates})>1:conflicts.append({'name':name,'selection':'largest_pixel_area_then_android_archive_priority','candidates':[{k:v for k,v in c.items() if k!='data'} for c in candidates]})
 cache[name]=chosen;return chosen
for lang in ['tr','en']:
 folder=PACKS/'packages'/lang;dbp=folder/'database.json';db=json.loads(dbp.read_text());b=OUT/f'{lang}.database.before.json'
 if not b.exists():b.write_bytes(dbp.read_bytes())
 for path in sorted({w['image']['path'] for w in db['words']}):
  words=[w for w in db['words'] if w['image']['path']==path];target=folder/path;name=Path(path).name;source=alias.get(name,name)
  if target.exists():
   with Image.open(target) as im:im.load()
   continue
  c=choose(source)
  if c is None:
   missing.append({'language':lang,'path':path,'concepts':[{'conceptId':w['conceptId'],'text':w['displayText']} for w in words]});continue
  target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(c['data'])
  info={'language':lang,'path':path,'sourceName':source,'match':'explicit_alias' if source!=name else 'exact_filename','conceptIds':[w['conceptId'] for w in words],**{k:v for k,v in c.items() if k!='data'},'visualSemanticReview':'pending'};report.append(info)
  for w in words:w['image'].update({'available':True,'source':'legacy_archive','sourceFilename':source,'mappingStatus':info['match'],'visualReviewStatus':'pending'})
 db['contentVersion']='8-tr-archive-images-draft' if lang=='tr' else '2-en-archive-images-draft'
 dbp.write_text(json.dumps(db,ensure_ascii=False,indent=2)+'\n')
for name,value in [('imported.json',report),('missing.json',missing),('source_variants.json',conflicts)]:
 (OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n')
summary={lang:{'importedFiles':sum(r['language']==lang for r in report),'missingUniquePaths':sum(r['language']==lang for r in missing)} for lang in ['tr','en']}
(OUT/'summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n')
lines=['# Kelime görselleri aktarımı','','Dosya adı eşleşmesi görsel anlam onayı değildir. Kaynaklar okunarak aktarıldı; mevcut paket görselleri değiştirilmedi. En büyük piksel alanına sahip sürüm, eşitlikte Android kaynağı seçildi.','','## Eksik görseller','','| Dil | Dosya | Kelimeler |','|---|---|---|']
lines += [f"| {r['language']} | {Path(r['path']).name} | {', '.join(x['text'] for x in r['concepts'])} |" for r in missing]
(OUT/'README.md').write_text('\n'.join(lines)+'\n')
selected=[]
for term in ['slide_my_home_pencil.jpg','slide_my_home_ruler.jpg','slide_professions_farmer.jpg','slide_discover_space_astronaut.jpg']:
 selected.append(next(r for r in report if r['sourceName']==term))
selected+=report[::max(1,len(report)//20)][:20]
sheet=Image.new('RGB',(1000,((len(selected)+4)//5)*180),'white');draw=ImageDraw.Draw(sheet)
for i,r in enumerate(selected):
 with Image.open(PACKS/'packages'/r['language']/r['path']) as im:im=im.convert('RGB');im.thumbnail((180,140));x=(i%5)*200;y=(i//5)*180;sheet.paste(im,(x,y));draw.text((x,y+143),Path(r['path']).stem[:27],fill='black')
sheet.save(OUT/'sample-contact-sheet.jpg')
print(json.dumps(summary));print('source variants',len(conflicts))
