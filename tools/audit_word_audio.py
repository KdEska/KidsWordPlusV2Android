"""Audit V2 filenames and reuse exact-title recordings without modifying inputs."""
import collections
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'audio-output'
OUT.mkdir(exist_ok=True)
db = json.loads((ROOT / 'database-output/kidswordplus_unified_en_v2_subcategorized.json').read_text())
archive = zipfile.ZipFile('/Users/kursatdemirci/Projects/KidsWordPlus/BACKUP/sounds_slides.zip')
files = {Path(i.filename).name: i for i in archive.infolist()
         if not i.filename.startswith('__MACOSX/') and i.filename.endswith('.mp3')}
words = [(c['Id'], w) for c in db['Categories'] for lesson in c['Lessons'] for w in lesson['Words']]
by_title = collections.defaultdict(list)
for category, word in words:
    by_title[word['Title'].casefold()].append((category, word))
rows, reused, missing = [], [], []
with zipfile.ZipFile(OUT / 'reused_audio_additions.zip', 'w', zipfile.ZIP_DEFLATED) as additions:
    for category, word in words:
        for lang in ('en', 'tr'):
            expected = word['WordSound'].removesuffix('_en.mp3') + f'_{lang}.mp3'
            row = dict(category=category, word=word['Title'], language=lang,
                       filename=expected, added_in_v2=word.get('IsAddedInV2', False))
            if expected in files:
                row.update(status='filename_match', source=files[expected].filename)
            else:
                candidates = []
                for other_category, other in by_title[word['Title'].casefold()]:
                    # Geometric triangle and musical triangle differ in Turkish.
                    if word['Title'] == 'Triangle' and other_category != category:
                        continue
                    candidate = other['WordSound'].removesuffix('_en.mp3') + f'_{lang}.mp3'
                    if candidate in files:
                        candidates.append(candidate)
                if candidates:
                    source = sorted(set(candidates))[0]
                    payload = archive.read(files[source])
                    assert payload
                    additions.writestr(expected, payload)
                    row.update(status='reused_exact_database_title', source=files[source].filename)
                    reused.append(row)
                else:
                    row.update(status='content_decision_required' if lang == 'tr' and word['Title'] in ('Q','W','X') else 'recording_required')
                    missing.append(row)
            rows.append(row)
summary = dict(database_word_records=len(words), archive_mp3_files=len(files),
               required_language_references=len(rows), initial_missing_references=len(reused)+len(missing),
               reused_files=len(reused), generated_files=0, remaining_missing_references=len(missing),
               remaining_by_language=dict(collections.Counter(r['language'] for r in missing)),
               unique_remaining_filenames=len({r['filename'] for r in missing}),
               unique_remaining_title_language_pairs=len({(r['word'],r['language']) for r in missing}))
(OUT / 'audio_audit.json').write_text(json.dumps(dict(summary=summary, records=rows), ensure_ascii=False, indent=2))
(OUT / 'missing_audio.json').write_text(json.dumps(missing, ensure_ascii=False, indent=2))
header = '# Eklenen ses dosyaları\n\nYeni ses sentezlenmedi. Aşağıdaki kayıtlar, V2 içinde aynı İngilizce başlığı taşıyan başka kategorilerin mevcut dosyalarından birebir kopyalandı. Ses içeriği değiştirilmedi. Dinleyerek telaffuz doğrulaması henüz yapılmadı.\n\n| Kelime | Dil | Yeni dosya | Kaynak |\n|---|---|---|---|\n'
(OUT / 'added_audio.md').write_text(header + '\n'.join(f"| {r['word']} | {r['language']} | {r['filename']} | {r['source']} |" for r in reused)+'\n')
lines = ['# V2 Türkçe ve İngilizce ses denetimi', '', 'Dosya adı ve arşivde bulunma kontrolüdür; konuşulan metin, kadın sesi, ton ve ses kalitesi dinleyerek doğrulanmadı. Türkçe dosya adları İngilizce WordSound alanındaki _en son ekinden _tr türetilerek arandı. Girdiler değiştirilmedi.', '', f'- Kelime kaydı: {len(words)}', f'- Arşivde MP3: {len(files)}', f'- İlk eksik eşleşme: {len(reused)+len(missing)}', f'- Mevcut kayıttan tamamlanan dosya: {len(reused)}', '- Sentezlenen yeni ses: 0', f'- Kalan eksik: {len(missing)} (EN: {summary["remaining_by_language"].get("en",0)}, TR: {summary["remaining_by_language"].get("tr",0)})', '', '## Bekleyen işler', '', '- Aynı kadın sesiyle üretim için kaynak servis/ses kimliği veya uygun ses üretim erişimi gerekiyor.', '- Türkçe Q/W/X için içerik kararı gerekiyor; otomatik üretim kapsamına alınmadı.', '- Ülkeler ve bayraklardaki aynı ülke adı bir kez seslendirilip iki dosya adına dağıtılabilir.', '- Farklı ad altında bulunabilecek diğer kayıtlar bu kesin başlık eşleştirmesiyle dışlanmış değildir.', '', '## Eksik kayıt listesi', '', '| Kategori | Kelime (V2 başlığı) | Eksik diller |', '|---|---|---|']
grouped = collections.defaultdict(list)
for r in missing:
    grouped[(r['category'],r['word'])].append(r['language'])
for (category,title), languages in grouped.items():
    lines.append(f'| {category} | {title} | {", ".join(languages)} |')
(OUT / 'README.md').write_text('\n'.join(lines)+'\n')
with zipfile.ZipFile(OUT / 'reused_audio_additions.zip') as check:
    assert len(check.namelist()) == len(reused)
    assert check.testzip() is None
    for row in reused:
        assert check.read(row['filename']) == archive.read(row['source'])
print(json.dumps(summary,ensure_ascii=False,indent=2))
