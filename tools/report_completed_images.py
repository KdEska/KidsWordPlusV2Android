"""Verify and present newly installed images in both language packs."""
from pathlib import Path
import json,hashlib,html
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];BASE=ROOT/'language-packs-v2';OUT=BASE/'production/generated-images'
missing=json.loads((BASE/'reports/image-import/missing.json').read_text());paths=sorted({x['path'] for x in missing})
dbs={lang:json.loads((BASE/'packages'/lang/'database.json').read_text()) for lang in ['tr','en']}
summary={};cards=[]
for lang,db in dbs.items():
 folder=BASE/'packages'/lang
 unavailable=[w['conceptId'] for w in db['words'] if not (folder/w['image']['path']).is_file()]
 assert not unavailable,(lang,unavailable)
 before=json.loads((OUT/(lang+'.database.before.json')).read_text())
 for old,new in zip(before['words'],db['words']):
  assert {k:v for k,v in old.items() if k!='image'}=={k:v for k,v in new.items() if k!='image'},(lang,old['conceptId'])
 summary[lang]={'records':len(db['words']),'uniqueImageFiles':len({w['image']['path'] for w in db['words']}),'missingImages':len(unavailable),'newImageFiles':len(paths),'newImageBytes':sum((folder/p).stat().st_size for p in paths),'contentVersion':db['contentVersion']}
for path in paths:
 files=[BASE/'packages'/lang/path for lang in ['tr','en']]
 assert files[0].read_bytes()==files[1].read_bytes(),path
 with Image.open(files[0]) as im:im.verify()
 stem=Path(path).stem;meta=json.loads((OUT/(stem+'.json')).read_text());assert meta['visualReview']=='subject_checked',stem
 labels={lang:' / '.join(dict.fromkeys(w['displayText'] for w in db['words'] if w['image']['path']==path)) for lang,db in dbs.items()}
 cards.append('<article><img loading="lazy" src="../../packages/tr/'+html.escape(path)+'"><h2>'+html.escape(labels['tr'])+'</h2><p>'+html.escape(labels['en'])+'</p><small>'+html.escape(stem)+'</small></article>')
page='''<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>62 yeni kelime görseli</title><style>body{font:17px system-ui;margin:32px;background:#f1f5f9;color:#172033}main{display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:20px}article{background:white;padding:16px;border-radius:16px}img{width:100%;height:240px;object-fit:contain}h2{font-size:21px;margin-bottom:4px}p{margin-top:0}small{font-size:10px;overflow-wrap:anywhere;color:#64748b}</style><h1>62 yeni kelime görseli</h1><p>Türkçe ve İngilizce paketlerine eklendi. Yeni görsellerin kavram eşleşmesi asistan tarafından kontrol edildi; kullanıcı görsel onayı ayrıca izlenir.</p><main>'''+''.join(cards)+'</main></html>'
(OUT/'review.html').write_text(page)
(OUT/'verification.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(summary,ensure_ascii=False,indent=2))
