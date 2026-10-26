"""One-time, auditable Turkish editorial migration. Package remains authoritative."""
import copy
import hashlib
import json
from pathlib import Path
import re
import zipfile

ROOT=Path(__file__).resolve().parents[1]
PACK=ROOT/'language-packs-v2/packages/tr'
REPORT=ROOT/'language-packs-v2/reports/tr-editorial'
LEGACY=Path('/Users/kursatdemirci/Projects/KidsWordPlus/BACKUP/KidsWordPlusProAndroid/app/src/main/assets')
BACKUP=LEGACY.parents[4]
SOUNDS=Path('/Users/kursatdemirci/Projects/KidsWordPlus/BACKUP/sounds_slides.zip')
GRAPHICS=Path('/Users/kursatdemirci/Projects/KidsWordPlus/BACKUP/Android KidsWordPlusPro 27_02_2023.zip')

# Reviewed against the English concept and, where ambiguous, original illustration.
CORRECTIONS={
 'home:1101':'Kara tahta','home:1102':'Blender','home:1108':'Kâse',
 'home:1109':'Boksör şortu','home:1131':'Çalışma masası','home:1141':'Kışlık kulaklık',
 'home:1150':'Yer küresi','home:1156':'Kapüşon','home:1165':'Paspas',
 'home:1166':'Tırnak törpüsü','home:1167':'Kolye','home:1176':'Kâğıt',
 'home:1179':'Raptiye','home:1183':'El çantası','home:1190':'Şal',
 'home:1192':'Skuter','home:1197':'Duş kabini','home:1203':'Rüzgâr gülü',
 'home:1212':'Kapüşonlu sweatshirt','home:1219':'Kravat','home:1231':'Yıkanma bezi',
 'home:1233':'Yo-yo','home:1258':'Atlama ipi','home:1260':'Sopalı oyuncak at',
 'home:1262':'Oyuncak ayı','home:1263':'Masa tenisi masası','home:1264':'Tuvalet fırçası',
 'home:1268':'Kalemtıraş','home:1269':'Pinbol makinesi',
 'buildings:160':'Akashi Kaikyō Köprüsü - Japonya',
 'buildings:174':'Öresund Köprüsü - İsveç ve Danimarka',
 'buildings:178':'Ponte Vecchio Köprüsü - İtalya',
 'buildings:181':'Si-o-se Pol Köprüsü - İran',
 'buildings:184':'Sultanahmet Camii - Türkiye',
 'buildings:185':'Sidney Liman Köprüsü - Avustralya',
 'buildings:186':'Tac Mahal - Hindistan',
}
ALPHABET='ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ'
NAMES=dict(zip(ALPHABET,['a','be','ce','çe','de','e','fe','ge','yumuşak ge','he','ı','i','je','ke','le','me','ne','o','ö','pe','re','se','şe','te','u','ü','ve','ye','ze']))
SUFFIX={'Ç':'cc','Ğ':'gg','I':'ii','İ':'i','Ö':'oo','Ş':'ss','Ü':'uu'}

def dump(p,d):
 p.parent.mkdir(parents=True,exist_ok=True)
 p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')

