# Home portal and continuous learning — 26 October 2026

Both native apps now show six shuffled categories and six sample words per app launch. Each sample has artwork, its learning-language text and a listen action using the installed recording. The selection stays stable while switching screens. The bottom card opens the chess world.

Game menus place Word Train fifth. iPad uses three columns. Games and completion star counters use the existing daily-star artwork. Scoreboard and daily tasks retain normal back navigation without a separate return-to-games link.

Lesson completion follows the catalog across topics and categories. Its action label reflects the destination, and the next lesson starts at its first word. Home lesson and lesson-slide next actions align right.

Chess has ten sequential stages of ten tasks per section. Android persists the 100-task order and solved position; iOS persists a 100-task deck and completed IDs. Reopening resumes the current stage; solving the tenth task offers the next ten. After all 100, replay starts a new campaign. Legacy lifetime completions remain unchanged. The iOS king-safety pool was expanded from 80 to 240 valid diagrams, preserving old IDs.

Computer victories show a companion/trophy celebration and a celebration cue, with result and replay actions. iPhone listen-and-choose images clip the fitted bitmap itself so all image corners are rounded.

Validation: Android debug build and 150 unit tests pass, including cross-category continuation, sequential stages, save/restore and no skipping. iOS simulator build passes; ChessCampaignChecks verifies four 100-task decks, ten-task boundaries and Codable restore. ChessPuzzleRoundChecks verifies legal solutions in all three puzzle pools.

## Chess discoveries and interface languages

The chess portal now includes a launch-stable random piece, a legal move sampled from one of the eight existing named openings, and one of 40 original child-friendly strategy tips. The preview uses the actual position after the selected move and highlights source and destination squares. The miniature boards keep chess coordinates left-to-right even in Arabic. The surrounding card follows Arabic reading direction.

Android drag instructions display LocalViewConfiguration.longPressTimeoutMillis in localized seconds. iOS's native lift gesture is described as approximately one second, with the lift preview as the cue to start dragging. Both retain tap-to-select as an alternative. Gesture references: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures and https://developer.apple.com/documentation/swiftui/making-a-view-into-a-drag-source.

The 49 recent interface templates in localization-keys.json have matching translations in both apps for German, French, Spanish, Chinese, Japanese, Korean, Italian, Portuguese, Hindi, Arabic and Russian, alongside the Turkish and English source copy. Arabic and Russian are now offered in the interface selector; core navigation and pack notices were also supplied. Existing older Arabic/Russian screens may still use English fallback where no translation exists. Interpolation and missing-template checks cover the recent feature set in all thirteen interface choices.

Latest validation: Android debug build and all 151 unit tests pass. iOS simulator build and thirteen-language routing/template checks pass. Both catalogs agree on all 49 recent templates. Opening checks cover all eight lines and all 51 legal half-moves available to the preview.


## Forty-secret expansion

The original three secrets are retained and 37 original tips added. The canonical content is chess-secrets.json, with 40 stable IDs and all 520 texts across thirteen languages. Run tools/generate_chess_secrets.py to synchronize native Turkish/English copy and both translation catalogs; tools/check_chess_secrets.py validates the full set. CHESS-SECRETS-TR.md lists the Turkish copy for review.

A persisted local shuffle bag exhausts all 40 IDs before refilling and avoids repeating the final ID at the next cycle boundary. Selection is stable for the app process and independent of interface language. Stale IDs and duplicate stored entries are removed on restore.

Expansion validation: both native builds pass; all 155 Android unit tests pass. Swift shuffle-bag checks cover 20 complete cycles, save/restore, cycle-boundary repeats and stale-ID cleanup. The content checker verifies all 520 texts and generated Turkish/English copies against both translation catalogs.

## Victory replay fix and portal destinations

Victory announcements now use a persisted set of consumed game IDs plus an observed ongoing-to-won transition. Restoring an old win consumes it silently. Menu entry, returning to a finished match, and undo/re-win in the same game cannot reopen the celebration. Each genuinely new visible win can celebrate once. Both platforms have focused regression checks for these transitions.

The portal piece card opens the pieces library; the opening preview opens the openings library. The orange world CTA retains the world destination. Independent links avoid nested navigation controls. Android direct entry initializes the corresponding page and bypasses the introductory guide for that entry.
