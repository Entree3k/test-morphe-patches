package morningentree.morphe.patches.obdmary.premium

import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.obdmary.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Obd Mary's paid features, including the Diagnostics edition.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        IsFullAppSubscriptionPurchasedFingerprint.method.returnEarly(true)
        IsDiagnosticsEditionOwnedFingerprint.method.returnEarly(true)
        // Inverted: this one reports that the install is still on the free tier.
        IsFreeAppFingerprint.method.returnEarly(false)
    }
}
