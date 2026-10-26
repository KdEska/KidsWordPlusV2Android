# Satranç — iOS ekran düzeni

Referanslar: iOS ChessViews, ChessTrainingView, ChessBoardSetupView,
ChessKingSafetyView, ChessCustomGameView ve ChessOpeningsView.

- Ortak tahta: krem/mor kareler korunur; sol kenarda sıralar, altında dosya
  harfleri; 16 dp dış köşe, sarı seçim/son hamle ve yeşil hedef çerçeveleri,
  yasal boş hedeflerde yeşil noktalar. Her kare tam konum/taş etiketi taşır.
  Tüm kareleri metinle dolduran önceki görünüm kaldırıldı. 2D/3D ortak tercih
  oyun ve eğitim tahtalarında aynıdır. Oyun tahtası çevrilebilir.
- Üst gezinme: alt sayfalardaki gövde geri düğmesi üst başlığa taşındı.
  Oyun/eğitim/bulmaca/konum tahtalarında çocuk sekmeleri iOS'taki gibi gizlenir;
  menü ve kitaplık ekranlarında kalır. Geri, bulunduğu alt sayfaya döner.
- Dizim ve özel konum: görselli taş şeridi ve renk seçimi; gerçek mevcut
  ilerleme/taslak korunur. Yerleştirme hâlâ dokunarak yapılır.
- Alıştırma ve güvenlik: beyaz yönerge kartları, mor hedef ilerlemesi,
  daraltılmış hedef seçicisi ve ortak tahta. Metin kalabalığı azaltıldı.
- Taş ayrıntısı: hareket tahtası, tüm yasal hedef noktaları, deneme eylemi,
  hareket kuralı ve dinleme aynı beyaz bölümde sunulur.
- Şah/mat/pat ve tek hamlede mat: bölüm seçimi, konum seçimi, ilerleme,
  ortak tahta ve mavi cevap eylemleri. Bulmaca havuzları bu görsel adımda
  genişletilmez; mevcut gerçek sayılar gösterilir.
- Yeni oyun: görselli Beyaz/Siyah kartları, üç zorluk kartı ve yıldızlar;
  süre ve iki kişilik oyun desteği korunur. Büyük yazıda kartlar tek sütun.
- Oyun: oyuncu/kayıt özeti, ayrı saat kartları, ortak tahta, hamle geçmişi
  beyaz yüzeyde; bitmiş oyunda sonuç kartı/şerit. Mevcut oyun doğrudan açılır;
  yeni oyun seçimi mevcut oyunu onaysız değiştirmez.
- Açılış kitaplığı ve İzle/Sıra sende akışı önceki iOS çalışmasını korur;
  ders üst gezinmesi ve alt sekme davranışı ortaklaştırılır.
- Genel bilgiler: iOS'taki Tarihçe/Temel bilgiler/Ustalar bölümleri,
  modern/öncüler seçimi, on özgün portre ve kaynak/lisans bağlantıları.
  History/content/credits JSON dosyaları ve portreler mevcut referanstan
  değişmeden kopyalanmıştır; kaynak dosyalara yazılmaz.

Paylaşım kartlarının güncel kapsamı aşağıdaki bölümde belirtilmiştir. Oyun motoru, kayıt formatı ve öğrenme hedefleri değişmez.
Android yazı/sistem materyali farklıdır; tam piksel eşitliği iddia edilmez.
Kontroller `verification.json`, ekranlar `reviews/chess-ios-all/` altındadır.

## Serbest alıştırma
Serbest/Görevler geçişi, altı taşın seçimi, yasal kare işaretleri, art arda
hamleler, piyonun vezire dönüşmesi, sıfırlama ve özgün kural sesleri eklendi.
Taş ayrıntısından Sıra sende serbest modu açar. Alıştırma, maç kaydı veya
görev tamamlanması yazmaz. İki yeni kural testi dahil 67 test geçti; Debug,
imzasız Release ve lint başarılı. Tablet için ortak uyarlanabilir tahta
kullanılır; bu adımda yeni tablet ekran görüntüsü kontrolü yapılmadı.

