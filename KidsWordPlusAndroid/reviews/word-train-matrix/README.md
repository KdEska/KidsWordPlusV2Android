# Word Train: variable wagons and serpentine departure

Panda's Bag is now Word Train on both platforms. The existing BAG progress token is retained so recorded activity remains compatible.

The introduction and completed-round screen offer a persisted 3–12 wagon setting. The engine clamps requested counts to that range and creates exactly that many questions. Correct answers fill sequential wagons with the picture and learning-language word. A three-column grid snakes left-to-right, then right-to-left, up to four rows; the locomotive leads above the first wagon. Row heights adapt to the phone viewport.

After the last correct answer, the game waits briefly for feedback and automatically completes. Each wagon follows the same serpentine path behind the locomotive and exits to the right. Completion controls appear after departure. Reduced motion skips travel. Android retains the once-only departure flag over recreation; iOS cancels and finishes an interrupted departure on return. Completion rewards retain the existing persisted deduplication.

Validation:
- Android debug build and 141 unit tests passed, including count bounds, all supported lengths, gating and completion.
- Swift randomized engine/progress checks passed, including 3–12 wagon bounds.
- iOS small-screen UI test passed: choose 3, wrong-answer retry, fill three wagons, automatic departure, choose 12, new round showing 1/12 and the complete matrix.
- Android emulator UI test passed for 3 wagons, automatic departure, completion and 12-wagon restart.
- Final iOS simulator build passed.
- New interface labels cover every interface dictionary on both platforms.

Screenshots document both configurations and departure. Android push notifications remained disabled during local emulator testing.
