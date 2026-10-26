# Satranç — ilk çalışan Android dilimi

Ana Sayfa → Satranç dünyası üzerinden açılır. İki kişinin aynı cihazda
oynaması veya Beyaz/Siyah ve Kolay/Orta/Zor seçerek bilgisayara karşı
oynaması desteklenir. Bu dilim tam iOS satranç kapsamının ilk parçasıdır.

## Eklenenler

- Koordinatlı tahta, seçili taş, yasal hedefler ve son hamle vurgusu.
- Mevcut proje görsellerinden alınan 12 Beyaz/Siyah taş görseli; tahta çevirme.
- Şahı açıkta bırakmayan hamleler, şah uyarısı, iki yönde rok, geçerken alma,
  vezir/kale/fil/at terfisi; şah alma hamlesi yoktur.
- Mat, pat, üç konum tekrarı, 100 yarım hamlelik sayaç ve K–K / K+fil–K /
  K+at–K yetersiz materyal durumları. iOS uygulaması gibi tekrar ve 50 hamle
  beraberliği otomatik uygulanır; ayrı talep düğmesi yoktur.
- Başlangıç rakibi ve aynı değerlendirmeyi kullanan ipucu. Güçlü satranç motoru
  veya ölçülmüş güç seviyesi iddiası yoktur. Hesaplama arka planda yapılır.
- İki kişilik modda tek hamle geri alma; bilgisayar modunda oyuncunun son
  hamlesine kadar tur geri alma. Bilgisayar Beyaz açtıysa ilk oyuncu hamlesinden
  önce geri alma sunulmaz.
- Her hamleden sonra kalıcı kayıt; yeniden açılışta sıranın ve özel hamle
  haklarının hamle geçmişinden kurulması. Bilgisayarın sırasıysa tahta açılınca
  cevap verir. Yeni oyun devam eden oyunun yerini alacaksa uygulama içi onay vardır.
- Oyun kimliğiyle bir kez sayılan tamamlanma istatistiği; Beyaz/Siyah galibiyeti
  ve beraberlikler. Sonuçtan geri almak önceki istatistiği ikinci kez yazmaz.
- Bozuk kayıt otomatik üzerine yazılmaz. Ebeveyn alanında ham kayıtları
  yedekleyip yeni oyun/istatistik kaydı başlatma işlemi vardır.

## Testler

ChessTests: başlangıç için 20 / 400 / 8902 hamle ağacı düğümü, rok ağırlıklı
konumda 48 / 2039, bağlı taş konumunda 14 / 191 / 2812; özel hamleler, iki renk
ve dört terfi, mat/pat, tekrar, sayaç, rok hakkının geri gelmemesi, şah alma ve
bağlı taş engeli, bilgisayar hamlesi/mat bulma, kayıt/geri alma ve bozuk kayıt.

