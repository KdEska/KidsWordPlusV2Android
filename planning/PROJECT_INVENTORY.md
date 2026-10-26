# KidsWordPlus — Geliştirme öncesi envanter

10 Eylül: iOS yerel dil paketi kurulum/yönetim adımı eklendi. Dosya hash/boyut doğrulaması, güvenli sürüm geçişi, iptal ve ilerlemeyi koruyan silme kontrolleri geçti. .DS_Store manifest/ZIP dışına alındı; güncel TR sürümü `10-tr-installable-content-draft`, EN `4-en-installable-content-draft`. HTTPS/ZIP indirme ve CDN bağlantısı hâlâ bekliyor; W04 kısmen tamamlandı.

9 Eylül iOS başlangıcı: `KidsWordPlusV2/` altında bağımsız SwiftUI proje iskeleti oluşturuldu. Gerçek TR/EN paketinden kategori/ders/kelime/görsel/ses ve yerel ilerleme akışı uygulandı. Debug ve Release simülatör derlemeleri ile gerçek paket çekirdek kontrolleri geçti. Release test paketlerini gömmez. Simülatör etkileşim ve gerçek cihaz ses incelemesi henüz yapılmadı; CDN indirme ve nihai tasarım sonraki işlerdir. Çalışma varsayımları iOS 17+, iPhone/dikey; kesin ürün kararı değildir.

9 Eylül UI/UX hazırlığı: 12 temel ekran ve faz 2–5 panoları incelendi. 21 ekranın referans, geçiş, hata durumu ve kabul ölçütleri `planning/IOS_V1_SCREEN_PLAN.md` içinde toplandı. Kesin kararlar ile önerilen V1 kapsamı ayrıldı; ikinci dil çeviri satırı, menü tutarlılığı ve tam dil indirme uyarlaması belgelendi. Sıradaki uygulama işi yeni Swift projesinde paket → ders → kelime/görsel/ses → kalıcı devam akışıdır.

9 Eylül görsel tamamlama: Eksik 62 benzersiz görsel tamamlanarak hem Türkçe hem İngilizce paketine eklendi (124 paket dosyası). Her iki dilde eksik görsel yolu 0. Türkçe sürüm `9-tr-complete-images-draft`, İngilizce sürüm `3-en-complete-images-draft`. Türkçe 1.456 kayıt / 1.453 etkin kayıt, İngilizce 1.450 kayıt; görsel güncellemesinde sesler ve kelime metinleri korunmuştur. Yeni görseller asistan tarafından kavram eşleşmesi açısından incelendi; eski arşivin tam görsel incelemesi ve yayın onayı ayrı işlerdir. İnceleme sayfası: `language-packs-v2/production/generated-images/review.html`; doğrulama: aynı klasörde `verification.json`. Aşağıdaki eski sayılar tarihsel durumları anlatır.

9 Eylül görsel aktarımı: Türkçeye 1.363, İngilizceye 1.386 arşiv görseli eklendi. Her iki dilde aynı 62 benzersiz görsel yolu eksik. Eksik listesi `language-packs-v2/reports/image-import/missing-62.md`; görsel galerisi aynı klasörde `gallery.html`. Önceki görsel yokluğu ifadeleri tarihsel durumdur. Dosyalar açılarak doğrulandı; tam anlamsal görsel incelemesi bekliyor.

9 Eylül: Kalan 31 Türkçe ses kullanıcı tarafından dinlenip onaylandı ve pakete aktarıldı. Önceden onaylanan beş telaffuz düzeltmesi korundu. Güncel Türkçe sürüm `7-tr-remaining31-approved-draft`; eski sesler yedeklendi. Bu 31 sesin üretim/dinleme işi tamamlandı, tüm paketin yayın onayı değildir.

8 Eylül ses düzeltmesi: 20 yeni Türkçe ses üretildi. Otomatik metin eşleşmesi olan 13 ses / 17 kavram Türkçe taslak pakete aktarıldı (3-tr-audio-corrections-draft). 7 yeni ses dinleme bekliyor; 31 ses tarayıcının yönetici politikası doğrulanamadığı için henüz üretilemedi. Eski sesler korundu. Üretim ve devam listesi: `language-packs-v2/production/tr-editorial-corrections/README.md`. Hiçbiri insan dinleme onayı almış sayılmaz.

8 Eylül ses kontrolü: İnceleme kuyruğundaki 225 kayıt / 208 farklı dosya yerel konuşma tanıma ile incelendi. 31 kayıt metinle eşleşti; 63 kayıtta eski etiket ve döküm aynı farklı ifadeyi destekledi. 102 kayıt belirsiz/farklı döküm nedeniyle, 29 harf ise ayrıca dinleme gerektiriyor. Bunlar otomatik bulgulardır; kesin hata veya insan dinleme onayı değildir. Paket sesleri değiştirilmedi. Sonuçlar ve ses oynatıcıları: `language-packs-v2/reports/tr-audio-audit/review.html`; ayrıntı: aynı klasördeki `README.md` ve `comparison.json`.

