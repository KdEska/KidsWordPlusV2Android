# iOS → Android ekran karşılaştırması

20 Ekim 2026. Referans: yerel KidsWordPlusV2/App kaynakları ve açık iOS iPhone 17 / iPad simülatörleri. Android: Kotlin/Compose, telefon ve tablet.

**Bu bir tam görsel eşitlik onayı değildir.** Tüm aşa ğıda ki e 
 
  ekran/dur           u7        r kaynak akışı açısından karşılaştırıldı. Her satırın iki platformda aynı veri/dil ve benzer ekran oranıyla görsel doğrulaması ayrıca tutulur. Önceki uyarlamalar otomatik olarak eşit kabul edilmedi. iOS dosyaları değiştirilmedi. Konuşma pratiğinin ertelenmesi geçerli.

## Düzeltme sırası

| Kimlik | Fark | Durum | Yapılacak / bulunan |
|---|---|---|---|
| U01 | Karakterli splash | düzeltildi; telefon/tablet doğrulandı | İlk karede logo/bahçe/karakter hazır, soğuk açılışta bir kez, 2750ms hareket, atla ve azaltılmış hareket. |
| U12 | Satranç menü ve ekran metinleri | menü/görev/dizim/özel konum uygulandı; piksel kontrolü açık | Aynı görsele farklı adlar veriliyordu: Taşlar, Satranç oyna, Özel konum oluştur vb. Referans metinleri kullanıldı. |
| U06 | Ders tamamlandı | uyarlandı; Android doğrulandı | Kutlama, çalışılan kelime görselleri, sonraki ders, tekrar ve aile paylaşımını eşle. |
| U04 | Tüm sayfalarda ortak yardım | uygulandı; Android doğrulandı | Öğren, oyunlar, paketler ve dil yardımlarını bağla; sunum ve simgeyi ortaklaştır. |
| U05 | Kelime ve oyun ses durumu | uygulandı; Android kontrol edildi | Ses oynatılırken dalga/ışıldama ve çalma durumu geri bildirimini eşle. |
| U07 | Oyun sonuçlarında aile paylaşımı | uygulandı; Android doğrulandı | Metin paylaşımını mevcut doğrulama + resimli kart akışına geçir, sembol/örnekleri eşle. |
| U08 | Ebeveyn raporu paylaşımı | uygulandı; Android doğrulandı | Ebeveyn yetkisiyle önizleme ve dil/kelime sayılı PNG kart. |
| U09 | Ebeveyn ayarlarının içeriği | fotoğraf/hedef/paylaşım uygulandı | Ses ve animasyon U10 ile tamamlandı; kurulum/hatırlatma U15 açık. |
| U10 | Genel hareket/ses tercihleri | uygulandı; Android doğrulandı | Kalıcı Ses/Animasyon anahtarları; cihaz hareket azaltma önceliği korunur. |
| U11 | Gizlilik içeriği | uygulandı; Android doğrulandı | Altı TR/EN bölüm, iOS kartları ve Android davranışına uygun metinler. |
| U03 | Kurulum, diller ve paket görünümü | açık | Altı adım var; alanlar, zemin, paket adımı ve sistem dili farklı. Çok dil desteği yalnız görsel değişiklikle sağlanamaz. |
| U13 | Satranç maç içeriği | açık | Yakalanan taşlar, istatistik, son hamle gösterimi, eylem sırası/renkleri ve özel oyuna düzenle dönüşü. |
| U14 | Satranç oyun ayarları | açık | Ayrı ayar sheet, hareket efekti, oyun içi süre değişimi ve bekletme davranışı. |
| U15 | İsteğe bağlı ebeveyn kurulumu/hatırlatma | açık, özellik kapsamı | iOS davet, hızlı kurulum ve hatırlatma ekranları var. Konuşma pratiği önceki kararla ertelenmiş durumda; bu turda sessizce etkinleştirilmedi. |
| U02 | Ortak görünüm ölçüleri ve koyu tema | açık | Android açık temaya sabit. Bu turda bahçe sistem çubuklarının arkasına taşındı; telefon/tablet ilk kontrolü geçti. Buton/başlık/kart/alt menü genişlik ve güvenli alan ölçüleri aynı cihaz oranlarında ölçülecek. |

## Tüm ekranlar ve durumlar

68 ekran/durum kaydı. “Mevcut” yalnız karşılık bulunduğu anlamına gelir; görsel eşitlik anlamına gelmez.

