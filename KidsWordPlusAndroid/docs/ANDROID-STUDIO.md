# Android Studio uyumluluğu

2026-10-21: “Latest supported version is AGP 8.6.0” hatası için proje şu sürümlere sabitlendi:

- Android Gradle Plugin: 8.6.0
- Gradle wrapper: 8.7
- Kotlin ve Compose derleyici eklentileri: 2.1.21
- compileSdk / targetSdk: 35
- Gradle JDK: 17 veya 21

Android Studio'da `KidsWordPlusAndroid` klasörünü açıp **File > Sync Project with Gradle Files** çalıştırın. Gradle JDK seçiminde Embedded JDK 17/21 kullanılabilir. İlk eşitleme uyumlu araç sürümlerini indirebilir.

Kotlin 2.1 için FEN rok hakları hesabındaki `sumOf` sayı türü belirsizliği, aynı hesabı açık Int başlangıçlı `fold` ile yaparak giderildi. Satranç davranışı değişmedi; mevcut kurallar testlerle doğrulandı.

Kaynaklar: [AGP 8.6 uyumluluğu](https://developer.android.com/build/releases/agp-8-6-0-release-notes), [Kotlin/AGP desteği](https://developer.android.com/build/kotlin-support).

Doğrulama: Debug APK derlendi, 115 test geçti. Lint 31.6.0 ilk bağımlılık indirmesi Google/Maven DNS bağlantı hatasında durdu; lint sonucu henüz doğrulanmadı.
