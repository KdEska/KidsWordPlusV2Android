"""Measure a deterministic sample; do not infer speaker identity or emotion."""
import collections
import json
from pathlib import Path
import subprocess
import tempfile
import wave
import zipfile
import numpy as np
import sys
sys.path.insert(0, '/tmp/kidswordplus-audio-libs')
import miniaudio

root = Path(__file__).resolve().parents[1]
audit = json.loads((root/'audio-output/audio_audit.json').read_text())
groups = collections.defaultdict(list)
for r in audit['records']:
    if r['status'] == 'filename_match':
        groups[(r['category'], r['language'])].append(r)
selected = []
for key, rows in sorted(groups.items()):
    for index in (0, len(rows)//2):
        selected.append(rows[index])
results = []
with zipfile.ZipFile('/Users/kursatdemirci/Projects/KidsWordPlus/BACKUP/sounds_slides.zip') as z, tempfile.TemporaryDirectory() as tmp:
    for i, r in enumerate(selected):
        src = Path(tmp)/f'{i}.mp3'
        dst = Path(tmp)/f'{i}.wav'
        src.write_bytes(z.read(r['source']))
        info = miniaudio.get_file_info(str(src))
        sr, channels = info.sample_rate, info.nchannels
        decoded = miniaudio.decode_file(str(src), output_format=miniaudio.SampleFormat.SIGNED16, nchannels=channels, sample_rate=sr)
        x = np.asarray(decoded.samples,dtype=float).reshape(-1,channels)/32768
        mono=x.mean(axis=1)
        size=int(sr*.02)
        frame_rms=np.array([np.sqrt(np.mean(mono[k:k+size]**2)) for k in range(0,len(mono),size)])
        threshold=max(10**(-45/20), float(frame_rms.max())*10**(-30/20))
        active=np.flatnonzero(frame_rms>threshold)
        span=min(len(mono)/sr,(active[-1]+1)*.02)-active[0]*.02 if len(active) else 0
        results.append(dict(category=r['category'],language=r['language'],word=r['word'],source=r['source'],sample_rate=sr,channels=channels,duration_seconds=round(len(mono)/sr,3),active_span_seconds=round(span,3),leading_silence_seconds=round(active[0]*.02,3) if len(active) else None,peak_dbfs=round(20*np.log10(max(np.max(np.abs(x)),1e-9)),2),rms_dbfs=round(20*np.log10(max(np.sqrt(np.mean(x*x)),1e-9)),2),clipped_sample_fraction=float(np.mean(np.abs(x)>=32767/32768))))
out=root/'audio-output'
(out/'reference_measurements.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
lines=['# Mevcut seslerin teknik analizi','',f'15 kategorinin her birinden iki Türkçe ve iki İngilizce kayıt: toplam {len(results)} kayıt çözümlendi. Bu örneklem tüm arşivin kalite garantisi değildir.','', 'Kullanıcı tarafından belirtilen ses yönlendirmesi: gerçek kadın sesi, çocuklara hitap eden enerjik anlatım, normal hız. Bu nitelikler otomatik ölçümle doğrulanmış bir duygu veya konuşmacı tespiti değildir.','', '| Dil | Örnek | Ortanca süre | Ortanca etkin ses aralığı | Ortanca RMS | Ortanca tepe |','|---|---|---|---|---|---|']
for lang in ('tr','en'):
    rr=[r for r in results if r['language']==lang]
    median=lambda k: round(float(np.median([r[k] for r in rr])),2)
    lines.append(f'| {lang} | {len(rr)} | {median("duration_seconds")} sn | {median("active_span_seconds")} sn | {median("rms_dbfs")} dBFS | {median("peak_dbfs")} dBFS |')
lines += ['',f'Çözümlenen örneklerde örnekleme hızları: {sorted(set(r["sample_rate"] for r in results))} Hz. Kanal sayıları: {sorted(set(r["channels"] for r in results))}.', '', '## Üretime uygulanacak ilkeler', '', '- Normal hız ve enerjik, anlaşılır anlatım korunmalı; kelimeler tek tek okunmalı.', '- Kelime ve cümle uzunlukları farklıdır; tüm dosyaları aynı süreye sıkıştırmak doğru değildir. Süre, konuşma hızının doğrudan ölçümü değildir.', '- RMS değerleri LUFS değildir; yeni sesler ayrıca algısal ses seviyesi ve telaffuz açısından kontrol edilmeli.', '- Aynı konuşmacının kimliği bu ölçümlerden çıkarılamaz. Aynı kişiye ait yeni kayıt veya referans sesle üretim yapabilen bir hizmet gerekir.', '- Bu ortamın kullanılabilir araçlarında referans sesle konuşma üretimi bulunamadı. Yeni konuşma sentezlenmedi.', '- Benzer dosya adı aynı sözcük değildir: örneğin mandarin/tangerine ve geometrik triangle/müzik aleti triangle otomatik olarak eşleştirilmedi.']
(out/'voice_reference_analysis.md').write_text('\n'.join(lines)+'\n')
print('\n'.join(lines))