| Kimlik | Bölüm / ekran | Farklar | Kaynak karşılaştırması |
|---|---|---|---|
| S01 | Açılış / Sistem açılışı | U02 | Android sistem açılış simgesi işletim sistemine ait; uygulama splash ekranından ayrı incelenecek. |
| S02 | Açılış / Karakterli splash | U01 | Bu turda eklendi: logo, seçili karakter, selamlama, yıldız, Başlayalım, otomatik geçiş. |
| S03 | Kurulum / Arayüz dili | U03 | iOS sistem dili ve çok dilli seçim; Android TR/EN, seçili kart rengi/işareti ve zemin farklı. |
| S04 | Kurulum / Öğrenme dili | U03 | iOS çok dilli seçim ve indirme boyutu; Android TR/EN. |
| S05 | Kurulum / Paket hazırlama | U03 | iOS adım içinde hazırla/ilerleme/iptal; Android ayrı paket ekranına yönlendiriyor. |
| S06 | Kurulum / Ad veya takma ad | U03 | iOS özel kalemli ProfileNameField; Android standart OutlinedTextField. |
| S07 | Kurulum / Karakter seçimi | U03 | iOS ortak CompanionPicker; Android kurulumda farklı basit kart ve iki sütun. |
| S08 | Kurulum / Kurulum özeti | U03 | iOS etiket/değer satırları; Android düz metin. Alt eylem zemini ve içerik genişliği de farklı. |
| S09 | Kurulum / Ebeveyn kurulum daveti | U15 | Yeni kurulum sonrası isteğe bağlı davet ve hızlı kurulum Android’de yok. Konuşma pratiği önceki kararla kapsam dışı. |
| S10 | Ana Sayfa / Ana sayfa | U02 | Logo, balon, karakter, ders kartı, altı kategori mevcut; ölçüler aynı veri/dil ve ekran oranında tekrar karşılaştırılmalı. |
| S11 | Öğren / Kategori listesi | U04 | Kategori yardım düğmesi yok; kartlar ve renk ailesi mevcut. |
| S12 | Öğren / Alt kategori listesi | U04 | Yardım düğmesi yok; görselli yatay kartlar mevcut. |
| S13 | Öğren / Ders listesi | U04 | Yardım düğmesi yok; ders kartları tekrar görsel kontrol bekliyor. |
| S14 | Öğren / Kelime kartı / önceki / sonraki | U04, U05 | Sayfa yardımı ve iOS dinleme dalgası/hareketli ses düğmesi eksik. |
| S15 | Öğren / Ders tamamlandı | U06 | iOS kutlama, kelime küçük görselleri, aile paylaşımı, sonraki ders ve tekrar; Android kurdele, kısa başlık ve dön düğmesi. |
| S16 | Öğren / Paket bulunamadı | Görsel kontrol | Paket açma eylemi var; boş durum ölçüleri görsel kontrol bekliyor. |
| S17 | Oyunlar / Oyun seçimi | U04 | Yardım yok. Konuşma kartı bilerek kapsam dışı; diğer beş kart mevcut. |
| S18 | Oyunlar / Dinle ve Seç / soru | U04, U05 | Soru/yanlış/doğru ve alt eylemler var; yardım ve ses çalma göstergesi eksik. |
| S19 | Oyunlar / Dinle ve Seç / sonuç | U07 | iOS ebeveyn doğrulamalı resimli paylaşım; Android GameShare metin paylaşımı. |
| S20 | Oyunlar / Tekrar / boş | U04 | Boş durum ve öğrenmeye yönlendirme var; yardım yok. |
| S21 | Oyunlar / Tekrar / soru | U04, U05 | Soru akışı mevcut; yardım ve ses çalma göstergesi eksik. |
| S22 | Oyunlar / Tekrar / sonuç | U07 | Oyun sonuçlarının ortak paylaşım farkı. |
| S23 | Oyunlar / Eşleştir / seviye | U04 | Kelime sayısı ve erişilebilir/kapalı seçenekler var; yardım yok. |
| S24 | Oyunlar / Eşleştir / oyun | U04, U05 | Bağlantı çizgileri ve geri bildirim mevcut; ses hareketi ve yardım farkı. |
| S25 | Oyunlar / Eşleştir / sonuç | U07 | Ortak paylaşım farkı; düğme sırası/tonları yeniden görsel kontrol edilecek. |
| S26 | Oyunlar / Hafıza / seviye | U04 | Dört seviye ve kart sayıları var; yardım yok. |
| S27 | Oyunlar / Hafıza / oyun | U04 | Kart açma/eşleşme/tur akışı mevcut; yardım yok. |
| S28 | Oyunlar / Hafıza / sonuç | U07 | Ortak paylaşım farkı. |
| S29 | Oyunlar / Bugünün görevleri | U04 | Ayrı sayfa, ödül, üç hedef ve seri var; yardım ve başlıktaki yıldız rozeti kontrol edilecek. |
| S30 | Oyunlar / Skor tablosu | U04 | Ayrı sayfa, 180dp görsel, yıldız sayacı mevcut; yardım yok. |
| S31 | Profil / Profil / ad düzenleme | Görsel kontrol | Karakter, ad alanı ve otomatik kayıt mevcut. Boş/dolu/klavye/büyük yazı görsel onayı bekliyor. |
| S32 | Profil / Karakter dünyası / açık kartlar | Görsel kontrol | 21 karakter, başlangıç/yeni arkadaş bölümleri ve ilerleme mevcut. |
| S33 | Profil / Kilitli kart / Nova | Görsel kontrol | Kilitli/siluet/seçili durumlar mevcut; koşulların ekran metni ve görünümü yeniden karşılaştırılacak. |
| S34 | Ebeveyn / Ebeveyn kapısı / basılı tut | Görsel kontrol | Üç saniye basılı tutma mevcut; arka planda tekrar kilitleniyor. |
| S35 | Ebeveyn / Ebeveyn kapısı / alternatif | Görsel kontrol | Alternatif doğrulama var; hata/büyük yazı durumları görsel kontrol bekliyor. |
| S36 | Ebeveyn / Haftalık panel | Görsel kontrol | Dört metrik, grafik ve alt sekmeler mevcut; aynı veriyle görsel onay gerekli. |
| S37 | Ebeveyn / Öğrenme raporu | U08 | Kategori ve metrikler mevcut; iOS rapor aile paylaşımı Android’de yok. |
| S38 | Ebeveyn / Ebeveyn profili | Görsel kontrol | Çocuk profiliyle aynı düzenleyici kullanılıyor. |
| S39 | Ebeveyn / Ayarlar | U09, U10, U15 | Profil fotoğrafı değiştirme, günlük hedef, ses/animasyon, hatırlatma/kurulum ve uygulama paylaşımı gibi iOS bölümleri eksik. |
| S40 | Ebeveyn / Dil tercihleri | U03, U04 | iOS anında kaydeden seçiciler ve sistem dili; Android TR/EN radyo + Kaydet. |
| S41 | Ebeveyn / Dil paketleri / liste | U03, U04 | iOS bölüm/list görünümü; Android kartlar. Android yalnız TR/EN listeliyor, yardım yok. |
| S42 | Ebeveyn / Dil paketleri / kurulum-iptal | Görsel kontrol | İlerleme/iptal mevcut; hata/çevrimdışı/yenileme görsel kontrol bekliyor. |
| S43 | Ebeveyn / Dil paketi / silme onayı | Görsel kontrol | İlerlemenin korunacağı onay mevcut; Android yerel diyalog görünümü farklı. |
| S44 | Ebeveyn / Gizlilik | U11 | iOS ayrıntılı sayfaya karşı Android kısa tek kart açıklama. |
| S45 | Satranç / Satranç menüsü | U12 | Bu turda başlıklar/karşılama/dil rozeti iOS TR/EN metinlerine eşlendi. Kart kırpması/ölçüleri görsel kontrol bekliyor. |
| S46 | Satranç / Genel Bilgiler / tarih | Görsel kontrol | Tarih kartları, özgün görseller ve dinleme mevcut. |
| S47 | Satranç / Genel Bilgiler / temel kurallar | Görsel kontrol | Temel kurallar sekmesi mevcut. |
| S48 | Satranç / Genel Bilgiler / ustalar ve kaynaklar | Görsel kontrol | İki dönem, on fotoğraf, lisans/kaynak bağlantıları mevcut. |
| S49 | Satranç / Taşları Tanı / liste | U12 | Başlık eşlendi; 2D/3D kart düzeni mevcut. |
| S50 | Satranç / Taş ayrıntısı | Görsel kontrol | Ad/kural sesi, örnek ve deneme geçişi mevcut. |
| S51 | Satranç / Tahtayı Hazırla | U12 | Başlık eşlendi; beyaz/siyah yerleştirme ve paylaşım mevcut. |
| S52 | Satranç / Alıştırma / serbest | Görsel kontrol | Altı taş, yasal hedef, sürükleme ve sıfırlama mevcut. |
| S53 | Satranç / Alıştırma / görev ve tur sonu | Görsel kontrol | Dengeli on görev ve aile paylaşımı mevcut. |
| S54 | Satranç / Açılış listesi / ilkeler | Görsel kontrol | Dört açılır ilke ve sekiz açılış önizlemesi mevcut. |
| S55 | Satranç / Açılış dersi / izle | Görsel kontrol | Hamle önizlemesi, ileri/geri/sıfırla ve ses mevcut. |
| S56 | Satranç / Açılış dersi / dene | Görsel kontrol | Beklenen hamle, ipucu ve ilerleme mevcut. |
| S57 | Satranç / Şahını Koru | Görsel kontrol | On görev, tehdit/kaçış/alma/engel ve paylaşım mevcut. |
| S58 | Satranç / Mat ve Pat / soru | U12 | Başlık eşlendi; üç cevap ve dengeli tur mevcut. iOS ayrı sayfa, Android ayrıca mod seçici gösteriyor. |
| S59 | Satranç / Mat ve Pat / kaçış | Görsel kontrol | Doğru Şah sonrası liste, kareden hamle, animasyon ve ilk konuma dönüş mevcut. |
| S60 | Satranç / Tek Hamlede Mat / soru-sonuç | U12 | Başlık eşlendi; Android ortak ekranın fazladan mod seçicisi var. |
| S61 | Satranç / Bilgisayara Karşı / oyun | U13 | Yakalanan taş şeritleri, oyun içi istatistik kartı ve iOS eylem tonları/sırası eksik/farklı. |
| S62 | Satranç / Bilgisayara Karşı / ayarlar | U14 | iOS sağ üst ayrı ayar sayfasında ses, hareket efekti ve aktif saat. Android ses satırı tahtada, süre yeni oyunda; ayrı ayarlar yok. |
| S63 | Satranç / Yeni oyun / onay | Görsel kontrol | Taraf/zorluk/süre ve mevcut oyunu değiştirme onayı var; iOS sheet ile Android sayfa sunumu farklı. |
| S64 | Satranç / Oyun / hamle geçmişi | Görsel kontrol | Taşlı beyaz/siyah sütun ve açılır panel mevcut; önceki turda telefon/tablet incelendi. |
| S65 | Satranç / Oyun / sonuç ve paylaşım | Görsel kontrol | Sonuç/paylaşım mevcut; iOS istatistik/taş şeritleri ile birlikte kontrol edilecek. |
| S66 | Satranç / Kendi Oyununu Kur | U12, U13 | Başlık eşlendi; dizilim araçları mevcut. Oyundan düzenlemeye dönüş düğmesi iOS ile karşılaştırılmalı. |
| S67 | Satranç / Satranç yardım pencereleri | U04 | 14 yardım mevcut; iOS büyük sheet, Android ortalı Dialog. Yardım simgesi iOS daireli, Android düz soru işareti. |
| S68 | Satranç / Aile paylaşımı / doğrulama-önizleme | U07, U08 | Satranç paylaşımı var; diğer bölümlerde ortak kullanım eksik. Kart sembolü/örnek görseller birebir değil. |