## Taşları sürükleme
Ortak tahtada basılı tutarak sürükleme ve parmağı izleyen taş eklendi.
Oyun, görevler, serbest alıştırma, tek hamlede mat ve şahı kurtarma
hedefleri aynı kural doğrulamasını kullanır. Tahta dışı veya aynı kareye
bırakma hamle göndermez; geçersiz hedef taşı yerinde tutar. Bilgisayar
sırası, meşgul durum ve tamamlanmış hedeflerde sürükleme kapalıdır.
Terfi seçim penceresi maçta korunur. Dokunarak oynama devam eder.
Dizim/özel konum editörleri ve salt inceleme tahtaları bu davranışı almaz.
Telefon ve tablet serbest modda geçerli sürükleme/sıfırlama kontrol edildi;
telefonda geçersiz ve tahta dışı bırakma ile dokunma da denendi. Maç veya
mat/güvenlik hedefleri emülatörde tamamlanmadı; mevcut kayıt korunur.
İki yön ve üç tahta boyutunun koordinat eşlemesi ile sınır dışı girişler
test edildi. 69 test, Debug/imzasız Release ve lint başarılı.
Ekranlar: reviews/chess-drag/ (tablet görseli son metin ipucundan önce).

## On soruluk alıştırma turları
Özgün dokuz hedef ve kimlikleri korunarak, iOS ile aynı hedef karelerini
kullanan farklı başlangıç/engel konumları eklendi. Tüm oluşturulan
görevlerin erişilebilirliği kural testinde doğrulanır. Her tur 10 farklı
hedef, altı taş türü, en az bir alma hedefi ve taş başına en çok iki soru
içerir. Yeni tur önceki turun kimliklerini dışlar; uygun havuz kalmazsa
tam kütüphaneye döner. Sonraki hedef ancak doğru hamle sonrası açılır.
Tur sayacı 0–10 ilerlemesini gösterir; havuzdaki tüm görev sayısını göstermez.
Tur bitince tamamlanma metni ve Yeni tur eylemi gelir. Serbest moddan
görevlere dönmek yeni tur başlatır. Eski görev tamamlamaları korunur.
Turun soru listesi, sırası ve çözülme durumu Android ekran yeniden
oluşturmasında korunur; uygulamayı zorla kapatınca yeni tur açılır.
74 test (200 tur seçimi, atlama engeli, tamamlama, geri yükleme ve
eski kayıt uyumluluğu dahil), Debug, imzasız Release ve lint başarılı.

Emülatörde tablette on görevin tamamı çözülerek 10/10 ve tur sonu
doğrulandı. Telefon boyutuna geçişte tamamlanan tur korundu; Yeni tur
0/10 başlattı. Deneme tamamlamaları test öncesi profil geri yüklenerek
kaldırıldı; kayıtlı maç değişmedi. Ekranlar reviews/chess-round/ altında.

## Maç hamle animasyonu
Kaydedilen tek bir yeni hamle için 450 ms yol ön izlemesi ve 900 ms
yumuşak kayma eklendi. Rokta şah ve kale birlikte kayar. Taş alma ve
geçerken almada eski konum animasyon sonuna dek görselleştirilir; terfide
piyon inişte seçilen taşa dönüşür. Yol çizimi ve kayan taşlar yalnız
görseldir; erişilebilir kare açıklamaları kayıtlı konumu temsil eder.
Animasyon bitene kadar tahta/ipuçları kapalı ve bilgisayar cevabı bekler.
Geri alma, yeni oyun, tahta çevirme, görünüm değiştirme, sayfadan çıkma,
arka plana geçme ve Android hareket efektlerini kapatma bekleyen görseli
bırakır. Açılan eski kayıt veya saat güncellemesi hamleyi tekrar oynatmaz.
Motor ve kayıt biçimi değişmez. Android hamleyi önce doğrular ve kaydeder;
animasyon yalnız sonrasında çizilir. Efekt iptalinde hamle geri alınmaz,
yeniden uygulanmaz. Saat mevcut motorun yeni sıra davranışını korur.
Bu sıra iOS'un hamle öncesi görsel beklemesinden farklıdır; kayıt güvenliği
ve mevcut saat davranışı korunmuştur.

