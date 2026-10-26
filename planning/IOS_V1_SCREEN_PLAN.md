# KidsWordPlus — iOS V1 ekran ve davranış planı

Tarih: 9 Eylül 2026. Durum: geliştirme hazırlığı; çalışan uygulama değildir.

Bu belge `DECISIONS.md`, dil paketi sözleşmesi ve incelenen ekran panolarını birleştirir. **Kesin karar** kullanıcının talimatıdır. **Öneri** uygulamaya başlamayı kolaylaştıran çalışma varsayımıdır; kullanıcı tarafından kesinleştirilmiş sayılmaz. Tasarımda görünen sayılar ürün kuralı değildir.

## 1. Kesin ürün çerçevesi

- Mevcut KidsWordPlus projesinden bağımsız yeni proje. Önce Swift iOS V1; sonra aynı davranışlarla Kotlin Android.
- Arayüz dili TR/EN, öğrenme dili TR/EN; bağımsız seçilir. Sonraki dokuz öğrenme dili için yapı genişleyebilir.
- Kullanıcı öğrenme dilinin veritabanı, ses ve görsellerini tek paket olarak indirir. Kurulu paket çevrimdışı çalışır.
- Arayüz dilini değiştirmek paket indirmez; öğrenme dilini değiştirmek eski paketi veya ilerlemeyi silmez.
- Kelimelerin ana kaynağı `language-packs-v2/packages/{language}/database.json`.
- Türkçe 1.456 kayıt/1.453 etkin; İngilizce 1.450 kayıt. Ekrandaki sayılar veriden hesaplanır.
- Güncel paketlerde eksik görsel yok. Dosya bütünlüğü kontrolü yayın/editör onayı değildir. Paketler taslak; CDN adresleri henüz yok.

## 2. Önerilen V1 sınırı

İlk çalışan dilim: paket okuma → kategori/alt kategori → ders → kelime/görsel/ses → kalıcı ilerleme. Sonra kurulum, tam indirme yönetimi ve ebeveyn alanı eklenir.

V1 önerisi: tek yerel profil, karakter seçimi, kelime kartları, ders devamı, Dinle ve Seç ile temel tekrar, sade ilerleme raporu ve dil paketi yönetimi. Hesap/senkronizasyon, çoklu profil, mikrofonla telaffuz puanı, liderlik tablosu, karakter ekonomisi ve gelişmiş aralıklı tekrar sonraki sürüm adaylarıdır; mevcut ekranları silinmez.

Önerilen ana menü: **Ana Sayfa · Öğren · Oyunlar · Profil**. Ebeveyn ayarları Profil içinden açılır. İlk teknik dilimde yalnız çalışan rotalar gösterilir. Ekranlarda görülen 3/5 sekmeli menüler tek yapıya uyarlanır.

## 3. Ekran bazında geliştirme listesi

Referanslarda `temel` = `../design/ui-basic-learning/`, `faz` = `../design/ui-all-phases/`. Tüm yeni durumlar mevcut tasarımın renk, karakter ve kart dilini kullanır; ekran görüntüsü uygulama arayüzü olarak gömülmez.

