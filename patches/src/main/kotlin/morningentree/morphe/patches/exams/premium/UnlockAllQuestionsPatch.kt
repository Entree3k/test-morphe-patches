package morningentree.morphe.patches.exams.premium

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.rawResourcePatch
import morningentree.morphe.patches.shared.misc.hex.Replacement
import java.io.FileNotFoundException

private const val BUNDLE_PATH = "assets/index.android.bundle"

// One universal patch for the trieudadlovestun exam apps (CompTIA Security+,
// AZ-900, Terraform). They are the same React Native template built from one
// codebase — plain-JavaScript bundles (header "var __BUNDLE_START_TIME__", NOT
// Hermes, so no SHA-1 footer to recompute). Premium is Redux state
// `state.homeScreenReducer.isPremium`, initialized to false in the homeScreenSlice
// as `initialState:{isPremium:!1}`. The UI reads it via useSelector; nothing ever
// dispatches it false (the four `isPremium:!0` occurrences only SET it true on a
// verified purchase). So flipping the single initial `!1` to `!0` makes premium
// true from launch and it stays true — every gated exam question unlocks.
//
// Verified: each of the three bundles contains exactly ONE `isPremium:!1`, always
// in the homeScreenSlice initial state, so the match is unique. The edit is
// equal-length (a single ASCII byte '1' -> '0'), an in-place overwrite.
@Suppress("unused")
val unlockAllQuestionsPatch = rawResourcePatch(
    name = "Unlock all questions",
    description = "Unlocks all exam questions by forcing premium on in the app's React Native bundle.",
) {
    compatibleWith(
        Compatibility(
            name = "CompTIA Security+",
            packageName = "com.trieudadlovestun.comptiasecurityplus",
            // Cosmetic only (patcher UI accent) — shared app colorPrimary.
            appIconColor = 0x023C69,
            targets = listOf(AppTarget("1.0.16")),
        ),
        Compatibility(
            name = "AZ-900",
            packageName = "com.trieudadlovestun.az900ver3",
            appIconColor = 0x023C69,
            targets = listOf(AppTarget("1.0.23")),
        ),
        Compatibility(
            name = "Terraform",
            packageName = "com.trieudadlovestun.terraform",
            appIconColor = 0x023C69,
            targets = listOf(AppTarget("1.0.10")),
        ),
    )

    execute {
        val file = get(BUNDLE_PATH, true)
        if (!file.exists())
            throw FileNotFoundException("React Native bundle not found at: $BUNDLE_PATH")

        val bytes = file.readBytes()

        Replacement(
            "isPremium:!1".toByteArray(Charsets.US_ASCII),
            "isPremium:!0".toByteArray(Charsets.US_ASCII),
            BUNDLE_PATH,
        ).replacePattern(bytes)

        file.writeBytes(bytes)
    }
}