## Doğrulama yöntemi

Her fark için telefon + tablet, TR + EN, normal + büyük yazı, boş/yüklü/yanlış/doğru/tamamlandı durumları ilgisine göre kontrol edilecek. Aynı içerik ve ilerleme kullanılacak; sayısal farklar tasarım hatası sayılmayacak. Android sistem açılışı, sistem paylaşım seçicisi, klavye ve yerel izin diyalogları ayrı platform bileşenleri olarak kaydedilecek; uygulama içi ekranlar iOS düzenini izleyecek.

Bu turda canlı iPad gezinmesi: ana sayfa, kategoriler, oyun menüsü, profil, satranç menüsü. iPhone iOS splash görüntüsü alındı. Android splash telefon/tablet otomatik geçiş, Başlayalım, azaltılmış hareket ve arka plandan dönüş kontrol edildi; ilk karede görsellerin geç gelmesi farkı bulunup düzeltildi, son sürüm telefon/tablette doğrulandı. Diğer tüm satırlar eşlenmiş ekran görüntüsü aşamasını bekliyor.

Görseller: ../reviews/full-ui-audit/. Önceki ekran doğrulamaları IOS-UI.md ve CHESS-IOS-UI.md içinde tarihlidir.

## İlk düzeltme turu sonucu

- U01: Karakterli splash eklendi. Logo/bahçe/seçili karakter ilk kareden
  önce hazırlanır. Android 12+ sistem splash simgesi uygulama ekranının
  üzerine taşınmadan kaldırılır. Aynı oturumda arka plandan dönünce
  karşılama yeniden gösterilmez. Durum AppModel içinde tutularak ekran
  yeniden oluşturmasında korunur.
- U02 kısmi: Bahçe bütün ekranlarda sistem çubuklarının arkasına taşındı;
  içerik güvenli alanda. Koyu tema ve ortak ölçü karşılaştırmaları açık.
- U12 kısmi: On satranç menü başlığı, karşılama ve dil rozeti TR/EN
  iOS metinleriyle eşlendi. Kart ölçüleri ve iki bulmaca ekranındaki
  fazladan mod seçici gibi yapısal farklar açık.

Son Debug/imzasız Release/lint başarılı. Önceki 89 birim test, bu görsel
ve açılış değişikliğinde tekrar çalıştırılmadı. Emülatörde telefon/tablet
soğuk açılışı, otomatik geçiş, arka plandan dönüş, Başlayalım, azaltılmış
hareket, on menü etiketi ve Taşları Tanı başlığı kontrol edildi. Profil
birebir korunmuş, cihazın animasyon tercihi ve telefon boyutu geri
getirilmiştir. Kalıcı ilerleme veya iOS kaynakları değiştirilmedi.

Bir sonraki fark: U06 — ders tamamlama ekranı. Kalan kayıtlar bu dosyada
açık tutulur; bu ilk tur tüm ekranların görsel onayının tamamlandığı
anlamına gelmez.

Son ek kontrol: iPad splash yatay yönde kaydedildi; Android’de de
1180×820dp yatay tablet görünümünde açılış ve otomatik geçiş kontrol
edildi. Ham iPad PNG'si cihazın fiziksel yönünde döndürülmüş görünür.
Bu iki görüntü piksel eşitliği onayı sayılmadı. Telefon görünümüne dönüldü.

## İkinci düzeltme turu — U06 ders tamamlama

