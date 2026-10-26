"""Compare acoustic ASR evidence with intended text without treating ASR as human QA."""
import json,re,html,hashlib
from pathlib import Path
from collections import Counter

ROOT=Path(__file__).resolve().parents[1]/'language-packs-v2'
OUT=ROOT/'reports/tr-audio-audit'
def norm(s):
 s=s.replace('İ','i').replace('I','ı').lower()
 s=s.translate(str.maketrans({'â':'a','î':'i','û':'u'}))
 return re.sub(r'[^a-zçğıöşü0-9]','',s)

def main():
 queue=json.loads((ROOT/'reports/tr-editorial/audio_text_review.json').read_text())
 raw=json.loads((OUT/'raw_transcripts.json').read_text())
 evidence={r['audioPath']:r for r in raw}
 rows=[]
 for r in queue:
  e=evidence.get(r['audioPath']);status='not_transcribed'
  if e:
   original=OUT/'original-audio'/r['audioPath']
   source=original if original.exists() else ROOT/'packages/tr'/r['audioPath']
   assert e['sha256']==hashlib.sha256(source.read_bytes()).hexdigest()
   text=e['transcript'];scores=[s['avgLogprob'] for s in e['segments']]
   weak=not scores or min(scores)<-0.8 or not text
   if r['status']=='letter_name_listening_review':status='alphabet_manual_review'
   elif weak:status='uncertain_transcript'
   elif norm(text)==norm(r['expectedSpokenText']):status='asr_text_match'
   elif any(norm(text)==norm(old) for old in r['legacyLabels']):status='asr_and_legacy_support_difference'
   else:status='different_or_uncertain_transcript'
  rows.append({**r,'transcript':e['transcript'] if e else None,'auditStatus':status,
               'avgLogprobs':[s['avgLogprob'] for s in e['segments']] if e else [],
               'evidenceSha256':e['sha256'] if e else None,'humanListeningVerified':False})
 counts=dict(Counter(r['auditStatus'] for r in rows))
 unique_counts={s:len({r['audioPath'] for r in rows if r['auditStatus']==s}) for s in counts}
 (OUT/'comparison.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
 (OUT/'summary.json').write_text(json.dumps({'recordCount':len(rows),'uniqueAudioFiles':len({r['audioPath'] for r in rows}),
    'transcribedUniqueFiles':len(evidence),'statuses':counts,'uniqueFilesByStatus':unique_counts,'scope':'Only the 225 records in the editorial listening queue, not the complete Turkish package.','method':'Independent local Whisper small ASR; no expected-text prompt; original labels used only after transcription.',
    'humanListeningVerified':False,'packageAudioChanged':False},ensure_ascii=False,indent=2)+'\n')
 candidates=[r for r in rows if r['auditStatus']=='asr_and_legacy_support_difference']
 (OUT/'replacement_candidates.json').write_text(json.dumps(candidates,ensure_ascii=False,indent=2)+'\n')
 labels={'asr_text_match':'Döküm metinle eşleşiyor','asr_and_legacy_support_difference':'Eski etiket ve döküm farklı metni destekliyor',
 'alphabet_manual_review':'Harf: dinleme gerekli','uncertain_transcript':'Döküm belirsiz',
 'different_or_uncertain_transcript':'Fark var: dinleme gerekli','not_transcribed':'Henüz incelenmedi'}
 lines=['# Türkçe ses–metin kontrolü','',
  'Yöntem: Her dosya bağımsız, yerel konuşma tanıma ile çözümlendi. Beklenen metin modele ipucu olarak verilmedi. Sonuçlar otomatik dökümdür; insan dinleme onayı değildir. Paket sesleri değiştirilmedi.',
  '', 'Kapsam: Önceki metin incelemesinde ayrılan 225 kayıt / 208 farklı ses dosyası. Bu çalışma tüm Türkçe paketin dinleme onayı değildir. Aynı ses dosyası birden fazla kavramda kullanılabilir.',
  '', '| Durum | Kayıt | Farklı dosya |','|---|---:|---:|']
 lines += [f'| {labels[k]} | {v} | {unique_counts[k]} |' for k,v in counts.items()]
 lines += ['','## Eski etiket ve ses dökümünün birlikte desteklediği farklar','',
 '| Kavram | Beklenen | Döküm | Eski etiket |','|---|---|---|---|']
 lines += [f"| {r['conceptId']} | {r['expectedSpokenText']} | {r['transcript']} | {' / '.join(r['legacyLabels'])} |" for r in candidates]
 lines += ['','Bu tablo yeniden üretim emri değildir. Önce anlam farkları dinlenmeli; yazım, eş anlam ve özel ad farkları ayrıca değerlendirilmelidir. Harfler otomatik eşleşse bile dinleme kuyruğunda kalır.',
 '', 'Bütün kayıtlar ve oynatıcılar: `review.html`. Her kayıt için ham sonuç `raw_transcripts.json` içinde saklanır.',
 '', 'Kullanılan araç: [faster-whisper resmî deposu](https://github.com/SYSTRAN/faster-whisper). Tek sözcük, özel ad ve harf dökümleri yanılabilir; belirsiz sonuçlar kesin hata sayılmaz.']
 (OUT/'README.md').write_text('\n'.join(lines)+'\n')
 cards=[]
 priority={'asr_and_legacy_support_difference':0,'different_or_uncertain_transcript':1,'uncertain_transcript':2,'alphabet_manual_review':3,'asr_text_match':4,'not_transcribed':5}
 for r in sorted(rows,key=lambda r:priority[r['auditStatus']]):
  esc=html.escape
  audio_src=('original-audio/' if (OUT/'original-audio'/r['audioPath']).exists() else '../../packages/tr/')+r['audioPath']
  cards.append(f'''<article data-status="{r['auditStatus']}"><small>{esc(r['conceptId'])} · {esc(labels[r['auditStatus']])}</small>
<h2>{esc(r['expectedSpokenText'])}</h2><p>Döküm: <b>{esc(r['transcript'] or '—')}</b></p>
<p>Eski etiket: {esc(' / '.join(r['legacyLabels']) or '—')}</p>
<audio controls preload="none" src="{esc(audio_src)}"></audio></article>''')
 page='''<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Türkçe ses incelemesi</title>
<style>body{font:17px system-ui;background:#f5f4fa;color:#23213b;max-width:960px;margin:40px auto;padding:0 22px}article{background:white;padding:20px;border-radius:16px;margin:14px 0}h2{margin:10px 0}small{color:#625777}audio{width:100%;max-width:500px}select{padding:12px;margin:14px 0}p{line-height:1.5}</style>
<h1>Türkçe ses–metin incelemesi</h1><p>Beklenen metin ile otomatik dökümü karşılaştırın ve asıl dosyayı dinleyin. Otomatik döküm kesin dinleme onayı değildir. Ses dosyaları değiştirilmedi.</p><select id="filter"><option value="all">Tüm kayıtlar</option>'''
 page+=''.join(f'<option value="{k}">{html.escape(v)}</option>' for k,v in labels.items())+'</select>'+''.join(cards)
 page+='''<script>document.querySelector('#filter').onchange=e=>document.querySelectorAll('article').forEach(a=>a.hidden=e.target.value!=='all'&&a.dataset.status!==e.target.value);document.addEventListener('play',e=>{if(e.target.tagName==='AUDIO')document.querySelectorAll('audio').forEach(a=>{if(a!==e.target)a.pause()})},true)</script></html>'''
 (OUT/'review.html').write_text(page)
 print(counts)

if __name__=='__main__':main()
