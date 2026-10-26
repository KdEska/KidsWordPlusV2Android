# Kompakt aile paylaşımı penceresi

Android paylaşım penceresinin %94 sabit ekran yüksekliği kaldırıldı. Pencere içeriğine göre yükseklik alır, varsayılan Dialog yerleşimi ve ortalanmış kapsayıcı ile ekranın ortasında kalır. Üst sınır görünür alanın %78’i veya 560 dp; küçük ekran/klavye durumlarında içerik kaydırılabilir. Genişlik en fazla 520 dp. Kart ön izlemesi 220 dp yüksekliğe küçültüldü, görsel oranı korunur ve kırpılmaz. İç boşluklar sıkılaştırıldı.

Doğrulama: assembleDebug başarılı. 1080×1920 API 34 telefon emülatöründe paylaşım ön izlemesi açıldı; başlık, kart, açıklama ve Paylaş düğmesi aynı anda görünür. Pencere dikey olarak ekranın ortasında; yaklaşık y=372–1548 aralığında. Sistem paylaşımı başlatılmadı, hiçbir içerik gönderilmedi.
