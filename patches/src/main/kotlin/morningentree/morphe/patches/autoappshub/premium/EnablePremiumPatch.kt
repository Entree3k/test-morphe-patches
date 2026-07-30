package morningentree.morphe.patches.autoappshub.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.autoappshub.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks apps in AutoApps as if the full subscription is owned.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        SubscriptionUnlockFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        AutoAppsLicensedFingerprint.method.returnEarly(true)
    }
}
