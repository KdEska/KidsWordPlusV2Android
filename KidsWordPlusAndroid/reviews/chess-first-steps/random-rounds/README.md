# Random progressive first chess steps — 2026-10-23

Both platforms now generate 36 exercises per guide presentation. The three stages each have twelve exercises: movement alone, movement with opponents, captures. Each covers pawn, rook, bishop, knight, queen, king twice. The second pass increases slider distance and, where opponents are present, adds more opponents and a friendly blocker.

These are movement teaching diagrams, not full matches. No opposing king is generated or captured, no castling/en passant/promotion is requested. Each target passes the platform's existing legal-movement checker. Destination squares are not attacked after the move, and kings never start in check. Pawn forward movement and diagonal captures remain distinct.

A round has no repeated board/source/target signature. The previous presentation's complete signature set is persisted and excluded from the next generated round. Android saves the current seed and exclusion set across configuration recreation so rotation retains progress; a new presentation produces a new round. iOS retains the round in view state. Back revisits the same round instead of generating another exercise mid-flow. Opt-out still controls automatic opening only; the manual guide button remains available.

Prompts and phase labels reserve the tallest localized text to keep board placement stable. Black opponent pieces and capture targets use the same board, and the captured piece disappears only after the correct target is tapped. Existing success feedback/audio and final choices remain. Added ten strings in all thirteen catalogue languages with iOS/Android parity.

Validation: Android debug build and 135 unit tests passed. Android generation tests cover 60 seeded rounds (2,160 exercises), all stages/pieces, legal safe destinations, captures, previous-round exclusion, selection gating, completion and back. Swift guide checker covers 60 additional random rounds with equivalent invariants. iOS simulator debug build passed. UI checks are recorded below when complete.

Android phone UI validation: all 36 exercises completed via source selection and target taps, with screenshots of all three stages and the final congratulations screen. The final APK was also reopened and its persisted 36 signatures were checked to be disjoint from the previous presentation. Screenshots are included.

iOS final UI acceptance passed on iPhone SE / iOS 18: skip, auto reopen until opt-out, opt-out persistence, manual reopen, all 36 randomized exercises, final congratulations/return choice, and a manual reopen with a different opening pawn source/target. Result: `/tmp/kwp-v2-build/Logs/Test/Test-KidsWordPlusV2-2026.10.23_01-56-57-+0300.xcresult` (one UI test, zero failures). The final target generator also prevents pawns on either back rank. Standalone Swift validation can be rerun with `sh KidsWordPlusV2/Scripts/check-chess-guide.sh` from the workspace root.
