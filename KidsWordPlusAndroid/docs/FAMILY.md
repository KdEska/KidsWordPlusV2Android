# Karakterler, ebeveyn kapısı ve raporlar

## Karakterler

Mevcut iOS CompanionCatalog içindeki 21 karakter ve aynı açılma koşulları
aktarıldı. Görseller `design/companion-characters` kaynağından değiştirilmeden
Android varlıklarına kopyalandı; kaynak dosyalar salt okunur kullanıldı.

İlk altı arkadaş başlangıçta seçilebilir. Diğerlerinde kelime eşiğine ek olarak
10 oyun, hatasız en az dört soruluk dinleme/tekrar turu, yedi günlük çalışma
serisi veya bir öğrenme seviyesinin tamamlanması koşulları vardır. Nova diğer
20 arkadaş açılınca görünür. Öğrenme kelime sayısı ve karakter kanıtları diller
arasında toplanır; seri aynı tarihteki farklı dilleri tek gün sayar.

Açılan arkadaşlar, seçili karakter ve ek koşul kanıtları kalıcı saklanır.
Paket silmek veya başka dile geçmek kazanılmış arkadaşları kilitlemez. Eski
çalışılmış kelime kayıtları eşiklere katılır. Önceki Android sürümlerinde
hatırlanmayan kusursuz tur kanıtı tahmin edilmez; bu koşul yeni bir kusursuz
 turla kazanılır. Seçim uygulama karakterini değiştirir; Android başlatıcı
ikonunu değiştirme bu adımın kapsamında değildir.

## Ebeveyn alanı

Kurulum sonrası profil/dil düzenleme, paket kurma/silme ve katalog yenileme
üç saniyelik kapının arkasındadır. Yönetim işlemlerinin kendisi de ViewModel'de
kapı durumunu denetler. İlk kurulum tamamlanmadan paket hazırlama serbesttir.

Erken bırakma, basılı noktadan 20 dp uzaklaşma veya ekrandan çıkış tutmayı
iptal eder. Uygulama arka plana geçtiğinde ve ebeveyn alanından çıkıldığında
açık erişim sıfırlanır; erişim diske yazılmaz. Ekran okuyucu/klavye kullanımına
alternatif olarak iki rastgele iki basamaklı sayıyı toplama seçeneği vardır.
Gerçek TalkBack ve işaretçi davranışı cihaz kabulünde ayrıca kontrol edilmelidir.

## Rapor

Seçili öğrenme dilinin Pazartesi–Pazar haftası kullanılır. Haftalık yeni
kelimeler benzersiz kavram birleşimidir; tekrarlar günlük farklı kavram
sayılarının toplamıdır. Çalışma günleri, tamamlanan oyunlar, tüm zamanların
çalışılmış etkin kelimeleri ve tamamen çalışılmış dersleri ayrı gösterilir.
Yıldız, yerel çalışma serisi, bekleyen dinleme tekrarı, günlük yeni kelime
listesi, güçlü/gelişime açık kategoriler ve kategori ilerlemesi vardır.

Geçmiş kayıtlar için süre tahmin edilmez; bugünkü kelime etkinliği gösterilir.
Paket kaldırılmışsa rapor için yeniden kurma yönlendirmesi sunulur, ilerleme
silinmez. Hesap/bulut senkronizasyonu veya rapor paylaşımı eklenmedi.

## Kontroller

FamilyTests: başlangıç karakterleri, eşik + ek koşul birleşimi, kazanılmış
karakterin korunması, Nova koşulu, dil birleşimli seri, seviyenin aynı dilde
tamamlanması, kesintisiz üç saniye ve erken bırakma, kurulum/yönetim erişim
ayrımı, haftanın Pazartesi başlaması, raporda dil ve etkin içerik ayrımı.

Telefon/tablet, yatay/dikey, büyük yazı, ekran okuyucu, basılı tutma sırasında
arka plana geçiş ve kalıcı seçimin yeniden açılışta görünmesi cihaz kabulünde
bekliyor. Karakter animasyonları ve başlatıcı ikon değişimi henüz uygulanmadı.
