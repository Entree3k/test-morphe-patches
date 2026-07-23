package morningentree.morphe.patches.vocabulary.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.vocabulary.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Vocabulary Premium",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Force the aggregate premium gate to always report premium. Every
        // premium feature routes through this single method, so overriding it
        // to return true unlocks them all regardless of the RevenueCat
        // entitlement / Supabase flag / local prefs state.
        IsUserPremiumFingerprint.method.returnEarly(true)
    }
}
