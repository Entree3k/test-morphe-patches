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
        // examples" unlock) observes a MutableStateFlow written by this setter. The
        // RevenueCat/purchase sync calls it with the real status, setting the flow
        // back to false for a non-subscriber — which re-locks those screens even
        // with (1) applied. Force the setter's boolean argument to true so the
        // premium flow (and the backing prefs flag) can never be set false.
        SetUserPremiumFingerprint.method.addInstructions(0, "const/4 p0, 0x1")
    }
}
