# iOS arayüzünün Android’e uyarlanması

Referans: güncel `KidsWordPlusV2/App/HomeViews.swift`, `SetupViews.swift`,
`KidsWordPlusApp.swift` ve `ChessViews.swift`. Eski `Tests/screenshots/home`
kayıtları güncel tasarımdan farklı olduğu için düzen referansı olarak kullanılmadı.
Açık iPhone 17 / iOS 26.5 simülatörünün güncel ana ekranı da incelendi.

## Eşlenen tasarım

- Ana Sayfa / Öğren / Oyunlar / Satranç / Profil sekmeleri; yüzen yarı saydam alt menü.
- Tam ekran bahçe, ekran başına başlık; genel beyaz panel kaldırıldı.
- Ana sayfada logo, konuşma balonu ve selamlayan Piko, beyaz ders kartı, yeşil
  ilerleme, mor geçişli eylem düğmesi, ilk altı kategori ve tümünü açma bağlantısı.
- Kategorilerde aynı altı renk ailesi, ışık halkaları, 28 dp köşeler ve telefon
  için iki sütun. Tablet sütun sayısı kullanılabilir genişliğe göre değişir.
- Alt kategorilerde yatay 104×112 görsel, ders/kelime sayısı, ilerleme ve sağ ok.
- Kelime dersinde beyaz görsel kartı, ortalı mor kelime, mavi dinleme ve altta
  mor/yeşil devam düğmesi.
- Oyunlarda logo/yıldız/karakter başlığı, günlük skor, turuncu skor tablosu,
  resimli renkli oyun kartları; kapalı hafıza kartlarında mor yüz ve yıldız.
- Satrançta büyük kapak, dil rozeti ve iOS’taki on menü kartı. Taş tanıtımı ile
  alıştırma ayrıldı; mevcut ders, konum ve oyun kuralları korundu.
- Profilde karakter başlığı, ad alanı, karakter dünyası ve iki sütunlu seçim.
  Ayarlar dişlisi ebeveyn kapısını açar. Ad değişikliği çocuk profilinde mümkündür;
  dil/paket yönetimi ebeveyn kapısı gerektirir.
- İlk kurulum: arayüz dili, öğrenme dili, paket, ad, karakter ve özet.
- Ortak renkler iOS `KidText` değerlerinden; kartlar ve ana düğmeler iOS
  ölçüleri/geçiş renklerinden alındı. Satranç tahtaları mor/krem palete geçirildi.

Konuşma pratiği, önceki kapsam kararı gereği menüye eklenmedi. Sistem çubukları,
klavye, Android yazı tipi çizimi ve saydam menünün işlenmesi işletim sistemine
bağlıdır; iOS Liquid Glass ve SF Symbols birebir taşınmadı. 21 karakterin dört hizalanmış selamlama pozu ve iOS ile aynı sekiz saniyelik
el sallama/göz kırpma akışı aktarıldı. Hareketler uygulama arka plandayken ve
Android animasyonları kapatıldığında durur. Açık tema eşlendi;
koyu tema ve tüm alt ekranların piksel düzeyinde eşitliği henüz doğrulanmadı.

Doğrulama kayıtları: `verification.json`, ekran görüntüleri:
`../reviews/ios-parity/`.

## İkinci görsel eşleme adımı

- `WelcomeSplashView.swift` içindeki dört poz ve süreler ana ekran için taşındı.
  `scripts/prepare_greeting_assets.py`, Piko PNG’leri ve diğer karakterlerin
  atlas/frames.json hizalarını kullanarak 84 WebP üretir (4.304.432 bayt).
  Referanslar değiştirilmez. `greeting-assets.json` kaynak/hedef hash dökümüdür.
- Yalnız seçili karakterin dört karesi çözülür; önbellek en çok 12 MiB’dır.
  Eksik bir set statik karaktere döner; karakter değişiminde eski set gösterilmez.
- Ana düğmelere iOS’taki 4 dp basılma hareketi eklendi; Android’in animasyon
  ayarı kapalıysa bu hareket de kapatılır.
