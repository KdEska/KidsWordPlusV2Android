package com.demarpsoftware.kidswordplus

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject

/** The single app locale; content language remains independent. Missing keys fall back to English. */
internal object InterfaceStrings {
    val codes=listOf("tr","en","de","fr","es","zh","ja","ko","it","pt","hi","ar","ru")
    var language by mutableStateOf("en")
    private var translations=JSONObject()
    fun load(context:Context) {loadJson(context.assets.open("localization/interface.json").bufferedReader().use {it.readText()})}
    internal fun loadJson(json:String) {translations=JSONObject(json)}
    fun text(locale:String,turkish:String,english:String):String = if(locale=="tr")turkish else resolveInterfaceText(translations.optJSONObject(locale),english)
    fun categoryText(turkish:Boolean,a:String,b:String):String {
        val locale=if(turkish)"tr"else language.takeUnless{it=="tr"} ?: "en"
        if(locale=="tr")return a
        val dictionary=translations.optJSONObject(locale)
        return dictionary?.optString("category.$b")?.takeIf{it.isNotBlank()} ?: resolveInterfaceText(dictionary,b)
    }
    fun render(turkish:Boolean,a:String,b:String,vararg arguments:String):String = renderInterfaceTemplate(text(turkish,a,b),arguments.toList())
    fun named(turkish:Boolean,a:String,b:String,arguments:Map<String,String>):String=renderNamedInterfaceTemplate(text(turkish,a,b),arguments)
    fun text(turkish:Boolean,a:String,b:String)=text(if(turkish)"tr"else language.takeUnless{it=="tr"} ?: "en",a,b)
}

/** One pass: names containing braces or dollar signs remain literal user text. */
internal fun renderInterfaceTemplate(template:String,arguments:List<String>):String =
    Regex("\\{([0-9]+)\\}").replace(template) {match ->
        match.groupValues[1].toIntOrNull()?.let {arguments.getOrNull(it)} ?: match.value
    }

internal fun resolveInterfaceText(dictionary:JSONObject?,english:String):String {
    dictionary?.optString(english)?.takeIf {it.isNotEmpty()}?.let {return it}
    val prefix=if(english.startsWith("✓ "))"✓ "else ""
    val suffix=if(english.endsWith(": "))": "else if(english.endsWith(":"))":"else ""
    val key=english.removePrefix(prefix).removeSuffix(suffix)
    val translated=dictionary?.optString(key)?.takeIf {it.isNotEmpty()} ?: return english
    return prefix+translated+suffix
}

internal fun renderNamedInterfaceTemplate(template:String,arguments:Map<String,String>):String =
    Regex("\\{([a-zA-Z][a-zA-Z0-9_]*)\\}").replace(template) {arguments[it.groupValues[1]] ?: it.value}
