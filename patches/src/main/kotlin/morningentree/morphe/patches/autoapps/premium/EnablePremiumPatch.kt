package morningentree.morphe.patches.autoapps.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks all AutoApps: reports every Play-Billing purchase as owned and every app as licensed.",
) {
    compatibleWith(
        Compatibility(
            name = "AutoApps",
            packageName = "com.joaomgcd.autoappshub",
            appIconColor = 0xFF5722,
            targets = listOf(AppTarget("1.8.13")),
        ),
    )

    execute {
        // Primary gate: the hub's UI marks a project/app locked/unlocked from the Play-Billing
        // purchase check. Every "is purchased" query reduces through this method; force it to
        // report owned so nothing shows as locked.
        PurchasedMultipleResultFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """,
        )

        // Cross-app: other installed AutoApps ask this hub whether their package is licensed.
        // Force every license read to report licensed.
        IsLicensedFingerprint.method.returnEarly(true)
        IsLicensedDefaultFalseFingerprint.method.returnEarly(true)
        IsLicensedDefaultTrueFingerprint.method.returnEarly(true)
    }
}
