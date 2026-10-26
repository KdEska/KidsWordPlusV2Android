# Türkçe ses–metin kontrolü

Yöntem: Her dosya bağımsız, yerel konuşma tanıma ile çözümlendi. Beklenen metin modele ipucu olarak verilmedi. Sonuçlar otomatik dökümdür; insan dinleme onayı değildir. Paket sesleri değiştirilmedi.

Kapsam: Önceki metin incelemesinde ayrılan 225 kayıt / 208 farklı ses dosyası. Bu çalışma tüm Türkçe paketin dinleme onayı değildir. Aynı ses dosyası birden fazla kavramda kullanılabilir.

| Durum | Kayıt | Farklı dosya |
|---|---:|---:|
| Eski etiket ve döküm farklı metni destekliyor | 63 | 58 |
| Döküm metinle eşleşiyor | 31 | 27 |
| Döküm belirsiz | 27 | 24 |
| Fark var: dinleme gerekli | 75 | 70 |
| Harf: dinleme gerekli | 29 | 29 |

## Eski etiket ve ses dökümünün birlikte desteklediği farklar

| Kavram | Beklenen | Döküm | Eski etiket |
|---|---|---|---|
| animals:7 | Buzağı | Dana! | Dana |
| animals:21 | Yunus | Yunus Balığı | Yunus Balığı |
| animals:23 | Yusufçuk | Helikopter böceği | Helikopter Böceği |
| animals:42 | Karasinek | Sinek | Sinek |
| animals:78 | Kalamar | Mürekkep balığı | Mürekkep Balığı |
| buildings:104 | Plaj | Sahil. | Sahil |
| buildings:121 | Otel | Hotel | Hotel |
| buildings:122 | Tropikal orman | Yağmur ormanı. | Yağmur ormanı |
| buildings:139 | Sinagog | Havra | Havra |
| buildings:174 | Öresund Köprüsü - İsveç ve Danimarka | Öresund Köprüsü Malmö, İsveç ve Danimarka | Öresund Köprüsü Malmö - İsveç ve Danimarka |
| buildings:186 | Tac Mahal - Hindistan | Taj Mahal, Hindistan | Taj Mahal - Hindistan |
| countries:245 | Angola | Angora | Angora |
| countries:326 | Kuzey Makedonya | Makedonya. | Makedonya |
| countries:356 | Filistin Bölgeleri | Filistin | Filistin |
| countries:416 | Çekya | Çek Cumhuriyeti | Çek Cumhuriyeti |
| countries:422 | Marshall Adaları | Marşaladaları | Marşal Adaları |
| space:445 | Uzay kapsülü | Kapsül | Kapsül |
| space:463 | Kuazar | Kua sar | Kuasar |
| farm:513 | Buzağı | Dana! | Dana |
| farm:536 | Çimen | Ot. | Ot |
| farm:544 | Kağnı | Öküz Arabası | Öküz Arabası |
| farm:583 | Yemlik | Yalak. | Yalak |
| flags:595 | Angola | Angora | Angora |
| flags:676 | Kuzey Makedonya | Makedonya. | Makedonya |
| flags:706 | Filistin Bölgeleri | Filistin | Filistin |
| flags:766 | Çekya | Çek Cumhuriyeti | Çek Cumhuriyeti |
| flags:772 | Marshall Adaları | Marşaladaları | Marşal Adaları |
| food:795 | Şekerleme | Şeker | Şeker |
| food:812 | Milkshake | Aromalı süt | Aromalı süt |
| food:814 | Pepperoni | Sucuk | Sucuk |
| fruits_vegetables:841 | Meşe palamudu | Palamut | Palamut |
| fruits_vegetables:861 | Turna yemişi | Kızılcık. | Kızılcık |
| fruits_vegetables:886 | Yer fıstığı | Fıstık | Fıstık |
| fruits_vegetables:898 | Taze soğan | Yeşil soğan. | Yeşil Soğan |
| heavy_equipment:922 | Sıcak hava balonu | Balon | Balon |
| heavy_equipment:928 | Keski | Oymak Alemi | Oyma Kalemi |
| heavy_equipment:929 | İşkence | Menge ne? | Mengene |
| heavy_equipment:933 | Kesici | Keski | Keski |
| heavy_equipment:934 | Yol silindiri | Silindir. | Silindir |
| heavy_equipment:937 | Dron | Drone | Drone |
| heavy_equipment:945 | Yükleyici | Kepçe | Kepçe |
| heavy_equipment:967 | Römork | Dorse | Dorse |
| heavy_equipment:987 | Ölçü çubuğu | Çubuk metre. | Çubuk Metre |
| heavy_equipment:990 | İş kıyafetleri | İş Elbisesi | İş Elbisesi |
| human_body:1027 | Göbek deliği | Göbek | Göbek |
| human_body:1043 | El bileği | Bilek | Bilek |
| human_body:1055 | Serçe parmak | Küçük parmak, serçe parmak. | Küçük parmak / serçe parmak |
| musical_instruments:1063 | Viyolonsel | Çello | Çello |
| musical_instruments:1065 | Konga | Tumba | Tumba |
| home:1141 | Kışlık kulaklık | Kulaklık. | Kulaklık |
| home:1150 | Yer küresi | Dünya | Dünya |
| home:1156 | Kapüşon | Kapşon | Kapşon |
| home:1167 | Kolye | Gerdanlık | Gerdanlık |
| home:1177 | Kurşun kalem | Kalem | Kalem |
| home:1183 | El çantası | Çanta | Çanta |
| home:1190 | Şal | Başörtüsü | Başörtüsü |
| home:1231 | Yıkanma bezi | Elbezi. | El bezi |
| home:1258 | Atlama ipi | İP | İp |
| home:1260 | Sopalı oyuncak at | Oyuncakat | Oyuncak at |
| school:1285 | Elmas | Baklava şekli. | Baklava şekli |
| professions:1404 | İş insanı | İş adamı. | İş adamı |
| professions:1409 | Diş hekimi | Dişçi | Dişçi |
| professions:1431 | Bilim insanı | Bilim adamı. | Bilim adamı |

Bu tablo yeniden üretim emri değildir. Önce anlam farkları dinlenmeli; yazım, eş anlam ve özel ad farkları ayrıca değerlendirilmelidir. Harfler otomatik eşleşse bile dinleme kuyruğunda kalır.

Bütün kayıtlar ve oynatıcılar: `review.html`. Her kayıt için ham sonuç `raw_transcripts.json` içinde saklanır.

Kullanılan araç: [faster-whisper resmî deposu](https://github.com/SYSTRAN/faster-whisper). Tek sözcük, özel ad ve harf dökümleri yanılabilir; belirsiz sonuçlar kesin hata sayılmaz.

8 Eylül düzeltme sonrası not: Bu rapor önceki seslerin tarihsel incelemesidir. Oynatıcılar yedeklenen eski dosyaları açar. Güncel düzeltmeler `../../production/tr-editorial-corrections/README.md` içinde izlenir.
