# Expanded chess introduction

The guide now uses the available screen dimensions instead of a 240 dp board cap. On iPad it opens full screen instead of a narrow sheet. During exercises the companion and opt-out control give their space to the board; the opt-out remains available on welcome and completion. Scrolling remains available for large accessibility text.

Six guided moves: e2–e4, e4–e5, d2–d3, bishop c1–f4, knight g1–f3, rook a1–a4. Each mini exercise requires selecting the highlighted piece and then its target. After each correct move, the destination remains visible for 1400 ms with a green border and “Great, correct move!” plus a checkmark. Each accepted move plays the existing success effect once, respecting the sound setting. The final move then opens congratulations and the existing destination choices without starting the sound a second time.

All new instructions follow the interface language, with matching translations across 13 chess catalogue languages. Existing “Don’t show again” behaviour is preserved.

Validation: Android Debug build and all 124 unit tests passed; iOS Simulator Debug build passed; localization parity and placeholder validation passed (34 guide keys × 13 languages). Android tablet emulator at 1600×2560, 240 dpi completed all six moves through the congratulations screen; board cells measured 190 px (board approximately 1516 px wide). Screenshots show pawn, bishop, knight, rook and congratulations.

Phone emulator at 1080×1920 with original density: the full board bottom measured 1564 px, within the visible viewport. Selection and destination targeting both verified on the final APK. Screenshots include phone selection and target steps. Push remained disabled throughout UI verification.