Ders sonu artık iOS WordLessonView / LessonCelebration düzenini izler:
seçili karakter, toplam yıldız rozeti, yalnız gerçek artış varsa +yıldız,
konfeti/karakter hareketi ve kazanılan yıldızın sayaca uçuşu, Harika çalışma,
çalışılan kelime sayısı, yatay küçük görseller, aile paylaşımı, aynı konudaki
sonraki ders, turuncu tekrar ve mavi geri eylemi. Son ders sonraki eylemi
göstermez. Tekrar sıfırıncı kelimeyi açar ve kazanılmış ilerlemeyi silmez.
Kutlama bir görünüm oturumunda bir kez başlar; arka planda ses/hareket durur.
Hareket azaltma etkinse dekoratif hareketler atlanır.

Aile paylaşımı mevcut ebeveyn doğrulamasını kullanır; ders simgesi ve ilk
üç kelime görseliyle PNG üretir. Başlık/dil/kelime sayısı ve etkinlik görselleri
dışında profil verisi alınmaz. Mevcut satranç kartlarının varsayılan çağrısı
korundu. Özgün 12 TR/EN kutlama sesi 133.699 bayt olarak aktarıldı;
lesson-celebration-assets.json dosya hashlerini tutar. Ses paylaşım/gezinme
ve arka plana geçişte durdurulur.

91 test geçti: yeni testler konular arası atlamayı, son/olmayan dersleri,
katalog sırasını ve gerçek yıldız farkını kapsar. Debug, imzasız Release ve
lint geçti; son simge hizalamasında derlemeler/lint tekrar çalıştırıldı.
Emülatörde 10 kelimelik ders tamamlandı, tekrar baştan tamamlandı, aynı
konudaki dört kelimelik son derse geçildi. Telefon/tablet sonuç görünümü,
son derste sonraki eylemin yokluğu, ebeveyn kapısı, görselli önizleme ve PNG
paylaşım seçicisi kontrol edildi. Alıcı seçilmedi. Son ikonlarla kısa ders ve
Derslere dön eylemi ayrıca kontrol edildi. Her testten sonra profil dosyası
özgün yedekle byte düzeyinde karşılaştırılarak geri yüklendi.

Görseller: ../reviews/lesson-completion/. phone-complete-final.png ve
tablet-last-lesson-final.png son ikon düzenini; exported-lesson-card.png
üretilen kartı gösterir. iOS ile tam piksel onayı hâlâ ayrı değerlendirmedir.
Bu turda gerçek günlük +20 yıldız kazanma koşulu emülatörde oluşturulmadı;
artış hesabı birim testinde, olumlu uçuş dalı kod üzerinden kontrol edildi.
Genel ses/animasyon ebeveyn tercihleri U10 kapsamında açık kalır.

Sıradaki fark grubu: U04 — Öğren, Oyunlar, dil ve paket sayfalarının ortak
sayfa yardımları ve yardım penceresi sunumu.


### U04 — ortak sayfa yardımı (2026-10-20)

Yukarıdaki ilk denetim bulgularının U04 yardım eksikleri giderildi. 27 Türkçe/İngilizce konu ortak daireli simge ve büyük alt panelle açılıyor. Öğren, tüm oyun türleri, günlük görevler, skor, dil ve paket sayfalarındaki 13 yeni konu cihazda açıldı; mevcut satranç menü/maç yardımı kontrol edildi. Ses ve kart örnekleri, göster/başa al, kapatma düğmesi, sistem geri, Anladım, açık pencereyle telefon/tablet geçişi ve yardım tercihi kontrol edildi. Özgün profil birebir geri yüklendi. 94 birim testi geçti. Tam iOS piksel karşılaştırması ve fiziksel cihaz kabulü bekliyor.

Sıradaki fark grubu: U05 — kelime ve oyunlarda ses çalma göstergeleri.


### U05 — kelime ve oyun ses göstergeleri (2026-10-20)

Gerçek ses oynatıcısı durumuna bağlı dalga ve camgöbeği/beyaz vurgu eklendi. Kelime, Dinle ve Seç/Tekrar düğmeleri, eşleştirme sesleri ve hafıza kartları ortak efekti kullanır. Eşleştirmede seçim ile çalma ayrı tutulur; hafızada aynı kelimenin diğer kopyası aydınlanmaz. Bitiş, hata, durdurma ve mevcut arka plan/yardım/gezinme durdurmaları göstergeyi temizler. Hareket azaltmada sabit vurgu kullanılır, boşta animasyon çalışmaz. Telefon kelime/dinleme/eşleştirme ve tablet hafıza görselleri kontrol edildi. Debug, imzasız Release ve lint başarılı. Önceki 94 birim testi bu görsel entegrasyon için tekrar çalıştırılmadı. Tam eşlenmiş iOS piksel karşılaştırması bekliyor.

Sıradaki fark grubu: U07 — oyun sonuçlarında doğrulamalı resimli aile paylaşımı.


### U07 — oyun sonuçlarının resimli paylaşımı (2026-10-20)

Doğrudan metin paylaşımı kaldırıldı. Dört oyun türü ortak yetişkin doğrulaması, kart önizlemesi ve PNG seçicisine bağlandı. iOS ile aynı soru/çift sayıları ve oyuna özgü kulak/ızgara/yapboz simgeleri kullanılır. Günlük ödül kazanılınca yıldız çemberli kart paylaşımı görünür. Gerçek iki çiftlik hafıza oyunu tamamlandı; doğrulama, kart önizlemesi ve sistem PNG seçicisi alıcı seçilmeden kontrol edildi. Telefon boyutuna geçiş tekrar doğrulama gerektirdi. Özgün profil birebir geri yüklendi. 96 test, Debug/Release ve lint geçti. Diğer oyun sonuçlarının her biri ve günlük ödül kartı cihazda ayrı ayrı tamamlanmadı; tam iOS piksel onayı bekliyor.

Sıradaki fark grubu: U08 — ebeveyn raporu paylaşımı.


### U08 — ebeveyn raporu paylaşımı (2026-10-20)

Rapor sonuna Paylaş eklendi. Ebeveyn giriş doğrulaması kullanılarak doğrudan kart önizlemesi açılır. iOS ile aynı Öğrenme özeti başlığı, öğrenme dilinin yerel adı ve güncel çalışılan kelime sayısı; sütun grafik simgesi kullanılır. Tablet PNG seçicisi alıcı seçilmeden açıldı; arka plana geçiş önizlemeyi kapattı. Telefon kaydırma ve önizleme kontrolü geçti. Özgün profil birebir geri yüklendi. Debug/Release ve lint geçti; önceki 96 test bu UI entegrasyonunda tekrar çalıştırılmadı. İngilizce cihaz kabulü ve tam iOS piksel eşleştirmesi bekliyor.

Sıradaki fark grubu: U09 — ebeveyn ayarlarının içeriği.