Doğrulama: 78 kural testi geçti; iki yön/renk için rok, tüm terfi
seçimleri, taş alma/geçerken alma ve tekrar oynatılmaması gereken
geçişler kapsanır. Emülatörde normal hamle/yol çizgisi ve Beyaz kısa rok
kontrol edildi; kayıt yalnız bir hamle içerdi, şah g1/kale f1 oldu.
Terfi/geçerken alma UI denemeleri emülatörün taze arayüz ağacı
verememesi nedeniyle tamamlanmadı. Son arka plan bilgisayar bekletme
kontrolü ayrıca elle denenmedi. Deneme profili kaldırılıp önceki profil
yedekle birebir karşılaştırılarak geri yüklendi; telefon görünümü ve
varsayılan Android hareket ayarı korundu. Görseller reviews/chess-motion/
altındadır; son arka plan kontrolünden önce çekilmiştir.

## Aile paylaşım kartları
Alıştırmada 10/10, tamamlanan tahta dizimi ve bitmiş maç için iOS düzeninde
PNG kartları eklendi: özgün logo, taç/ızgara/bayrak simgesi, sonuç metni,
isteğe bağlı tahta ve alt slogan. Kartlar 1080 piksel genişlikte, metin ve
tahta boyuna göre yükseklikte hazırlanır; tahta özgün 3D varlıkları kullanır.
Çocuğun adı, profil/hesap alanları veya öğrenme geçmişi renderere verilmez.
Çocuk ekranında 12–19 × 6–9 yetişkin doğrulaması, ön izleme ve ayrı Paylaş
eylemi vardır. Unicode rakamlar desteklenir; arka planda doğrulama sıfırlanır.
Görseller yalnız cache/family-share altında; dışa kapalı FileProvider ile
seçilen alıcıya geçici okuma izni verilir. Android pencere açar; otomatik
gönderim veya alıcı seçimi yapılmaz. 24 saatten eski kartlar sonraki
paylaşımda temizlenir. Mevcut diğer oyunların paylaşımı bu kapsama alınmadı.
80 test geçti; son simge düzenlemesinde Debug/imzasız Release ve lint
tekrar geçti. Tahta dizimi kartında yanlış/doğru cevap, ön izleme, PNG
çıktısı, sistem paylaşım penceresi ve arka plan kilidi denendi. Telefon ve
tablet ekranları kontrol edildi. Tur/maç düğmeleri ayrıca elle açılmadı.
Ekranlar ve incelenen gerçek PNG: reviews/family-share/. Deneme dizim
sayacı kaldırıldı; önceki profil yedekle birebir karşılaştırılarak yüklendi.

Ek doğrulama: terfi a7a8q ve geçerken alma d7d5/e5d6 artık emülatörde
de denendi. Eski UI engeli emülatörün RenderProxy.setStopped beklemesiydi;
aynı veriler gpu host / Vulkan kapalı ile açıldı. Yeni ekranlar
reviews/chess-motion/verified-*.png altında.

## Şah güvenliği ve bulmaca turları
Şah güvenliği için 80, şah/mat/pat için 240 (tür başına 80) ve tek hamlede
mat için 240 doğrulanmış konum eklendi. Eski dört güvenlik ve altı bulmaca
kimliği korunur; mevcut tercih anahtarları değişmez. Konumlar iOS'taki
üretim yaklaşımıyla hazırlanır: güvenlikte simetri/kaydırma, bitişlerde
yasal şah/vezir düzenleri, matta arka sıra kaleleri ve mat konumundan
geriye kurulmuş vezir soruları. Her mat sorusunda örnek çözüm saklanır;
oyun motorunun kabul ettiği diğer mat hamleleri de geçerlidir.

