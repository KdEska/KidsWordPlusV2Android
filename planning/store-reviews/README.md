# Store review requests — iOS and Android

Implemented 2026-10-25. No rating solicitation or actual review was submitted during development.

## Research and interpretation

- [Apple: Ratings and reviews](https://developer.apple.com/design/human-interface-guidelines/ratings-and-reviews): request after meaningful engagement, at a natural stopping point, and avoid pestering. Apple suggests at least one or two weeks between requests; the OS allows at most three presentations in 365 days.
- [Apple: Requesting App Store reviews](https://developer.apple.com/documentation/storekit/requesting-app-store-reviews): request from a completed-process screen after a short pause, with version and engagement gating. Apple's sample completion threshold is explicitly arbitrary, not a statistically proven optimum.
- [Google: In-app review guidance](https://developer.android.com/guide/playcore/in-app-review): wait for sufficient experience, use the untouched native card, do not ask opinion/5-star pre-screening questions. Google's exact quota is private and may change; repeated calls within less than a month may not show anything. API completion does not reveal whether the card was shown or a rating submitted.
- [Apple Review Guidelines 1.3 and 5.6.1](https://developer.apple.com/app-store/review/guidelines/): Kids experiences reserve adult distractions behind a parental gate; use the official review API, not a custom star-rating popup.
- [Peer-reviewed field research, Marketing Letters](https://link.springer.com/article/10.1007/s11002-022-09631-w): two field experiments with over 80,000 website users found effects on paid conversion persisting up to 90 days. This is not a children's app-store rating-frequency trial. It cannot establish an ideal number of lessons, an optimal cooldown or a promised increase in app stars. We therefore do not claim a statistically proven optimal frequency.

## Chosen starting policy (product decisions, not research findings)

Identical on both platforms:

1. At least seven elapsed days since this feature first records local usage. Existing users also get this observation period.
2. At least five completed lessons/games in the stored progress and completion activity on at least three distinct calendar days.
3. At least five further completions since the last opportunity.
4. At least 120 days since the last opportunity; at most three opportunities in a rolling 365-day period; once per marketing version.
5. Observe only completion screens, wait 6.5 seconds for star flights/celebration to finish, and cancel if the screen disappears or the app becomes inactive. Do not show during onboarding, a task, an error, package activity, help, sharing or a new-companion celebration.
6. An optional neutral adult verification step appears first; closing, dismissing or selecting Not now ends this opportunity. A completed three-second adult hold dismisses the gate and then invokes the official system review UI. The gate asks no questions. This is not a satisfaction survey, a custom rating widget or a filter based on whether the user liked the app.
7. Reserve the opportunity persistently before presenting the gate. Even a decline, SDK error or silent OS suppression observes the cooldown/version cap; never retry aggressively.
8. No reward or star change is linked to a rating. No rating value, submitted/reviewed flag, child details or new analytics data is collected. Counts, dates and request-version history stay on-device. The platform handles any voluntarily submitted review.

The counter uses durable completed-lesson totals and completed game-session totals; revisiting the same result cannot increase it. Ordinary learning games, Word Train and speaking round completion screens share the hook. Previously completed lessons can contribute to the lifetime total, but revisiting them does not manufacture a new active day.

## Implementation

- Android: official `com.google.android.play:review:2.0.2`, `ReviewManagerFactory.requestReviewFlow` and `launchReviewFlow`. Local SharedPreferences history; adult gate reuses the existing three-second ParentGate hold rule. Activity focus/background and native-task failures are handled without affecting learning.
- iOS: StoreKit `@Environment(\.requestReview)`, with the native request dispatched after the adult sheet closes. Local Codable history in the app's existing UserDefaults suite. Background, another modal or a new friend cancels/defer this opportunity.
- Policy units have mirrored checks for first-use age, engagement days/count, result deduplication, five new completions, 120-day cooldown, same-version rejection, rolling three/year cap, clock rollback and persistence.

## Validation and release testing

Android Debug build and unit tests; iOS simulator Debug build; Swift policy checks. The app does not pretend that a completed API task means the user rated it.

Actual store UI cannot be guaranteed in a sideloaded APK, TestFlight or simulator. [Google test requirements](https://developer.android.com/guide/playcore/in-app-review/test) require a Play internal test track/internal app sharing and a suitable account with the app in its Play library. Use the official StoreKit development/testing behavior for iOS; the production OS may suppress requests based on quota or user settings. Never submit a store review as part of automated validation.

For post-release evaluation, compare store-provided new-rating counts per active user and retention before/after this release, keeping the version/seasonality caveat. Change one timing threshold at a time. A native-request count is not a displayed-dialog count or a submitted-rating count.

## Development-only manual test

On both apps, open the parent area → Settings → “Mağaza değerlendirmesini test et” / “Test store review”. This row exists only under Android `BuildConfig.DEBUG` / iOS `#if DEBUG`. It bypasses local age, completion, cooldown and version checks without changing review-history counters, dates or attempt reservations. The three-second parent hold remains required. Close/Not now tests cancellation; completing the hold sends a real native review request. A status message reports request/cancellation outcomes and never claims a review was shown or submitted.

Android sideloads may fail or silently complete without a card; the test deliberately does not replace Google's UI with a fake review popup. Store eligibility/quotas still apply. iOS development requests use StoreKit's native testing behavior. TestFlight does not display the review request.