### U09 — fotoğraf, günlük hedef ve uygulama paylaşımı (2026-10-20)

Ayarlar üst kartına iOS profil düzenleme görseliyle fotoğraf değiştirme eklendi. Android Photo Picker ve dış kamera açılabilir; seçilen fotoğraf merkezden kırpılıp EXIF içermeyen 512×512 özel JPEG olarak atomik kaydedilir. Giriş 20MB ile sınırlı ve büyük görseller örneklenerek çözülür. Kamera avatar önizleme çözünürlüğünü kullanır. Hedef seçenekleri 20/40/60/80/100, varsayılan 60; iOS gibi günlük görev ödülünden ayrı tercihtir. Uygulamayı paylaş, ebeveyn yetkisiyle kalpli kart önizlemesini açar.

80 kelime hedefi seçimi ve yeniden açılınca korunması, galeri iptali, uygulama paylaşım önizlemesi, gerçek test görselinin kaydı ve yeniden açılınca görünmesi, kamera açma/iptal kontrolleri geçti. Özgün profil/fotoğraf geri yüklendi, test görseli temizlendi. Debug/Release/lint geçti. Önceki 96 test UI entegrasyonunda tekrar çalıştırılmadı. Kamera deklanşörü, İngilizce cihaz görünümü ve tam iOS piksel onayı bekliyor.

Sıradaki fark grubu: U10 — ses ve animasyon tercihleri. Hatırlatma/kurulum U15 olarak açık.


### U10 — genel ses ve animasyon tercihleri (2026-10-20)

Ses/Animasyon anahtarları ebeveyn alanına eklendi; varsayılan açık ve kalıcıdır. Kelime, oyun, kutlama, manuel/otomatik satranç sesleri genel tercihe bağlıdır. Ses kapalıyken manuel dinlemede TR/EN açıklama gösterilir; dinleme oyunu soruyu dinlenmiş saymaz. Kapatma mevcut/bekleyen sesleri durdurur. Uygulama animasyon tercihi, sistem hareket azaltmayla birlikte ortak hareket bağlamında değerlendirilir.

Kapalı tercihlerin süreç yeniden açıldığında korunması, kelime ve oyunda sessiz mod, satranç taş ekranında görünür ses kapalı uyarısı, tekrar açınca dinleme oyununun heard durumuna geçmesi kontrol edildi. Yardımın ses örneği kareleri uygulama animasyonu kapalıyken aynı, açıkken farklı; sistem hareket azaltma açıkken tekrar aynı bulundu. Özgün profil ve sistem animasyon ayarı geri yüklendi. Debug/Release/lint geçti. Önceki 96 test UI tercihi için tekrar çalıştırılmadı. Fiziksel cihaz, kutlamanın sessiz modda elle tamamlanması ve tam iOS piksel kabulü bekliyor.

Sıradaki fark grubu: U11 — gizlilik ekranı.


### U11 — gizlilik ekranı (2026-10-20)

Kısa tek kart yerine iOS başlık ve bölüm kartları eklendi. Altı TR/EN bölüm: öğrenme verileri, fotoğraf, dil paketleri, aile paylaşımı, tanılama/bildirimler, verileri silme. 24dp yuvarlama, 20sp bölüm başlığı, 17sp gövde ve 27sp satır yüksekliği; erişilebilir başlıklar ve seçilebilir metin. Metin özel fotoğraf depolaması, paket/ilerleme ayrımı, paylaşım önbelleği ve Firebase SDK bulunmayan Android sürümüyle doğrulandı. Olmayan konuşma/hatırlatma özellikleri anlatılmadı.

Türkçe altı bölüm telefon/tablette, İngilizce altı bölüm telefonda kontrol edildi. Üst ve sistem geri tuşları Gizlilikten Ayarlara döndü; İngilizce dil alt ekranında da üst geri çalıştı. Özgün profil birebir geri yüklendi. Debug/Release/lint geçti; önceki 96 test metin/UI değişikliğinde tekrar çalıştırılmadı. TR görüntüleri son yazı ölçüsü değişikliğinden öncedir; EN görüntüleri son APK’dandır. Seçim menüsü, fiziksel cihaz ve tam iOS piksel karşılaştırması bekliyor.

Sıradaki fark grubu: U12 — satranç ekranlarının kalan yerleşim ve mod seçimi farkları.


### U12 — menü ve iki ayrı görev ekranı (2026-10-20)

Menü görsellerinin 1.15 kırpma oranı ve uyarlanabilir sütunları iOS kaynağıyla karşılaştırıldı. Renkli büyük ok yerine ince mor şevron, font ölçeği ve ölçülen metne göre tüm kartlarda aynı başlık yüksekliği kullanıldı. Büyük yazıda tek sütun kontrol edildi.

Mat ve Pat ile Tek Hamlede Mat artık kendi rotalarıdır; fazladan mod seçici kaldırıldı. Görev/tamamlanma aynı satıra, ilerleme çizgisi altına taşındı. Mat/Pat cevapları iOS sırasıyla Mat, Pat, Şah; doğru cevaptan sonra açıklama, Dinle ve Sonraki görev gösterilir. Tek Hamlede Mat yönerge/Dinle kartı, doğru cevap ve sonraki düğmesi tahta üzerinde; üstte aynı konumun seçimini temizleyen sıfırlama eklendi. Seçilen taşın yasal hedefleri gösterilir. Erken tur/ipuçları yerine iOS soru ve tamamlanma akışı kullanılır. Kayıt hatasında yeniden deneme korunur.

96 test geçti. Telefon/tablet menüsü, büyük yazı, yanlış/doğru cevap, sonraki görev, mat sıfırlama ve gerçek mat çözümü, iki ekranda 2/10 durumunun boyut değişiminde korunması ve yardım dönüşü doğrulandı. Özgün profil ve yazı boyutu geri yüklendi. Debug/Release/lint başarılı. İngilizce cihaz görünümü, bu turda 10/10 tamamlanma ve tam iOS piksel kabulü bekliyor.

Sırada U12 içindeki tahta hazırlama/özel konum ve kalan görsel kontroller var; U13/U14 maç ayrıntıları ayrıca açık.


### U12 — tahta hazırlama ve özel konum; U13 düzenlemeye dönüş (2026-10-20)

Tahta hazırlama kendi ekranına ayrıldı. iOS renk/sayaç satırı, yönerge ve yuvarlak Dinle, taş paleti, sürekli kesikli turuncu hedef, geri bildirim/vezir notu, üst sıfırlama ve bitiş paylaşımı eklendi. Paletten tahtaya uzun basıp sürükleme ortak koordinatlarla çalışır. Mevcut 32 adımlık yerleştirme/persist kuralları kullanılır.

