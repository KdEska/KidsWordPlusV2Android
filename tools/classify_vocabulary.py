"""Generate a reviewable, concept-based three-level vocabulary classification.
This is an app curriculum grouping, not a CEFR or age assessment.
"""
from pathlib import Path
import json,collections
ROOT=Path(__file__).resolve().parents[1]
def write(p,x):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(x,ensure_ascii=False,indent=2)+'\n')
EASY='''Kedi|Köpek|Balık|Kuş|Tavşan|Kaplumbağa|İnek|At|Koyun|Kuzu|Tavuk|Civciv|Ördek|Keçi|Eşek|Arı|Karınca|Kelebek|Aslan|Kaplan|Fil|Zürafa|Maymun|Zebra|Ayı|Fare|Yılan|Kurbağa|Yunus|Balina|Sincap|Tilki|Horoz|Domuz|Kaz|Ev|Okul|Hastane|Park|Market|Oyun parkı|Deniz|Dağ|Orman|Plaj|Bahçe|Çiçek|Ağaç|Yaprak|Çimen|Çiftlik|Çiftçi|Tarla|Tohum|Meyveler|Sebzeler|Süt|Su|Ekmek|Peynir|Yumurta|Yoğurt|Tereyağı|Çorba|Makarna|Pirinç|Sandviç|Pizza|Patates kızartması|Meyve suyu|Çay|Kek|Şekerleme|Çikolata|Kurabiye|Bal|Dondurma|Reçel|Tuz|Şeker|Buz|Elma|Muz|Portakal|Mandalina|Armut|Şeftali|Kiraz|Üzüm|Çilek|Karpuz|Kavun|Limon|Kayısı|Erik|Domates|Patates|Soğan|Havuç|Salatalık|Biber|Mısır|Bezelye|Fasulye|Kabak|Otomobil|Otobüs|Tren|Kamyon|Uçak|Helikopter|Gemi|Tekne|Traktör|Taksi|İtfaiye aracı|Kol|El|Parmak|Bacak|Ayak|Baş|Saç|Göz|Kulak|Burun|Ağız|Diş|Dil|Boyun|Omuz|Diz|Karın|Gitar|Davul|Piyano|Flüt|Düdük|Keman|Top|Balon|Bisiklet|Oyuncak|Oyuncak bebek|Oyuncak ayı|Robot|Kitap|Defter|Kurşun kalem|Tükenmez kalem|Silgi|Cetvel|Kâğıt|Makas|Yapıştırıcı|Bilgisayar|Telefon|Televizyon|Saat|Kapı|Pencere|Masa|Sandalye|Koltuk|Halı|Yatak|Yatak odası|Çanta|Sırt çantası|Şişe|Kova|Anahtar|Şemsiye|Bardak|Fincan|Kâse|Tabak|Çatal|Kaşık|Bıçak|Tencere|Tava|Fırın|Buzdolabı|Sabun|Havlu|Diş fırçası|Diş macunu|Tarak|Fırça|Şapka|Mont|Kaban|Gömlek|Tişört|Pantolon|Şort|Elbise|Etek|Çorap|Ayakkabı|Terlik|Bot|Eldiven|Kazak|Pijama|Öğretmen|Öğrenci|Doktor|Hemşire|Polis|İtfaiyeci|Şoför|Aşçı|Fırıncı|Güneş|Ay|Yıldız|Dünya|Roket|Astronot|Gezegen|Siyah|Beyaz|Mavi|Kırmızı|Yeşil|Sarı|Pembe|Mor|Kahverengi|Gri|Daire|Kare|Üçgen|Dikdörtgen|Bir|İki|Üç|Dört|Beş|Altı|Yedi|Sekiz|Dokuz|On|Sıfır'''.split('|')
HARD='''Pulsar|Kuazar|Eksen|Yörünge|Kozmik|Kozmonot|Apollo 11|Hubble Teleskobu|Kara delik|Asteroit kuşağı|Cüce gezegen|Ay modülü|Uzay sondası|Uluslararası Uzay İstasyonu|Paramotor|Draglayn ekskavatör|Kırıcılı ekskavatör|Teleskopik vinç|El kompaktörü|Mikrometre|Kumpas|Asfalt serme makinesi|Greyder|Petrol platformu|Safran|Zerdeçal|Polen|Yer bademi|Üvez|Ahlat|Demirhindi|Turna yemişi|Opak|Şeffaf|Katı|Esnek|Mat|Hayal kırıklığına uğramış|Minnettar|Kendinden emin|Cesaretlendirmek|Teselli etmek|İş birliği yapmak|Paramedik|Resepsiyonist|Siyasetçi|İnşaat mühendisi|Bilgisayar mühendisi|Elektronik mühendisi|Obua|Korno|Kanun|Konga|Ksilofon|Çelik üçgen'''.split('|')
COUNTRY_EASY=set('Türkiye|Amerika Birleşik Devletleri|Birleşik Krallık|İngiltere|Almanya|Fransa|İtalya|İspanya|Çin|Japonya|Rusya|Brezilya|Kanada|Avustralya|Hindistan'.split('|'))
COUNTRY_MEDIUM=set('Arjantin|Avusturya|Azerbaycan|Belçika|Bulgaristan|Danimarka|Mısır|Finlandiya|Yunanistan|Gürcistan|Hollanda|İran|Irak|İrlanda|İsrail|İsveç|İsviçre|Kazakistan|Kıbrıs|Güney Kore|Meksika|Norveç|Pakistan|Polonya|Portekiz|Romanya|Suudi Arabistan|Suriye|Ukrayna|Yeni Zelanda|Güney Afrika|Fas|Tunus|Tayland|Endonezya|İskoçya|Galler|Küba|Peru|Şili|Kolombiya|Macaristan|Çekya|Hırvatistan|Sırbistan|Bosna-Hersek'.split('|'))
def classify(w):
 c,s,t=w['categoryId'],w['subcategoryId'],w['displayText']
 if c in ['countries','flags']:return 'beginner' if t in COUNTRY_EASY else 'intermediate' if t in COUNTRY_MEDIUM else 'advanced'
 if c=='clocks':return 'beginner' if s=='full_hours' else 'intermediate' if s=='half_hours' else 'advanced'
 if s=='alphabet':return 'beginner'
 if s in ['landmarks_bridges','constellations','body_systems','organs','punctuation']:return 'advanced'
 if t in HARD:return 'advanced'
 if t in EASY:return 'beginner'
 if c=='daily_life_communication':
  if s=='actions':return 'beginner' if w['lessonNumber']<=3 else 'intermediate'
  if s=='feelings':return 'beginner' if t in ['Mutlu','Üzgün','Kızgın','Korkmuş','Şaşkın','Yorgun'] else 'intermediate'
  if s=='daily_routines':return 'beginner'
  return 'beginner' if t in ['Paylaşmak','Yardım etmek','Teşekkür etmek','Selamlaşmak','Dinlemek'] else 'intermediate'
 if c=='concepts_relationships':
  if s=='opposites':return 'beginner' if w['lessonNumber']<=3 else 'intermediate'
  if s=='time_sequence':return 'intermediate'
  if s=='positions_directions':return 'beginner' if t in ['İçinde','Dışında','Üstünde','Altında','Önünde','Arkasında','Yanında','Yukarı','Aşağı','İçeri','Dışarı'] else 'intermediate'
 if s=='ordinal_numbers':return 'advanced' if w['lessonNumber']>=2 else 'intermediate'
 if c=='buildings' and (' - ' in t or t in ['İskenderiye Feneri']):return 'advanced'
 return 'intermediate'
