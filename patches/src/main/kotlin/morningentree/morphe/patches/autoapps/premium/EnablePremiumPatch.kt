package morningentree.morphe.patches.autoapps.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks AutoApps as if every app as licensed.",
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
        PurchasedMultipleResultFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """,
        )

        IsLicensedFingerprint.method.returnEarly(true)
        IsLicensedDefaultFalseFingerprint.method.returnEarly(true)
        IsLicensedDefaultTrueFingerprint.method.returnEarly(true)
    }
}
