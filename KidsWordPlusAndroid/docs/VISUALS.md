# Android görsel entegrasyonu

Mevcut projenin tasarım dosyaları Android ekranlarına bağlandı; kaynak referanslar değiştirilmedi.

- Logo ve bahçe arka planı; okunabilirlik için beyaz yarı saydam içerik paneli.
- Ana ekran kartları, alt menü ve tablet yan menüsünde özgün ikonlar.
- Kategori ve alt kategori resimleri, ders kapaklarında kurulu paketten kelime görseli.
- Dinleme, tekrar, eşleştirme, hafıza ve günlük yıldız görselleri.
- Ebeveyn kapısı/rapor ikonları ve satranç menüsündeki bölüm görselleri.
- Özgün panda ile adaptive launcher icon ve Android 12+ açılış ekranı.

## Dosyalar ve yeniden hazırlama

`scripts/prepare_ui_assets.py`, kardeş `design/` ve `KidsWordPlusV2/` referanslarından
187 WebP dosyası üretir (8.591.728 bayt). Saydamlık korunur, boyutlar kullanım yerine
göre sınırlandırılır. Pillow gerekir. `--force` yeniden üretir. Kaynak/hedef SHA-256
ve boyut dökümü `ui-assets.json` içindedir. Resimler arka planda yüklenir;
çözünürlük sınırı ve 20 MiB bellek önbelleği kullanılır.

Düşük çözünürlüklü landscape referansı tam ekran büyütülmez. Telefon ve tablette
bahçe resmi kullanılır; tablet ekranında oran korunarak kırpılır.

## Doğrulama

Debug ve imzasız Release derlendi; 65 birim testi başarılı. Lint: 0 hata,
27 uyarı, 9 bilgi. Her iki APK içinde 187 UI görseli ve mevcut 538 satranç
ses kaydı doğrulandı. Android 8.0 için koyu gezinme çubuğu, 8.1+ için açık
gezinme çubuğu kaynakları ayrıldı.

API34 ARM64 emülatörde telefon (1080×1920 / 420 dpi) ve tablet ölçüsü
(1600×2560 / 240 dpi) kontrol edildi. Ana ekran, kategori/alt kategori, oyunlar,
ebeveyn kapısı ve satranç menüsünün kayıtları `../reviews/visual-integration/`
altındadır. Tablet kontrolü aynı emülatörün yeniden boyutlandırılmış görünümüdür.

Fiziksel cihaz, büyük yazı, TalkBack, yatay ekran kabulü ve karakter animasyonları
bu kontrolün dışında kaldı. iOS ile tüm ekranların birebir eşdeğerliği henüz tamamlanmadı.
