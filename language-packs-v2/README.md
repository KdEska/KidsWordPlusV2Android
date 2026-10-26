# KidsWordPlus V2 — İndirilebilir öğrenme dili paketleri

## Dağıtım yapısı — şema 2.3

Türkçe/İngilizce arayüz uygulamadadır; öğrenme dili bağımsız seçilir. İlk öğrenme diliyle **Temel dil paketi** (`base`) otomatik kurulur. Daha sonra eklenen diller aynı temel paketi kullanır. Uygulama bütün dillerin veritabanlarını ve seslerini birlikte indirmez.

```
packages/base/
  manifest.json
  images/*              # 1.422 ortak kelime görseli, tek kopya
packages/tr/             # en ve diğer diller de aynı yapıda
  manifest.json          # Temel paketin sürümü ve manifest SHA-256 bağımlılığı
  database.json
  audio/*.mp3            # Yalnızca bu dilin sesleri
  images/*               # Alfabe ve diğer dile özel görseller
  images/overrides/*
```

11 dilin veritabanı ve taslak arşivi ayrı kalır. `base` bir öğrenme dili değildir ve dil seçiminde gösterilmez. Şu an uygulamada TR/EN kurulabilir; diğer dokuz dilin çeviri, ses ve alfabe hazırlığı bitmeden yayımlanması engellenir. Ortak `conceptId` kavramları eşler; kelime kimlikleri, metinler, sesler ve ders ilerlemesi bu geçişte değiştirilmedi.

## Görsel çözümleme ve çevrimdışı kullanım

Ortak görsel kaydında `image.package = "base"` vardır; `image.overridePath ?? image.path` temel paket köküne göre açılır. Dile özel görsellerde `image.package` alanı yoktur; aynı yol dilin kendi kökünde açılır. Alfabe görselleri dile özeldir. Sesler ve veritabanı her zaman dil paketindedir. Paketler arası `../` dosya yolları kullanılmaz.

Dil manifesti ve katalog kaydı aynı `dependencies` listesini taşır:

```json
{"dependencies":[{"language":"base","version":"<temel-paket-sürümü>","manifestSha256":"<64-haneli-hash>"}]}
```

Eksik temel paket önce indirilir ve doğrulanır; ardından dil paketi etkinleştirilir. Dil kurulum makbuzu kullanılan temel paket revizyonunu sabitler. Sonradan yeni temel sürüm kurulsa da eski dillerin kullandığı revizyon silinmez. Bir dili silmek temel paketi veya diğer dilin verilerini silmez. Başarısız/iptal edilmiş kurulum öğrenme ilerlemesini değiştirmez; temel paket zaten tamamlandıysa yeniden denemede kullanılır.

Eski şema 2.2 bağımsız paketleri okunmaya devam eder. Kullanıcı bir dili yeni sürüme güncellediğinde o dildeki ortak görsel kopyaları eski revizyonla birlikte kaldırılır. Tüm gerekli paketler kurulduktan sonra çevrimdışı kullanım mümkündür. Kullanılmayan eski temel sürümlerin otomatik temizliği henüz yapılmaz.

## İndirme ve kurulum sözleşmesi

1. Katalogda `availability=published` olan sürüm ve HTTPS adresi seçilir. Taslaklar kullanıcıya indirme seçeneği olarak sunulmaz.
2. Yeterli boş alan kontrol edilir: ZIP + açılmış paket + varsa eski sürümün geçici korunması hesaba katılır.
3. ZIP geçici alana indirilir. Sunucu destekliyorsa Range ile yarım indirme sürdürülür; kısmi dosya arşiv SHA-256 kimliğiyle saklanır.
4. Katalogdaki `archiveSha256` ve `downloadBytes` doğrulanır. Güvenilir HTTPS katalog kaynağı gerekir; hash tek başına kaynak doğruluğu sağlamaz.
5. Güvenli açma yapılır: mutlak yollar, `..`, sembolik bağlantılar ve boyut sınırını aşan içerik reddedilir. Manifest hash'i ve manifestteki her dosyanın SHA-256/boyutu doğrulanır.
6. Şema uyumluluğu, paket dili, kayıt kimlikleri ve tüm zorunlu medya referansları doğrulanır. `releaseReady=false` paket üretim uygulamasına kurulmaz.
7. Doğrulanan sürüm atomik olarak etkinleştirilir. İşlem başarısızsa eski çalışan sürüm korunur. Sonra geçici indirme temizlenir.
8. Kullanıcı dili değiştirince yalnızca eksik dil indirilir. Kurulu diller çevrimdışı açılır. Dil silme açık kullanıcı eylemidir; öğrenme ilerlemesi silinmez.

İlerleme anahtarı `(learningLanguage, conceptId)` olmalıdır. UI dili öğrenme ilerlemesini veya indirilen dili değiştirmez. Yerel paket kayıt tablosu `packageId`, `version`, `state`, `installedPath`, `installedBytes`, `verifiedAt` alanlarını tutar. Durumlar: `notInstalled`, `downloading`, `verifying`, `installed`, `failed`, `updateAvailable`.

V1 dağıtım birimi tam dil ZIP paketidir. Güncellemede yeni tam paket indirilir. Dosya hash'leri sonraki aşamada fark indirmesine imkan verir; mevcut çıktıda fark indirme veya kategori indirmesi uygulanmış değildir.

## Dokuz dil için içerik güncellemesi

