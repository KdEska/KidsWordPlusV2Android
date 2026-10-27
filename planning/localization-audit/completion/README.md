# Çeviri tamamlama durumu — 26 Ekim 2026

Durum: **Kısmen tamamlandı; dış çeviri servisine aktarım onayı bekleniyor.**

## Tamamlanan işler

- Mevcut arayüz, yardım ve satranç çevirileri iki platformun ortak metin sözlüğünde tekrar kullanıldı. Eksik yerlere İngilizce kopyalanmadı.
- Android: Yardım örneği düğmeleri, satranç geçmişi, alınan taşlar, erişilebilirlik durumları, karakter açıklamaları/koşulları, haftanın günleri ve ses düğmelerinin dil bağlantıları düzeltildi.
- Android: Sayı, isim ve kare adı içeren 15 dinamik çağrı çeviri şablonuna dönüştürüldü; çeviriden önce metin birleştirilmesi engellendi.
- Android: Satranç genel bilgiler sayfası seçilen arayüz dilini kullanıyor. Açılışlar ekranı yalnızca iki ses dosyası klasöründen okumak yerine, mevcut 13 dilli ortak içerik kataloğunu kullanıyor.
- iOS: Arapça/Rusça kamera, mikrofon ve konuşma tanıma izin açıklamaları eklendi; paketleme ve Xcode dil listesi güncellendi.
- Çeviri üretim araçlarına 11 hedef dilin tamamı dahil edildi. Yeni `tools/check_interface_coverage.py` eksik çeviri varsa başarısız olur; Arapça/Rusça istisnası yoktur.

## Doğrulama

- iOS derlemesi başarılı; Arapça/Rusça InfoPlist.strings dosyaları uygulama paketinde mevcut.
- Android derlemesi başarılı; 165 test, 0 hata. Yeni test, açılış metinlerinin 13 dilde ses paketi bağımlılığı olmadan okunduğunu doğruluyor.
- Tam çeviri kapsamı kontrolü halen başarısız: 667 farklı İngilizce kaynak metin için toplam 3133 hedef dil karşılığı bekleniyor. Bu sayı mevcut ve eski katalog anahtarlarını da içeren envantere dayanır; tamamı farklı ekran sayısı değildir.
- `pending.json`: Her hedef dil için çevrilecek kaynak metinlerin somut listesi.
- `keys.json`: Ortak anahtar envanteri.

## Onay bekleyen işlem

Yalnızca `pending.json` içindeki sabit uygulama metinleri, eksik hedef dillere çevrilmek üzere `https://translate.googleapis.com/translate_a/single` adresindeki Google Translate hizmetine gönderilecek. Profil, çocuk bilgileri, fotoğraflar, kullanıcı tarafından yazılan mesajlar, kullanım/ilerleme kayıtları, anahtarlar veya başka çalışma alanı dosyaları gönderilmeyecek. Mevcut çeviriler korunacak; dönen sonuçlarda boş metinler ve değişken yer tutucuları kontrol edilecek.

Otomatik onay denetimi ilk gönderim girişimini, uygulama metinlerinin üçüncü taraf çeviri servisine aktarımı için açık kullanıcı onayı bulunmadığı gerekçesiyle reddetti. İşlem yürütülmedi ve bu servise metin gönderilmedi. Çeviriler tamamlanmadan işin tamamlandığı iddia edilmemelidir.