| Kimlik / ekran | Görsel referansı | Davranış ve geçiş | Boş, geri ve hata durumu / kabul ölçütü |
|---|---|---|---|
| S01 Açılış | temel/01-splash.png | Yerel kurulum ve son seçili dil okunur; yeni kullanıcı S02, kurulumu bitiren S09'a gider. | Yapay bekleme yok; bozuk yerel kayıtta kurtarma seçeneği; internet yokken kurulu dil açılır. |
| S02 Arayüz dili | temel/02-welcome-language.png | Türkçe/English seçimi yalnız UI metinlerini değiştirir; Devam → S03. | Her iki UI dilinde aynı işlevler; geri dönüşte seçim korunur. |
| S03 Öğrenme dili | temel/03-target-language.png | TR/EN seçimi; seçili dilin indirme boyutu ve çevrimdışı kullanım açıklaması. Devam → S04. | UI diliyle aynı öğrenme dili seçilebilir. Sonraki dokuz taslak dil indirme seçeneği olmaz. |
| S04 Paket indirme/kurulum | faz/phase-4/02.png, yeniden düzenlenecek | Tam dil satırı: indiriliyor → kontrol ediliyor → hazır. İlk kurulumda hazır → S05; ayarlardan gelindiyse önceki ekran. | Bağlantı kesilmesi, iptal, yetersiz alan, hash/şema hatası ve yeniden deneme görünür. Doğrulanmayan paket ders açmaz; önceki sürüm korunur. |
| S05 Hitap adı | temel/04-name-entry.png | Yerel hitap adı; Devam → S06. Öneri: boş bırakma/Atla mümkün. | Klavye düğmeyi kapatmaz; yalnız boşluk adı kabul edilmez; varsayılan hitap UI diline uygun olur. |
| S06 Profil bilgileri | temel/05-profile-basics.png | Referansta doğum tarihi/cinsiyet var. Öneri: zorunlu değil; yaş grubu ve Atla ile sadeleştirme. → S07. | Pedagojik kullanım kararı olmadan yaşa göre içerik kilitlenmez. Alanlar kesinleşene kadar veri toplama uygulanmaz. |
| S07 Karakter seçimi | temel/06-character-selection.png | Var olan karakterlerden seçim; → S08. | Yalnız kullanılabilir karakter varlıkları; seçili işareti erişilebilir. Karakter seçimi kelime içeriğini değiştirmez. |
| S08 Kurulum özeti | temel/07-setup-complete.png | UI dili, öğrenme dili ve paket hazır bilgisi; Başla → S09. | “Her şey hazır” yalnız doğrulanmış paket varsa; geri dönüşte bilgiler korunur. |
| S09 Ana sayfa | temel/08-home.png | Seçili öğrenme dili, devam edilen ders, kategorilere erişim. Devam → S12, ilk kullanım → S10. | Yeni kullanıcıya sahte yıldız/ilerleme yok. Son ders tamamlandıysa yeni öneri; paket yoksa S04. |
| S10 Kategoriler/alt kategoriler | temel/09-categories.png + kategori varlıkları | Paket kategori kimlikleriyle liste; alt kategori seçimi → S11. | 15 kategori/alt kategori sayıları veriden hesaplanır. UI kategori adları TR/EN yerel sözlükten gelir; boş alt kategori gösterilmez. |
| S11 Ders listesi | temel/10-lessons.png | Alt kategori dersleri ve gerçek ilerleme; ders seç → S12. | Öneri: V1'de dersler açık, önerilen sıra belirtilir. Kilitler ancak karar verilirse uygulanır. Son ders/eksik paket durumları ele alınır. |
| S12 Kelime kartı | temel/11-word-learning.png | Hedef dil `displayText`, paket görseli ve Dinle; ileri/geri; son kart → S13. | Yeni kartta önceki ses durur. Tekrarlı Dinle sesleri üst üste bindirmez. Arka plana geçince ses durur. Hatalı medya için anlaşılır yeniden deneme; sahte başarı yok. |
| S13 Ders sonucu | temel/12-lesson-complete.png | Gerçek işlenen kart sayısı, tekrar veya sonraki ders; kategoriye dönüş. | Son derste “Sonraki ders” yerine kategori seçimi. Tekrar açmak aynı tamamlamayı iki kez saymaz. Kart görmek otomatik “öğrenildi” sayılmaz. |
| S14 Oyunlar | faz/phase-2/01.png | İlk öneri: Dinle ve Seç ve Tekrar girişleri; → S15/S16. | Hazır olmayan oyunlar çalışıyormuş gibi gösterilmez; uygun kelime yoksa ders önerilir. |
| S15 Dinle ve Seç | faz/phase-2/02.png | Hedef dil sesi, görsel seçenekler, cevap geri bildirimi ve oturum sonucu. | Ses metni cevabı ele vermemeli. Aynı görseli/sesi kullanan kavramlar birbirine yanlış seçenek olamaz. Yeterli farklı seçenek yoksa soru atlanır. |
| S16 Tekrar | faz/phase-2/03.png | Önceden çalışılan/yanlış cevaplanan kelimelerden kuyruk; kart veya S15. | Yeni kullanıcıda boş açıklama + derse git. Yüzdeler için tanımlanmamış “ustalık” puanı kullanılmaz. |
| S17 Profil | temel/08-home.png profil girişi + faz/phase-3/04.png | Ad/karakter özeti; ebeveyn alanına S18. | Profil değişikliği ilerlemeyi sıfırlamaz. Tek yerel profil önerisi. |
| S18 Ebeveyn kapısı | faz/phase-3/01.png | Ayarlar/rapor/indirme yönetimi için yetişkin etkileşimi. | Referanstaki 3 saniye basılı tutma yalnız taslaktır; yeterliliği varsayılmaz. Nihai yöntem seçilmeli; iptal → çocuk ekranı, arka plandan dönüşte kapanır. |
| S19 Ebeveyn özeti/rapor | faz/phase-3/02.png, 03.png | Çalışılan farklı kelime, tamamlanan ders, doğru cevap ve çalışma günleri. | Boş rapor 0 gösterir; yerel veriye dayanır. Dillerin ilerlemesi ayrı filtrelenir; temelsiz başarı yüzdesi yok. |
| S20 Ayarlar | faz/phase-3/04.png | UI dili, öğrenme dili, ses/animasyon tercihleri; indirilen diller → S21. | UI değişimi ilerlemeyi korur. Öğrenme dili değişimi doğrulanmış kuruluma bağlı. Bildirim kapsamı açık karardır. |
| S21 İndirilen diller | faz/phase-4/01.png, 02.png, yeniden düzenlenecek | Tam dil paketleri; kurulu/indirme/güncelleme, gerçek boyut, açık silme eylemi. | Dil silmek ilerlemeyi silmez. Etkin paket silinirse ders durur, dil seçimine dönülür; diğer paket dosyaları korunur. |