Almanca, Fransızca, İspanyolca, İtalyanca, Portekizce, Çince, Japonca, Korece ve Hintçe paketlerindeki eksik metinler tamamlandı; 481 yazı kartı için 480 şeffaf PNG eklendi. Metinler ve alfabe görselleri kullanıcı tarafından onaylandı. Sesler eksik olduğu için bu paketler taslaktır. Ayrıntılar ve inceleme sayfası: [içerik güncellemesi](production/nine-language-content/README.md), [görsel ve çeviri incelemesi](production/nine-language-content/review.html).

## Hazırlık durumu

Paketler mimari ve içerik taslağıdır, yayın değildir. Mevcut ses arşivinde bulunan ses ve resimler gerçekten kopyalanır; bulunamayanlar `reports/{language}.json` içinde listelenir. Henüz üretilmeyen sesler için boş/sahte MP3 oluşturulmaz. `downloadUrl=null` çünkü CDN yayını yapılmadı.

Eksik çeviriler, alfabenin dile uyarlanması ve saat ifadelerinin dilbilgisi kontrolü ayrıca raporlanır. Önceki çok dilli kaynak bir düzenleme girdisidir; bütün metinlerin doldurulmuş olması dilbilgisi onayı anlamına gelmez. Bu nedenle bütün paketler `releaseReady=false` olarak üretilir. Manifest medya bütünlüğünü, editör kontrolü ise dil doğruluğunu sağlar.

## Ana çalışma kaynağı

8 Eylül Türkçe içerik güncellemesi: 1.450 ortak temel kayda Ç, Ğ, İ, Ö, Ş, Ü için altı dile özel kavram eklendi. Türkçe kayıt sayısı 1.456, etkin kayıt sayısı 1.453'tür. İngilizce 1.450 kayıtta kaldı. Ortak kavramlar aynı kimliklerle korunur; bütün dillerin aynı toplam kayıt sayısına sahip olması artık doğrulama koşulu değildir. Dile özel kayıtlar `conceptScope=language_specific` ile ayrılır.

Türkçe alfabe dersleri için `curricula.alphabet.lessonConceptIds` üç dersin kesin sırasını verir (10+10+9 harf); eski genel okul `lessonNumber` alanına göre alfabetik sıra çıkarılmamalıdır. `alphabetPosition` 29 harfin sırasıdır, `spokenText` harf adıdır. I, `alphabets_ii_tr.mp3`; İ, `alphabets_i_tr.mp3` dosyasını kullanır. 29 özgün harf sesi ve görseli pakete aktarılmıştır. Yeni Türkçe içerik sürümü `2-tr-editorial-draft`tır; katalog ilgili güncel ZIP'i gösterir.

Kullanıcının son kararıyla bütün kelime ve ses düzenlemeleri artık `packages/{language}/database.json` ve aynı klasördeki medya dosyaları üzerinden yapılır. Eski birleşik veritabanı yalnızca ilk oluşturma girdisidir. `tools/build_language_packs.py` mevcut paket veritabanlarının üzerine eski kaynağı yazmaz; dosya durumlarını, manifestleri, raporları ve ZIP dosyalarını yeniler.

Ses üretim kuyruğu `production/{language}_queue.json`, eklenen ses listesi `reports/added_audio.md` dosyasındadır. Aynı metin bir kez üretilip birden fazla kavram kaydının gerekli dosya adlarına kopyalanabilir. Sesin teknik olarak açılabilmesi telaffuz veya konuşmacı benzerliği onayı değildir; yeni kayıtlarda kalite inceleme durumu ayrı tutulur.

Referanslar dile özeldir: Türkçe üretim Türkçe orijinal kayıtların klonunu; İngilizce üretim İngilizce orijinal kayıtların klonunu kullanır. Kimlikler ve kaynaklar `production/voice_profiles.json` içindedir. `production/rejected-english-turkish-clone/` altındaki yanlış referanslı üretimler paketlere alınmaz. Son ses durumu için eski `audio-output` raporları yerine buradaki paket raporları kullanılmalıdır.

`learningEnabled` alanı yoksa kayıt varsayılan olarak etkindir; `false` ise derslerde gösterilmez. Türkçe alfabe dışındaki Q/W/X kayıtları kimlik sürekliliği için korunur ancak pasiftir (`disabledReason=not_in_turkish_alphabet`, `audio.required=false`). Eksik dosya raporu bunları saymaya devam eder; bu sayı ile etkin derslerin zorunlu eksik ses sayısı birbirine karıştırılmamalıdır. Türkçe alfabenin genel uyarlaması ayrı bir editör incelemesi olarak beklemektedir.

Swift uygulamasında temel paket kurulumu ve ortak görsel okuma uygulanmıştır. CDN henüz kurulmadı; arşivler taslaktır. Kotlin sürümü aynı şema ve bağımlılık sözleşmesini uygulamalıdır.

## Paketleri yeniden oluşturma

`python3 tools/split_shared_package.py` ya da `python3 tools/build_language_packs.py` mevcut şema 2.3 paketlerini yeniden envanterler, hash ve arşivleri günceller. `tools/validate_shared_packages.py` bütün katalog/arşiv/manifest/dosya bağlarını ve geçişte kelime/ses/görsel korunmasını kontrol eder. İlk geçişin kaynak metadata yedeği `production/shared-package-migration/original-metadata/` altındadır. Eski bağımsız arşivler yerel çalışma alanında korunur; dağıtımda yalnız güncel katalogdaki arşivler kullanılır.
