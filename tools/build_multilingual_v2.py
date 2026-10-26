"""Build localized V2 without changing the reference database. Never fill missing translations with English."""
import json
from pathlib import Path
from collections import Counter
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'localization-output'
LANGS=['tr','de','fr','es','zh','ja','ko','it','pt','hi']
db=json.loads((ROOT/'database-output/kidswordplus_unified_en_v2_subcategorized.json').read_text())
country=json.loads((OUT/'lexicon/countries.json').read_text())
lex={}
for path in sorted((OUT/'lexicon').glob('*_authored.json')):
    lex.update(json.loads(path.read_text()))
audio=json.loads((ROOT/'audio-output/audio_audit.json').read_text())
audio_map={(r['category'],r['word'],r['language']):r for r in audio['records']}
missing=[];stats=Counter(); records=[]
for c in db['Categories']:
    for lesson in c['Lessons']:
        for w in lesson['Words']:
            key=f"{c['Id']}:{w['Id']}"
            translations={}
            for lang in ['en']+LANGS:
                status='assistant_draft'
                if lang=='en': title=w['Title'];status='source_v2'
                elif c['Id'] in ('countries','flags'):
                    title=country[w['Title']][LANGS.index(lang)];status='standard_locale_draft'
                else:
                    values=lex.get(c['Id']+':'+w['Title'],lex.get(w['Title']))
                    title=values[LANGS.index(lang)] if values else None
                    if title is None:status='translation_pending'
                if c['Id']=='school' and w.get('Subcategory')=='alphabet' and lang!='en':
                    title=w['Title'];status='curriculum_review_required'
                f=w['WordSound'].removesuffix('_en.mp3')+f'_{lang}.mp3'
                a=audio_map.get((c['Id'],w['Title'],lang))
                translations[lang]={'DisplayText':title,'SpokenText':title if status!='curriculum_review_required' else None,'TranslationStatus':status,'AudioFile':f,'AudioStatus':a['status'] if a else 'not_generated'}
                stats[lang+':'+status]+=1
            r={'Key':key,'Category':c['Id'],'Subcategory':w.get('Subcategory'),'Id':w['Id'],'OriginalId':w.get('OriginalId'),'SourceTitle':w['Title'],'Image':w['Image'],'Difficulty':w.get('Difficulty'),'Lesson':lesson['Lesson'],'LessonPosition':w.get('LessonPosition'),'Translations':translations}
            records.append(r)
            if any(t['TranslationStatus']=='translation_pending' for t in translations.values()):missing.append({'key':key,'category':c['Id'],'word':w['Title']})
result={'SchemaVersion':'2.1-localization-draft','SourceDatabaseVersion':db['DatabaseVersion'],'Languages':['en']+LANGS,'Locales':{'en':'en','tr':'tr-TR','de':'de-DE','fr':'fr-FR','es':'es-ES','zh':'zh-Hans-CN','ja':'ja-JP','ko':'ko-KR','it':'it-IT','pt':'pt-BR','hi':'hi-IN'},'LocaleVariantsStatus':'provisional','ReleaseReady':False,'WordCount':len(records),'Records':records}
(OUT/'kidswordplus_v2_11_languages.draft.json').write_text(json.dumps(result,ensure_ascii=False,indent=2))
(OUT/'translation_progress.json').write_text(json.dumps(dict(stats),ensure_ascii=False,indent=2))
(OUT/'translation_pending.json').write_text(json.dumps(missing,ensure_ascii=False,indent=2))
assert len(records)==1450 and len({r['Key'] for r in records})==1450
assert all(set(r['Translations'])==set(['en']+LANGS) for r in records)
print({'records':len(records),'remaining_records':len(missing),'complete_text_records':len(records)-len(missing),'languages':11})
