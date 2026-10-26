# Android back gesture — 2026-10-23

System back and the toolbar share the same navigation dispatch. ChessPageNavigation registers its current back action; game subpages publish their return action. Parent subpages preserve their current destination when closed. The application opts into Android's back-invoked callback bridge.

Validation: debug APK assembled; 135 unit tests passed. On API 34 emulator with gesture navigation enabled, left-edge swipes returned lesson list → subcategories → categories, speaking practice → games, chess introduction → chess menu, and feedback → parent settings. The chess guide reappears when returning to the menu unless its opt-out is selected, as intended. No physical device settings were changed.

Reference: https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture

## In-app gesture correction

System back integration alone did not meet the requested in-app swipe behavior. Added a left-edge (48 dp) horizontal swipe modifier on the main content only when a back button is available. It uses the same current navigation callback as the toolbar, commits once on release after 72 dp, rejects vertical/leftward/multitouch gestures, and leaves central gestures alone. Help sheets disable the underlying swipe; modal dialogs remain separate input windows.

Rebuilt successfully and tested on the API 34 emulator with three-button navigation explicitly enabled (`navigation_mode=0`). App swipes returned lesson list → subcategories → categories and speaking practice → games. Center horizontal and left-edge vertical swipes did not navigate. No physical-device settings were changed.
