# Android aktarım sözleşmesi

## Referans

Çalışan iOS kodu ve Core testleri esas alınır. Eylül planları tarihsel kararlar
ve eski sayılar içerir. Android geliştirmesi iOS ve ortak içerik kaynaklarını
salt okunur kullanır. Konuşma pratiği bu Android sürümünün kapsamı dışındadır.

## Bu aşamada uygulanan kurallar

- Arayüz dili ve öğrenme dili bağımsızdır.
- İlerleme öğrenme dili + conceptId ile saklanır; tekrar tamamlama sayacı artırmaz.
- Devam noktası kavram kimliğinden güncel derse eşlenir.
- `learningEnabled != false` olan kelimeler gösterilir.
- TR/EN dersleri iOS `makeLessons` gibi kaynak ders sırasını koruyup alt kategori
  içinde 10'arlı gruplara ayrılır. CAT kelimelerindeki eski ders anahtarı düzeni
  korunur. Alfabe müfredatı eksiksiz ve tekil olmalı; I ve İ farklı kavramlardır.
- Paket silme ilerlemeyi silmez; ortak base revizyonları silinmez.
- Dil, manifestindeki base sürümünü ve hash'ini sabitler; base güncellemesi başka
  bir dilin görsellerini değiştirmez.
- Şema 2.3 desteklenir. Android'de daha eski kurulum olmadığından 2.2 geçişi yoktur.
- Hash/boyut veya içerik hatası ve iptalde mevcut dil revizyonu korunur.
- Release yalnız yayınlanmış katalog girdilerini ve releaseReady manifestleri kabul eder.
- HTTPS yönlendirmeleri otomatik izlenmez; HTTP, kullanıcı bilgisi ve fragment içeren
  URL'ler reddedilir. Değişmeyen doğrudan HTTPS arşiv adresleri gerekir.
- ZIP yolları ve yinelenen kayıtlar denetlenir; girdiler normal dosya olarak yazılır,
  arşiv içindeki symlink bilgisi kullanılarak dosya sistemi bağlantısı oluşturulmaz.
- Arşiv ve açılmış toplam içerik sınırı 1 GB; tek dosya 100 MB, 20.000 girdi;
  manifest 5 MB, veritabanı 16 MB, katalog 2 MB sınırındadır.
- Ağ, JSON, dosya doğrulama ve görsel çözümleme arka planda yapılır. Ekran
  dönüşünde ViewModel indirme işlemini korur. Süreç kapanırsa HTTPS kısmi dosyası
  kalır; kullanıcı yeniden deneyebilir. Arka planda indirme garantisi yoktur.

## Kalan işler / sınırlar

TR/EN içerik, ders akışı, üç temel oyun, karakter seçimi ve ebeveyn alanı
eklendi; temel satranç oyunu, taş hareketleri, tahta dizimi ve dört şah güvenliği
hedefi, üç şah/mat/pat konumu ve üç tek hamlede mat bulmacası ve sekiz açılış dersi ve özel konumdan oyun başlatma ve süreli oyun da eklendi.
Diğer diller, genişletilmiş eğitim/bulmaca turları ve cihaz kabulü sıradadır. Çevrimdışı TR/EN ders/hamle anlatımı eklendi. Kolay/Orta/Zor bilgisayar seviyeleri eklendi. Şu an profil ve ilerleme SharedPreferences
ile saklanır; daha geniş ilerleme modeline geçişte sürümlü taşıma gerekir.
Dinleme tekrar kuyruğu ve günlük yıldız kuralları eklendi. Karakter açma, çalışma serisi ve ebeveyn raporu eklendi;
gerçek cihaz kabulü bekliyor. Ayrıntı: `GAMES.md`.

Ses ekran/kart değişiminde ve arka plana geçişte durur; ses odağı ve kulaklık
çıkarma kabulü henüz yapılmadı. Telefon/tablet düzeni kodda vardır; etkileşim ve
görsel kabul tamamlanmadı. Sunucu paketi henüz yayımlanmadığından canlı CDN
kabulü bekler. Ağ testleri taklit yanıtlarla, dosya testleri gerçek arşivlerle yapılır.
