package com.demarpsoftware.kidswordplus

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.demarpsoftware.kidswordplus.notifications.LearningReminder
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.ZoneId

internal data class NotificationPreferences(
    val pushEnabled: Boolean = false, val pushBusy: Boolean = false, val tokenReady: Boolean = false,
    val pushFailed: Boolean = false, val permissionAllowed: Boolean = false, val permissionAsked: Boolean = false,
    val remindersEnabled: Boolean = false, val reminders: List<LearningReminder> = emptyList(),
    val reminderFailed: Boolean = false, val reminderStorageError: Boolean = false
)

/** Push opt-in and device-local alarms are separate; neither includes child/profile data. */
internal object AppNotifications {
    const val PUSH_CHANNEL = "kwp.push.v1"
    const val REMINDER_CHANNEL = "kwp.learning.v1"
    const val SILENT_REMINDER_CHANNEL = "kwp.learning.silent.v1"
    const val OPEN_LEARNING = "kwp.open-learning"
    const val REMINDER_ACTION = "com.demarpsoftware.kidswordplus.LEARNING_REMINDER"
    private const val PUSH_KEY = "notifications.push.enabled"
    private const val DELETE_KEY = "notifications.push.deletePending"
    private const val LIST_KEY = "learningReminder.entries.v1"
    private const val ENABLED_KEY = "learningReminder.enabled"
    private val mutable = MutableStateFlow(NotificationPreferences())
    val state = mutable.asStateFlow()
    private lateinit var app: Context
    private val prefs get() = app.getSharedPreferences("profile", 0)
    private val manager get() = app.getSystemService(NotificationManager::class.java)
    private val alarms get() = app.getSystemService(AlarmManager::class.java)
    private var token: String? = null
    private var pushGeneration = 0L

