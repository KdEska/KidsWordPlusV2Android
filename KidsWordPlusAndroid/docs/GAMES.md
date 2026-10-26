# Oyunlar ve günlük ilerleme

Referans: iOS `LearningCore.swift` içindeki ListeningGame, MatchingRound,
MemoryRound ve ProgressSnapshot; `HomeViews.swift` oyun ekranları.

## Uygulanan davranış

- Oyunlar yalnız seçili dilde çalışılmış etkin kavramları kullanır. En fazla
  40 aday içinden aynı resim SHA-256, ses SHA-256 veya harf büyüklüğü normalize
  edilmiş metni paylaşan adaylar tekilleştirilir. Dosya okuma arka plandadır.
- Dinle ve Seç: en az dört farklı aday, en fazla 10 soru, her soruda dört seçenek.
  Ses oynatılmaya başlamadan cevap verilemez. Yanlış seçenekte yeniden deneme
  mümkündür; ilerlemek için doğruyu bulmak gerekir. İlk cevap ayrı kaydedilir.
- Tekrar Zamanı: son dinleme turunda ilk cevabı yanlış olan, hâlâ etkin ve
  çalışılmış kelimeler hedeflenir. Aynı soruda yanlışın ardından doğruyu bulmak
  kuyruğu silmez; sonraki turda ilk cevap doğru olunca kuyruktan çıkar.
- Eşleştir: 2–4 çift. Önce ses seçilir, sonra karışık sıralı resim seçilir.
  Her ses için ilk cevap kaydedilir; doğru eşleştirilen çift tekrar seçilemez.
- Hafıza Kartları: 2/4/6/8 çift. Aynı açık karta tekrar basılamaz. Eşleşmeyen
  iki kart açıkken üçüncü kart açılamaz; bir saniye sonra veya düğmeyle kapanır.
  Deneme sayısı her ikinci farklı kart açılışında artar.
- Dinleme/eşleştirmede çözülen hedef günlük tekrar listesine eklenir. iOS'taki
  gibi hafıza oyunu günlük oyun sayısını artırır; tekrar kelimesi eklemez.
- Yarım tur günlük tamamlanan oyun sayısına eklenmez. Tur kimliği sayesinde
  tamamlanma iki kez bildirilse de oyun ve hafıza istatistiği bir kez artar.
- Günlük görev: 5 yeni kelime + 10 farklı tekrar + 2 oyun. Kalan kelime sayısı
  azsa yeni kelime hedefi uyarlanır, tümü çalışılmışsa 0 olur. Üç görev bitince
  seçili dil için o gün 20 yıldız verilir; aynı gün yeniden ödül verilmez.
- Tarih cihazın yerel takvim günüdür. Oyun ekranı açık kalırsa gün değişimi
  kontrol edilir. Dil geçmişleri ve yıldızları birbirinden ayrıdır.

## Kalıcılık ve taşıma

Önceki `completed` kavram seti aynen korunur. Yeni oyun/günlük veriler sürümlü
`games.v1` alanına eklenir. Ders çalışırken kavram ve günlük etkinlik aynı
SharedPreferences işlemiyle yazılır. Ekran, kayıt başarılı olduktan sonra
ilerler. Eski çalışılmış kelimeler bugüne yeni kelime olarak yazılmaz.

Tur ViewModel içinde tutulur; ekran dönüşünde soru, eşleşmeler ve açık kartlar
korunur. Süreç sonlandırılırsa tur yeniden başlar. O ana kadar kaydedilmiş ilk
cevaplar/tekrarlar ve tamamlanmış turlar saklanır. Kayıt okunamazsa boş verinin
eski kaydın üzerine otomatik yazılması engellenir.

## Doğrulama

GameTests: ses öncesi seçim engeli, ilk yanlış/sonraki doğru ayrımı, tekrarın
dil bazında çözülmesi, yarım tur saymama, çift tamamlanma, hafızada tekrar/üçüncü
kart engeli, günlük ödül tekilliği, içerik azaldığında hedef uyarlaması, kayıt
kodlama/okuma, medya tekilleştirmesi ve zorluk sınırları.

Cihaz kabulü henüz yapılmadı. Telefon/tablette dikey/yatay dönüş, büyük yazı,
TalkBack, arka plana geçiş, ses hatası, yarım turda süreç sonlandırma ve yeniden
açılış, gece yarısı gün değişimi ayrıca kontrol edilmelidir. Nihai karakter
animasyonları, çalışma serisi ve ebeveyn istatistik ekranları sonraki iştir.
