import groovy.json.JsonSlurper

plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
val bundleLocalPackages = providers.gradleProperty("bundleLocalPackages").map(String::toBoolean).orElse(true)
val prepareLocalPackageAssets = tasks.register<Sync>("prepareLocalPackageAssets") {
    onlyIf { bundleLocalPackages.get() }
    if (bundleLocalPackages.get()) {
        val referenceRoot = rootProject.file("../language-packs-v2")
        val referenceCatalog = referenceRoot.resolve("catalog.json")
        val appCatalog = project.file("src/debug/assets/packages/catalog.json")
        inputs.files(referenceCatalog, appCatalog)
        val sourceEntries = (JsonSlurper().parse(referenceCatalog) as Map<*, *>)["packages"] as List<*>
        val targetEntries = (JsonSlurper().parse(appCatalog) as Map<*, *>)["packages"] as List<*>
        val targetNames = targetEntries.map { it as Map<*, *> }.associate { it["language"] to it["assetArchive"] }
        sourceEntries.map { it as Map<*, *> }.filter { it["language"] in targetNames }.forEach { entry ->
            val archive = referenceRoot.resolve(entry["localArchive"] as String)
            require(archive.isFile) { "Local development package missing: ${archive.name}" }
            from(archive) { into("packages"); rename { targetNames.getValue(entry["language"]) as String } }
        }
    }
    into(layout.buildDirectory.dir("generated/localPackageAssets"))
}
android {
    namespace = "com.demarpsoftware.kidswordplus"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.demarpsoftware.kidswordplus.v2"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "FEEDBACK_API_URL", groovy.json.JsonOutput.toJson(providers.gradleProperty("feedbackApiUrl").orElse("https://kidswordplus.com/api/feedback.php").get()))
    }
    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
        }
        release {
            // Production activation waits for the matching AdMob app and privacy review.
            manifestPlaceholders["admobAppId"] = providers.gradleProperty("admobAppId").orElse("ca-app-pub-3940256099942544~3347511713").get()
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", groovy.json.JsonOutput.toJson(providers.gradleProperty("admobInterstitialId").orElse("").get()))
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    androidResources { noCompress += "zip" }
    sourceSets.getByName("debug").assets.srcDir(layout.buildDirectory.dir("generated/localPackageAssets"))
    if (!bundleLocalPackages.get()) {
        sourceSets.getByName("debug").assets.exclude("**/*.zip")
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies {
    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.gms:play-services-ads:24.9.0")
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    constraints {
        implementation("androidx.fragment:fragment:1.8.9") {
            because("FCM's legacy transitive Fragment must support Activity Result permission handling")
        }
    }
    implementation("com.google.firebase:firebase-crashlytics")
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation("org.json:json:20240303")
    testImplementation("junit:junit:4.13.2")
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

tasks.withType<Test>().configureEach {
    systemProperty("kwp.packages", rootProject.file("../language-packs-v2").absolutePath)
}

// AGP 8.6 does not infer the generating task from an assets directory provider.
tasks.configureEach {
    if (name.contains("Debug") && (name.contains("Assets") || name.contains("Lint") || name.startsWith("lint"))) {
        dependsOn(prepareLocalPackageAssets)
    }
}