8 Eylül güncellemesi: Türkçede 196 boş metin tamamlandı; 48 saat ifadesi düzeltildi. 29 harflik alfabe için altı dile özel kayıt eklendi ve I/İ ayrıldı. Türkçe paket artık 1.456 kayıt (1.453 etkin), 1.256 MP3 ve 29 alfabe görseli içerir. Etkin Türkçe metin alanlarında boşluk yoktur. Ses–metin dinleme kuyruğu `language-packs-v2/reports/tr-editorial/README.md` içindedir. Aşağıdaki 1.450 kayıt/196 boş metin sayıları düzenleme öncesi envanterdir; güncel durumu bu paragraf ve paket raporları belirtir.

Bu envanter çalışma klasöründeki mevcut dosyaların kontrolüne dayanır. Ses dosyasının bulunması, kelime metninin, görselinin ve ders kuralının da tamam olduğu anlamına gelmez. Önceki “etkin kelimelerde eksik yok” sonucu yalnızca ses dosyası eşleşmeleri için geçerlidir.

## 1. Kelime veritabanı

Ana çalışma kaynakları `language-packs-v2/packages/tr/database.json` ve `language-packs-v2/packages/en/database.json` dosyalarıdır. Her biri 1.450 kayıt, 15 kategori ve kategoriyle birlikte sayılan 96 alt kategori içerir. Kavram kimlikleri diller arasında ortak; metin, ses ve görsel bağlantıları dil paketinin içindedir. Eski `database-output/` ve `localization-output/` dosyaları tarihsel/ilk aktarım kaynaklarıdır; paket düzenlemelerinin üzerine yazılmamalıdır.

| Kontrol | İngilizce | Türkçe |
|---|---:|---:|
| Toplam kayıt | 1.450 | 1.450 |
| Dolu görünen kelime metni | 1.450 | 1.254 |
| Boş görünen kelime metni | 0 | 196 |
| Boş seslendirme metni alanı | 0 | 222 |
| Pasif kayıt | 0 | 3 |

Türkçede boş metinler: 173 ev/eşya kaydı ve 23 yapı/anıt kaydı. Ek 26 boş seslendirme alanı, henüz Türkçe müfredata uyarlanmamış alfabe kayıtlarıdır. Q/W/X bunların içinde pasiftir. Türkçe saat ifadeleri dilbilgisi incelemesi beklemektedir. Dolu metinlerin tamamı yayın onayı almış sayılmaz.

Diğer dokuz dilin paket iskeletleri ve çeviri taslakları da vardır; yayın için hazır değildir. Bütün paketler `releaseReady=false` durumundadır.

## 2. Kelimelere ait sesler

| Paket | Fiziksel MP3 | Kapsanan kayıt | Etkin kayıtta eksik dosya |
|---|---:|---:|---:|
| İngilizce | 1.253 | 1.450 | 0 |
| Türkçe | 1.250 | 1.447 | 0 |

195 ülke/bayrak çifti ve ev kategorisindeki iki tekrar ortak ses kullanır. Türkçede Q/W/X için dosya yoktur; bu üç kayıt pasiftir. Metin alanı boş olan bazı Türkçe kayıtlarda eski arşivden gelen ses dosyası mevcuttur; bu, metin düzenlemesinin tamamlandığını göstermez.

Yeni eklenenler: Türkçe 62, İngilizce 60 dosya bağlantısı. Türkçe sesler Türkçe orijinal referanstan; İngilizce sesler İngilizce orijinal referanstan üretilmiştir. Kullanıcının reddettiği Türkçe klonla yapılmış İngilizce denemeler paketlerin dışındadır. Teknik dosya ve arşiv kontrolleri geçti; İngilizce son dinleme/telaffuz incelemesi bekliyor.

Takip dosyaları: `language-packs-v2/reports/added_audio.md`, `reports/completion.json`, `reports/audio_record_reconciliation.json` ve `production/voice_profiles.json`.

## 3. UI/UX akışları

`design/ui-basic-learning/` içindeki 12 ekran referansı temel yolculuğu gösterir: açılış → karşılama/arayüz dili → öğrenme dili → isim → profil bilgileri → karakter seçimi → kurulum tamamlandı → ana sayfa → kategoriler → dersler → kelime öğrenme → ders tamamlandı.

