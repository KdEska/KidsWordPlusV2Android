# Android speaking practice

Games → Your turn to speak provides ten-word rounds using the active learning pack. Studied words are preferred; a new round reshuffles candidates. Alphabet entries are excluded, as letter names are ambiguous to word recognizers. Listen, speak, retry and skip are separate actions. Only final recognition results are evaluated. Matching is case/punctuation tolerant while retaining meaningful language distinctions; no pronunciation percentage or confidence score is used.

## Parent and microphone controls

Speaking is disabled by default. Only the gated parent settings can enable it. Android RECORD_AUDIO permission is requested only when the child explicitly taps the speaking action; permission approval continues the same explicit speaking request automatically once the screen is resumed. Leaving the screen, denying permission, opening help or revoking parent consent cancels the request. No microphone use occurs on opening a card or turning on the parent preference. Pausing the activity, leaving the screen, opening help, revoking consent or changing words cancels/destroys the recognizer and clears the transient transcript.

## Local recognition only

API 31 (Android 12) or higher and an available on-device recognition service are required. Only SpeechRecognizer.createOnDeviceSpeechRecognizer is used. There is no generic/network recognizer fallback. API 33+ checks language support before capture. An installed locale is used; a known downloadable-but-not-installed locale is reported unavailable. A service returning ERROR_CANNOT_CHECK_SUPPORT (14), or supplying no language lists, is allowed to try the on-device engine directly. No network recognizer is used. Earlier supported APIs may report missing-language errors at recognition time. No language model is downloaded automatically. Unsupported devices/languages explain the limitation and keep listen/repeat and skip available. Audio capture stops after at most five seconds from the service ready callback; a separate 18-second attempt timeout handles stalled services.

The app stores no audio files and uploads neither audio nor transcripts. Unlike the current iOS recorder flow, this Android implementation uses live recognition and does not offer playback of the child’s own voice. Word reference audio remains available.

## Progress and localization

A finished round counts as a daily game only if at least one word received a genuine matched/different-word result. Matched word IDs count toward daily review; skipped words, permission failures, unsupported languages, no-speech and service errors do not count as matches. A round token prevents duplicate completion credit. Transcripts are never added to persisted progress, diagnostics or Analytics.

Recognition locale follows the learning language (13 locale mappings). Labels and feedback follow the interface language (11 selectable languages; 32 strings checked in all nine additional translated dictionaries). Parent settings and privacy screen explain the behavior.

## Validation

- Debug build passed and all 133 unit tests passed, including multilingual matching (Turkish dotted/dotless I, Japanese voicing/kana, Hindi marks, Arabic hamza, Korean normalization), strict whole-word matching and idempotent progress persistence.
- API 34 phone emulator: games entry, parent-required state, parent gate/settings shortcut and consent toggle, word/listen/skip screen inspected. No microphone capture and no FCM push registration were performed.
- End-to-end recognition with a real microphone and an installed language model requires a supported physical Android device; it has not been verified in the emulator.

References: https://developer.android.com/reference/android/speech/SpeechRecognizer and https://developer.android.com/reference/android/speech/RecognitionSupport

Final phone smoke check also verified Next word advances 1/10 → 2/10 without requesting microphone permission. Screenshots are in `reviews/speaking-practice/`. Full Debug lint remains blocked by the same 11 existing ProduceStateDoesNotAssignValue findings in image loaders; no new speech API errors were reported. Window sizing uses LocalWindowInfo rather than Configuration screen dimensions. The final Debug APK rebuilt successfully after this layout adjustment.

## 2026-10-23 UI and start-flow revision

The speaking card is second (after Listen and Choose), with routing based on stable card identifiers rather than indices. The practice layout follows iOS: count and language badge, progress, word picture/title, two stacked blue/green Listen/Skip actions beside a tall purple microphone action, pinned status notes and result sheets. The main header Back returns to Games. Live listening shows a five-second progress indicator and sound-level bars. Result sheets support retry, skip/advance, reopening and microphone-settings help. Own-voice playback remains unavailable because Android live recognition does not create an audio file.

Permission regression checks verify that the same word request resumes after approval, and cannot start for another word, without consent, in the background or over help. Service support-policy tests cover error 14 and known missing models. All 133 tests passed; the final Debug build passed. Emulated UI checks confirmed second-card ordering, action columns, the parent gate and microphone approval continuing without a second tap. The emulator reported that its Turkish model was not ready; a visible result sheet explained that limitation. No real word match was claimed.

Final phone check confirmed the full two-column action area fits above the pinned notes, and the header Back returns to game selection. Screenshots: `reviews/speaking-practice/ios-layout/`. Localization check passed for 43 strings × nine additional UI languages.

## 2026-10-23 model preparation

Parent Settings → Speaking practice now checks installed, downloadable and pending on-device model lists. It distinguishes ready, missing but downloadable, pending, unsupported, unknown service response, missing microphone permission, unavailable service and Android versions requiring manual setup. The model's localized language/region name is shown. Checks do not start audio capture.

Exact requested locale is preferred. For English only, an already installed English regional model (en-GB preferred among alternatives) can replace a missing en-US model. No other language is substituted; Chinese region/script distinctions stay strict. The same policy selects an available English download target and is shared by setup and live recognition.

Prepare language model is available only in gated parent settings and only when a request is appropriate on Android 13+. The controller independently checks current parent authorization before checking or requesting a download. There is no automatic model download. A visible note explains potential internet use and that preparation never opens the microphone or sends audio. Unsupported child results link to the parent gate/settings rather than initiating a download. Voice-input and permission settings provide manual recovery paths.

Android 14+ uses ModelDownloadListener: progress does not imply readiness, scheduled work stays pending, and only onSuccess marks ready. Android 13 requires an explicit support recheck after requesting preparation. Leaving settings destroys the recognizer/listener; subsequent checks read the system's actual state. Late callbacks and language switches are guarded by generation tokens. No network-based speech recognizer fallback was added.

Validation: Debug build and 135 unit tests passed; new coverage checks English region precedence, cross-language/Chinese-region rejection, downloadable vs pending vs ready, and unknown support. Localization checks cover 58 strings across all nine additional interface dictionaries. Model downloads and real-device recognition remain unverified: tests do not request model network transfers or use a real microphone.

Validation follow-up: parent-gated model controls and the support recheck passed the emulator UI smoke test. The emulator reported Turkish unsupported; no microphone capture or model download was started. Screenshot: `reviews/speaking-practice/model-setup/parent.png`. Final assemble and all 135 unit tests passed. Full lint remains blocked by the 11 existing `ProduceStateDoesNotAssignValue` findings elsewhere; no new API compatibility errors remain in model setup.
