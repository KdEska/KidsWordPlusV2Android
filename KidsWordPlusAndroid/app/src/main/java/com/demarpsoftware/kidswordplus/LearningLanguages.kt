package com.demarpsoftware.kidswordplus

/** Learning content languages, independent of the app's menu language. */
internal object LearningLanguages {
    val names=linkedMapOf("tr" to "🇹🇷 Türkçe","en" to "🇬🇧 English","fr" to "🇫🇷 Français","de" to "🇩🇪 Deutsch","es" to "🇲🇽 Español (Latinoamérica)","pt" to "🇧🇷 Português (Brasil)","zh" to "🇨🇳 简体中文","ja" to "🇯🇵 日本語","ko" to "🇰🇷 한국어","it" to "🇮🇹 Italiano","hi" to "🇮🇳 हिन्दी","ar" to "🇸🇦 العربية (السعودية)","ru" to "🇷🇺 Русский")
    val codes get()=names.keys
    fun title(code:String)=names[code] ?: code
}
