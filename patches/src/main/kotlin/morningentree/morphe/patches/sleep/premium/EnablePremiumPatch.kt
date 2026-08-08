package morningentree.morphe.patches.sleep.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.sleep.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Sleep as Android features locked behind the subscription paywall.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        HasUnlockFingerprint.method.returnEarly(true)
        IsUnlockAckedFingerprint.method.returnEarly(true)
        IsTrialFingerprint.method.returnEarly(false)
    }
}
