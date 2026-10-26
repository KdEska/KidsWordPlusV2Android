# Dil desteği

2026-10-21

- Öğrenme dilleri: tr, en, fr, de, es, pt, zh, ja, ko, it, hi, ar, ru.
- Arayüz dilleri (iOS ile aynı): tr, en, de, fr, es, zh, ja, ko, it, pt, hi.
- Arayüz ve öğrenme dili birbirinden bağımsız kaydedilir.
- Öğrenme paketi başlıkları kendi dil adını kullanır. Ders/oyun kelimeleri, görselleri ve sesleri seçilen öğrenme paketinden gelir.
- Arayüz çevirileri iOS Localization/interface.json kaynağının Android'e kopyasından okunur. Mevcut çift dilli metin yardımcıları, kategori adları ve sabit metin çiftleri bu sözlüğü kullanır. Eksik anahtarlar İngilizceye döner; Android'e özgü bazı açıklamalar ve dinamik metinler hâlâ İngilizce olabilir. Bu, tüm ekranların tam yerelleştirme onayı değildir.
- Satranç seslendirmesinde mevcut TR/EN kayıtları kullanılır; diğer arayüz dilleri İngilizce seslendirmeye döner. Öğrenme kelimelerinin yerel dil sesleri bundan bağımsızdır.
- Cihaz dilini otomatik izleme bu değişikliğin kapsamında değildir.

Yerel geliştirme kataloğu: `scripts/prepare_packages.py`. Arşivler salt okunur referanslardan doğrulanır; `--bundle` APK içine kopyalamak içindir. Varsayılan Debug derlemesi ZIP arşivlerini APK içine otomatik ekler. Küçük Debug APK için `-PbundleLocalPackages=false` kullanılabilir; bu modda arşivleri ayrıca aktarmak gerekir. `scripts/load_device_packages.py --serial DEVICE` ile tüm doğrulanmış arşivler seçilen geliştirme cihazına aktarılır. Release indirmeleri yapılandırılmış uzak kataloğa bağlıdır; içerik bu çalışmada yayınlanmadı.

Doğrulama: 108 birim testi geçti. Yeni testler 13 gerçek pakette sözlük/medya yolları ve ders benzersizliği, 13 dilin katalog kapsamı, 11 arayüz dili ve geri dönüş davranışını kapsar.

2026-10-21 ek kontrol: İsimli karşılama, açılış arkadaş mesajı, ebeveyn öğrenme yolculuğu ve haftalık yeni kelime özeti açık çeviri şablonlarına bağlandı. Yer tutucular tek geçişte işlenir; kullanıcı adındaki süslü parantezler/dolar karakterleri yeniden yorumlanmaz. Kurulum özetinde arayüzün gerçek yerel dil adı gösterilir.

Satranç arayüz ve içerik sözlükleri de `prepare_localization.py` ile İngilizce anahtarlar üzerinden ortak menü sözlüğüne eklenir. Android'e özgü kısa satranç metinleri `localization/android.json` içinde tutulur. Tam anahtar bulunamazsa ✓ işareti ve son iki nokta korunarak alanın temel anahtarı aranır. Satranç dünyasındaki dil bilgisi seçilen gerçek arayüz adını kullanır.

Ortak Android mesajları için ses/kamera hatası, önce sesi dinle, ebeveyn alanını kilitle, cihazdaki veri, arayüz/öğrenme dili, satranç kurtarma ve deneme sayısı metinleri dokuz ek arayüz diline eklendi. ViewModel'in kullanıcı mesajları seçilen arayüz sözlüğünü kullanır. Hafıza oyunu sonucu yer tutuculu çeviriye bağlandı; Tekrar Zamanı başlığı mevcut iOS anahtarına eşlendi.

Eksik metin taraması: `python3 scripts/audit_localization.py` komutu `docs/localization-audit.json` üretir. Rapor literal çiftleri ve dinamik metin adaylarını dosya/satırla listeler. Kullanılmayan/debug kodu da kapsar; iç içe koşulların ve üretilen bütün metinlerin tam analizi değildir. Kurulumdaki temel paket açıklaması iOS'un uzun açıklamasına, rapor için paket gerekli mesajı Android çevirilerine bağlandı.

Tahta hazırlama yönergesi adlandırılmış `{piece}` / `{square}` şablonuyla yerelleştirilir. Seslendirme, gösterilen çevrilmiş taş adını kullanmaz: sabit taş türünü TR/EN kayıt kataloğundaki ada dönüştürür. Aynı ayrım taş alıştırmalarının hedef seslendirmesinde de kullanılır. Özel konumun materyal/komşu şah/sırası olmayan şah/bitmiş konum uyarıları iOS'un mevcut çevirilerine bağlandı.
