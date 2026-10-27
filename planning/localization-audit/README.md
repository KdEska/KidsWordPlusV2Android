# iOS / Android 13 dil denetimi

> Güncelleme: Dil bağlantıları ve izin açıklamaları düzeltildi; mevcut çeviriler ortaklaştırıldı. Yeni çeviri aktarımı için onay bekleniyor. Güncel durum: [Tamamlama raporu](completion/README.md). Aşağıdaki bulgular ilk denetim anına aittir; audit.json güncel durumu gösterir.

Tarih: 26 Ekim 2026

**Sonuç: Çeviriler eksiksiz değil.** Bu çalışma kontrol ve raporlamadır; uygulama metinleri değiştirilmedi.

## Kapsam ve sınırlar

Aktif Swift/Kotlin kaynakları, paketlenmiş ana arayüz sözlükleri, yardım ve satranç metin katalogları, iOS izin açıklamaları incelendi. `sources/`, Git kopyaları ve derleme çıktıları kapsam dışıdır. Türkçe ve İngilizce ana arayüz metinleri çoğunlukla kaynak koddan gelir; diğer 11 dil sözlükten çözülür.

Doğrudan yazılmış çift dilli çağrı taraması, eksik anahtarlar için kanıttır; bütün dinamik ifadeleri kapsayan bir sözdizimi çözümlemesi değildir. Sayılar benzersiz İngilizce anahtar adedidir; dil başına eksik çeviri toplamı değildir. Kullanılmayan veya yalnızca test araçlarında görünen çağrılar da bulunabilir. Katalogda metin bulunması, çevirinin dil uzmanı tarafından onaylandığı anlamına gelmez.

## Ana bulgular

| Platform | En az bir hedef dilde eksik anahtar | Arapça/Rusça dışındaki dillerde de eksik anahtar |
|---|---:|---:|
| ios | 288 | 28 |
| android | 522 | 311 |

Arapça ve Rusça en fazla eksiği bulunan diller. Bir metin sözlükte bulunmadığında her iki platformun ana arayüz çözümleyicisi İngilizceye dönüyor.

## Doğrulanan örnekler

- iOS: Yeni arkadaş kutlamasındaki “A new friend unlocked!”, “Go to my friends”; paylaşım alanındaki “Share the joy of learning”; mağaza puanı test ve ebeveyn doğrulama mesajları 11 hedef dilde de eksik.
- Android: Satranç alıştırmalarındaki “Choose a piece first.”, “Show target”, “Retry saving”; 2D/3D taşların erişilebilirlik açıklamaları 11 hedef dilde de eksik.
- Android `PageHelp.kt`: Örneği göster / Örneği başa al düğmeleri doğrudan Türkçe/İngilizce dallanıyor.
- Android `ChessCapturedPanel.kt`: Alınan taşların başlıkları ve renk erişilebilirlik açıklamaları Türkçe/İngilizce dallanıyor.
- Android `ChessHistoryPanel.kt`: Açık/Kapalı ekran okuyucu durum etiketleri ve taraf isimleri Türkçe/İngilizce dallanıyor.
- Android `ParentDesign.kt`: Haftanın gün kısaltmaları Türkçe/İngilizce listelerinden geliyor.
- Android `ChessIntroduction.kt`: İçerik dilini `InterfaceStrings.text(tr,"tr","en")` ile seçiyor; diğer dillerde `en` dönüyor. Dolayısıyla 13 dilde içerik dosyası olmasına rağmen bu ekran İngilizce içeriği kullanabiliyor.
- Android arkadaş açıklamaları ve açılış ders başlığında `if(tr) … else …` kullanımları var; bu alanlar seçilen diğer arayüz dillerine bağlı değil.
- iOS Arapça ve Rusça `InfoPlist.strings` dosyaları yok: kamera, mikrofon ve konuşma tanıma izin açıklamalarının bu iki dilde yerel karşılığı bulunmuyor. Sistem gösterimi cihazın diline ve uygulamanın bildirdiği dillere bağlıdır.

## Tam olan kataloglar / sınırı

- iOS yardım kataloğu: 218 metin × 13 dil, anahtar eksikliği yok. Android yardım ekranları bu kataloğu doğrudan okumuyor; kendi `PageHelpCatalog.kt` metinlerini ana arayüz sözlüğünden çözüyor. Bu nedenle iOS yardım kataloğunun tam olması Android yardımını garanti etmez.
- Satranç arayüz kataloğu: 202 anahtar × 13 dil, anahtar eksikliği yok.
- Satranç temel içerik kataloğu: 85 anahtar × 13 dil, anahtar eksikliği yok.
- Satranç tarih kataloğu: 38 üst düzey kayıt × 13 dil, üst düzey anahtar eksikliği yok. İç içe metinlerde dil ve kalite kontrolü ayrıca gerekir.
- Ana arayüz sözlüklerinde sayısal/isimli yer tutucu uyumsuzluğu bulunmadı.
- Öğrenilen kelimeler arayüz dilinden bağımsız olarak seçilen öğrenme dilinde kalmalıdır. Marka, karakter özel adları, teknik kayıtlar, URL ve satranç notasyonu otomatik olarak çeviri eksiği sayılmamalıdır.

## Mevcut testlerin açığı

`KidsWordPlusV2/Tests/LocalizationChecks.swift` anahtar eşitliği ve kategori kontrolünde Arapça/Rusçayı açıkça hariç tutuyor. Android’in önceki `scripts/audit_localization.py` aracı da yalnızca dokuz hedef dili inceliyor. Bu nedenle önceki test başarıları bütün metinlerin 13 dilde tam olduğunu kanıtlamıyor.

## Dosyalar

- `audit.json`: Dil, dosya, satır ve İngilizce anahtarı içeren ayrıntılı eksik listesi; sözlük farkları, yer tutucu kontrolleri ve izin dosyalarının durumu.
- `tools/audit_localization.py`: Tekrar çalıştırılabilir kontrol; uygulama kaynaklarını değiştirmez.

## Düzeltme sırası

1. Sözlükleri tek bir anahtar envanteri altında birleştirmek; iOS / Android farklarını ve Android üretim betiğini koruyarak düzeltmek.
2. Arapça/Rusça eksiklerini ve diğer dokuz dilde eksik yeni metinleri çevirmek.
3. Doğrudan Türkçe/İngilizce seçen alanları, dinamik mesajları ve erişilebilirlik etiketlerini dil sistemine bağlamak.
4. iOS izin açıklamalarını tamamlamak; sistem dili / arayüz dili ayrımını korumak.
5. Her 13 dil için eksik anahtarları reddeden kontroller eklemek; oyun, hata, kutlama, bildirim ve erişilebilirlik akışlarında cihaz testi yapmak.
