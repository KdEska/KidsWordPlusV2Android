# Ebeveyn alanı — iOS görünümü

Referans: `KidsWordPlusV2/App/HomeViews.swift` içindeki ParentGateView,
ParentDashboardView, LearningReportView ve ParentSettingsView;
`KidsWordPlusApp.swift` içindeki PackageLibraryView.

Ebeveyn kilidi 3 saniye basılı tutmayı ve erken bırakmada sıfırlamayı korur.
iOS'taki mor geçişli Basılı tut düğmesi kullanılır. Android'in erişilebilir
alternatif doğrulaması korunur. Çocuk sekmeleri bu alanda görünmez.

Panelin kendi dört sekmesi vardır: Ana Sayfa, Raporlar, Profil, Ayarlar.
Profil sekmesi güncel iOS profil/karakter dünyasını tekrar kullanır. Paket
sayfasından geri dönüldüğünde Ayarlar sekmesi korunur. Çıkış ve arka plana
geçiş mevcut ebeveyn kilidini yeniden etkinleştirir.

Ana sayfada karakterli selamlama, günlük görevlere giriş, dört eşit yükseklikli
haftalık ölçüm kartı, Pazartesi–Pazar yeni kelime grafiği ve teşvik kartı vardır.
Veriler mevcut ParentReport/GameProgress'ten hesaplanır; ekran 60 saniyede bir
günlük veriyi yeniler. Grafiğin giriş animasyonu sistem hareket tercihini izler.
Büyük yazıda ölçümler tek sütuna geçer.

Raporlarda güçlü/gelişime açık kategoriler görsel ve yüzdeyle, çalışma etkinliği,
çalışılan kelime, tamamlanan ders ve yıldız sayısı ayrı beyaz kartlarla sunulur.
Çalışma süresi alanı iOS'taki gibi kelime etkinliği gösterir; süre uydurulmaz.

Ayarlar mevcut Android işlevlerini kullanır: dil tercihleri, dil paketleri,
cihazdaki verilerin açıklaması, kilitleme ve gerekirse satranç kaydı kurtarma.
Dil seçimi ebeveyn yetkisiyle mevcut kayıt yöntemine gider. Kurulu olmayan
paketler mevcut paket yönetimi üzerinden kurulabilir.

Paketler açıklama/temel paket/dil bölümlerine ayrılır. Boyut, çevrimdışı durum,
seçili dil, kurulum ve doğrulama eylemleri gösterilir. Silme düğmesi iOS'taki
gibi onay penceresi açar; vazgeçme veriyi değiştirmez.

Bu çalışma iOS'ta olup Android'de henüz bulunmayan fotoğraf/kamera, bildirim,
hatırlatıcı, analiz ve günlük hedef ayarlarına sahte kontroller eklemez.
Bunlar ayrıca uygulanmalıdır. Ekran okuyucu/fiziksel cihaz kabulü ve tam piksel
eşitliği bekler. Ekran kayıtları: `reviews/parent-ios/`.
