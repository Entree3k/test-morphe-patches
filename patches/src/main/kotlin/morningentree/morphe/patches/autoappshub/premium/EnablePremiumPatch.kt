package morningentree.morphe.patches.autoappshub.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.patches.autoappshub.shared.Constants
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks every AutoApp in AutoAppsHub as if the full AutoApps subscription is owned.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        // Primary gate: the per-app subscription check the hub's app list reads. Return Boolean.TRUE
        // at entry so every AutoApp is reported as owned (bypasses the null-purchase-data guards and
        // the fullsub/fullsubextra/fullsubyearly ownership lookup). Method returns Ljava/lang/Boolean;
        // so we return the boxed constant rather than a primitive.
        SubscriptionUnlockFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        // Secondary: keep the hub-wide "AutoApps subscription active" flag on, so the FullVersion
        // unlocked event / islicensed broadcast fire and nothing re-locks based on the cached pref.
        AutoAppsLicensedFingerprint.method.returnEarly(true)
    }
}
