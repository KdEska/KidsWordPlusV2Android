# Android AdMob integration — 2026-10-23

Existing Play app is being updated, not published as a separate app.

- Existing Play package: `com.demarpsoftware.kidswordplus`
- AdMob app: `ca-app-pub-4061338952988574~8302560809`
- New interstitial: Android V2 - Lesson Break
- Ad unit: `ca-app-pub-4061338952988574/5008676215`
- AdMob frequency cap: 1 impression per user per 5 minutes.
- App-level maximum ad content rating changed from MA to G.

SDK 24.9.0 is used because 25.5.0 Kotlin metadata is incompatible with this project's Kotlin 2.1 compiler.
Debug builds use Google's sample app and interstitial IDs. Debug initialization applies child-directed treatment, under-age-of-consent treatment and G rating before SDK initialization. The existing AD_ID permission removal remains in place.

The activity-owned controller loads test ads and considers showing them only on completion-screen navigation, after five minutes. Missing/failed ads do not block navigation. Loaded ads expire after 55 minutes. Activity destruction clears retained ads.

Release initialization remains off. Production prerequisites:
1. Register/retrieve the Firebase Android app configuration for the existing Play package. Current google-services.json is for `.v2`; do not simply relabel this JSON.
2. Change applicationId to the existing Play package together with matching Firebase configuration; confirm release signing and a versionCode above the published version.
3. Finish privacy disclosures and applicable consent setup before enabling SDK initialization in release.
4. Configure `admobAppId` and `admobInterstitialId` Gradle properties with the above production IDs.
5. Verify actual test ad loading/display on an Android device and inspect the merged manifest before release.

No production ads have been enabled by this change.

The three-lesson minimum was removed at the owner’s request. Eligibility now depends only on the five-minute interval and a ready ad at completion-screen navigation.

## Continuation

Firebase app `KidsWordPlus Android Play` has been registered in the same project for `com.demarpsoftware.kidswordplus`. Its confirmed app ID is `1:411594943772:android:f96bb8910d0c0ce6941ed9`. The config download appeared as completed in Chrome, but browser automation did not return a usable file path. Keep the current package/config intact until the official downloaded config can be copied into the project.

No emulator is currently available: AVD .ini files point to missing AVD directories. A physical device is connected, but no install or data changes were performed on it.

## Samsung tablet test device — 2026-10-24

Connected Samsung SM-P617 tablet is configured programmatically as an AdMob test device in debug builds. Test ID was read from the SDK's own logcat recommendation: `38B5B429C323114DDE1DE9E78CEF091A`. Debug builds also use Google's demo interstitial unit, so ads remain test-only even if the device identifier changes. Release test-device list remains empty.

The debug APK was installed with `install -r` preserving application data. No app uninstall or storage reset was performed.

## Internet requirement

The owner requested internet-only Android use. `InternetRequired` blocks app interaction with a full-screen dialog when Android's default network lacks INTERNET or VALIDATED capability. It monitors default-network changes and rechecks on resume and on Try again. Connection loss stops lesson/chess/game audio. The app composition is retained behind the dialog so navigation state and saved learning progress are preserved. Parent privacy copy now describes the internet requirement. This gate depends on connectivity, not ad availability: no-fill or failed ad requests do not block learning when internet is available.

## Game transitions

Games share the activity's existing interstitial controller with lessons, including the single five-minute cooldown. `LocalGameAdBreak` supplies this callback to completed game actions. It wraps Play again, Change level and Back to games in listening/review, memory and matching result views; New round/Back to games in Word Train and Speaking Practice; completed chess match New game; solved chess task Next/New round and completed opening practice restart. It does not wrap initial Start game, individual word answers, memory flips, or unfinished match actions. Audio is stopped before presentation and no-fill navigates immediately. Debug demo IDs and the tablet test-device configuration remain in effect.
