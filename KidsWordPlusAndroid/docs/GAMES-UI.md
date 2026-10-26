# Oyun ekranları — iOS eşleştirmesi

Referans: `KidsWordPlusV2/App/HomeViews.swift` içindeki GamesLibraryView,
ListeningGameView, MatchingGameView, MemoryGameView, DailyTasksView,
ScoreboardView, PortraitGameArtwork ve FlippingMemoryCard.

- Oyun menüsü: mevcut özgün görseller, beş renkli oyun/görev kartı,
  günlük skor, yıldız ve skor tablosu. Konuşma pratiği önceki ürün kararıyla
  ertelenmiştir.
- Dinle ve Seç / Tekrar: soru sayacı, yeşil ilerleme, 3:4 beyaz görsel
  kartları, kırmızı/yeşil cevap çerçeveleri ve işaretleri, yanlışta sallanma,
  doğruda kısa yükselme; kaydırılabilir sorudan bağımsız Dinle/Devam alanı.
  Telefonda iki sütun, tablette 220 dp civarı minimum genişlik, büyük yazıda
  tek sütun. Önce dinleme ve ilk cevap kuralları değişmez.
- Boş durum: bekleyen tekrar yoksa veya yeterli farklı kelime çalışılmamışsa
  ayrı açıklama ve kategorilere geçiş ekranı açılır.
- Eşleştir: pencere yerine 2–4 kelimelik sayfa halinde seçim; eksik kelimede
  seçenek pasif. Mavi ses kartları, beyaz görseller, seçili durum,
  doğru çiftler arasında yeşil bağlantılar ve eşleşme işaretleri.
- Hafıza: başlangıç/kolay/orta/zor seçim kartları, 2×2 / 4×2 / 4×3 / 4×4
  düzenleri; mor yıldızlı arka yüz, 360 ms dönüş, yeşil eşleşme işareti.
  Tablet kart alanı 640 dp ile sınırlı. Son eşleşmenin görünmesi için sonuç
  ekranı hareket açıksa 750 ms bekler. Yanlış çift otomatik kapanır; erişilebilir
  devam düğmesi de korunur.
- Günlük görevler: özgün ödül şeridi, üç ayrı görev kartı ve ilerleme çubukları,
  yıldızlar, çalışma serisi ve kelimelere geçiş. Sayılar mevcut GameProgress'ten
  okunur; yeni ödül veya ilerleme formatı oluşturulmaz.
- Sonuçlar: karakter, oyun türüne uygun başlık ve skor, tekrar oyna,
  eşleştirme/hafızada seviye değiştir, oyunlara dön. Paylaş düğmesi Android'in
  paylaşım seçicisini açar; kendiliğinden mesaj göndermez.
- Alt sayfaların başlıkları üst gezinmede görünür; dekoratif hareketler sistemin
  hareket tercihini izler. Oyun motoru ve ilerleme kayıtlarının kuralları aynı.

Ekran kayıtları `reviews/games-ios/`, kontrol sonuçları `verification.json`.
Android yazı çizimi, sistem paylaşım paneli ve materyal efektleri iOS'tan
platform gereği farklıdır. Tam piksel eşitliği ve fiziksel cihaz kabulü ayrıca
incelenmelidir.
