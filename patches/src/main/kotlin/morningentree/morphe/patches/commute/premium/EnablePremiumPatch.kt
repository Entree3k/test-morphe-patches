package morningentree.morphe.patches.commute.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.commute.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks the paid premium upgrade of Does Not Commute without a purchase. " +
        "The game's entitlement logic lives in native libcommute.so, which cannot be patched here, " +
        "so if it ever re-verifies the purchase independently this may not take effect.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // "Is premium owned?" -> always true.
        IsProductIdRestoredFingerprint.method.returnEarly(true)

        // Store operation status -> "2" (succeeded), so the native restore flow proceeds to and
        // trusts the ownership check above.
        GetStatusAsStringFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "2"
                return-object v0
            """,
        )
    }
}