## 4. İlk kullanım ve geri dönüş sözleşmesi

S01 → S02 → S03 → S04 → S05 → S06 → S07 → S08 → S09 → S10 → S11 → S12 → S13.

Bu sıra **öneridir**: indirme ilk ders öncesi zorunludur. Kurulum kapanırsa son tamamlanan adım ve indirme durumu korunur. Kurulu paketle sonraki açılış S09'a gider. İlk açılışta çevrimdışı ve paket yoksa bağlantı açıklaması/yeniden deneme gösterilir.

Önceden kararlaştırılan küçük başlangıç içeriğinin tam içeriği henüz tanımlı değil. İlk teknik dilimde yerel geliştirme paketiyle test yapılır; bu, son kullanıcı için başlangıç içeriği tasarımının yerine geçmez. Demo içeriğin sınırı ilk kullanım akışı tamamlanmadan seçilmelidir.

## 5. Veri ve öğrenme davranışı

- Dersler kategori + alt kategori + ders numarasıyla gruplanır; yalnız `lessonNumber` tüm paket için benzersiz sayılmaz. Türkçe alfabede `curricula.alphabet.lessonConceptIds` sırası önceliklidir.
- `learningEnabled=false` kayıtlar ders/soru/ilerleme paydasından çıkarılır. 10 kart sabit varsayılmaz; Türkçe son alfabe dersi 9 karttır.
- İlerleme `(learningLanguage, conceptId)` anahtarıyla cihazda kalır. Aynı sesi paylaşan ülke/bayrak kavramlarının ilerlemesi kimlik bazında ayrıdır.
- Öneri: karttan ileri geçmek “çalışıldı”; bir dersin bütün etkin kartlarının çalışılması “ders tamamlandı”. Dinlemek veya kart görmek “öğrenildi” ispatı sayılmaz.
- Her kart geçişinde kalıcı kayıt; kapanıp açılınca son konum. Ders sonucu tekilleştirilir. Paket güncellenince eşleşen kavramların ilerlemesi korunur; kaldırılan kimliklerin geçmişi sessizce başka kelimeye taşınmaz.
- Önerilen Dinle ve Seç: ilk yanıtın doğruluğu kaydedilir; yanlış seçimde nazik geri bildirim ve yeniden deneme; sonraki doğru cevap ilk hatayı silmez. Can/süre cezası önerilmez. Yıldız, öğrenildi eşiği ve zamanlanmış tekrar kuralı açık karar olarak kalır.
- UI metinleri görsellere gömülmez; TR/EN kaynaklardan okunur. Hedef kelimeye zorunlu büyük harf dönüşümü uygulanmaz (Türkçe I/İ). Büyük yazı ayarı, VoiceOver etiketleri, ses tekrar düğmesi ve hareket azaltma dikkate alınır.

### İki dilli kelime kartında açık bağımlılık