Her bölümün turu 10 benzersiz hedef içerir. Gruplar dengeli seçilir; yeni
tur önceki turun kimliklerini dışlar. Havuz yetersizse tam kütüphaneye
döner. Doğru cevap olmadan sonraki soru açılmaz; sayaç yalnız turdaki
tamamlamaları gösterir. Bölüm geçişi bitiş/mat turlarını ve çözülmüş
tahtayı korur. Telefon/tablet yeniden oluşturması turu korur. Tur sonunda
yeni tur ve mevcut ebeveyn doğrulamalı aile paylaşımı vardır.

Güvenlik yönergesi değişen kaynak/hedef kareleri kullanır. Özgün sabit
kare sesleri yalnız temel konumlarda oynar; varyantlarda taşın kayıtlı
hareket kuralı, tehdit bulmada genel tehdit kaydı kullanılır. Böylece ses
farklı kare söylemez. Tüm anahtarlar TR/EN kayıt envanterinde test edildi.

85 test geçti: üç havuzda toplam 300 seçilmiş tur, tüm üretilen
konumların kuralları, doğru ilerleme, eski kimlikler ve tur geri yükleme
dahil. Son boş kaynak karesi seçimini temizleyen görsel değişiklikte
Debug/imzasız Release ve lint tekrar çalıştırıldı. Emülatörde üç tam tur
(toplam 30 hedef), yanlış bitiş cevabı, 10/10 sonuç/paylaşım düğmeleri,
bölüm geçişinde çözülmüş tahta, telefonda 10/10 geri yükleme ve 0/10 yeni
tur kontrol edildi. Görseller reviews/puzzle-rounds/ altında; son seçim
çerçevesi temizliğinden önce çekildi. Deneme ilerlemesi kaldırıldı; önceki
profil test öncesi yedekle birebir karşılaştırılarak geri yüklendi.


## Satranç sayfa yardımları — 20 Ekim 2026

14 bağlama özel TR/EN yardım içeriği iOS PageHelp.swift ve help.json
referansından uyarlandı. Menü, genel bilgiler, taşlar/taş ayrıntısı,
alıştırma, dizilim, şah güvenliği, bitişler, tek hamlede mat, oyun,
özel konum, açılış listesi/dersi ve yeni oyun kapsanır. Sağ üstteki
yardım düğmesi numaralı beyaz kartları, kapatma ve Anladım eylemlerini
açar. Telefon içeriği kaydırılabilir; tablette genişlik 760dp ile sınırlı.
Tahta örneği göster/başa al ile iki konum arasında değişir; iOS'taki
ara animasyon bu örnekte yoktur. Sistem hareket ayarından bağımsızdır.
Bitiş yardımında Android'de henüz olmayan kaçış listesi anlatılmadı.

Yardım açılınca ses durur. Oyun saati ve yeni bilgisayar hamlesi
bekler; işlem sürerken yardım düğmesi devre dışıdır. Açılışta bekleyen
örnek hamle iptal edilir, ders adımı değişmez. Kapat ve sistem Geri
pencereyi kapatır. Mevcut kayıt biçimi ve görev ilerlemesi değişmedi.

Debug, imzasız Release ve lint başarılı. Emülatörde telefon menü
yardımı, tablet alıştırma/oyun yardımı, örneği göster/başa al ve sistem
Geri kontrol edildi. Kurulum öncesi/sonrası profil dosyası birebir aynı.
Ekran görüntüleri reviews/chess-help/ altında. Bu UI değişikliğinde
önceki 85 birim test yeniden çalıştırılmadı. Tüm 14 içerik ayrı ayrı
emülatörde dolaşılmadı; süreli oyun bekletmesi kod üzerinden kontrol
edildi, bu turda süreli oyun oluşturulmadı.


## Bitişlerde kaçış önizlemesi — 20 Ekim 2026

