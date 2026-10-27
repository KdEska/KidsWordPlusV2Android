# KidsWordPlus Android — Faz 1

## 27 Ekim 2026 güncellemesi

Son değişiklikler: kümülatif Basit/Orta/Zor kelime seviyeleri, şarap kaydı çıkarılmış güncel dil paketleri, yıldız toplama sesi, Satranç Sırları sayfası, satranç görev ve ses geri bildirimleri, 2×2 piyon terfi seçimi ve ana sayfa yükleme kartı.

Yerelleştirme bağlantıları ve mevcut çeviriler ortaklaştırıldı. **13 dilde çeviri kapsamı henüz tam değil.** Eksik metinler ve doğrulama durumu: [çeviri tamamlama raporu](planning/localization-audit/completion/README.md). Katı çeviri kapsamı kontrolü, bu eksikler tamamlanana kadar başarısız olur.

Son doğrulama: iOS ve Android debug derlemeleri başarılı; Android’de 165 test geçti. Yeni faz etiketi oluşturulmadı.


Kotlin ve Jetpack Compose ile telefon ve tablet uygulaması.

Android Studio’da `KidsWordPlusAndroid` klasörünü açın. JDK 17 kullanın; SDK yolunu yerel `local.properties` dosyanızda ayarlayın. `language-packs-v2` kardeş klasör olarak korunmalıdır: debug paketleri ve paket testleri bu verileri kullanır.

Kurulum, özellikler ve testler: [Android README](KidsWordPlusAndroid/README.md). `KidsWordPlusV2/Chess/interface.json` yalnızca ortak satranç çeviri testlerinin referansıdır.

Son doğrulama: debug derlemesi ve 159 birim testi başarılı.