Standart konumların referansı: [Stockfish perft testleri](https://github.com/official-stockfish/Stockfish/blob/master/tests/perft.sh).
Bu projeye Stockfish motoru veya kodu eklenmedi; konumlar doğrulama referansıdır.
Diğer davranış referansları iOS `ChessMatch`, `ChessPracticeGame` ve ilgili
`Chess*Checks.swift` dosyalarıdır.

## Henüz eklenmeyenler

Genişletilmiş mat/oyun sonu havuzları,
ayrıntılı geçmiş ekranı sonraki dilimlerdir. Hazır TR/EN anlatımı eklendi.
İlk sürümde hamle geçmişi koordinat biçimindedir (e2e4); SAN/PGN dışa aktarma yoktur.
Kayıt biçimi Android'e özel sürümlü JSON'dur; iOS oyun kaydını içe aktarma yoktur.
Satranç oyunları kelime oyunlarının günlük yıldız/karakter oyun sayısına eklenmez.

## Emülatör kontrolü — 19 Ekim 2026

API 34 ARM64 emülatöründe temel/TR paket kurulumu, iki kişilik oyun,
uygulamayı zorla kapatıp yeniden açtıktan sonra kayıtlı konuma dönüş ve
`f2f3 e7e5 g2g4 d8h4` mat sonucu arayüzden doğrulandı. Tamamlanan oyun
istatistiğinde tek kayıt bulundu. Aynı emülatör 1080×1920 / 420 dpi telefon
ve 1600×2560 / 240 dpi tablet ölçülerinde kontrol edildi; tablet ölçüsünde
sol gezinme şeridi ve oyun durumu korundu.

Görseller: [Telefon](../reviews/chess-smoke/phone.png),
[tablet ölçüsü](../reviews/chess-smoke/tablet.png).
Bu kontrol fiziksel telefon/tablet kabulünü kapsamaz. Bilgisayar rakibi,
terfi/özel hamle arayüzleri ve TalkBack için cihaz kontrolü bekliyor;
ilgili oyun kuralları birim testleriyle doğrulanmıştır.

## Taş eğitimleri — ilk bölüm

Satranç menüsündeki **Taşları öğren ve alıştırma yap** bölümünde altı taşın
Türkçe/İngilizce hareket açıklaması ve iOS temel hedefleriyle eşleşen dokuz
alıştırma bulunur: altı hareket, kale ile alma, piyonla çapraz alma, atla engel
üzerinden atlama. Beyaz taş seçilip istenen hedefe götürülür. Yanlış hedefler
ilerleme yazmaz; yardım hedefi ve mümkün hareketleri gösterir.

Tamamlanan hedefler `chess.lessons.v1` içinde tekrar sayılmadan saklanır.
Bu ilk bölümde dil bağımsızdır, mevcut maç kaydını ve kelime ödüllerini etkilemez.
Ekran boyutu değişince seçili ders ve çözüm korunur; uygulama yeniden açılınca
tamamlanan hedefler korunur, yarım alıştırma yeniden başlar. Kayıt hatasında
tekrar kaydet düğmesi sunulur. Hareket dersi sıra değiştirmez; rok ve geçerken
alma bu alıştırmalarda yer almaz.

Üç yeni birim testiyle toplam 37 test geçti. API34 emülatöründe şah hedefi
arayüzden çözüldü, kalıcı kayıt ve tablet ölçüsüne geçiş doğrulandı.
Görseller: [telefon](../reviews/chess-lessons/phone.png),
[tablet ölçüsü](../reviews/chess-lessons/tablet.png).
Rastgele hedef turları ve dokuz hedefin tamamının cihaz kabulü
sonraki çalışmalardır.

## Tahta dizimi ve şah güvenliği — ilk bölüm

Satranç menüsündeki **Tahtayı diz** bölümünde 32 taş, iOS sırasıyla Beyaz arka
sıra, Beyaz piyonlar, Siyah arka sıra ve Siyah piyonlar olarak yerleştirilir.
Çocuk istenen taş türünü seçip doğru kareye dokunur. Yanlış tür/kare ilerlemez;
hedef yardımı bulunur. Her yerleştirme `chess.setup.v1` ile kaydedilir. Baştan
başlama yalnız dizimi temizler ve uygulama içinde onay gerektirir.

**Şahını koru** bölümünde iOS temel konumlarına karşılık gelen dört sabit hedef
vardır: tehdit eden kaleyi bulma, şahı güvenli kareye kaçırma, saldıran kaleyi
fille alma ve kaleyle araya girme. İstenen hamle yasal olmalı ve şah tehdidini
kaldırmalıdır. Başka yasal çözüm bu hedefi tamamlamaz; istenen kaynak ve hedef
metinde belirtilir. Tehdit bulmada yalnız saldıran taş seçilir. Açıklamalar ve
hedef yardımı Türkçe/İngilizcedir.

Güvenlik hedefleri `chess.safety.v1` içinde bir kez sayılır. Bu eğitimlerin
ilerlemesi diller arasında ortaktır; maç, kelime ve karakter ödül kayıtlarından
bağımsızdır. Dizim ara adımı uygulama yeniden açılınca sürer. Güvenlikte biten
hedefler saklanır; yarım hedef yeniden başlar. Rastgele dönüştürülmüş konumlar,
mat/pat tanıma ve tek hamlede mat henüz bu bölümde yoktur.

Dört yeni test 32 taşın gerçek başlangıç tahtasıyla eşleşmesini, yanlış
yerleştirme ve geçersiz sayacın reddini, dört güvenlik konumunda şah tehdidini,
çözümlerin yasallığını ve tekrar ilerleme yazılmamasını doğrular.

API34 emülatöründe dört şah güvenliği hedefi tablet ölçüsünde arayüzden
çözüldü ve kayıtları doğrulandı. Dizimde ilk taş yerleştirildi; uygulama zorla
kapatılıp telefon ölçüsünde yeniden açılınca 1/32 konumundan sürdü ve ikinci taş
yerleştirildi. Tam 32 adımlık dizim arayüz kontrolü, fiziksel cihazlar ve TalkBack
kabulü bekliyor. Toplam **41 test**, Debug/Release derlemeleri başarılı;
lint: 0 hata, 22 uyarı ve 2 öneri.

Görseller: [şah güvenliği / tablet ölçüsü](../reviews/chess-academy/safety-tablet.png),
[tahta dizimi / telefon](../reviews/chess-academy/setup-phone.png).

## Şah / mat / pat ve tek hamlede mat — ilk bölüm

Satranç menüsü → **Mat, pat ve bulmacalar** bölümünde üç tanıma konumu ve üç
mat bulmacası bulunur. Konumlar iOS çekirdek eğitim örnekleridir; bu ilk bölüm
sabit altı hedef içerir, iOS rastgele genişletilmiş havuzlarının tamamı değildir.

Tanıma bölümünde sıra Beyazdadır. Çocuk **Şah**, **Mat** veya **Pat** cevabını
seçer; doğru cevapta nedenini görür. Şah tehdidi ve yasal hamle olup olmaması
mevcut satranç kurallarından hesaplanır. Yanlış cevap tekrar denenebilir.

Mat bölümünde sıra Beyazdadır. İki vezir ve bir kale konumunda bir hamlede
Siyahı mat etmek gerekir. Her yasal mat hamlesi kabul edilir; yalnız referans
hamle zorunlu değildir. Yasal olmayan hamle, yalnız şah çeken hamle ve pat
ilerleme kazandırmaz. Yanlış hamlede başlangıç tahtası korunur. İpucu örnek bir
çözümü ve hedefini gösterir; terfi bu üç konumun kapsamında değildir.

Bitirilen altı hedef `chess.puzzles.v1` ile bir kez kaydedilir; kayıt hatasında
tekrar kaydetme sunulur. İlerleme diller arasında ortaktır ve maç/kelime
istatistiklerini etkilemez. Ekran yeniden boyutlandığında seçili konum ve çözüm
korunur; süreç yeniden açıldığında tamamlanan hedefler saklanır. Metinler
Türkçe/İngilizcedir. Mat hedef yönergesi anlatımı ve genişletilmiş turlar bekliyor.

Dört yeni birim testi; üç sonuç türünü, her mat konumunun referans çözümünü,
bütün yasal hamleler arasındaki mat kabulünü, şah/pat hamlesinin reddini ve
tekil ilerleme yazımını kapsar. Toplam 45 test ve Debug/Release derlemeleri
başarılıdır; lint: 0 hata, 23 uyarı ve 5 öneri.

API34 ARM64 emülatöründe tablet ölçüsünde altı hedefin tamamı çözüldü ve
kaydedildi. Yanlış tanıma cevabı tekrar denendi; mat bulmacasında pat oluşturan
hamle reddedildi. Telefon ölçüsüne geçişte ekran korundu; uygulama zorla
kapatılıp açılınca 6/6 tamamlanma geri geldi. Fiziksel cihaz, TalkBack ve
İngilizce arayüz kabulü bekliyor.

Görseller: [tanıma / tablet ölçüsü](../reviews/chess-puzzles/ending-tablet.png),
[mat çözümü / tablet ölçüsü](../reviews/chess-puzzles/mate-tablet.png),
[yeniden açılış / telefon](../reviews/chess-puzzles/restored-phone.png).

## Açılış dersleri

Satranç menüsü → **Açılışları öğren** sekiz iOS örnek dizisini içerir:
İtalyan, İspanyol, İskoç, Vezir Gambiti, Londra, Sicilya, Fransız ve Caro–Kann.
Toplam 51 yarım hamle vardır. Bunlar kısa öğretici örneklerdir; rakibin gerçek
oyunda aynı diziyi izlemesi beklenmez. Çocuk derste iki tarafı da oynar.

Her adımda sıra, kaynak/hedef ve hamlenin fikri Türkçe/İngilizce gösterilir.
Merkez, gelişim, baskı, destek, değişim ve alan kazanma açıklamaları bulunur.
Koordinatlı tahta ve hedef yardımı kullanılır. Notasyonda N/B/x açıklanır.
Yasal ama örnek diziden farklı hamle ile yasal olmayan hamle ayrı geri bildirim
alır; ikisi de adımı ilerletmez. Rakip hamlesi otomatik oynatılmaz.

Her dersin mevcut adımı ve tamamlanma işareti `chess.openings.v1` sürümlü
kayıtta saklanır. Önceki hamle veya baştan çalışma, kazanılmış tamamlanma
işaretini kaldırmaz. Kayıt okunamıyorsa üzerine yazılmaz ve ders hamleleri
engellenir; kayıt kurtarma arayüzü bu bölümde henüz yoktur. Maç ve diğer eğitim
kayıtları etkilenmez. İlerleme diller arasında ortaktır. Dersin tamamını otomatik okuma,
serbest varyant analizi, otomatik rakip ve açılıştan yeni maç başlatma yoktur.

Dört yeni test sekiz dizinin 51 hamlesinin yasallığını, doğru sırayı, güvenli
şahları, adım konumlarının yeniden kurulmasını, yanlış/eski hamle reddini,
derslerin ayrı ilerlemesini, geri alma/baştan çalışmayı, kalıcı kayıt dönüşümünü
ve bozuk sürüm/adım sınırı reddini kapsar.

Toplam 49 test ve Debug/Release derlemeleri başarılıdır. Lint: 0 hata,
24 uyarı ve 6 öneri. Sekiz açılışın toplam 51 yarım hamlesi otomatik testlerde
yasal sıra ve konum yeniden kurulmasıyla doğrulandı.

API34 ARM64 emülatöründe tablet ölçüsünde İtalyan dizisinin altı hamlesi
arayüzden oynandı; ders dışı yasal `d2d4` reddedildi. Ders tamamlandı ve bir
hamle geri dönüşten sonra 5/6 adımı ile tamamlanma işareti birlikte kaydedildi.
Telefon ölçüsünde zorla kapatıp yeniden açınca 5/6 adımı ve 1/8 tamamlanma
korundu. Fiziksel cihaz, TalkBack, İngilizce arayüz ve diğer yedi dizinin arayüz
kabulü bekliyor; tüm diziler otomatik kurallar testlerinden geçmiştir.

Görseller: [tamamlanan ders / tablet ölçüsü](../reviews/chess-openings/completed-tablet.png),
[yeniden açılış / telefon](../reviews/chess-openings/restored-phone.png).

## Özel konum editörü — ilk bölüm

Satranç menüsü → **Özel konum oluştur** ile Beyaz/Siyah renk, altı taş veya
silgi seçilip kareye dokunulur. Dolu karedeki taş değişir; aynı renkte şah
başka yere konursa önceki şah kaldırılır. Standart dizim ve temizleme onayla
uygulanır. Renk/taş paleti, hamle sırası ve isteğe bağlı rok hakkı vardır.
Taslak `chess.custom.v1` ile her değişiklikte saklanır; eksik şah gibi henüz
oynanamayan taslaklar da korunur. Bozuk taslak üzerine yazılmaz.

Oyuna başlamak için her renkte tam bir şah, taraf başına en fazla 16 taş ve
8 piyon gerekir. Şahlar bitişik olamaz; ilk/son sırada piyon bulunamaz;
sırası gelmeyen şah tehdit altında olamaz. Mat/pat/yetersiz materyalle bitmiş
konum başlatılmaz. Bu, konumun tüm tarihsel hamlelerinin mümkün olduğunu
kanıtlamaz. Geçerken alma hakkı yoktur; başlangıç sırasındaki piyon çift adım
atabilir. Rok açılırsa yalnız kendi başlangıç karesindeki şah/kale çiftlerine
hak verilir; hamle sırasında geçiş/şah tehdidi kuralları yine uygulanır.

İki kişi veya başlangıç bilgisayarıyla oyun başlatılır. Mevcut oyun varsa
değiştirme onayı alınır, editör taslağı korunur. Standart oyun kaydı sürüm 1
olarak okunmaya devam eder; özel oyun sürüm 2 başlangıç FEN'i ve hamleleriyle
kaydedilir. Geri alma ve yeniden açılış bu başlangıçtan tekrar kurar. Siyahın
başladığı bilgisayarlı oyunda da ilk bilgisayar hamlesi tek başına geri alınmaz.

Bu bölümde sürükleyerek taşıma, tek tarafı otomatik dizme/rastgeleleştirme,
FEN içe/dışa aktarma ve taslak kurtarma arayüzü yoktur.

Dört yeni test; geçersiz konumları, şahın tek kalacak şekilde taşınmasını,
eksik taslak kaydını, rok hakkını, Siyah başlangıcı, özel kökene geri almayı,
sürüm 1/2 kayıt dönüşümünü ve özel konumdan matın bir kez sayılmasını kapsar.
Toplam 53 test ve Debug/Release derlemeleri başarılı; lint: 0 hata, 25 uyarı,
6 öneri.

Siyah başlangıçta koordinat geçmişi `1... e7e5` ile başlar; Beyazın sonraki
hamlesi `2.` olur. Özel konumun başlangıcı da bu numaralandırmada kullanılır.

API34 ARM64 emülatöründe tablet ölçüsünde üç taşlı konum dokunarak kuruldu.
Mevcut oyun yerine başlatma onaylandı; `f7g7` matı, başlangıca geri alma ve
tekrar mat arayüzden denendi. Oyun kimliği istatistikte yalnız bir kez bulundu.
Telefon ölçüsünde zorla kapatıp açılınca özel oyunun mat konumu geri geldi.
Fiziksel cihaz/TalkBack, bilgisayarlı mod, Siyah başlangıcı, rok ve palet
sıfırlama arayüz kabulü bekliyor; kurallar ve kayıtlar birim testleriyle kapsandı.

Görseller: [editör / tablet ölçüsü](../reviews/chess-custom/editor-tablet.png),
[özel oyun / tablet ölçüsü](../reviews/chess-custom/match-tablet.png),
[yeniden açılış / telefon](../reviews/chess-custom/restored-phone.png).

## Süreli oyun — eğitim saati

Standart ve özel konum oyunlarında taraf başına süresiz / 1 / 5 / 10 / 15 / 30
dakika seçilebilir. Ek süre yoktur. Eski sürüm 1/2 kayıtları süresiz kalır;
saatli kayıt sürüm 3, iki tarafın kalan milisaniyesini ve başlangıç bütçesini
saklar. Özel başlangıç FEN'i varsa aynı kayıtta korunur.

Bu eğitim saati yalnız oyun tahtası açık ve uygulama öndeyken işler. Menü,
eğitim ekranı veya arka planda durur. Süre işletim sisteminin monotonik
sayacıyla ölçülür; ekrandaki saniye sayacına bağlı değildir. Süre hamle öncesi,
bilgisayar hesabı sonrası ve yaklaşık saniyelik kayıt noktalarında işlenir.
Bilgisayar düşünürken onun süresi gider. Geri alma harcanan süreyi geri vermez;
yalnız taş konumunu geri alır. Süre dolunca hamle/geri alma engellenir, sonuç
oyun kimliğiyle bir kez sayılır. Bu ilk öğretici sürümde süreyi bitiren taraf
kaybeder; rakibin olası mat materyali için ayrı zaman aşımı beraberlik hesabı yoktur.

Yeniden açılışta kayıtlı kalan süre, tahta açılana kadar durur. İşletim sistemi
aniden süreci sonlandırırsa son kayıt noktasından sonraki yaklaşık bir saniye
veya devam eden disk işleminin süresi kaybolabilir. Saat turnuva saati iddiası
taşımaz; arka planda devam, Fischer ek süresi ve elle duraklat düğmesi yoktur.

Dört yeni test; bağımsız saatleri, hamleyle sıra değişimini, negatif süreyi yok
saymayı, süre dolunca hamle/geri alma engelini, tekil istatistiği, süre iadesiz
geri almayı, Siyah özel başlangıcı ve saatli/eski kayıt dönüşümünü kapsar.
Toplam 57 test başarılıdır.

Debug/Release derlemeleri ve lint tamamlandı: 0 hata, 25 uyarı ve 8 öneri.

API34 ARM64 emülatöründe tablet ölçüsünde bir dakikalık iki kişilik oyun
başlatıldı ve `e2e4` ile saat Siyah tarafa geçti. Arka planda dört saniyelik,
menüde üç saniyelik örneklemde kalan süre değişmedi. Dönüşte saat sürdü ve
Siyah süreyi doğal olarak bitirdi; Beyaz galibiyeti bir kez kaydedildi. Telefon
ölçüsünde zorla kapatıp açılınca süre dolma sonucu korundu. Fiziksel cihaz,
TalkBack/İngilizce, saatli bilgisayar/özel konum ve sıfırdan büyük kalan süreyle
süreç kapatıp açma arayüz kabulü bekliyor.

Görseller: [çalışan saat / tablet ölçüsü](../reviews/chess-clock/running-tablet.png),
[süre dolması / tablet ölçüsü](../reviews/chess-clock/timeout-tablet.png),
[yeniden açılış / telefon](../reviews/chess-clock/restored-phone.png).

## Bilgisayar zorluk seviyeleri

Standart ve özel konum yeni oyunlarında **Kolay / Orta / Zor** seçilir.
Aktif bilgisayarlı oyunda seçilen seviye gösterilir. Varsayılan **Orta**dır;
seçim mevcut oyunu değiştirmez, yeni oyuna uygulanır.

- Kolay: yasal hamlelerden rastgele seçim; özellikle hata yapması mümkündür.
- Orta: önceki rakibin taş alma, tehdit riski ve merkez puanlaması; tek hamlede
  matı önceliklendirir.
- Zor: aday hamleden sonra rakibin olası bir cevabını da değerlendirir; materyal
  ve merkez, mat/beraberlik sonuçları ile karşılaştırılır. Ek arama yaklaşık
  1 saniye / 2000 düğüm bütçesiyle sınırlıdır. Başlangıç yasal hamle ve Orta
  değerlendirmesinin maliyeti bu süre bütçesinin dışındadır; bu katı bir toplam
  duvar saati garantisi değildir. Tam değerlendirilen adaylar kullanılır;
  bütçe dolarsa eldeki yasal hamleye dönülür. Bilinen tek hamle mat önceliği korunur.

Model iOS `easy/normal/challenge` davranışlarını temel alır. Hesaplama arka
planda yapılır; iptal denetimleri arama noktalarında çalışır. Saatli oyunda
hesaplama rakibin süresinden düşer; hesaplama bitince önce süre yeniden
kontrol edilir. Ölçülmüş Elo veya her konumda daha güçlü oyun garantisi yoktur.
Stockfish ya da harici motor eklenmedi. İpucu mevcut Orta değerlendirmesini
kullanır; Kolay seçimi ipuçlarını rastgele yapmaz.

Oyun ve tamamlanma kayıtlarında ek `difficulty` alanı saklanır. Mevcut kayıt
sürümleri değişmez; eski kayıtta alan yoksa Orta kabul edilir, bilinmeyen değer
reddedilir. Hamle, süre kaydı, özel başlangıç, geri alma ve yeniden açılış
seviyeyi korur. Dört yeni test; rastgele yasal hamleleri, zehirli taş alma
senaryosunda rakip cevabını hesaba katmayı, mat önceliğini, sıfır arama bütçesi
ve iptali, kayıt/geri alma/özel konum/saat/istatistik uyumluluğunu kapsar.

Toplam 61 test ve Debug/Release derlemeleri başarılı; lint: 0 hata, 25 uyarı
ve 8 öneri. Zor seviyenin sınırlı araması, rakibin savunduğu fili kaleyle alma
örneğinde Orta seviyenin zehirli taş alma tercihinden kaçındı. Bu tek senaryo
bir Elo veya genel güç ölçümü değildir.

API34 ARM64 emülatöründe tablet ölçüsünde üç seviye ve açıklamaları seçildi.
Zor rakip Beyaz açtı; oyuncunun `e7e5` hamlesinden sonra cevap verdi ve üç
hamle kaydedildi. Telefon ölçüsünde zorla kapatıp açılınca **Rakip: Zor**
etiketi ve oyun korundu. Emülatör kullanıcı denemesi için açık bırakıldı.
Fiziksel cihaz/TalkBack/İngilizce, Kolay/Orta oyun akışı ve özel/saatli oyun
seviyelerinin cihaz kabulü bekliyor.

Görseller: [Zor oyun / tablet ölçüsü](../reviews/chess-difficulty/hard-tablet.png),
[yeniden açılış / telefon](../reviews/chess-difficulty/restored-phone.png).

## Çevrimdışı sesli anlatım — ilk bölüm

Mevcut TR/EN paketlerindeki 269'ar MP3, ses dizini, metin ve kredi bilgisi
Android ana varlıklarına değiştirilmeden kopyalandı. Toplam 538 kayıt APK ile
gelir; bu ilk bölüm için ayrıca paket veya cihaz konuşma motoru gerekmez.
Seçilen arayüz dili kullanılır. Dizin metinleri Unicode/büyük-küçük harf ve
noktalama normalizasyonuyla tam kayıtlara veya kayıt parçalarına çözümlenir.
Eksik kayıtta yarım cümle oynatılmaz; görünür hata ve tekrar deneme sunulur.
Metin ve mevcut ses kayıtları özgün kaynakların editoryal durumunu korur.

- Taş hareketi ve dokuz hedef: **Taşı dinle / Hedefi dinle**.
- Tahta dizimi ve dört şah güvenliği hedefi: **Dinle**.
- Şah/mat/pat tanıma yönergesi ve doğru cevap açıklaması: **Dinle**.
- Açılışların hamle fikirleri: **Dinle**.
- Oyunda **Hamleleri seslendir** kalıcı aç/kapat tercihi, **Son hamleyi dinle**
  ve **Sesi durdur**. Otomatik anlatım varsayılan açık; kayıtlı oyuna dönüşte
  eski hamleler yeniden otomatik okunmaz. Yeni hamle renk, taş, kaynak/hedef,
  taş alma ve terfiyle okunur; geçerken alma da taş alma olarak anlatılır.

Dinleme sırasında özgün kayıt metni ekranda görünür. Elle dinleme önceki sesi
keser. Otomatik hamleler tek oynatıcıda sıraya alınır; en fazla dört bekleyen
cümle tutulur, aşılırsa en eski bekleyen bırakılır. Ses odağı kaybı, arka plana
geçiş, satrançtan çıkış, ders/hedef değişimi ve geri alma sesi durdurur.
Kelime sesleriyle satranç sesi birlikte oynatılmaz. Biten veya hata veren
oynatıcı serbest bırakılır; ekran dönüşünde eski ses devam etmez.

Bu dilimde cihaz TTS yedeği, otomatik ders okuma, sonuç/saat/şah uyarısı,
özel konum editörü ve mat bulmacası yönergesi seslendirmesi yoktur. Rok hamlesi
şahın kaynak/hedef hareketi olarak okunur; kalenin eşzamanlı hareketi için ayrı
cümle yoktur. Türkçe ve İngilizce dışında ses dili henüz desteklenmez.

Dinleme oyun saatini durdurmaz. Oyunda ses metni ve dinleme kontrolleri tahtanın
altındadır; otomatik ses başlayınca tahta yer değiştirmez.

Dört yeni testle toplam 65 test geçti. Debug/Release derlemeleri başarılı;
lint: 0 hata, 26 uyarı, 9 öneri. Her iki APK'daki 538 MP3'ün SHA-256 değerleri
özgün paketlerle karşılaştırıldı. API34 emülatöründe Türkçe vezir açıklaması,
metin/durdurma, arka planda kesilme, otomatik hamle ve elle son hamle okuma
doğrulandı. Android oynatıcı **started**, konuşma türü ve **mutedState:none**
bildirdi; bu insan kulağıyla ses kalitesi onayı değildir. Telefon ölçüsünde
zorla kapatıp açınca sessiz tercih korundu; sonra otomatik anlatım yeniden
açılıp emülatör kullanıcı denemesi için açık bırakıldı.

İngilizce cihaz kontrolü, tüm derslerin tek tek cihaz kabulü, fiziksel cihaz,
TalkBack, kulaklık/ses odağı kesintisi ve insan dinleme kalite kontrolü bekliyor.
Görseller: [ders / tablet ölçüsü](../reviews/chess-audio/lesson-tablet.png),
[hamle / tablet ölçüsü](../reviews/chess-audio/move-tablet.png),
[tercih korunması / telefon](../reviews/chess-audio/restored-phone.png).