- Açılış kitaplığında kapak, dört açılır ilke kartı ve sekiz ders kartı var.
  Her önizleme o dersin gerçek son konumunu ve son hedefini gösterir.
  Seçim mevcut kayıtlı derse gider; açılış sayıları/kazanılan işaretler korunur.
- Açılış ve ders başlıkları üst gezinme başlığına yansır.
- Piko kare değişimi ve kapalı animasyon ayarındaki sabitlik, telefon ilke
  açıklaması/derse dönüş ve tablet kitaplık düzeni emülatörde kontrol edildi.
  Diğer 20 karakterin dosyaları ve tüm APK girişlerinin hash’leri doğrulandı;
  her karakterin cihazda ayrı görsel kabulü henüz yapılmadı.

Ekranlar: `../reviews/ios-motion-openings/`. Tüm alt ekranların eşdeğerliği, koyu tema ve fiziksel cihaz kabulü hâlâ bekliyor.

## Açılış derslerinde izleme ve alıştırma

`ChessOpeningLesson.kt`, iOS `ChessOpeningLessonView` düzenini izler: İzle /
Sıra sende seçimi, hamle sayısı, sırası gelen renk, mor/krem tahta, turkuaz
son hamle kenarları, yeşil ipucu, hamle düğmeleri ve ayrı açıklama kartları.
Taş 900 ms boyunca eski konumdan yeni konuma kayar; hareket bitince sayaç ilerler.
Hareket sırasında yeni hamle engellenir. Arka plana geçiş, mod değişimi veya
dersin kapanması yarım hareketi iptal eder. Hareket azaltma tercihinde geçiş
beklemeden tamamlanır. Tercih Android ayarından doğrudan okunur.

İzle sayacı yalnız ekran durumudur; öğrenme sayacına veya tamamlanan derslere
yazmaz. Sıra sende mevcut kayıtlı alıştırmaya döner. Bu, mevcut Android
kayıtlarını koruyan bilinçli bir farktır: mod değişimi alıştırmayı otomatik
sıfırlamaz. Baştan / Yeniden çalış düğmeleri sıfırlar, kazanılan tamamlanma
işareti korunur. Mevcut satranç oyunu değişmez.

Yatay tablette açıklamalar solda, tahta sağdadır; tahta yüksekliğe göre de
sınırlandırılır. Telefon ve dikey tablette ekran kaydırılır. 2D/3D görünüm seçicisi sonraki görsel eşleme adımında eklendi.

Emülatörde Londra izleme dizisinin tamamı, hızlı çift dokunma engeli, arka
planda iptal, yanlış alıştırma hamlesinin reddi, doğru hamlenin bir kez kaydı
ve deneme sonrası önceki sayacın korunması kontrol edildi. İtalyan Açılışı’nın
5/6 kaydına dönüş ve yatay tablet düzeni kontrol edildi.
Ekranlar: `../reviews/opening-watch/`.

## Ortak 2D / 3D taş görünümü

`ChessAppearance.kt`, iOS `ChessAppearancePicker` ve `ChessFlatPiece`
bileşenlerini izler. Altı klasik taşın konturları, eğrileri, detay çizgileri,
renkleri ve ortak tabanı iOS’taki normalize koordinatlardan Canvas’a aktarıldı.
Tek ölçek kullanıldığı için dikdörtgen önizlemelerde taşlar esnemez.

Varsayılan 2D’dir. 3D mevcut beyaz/siyah karakter PNG’lerini kullanır; portre
netliği için çözme sınırı 512 piksele yükseltildi. Görsel yüklenirken veya
açılamadığında 2D çizim gösterilir.

Tercih `chess-appearance` SharedPreferences alanındaki
`chess.piece.appearance.v1` anahtarında `classic` / `characters` olarak
saklanır. Profil, öğrenme ve oyun kaydına yazmaz. Tüm canlı oyun, eğitim,
bulmaca, konum ve açılış tahtaları aynı tercihi okur. iOS’taki gibi açılış
kitaplığının dekoratif tahta önizlemeleri 3D kalır.

