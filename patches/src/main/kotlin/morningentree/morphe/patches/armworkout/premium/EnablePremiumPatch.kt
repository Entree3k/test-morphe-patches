package morningentree.morphe.patches.armworkout.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.armworkout.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Arm Workout premium — removes ads and unlocks all workouts and plans.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Force the app's cached "purchased / remove-ads" getter to report true. Every
        // feature and ad-gate reads premium status through this single getter, so a
        // return-true here unlocks the whole app.
        PremiumStateGetterFingerprint.method.returnEarly(true)
    }
}
