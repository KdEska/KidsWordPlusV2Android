"""Decode newly produced audio and verify all package references and archive hashes."""
import hashlib
import json
from pathlib import Path
import sys
import zipfile
sys.path.insert(0,'/tmp/kidswordplus-audio-libs')
import miniaudio
import numpy as np

ROOT=Path(__file__).resolve().parents[1]/'language-packs-v2'
added=json.loads((ROOT/'reports/added_audio.json').read_text())
measurements=[]
for r in added:
    p=ROOT/r['path']
    decoded=miniaudio.decode_file(str(p),nchannels=1,sample_rate=44100)
    samples=np.asarray(decoded.samples,dtype=float)/32768
    duration=len(samples)/44100
    peak=float(np.max(np.abs(samples)))
    assert .15<duration<15, (p,duration)
    assert peak>.01,(p,peak)
    measurements.append({**r,'durationSeconds':round(duration,3),
       'peakDbfs':round(20*np.log10(peak),2),
       'rmsDbfs':round(20*np.log10(np.sqrt(np.mean(samples*samples))),2),
       'technicalCheck':'passed','listeningReview':'pending'})
catalog=json.loads((ROOT/'catalog.json').read_text())
for entry in catalog['packages']:
    folder=ROOT/'packages'/entry['language']
    db=json.loads((folder/'database.json').read_text())
    assert len({w['conceptId'] for w in db['words']})==db['wordCount']
    for w in db['words']:
        for kind in ('audio','image'):
            assert (folder/w[kind]['path']).is_file()==w[kind]['available']
    manifest=json.loads((folder/'manifest.json').read_text())
    for f in manifest['files']:
        p=folder/f['path']
        assert hashlib.sha256(p.read_bytes()).hexdigest()==f['sha256']
        assert p.stat().st_size==f['bytes']
    archive=ROOT/entry['localArchive']
    assert hashlib.sha256(archive.read_bytes()).hexdigest()==entry['archiveSha256']
    with zipfile.ZipFile(archive) as z: assert z.testzip() is None
(ROOT/'reports/generated_audio_measurements.json').write_text(json.dumps(measurements,ensure_ascii=False,indent=2)+'\n')
print('PASS:',len(measurements),'added audio references decoded; all package manifests and archives verified.')
