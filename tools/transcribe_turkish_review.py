"""Independent local ASR evidence, never use expected words as recognition hints."""
import sys
sys.path.insert(0,'/tmp/kwp-speech-review')
from faster_whisper import WhisperModel
from pathlib import Path
import json
import hashlib
import argparse

parser=argparse.ArgumentParser()
parser.add_argument('--download',action='store_true')
args=parser.parse_args()
model=WhisperModel('small',device='cpu',compute_type='int8',cpu_threads=4,
                  download_root='/tmp/kwp-whisper-models',local_files_only=not args.download)
if args.download:
 print('Speech model downloaded and load verified.',flush=True)
 raise SystemExit
root=Path(__file__).resolve().parents[1]/'language-packs-v2'
out=root/'reports/tr-audio-audit';out.mkdir(exist_ok=True)
queue=json.loads((root/'reports/tr-editorial/audio_text_review.json').read_text())
grouped={}
for r in queue:grouped.setdefault(r['audioPath'],[]).append(r)
results_path=out/'raw_transcripts.json'
results=json.loads(results_path.read_text()) if results_path.exists() else []
done={(r['audioPath'],r['sha256']) for r in results}
for path,records in grouped.items():
 original=out/'original-audio'/path
 file=original if original.exists() else root/'packages/tr'/path
 sha=hashlib.sha256(file.read_bytes()).hexdigest()
 if (path,sha) in done:continue
 segments,info=model.transcribe(str(file),language='tr',task='transcribe',beam_size=3,
     temperature=0,condition_on_previous_text=False,vad_filter=False,
     initial_prompt=None,word_timestamps=False)
 segments=list(segments)
 result={'audioPath':path,'sha256':sha,'model':'faster-whisper-small','language':'tr',
     'initialPrompt':None,'conceptIds':[r['conceptId'] for r in records],
     'transcript':' '.join(s.text.strip() for s in segments).strip(),
     'segments':[{'text':s.text,'start':s.start,'end':s.end,'avgLogprob':s.avg_logprob,
                  'noSpeechProbability':s.no_speech_prob} for s in segments],
     'duration':info.duration,'humanListeningVerified':False}
 results.append(result)
 results_path.write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n')
 print(f"{len(results)}/{len(grouped)} {file.name}: {result['transcript']}",flush=True)
