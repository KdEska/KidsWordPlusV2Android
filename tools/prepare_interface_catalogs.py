"""Reuse existing owned translations; write missing public copy for translation review.
Entirely offline. Does not fill missing values with English or contact any service.
"""
import json,re
from localization_inventory import ROOT,LANGS,calls,inventory_keys
OUT=ROOT/'planning/localization-audit/completion';OUT.mkdir(parents=True,exist_ok=True)
P=ROOT/'KidsWordPlusV2/Localization/interface.json'
ios=json.loads(P.read_text());android=json.loads((ROOT/'KidsWordPlusAndroid/app/src/main/assets/localization/interface.json').read_text())
targets=[l for l in LANGS if l not in ('tr','en')]
entries=list(calls());keys=set().union(*(set(d) for d in ios.values()),*(set(d) for d in android.values()))
keys.update(inventory_keys())
for file,pattern in [('KidsWordPlusV2/Core/CompanionCatalog.swift',r'traitsEN: "([^"]+)"'),('KidsWordPlusAndroid/app/src/main/java/com/demarpsoftware/kidswordplus/family/Companions.kt',r'Companion\("[^"]+", "[^"]+", "[^"]+", "([^"]+)"')]:
 keys.update(re.findall(pattern,(ROOT/file).read_text()))
# Data-driven form choices use localized titles rather than literal calls.
for file in ['KidsWordPlusV2/App/ParentFeedbackView.swift','KidsWordPlusAndroid/app/src/main/java/com/demarpsoftware/kidswordplus/ParentFeedback.kt']:
 text=(ROOT/file).read_text()
 keys.update(re.findall(r'en: "([^"\n]+)"',text))
 keys.update(re.findall(r'FeedbackChoice\("[^"\n]+","[^"\n]+","([^"\n]+)"',text))
keys.update(['Mon' ,'Tue','Wed','Thu','Fri','Sat','Sun','White','Black','Board','Turns','Goal'])
reuse={l:{} for l in targets}
def merge(en,value,target):
 if isinstance(en,str) and isinstance(value,str):target.setdefault(en,value)
 elif isinstance(en,dict) and isinstance(value,dict):
  for k,v in en.items():
   if k in value:merge(v,value[k],target)
 elif isinstance(en,list) and isinstance(value,list):
  for a,b in zip(en,value):merge(a,b,target)
for file in ['KidsWordPlusV2/Localization/help.json','KidsWordPlusV2/Chess/interface.json','KidsWordPlusV2/Chess/content.json','KidsWordPlusV2/Chess/history.json']:
 data=json.loads((ROOT/file).read_text())
 for lang in targets:merge(data['en'],data.get(lang,{}),reuse[lang])
chess_ui=json.loads((ROOT/'KidsWordPlusV2/Chess/interface.json').read_text())
base={}
for lang in targets:
 base[lang]={**android.get(lang,{}),**ios.get(lang,{})}
 for key in keys:
  if key not in base[lang] and key in reuse[lang]:base[lang][key]=reuse[lang][key]
 # Shared first-steps buttons use the reviewed chess glossary consistently.
 for key,english in chess_ui['en'].items():
  if key.startswith('guide.') and key in chess_ui[lang]:base[lang][english]=chess_ui[lang][key]
 additions=OUT/(lang+'.json')
 if additions.exists():base[lang].update(json.loads(additions.read_text()))
(OUT/'keys.json').write_text(json.dumps(sorted(keys),ensure_ascii=False,indent=2)+'\n')
(OUT/'pending.json').write_text(json.dumps({l:sorted(keys-set(v)) for l,v in base.items()},ensure_ascii=False,indent=2)+'\n')
P.write_text(json.dumps(base,ensure_ascii=False,indent=2)+'\n')
print('Offline inventory:',len(keys),'keys')
for lang,values in base.items():print(lang,'pending:',len(keys-set(values)))