Doğru Şah cevabından sonra tüm yasal kurtuluş hamleleri listelenir.
Hamle düğmesi veya işaretli hedef kare 900ms tahta önizlemesi başlatır;
İlk konuma dön başlangıç konumunu geri getirir. Önizleme görev
ilerlemesine veya kayıtlı oyuna yazmaz. Mat/pat konumlarında liste yoktur.
Hareket kapalıysa sonuç doğrudan gösterilir. Görev/bölüm değişimi,
yardım, görünüm değişimi ve arka plana geçiş önizlemeyi temizler.
İlgili TR/EN sayfa yardımı kaçış listesini anlatacak şekilde güncellendi.

86 test geçti. Yeni test tüm 240 bitiş konumunda kaçış listesini yasal
hamlelerle karşılaştırır, her kaçıştan sonra oynayan şahın güvende ve
kaynak konumun değişmemiş olduğunu doğrular. Debug, imzasız Release ve
lint başarılı. Tablet emülatöründe doğru cevap sonrası liste, düğmeyle
hamle, hedef kareyle hamle, konuma dönüş ve arka plandan dönüş kontrol
edildi. Görsel reviews/ending-escapes/tablet-preview.png altında.
Deneme profili önceki yedekle birebir geri yüklendi; emülatör telefon
boyutuna döndürüldü. Telefon önizlemesi bu turda ayrıca görsel incelenmedi.


## Görsel hamle geçmişi — 20 Ekim 2026

Oyun geçmişi iOS referansındaki açılır beyaz panele dönüştürüldü.
Numaralı satırlarda beyaz/siyah sütunları, mevcut 2D/3D görünümündeki
taş görseli, taraf, taş adı ve kaynak/hedef kareler gösterilir. Terfi
satırında ilk taş ve terfi edilen taşın adı korunur. Eksik yarım hamle
çizgiyle belirtilir. Siyahın başladığı özel konumlarda beyaz hücresi
boş bırakılır; başlangıç FEN hamle numarası korunur. Kaydırılabilir liste
220dp ile sınırlı ve açılınca/yeni hamlede son satıra gider. Genişleme
aynı oyun için ekran yeniden oluşturmasında korunur. Sesli erişim etiketi
taraf, taş, terfi ve kareleri birlikte okur. Kayıt biçimi değişmedi.

Debug ve imzasız Release derlendi, lint ve 89 test geçti. Üç yeni test
normal çiftleme/eksik sıra, siyah başlangıç ve özel hamle numarası,
terfi ve rokta ilk taş bilgisini doğrular. İlk siyah başlangıç testindeki
yalnız iki şahlı bitmiş konum, oynamaya devam eden bir konumla düzeltildi.
Emülatörde mevcut beş yarım hamleli oyunla telefon/tablet görünümü ve
aç/kapat kontrol edildi; profile.xml test öncesiyle birebir aynı kaldı.
Görseller reviews/move-history/ altında. Bu turda uzun geçmişin otomatik
son satıra kayması ayrıca emülatörde oynanarak test edilmedi.


## Yardım tercihi ve örnek hareketi — 20 Ekim 2026

Ebeveyn Ayarlar ekranına iOS benzeri Yardımı göster anahtarı eklendi.
Varsayılan açık; profile/help.enabled tercihi kalıcıdır. Değiştirme
model katmanında da ebeveyn doğrulaması gerektirir. Kapalıyken satranç
yardım düğmeleri gizlenir ve açık pencere kapanır. Açıklama yalnız mevcut
Android kapsamını, satranç yardımını belirtir; öğren/oyun yardımı henüz
bu tercihe bağlanacak içerik sunmaz.

Yardım tahtasının piyonu 800ms ile hedefe/ilk konuma hareket eder;
iOS'taki turkuaz hedef çerçevesi eklendi. Sistem animasyonu kapalıysa
konum doğrudan değişir; dekoratif tahta erişilebilirlik ağacından gizli.
Bu, önceki statik örnek sınırlamasını kaldırır.

