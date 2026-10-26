package com.demarpsoftware.kidswordplus

import android.app.TimePickerDialog
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.demarpsoftware.kidswordplus.notifications.LearningReminder
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal enum class NotificationFeature { PUSH, REMINDERS }
internal val LocalNotificationChange = staticCompositionLocalOf<(NotificationFeature, Boolean) -> Unit> { { _, _ -> } }

@Composable internal fun ParentNotificationSettings(state: AppState, model: AppModel) {
    val status by AppNotifications.state.collectAsState()
    val context = LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val change = LocalNotificationChange.current
    fun t(tr: String, en: String) = InterfaceStrings.text(state.ui, tr, en)
    fun authorized() = model.state.value.parentGate.allowsManagement(model.state.value.ready)
    var editFailed by remember { mutableStateOf(false) }
    val canEdit = !state.busy && !status.reminderStorageError
    fun edit(entry: LearningReminder, adding: Boolean) {
        if (!authorized()) return
        val localizedContext = android.view.ContextThemeWrapper(context, 0).apply {
            applyOverrideConfiguration(android.content.res.Configuration(configuration).apply { setLocale(Locale.forLanguageTag(state.ui)) })
            theme.setTo(context.theme)
        }
        TimePickerDialog(localizedContext, { _, hour, minute ->
            if (authorized()) editFailed = !AppNotifications.saveReminder(entry.copy(hour = hour, minute = minute))
        }, entry.hour, entry.minute, android.text.format.DateFormat.is24HourFormat(context)).apply {
            setTitle(t("Hatırlatma saati", "Reminder time"))
            setButton(android.content.DialogInterface.BUTTON_POSITIVE, t(if (adding) "Ekle" else "Tamam", if (adding) "Add reminder" else "Done"), this)
            setButton(android.content.DialogInterface.BUTTON_NEGATIVE, t("Vazgeç", "Cancel")) { _, _ -> }
        }.show()
    }
    SettingsCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingsIcon("notifications")
            Text(t("Bildirimler", "Notifications"), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = KidHeading)
            Switch(status.pushEnabled,
                { change(NotificationFeature.PUSH, it) }, enabled = !state.busy && !status.pushBusy,
                modifier = Modifier.semantics { contentDescription = t("Bildirimler", "Notifications") })
        }
        if (status.pushBusy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (status.pushFailed) {
            Text(t("Bildirim bağlantısı kurulamadı. Bağlantını kontrol edip yeniden dene.", "Could not connect notifications. Check your connection and try again."), color = MaterialTheme.colorScheme.error)
            TextButton({ if (authorized()) AppNotifications.setPushEnabled(status.pushEnabled) }, enabled = !status.pushBusy) { Text(t("Yeniden dene", "Try again")) }
        }
        if (BuildConfig.DEBUG && status.pushEnabled) {
            Text(t(if (status.tokenReady) "Firebase test belirteci hazır" else "Firebase test belirteci bekleniyor", if (status.tokenReady) "Firebase test token ready" else "Waiting for Firebase test token"), style = MaterialTheme.typography.bodySmall)
            TextButton({ if (authorized()) AppNotifications.copyToken(context) }, enabled = status.tokenReady) { Text(t("Test belirtecini kopyala", "Copy test token")) }
        }
    }
    SettingsCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingsIcon("learning-days")
            Text(t("Öğrenme hatırlatması", "Learning reminder"), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = KidHeading)
            Switch(status.remindersEnabled, { change(NotificationFeature.REMINDERS, it) },
                enabled = !state.busy && !status.reminderStorageError,
                modifier = Modifier.semantics { contentDescription = t("Öğrenme hatırlatması", "Learning reminder") })
        }
        Text(t("Birden fazla günlük hatırlatma ekleyebilir, her saati ayrı açıp kapatabilirsin. Ana düğme tüm hatırlatmaları duraklatır. Saatler bu cihazda kalır.",
            "Add multiple daily reminders and turn each time on or off. The main switch pauses all reminders. Times stay on this device."), style = MaterialTheme.typography.bodySmall, color = KidSecondary)
        Text(t("Android, pil tasarrufu nedeniyle hatırlatmaları seçilen saatten daha geç gösterebilir.", "Android may show reminders later than the selected time to save battery."), style = MaterialTheme.typography.bodySmall, color = KidSecondary)
        val sorted = status.reminders.sortedWith(compareBy<LearningReminder> { it.hour }.thenBy { it.minute }.thenBy { it.id })
        if (sorted.isEmpty()) Text(t("Henüz hatırlatma yok. Başlamak için bir saat ekle.", "No reminders yet. Add a time to get started."))
        sorted.forEachIndexed { index, entry ->
            key(entry.id) {
                val title = renderNamedInterfaceTemplate(t("Hatırlatma {n}", "Reminder {n}"), mapOf("n" to (index + 1).toString()))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Switch(entry.enabled, { enabled -> if (authorized()) editFailed = !AppNotifications.saveReminder(entry.copy(enabled = enabled)) }, enabled = canEdit,
                        modifier = Modifier.semantics { contentDescription = title })
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        Text(title, style = MaterialTheme.typography.titleSmall)
                        val pattern = if (android.text.format.DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
                        TextButton({ edit(entry, false) }, enabled = canEdit) {
                            Text(LocalTime.of(entry.hour, entry.minute).format(DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag(state.ui))))
                        }
                    }
                    TextButton({ if (authorized()) AppNotifications.deleteReminder(entry.id) }, enabled = canEdit) {
                        Text(t("Sil", "Delete reminder"))
                    }
                }
            }
        }
        TextButton({ edit(LearningReminder(hour = (java.time.LocalTime.now().hour + 1) % 24), true) }, enabled = canEdit) { Text(t("Hatırlatma ekle", "Add reminder")) }
        if (status.reminderFailed || status.reminderStorageError || editFailed) {
            Text(t("Hatırlatma kaydedilemedi. Yeniden deneyebilirsin.", "The reminder could not be saved. Please try again."), color = MaterialTheme.colorScheme.error)
            if (!status.reminderStorageError) TextButton({ editFailed = false; AppNotifications.refresh() }) { Text(t("Yeniden dene", "Try again")) }
        }
    }
    if ((status.permissionAsked && !status.permissionAllowed) ||
        (status.pushEnabled && !AppNotifications.canPost(AppNotifications.PUSH_CHANNEL)) ||
        (status.remindersEnabled && !AppNotifications.canPost(if (state.soundEnabled) AppNotifications.REMINDER_CHANNEL else AppNotifications.SILENT_REMINDER_CHANNEL))) {
        SettingsCard {
            Text(t("Hatırlatmaları almak için cihaz ayarlarından bildirimlere izin ver.", "Allow notifications in device Settings to receive reminders."))
            TextButton({
                if (authorized()) context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
            }) { Text(t("Android ayarlarını aç", "Open Android Settings")) }
        }
    }
}
