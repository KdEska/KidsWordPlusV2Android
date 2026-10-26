# Speaking settings navigation target

Speaking-practice permission/setup links keep their destination through the parent gate. They open the Settings tab directly and scroll to the speaking section. No consent, feature switch or OS permission is enabled automatically.

Android: MainActivity carries a speech-settings target into ParentDashboard/ParentSettings. SpeakingPracticeSettings uses BringIntoViewRequester after the heading row has been placed; the target row is highlighted. Leaving the parent area clears the target. The existing platform permission button still opens Android's app settings; arbitrary OS settings scroll/focus is not controlled by the app.

iOS: ParentGateView forwards the target into ParentDashboardView, whose initial tab is Settings for this route. ScrollViewReader scrolls to the speech-settings ID; the section is outlined and AccessibilityFocusState targets its title for VoiceOver. Ordinary parent/settings routes retain their default behavior. The iPhone permissions button still opens the system-managed app settings.

## Visible processing feedback

Android model downloads show a centered progress dialog with the real platform percentage when available, or an indeterminate bar when it is not. Continue in background dismisses only the dialog; the operation and inline status continue. Checks and downloads also have a high-contrast inline status card with polite accessibility announcements. Pending/scheduled requests show an explicit pending message and support recheck action, not a fabricated percentage or completion claim. Progress is nullable so an actual reported 0% can be distinguished from unavailable progress.

Android speech support preparation and recognition show a centered processing dialog with cancellation; listening keeps its real five-second timer/waveform. iOS permission requests and recognition show a centered, labelled progress card above the scrollable content. Processing indicators disappear on the operation's actual state change; they do not use a fake completion timer.
