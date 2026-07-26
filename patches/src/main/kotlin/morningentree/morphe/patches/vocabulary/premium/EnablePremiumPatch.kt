package morningentree.morphe.patches.vocabulary.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
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
        // 1) Synchronous aggregate gate — ~100 direct call sites (settings and most
        // feature checks). Force it true.
        IsUserPremiumFingerprint.method.returnEarly(true)

        // 2) Reactive premium StateFlow. The word detail / "more examples" screen
        // observes it. At startup the premium initializer sets the flow to the
        // synchronous gate above (now always true); the only thing that later pushes
        // it back to false is this setter, which the RevenueCat sync calls with the
        // real non-subscriber status. Neuter the setter so the flow stays true.
        SetUserPremiumFingerprint.method.addInstructions(0, "return-void")
    }
}
