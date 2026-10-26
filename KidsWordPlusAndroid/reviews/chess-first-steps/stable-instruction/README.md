# Stable instruction area

Android and iOS reserve the height of the tallest translated exercise instruction or success message at the current available width and text size. Only the visible message participates in accessibility. Selection, move and success now share the same area without moving the board or clipping longer translations.

Android phone emulator (1080×1920): screenshot comparison found the top of the first board square at y=794 for selection, move instruction and success. Screenshots are included. Android Debug and iOS Simulator Debug builds passed; localization parity validation passed for 34 guide strings across 13 catalogue languages.