Debug, imzasız Release ve lint geçti. UI/tercih değişikliği için önceki
89 birim test yeniden çalıştırılmadı. Emülatörde ebeveyn kapısı üzerinden
anahtarın kapatılması, uygulama yeniden açılınca simgenin gizli kalması,
özgün profil geri yüklenince yeniden yardım açılması ve örneği göster/
başa al kontrol edildi. Görseller reviews/help-settings/ altında.
Özgün profil birebir geri yüklendi; emülatör telefon boyutuna döndürüldü.


## Tam ekran denetimi — 20 Ekim 2026

Yeni kaynak karşılaştırması, 68 ekran/durum ve 15 fark grubunu
UI-PARITY-AUDIT.md / ui-parity-audit.json içinde tutar. Önceki
uyarlamalar tam görsel eşitlik onayı olarak değerlendirilmez. İlk turda
splash eklendi, görseller ilk kareden önce hazırlandı, bahçe sistem
çubuklarının arkasına taşındı ve satranç başlıkları iOS TR/EN metinlerine
eşlendi. Debug/imzasız Release/lint geçti; emülatör açılış kontrolleri
ve kalan açık işler ana denetim kaydında.


### Ortak yardım — 2026-10-20

Satranç yardımları PageHelp ortak bileşenine taşındı. 27 TR/EN konu, daireli simge, alttan açılan büyük panel ve ses/kart/tahta örnekleri mevcut. Yardım tercihi Öğren/Oyunlar/Satranç için ortak; açık yardım maçın mevcut duraklatma davranışını korur. Android telefon/tablet kontrolleri geçti; tam eşlenmiş iOS piksel onayı bekliyor.


### Genel tercihler — 2026-10-20

Satranç manuel sesleri genel Ses kapalıysa başlatılmaz ve ortak görünür uyarı gösterilir. Otomatik hamle anlatımı sessiz kalır; ayrı anlatım tercihi değiştirilmez. Uygulama Animasyon tercihi ortak LocalKidMotion üzerinden satranç hareketlerini de kapsar ve sistem hareket azaltmasına üstün gelemez.


### Satranç menüsü ve ayrı görev rotaları — 2026-10-20

Menüde ince şevron ve ölçeklenen ortak etiket yüksekliği. Mat/Pat ve Tek Hamlede Mat rotaları sabit; mod seçici kaldırıldı. Görev sayacı/ilerleme, cevap ve sonraki yerleşimi iOS sırasına alındı; mat yönerge/ses kartı ve üst sıfırlama eklendi. Yasal hedefler görünür. 96 test, Debug/Release/lint ve telefon/tablet/büyük yazı geçiş kontrolleri geçti. Tam eşlenmiş piksel incelemesi henüz tamamlanmadı.


### Tahta hazırlama ve özel konum — 2026-10-20

Yeni ChessSetupScreen, iOS yönerge/sayaç/renk/hedef/geri bildirim düzenini kullanır. Ortak palet sürükleme, özel konumda taş taşıma, geri alma, otomatik/rastgele seçili taraf ve iki taraf dizimi eklendi. Yatay tablette üç sütunlu palet ve iki panelli düzen kullanılır. Özel maçtan taslağa düzenleme ve devam geçişleri mevcut. 99 test ve Android 32 taş/özel konum/telefon-yatay tablet akışları geçti. Oyuncu/zorluk tercihi süreç kalıcılığı ve tam piksel onayı bekliyor.


2026-10-20: Maç alt eylemleri mavi Geri al/İpucu ve yeşil Yeni oyun şeklinde düzenlendi. Üst maç ayarları alttan açılan beyaz panelde seslendirme, kalıcı hamle animasyonu tercihi ve tahta çevirme içerir. Panel açıkken saat/AI görünürlük kapısı kapanır. Süreyi maç içinde değiştirme ve özel oyunun taraf/zorluk kalıcılığı açık.
