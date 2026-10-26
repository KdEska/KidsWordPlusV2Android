"""Install reviewed generated images as JPEGs; retain lossless originals and provenance."""
import json,hashlib,shutil,io
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];BASE=ROOT/'language-packs-v2';OUT=BASE/'production/generated-images'
original_missing=json.loads((BASE/'reports/image-import/missing.json').read_text());allowed={Path(x['path']).stem for x in original_missing}
for stem in sorted(allowed):
 meta=OUT/(stem+'.json')
 assert meta.exists(),f'Missing metadata: {stem}'
 info=json.loads(meta.read_text())
 assert info.get('visualReview')=='subject_checked',f'Image review incomplete: {stem}'
 assert Path(info['sourcePath']).is_file(),f'Missing source: {stem}'
log=[]
for lang in ['tr','en']:
 folder=BASE/'packages'/lang;p=folder/'database.json';db=json.loads(p.read_text());backup=OUT/(lang+'.database.before.json')
 if not backup.exists():backup.write_bytes(p.read_bytes())
 for meta in OUT.glob('slide_*.json'):
  info=json.loads(meta.read_text());stem=info['stem']
  if stem not in allowed or info.get('visualReview')!='subject_checked':continue
  source=Path(info['sourcePath']);lossless=OUT/(stem+'.png')
  if not lossless.exists():
   with Image.open(source) as original:original.convert('RGB').save(lossless,'PNG')
  target=folder/'images'/(stem+'.jpg')
  if not target.exists():
   with Image.open(lossless) as im:
    im.load();im=im.convert('RGB');im.thumbnail((1024,1024),Image.Resampling.LANCZOS);im.save(target,'JPEG',quality=90,optimize=True)
  sha=hashlib.sha256(target.read_bytes()).hexdigest()
  for w in db['words']:
   if w['image']['path']=='images/'+stem+'.jpg':w['image'].update({'available':True,'source':info.get('tool','built-in image_gen'),'mappingStatus':'existing_asset_for_concept' if info.get('tool')=='existing_archive_reuse' else 'generated_for_concept','visualReviewStatus':'assistant_subject_checked','evidenceSha256':sha})
  log.append({'language':lang,'path':str(target.relative_to(BASE)),'source':str(lossless.relative_to(BASE)),'sha256':sha})
 db['contentVersion']='9-tr-complete-images-draft' if lang=='tr' else '3-en-complete-images-draft'
 p.write_text(json.dumps(db,ensure_ascii=False,indent=2)+'\n')
(OUT/'installed.json').write_text(json.dumps(log,ensure_ascii=False,indent=2)+'\n');print('installed',len(log))
