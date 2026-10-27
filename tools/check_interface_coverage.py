"""Strict source/catalog check. A missing translation is an error, including Arabic/Russian."""
import json,re,sys,plistlib
from collections import Counter
from localization_inventory import ROOT,LANGS,inventory_keys
keys=inventory_keys()
manifest=ROOT/'planning/localization-audit/completion/keys.json'
if manifest.exists():keys.update(json.loads(manifest.read_text()))
ph=re.compile(r'\{(?:\d+|[A-Za-z][A-Za-z0-9_]*)\}')
errors=[]
for platform,file in [('ios','KidsWordPlusV2/Localization/interface.json'),('android','KidsWordPlusAndroid/app/src/main/assets/localization/interface.json')]:
 data=json.loads((ROOT/file).read_text())
 for lang in LANGS:
  if lang in ('tr','en'):continue # Source-language values are inline in calls.
  values=data.get(lang,{})
  missing=sorted(k for k in keys if not values.get(k,'').strip())
  if missing:errors.append(f'{platform}/{lang}: {len(missing)} missing translations')
  for key,value in values.items():
   if Counter(ph.findall(key))!=Counter(ph.findall(value)):errors.append(f'{platform}/{lang}: placeholder mismatch: {key}')
for lang in LANGS:
 p=ROOT/f'KidsWordPlusV2/Localization/{lang}.lproj/InfoPlist.strings'
 if not p.exists():errors.append(f'iOS/{lang}: permission strings missing')
 else:
  text=p.read_text()
  for key in ['NSCameraUsageDescription','NSMicrophoneUsageDescription','NSSpeechRecognitionUsageDescription']:
   if not re.search(r'"'+key+r'"\s*=\s*"[^"\n]+";',text):errors.append(f'iOS/{lang}: missing permission: {key}')
for file in ['AppInfo.plist','AppInfo.Release.plist']:
 info=plistlib.loads((ROOT/'KidsWordPlusV2/Configuration'/file).read_bytes())
 if set(info['CFBundleLocalizations'])!=set(LANGS):errors.append(f'{file}: unsupported locale declaration')
for error in errors:print(error)
if errors:print('INCOMPLETE: Translation coverage is not complete.');sys.exit(1)
print(f'PASS: {len(keys)} interface templates × 11 translations on both platforms; 13 system-permission locales.')