İki seçim kartı kendi tarzını bağımsız gösterir; seçili kart kenarlık/işaretle
belirtilir. Büyük yazıda seçimler tek sütuna geçer. Ekran okuyucu etiketleri
TR/EN, 2D/3D ve seçili durumunu içerir.

Ekranlar ve cihaz doğrulamaları: `../reviews/chess-appearance/`,
`verification.json`. Fiziksel cihaz, TalkBack ile insan kabulü, tüm tahtaların
tek tek kontrolü ve tam iOS piksel eşitliği bekliyor.


### Taşlar ekranı kartları

iOS `ChessPiecesView` düzenine göre tekrarlanan büyük başlık kaldırıldı,
dil rozeti eklendi. Kartlar 145 dp minimum genişlikle kullanılabilir alana
göre dizilir; büyük yazıda minimum 280 dp olur. İçerik telefonda 600 dp,
tablette 820 dp ile sınırlıdır. Son satırdaki eksik hücreler kartların
genişlemesine neden olmaz. 26 dp köşeler, taşa özgü altı kenarlık rengi
ve aynı renkte daireli ileri simgesi kullanılır.


### Büyük yazıda alt gezinme

Alt menü, metin genişliğini geçerli yazı ölçeğiyle ölçerek sekme genişliğini
belirler. Normal boyutta beş sekme alanı eşit paylaşır. Etiketler sığmazsa
menü yatay kayar; seçilen sekme, yazı boyutu veya ekran boyutu değiştiğinde
görünür alana taşınır. Sistem hareketleri kapalıysa bu geçiş animasyonsuzdur.
Sekmeler en az 48 dp genişlikte ve 56 dp yüksekliktedir; seçili durum ve
sekme rolü erişilebilirlik ağacında korunur.


### Karşılama balonunda büyük yazı

Normal yerleşimde balonun yüksekliği artık metinden hesaplanır; 130 dp
sabit üst sınır kaldırıldı. Yazı ölçeği 1.6 ve üzerindeyse veya içerik
genişliği 320 dp altındaysa karakter ve balon alt alta yerleşir. Balon
tam genişliği kullanır; selamlama, açıklama ve öğrenme dili satırı
kısıtlı yüksekliğe sıkıştırılmaz. Karakter animasyonu ve profil bağlantısı
aynı kalır.


### Oyun ekranlarının tamamı

Dinle ve Seç, Tekrar, Eşleştir, Hafıza, seviye seçimleri, sonuçlar, günlük
görevler ve skor tablosu iOS HomeViews.swift referansıyla ele alındı.
Ayrıntılı eşleştirme ve platform farkları: `GAMES-UI.md`.


### Profil ve karakter dünyası

`SetupViews.swift` içindeki ProfileEditorView, ProfileNameField, CompanionPicker
ve CompanionChoiceCard referans alındı. Profil başlığı ve dil, 28 dp köşeli
özet kartında ortalanır. Ad alanında kalem simgesi, 70 dp minimum yükseklik,
odak kenarlığı ve 30 karakter sayacı bulunur. Yazma durunca 600 ms sonra,
Bitti/odak kaybında ve ekrandan ayrılırken ad mevcut kayıt yöntemiyle saklanır.

Karakter dünyasında toplam arkadaş/kelime sayısı, tüm diller açıklaması,
başlangıç arkadaşları ve yeni arkadaşlar ayrı bölümlerdir. Kartlar iki sütun,
büyük yazıda tek sütundur. 145 dp portreler, seçili durum işareti ve yeşil
seçim etiketleri kullanılır. Kilitli portreler gri ve %42 opaklıktadır; kelime
hedefi ve ek görev koşulları kartın içinde görünür. Nova tam genişlikte ayrı
karttır; kilitliyken PNG alfa maskesinden mor dış çizgi çizilir, renkli karakter
gösterilmez. Açılma koşulları ve kazanılmış karakterler değişmez.

Ayarlar düğmesi mevcut ebeveyn kilidine gider. Ebeveyn raporları bu profil
görsel düzenlemesinin kapsamı dışındadır. Ekran kayıtları: `reviews/profile-ios/`.


