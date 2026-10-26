# Android notifications and learning reminders

Both features are off by default and are managed under Profile → Parent area → Settings.

## Push notifications

- Firebase Cloud Messaging uses the existing Android Firebase configuration.
- Automatic FCM initialization and notification delegation are disabled in the manifest. Registration is enabled only after the parent opts in and Android allows notifications.
- Android 13+ runtime permission is requested by MainActivity so its result survives the parent screen being removed or an Activity recreation.
- Turning push off disables automatic registration, clears displayed push notifications and deletes the token. Failed token deletion is retained and retried. Learning reminders remain independent.
- Token refresh is handled by KidsMessagingService. No token, notification body, child name, profile photo or studied words are logged to Analytics/Crashlytics by this integration.
- Debug builds offer the same parent-only test-token copy control as iOS; raw tokens are not shown on the screen. Release builds do not offer this control.
- This change does not add a remote sender, a token-upload backend, topic subscriptions or a cloud credential. As with the iOS client, an authorized Firebase sender is needed for an actual remote message.

Prefer **data-only FCM messages** when the sender must enforce app-side opt-out handling in all states. The service displays data messages only when push is opted in and permitted. Firebase handles background notification-payload messages itself; data-only avoids that automatic display path, including a notification already in flight when the user opts out.

Example sender payload (documentation only; nothing was sent):

```json
{
  "message": {
    "token": "<authorized test device token>",
    "data": {
      "title": "KidsWordPlus",
      "body": "Time to discover some words!",
      "destination": "learning"
    },
    "android": { "priority": "normal" }
  }
}
```

Only `destination=learning` opens the learning invitation. No arbitrary URL or external destination from a message is launched.

## Learning reminders

- Several daily clock times can be added, edited, individually enabled/disabled and deleted. A main switch pauses all times without removing them. A default 18:00 entry is initially present; the main switch is off.
- Entries have stable UUIDs and are stored in private `profile` preferences under `learningReminder.entries.v1`. Empty lists remain empty. Malformed data is preserved rather than silently overwritten. Up to 64 times can be active.
- AlarmManager uses one immutable PendingIntent per entry and `setAndAllowWhileIdle`. The app requests no exact-alarm permission. Android can delay delivery to save battery; the settings explain this.
- Pending due timestamps are persisted per entry. Foreground refresh and cold-process initialization preserve an undelivered occurrence for up to 24 hours, including delays across midnight, instead of replacing it with tomorrow’s alarm. Editing the time, pausing/deleting an entry or completing delivery invalidates that occurrence.
- Every delivery schedules the next local-calendar occurrence. DST gaps, overlap days and timezone changes are resolved using the device's current ZoneId. Boot, package replacement, clock/timezone changes and foreground refresh restore the schedules.
- Disabling/removing an entry cancels its alarm and delivered notification. The master switch cancels all owned alarms. Old edited intents, disabled/deleted entries, future intents, deliveries delayed by 24 hours or more and repeated deliveries of the same due timestamp are rejected.
- Normal and silent reminder channels respect the app's Sound preference; system channel controls remain authoritative.
- Reminder text uses the selected **interface language**, independently of the learning language. Times remain on the device; reminders do not upload profile/progress data.
- Tapping a reminder opens a companion invitation and starts a suggested real lesson. The cold-process path and ordinary Activity notification intents are supported.

## Validation

- Debug and unsigned Release APK builds passed, including Release's vital lint check.
- 124 unit tests passed. New cases cover independent entry persistence, IDs/flags, malformed/duplicate entries, time validity, next-day rollover, DST gap/overlap and timezone changes. Notification labels/body translations are covered for every additional selectable interface language.
- API 34 phone emulator: permission prompt only after the parent enabled reminders; denial then approval; adding a second time with edited minutes; separate per-entry disabling; master pause/resume preserving both entries; actual background alarm delivery; tapping the actual notification after killing the app process; invitation and real lesson start.
- Final native time-picker labels and controls were checked in Turkish. New Android-specific UI/privacy copy is translated into the other nine selectable interface languages.
- Screenshots: `reviews/notifications/`.
- Full Debug lint remains blocked by 11 existing `ProduceStateDoesNotAssignValue` findings in unrelated Compose image/content loaders. Each reported producer already assigns `value`; none is in the new notification/reminder files. No global suppression or baseline was added. The new code's configuration-read lint error was fixed using LocalConfiguration.
- Remote FCM registration/token retrieval and end-to-end remote delivery were **not executed**. Automatic approval review blocked the emulator registration test because it could send an installation identifier and notification token to Firebase without a separately explicit approval for that metadata egress. No remote message was sent. This is a remaining verification step, not a confirmed delivery result.
- A physical device and reboot-driven delivery have not been separately exercised. The emulator was run without audio, so notification sound was not audited by listening.

## Primary references

- [Firebase Android messaging setup](https://firebase.google.com/docs/cloud-messaging/android/get-started)
- [Firebase foreground/background message behavior](https://firebase.google.com/docs/cloud-messaging/android/receive-messages)
- [Android notification permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission)
- [Android inexact alarms and battery behavior](https://developer.android.com/develop/background-work/services/alarms)
- [AndroidX Fragment releases](https://developer.android.com/jetpack/androidx/releases/fragment) — the 1.8.9 dependency constraint prevents FCM's legacy transitive Fragment from breaking Activity Result validation while remaining compatible with this project's compile SDK.

## Delayed reminder regression (2026-10-22)

A refresh previously calculated the next future clock time unconditionally. Opening the app after the requested time but before Android delivered its inexact alarm could replace the pending alarm with tomorrow’s occurrence. The scheduler now retains the original timestamp until delivered or expired. Unit regression covers refresh/cold-start scheduling inputs, crossing midnight, already-delivered alarms, edited times, expiration and future alarms. Physical-device delivery still requires verification on the affected phone.