Özel konum araç kartına silgi, 50 adımlık oturum içi geri alma, seçili tarafı otomatik/rastgele ve iki tarafı otomatik dizme eklendi. Karşı tarafın işgal ettiği başlangıç karesinde otomatik dizim reddedilir. Rastgele dizim karşı tarafı korur, 1–16 taş içinde bir şah seçer, sınır sırasına piyon koymaz ve şahları bitişik yerleştirmez; mümkünse oynanabilir konum seçer. Yeni taslak varsayılanı standarttır; kayıtlı taslak değiştirilmez. Portrede araçlar/tahta/seçenekler; yatay tablette sol araçlar ve sağ tahta/başlat sütunları bulunur. Tahta üzerindeki taşlar sürüklenebilir. Özel maçtan Konumu düzenle ve taslaktan Oyuna devam et geçişleri bağlandı. Başlat düğmesi bilgisayarlı oyun akışını kullanır; mevcut maçın değiştirilmesi onaylanır.

99 test geçti (üç yeni düzenleme kuralı testi). Emülatörde yanlış hedef, paletten kale sürükleme, 32 taşın tamamı, 16/32 renk geçişi ve bitiş kontrol edildi. Özel konumda temizle/otomatik/rastgele/geri al, yeni maç/düzenlemeye dön, yatay palette ve tahtada sürükleme/geri alma, telefona geçiş ve süreç yeniden açılınca taslağın korunması geçti. Özgün profil, taslak ve maç birebir geri yüklendi. Debug/Release/lint başarılı. İngilizce cihaz görünümü, oyuncu/zorluk tercihinin süreç kalıcılığı ve tam iOS piksel onayı açık; tercih kalıcılığı U14 ile ele alınacak.

Sırada U13 maç içeriği: yakalanan taşlar, istatistik, son hamle ve eylem düzeni.


### U13 — son hamle ve alınan taşlar (2026-10-20)

Maç tahtasının altına iOS gibi son hamlenin başlangıç → hedef gösterimi ve iki satırlı alınan taşlar kartı eklendi. Bilgisayarlı oyunda oyuncunun rengine göre, iki kişilik oyunda Beyaz/Siyah başlıklarıyla ayrılır. Yatay taş listesi mevcut 2D/3D görünümünü kullanır; erişilebilir renk/taş adları korunur. Taşlar başlangıç FEN'inden geçerli hamleler yeniden yürütülerek bulunur; eksik başlangıç materyali alınmış sayılmaz, geçerken alma ve terfi etmiş taşın alınması desteklenir. İstatistik, eylem düzeni ve maç ayarları henüz açık.

Bu eklemede 101 birim testi geçti; Debug derlendi ve emülatöre veriler korunarak yüklendi. Yeni kartın telefon/tablet görsel karşılaştırması bekliyor.


### U13 — maç istatistik kartı ve alt eylem sırası (2026-10-20)

İstatistikler iOS gibi beyaz kartta oynanan oyun sayısı ve üç sütun Kazandın/Kaybettin/Beraberlik olarak gösterilir; maçta hamle geçmişinden önce de yer alır. Yalnız bilgisayarlı maçlar kişisel sonuç sayılır. Yeni sonuç kayıtları oyuncu rengini saklar. Eski renk bilgisi olmayan sonuçlar korunur ve açıklanır; onlara kazanma/kaybetme tahmini atanmaz. Yeni oyun yeşil eylem oldu; özel konumdaki düzenle eylemi bu düğmenin altına taşındı. Geri al/İpucu görünümü ve üst maç ayarları hâlâ açık.

103 test, Debug derleme ve lint geçti. Telefon maç alt bölümünde istatistik/eylem kartları kontrol edildi; özgün profil birebir geri yüklendi. Görsel kontrol sonrası düzenle düğmesinin kamera simgesi kalemle değiştirildi. Tablet ve tam iOS eşleştirmesi açık.


### U13/U14 — maç düğmeleri ve ayarlar (2026-10-20)

Geri al/İpucu mavi çocuk eylem düğmelerine dönüştürüldü; geri dönüş oku ve ampul çizimleri eklendi. Geri al animasyon sırasında da kilitlenir. Üst araç çubuğuna maç ayarları eklendi; alttan açılan beyaz panel seslendirme, hamle animasyonu ve tahtayı çevir seçeneklerini içerir. Hamle animasyonu tercihi ayrı yerel kayıtta süreçler arasında korunur, ebeveyn/cihaz hareket kısıtlarını aşmaz. Ayarlar açıkken tahta görünürlük kapısı saat ve bilgisayar hamlesini durdurur. Maç içinde saat aç/kapat/süre değiştirme, özel oyun taraf/zorluk kalıcılığı ve tam iOS/tablet karşılaştırması henüz açık.

103 test ve son Debug/lint geçti. Telefon ve tablette ayarları aç/iki kez çevir/kapat kontrolü geçti; özgün profil birebir geri yüklendi. Son alttan panel stili telefonda tekrar doğrulandı. Son panel stilinin tablette görsel kontrolü ve animasyon tercihinin süreç yeniden açılışında manuel kontrolü açık.


### U14 — maç saati ve özel oyun tercihleri (2026-10-20)

Maç ayarlarına saat aç/kapat ve taraf başına süre seçimi eklendi. Saat kapatılırken kalan süre korunur; aynı süreyle tekrar açıldığında devam eder. Süre değiştirmek iki tarafın saatini sıfırlar, oyun kimliği/tahta/hamle geçmişi korunur. Biten oyun değiştirilemez. Model önce geçen süreyi kesinleştirir, sonra ayarı kaydeder; kapalı saat ilerlemez. Kapalı saati saklamak için kayıt sürümü 4 eklendi; 1–3 sürümleri okunmaya devam eder. Geri alma saat durumunu korur. Özel konum oyuncu rengi ve zorluk seçimi ayrı yerel tercihlerde saklanır; hatalı zorluk değeri Orta'ya döner.

105 birim testi, Debug derleme ve lint geçti. Yeni testler saat kapalıyken ilerlememe, kaydedip geri okuma, yeniden açma, süre değişikliği, biten oyun ve geri almayı kapsar.

Telefon emülatöründe saati açma, 10 dakika seçme, kapatıp yeniden açma kontrol edildi; iki tarafın kalan süresi, oyun kimliği ve hamle geçmişi korundu. Özgün profil birebir geri yüklendi. Özel konum tercihlerinin süreç yeniden açılışındaki manuel kontrolü ve tablet saat paneli kontrolü açık.


### U14 — tablet saat paneli doğrulaması (2026-10-21)

