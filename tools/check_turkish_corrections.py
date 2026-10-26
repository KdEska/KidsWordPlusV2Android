"""Check staged recordings without equating ASR with human listening."""
import sys,json,hashlib,re
from pathlib import Path
sys.path[:0]=['/tmp/kwp-speech-review','/tmp/kidswordplus-audio-libs']
import miniaudio
import numpy as np
from faster_whisper import WhisperModel
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'language-packs-v2/production/tr-editorial-corrections'
queue=json.loads((OUT/'queue.json').read_text())
path=OUT/'checks.json'
checks=json.loads(path.read_text()) if path.exists() else []
done={(c['index'],c['sha256']) for c in checks}
model=WhisperModel('small',device='cpu',compute_type='int8',cpu_threads=4,download_root='/tmp/kwp-whisper-models',local_files_only=True)
def norm(s):
 return re.sub(r'[^a-zçğıöşü0-9]','',s.replace('İ','i').replace('I','ı').lower().translate(str.maketrans({'â':'a','î':'i','û':'u'})))
for r in queue:
 p=ROOT/r['output']
 if not p.exists():continue
 sha=hashlib.sha256(p.read_bytes()).hexdigest()
 if (r['index'],sha) in done:continue
 d=miniaudio.decode_file(str(p),nchannels=1,sample_rate=44100)
 a=np.asarray(d.samples,dtype=float)/32768
 duration=len(a)/44100;peak=float(np.max(np.abs(a)))
 assert .15<duration<15 and peak>.01,(p,duration,peak)
 segs,_=model.transcribe(str(p),language='tr',beam_size=3,temperature=0,condition_on_previous_text=False,vad_filter=False,initial_prompt=None)
 segs=list(segs);text=' '.join(s.text.strip() for s in segs)
 match=norm(text)==norm(r['text']) and bool(segs) and min(s.avg_logprob for s in segs)>=-.8
 c={'index':r['index'],'text':r['text'],'output':r['output'],'sha256':sha,'transcript':text,'durationSeconds':round(duration,3),'technicalCheck':'passed','asrTextMatch':match,'avgLogprobs':[s.avg_logprob for s in segs],'humanListeningVerified':False}
 checks.append(c);path.write_text(json.dumps(checks,ensure_ascii=False,indent=2)+'\n')
 print(r['index'],r['text'],text,match,flush=True)