def main():
 tr=json.loads((ROOT/'language-packs-v2/packages/tr/database.json').read_text())['words']
 levels={w['conceptId']:classify(w) for w in tr}
 # Language-specific letters are always foundational; no spelling-length heuristics.
 for p in (ROOT/'language-packs-v2/packages').glob('*/database.json'):
  for w in json.loads(p.read_text())['words']:
   if w['conceptId'] not in levels:
    assert w['subcategoryId']=='alphabet',w['conceptId']
    levels[w['conceptId']]='beginner'
 master={'version':1,'levels':levels}
 write(ROOT/'planning/vocabulary-levels/levels.json',master)
 write(ROOT/'KidsWordPlusV2/Localization/vocabulary-levels.json',master)
 write(ROOT/'KidsWordPlusAndroid/app/src/main/assets/localization/vocabulary-levels.json',master)
 rows=['# Kelime seviyeleri','', 'Uygulamaya özgü içerik sınıflandırmasıdır; CEFR veya yaş ölçümü değildir. Günlük somut sözcükler basit, daha özel sözcükler orta, teknik ve soyut kavramlar zor olarak gruplandırılır. Aynı kavram bütün dillerde aynı gruptadır.','', '| Kategori | Basit | Orta | Zor |','|---|---:|---:|---:|']
 for c in dict.fromkeys(w['categoryId'] for w in tr):
  counts=collections.Counter(levels[w['conceptId']] for w in tr if w['categoryId']==c);rows.append(f"| {c} | {counts['beginner']} | {counts['intermediate']} | {counts['advanced']} |")
 rows+=['','## İnceleme listesi','']
 for level,title in [('beginner','Basit'),('intermediate','Orta'),('advanced','Zor')]:
  rows+=['### '+title,'',', '.join(w['displayText'] for w in tr if levels[w['conceptId']]==level),'']
 (ROOT/'planning/vocabulary-levels/README.md').write_text('\n'.join(rows))
 print(collections.Counter(levels[w['conceptId']] for w in tr), 'concepts:',len(levels))
if __name__=='__main__':main()
