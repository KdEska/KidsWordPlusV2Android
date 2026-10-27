"""Report literal bilingual UI pairs without translations; dynamic strings are listed separately.
This is a source heuristic, not a substitute for screen QA. It does not modify source or references.
"""
import json,re
from pathlib import Path
root=Path(__file__).resolve().parents[1]
source=root/'app/src/main/java/com/demarpsoftware/kidswordplus'
dictionaries=json.loads((root/'app/src/main/assets/localization/interface.json').read_text())
locales=['de','fr','es','zh','ja','ko','it','pt','hi','ar','ru']
quoted=r'"((?:[^"\\]|\\.)*)"'
pattern=re.compile(r'(?:\bt|InterfaceStrings\.text)\(\s*(?:tr\s*,\s*)?'+quoted+r'\s*,\s*'+quoted)
def resolved(values,key):
    if values.get(key):return True
    core=key.removeprefix('✓ ')
    core=core[:-2] if core.endswith(': ') else core[:-1] if core.endswith(':') else core
    return bool(values.get(core))
missing=[];dynamic=[];total=0
for path in sorted(source.glob('*.kt')):
    text=path.read_text()
    for match in pattern.finditer(text):
        english=match.group(2)
        item={'file':str(path.relative_to(root)),'line':text.count('\n',0,match.start())+1,'english':english}
        if '$' in english:
            dynamic.append(item);continue
        total+=1
        absent=[code for code in locales if not resolved(dictionaries.get(code,{}),english)]
        if absent:missing.append(dict(item,missingLanguages=absent))
report={'literalPairs':total,'pairsWithFallback':len(missing),'dynamicPairsNeedingReview':len(dynamic),'limitations':['literal pair heuristic','includes unused/debug code','does not scan every nested conditional or generated string'],'missing':missing,'dynamic':dynamic}
output=root/'docs/localization-audit.json'
output.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps({k:report[k] for k in ['literalPairs','pairsWithFallback','dynamicPairsNeedingReview']}))