Referans S12 “ELEPHANT / Fil” gösteriyor. Mevcut İngilizce paket yalnız İngilizce, Türkçe paket yalnız Türkçe metin taşır. Arayüzün Türkçe olması tek başına “Fil” çevirisini sağlayamaz.

İlk çalışan dilim hedef kelime + görsel + ses kullanır. İkinci satır gerekiyorsa öneri: seçilen açıklama diline ait küçük, sürümlü **metin sözlüğü** ayrıca indirilir; tüm ses/görsellerin indirilmesi gerekmez. Bu yeni bir içerik sözleşmesi kararıdır, henüz uygulanmış değildir. Eksik çeviri sessizce uydurulmaz; ikinci satır gösterilmez. UI dili ile açıklama dilinin ilişkisi ayrıca seçilmelidir.

## 6. Geliştirmeden önce görünür tasarım düzeltmeleri

1. Faz 4 kategori indirmeleri yerine Türkçe/İngilizce tam paket satırları; boyutlar katalogdan.
2. Alt menü bütün ekranlarda aynı olmalı; arkadaşlar/puan ekonomisi kapsam kararı bekler.
3. Ders kartında alt kategori seçimi ve veriyle uyumlu ders adları/sayıları.
4. Kelime kartında gerçek paket görselleri; çizgi karakter örnekleri tüm kelime görsellerinin stil garantisi değildir.
5. “10 kelime öğrendin” yerine ölçülen davranışa uygun metin; son ders ve tekrar sonucu ayrı.
6. İndirme, boş ekran, kesinti ve geri dönüş durumlarının tasarımı; mevcut panolarda bunlar yok.
7. Profilde doğum tarihi/cinsiyet ve fotoğraf alanlarının gerekliliği; var oldukları için zorunlu kılınmaz.

## 7. Açık kararlar ve hangi işi etkiledikleri

| Karar | Çalışma önerisi | Karar bekleyen aşama |
|---|---|---|
| SwiftUI mi UIKit mi? | Yeni proje için SwiftUI | Proje iskeleti |
| En düşük iOS / iPhone-iPad / yön | iPhone dikey ilk hedef; iPad ayrı uyarlama | Proje hedef ayarları ve ekran ölçüleri |
| İkinci dil çeviri satırı | Ayrı küçük açıklama sözlüğü | S12 nihai görünümü; ilk hedef dil kartını engellemez |
| Başlangıç/demo içerik | Her TR/EN dili için küçük tanıtım dersi | Çevrimdışı ilk açılış ve kurulum boyutu |
| V1 oyun/rapor sınırı | S14–S16 temel tekrar, S19 sade rapor | İlk çalışan dilimden sonra |
| Öğrenildi/yıldız/kilit/tekrar | İlk dilimde yalnız çalışıldı ve tamamlandı; dersler açık | Oyun ve ödül sistemi |
| Profil alanları / yaş hedefi | Hitap adı + karakter; diğer alanlar isteğe bağlı | Kurulum akışı |
| Ebeveyn kapısı | Ayrı yetişkin etkileşimi; yöntemi seçilecek | Ebeveyn alanı |
| Hesap, ödeme, reklam, bildirim | İlk teknik dilimde yok; ticari V1 kararı açık | Yayın kapsamı |
| CDN / paket yayın onayı | Yerel test kaynağı; üretimde HTTPS yayımlanmış paket | Gerçek indirme ve yayın |

## 8. Sıralı uygulama işleri ve kabul ölçütleri

| İş | Teslim | Tamamlanma ölçütü |
|---|---|---|
| W01 Yeni bağımsız Swift iskeleti | Uygulama, TR/EN UI altyapısı, paket okuyucu sınırı | Eski uygulama değişmez; yerel TR/EN paketleri çözülür; taslak kabulü yalnız geliştirme yapılandırmasında |
| W02 İlk kelime akışı | S10/S11/S12/S13 | İki dilde kategori → ders → gerçek görsel/ses; alfabe sırası ve etkin kayıt süzme doğru |
| W03 Kalıcı devam | S09 ve ilerleme kaydı | Uygulama kapanınca son konum korunur; UI değişimi aynı ilerlemeyi gösterir; öğrenme dili ilerlemeleri ayrıdır |
| W04 Paket yönetimi | S04/S21 ve katalog servisi | Başarılı kurulum, kesinti/yeniden deneme, iptal, az alan, bozuk ZIP/hash, uyumsuz şema ve başarısız güncellemede eski sürüm korunması test edilir |
| W05 Kurulum ve görsel uyarlama | S01–S08/S17/S20 | TR/EN tüm kurulum; geri dönüş ve yeniden açılış; farklı ekran boyutu ve büyük yazı kontrolü |
| W06 Temel oyun ve tekrar | S14–S16 | Tek anlamlı seçenekler, ilk yanıt kaydı, boş kuyruk ve tekrar davranışı |
| W07 Ebeveyn ve son kalite | S18/S19 | Geçiş kapısı, ölçülebilir rapor, çevrimdışı uçtan uca test; açık yayın kararları tamamlanır |

