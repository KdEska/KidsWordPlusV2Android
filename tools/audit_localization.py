"""Read-only localization audit. Literal call coverage is a lower bound, not an AST proof."""
import json,re
from pathlib import Path
from collections import Counter
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'planning/localization-audit'
LANGS=['tr','en','de','fr','es','zh','ja','ko','it','pt','hi','ar','ru']
TARGETS=[x for x in LANGS if x not in ('tr','en')]
paths={'ios':'KidsWordPlusV2/Localization/interface.json','android':'KidsWordPlusAndroid/app/src/main/assets/localization/interface.json'}
catalogs={p:json.loads((ROOT/f).read_text()) for p,f in paths.items()}
report={'languages':LANGS,'scope':'Static audit of active source and bundled text catalogs; excludes sources/, repository/, generated builds and diagnostic-only logs. Dynamic source expressions require manual review.','catalogs':{},'literal_calls':{},'platform_differences':{}}
for platform,d in catalogs.items():
 union=set().union(*(set(v) for v in d.values()))
 report['catalogs'][platform]={'union_key_count':len(union),'languages':{l:{'key_count':len(d.get(l,{})),'missing_from_union':sorted(union-set(d.get(l,{})))} for l in TARGETS}}
a,b=catalogs.values()
for l in TARGETS:
 report['platform_differences'][l]={'ios_only':sorted(set(a[l])-set(b[l])),'android_only':sorted(set(b[l])-set(a[l])),'different_values':sorted(k for k in a[l].keys()&b[l].keys() if a[l][k]!=b[l][k])}
# Direct literal pairs only. Exclude interpolation rather than count a guessed template as a missing key.
q=r'"((?:\\.|[^"\\])*)"'
pair=re.compile(r'\b(?:t|text|render|named)\(\s*(?:(?:tr|state\.ui|mutable\.value\.ui|language)\s*,\s*)?'+q+r'\s*,\s*'+q)
roots={'ios':ROOT/'KidsWordPlusV2/App','android':ROOT/'KidsWordPlusAndroid/app/src/main/java'}
for platform,root in roots.items():
 calls=[]
 for p in sorted(root.rglob('*.swift' if platform=='ios' else '*.kt')):
  text=p.read_text()
  for m in pair.finditer(text):
   key=m.group(2)
   if '\\(' in key or '$' in key:continue
   key=key.replace('\\n','\n').replace('\\"','"')
   if not re.search('[A-Za-z]',key):continue
   lookup=key
   if platform=='android':
    lookup=lookup.removeprefix('✓ ')
    lookup=lookup[:-2] if lookup.endswith(': ') else lookup[:-1] if lookup.endswith(':') else lookup
   if key in ('en','tr'):continue  # Locale selectors, not user-facing sentences.
   missing=[l for l in TARGETS if not catalogs[platform].get(l,{}).get(key) and not catalogs[platform].get(l,{}).get(lookup)]
   if missing:calls.append({'file':str(p.relative_to(ROOT)),'line':text.count('\n',0,m.start())+1,'english':key,'missing_languages':missing})
 report['literal_calls'][platform]={'missing_call_sites':len(calls),'unique_missing_keys':len(set(c['english'] for c in calls)),'items':calls}
# Catalogs addressed by stable IDs / Turkish source text.
report['other_catalogs']={}
for name in ['KidsWordPlusV2/Localization/help.json','KidsWordPlusV2/Chess/interface.json','KidsWordPlusV2/Chess/content.json','KidsWordPlusV2/Chess/history.json','KidsWordPlusAndroid/app/src/main/assets/chess/reference-content.json','KidsWordPlusAndroid/app/src/main/assets/chess/history.json','KidsWordPlusAndroid/app/src/main/assets/localization/android.json']:
 d=json.loads((ROOT/name).read_text());base=set(d.get('en',{}));union=set().union(*(set(v) for v in d.values() if isinstance(v,dict)))
 report['other_catalogs'][name]={l:{'count':len(d.get(l,{})),'missing_from_union':sorted(union-set(d.get(l,{})))} for l in LANGS}
# Placeholder mismatches and equal-to-source translations are candidates, not assumed semantic failures.
report['placeholder_mismatches']=[];report['source_identical_candidates']={}
pattern=re.compile(r'\{(?:\d+|[a-zA-Z][a-zA-Z0-9_]*)\}')
for platform,d in catalogs.items():
 for l in TARGETS:
  equal=[]
  for k,v in d[l].items():
   if Counter(pattern.findall(k))!=Counter(pattern.findall(v)):report['placeholder_mismatches'].append({'platform':platform,'language':l,'key':k,'value':v})
   if k==v and re.search('[A-Za-z]',k):equal.append(k)
  report['source_identical_candidates'][platform+'.'+l]=equal
report['ios_system_permissions']={l:(ROOT/f'KidsWordPlusV2/Localization/{l}.lproj/InfoPlist.strings').exists() for l in LANGS}
OUT.mkdir(exist_ok=True)
(OUT/'audit.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps({'catalogs':{p:{l:v['key_count'] for l,v in d['languages'].items()} for p,d in report['catalogs'].items()},'literal_calls':{p:{k:v for k,v in d.items() if k!='items'} for p,d in report['literal_calls'].items()},'placeholder_mismatches':len(report['placeholder_mismatches']),'missing_ios_permissions':[l for l,v in report['ios_system_permissions'].items() if not v]},ensure_ascii=False,indent=2))
