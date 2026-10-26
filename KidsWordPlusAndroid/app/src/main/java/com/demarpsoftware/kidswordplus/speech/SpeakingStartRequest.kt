package com.demarpsoftware.kidswordplus.speech

/** A microphone request belongs to one visible word and never survives leaving the screen. */
internal data class SpeakingStartRequest(val wordID: String? = null, val waitingForPermission: Boolean = false) {
    fun permissionResult(granted: Boolean) =
        if (waitingForPermission && granted) copy(waitingForPermission=false) else SpeakingStartRequest()
    fun canStart(word: String?, foreground: Boolean, consent: Boolean, helpOpen: Boolean) =
        wordID != null && wordID == word && !waitingForPermission && foreground && consent && !helpOpen
}

internal enum class SpeakingModelAvailability { READY, DOWNLOADABLE, PENDING, UNSUPPORTED, UNKNOWN }
internal data class SpeakingModelPlan(val availability: SpeakingModelAvailability, val locale: String)

internal object SpeakingSupportPolicy {
    fun installedLocale(locales: List<String>, requested: String): String? {
        val tags=locales.map {it.replace('_','-')}
        tags.firstOrNull {it.equals(requested,true)}?.let {return it}
        // English regional models can recognize the same learning-language word set.
        // Never substitute another language or a different Chinese script/region.
        if(requested.substringBefore('-').equals("en",true)) {
            return tags.firstOrNull {it.equals("en-GB",true)}
                ?: tags.filter {it.substringBefore('-').equals("en",true)}.sorted().firstOrNull()
        }
        return null
    }
    fun plan(installed: List<String>, downloadable: List<String>, pending: List<String>, requested: String): SpeakingModelPlan {
        installedLocale(installed,requested)?.let {return SpeakingModelPlan(SpeakingModelAvailability.READY,it)}
        installedLocale(pending,requested)?.let {return SpeakingModelPlan(SpeakingModelAvailability.PENDING,it)}
        installedLocale(downloadable,requested)?.let {return SpeakingModelPlan(SpeakingModelAvailability.DOWNLOADABLE,it)}
        return SpeakingModelPlan(if(installed.isEmpty()&&downloadable.isEmpty()&&pending.isEmpty())
            SpeakingModelAvailability.UNKNOWN else SpeakingModelAvailability.UNSUPPORTED,requested)
    }
    fun tryLocal(installed: List<String>, downloadable: List<String>, locale: String): Boolean =
        installedLocale(installed,locale)!=null || (installed.isEmpty() && downloadable.isEmpty())
    fun queryUnavailable(error: Int) = error == 14
}