def main():
 path=PACK/'database.json';db=json.loads(path.read_text())
 REPORT.mkdir(parents=True,exist_ok=True)
 before=REPORT/'database.before.json'
 if before.exists():raise SystemExit('Migration already started/applied; edit package directly instead of replaying.')
 dump(before,db)
 legacy={}
 for p in sorted(LEGACY.glob('*_tr.json')):
  for w in json.loads(p.read_text()).get('Slide',[]):
   legacy.setdefault(w['WordSound'],[]).append({'title':w['Title'],'image':w['Image'],'source':str(p)})
 changed=[]
 def update(w,text,reason,spoken=None):
  old={'displayText':w['displayText'],'spokenText':w['spokenText']}
  w['displayText']=text;w['spokenText']=spoken if spoken is not None else text
  w['translationStatus']='assistant_reviewed_tr'
  w['reviewIssues']=[i for i in w.get('reviewIssues',[]) if i not in ('missing_translation','clock_grammar_review','language_curriculum')]
  if old!={'displayText':w['displayText'],'spokenText':w['spokenText']}:
   changed.append({'conceptId':w['conceptId'],'before':old,'displayText':text,'spokenText':w['spokenText'],'reason':reason})
 for w in db['words']:
  if not w['displayText']:
   candidates=legacy.get(Path(w['audio']['path']).name,[])
   titles={c['title'] for c in candidates}
   assert len(titles)==1,(w['conceptId'],titles)
   text=CORRECTIONS.get(w['conceptId'],next(iter(titles)).replace('–','-'))
   update(w,text,'missing_text_completed_with_editorial_review')
  if w['categoryId']=='clocks':
   source=Path(w['image']['path']).stem
   m=re.fullmatch(r'slide_my_school_clock_(\d+)(?:_(\d+))?',source)
   if m:
    h=int(m[1]);minute=int(m[2] or 0)
    base=['','bir','iki','üç','dört','beş','altı','yedi','sekiz','dokuz','on','on bir','on iki']
    accusative=['','biri','ikiyi','üçü','dördü','beşi','altıyı','yediyi','sekizi','dokuzu','onu','on biri','on ikiyi']
    dative=['','bire','ikiye','üçe','dörde','beşe','altıya','yediye','sekize','dokuza','ona','on bire','on ikiye']
    text={0:f'Saat {base[h]}',30:f'Saat {base[h]} buçuk',15:f'Saat {accusative[h]} çeyrek geçiyor',45:f'Saat {dative[h%12+1]} çeyrek var'}[minute]
    update(w,text,'Turkish_clock_grammar_corrected')
    w['clockTime']={'hour':h,'minute':minute,'cycleHours':12}
   else:
    w['clockTime']={'hour':1 if w['conceptId']=='clocks:239' else 2,'minute':5 if w['conceptId']=='clocks:239' else 10,'cycleHours':12}
 alpha={w['displayText']:w for w in db['words'] if w['subcategoryId']=='alphabet'}
 template=alpha['A']
 for letter in ALPHABET:
  if letter not in alpha:
   w=copy.deepcopy(template)
   w['conceptId']='school:tr-letter-'+f'{ord(letter):04x}'
   w['conceptScope']='language_specific';w['language']='tr'
   w['audio']={};w['image']={};db['words'].append(w);alpha[letter]=w
  w=alpha[letter]
  update(w,letter,'Turkish_29_letter_curriculum',NAMES[letter])
  w['learningEnabled']=True;w['alphabetPosition']=ALPHABET.index(letter)+1
  w['curriculumLessonId']='tr-alphabet-'+str((w['alphabetPosition']-1)//10+1)
  w['curriculumLessonPosition']=(w['alphabetPosition']-1)%10+1
  suffix=SUFFIX.get(letter,letter.lower());stem='slide_my_school_alphabets_'+suffix
  w['audio']={'path':'audio/'+stem+'_tr.mp3','available':False,'required':True,'provenance':'original_turkish_archive','qualityStatus':'legacy_letter_name_listening_review_pending'}
  w['image']={'path':'images/'+stem+'.jpg','available':False,'variant':'localized','overridePath':None}
 for letter in 'QWX':
  w=alpha[letter];w['learningEnabled']=False;w['disabledReason']='not_in_turkish_alphabet'
  w['spokenText']=None;w['translationStatus']='not_applicable_to_turkish_curriculum';w['reviewIssues']=[]
  w['audio']['required']=False
 # Reuse the original Turkish letter recordings, including dotless I and dotted İ.
 with zipfile.ZipFile(SOUNDS) as z,zipfile.ZipFile(GRAPHICS) as graphics:
  for letter in ALPHABET:
   w=alpha[letter];audio=PACK/w['audio']['path'];audio.write_bytes(z.read('sounds_slides/'+audio.name));w['audio']['available']=True
   w['audio']['sha256']=hashlib.sha256(audio.read_bytes()).hexdigest()
   image=PACK/w['image']['path'];entry='KidsWordPlusPro/app/src/main/res/drawable-xxxhdpi/'+image.name
   if entry in graphics.namelist():image.write_bytes(graphics.read(entry));w['image']['available']=True
 db['wordCount']=len(db['words']);db['baseConceptCount']=1450;db['languageSpecificConceptCount']=6
 db['activeWordCount']=sum(w.get('learningEnabled',True) for w in db['words'])
 db['contentVersion']='2-tr-editorial-draft'
 db['curricula']={'alphabet':{'language':'tr','letters':list(ALPHABET),'conceptIds':[alpha[c]['conceptId'] for c in ALPHABET],
     'lessonConceptIds':[[alpha[c]['conceptId'] for c in ALPHABET[i:i+10]] for i in range(0,29,10)],'orderField':'alphabetPosition','scope':'letter_names'}}
 # Metadata comparison is not a transcription or listening verification.
 review=[]
 for w in db['words']:
  if not w.get('learningEnabled',True):continue
  name=Path(w['audio']['path']).name;old=legacy.get(name,[])
  generated=w['audio'].get('spokenText')
  expected=w['spokenText']
  norm=lambda s: re.sub(r'\s+',' ',s.replace('–','-').strip()).casefold()
  if w['subcategoryId']=='alphabet':status='letter_name_listening_review'
  elif generated:status='generated_text_match' if norm(generated)==norm(expected) else 'generated_text_mismatch'
  elif old:status='legacy_label_match' if any(norm(c['title'])==norm(expected) for c in old) else 'legacy_label_differs_listen_before_replacing'
  else:status='no_legacy_label_listening_review'
  w['audio']['textAlignmentStatus']=status
  if status not in ('generated_text_match','legacy_label_match'):
   review.append({'conceptId':w['conceptId'],'displayText':w['displayText'],'expectedSpokenText':expected,
      'legacyLabels':list(dict.fromkeys(c['title'] for c in old)),'audioPath':w['audio']['path'],'status':status})
 dump(path,db);dump(REPORT/'changes.json',changed);dump(REPORT/'audio_text_review.json',review)
 dump(REPORT/'legacy_reference.json',legacy)
 summary={'filledMissingTexts':sum(c['reason']=='missing_text_completed_with_editorial_review' for c in changed),
   'correctedClockTexts':sum(c['reason']=='Turkish_clock_grammar_corrected' for c in changed),
   'wordCount':len(db['words']),'activeWordCount':db['activeWordCount'],'alphabetLetters':29,
   'missingDisplayTexts':sum(not w['displayText'] for w in db['words']),
   'missingActiveSpokenTexts':sum(w.get('learningEnabled',True) and not w['spokenText'] for w in db['words']),
   'audioReviewRecords':len(review),'listeningVerified':False}
 dump(REPORT/'summary.json',summary)
 lines=['# Türkçe paket düzenleme sonucu','',json.dumps(summary,ensure_ascii=False),'',
  '196 boş metin eski kayıt etiketleri ve anlam incelemesiyle tamamlandı. Saatlerin 48 metni düzeltildi. 29 harflik Türkçe alfabe tanımlandı. I/İ dosya eşleşmesi düzeltildi; altı dile özel kayıt eklendi. Q/W/X pasif kaldı.',
  '', '## Ses–metin inceleme kuyruğu','',
  'Aşağıdaki farklar eski veritabanı etiketleriyle karşılaştırmadır, dinlenerek doğrulanmış ses hataları değildir. Yazım, eş anlamlılık veya gerçek anlam farkı olabilir. Sesleri otomatik değiştirmeyin.',
  '', '| Kayıt | Beklenen ses metni | Eski etiket | Durum |','|---|---|---|---|']
 lines += [f"| {r['conceptId']} | {r['expectedSpokenText']} | {' / '.join(r['legacyLabels'])} | {r['status']} |" for r in review]
 (REPORT/'README.md').write_text('\n'.join(lines)+'\n')
 print(json.dumps(summary,ensure_ascii=False))

if __name__=='__main__':main()
