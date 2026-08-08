package morningentree.morphe.patches.automate.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.automate.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Automate Premium by removing the per-flow block limit, so flows of any " +
        "size run without a purchase.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Always "allowed to run this block" -> unlimited blocks per flow.
        CheckPremiumAllowFingerprint.method.returnEarly(true)
    }
}
