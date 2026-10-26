# Firebase Android

Connected project: `kids-word-plus-v2` (existing iOS project).
Android package: `com.demarpsoftware.kidswordplus.v2`.
Firebase app ID: `1:411594943772:android:ce1fbb1cfd68b5c5941ed9`.
`app/google-services.json` was downloaded from Firebase Console for this app.

Uses Google Services 4.5.0, Crashlytics Gradle plugin 3.0.8 and Firebase BoM 33.16.0 (Analytics 22.5.0 / Crashlytics 19.4.4). BoM 35.0.2 failed compilation because its Analytics dependency has Kotlin 2.4 metadata. Keep AGP 8.6.0 / Kotlin 2.1.21 for the installed Android Studio.

The owner explicitly approved sending technical crash reports, screen/game usage events, app/device information and installation identifiers to Google/Firebase on 2026-10-21. Collection is enabled. Advertising ID permission is removed; advertising ID collection, ad personalization and automatic activity screen reporting are disabled. Ad storage/user-data/personalization consent is denied in code. No user ID is set. App events never contain child names, photos, studied words or free text. Handled operation errors discard original messages and causes before recording stack traces. Fatal crash reports are collected by the SDK.

Events: `screen_view`, `tutorial_complete`, `language_pack_installed`, `learning_game_started`, `chess_game_started`; Firebase also records standard automatic lifecycle events. Screen names are fixed codes, not displayed titles. Parent privacy text describes the integration.

## Verification

- Debug APK builds; all 115 unit tests pass.
- Emulator `emulator-5580`: SDK initialization and `screen_view(home)` observed. Google Analytics DebugView confirms 4 screen_view, 1 app_exception and 1 first_open, plus non_personalized_ads=1. Screenshot: `reviews/firebase/analytics-debugview.jpg`.
- Controlled crash: `KidsWordPlus Firebase integration test crash`, caught by Crashlytics; DataTransport endpoint returned HTTP 200. Firebase Crashlytics console shows the named test issue (1 crash, 1 user). Screenshot: `reviews/firebase/crashlytics-test.jpg`.
- Debug-only `FirebaseDiagnosticsActivity` can be invoked explicitly with adb on `sdk_gphone` emulators. It has no launcher entry and is absent from release. `--ez test_crash true` produces a crash; without it, it records a sanitized nonfatal. Do not run on personal devices.
- Lint now runs with AGP 8.6 but reports 13 existing activity-result/Compose producer errors plus 12 warnings. No Firebase integration errors were reported. Full report: `app/build/reports/lint-results-debug.html`.

Release Kotlin/Java compilation passes, but release packaging is blocked by the two existing `InvalidFragmentVersionForActivityResult` lint errors in MainActivity. Release manifest contains no FirebaseDiagnosticsActivity.

No billing plan, database permissions, notifications, iOS code or Firebase project access permissions were changed.
