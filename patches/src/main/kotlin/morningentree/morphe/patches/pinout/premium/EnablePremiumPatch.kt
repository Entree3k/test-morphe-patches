package morningentree.morphe.patches.pinout.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.pinout.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks the paid premium upgrade of PinOut without a purchase. The game's " +
        "entitlement logic lives in native code, which cannot be patched here, so if it ever " +
        "re-verifies the purchase independently this may not take effect.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // "Is premium owned?" -> always true.
        IsProductIdRestoredFingerprint.method.returnEarly(true)

        // Store operation status -> "2" (STORE_SUCCEEDED), so the native restore flow proceeds to
        // and trusts the ownership check above.
        GetStatusAsStringFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "2"
                return-object v0
            """,
        )
    }
}
