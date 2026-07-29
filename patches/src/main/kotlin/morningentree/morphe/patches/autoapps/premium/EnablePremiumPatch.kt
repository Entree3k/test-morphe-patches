package morningentree.morphe.patches.autoapps.premium

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import morningentree.morphe.util.returnEarly

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks all AutoApps by forcing the hub's license checks to report every app as licensed.",
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
        // Every per-package license read funnels through these three methods; force each
        // to return true so the hub reports (and broadcasts) every app as Licensed.
        IsLicensedFingerprint.method.returnEarly(true)
        IsLicensedDefaultFalseFingerprint.method.returnEarly(true)
        IsLicensedDefaultTrueFingerprint.method.returnEarly(true)
    }
}