W01–W03 önce tamamlanır. CDN veya puanlama kararı beklemek bu yerel geliştirme dilimini durdurmaz. Üretim paket kontrolü geliştirme kolaylığı için gevşetilmez.

## 9. Küçük işler için çalışma düzeni

Her geliştirme işi tek davranış ve kabul ölçütüyle yürütülür. Başlangıçta yalnız bu belgenin ilgili ekranları, `DECISIONS.md` ve gerekiyorsa paket sözleşmesi okunur. Binlerce medya dosyası veya tüm veritabanı konuşmaya dökülmez; örnek kayıt + sayım kullanılır. Sonuç kaydı değişen dosyalar, yapılan kontrol ve kalan sorunlarla sınırlıdır. Hedeflenen kontrol geçince aynı test gerekçesiz tekrarlanmaz.

Bir sonraki somut iş W01–W02: yeni Swift projesinde yerel Türkçe/İngilizce paketten bir dersin kelimelerini görseli ve sesiyle açmak. Bu belge ekran planını tamamlar; uygulama oluşturma ayrı uygulama adımıdır.

## 10 Eylül 2026 — güncel uygulama durumu

W01–W03 çalışan Swift uygulamasında mevcut. W04 için yerel ve HTTPS katalog/indirme/sürdürme/ZIP doğrulama/atomik kurulum tamamlandı; otomatik kontroller geçti. Gerçek CDN adresi henüz yok, yayın ve cihaz kabulü açık. Uygulama tarafında sonraki iş W05 kurulum ve görsel uyarlama. Dağıtım ayrıntıları `../KidsWordPlusV2/PACKAGE_DISTRIBUTION.md`, doğrulama kaydı `../KidsWordPlusV2/Tests/verification.json`.

### W05 ilk uygulama

Altı adımlı ilk açılış, cihazda kalıcı profil, Piko/Max seçimi ve ana sayfadan profil düzenleme eklendi. Yaş/cinsiyet alanları planın önerisi doğrultusunda alınmıyor. Durum/paket geçiş kontrolleri geçti. Karşılama ekranı simülatörde incelendi. Sırada tüm akışın dokunarak kabulü, küçük ekran/büyük yazı uyarlaması ve ana sayfa/kategori ekranlarının görsel uyarlaması var.

### W05 kurulum kabulü — 10 Eylül

İngilizce UI/Türkçe öğrenme ve Türkçe UI/İngilizce öğrenme için XCTest ekran senaryoları eklendi. iPhone SE küçük ekran, en büyük erişilebilirlik yazısı ve koyu görünüm kontrolü; ad, karakter, geri dönüş, paket kapısı ve yeniden açılış davranışları kapsanır. Ayrıntı ve kalan manuel cihaz kontrolleri `../KidsWordPlusV2/Tests/UI_ACCEPTANCE.md`. Sonraki geliştirme: ana sayfa/kategori görsel uyarlaması; W05 bütünü henüz kapatılmadı.

### W05 ana sayfa ve kategori — 10 Eylül

Karakterli ana sayfa, mor ders önerisi, gerçek sayımlı kategori ızgarası ve Ana Sayfa/Öğren/Profil sekmeleri uygulandı. Ayarlar bu çalışan akışa bağlıdır. Kategori görselleri kurulu paketlerden alınır. Sonraki iş alt kategori/ders listesi ve kelime ekranının görsel uyarlamasıdır. Kalan manuel cihaz kabulleri `Tests/UI_ACCEPTANCE.md` içinde izlenir.