    fun initialize(context: Context) {
        app = context.applicationContext
        val raw = prefs.getString(LIST_KEY, null)
        val entries = raw?.let(LearningReminder::decode) ?: if (raw == null) listOf(LearningReminder()) else emptyList()
        if (raw == null) prefs.edit().putString(LIST_KEY, LearningReminder.encode(entries)).commit()
        mutable.value = NotificationPreferences(pushEnabled = prefs.getBoolean(PUSH_KEY, false),
            permissionAsked = prefs.getBoolean("notifications.permissionAsked", false),
            remindersEnabled = prefs.getBoolean(ENABLED_KEY, false), reminders = entries,
            reminderStorageError = raw != null && LearningReminder.decode(raw) == null)
        channels()
        refresh()
        if (!prefs.getBoolean(PUSH_KEY, false)) {
            if (prefs.getBoolean(DELETE_KEY, false)) setPushEnabled(false)
            else FirebaseMessaging.getInstance().isAutoInitEnabled = false
        } else if (!canPost(PUSH_CHANNEL)) FirebaseMessaging.getInstance().isAutoInitEnabled = false
    }
    private fun text(tr: String, en: String) = InterfaceStrings.text(prefs.getString("ui", "tr") ?: "tr", tr, en)
    private fun channels() {
        manager.createNotificationChannel(NotificationChannel(PUSH_CHANNEL, text("Bildirimler", "Notifications"), NotificationManager.IMPORTANCE_DEFAULT))
        manager.createNotificationChannel(NotificationChannel(REMINDER_CHANNEL, text("Öğrenme hatırlatmaları", "Learning reminders"), NotificationManager.IMPORTANCE_DEFAULT))
        manager.createNotificationChannel(NotificationChannel(SILENT_REMINDER_CHANNEL, text("Öğrenme hatırlatmaları", "Learning reminders"), NotificationManager.IMPORTANCE_LOW).apply { setSound(null, null); enableVibration(false) })
    }
    fun canPost(channel: String? = null): Boolean = manager.areNotificationsEnabled() &&
        (Build.VERSION.SDK_INT < 33 || app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        (channel == null || manager.getNotificationChannel(channel)?.importance != NotificationManager.IMPORTANCE_NONE)
    fun permissionRequested() {
        prefs.edit().putBoolean("notifications.permissionAsked", true).apply()
        mutable.update { it.copy(permissionAsked = true) }
    }
    fun refresh() {
        channels()
        mutable.update { it.copy(permissionAllowed = canPost(), tokenReady = token != null && canPost(PUSH_CHANNEL)) }
        reschedule()
        if (prefs.getBoolean(PUSH_KEY, false) && canPost(PUSH_CHANNEL) && token == null && !mutable.value.pushBusy) setPushEnabled(true)
    }
    fun setPushEnabled(enabled: Boolean) {
        if (mutable.value.pushBusy) return
        if (enabled && !canPost(PUSH_CHANNEL)) { refresh(); return }
        val generation = ++pushGeneration
        prefs.edit().putBoolean(PUSH_KEY, enabled).apply()
        mutable.update { it.copy(pushEnabled = enabled, pushBusy = true, pushFailed = false, tokenReady = false) }
        val messaging = FirebaseMessaging.getInstance()
        messaging.isAutoInitEnabled = false
        if (!enabled) {
            token = null
            prefs.edit().putBoolean(DELETE_KEY, true).apply()
            manager.activeNotifications.filter { it.notification.channelId == PUSH_CHANNEL }.forEach { manager.cancel(it.tag, it.id) }
            messaging.deleteToken().addOnCompleteListener { task ->
                if (generation != pushGeneration) return@addOnCompleteListener
                if (task.isSuccessful) prefs.edit().putBoolean(DELETE_KEY, false).apply()
                mutable.update { it.copy(pushBusy = false, pushFailed = !task.isSuccessful) }
            }
        } else {
            fun register() {
                messaging.isAutoInitEnabled = true
                messaging.token.addOnCompleteListener { task ->
                    if (generation != pushGeneration || !prefs.getBoolean(PUSH_KEY, false)) return@addOnCompleteListener
                    token = if (task.isSuccessful) task.result else null
                    mutable.update { it.copy(pushBusy = false, tokenReady = token != null, pushFailed = !task.isSuccessful) }
                }
            }
            if (prefs.getBoolean(DELETE_KEY, false)) messaging.deleteToken().addOnCompleteListener { task ->
                if (generation != pushGeneration) return@addOnCompleteListener
                if (task.isSuccessful) { prefs.edit().putBoolean(DELETE_KEY, false).apply(); register() }
                else mutable.update { it.copy(pushBusy = false, pushFailed = true) }
            } else register()
        }
    }
    fun refreshedToken(value: String) {
        Handler(Looper.getMainLooper()).post {
            if (prefs.getBoolean(PUSH_KEY, false) && canPost(PUSH_CHANNEL)) {
                token = value; mutable.update { it.copy(tokenReady = true, pushFailed = false) }
            } else if (!prefs.getBoolean(PUSH_KEY, false)) setPushEnabled(false)
        }
    }
    fun copyToken(context: Context) {
        if (!BuildConfig.DEBUG) return
        token?.let { context.getSystemService(android.content.ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("FCM", it)) }
    }
    fun setRemindersEnabled(enabled: Boolean) {
        if (enabled && (!canPost(reminderChannel()) || mutable.value.reminderStorageError)) { refresh(); return }
        if (!prefs.edit().putBoolean(ENABLED_KEY, enabled).commit()) { mutable.update { it.copy(reminderFailed = true) }; return }
        mutable.update { it.copy(remindersEnabled = enabled) }
        reschedule()
    }
    fun saveReminder(entry: LearningReminder): Boolean {
        if (mutable.value.reminderStorageError || !entry.valid) return false
        val current = mutable.value.reminders
        val next = if (current.any { it.id == entry.id }) current.map { if (it.id == entry.id) entry else it } else current + entry
        if (next.count { it.enabled } > 64) return false
        return saveEntries(next)
    }
    fun deleteReminder(id: String) { if (!mutable.value.reminderStorageError) saveEntries(mutable.value.reminders.filterNot { it.id == id }) }
    private fun saveEntries(entries: List<LearningReminder>): Boolean {
        if (!prefs.edit().putString(LIST_KEY, LearningReminder.encode(entries)).commit()) { mutable.update { it.copy(reminderFailed = true) }; return false }
        mutable.update { it.copy(reminders = entries) }
        reschedule()
        return true
    }
    private fun reminderChannel() = if (prefs.getBoolean("sound.enabled", true)) REMINDER_CHANNEL else SILENT_REMINDER_CHANNEL
    private fun alarmIntent(id: String, entry: LearningReminder? = null, due: Long = 0): PendingIntent {
        val intent = Intent(app, LearningReminderReceiver::class.java).setAction(REMINDER_ACTION)
            .setData(Uri.parse("kidswordplus://learning-reminder/$id"))
            .putExtra("id", id).putExtra("hour", entry?.hour ?: -1).putExtra("minute", entry?.minute ?: -1).putExtra("due", due)
        return PendingIntent.getBroadcast(app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    fun reschedule() {
        val previous = prefs.getStringSet("learningReminder.scheduled", emptySet())!!.toSet()
        val entries = if (mutable.value.remindersEnabled && !mutable.value.reminderStorageError && canPost(reminderChannel())) mutable.value.reminders.filter { it.enabled }.take(64) else emptyList()
        val keep = entries.map { it.id }.toSet()
        try {
            (previous - keep).forEach { id ->
                alarms.cancel(alarmIntent(id)); manager.cancel("learning.$id", 0)
                prefs.edit().remove("learningReminder.due.$id").commit()
            }
            // Keep the union until the whole update succeeds so failed/obsolete alarms remain cancellable.
            prefs.edit().putStringSet("learningReminder.scheduled", previous + keep).commit()
            entries.forEach { entry ->
                val due = entry.scheduledOccurrence(Instant.now(), ZoneId.systemDefault(),
                    prefs.getLong("learningReminder.due.${entry.id}", 0),
                    prefs.getLong("learningReminder.delivered.${entry.id}", 0)).toEpochMilli()
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, alarmIntent(entry.id, entry, due))
                prefs.edit().putLong("learningReminder.due.${entry.id}", due).commit()
            }
            prefs.edit().putStringSet("learningReminder.scheduled", keep).commit()
            mutable.update { it.copy(reminderFailed = false) }
        } catch (_: Exception) { mutable.update { it.copy(reminderFailed = true) } }
    }
    fun deliverReminder(intent: Intent) {
        val entry = mutable.value.reminders.firstOrNull { it.id == intent.getStringExtra("id") } ?: return
        val due = intent.getLongExtra("due", 0)
        val now = Instant.now()
        val valid = mutable.value.remindersEnabled && entry.enabled && entry.hour == intent.getIntExtra("hour", -1) &&
            entry.minute == intent.getIntExtra("minute", -1) && due > 0 && due <= now.toEpochMilli() &&
            java.time.Duration.between(Instant.ofEpochMilli(due), now) < java.time.Duration.ofDays(1)
        if (valid && prefs.getLong("learningReminder.delivered.${entry.id}", 0) != due && canPost(reminderChannel())) {
            if (show("learning.${entry.id}", reminderChannel(),
                text("Kelime öğrenmeye hazır mısın?", "Ready to learn some words?"),
                text("Yol arkadaşın seni bekliyor. Birlikte yeni kelimeler keşfedelim!", "Your companion is waiting. Let's discover new words together!"), true)) {
                prefs.edit().putLong("learningReminder.delivered.${entry.id}", due).apply()
            }
        }
        reschedule()
    }
    fun scheduleDeveloperReminder() {
        if (!BuildConfig.DEBUG || !mutable.value.remindersEnabled) return
        val entry = mutable.value.reminders.firstOrNull { it.enabled } ?: return
        val due = System.currentTimeMillis() + 2000
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due, alarmIntent(entry.id, entry, due))
    }
    fun receivePush(message: RemoteMessage) {
        if (!prefs.getBoolean(PUSH_KEY, false) || !canPost(PUSH_CHANNEL)) return
        val title = message.data["title"] ?: message.notification?.title ?: "KidsWordPlus"
        val body = message.data["body"] ?: message.notification?.body ?: return
        show("push.${message.messageId ?: java.util.UUID.randomUUID()}", PUSH_CHANNEL, title.take(200), body.take(2000), message.data["destination"] == "learning")
    }
    private fun show(tag: String, channel: String, title: String, body: String, learning: Boolean): Boolean {
        val intent = Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .setData(Uri.parse("kidswordplus://notification/" + Uri.encode(tag))).putExtra(OPEN_LEARNING, learning)
        val tap = PendingIntent.getActivity(app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(app, channel).setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title).setContentText(body).setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(tap).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build()
        return try { manager.notify(tag, 0, notification); true } catch (_: SecurityException) { refresh(); false }
    }
}

class KidsMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) = AppNotifications.refreshedToken(token)
    override fun onMessageReceived(message: RemoteMessage) = AppNotifications.receivePush(message)
}

class LearningReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AppNotifications.REMINDER_ACTION) AppNotifications.deliverReminder(intent)
        else if (intent.action in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) AppNotifications.refresh()
    }
}