### Ebeveyn alanı

Tek uzun rapor listesi, iOS ParentDashboardView düzenindeki ayrı Ana Sayfa,
Raporlar, Profil ve Ayarlar sekmelerine taşındı. Çocuk alt menüsü bu alanda
gösterilmez. Haftalık özet ve grafik, görselli raporlar, mevcut dil ayarları
ve paket yönetimi düzenlendi. Ayrıntılar ve işlev kapsamı: `PARENT-UI.md`.


### Satranç alt ekranları

Ortak tahta, görselli taş seçimi, yönerge ve yeni oyun kartları tüm satranç
alt ekranlarına uygulandı. Eğitim/oyun tahtalarında çocuk alt sekmeleri
gizlenir ve geri dönüş üst başlıktan yapılır. Tarihçe, Temel bilgiler ve
on portreli Ustalar içeriği özgün iOS dosyalarından alındı; kaynak/lisans
bağlantıları korunur. Ayrıntılar: `CHESS-IOS-UI.md`; portrelerin kaynak ve
SHA-256 dökümü: `chess-history-assets.json`.

### Satranç aile kartları — 20 Ekim 2026
On görevlik tur sonu, tahta dizimi ve maç sonucu için yetişkin doğrulamalı
PNG paylaşım kartları eklendi. Telefon/tablet ön izleme, Android paylaşım
penceresi ve arka plan kilidi kontrol edildi; 80 test geçti. Ayrıntılar
CHESS-IOS-UI.md ve verification.json içinde. Terfi/geçerken alma ekran
kontrolleri de tamamlandı. Önceki profil denemelerden sonra geri yüklendi.

### Güvenlik ve bulmaca turları — 20 Ekim 2026
Şah güvenliği, şah/mat/pat ve tek hamlede mat ekranları dengeli on soruluk
turlara taşındı. 80/240/240 konum ve eski kayıt uyumluluğu doğrulandı.
85 test geçti; emülatörde her bölümde tam tur, telefon geri yükleme ve
bölüm geçişi denendi. Ayrıntılar CHESS-IOS-UI.md içinde.


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


## Ders tamamlama — 20 Ekim 2026

U06 uygulandı: karakterli kutlama, gerçek yıldız farkı, küçük kelime
görselleri, ders görselli ebeveyn doğrulamalı paylaşım, sonraki/tekrar/geri
eylemleri. 91 test ve derleme/lint geçti. Telefon/tablet ve 24 kelimelik
test akışı doğrulandı; özgün profil geri yüklendi. Son ikonlarla dört
kelimelik ek kontrol yapıldı. Ayrıntılar UI-PARITY-AUDIT.md içinde.


### Ortak yardım — 2026-10-20

Satranç yardımları PageHelp ortak bileşenine taşındı. 27 TR/EN konu, daireli simge, alttan açılan büyük panel ve ses/kart/tahta örnekleri mevcut. Yardım tercihi Öğren/Oyunlar/Satranç için ortak; açık yardım maçın mevcut duraklatma davranışını korur. Android telefon/tablet kontrolleri geçti; tam eşlenmiş iOS piksel onayı bekliyor.


### Ses çalma geri bildirimi — 2026-10-20

WordAudioEffect ortak dalga ve çalma vurgusunu sağlar. Durum yalnız MediaPlayer hazır olup başladığında açılır; bitiş/hata/stop ile temizlenir. Kelime ve oyunlarda yalnız doğru düğme/kart etkilenir; hareket azaltmada sabit görünüm kullanılır.


### Oyunlarda aile paylaşımı — 2026-10-20

Dinle ve Seç/Tekrar, Eşleştir ve Hafıza sonuçları mevcut FamilyShareButton yetişkin doğrulaması, kart önizlemesi ve PNG seçicisini kullanır. Kartta yalnız oyun başlığı ve tamamlanan soru/çift sayısı yer alır. Dinleme kulağı, hafıza 2×2 ızgarası, eşleştirme yapbozu ve günlük yıldız çemberi çizimleri eklendi. Günlük paylaşım yalnız rewarded durumunda görünür. GameShareInfo tamamlanmamış oturumda kart üretmez.


