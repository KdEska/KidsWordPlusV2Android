# Android satranç ilk giriş rehberi

## Güncel dil uyarlaması

22 rehber metni desteklenen 11 arayüz dilinin tamamında hazır: tr/en/de/fr/es/pt/it/zh/ja/ko/hi. Mevcut ar/ru satranç katalogları için de çeviriler hazırlandı; bu işlem uygulamaya yeni arayüz dili seçeneği eklemez. Önceki ilk sürümün İngilizceye dönüş notları artık geçerli değildir.

Android rehberi ve yeniden açma düğmeleri dili doğrudan state.ui üzerinden çözer. iOS model.ui dilindeki satranç arayüz kataloğunu kullanır. Ekran okuyucu piyon/hedef kare etiketleri de {square} şablonlarıyla dilin sözcük sırasına uyar. Öğrenme dili bağımsız kalır.

Doğrulama: iki platformun Debug derlemesi başarılı; Android 119 test geçti. check_localization.py 22×13 katalog girdisinde kapsam, platformlar arası eşleşme, yer tutucular ve eksik İngilizce dönüşü olmadığını doğrular. Android testi seçili görünüm dilinin genel dil durumundan bağımsız uygulandığını da kontrol eder. Yeni sesli anlatım kaydı eklenmedi.


## Güncel tekrar gösterme davranışı

Rehbere “Bir daha gösterme” seçeneği eklendi. Varsayılan kapalıdır; işaretlenmedikçe satranç menüsüne her girişte rehber açılır. Tamamlamak veya ertelemek tek başına otomatik gösterimi durdurmaz. Seçenek chess.guide.dontShowAgain.v1 anahtarında kalıcı saklanır; önceki seen/completed kayıtları otomatik açılışı engellemez. Elle tekrar açma korunur, seçenek kaldırıldığında otomatik gösterim yeniden başlar. Aşağıdaki ilk sürüm davranışına ilişkin notların yerine bu kural geçerlidir.


Karşılama → piyonu seçme → e3/e4 karesine ilk hamle → yön seçimi.
Taşları tanı, mevcut hedef kareli alıştırmalar ve satranç menüsüne dönüş seçenekleri iOS ile aynı davranır.

Profil tercihlerinde chess.guide.seen.v1 ve chess.guide.completed.v1 saklanır. Ertelendikten sonra Birlikte öğrenelim, tamamlandıktan sonra Yeniden göster düğmesiyle tekrar açılabilir. Dil Türkçe/İngilizce; mevcut arayüz çeviri mekanizması kullanılır. Yeni sesli anlatım kaydı eklenmedi.

Kutlama, mevcut ChessAudioPlayer.celebrate kullanır ve ses ayarına uyar. Android satranç alıştırmalarında görev tamamlanması zaten aynı kutlama yöntemini kullanıyordu; korundu.

Doğrulama:
- assembleDebug ve testDebugUnitTest başarılı; 118 test, 0 hata.
- Yeni ChessFirstStepsTests: piyon seçmeden hamle yapılamaması, yalnız e3/e4 kabulü, tamamlanmadan sonra tekrarlayan dokunuşların etkisiz olması ve geri dönüşte yeniden deneme.
- API 34 telefon emülatörü, 1080×1920: ilk açılış; erteleme; uygulamayı kapatıp açtıktan sonra tekrar gösterilmeme; elle yeniden açma; piyon seçimi; e2→e3; kutlama ekranı; alıştırmaya yönlendirme ve iki kayıt bayrağı doğrulandı.
- Ekran görüntüleri son Debug sürümündendir. Başlık/düğme çakışması giderildi, tahta telefon ekranına sığacak şekilde daraltıldı.
- Fiziksel cihaz, tablet ve büyük yazı ayrı kontrol edilmedi. Emülatör sessiz çalıştırıldığından kutlama sesinin işitsel değerlendirmesi yapılmadı.

Güncel doğrulama: Android derlemesi ve 118 birim testi başarılı. Emülatörde işaretsiz tekrar gösterme, işaretli kapalı kalma, süreç yeniden başlatma sonrası tercih korunması ve elle açıp seçeneği kaldırınca otomatik gösterimin yeniden başlaması geçti. dont-show-again.png seçenekli ekranı gösterir.

Dil ekran kontrolü: Android API 34 telefon emülatöründe öğrenme dili tr sabitken fr/ja/de/hi görünüm dilleri için karşılama, seçenek ve piyon etiketi doğrulandı. Uzun Japonca/Hintçe açıklamalarda deneme tahtası 240 dp ile sınırlandı; son Hintçe ekranında bütün tahta ve piyon görünür. Son Debug derlemesi başarılı. localized/ klasöründeki Hintçe seçim ekranı son yerleşimi, diğer ekranlar değişmeyen karşılama düzenini gösterir.
