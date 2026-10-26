#!/usr/bin/env python3
"""Validate all original secrets, native copies and 13-language resources."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
master=json.loads((ROOT/'planning/portal-refresh/chess-secrets.json').read_text())
langs=master['languages'];tips=master['tips']
assert len(langs)==13 and len(tips)==40
assert len({tip['id'] for tip in tips})==40
swift=(ROOT/'KidsWordPlusV2/App/HomeViews.swift').read_text()
kotlin=(ROOT/'KidsWordPlusAndroid/app/src/main/java/com/demarpsoftware/kidswordplus/ChessSecrets.kt').read_text()
catalogs=[json.loads((ROOT/path).read_text()) for path in ['KidsWordPlusV2/Localization/interface.json','KidsWordPlusAndroid/app/src/main/assets/localization/interface.json']]
for tip in tips:
    assert set(tip['text'])==set(langs)
    values=[tip['id'],tip['text']['tr'],tip['text']['en']]
    quoted=','.join(json.dumps(v,ensure_ascii=False) for v in values)
    assert '('+quoted+')' in swift,tip['id']
    assert 'ChessSecretCopy('+quoted+')' in kotlin,tip['id']
    for lang,text in tip['text'].items():
        assert text.strip() and '{' not in text and '}' not in text
        if lang not in ['en','tr']:
            for catalog in catalogs:assert catalog[lang][tip['text']['en']]==text,(tip['id'],lang)
for lang in langs:assert len({tip['text'][lang] for tip in tips})==40,lang
print('PASS: 40 unique secrets × 13 languages (520 texts), matching native source and translation catalogs.')
