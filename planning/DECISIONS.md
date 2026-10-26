# KidsWordPlus — Kesinleşen geliştirme kararları

Güncelleme: 7 Eylül 2026. Kaynak: kullanıcının doğrudan talimatları.

- Mevcut uygulama üzerinde geliştirme yapılmayacak; yeni ve bağımsız bir proje sıfırdan oluşturulacak.
- UI (arayüz) dilleri: Türkçe ve İngilizce.
- Öğrenilen dil seçenekleri: Türkçe ve İngilizce.
- Arayüz dili ile öğrenilen dil ayrı kavramlar olarak modellenmeli; birinin diğerini otomatik belirlediği varsayılmamalı.
- Önce iOS V1 geliştirilecek. Programlama dili Swift.
- iOS V1 sonrasında aynı sürümün Android karşılığı geliştirilecek. Programlama dili Kotlin.
- Platformlar için ayrı native uygulamalar geliştirilecek. Ortak ürün akışları, içerik şeması ve davranış tanımları iki uygulamaya referans olacak.
- SwiftUI/UIKit veya Compose/Views seçimi henüz kesinleştirilmedi.
- Sonraki aşamalarda öğrenme içeriğine eklenecek diller: Almanca, Fransızca, İspanyolca, Mandarin Çincesi, Japonca, Korece, İtalyanca, Portekizce ve Hintçe (Hindi).
- Bu dokuz dil için V2 kavramlarının yerelleştirilmesi ve aynı kadın ses karakteri, enerjik çocuk dostu anlatım ve normal hız hedefiyle seslendirme isteniyor. Bu dillerin UI dili olarak eklenmesi henüz belirtilmedi.
- İspanyolca ve Portekizce bölgesel varyantları ile Çince yazı sistemi henüz kesinleştirilmedi.

Planlama etkisi: Mevcut İngilizce kelime verisinin yanında Türkçe öğrenme içeriği ve ilgili ses/görsel eşleştirmeleri de hazırlanıp doğrulanmalı. UI çevirileri öğrenme içeriğinden ayrı tutulmalı.

## Ses içeriğinin dağıtımı — kullanıcı tarafından onaylandı

- Toplam hedef 11 öğrenme dili. Bütün dillerin sesleri uygulama kurulumuna gömülmeyecek.
- Uygulama ilk dersi denemeye yetecek küçük bir başlangıç ses içeriğiyle kurulacak.
- Kullanıcının seçtiği öğrenme dilinin sesleri indirilebilir dil paketi olarak sunulacak; indirilen içerik çevrimdışı kullanılabilecek.
- Arayüz dili tek başına ses indirmesini tetiklemeyecek. Ders akışının gerektirdiği dillerin sesleri indirilecek.
- 7 Eylül güncellemesi: Kullanıcı seçtiği dilin veritabanı, sesleri ve gerekli görsellerini birlikte tek dil paketi olarak indirecek. V1 dağıtım birimi tam dil ZIP paketidir; kategori bazlı indirme sonraki optimizasyondur.
- Dil değişiminde yeni paket indirilecek; önceki paket otomatik silinmeyecek.
- Ayarlar içindeki İndirilen Diller ekranında paket boyutları gösterilecek ve kullanıcı paketleri silebilecek.
- iOS ve Android aynı sunucudaki ortak ses dosyalarını ve paket tanımlarını kullanacak.
- Paketlerde sürüm, boyut ve bütünlük kontrolü bulunacak. Yarım kalan indirme devam edebilecek; doğrulanmamış paket kullanıma alınmayacak.
- V1 güncellemesinde tam dil paketi indirilir. Yalnız değişen dosyaların indirilmesi sonraki optimizasyondur. Dil bağımsız görseller paketlere dahil edilir; cihazda hash ile ortak saklama uygulanabilir.
- Bu yapı Swift ile iOS V1'den itibaren kurulacak; Kotlin Android sürümü aynı içerik sözleşmesini kullanacak.
- Sunucu/CDN sağlayıcısı ve kesin ses kodlama ayarları henüz seçilmedi. Önceki boyut hesapları tahmindir, kesin paket boyutu değildir.

## Çeviri yöntemi

- Kullanıcı Google Translate kullanılmasını istemiyor. Kelime çevirilerini asistan doğrudan kategori ve anlam bağlamıyla hazırlayacak.
- Google Translate denemelerinden alınan metinler nihai dil veritabanına dahil edilmeyecek.
- Belirsiz kaynak kavramları ve dile özgü alfabe/saat uyarlamaları ayrıca işaretlenecek.

## Her dil için bağımsız V2 veritabanı

- İçerik sözleşmesi ve paketler `language-packs-v2/` altında oluşturuldu.
- Uygulamada tüm dillerin birleşik veritabanı bulunmayacak. Her paket yalnızca kendi dilinin metnini taşıyacak.
- Ortak kavram kimlikleri korunacak; kullanıcı ilerlemesi öğrenme dili ve kavram kimliğiyle tutulacak.
- Yayına hazır olmayan paketler katalogda `draft` olarak kalacak ve üretim uygulamasına kurulmayacak.
- Son kullanıcı kararı: bütün kelimeler artık `language-packs-v2/packages/{language}/database.json` üzerinden takip edilecek; aynı paketin `audio/` klasörü seslerin ana konumu olacak. Eski birleşik DB yalnızca tarihsel/ilk aktarım kaynağıdır.
- Ses düzeltmesi: İngilizce eksikler kullanıcı tarafından verilen İngilizce kayıtların ses karakteriyle oluşturulacak. Türkçe referanstan türetilen klon İngilizce için kullanılmayacak. Kullanıcı Türkçe yeni sesleri uygun buldu. Yanlış klonla üretilen İngilizce kayıtlar `production/rejected-english-turkish-clone/` altında kullanım dışıdır.
- 8 Eylül uygulaması: Türkçe 29 harfli alfabe için altı dile özel kavram eklendi; ortak 1.450 kimlik korundu. Türkçe toplam 1.456 kayıt/1.453 etkin kayıt, İngilizce 1.450 kayıt. Alfabe sırası `curricula.alphabet` üzerinden okunur; I ve İ farklı dosyalardır. Arşivden bulunan kaynak veriler doğrulama girdisidir; ana çalışma kaynağı yine dil paketidir.
