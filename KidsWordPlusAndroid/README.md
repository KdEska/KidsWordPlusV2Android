# KidsWordPlus Android

Kotlin + Jetpack Compose. Telefon ve tablet aynı uygulamada, yön kilidi olmadan
hedeflenir. Satranç kapsamda; konuşma pratiği sonraki sürüme bırakıldı.

## Mevcut çalışan kapsam

- Kalıcı isim, bağımsız TR/EN arayüz ve öğrenme dili, kurulu paket olmadan
  ilk kurulumu tamamlamayı engelleme.
- iOS ile aynı beş sekmeli alt menü; geniş pencerelerde ortalanmış içerik ve uyarlanan kart sütunları.
- Tam TR/EN veriyle kategori → alt kategori → ders → kelime/görsel/ses akışı.
- Dil ve kavram bazında tekrar sayılmayan ilerleme, kaldığı kelimeye dönüş.
- iOS ile aynı ders gruplaması: her alt kategoride en fazla 10 kelime;
  Türkçe alfabe paketteki sırayla 10+10+9.
- Katalog 2.3, ortak base bağımlılığı ve sürüme sabitlenmiş görsel çözümleme.
- Yerel geliştirme ZIP'leri ve HTTPS indirme; indirme ilerlemesi, iptal, yeniden
  deneme, Range ile sürdürme, sunucu Range desteklemiyorsa baştan indirme.
- Arşiv/manifest/dosya SHA-256, dosya ve arşiv boyutu denetimi; güvenli ZIP açma;
  yalnız doğrulanan revizyonu atomik etkinleştirme.
- Paket kaldırma ilerlemeyi silmez; base revizyonları diğer diller için korunur.
- Release taslak paketleri ve yerel geliştirme arşivlerini kullanmaz.
- Oyunlar sekmesi: Dinle ve Seç, Tekrar Zamanı, Eşleştir (2–4 çift), Hafıza
  Kartları (2/4/6/8 çift). Yalnız çalışılmış ve görsel/ses/metin açısından
  birbirinden farklı kelimeler kullanılır.
- İlk cevap istatistikleri, dil bazında dinleme tekrar kuyruğu, günlük görevler
  ve günde bir kez 20 yıldız. Yeni kelime hedefi kalan içeriğe uyarlanır.
- Oyun ve günlük ilerleme eski öğrenme kayıtları korunarak cihazda saklanır.
  Ekran dönüşünde tur devam eder; süreç sonlandırılırsa yarım tur yeniden başlar,
  daha önce kaydedilen ilk cevaplar ve tamamlanmış oyunlar korunur.

Diğer diller ve satrancın ileri bulmaca bölümleri henüz eklenmedi. Taş hareketleri, tahta dizimi, dört temel şah güvenliği hedefi, üç şah/mat/pat konumu ve üç tek hamlede mat bulmacası ve sekiz açılış dersi ve özel konumdan oyun başlatma ve süreli oyun eklendi.
Temel satranç oyunu Ana Sayfa → Satranç dünyası üzerinden açılır. Telefon ve tablet ölçüsündeki emülatörde oyun, kayıtlı konuma dönüş ve mat sonucu doğrulandı. Ayrıntı ve ekran görüntüleri: [CHESS.md](docs/CHESS.md). Nihai görsel eşdeğerlik ve fiziksel cihaz kabulü tamamlanmadı.

- 21 karakter, ilk altı başlangıç seçimi, iOS eşik ve görevleriyle kalıcı karakter açılması.
- Üç saniyelik ebeveyn kapısı; profil/dil ve paket yönetiminde işlem düzeyinde kontrol.
- Seçili dil için haftalık ebeveyn raporu, kategori ilerlemesi ve çalışma serisi.

- Temel satranç: iki kişilik/bilgisayarlı oyun, yasal hamleler, rok/geçerken
  alma/terfi, oyun sonları, ipucu, geri alma ve kalıcı oyun kaydı.

## iOS arayüzü

Güncel iOS kaynaklarındaki ana ekran, kategori/alt kategori, oyun, satranç,
profil ve altı adımlı kurulum düzeni Android’e uyarlandı. Alt menü:
Ana Sayfa / Öğren / Oyunlar / Satranç / Profil. Ebeveyn alanı Profil → Ayarlar
üzerinden açılır. Kapsam ve platform farkları: [IOS-UI.md](docs/IOS-UI.md).

21 karakterde iOS ile aynı el sallama/göz kırpma kareleri ve ritmi eklendi.
Açılışlar kitaplığı; dört ilke kartı ve gerçek son konumları gösteren sekiz
ders kartıyla yenilendi. Hareketler Android animasyon ayarına uyar.

Açılış derslerinde İzle / Sıra sende modları, animasyonlu hamleler ve yatay
tablette tahta/açıklama yerleşimi var. İzlemek öğrenme kaydını değiştirmez.

Satrançta kalıcı 2D/3D taş seçimi eklendi. Klasik taşlar iOS ile aynı
vektör konturları kullanır; oyun ve eğitim tahtaları ortak tercihi izler.

## Görseller

Özgün projeden 187 görsel; kategori/alt kategori resimleri, oyun ve satranç menüleri,
logo, gezinme ikonları ve bahçe arka planı entegre edildi. Panda uygulama simgesi ve
açılış ekranı eklendi. Telefon/tablet emülatör kontrolü ve kaynak dökümü:
[VISUALS.md](docs/VISUALS.md).

