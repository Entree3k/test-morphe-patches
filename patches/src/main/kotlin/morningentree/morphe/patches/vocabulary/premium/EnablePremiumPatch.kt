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
        // 1) Synchronous aggregate gate. Every direct premium check (settings and
        // most feature gates) routes through this method, so force it true.
        IsUserPremiumFingerprint.method.returnEarly(true)

        // 2) Reactive premium StateFlow. The Compose UI (e.g. the word "more
        // examples" unlock) observes a MutableStateFlow. At startup the premium
        // initializer sets that flow to N0() — now always true via (1). The only
        // thing that later sets it back to false is this setter, which the
        // RevenueCat/purchase sync calls with the real (non-subscriber) status.
        // Neuter the setter so the flow can never be pushed false; it stays at the
        // startup-true value. (return-void is used instead of overwriting the
        // boolean argument: it avoids touching the parameter register and never
        // writes the real prefs flag, so it can't trip a premium-integrity check.)
        SetUserPremiumFingerprint.method.addInstructions(0, "return-void")
    }
}