### Ebeveyn raporu paylaşımı — 2026-10-20

Rapor sonunda iOS ile aynı Öğrenme özeti / Learning summary başlığı, öğrenme dilinin yerel adı ve çalışılan kelime sayısı ile resimli paylaşım bulunur. Sütun grafik simgesi eklendi. Yalnız ebeveyn raporu parentArea yetkisini kullanır; çocuk ekranlarında aritmetik doğrulama varsayılanı korunur. Arka plana geçiş yerel doğrulamayı temizler ve mevcut ebeveyn kilidi raporu kapatır.


### Ebeveyn ayarları — 2026-10-20

Profil düzenleme simgeli fotoğraf seçenekleri (galeri/kamera), özel JPEG avatar kaydı, günlük hedef menüsü (20–100, varsayılan 60) ve kalpli uygulama paylaşım kartı eklendi. Hedef, iOS gibi ödül algoritmasından ayrı bir tercihtir. Ses/animasyon U10 ve kurulum/hatırlatmalar U15 ile izlenir.


### Genel ses ve animasyon — 2026-10-20

Ebeveyn ayarlarında iOS ile aynı Ses ve Animasyon anahtarları, başlangıçta açık olarak kalıcı saklanır. Kelime/oyun/kutlama sesleri ve manuel/otomatik satranç sesleri genel tercihe bağlıdır. Manuel sessiz dinlemede uygun TR/EN uyarısı gösterilir; oyun heard callback almaz. Ses kapatma mevcut/pending oynatıcıları durdurur. KidMotionProvider, uygulama tercihini sistem hareket azaltması ile birleştirir; mevcut splash, kart, ses dalgası, satranç hareketi, kutlama ve grafikler ortak tercihi kullanır.


### Gizlilik — 2026-10-20

Tek kısa açıklama, iOS başlık kartı ve beyaz yuvarlak bölüm kartlarıyla değiştirildi: öğrenme verileri, profil fotoğrafı, dil paketleri, aile paylaşımı, tanılama/bildirimler ve verileri silme. Bölüm başlıkları 20sp, gövde 17sp/27sp satır aralığı ve seçilebilir metin kullanır. Metin Android’in mevcut depolama/paket/paylaşım davranışına göre TR/EN yazıldı. Firebase SDK olmayan sürüm etkin hizmet varmış gibi anlatılmaz; ertelenen konuşma ve henüz olmayan hatırlatma bölümleri eklenmez. Ebeveyn alt sayfalarında üst geri tuşu ilgili ayarlara döner.


### Satranç menüsü ve ayrı görev rotaları — 2026-10-20

Menüde ince şevron ve ölçeklenen ortak etiket yüksekliği. Mat/Pat ve Tek Hamlede Mat rotaları sabit; mod seçici kaldırıldı. Görev sayacı/ilerleme, cevap ve sonraki yerleşimi iOS sırasına alındı; mat yönerge/ses kartı ve üst sıfırlama eklendi. Yasal hedefler görünür. 96 test, Debug/Release/lint ve telefon/tablet/büyük yazı geçiş kontrolleri geçti. Tam eşlenmiş piksel incelemesi henüz tamamlanmadı.


### Tahta hazırlama ve özel konum — 2026-10-20

Yeni ChessSetupScreen, iOS yönerge/sayaç/renk/hedef/geri bildirim düzenini kullanır. Ortak palet sürükleme, özel konumda taş taşıma, geri alma, otomatik/rastgele seçili taraf ve iki taraf dizimi eklendi. Yatay tablette üç sütunlu palet ve iki panelli düzen kullanılır. Özel maçtan taslağa düzenleme ve devam geçişleri mevcut. 99 test ve Android 32 taş/özel konum/telefon-yatay tablet akışları geçti. Oyuncu/zorluk tercihi süreç kalıcılığı ve tam piksel onayı bekliyor.