## Açma ve derleme

Android Studio'da bu klasörü açın. JDK 17 veya 21 ve Android SDK 35 gerekir. AGP 8.6.0 sınırı olan Android Studio sürümleri için proje AGP 8.6.0, Gradle 8.7 ve Kotlin/Compose compiler 2.1.21 kullanır. Sürüm değişikliği sonrası File > Sync Project with Gradle Files çalıştırın.
Android 8+ (`minSdk 26`) ilk çalışma varsayımıdır. Uygulama kimliği
`com.demarpsoftware.kidswordplus.v2`. `local.properties` makineye özeldir.

```sh
python3 scripts/prepare_packages.py
./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug
```

Kaynak veriler kardeş `language-packs-v2` klasöründen salt okunur alınır.
Testler gerçek TR/EN/base arşivlerini kullanır; tam kurulum testinin geçici
alanı için en az 500 MB, derleme ortamı için birkaç GB boş alan bırakın.
Bellek kullanımını sınırlamak için Gradle 1 GB bellek ve iki çalışanla ayarlı.

## Geliştirme paketlerini cihazda deneme

Normal Debug derlemesi Temel paket ve 13 öğrenme dilinin doğrulanmış yerel
arşivlerini otomatik olarak APK'ya dahil eder. Android Studio'dan Run ile
kurulan uygulama ayrıca dosya aktarmadan **Paketi hazırla** adımını tamamlar.
Arşivler kardeş `language-packs-v2` klasöründen `build/generated` altına
kopyalanır; referanslar değişmez. Tam Debug APK yaklaşık 685 MB'dır.

Küçük APK isteyen geliştiriciler açıkça bu modu seçebilir:

```sh
./gradlew :app:assembleDebug -PbundleLocalPackages=false
python3 scripts/load_device_packages.py --serial DEVICE_SERIAL
```

Bu küçük modda, APK kurulduktan sonra arşivlerin cihaza aktarılması gerekir.
Varsayılan paketli modda bu ek adım yoktur. Kurulu içerik çevrimdışı çalışır.

Büyük ZIP'ler sürüm kontrolüne alınmaz; Release kaynaklarına eklenmez.
`load_device_packages.py` yalnız belirtilen yerel Debug cihaza dosya kopyalar;
bulut servisine yükleme yapmaz.

## Uzak katalog

**Çevrimiçi kataloğu yenile**, iOS ile aynı Firebase HTTPS katalog adresine gider.
Debug önizleme kanalını, Release yayın kanalını kullanır. Başarılı katalog
önbelleğe alınır; ağ hatasında önceki katalog ve kurulu içerik korunur. Uzak
kaynak başarısızsa yerel geliştirme kaynağına düğmeyle dönülebilir.

Kaynak paketler taslaktır; bu çalışma Firebase'e dosya yüklemez veya içerik
onayı vermez. Canlı katalog/indirme ancak sunucuda paketler yayımlandıktan
sonra doğrulanabilir. Release yerel içerik olmadan açıldığında paket ister.

## Sıradaki işler

1. Telefon/tablet, dikey/yatay, süreç sonlandırma, büyük yazı ve TalkBack kabulü.
2. Gerçek CDN üzerinde indirme/kesinti/iptal ve cihaz ses denetimi.
3. Güncel iOS görsel eşdeğerliği, karakter animasyonları ve kalan arayüz/öğrenme dilleri.
   Güncel öğrenme dili listesi: tr, en, fr, de, es, pt, zh, ja, ko, it, hi, ar, ru.
4. Oyunlar ve karakter ödüllerinin gerçek cihaz kabulü.
5. Ebeveyn alanının cihaz/erişilebilirlik kabulü, hatırlatmalar ve gelişmiş ayarlar.
6. Satranç: oyun/kayıt ve temel eğitim/bulmacalar hazır; genişletilmiş turlar ve cihaz kabulü sıradadır. Kolay/Orta/Zor bilgisayar seviyeleri ve çevrimdışı TR/EN ders/hamle anlatımı eklendi.
7. Firebase servisleri, güncel hedef SDK, gizlilik ve mağaza hazırlığı.

Aktarım kuralları: `docs/PORTING.md`. Son doğrulama: `docs/verification.json`.

## Son kontrol

Güncel derleme ve test sonuçları `docs/verification.json` içinde tutulur.
Oyun kuralları: `docs/GAMES.md`. Karakter ve ebeveyn kapsamı: `docs/FAMILY.md`. Satranç: `docs/CHESS.md`.
Cihaz ve canlı CDN kabulü bekliyor.

Firebase Crashlytics ve Analytics entegrasyonu ve doğrulama: [docs/FIREBASE.md](docs/FIREBASE.md).

## Bildirimler ve öğrenme hatırlatmaları

Ebeveyn alanı → Ayarlar bölümüne isteğe bağlı FCM push bildirimi ve birden fazla günlük öğrenme hatırlatması eklendi. İki özellik ayrı yönetilir ve varsayılan olarak kapalıdır. Uygulama, izin gerektiğinde Android bildirim iznini ister. Bildirimden öğrenme daveti ve önerilen ders açılır. Kurulum, gönderici sözleşmesi ve doğrulama sınırları: [NOTIFICATIONS.md](NOTIFICATIONS.md).