1600×2560 / 240 dpi tablette son alttan açılan ayar paneli kontrol edildi. Saati açma, 10 dakika seçme, kapatıp tekrar açma geçti. Saatler aynı değerlerle devam etti; oyun kimliği ve hamleler değişmedi. Son panel ekran görüntüsü `reviews/match-clock/tablet.png`. Deneme sonunda profil dosyası bayt düzeyinde doğrulanarak geri yüklendi ve cihaz 1080×1920 / 420 dpi telefon boyutuna döndürüldü.

Özel oyun ekranında Siyah ve Zor seçildi; süreç tamamen kapatılıp yeniden açıldı. Hem kalıcı kayıtta hem erişilebilir seçim durumunda iki seçenek geri geldi. Son görüntü `reviews/custom-preferences/phone.png`. Özgün profil ve özel oyun tercihleri geri yüklendi. Bu tur uygulama kodu değişmedi; önceki 105 test sonucu geçerli. Sıradaki alan U03 kurulum/diller/paketler; tam iOS görsel karşılaştırması ayrıca açık.


### U03 — ilk kurulum zemin/dil/paket adımı (2026-10-21)

Kurulum zemini iOS gibi açık camgöbeği/beyaz/yeşil geçişe alındı. Dil kartları beyaz 22 dp köşe, 24 dp iç boşluk ve seçiliyken 3 dp mor çerçeveyle düzenlendi; seçim erişilebilir duruma eklendi. Başlık/geri alanları ortalandı; işlem sırasında geri ve dil değişimi kapatıldı. Adım değişiminde kaydırma konumu sıfırlanır. İçerik tablette 800 dp ile sınırlanır; büyük yazıda üst karakter 80 dp olur. Başlık ve adım sayacı erişilebilir biçimde işaretlendi.

Paket adımı ayrı paket ekranına yönlendirme yerine aynı ekranda hazırlama, mevcut kurulum ilerlemesi/iptal, hazır durum ve yeniden dene gösterir. Mevcut doğrulanmış model.install ve katalog işlemleri kullanılır. Hazır olmayan paketle devam engeli korunur. Desteklenmeyen diller veya cihaz dilini izleme seçeneği eklenmedi; çok dil/sistem dili, karakter kataloğunun tamamı ve ebeveyn dil/paket görünümü U03 içinde hâlâ açık.

Debug/lint geçti. Altı Türkçe kurulum adımı, karakter adımından sonra kaydırma sıfırlanması, tablette boyut değişimi ve tüm adımlardan geri dönüş, İngilizce arayüz/paket hazır görünümü emülatörde geçti. Özgün profil bayt düzeyinde karşılaştırılarak geri yüklendi. Görüntüler `reviews/setup-parity/`. Yeni indirme/iptal yolu bu tur çalıştırılmadı; mevcut kurulum işlemlerine bağlandı.


### U03 — ortak karakter seçimi ve ebeveyn dil satırları (2026-10-21)

Profildeki karakter dünyası ortak `CompanionPicker` bileşenine çıkarıldı ve kurulumdaki altı kart yerine kullanıldı. 21 arkadaş sayacı, tüm dillerden kelime sayısı, altı başlangıç arkadaşı, 14 kilit açılabilir arkadaş ve gizli Nova; mevcut görsel/kilit/seçim erişilebilirliği ve büyük yazıda tek sütun kuralıyla paylaşılır. Kilit açma ve seçimi kaydetme kuralları değiştirilmedi.

Ebeveyn dil ekranı beyaz kartta Arayüz/Öğrendiğim dil seçim satırları ve Dil paketleri geçişiyle düzenlendi. TR/EN seçimi mevcut korumalı profil kaydı üzerinden anında kaydedilir; ayrı Kaydet adımı kaldırıldı. Üst geri davranışı korunur. Paket kütüphanesinin kart ölçüleri/güncelleme ayrıntıları, sistem dili ve ek yerelleştirmeler açık.

İlk ebeveyn kontrolünde paketlerden dönüşte dil alt sayfasının kaybolduğu bulundu. Ebeveyn alanı, paket sayfasına geçerken kaydedilebilir alt ekran durumunu koruyacak şekilde sarıldı; ebeveyn alanından çıkışta durum temizlenir. Böylece paketlerden geri, dil tercihlerine döner. Düzeltme sonrası aynı akış yeniden kontrol ediliyor.

Kurulum ortak karakter dünyası başlangıç ve gizli Nova kartına kadar kontrol edildi; altı adım/TR ve EN-tablet regresyonu geçti. Ebeveyn TR/EN anında dil kaydı, paketlere geçiş ve dil ekranına dönüş, telefon/tablet görünümü geçti. Özgün profil birebir geri yüklendi. Debug/lint başarılı; mevcut kilit kuralları değişmediği için önceki 105 birim testi bu tur tekrarlanmadı.


### U03 — paket boyutları ve güncelleme durumu (2026-10-21)

Paket boyutları iOS gibi etiket/değer satırlarında gösterilir; büyük yazıda alt alta dizilir. Cihazda boyutu katalogdaki olası yeni sürümden değil, kurulu ve hash'i doğrulanan manifestin payload boyutu + manifest dosyası boyutundan alınır. Kurulu sürüm/manifest bilgisi salt okunur olarak yüklenir; başka dilin bozuk işaretçisi geçerli kayıtları etkilemez. Güncel manifestle eşleşen pakette gereksiz doğrula/güncelle düğmesi gizlenir; farklı sürüm Güncelle, kurulu olmayan Paketi kur gösterir. İşlem sırasında indirme düğmesi yerine mevcut ilerleme/iptal kullanılır. Temel paket bağımlılığı kurulu temel manifestle eşleşmiyorsa toplam indirme satırı gösterilir. Temel paketin kurulu durumu gerçek temel kayıtla belirlenir.

Katalog dışı kurulu dil yönetimi korunur. Paket silme/indirme mekanizması değiştirilmedi. Tam iOS görsel eşleştirmesi ve sistem dili/ek diller açık.

106 birim testi geçti; yeni paket bilgisi testi doğrulanmış boyut/sürüm, bozuk diğer dil işaretçisi ve değiştirilmiş manifesti kapsar. Debug/lint başarılı. TR/EN tablette ve EN telefonda boyut satırları, güncel Türkçe pakette güncelleme olmaması ve kurulu olmayan İngilizce pakette kurulum düğmesi kontrol edildi. Özgün profil birebir geri yüklendi. Bu tur gerçek indirme/silme veya uzak katalog yenileme yapılmadı.


### U03 — 13 öğrenme dili ve 11 arayüz dili (2026-10-21)