`design/ui-all-phases/` altında beş faz için toplam 20 ayrı ekran referansı ve beş toplu pano vardır. Bunlar temel öğrenmenin yanında oyunlar/pekiştirme, ebeveyn deneyimi, indirilebilir içerik ve gelişmiş öğrenme taslaklarını içerir. Temel öğrenme setiyle kapsamları örtüşür; 32 farklı kesinleşmiş ürün ekranı olarak sayılmamalıdır.

Görüntüler tasarım referansıdır; çalışır prototip veya tüm durumları tanımlanmış akış belgesi değildir. Tıklama sonuçları, geri dönüşler, boş/hata durumları, indirme iptali/yeniden deneme, çevrimdışı davranış ve ekran başına kabul ölçütleri tek bir ekran sözleşmesinde henüz toplanmamıştır.

## 4. Referans ekran görüntüleri ve tasarım varlıkları

- `design/ui-basic-learning/`: 12 ekran + 3 toplu pano.
- `design/ui-all-phases/`: 20 ekran + 5 toplu pano.
- `design/main-category-assets-reference/` ve `design/subcategory-assets-reference/`: kategori ve alt kategori görsel referansları.
- `design/phase-2-3-assets/`: PNG/SVG arayüz varlıkları.
- `design/assets-phase-1-3/`: karakter/marka varlıkları.

Bunlar çalışan uygulamadan alınmış doğrulama ekranları değil, uygulamayı geliştirirken örnek alınacak tasarım çıktılarıdır. Pano üzerindeki puanlar, yüzdeler, kelime hedefleri ve indirme boyutları gerçek hesaplama kuralları veya ölçülmüş dosya boyutları olarak kullanılmamalıdır.

Faz 4 içerik merkezi taslağı kategori indirmeleri gösteriyor; son kararda V1 tam dil paketi indirecek. Bu ekran güncellenmelidir. Sonraki fazlarda görülen senkronizasyon, telaffuz puanlama ve çoklu profil gibi özellikler yalnızca görselde bulunmaları nedeniyle iOS V1 kapsamına alınmamalıdır.

Önemli görsel eksikliği: Türkçe ve İngilizce paketlerinin `images/` klasörlerinde şu anda dosya yok. Her pakette 1.448 benzersiz kelime görseli yolu çözümlenemiyor. Kategori ikonları veya ekran panoları kelime kartı görsellerinin yerine geçmez. Görseller başka arşivde bulunabilir; bu envanter onların yok olduğunu değil, paketlere aktarılmadığını doğrular.

## 5. Uygulamanın nasıl işleyeceğine ait bilgiler

`planning/DECISIONS.md` ürün kararlarını, `language-packs-v2/README.md` paket ve kurulum sözleşmesini içerir:

- Mevcut uygulamadan bağımsız yeni proje; önce Swift ile iOS, sonra Kotlin ile Android.
- Arayüz Türkçe/İngilizce; öğrenme dili bağımsızdır.
- Kullanıcı seçtiği dilin veritabanını, seslerini ve görsellerini birlikte indirir; doğrulanan paket çevrimdışı kullanılır.
- İndirme sürümü, boyutu ve bütünlük kontrolü; yarım indirme ve güvenli kurulum davranışları tanımlıdır.
- Dil değiştirmek eski paketi otomatik silmez; paket silmek öğrenme ilerlemesini silmez.
- İlerleme dil ve kavram kimliğiyle ilişkilendirilir.

Hâlâ tek bir uygulama davranış belgesinde kesinleştirilmesi gerekenler: iOS V1 ekran/özellik kapsamı, bir kelimenin ne zaman öğrenilmiş sayılacağı, ders geçişi ve kilit açma, yanlış cevap davranışı, tekrar aralıkları, yıldız/puan kuralları, ebeveyn kapısı, yerel profil ve gerekiyorsa hesap/senkronizasyon kapsamı. Bu çalışma klasöründe Swift/Kotlin uygulama kodu bulunmadı; mevcut sözleşme henüz uygulanmış ürün değildir.

## Öncelikli hazırlık sırası

1. Türkçe paket metinlerindeki 196 boşluğu ve saat/alfabe uyarlamalarını tamamlamak.
2. Kelime görsellerini bulup paket yollarına eşlemek; bulunamayanları üretim listesine almak.
3. İngilizce yeni seslerin son dinleme incelemesini yapmak.
4. iOS V1 kapsamını seçip ekran bazında davranış ve kabul ölçütlerini birleştirmek; dil indirme tasarımını güncellemek.
5. Bu kaynaklarla yeni Swift projesinde ilk çalışan akışı kurmak: dil paketi indirme → kategori → ders → kelime/ses → ilerleme kaydı.
