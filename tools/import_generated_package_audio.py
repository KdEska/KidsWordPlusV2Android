"""Import individually generated files using package words as the sole word source."""
import hashlib
import json
from pathlib import Path
import shutil

ROOT=Path(__file__).resolve().parents[1]
PACKS=ROOT/'language-packs-v2'

def run():
    report=[]
    profiles_path=PACKS/'production/voice_profiles.json'
    profiles=json.loads(profiles_path.read_text()) if profiles_path.exists() else {}
    for lang in ('tr','en'):
        folder=PACKS/'packages'/lang
        path=folder/'database.json'
        db=json.loads(path.read_text())
        queue_path=PACKS/'production'/(lang+'_queue.json')
        if queue_path.exists():
            queue=json.loads(queue_path.read_text())
        else:
            by_text={}
            for word in db['words']:
                if not word['audio']['available'] and word.get('spokenText'):
                    by_text.setdefault(word['spokenText'],[]).append(word['conceptId'])
            queue=[{'text':text,'conceptIds':ids,'sourceFile':
                    ('single_tr_kunduz.mp3' if lang=='tr' and n==1 else f'generated_{lang}_{n:03}.mp3')}
                    for n,(text,ids) in enumerate(by_text.items(),1)]
            queue_path.parent.mkdir(exist_ok=True)
            queue_path.write_text(json.dumps(queue,ensure_ascii=False,indent=2)+'\n')
        for item in queue:
            source=ROOT/'audio-output'/item['sourceFile']
            if not source.exists(): continue
            profile=profiles.get(lang)
            if not profile: raise ValueError('Missing approved language reference profile: '+lang)
            for word in db['words']:
                if word['conceptId'] not in item['conceptIds']:continue
                assert word['spokenText']==item['text'], 'Text changed after queue creation'
                dst=folder/word['audio']['path']
                dst.parent.mkdir(exist_ok=True)
                shutil.copyfile(source,dst)
                word['audio'].update(available=True,provenance='elevenlabs_individual_generation',
                   voiceId=profile['voiceId'],referenceLanguage=lang,model='eleven_multilingual_v2',
                   spokenText=item['text'],qualityStatus='technical_check_pending_listening_review',
                   sha256=hashlib.sha256(dst.read_bytes()).hexdigest())
                report.append({'language':lang,'conceptId':word['conceptId'],'text':item['text'],
                               'path':str(dst.relative_to(PACKS)), 'sourceFile':item['sourceFile']})
        path.write_text(json.dumps(db,ensure_ascii=False,indent=2)+'\n')
    (PACKS/'reports/added_audio.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
    lines=['# Eklenen ses dosyaları','','Tek tek ElevenLabs üretimi. Dinleme/telaffuz incelemesi bekliyor.','',
           '| Dil | Kayıt | Metin | Dosya |','|---|---|---|---|']
    lines += [f"| {r['language']} | {r['conceptId']} | {r['text']} | {r['path']} |" for r in report]
    (PACKS/'reports/added_audio.md').write_text('\n'.join(lines)+'\n')
    print('Imported references:',len(report))

if __name__=='__main__':run()