Öğrenme dili kısıtları tek 13 dil kataloğunda toplandı; kurulum, ebeveyn seçimi, profil, öğrenme özeti/paylaşımı ve paket kütüphanesine bağlandı. Yerel hazırlama/cihaza aktarma betikleri 13 dilin arşivini doğrular. Gerçek paketlerde bütün kelime medya yolları ve ders kapsamı test edildi. Arayüz için iOS'un 11 dil sözlüğü Android varlıklarına kopyalandı; çift dilli yardımcılar/sabit metin çiftleri, kategori ve ebeveyn sekmeleri sözlüğe bağlandı. Tercih uygulama yeniden açılınca yüklenir. Eksik/dinamik metinler İngilizceye döner; tam ekran yerelleştirme onayı değildir. Satranç seslendirmesi TR/EN ile sınırlı, diğer arayüz dillerinde EN geri dönüşlüdür.

108 test ve Debug/lint geçti. Emülatörde Fransızca paket kuruldu/seçildi, Almanca arayüz ve kategori başlıkları kontrol edildi; iki tercih süreç yeniden açılışında korundu. Özgün profil birebir geri yüklendi; Fransızca paket kullanılabilir bırakıldı. Görseller `reviews/multilingual/`. Her dilde tek tek görsel onay, cihaz dili otomatik izleme ve Release içerik yayını açık.


### U03 — dinamik çeviri şablonları (2026-10-21)

İsimli ana sayfa/açılış karşılama, açılış arkadaşı, ebeveyn öğrenme yolculuğu ve haftalık yeni kelime metinleri iOS sözlüğündeki yer tutuculu anahtarlara bağlandı. Tek geçişli yerleştirme kullanıcı adındaki `{1}`/dolar karakterlerini yeniden işlemez. Kurulum özeti arayüz için her zaman English göstermek yerine gerçek yerel dil adını gösterir. 109 test ve Debug/lint geçti. Almanca/Japonca/Çince isimli karşılama ve Almanca altı adım/özet emülatörde doğrulandı; özgün profil birebir geri yüklendi. Görseller `reviews/dynamic-localization/`. Diğer eksik/dinamik Android metinleri ve her dilde tüm ekranların görsel onayı açık.


### U03 — satranç sözlükleri ve işaretli etiketler (2026-10-21)

iOS Chess/interface.json ve Chess/content.json çevirileri ortak sözlüğe eklendi. Menü/maç ayarı/geri al/ipucu vb. metinleri mevcut kaynaktan gelir. Android'e özel sıra, şah, kaydedildi, çevir, son hamleyi dinle ve saat sıfırlama metinleri dokuz ek arayüz diline çevrildi. Tam anahtar yoksa ✓ ve son iki nokta korunarak temel etiket çevrilir. Satranç dil rozeti sabit English yerine gerçek dil adını gösterir.

110 test ve Debug/lint geçti. Almanca/Japonca satranç sıra durumu ve ayar paneli emülatörde doğrulandı; özgün profil/maç birebir geri yüklendi. Görseller `reviews/chess-localization/`. Bazı Android'e özel satranç metinleri ve her dilde tüm ekranların görsel onayı açık; seslendirme TR/EN kalır.

Japonca ekran görüntüsünde kalan hareket ayarları açıklaması da dokuz ek dilde tamamlandı; sözlük kapsamı doğrulandı. Bu son veri değişikliği sonrası ekran görüntüsü yenilenmedi.


### U03 — oyun ve ebeveyn ortak mesajları (2026-10-21)

Tekrar Zamanı başlığı iOS anahtarına eşlendi; hafıza sonucu `{0} pairs in {1} attempts` şablonuyla çevrilir. Ses/kamera hataları, önce sesi dinle, ebeveyn kilidi/veri/dil ve satranç kurtarma etiketleri dokuz ek arayüz diline eklendi. Paket kaldırma/hazırlama mesajları mevcut iOS metinlerine bağlandı. ViewModel kullanıcı mesajları seçilen arayüz sözlüğünü kullanır.

111 test ve Debug/lint geçti. İspanyolca/Korece oyun başlığı ve ebeveyn etiketleri emülatörde doğrulandı; özgün profil birebir geri yüklendi. Ortak hata metinleri ve hafıza sonucu şablonu sözlük/birim testleriyle doğrulandı, cihazda hata zorlanmadı veya hafıza tamamlanmadı. Görseller `reviews/common-localization/`. Kalan İngilizce geri dönüşler ve tüm ekran/dil görsel onayı açık.


### U03 — paket açıklamaları ve tekrarlanabilir çeviri taraması (2026-10-21)

Kurulum temel paket açıklaması mevcut iOS metnine eşlendi; rapor için paket gerektiği ve önceki ilerlemenin korunacağı mesaj dokuz ek arayüz diline çevrildi. `scripts/audit_localization.py` dosya/satır bazlı literal/dinamik aday raporu üretir. İlk rapor 798 literal çiftte 324 geri dönüş adayı ve 21 dinamik çift buldu; kullanılmayan/debug kodu da içerir ve tam ekran analizi değildir. Rapor `docs/localization-audit.json`.

112 test ve Debug/lint geçti. Almanca/Fransızca kurulum açıklaması/hazır durumu emülatörde doğrulandı; özgün profil birebir geri yüklendi. Rapor mesajının kapsamı sözlük testiyle doğrulandı. Görseller `reviews/setup-message-localization/`. Kalan adayların ekran erişilebilirliği ve çeviri önceliği ayrıca değerlendirilecek.


### U03 — taş adları, tahta yönergesi ve ses dili ayrımı (2026-10-21)

Tahta hazırlama yönergesi `{piece}` / `{square}` adlı yer tutucularla çevrilir. Ortak taş adı işlevi seçilen arayüz sözlüğünü kullanır; ilk ekran kontrolüyle bulunan İngilizce taş adı eksikliği giderildi. Beyaz/Siyah taş satırı sembolden ayrı çevrilir. Özel konum materyal/komşu şah/sırası olmayan şah/bitmiş konum uyarıları mevcut iOS anahtarlarına eşlendi; istatistik kazanma/kaybetme başlıkları bağlandı.

Arayüzde çevrilmiş taş adı İngilizce geri dönüş sesini çözemiyordu. Tahta ve taş hedef alıştırmaları artık sabit taş türü kodu gönderir; ses kataloğu o kodu kendi TR/EN adına dönüştürür. 115 test ve Debug/lint geçti; tüm 32 tahta yönergesinin TR/EN kaydı çözümlenir. DE/JA yönergesi ve Dinle hata kontrolü geçti; özgün profil/kurulum ilerlemesi birebir geri yüklendi. Sesin gerçekten duyulduğu otomatik ölçülmedi. Görseller `reviews/setup-instruction-localization/`.
